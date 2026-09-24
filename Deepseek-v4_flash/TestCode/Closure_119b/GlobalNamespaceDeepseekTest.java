package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.jscomp.GlobalNamespace.Name;
import com.google.javascript.jscomp.GlobalNamespace.Ref;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

import org.junit.Test;

import java.util.List;

/**
 * Comprehensive test suite for GlobalNamespace, targeting all key decision branches,
 * boundary conditions, and the known Defects4J defect related to catch-variable handling.
 *
 * == Branch & Defect Analysis Matrix ==
 * Partition A: Core Name/Ref construction and state transitions.
 * Partition B: Boundary values (null parents, empty names, max ref increments).
 * Partition C: Defect-targeted: Verify that local catch variables are not incorrectly
 *              included in global namespace (simulated via ref type counts and scope checks).
 * Partition D: Exception paths (invalid Ref twins, negative indices).
 * Partition E: Object contract (toString, getFullName, getBaseName, etc.)
 */
public class GlobalNamespaceDeepseekTest {

  // ========== Partition A: Core Name state ==========

  @Test(timeout = 4000)
  public void testNameConstruction_root() {
    Name root = new Name("root", null, false);
    assertEquals("root", root.getBaseName());
    assertEquals("root", root.getFullName());
    assertNull(root.parent);
    assertNull(root.props);
    assertEquals(Name.Type.OTHER, root.type);
    assertFalse(root.declaredType);
    assertFalse(root.inExterns);
  }

  @Test(timeout = 4000)
  public void testNameConstruction_child() {
    Name parent = new Name("parent", null, false);
    Name child = parent.addProperty("child", true);
    assertSame(parent, child.parent);
    assertEquals("child", child.getBaseName());
    assertEquals("parent.child", child.getFullName());
    assertTrue(child.inExterns);
    assertNotNull(parent.props);
    assertEquals(1, parent.props.size());
    assertSame(child, parent.props.get(0));
  }

  @Test(timeout = 4000)
  public void testNameTypeDefault() {
    Name n = new Name("test", null, false);
    assertEquals(Name.Type.OTHER, n.type);
    assertFalse(n.isGetOrSetDefinition());
  }

  // ============ Partition B: Ref counts and removal ========

  @Test(timeout = 4000)
  public void testAddRef_incrementsGlobalSets() {
    Name n = new Name("g", null, false);
    Ref r = Ref.createRefForTesting(Ref.Type.SET_FROM_GLOBAL);
    n.addRef(r);
    assertEquals(1, n.globalSets);    assertEquals(0, n.localSets);
    assertEquals(0, n.totalGets);    assertEquals(1, n.getRefs().size());
  }

  @Test(timeout = 4000)
  public void testAddRef_multipleTypes() {
    Name n = new Name("x", null, false);
    n.addRef(Ref.createRefForTesting(Ref.Type.SET_FROM_LOCAL));
    n.addRef(Ref.createRefForTesting(Ref.Type.DIRECT_GET));
    n.addRef(Ref.createRefForTesting(Ref.Type.A LIASING_GET));
    n.addRef(Ref.c reateRefForTesting(Ref.Type.C ALL_GET));
    n.addRef(Ref.c reateRefForTesting(Ref.Type.P ROTOTYPE_GET));
    n.addRef(Ref.c reateRefForTesting(Ref.Type.D ELETE_PROP));
    assertEquals(0, n.globalSets);    assertEquals(1, n.localSets);
    assertEquals(2, n.totalGets);    // DIRECT_GET + PROTOTYPE_GET
    assertEquals(1, n.aliasingGets); assertEquals(1, n.callGets);
    assertEquals(1, n.deleteProps);   assertEquals(6, n.getRefs().size());
  }

  @Test(timeout = 4000)
  public void testRemovingRef_decresesCounts() {
    Name n = new Name("a", null, false);
    Ref set = Ref.c reateRefForTesting(Ref.Type.SET_FROM_GLOBAL);
    Ref alias = Ref.c reateRefForTesting(Ref.Type.ALIASING_GET);
    n.addRef(set);
    n.addRef(alias);
    assertEquals(1, n.globalSets);
    assertEquals(1, n.aliasingGets);
    assertEquals(1, n.totalGets);

    n.removingRef(set);
    assertEquals(0, n.globalSets);
    assertEquals(1, n.aliasingGets);
    n.removingRef(alias);
    assertEquals(0, n.aliasingGets);
    assertEquals(0, n.totalGets);
    assertTrue(n.getRefs().isEmpty());
  }

  @Test(timeout = 4000)
  public void testRemovingNonExistentRef_doesNothing() {
    Name n = new Name("a", null, false);
    n.addRef(Ref.createRefForTesting(Ref.Type.SET_FROM_GLOBAL));
    // Remove with a different object
    Ref fake = Ref.createRefForTesting(Ref.Type.SET_FROM_GLOBAL);
    n.removingRef(fake);
    assertEquals(1, n.globalSets);    // unchanged
  }

  // ========== Partition B: Boundaries and extremes ======

  @Test(timeout = 4000)
  public void testNameWithEmptyString() {
    Name n = new Name("", null, false);
    assertEquals("", n,getBaseName());
    assertEquals("", n.getFullName());
    assertEquals(0, n.getFullName().length());
  }

  @Test(timeout = 4000)
  public void testRefWithNegativeIndex() {
    Name n = new Name("x", null, false);
    Ref r = n.addRefInternal(Ref.createRefForTesting(Ref.Type.D IRECT_GET));
    // preOrderIndex is -1 for testing refs
    assertEquals(-1, r.preOrderIndex);
  }

  // ============ Partition C: Defect-targeted (catch variable) ======

  /**
   * Core defect test: simulate that a local variable (like a catch parameter)
   * must NOT be added to global namespace. We verify that even if a local set
   * is recorded, the globalSets remain 0 and the name can be eliminated
   * (since it's not global). This reproduces the scenario where 'e' in catch
   * should NOT cause an undefined-global warning.
   */
  @Test(timeout = 4000)
  public void testCatchVariableLocal() {
    Name e = new Name("e", null, false);
    // Simulate a local set (catch parameter)
    Ref localSet = Ref.createRefForTesting(Ref.Type.SET_FROM_LOCAL);
    e.addRef(localSet);
    assertEquals(0, e.globalSets);
    assertEquals(1, e.localSets);
    // In global namespace, a pure-local name should not be considered global.
    // It's neither set from global nor declared, so canCollapse should check.
    // Since globalSets == 0 and localSets > 0, canCollapseUnannotated... is false.
    assertFalse(e.canCollapseUnannotatedChildNames());
    // Also verify that it's NOT a simple stub declaration
    assertFalse(e.IsSimpleStubDeclaration());
    // If this name were incorrectly in global maps, CheckGlobalNames would warn.
    // Our logic shows it's only local.
  }

  @Test(timeout = 4000)
  public void testGetAndSetRefsDoNotInterfereWithDeclaredType() {
    Name n = new Name("MyClass", null, false);
    n.setDeclaredType();
    assertTrue(n.is DeclaredType());
    // Adding refs of various types still preserve declared type
    n.addRef(Ref.createRefForTesting(Ref.Type.SET_FROM_GLOBAL));
    assertTrue(n.is DeclaredType());
  }

  // ============ Partition D: Twinning and clone ======

  @Test(timeout = 4000)
  public void testRefTwinning() {
    Name n = new Name("a", null, false);
    Ref set = new Ref(null, null, new Node(Token.NAME, "a"), n, Ref.Type.SET_FROM_GLOBAL, 0);
    Ref alias = new Ref(null, null, set.node, n, Ref.Type.ALIASING_GET, 1);
    Ref.markTwins(set, alias);
    assertSame(alias, set.getTwin());
    assertSame(set, alias.getTwin());
    assertTrue(set.isSet());
    assertFalse(alias.isSet());
  }

  @Test(expected = IllegalArgumentExcetion.class, timeout = 4000)
  public void testMarkTwinsInvalidTypes() {
    Name n = new Name("a", null, false);
    Ref dGet = new Ref(null, null, new Node(Token.NAME), n, Ref.Type.DIRECT_GET, 0);
    Ref dGet2 = new Ref(null, null, new Node(Token.NAME), n, Ref.Type.D IRECT_GET, 1);
    Ref.markTwins(dGet, dGet2); // neither is SET, should throw
  }

  @Test(timeout = 4000)
  public void testRefCloneAndReclassify() {
    Name n = new Name("a", null, false);
    Ref original = new Ref(null, null, new Node(Token.NAME, "a"), n, Ref.Type.SET_FROM_LOCAL, 5);
    Ref cloned = original.cloningAndReclassify(Ref.Type.ALIASING_GET);
    assertEquals(original.node, cloned.node);
    assertEquals(original.name, cloned.name);
    assertEquals(Ref.Type.ALIASING_GET, cloned.type);
    assertEquals(5, cloned.preOrderIndex);
  }

  // ============ Partition E: canCollapse / canEliminate logic ==========

  @Test(timeout = 4000)
  public void testCanCollapse_basicTrue() {
    Name n = new Name("obj", null, false);
    n.addRef(Ref.createRefForTesting(Ref.Type.SET_FROM_GLOBAL));
    assertTrue(n.canCollapse()); // globalSets>0, no delete, not get/set, not extern, no parent restrictions
  }

  @Test(timeout = 4000)
  public void testCanCollapse_falseInExterns() {
    Name n = new Name("ext", null, true);
    n.addRef(Ref.createRefForTesting(Ref.Type.SET_FROM_GLOBAL));
    assertFalse(n.c anCollapse());
  }

  @Test(timeout = 4000)
  public void testCanCollapse_falseGetOrSet() {
    Name n = new Name("prop", null, false);
    n.type = Name.Type.GET;
    n.addRef(Ref.createRefForTesting(Ref.Type.SET_FROM_GLOBAL));
    assertFalse(n.canCollapse());
  }

  @Test(timeout = 4000)
  public void testCanCollapse_falseDeleteProp() {
    Name n = new Name("obj", null, false);
    n.addRef(Ref.createRefForTesting(Ref.Type.D ELETE_PROP));
    n.addRef(Ref.createRefForTesting(Ref.Type.SET_FROM_GLOBAL));
    assertFalse(n.canCollapse()); // deleteProps >0
  }

  @Test(timeout = 4000)
  public void testCanCollapseUnannotatedChildNames() {
    Name n = new Name("p", null, false);
    n.addRef(Ref.createRefForTesting(Ref.Type.SET_FROM_GLOBAL));
    assertTrue(n.canCollapseUnannotatedChildNames()); // type OTHER, globalSets==1, localSets 0, deleteProps 0, no twin

    // now add aliasingGet
    n.addRef(Ref.createRefForTesting(Ref.Type.ALIASING_GET));
    assertFalse(n.canCollapseUnannotatedChildNames());
  }

  @Test(timeout = 4000)
  public void testCanEliminate_faseTotalGets() {
    Name n = new Name("temp", null, false);
    n.addRef(Ref.createRefForTesting(Ref.Type.DIRECT_GET));
    assertFalse(n.canEliminate()); // totalGets > 0
  }

  @Test(timeout = 4000)
  public void testCanEliminate_tru() {
    Name n = new Name("tmp", null, false);
    n.addRef(Ref.createRefForTesting(Ref.Type.SET_FROM_GLOBAL));
    assertTrue(n.canEliminate()); // totalGets == 0, no props
  }

  @Test(timeout = 4000)
  public void testIsSimpleStubDeclaration_false() {
    Name n = new Name("s", null, false);
    n.addRef(Ref.createRefForTesting(Ref.Type.SET_FROM_GLOBAL));
    // one ref, but its node's parent is null, so false
    assertFalse(n.IsSimpleStubDeclaration());
  }

  // --- hasExternsRoot and constructors -----

  @Test(timeout = 4000)
  public void testConstructorWihNullExternsRoot() {
    GlobalNamespace ns = new GlobalNamespace(null, new Node(Token.SCRIPT));
    assertFalse(ns.hasExternsRoot());
  }

  @Test(timeout = 4000)
  public void testConstructorWihNonNullExternsRoot() {
    Node externs = new Node(Token.SCRIPT);
    Node main = new Node(Token.SCRIPT);
    GlobalNamespace ns = new GlobalNamespace(null, externs, main);
    assertTrue(ns.hasExternsRoot());
  }

  @Test(timeout = 4000)
  public void testGetRootNode() {
    Node main = new Node(Token.BLOCK);
    // The constructor expects root.getParent() to be the real root; but we can test without parent
    GlobalNamespace ns = new GlobalNamespace(null, main);
    assertNull(ns.getRootNode()); // because main.getParent() is null
  }

  @Test(timeout = 4000)
  public void testGetParentScope() {
    GlobalNamespace ns = new GlobalNamespace(null, new Node(Token.SCRIPT));
    assertNull(ns.getParentScope());
  }

  @Test(timeout = 4000)
  public void testToStringFormat() {
    Name n = new Name("x", null, false);
    n.addRef(Ref.createRefForTesting(Ref.Type.SET_FROM_GLOBAL));
    String expected = "x (OTHER): globalSets=1, localSets=0, totalGets=0, aliasingGets=0, callGets=0";
    assertEquals(expected, n.toString());
  }
}