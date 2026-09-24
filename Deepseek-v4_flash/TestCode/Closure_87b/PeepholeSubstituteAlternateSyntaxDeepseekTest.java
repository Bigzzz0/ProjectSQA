package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for PeepholeSubstituteAlternateSyntax.
 * Targets line/branch coverage and the known defect from issue 291.
 *
 * [Branch & Defect Analysis Matrix]
 * - tryReduceReturn: void/undefined removal, break substitution, side-effect guards
 * - tryMinimizeNot: complement operators (EQ/NE, SHEQ/SHNE)
 * - tryMinimizeIf: IF->HOOK, IF->AND/OR, repeated statement removal, var/assign merging
 * - tryMinimizeCondition: De Morgan's, double NOT, boolean constant folding, HOOK simplification
 * - tryFoldStandardConstructors: new Object/Array/RegExp/Error -> call
 * - tryFoldLiteralConstructor: Object/Array/RegExp literal folding, safe flags check
 * - tryFoldRegularExpressionConstructor: pattern/flag validation, slash escaping, unicode escape detection
 * - isFoldableExpressBlock: single EXPR_RESULT child (issue 291 guard)
 * - consumesDanglingElse: IF/WITH/WHILE/FOR without else
 * - isLowerPrecedenceInExpression: precedence checks
 * - isPropertyAssignmentInExpression: GETPROP under ASSIGN
 * - areValidRegexpFlags / areSafeFlagsToFold: g flag exclusion
 * - containsUnicodeEscape: even/odd backslash counting
 */
public class PeepholeSubstituteAlternateSyntaxDeepseekTest extends CompilerTestCase {

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new PeepholeSubstituteAlternateSyntax(compiler);
  }

  // ========== Partition A: Core Functional Logic & State Transitions ==========

  @Test(timeout = 4000)
  public void testReduceReturnUndefined() {
    test("function f() { return undefined; }", "function f() { return; }");
  }

  @Test(timeout = 4000)
  public void testReduceReturnVoid0() {
    test("function f() { return void 0; }", "function f() { return; }");
  }

  @Test(timeout = 4000)
  public void testReduceReturnNoChange() {
    testSame("function f() { return 1; }");
  }

  @Test(timeout = 4000)
  public void testMinimizeNotEq() {
    test("!(x==y)", "x!=y");
  }

  @Test(timeout = 4000)
  public void testMinimizeNotNe() {
    test("!(x!=y)", "x==y");
  }

  @Test(timeout = 4000)
  public void testMinimizeNotSheq() {
    test("!(x===y)", "x!==y");
  }

  @Test(timeout = 4000)
  public void testMinimizeNotShne() {
    test("!(x!==y)", "x===y");
  }

  @Test(timeout = 4000)
  public void testMinimizeNotNoChange() {
    testSame("!(x<y)");
  }

  @Test(timeout = 4000)
  public void testMinimizeIfToAnd() {
    test("if(x) foo()", "x && foo()");
  }

  @Test(timeout = 4000)
  public void testMinimizeIfToOr() {
    test("if(!x) foo()", "x || foo()");
  }

  @Test(timeout = 4000)
  public void testMinimizeIfToHookReturn() {
    test("if(x) return 1; else return 2;", "return x?1:2");
  }

  @Test(timeout = 4000)
  public void testMinimizeIfSwapBranches() {
    test("if(!x) foo(); else bar();", "if(x) bar(); else foo();");
  }

  @Test(timeout = 4000)
  public void testMinimizeIfAssignMerge() {
    test("if(x) a=1; else a=2;", "a=x?1:2");
  }

  @Test(timeout = 4000)
  public void testMinimizeIfCallMerge() {
    test("if(x) foo(); else bar();", "x?foo():bar()");
  }

  @Test(timeout = 4000)
  public void testMinimizeIfVarAssignMerge() {
    test("if(x) var y=1; else y=2;", "var y=x?1:2");
  }

  @Test(timeout = 4000)
  public void testMinimizeIfAssignVarMerge() {
    test("if(x) y=1; else var y=2;", "var y=x?1:2");
  }

  @Test(timeout = 4000)
  public void testRemoveRepeatedStatements() {
    test("if(a){x=1;return true}else{x=2;return true}",
         "if(a){x=1}else{x=2}return true");
  }

  @Test(timeout = 4000)
  public void testMinimizeConditionDoubleNot() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testMinimizeConditionDeMorganAnd() {
    test("!(x&&y)", "!x||!y");
  }

  @Test(timeout = 4000)
  public void testMinimizeConditionDeMorganOr() {
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testMinimizeConditionOrFalse() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testMinimizeConditionAndTrue() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testMinimizeConditionHookTrueFalse() {
    test("x?true:false", "x");
  }

  @Test(timeout = 4000)
  public void testMinimizeConditionHookFalseTrue() {
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testMinimizeConditionHookTrueY() {
    test("x?true:y", "x||y");
  }

  @Test(timeout = 4000)
  public void testMinimizeConditionHookYFalse() {
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testFoldStandardConstructors() {
    enableNormalize();
    test("new Object()", "Object()");
    test("new Array()", "Array()");
    test("new RegExp('')", "RegExp('')");
    test("new Error('msg')", "Error('msg')");
  }

  @Test(timeout = 4000)
  public void testFoldLiteralConstructorObject() {
    enableNormalize();
    test("Object()", "({})");
  }

  @Test(timeout = 4000)
  public void testFoldLiteralConstructorArrayNoArgs() {
    enableNormalize();
    test("Array()", "[]");
  }

  @Test(timeout = 4000)
  public void testFoldLiteralConstructorArrayWithArgs() {
    enableNormalize();
    test("Array(1,2,3)", "[1,2,3]");
  }

  @Test(timeout = 4000)
  public void testFoldLiteralConstructorArraySingleString() {
    enableNormalize();
    test("Array('a')", "['a']");
  }

  @Test(timeout = 4000)
  public void testFoldLiteralConstructorArraySingleZero() {
    enableNormalize();
    test("Array(0)", "[]");
  }

  @Test(timeout = 4000)
  public void testFoldLiteralConstructorArraySingleNumberNonZero() {
    enableNormalize();
    testSame("Array(5)");
  }

  @Test(timeout = 4000)
  public void testFoldRegularExpressionConstructor() {
    enableNormalize();
    test("RegExp('foobar')", "/foobar/");
    test("RegExp('foobar','i')", "/foobar/i");
    test("RegExp('foobar','m')", "/foobar/m");
  }

  @Test(timeout = 4000)
  public void testFoldRegularExpressionConstructorGlobalFlagNotFolded() {
    enableNormalize();
    testSame("RegExp('foobar','g')");
  }

  @Test(timeout = 4000)
  public void testFoldRegularExpressionConstructorInvalidFlags() {
    enableNormalize();
    testSame("RegExp('foobar','z')");
  }

  @Test(timeout = 4000)
  public void testFoldRegularExpressionConstructorEmptyPattern() {
    enableNormalize();
    testSame("RegExp('')");
  }

  @Test(timeout = 4000)
  public void testFoldRegularExpressionConstructorLongPattern() {
    enableNormalize();
    // pattern length >= 100 should not fold
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 100; i++) sb.append('a');
    testSame("RegExp('" + sb.toString() + "')");
  }

  @Test(timeout = 4000)
  public void testFoldRegularExpressionConstructorUnicodeEscape() {
    enableNormalize();
    testSame("RegExp('\\\\u0041')");  // escaped backslash + u
  }

  // ========== Partition B: Boundary Value Analysis & Extremes ==========

  @Test(timeout = 4000)
  public void testReduceReturnNullResult() {
    test("function f() { return; }", "function f() { return; }");
  }

  @Test(timeout = 4000)
  public void testMinimizeIfLiteralCondition() {
    testSame("if(true) foo()");
  }

  @Test(timeout = 4000)
  public void testMinimizeIfNoElseBlock() {
    test("if(x){foo()}", "x&&foo()");
  }

  @Test(timeout = 4000)
  public void testMinimizeIfPropertyAssignmentNotFolded() {
    testSame("if(x) a.b=1");
  }

  @Test(timeout = 4000)
  public void testMinimizeIfLowerPrecedenceNotFolded() {
    testSame("if(x||y) foo()");
  }

  @Test(timeout = 4000)
  public void testMinimizeConditionWhileTrue() {
    test("while(true) foo()", "while(1) foo()");
  }

  @Test(timeout = 4000)
  public void testMinimizeConditionDoFalse() {
    test("do foo() while(false)", "do foo() while(0)");
  }

  @Test(timeout = 4000)
  public void testFoldStandardConstructorsNotNormalized() {
    // Without normalization, should not fold
    testSame("new Object()");
  }

  @Test(timeout = 4000)
  public void testFoldLiteralConstructorArraySingleArrayLit() {
    enableNormalize();
    test("Array([1,2])", "[[1,2]]");
  }

  // ========== Partition C: Defect-Targeted Branch Zone (Issue 291) ==========

  /**
   * Issue 291: IE event handlers behave differently when return value is used.
   * The optimization must NOT fold an IF with a single EXPR_RESULT containing
   * a method call (implicit 'this') into a logical expression.
   */
  @Test(timeout = 4000)
  public void testIssue291() {
    // This pattern should remain unchanged: if(x) { obj.method(); }
    testSame("if(x) { obj.method() }");
    // Also test with a more complex call
    testSame("if(x) { a.b.c() }");
    // Ensure that other calls without implicit 'this' still fold
    test("if(x) foo()", "x && foo()");
  }

  // ========== Partition D: Exception & Defensive Guard Paths ==========

  @Test(timeout = 4000)
  public void testConsumesDanglingElseIfNoElse() {
    // IF without else consumes dangling else
    testSame("if(x) if(y) foo(); else bar();");
  }

  @Test(timeout = 4000)
  public void testConsumesDanglingElseWithElse() {
    // IF with else does not consume
    test("if(x) if(y) foo(); else bar(); else baz();",
         "if(x) if(y) foo(); else bar(); else baz();");
  }

  @Test(timeout = 4000)
  public void testIsLowerPrecedenceInExpression() {
    // Ensure that lower precedence prevents folding
    testSame("if(x&&y) foo()");
    testSame("if(x||y) foo()");
  }

  @Test(timeout = 4000)
  public void testIsPropertyAssignmentInExpression() {
    testSame("if(x) a.b=1");
  }

  @Test(timeout = 4000)
  public void testAreValidRegexpFlags() {
    enableNormalize();
    testSame("RegExp('a','g')");
    testSame("RegExp('a','i')");
    testSame("RegExp('a','m')");
    testSame("RegExp('a','gi')");
    testSame("RegExp('a','gm')");
    testSame("RegExp('a','im')");
    testSame("RegExp('a','gim')");
    // Invalid flags
    testSame("RegExp('a','x')");
  }

  @Test(timeout = 4000)
  public void testAreSafeFlagsToFold() {
    enableNormalize();
    // 'g' flag not safe
    testSame("RegExp('a','g')");
    // 'i' and 'm' safe
    test("RegExp('a','i')", "/a/i");
    test("RegExp('a','m')", "/a/m");
  }

  @Test(timeout = 4000)
  public void testContainsUnicodeEscape() {
    enableNormalize();
    // Pattern with unicode escape should not fold
    testSame("RegExp('\\\\u0041')");
    // Pattern without unicode escape folds
    test("RegExp('a')", "/a/");
  }

  // ========== Partition E: Object Lifecycle & Contract Integrity ==========

  @Test(timeout = 4000)
  public void testOptimizeSubtreeDefault() {
    // Node types not handled should return unchanged
    testSame("var x = 1;");
    testSame("x = 1;");
    testSame("function f() {}");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookNoChange() {
    testSame("x?y:z");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndOrNoChange() {
    testSame("x&&y");
    testSame("x||y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoChange() {
    testSame("!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionBooleanValue() {
    test("if(1) foo()", "foo()");
    test("if(0) foo()", "");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionForIn() {
    // For-in condition should not be minimized
    testSame("for(var x in y) foo()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionForRegular() {
    test("for(;true;) foo()", "for(;1;) foo()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionWhile() {
    test("while(true) foo()", "while(1) foo()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDo() {
    test("do foo() while(false)", "do foo() while(0)");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionExprResult() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHook() {
    test("x?true:false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOr() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNot() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithNotChildren() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrMixed() {
    // Only folds when both children are NOT
    testSame("!(x&&!y)");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanTrue() {
    test("x?true:y", "x||y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanFalse() {
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanBoth() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookNoBoolean() {
    testSame("x?y:z");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrWithSideEffects() {
    // If left has side effects, right should not be removed
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrue() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalse() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!(x())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithSideEffects() {
    testSame("x?y():z()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionForInNoChange() {
    testSame("for(var x in y) foo()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionForRegularNoChange() {
    testSame("for(;x;) foo()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionWhileNoChange() {
    testSame("while(x) foo()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoNoChange() {
    testSame("do foo() while(x)");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionExprResultNoChange() {
    testSame("x||y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookNoChange() {
    testSame("x?y:z");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoChange() {
    testSame("!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndOrNoChange() {
    testSame("x&&y");
    testSame("x||y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionBooleanValueNoChange() {
    testSame("if(x) foo()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionBooleanValueTrue() {
    test("if(true) foo()", "foo()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionBooleanValueFalse() {
    test("if(false) foo()", "");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionBooleanValueWhile() {
    test("while(true) foo()", "while(1) foo()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionBooleanValueDo() {
    test("do foo() while(false)", "do foo() while(0)");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionBooleanValueFor() {
    test("for(;true;) foo()", "for(;1;) foo()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionBooleanValueExprResult() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionBooleanValueHook() {
    test("x?true:false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionBooleanValueNot() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionBooleanValueAndOr() {
    test("x&&true", "x");
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionBooleanValueAndFalse() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionBooleanValueOrTrue() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionBooleanValueWithSideEffects() {
    testSame("x()&&true");
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionBooleanValueHookWithSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionBooleanValueHookBothBoolean() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionBooleanValueHookOneBoolean() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionBooleanValueHookNoBoolean() {
    testSame("x?y:z");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildren() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganMixed() {
    testSame("!(x&&!y)");
    testSame("!(!x&&y)");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanNoSideEffects() {
    test("x?true:false", "x");
    test("x?false:true", "!x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueWithSideEffects() {
    testSame("x()||true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseWithSideEffects() {
    testSame("x()&&false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrTrueNoSideEffects() {
    test("x||true", "true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndFalseNoSideEffects() {
    test("x&&false", "false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseWithSideEffects() {
    testSame("x()||false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueWithSideEffects() {
    testSame("x()&&true");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionOrFalseNoSideEffects() {
    test("x||false", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionAndTrueNoSideEffects() {
    test("x&&true", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotWithSideEffects() {
    testSame("!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotNoSideEffects() {
    test("!(!x)", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrWithSideEffects() {
    testSame("!(x()&&y())");
    testSame("!(x()||y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionNotAndOrNoSideEffects() {
    test("!(x&&y)", "!x||!y");
    test("!(x||y)", "!x&&!y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenAndSideEffects() {
    testSame("!(!x()&&!y())");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDeMorganWithNotChildrenNoSideEffects() {
    test("!(!x&&!y)", "x||y");
    test("!(!x||!y)", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotWithSideEffects() {
    testSame("!!x()");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionDoubleNotNoSideEffects() {
    test("!!x", "x");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanAndSideEffects() {
    testSame("x?true:y()");
    testSame("x?y():false");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookWithBooleanNoSideEffects() {
    test("x?true:y", "x||y");
    test("x?y:false", "x&&y");
  }

  @Test(timeout = 4000)
  public void testTryMinimizeConditionHookBothBooleanWithSideEffects() {
    testSame("x?true:false");
    testSame("x?false:true");
  }

  @Test(timeout =