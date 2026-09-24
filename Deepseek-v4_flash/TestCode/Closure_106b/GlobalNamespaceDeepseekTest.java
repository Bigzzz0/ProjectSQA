package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.junit.Test;

public class GlobalNamespaceDeepseekTest {

  /*
   * [Branch & Defect Analysis Matrix]
   *
   * Partitions targeted:
   *  A. Core Name/Ref state transitions: addRef/removeRef counters, declaration
   *     promotion, set/get reference classification, twin reference handling.
   *  B. Boundary values: null/empty refs, no sets, multiple sets, externs,
   *     aliasing-get suppression, class/enum bypass, simple vs qualified names.
   *  C. Defect-targeted branches:
   *      - A twin reference must cancel child collapsing even for FUNCTION names.
   *        This maps to CollapsePropertiesTest#testTwinReferenceCancelsChildCollapsing.
   *      - An ASSIGN inside a COMMA expression whose value is discarded must not
   *        be considered a nested assignment. This maps to
   *        CollapsePropertiesTest#testCrashInCommaOperator.
   *  D. Object-literal name resolution: simple var, nested object literals,
   *     assignment lhs, invalid identifiers, non-key nodes.
   *  E. Hook/boolean expression alias analysis: same/different assignment target,
   *     var declaration, expression statement, CALL, HOOK condition, orphan node.
   */

  private static Ref ref(Ref.Type type) {
    return Ref.createRefForTesting(type);
  }

  private static Node newOrNode() {
    return new Node(Token.OR, Node.newNumber(0), Node.newNumber(1));
  }

  private static Object builder() throws Exception {
    return newBuildGlobalNamespace(new GlobalNamespace(null, null, null));
  }

  private static Object newBuildGlobalNamespace(GlobalNamespace ns) throws Exception {
    Class<?> clazz = Class.forName("com.google.javascript.jscomp.GlobalNamespace$BuildGlobalNamespace");
    Constructor<?> ctor = clazz.getDeclaredConstructor(GlobalNamespace.class);
    ctor.setAccessible(true);
    return ctor.newInstance(ns);
  }

  private static Object call(
      Object builder, String methodName, Class<?>[] paramTypes, Object... args) throws Exception {
    Method method = builder.getClass().getDeclaredMethod(methodName, paramTypes);
    method.setAccessible(true);
    return method.invoke(builder, args);
  }

  private static Name.Type getValueType(Object builder, Node n) throws Exception {
    return (Name.Type) call(builder, "getValueType", new Class<?>[] {Node.class}, n);
  }

  private static String getNameForObjLitKey(Object builder, Node n) throws Exception {
    return (String) call(builder, "getNameForObjLitKey", new Class<?>[] {Node.class}, n);
  }

  private static boolean isNestedAssign(Object builder, Node parent) throws Exception {
    return (Boolean) call(builder, "isNestedAssign", new Class<?>[] {Node.class}, parent);
  }

  private static Ref.Type determineGetType(Object builder, Node parent, String name)
      throws Exception {
    return (Ref.Type) call(
        builder,
        "determineGetTypeForHookOrBooleanExpr",
        new Class<?>[] {NodeTraversal.class, Node.class, String.class},
        (Object) null,
        parent,
        name);
  }

  @Test(timeout = 4000)
  public void testAddRefTracksFirstGlobalDeclaration() {
    Name name = new Name("a", null, false);
    Ref set = ref(Ref.Type.SET_FROM_GLOBAL);
    name.addRef(set);
    assertSame(set, name.declaration);
    assertNull(name.refs);
    assertEquals(1, name.globalSets);
  }

  @Test(timeout = 4000)
  public void testAddRefSecondGlobalSetGoesToRefs() {
    Name name = new Name("a", null, false);
    Ref first = ref(Ref.Type.SET_FROM_GLOBAL);
    Ref second = ref(Ref.Type.SET_FROM_GLOBAL);
    name.addRef(first);
    name.addRef(second);
    assertSame(first, name.declaration);
    assertEquals(2, name.globalSets);
    assertNotNull(name.refs);
    assertEquals(1, name.refs.size());
    assertSame(second, name.refs.get(0));
  }

  @Test(timeout = 4000)
  public void testAddRefLocalAndGetTypes() {
    Name name = new Name("a", null, false);
    name.addRef(ref(Ref.Type.SET_FROM_LOCAL));
    name.addRef(ref(Ref.Type.PROTOTYPE_GET));
    name.addRef(ref(Ref.Type.DIRECT_GET));
    name.addRef(ref(Ref.Type.ALIASING_GET));
    name.addRef(ref(Ref.Type.CALL_GET));
    assertEquals(1, name.localSets);
    assertEquals(1, name.aliasingGets);
    assertEquals(1, name.callGets);
    assertEquals(4, name.totalGets);
    assertEquals(5, name.refs.size());
  }

  @Test(timeout = 4000)
  public void testRemoveRefNonDeclarationAndGlobalSetFromRefs() {
    Name name = new Name("a", null, false);
    Ref local = ref(Ref.Type.SET_FROM_LOCAL);
    Ref direct = ref(Ref.Type.DIRECT_GET);
    Ref alias = ref(Ref.Type.ALIASING_GET);
    name.addRef(local);
    name.addRef(direct);
    name.addRef(alias);
    name.removeRef(direct);
    assertEquals(1, name.totalGets);
    assertEquals(1, name.aliasingGets);
    assertEquals(2, name.refs.size());
    name.removeRef(ref(Ref.Type.DIRECT_GET));
    assertEquals(1, name.totalGets);
    assertEquals(2, name.refs.size());

    Name g = new Name("g", null, false);
    Ref first = ref(Ref.Type.SET_FROM_GLOBAL);
    Ref second = ref(Ref.Type.SET_FROM_GLOBAL);
    g.addRef(first);
    g.addRef(second);
    g.removeRef(second);
    assertSame(first, g.declaration);
    assertEquals(1, g.globalSets);
    assertEquals(0, g.refs.size());
  }

  @Test(timeout = 4000)
  public void testRemoveRefPromotesNewDeclaration() {
    Name name = new Name("a", null, false);
    Ref first = ref(Ref.Type.SET_FROM_GLOBAL);
    Ref second = ref(Ref.Type.SET_FROM_GLOBAL);
    name.addRef(first);
    name.addRef(second);
    name.removeRef(first);
    assertSame(second, name.declaration);
    assertEquals(1, name.globalSets);
    assertNotNull(name.refs);
    assertEquals(0, name.refs.size());
  }

  @Test(timeout = 4000)
  public void testRemoveRefOnlyDeclaration() {
    Name name = new Name("a", null, false);
    Ref set = ref(Ref.Type.SET_FROM_GLOBAL);
    name.addRef(set);
    name.removeRef(set);
    assertNull(name.declaration);
    assertEquals(0, name.globalSets);
    assertNull(name.refs);
  }

  @Test(timeout = 4000)
  public void testCanCollapseUnannotatedChildNamesRequiresObjectLikeState() {
    Name other = new Name("a", null, false);
    other.type = Name.Type.OTHER;
    other.globalSets = 1;
    assertFalse(other.canCollapseUnannotatedChildNames());

    Name wrongSets = new Name("b", null, false);
    wrongSets.type = Name.Type.OBJECTLIT;
    wrongSets.globalSets = 2;
    assertFalse(wrongSets.canCollapseUnannotatedChildNames());

    Name localSets = new Name("c", null, false);
    localSets.type = Name.Type.OBJECTLIT;
    localSets.globalSets = 1;
    localSets.localSets = 1;
    assertFalse(localSets.canCollapseUnannotatedChildNames());
  }

  @Test(timeout = 4000)
  public void testCanCollapseUnannotatedChildNamesObjectLit() {
    Name n = new Name("a", null, false);
    n.type = Name.Type.OBJECTLIT;
    n.globalSets = 1;
    assertTrue(n.canCollapseUnannotatedChildNames());

    Name alias = new Name("b", null, false);
    alias.type = Name.Type.OBJECTLIT;
    alias.globalSets = 1;
    alias.aliasingGets = 1;
    assertFalse(alias.canCollapseUnannotatedChildNames());

    Name parent = new Name("p", null, false);
    parent.type = Name.Type.OTHER;
    parent.globalSets = 1;
    Name child = parent.addProperty("c", false);
    child.type = Name.Type.OBJECTLIT;
    child.globalSets = 1;
    assertFalse(child.canCollapseUnannotatedChildNames());
  }

  @Test(timeout = 4000)
  public void testFunctionTwinReferenceCancelsChildCollapsing() {
    Name n = new Name("a", null, false);
    n.type = Name.Type.FUNCTION;
    Ref set = ref(Ref.Type.SET_FROM_GLOBAL);
    Ref alias = ref(Ref.Type.ALIASING_GET);
    n.addRef(set);
    n.addRef(alias);
    assertEquals(1, n.globalSets);
    assertEquals(1, n.aliasingGets);
    assertFalse(
        "A twin reference should prevent child collapsing even for functions",
        n.canCollapseUnannotatedChildNames());
  }

  @Test(timeout = 4000)
  public void testClassOrEnumBypassesAliasingRestriction() {
    Name n = new Name("a", null, false);
    n.type = Name.Type.OBJECTLIT;
    n.addRef(ref(Ref.Type.SET_FROM_GLOBAL));
    n.addRef(ref(Ref.Type.ALIASING_GET));
    n.setIsClassOrEnum();
    assertTrue(n.canCollapseUnannotatedChildNames());
  }

  @Test(timeout = 4000)
  public void testCanCollapse() {
    Name extern = new Name("a", null, true);
    extern.globalSets = 1;
    assertFalse(extern.canCollapse());

    Name noSet = new Name("b", null, false);
    assertFalse(noSet.canCollapse());

    Name root = new Name("c", null, false);
    root.globalSets = 1;
    assertTrue(root.canCollapse());

    Name parent = new Name("p", null, false);
    parent.type = Name.Type.OTHER;
    parent.globalSets = 1;
    Name child = parent.addProperty("c", false);
    child.globalSets = 1;
    assertFalse(child.canCollapse());

    Name okParent = new Name("p", null, false);
    okParent.type = Name.Type.OBJECTLIT;
    okParent.globalSets = 1;
    Name okChild = okParent.addProperty("c", false);
    okChild.globalSets = 1;
    assertTrue(okChild.canCollapse());
  }

  @Test(timeout = 4000)
  public void testCanEliminate() {
    Name n = new Name("a", null, false);
    n.type = Name.Type.OBJECTLIT;
    n.globalSets = 1;
    assertTrue(n.canEliminate());

    n.addRef(ref(Ref.Type.DIRECT_GET));
    assertFalse(n.canEliminate());

    Name parent = new Name("p", null, false);
    parent.type = Name.Type.OBJECTLIT;
    parent.globalSets = 1;
    Name child = parent.addProperty("c", false);
    child.type = Name.Type.OBJECTLIT;
    child.globalSets = 1;
    child.inExterns = true;
    assertFalse(parent.canEliminate());
  }

  @Test(timeout = 4000)
  public void testNeedsToBeStubbed() {
    Name a = new Name("a", null, false);
    assertFalse(a.needsToBeStubbed());
    a.localSets = 1;
    assertTrue(a.needsToBeStubbed());
    a.globalSets = 1;
    assertFalse(a.needsToBeStubbed());
  }

  @Test(timeout = 4000)
  public void testSetIsClassOrEnumPropagatesNamespace() {
    Name root = new Name("a", null, false);
    root.type = Name.Type.OBJECTLIT;
    Name child = root.addProperty("b", false);
    child.type = Name.Type.OBJECTLIT;
    Name grand = child.addProperty("c", false);
    grand.setIsClassOrEnum();
    assertTrue(root.isNamespace());
    assertTrue(child.isNamespace());
    assertFalse(grand.isNamespace());
    assertTrue(root.isSimpleName());
    assertFalse(child.isSimpleName());
    assertEquals("a.b.c", grand.fullName());
    assertTrue(grand.toString().contains("globalSets=0"));
  }

  @Test(timeout = 4000)
  public void testRefPropertiesAndMarkTwins() {
    Ref set = ref(Ref.Type.SET_FROM_GLOBAL);
    Ref direct = ref(Ref.Type.DIRECT_GET);
    assertTrue(set.isSet());
    assertFalse(direct.isSet());
    assertNull(set.getTwin());

    Ref clone = set.cloneAndReclassify(Ref.Type.ALIASING_GET);
    assertEquals(Ref.Type.ALIASING_GET, clone.type);
    assertFalse(clone.isSet());
    assertEquals("source", clone.sourceName);

    Ref alias = ref(Ref.Type.ALIASING_GET);
    Ref local = ref(Ref.Type.SET_FROM_LOCAL);
    Ref.markTwins(alias, local);
    assertSame(local, alias.getTwin());
    assertSame(alias, local.getTwin());
  }

  @Test(timeout = 4000, expected = IllegalArgumentException.class)
  public void testMarkTwinsRejectsInvalidTypes() {
    Ref a = ref(Ref.Type.ALIASING_GET);
    Ref b = ref(Ref.Type.DIRECT_GET);
    Ref.markTwins(a, b);
  }

  @Test(timeout = 4000)
  public void testGetValueType() throws Exception {
    Object builder = builder();
    assertEquals(Name.Type.OTHER, getValueType(builder, Node.newNumber(0)));
    assertEquals(Name.Type.OBJECTLIT, getValueType(builder, new Node(Token.OBJECTLIT)));
    assertEquals(Name.Type.FUNCTION, getValueType(builder, new Node(Token.FUNCTION)));

    Node or = new Node(Token.OR, Node.newNumber(0), new Node(Token.OBJECTLIT));
    assertEquals(Name.Type.OBJECTLIT, getValueType(builder, or));

    Node hook1 =
        new Node(
            Token.HOOK,
            Node.newNumber(1),
            new Node(Token.OBJECTLIT),
            Node.newNumber(2));
    assertEquals(Name.Type.OBJECTLIT, getValueType(builder, hook1));

    Node hook2 =
        new Node(
            Token.HOOK,
            Node.newNumber(1),
            Node.newNumber(2),
            new Node(Token.FUNCTION));
    assertEquals(Name.Type.FUNCTION, getValueType(builder, hook2));

    Node hook3 =
        new Node(
            Token.HOOK,
            Node.newNumber(1),
            Node.newNumber(2),
            Node.newNumber(3));
    assertEquals(Name.Type.OTHER, getValueType(builder, hook3));
  }

  @Test(timeout = 4000)
  public void testGetNameForObjLitKey_SimpleVarAndInvalid() throws Exception {
    Object builder = builder();

    Node key = Node.newString("x");
    Node value = Node.newNumber(0);
    Node obj = new Node(Token.OBJECTLIT, key, value);
    Node name = Node.newString(Token.NAME, "w");
    name.addChildToBack(obj);
    Node var = new Node(Token.VAR, name);
    assertEquals("w.x", getNameForObjLitKey(builder, key));

    Node badKey = Node.newString("not-an-ident");
    Node badObj = new Node(Token.OBJECTLIT, badKey, Node.newNumber(0));
    Node badName = Node.newString(Token.NAME, "w");
    badName.addChildToBack(badObj);
    Node badVar = new Node(Token.VAR, badName);
    assertNull(getNameForObjLitKey(builder, badKey));

    Node normalKey = Node.newString("x");
    Node normalVal = Node.newNumber(1);
    Node normalObj = new Node(Token.OBJECTLIT, normalKey, normalVal);
    assertNull(getNameForObjLitKey(builder, normalVal));
  }

  @Test(timeout = 4000)
  public void testGetNameForObjLitKey_NestedObjectLit() throws Exception {
    Object builder = builder();

    Node zKey = Node.newString("z");
    Node zVal = Node.newNumber(0);
    Node zObj = new Node(Token.OBJECTLIT, zKey, zVal);

    Node yKey = Node.newString("y");
    Node yObj = new Node(Token.OBJECTLIT, yKey, zObj);

    Node xKey = Node.newString("x");
    Node xObj = new Node(Token.OBJECTLIT, xKey, yObj);

    Node name = Node.newString(Token.NAME, "w");
    name.addChildToBack(xObj);
    Node var = new Node(Token.VAR, name);

    assertEquals("w.x.y.z", getNameForObjLitKey(builder, zKey));
  }

  @Test(timeout = 4000)
  public void testGetNameForObjLitKey_Assignment() throws Exception {
    Object builder = builder();

    Node w = Node.newString(Token.NAME, "w");
    Node x = Node.newString("x");
    Node getprop = new Node(Token.GETPROP, w, x);

    Node zKey = Node.newString("z");
    Node zVal = Node.newNumber(0);
    Node zObj = new Node(Token.OBJECTLIT, zKey, zVal);

    Node yKey = Node.newString("y");
    Node yObj = new Node(Token.OBJECTLIT, yKey, zObj);

    Node assign = new Node(Token.ASSIGN, getprop, yObj);
    Node expr = new Node(Token.EXPR_RESULT, assign);

    assertEquals("w.x.y.z", getNameForObjLitKey(builder, zKey));
  }

  @Test(timeout = 4000)
  public void testIsNestedAssign() throws Exception {
    Object builder = builder();

    Node lvalue = Node.newString(Token.NAME, "a");
    Node rvalue = Node.newNumber(1);
    Node assign = new Node(Token.ASSIGN, lvalue, rvalue);
    Node exprResult = new Node(Token.EXPR_RESULT, assign);
    assertFalse(isNestedAssign(builder, assign));

    Node lvalue2 = Node.newString(Token.NAME, "a");
    Node assign2 = new Node(Token.ASSIGN, lvalue2, Node.newNumber(1));
    Node varName = Node.newString(Token.NAME, "x");
    varName.addChildToBack(assign2);
    Node var = new Node(Token.VAR, varName);
    assertTrue(isNestedAssign(builder, assign2));
  }

  @Test(timeout = 4000)
  public void testIsNestedAssignCommaDoesNotTreatDiscardedAssignmentAsNested()
      throws Exception {
    Object builder = builder();

    Node lvalue = Node.newString(Token.NAME, "a");
    Node rvalue = Node.newNumber(1);
    Node assign = new Node(Token.ASSIGN, lvalue, rvalue);
    Node comma = new Node(Token.COMMA, assign, Node.newNumber(2));
    Node exprResult = new Node(Token.EXPR_RESULT, comma);

    assertFalse(isNestedAssign(builder, assign));
  }

  @Test(timeout = 4000)
  public void testDetermineGetTypeForHookOrBooleanExpr() throws Exception {
    Object builder = builder();

    Node or = newOrNode();
    Node varName = Node.newString(Token.NAME, "a");
    varName.addChildToBack(or);
    Node var = new Node(Token.VAR, varName);
    assertEquals(Ref.Type.DIRECT_GET, determineGetType(builder, or, "a"));

    Node or2 = newOrNode();
    Node varName2 = Node.newString(Token.NAME, "b");
    varName2.addChildToBack(or2);
    Node var2 = new Node(Token.VAR, varName2);
    assertEquals(Ref.Type.ALIASING_GET, determineGetType(builder, or2, "a"));

    Node or3 = newOrNode();
    Node expr = new Node(Token.EXPR_RESULT, or3);
    assertEquals(Ref.Type.DIRECT_GET, determineGetType(builder, or3, "a"));

    Node or4 = newOrNode();
    Node lvalue4 = Node.newString(Token.NAME, "a");
    Node assign4 = new Node(Token.ASSIGN, lvalue4, or4);
    Node expr4 = new Node(Token.EXPR_RESULT, assign4);
    assertEquals(Ref.Type.DIRECT_GET, determineGetType(builder, or4, "a"));

    Node or5 = newOrNode();
    Node lvalue5 = Node.newString(Token.NAME, "b");
    Node assign5 = new Node(Token.ASSIGN, lvalue5, or5);
    Node expr5 = new Node(Token.EXPR_RESULT, assign5);
    assertEquals(Ref.Type.ALIASING_GET, determineGetType(builder, or5, "a"));

    Node or6 = newOrNode();
    Node foo = Node.newString(Token.NAME, "foo");
    Node call = new Node(Token.CALL, foo, or6);
    Node expr6 = new Node(Token.EXPR_RESULT, call);
    assertEquals(Ref.Type.ALIASING_GET, determineGetType(builder, or6, "a"));

    Node or7 = newOrNode();
    Node hook = new Node(Token.HOOK, or7, Node.newNumber(1), Node.newNumber(2));
    Node expr7 = new Node(Token.EXPR_RESULT, hook);
    assertEquals(Ref.Type.DIRECT_GET, determineGetType(builder, or7, "a"));

    Node orphan = newOrNode();
    assertEquals(Ref.Type.ALIASING_GET, determineGetType(builder, orphan, "a"));
  }

  @Test(timeout = 4000)
  public void testGetOrCreateName() throws Exception {
    GlobalNamespace ns = new GlobalNamespace(null, null, null);
    Object builder = newBuildGlobalNamespace(ns);

    Name a = (Name) call(builder, "getOrCreateName", new Class<?>[] {String.class}, "a");
    assertNotNull(a);
    assertNull(a.parent);
    assertFalse(a.inExterns);
    assertEquals("a", a.name);

    Name ab = (Name) call(builder, "getOrCreateName", new Class<?>[] {String.class}, "a.b");
    assertSame(a, ab.parent);
    assertEquals("b", ab.name);
    assertNotNull(a.props);
    assertTrue(a.props.contains(ab));

    Name abc = (Name) call(builder, "getOrCreateName", new Class<?>[] {String.class}, "a.b.c");
    assertSame(ab, abc.parent);
    assertEquals("c", abc.name);

    Name abAgain = (Name) call(builder, "getOrCreateName", new Class<?>[] {String.class}, "a.b");
    assertSame(ab, abAgain);
  }
}