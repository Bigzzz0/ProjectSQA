package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

/* [Branch & Defect Analysis Matrix]
 *
 * Defect target:
 * - AmbiguatePropertiesTest::testImplementsAndExtends
 *   In the buggy implementation, properties accessed on a subtype and on its
 *   supertype are not always considered related when the subtype is described
 *   only through constructor `this` assignments.  This lets the graph coloring
 *   collapse distinct properties such as `baseProp` and `derivedProp` to the
 *   same generated name.  The tests below assert that supertypes and subtypes
 *   never share a renamed property.
 *
 * - TypeCheckTest::testIssue86
 *   Guarded by exercising the full property ambiguity pass on typed classes,
 *   interfaces, union types, extern/quoted reserved names, and invalidating
 *   types so that malformed or incomplete renaming cannot hide type warnings.
 *
 * Branch/decision coverage targets:
 *   A. ProcessExterns: GETPROP and OBJECTLIT reservation paths.
 *   B. ProcessProperties: GETPROP, OBJECTLIT quoted/unquoted, GETELEM.
 *   C. Property.addType: union restriction/decomposition and skip guard.
 *   D. Property.addNonUnionType: invalidating type and first-type transitions.
 *   E. PropertySubGraph adjacency decisions: unrelated sharing and related
 *      separation.
 *   F. Name generation reservation: quoted names, extern names, skip prefix.
 *   G. Invalidation: UNKNOWN_TYPE and anonymous object type.
 */
public class AmbiguatePropertiesDeepseekTest {

  private static class Compilation {
    final Compiler compiler;
    final Map<String, String> renamingMap;

    Compilation(Compiler compiler, Map<String, String> renamingMap) {
      this.compiler = compiler;
      this.renamingMap = renamingMap;
    }
  }

  private static void enableCheckTypes(CompilerOptions options) {
    try {
      options.getClass().getField("checkTypes").setBoolean(options, true);
    } catch (Exception ignored) {
      try {
        options.getClass().getMethod("setCheckTypes", boolean.class)
            .invoke(options, Boolean.TRUE);
      } catch (Exception e) {
        throw new RuntimeException("Unable to enable type checking", e);
      }
    }
  }

  private Compilation ambiguate(String source) {
    return ambiguate("", source, new char[0]);
  }

  private Compilation ambiguate(String externs, String source) {
    return ambiguate(externs, source, new char[0]);
  }

  private Compilation ambiguate(String externs, String source, char[] reserved) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    enableCheckTypes(options);

    Result result = compiler.compile(
        SourceFile.fromCode("externs", externs),
        SourceFile.fromCode("test", source),
        options);
    assertTrue("Compilation failed: " + result.errors, result.success);

    AmbiguateProperties pass = new AmbiguateProperties(compiler, reserved);
    pass.process(compiler.getExternsRoot(), compiler.getJsRoot());
    return new Compilation(compiler, pass.getRenamingMap());
  }

  private static List<String> getPropStrings(Node node) {
    List<String> props = new ArrayList<String>();
    if (node == null) {
      return props;
    }
    if (node.getType() == Token.GETPROP) {
      Node propNode = node.getFirstChild().getNext();
      if (propNode != null) {
        props.add(propNode.getString());
      }
    }
    for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
      props.addAll(getPropStrings(child));
    }
    return props;
  }

  @Test(timeout = 4000)
  public void testUnrelatedPropertiesMayShareShortName() {
    String source =
        "/** @constructor */ function Alpha() { this.x = 1; }\n"
        + "/** @constructor */ function Beta() { this.y = 2; }\n";
    Map<String, String> renaming = ambiguate(source).renamingMap;

    assertTrue(renaming.containsKey("x"));
    assertTrue(renaming.containsKey("y"));
    assertTrue("Unrelated properties should be allowed to share a short name",
        renaming.get("x").equals(renaming.get("y")));
  }

  @Test(timeout = 4000)
  public void testPropertiesOnSameTypeGetDistinctNames() {
    String source =
        "/** @constructor */ function Gamma() { this.a = 1; this.b = 2; }\n";
    Map<String, String> renaming = ambiguate(source).renamingMap;

    assertTrue(renaming.containsKey("a"));
    assertTrue(renaming.containsKey("b"));
    assertFalse("Properties on the same type must not collapse",
        renaming.get("a").equals(renaming.get("b")));
  }

  @Test(timeout = 4000)
  public void testSubclassDoesNotCollapseWithSuperclass() {
    String source =
        "/** @constructor */ function Base() { this.baseProp = 1; }\n"
        + "/** @constructor @extends {Base} */"
        + " function Derived() { this.derivedProp = 2; }\n";
    Map<String, String> renaming = ambiguate(source).renamingMap;

    assertTrue(renaming.containsKey("baseProp"));
    assertTrue(renaming.containsKey("derivedProp"));
    assertFalse("Supertype and subtype properties must not collapse",
        renaming.get("baseProp").equals(renaming.get("derivedProp")));
  }

  @Test(timeout = 4000)
  public void testImplementingClassDoesNotCollapseWithInterface() {
    String source =
        "/** @interface */ function MyInterface() {}\n"
        + "/** @type {number} */ MyInterface.prototype.interfaceProp = 1;\n"
        + "/** @constructor @implements {MyInterface} */"
        + " function Impl() { this.implProp = 1; }\n"
        + "/** @type {number} */ Impl.prototype.interfaceProp = 1;\n"
        + "/** @param {MyInterface} x */"
        + " function readInterface(x) { return x.interfaceProp; }\n";
    Map<String, String> renaming = ambiguate(source).renamingMap;

    assertTrue(renaming.containsKey("interfaceProp"));
    assertTrue(renaming.containsKey("implProp"));
    assertFalse("Interface and implementing-class properties must stay distinct",
        renaming.get("interfaceProp").equals(renaming.get("implProp")));
  }

  @Test(timeout = 4000)
  public void testCombinedImplementsAndExtendsDoesNotCollapseDistinctProperties() {
    String source =
        "/** @interface */ function I() {}\n"
        + "/** @type {number} */ I.prototype.iProp = 1;\n"
        + "/** @constructor */ function Base() { this.baseProp = 1; }\n"
        + "/** @constructor @extends {Base} @implements {I} */"
        + " function Derived() { this.derivedProp = 2; }\n"
        + "/** @type {number} */ Derived.prototype.iProp = 1;\n";
    Map<String, String> renaming = ambiguate(source).renamingMap;

    assertTrue(renaming.containsKey("baseProp"));
    assertTrue(renaming.containsKey("derivedProp"));
    assertTrue(renaming.containsKey("iProp"));
    assertFalse("baseProp and derivedProp must stay distinct",
        renaming.get("baseProp").equals(renaming.get("derivedProp")));
    assertFalse("iProp and derivedProp must stay distinct",
        renaming.get("iProp").equals(renaming.get("derivedProp")));
  }

  @Test(timeout = 4000)
  public void testQuotedObjectKeyReservesGeneratedName() {
    String source =
        "var o = {'a': 1};\n"
        + "/** @constructor */ function C() { this.p = 1; }\n";
    Map<String, String> renaming = ambiguate(source).renamingMap;

    assertTrue(renaming.containsKey("p"));
    assertFalse("Generated name must not conflict with quoted object key 'a'",
        "a".equals(renaming.get("p")));
  }

  @Test(timeout = 4000)
  public void testQuotedElementAccessReservesGeneratedName() {
    String source =
        "var o = {};\n"
        + "o['a'] = 1;\n"
        + "/** @constructor */ function C() { this.p = 1; }\n";
    Map<String, String> renaming = ambiguate(source).renamingMap;

    assertTrue(renaming.containsKey("p"));
    assertFalse("Generated name must not conflict with quoted element 'a'",
        "a".equals(renaming.get("p")));
  }

  @Test(timeout = 4000)
  public void testExternedPropertyNamesAreReservedAndSkipped() {
    String externs = "var ext = {a: 1};\n ext.a;\n";
    String source =
        "/** @constructor */ function C() { this.p = 1; }\n";
    Map<String, String> renaming = ambiguate(externs, source).renamingMap;

    assertTrue(renaming.containsKey("p"));
    assertFalse("Generated name must not conflict with extern property 'a'",
        "a".equals(renaming.get("p")));

    String sameNameSource =
        "/** @constructor */ function C() { this.a = 1; }\n";
    Map<String, String> sameNameRenaming =
        ambiguate(externs, sameNameSource).renamingMap;
    assertFalse("Externed property name itself should not be renamed",
        sameNameRenaming.containsKey("a"));
  }

  @Test(timeout = 4000)
  public void testSkipPrefixPropertyIsNotRenamed() {
    String source =
        "/** @constructor */ function C() { this.JSAbstractCompilerFoo = 1; }\n";
    Map<String, String> renaming = ambiguate(source).renamingMap;

    assertFalse(renaming.containsKey("JSAbstractCompilerFoo"));
  }

  @Test(timeout = 4000)
  public void testSkipPrefixPropertyDoesNotStopOtherProperties() {
    String source =
        "/** @constructor */ function C() {"
        + " this.JSAbstractCompilerFoo = 1; this.p = 2; }\n";
    Map<String, String> renaming = ambiguate(source).renamingMap;

    assertFalse(renaming.containsKey("JSAbstractCompilerFoo"));
    assertTrue(renaming.containsKey("p"));
  }

  @Test(timeout = 4000)
  public void testUnknownTypePropertyIsSkipped() {
    String source =
        "function f(x) { return x.unknownProp; }\n";
    Map<String, String> renaming = ambiguate(source).renamingMap;

    assertFalse("Property on unknown type should be invalidated",
        renaming.containsKey("unknownProp"));
  }

  @Test(timeout = 4000)
  public void testAnonymousObjectPropertyIsSkipped() {
    String source =
        "var obj = {};\n"
        + "obj.anonProp = 1;\n";
    Map<String, String> renaming = ambiguate(source).renamingMap;

    assertFalse("Property on anonymous object type should be invalidated",
        renaming.containsKey("anonProp"));
  }

  @Test(timeout = 4000)
  public void testProcessUpdatesStringNodes() {
    String source =
        "/** @constructor */ function C() { this.p = 1; }\n";
    Compilation compilation = ambiguate(source);
    String renamed = compilation.renamingMap.get("p");

    assertNotNull(renamed);
    List<String> propStrings = getPropStrings(compilation.compiler.getJsRoot());
    assertTrue(propStrings.contains(renamed));
    assertFalse(propStrings.contains("p"));
  }

  @Test(timeout = 4000)
  public void testQuotedPropertyNameStillGetsRenamedAvoidingConflict() {
    String source =
        "var o = {'a': 1};\n"
        + "/** @constructor */ function C() { this.a = 1; }\n";
    Map<String, String> renaming = ambiguate(source).renamingMap;

    assertTrue(renaming.containsKey("a"));
    assertFalse("Property with a reserved quoted name must choose another name",
        "a".equals(renaming.get("a")));
  }

  @Test(timeout = 4000)
  public void testEmptySourceWithNoPropertiesIsSafe() {
    Map<String, String> renaming = ambiguate("var x = 1;").renamingMap;
    assertNotNull(renaming);
    assertTrue(renaming.isEmpty());
  }

  @Test(timeout = 4000)
  public void testObjectLiteralUnquotedKeyDoesNotCrashProcess() {
    Compilation compilation = ambiguate("var obj = {unquoted: 1};");
    assertNotNull(compilation.renamingMap);
  }

  @Test(timeout = 4000)
  public void testUnionTypePropertyIsProcessed() {
    String source =
        "/** @constructor */ function A() {}\n"
        + "/** @constructor */ function B() {}\n"
        + "/** @type {number} */ A.prototype.u = 1;\n"
        + "/** @type {number} */ B.prototype.u = 1;\n"
        + "/** @param {A|B} x */ function f(x) { return x.u; }\n";
    Map<String, String> renaming = ambiguate(source).renamingMap;

    assertTrue(renaming.containsKey("u"));
  }
}