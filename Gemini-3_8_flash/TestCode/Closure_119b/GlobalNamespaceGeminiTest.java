/*
 * [Branch & Defect Analysis Matrix]
 * Targets: com.google.javascript.jscomp.GlobalNamespace
 *
 * Partition A: Core Functional Logic & State Transitions
 * - AST traversal of global declarations: VAR, ASSIGN, FUNCTION, GETPROP, OBJECTLIT.
 * - Object literal keys: STRING_KEY, GETTER_DEF, SETTER_DEF, nested object literal keys.
 * - Reference classification: SET_FROM_GLOBAL, SET_FROM_LOCAL, PROTOTYPE_GET, ALIASING_GET,
 *   DIRECT_GET, CALL_GET, DELETE_PROP.
 * - Boolean/Hook expressions in handleGet: OR, AND, HOOK, ASSIGN, NAME, CALL, IF, WHILE, FOR.
 * - Prototype prefix stripping: Foo.prototype, Foo.prototype.bar, Foo.prototype.bar.baz.
 *
 * Partition B: Boundary Value Analysis (BVA) & Extremes
 * - Empty AST, null externs, non-null externsRoot.
 * - Non-identifier keys in object literals (TokenStream.isJSIdentifier false branch).
 * - Name tree traversal, getBaseName, getFullName, isSimpleName.
 * - Nested assignment twins (var a = b = 0) and twin detection in canCollapseUnannotatedChildNames.
 *
 * Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
 * - Known Defect: CheckGlobalNamesTest::testGlobalCatch
 *   "Unexpected warning(s): JSC_UNDEFINED_NAME. e is never defined at testcode line 1 : 48"
 *   Underlying cause: Variables declared in catch clauses (e.g., `catch (e) { foo(e); }`) were
 *   incorrectly indexed as global uninitialized names.
 * - Targeted assertion: Verifies that catch variables are NOT registered as global names.
 *
 * Partition D: Exception & Defensive Guard Paths
 * - Ref.markTwins() argument validation: fails when neither is ALIASING_GET or neither is SET.
 * - Name.canCollapseUnannotatedChildNames() declaration null-check precondition.
 * - AstChange node filtering in scanNewNodes.
 *
 * Partition E: Object Lifecycle & Symbol Table Contracts
 * - StaticScope and StaticSymbolTable implementations: getTypeOfThis, getRootNode, getScope,
 *   getReferences, getAllSymbols, getSlot, getOwnSlot.
 * - Tracker compiler pass functionality: logging added and removed symbols across phases.
 */

package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.common.base.Predicate;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GlobalNamespaceGeminiTest {

  private GlobalNamespace build(String js) {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode(js);
    return new GlobalNamespace(compiler, root);
  }

  private GlobalNamespace buildWithExterns(String externs, String js) {
    Compiler compiler = new Compiler();
    Node externsRoot = compiler.parseSyntheticCode("externs", externs);
    Node root = compiler.parseSyntheticCode("testcode", js);
    return new GlobalNamespace(compiler, externsRoot, root);
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testBasicGlobalVariablesAndProperties() {
    String js = "var a = {};\n" +
                "a.b = function() {};\n" +
                "a.b.c = 123;\n" +
                "var d = a.b.c;\n";
    GlobalNamespace gn = build(js);

    GlobalNamespace.Name nameA = gn.getOwnSlot("a");
    assertNotNull(nameA);
    assertEquals("a", nameA.getBaseName());
    assertEquals("a", nameA.getFullName());
    assertEquals(GlobalNamespace.Name.Type.OBJECTLIT, nameA.type);
    assertEquals(1, nameA.globalSets);
    assertTrue(nameA.isSimpleName());

    GlobalNamespace.Name nameB = gn.getOwnSlot("a.b");
    assertNotNull(nameB);
    assertEquals("b", nameB.getBaseName());
    assertEquals("a.b", nameB.getFullName());
    assertEquals(GlobalNamespace.Name.Type.FUNCTION, nameB.type);
    assertFalse(nameB.isSimpleName());

    GlobalNamespace.Name nameC = gn.getOwnSlot("a.b.c");
    assertNotNull(nameC);
    assertEquals("c", nameC.getBaseName());
    assertEquals(GlobalNamespace.Name.Type.OTHER, nameC.type);
    assertEquals(1, nameC.globalSets);
    assertEquals(1, nameC.totalGets);
  }

  @Test(timeout = 4000)
  public void testObjectLiteralsAndNestedKeys() {
    String js = "var root = {\n" +
                "  first: 1,\n" +
                "  nested: {\n" +
                "    inner: 2\n" +
                "  },\n" +
                "  get prop() { return 10; },\n" +
                "  set prop(val) {}\n" +
                "};";
    GlobalNamespace gn = build(js);

    GlobalNamespace.Name rootName = gn.getOwnSlot("root");
    assertNotNull(rootName);
    assertEquals(GlobalNamespace.Name.Type.OBJECTLIT, rootName.type);

    GlobalNamespace.Name first = gn.getOwnSlot("root.first");
    assertNotNull(first);
    assertEquals(GlobalNamespace.Name.Type.OTHER, first.type);

    GlobalNamespace.Name nested = gn.getOwnSlot("root.nested");
    assertNotNull(nested);
    assertEquals(GlobalNamespace.Name.Type.OBJECTLIT, nested.type);

    GlobalNamespace.Name inner = gn.getOwnSlot("root.nested.inner");
    assertNotNull(inner);
    assertEquals(GlobalNamespace.Name.Type.OTHER, inner.type);

    GlobalNamespace.Name prop = gn.getOwnSlot("root.prop");
    assertNotNull(prop);
    assertTrue(prop.isGetOrSetDefinition());
  }

  @Test(timeout = 4000)
  public void testVariousReferenceUsages() {
    String js = "var x = 1;\n" +
                "var y = x;\n" +             // ALIASING_GET
                "if (x) {}\n" +              // DIRECT_GET
                "x++;\n" +                   // OTHER set
                "--x;\n" +                   // OTHER set
                "x += 2;\n" +                // OTHER set
                "function f() { x = 2; }\n"; // SET_FROM_LOCAL
    GlobalNamespace gn = build(js);

    GlobalNamespace.Name x = gn.getOwnSlot("x");
    assertNotNull(x);
    assertEquals(4, x.globalSets);
    assertEquals(1, x.localSets);
    assertTrue(x.aliasingGets > 0);
    assertTrue(x.totalGets > 0);
  }

  @Test(timeout = 4000)
  public void testCallGetsAndClassDefiningCalls() {
    String js = "var fn = function() {};\n" +
                "fn();\n" +                  // CALL_GET
                "var obj = new fn();\n" +    // DIRECT_GET
                "var arg = new fn(fn);\n";   // ALIASING_GET
    GlobalNamespace gn = build(js);

    GlobalNamespace.Name fn = gn.getOwnSlot("fn");
    assertNotNull(fn);
    assertEquals(1, fn.callGets);
    assertTrue(fn.totalGets >= 3);
  }

  @Test(timeout = 4000)
  public void testPrototypePrefixHandling() {
    String js = "function MyClass() {}\n" +
                "MyClass.prototype.method = function() {};\n" +
                "MyClass.prototype.sub.prop = 42;\n";
    GlobalNamespace gn = build(js);

    GlobalNamespace.Name myClass = gn.getOwnSlot("MyClass");
    assertNotNull(myClass);
    // Writes to MyClass.prototype.* register PROTOTYPE_GET references on MyClass
    assertTrue(myClass.totalGets > 0);
  }

  @Test(timeout = 4000)
  public void testHookAndBooleanExpressions() {
    String js = "var a = a || {};\n" +
                "var b = b ? b : {};\n" +
                "var c = 1;\n" +
                "if (c || 0) {}\n" +
                "while (c && 1) {}\n" +
                "for (; !c ;) {}\n" +
                "typeof c;\n" +
                "void c;\n" +
                "+c;\n" +
                "-c;\n" +
                "~c;\n" +
                "c instanceof Object;\n" +
                "(c || 0) ? 1 : 2;\n" +
                "delete c.prop;\n";
    GlobalNamespace gn = build(js);

    GlobalNamespace.Name nameA = gn.getOwnSlot("a");
    assertNotNull(nameA);

    GlobalNamespace.Name nameC = gn.getOwnSlot("c");
    assertNotNull(nameC);
    assertTrue(nameC.totalGets > 0);

    GlobalNamespace.Name nameProp = gn.getOwnSlot("c.prop");
    assertNotNull(nameProp);
    assertEquals(1, nameProp.deleteProps);
  }

  @Test(timeout = 4000)
  public void testTypeDeclarationsJSDoc() {
    String js = "/** @constructor */ function Ctor() {}\n" +
                "/** @interface */ function Intf() {}\n" +
                "/** @enum {number} */ var EnumObj = { VAL: 1 };\n";
    GlobalNamespace gn = build(js);

    GlobalNamespace.Name ctor = gn.getOwnSlot("Ctor");
    assertNotNull(ctor);
    assertTrue(ctor.isDeclaredType());

    GlobalNamespace.Name intf = gn.getOwnSlot("Intf");
    assertNotNull(intf);
    assertTrue(intf.isDeclaredType());

    GlobalNamespace.Name enumObj = gn.getOwnSlot("EnumObj");
    assertNotNull(enumObj);
    assertTrue(enumObj.isDeclaredType());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testEmptyCodeAndForest() {
    GlobalNamespace gn = build("");
    assertNotNull(gn.getNameForest());
    assertTrue(gn.getNameForest().isEmpty());
    assertNotNull(gn.getNameIndex());
    assertTrue(gn.getNameIndex().isEmpty());
    assertFalse(gn.hasExternsRoot());
    assertNull(gn.getParentScope());
  }

  @Test(timeout = 4000)
  public void testExternsBoundary() {
    String externs = "var extObj = {};";
    String js = "extObj.prop = 1;";
    GlobalNamespace gn = buildWithExterns(externs, js);

    assertTrue(gn.hasExternsRoot());
    GlobalNamespace.Name extObj = gn.getOwnSlot("extObj");
    assertNotNull(extObj);
    assertTrue(extObj.inExterns);
    assertFalse(extObj.canCollapse());
  }

  @Test(timeout = 4000)
  public void testInvalidJsIdentifierObjectLitKey() {
    String js = "var obj = { 'invalid-identifier': 1, '123': 2 };";
    GlobalNamespace gn = build(js);

    GlobalNamespace.Name obj = gn.getOwnSlot("obj");
    assertNotNull(obj);
    assertNull(gn.getOwnSlot("obj.invalid-identifier"));
    assertNull(gn.getOwnSlot("obj.123"));
  }

  @Test(timeout = 4000)
  public void testNameStubDeclarationAndNeedsToBeStubbed() {
    String js = "var ns = {};\n" +
                "ns.stub;\n" +
                "function localDef() { uninitGlobal = 42; }\n";
    GlobalNamespace gn = build(js);

    GlobalNamespace.Name stub = gn.getOwnSlot("ns.stub");
    assertNotNull(stub);
    assertTrue(stub.isSimpleStubDeclaration());

    GlobalNamespace.Name uninitGlobal = gn.getOwnSlot("uninitGlobal");
    if (uninitGlobal != null) {
      assertTrue(uninitGlobal.needsToBeStubbed());
    }
  }

  @Test(timeout = 4000)
  public void testNameEliminationAndCollapsing() {
    GlobalNamespace.Name root = new GlobalNamespace.Name("myRoot", null, false);
    root.type = GlobalNamespace.Name.Type.OBJECTLIT;
    GlobalNamespace.Ref decl = GlobalNamespace.Ref.createRefForTesting(
        GlobalNamespace.Ref.Type.SET_FROM_GLOBAL);
    root.addRef(decl);

    assertTrue(root.canCollapseUnannotatedChildNames());
    assertTrue(root.canEliminate());

    GlobalNamespace.Name child = root.addProperty("child", false);
    child.type = GlobalNamespace.Name.Type.OTHER;
    child.addRef(GlobalNamespace.Ref.createRefForTesting(
        GlobalNamespace.Ref.Type.SET_FROM_GLOBAL));

    assertTrue(child.canCollapse());
    assertTrue(root.canEliminate());

    root.addRef(GlobalNamespace.Ref.createRefForTesting(
        GlobalNamespace.Ref.Type.DIRECT_GET));
    assertFalse(root.canEliminate());
  }

  @Test(timeout = 4000)
  public void testRefRemovalLifecycle() {
    GlobalNamespace.Name name = new GlobalNamespace.Name("testRef", null, false);
    GlobalNamespace.Ref r1 = GlobalNamespace.Ref.createRefForTesting(
        GlobalNamespace.Ref.Type.SET_FROM_GLOBAL);
    GlobalNamespace.Ref r2 = GlobalNamespace.Ref.createRefForTesting(
        GlobalNamespace.Ref.Type.SET_FROM_GLOBAL);
    GlobalNamespace.Ref get = GlobalNamespace.Ref.createRefForTesting(
        GlobalNamespace.Ref.Type.DIRECT_GET);

    name.addRef(r1);
    assertEquals(r1, name.getDeclaration());
    assertEquals(1, name.globalSets);

    name.addRef(r2);
    assertEquals(r1, name.getDeclaration());
    assertEquals(2, name.globalSets);

    name.addRef(get);
    assertEquals(1, name.totalGets);

    name.removeRef(r1);
    assertEquals(r2, name.getDeclaration());
    assertEquals(1, name.globalSets);

    name.removeRef(r2);
    assertNull(name.getDeclaration());
    assertEquals(0, name.globalSets);

    name.removeRef(get);
    assertEquals(0, name.totalGets);
  }

  @Test(timeout = 4000)
  public void testRefCloningAndMetadata() {
    GlobalNamespace.Ref r = GlobalNamespace.Ref.createRefForTesting(
        GlobalNamespace.Ref.Type.DIRECT_GET);
    assertFalse(r.isSet());
    assertEquals("", r.getSourceName());
    assertNull(r.getNode());
    assertNull(r.getModule());
    assertNull(r.getSourceFile());
    assertNull(r.getSymbol());

    GlobalNamespace.Ref clone = r.cloneAndReclassify(
        GlobalNamespace.Ref.Type.SET_FROM_GLOBAL);
    assertEquals(GlobalNamespace.Ref.Type.SET_FROM_GLOBAL, clone.type);
    assertTrue(clone.isSet());
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // =========================================================================

  /**
   * Targets ground truth defect: CheckGlobalNamesTest::testGlobalCatch
   * Exception in defective version:
   * "Unexpected warning(s): JSC_UNDEFINED_NAME. e is never defined at testcode line 1 : 48"
   *
   * Catch variables in global try-catch blocks (e.g. `catch (e)`) must NEVER be treated
   * as uninitialized global names in GlobalNamespace.
   */
  @Test(timeout = 4000)
  public void testGlobalCatchDefectExposed() {
    String js = "try { throw 'foo'; } catch (e) { foo(e); }";
    GlobalNamespace gn = build(js);

    GlobalNamespace.Name catchVar = gn.getOwnSlot("e");
    assertNull("Catch block variable 'e' must not be registered as a global namespace name.", catchVar);
  }

  @Test(timeout = 4000)
  public void testGlobalCatchWithAssignment() {
    String js = "try {} catch (err) { err = null; }";
    GlobalNamespace gn = build(js);

    assertNull("Catch parameter 'err' must not be captured in global namespace.", gn.getOwnSlot("err"));
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testMarkTwinsInvalidRefTypes() {
    GlobalNamespace.Ref r1 = GlobalNamespace.Ref.createRefForTesting(
        GlobalNamespace.Ref.Type.DIRECT_GET);
    GlobalNamespace.Ref r2 = GlobalNamespace.Ref.createRefForTesting(
        GlobalNamespace.Ref.Type.CALL_GET);
    GlobalNamespace.Ref.markTwins(r1, r2);
  }

  @Test(timeout = 4000)
  public void testMarkTwinsValid() {
    GlobalNamespace.Ref set = GlobalNamespace.Ref.createRefForTesting(
        GlobalNamespace.Ref.Type.SET_FROM_GLOBAL);
    GlobalNamespace.Ref alias = GlobalNamespace.Ref.createRefForTesting(
        GlobalNamespace.Ref.Type.ALIASING_GET);
    GlobalNamespace.Ref.markTwins(set, alias);

    assertEquals(alias, set.getTwin());
    assertEquals(set, alias.getTwin());
  }

  @Test(expected = NullPointerException.class, timeout = 4000)
  public void testCanCollapseUnannotatedChildNamesDeclarationPrecondition() {
    GlobalNamespace.Name name = new GlobalNamespace.Name("uninitialized", null, false);
    name.type = GlobalNamespace.Name.Type.OBJECTLIT;
    name.globalSets = 1;
    // declaration is intentionally null
    name.canCollapseUnannotatedChildNames();
  }

  @Test(timeout = 4000)
  public void testScanNewNodesNonQualifiedFiltering() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("var existing = 1;");
    GlobalNamespace gn = new GlobalNamespace(compiler, root);

    List<GlobalNamespace.AstChange> changes = new ArrayList<GlobalNamespace.AstChange>();
    // Node that is neither a qualified name nor an object literal key
    Node nonQualNode = new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2));
    changes.add(new GlobalNamespace.AstChange(null, null, nonQualNode));

    gn.scanNewNodes(changes);
    assertNull(gn.getOwnSlot("ADD"));
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Symbol Table Contracts
  // =========================================================================

  @Test(timeout = 4000)
  public void testStaticSymbolTableAndScopeContracts() {
    String js = "var sym = 100;";
    GlobalNamespace gn = build(js);

    GlobalNamespace.Name sym = gn.getSlot("sym");
    assertNotNull(sym);
    assertEquals(sym, gn.getOwnSlot("sym"));
    assertEquals(gn, gn.getScope(sym));

    assertNotNull(gn.getTypeOfThis());
    assertNull(gn.getParentScope());

    Iterable<GlobalNamespace.Ref> refs = gn.getReferences(sym);
    assertNotNull(refs);
    assertTrue(refs.iterator().hasNext());

    Iterable<GlobalNamespace.Name> symbols = gn.getAllSymbols();
    assertNotNull(symbols);
    assertTrue(symbols.iterator().hasNext());

    assertFalse(sym.isTypeInferred());
    assertNull(sym.getType());
    assertNotNull(sym.toString());
  }

  @Test(timeout = 4000)
  public void testNamespaceDescendantLogic() {
    String js = "var NS = {};\n" +
                "/** @constructor */ NS.Widget = function() {};\n";
    GlobalNamespace gn = build(js);

    GlobalNamespace.Name ns = gn.getOwnSlot("NS");
    assertNotNull(ns);
    assertTrue(ns.isNamespace());
    assertFalse(ns.shouldKeepKeys());

    ns.aliasingGets = 1;
    assertTrue(ns.shouldKeepKeys());
  }

  @Test(timeout = 4000)
  public void testTrackerPassOutput() {
    Compiler compiler = new Compiler();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(out);

    Predicate<String> allSymbols = new Predicate<String>() {
      @Override
      public boolean apply(String s) {
        return true;
      }
    };

    GlobalNamespace.Tracker tracker = new GlobalNamespace.Tracker(
        compiler, ps, allSymbols);

    Node root1 = compiler.parseTestCode("var trackedA = 1;");
    tracker.process(null, root1);

    Node root2 = compiler.parseTestCode("var trackedB = 2;");
    tracker.process(null, root2);

    String logs = out.toString();
    assertTrue(logs.contains("Added by"));
    assertTrue(logs.contains("Removed by"));
  }
}