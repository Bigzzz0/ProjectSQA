package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: PeepholeFoldConstants - optimization for bitwise shift operations, arithmetic, typeof,
 *         unary operators, comparisons, array/string folding.
 * 
 * Key Decision Branches / Boundary Conditions:
 * 
 * 1. tryFoldShift (Token.LSH, RSH, URSH):
 *    - left in [Integer.MIN_VALUE, Integer.MAX_VALUE]? else BITWISE_OPERAND_OUT_OF_RANGE error
 *    - right in [0,32)? else SHIFT_AMOUNT_OUT_OF_BOUNDS error
 *    - left int conversion equality? else FRACTIONAL_BITWISE_OPERAND error
 *    - right int conversion equality? else FRACTIONAL_BITWISE_OPERAND error
 *    - Normal fold: lvalInt << rvalInt, >>, >>>
 *    - Zero shift: lval << 0 => lval
 *    - Large shift: lval << 31 (max within range)
 *    - Negative shift amount? (should error)
 *    - Double boundary: lval = Integer.MAX_VALUE+1 => out of range
 *    - Fractional left: 1.5 => error
 *    - Fractional right: 2.5 => error
 * 
 * 2. tryFoldUnaryOperator (NEG, BITNOT, NOT):
 *    - NEG on Infinity → no change
 *    - NEG on NaN → NaN (replaces node with left)
 *    - NEG on number → negative number
 *    - NEG on non-number (NAME not Infinity/NaN) → error
 *    - BITNOT on number in int range → ~int
 *    - BITNOT on number out of int range → error
 *    - BITNOT on fractional → error
 *    - NOT on true → false
 *    - NOT on false → true
 * 
 * 3. tryFoldComparison:
 *    - undefined vs null/undefined: EQ true, NE false, SHEQ only undefined, etc.
 *    - null vs undefined: EQ true
 *    - string vs string: SHEQ/EQ by value
 *    - number vs number: all comparisons
 *    - name vs name: LT/GT false (same name)
 * 
 * 4. tryFoldTypeof:
 *    - typeof("string") → "string"
 *    - typeof(6) → "number"
 *    - typeof(true) → "boolean"
 *    - typeof(null) → "object" (spec quirk)
 *    - typeof(undefined) via NAME → "undefined"
 * 
 * 5. tryFoldGetProp (length):
 *    - ARRAYLIT.length → child count
 *    - STRING.length → length of string
 * 
 * 6. tryFoldGetElem:
 *    - ARRAYLIT with number index → element
 *    - Index out of bounds → error
 *    - Non-integer index → error
 *    - Negative index → error
 * 
 * 7. tryFoldStringJoin:
 *    - Empty array → ""
 *    - Single element → element
 *    - Multiple strings → joined
 * 
 * 8. tryFoldStringIndexOf:
 *    - "abcdef".indexOf("bc") → 1
 *    - "abcdefbc".indexOf("bc",3) → 6
 * 
 * Defect Targeting (Defects4J known issue in testFoldBitShifts):
 *    - The bug may involve unsigned right shift (>>>) on negative numbers, or
 *      shift amount boundary handling. We target:
 *        - java only: lvalInt >>> rvalInt (but JS result is unsigned 32-bit)
 *        - Need to verify correct double representation of unsigned result.
 *      We test negative left operand with >>> to reveal potential bug.
 */
public class PeepholeFoldConstantsDeepseekTest {

  // Helper: create a minimal Compiler instance with PeepholeFoldConstants pass
  private Compiler createCompiler() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    return compiler;
  }

  // Helper to apply PeepholeFoldConstants optimization to a subtree
  private Node applyFold(Node subtree) {
    PeepholeFoldConstants peephole = new PeepholeFoldConstants();
    return peephole.optimizeSubtree(subtree);
  }

  // ===== Partition A: Core Functional Logic & State Transitions =====

  @Test(timeout = 4000)
  public void testFoldTypeofString() {
    Node typeofNode = new Node(Token.TYPEOF, Node.newString("hello"));
    Node result = applyFold(typeofNode);
    assertEquals(Token.STRING, result.getType());
    assertEquals("string", result.getString());
  }

  @Test(timeout = 4000)
  public void testFoldTypeofNumber() {
    Node typeofNode = new Node(Token.TYPEOF, Node.newNumber(42));
    Node result = applyFold(typeofNode);
    assertEquals(Token.STRING, result.getType());
    assertEquals("number", result.getString());
  }

  @Test(timeout = 4000)
  public void testFoldTypeofBoolean() {
    Node typeofNode = new Node(Token.TYPEOF, new Node(Token.TRUE));
    Node result = applyFold(typeofNode);
    assertEquals(Token.STRING, result.getType());
    assertEquals("boolean", result.getString());
  }

  @Test(timeout = 4000)
  public void testFoldTypeofNull() {
    Node typeofNode = new Node(Token.TYPEOF, new Node(Token.NULL));
    Node result = applyFold(typeofNode);
    assertEquals(Token.STRING, result