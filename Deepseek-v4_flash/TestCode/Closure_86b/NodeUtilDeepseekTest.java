package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.TernaryValue;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * White-box test suite for NodeUtil targeting the known defect in Defects4J.
 * <p>
 * [Branch & Defect Analysis Matrix]
 * - Targets: constructorCallHasSideEffects, functionCallHasSideEffects,
 *   evaluatesToLocalValue, mayHaveSideEffects, checkForStateChangeHelper.
 * - Defect: NEW/call nodes with side effects are incorrectly treated as
 *   side-effect-free (e.g., "NEW STRING setLocation").
 * - Key branches: Token.NEW in mayHaveSideEffects, constructorCallHasSideEffects,
 *   callNode.isNoSideEffectsCall(), BUILTIN_FUNCTIONS_WITHOUT_SIDEEFFECTS,
 *   CONSTRUCTORS_WITHOUT_SIDE_EFFECTS, OBJECT_METHODS_WITHOUT_SIDEEFFECTS,
 *   REGEXP_METHODS, STRING_REGEXP_METHODS.
 * - Boundary values: null, empty strings, zero/NaN/Infinity, empty collections,
 *   function expression vs declaration, assignment ops, etc.
 */
public class NodeUtilDeepseekTest {

  // ------------------------------------------------------------------
  // Partition A: Core Functional Logic & State Transitions
  // ------------------------------------------------------------------

  @Test(timeout = 4000)
  public void testGetBooleanValue() {
    // STRING: non-empty true, empty false
    Node strNonEmpty = Node.newString("hello");
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(strNonEmpty));
    Node strEmpty = Node.newString("");
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(strEmpty));

    // NUMBER: non-zero true, zero false
    Node numNonZero = Node.newNumber(1.5);
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(numNonZero));
    Node numZero = Node.newNumber(0.0);
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(numZero));
    Node numNeg = Node.newNumber(-0.0);
    // -0.0 is 0.0 in double, so should be false
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(numNeg));

    // NULL, FALSE, VOID -> FALSE
    Node nullNode = new Node(Token.NULL);
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(nullNode));
    Node falseNode = new Node(Token.FALSE);
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(falseNode));
    Node voidNode = new Node(Token.VOID, Node.newNumber(0));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(voidNode));

    // NAME: undefined, NaN -> FALSE; Infinity -> TRUE; else UNKNOWN
    Node nameUndefined = Node.newString(Token.NAME, "undefined");
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(nameUndefined));
    Node nameNaN = Node.newString(Token.NAME, "NaN");
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(nameNaN));
    Node nameInfinity = Node.newString(Token.NAME, "Infinity");
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(nameInfinity));
    Node nameOther = Node.newString(Token.NAME, "x");
    assertEquals(TernaryValue.UNKNOWN, NodeUtil.getBooleanValue(nameOther));

    // TRUE, ARRAYLIT, OBJECTLIT, REGEXP -> TRUE
    Node trueNode = new Node(Token.TRUE);
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(trueNode));
    Node arrayLit = new Node(Token.ARRAYLIT);
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(arrayLit));
    Node objLit = new Node(Token.OBJECTLIT);
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(objLit));
    Node regexp = new Node(Token.REGEXP);
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(regexp));
  }

  @Test(timeout = 4000)
  public void testGetStringValue() {
    // STRING -> itself
    Node str = Node.newString("test");
    assertEquals("test", NodeUtil.getStringValue(str));

    // NAME: undefined, Infinity, NaN -> their names
    Node nameUndef = Node.newString(Token.NAME, "undefined");
    assertEquals("undefined", NodeUtil.getStringValue(nameUndef));
    Node nameInf = Node.newString(Token.NAME, "Infinity");
    assertEquals("Infinity", NodeUtil.getStringValue(nameInf));
    Node nameNaN = Node.newString(Token.NAME, "NaN");
    assertEquals("NaN", NodeUtil.getStringValue(nameNaN));
    Node nameOther = Node.newString(Token.NAME, "foo");
    assertNull(NodeUtil.getStringValue(nameOther));

    // NUMBER: integer -> "1", double -> "1.5"
    Node intNum = Node.newNumber(42);
    assertEquals("42", NodeUtil.getStringValue(intNum));
    Node doubleNum = Node.newNumber(3.14);
    assertEquals("3.14", NodeUtil.getStringValue(doubleNum));
    Node negDouble = Node.newNumber(-2.5);
    assertEquals("-2.5", NodeUtil.getStringValue(negDouble));

    // FALSE, TRUE, NULL -> token names
    Node falseNode = new Node(Token.FALSE);
    assertEquals("false", NodeUtil.getStringValue(falseNode));
    Node trueNode = new Node(Token.TRUE);
    assertEquals("true", NodeUtil.getStringValue(trueNode));
    Node nullNode = new Node(Token.NULL);
    assertEquals("null", NodeUtil.getStringValue(nullNode));

    // VOID -> "undefined"
    Node voidNode = new Node(Token.VOID, Node.newNumber(0));
    assertEquals("undefined", NodeUtil.getStringValue(voidNode));
  }

  @Test(timeout = 4000)
  public void testGetNumberValue() {
    // TRUE -> 1.0, FALSE/NULL -> 0.0
    Node trueNode = new Node(Token.TRUE);
    assertEquals(1.0, NodeUtil.getNumberValue(trueNode), 0.0);
    Node falseNode = new Node(Token.FALSE);
    assertEquals(0.0, NodeUtil.getNumberValue(falseNode), 0.0);
    Node nullNode = new Node(Token.NULL);
    assertEquals(0.0, NodeUtil.getNumberValue(nullNode), 0.0);

    // NUMBER -> its value
    Node num = Node.newNumber(2.718);
    assertEquals(2.718, NodeUtil.getNumberValue(num), 1e-15);

    // VOID -> NaN
    Node voidNode = new Node(Token.VOID, Node.newNumber(0));
    assertTrue(Double.isNaN(NodeUtil.getNumberValue(voidNode)));

    // NAME: undefined -> NaN, NaN -> NaN, Infinity -> +Infinity, else null
    Node nameUndef = Node.newString(Token.NAME, "undefined");
    assertTrue(Double.isNaN(NodeUtil.getNumberValue(nameUndef)));
    Node nameNaN = Node.newString(Token.NAME, "NaN");
    assertTrue(Double.isNaN(NodeUtil.getNumberValue(nameNaN)));
    Node nameInf = Node.newString(Token.NAME, "Infinity");
    assertEquals(Double.POSITIVE_INFINITY, NodeUtil.getNumberValue(nameInf), 0.0);
    Node nameOther = Node.newString(Token.NAME, "x");
    assertNull(NodeUtil.getNumberValue(nameOther));
  }

  @Test(timeout = 4000)
  public void testGetExpressionBooleanValue() {
    // ASSIGN: value is RHS
    Node assign = new Node(Token.ASSIGN);
    Node lhs = Node.newString(Token.NAME, "a");
    Node rhs = Node.newString("non-empty");
    assign.addChildToBack(lhs);
    assign.addChildToBack(rhs);
    assertEquals(TernaryValue.TRUE, NodeUtil.getExpressionBooleanValue(assign));

    // COMMA: value is last child
    Node comma = new Node(Token.COMMA);
    Node first = Node.newNumber(0);
    Node second = Node.newString("hello");
    comma.addChildToBack(first);
    comma.addChildToBack(second);
    assertEquals(TernaryValue.TRUE, NodeUtil.getExpressionBooleanValue(comma));

    // NOT
    Node not = new Node(Token.NOT, Node.newNumber(0));
    assertEquals(TernaryValue.TRUE, NodeUtil.getExpressionBooleanValue(not));

    // AND
    Node and = new Node(Token.AND);
    and.addChildToBack(Node.newString("non-empty"));
    and.addChildToBack(Node.newNumber(1));
    assertEquals(TernaryValue.TRUE, NodeUtil.getExpressionBooleanValue(and));

    // OR
    Node or = new Node(Token.OR);
    or.addChildToBack(Node.newNumber(0));
    or.addChildToBack(Node.newString(""));
    assertEquals(TernaryValue.FALSE, NodeUtil.getExpressionBooleanValue(or));

    // HOOK: true and false branches equal
    Node hook = new Node(Token.HOOK);
    Node cond = Node.newNumber(1);
    Node trueVal = Node.newString("yes");
    Node falseVal = Node.newString("no");
    hook.addChildToBack(cond);
    hook.addChildToBack(trueVal);
    hook.addChildToBack(falseVal);
    // Not equal -> UNKNOWN
    assertEquals(TernaryValue.UNKNOWN, NodeUtil.getExpressionBooleanValue(hook));
    // Equal branches
    Node hookSame = new Node(Token.HOOK);
    Node sameVal = Node.newString("same");
    hookSame.addChildToBack(cond);
    hookSame.addChildToBack(sameVal);
    hookSame.addChildToBack(sameVal.cloneTree());
    assertEquals(TernaryValue.TRUE, NodeUtil.getExpressionBooleanValue(hookSame));
  }

  // ------------------------------------------------------------------
  // Partition B: Boundary Value Analysis & Extremes
  // ------------------------------------------------------------------

  @Test(timeout = 4000)
  public void testIsImmutableValueBoundaries() {
    // STRING, NUMBER, NULL, TRUE, FALSE immutable
    assertTrue(NodeUtil.isImmutableValue(Node.newString("")));
    assertTrue(NodeUtil.isImmutableValue(Node.newNumber(0)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.NULL)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.TRUE)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.FALSE)));

    // VOID with immutable child -> immutable
    Node voidNode = new Node(Token.VOID, Node.newNumber(0));
    assertTrue(NodeUtil.isImmutableValue(voidNode));

    // NEG with immutable child -> immutable
    Node neg = new Node(Token.NEG, Node.newNumber(5));
    assertTrue(NodeUtil.isImmutableValue(neg));

    // NAME: undefined, Infinity, NaN -> immutable; else not
    assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "undefined")));
    assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "Infinity")));
    assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "NaN")));
    assertFalse(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "x")));

    // Other types not immutable
    assertFalse(NodeUtil.isImmutableValue(new Node(Token.CALL)));
    assertFalse(NodeUtil.isImmutableValue(new Node(Token.ARRAYLIT)));
  }

  @Test(timeout = 4000)
  public void testIsLiteralValueBoundaries() {
    // ARRAYLIT with all literal children
    Node arrayLit = new Node(Token.ARRAYLIT);
    arrayLit.addChildToBack(Node.newNumber(1));
    arrayLit.addChildToBack(Node.newString("a"));
    assertTrue(NodeUtil.isLiteralValue(arrayLit, false));

    // ARRAYLIT with non-literal child
    Node arrayNonLit = new Node(Token.ARRAYLIT);
    arrayNonLit.addChildToBack(Node.newString(Token.NAME, "x"));
    assertFalse(NodeUtil.isLiteralValue(arrayNonLit, false));

    // OBJECTLIT with all literal values
    Node objLit = new Node(Token.OBJECTLIT);
    Node key = Node.newString("key");
    Node val = Node.newNumber(42);
    objLit.addChildToBack(key);
    key.addChildToBack(val);
    assertTrue(NodeUtil.isLiteralValue(objLit, false));

    // FUNCTION expression with includeFunctions=true
    Node funcExpr = new Node(Token.FUNCTION);
    Node body = new Node(Token.BLOCK);
    funcExpr.addChildToBack(Node.newString(Token.NAME, ""));
    funcExpr.addChildToBack(new Node(Token.LP));
    funcExpr.addChildToBack(body);
    // Not a statement -> function expression
    assertTrue(NodeUtil.isLiteralValue(funcExpr, true));
    assertFalse(NodeUtil.isLiteralValue(funcExpr, false));

    // IMMUTABLE value passes
    assertTrue(NodeUtil.isLiteralValue(Node.newString(""), false));
  }

  @Test(timeout = 4000)
  public void testIsSimpleOperatorAll() {
    // All simple operator types
    assertTrue(NodeUtil.isSimpleOperatorType(Token.ADD));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.BITAND));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.BITNOT));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.BITOR));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.BITXOR));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.COMMA));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.DIV));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.EQ));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.GE));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.GETELEM));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.GETPROP));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.GT));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.INSTANCEOF));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.LE));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.LSH));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.LT));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.MOD));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.MUL));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.NE));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.NOT));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.RSH));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.SHEQ));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.SHNE));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.SUB));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.TYPEOF));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.VOID));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.POS));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.NEG));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.URSH));

    // Non-simple operators
    assertFalse(NodeUtil.isSimpleOperatorType(Token.ASSIGN));
    assertFalse(NodeUtil.isSimpleOperatorType(Token.CALL));
    assertFalse(NodeUtil.isSimpleOperatorType(Token.NEW));
    assertFalse(NodeUtil.isSimpleOperatorType(Token.OR));
    assertFalse(NodeUtil.isSimpleOperatorType(Token.AND));
    assertFalse(NodeUtil.isSimpleOperatorType(Token.HOOK));
  }

  // ------------------------------------------------------------------
  // Partition C: Defect-Targeted Branch Zone (Side Effect Analysis)
  // ------------------------------------------------------------------

  @Test(timeout = 4000)
  public void testConstructorCallHasSideEffects_CustomConstructor() {
    // NEW with custom constructor should have side effects
    Node newCall = new Node(Token.NEW);
    Node constructorName = Node.newString(Token.NAME, "MyConstructor");
    newCall.addChildToBack(constructorName);
    assertTrue("Custom constructor should have side effects",
        NodeUtil.constructorCallHasSideEffects(newCall));
  }

  @Test(timeout = 4000)
  public void testConstructorCallHasSideEffects_BuiltinNoSideEffects() {
    // NEW with built-in constructors from CONSTRUCTORS_WITHOUT_SIDE_EFFECTS
    String[] builtins = {"Array", "Date", "Error", "Object", "RegExp", "XMLHttpRequest"};
    for (String name : builtins) {
      Node newCall = new Node(Token.NEW);
      Node constructorName = Node.newString(Token.NAME, name);
      newCall.addChildToBack(constructorName);
      assertFalse("Constructor " + name + " should have no side effects",
          NodeUtil.constructorCallHasSideEffects(newCall));
    }
  }

  @Test(timeout = 4000)
  public void testConstructorCallHasSideEffects_NoSideEffectsFlag() {
    // If isNoSideEffectsCall flag is set, should return false even for unknown constructor
    Node newCall = new Node(Token.NEW);
    Node constructorName = Node.newString(Token.NAME, "SomeConstructor");
    newCall.addChildToBack(constructorName);
    newCall.putBooleanProp(Node.SIDE_EFFECT_FLAGS, Node.FLAG_NO_SIDE_EFFECTS);
    assertFalse("NEW with no side effects flag should be false",
        NodeUtil.constructorCallHasSideEffects(newCall));
  }

  @Test(timeout = 4000)
  public void testFunctionCallHasSideEffects_CustomFunction() {
    // CALL to unknown function should have side effects
    Node call = new Node(Token.CALL);
    Node funcName = Node.newString(Token.NAME, "myFunc");
    call.addChildToBack(funcName);
    assertTrue("Unknown function call should have side effects",
        NodeUtil.functionCallHasSideEffects(call));
  }

  @Test(timeout = 4000)
  public void testFunctionCallHasSideEffects_BuiltinFunctions() {
    // CALL to BUILTIN_FUNCTIONS_WITHOUT_SIDEEFFECTS
    String[] builtins = {"Object", "Array", "String", "Number", "Boolean", "RegExp", "Error"};
    for (String name : builtins) {
      Node call = new Node(Token.CALL);
      Node funcName = Node.newString(Token.NAME, name);
      call.addChildToBack(funcName);
      assertFalse("Builtin function " + name + " should have no side effects",
          NodeUtil.functionCallHasSideEffects(call));
    }
  }

  @Test(timeout = 4000)
  public void testFunctionCallHasSideEffects_ObjectMethods() {
    // obj.toString() or obj.valueOf() with one child (no args) should have no side effects
    // Build: GETPROP [NAME obj, STRING toString] then CALL
    Node getProp = new Node(Token.GETPROP);
    Node obj = Node.newString(Token.NAME, "obj");
    Node methodName = Node.newString("toString");
    getProp.addChildToBack(obj);
    getProp.addChildToBack(methodName);
    Node call = new Node(Token.CALL, getProp);
    assertFalse("obj.toString() should have no side effects",
        NodeUtil.functionCallHasSideEffects(call));

    // obj.valueOf() similarly
    Node getProp2 = new Node(Token.GETPROP);
    getProp2.addChildToBack(obj.cloneTree());
    getProp2.addChildToBack(Node.newString("valueOf"));
    Node call2 = new Node(Token.CALL, getProp2);
    assertFalse("obj.valueOf() should have no side effects",
        NodeUtil.functionCallHasSideEffects(call2));
  }

  @Test(timeout = 4000)
  public void testFunctionCallHasSideEffects_MathNamespace() {
    // Math functions have no side effects
    Node getProp = new Node(Token.GETPROP);
    Node math = Node.newString(Token.NAME, "Math");
    Node method = Node.newString("random");
    getProp.addChildToBack(math);
    getProp.addChildToBack(method);
    Node call = new Node(Token.CALL, getProp);
    assertFalse("Math.random() should have no side effects",
        NodeUtil.functionCallHasSideEffects(call));
  }

  @Test(timeout = 4000)
  public void testFunctionCallHasSideEffects_RegExpAndStringMethods_NoGlobalRefs() {
    // When compiler has no global regexp references, regexp methods and string regexp methods
    // with literal arguments are side-effect-free. We'll simulate by passing null compiler.
    // regexp.test() (assuming no global refs)
    Node regexp = new Node(Token.REGEXP);
    Node getProp = new Node(Token.GETPROP);
    getProp.addChildToBack(regexp);
    getProp.addChildToBack(Node.newString("test"));
    Node call = new Node(Token.CALL, getProp);
    assertFalse("regexp.test() should have no side effects (no global refs)",
        NodeUtil.functionCallHasSideEffects(call, null));

    // string.match(/pat/) with literal string or regexp
    Node strLit = Node.newString("hello");
    Node getPropStr = new Node(Token.GETPROP);
    getPropStr.addChildToBack(strLit);
    getPropStr.addChildToBack(Node.newString("match"));
    Node callStr = new Node(Token.CALL, getPropStr);
    Node regexpArg = new Node(Token.REGEXP);
    callStr.addChildToBack(regexpArg);
    assertFalse("string.match(regexp) should have no side effects (no global refs)",
        NodeUtil.functionCallHasSideEffects(callStr, null));
  }

  @Test(timeout = 4000)
  public void testMayHaveSideEffects_AssignmentWithNameLHS() {
    // Assignment to a name: var x = ... has side effects
    Node assign = new Node(Token.ASSIGN);
    Node lhs = Node.newString(Token.NAME, "x");
    Node rhs = Node.newNumber(5);
    assign.addChildToBack(lhs);
    assign.addChildToBack(rhs);
    assertTrue("Assignment to name should have side effects",
        NodeUtil.mayHaveSideEffects(assign));
  }

  @Test(timeout = 4000)
  public void testMayHaveSideEffects_AssignmentToLocalGetter() {
    // When LHS is a get (GETPROP) and the base object evaluates to local value,
    // assignment may not have side effects.
    // Build: GETPROP [name "a" (local), string "prop"] = literal
    Node assign = new Node(Token.ASSIGN);
    Node getProp = new Node(Token.GETPROP);
    Node localName = Node.newString(Token.NAME, "a");
    getProp.addChildToBack(localName);
    getProp.addChildToBack(Node.newString("prop"));
    assign.addChildToBack(getProp);
    assign.addChildToBack(Node.newNumber(10));
    // "a" is a name, and local values are names that are immutable? Actually, we need to check
    // evaluatesToLocalValue. For now, name "a" is not immutable, so mayHaveSideEffects should
    // return true because the LHS is not a literal and not a local value.
    // But if "a" is considered local? The method checks evaluatesToLocalValue on the base object.
    // Since "a" is a NAME, it is not immutable unless it's undefined/Infinity/NaN.
    // So it should have side effects.
    assertTrue("Assignment to property of non-local should have side effects",
        NodeUtil.mayHaveSideEffects(assign));
  }

  @Test(timeout = 4000)
  public void testMayHaveSideEffects_AssignmentToLocalLiteralObject() {
    // If the root object is a literal (e.g., {}.prop = 5), it may be local but
    // the assignment to a literal object literal is considered side-effect free
    // because the object is not aliased.
    Node assign = new Node(Token.ASSIGN);
    Node objectLit = new Node(Token.OBJECTLIT);
    Node getProp = new Node(Token.GETPROP);
    getProp.addChildToBack(objectLit);
    getProp.addChildToBack(Node.newString("prop"));
    assign.addChildToBack(getProp);
    assign.addChildToBack(Node.newNumber(5));
    // The root object (objectLit) is a literal, so the assignment is local.
    // However, the RHS is immutable, so mayHaveSideEffects should return false.
    assertFalse("Assignment to literal object should have no side effects",
        NodeUtil.mayHaveSideEffects(assign));
  }

  @Test(timeout = 4000)
  public void testMayHaveSideEffects_ObjectLitWithNonLiteralChild() {
    // Object literal with a child that has side effects -> side effects
    Node objLit = new Node(Token.OBJECTLIT);
    Node key = Node.newString("key");
    Node call = new Node(Token.CALL, Node.newString(Token.NAME, "f"));
    key.addChildToBack(call);
    objLit.addChildToBack(key);
    assertTrue("Object literal with side-effecting child should have side effects",
        NodeUtil.mayHaveSideEffects(objLit));
  }

  @Test(timeout = 4000)
  public void testMayHaveSideEffects_NEWWithSideEffectsConstructor() {
    // NEW with custom constructor should have side effects
    Node newCall = new Node(Token.NEW);
    Node constructor = Node.newString(Token.NAME, "Custom");
    newCall.addChildToBack(constructor);
    assertTrue("NEW with custom constructor should have side effects",
        NodeUtil.mayHaveSideEffects(newCall));
  }

  @Test(timeout = 4000)
  public void testMayHaveSideEffects_NEWBuiltinNoSideEffects() {
    // NEW with Array should have no side effects (assuming checkForNewObjects is false)
    // mayHaveSideEffects uses checkForNewObjects = false, so NEW should be checked via
    // constructorCallHasSideEffects.
    Node newCall = new Node(Token.NEW);
    Node constructor = Node.newString(Token.NAME, "Array");
    newCall.addChildToBack(constructor);
    assertFalse("NEW Array should have no side effects",
        NodeUtil.mayHaveSideEffects(newCall));
  }

  @Test(timeout = 4000)
  public void testMayHaveSideEffects_CallWithSideEffects() {
    Node call = new Node(Token.CALL);
    Node func = Node.newString(Token.NAME, "alert");
    call.addChildToBack(func);
    assertTrue("alert() should have side effects",
        NodeUtil.mayHaveSideEffects(call));
  }

  @Test(timeout = 4000)
  public void testMayHaveSideEffects_CallNoSideEffects() {
    Node call = new Node(Token.CALL);
    Node func = Node.newString(Token.NAME, "String");
    call.addChildToBack(func);
    assertFalse("String() should have no side effects",
        NodeUtil.mayHaveSideEffects(call));
  }

  @Test(timeout = 4000)
  public void testMayHaveSideEffects_FunctionDeclaration() {
    // Function declaration (statement) changes namespace -> side effects
    Node func = new Node(Token.FUNCTION);
    Node name = Node.newString(Token.NAME, "f");
    Node params = new Node(Token.LP);
    Node body = new Node(Token.BLOCK);
    func.addChildToBack(name);
    func.addChildToBack(params);
    func.addChildToBack(body);
    // Make it a statement: parent is SCRIPT or BLOCK
    Node script = new Node(Token.SCRIPT);
    script.addChildToBack(func);
    assertTrue("Function declaration should have side effects",
        NodeUtil.mayHaveSideEffects(func));
  }

  @Test(timeout = 4000)
  public void testMayHaveSideEffects_FunctionExpression() {
    // Function expression should NOT have side effects
    Node func = new Node(Token.FUNCTION);
    Node name = Node.newString(Token.NAME, "");
    Node params = new Node(Token.LP);
    Node body = new Node(Token.BLOCK);
    func.addChildToBack(name);
    func.addChildToBack(params);
    func.addChildToBack(body);
    // Not a statement (no parent or parent is expression)
    assertFalse("Function expression should have no side effects",
        NodeUtil.mayHaveSideEffects(func));
  }

  @Test(timeout = 4000)
  public void testMayHaveSideEffects_Throw() {
    Node throwNode = new Node(Token.THROW, Node.newString("error"));
    assertTrue("Throw should have side effects",
        NodeUtil.mayHaveSideEffects(throwNode));
  }

  // ------------------------------------------------------------------
  // Partition D: Exception & Defensive Guard Paths
  // ------------------------------------------------------------------

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testConstructorCallHasSideEffects_NonNew() {
    Node call = new Node(Token.CALL);
    NodeUtil.constructorCallHasSideEffects(call);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testFunctionCallHasSideEffects_NonCall() {
    Node newCall = new Node(Token.NEW);
    NodeUtil.functionCallHasSideEffects(newCall);
  }

  @Test(expected = NullPointerException.class, timeout = 4000)
  public void testGetFunctionBody_Null() {
    NodeUtil.getFunctionBody(null);
  }

  @Test(expected = NullPointerException.class, timeout = 4000)
  public void testGetBooleanValue_Null() {
    NodeUtil.getBooleanValue(null);
  }

  // ------------------------------------------------------------------
  // Partition E: Object Lifecycle & Contract Integrity
  // ------------------------------------------------------------------

  @Test(timeout = 4000)
  public void testIsFunctionExpression() {
    // Function expression: not a statement
    Node funcExpr = new Node(Token.FUNCTION);
    funcExpr.addChildToBack(Node.newString(Token.NAME, ""));
    funcExpr.addChildToBack(new Node(Token.LP));
    funcExpr.addChildToBack(new Node(Token.BLOCK));
    assertTrue("Function with no parent should be expression (not statement)",
        NodeUtil.isFunctionExpression(funcExpr));

    // Function declaration: parent is BLOCK/SCRIPT
    Node script = new Node(Token.SCRIPT);
    script.addChildToBack(funcExpr);
    assertFalse("Function as child of SCRIPT should be declaration",
        NodeUtil.isFunctionExpression(funcExpr));
  }

  @Test(timeout = 4000)
  public void testIsSimpleOperator_Node() {
    Node addNode = new Node(Token.ADD);
    assertTrue(NodeUtil.isSimpleOperator(addNode));
    Node assignNode = new Node(Token.ASSIGN);
    assertFalse(NodeUtil.isSimpleOperator(assignNode));
  }

  @Test(timeout = 4000)
  public void testIsAssignmentOp() {
    assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN)));
    assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_ADD)));
    assertFalse(NodeUtil.isAssignmentOp(new Node(Token.ADD)));
    assertFalse(NodeUtil.isAssignmentOp(new Node(Token.CALL)));
  }

  @Test(timeout = 4000)
  public void testIsGet() {
    assertTrue(NodeUtil.isGet(new Node(Token.GETPROP)));
    assertTrue(NodeUtil.isGet(new Node(Token.GETELEM)));
    assertFalse(NodeUtil.isGet(new Node(Token.CALL)));
    assertFalse(NodeUtil.isGet(new Node(Token.NAME)));
  }

  @Test(timeout = 4000)
  public void testIsName() {
    assertTrue(NodeUtil.isName(new Node(Token.NAME)));
    assertFalse(NodeUtil.isName(new Node(Token.STRING)));
  }

  @Test(timeout = 4000)
  public void testIsCall() {
    assertTrue(NodeUtil.isCall(new Node(Token.CALL)));
    assertFalse(NodeUtil.isCall(new Node(Token.NEW)));
  }

  @Test(timeout = 4000)
  public void testIsNew() {
    assertTrue(NodeUtil.isNew(new Node(Token.NEW)));
    assertFalse(NodeUtil.isNew(new Node(Token.CALL)));
  }

  @Test(timeout = 4000)
  public void testIsVar() {
    assertTrue(NodeUtil.isVar(new Node(Token.VAR)));
    assertFalse(NodeUtil.isVar(new Node(Token.NAME)));
  }

  // ------------------------------------------------------------------
  // Partition F: Additional defect-targeted tests from ground truth
  // ------------------------------------------------------------------

  @Test(timeout = 4000)
  public void testLocalValue1_DefectRegression() {
    // This test corresponds to NodeUtilTest.testLocalValue1 failure.
    // The bug was that some construction like "new String()" was incorrectly
    // considered as having no local result. We'll test evaluatesToLocalValue
    // for a NEW node with a literal constructor and no args.
    Node newCall = new Node(Token.NEW);
    Node constructor = Node.newString(Token.NAME, "String");
    newCall.addChildToBack(constructor);
    // Should be local value (no side effects, result is local)
    assertTrue("new String() should evaluate to local value",
        NodeUtil.evaluatesToLocalValue(newCall));
  }

  @Test(timeout = 4000)
  public void testLocalizedSideEffects_DefectRegression() {
    // This tests that a call like "f()" inside a NEW constructor argument has side effects.
    // The bug may be that side effects of constructor arguments are not checked.
    // We'll create a NEW node with a call as argument.
    Node newCall = new Node(Token.NEW);
    Node constructor = Node.newString(Token.NAME, "Array");
    newCall.addChildToBack(constructor);
    Node callArg = new Node(Token.CALL, Node.newString(Token.NAME, "g"));
    newCall.addChildToBack(callArg);
    // mayHaveSideEffects should consider the call argument.
    assertTrue("new Array(g()) should have side effects due to arg",
        NodeUtil.mayHaveSideEffects(newCall));
  }

  @Test(timeout = 4000)
  public void testIssue303_DefectRegression() {
    // Known failure: expected [] but was [NEW STRING setLocation]
    // This indicates that a NEW node with a constructor that has side effects
    // (like "setLocation") was incorrectly considered side-effect-free.
    // We'll test that constructorCallHasSideEffects returns true for "setLocation".
    Node newCall = new Node(Token.NEW);
    Node constructor = Node.newString(Token.NAME, "setLocation");
    newCall.addChildToBack(constructor);
    assertTrue("new setLocation() should have side effects",
        NodeUtil.mayHaveSideEffects(newCall));
  }

  @Test(timeout = 4000)
  public void testAnnotationInExternsNew_DefectRegression() {
    // Test NEW with externs method that should have side effects.
    // The defect was that "NEW STRING externObjSEThisMethod" was not flagged.
    // We'll simulate a NEW with a name that is not in CONSTRUCTORS_WITHOUT_SIDE_EFFECTS.
    Node newCall = new Node(Token.NEW);
    Node constructor = Node.newString(Token.NAME, "externObjSEThisMethod");
    newCall.addChildToBack(constructor);
    assertTrue("new externObjSEThisMethod() should have side effects",
        NodeUtil.mayHaveSideEffects(newCall));
  }

  @Test(timeout = 4000)
  public void testPureFunctionIdentifier_DefectRegression() {
    // Another case: call to a function that modifies 'this' with local base may have side effects?
    // The defect test testLocalizedSideEffects8/9 show that "f" is incorrectly retained.
    // We'll test that a call with a GETPROP base that is local (e.g., ({}).method) may have side effects
    // if the method is unknown. But the bug is about local variables being considered as having side effects
    // when they should not? Actually the failure expected <[A]> but was <[A, f]>, meaning 'f' was incorrectly
    // considered to have side effects. So we need to ensure that a call like "f()" where f is a local function
    // with no side effects is correctly identified. Hard to simulate without compiler.
    // We'll at least test that a call to a NAME with no known side effects returns true (unknown function).
    Node call = new Node(Token.CALL);
    Node func = Node.newString(Token.NAME, "f");
    call.addChildToBack(func);
    // Without compiler, mayHaveSideEffects should return true because functionCallHasSideEffects returns true.
    assertTrue("Unknown call should have side effects",
        NodeUtil.mayHaveSideEffects(call));
  }

  @Test(timeout = 4000)
  public void testEvaluatesToLocalValue_AssignmentWithImmutableRHS() {
    // Assignment where RHS is immutable and LHS is a GETPROP with local base should be local.
    Node assign = new Node(Token.ASSIGN);
    Node getProp = new Node(Token.GETPROP);
    Node localBase = Node.newString(Token.NAME, "a"); // 'a' is not immutable but is a name, it's not local per se.
    // To make it local, base must be a literal or something that evaluates to local.
    // We'll use an object literal as base.
    Node objLit = new Node(Token.OBJECTLIT);
    getProp.addChildToBack(objLit);
    getProp.addChildToBack(Node.newString("prop"));
    assign.addChildToBack(getProp);
    assign.addChildToBack(Node.newNumber(42)); // immutable RHS
    // Should be local because object literal is local and RHS is immutable.
    assertTrue("Assignment to property of literal with immutable RHS should be local",
        NodeUtil.evaluatesToLocalValue(assign));
  }

  @Test(timeout = 4000)
  public void testEvaluatesToLocalValue_NotLocalAssignment() {
    // Assignment to a property of a non-local name should not be local.
    Node assign = new Node(Token.ASSIGN);
    Node getProp = new Node(Token.GETPROP);
    Node name = Node.newString(Token.NAME, "global");
    getProp.addChildToBack(name);
    getProp.addChildToBack(Node.newString("prop"));
    assign.addChildToBack(getProp);
    assign.addChildToBack(Node.newNumber(42));
    // 'global' is not immutable and not a local value, so the result is not local.
    assertFalse("Assignment to property of non-local name should not be local",
        NodeUtil.evaluatesToLocalValue(assign));
  }

  @Test(timeout = 4000)
  public void testGetFunctionName() {
    // function name() {} -> parent is SCRIPT, returns name from first child
    Node func = new Node(Token.FUNCTION);
    func.addChildToBack(Node.newString(Token.NAME, "myFunc"));
    func.addChildToBack(new Node(Token.LP));
    func.addChildToBack(new Node(Token.BLOCK));
    Node script = new Node(Token.SCRIPT);
    script.addChildToBack(func);
    assertEquals("myFunc", NodeUtil.getFunctionName(func));

    // var name = function() {} -> parent is NAME
    Node varName = Node.newString(Token.NAME, "x");
    Node funcExpr = new Node(Token.FUNCTION);
    funcExpr.addChildToBack(Node.newString(Token.NAME, ""));
    funcExpr.addChildToBack(new Node(Token.LP));
    funcExpr.addChildToBack(new Node(Token.BLOCK));
    varName.addChildToBack(funcExpr);
    Node var = new Node(Token.VAR, varName);
    assertEquals("x", NodeUtil.getFunctionName(funcExpr));

    // qualified.name = function() {} -> parent is ASSIGN
    Node assign = new Node(Token.ASSIGN);
    Node getProp = new Node(Token.GETPROP);
    Node base = Node.newString(Token.NAME, "qualified");
    Node prop = Node.newString("name");
    getProp.addChildToBack(base);
    getProp.addChildToBack(prop);
    assign.addChildToBack(getProp);
    assign.addChildToBack(funcExpr);
    assertEquals("qualified.name", NodeUtil.getFunctionName(funcExpr));
  }

  @Test(timeout = 4000)
  public void testIsConstantName() {
    Node nameConst = Node.newString(Token.NAME, "$$constant");
    nameConst.putBooleanProp(Node.IS_CONSTANT_NAME, true);
    assertTrue(NodeUtil.isConstantName(nameConst));
    Node nameNonConst = Node.newString(Token.NAME, "x");
    assertFalse(NodeUtil.isConstantName(nameNonConst));
  }
}