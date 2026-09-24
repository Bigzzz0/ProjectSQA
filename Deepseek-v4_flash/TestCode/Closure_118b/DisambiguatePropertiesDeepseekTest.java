package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.common.collect.Multimap;
import com.google.javascript.rhino.jstype.JSType;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/* [Branch & Defect Analysis Matrix]
 * Target: DisambiguateProperties.getRenamedTypesForTesting() incorrectly
 * reports a single equivalence class (e.g., [Foo.prototype]) as a renaming
 * candidate.  The defect is exposed when a property is seen on exactly one
 * type; the expected renaming map is {}.
 *
 * Branches covered:
 *   - Property.shouldRename(): types == null, equivalence class count <= 1
 *   - getRenamedTypesForTesting(): skipRenaming, empty equivalence class,
 *     typesToSkip filtering
 *   - JSTypeSystem.isInvalidatingType(): null, unknown/all, anonymous object
 *   - FindExternProperties and FindRenameableProperties: GETPROP/OBJECTLIT
 *   - processProperty(): invalidating type short-circuit, union alternatives
 *   - renameProperties(): shouldRename true/false, skipped instances
 *   - Error reporting through propertiesToErrorFor for important properties.
 */
public class DisambiguatePropertiesDeepseekTest {

  private DisambiguateProperties<JSType> process(String js) {
    return process("", js);
  }

  private DisambiguateProperties<JSType> process(String externs, String js) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.checkTypes = true;

    compiler.compile(
        SourceFile.fromCode("externs", externs),
        SourceFile.fromCode("test", js),
        options);

    if (compiler.getErrors().length > 0) {
      fail("Unexpected compile errors: " + compiler.getErrors()[0].toString());
    }

    compiler.setLifeCycleStage(LifeCycleStage.NORMALIZED);

    DisambiguateProperties<JSType> pass =
        DisambiguateProperties.forJSTypeSystem(
            compiler, Collections.<String, CheckLevel>emptyMap());
    pass.process(compiler.getExternsRoot(), compiler.getRoot());
    return pass;
  }

  @Test(timeout = 4000)
  public void testEmptyInputHasNoRenamingCandidates() {
    DisambiguateProperties<JSType> pass = process("");
    assertTrue(pass.getRenamedTypesForTesting().isEmpty());
  }

  @Test(timeout = 4000)
  public void testOneType4Regression_NoSingleClassReported() {
    String js = "/** @constructor */ function Foo() {}\n"
        + "Foo.prototype.a = 1;\n";

    DisambiguateProperties<JSType> pass = process(js);
    assertEquals("{}", pass.getRenamedTypesForTesting().toString());
  }

  @Test(timeout = 4000)
  public void testOneTypeWithInstanceReferenceHasNoRenaming() {
    String js = "/** @constructor */ function Foo() {}\n"
        + "Foo.prototype.a = 1;\n"
        + "var f = new Foo();\n"
        + "var g = f.a;\n";

    DisambiguateProperties<JSType> pass = process(js);
    assertTrue(pass.getRenamedTypesForTesting().isEmpty());
  }

  @Test(timeout = 4000)
  public void testTwoUnrelatedTypesProduceTwoEquivalenceClasses() {
    String js = "/** @constructor */ function Foo() {}\n"
        + "/** @constructor */ function Bar() {}\n"
        + "Foo.prototype.a = 1;\n"
        + "Bar.prototype.a = 2;\n";

    DisambiguateProperties<JSType> pass = process(js);
    Multimap<String, Collection<JSType>> map =
        pass.getRenamedTypesForTesting();

    assertTrue(map.containsKey("a"));
    assertEquals(2, map.get("a").size());
  }

  @Test(timeout = 4000)
  public void testThreeUnrelatedTypesProduceThreeEquivalenceClasses() {
    String js = "/** @constructor */ function Foo() {}\n"
        + "/** @constructor */ function Bar() {}\n"
        + "/** @constructor */ function Baz() {}\n"
        + "Foo.prototype.a = 1;\n"
        + "Bar.prototype.a = 2;\n"
        + "Baz.prototype.a = 3;\n";

    DisambiguateProperties<JSType> pass = process(js);
    Multimap<String, Collection<JSType>> map =
        pass.getRenamedTypesForTesting();

    assertTrue(map.containsKey("a"));
    assertEquals(3, map.get("a").size());
  }

  @Test(timeout = 4000)
  public void testInheritedPrototypePropertyYieldsSingleClass() {
    String js = "/** @constructor */ function Foo() {}\n"
        + "Foo.prototype.a = 1;\n"
        + "/** @constructor */ function Bar() {}\n"
        + "Bar.prototype = new Foo();\n"
        + "Bar.prototype.a = 2;\n"
        + "var b = new Bar();\n"
        + "var g = b.a;\n";

    DisambiguateProperties<JSType> pass = process(js);
    assertTrue(pass.getRenamedTypesForTesting().isEmpty());
  }

  @Test(timeout = 4000)
  public void testMultiplePropertiesAreTrackedIndependently() {
    String js = "/** @constructor */ function Foo() {}\n"
        + "/** @constructor */ function Bar() {}\n"
        + "Foo.prototype.a = 1;\n"
        + "Foo.prototype.b = 2;\n"
        + "Bar.prototype.b = 3;\n";

    DisambiguateProperties<JSType> pass = process(js);
    Multimap<String, Collection<JSType>> map =
        pass.getRenamedTypesForTesting();

    assertFalse(map.containsKey("a"));
    assertTrue(map.containsKey("b"));
    assertEquals(2, map.get("b").size());
  }

  @Test(timeout = 4000)
  public void testUnknownTypeInvalidatesPropertyRenaming() {
    String js = "/** @param {*} x */ function f(x) { return x.foo; }\n";

    DisambiguateProperties<JSType> pass = process(js);
    assertTrue(pass.getRenamedTypesForTesting().isEmpty());
  }

  @Test(timeout = 4000)
  public void testAnonymousObjectLiteralInvalidatesPropertyRenaming() {
    String js = "var x = {foo: 1, bar: 2};\n";

    DisambiguateProperties<JSType> pass = process(js);
    assertTrue(pass.getRenamedTypesForTesting().isEmpty());
  }

  @Test(timeout = 4000)
  public void testExternsPropertyDefinitionSkipsRenaming() {
    String externs = "/** @constructor */ function Foo() {}\n"
        + "Foo.prototype.a;\n";
    String js = "var f = new Foo();\n"
        + "var g = f.a;\n";

    DisambiguateProperties<JSType> pass = process(externs, js);
    assertTrue(pass.getRenamedTypesForTesting().isEmpty());
  }

  @Test(timeout = 4000)
  public void testExternSkippedTypeStillAllowsOtherTypeRenaming() {
    String externs = "/** @constructor */ function Foo() {}\n"
        + "Foo.prototype.a;\n";
    String js = "/** @constructor */ function Bar() {}\n"
        + "Bar.prototype.a = 1;\n"
        + "var f = new Foo();\n"
        + "var g = f.a;\n"
        + "var b = new Bar();\n"
        + "var y = b.a;\n";

    DisambiguateProperties<JSType> pass = process(externs, js);
    Multimap<String, Collection<JSType>> map =
        pass.getRenamedTypesForTesting();

    assertTrue(map.containsKey("a"));
    assertEquals(1, map.get("a").size());
  }

  @Test(timeout = 4000)
  public void testUnknownAccessInvalidatesPreviouslyKnownProperty() {
    String js = "/** @constructor */ function Foo() {}\n"
        + "Foo.prototype.a = 1;\n"
        + "/** @param {*} x */ function f(x) { return x.a; }\n";

    DisambiguateProperties<JSType> pass = process(js);
    assertTrue(pass.getRenamedTypesForTesting().isEmpty());
  }

  @Test(timeout = 4000)
  public void testUnionTypeAlternativesAreRenamed() {
    String js = "/** @constructor */ function Foo() {}\n"
        + "Foo.prototype.a = 1;\n"
        + "/** @constructor */ function Bar() {}\n"
        + "Bar.prototype.a = 2;\n"
        + "/** @param {Foo|Bar} x */ function f(x) { return x.a; }\n";

    DisambiguateProperties<JSType> pass = process(js);
    Multimap<String, Collection<JSType>> map =
        pass.getRenamedTypesForTesting();

    assertTrue(map.containsKey("a"));
    assertEquals(2, map.get("a").size());
  }

  @Test(timeout = 4000)
  public void testInterfaceTypeUsesDirectImplementors() {
    String js = "/** @interface */ function I() {}\n"
        + "I.prototype.a = function() {};\n"
        + "/** @constructor @implements {I} */ function Foo() {}\n"
        + "Foo.prototype.a = function() {};\n"
        + "/** @constructor @implements {I} */ function Bar() {}\n"
        + "Bar.prototype.a = function() {};\n"
        + "/** @param {I} x */ function f(x) { return x.a; }\n";

    DisambiguateProperties<JSType> pass = process(js);
    assertFalse(pass.getRenamedTypesForTesting().isEmpty());
  }

  @Test(timeout = 4000)
  public void testGetTypeWithPropertyNullArgument() {
    DisambiguateProperties<JSType> pass = process("");
    assertNull(pass.getTypeWithProperty("a", null));
  }

  @Test(timeout = 4000)
  public void testImportantPropertyInvalidationReportsError() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.checkTypes = true;

    compiler.compile(
        SourceFile.fromCode("externs", ""),
        SourceFile.fromCode("test",
            "/** @param {*} x */ function f(x) { return x.toString; }"),
        options);

    if (compiler.getErrors().length > 0) {
      fail("Unexpected initial errors: " + compiler.getErrors()[0].toString());
    }

    compiler.setLifeCycleStage(LifeCycleStage.NORMALIZED);

    Map<String, CheckLevel> props = new HashMap<>();
    props.put("toString", CheckLevel.ERROR);

    DisambiguateProperties<JSType> pass =
        DisambiguateProperties.forJSTypeSystem(compiler, props);
    pass.process(compiler.getExternsRoot(), compiler.getRoot());

    assertEquals(1, compiler.getErrors().length);
  }

  @Test(timeout = 4000)
  public void testImportantPropertyWithCheckLevelOffDoesNotReport() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.checkTypes = true;

    compiler.compile(
        SourceFile.fromCode("externs", ""),
        SourceFile.fromCode("test",
            "/** @param {*} x */ function f(x) { return x.toString; }"),
        options);

    if (compiler.getErrors().length > 0) {
      fail("Unexpected initial errors: " + compiler.getErrors()[0].toString());
    }

    compiler.setLifeCycleStage(LifeCycleStage.NORMALIZED);

    Map<String, CheckLevel> props = new HashMap<>();
    props.put("toString", CheckLevel.OFF);

    DisambiguateProperties<JSType> pass =
        DisambiguateProperties.forJSTypeSystem(compiler, props);
    pass.process(compiler.getExternsRoot(), compiler.getRoot());

    assertEquals(0, compiler.getErrors().length);
  }
}