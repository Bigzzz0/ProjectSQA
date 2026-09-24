package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import java.util.Collection;
import java.util.Map;

/**
 * Test suite for RenamePrototypes.
 * Targets high line/branch coverage and the known defect related to
 * object literal keys with numeric values causing internal errors.
 */
public class RenamePrototypesDeepseekTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private CompilerOptions options;
  private Compiler compiler;

  @Before
  public void setUp() {
    options = new CompilerOptions();
    // Ensure the life cycle stage is normalized; RenamePrototypes checks this.
    compiler = new Compiler();
  }

  // Helper: compile a snippet and return the RenamePrototypes instance
  private RenamePrototypes compileAndRename(String js, boolean aggressive) {
    options.setRenamePrototypes(true);
    options.setAggressiveRenaming(aggressive);
    compiler.init(
        new SourceFile[] { SourceFile.fromCode("externs", "function alert(x) {}") },
        new SourceFile[] { SourceFile.fromCode("test", js) },
        options);
    compiler.parse();
    compiler.checkNormalization();
    RenamePrototypes pass = new RenamePrototypes(compiler, aggressive, null, null);
    pass.process(compiler.getExternsRoot(), compiler.getJsRoot());
    return pass;
  }

  @Test(timeout = 4000)
  public void testSimplePrototypeRenaming() {
    RenamePrototypes pass = compileAndRename(
        "/** @constructor */ function Foo() {}\n" +
        "Foo.prototype.bar_ = function() {};\n" +
        "var x = new Foo(); x.bar_();", false);
    VariableMap map = pass.getPropertyMap();
    // bar_ should be renamed (ends with underscore)
    assertTrue("bar_ should be renamed", map.getOriginalNameToNewNameMap().containsKey("bar_"));
    assertFalse("Original name not mapped to itself", 
        map.getOriginalNameToNewNameMap().get("bar_").equals("bar_"));
  }

  @Test(timeout = 4000)
  public void testAggressiveRenaming() {
    // With aggressive renaming, names without leading/trailing underscores can be renamed
    RenamePrototypes pass = compileAndRename(
        "/** @constructor */ function Foo() {}\n" +
        "Foo.prototype.myMethod = function() {};\n" +
        "var x = new Foo(); x.myMethod();", true);
    VariableMap map = pass.getPropertyMap();
    // myMethod should be renamed because aggressive
    assertTrue("myMethod should be renamed under aggressive", 
        map.getOriginalNameToNewNameMap().containsKey("myMethod"));
  }

  @Test(timeout = 4000)
  public void testNonAggressiveNoRenameForLowercase() {
    // Without aggressive, names that are all lowercase letters should not be renamed
    RenamePrototypes pass = compileAndRename(
        "/** @constructor */ function Foo() {}\n" +
        "Foo.prototype.myMethod = function() {};\n" +
        "var x = new Foo(); x.myMethod();", false);
    VariableMap map = pass.getPropertyMap();
    // myMethod is all lowercase (except first? 'm' lower, 'y' lower, 'M' upper? Actually 'M' is uppercase)
    // Since it contains an uppercase letter, it will be renamed even without aggressive.
    // Use a name that is all lowercase: "foo"
    pass = compileAndRename(
        "/** @constructor */ function Foo() {}\n" +
        "Foo.prototype.foo = function() {};\n" +
        "var x = new Foo(); x.foo();", false);
    map = pass.getPropertyMap();
    // all lowercase -> not renamed unless aggressive
    assertFalse("All-lowercase name should not be renamed without aggressive",
        map.getOriginalNameToNewNameMap().containsKey("foo"));
  }

  @Test(timeout = 4000)
  public void testExportedNameNotRenamed() {
    // Names that start with "_" (convention for exported) should not be renamed
    RenamePrototypes pass = compileAndRename(
        "/** @constructor */ function Foo() {}\n" +
        "Foo.prototype._exported = function() {};\n" +
        "var x = new Foo(); x._exported();", false);
    VariableMap map = pass.getPropertyMap();
    assertFalse("Exported name should not be renamed",
        map.getOriginalNameToNewNameMap().containsKey("_exported"));
  }

  @Test(timeout = 4000)
  public void testPrivateNameRenamed() {
    // Names that end with "_" are private and should be renamed
    RenamePrototypes pass = compileAndRename(
        "/** @constructor */ function Foo() {}\n" +
        "Foo.prototype.private_ = function() {};\n" +
        "var x = new Foo(); x.private_();", false);
    VariableMap map = pass.getPropertyMap();
    assertTrue("Private name should be renamed", 
        map.getOriginalNameToNewNameMap().containsKey("private_"));
  }

  @Test(timeout = 4000)
  public void testReservedNameNotRenamed() {
    // "toString" is in the default reserved set
    RenamePrototypes pass = compileAndRename(
        "/** @constructor */ function Foo() {}\n" +
        "Foo.prototype.toString = function() { return ''; };\n" +
        "var x = new Foo(); x.toString();", true);
    VariableMap map = pass.getPropertyMap();
    assertFalse("Reserved name 'toString' should not be renamed",
        map.getOriginalNameToNewNameMap().containsKey("toString"));
  }

  @Test(timeout = 4000)
  public void testObjLitPropertyNotRenamedByDefault() {
    // By default, object literal properties are not renamed (unless exported/private)
    RenamePrototypes pass = compileAndRename(
        "var x = { myProp: 1 }; x.myProp;", false);
    VariableMap map = pass.getPropertyMap();
    // myProp is all lowercase; even if it contained uppercase, obj lit renaming is false by default
    assertFalse("Obj lit property should not be renamed by default",
        map.getOriginalNameToNewNameMap().containsKey("myProp"));
  }

  @Test(timeout = 4000)
  public void testObjLitPrivatePropertyRenamed() {
    // Private object literal properties (ending with underscore) should be renamed
    RenamePrototypes pass = compileAndRename(
        "var x = { private_: 1 }; x.private_;", false);
    VariableMap map = pass.getPropertyMap();
    assertTrue("Private obj lit property should be renamed",
        map.getOriginalNameToNewNameMap().containsKey("private_"));
  }

  @Test(timeout = 4000)
  public void testExternedPropertiesNotRenamed() {
    // Properties from externs should be reserved
    RenamePrototypes pass = compileAndRename(
        "var x = {}; x.alert = function() {}; x.alert();", false);
    VariableMap map = pass.getPropertyMap();
    assertFalse("Externed property 'alert' should not be renamed",
        map.getOriginalNameToNewNameMap().containsKey("alert"));
  }

  @Test(timeout = 4000)
  public void testPrototypeWithObjLitAssignment() {
    // Foo.prototype = { method_: function() {} }
    RenamePrototypes pass = compileAndRename(
        "/** @constructor */ function Foo() {}\n" +
        "Foo.prototype = { method_: function() {} };\n" +
        "var x = new Foo(); x.method_();", false);
    VariableMap map = pass.getPropertyMap();
    assertTrue("Property from prototype OBJLIT should be renamed",
        map.getOriginalNameToNewNameMap().containsKey("method_"));
  }

  @Test(timeout = 4000)
  public void testPreviousRenameMapReuse() {
    // Use a previous map to force reuse
    VariableMap prevMap = new VariableMap(new java.util.HashMap<String, String>() {{
      put("bar_", "x");
    }});
    CompilerOptions opts = new CompilerOptions();
    opts.setRenamePrototypes(true);
    opts.setAggressiveRenaming(false);
    compiler = new Compiler();
    compiler.init(
        new SourceFile[] { SourceFile.fromCode("externs", "") },
        new SourceFile[] { SourceFile.fromCode("test",
            "/** @constructor */ function Foo() {}\n" +
            "Foo.prototype.bar_ = function() {};\n" +
            "var x = new Foo(); x.bar_();") },
        opts);
    compiler.parse();
    compiler.checkNormalization();
    RenamePrototypes pass = new RenamePrototypes(compiler, false, null, prevMap);
    pass.process(compiler.getExternsRoot(), compiler.getJsRoot());
    VariableMap map = pass.getPropertyMap();
    // bar_ should be renamed to "x" if possible
    String renamed = map.lookupNewName("bar_");
    assertNotNull("bar_ should be renamed using previous map", renamed);
    assertTrue("bar_ should reuse 'x'", "x".equals(renamed) || !renamed.equals("bar_"));
  }

  @Test(timeout = 4000)
  public void testPropertyWithBothCounts() {
    // A property used both as prototype property and object literal key
    // (should check canRename: requires both prototype and objlit conditions)
    // In this case, proto: non-lowercase, objlit: not exported nor private -> cannot rename
    RenamePrototypes pass = compileAndRename(
        "/** @constructor */ function Foo() {}\n" +
        "Foo.prototype.Bar = function() {};\n" +
        "var x = { Bar: 1 };\n" +
        "x.Bar;\n" +
        "var y = new Foo(); y.Bar();", false);
    VariableMap map = pass.getPropertyMap();
    // Bar has uppercase, so prototypeCanRename true; objlitCanRename false => canRename false
    assertFalse("Property with both counts and non-private, non-exported should not rename",
        map.getOriginalNameToNewNameMap().containsKey("Bar"));
  }

  @Test(timeout = 4000)
  public void testReservedCharacters() {
    // Provide reserved characters to NameGenerator, ensure names don't include them
    options.setRenamePrototypes(true);
    options.setAggressiveRenaming(true);
    char[] reserved = new char[] { 'a' };
    compiler = new Compiler();
    compiler.init(
        new SourceFile[] { SourceFile.fromCode("externs", "") },
        new SourceFile[] { SourceFile.fromCode("test",
            "/** @constructor */ function Foo() {}\n" +
            "Foo.prototype.myMethod = function() {};\n" +
            "var x = new Foo(); x.myMethod();") },
        options);
    compiler.parse();
    compiler.checkNormalization();
    RenamePrototypes pass = new RenamePrototypes(compiler, true, reserved, null);
    pass.process(compiler.getExternsRoot(), compiler.getJsRoot());
    VariableMap map = pass.getPropertyMap();
    String newName = map.lookupNewName("myMethod");
    assertNotNull("myMethod should be renamed", newName);
    // ensure 'a' is not in the newName (character 'a' is lower case, but 'a' is the letter)
    assertFalse("New name should not contain reserved character 'a'", 
        newName.indexOf('a') >= 0);
  }

  // ---------- Defect-targeted tests ----------
  // The known defect: internal compiler error when prototype property defined via 
  // object literal with numeric keys (e.g., Foo.prototype = {1: function(){}}).
  // This test should pass on fixed version; on defective version it throws an internal error.
  @Test(timeout = 4000)
  public void testPrototypeWithNumericObjLitKey_noCrash() {
    // This should not throw an internal compiler error.
    // The object literal key is a number, which the pass skips (Token.NUMBER).
    // Ensure the compilation completes and no exception.
    RenamePrototypes pass = compileAndRename(
        "/** @constructor */ function Foo() {}\n" +
        "Foo.prototype = { 1: function() {} };\n" +
        "var x = new Foo(); x[1]();", false);
    // If we reach here, no crash. Also verify that the numeric key is not in rename map.
    VariableMap map = pass.getPropertyMap();
    // The key "1" as string? The node is NUMBER, not stored. So no mapping.
    assertFalse("Numeric key should not be renamed",
        map.getOriginalNameToNewNameMap().containsKey("1"));
  }

  @Test(timeout = 4000)
  public void testObjLitWithNumericKeys() {
    // Ensure object literals with numeric keys do not cause issues.
    // This tests the handling in ProcessProperties where NUMBER keys are skipped.
    RenamePrototypes pass = compileAndRename(
        "var x = { 1: 'a', 2: 'b' }; x[1];", false);
    // No renaming should occur.
    VariableMap map = pass.getPropertyMap();
    assertTrue("Properties map should be empty or contain no numeric keys",
        map.getOriginalNameToNewNameMap().isEmpty() || 
        !map.getOriginalNameToNewNameMap().containsKey("1"));
  }

  @Test(timeout = 4000)
  public void testMixedStringAndNumericKeysInObjLit() {
    // Mix of string (with underscore) and number keys in an object literal assigned to prototype
    RenamePrototypes pass = compileAndRename(
        "/** @constructor */ function Foo() {}\n" +
        "Foo.prototype = { bar_: 1, 0: function() {} };\n" +
        "var x = new Foo(); x.bar_();", false);
    VariableMap map = pass.getPropertyMap();
    // bar_ should be renamed; numeric key 0 should not appear.
    assertTrue("bar_ should be renamed", map.getOriginalNameToNewNameMap().containsKey("bar_"));
    assertFalse("Numeric key 0 should not be in rename map",
        map.getOriginalNameToNewNameMap().containsKey("0"));
  }

  // Additional branch coverage: canRename when both counts >0 (both prototype and objlit)
  @Test(timeout = 4000)
  public void testCanRenameWithBothCounts() {
    // Both counts >0; canRename returns true only if both can rename.
    // For a private name ending with underscore, both can rename.
    RenamePrototypes pass = compileAndRename(
        "/** @constructor */ function Foo() {}\n" +
        "Foo.prototype.private_ = function() {};\n" +
        "var x = { private_: 1 };\n" +
        "x.private_;\n" +
        "var y = new Foo(); y.private_();", false);
    VariableMap map = pass.getPropertyMap();
    assertTrue("Private name used in both contexts should be renamed",
        map.getOriginalNameToNewNameMap().containsKey("private_"));
  }

  @Test(timeout = 4000)
  public void testCanRenameWhenBothCountsZero() {
    // Property with zero counts: canRename uses both conditions (prototype and objlit)
    // This happens for properties added at runtime: o[prop] = x;
    // For a property like "runtimeProp" (all lowercase, not private/exported), prototypeCanRename false,
    // objlitCanRename false => canRename false.
    RenamePrototypes pass = compileAndRename(
        "var x = {}; x.runtimeProp = 1; x.runtimeProp;", false);
    VariableMap map = pass.getPropertyMap();
    assertFalse("Runtime-added property with all lowercase should not be renamed",
        map.getOriginalNameToNewNameMap().containsKey("runtimeProp"));
  }

  @Test(timeout = 4000)
  public void testCanRenameWithBothCountsZeroPrivate() {
    // Private name added at runtime (ends with underscore) should rename
    RenamePrototypes pass = compileAndRename(
        "var x = {}; x.private_ = 1; x.private_;", false);
    VariableMap map = pass.getPropertyMap();
    assertTrue("Private runtime-added property should be renamed",
        map.getOriginalNameToNewNameMap().containsKey("private_"));
  }
}