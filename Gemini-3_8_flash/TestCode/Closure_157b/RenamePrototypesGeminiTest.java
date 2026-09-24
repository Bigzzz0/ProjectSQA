package com.google.javascript.jscomp;

import com.google.javascript.jscomp.AbstractCompiler.LifeCycleStage;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: RenamePrototypes
 *
 * 1. Normalized Lifecycle Check:
 *    - Branch: Preconditions.checkState(compiler.getLifeCycleStage().isNormalized())
 *    - Tested: Raw compiler state (throws IllegalStateException), properly set NORMALIZED state.
 *
 * 2. AST Traversal Paths:
 *    - ProcessExternedProperties:
 *      * GETPROP / GETELEM with STRING child -> marks old names as reserved.
 *      * Non-string child in GETPROP/GETELEM -> no reservation.
 *    - ProcessProperties:
 *      * GETPROP / GETELEM with STRING child:
 *        - "prototype" -> triggers processPrototypeParent:
 *          * parent GETPROP / GETELEM -> markPrototypePropertyCandidate.
 *          * parent ASSIGN (RHS is OBJECTLIT) -> markPrototypePropertyCandidate for keys, adds to prototypeObjLits.
 *          * parent CALL (last child is OBJECTLIT) -> markPrototypePropertyCandidate for keys, adds to prototypeObjLits.
 *        - non-"prototype" -> markPropertyAccessCandidate.
 *      * OBJECTLIT:
 *        - If already processed in prototypeObjLits -> skip.
 *        - If standalone OBJECTLIT -> iterate children, if child is not NUMBER -> markObjLitPropertyCandidate.
 *
 * 3. Property Classification & canRename Decisions:
 *    - prototypeCount > 0 && objLitCount == 0 -> canRenamePrototypeProperty
 *    - objLitCount > 0 && prototypeCount == 0 -> canRenameObjLitProperty
 *    - Both > 0 or both == 0 -> canRenamePrototypeProperty() && canRenameObjLitProperty()
 *    - canRenamePrototypeProperty:
 *      * isExported -> false
 *      * isPrivate -> true
 *      * aggressiveRenaming == true -> true
 *      * non-aggressive -> true if contains uppercase or non-letter, false if all lowercase letters.
 *    - canRenameObjLitProperty:
 *      * isExported -> false
 *      * isPrivate -> true
 *      * otherwise -> false
 *
 * 4. Frequency Ordering & Name Generation:
 *    - FREQUENCY_COMPARATOR: higher count first; if counts equal, alphabetical tie-break.
 *    - NameGenerator: reservedNames (indexOf, lastIndexOf, toString, valueOf + externs) avoided.
 *    - reservedCharacters: ensures generated names avoid these characters.
 *
 * 5. Reusing Previous Rename Maps:
 *    - prevUsedRenameMap with match not in reservedNames -> reused.
 *    - prevUsedRenameMap with match in reservedNames -> skipped, newly generated.
 *
 * 6. Defect Target:
 *    - Proper handling of prototype assignments with object literals containing numeric/getter/setter/string keys,
 *      ensuring no crash and proper preservation or renaming according to rules.
 */
public class RenamePrototypesGeminiTest {

  private Compiler compiler;
  private Node externsRoot;
  private Node mainRoot;

  @Before
  public void setUp() {
    compiler = new Compiler();
    compiler.initCompilerOptionsIfTesting();
    compiler.setLifeCycleStage(LifeCycleStage.NORMALIZED);
    externsRoot = new Node(Token.BLOCK);
    mainRoot = new Node(Token.BLOCK);
  }

  // =================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =================================================================

  @Test(timeout = 4000)
  public void testPrototypePropertyRenamedAggressive() {
    // Foo.prototype.myMethod = function() {};
    Node getPropFooProto = Node.newString(Token.GETPROP, "prototype");
    getPropFooProto.addChildToBack(Node.newString(Token.NAME, "Foo"));
    getPropFooProto.addChildToBack(Node.newString(Token.STRING, "prototype"));

    Node getPropMethod = Node.newString(Token.GETPROP, "myMethod");
    getPropMethod.addChildToBack(getPropFooProto);
    Node methodName = Node.newString(Token.STRING, "myMethod");
    getPropMethod.addChildToBack(methodName);

    Node assign = new Node(Token.ASSIGN, getPropMethod, new Node(Token.FUNCTION));
    Node expr = new Node(Token.EXPR_RESULT, assign);
    mainRoot.addChildToBack(expr);

    RenamePrototypes pass = new RenamePrototypes(compiler, true, null, null);
    pass.process(externsRoot, mainRoot);

    assertEquals(LifeCycleStage.NORMALIZED_OBFUSCATED, compiler.getLifeCycleStage());
    VariableMap map = pass.getPropertyMap();
    assertTrue(map.hasOriginal("myMethod"));
    String newName = map.lookupNewName("myMethod");
    assertNotNull(newName);
    assertEquals(newName, methodName.getString());
  }

  @Test(timeout = 4000)
  public void testPrototypePropertyNonAggressiveRenaming() {
    // In non-aggressive mode:
    // "alllowercase" has no upper-case and no non-letter -> should NOT be renamed.
    // "hasUpperCase" has upper-case -> should BE renamed.
    // "has_underscore" has non-letter -> should BE renamed.

    Node getPropProto = Node.newString(Token.GETPROP, "prototype");
    getPropProto.addChildToBack(Node.newString(Token.NAME, "Bar"));
    getPropProto.addChildToBack(Node.newString(Token.STRING, "prototype"));

    Node method1 = Node.newString(Token.STRING, "alllowercase");
    Node getProp1 = new Node(Token.GETPROP, getPropProto.cloneTree(), method1);

    Node method2 = Node.newString(Token.STRING, "hasUpperCase");
    Node getProp2 = new Node(Token.GETPROP, getPropProto.cloneTree(), method2);

    Node method3 = Node.newString(Token.STRING, "has_underscore");
    Node getProp3 = new Node(Token.GETPROP, getPropProto.cloneTree(), method3);

    mainRoot.addChildToBack(new Node(Token.EXPR_RESULT, getProp1));
    mainRoot.addChildToBack(new Node(Token.EXPR_RESULT, getProp2));
    mainRoot.addChildToBack(new Node(Token.EXPR_RESULT, getProp3));

    RenamePrototypes pass = new RenamePrototypes(compiler, false, null, null);
    pass.process(externsRoot, mainRoot);

    VariableMap map = pass.getPropertyMap();
    assertFalse("alllowercase should not be renamed in non-aggressive mode",
        map.hasOriginal("alllowercase"));
    assertTrue("hasUpperCase should be renamed",
        map.hasOriginal("hasUpperCase"));
    assertTrue("has_underscore should be renamed",
        map.hasOriginal("has_underscore"));
  }

  @Test(timeout = 4000)
  public void testFrequencyComparatorTieBreakingAndFrequency() {
    // freqProp appears 2 times, tieA appears 1 time, tieB appears 1 time.
    // All should be renamed in aggressive mode.
    Node getPropProto = Node.newString(Token.GETPROP, "prototype");
    getPropProto.addChildToBack(Node.newString(Token.NAME, "Test"));
    getPropProto.addChildToBack(Node.newString(Token.STRING, "prototype"));

    Node pFreq1 = Node.newString(Token.STRING, "freqProp");
    Node pFreq2 = Node.newString(Token.STRING, "freqProp");
    Node pTieA = Node.newString(Token.STRING, "tieA");
    Node pTieB = Node.newString(Token.STRING, "tieB");

    mainRoot.addChildToBack(new Node(Token.EXPR_RESULT, new Node(Token.GETPROP, getPropProto.cloneTree(), pFreq1)));
    mainRoot.addChildToBack(new Node(Token.EXPR_RESULT, new Node(Token.GETPROP, getPropProto.cloneTree(), pFreq2)));
    mainRoot.addChildToBack(new Node(Token.EXPR_RESULT, new Node(Token.GETPROP, getPropProto.cloneTree(), pTieA)));
    mainRoot.addChildToBack(new Node(Token.EXPR_RESULT, new Node(Token.GETPROP, getPropProto.cloneTree(), pTieB)));

    RenamePrototypes pass = new RenamePrototypes(compiler, true, null, null);
    pass.process(externsRoot, mainRoot);

    VariableMap map = pass.getPropertyMap();
    String newFreq = map.lookupNewName("freqProp");
    String newTieA = map.lookupNewName("tieA");
    String newTieB = map.lookupNewName("tieB");

    // Most frequent property gets the first generated name ("a")
    assertEquals("a", newFreq);
    // tieA precedes tieB alphabetically, so tieA gets "b", tieB gets "c"
    assertEquals("b", newTieA);
    assertEquals("c", newTieB);
  }

  // =================================================================
  // Partition B: Boundary Value Analysis & Reserved Names
  // =================================================================

  @Test(timeout = 4000)
  public void testBuiltinReservedNamesAreNeverRenamed() {
    // toString and valueOf are built-in reserved names
    Node getPropProto = Node.newString(Token.GETPROP, "prototype");
    getPropProto.addChildToBack(Node.newString(Token.NAME, "Foo"));
    getPropProto.addChildToBack(Node.newString(Token.STRING, "prototype"));

    Node tsNode = Node.newString(Token.STRING, "toString");
    Node vOfNode = Node.newString(Token.STRING, "valueOf");
    Node customNode = Node.newString(Token.STRING, "customProp");

    mainRoot.addChildToBack(new Node(Token.EXPR_RESULT, new Node(Token.GETPROP, getPropProto.cloneTree(), tsNode)));
    mainRoot.addChildToBack(new Node(Token.EXPR_RESULT, new Node(Token.GETPROP, getPropProto.cloneTree(), vOfNode)));
    mainRoot.addChildToBack(new Node(Token.EXPR_RESULT, new Node(Token.GETPROP, getPropProto.cloneTree(), customNode)));

    RenamePrototypes pass = new RenamePrototypes(compiler, true, null, null);
    pass.process(externsRoot, mainRoot);

    VariableMap map = pass.getPropertyMap();
    assertFalse("toString must be reserved", map.hasOriginal("toString"));
    assertFalse("valueOf must be reserved", map.hasOriginal("valueOf"));
    assertTrue("customProp should be renamed", map.hasOriginal("customProp"));
    assertEquals("toString", tsNode.getString());
    assertEquals("valueOf", vOfNode.getString());
  }

  @Test(timeout = 4000)
  public void testExternsPropertiesAreReserved() {
    // extern: window.externProp
    Node externGetProp = Node.newString(Token.GETPROP, "externProp");
    externGetProp.addChildToBack(Node.newString(Token.NAME, "window"));
    externGetProp.addChildToBack(Node.newString(Token.STRING, "externProp"));
    externsRoot.addChildToBack(new Node(Token.EXPR_RESULT, externGetProp));

    // code: Foo.prototype.externProp
    Node protoGetProp = Node.newString(Token.GETPROP, "prototype");
    protoGetProp.addChildToBack(Node.newString(Token.NAME, "Foo"));
    protoGetProp.addChildToBack(Node.newString(Token.STRING, "prototype"));

    Node codePropNode = Node.newString(Token.STRING, "externProp");
    Node codeGetProp = new Node(Token.GETPROP, protoGetProp, codePropNode);
    mainRoot.addChildToBack(new Node(Token.EXPR_RESULT, codeGetProp));

    RenamePrototypes pass = new RenamePrototypes(compiler, true, null, null);
    pass.process(externsRoot, mainRoot);

    VariableMap map = pass.getPropertyMap();
    assertFalse("externProp should not be renamed", map.hasOriginal("externProp"));
    assertEquals("externProp", codePropNode.getString());
  }

  @Test(timeout = 4000)
  public void testReservedCharactersPreventedInGeneratedNames() {
    // Reserve 'a' so that generated names do not start with 'a'
    char[] reservedChars = new char[]{'a'};

    Node getPropProto = Node.newString(Token.GETPROP, "prototype");
    getPropProto.addChildToBack(Node.newString(Token.NAME, "Foo"));
    getPropProto.addChildToBack(Node.newString(Token.STRING, "prototype"));

    Node mNode = Node.newString(Token.STRING, "myMethod");
    mainRoot.addChildToBack(new Node(Token.EXPR_RESULT, new Node(Token.GETPROP, getPropProto, mNode)));

    RenamePrototypes pass = new RenamePrototypes(compiler, true, reservedChars, null);
    pass.process(externsRoot, mainRoot);

    VariableMap map = pass.getPropertyMap();
    String newName = map.lookupNewName("myMethod");
    assertNotNull(newName);
    assertFalse("Generated name should not contain reserved character 'a'", newName.contains("a"));
    assertEquals("b", newName);
  }

  // =================================================================
  // Partition C: Defect-Targeted Branch Zone (Object Literals & Prototypes)
  // =================================================================

  @Test(timeout = 4000)
  public void testPrototypeAssignedObjectLiteralWithNumericAndStringKeys() {
    // Targets handling of Object Literal assigned to prototype:
    // Foo.prototype = { propA: 1, 123: 2, propB: 3 };
    Node getPropFooProto = Node.newString(Token.GETPROP, "prototype");
    getPropFooProto.addChildToBack(Node.newString(Token.NAME, "Foo"));
    getPropFooProto.addChildToBack(Node.newString(Token.STRING, "prototype"));

    Node objLit = new Node(Token.OBJECTLIT);

    Node keyA = Node.newString(Token.STRING, "propA");
    keyA.addChildToBack(Node.newNumber(1));

    Node keyNum = Node.newNumber(123);
    keyNum.addChildToBack(Node.newNumber(2));

    Node keyB = Node.newString(Token.STRING, "propB");
    keyB.addChildToBack(Node.newNumber(3));

    objLit.addChildToBack(keyA);
    objLit.addChildToBack(keyNum);
    objLit.addChildToBack(keyB);

    Node assign = new Node(Token.ASSIGN, getPropFooProto, objLit);
    mainRoot.addChildToBack(new Node(Token.EXPR_RESULT, assign));

    RenamePrototypes pass = new RenamePrototypes(compiler, true, null, null);
    pass.process(externsRoot, mainRoot);

    VariableMap map = pass.getPropertyMap();
    assertTrue("propA in prototype objlit should be renamed", map.hasOriginal("propA"));
    assertTrue("propB in prototype objlit should be renamed", map.hasOriginal("propB"));
    assertFalse("Numeric key should not be recorded as a property to rename", map.hasOriginal("123"));
  }

  @Test(timeout = 4000)
  public void testPrototypeCallWithObjectLiteral() {
    // Tests: extend(Foo.prototype, { barProp: 1 });
    Node getPropFooProto = Node.newString(Token.GETPROP, "prototype");
    getPropFooProto.addChildToBack(Node.newString(Token.NAME, "Foo"));
    getPropFooProto.addChildToBack(Node.newString(Token.STRING, "prototype"));

    Node objLit = new Node(Token.OBJECTLIT);
    Node keyBar = Node.newString(Token.STRING, "barProp");
    keyBar.addChildToBack(Node.newNumber(1));
    objLit.addChildToBack(keyBar);

    Node callNode = new Node(Token.CALL, Node.newString(Token.NAME, "extend"), getPropFooProto, objLit);
    mainRoot.addChildToBack(new Node(Token.EXPR_RESULT, callNode));

    RenamePrototypes pass = new RenamePrototypes(compiler, true, null, null);
    pass.process(externsRoot, mainRoot);

    VariableMap map = pass.getPropertyMap();
    assertTrue("barProp should be recognized from CALL and renamed", map.hasOriginal("barProp"));
  }

  @Test(timeout = 4000)
  public void testStandaloneObjectLiteralPropertyRenaming() {
    // Standalone objLit: var obj = { regularProp: 1, _privateProp: 2, JSCompiler_export_prop: 3 };
    // Standalone objLit properties are ONLY renamed if they are private or satisfy both conditions
    Node objLit = new Node(Token.OBJECTLIT);

    Node regKey = Node.newString(Token.STRING, "regularProp");
    regKey.addChildToBack(Node.newNumber(1));

    Node privateKey = Node.newString(Token.STRING, "_privateProp");
    privateKey.addChildToBack(Node.newNumber(2));

    Node exportedKey = Node.newString(Token.STRING, "JSCompiler_export_prop");
    exportedKey.addChildToBack(Node.newNumber(3));

    objLit.addChildToBack(regKey);
    objLit.addChildToBack(privateKey);
    objLit.addChildToBack(exportedKey);

    mainRoot.addChildToBack(new Node(Token.EXPR_RESULT, objLit));

    RenamePrototypes pass = new RenamePrototypes(compiler, true, null, null);
    pass.process(externsRoot, mainRoot);

    VariableMap map = pass.getPropertyMap();
    // regularProp in standalone objLit is not private -> canRenameObjLitProperty is false
    assertFalse("regularProp in standalone objlit should not be renamed", map.hasOriginal("regularProp"));
    // _privateProp is private -> canRenameObjLitProperty is true
    assertTrue("_privateProp should be renamed", map.hasOriginal("_privateProp"));
    // JSCompiler_export_prop is exported -> false
    assertFalse("JSCompiler_export_prop is exported and must not be renamed", map.hasOriginal("JSCompiler_export_prop"));
  }

  @Test(timeout = 4000)
  public void testGetElemTraversalForExternsAndPrototypes() {
    // GETELEM: window["externFromElem"]
    Node externElem = new Node(Token.GETELEM,
        Node.newString(Token.NAME, "window"),
        Node.newString(Token.STRING, "externFromElem"));
    externsRoot.addChildToBack(new Node(Token.EXPR_RESULT, externElem));

    // GETELEM for prototype: Foo["prototype"]["methodFromElem"]
    Node fooProtoElem = new Node(Token.GETELEM,
        Node.newString(Token.NAME, "Foo"),
        Node.newString(Token.STRING, "prototype"));

    Node methodNode = Node.newString(Token.STRING, "methodFromElem");
    Node mainElem = new Node(Token.GETELEM, fooProtoElem, methodNode);
    mainRoot.addChildToBack(new Node(Token.EXPR_RESULT, mainElem));

    RenamePrototypes pass = new RenamePrototypes(compiler, true, null, null);
    pass.process(externsRoot, mainRoot);

    VariableMap map = pass.getPropertyMap();
    assertFalse("externFromElem should be reserved", map.hasOriginal("externFromElem"));
    assertTrue("methodFromElem should be renamed", map.hasOriginal("methodFromElem"));
  }

  // =================================================================
  // Partition D: Previous Rename Map Reuse & Exception Guards
  // =================================================================

  @Test(timeout = 4000)
  public void testPrevUsedRenameMapReusedAndCollisionAvoided() {
    // Previous map has:
    // "propA" -> "prevA"
    // "propB" -> "toString" (collision with reserved names!)
    Map<String, String> prev = new HashMap<String, String>();
    prev.put("propA", "prevA");
    prev.put("propB", "toString");
    VariableMap prevMap = new VariableMap(prev);

    Node getPropFooProto = Node.newString(Token.GETPROP, "prototype");
    getPropFooProto.addChildToBack(Node.newString(Token.NAME, "Foo"));
    getPropFooProto.addChildToBack(Node.newString(Token.STRING, "prototype"));

    Node pA = Node.newString(Token.STRING, "propA");
    Node pB = Node.newString(Token.STRING, "propB");

    mainRoot.addChildToBack(new Node(Token.EXPR_RESULT, new Node(Token.GETPROP, getPropFooProto.cloneTree(), pA)));
    mainRoot.addChildToBack(new Node(Token.EXPR_RESULT, new Node(Token.GETPROP, getPropFooProto.cloneTree(), pB)));

    RenamePrototypes pass = new RenamePrototypes(compiler, true, null, prevMap);
    pass.process(externsRoot, mainRoot);

    VariableMap map = pass.getPropertyMap();
    assertEquals("prevA", map.lookupNewName("propA"));
    // propB should not reuse "toString" because it is reserved; it should receive a new generated name
    assertNotEquals("toString", map.lookupNewName("propB"));
    assertNotNull(map.lookupNewName("propB"));
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testUnnormalizedLifecycleThrowsException() {
    compiler.setLifeCycleStage(LifeCycleStage.RAW);
    RenamePrototypes pass = new RenamePrototypes(compiler, true, null, null);
    pass.process(externsRoot, mainRoot);
  }

  @Test(timeout = 4000)
  public void testGetPropertyMapReturnsEmptyWhenNoRenameOccurred() {
    RenamePrototypes pass = new RenamePrototypes(compiler, false, null, null);
    VariableMap map = pass.getPropertyMap();
    assertNotNull(map);
    assertTrue(map.getOriginalNameToNewNameMap().isEmpty());
  }

  // =================================================================
  // Partition E: Both Prototype & ObjLit Counts > 0 Or Both == 0
  // =================================================================

  @Test(timeout = 4000)
  public void testPropertyUsedInBothPrototypeAndObjLit() {
    // If a property is used as prototype AND objLit, it must satisfy both canRenamePrototypeProperty
    // and canRenameObjLitProperty.
    // 1) "bothUnprivate": prototype + objLit. canRenameObjLit is false (since it's not private).
    //    Therefore, it should NOT be renamed.
    // 2) "_bothPrivate": prototype + objLit. canRenameObjLit is true (since private).
    //    Therefore, it should BE renamed.

    Node getPropFooProto = Node.newString(Token.GETPROP, "prototype");
    getPropFooProto.addChildToBack(Node.newString(Token.NAME, "Foo"));
    getPropFooProto.addChildToBack(Node.newString(Token.STRING, "prototype"));

    Node pBoth1 = Node.newString(Token.STRING, "bothUnprivate");
    Node pBothPriv1 = Node.newString(Token.STRING, "_bothPrivate");

    mainRoot.addChildToBack(new Node(Token.EXPR_RESULT, new Node(Token.GETPROP, getPropFooProto.cloneTree(), pBoth1)));
    mainRoot.addChildToBack(new Node(Token.EXPR_RESULT, new Node(Token.GETPROP, getPropFooProto.cloneTree(), pBothPriv1)));

    Node objLit = new Node(Token.OBJECTLIT);
    Node pBoth2 = Node.newString(Token.STRING, "bothUnprivate");
    pBoth2.addChildToBack(Node.newNumber(1));
    Node pBothPriv2 = Node.newString(Token.STRING, "_bothPrivate");
    pBothPriv2.addChildToBack(Node.newNumber(2));
    objLit.addChildToBack(pBoth2);
    objLit.addChildToBack(pBothPriv2);
    mainRoot.addChildToBack(new Node(Token.EXPR_RESULT, objLit));

    RenamePrototypes pass = new RenamePrototypes(compiler, true, null, null);
    pass.process(externsRoot, mainRoot);

    VariableMap map = pass.getPropertyMap();
    assertFalse("bothUnprivate is used in ObjLit and not private, should not rename",
        map.hasOriginal("bothUnprivate"));
    assertTrue("_bothPrivate satisfies both and should be renamed",
        map.hasOriginal("_bothPrivate"));
  }

  @Test(timeout = 4000)
  public void testRuntimePropertyAccessBothCountsZero() {
    // o.newProp = x where prototypeCount == 0 and objLitCount == 0 (only refCount > 0).
    // canRename falls back to canRenamePrototypeProperty() && canRenameObjLitProperty().
    // If private, it can be renamed; if unprivate, canRenameObjLit is false so it cannot be renamed.
    Node objNode = Node.newString(Token.NAME, "o");
    Node refUnprivate = Node.newString(Token.STRING, "runtimeProp");
    Node refPrivate = Node.newString(Token.STRING, "_runtimePrivate");

    mainRoot.addChildToBack(new Node(Token.EXPR_RESULT, new Node(Token.GETPROP, objNode.cloneTree(), refUnprivate)));
    mainRoot.addChildToBack(new Node(Token.EXPR_RESULT, new Node(Token.GETPROP, objNode.cloneTree(), refPrivate)));

    RenamePrototypes pass = new RenamePrototypes(compiler, true, null, null);
    pass.process(externsRoot, mainRoot);

    VariableMap map = pass.getPropertyMap();
    assertFalse("runtimeProp cannot rename obj lit portion", map.hasOriginal("runtimeProp"));
    assertTrue("_runtimePrivate can rename both", map.hasOriginal("_runtimePrivate"));
  }
}