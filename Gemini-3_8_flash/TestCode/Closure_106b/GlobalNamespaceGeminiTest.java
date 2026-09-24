/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: com.google.javascript.jscomp.GlobalNamespace
 * Associated Nested Classes: GlobalNamespace.Name, GlobalNamespace.Ref, GlobalNamespace.BuildGlobalNamespace
 *
 * ------------------------------------------------------------------------------------------------------------------
 * Branch / Condition Coverage:
 * 1. BuildGlobalNamespace.visit:
 *    - Token.STRING in Token.OBJECTLIT (valid JS identifier vs non-identifier vs nested objlits vs walker null)
 *    - Token.NAME (parent: VAR with/without rvalue, ASSIGN as first child vs value, GETPROP, FUNCTION stmt vs anon)
 *    - Token.GETPROP (parent: ASSIGN lvalue vs value, GETPROP recursion)
 *    - isGlobalNameReference checks: global scope vs local scope (SET_FROM_GLOBAL vs SET_FROM_LOCAL vs handleGet)
 * 2. getValueType:
 *    - OBJECTLIT, FUNCTION, OR (recurse last child), HOOK (recurse second/third child), OTHER
 * 3. isConstructorOrEnumDeclaration:
 *    - ASSIGN with JSDoc @constructor / @enum vs non-matching types
 *    - VAR with JSDoc on name vs parent with @constructor / @enum vs non-matching types
 * 4. determineGetTypeForHookOrBooleanExpr:
 *    - Ancestors: EXPR_RESULT, VAR, IF, WHILE, FOR, TYPEOF, VOID, NOT, BITNOT, POS, NEG, HOOK, ASSIGN, NAME, CALL
 * 5. maybeHandlePrototypePrefix:
 *    - endsWith(".prototype"), contains(".prototype."), object literal key prefix bypass, multi-level stripping
 * 6. GlobalNamespace.Name:
 *    - addRef / removeRef across all Ref.Type enum values:
 *      SET_FROM_GLOBAL (decl assignment, reassignment promotion), SET_FROM_LOCAL,
 *      PROTOTYPE_GET, DIRECT_GET, ALIASING_GET, CALL_GET
 *    - canEliminate: totalGets > 0, child prop collapse checks
 *    - canCollapse: inExterns boundary, isClassOrEnum, parent child collapse checks
 *    - canCollapseUnannotatedChildNames: type check, globalSets != 1, localSets != 0, aliasingGets, twin ref guard
 *    - isNamespace: hasClassOrEnumDescendant && type == OBJECTLIT
 *    - isSimpleName, fullName, toString
 * 7. GlobalNamespace.Ref:
 *    - markTwins: valid ALIASING_GET + SET permutations, invalid precondition violation -> IllegalArgumentException
 *    - isSet, cloneAndReclassify, createRefForTesting
 * 8. NodeFilter & scanNewNodes:
 *    - isQualifiedName false vs GETPROP traverse vs Token.NAME target
 *
 * ------------------------------------------------------------------------------------------------------------------
 * Defect Ground Truth Targeting:
 * - Defects4J CollapsePropertiesTest::testTwinReferenceCancelsChildCollapsing:
 *   When a global set has a twin reference (e.g. nested assignment/aliasing get), collapsing unannotated child
 *   names should be prohibited. The comment in GlobalNamespace.Name explicitly specifies:
 *   "Don't try to collapse if the one global set is a twin reference."
 *   The defect omits checking declaration.getTwin() != null in canCollapseUnannotatedChildNames().
 * - CollapsePropertiesTest::testCrashInNestedAssign & testCrashInCommaOperator:
 *   Ensures robustness when nested assignments, comma operators, and hook expressions are processed.
 */

package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class GlobalNamespaceGeminiTest {

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testBasicGlobalVariablesAndProperties() {
    Compiler compiler = new Compiler();
    String js = "var a = {};\n" +
                "a.b = 1;\n" +
                "a.c = function() {};\n" +
                "var d = a.b;\n";
    Node root = compiler.parseTestCode(js);
    GlobalNamespace gn = new GlobalNamespace(compiler, root);

    Map<String, GlobalNamespace.Name> index = gn.getNameIndex();
    assertNotNull(index);
    assertTrue(index.containsKey("a"));
    assertTrue(index.containsKey("a.b"));
    assertTrue(index.containsKey("a.c"));
    assertTrue(index.containsKey("d"));

    GlobalNamespace.Name nameA = index.get("a");
    assertEquals(GlobalNamespace.Name.Type.OBJECTLIT, nameA.type);
    assertEquals(1, nameA.globalSets);
    assertTrue(nameA.isSimpleName());
    assertEquals("a", nameA.fullName());

    GlobalNamespace.Name nameAB = index.get("a.b");
    assertEquals(GlobalNamespace.Name.Type.OTHER, nameAB.type);
    assertEquals(1, nameAB.globalSets);
    assertEquals(1, nameAB.totalGets);
    assertFalse(nameAB.isSimpleName());
    assertEquals("a.b", nameAB.fullName());

    List<GlobalNamespace.Name> forest = gn.getNameForest();
    assertEquals(2, forest.size()); // "a" and "d" are roots
  }

  @Test(timeout = 4000)
  public void testObjectLiteralKeyResolution() {
    Compiler compiler = new Compiler();
    String js = "var config = {\n" +
                "  sub: {\n" +
                "    item: 42\n" +
                "  },\n" +
                "  'invalid-identifier': 100\n" +
                "};\n";
    Node root = compiler.parseTestCode(js);
    GlobalNamespace gn = new GlobalNamespace(compiler, root);

    Map<String, GlobalNamespace.Name> index = gn.getNameIndex();
    assertTrue(index.containsKey("config"));
    assertTrue(index.containsKey("config.sub"));
    assertTrue(index.containsKey("config.sub.item"));
    assertFalse(index.containsKey("config.invalid-identifier"));
  }

  @Test(timeout = 4000)
  public void testAssignmentsInsideFunctionsAreLocalSets() {
    Compiler compiler = new Compiler();
    String js = "var x = 1;\n" +
                "function foo() {\n" +
                "  x = 2;\n" +
                "}\n";
    Node root = compiler.parseTestCode(js);
    GlobalNamespace gn = new GlobalNamespace(compiler, root);

    GlobalNamespace.Name nameX = gn.getNameIndex().get("x");
    assertNotNull(nameX);
    assertEquals(1, nameX.globalSets);
    assertEquals(1, nameX.localSets);
    assertFalse(nameX.needsToBeStubbed());

    GlobalNamespace.Name nameFoo = gn.getNameIndex().get("foo");
    assertNotNull(nameFoo);
    assertEquals(GlobalNamespace.Name.Type.FUNCTION, nameFoo.type);
  }

  @Test(timeout = 4000)
  public void testPrototypePrefixHandling() {
    Compiler compiler = new Compiler();
    String js = "function MyClass() {}\n" +
                "MyClass.prototype.sayHi = function() {};\n" +
                "MyClass.prototype.sub.prop = 123;\n" +
                "var p = MyClass.prototype;\n";
    Node root = compiler.parseTestCode(js);
    GlobalNamespace gn = new GlobalNamespace(compiler, root);

    Map<String, GlobalNamespace.Name> index = gn.getNameIndex();
    GlobalNamespace.Name myClass = index.get("MyClass");
    assertNotNull(myClass);
    assertTrue(myClass.totalGets > 0);
  }

  @Test(timeout = 4000)
  public void testConstructorAndEnumDeclarations() {
    Compiler compiler = new Compiler();
    String js = "/** @constructor */ var MyClass = function() {};\n" +
                "/** @enum {number} */ var MyEnum = { FIRST: 1, SECOND: 2 };\n" +
                "/** @constructor */ MyClass.SubClass = function() {};\n";
    Node root = compiler.parseTestCode(js);
    GlobalNamespace gn = new GlobalNamespace(compiler, root);

    Map<String, GlobalNamespace.Name> index = gn.getNameIndex();
    GlobalNamespace.Name myClass = index.get("MyClass");
    assertNotNull(myClass);
    assertTrue(myClass.canCollapse());

    GlobalNamespace.Name myEnum = index.get("MyEnum");
    assertNotNull(myEnum);
    assertTrue(myEnum.canCollapse());
  }

  @Test(timeout = 4000)
  public void testNamespaceRecognition() {
    GlobalNamespace.Name ns = new GlobalNamespace.Name("ns", null, false);
    ns.type = GlobalNamespace.Name.Type.OBJECTLIT;
    assertFalse(ns.isNamespace());

    GlobalNamespace.Name childClass = ns.addProperty("Child", false);
    childClass.setIsClassOrEnum();

    assertTrue(ns.isNamespace());
    assertFalse(childClass.isNamespace());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testEmptyCodeRoot() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("");
    GlobalNamespace gn = new GlobalNamespace(compiler, root);

    assertTrue(gn.getNameForest().isEmpty());
    assertTrue(gn.getNameIndex().isEmpty());
  }

  @Test(timeout = 4000)
  public void testExternsBoundary() {
    Compiler compiler = new Compiler();
    Node externs = compiler.parseTestCode("var externObj;");
    Node root = compiler.parseTestCode("externObj.customProp = 10; var localObj = 20;");
    GlobalNamespace gn = new GlobalNamespace(compiler, externs, root);

    Map<String, GlobalNamespace.Name> index = gn.getNameIndex();
    GlobalNamespace.Name extName = index.get("externObj");
    assertNotNull(extName);
    assertTrue(extName.inExterns);
    assertFalse(extName.canCollapse());

    GlobalNamespace.Name localName = index.get("localObj");
    assertNotNull(localName);
    assertFalse(localName.inExterns);
  }

  @Test(timeout = 4000)
  public void testGetValueTypeOrAndHookBranches() {
    Compiler compiler = new Compiler();
    String js = "var orLit = false || {};\n" +
                "var orFunc = false || function() {};\n" +
                "var hookLit1 = true ? {} : 1;\n" +
                "var hookLit2 = true ? 1 : {};\n" +
                "var hookOther = true ? 1 : 2;\n";
    Node root = compiler.parseTestCode(js);
    GlobalNamespace gn = new GlobalNamespace(compiler, root);

    Map<String, GlobalNamespace.Name> index = gn.getNameIndex();
    assertEquals(GlobalNamespace.Name.Type.OBJECTLIT, index.get("orLit").type);
    assertEquals(GlobalNamespace.Name.Type.FUNCTION, index.get("orFunc").type);
    assertEquals(GlobalNamespace.Name.Type.OBJECTLIT, index.get("hookLit1").type);
    assertEquals(GlobalNamespace.Name.Type.OBJECTLIT, index.get("hookLit2").type);
    assertEquals(GlobalNamespace.Name.Type.OTHER, index.get("hookOther").type);
  }

  @Test(timeout = 4000)
  public void testDetermineGetTypeForHookOrBooleanExprVariations() {
    Compiler compiler = new Compiler();
    String js = "var a = {};\n" +
                "var b = {};\n" +
                "if (a || b) {}\n" +
                "while (a && b) {}\n" +
                "for (; a || b;) {}\n" +
                "typeof (a || b);\n" +
                "void (a || b);\n" +
                "!(a || b);\n" +
                "~(a || b);\n" +
                "+(a || b);\n" +
                "-(a || b);\n" +
                "a = a || {};\n" +
                "var aliasTarget = a || b;\n";
    Node root = compiler.parseTestCode(js);
    GlobalNamespace gn = new GlobalNamespace(compiler, root);

    Map<String, GlobalNamespace.Name> index = gn.getNameIndex();
    GlobalNamespace.Name nameA = index.get("a");
    assertNotNull(nameA);
    assertTrue(nameA.totalGets > 0);
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Closure Known Bugs)
  // =========================================================================

  /**
   * Ground Truth Defect: CollapsePropertiesTest::testTwinReferenceCancelsChildCollapsing
   *
   * In GlobalNamespace.Name.canCollapseUnannotatedChildNames():
   * The specification explicitly dictates that when a global set is a twin reference
   * (e.g. from a nested assignment var a = b = function() {};), child properties
   * MUST NOT be collapsed because an alias exists for the object.
   * On buggy versions, this twin reference check is omitted and returns true.
   */
  @Test(timeout = 4000)
  public void testTwinReferenceCancelsChildCollapsing() {
    GlobalNamespace.Name name = new GlobalNamespace.Name("twinParent", null, false);
    name.type = GlobalNamespace.Name.Type.FUNCTION;

    GlobalNamespace.Ref setRef = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.SET_FROM_GLOBAL);
    GlobalNamespace.Ref aliasRef = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.ALIASING_GET);
    GlobalNamespace.Ref.markTwins(setRef, aliasRef);

    name.addRef(setRef);
    name.addRef(aliasRef);

    assertEquals(1, name.globalSets);
    assertEquals(0, name.localSets);
    assertNotNull(name.declaration);
    assertNotNull(name.declaration.getTwin());

    // Expected correct behavior: twin reference cancels collapsing child names.
    assertFalse("Twin reference on declaration must cancel child collapsing",
        name.canCollapseUnannotatedChildNames());
  }

  /**
   * Defect Target: CollapsePropertiesTest::testCrashInNestedAssign & testCrashInCommaOperator
   *
   * Verifies that chained assignments inside comma operators or expressions
   * do not crash the AST traversal or twin marking logic.
   */
  @Test(timeout = 4000)
  public void testNestedAssignInCommaOperatorDoesNotCrash() {
    Compiler compiler = new Compiler();
    String js = "var a = {};\n" +
                "var b;\n" +
                "var c = (1, b = a = {});\n" +
                "a.x = 10;\n";
    Node root = compiler.parseTestCode(js);
    GlobalNamespace gn = new GlobalNamespace(compiler, root);

    Map<String, GlobalNamespace.Name> index = gn.getNameIndex();
    assertTrue(index.containsKey("a"));
    assertTrue(index.containsKey("b"));
    assertTrue(index.containsKey("a.x"));
  }

  @Test(timeout = 4000)
  public void testNestedAssignTwinReferences() {
    Compiler compiler = new Compiler();
    String js = "var a = {};\n" +
                "var b = a = {};\n";
    Node root = compiler.parseTestCode(js);
    GlobalNamespace gn = new GlobalNamespace(compiler, root);

    GlobalNamespace.Name nameA = gn.getNameIndex().get("a");
    assertNotNull(nameA);
    assertTrue("Nested assignment should create aliasing get", nameA.aliasingGets > 0);
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testMarkTwinsInvalidRefTypesBothGetsThrowsException() {
    GlobalNamespace.Ref r1 = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.DIRECT_GET);
    GlobalNamespace.Ref r2 = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.CALL_GET);
    GlobalNamespace.Ref.markTwins(r1, r2);
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testMarkTwinsInvalidRefTypesBothSetsThrowsException() {
    GlobalNamespace.Ref r1 = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.SET_FROM_GLOBAL);
    GlobalNamespace.Ref r2 = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.SET_FROM_LOCAL);
    GlobalNamespace.Ref.markTwins(r1, r2);
  }

  @Test(timeout = 4000)
  public void testMarkTwinsSuccessCondition() {
    GlobalNamespace.Ref set = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.SET_FROM_GLOBAL);
    GlobalNamespace.Ref alias = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.ALIASING_GET);

    GlobalNamespace.Ref.markTwins(set, alias);
    assertSame(alias, set.getTwin());
    assertSame(set, alias.getTwin());
  }

  @Test(timeout = 4000)
  public void testScanNewNodesWithFilter() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("var m = {}; m.n = 1;");
    GlobalNamespace gn = new GlobalNamespace(compiler, root);
    gn.getNameForest();

    Node notQualified = new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2));
    Node nameNode = Node.newString(Token.NAME, "m");
    Node propNode = new Node(Token.GETPROP, nameNode, Node.newString("n"));

    Set<Node> nodeSet = new HashSet<Node>();
    nodeSet.add(nameNode);

    SyntacticScopeCreator scopeCreator = new SyntacticScopeCreator(compiler);
    Scope scope = scopeCreator.createScope(root, null);

    gn.scanNewNodes(scope, nodeSet);
    assertNotNull(gn.getNameIndex().get("m"));
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testRefIsSetAndCloneAndReclassify() {
    GlobalNamespace.Ref setGlobal = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.SET_FROM_GLOBAL);
    GlobalNamespace.Ref setLocal = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.SET_FROM_LOCAL);
    GlobalNamespace.Ref directGet = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.DIRECT_GET);

    assertTrue(setGlobal.isSet());
    assertTrue(setLocal.isSet());
    assertFalse(directGet.isSet());

    GlobalNamespace.Ref reclassified = directGet.cloneAndReclassify(GlobalNamespace.Ref.Type.CALL_GET);
    assertEquals(GlobalNamespace.Ref.Type.CALL_GET, reclassified.type);
    assertNull(reclassified.node);
  }

  @Test(timeout = 4000)
  public void testNameAddAndRemoveAllRefTypes() {
    GlobalNamespace.Name name = new GlobalNamespace.Name("sample", null, false);

    GlobalNamespace.Ref decl = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.SET_FROM_GLOBAL);
    GlobalNamespace.Ref secondSet = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.SET_FROM_GLOBAL);
    GlobalNamespace.Ref localSet = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.SET_FROM_LOCAL);
    GlobalNamespace.Ref directGet = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.DIRECT_GET);
    GlobalNamespace.Ref protoGet = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.PROTOTYPE_GET);
    GlobalNamespace.Ref aliasGet = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.ALIASING_GET);
    GlobalNamespace.Ref callGet = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.CALL_GET);

    name.addRef(decl);
    assertSame(decl, name.declaration);
    assertEquals(1, name.globalSets);

    name.addRef(secondSet);
    assertEquals(2, name.globalSets);

    name.addRef(localSet);
    assertEquals(1, name.localSets);

    name.addRef(directGet);
    name.addRef(protoGet);
    name.addRef(aliasGet);
    name.addRef(callGet);
    assertEquals(4, name.totalGets);
    assertEquals(1, name.aliasingGets);
    assertEquals(1, name.callGets);

    // Removal lifecycle
    name.removeRef(callGet);
    assertEquals(3, name.totalGets);
    assertEquals(0, name.callGets);

    name.removeRef(aliasGet);
    assertEquals(2, name.totalGets);
    assertEquals(0, name.aliasingGets);

    name.removeRef(protoGet);
    name.removeRef(directGet);
    assertEquals(0, name.totalGets);

    name.removeRef(localSet);
    assertEquals(0, name.localSets);

    // Remove primary declaration, secondSet should be promoted
    name.removeRef(decl);
    assertSame(secondSet, name.declaration);
    assertEquals(1, name.globalSets);

    name.removeRef(secondSet);
    assertNull(name.declaration);
    assertEquals(0, name.globalSets);
  }

  @Test(timeout = 4000)
  public void testNameHierarchyAndToString() {
    GlobalNamespace.Name parent = new GlobalNamespace.Name("foo", null, false);
    GlobalNamespace.Name child = parent.addProperty("bar", false);
    GlobalNamespace.Name grandChild = child.addProperty("baz", false);

    assertEquals("foo", parent.fullName());
    assertEquals("foo.bar", child.fullName());
    assertEquals("foo.bar.baz", grandChild.fullName());

    assertTrue(parent.isSimpleName());
    assertFalse(child.isSimpleName());
    assertFalse(grandChild.isSimpleName());

    String str = child.toString();
    assertTrue(str.contains("foo.bar"));
    assertTrue(str.contains("globalSets=0"));
    assertTrue(str.contains("localSets=0"));
  }

  @Test(timeout = 4000)
  public void testNameCanEliminateAndNeedsToBeStubbed() {
    GlobalNamespace.Name name = new GlobalNamespace.Name("stubTarget", null, false);
    name.type = GlobalNamespace.Name.Type.OBJECTLIT;

    assertFalse(name.needsToBeStubbed());
    name.localSets = 2;
    assertTrue(name.needsToBeStubbed());

    name.globalSets = 1;
    name.localSets = 0;
    name.totalGets = 0;
    assertTrue(name.canEliminate());

    name.totalGets = 1;
    assertFalse(name.canEliminate());
  }
}