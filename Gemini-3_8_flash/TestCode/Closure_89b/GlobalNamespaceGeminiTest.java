/*
 * Copyright 2006 The Closure Compiler Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

/* [Branch & Defect Analysis Matrix]
 * -------------------------------------------------------------------------------------------------------------------
 * Target Class: GlobalNamespace
 * Targeted Branches & Decision Logic:
 *  - GlobalNamespace:
 *    * process(): externsRoot != null branch (inExterns true/false transitions).
 *    * isGlobalNameReference() / getTopVarName() / isGlobalVarReference(): dot index finding, externsScope fallback.
 *    * isGlobalScope(): parent scope null vs non-null (local).
 *  - BuildGlobalNamespace:
 *    * visit(): Token.STRING, Token.NAME (VAR, ASSIGN, GETPROP, FUNCTION), Token.GETPROP (ASSIGN, GETPROP), default.
 *    * getNameForObjLitKey(): NAME (VAR greatGramps), ASSIGN, STRING (nested OBJLIT), invalid JS identifiers.
 *    * getValueType(): OBJECTLIT, FUNCTION, OR (last child recursion), HOOK (2nd/3rd child recursion), OTHER.
 *    * handleSetFromGlobal() vs handleSetFromLocal(): nested assign (markTwins), isConstructorOrEnumDeclaration.
 *    * isConstructorOrEnumDeclaration(): ASSIGN vs VAR, JSDoc constructor/enum validation.
 *    * handleGet(): IF, TYPEOF, VOID, NOT, BITNOT, POS, NEG, CALL, NEW, OR, AND, HOOK contexts.
 *    * determineGetTypeForHookOrBooleanExpr(): ancestor traversal (EXPR_RESULT, VAR, IF, WHILE, FOR, ASSIGN, NAME, CALL).
 *    * maybeHandlePrototypePrefix(): ".prototype" suffix, ".prototype." infix, multi-level nesting, OBJLIT key guard.
 *  - Name:
 *    * addRef() / removeRef(): declaration tracking, declaration promotion from refs, counter increments/decrements.
 *    * canEliminate(): totalGets guard, props collapse recursion.
 *    * canCollapse(): inExterns guard, isClassOrEnum, parent unannotated child collapse check.
 *    * canCollapseUnannotatedChildNames(): type == OTHER, globalSets != 1, localSets != 0, twin ref guard,
 *      parent.shouldKeepKeys(), aliasingGets on FUNCTION vs OTHER types [DEFECT ZONE].
 *    * needsToBeStubbed(): globalSets == 0 && localSets > 0.
 *    * isNamespace(): hasClassOrEnumDescendant && type == OBJECTLIT.
 *  - Ref:
 *    * markTwins(): precondition checks, bidirectional linkage.
 *    * isSet(): SET_FROM_GLOBAL / SET_FROM_LOCAL vs get types.
 *    * cloneAndReclassify(): state copy with altered type.
 *
 * Known Defect (Closure / Defects4J):
 *  - CollapseProperties / GlobalNamespace: canCollapseUnannotatedChildNames() previously excluded FUNCTION
 *    from the aliasing check (if (type != Type.FUNCTION && aliasingGets > 0)). When an alias is created for a
 *    function (aliasingGets > 0), collapsing its child properties causes runtime breakages since alias references
 *    cannot access the renamed/flattened properties.
 * -------------------------------------------------------------------------------------------------------------------
 */
public class GlobalNamespaceGeminiTest {

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testGlobalVariableLifecycleAndNames() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("var a = 1; a = 2; var b = a;");
    GlobalNamespace gn = new GlobalNamespace(compiler, root);

    Map<String, GlobalNamespace.Name> index = gn.getNameIndex();
    assertNotNull("Name index must not be null", index);
    assertTrue("Index must contain variable 'a'", index.containsKey("a"));
    assertTrue("Index must contain variable 'b'", index.containsKey("b"));

    GlobalNamespace.Name aName = index.get("a");
    assertEquals("a", aName.fullName());
    assertEquals(1, aName.globalSets);
    assertEquals(1, aName.totalGets);
    assertEquals(1, aName.aliasingGets);
    assertTrue(aName.isSimpleName());

    List<GlobalNamespace.Name> forest = gn.getNameForest();
    assertEquals(2, forest.size());
  }

  @Test(timeout = 4000)
  public void testObjectLiteralKeyHierarchy() {
    Compiler compiler = new Compiler();
    String js = "var ns = { nested: { prop: 10 }, fn: function() {} };";
    Node root = compiler.parseTestCode(js);
    GlobalNamespace gn = new GlobalNamespace(compiler, root);

    Map<String, GlobalNamespace.Name> index = gn.getNameIndex();
    assertTrue(index.containsKey("ns"));
    assertTrue(index.containsKey("ns.nested"));
    assertTrue(index.containsKey("ns.nested.prop"));
    assertTrue(index.containsKey("ns.fn"));

    GlobalNamespace.Name nsName = index.get("ns");
    assertEquals(GlobalNamespace.Name.Type.OBJECTLIT, nsName.type);
    assertNotNull(nsName.props);
    assertEquals(2, nsName.props.size());

    GlobalNamespace.Name fnName = index.get("ns.fn");
    assertEquals(GlobalNamespace.Name.Type.FUNCTION, fnName.type);
    assertEquals("ns.fn", fnName.fullName());
    assertFalse(fnName.isSimpleName());
  }

  @Test(timeout = 4000)
  public void testFunctionDeclarationAndInvocation() {
    Compiler compiler = new Compiler();
    String js = "function greet() {} greet(); var alias = greet;";
    Node root = compiler.parseTestCode(js);
    GlobalNamespace gn = new GlobalNamespace(compiler, root);

    GlobalNamespace.Name greetName = gn.getNameIndex().get("greet");
    assertNotNull(greetName);
    assertEquals(GlobalNamespace.Name.Type.FUNCTION, greetName.type);
    assertEquals(1, greetName.globalSets);
    assertEquals(2, greetName.totalGets);
    assertEquals(1, greetName.callGets);
    assertEquals(1, greetName.aliasingGets);
  }

  @Test(timeout = 4000)
  public void testConstructorAndEnumDetection() {
    Compiler compiler = new Compiler();
    String js =
        "var ns = {};\n" +
        "/** @constructor */ ns.MyClass = function() {};\n" +
        "/** @enum {number} */ ns.MyEnum = { FIRST: 1, SECOND: 2 };\n";
    Node root = compiler.parseTestCode(js);
    GlobalNamespace gn = new GlobalNamespace(compiler, root);

    Map<String, GlobalNamespace.Name> index = gn.getNameIndex();
    GlobalNamespace.Name nsName = index.get("ns");
    GlobalNamespace.Name className = index.get("ns.MyClass");
    GlobalNamespace.Name enumName = index.get("ns.MyEnum");

    assertNotNull(className);
    assertNotNull(enumName);
    assertTrue("ns must be identified as a namespace due to class/enum children", nsName.isNamespace());
    assertTrue("Class constructor must be recognized", className.canCollapse());
    assertTrue("Enum must be recognized", enumName.canCollapse());
  }

  @Test(timeout = 4000)
  public void testExternsHandling() {
    Compiler compiler = new Compiler();
    Node externs = compiler.parseTestCode("var extObj = {}; extObj.extProp = 1;");
    Node root = compiler.parseTestCode("extObj.extProp = 2; var localRef = extObj.extProp;");
    GlobalNamespace gn = new GlobalNamespace(compiler, externs, root);

    Map<String, GlobalNamespace.Name> index = gn.getNameIndex();
    GlobalNamespace.Name extObj = index.get("extObj");
    GlobalNamespace.Name extProp = index.get("extObj.extProp");

    assertNotNull(extObj);
    assertNotNull(extProp);
    assertTrue("Extern name must be marked inExterns", extObj.inExterns);
    assertFalse("Extern name cannot be collapsed", extObj.canCollapse());
    assertFalse("Extern property cannot be collapsed", extProp.canCollapse());
  }

  @Test(timeout = 4000)
  public void testPrototypePrefixResolution() {
    Compiler compiler = new Compiler();
    String js =
        "function Widget() {}\n" +
        "Widget.prototype.render = function() {};\n" +
        "Widget.prototype.theme.color = 0xffffff;\n" +
        "var proto = Widget.prototype;\n" +
        "Widget.prototype = { render2: function() {} };\n";
    Node root = compiler.parseTestCode(js);
    GlobalNamespace gn = new GlobalNamespace(compiler, root);

    Map<String, GlobalNamespace.Name> index = gn.getNameIndex();
    GlobalNamespace.Name widgetName = index.get("Widget");
    assertNotNull(widgetName);
    assertTrue("Widget prototype accesses must register reads on Widget", widgetName.totalGets > 0);
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testNonIdentifierObjLitKeys() {
    Compiler compiler = new Compiler();
    String js = "var m = { 'invalid identifier': 1, '123_numeric': 2, validProp: 3 };";
    Node root = compiler.parseTestCode(js);
    GlobalNamespace gn = new GlobalNamespace(compiler, root);

    Map<String, GlobalNamespace.Name> index = gn.getNameIndex();
    assertTrue(index.containsKey("m"));
    assertTrue(index.containsKey("m.validProp"));
    assertFalse("Invalid JS identifier must not form global name", index.containsKey("m.invalid identifier"));
    assertFalse("Numeric start JS identifier must not form global name", index.containsKey("m.123_numeric"));
  }

  @Test(timeout = 4000)
  public void testEmptyOrUninitializedVar() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("var uninit;");
    GlobalNamespace gn = new GlobalNamespace(compiler, root);

    GlobalNamespace.Name name = gn.getNameIndex().get("uninit");
    assertNotNull(name);
    assertEquals(GlobalNamespace.Name.Type.OTHER, name.type);
    assertEquals(1, name.globalSets);
    assertEquals(0, name.totalGets);
  }

  @Test(timeout = 4000)
  public void testMultipleSetsAndDeclarationPromotionOnRemoval() {
    GlobalNamespace.Name name = new GlobalNamespace.Name("testVar", null, false);
    GlobalNamespace.Ref ref1 = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.SET_FROM_GLOBAL);
    GlobalNamespace.Ref ref2 = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.SET_FROM_GLOBAL);
    GlobalNamespace.Ref getRef = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.DIRECT_GET);

    name.addRef(ref1);
    name.addRef(ref2);
    name.addRef(getRef);

    assertEquals(ref1, name.declaration);
    assertEquals(2, name.globalSets);
    assertEquals(1, name.totalGets);

    // Remove primary declaration: ref2 must be promoted to declaration
    name.removeRef(ref1);
    assertEquals("Secondary global set must be promoted to declaration", ref2, name.declaration);
    assertEquals(1, name.globalSets);

    // Remove promoted declaration: declaration becomes null
    name.removeRef(ref2);
    assertNull("Declaration must become null when all global sets are removed", name.declaration);
    assertEquals(0, name.globalSets);

    // Remove remaining getRef
    name.removeRef(getRef);
    assertEquals(0, name.totalGets);

    // Removing an already removed or absent ref must be a no-op
    name.removeRef(getRef);
    assertEquals(0, name.totalGets);
  }

  @Test(timeout = 4000)
  public void testBooleanAndHookContexts() {
    Compiler compiler = new Compiler();
    String js =
        "var a = 1;\n" +
        "var b = a || 2;\n" +
        "var c = 1 && a;\n" +
        "var d = a ? 1 : 2;\n" +
        "var e = true ? a : 2;\n" +
        "var f = true ? 1 : a;\n" +
        "if (a) {}\n" +
        "typeof a;\n" +
        "!a;\n";
    Node root = compiler.parseTestCode(js);
    GlobalNamespace gn = new GlobalNamespace(compiler, root);

    GlobalNamespace.Name aName = gn.getNameIndex().get("a");
    assertNotNull(aName);
    assertTrue(aName.totalGets >= 7);
  }

  @Test(timeout = 4000)
  public void testNestedAssignmentsMarkTwins() {
    Compiler compiler = new Compiler();
    String js = "var a; var b = (a = 2);";
    Node root = compiler.parseTestCode(js);
    GlobalNamespace gn = new GlobalNamespace(compiler, root);

    GlobalNamespace.Name aName = gn.getNameIndex().get("a");
    assertNotNull(aName);
    assertTrue("Nested assignment to 'a' must introduce an aliasing get", aName.aliasingGets > 0);
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Closure Defects4J Ground Truth)
  // =========================================================================

  /*
   * Targets Defect: CollapseProperties / GlobalNamespace:
   * Ground truth: canCollapseUnannotatedChildNames() checked:
   *    if (type != Type.FUNCTION && aliasingGets > 0) return false;
   * This allowed functions that are aliased to return true, leading to faulty property collapsing.
   * An aliased function (aliasingGets > 0) MUST NOT allow its unannotated child names to be collapsed.
   */
  @Test(timeout = 4000)
  public void testDefectAliasedFunctionCannotCollapseUnannotatedChildNamesUnit() {
    GlobalNamespace.Name fnName = new GlobalNamespace.Name("targetFunc", null, false);
    fnName.type = GlobalNamespace.Name.Type.FUNCTION;
    fnName.globalSets = 1;
    fnName.localSets = 0;
    fnName.aliasingGets = 1;
    fnName.declaration = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.SET_FROM_GLOBAL);

    assertFalse("Aliased function (aliasingGets > 0) must not collapse unannotated child names",
        fnName.canCollapseUnannotatedChildNames());
  }

  @Test(timeout = 4000)
  public void testDefectAliasedFunctionInAST() {
    Compiler compiler = new Compiler();
    String js = "var a = function() {}; var b = a; a.c = 1;";
    Node root = compiler.parseTestCode(js);
    GlobalNamespace gn = new GlobalNamespace(compiler, root);

    Map<String, GlobalNamespace.Name> names = gn.getNameIndex();
    GlobalNamespace.Name aName = names.get("a");
    assertNotNull("Function 'a' must exist in global namespace", aName);
    assertEquals(GlobalNamespace.Name.Type.FUNCTION, aName.type);
    assertTrue("Function 'a' must register an aliasing get through 'b = a'", aName.aliasingGets > 0);

    assertFalse("Child names of aliased function 'a' must not be collapsible",
        aName.canCollapseUnannotatedChildNames());
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testMarkTwinsInvalidArgumentsNeitherIsAliasingGet() {
    GlobalNamespace.Ref refA = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.DIRECT_GET);
    GlobalNamespace.Ref refB = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.SET_FROM_GLOBAL);
    GlobalNamespace.Ref.markTwins(refA, refB);
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testMarkTwinsInvalidArgumentsNeitherIsSet() {
    GlobalNamespace.Ref refA = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.ALIASING_GET);
    GlobalNamespace.Ref refB = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.CALL_GET);
    GlobalNamespace.Ref.markTwins(refA, refB);
  }

  @Test(expected = NullPointerException.class, timeout = 4000)
  public void testCanCollapseUnannotatedChildNamesNullDeclarationGuarded() {
    GlobalNamespace.Name name = new GlobalNamespace.Name("orphan", null, false);
    name.type = GlobalNamespace.Name.Type.OBJECTLIT;
    name.globalSets = 1;
    name.localSets = 0;
    name.declaration = null; // triggers Preconditions.checkNotNull(declaration)
    name.canCollapseUnannotatedChildNames();
  }

  @Test(timeout = 4000)
  public void testScanNewNodesWithFilter() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("var target = {}; target.prop = 1;");
    GlobalNamespace gn = new GlobalNamespace(compiler, root);
    gn.getNameIndex(); // process initial AST

    final Scope[] scopeHolder = new Scope[1];
    NodeTraversal.traverse(compiler, root, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        if (scopeHolder[0] == null) {
          scopeHolder[0] = t.getScope();
        }
      }
    });

    assertNotNull("Scope must be captured", scopeHolder[0]);
    Set<Node> newNodes = new HashSet<Node>();
    newNodes.add(root.getFirstChild()); // add SCRIPT or VAR

    // Must execute without exception
    gn.scanNewNodes(scopeHolder[0], newNodes);
    gn.scanNewNodes(scopeHolder[0], Collections.<Node>emptySet());
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testRefIsSetAndClone() {
    GlobalNamespace.Ref setGlobal = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.SET_FROM_GLOBAL);
    GlobalNamespace.Ref setLocal = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.SET_FROM_LOCAL);
    GlobalNamespace.Ref directGet = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.DIRECT_GET);
    GlobalNamespace.Ref aliasingGet = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.ALIASING_GET);
    GlobalNamespace.Ref callGet = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.CALL_GET);

    assertTrue(setGlobal.isSet());
    assertTrue(setLocal.isSet());
    assertFalse(directGet.isSet());
    assertFalse(aliasingGet.isSet());
    assertFalse(callGet.isSet());

    GlobalNamespace.Ref.markTwins(aliasingGet, setGlobal);
    assertEquals(setGlobal, aliasingGet.getTwin());
    assertEquals(aliasingGet, setGlobal.getTwin());

    GlobalNamespace.Ref reclassified = directGet.cloneAndReclassify(GlobalNamespace.Ref.Type.CALL_GET);
    assertEquals(GlobalNamespace.Ref.Type.CALL_GET, reclassified.type);
    assertNull(reclassified.getTwin());
  }

  @Test(timeout = 4000)
  public void testNameToStringAndHierarchy() {
    GlobalNamespace.Name parent = new GlobalNamespace.Name("Parent", null, false);
    parent.type = GlobalNamespace.Name.Type.OBJECTLIT;
    GlobalNamespace.Name child = parent.addProperty("child", false);
    child.type = GlobalNamespace.Name.Type.OTHER;

    assertEquals("Parent.child", child.fullName());
    assertTrue(parent.isSimpleName());
    assertFalse(child.isSimpleName());

    String str = parent.toString();
    assertTrue(str.contains("Parent"));
    assertTrue(str.contains("OBJECTLIT"));
    assertTrue(str.contains("globalSets=0"));
  }

  @Test(timeout = 4000)
  public void testNeedsToBeStubbedAndKeepKeys() {
    GlobalNamespace.Name name = new GlobalNamespace.Name("stubCandidate", null, false);
    name.globalSets = 0;
    name.localSets = 2;
    assertTrue("Variable with only local sets needs to be stubbed", name.needsToBeStubbed());

    name.globalSets = 1;
    assertFalse("Variable with global set does not need to be stubbed", name.needsToBeStubbed());

    GlobalNamespace.Name objLit = new GlobalNamespace.Name("obj", null, false);
    objLit.type = GlobalNamespace.Name.Type.OBJECTLIT;
    objLit.aliasingGets = 1;
    assertTrue("Aliased object literal must keep keys", objLit.shouldKeepKeys());

    objLit.aliasingGets = 0;
    assertFalse("Non-aliased object literal does not need to keep keys", objLit.shouldKeepKeys());
  }

  @Test(timeout = 4000)
  public void testCanEliminateContract() {
    GlobalNamespace.Name parent = new GlobalNamespace.Name("mod", null, false);
    parent.type = GlobalNamespace.Name.Type.OBJECTLIT;
    parent.globalSets = 1;
    parent.declaration = GlobalNamespace.Ref.createRefForTesting(GlobalNamespace.Ref.Type.SET_FROM_GLOBAL);

    assertTrue("Parent without gets or uncollapsible children can be eliminated", parent.canEliminate());

    parent.totalGets = 1;
    assertFalse("Parent with gets cannot be eliminated", parent.canEliminate());
    parent.totalGets = 0;

    GlobalNamespace.Name uncollapsibleChild = parent.addProperty("child", false);
    uncollapsibleChild.inExterns = true; // prevents child from collapsing
    assertFalse("Parent with uncollapsible child cannot be eliminated", parent.canEliminate());
  }
}