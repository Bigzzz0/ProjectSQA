package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Targets: PeepholeReplaceKnownMethods.java
 *
 * 1. String.prototype.toLowerCase() and toUpperCase()
 *    - Branch: 0 args -> folds to lower/upper string with ROOT_LOCALE.
 *    - Branch: >=1 args -> does not fold.
 *    - Boundary: empty string, special unicode characters, case invariance.
 *
 * 2. String.prototype.indexOf() and lastIndexOf()
 *    - Branch: 1 arg (string) -> computes index / lastIndexOf.
 *    - Branch: 2 args (string, number) -> computes with fromIndex.
 *    - Branch: >2 args or non-number 2nd arg -> discarded (no fold).
 *    - Branch: non-string target or non-immutable search value -> no fold.
 *    - Boundary: search not found (-1), negative fromIndex, out-of-range fromIndex.
 *
 * 3. String.prototype.substr() and substring()
 *    - Branch: 1 arg (start) vs 2 args (start, length/end).
 *    - Branch: >2 args -> discarded (no fold).
 *    - Boundary: start/length/end negative, start > end, out-of-bounds (start + length > len).
 *
 * 4. String.prototype.charAt() and charCodeAt()
 *    - Branch: 1 numeric arg -> char string or char code.
 *    - Branch: >1 args -> discarded (no fold).
 *    - Boundary: index < 0, index >= length (returns original node, no fold).
 *
 * 5. parseInt() and parseFloat()
 *    - Branch: AST normalized vs not normalized.
 *    - Branch: parseInt with numeric argument (radix 0, 10 vs other radix).
 *    - Branch: parseInt with string arg, radix 0/16 with "0x" prefix.
 *    - Branch: parseInt with string arg, leading "0" in non-ES5 mode vs ES5.
 *    - Branch: parseFloat with numeric argument vs string argument.
 *    - Branch: normalizeNumericString check (leading/trailing zeros, match original string).
 *    - Boundary: invalid radix (< 0, == 1, > 36), non-integer radix, non-numeric strings.
 *
 * 6. Array.prototype.join()
 *    - Branch: empty array -> ""
 *    - Branch: single element -> folded string or coerced string (+ "")
 *    - Branch: multiple elements -> merged adjacent literals with join separator.
 *    - Branch: size tradeoff (foldedSize > originalSize -> bail).
 *    - Defect-Targeted (Defects4J):
 *      testStringJoinAdd / testNoStringJoin: joining array elements with non-string literals
 *      or folding size comparison issues in Array.join folding.
 */
public class PeepholeReplaceKnownMethodsGeminiTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
  }

  private Node parseAndFold(String js) {
    return parseAndFold(js, true);
  }

  private Node parseAndFold(String js, boolean normalized) {
    Node root = compiler.parseTestCode(js);
    if (normalized) {
      compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);
    }
    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(
        compiler, new PeepholeReplaceKnownMethods());
    pass.process(null, root);
    return root;
  }

  private String toSource(Node root) {
    return compiler.toSource(root);
  }

  private void testFold(String js, String expectedJs) {
    Node root = parseAndFold(js);
    Node expectedRoot = compiler.parseTestCode(expectedJs);
    assertEquals(toSource(expectedRoot), toSource(root));
  }

  private void testSame(String js) {
    testFold(js, js);
  }

  // =========================================================================
  // Partition A: Core Functional Logic - String Methods
  // =========================================================================

  @Test(timeout = 4000)
  public void testStringToLowerCase() {
    testFold("x = 'HELLO WORLD'.toLowerCase()", "x = 'hello world'");
    testFold("x = 'already lower'.toLowerCase()", "x = 'already lower'");
    testFold("x = ''.toLowerCase()", "x = ''");
  }

  @Test(timeout = 4000)
  public void testStringToUpperCase() {
    testFold("x = 'hello world'.toUpperCase()", "x = 'HELLO WORLD'");
    testFold("x = 'ALREADY UPPER'.toUpperCase()", "x = 'ALREADY UPPER'");
    testFold("x = ''.toUpperCase()", "x = ''");
  }

  @Test(timeout = 4000)
  public void testStringIndexOf() {
    testFold("x = 'abcdef'.indexOf('c')", "x = 2");
    testFold("x = 'abcdef'.indexOf('z')", "x = -1");
    testFold("x = 'abcdefabcdef'.indexOf('c', 4)", "x = 8");
    testFold("x = 'abcdef'.indexOf('bc', 1)", "x = 1");
  }

  @Test(timeout = 4000)
  public void testStringLastIndexOf() {
    testFold("x = 'abcdefabcdef'.lastIndexOf('c')", "x = 8");
    testFold("x = 'abcdef'.lastIndexOf('z')", "x = -1");
    testFold("x = 'abcdefabcdef'.lastIndexOf('c', 5)", "x = 2");
    testFold("x = 'abcdef'.lastIndexOf('bc', 3)", "x = 1");
  }

  @Test(timeout = 4000)
  public void testStringSubstr() {
    testFold("x = 'abcdef'.substr(2)", "x = 'cdef'");
    testFold("x = 'abcdef'.substr(2, 3)", "x = 'cde'");
    testFold("x = 'abcdef'.substr(0, 6)", "x = 'abcdef'");
    testFold("x = 'abcdef'.substr(0, 0)", "x = ''");
  }

  @Test(timeout = 4000)
  public void testStringSubstring() {
    testFold("x = 'abcdef'.substring(2)", "x = 'cdef'");
    testFold("x = 'abcdef'.substring(2, 4)", "x = 'cd'");
    testFold("x = 'abcdef'.substring(0, 6)", "x = 'abcdef'");
    testFold("x = 'abcdef'.substring(3, 3)", "x = ''");
  }

  @Test(timeout = 4000)
  public void testStringCharAt() {
    testFold("x = 'abcdef'.charAt(0)", "x = 'a'");
    testFold("x = 'abcdef'.charAt(2)", "x = 'c'");
    testFold("x = 'abcdef'.charAt(5)", "x = 'f'");
  }

  @Test(timeout = 4000)
  public void testStringCharCodeAt() {
    testFold("x = 'ABC'.charCodeAt(0)", "x = 65");
    testFold("x = 'ABC'.charCodeAt(1)", "x = 66");
    testFold("x = 'ABC'.charCodeAt(2)", "x = 67");
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis & Invalid Invocations
  // =========================================================================

  @Test(timeout = 4000)
  public void testStringToCaseWithArguments() {
    // Methods with arguments should not fold
    testSame("x = 'hello'.toLowerCase('extra')");
    testSame("x = 'hello'.toUpperCase('extra')");
  }

  @Test(timeout = 4000)
  public void testStringIndexOfWithExtraArgs() {
    // 3 arguments -> discard and don't fold
    testSame("x = 'abcdef'.indexOf('c', 1, 'extra')");
    testSame("x = 'abcdef'.lastIndexOf('c', 4, 'extra')");
  }

  @Test(timeout = 4000)
  public void testStringIndexOfNonNumericFromIndex() {
    testSame("x = 'abcdef'.indexOf('c', 'foo')");
    testSame("x = 'abcdef'.lastIndexOf('c', 'foo')");
  }

  @Test(timeout = 4000)
  public void testStringSubstrBoundaries() {
    // Negative start or length
    testSame("x = 'abcdef'.substr(-1, 2)");
    testSame("x = 'abcdef'.substr(1, -1)");
    // Length exceeds boundary
    testSame("x = 'abcdef'.substr(2, 10)");
    // More than 2 arguments
    testSame("x = 'abcdef'.substr(1, 2, 3)");
    // Non-numeric argument
    testSame("x = 'abcdef'.substr('a')");
  }

  @Test(timeout = 4000)
  public void testStringSubstringBoundaries() {
    // Negative start or end
    testSame("x = 'abcdef'.substring(-1, 2)");
    testSame("x = 'abcdef'.substring(1, -2)");
    // Start or end out of bounds
    testSame("x = 'abcdef'.substring(10)");
    testSame("x = 'abcdef'.substring(1, 10)");
    // More than 2 arguments
    testSame("x = 'abcdef'.substring(1, 2, 3)");
    // Non-numeric argument
    testSame("x = 'abcdef'.substring('a')");
  }

  @Test(timeout = 4000)
  public void testStringCharAtAndCharCodeAtBoundaries() {
    // Negative index or >= length
    testSame("x = 'abcdef'.charAt(-1)");
    testSame("x = 'abcdef'.charAt(6)");
    testSame("x = 'abcdef'.charCodeAt(-1)");
    testSame("x = 'abcdef'.charCodeAt(6)");
    // Extra arguments
    testSame("x = 'abcdef'.charAt(1, 2)");
    testSame("x = 'abcdef'.charCodeAt(1, 2)");
    // Non-numeric argument
    testSame("x = 'abcdef'.charAt('a')");
    testSame("x = 'abcdef'.charCodeAt('a')");
  }

  // =========================================================================
  // Partition C: Numeric Methods (parseInt, parseFloat)
  // =========================================================================

  @Test(timeout = 4000)
  public void testParseIntStringRadix10() {
    testFold("x = parseInt('123')", "x = 123");
    testFold("x = parseInt('123', 10)", "x = 123");
    testFold("x = parseInt('  456  ')", "x = 456");
    testFold("x = parseInt('-789')", "x = -789");
  }

  @Test(timeout = 4000)
  public void testParseIntHex() {
    testFold("x = parseInt('0x1F')", "x = 31");
    testFold("x = parseInt('0X1F')", "x = 31");
    testFold("x = parseInt('1F', 16)", "x = 31");
  }

  @Test(timeout = 4000)
  public void testParseIntOtherRadixes() {
    testFold("x = parseInt('1010', 2)", "x = 10");
    testFold("x = parseInt('77', 8)", "x = 63");
    testFold("x = parseInt('z', 36)", "x = 35");
  }

  @Test(timeout = 4000)
  public void testParseIntNumberArgument() {
    testFold("x = parseInt(123)", "x = 123");
    testFold("x = parseInt(123.45)", "x = 123");
    testFold("x = parseInt(123, 10)", "x = 123");
    testFold("x = parseInt(1010, 2)", "x = 10");
  }

  @Test(timeout = 4000)
  public void testParseIntInvalidRadix() {
    testSame("x = parseInt('123', -1)");
    testSame("x = parseInt('123', 1)");
    testSame("x = parseInt('123', 37)");
    testSame("x = parseInt('123', 1.5)");
    testSame("x = parseInt('123', '10')");
    testSame("x = parseInt('123', 10, 'extra')");
  }

  @Test(timeout = 4000)
  public void testParseFloat() {
    testFold("x = parseFloat('1.25')", "x = 1.25");
    testFold("x = parseFloat('10')", "x = 10");
    testFold("x = parseFloat(1.25)", "x = 1.25");
    testFold("x = parseFloat('  3.14  ')", "x = 3.14");
    // parseFloat does not accept radix
    testSame("x = parseFloat('1.25', 10)");
  }

  @Test(timeout = 4000)
  public void testParseFloatInvalid() {
    testSame("x = parseFloat('abc')");
    testSame("x = parseFloat('1.25abc')");
  }

  @Test(timeout = 4000)
  public void testNumericMethodsUnnormalizedAst() {
    // Should not fold if AST is not normalized
    Node root = parseAndFold("x = parseInt('123')", false);
    Node expectedRoot = compiler.parseTestCode("x = parseInt('123')");
    assertEquals(toSource(expectedRoot), toSource(root));
  }

  // =========================================================================
  // Partition D: Array.prototype.join() and Defect-Targeted Tests
  // =========================================================================

  @Test(timeout = 4000)
  public void testArrayJoinBasic() {
    testFold("x = ['a', 'b', 'c'].join()", "x = 'a,b,c'");
    testFold("x = ['a', 'b', 'c'].join(',')", "x = 'a,b,c'");
    testFold("x = ['a', 'b', 'c'].join('')", "x = 'abc'");
    testFold("x = ['a', 'b', 'c'].join('-')", "x = 'a-b-c'");
    testFold("x = [].join()", "x = ''");
    testFold("x = [].join(',')", "x = ''");
  }

  @Test(timeout = 4000)
  public void testArrayJoinNumbersAndBooleans() {
    testFold("x = [1, 2, 3].join()", "x = '1,2,3'");
    testFold("x = [true, false].join('-')", "x = 'true-false'");
    testFold("x = [null, undefined].join(',')", "x = ','");
  }

  @Test(timeout = 4000)
  public void testArrayJoinSingleElement() {
    testFold("x = ['hello'].join()", "x = 'hello'");
    testFold("x = [1].join()", "x = '1'");
    // Single non-literal element coerced to string
    testFold("x = [a].join()", "x = '' + a");
    testFold("x = [a].join(',')", "x = '' + a");
  }

  /**
   * Defects4J Ground Truth Target: testStringJoinAdd
   * Validates folding when join includes variable elements merged with string literals.
   */
  @Test(timeout = 4000)
  public void testStringJoinAdd() {
    testFold("x = ['a', 'b', c].join('')", "x = 'ab' + c");
    testFold("x = [a, 'b', 'c'].join('')", "x = a + 'bc'");
    testFold("x = ['a', 'b', c].join(',')", "x = 'a,b,' + c");
    testFold("x = [a, 'b', 'c'].join(',')", "x = a + ',b,c'");
    testFold("x = [a, 'b', 'c', d].join('')", "x = a + 'bc' + d");
  }

  /**
   * Defects4J Ground Truth Target: testNoStringJoin
   * Asserts when folding would increase the code size, no folding or partial folding is performed.
   */
  @Test(timeout = 4000)
  public void testNoStringJoin() {
    testSame("x = [a, b, c].join('')");
    testSame("x = [a, b, c].join(',')");
    testSame("x = [a, b].join()");
  }

  @Test(timeout = 4000)
  public void testArrayJoinNonImmutableJoinArgument() {
    // Non-literal join separator should not fold
    testSame("x = ['a', 'b'].join(sep)");
  }

  @Test(timeout = 4000)
  public void testArrayJoinSparseElements() {
    // Array with empty slots: [1, , 2]
    testFold("x = [1, , 2].join()", "x = '1,,2'");
  }

  // =========================================================================
  // Partition E: Non-matching Call Targets & Edge Guards
  // =========================================================================

  @Test(timeout = 4000)
  public void testNonCallNodesUntouched() {
    PeepholeReplaceKnownMethods peephole = new PeepholeReplaceKnownMethods();
    Node varNode = new Node(Token.VAR);
    Node result = peephole.optimizeSubtree(varNode);
    assertSame(varNode, result);
  }

  @Test(timeout = 4000)
  public void testCallTargetNullOrEmpty() {
    PeepholeReplaceKnownMethods peephole = new PeepholeReplaceKnownMethods();
    Node callNode = new Node(Token.CALL);
    Node result = peephole.optimizeSubtree(callNode);
    assertSame(callNode, result);
  }

  @Test(timeout = 4000)
  public void testCallOnUnknownMethod() {
    testSame("x = 'abc'.unknownMethod()");
    testSame("x = unknownFunction('abc')");
  }

  @Test(timeout = 4000)
  public void testCallOnNonStringObject() {
    testSame("x = obj.indexOf('a')");
    testSame("x = (123).toString()");
  }
}