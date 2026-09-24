package com.google.javascript.jscomp;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Collection;
import java.util.List;

import com.google.javascript.jscomp.CodingConvention.AssertionFunctionSpec;
import com.google.javascript.jscomp.CodingConvention.Bind;
import com.google.javascript.jscomp.CodingConvention.ObjectLiteralCast;
import com.google.javascript.jscomp.CodingConvention.SubclassRelationship;
import com.google.javascript.jscomp.CodingConvention.SubclassType;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Class Under Test: com.google.javascript.jscomp.ClosureCodingConvention
 *
 * Targeted Decision Branches & Boundaries:
 * 1. extractClassNameIfRequire / extractClassNameIfProvide (Line 175-207):
 *    - DEFECT TARGET (ClosureCodingConventionTest::testRequire):
 *      When goog.require(arg) has a non-STRING argument (e.g. Token.NAME or Token.NUMBER),
 *      the method must return null. The defective code blindly invokes target.getString()
 *      on Token.NAME, returning "foo" instead of null.
 *    - Parent is not EXPR_RESULT: return null.
 *    - Callee is not GETPROP: return null.
 *    - Callee qualified name != "goog.require" / "goog.provide": return null.
 *    - Callee has no arguments: return null.
 *
 * 2. getClassesDefinedByCall & typeofClassDefiningName (Line 54-154):
 *    - Syntax 1: SubClass.inherits(SuperClass) [isDeprecatedCall, childCount=2, GETPROP]
 *    - Syntax 2: goog.inherits(SubClass, SuperClass) [childCount=3, GETPROP]
 *    - Syntax 3: goog$inherits(SubClass, SuperClass) [childCount=3, NAME with '$']
 *    - Syntax 4: SubClass.mixin(SuperClass.prototype) [isDeprecatedCall, MIXIN]
 *    - Syntax 5: goog.mixin(SubClass.prototype, SuperClass.prototype) [MIXIN, childCount=3]
 *    - Syntax 6: goog$mixin(SubClass.prototype, SuperClass.prototype) [MIXIN, childCount=3]
 *    - Negative Branches:
 *      * Child count < 2 or > 3.
 *      * Child count == 2 but not GETPROP.
 *      * Mixin where superclass does not end with ".prototype".
 *      * Mixin (non-deprecated) where subclass does not end with ".prototype".
 *      * Unscoped qualified name checks failing for subclass or superclass.
 *      * Unrecognized method names or names without '$'.
 *
 * 3. getSingletonGetterClassName (Line 227-240):
 *    - "goog.addSingletonGetter" with childCount == 2: extract class name.
 *    - "goog$addSingletonGetter" with childCount == 2: extract class name.
 *    - False branch: childCount != 2 or unmatched name: return null.
 *
 * 4. identifyTypeDeclarationCall (Line 214-225):
 *    - "goog.addDependency" with childCount >= 3 and ARRAYLIT: extract STRING elements.
 *    - False branches: wrong name, childCount < 3, non-ARRAYLIT argument.
 *
 * 5. describeFunctionBind (Line 312-353):
 *    - Delegates to super.describeFunctionBind first (fn.bind).
 *    - Non-CALL nodes: return null.
 *    - "goog.bind" / "goog$bind" with varying argument lengths (0, 1, 2, 3+).
 *    - "goog.partial" / "goog$partial" with varying argument lengths (0, 1, 2+).
 *
 * 6. getObjectLiteralCast (Line 268-289):
 *    - Precondition check on Token.CALL.
 *    - Name == "goog.reflect.object" and childCount == 3.
 *    - Valid typeNode (qualified name) and objectNode (OBJECTLIT).
 *    - Negative branches: wrong name, bad child count, type not qualified, object not OBJECTLIT.
 *
 * 7. isPropertyTestFunction (Line 262-266):
 *    - Precondition check on Token.CALL.
 *    - True for all 9 recognized Closure property test functions.
 *    - False for unrecognized functions.
 */
public class ClosureCodingConventionGeminiTest {

  private ClosureCodingConvention convention;

  @Before
  public void setUp() {
    convention = new ClosureCodingConvention();
  }

  // Helper to build AST qualified names: e.g. "goog.require"
  private Node createQualifiedName(String name) {
    String[] parts = name.split("\\.");
    Node node = Node.newString(Token.NAME, parts[0]);
    for (int i = 1; i < parts.length; i++) {
      node = new Node(Token.GETPROP, node, Node.newString(parts[i]));
    }
    return node;
  }

  // =========================================================================
  // PARTITION A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testCorePropertiesAndSymbols() {
    assertEquals("superClass_", convention.isSuperClassReference("superClass_") ? "superClass_" : "");
    assertTrue(convention.isSuperClassReference("superClass_"));
    assertFalse(convention.isSuperClassReference("superClass"));
    assertFalse(convention.isSuperClassReference(""));
    assertFalse(convention.isSuperClassReference(null));

    assertEquals("goog.exportProperty", convention.getExportPropertyFunction());
    assertEquals("goog.exportSymbol", convention.getExportSymbolFunction());
    assertEquals("goog.abstractMethod", convention.getAbstractMethodName());
    assertEquals("goog.global", convention.getGlobalObject());
  }

  @Test(timeout = 4000)
  public void testParameterAndVisibilityFlags() {
    Node param = Node.newString(Token.NAME, "arg");
    assertFalse(convention.isOptionalParameter(param));
    assertFalse(convention.isVarArgsParameter(param));
    assertFalse(convention.isPrivate("privateVar_"));
    assertFalse(convention.isPrivate("_privateVar"));
    assertFalse(convention.isPrivate("publicVar"));
  }

  @Test(timeout = 4000)
  public void testAssertionFunctionsList() {
    Collection<AssertionFunctionSpec> assertions = convention.getAssertionFunctions();
    assertNotNull(assertions);
    assertEquals(7, assertions.size());

    boolean foundAssert = false;
    boolean foundAssertNumber = false;
    boolean foundAssertInstanceof = false;

    for (AssertionFunctionSpec spec : assertions) {
      if ("goog.asserts.assert".equals(spec.getFunctionName())) {
        foundAssert = true;
      } else if ("goog.asserts.assertNumber".equals(spec.getFunctionName())) {
        foundAssertNumber = true;
      } else if ("goog.asserts.assertInstanceof".equals(spec.getFunctionName())) {
        foundAssertInstanceof = true;
      }
    }
    assertTrue("Should include goog.asserts.assert", foundAssert);
    assertTrue("Should include goog.asserts.assertNumber", foundAssertNumber);
    assertTrue("Should include goog.asserts.assertInstanceof", foundAssertInstanceof);
  }

  @Test(timeout = 4000)
  public void testExtractClassNameIfProvideValid() {
    Node callee = createQualifiedName("goog.provide");
    Node arg = Node.newString("apps.MyClass");
    Node call = new Node(Token.CALL, callee, arg);
    Node expr = new Node(Token.EXPR_RESULT, call);

    String extracted = convention.extractClassNameIfProvide(call, expr);
    assertEquals("apps.MyClass", extracted);
  }

  @Test(timeout = 4000)
  public void testExtractClassNameIfRequireValid() {
    Node callee = createQualifiedName("goog.require");
    Node arg = Node.newString("apps.MyDep");
    Node call = new Node(Token.CALL, callee, arg);
    Node expr = new Node(Token.EXPR_RESULT, call);

    String extracted = convention.extractClassNameIfRequire(call, expr);
    assertEquals("apps.MyDep", extracted);
  }

  @Test(timeout = 4000)
  public void testGetSingletonGetterClassName() {
    // goog.addSingletonGetter(Foo)
    Node call1 = new Node(Token.CALL,
        createQualifiedName("goog.addSingletonGetter"),
        createQualifiedName("my.company.Foo"));
    assertEquals("my.company.Foo", convention.getSingletonGetterClassName(call1));

    // goog$addSingletonGetter(Bar)
    Node call2 = new Node(Token.CALL,
        Node.newString(Token.NAME, "goog$addSingletonGetter"),
        createQualifiedName("my.company.Bar"));
    assertEquals("my.company.Bar", convention.getSingletonGetterClassName(call2));
  }

  @Test(timeout = 4000)
  public void testIdentifyTypeDeclarationCall() {
    Node arrayLit = new Node(Token.ARRAYLIT,
        Node.newString("my.TypeA"),
        Node.newString("my.TypeB"),
        new Node(Token.NUMBER)); // mixed non-string token should be skipped

    Node call = new Node(Token.CALL,
        createQualifiedName("goog.addDependency"),
        Node.newString("my/file.js"),
        arrayLit);

    List<String> types = convention.identifyTypeDeclarationCall(call);
    assertNotNull(types);
    assertEquals(2, types.size());
    assertEquals("my.TypeA", types.get(0));
    assertEquals("my.TypeB", types.get(1));
  }

  // =========================================================================
  // PARTITION B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testIsPropertyTestFunction() {
    String[] validFunctions = {
        "goog.isDef", "goog.isNull", "goog.isDefAndNotNull",
        "goog.isString", "goog.isNumber", "goog.isBoolean",
        "goog.isFunction", "goog.isArray", "goog.isObject"
    };

    for (String fn : validFunctions) {
      Node call = new Node(Token.CALL, createQualifiedName(fn), Node.newString(Token.NAME, "x"));
      assertTrue("Expected true for " + fn, convention.isPropertyTestFunction(call));
    }

    Node invalidCall = new Node(Token.CALL, createQualifiedName("goog.isDate"), Node.newString(Token.NAME, "x"));
    assertFalse(convention.isPropertyTestFunction(invalidCall));

    Node notGoogCall = new Node(Token.CALL, Node.newString(Token.NAME, "isDef"), Node.newString(Token.NAME, "x"));
    assertFalse(convention.isPropertyTestFunction(notGoogCall));
  }

  @Test(timeout = 4000)
  public void testIdentifyTypeDeclarationCallBoundaries() {
    // Callee not goog.addDependency
    Node wrongNameCall = new Node(Token.CALL,
        createQualifiedName("goog.otherCall"),
        Node.newString("file.js"),
        new Node(Token.ARRAYLIT));
    assertNull(convention.identifyTypeDeclarationCall(wrongNameCall));

    // Fewer than 3 children
    Node insufficientArgs = new Node(Token.CALL,
        createQualifiedName("goog.addDependency"),
        Node.newString("file.js"));
    assertNull(convention.identifyTypeDeclarationCall(insufficientArgs));

    // Third child not ARRAYLIT
    Node nonArrayType = new Node(Token.CALL,
        createQualifiedName("goog.addDependency"),
        Node.newString("file.js"),
        Node.newString(Token.NAME, "notArray"));
    assertNull(convention.identifyTypeDeclarationCall(nonArrayType));

    // Empty ARRAYLIT
    Node emptyArrayCall = new Node(Token.CALL,
        createQualifiedName("goog.addDependency"),
        Node.newString("file.js"),
        new Node(Token.ARRAYLIT));
    List<String> emptyTypes = convention.identifyTypeDeclarationCall(emptyArrayCall);
    assertNotNull(emptyTypes);
    assertTrue(emptyTypes.isEmpty());
  }

  @Test(timeout = 4000)
  public void testGetSingletonGetterBoundaries() {
    // Unmatched name
    Node wrongName = new Node(Token.CALL,
        createQualifiedName("my.addSingletonGetter"),
        createQualifiedName("Foo"));
    assertNull(convention.getSingletonGetterClassName(wrongName));

    // Child count != 2 (0 arguments)
    Node noArgs = new Node(Token.CALL, createQualifiedName("goog.addSingletonGetter"));
    assertNull(convention.getSingletonGetterClassName(noArgs));

    // Child count != 2 (2 arguments)
    Node tooManyArgs = new Node(Token.CALL,
        createQualifiedName("goog.addSingletonGetter"),
        createQualifiedName("Foo"),
        createQualifiedName("Bar"));
    assertNull(convention.getSingletonGetterClassName(tooManyArgs));
  }

  @Test(timeout = 4000)
  public void testDescribeFunctionBindVariations() {
    // Non-CALL node
    assertNull(convention.describeFunctionBind(new Node(Token.NAME, "varName")));

    // Native bind delegated to super.describeFunctionBind
    Node fn = Node.newString(Token.NAME, "fn");
    Node nativeBindCall = new Node(Token.CALL,
        new Node(Token.GETPROP, fn, Node.newString("bind")),
        Node.newString(Token.NAME, "thisObj"),
        Node.newString(Token.NAME, "p1"));
    Bind nativeBind = convention.describeFunctionBind(nativeBindCall);
    assertNotNull(nativeBind);
    assertSame(fn, nativeBind.target);

    // goog.bind with no fn argument
    Node bindNoArgs = new Node(Token.CALL, createQualifiedName("goog.bind"));
    assertNull(convention.describeFunctionBind(bindNoArgs));

    // goog.bind(fn) -> target=fn, thisValue=null, parameters=null
    Node argFn = Node.newString(Token.NAME, "myFn");
    Node bind1 = new Node(Token.CALL, createQualifiedName("goog.bind"), argFn);
    Bind b1 = convention.describeFunctionBind(bind1);
    assertNotNull(b1);
    assertSame(argFn, b1.target);
    assertNull(b1.thisValue);
    assertNull(b1.parameters);

    // goog.bind(fn, self)
    Node self = Node.newString(Token.NAME, "selfObj");
    Node argFn2 = Node.newString(Token.NAME, "myFn2");
    Node bind2 = new Node(Token.CALL, createQualifiedName("goog.bind"), argFn2, self);
    Bind b2 = convention.describeFunctionBind(bind2);
    assertNotNull(b2);
    assertSame(argFn2, b2.target);
    assertSame(self, b2.thisValue);
    assertNull(b2.parameters);

    // goog$bind(fn, self, p1, p2)
    Node p1 = Node.newString(Token.NAME, "p1");
    Node p2 = Node.newString(Token.NAME, "p2");
    Node argFn3 = Node.newString(Token.NAME, "myFn3");
    Node self3 = Node.newString(Token.NAME, "selfObj3");
    Node bind3 = new Node(Token.CALL,
        Node.newString(Token.NAME, "goog$bind"),
        argFn3, self3, p1, p2);
    Bind b3 = convention.describeFunctionBind(bind3);
    assertNotNull(b3);
    assertSame(argFn3, b3.target);
    assertSame(self3, b3.thisValue);
    assertSame(p1, b3.parameters);

    // goog.partial with no arguments
    Node partialNoArgs = new Node(Token.CALL, createQualifiedName("goog.partial"));
    assertNull(convention.describeFunctionBind(partialNoArgs));

    // goog.partial(fn)
    Node partFn = Node.newString(Token.NAME, "partFn");
    Node partial1 = new Node(Token.CALL, createQualifiedName("goog.partial"), partFn);
    Bind bp1 = convention.describeFunctionBind(partial1);
    assertNotNull(bp1);
    assertSame(partFn, bp1.target);
    assertNull(