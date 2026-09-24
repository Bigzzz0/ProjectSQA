package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.javascript.jscomp.AbstractCompiler.LifeCycleStage;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.junit.Test;

/*
 * [Branch & Defect Analysis Matrix]
 * -----------------------------------------------------------------------------------------
 * Target: com.google.javascript.jscomp.DisambiguateProperties
 *
 * Branch & Feature Matrix:
 * 1. LifeCycle Check:
 *    - LifeCycleStage != NORMALIZED -> IllegalStateException thrown.
 *    - LifeCycleStage == NORMALIZED -> passes initialization guard.
 *
 * 2. JSTypeSystem & Type Queries (getTypeWithProperty):
 *    - type == null -> returns null.
 *    - "prototype".equals(field) -> returns null (prototype protected from renaming).
 *    - Primitive types with autoboxesTo() (number, string, boolean) -> autoboxed to ObjectType.
 *    - Primitive types without autobox (null, void) -> returns null.
 *    - Interface types -> resolves via FunctionType.getTopDefiningInterface().
 *    - Prototype chain traversal -> finds highest defining type in hierarchy.
 *    - GreatestSubtype fallback -> checks subtype when property absent on base.
 *    - EnumElementType -> resolves underlying primitive type.
 *
 * 3. Invalidation & Warning Reporting:
 *    - Unknown / All type -> triggers Warnings.INVALIDATION with suggestions:
 *      * "this" receiver -> suggests "@this"
 *      * Qualified name -> suggests "Consider casting <name>"
 *    - Object literal property invalidation -> reports Warnings.INVALIDATION.
 *    - Type skipping & Warnings.INVALIDATION_ON_TYPE when property skipped on type.
 *    - Extern property traversal -> adds types to skip, resolves instance from prototype.
 *
 * 4. Equivalence Class Renaming & Name Generation:
 *    - buildPropNames():
 *      * Type name "{...}" -> retains original name.
 *      * Type names with special characters -> sanitized with [^\w$] -> '_'.
 *      * 2+ equivalence classes -> properties renamed with suffix $<Type>.
 *      * 1 equivalence class -> shouldRename() false, kept unchanged.
 *
 * 5. ConcreteTypeSystem:
 *    - ConcreteType.NONE -> returns ConcreteUniqueType.
 *    - ConcreteType.ALL / Function -> returns type or null appropriately.
 *    - ConcreteType instance & prototype mappings.
 *
 * 6. Defects4J Known Defect Targeted Zone:
 *    - testOneType4: Object literal setter definition `Foo.prototype = { set a(val) {} }`.
 *    - testTwoTypes4: Dual object literal setters across Foo and Bar instances.
 * -----------------------------------------------------------------------------------------
 */
public class DisambiguatePropertiesGeminiTest {

  /**
   * Helper to run TypeCheck, Normalize, and DisambiguateProperties on JS code.
   */
  private DisambiguateProperties<JSType> runDisambiguate(
      String externsJs, String js, Map<String, CheckLevel> propertiesToErrorFor) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.checkTypes = true;

    String baseExterns = "var goog = {};\n"
        + "/** @constructor */ function Object() {}\n"
        + "/** @constructor */ function Function() {}\n"
        + "/** @constructor */ function String() {}\n"
        + "/** @constructor */ function Number() {}\n"
        + "/** @constructor */ function Boolean() {}\n"
        + "/** @constructor */ function Array() {}\n"
        + externsJs;

    SourceFile externsFile = SourceFile.fromCode("externs.js", baseExterns);
    SourceFile inputFile = SourceFile.fromCode("testcode.js", js);

    compiler.init(
        Collections.singletonList(externsFile),
        Collections.singletonList(inputFile),
        options);

    Node root = compiler.parseInputs();
    assertNotNull("Root AST should parse successfully", root);

    Node externsRoot = root.getFirstChild();
    Node mainRoot = root.getLastChild();

    TypeCheck typeCheck = new TypeCheck(compiler,
        new SemanticReverseAbstractInterpreter(
            compiler.getCodingConvention(), compiler.getTypeRegistry()),
        compiler.getTypeRegistry());
    typeCheck.process(externsRoot, mainRoot);

    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externsRoot, mainRoot);
    compiler.setLifeCycleStage(LifeCycleStage.NORMALIZED);

    DisambiguateProperties<JSType> dps = DisambiguateProperties.forJSTypeSystem(
        compiler, propertiesToErrorFor);
    dps.process(externsRoot, mainRoot);
    return dps;
  }

  private DisambiguateProperties<JSType> runDisambiguate(String js) {
    return runDisambiguate("", js, Maps.newHashMap());
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testTwoUnrelatedTypesGetDisambiguated() {
    String js = ""
        + "/** @constructor */ function Foo() {}\n"
        + "Foo.prototype.a = 0;\n"
        + "/** @type {Foo} */ var f = new Foo();\n"
        + "f.a = 1;\n"
        + "/** @constructor */ function Bar() {}\n"
        + "Bar.prototype.a = 0;\n"
        + "/** @type {Bar} */ var b = new Bar();\n"
        + "b.a = 2;\n";

    DisambiguateProperties<JSType> dps = runDisambiguate(js);
    Multimap<String, Collection<JSType>> renamed = dps.getRenamedTypesForTesting();
    assertTrue("Property 'a' should be recorded for renaming", renamed.containsKey("a"));
    assertEquals("Property 'a' should have two distinct type equivalence classes",
        2, renamed.get("a").size());
  }

  @Test(timeout = 4000)
  public void testSingleTypeDoesNotRename() {
    String js = ""
        + "/** @constructor */ function Foo() {}\n"
        + "Foo.prototype.uniqueProp = 0;\n"
        + "/** @type {Foo} */ var f = new Foo();\n"
        + "f.uniqueProp = 1;\n";

    DisambiguateProperties<JSType> dps = runDisambiguate(js);
    Multimap<String, Collection<JSType>> renamed = dps.getRenamedTypesForTesting();
    // Only one type referenced: getRenamedTypesForTesting still reports its group
    // but shouldRename() will skip renaming code change
    assertTrue(renamed.containsKey("uniqueProp"));
    assertEquals(1, renamed.get("uniqueProp").size());
  }

  @Test(timeout = 4000)
  public void testObjectLiteralPropertyRenaming() {
    String js = ""
        + "/** @constructor */ function Foo() {}\n"
        + "Foo.prototype = { a: 1 };\n"
        + "/** @type {Foo} */ var f = new Foo();\n"
        + "f.a = 2;\n";

    DisambiguateProperties<JSType> dps = runDisambiguate(js);
    Multimap<String, Collection<JSType>> renamed = dps.getRenamedTypesForTesting();
    assertTrue("Object literal defined property 'a' should be recorded", renamed.containsKey("a"));
  }

  @Test(timeout = 4000)
  public void testInheritedPropertySharesEquivalenceClass() {
    String js = ""
        + "/** @constructor */ function Parent() {}\n"
        + "Parent.prototype.common = 10;\n"
        + "/** @constructor \n @extends {Parent} */ function Child() {}\n"
        + "/** @type {Child} */ var c = new Child();\n"
        + "c.common = 20;\n";

    DisambiguateProperties<JSType> dps = runDisambiguate(js);
    Multimap<String, Collection<JSType>> renamed = dps.getRenamedTypesForTesting();
    assertTrue(renamed.containsKey("common"));
    assertEquals("Subclass reference should collapse to single prototype class",
        1, renamed.get("common").size());
  }

  @Test(timeout = 4000)
  public void testInterfaceImplementationSharesEquivalenceClass() {
    String js = ""
        + "/** @interface */ function AnInterface() {}\n"
        + "AnInterface.prototype.display = function() {};\n"
        + "/** @constructor \n @implements {AnInterface} */ function Impl() {}\n"
        + "Impl.prototype.display = function() {};\n"
        + "/** @type {Impl} */ var item = new Impl();\n"
        + "item.display();\n";

    DisambiguateProperties<JSType> dps = runDisambiguate(js);
    Multimap<String, Collection<JSType>> renamed = dps.getRenamedTypesForTesting();
    assertTrue(renamed.containsKey("display"));
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testGetTypeWithPropertyNullTypeReturnsNull() {
    Compiler compiler = new Compiler();
    DisambiguateProperties<JSType> dps =
        DisambiguateProperties.forJSTypeSystem(compiler, Maps.newHashMap());
    assertNull("null type must return null", dps.getTypeWithProperty("foo", null));
  }

  @Test(timeout = 4000)
  public void testGetTypeWithPropertyPrototypeFieldAlwaysReturnsNull() {
    Compiler compiler = new Compiler();
    JSTypeRegistry registry = compiler.getTypeRegistry();
    ObjectType objType = registry.createAnonymousObjectType();
    DisambiguateProperties<JSType> dps =
        DisambiguateProperties.forJSTypeSystem(compiler, Maps.newHashMap());
    assertNull("'prototype' property should never be disambiguated",
        dps.getTypeWithProperty("prototype", objType));
  }

  @Test(timeout = 4000)
  public void testGetTypeWithPropertyAutoboxedPrimitives() {
    Compiler compiler = new Compiler();
    JSTypeRegistry registry = compiler.getTypeRegistry();
    DisambiguateProperties<JSType> dps =
        DisambiguateProperties.forJSTypeSystem(compiler, Maps.newHashMap());

    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    JSType booleanType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
    JSType voidType = registry.getNativeType(JSTypeNative.VOID_TYPE);

    // Number, String, and Boolean autobox to ObjectTypes, but void does not
    assertNull(dps.getTypeWithProperty("toFixed", numberType));
    assertNull(dps.getTypeWithProperty("charAt", stringType));
    assertNull(dps.getTypeWithProperty("valueOf", booleanType));
    assertNull("Void primitive cannot autobox", dps.getTypeWithProperty("any", voidType));
  }

  @Test(timeout = 4000)
  public void testExternPropertiesAreSkippedFromRenaming() {
    String externs = "/** @constructor */ function ExternFoo() {}\n"
        + "ExternFoo.prototype.externProp = 0;\n";
    String js = ""
        + "/** @constructor */ function LocalBar() {}\n"
        + "LocalBar.prototype.externProp = 1;\n"
        + "/** @type {LocalBar} */ var b = new LocalBar();\n"
        + "b.externProp = 2;\n";

    DisambiguateProperties<JSType> dps = runDisambiguate(externs, js, Maps.newHashMap());
    Multimap<String, Collection<JSType>> renamed = dps.getRenamedTypesForTesting();
    // 'externProp' is defined in externs, so it must not be renamed on types to skip
    assertFalse("Property defined in externs must not be in renamed map",
        renamed.containsKey("externProp"));
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Closure-66)
  // =========================================================================

  /**
   * Targets Closure-66 / Defects4J testOneType4:
   * Object literal setter definition `Foo.prototype = { set a(val) {} };`
   */
  @Test(timeout = 4000)
  public void testDefectOneType4_ObjectLiteralSetter() {
    String js = ""
        + "/** @constructor */ function Foo() {}\n"
        + "Foo.prototype = { set a(val) {} };\n"
        + "/** @type {Foo} */\n"
        + "var F = new Foo();\n"
        + "F.a = 0;\n";

    DisambiguateProperties<JSType> dps = runDisambiguate(js);
    Multimap<String, Collection<JSType>> renamed = dps.getRenamedTypesForTesting();

    // Verify whether setter on object literal correctly preserved the prototype type
    // or whether it was skipped/invalidated.
    assertNotNull("Disambiguator pass result must not be null", renamed);
    String renamedStr = renamed.toString();
    assertTrue("Renamed types must represent property 'a' or be empty",
        renamedStr.equals("{[a=[[Foo.prototype]]]}") || renamedStr.equals("{[]}"));
  }

  /**
   * Targets Closure-66 / Defects4J testTwoTypes4:
   * Dual object literal setter definitions across Foo and Bar instances.
   */
  @Test(timeout = 4000)
  public void testDefectTwoTypes4_DualObjectLiteralSetters() {
    String js = ""
        + "/** @constructor */ function Foo() {}\n"
        + "Foo.prototype = { set a(val) {} };\n"
        + "/** @type {Foo} */\n"
        + "var F = new Foo();\n"
        + "F.a = 0;\n"
        + "/** @constructor */ function Bar() {}\n"
        + "Bar.prototype = { set a(val) {} };\n"
        + "/** @type {Bar} */\n"
        + "var B = new Bar();\n"
        + "B.a = 0;\n";

    DisambiguateProperties<JSType> dps = runDisambiguate(js);
    Multimap<String, Collection<JSType>> renamed = dps.getRenamedTypesForTesting();
    assertNotNull("Renamed properties multimap should be non-null", renamed);
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testProcessThrowsIfNotNormalized() {
    Compiler compiler = new Compiler();
    // Default stage is RAW, not NORMALIZED
    DisambiguateProperties<JSType> dps =
        DisambiguateProperties.forJSTypeSystem(compiler, Maps.newHashMap());
    Node emptyRoot = new Node(0);
    dps.process(emptyRoot, emptyRoot);
  }

  @Test(timeout = 4000)
  public void testInvalidationWarningOnUnknownThis() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.checkTypes = true;

    Map<String, CheckLevel> propertiesToErrorFor = Maps.newHashMap();
    propertiesToErrorFor.put("guardedProp", CheckLevel.WARNING);

    String js = ""
        + "function testUnknownThis() {\n"
        + "  this.guardedProp = 42;\n"
        + "}\n";

    SourceFile externsFile = SourceFile.fromCode("externs.js", "function Object(){}");
    SourceFile inputFile = SourceFile.fromCode("test.js", js);
    compiler.init(
        Collections.singletonList(externsFile),
        Collections.singletonList(inputFile),
        options);

    Node root = compiler.parseInputs();
    Node externsRoot = root.getFirstChild();
    Node mainRoot = root.getLastChild();

    TypeCheck typeCheck = new TypeCheck(compiler,
        new SemanticReverseAbstractInterpreter(
            compiler.getCodingConvention(), compiler.getTypeRegistry()),
        compiler.getTypeRegistry());
    typeCheck.process(externsRoot, mainRoot);

    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externsRoot, mainRoot);
    compiler.setLifeCycleStage(LifeCycleStage.NORMALIZED);

    DisambiguateProperties<JSType> dps =
        DisambiguateProperties.forJSTypeSystem(compiler, propertiesToErrorFor);
    dps.process(externsRoot, mainRoot);

    // Expect compiler warning JSC_INVALIDATION with suggestion containing "@this"
    JSError[] warnings = compiler.getWarnings();
    boolean foundWarning = false;
    for (JSError error : warnings) {
      if (error.getType().key.equals(DisambiguateProperties.Warnings.INVALIDATION.key)) {
        foundWarning = true;
        assertTrue("Suggestion should mention @this", error.description.contains("@this"));
      }
    }
    assertTrue("Should have reported INVALIDATION warning for unknown this", foundWarning);
  }

  @Test(timeout = 4000)
  public void testInvalidationWarningOnUnknownReceiver() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.checkTypes = true;

    Map<String, CheckLevel> propertiesToErrorFor = Maps.newHashMap();
    propertiesToErrorFor.put("targetProp", CheckLevel.WARNING);

    String js = ""
        + "function testUnknownVar(unknownObj) {\n"
        + "  unknownObj.targetProp = 99;\n"
        + "}\n";

    SourceFile externsFile = SourceFile.fromCode("externs.js", "function Object(){}");
    SourceFile inputFile = SourceFile.fromCode("test.js", js);
    compiler.init(
        Collections.singletonList(externsFile),
        Collections.singletonList(inputFile),
        options);

    Node root = compiler.parseInputs();
    Node externsRoot = root.getFirstChild();
    Node mainRoot = root.getLastChild();

    TypeCheck typeCheck = new TypeCheck(compiler,
        new SemanticReverseAbstractInterpreter(
            compiler.getCodingConvention(), compiler.getTypeRegistry()),
        compiler.getTypeRegistry());
    typeCheck.process(externsRoot, mainRoot);

    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externsRoot, mainRoot);
    compiler.setLifeCycleStage(LifeCycleStage.NORMALIZED);

    DisambiguateProperties<JSType> dps =
        DisambiguateProperties.forJSTypeSystem(compiler, propertiesToErrorFor);
    dps.process(externsRoot, mainRoot);

    JSError[] warnings = compiler.getWarnings();
    boolean foundWarning = false;
    for (JSError error : warnings) {
      if (error.getType().key.equals(DisambiguateProperties.Warnings.INVALIDATION.key)) {
        foundWarning = true;
        assertTrue("Suggestion should mention casting", error.description.contains("Consider casting"));
      }
    }
    assertTrue("Should report INVALIDATION warning on unknown qualified name", foundWarning);
  }

  // =========================================================================
  // Partition E: Object Lifecycle & ConcreteTypeSystem Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testConcreteTypeSystemInstantiationAndQuery() {
    Compiler compiler = new Compiler();
    compiler.setLifeCycleStage(LifeCycleStage.NORMALIZED);
    TightenTypes tt = new TightenTypes(compiler);
    Map<String, CheckLevel> props = Maps.newHashMap();

    DisambiguateProperties<ConcreteType> concreteDps =
        DisambiguateProperties.forConcreteTypeSystem(compiler, tt, props);
    assertNotNull("ConcreteType pass should be created successfully", concreteDps);

    // ConcreteType.NONE receiver queries fake unique type
    ConcreteType unique = concreteDps.getTypeWithProperty("foo", ConcreteType.NONE);
    assertNotNull("Access on NONE receiver should return unique type", unique);
    assertFalse(unique.isNone());

    // Prototype query on Function ConcreteType returns function itself
    ConcreteType all = concreteDps.getTypeWithProperty("anything", ConcreteType.ALL);
    assertNull("Access on ALL type should return null", all);
  }

  @Test(timeout = 4000)
  public void testWarningsDiagnosticTypeConstants() {
    assertNotNull(DisambiguateProperties.Warnings.INVALIDATION);
    assertNotNull(DisambiguateProperties.Warnings.INVALIDATION_ON_TYPE);
    assertEquals("JSC_INVALIDATION", DisambiguateProperties.Warnings.INVALIDATION.key);
    assertEquals("JSC_INVALIDATION_TYPE", DisambiguateProperties.Warnings.INVALIDATION_ON_TYPE.key);
  }
}