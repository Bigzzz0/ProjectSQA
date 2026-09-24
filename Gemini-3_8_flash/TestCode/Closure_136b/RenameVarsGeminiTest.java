/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.google.javascript.jscomp.RenameVars
 *
 * 1. Constructor Initialization:
 *    - prefix null vs non-null (prefix == null ? "" : prefix)
 *    - reservedNames null vs non-null (Sets.newHashSet())
 *    - localRenamingOnly (true/false)
 *    - preserveAnonymousFunctionNames (true/false)
 *    - generatePseudoNames (true/false)
 *    - prevUsedRenameMap (null vs populated VariableMap)
 *    - reservedCharacters (null vs char[])
 *
 * 2. ProcessVars Callbacks & Scope Traversal:
 *    - n.getType() != Token.NAME (skip non-name tokens, e.g. VAR, FUNCTION, ASSIGN)
 *    - name.length() == 0 (anonymous function name tokens)
 *    - local vs global determination (var != null && var.isLocal())
 *    - localRenamingOnly = true with !local -> adds name to reservedNames and early returns
 *    - preserveAnonymousFunctionNames = true with NodeUtil.isAnonymousFunction -> reservedNames.add, returns
 *    - okToRenameVar (compiler.getCodingConvention().isExported):
 *      * exported vars (like GoogleCodingConvention or Prototype $$super / global exports) -> skipped
 *    - isExternsPass_:
 *      * !local extern names recorded in externNames
 *      * local in externs ignored
 *    - Source pass:
 *      * local: LOCAL_VAR_PREFIX + var.getLocalVarIndex(), added to localNameNodes & localTempNames
 *      * global (var != null): incCount for global, added to globalNameNodes
 *      * undeclared global (var == null): not added to globalNameNodes
 *
 * 3. Sorting & Name Generation (assignNames):
 *    - FREQUENCY_COMPARATOR: a1.count != a2.count vs tie-breaking ORDER_OF_OCCURRENCE_COMPARATOR
 *    - prevUsedRenameMap reuse (reusePreviouslyUsedVariableMap):
 *      * lookup matches vs null
 *      * prevNewName in reservedNames -> skipped
 *      * oldName starts with LOCAL_VAR_PREFIX or (!externNames.contains && startsWith(prefix))
 *    - Name generation for local vs non-local:
 *      * local: immediate assignment via localNameGenerator
 *      * non-local: pendingAssignments list and grouped by length tie-breaking
 *      * sorting pending non-local assignments of same length by source order
 *
 * 4. Application & Pseudo Names:
 *    - changed flag reporting compiler.reportCodeChange()
 *    - generatePseudoNames: '$' + name + "$$"
 *    - debug log writing to compiler.addToDebugLog()
 *
 * 5. Targeted Defects & Corner Cases:
 *    - Dollar sign super export / exported variable preservation (okToRenameVar).
 *    - Reusing previous variable maps with collision against externs or reserved names.
 *    - Zero variables, only externs, only locals, mix of frequencies.
 */

package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class RenameVarsGeminiTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
  }

  // =========================================================================
  // Helper Methods to Build ASTs and Run RenameVars
  // =========================================================================

  private Node compileExternsAndSource(String externsJs, String sourceJs) {
    SourceFile externFile = SourceFile.fromCode("externs.js", externsJs);
    SourceFile sourceFile = SourceFile.fromCode("input.js", sourceJs);
    compiler.compile(externFile, sourceFile, new CompilerOptions());
    return compiler.getRoot();
  }

  private RenameVars createRenameVars(
      String prefix,
      boolean localRenamingOnly,
      boolean preserveAnon,
      boolean generatePseudoNames,
      VariableMap prevMap,
      char[] reservedChars,
      Set<String> reservedNames) {
    return new RenameVars(
        compiler,
        prefix,
        localRenamingOnly,
        preserveAnon,
        generatePseudoNames,
        prevMap,
        reservedChars,
        reservedNames);
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testSimpleGlobalRenaming() {
    String externs = "";
    String js = "var foobar = 1; var secondVar = foobar + 2;";
    Node root = compileExternsAndSource(externs, js);

    RenameVars renamer = createRenameVars(
        null, false, false, false, null, null, null);

    Node externsNode = root.getFirstChild();
    Node sourcesNode = root.getLastChild();

    renamer.process(externsNode, sourcesNode);

    VariableMap map = renamer.getVariableMap();
    assertNotNull(map);
    assertTrue(map.getOriginalNameToNewNameMap().containsKey("foobar"));
    assertTrue(map.getOriginalNameToNewNameMap().containsKey("secondVar"));

    String newFoobar = map.lookupNewName("foobar");
    String newSecond = map.lookupNewName("secondVar");
    assertNotNull(newFoobar);
    assertNotNull(newSecond);
    assertNotEquals(newFoobar, newSecond);
  }

  @Test(timeout = 4000)
  public void testLocalAndGlobalRenamingWithFrequencies() {
    // foobar occurs 3 times, baz occurs 1 time. foobar should get a shorter or earlier name.
    String js = "var foobar = 10; foobar++; foobar++; var baz = 20;";
    Node root = compileExternsAndSource("", js);

    RenameVars renamer = createRenameVars(
        "", false, false, false, null, null, null);

    renamer.process(root.getFirstChild(), root.getLastChild());

    VariableMap map = renamer.getVariableMap();
    assertEquals("a", map.lookupNewName("foobar"));
    assertEquals("b", map.lookupNewName("baz"));
  }

  @Test(timeout = 4000)
  public void testLocalVariablesRenamedIndependently() {
    String js = "function f() { var localVar1 = 1; return localVar1; }\n"
        + "function g() { var localVar2 = 2; return localVar2; }";
    Node root = compileExternsAndSource("", js);

    RenameVars renamer = createRenameVars(
        null, false, false, false, null, null, null);

    renamer.process(root.getFirstChild(), root.getLastChild());

    VariableMap map = renamer.getVariableMap();
    assertNotNull(map.lookupNewName("f"));
    assertNotNull(map.lookupNewName("g"));
    // Locals use the "L " prefix in the internal renameMap
    boolean foundLocalMap = false;
    for (String originalName : map.getOriginalNameToNewNameMap().keySet()) {
      if (originalName.startsWith("L ")) {
        foundLocalMap = true;
        break;
      }
    }
    assertTrue("Should contain local temp names starting with 'L '", foundLocalMap);
  }

  @Test(timeout = 4000)
  public void testLocalRenamingOnlyOption() {
    String js = "var globalVar = 1; function test() { var localVar = 2; return localVar + globalVar; }";
    Node root = compileExternsAndSource("", js);

    RenameVars renamer = createRenameVars(
        null, true, false, false, null, null, null);

    renamer.process(root.getFirstChild(), root.getLastChild());

    VariableMap map = renamer.getVariableMap();
    assertNull("Global variable must not be renamed when localRenamingOnly is true",
        map.lookupNewName("globalVar"));
    assertNull("Function name at global scope must not be renamed",
        map.lookupNewName("test"));
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testEmptySourceAndExterns() {
    Node root = compileExternsAndSource("", "");
    RenameVars renamer = createRenameVars(
        "", false, false, false, null, null, Collections.emptySet());

    renamer.process(root.getFirstChild(), root.getLastChild());
    VariableMap map = renamer.getVariableMap();
    assertNotNull(map);
    assertTrue(map.getOriginalNameToNewNameMap().isEmpty());
  }

  @Test(timeout = 4000)
  public void testPrefixAppliedToGlobalsOnly() {
    String js = "var g1 = 1; function f() { var l1 = 2; return l1; }";
    Node root = compileExternsAndSource("", js);

    RenameVars renamer = createRenameVars(
        "MY_PREFIX_", false, false, false, null, null, null);

    renamer.process(root.getFirstChild(), root.getLastChild());

    VariableMap map = renamer.getVariableMap();
    String newG1 = map.lookupNewName("g1");
    assertNotNull(newG1);
    assertTrue("Global variable must start with prefix", newG1.startsWith("MY_PREFIX_"));

    for (Map.Entry<String, String> entry : map.getOriginalNameToNewNameMap().entrySet()) {
      if (entry.getKey().startsWith("L ")) {
        assertFalse("Local variable must not start with global prefix",
            entry.getValue().startsWith("MY_PREFIX_"));
      }
    }
  }

  @Test(timeout = 4000)
  public void testReservedCharactersIgnoredInGeneratedNames() {
    String js = "var a1 = 1; var b1 = 2;";
    Node root = compileExternsAndSource("", js);

    // Disallow 'a' and 'b'
    char[] reservedChars = new char[]{'a', 'b'};
    RenameVars renamer = createRenameVars(
        "", false, false, false, null, reservedChars, null);

    renamer.process(root.getFirstChild(), root.getLastChild());

    VariableMap map = renamer.getVariableMap();
    String newA1 = map.lookupNewName("a1");
    assertNotNull(newA1);
    assertFalse("Generated name must not contain reserved character 'a'", newA1.contains("a"));
    assertFalse("Generated name must not contain reserved character 'b'", newA1.contains("b"));
  }

  @Test(timeout = 4000)
  public void testReservedNamesNotAssigned() {
    String js = "var x = 1; var y = 2;";
    Node root = compileExternsAndSource("", js);

    Set<String> reserved = new HashSet<>();
    reserved.add("a"); // "a" would normally be the first generated name
    reserved.add("b");

    RenameVars renamer = createRenameVars(
        "", false, false, false, null, null, reserved);

    renamer.process(root.getFirstChild(), root.getLastChild());

    VariableMap map = renamer.getVariableMap();
    String newX = map.lookupNewName("x");
    String newY = map.lookupNewName("y");

    assertNotEquals("a", newX);
    assertNotEquals("b", newX);
    assertNotEquals("a", newY);
    assertNotEquals("b", newY);
  }

  @Test(timeout = 4000)
  public void testExternNamesPreservedAndNotOverwritten() {
    String externs = "var externGlobalVar = 100;";
    String js = "var myLocalScriptVar = externGlobalVar + 1;";
    Node root = compileExternsAndSource(externs, js);

    RenameVars renamer = createRenameVars(
        "", false, false, false, null, null, null);

    renamer.process(root.getFirstChild(), root.getLastChild());

    VariableMap map = renamer.getVariableMap();
    assertNull("Extern variable must never be renamed", map.lookupNewName("externGlobalVar"));
    assertNotNull(map.lookupNewName("myLocalScriptVar"));
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // =========================================================================

  @Test(timeout = 4000)
  public void testDollarSignSuperExport2() {
    // Tests that exported / super variables (e.g. $$super or $super) conform
    // to coding convention and are preserved or handled accurately without colliding.
    String js = "var $$super = 10; var x = $$super + 1;";
    Node root = compileExternsAndSource("", js);

    RenameVars renamer = createRenameVars(
        "", false, false, false, null, null, null);

    renamer.process(root.getFirstChild(), root.getLastChild());

    VariableMap map = renamer.getVariableMap();
    assertNotNull(map);
    // In Google/Closure coding conventions, $$ or $super may be exported or treated as unrenamable.
    // If $$super is exported, okToRenameVar returns false and $$super is not renamed.
    // Whatever the coding convention dictates, process() must execute without throwing an internal exception.
    String newX = map.lookupNewName("x");
    assertNotNull("Ordinary variable x must be assigned a new name", newX);
  }

  @Test(timeout = 4000)
  public void testPseudoNamesGeneration() {
    String js = "var myGlobal = 10; function run() { var myLocal = 20; return myLocal; }";
    Node root = compileExternsAndSource("", js);

    RenameVars renamer = createRenameVars(
        "", false, false, true, null, null, null);

    renamer.process(root.getFirstChild(), root.getLastChild());

    // When pseudo names are generated, the AST nodes are updated to $originalName$$
    Node script = root.getLastChild().getFirstChild();
    assertNotNull(script);

    // Check that pseudo names were applied to the AST
    boolean foundPseudoName = false;
    for (Node child : script.children()) {
      if (child.isVar()) {
        Node nameNode = child.getFirstChild();
        if (nameNode.getString().equals("$myGlobal$$")) {
          foundPseudoName = true;
        }
      }
    }
    assertTrue("Global variable AST node should be renamed to pseudo name $myGlobal$$",
        foundPseudoName);
  }

  @Test(timeout = 4000)
  public void testPreserveAnonymousFunctionNames() {
    String js = "var anon = function() {};";
    Node root = compileExternsAndSource("", js);

    RenameVars renamer = createRenameVars(
        "", false, true, false, null, null, null);

    renamer.process(root.getFirstChild(), root.getLastChild());

    VariableMap map = renamer.getVariableMap();
    // anon is assigned an anonymous function; when preserveAnonymousFunctionNames is true,
    // it should be kept in reservedNames and not renamed.
    assertNull("Anonymous function variable name must be preserved when flag is true",
        map.lookupNewName("anon"));
  }

  // =========================================================================
  // Partition D: Previously Used Variable Map Reuse & Collisions
  // =========================================================================

  @Test(timeout = 4000)
  public void testReusePreviouslyUsedVariableMap() {
    String js = "var alpha = 1; var beta = 2; var gamma = 3;";
    Node root = compileExternsAndSource("", js);

    Map<String, String> prevMapData = new HashMap<>();
    prevMapData.put("alpha", "prev_a");
    prevMapData.put("beta", "prev_b");
    VariableMap prevVariableMap = new VariableMap(prevMapData);

    RenameVars renamer = createRenameVars(
        "prev_", false, false, false, prevVariableMap, null, null);

    renamer.process(root.getFirstChild(), root.getLastChild());

    VariableMap map = renamer.getVariableMap();
    assertEquals("prev_a", map.lookupNewName("alpha"));
    assertEquals("prev_b", map.lookupNewName("beta"));
    // gamma was not in prevMap, so it receives a newly generated name with prefix
    String newGamma = map.lookupNewName("gamma");
    assertNotNull(newGamma);
    assertTrue(newGamma.startsWith("prev_"));
  }

  @Test(timeout = 4000)
  public void testReusePreviouslyUsedVariableMapCollisionWithReserved() {
    String js = "var alpha = 1;";
    Node root = compileExternsAndSource("", js);

    Map<String, String> prevMapData = new HashMap<>();
    prevMapData.put("alpha", "reserved_name");
    VariableMap prevVariableMap = new VariableMap(prevMapData);

    Set<String> reservedNames = new HashSet<>();
    reservedNames.add("reserved_name");

    RenameVars renamer = createRenameVars(
        "", false, false, false, prevVariableMap, null, reservedNames);

    renamer.process(root.getFirstChild(), root.getLastChild());

    VariableMap map = renamer.getVariableMap();
    // Because "reserved_name" was already reserved, the previous mapping cannot be reused.
    assertNotEquals("reserved_name", map.lookupNewName("alpha"));
  }

  @Test(timeout = 4000)
  public void testReusePreviouslyUsedVariableMapCollidesWithExtern() {
    String externs = "var clash = 1;";
    String js = "var alpha = 2;";
    Node root = compileExternsAndSource(externs, js);

    Map<String, String> prevMapData = new HashMap<>();
    // Trying to reuse an extern name should fail because externs are added to reservedNames
    prevMapData.put("alpha", "clash");
    VariableMap prevVariableMap = new VariableMap(prevMapData);

    RenameVars renamer = createRenameVars(
        "", false, false, false, prevVariableMap, null, null);

    renamer.process(root.getFirstChild(), root.getLastChild());

    VariableMap map = renamer.getVariableMap();
    assertNotEquals("clash", map.lookupNewName("alpha"));
  }

  // =========================================================================
  // Partition E: Internal Class / Comparator & Edge Cases
  // =========================================================================

  @Test(timeout = 4000)
  public void testOrderOfOccurrenceTiesWithSameFrequency() {
    // Both v1, v2, v3 have frequency 1.
    // They should be renamed deterministically based on source occurrence order.
    String js = "var v1 = 1; var v2 = 2; var v3 = 3;";
    Node root = compileExternsAndSource("", js);

    RenameVars renamer = createRenameVars(
        "", false, false, false, null, null, null);

    renamer.process(root.getFirstChild(), root.getLastChild());

    VariableMap map = renamer.getVariableMap();
    assertEquals("a", map.lookupNewName("v1"));
    assertEquals("b", map.lookupNewName("v2"));
    assertEquals("c", map.lookupNewName("v3"));
  }

  @Test(timeout = 4000)
  public void testAssignmentSetNewNameTwiceThrowsException() {
    RenameVars renamer = createRenameVars(
        "", false, false, false, null, null, null);

    RenameVars.Assignment assignment = renamer.new Assignment("foo", null);
    assertEquals("foo", assignment.oldName);
    assertNull(assignment.newName);
    assertEquals(0, assignment.count);

    assignment.setNewName("newFoo");
    assertEquals("newFoo", assignment.newName);

    try {
      assignment.setNewName("illegalSecondAssignment");
      fail("Setting newName twice must throw IllegalStateException");
    } catch (IllegalStateException expected) {
      // Success: checkState(this.newName == null) violated
    }
  }

  @Test(timeout = 4000)
  public void testNonNameNodesIgnoredDuringTraversal() {
    // Ast with various token types that are not Token.NAME
    Node externs = new Node(Token.BLOCK);
    Node root = new Node(Token.BLOCK);
    Node numberNode = Node.newNumber(42);
    Node stringNode = Node.newString("hello");
    root.addChildToBack(numberNode);
    root.addChildToBack(stringNode);

    RenameVars renamer = createRenameVars(
        "", false, false, false, null, null, null);

    renamer.process(externs, root);

    VariableMap map = renamer.getVariableMap();
    assertTrue(map.getOriginalNameToNewNameMap().isEmpty());
  }

  @Test(timeout = 4000)
  public void testAnonymousFunctionWithEmptyNameIgnored() {
    // Construct a NAME node with an empty string: name.length() == 0
    Node externs = new Node(Token.BLOCK);
    Node root = new Node(Token.BLOCK);
    Node emptyNameNode = Node.newString(Token.NAME, "");
    root.addChildToBack(emptyNameNode);

    RenameVars renamer = createRenameVars(
        "", false, false, false, null, null, null);

    renamer.process(externs, root);

    VariableMap map = renamer.getVariableMap();
    assertTrue(map.getOriginalNameToNewNameMap().isEmpty());
  }

  @Test(timeout = 4000)
  public void testLocalVarPrefixReuseAcrossMultipleFunctions() {
    // Both f1 and f2 declare local variables.
    // Local variable indices are reused across global function scopes.
    String js = "function f1(a, b) { var c = a + b; return c; }\n"
        + "function f2(d, e) { var f = d + e; return f; }";
    Node root = compileExternsAndSource("", js);

    RenameVars renamer = createRenameVars(
        "", false, false, false, null, null, null);

    renamer.process(root.getFirstChild(), root.getLastChild());

    VariableMap map = renamer.getVariableMap();
    assertNotNull(map);
    // f1 and f2 are global functions, a,b,c and d,e,f are local variables.
    assertNotNull(map.lookupNewName("f1"));
    assertNotNull(map.lookupNewName("f2"));
  }
}