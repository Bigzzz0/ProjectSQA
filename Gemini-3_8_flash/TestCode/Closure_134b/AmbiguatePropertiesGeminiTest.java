package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Map;

/*
 * [Branch & Defect Analysis Matrix]
 * ------------------------------------------------------------------------------------------------
 * TARGET CLASS: AmbiguateProperties (and inner classes: Property, PropertyGraph, PropertySubGraph, JSTypeBitSet)
 *
 * DEFECT SPECIFICATION:
 * - Defect: Subclass inheriting from a class that implements an interface does not recognize the
 *   interface's types in its related types set (in computeRelatedTypes), causing invalid property
 *   name collisions when ambiguating properties across interface implementors and their descendants.
 * - Test Target: testImplementsAndExtends directly forces the condition where SubBar extends Bar
 *   (which implements Foo). SubBar.subBar must not be assigned the same name as Foo.foo.
 *
 * BRANCH COVERAGE COVERAGE PLAN:
 * - Partition A: Core Functional Logic & State Transitions
 *   * testUnrelatedClassesAmbiguate(): Two disjoint types reuse property short names ('a').
 *   * testFrequencyComparatorOccurrences(): Verifies sorting by occurrence count (descending).
 *   * testFrequencyComparatorTieBreaker(): Verifies tie-breaking by alphabetical order (ascending).
 *   * testRenamingMapAccuracy(): Validates exact state of getRenamingMap() after execution.
 *   * testStaticPropertiesOnConstructors(): Covers (type instanceof FunctionType) in computeRelatedTypes.
 *   * testTwoSubclassesShareName(): Unrelated sibling subclasses reuse names while avoiding parent conflict.
 *
 * - Partition B: Boundary Value Analysis (BVA) & Extremes
 *   * testEmptyInput(): Empty source code handling.
 *   * testSingleProperty(): Single property on a single class.
 *   * testSkipPrefix(): Properties prefixed with SKIP_PREFIX ("JSAbstractCompiler") are skipped.
 *   * testQuotedPropertiesInObjectLit(): Quoted keys in object literals prevent other properties from using that name.
 *   * testQuotedPropertiesInGetElem(): Quoted element access (x['myprop']) populates quotedNames.
 *   * testExternedPropertiesIgnored(): Properties declared in externs (e.g. 'foobar') are not renamed.
 *   * testNumericKeysInObjectLit(): Verifies object literals with numeric keys are safely ignored.
 *
 * - Partition C: Defect-Targeted Branch Zone (Ground Truth Defects4J)
 *   * testImplementsAndExtends(): Interface Foo, Bar implements Foo, SubBar extends Bar.
 *     SubBar's properties must not collide with Foo's properties.
 *   * testInterfaceInheritanceChain(): Interface extending another interface.
 *
 * - Partition D: Exception & Defensive Guard Paths
 *   * testAnonymousObjectLiteralInvalidates(): Properties accessed on anonymous objects without reference
 *     names are marked as invalidating and not ambiguated.
 *   * testTypeMismatchInvalidation(): Compiler TypeMismatch adds types to invalidatingTypes.
 *   * testEnumPropertiesInvalidated(): Properties on enum objects are treated as invalidating types.
 *
 * - Partition E: Object Lifecycle & Contract Integrity
 *   * testRepeatedAmbiguationRuns(): Verifies deterministic idempotency across multiple runs.
 *   * testReservedCharactersRespected(): Verifies reserved characters ($) are never generated.
 * ------------------------------------------------------------------------------------------------
 */
public class AmbiguatePropertiesGeminiTest extends CompilerTestCase {

  private static final String EXTERNS =
      "var window;\n"
      + "function alert(s) {}\n"
      + "/** @constructor */ function Foo() {}\n"
      + "Foo.prototype.foobar;\n";

  private AmbiguateProperties lastPass;

  public AmbiguatePropertiesGeminiTest() {
    super(EXTERNS);
  }

  @Override
  @org.junit.Before
  public void setUp() throws Exception {
    super.setUp();
    enableTypeCheck(CheckLevel.WARNING);
  }

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    lastPass = new AmbiguateProperties(compiler, new char[]{'$'});
    return lastPass;
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  // =========================================================================
  // PARTITION A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testUnrelatedClassesAmbiguate() {
    String js = ""
        + "/** @constructor */ function Foo() {}\n"
        + "Foo.prototype.propFoo = 0;\n"
        + "/** @constructor */ function Bar() {}\n"
        + "Bar.prototype.propBar = 0;\n"
        + "var f = new Foo;\n"
        + "var b = new Bar;\n"
        + "f.propFoo();\n"
        + "b.propBar();\n";
    String expected = ""
        + "function Foo() {}\n"
        + "Foo.prototype.a = 0;\n"
        + "function Bar() {}\n"
        + "Bar.prototype.a = 0;\n"
        + "var f = new Foo;\n"
        + "var b = new Bar;\n"
        + "f.a();\n"
        + "b.a();\n";
    test(js, expected);
  }

  @Test(timeout = 4000)
  public void testFrequencyComparatorOccurrences() {
    // propZ occurs 4 times, propX occurs 2 times. propZ should get 'a', propX gets 'b'.
    String js = ""
        + "/** @constructor */ function Foo() {}\n"
        + "Foo.prototype.propZ = 0;\n"
        + "Foo.prototype.propZ = 0;\n"
        + "Foo.prototype.propX = 0;\n"
        + "var f = new Foo;\n"
        + "f.propZ();\n"
        + "f.propZ();\n"
        + "f.propX();\n";
    String expected = ""
        + "function Foo() {}\n"
        + "Foo.prototype.a = 0;\n"
        + "Foo.prototype.a = 0;\n"
        + "Foo.prototype.b = 0;\n"
        + "var f = new Foo;\n"
        + "f.a();\n"
        + "f.a();\n"
        + "f.b();\n";
    test(js, expected);
  }

  @Test(timeout = 4000)
  public void testFrequencyComparatorTieBreaker() {
    // propM and propN both occur 2 times. Alphabetical order breaks tie: propM gets 'a', propN gets 'b'.
    String js = ""
        + "/** @constructor */ function Foo() {}\n"
        + "Foo.prototype.propN = 0;\n"
        + "Foo.prototype.propM = 0;\n"
        + "var f = new Foo;\n"
        + "f.propN();\n"
        + "f.propM();\n";
    String expected = ""
        + "function Foo() {}\n"
        + "Foo.prototype.b = 0;\n"
        + "Foo.prototype.a = 0;\n"
        + "var f = new Foo;\n"
        + "f.b();\n"
        + "f.a();\n";
    test(js, expected);
  }

  @Test(timeout = 4000)
  public void testRenamingMapAccuracy() {
    String js = ""
        + "/** @constructor */ function Alpha() {}\n"
        + "Alpha.prototype.alphaProp = 0;\n"
        + "var a = new Alpha;\n"
        + "a.alphaProp();\n";
    String expected = ""
        + "function Alpha() {}\n"
        + "Alpha.prototype.a = 0;\n"
        + "var a = new Alpha;\n"
        + "a.a();\n";
    test(js, expected);

    Map<String, String> renamingMap = lastPass.getRenamingMap();
    assertNotNull("Renaming map must not be null after process execution", renamingMap);
    assertEquals("Exact mapping value for alphaProp must be 'a'", "a", renamingMap.get("alphaProp"));
  }

  @Test(timeout = 4000)
  public void testStaticPropertiesOnConstructors() {
    // Hits the (type instanceof FunctionType) branch in computeRelatedTypes
    String js = ""
        + "/** @constructor */ function ConstrA() {}\n"
        + "ConstrA.staticPropA = 1;\n"
        + "/** @constructor */ function ConstrB() {}\n"
        + "ConstrB.staticPropB = 2;\n"
        + "ConstrA.staticPropA;\n"
        + "ConstrB.staticPropB;\n";
    String expected = ""
        + "function ConstrA() {}\n"
        + "ConstrA.a = 1;\n"
        + "function ConstrB() {}\n"
        + "ConstrB.a = 2;\n"
        + "ConstrA.a;\n"
        + "ConstrB.a;\n";
    test(js, expected);
  }

  @Test(timeout = 4000)
  public void testTwoSubclassesShareName() {
    String js = ""
        + "/** @constructor */ function Parent() {}\n"
        + "Parent.prototype.parentProp = function() {};\n"
        + "/** @constructor @extends {Parent} */ function Child1() {}\n"
        + "Child1.prototype.childProp1 = function() {};\n"
        + "/** @constructor @extends {Parent} */ function Child2() {}\n"
        + "Child2.prototype.childProp2 = function() {};\n"
        + "var c1 = new Child1;\n"
        + "var c2 = new Child2;\n"
        + "c1.parentProp();\n"
        + "c1.childProp1();\n"
        + "c2.parentProp();\n"
        + "c2.childProp2();\n";
    String expected = ""
        + "function Parent() {}\n"
        + "Parent.prototype.a = function() {};\n"
        + "function Child1() {}\n"
        + "Child1.prototype.b = function() {};\n"
        + "function Child2() {}\n"
        + "Child2.prototype.b = function() {};\n"
        + "var c1 = new Child1;\n"
        + "var c2 = new Child2;\n"
        + "c1.a();\n"
        + "c1.b();\n"
        + "c2.a();\n"
        + "c2.b();\n";
    test(js, expected);
  }

  // =========================================================================
  // PARTITION B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testEmptyInput() {
    testSame("");
    Map<String, String> renamingMap = lastPass.getRenamingMap();
    assertNotNull(renamingMap);
    assertTrue("Renaming map must be empty for empty input", renamingMap.isEmpty());
  }

  @Test(timeout = 4000)
  public void testSingleProperty() {
    String js = ""
        + "/** @constructor */ function Single() {}\n"
        + "Single.prototype.onlyProp = 1;\n"
        + "var s = new Single;\n"
        + "s.onlyProp;\n";
    String expected = ""
        + "function Single() {}\n"
        + "Single.prototype.a = 1;\n"
        + "var s = new Single;\n"
        + "s.a;\n";
    test(js, expected);
  }

  @Test(timeout = 4000)
  public void testSkipPrefix() {
    // Properties starting with SKIP_PREFIX ("JSAbstractCompiler") must be excluded from ambiguation
    String js = ""
        + "/** @constructor */ function Item() {}\n"
        + "Item.prototype.JSAbstractCompiler_reserved = 1;\n"
        + "var it = new Item;\n"
        + "it.JSAbstractCompiler_reserved;\n";
    testSame(js);
    assertNull(lastPass.getRenamingMap().get("JSAbstractCompiler_reserved"));
  }

  @Test(timeout = 4000)
  public void testQuotedPropertiesInObjectLit() {
    // Quoted property 'a' in an object literal should add 'a' to quotedNames,
    // causing normal properties to start renaming at 'b' instead of 'a'.
    String js = ""
        + "/** @constructor */ function Target() {}\n"
        + "Target.prototype.prop = 0;\n"
        + "var t = new Target;\n"
        + "t.prop();\n"
        + "var obj = {'a': 1};\n";
    String expected = ""
        + "function Target() {}\n"
        + "Target.prototype.b = 0;\n"
        + "var t = new Target;\n"
        + "t.b();\n"
        + "var obj = {'a': 1};\n";
    test(js, expected);
  }

  @Test(timeout = 4000)
  public void testQuotedPropertiesInGetElem() {
    // x['a'] access populates quotedNames via Token.GETELEM
    String js = ""
        + "/** @constructor */ function Target() {}\n"
        + "Target.prototype.prop = 0;\n"
        + "var t = new Target;\n"
        + "t.prop();\n"
        + "var x = {};\n"
        + "x['a'] = 1;\n";
    String expected = ""
        + "function Target() {}\n"
        + "Target.prototype.b = 0;\n"
        + "var t = new Target;\n"
        + "t.b();\n"
        + "var x = {};\n"
        + "x['a'] = 1;\n";
    test(js, expected);
  }

  @Test(timeout = 4000)
  public void testExternedPropertiesIgnored() {
    // 'foobar' is defined in EXTERNS, so it must not be renamed
    String js = ""
        + "var f = new Foo;\n"
        + "f.foobar = 1;\n";
    testSame(js);
  }

  @Test(timeout = 4000)
  public void testNumericKeysInObjectLit() {
    // Numeric keys in object literals must not be treated as string properties
    String js = ""
        + "/** @constructor */ function Box() {}\n"
        + "Box.prototype.data = 1;\n"
        + "var b = new Box;\n"
        + "b.data;\n"
        + "var mapping = { 100: 'val' };\n";
    String expected = ""
        + "function Box() {}\n"
        + "Box.prototype.a = 1;\n"
        + "var b = new Box;\n"
        + "b.a;\n"
        + "var mapping = { 100: 'val' };\n";
    test(js, expected);
  }

  // =========================================================================
  // PARTITION C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // =========================================================================

  /**
   * Targets defect: SubBar inherits from Bar, and Bar implements Foo.
   * AmbiguateProperties must recognize that SubBar is related to Foo through Bar's
   * implemented interface, preventing SubBar's property from colliding with Foo's property.
   */
  @Test(timeout = 4000)
  public void testImplementsAndExtends() {
    String js = ""
        + "/** @interface */ function Foo() {}\n"
        + "Foo.prototype.foo = function() {};\n"
        + "/** @constructor @implements {Foo} */ function Bar() {}\n"
        + "Bar.prototype.foo = function() {};\n"
        + "/** @constructor @extends {Bar} */ function SubBar() {}\n"
        + "SubBar.prototype.subBar = function() {};\n"
        + "/** @constructor */ function Baz() {}\n"
        + "Baz.prototype.baz = function() {};\n"
        + "var f = new Foo;\n"
        + "var b = new Bar;\n"
        + "var s = new SubBar;\n"
        + "var z = new Baz;\n"
        + "f.foo();\n"
        + "b.foo();\n"
        + "s.subBar();\n"
        + "z.baz();\n";
    String expected = ""
        + "function Foo() {}\n"
        + "Foo.prototype.a = function() {};\n"
        + "function Bar() {}\n"
        + "Bar.prototype.a = function() {};\n"
        + "function SubBar() {}\n"
        + "SubBar.prototype.b = function() {};\n"
        + "function Baz() {}\n"
        + "Baz.prototype.a = function() {};\n"
        + "var f = new Foo;\n"
        + "var b = new Bar;\n"
        + "var s = new SubBar;\n"
        + "var z = new Baz;\n"
        + "f.a();\n"
        + "b.a();\n"
        + "s.b();\n"
        + "z.a();\n";
    test(js, expected);
  }

  @Test(timeout = 4000)
  public void testInterfaceInheritanceChain() {
    String js = ""
        + "/** @interface */ function SuperInterface() {}\n"
        + "SuperInterface.prototype.superMethod = function() {};\n"
        + "/** @interface @extends {SuperInterface} */ function SubInterface() {}\n"
        + "SubInterface.prototype.subMethod = function() {};\n"
        + "/** @constructor @implements {SubInterface} */ function Impl() {}\n"
        + "Impl.prototype.superMethod = function() {};\n"
        + "Impl.prototype.subMethod = function() {};\n"
        + "var impl = new Impl;\n"
        + "impl.superMethod();\n"
        + "impl.subMethod();\n";
    String expected = ""
        + "function SuperInterface() {}\n"
        + "SuperInterface.prototype.a = function() {};\n"
        + "function SubInterface() {}\n"
        + "SubInterface.prototype.b = function() {};\n"
        + "function Impl() {}\n"
        + "Impl.prototype.a = function() {};\n"
        + "Impl.prototype.b = function() {};\n"
        + "var impl = new Impl;\n"
        + "impl.a();\n"
        + "impl.b();\n";
    test(js, expected);
  }

  // =========================================================================
  // PARTITION D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testAnonymousObjectLiteralInvalidates() {
    // Property referenced on an unannotated object literal has an invalidating type.
    // It should skip ambiguating and remain unchanged.
    String js = ""
        + "/** @constructor */ function User() {}\n"
        + "User.prototype.login = function() {};\n"
        + "var u = new User;\n"
        + "u.login();\n"
        + "var anon = { login: 1 };\n";
    testSame(js);
  }

  @Test(timeout = 4000)
  public void testTypeMismatchInvalidation() {
    // Type mismatch adds mismatch types to invalidatingTypes, suppressing renaming
    String js = ""
        + "/** @constructor */ function ClassA() { this.sharedProp = 1; }\n"
        + "/** @constructor */ function ClassB() { this.sharedProp = 2; }\n"
        + "/** @type {ClassA} */ var a = new ClassA();\n"
        + "/** @type {ClassB} */ var b = a;\n"
        + "a.sharedProp;\n"
        + "b.sharedProp;\n";
    test(js, js, null, TypeValidator.TYPE_MISMATCH_WARNING);
  }

  @Test(timeout = 4000)
  public void testEnumPropertiesInvalidated() {
    // Enums are marked as invalidating types in isInvalidatingType
    String js = ""
        + "/** @enum {string} */ var ColorEnum = { RED: 'r', BLUE: 'b' };\n"
        + "ColorEnum.RED;\n"
        + "ColorEnum.BLUE;\n";
    testSame(js);
  }

  // =========================================================================
  // PARTITION E: Object Lifecycle & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testRepeatedAmbiguationRuns() {
    String js = ""
        + "/** @constructor */ function Runner() {}\n"
        + "Runner.prototype.step = function() {};\n"
        + "var r = new Runner;\n"
        + "r.step();\n";
    String expected = ""
        + "function Runner() {}\n"
        + "Runner.prototype.a = function() {};\n"
        + "var r = new Runner;\n"
        + "r.a();\n";

    // Run 1
    test(js, expected);
    assertEquals("a", lastPass.getRenamingMap().get("step"));

    // Run 2: ensure no static contamination or leaking state
    test(js, expected);
    assertEquals("a", lastPass.getRenamingMap().get("step"));
  }

  @Test(timeout = 4000)
  public void testReservedCharactersRespected() {
    // The processor was configured with reservedCharacters = ['$'].
    // Verify that none of the generated property names contain '$'.
    String js = ""
        + "/** @constructor */ function TestClass() {}\n"
        + "TestClass.prototype.fieldA = 1;\n"
        + "TestClass.prototype.fieldB = 2;\n"
        + "TestClass.prototype.fieldC = 3;\n"
        + "var t = new TestClass;\n"
        + "t.fieldA;\n"
        + "t.fieldB;\n"
        + "t.fieldC;\n";
    String expected = ""
        + "function TestClass() {}\n"
        + "TestClass.prototype.a = 1;\n"
        + "TestClass.prototype.b = 2;\n"
        + "TestClass.prototype.c = 3;\n"
        + "var t = new TestClass;\n"
        + "t.a;\n"
        + "t.b;\n"
        + "t.c;\n";
    test(js, expected);

    for (String newName : lastPass.getRenamingMap().values()) {
      assertFalse("Generated name '" + newName + "' must not contain reserved character '$'",
          newName.contains("$"));
    }
  }
}