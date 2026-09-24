package com.google.javascript.jscomp;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * =========================================================================
 * Target: com.google.javascript.jscomp.DisambiguateProperties
 * Primary Defect Target: Closure-103 (Supertype reference of subtype property)
 *
 * Decision / Branch Matrix:
 * 1. getTypeWithProperty (JSTypeSystem):
 *    - field == "prototype": Returns null (Branch: prototype guard).
 *    - type not ObjectType: autoboxesTo() != null (autoboxing branch) vs null.
 *    - prototype chain loop: objType.hasOwnProperty(field).
 *    - DEFECT ZONE: field missing on referenced supertype, but present on subtype.
 *      Bug: returns null -> invalidates the field globally across all instances.
 *      Fix: locates subtype via registry.getGreatestSubtypeWithProperty().
 * 2. processProperty (FindRenameableProperties):
 *    - prop.skipRenaming || isInvalidatingType: prune traversal.
 *    - type alternatives != null (Unions, Interfaces): recurse and link alternatives.
 *    - topType is invalidating: returns null.
 *    - addType / recordInterfaces: link equivalence classes.
 * 3. expandTypesToSkip:
 *    - Iterative expansion of typesToSkip with union-find roots.
 * 4. renameProperties:
 *    - shouldRename() checks: !skipRenaming && classes.size() > 1.
 *    - shouldRename(rootType) checks: !skipRenaming && !typesToSkip.contains(type).
 * 5. Property state guards:
 *    - addType when skipRenaming == true -> IllegalStateException.
 * =========================================================================
 */
public class DisambiguatePropertiesGeminiTest extends CompilerTestCase {

  private Compiler compiler;
  private DisambiguateProperties<JSType> lastPass;

  public DisambiguatePropertiesGeminiTest() {
    super();
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    enableTypeCheck(CheckLevel.WARNING);
  }

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    this.compiler = compiler;
    return lastPass = DisambiguateProperties.forJSTypeSystem(compiler);
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  /**
   * Helper that runs compilation and asserts equivalence classes tracked by the pass.
   */
  private void testSets(String js, String expected) {
    testSame(js);
    assertEquals(expected, mapToString(lastPass.getRenamedTypesForTesting()));
  }

  private <T> String mapToString(Multimap<String, Collection<T>> map) {
    Map<String, List<String>> ret = Maps.newTreeMap();
    for (String key : map.keySet()) {
      List<String> entries = Lists.newArrayList();
      for (Collection<T> collection : map.get(key)) {
        List<String> inner = Lists.newArrayList();
        for (T t : collection) {
          inner.add(t.toString());
        }
        Collections.sort(inner);
        entries.add(inner.toString());
      }
      Collections.sort(entries);
      ret.put(key, entries);
    }
    return ret.toString();
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Ground Truth: Closure-103)
  // =========================================================================

  /**
   * Targets the defect where a property is referenced on a supertype that does
   * not declare it, but the property exists on a subtype.
   * Buggy behavior: getTypeWithProperty returns null, causing property invalidation.
   */
  @Test(timeout = 4000)
  public void testSupertypeReferenceOfSubtypeProperty() {
    String js = ""
        + "/** @constructor */ function Foo() {}\n"
        + "/** @constructor\n * @extends {Foo} */ function Bar() {}\n"
        + "Bar.prototype.bar = 0;\n"
        + "function foo(x) {\n"
        + "  var y = /** @type {Foo} */ (x);\n"
        + "  return y.bar;\n"
        + "}\n"
        + "foo(new Bar);";
    testSets(js, "{bar=[[Bar.prototype]]}");
  }

  /**
   * Direct inspection of getTypeWithProperty when called with a supertype
   * reference whose property only exists on an extending subtype.
   */
  @Test(timeout = 4000)
  public void testSupertypeReferenceOfSubtypePropertyDirect() {
    String js = ""
        + "/** @constructor */ function Base() {}\n"
        + "/** @constructor\n * @extends {Base} */ function Sub() {}\n"
        + "Sub.prototype.subProp = 1;\n";
    testSame(js);
    JSTypeRegistry registry = compiler.getTypeRegistry();
    JSType baseType = registry.getType("Base");
    assertNotNull("Base type should exist in registry", baseType);

    JSType found = lastPass.getTypeWithProperty("subProp", baseType);
    assertNotNull("Subtype property must be discoverable from supertype reference", found);
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testTwoUnrelatedTypesSamePropertyRenamed() {
    String js = ""
        + "/** @constructor */ function Foo() {}\n"
        + "Foo.prototype.a = 0;\n"
        + "/** @constructor */ function Bar() {}\n"
        + "Bar.prototype.a = 0;\n"
        + "new Foo().a;\n"
        + "new Bar().a;";
    String expected = ""
        + "/** @constructor */ function Foo() {}\n"
        + "Foo.prototype.Foo_prototype$a = 0;\n"
        + "/** @constructor */ function Bar() {}\n"
        + "Bar.prototype.Bar_prototype$a = 0;\n"
        + "new Foo().Foo_prototype$a;\n"
        + "new Bar().Bar_prototype$a;";
    test(js, expected);
  }

  @Test(timeout = 4000)
  public void testSingleTypeDoesNotRename() {
    String js = ""
        + "/** @constructor */ function Foo() {}\n"
        + "Foo.prototype.uniqueProp = 0;\n"
        + "new Foo().uniqueProp;";
    testSame(js);
    assertEquals("{uniqueProp=[[Foo.prototype]]}",
        mapToString(lastPass.getRenamedTypesForTesting()));
  }

  @Test(timeout = 4000)
  public void testMultiplePropertiesOnDifferentClasses() {
    String js = ""
        + "/** @constructor */ function Foo() {}\n"
        + "Foo.prototype.x = 1;\n"
        + "Foo.prototype.y = 2;\n"
        + "/** @constructor */ function Bar() {}\n"
        + "Bar.prototype.x = 3;\n"
        + "Bar.prototype.y = 4;\n"
        + "new Foo().x; new Foo().y;\n"
        + "new Bar().x; new Bar().y;";
    String expected = ""
        + "/** @constructor */ function Foo() {}\n"
        + "Foo.prototype.Foo_prototype$x = 1;\n"
        + "Foo.prototype.Foo_prototype$y = 2;\n"
        + "/** @constructor */ function Bar() {}\n"
        + "Bar.prototype.Bar_prototype$x = 3;\n"
        + "Bar.prototype.Bar_prototype$y = 4;\n"
        + "new Foo().Foo_prototype$x; new Foo().Foo_prototype$y;\n"
        + "new Bar().Bar_prototype$x; new Bar().Bar_prototype$y;";
    test(js, expected);
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testAnonymousObjectTypeSkipsRenaming() {
    String js = ""
        + "/** @constructor */ function Foo() {}\n"
        + "Foo.prototype.a = 0;\n"
        + "var anon = {a: 1};\n"
        + "new Foo().a;";
    // Anonymous object has no reference name -> invalidating -> skips renaming
    testSets(js, "{}");
  }

  @Test(timeout = 4000)
  public void testPrototypePropertyIsNeverDisambiguated() {
    String js = ""
        + "function Foo() {}\n"
        + "function Bar() {}\n"
        + "Foo.prototype;\n"
        + "Bar.prototype;";
    testSame(js);
    assertTrue(lastPass.getRenamedTypesForTesting().isEmpty());
  }

  @Test(timeout = 4000)
  public void testExternPropertiesAreSkipped() {
    String externs = "var window; window.alert = function() {};";
    String js = ""
        + "/** @constructor */ function Foo() {}\n"
        + "Foo.prototype.alert = 1;\n"
        + "window.alert();\n"
        + "new Foo().alert;";
    testSame(externs, js);
  }

  @Test(timeout = 4000)
  public void testAutoboxedTypeProperties() {
    testSame("var x = 'hello'.length;");
    JSTypeRegistry registry = compiler.getTypeRegistry();
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    JSType typeWithProp = lastPass.getTypeWithProperty("charAt", stringType);
    assertNotNull("Autoboxed string must contain charAt property", typeWithProp);
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testAddTypeOnSkippedPropertyThrows() {
    testSame("/** @constructor */ function A() {}");
    DisambiguateProperties<JSType>.Property prop = lastPass.getProperty("invalidatedField");
    prop.invalidate();
    assertTrue(prop.skipRenaming);
    JSType objType = compiler.getTypeRegistry().getNativeType(JSTypeNative.OBJECT_TYPE);
    // Precondition failure: Attempt to record skipped property
    prop.addType(objType, objType, null);
  }

  @Test(timeout = 4000)
  public void testTypeWithPropertyReturnsNullForPrototypeField() {
    testSame("/** @constructor */ function Foo() {}");
    JSTypeRegistry registry = compiler.getTypeRegistry();
    JSType fooType = registry.getType("Foo");
    assertNull("Prototype property lookup should immediately return null",
        lastPass.getTypeWithProperty("prototype", fooType));
  }

  @Test(timeout = 4000)
  public void testTypeWithPropertyReturnsNullForNonExistentField() {
    testSame("/** @constructor */ function Foo() {}");
    JSTypeRegistry registry = compiler.getTypeRegistry();
    JSType numType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    assertNull(lastPass.getTypeWithProperty("completelyUnknownProp_XYZ", numType));
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testUnionTypeUnionsEquivalenceClasses() {
    String js = ""
        + "/** @constructor */ function Foo() {}\n"
        + "Foo.prototype.a = 0;\n"
        + "/** @constructor */ function Bar() {}\n"
        + "Bar.prototype.a = 0;\n"
        + "/** @type {Foo|Bar} */ var x;\n"
        + "x.a;";
    testSets(js, "{a=[[Bar.prototype, Foo.prototype]]}");
  }

  @Test(timeout = 4000)
  public void testInterfaceAndImplementorsTracked() {
    String js = ""
        + "/** @interface */ function I() {}\n"
        + "I.prototype.a = function() {};\n"
        + "/** @constructor\n * @implements {I} */ function Foo() {}\n"
        + "Foo.prototype.a = function() {};\n"
        + "/** @type {I} */ var i;\n"
        + "i.a();";
    testSets(js, "{a=[[Foo.prototype, I.prototype]]}");
  }

  @Test(timeout = 4000)
  public void testTypeMismatchesInvalidateTypes() {
    String js = ""
        + "/** @constructor */ function Foo() {}\n"
        + "Foo.prototype.a = 0;\n"
        + "/** @constructor */ function Bar() {}\n"
        + "Bar.prototype.a = 0;\n"
        + "/** @type {Foo} */ var f = new Foo();\n"
        + "/** @type {Bar} */ var b = new Bar();\n"
        + "f = b;\n"
        + "f.a;\n"
        + "b.a;";
    // Type mismatch records both Foo and Bar as invalidating
    testSets(js, "{}");
  }

  @Test(timeout = 4000)
  public void testConcreteTypeSystemInstantiation() {
    TightenTypes tt = new TightenTypes(compiler);
    DisambiguateProperties<ConcreteType> concretePass =
        DisambiguateProperties.forConcreteTypeSystem(compiler, tt);
    assertNotNull("ConcreteType disambiguator should be instantiable", concretePass);
  }
}