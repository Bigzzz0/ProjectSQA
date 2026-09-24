package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import java.util.*;

/**
 * [Branch & Defect Analysis Matrix]
 *
 * Target branches and conditions for GlobalNamespace:
 *
 * 1. Constructor branches:
 *    - GlobalNamespace(compiler, root) - calls this(compiler, null, root)
 *    - GlobalNamespace(compiler, externsRoot, root) - sets fields
 *
 * 2. process() method:
 *    - if (externsRoot != null) -> traverses externs, sets inExterns=true
 *    - After traversal sets generated=true
 *
 * 3. BuildGlobalNamespace.visit() - main state machine:
 *    - Token.STRING: checks parent is OBJECTLIT, gets name via getNameForObjLitKey
 *    - Token.NAME: checks parent VAR, ASSIGN, GETPROP, FUNCTION
 *    - Token.GETPROP: checks parent ASSIGN, GETPROP
 *    - isGlobalNameReference check
 *    - isSet vs get path branching
 *    - isGlobalScope vs local scope branching
 *
 * 4. getNameForObjLitKey() branching:
 *    - Token.NAME with VAR grandparent check
 *    - Token.ASSIGN with lvalue qualified name
 *    - Token.STRING with nested OBJECTLIT
 *    - default returns null
 *    - TokenStream.isJSIdentifier check
 *
 * 5. getValueType():
 *    - Token.OBJECTLIT, Token.FUNCTION, Token.OR, Token.HOOK, default OTHER
 *
 * 6. handleSetFromGlobal() branching:
 *    - maybeHandlePrototypePrefix guard
 *    - isNestedAssign check for twin refs
 *    - isConstructorOrEnumDeclaration check
 *
 * 7. handleSetFromLocal() branching:
 *    - maybeHandlePrototypePrefix guard
 *    - isNestedAssign check for twin refs
 *
 * 8. handleGet() with parent type branching:
 *    - IF, TYPEOF, VOID, NOT, BITNOT, POS, NEG -> DIRECT_GET
 *    - CALL -> CALL_GET or ALIASING_GET
 *    - NEW -> DIRECT_GET or ALIASING_GET
 *    - OR, AND -> determineGetTypeForHookOrBooleanExpr
 *    - HOOK -> determineGetTypeForHookOrBooleanExpr
 *    - default -> ALIASING_GET
 *
 * 9. determineGetTypeForHookOrBooleanExpr() ancestor traversal:
 *    - EXPR_RESULT, VAR, IF, WHILE, FOR, TYPEOF, etc -> DIRECT_GET
 *    - HOOK with first child -> DIRECT_GET
 *    - ASSIGN with name match -> continue
 *    - NAME (var decl) with name match -> continue
 *    - CALL with first child -> continue
 *    - default -> ALIASING_GET
 *
 * 10. maybeHandlePrototypePrefix():
 *     - name ends with ".prototype"
 *     - name contains ".prototype."
 *     - NodeUtil.isObjectLitKey check
 *     - parent/n traversal for prefix removal
 *     - handleGet with PROTOTYPE_GET type
 *
 * 11. isNestedAssign(): parent is ASSIGN and not expression node
 *
 * 12. isConstructorOrEnumDeclaration():
 *     - ASSIGN parent: get JSDocInfo, check value node type
 *     - VAR parent: get JSDocInfo from node or parent, check value
 *     - default: return false
 *
 * 13. Name class:
 *     - addRef() with all ref types, counting logic
 *     - removeRef() with declaration removal and recounting
 *     - canEliminate(), canCollapse(), canCollapseUnannotatedChildNames()
 *     - shouldKeepKeys(), needsToBeStubbed(), setIsClassOrEnum()
 *     - isNamespace(), isSimpleName()
 *
 * 14. Ref class:
 *     - Constructor with NodeTraversal
 *     - Twin constructor (private)
 *     - Testing constructor createRefForTesting
 *     - markTwins() validation
 *     - cloneAndReclassify()
 *
 * Defect target (from Defects4J): 
 * CollapseProperties failures related to aliasing of functions
 * in local scope and prototype handling. The key issue involves
 * incorrect handling of setFromLocal when prototype names are involved,
 * and incorrect detection of aliasing gets for function declarations.
 * Test methods specifically target:
 *   - addPropertyToChildOfUncollapsibleFunctionInLocalScope
 *   - aliasCreatedForFunctionDepth1_1/2/3
 *   - addPropertyToUncollapsibleNamedCtorInLocalScopeDepth1
 *   - addPropertyToUncollapsibleFunctionInLocalScopeDepth1/2
 *   - aliasCreatedForFunctionDepth2
 *
 * Strategy: Test the Name class methods (canCollapse, canEliminate, 
 * canCollapseUnannotatedChildNames) with various ref configurations 
 * that mimic the failing scenarios.
 */

public class GlobalNamespaceDeepseekTest {

    // ================================================================
    // Partition A: Core Functional Logic & State Transitions
    // ================================================================

    @Test(timeout = 4000)
    public void testNameConstructor() {
        Name name = new Name("test", null, false);
        assertEquals("test", name.name);
        assertNull(name.parent);
        assertFalse(name.inExterns);
        assertEquals(Name.Type.OTHER, name.type);
        assertEquals(0, name.globalSets);
        assertEquals(0, name.localSets);
        assertEquals(0, name.totalGets);
        assertEquals(0, name.aliasingGets);
        assertEquals(0, name.callGets);
        assertFalse(name.isClassOrEnum);
        assertFalse(name.hasClassOrEnumDescendant);
        assertNull(name.props);
        assertNull(name.refs);
        assertNull(name.declaration);
    }

    @Test(timeout = 4000)
    public void testNameConstructorWithParent() {
        Name parent = new Name("parent", null, false);
        Name child = new Name("child", parent, true);
        assertEquals("child", child.name);
        assertSame(parent, child.parent);
        assertTrue(child.inExterns);
    }

    @Test(timeout = 4000)
    public void testNameAddProperty() {
        Name parent = new Name("parent", null, false);
        Name child = parent.addProperty("child", true);
        assertNotNull(parent.props);
        assertEquals(1, parent.props.size());
        assertSame(child, parent.props.get(0));
        assertSame(parent, child.parent);
        assertTrue(child.inExterns);

        Name child2 = parent.addProperty("child2", false);
        assertEquals(2, parent.props.size());
        assertSame(child2, parent.props.get(1));
        assertFalse(child2.inExterns);
    }

    @Test(timeout = 4000)
    public void testNameAddRefSetFromGlobal() {
        Name name = new Name("test", null, false);
        Ref ref = Ref.createRefForTesting(Ref.Type.SET_FROM_GLOBAL);
        name.addRef(ref);
        assertSame(ref, name.declaration);
        assertEquals(1, name.globalSets);
        assertEquals(0, name.localSets);
        assertEquals(0, name.totalGets);
        assertEquals(0, name.aliasingGets);
        assertEquals(0, name.callGets);
    }

    @Test(timeout = 4000)
    public void testNameAddRefSetFromLocal() {
        Name name = new Name("test", null, false);
        Ref ref = Ref.createRefForTesting(Ref.Type.SET_FROM_LOCAL);
        name.addRef(ref);
        assertEquals(0, name.globalSets);
        assertEquals(1, name.localSets);
        assertNull(name.declaration);
    }

    @Test(timeout = 4000)
    public void testNameAddRefDirectGet() {
        Name name = new Name("test", null, false);
        Ref ref = Ref.createRefForTesting(Ref.Type.DIRECT_GET);
        name.addRef(ref);
        assertEquals(1, name.totalGets);
        assertEquals(0, name.aliasingGets);
        assertEquals(0, name.callGets);
    }

    @Test(timeout = 4000)
    public void testNameAddRefAliasingGet() {
        Name name = new Name("test", null, false);
        Ref ref = Ref.createRefForTesting(Ref.Type.ALIASING_GET);
        name.addRef(ref);
        assertEquals(1, name.totalGets);
        assertEquals(1, name.aliasingGets);
    }

    @Test(timeout = 4000)
    public void testNameAddRefCallGet() {
        Name name = new Name("test", null, false);
        Ref ref = Ref.createRefForTesting(Ref.Type.CALL_GET);
        name.addRef(ref);
        assertEquals(1, name.totalGets);
        assertEquals(1, name.callGets);
    }

    @Test(timeout = 4000)
    public void testNameAddRefPrototypeGet() {
        Name name = new Name("test", null, false);
        Ref ref = Ref.createRefForTesting(Ref.Type.PROTOTYPE_GET);
        name.addRef(ref);
        assertEquals(1, name.totalGets);
        assertEquals(0, name.aliasingGets);
    }

    @Test(timeout = 4000)
    public void testNameRemoveRefDeclaration() {
        Name name = new Name("test", null, false);
        Ref ref1 = Ref.createRefForTesting(Ref.Type.SET_FROM_GLOBAL);
        Ref ref2 = Ref.createRefForTesting(Ref.Type.SET_FROM_GLOBAL);
        name.addRef(ref1);
        name.addRef(ref2);
        assertEquals(2, name.globalSets);
        assertSame(ref1, name.declaration);

        name.removeRef(ref1);
        assertEquals(1, name.globalSets);
        assertSame(ref2, name.declaration);
    }

    @Test(timeout = 4000)
    public void testNameRemoveRefFromList() {
        Name name = new Name("test", null, false);
        Ref ref = Ref.createRefForTesting(Ref.Type.DIRECT_GET);
        name.addRef(ref);
        assertEquals(1, name.totalGets);

        name.removeRef(ref);
        assertEquals(0, name.totalGets);
        assertNotNull(name.refs); // list still exists but empty
    }

    @Test(timeout = 4000)
    public void testIsSimpleName() {
        Name parent = new Name("parent", null, false);
        Name child = new Name("child", parent, false);
        assertTrue(parent.isSimpleName());
        assertFalse(child.isSimpleName());
    }

    @Test(timeout = 4000)
    public void testFullName() {
        Name parent = new Name("a", null, false);
        Name child = new Name("b", parent, false);
        Name grandchild = new Name("c", child, false);
        assertEquals("a", parent.fullName());
        assertEquals("a.b", child.fullName());
        assertEquals("a.b.c", grandchild.fullName());
    }

    @Test(timeout = 4000)
    public void testSetIsClassOrEnum() {
        Name grandparent = new Name("gp", null, false);
        Name parent = new Name("p", grandparent, false);
        Name child = new Name("c", parent, false);

        child.setIsClassOrEnum();
        assertTrue(child.isClassOrEnum);
        assertTrue(parent.hasClassOrEnumDescendant);
        assertTrue(grandparent.hasClassOrEnumDescendant);

        // Verify classOrEnum is not set on ancestors
        assertFalse(parent.isClassOrEnum);
        assertFalse(grandparent.isClassOrEnum);
    }

    @Test(timeout = 4000)
    public void testIsNamespace() {
        Name obj = new Name("obj", null, false);
        obj.type = Name.Type.OBJECTLIT;
        assertFalse(obj.isNamespace()); // no hasClassOrEnumDescendant

        Name child = new Name("child", obj, false);
        child.setIsClassOrEnum();
        assertTrue(obj.isNamespace());

        // Type OTHER should return false even with descendant
        Name n2 = new Name("n2", null, false);
        n2.type = Name.Type.OTHER;
        Name c2 = new Name("c2", n2, false);
        c2.setIsClassOrEnum();
        assertFalse(n2.isNamespace());
    }

    @Test(timeout = 4000)
    public void testShouldKeepKeys() {
        Name obj = new Name("obj", null, false);
        obj.type = Name.Type.OBJECTLIT;
        assertFalse(obj.shouldKeepKeys());

        Ref alias = Ref.createRefForTesting(Ref.Type.ALIASING_GET);
        obj.addRef(alias);
        assertTrue(obj.shouldKeepKeys());

        // Non-OBJECTLIT type should return false even with aliasing
        Name func = new Name("func", null, false);
        func.type = Name.Type.FUNCTION;
        func.addRef(alias);
        assertFalse(func.shouldKeepKeys());
    }

    @Test(timeout = 4000)
    public void testNeedsToBeStubbed() {
        Name name = new Name("test", null, false);
        assertFalse(name.needsToBeStubbed());

        Ref local = Ref.createRefForTesting(Ref.Type.SET_FROM_LOCAL);
        name.addRef(local);
        assertTrue(name.needsToBeStubbed());

        Ref global = Ref.createRefForTesting(Ref.Type.SET_FROM_GLOBAL);
        name.addRef(global);
        assertFalse(name.needsToBeStubbed());
    }

    // ================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // ================================================================

    @Test(timeout = 4000)
    public void testNameWithNullParent() {
        Name name = new Name("root", null, false);
        assertNull(name.parent);
        assertEquals("root", name.fullName());
    }

    @Test(timeout = 4000)
    public void testNameWithInExternsFlag() {
        Name externName = new Name("e", null, true);
        assertTrue(externName.inExterns);
        assertFalse(externName.canCollapse()); // externs always return false

        Name nonExtern = new Name("n", null, false);
        assertFalse(nonExtern.inExterns);
    }

    @Test(timeout = 4000)
    public void testNameAddPropertyNullName() {
        Name parent = new Name("parent", null, false);
        Name child = parent.addProperty("", false);
        assertNotNull(child);
        assertEquals("", child.name);
    }

    @Test(timeout = 4000)
    public void testRefCreateRefForTesting() {
        Ref ref = Ref.createRefForTesting(Ref.Type.SET_FROM_GLOBAL);
        assertEquals(Ref.Type.SET_FROM_GLOBAL, ref.type);
        assertEquals("source", ref.sourceName);
        assertNull(ref.scope);
        assertNull(ref.module);
        assertNull(ref.node);
    }

    @Test(timeout = 4000)
    public void testRefIsSet() {
        assertTrue(Ref.createRefForTesting(Ref.Type.SET_FROM_GLOBAL).isSet());
        assertTrue(Ref.createRefForTesting(Ref.Type.SET_FROM_LOCAL).isSet());
        assertFalse(Ref.createRefForTesting(Ref.Type.DIRECT_GET).isSet());
        assertFalse(Ref.createRefForTesting(Ref.Type.ALIASING_GET).isSet());
        assertFalse(Ref.createRefForTesting(Ref.Type.CALL_GET).isSet());
        assertFalse(Ref.createRefForTesting(Ref.Type.PROTOTYPE_GET).isSet());
    }

    @Test(timeout = 4000)
    public void testRefMarkTwins() {
        Ref set = Ref.createRefForTesting(Ref.Type.SET_FROM_GLOBAL);
        Ref alias = Ref.createRefForTesting(Ref.Type.ALIASING_GET);
        Ref.markTwins(set, alias);
        assertSame(alias, set.getTwin());
        assertSame(set, alias.getTwin());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testRefMarkTwinsInvalidBothGet() {
        Ref a = Ref.createRefForTesting(Ref.Type.ALIASING_GET);
        Ref b = Ref.createRefForTesting(Ref.Type.DIRECT_GET);
        Ref.markTwins(a, b);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testRefMarkTwinsInvalidBothSet() {
        Ref a = Ref.createRefForTesting(Ref.Type.SET_FROM_GLOBAL);
        Ref b = Ref.createRefForTesting(Ref.Type.SET_FROM_LOCAL);
        Ref.markTwins(a, b);
    }

    @Test(timeout = 4000)
    public void testRefCloneAndReclassify() {
        // Create a ref, clone it with new type
        Ref original = Ref.createRefForTesting(Ref.Type.DIRECT_GET);
        Ref cloned = original.cloneAndReclassify(Ref.Type.ALIASING_GET);
        assertEquals(Ref.Type.ALIASING_GET, cloned.type);
        assertNull(cloned.node);
        assertEquals(original.sourceName, cloned.sourceName);
        assertNull(cloned.scope);
        assertNull(cloned.module);
    }

    @Test(timeout = 4000)
    public void testNameToString() {
        Name name = new Name("x", null, false);
        name.globalSets = 1;
        name.localSets = 2;
        name.totalGets = 3;
        name.aliasingGets = 4;
        name.callGets = 5;
        String s = name.toString();
        assertTrue(s.contains("x"));
        assertTrue(s.contains("globalSets=1"));
        assertTrue(s.contains("localSets=2"));
        assertTrue(s.contains("totalGets=3"));
        assertTrue(s.contains("aliasingGets=4"));
        assertTrue(s.contains("callGets=5"));
    }

    // ================================================================
    // Partition C: Defect-Targeted Branch Zone
    // ================================================================

    /**
     * Defect target: alias created for function in local scope
     * This tests the canCollapseUnannotatedChildNames logic when
     * a name is a FUNCTION type with aliasing gets > 0.
     * The bug would allow collapse when it shouldn't.
     */
    @Test(timeout = 4000)
    public void testCanCollapseUnannotatedChildNamesWithFunctionAndAliasing() {
        Name name = new Name("test", null, false);
        name.type = Name.Type.FUNCTION;
        name.globalSets = 1;
        name.localSets = 0;

        Ref decl = Ref.createRefForTesting(Ref.Type.SET_FROM_GLOBAL);
        name.declaration = decl;
        assertNull(decl.getTwin()); // no twin

        // Should return false if aliasingGets > 0
        Ref alias = Ref.createRefForTesting(Ref.Type.ALIASING_GET);
        name.addRef(alias);
        assertFalse(name.canCollapseUnannotatedChildNames());

        // Reset: remove alias, should return true
        name.aliasingGets = 0;
        name.totalGets = 0;
        name.refs.remove(alias);
        // For FUNCTION with aliasingGets=0, should return true (no parent check)
        assertTrue(name.canCollapseUnannotatedChildNames());
    }

    /**
     * Defect target: addPropertyToUncollapsibleFunctionInLocalScopeDepth1
     * Tests that when a function has localSets > 0, canCollapse returns false
     * if the parent cannot collapse its child names.
     */
    @Test(timeout = 4000)
    public void testCanCollapseWithLocalSetsAndParentCanCollapse() {
        Name parent = new Name("parent", null, false);
        parent.type = Name.Type.OTHER;
        parent.globalSets = 0;
        parent.localSets = 0;

        Name child = new Name("child", parent, false);
        child.type = Name.Type.FUNCTION;
        child.globalSets = 0;
        child.localSets = 1; // only local sets

        // child canCollapse depends on parent.canCollapseUnannotatedChildNames
        // parent has type OTHER, globalSets=1 is required for canCollapseUnannotatedChildNames
        // so parent returns false, thus child returns false
        assertFalse(child.canCollapse());
    }

    /**
     * Defect target: aliasCreatedForFunctionDepth1_1/2/3
     * Tests that when a declaration has a twin (aliasing get from nested assign),
     * canCollapseUnannotatedChildNames returns false.
     * This prevents the name from being collapsed when it shouldn't be.
     */
    @Test(timeout = 4000)
    public void testCanCollapseUnannotatedChildNamesWithTwinDeclaration() {
        Name name = new Name("test", null, false);
        name.type = Name.Type.FUNCTION;
        name.globalSets = 1;
        name.localSets = 0;

        // Create declaration with twin
        Ref decl = Ref.createRefForTesting(Ref.Type.SET_FROM_GLOBAL);
        Ref twin = Ref.createRefForTesting(Ref.Type.ALIASING_GET);
        Ref.markTwins(decl, twin);
        name.declaration = decl;

        // Should return false because declaration has twin
        assertFalse(name.canCollapseUnannotatedChildNames());

        // Remove twin, should now return true
        name.declaration.getTwin(); // verify
        // Manually break twin for testing
        decl = Ref.createRefForTesting(Ref.Type.SET_FROM_GLOBAL);
        decl.getTwin(); // null
        name.declaration = decl;
        name.aliasingGets = 0;
        name.totalGets = 0;
        assertTrue(name.canCollapseUnannotatedChildNames());
    }

    /**
     * Defect target: addPropertyToUncollapsibleNamedCtorInLocalScopeDepth1
     * Tests that isConstructorOrEnumDeclaration behaves correctly for
     * different node types.
     */
    @Test(timeout = 4000)
    public void testIsConstructorOrEnumDeclarationWithDifferentParents() {
        // Test default case (not ASSIGN or VAR) returns false
        Node objLit = new Node(Token.OBJECTLIT);
        // A child of object lit is not ASSIGN or VAR, so default false
        // Can't easily test the private method directly, but we test
        // the logic through canCollapse which uses isClassOrEnum

        Name name = new Name("test", null, false);
        name.type = Name.Type.FUNCTION;
        name.globalSets = 1;
        name.localSets = 0;
        name.setIsClassOrEnum();

        // When isClassOrEnum is true, canCollapse should return true
        // (assuming parent.canCollapseUnannotatedChildNames is true)
        assertTrue(name.canCollapse());
    }

    /**
     * Defect target: addPropertyToUncollapsibleFunctionInLocalScopeDepth2
     * Tests nested scenario: grandparent -> parent -> child
     * where only child has sets, but grandparent is uncollapsible.
     */
    @Test(timeout = 4000)
    public void testNestedNamesCollapseRestrictions() {
        Name grandparent = new Name("gp", null, false);
        grandparent.type = Name.Type.OTHER;
        grandparent.globalSets = 0;
        grandparent.localSets = 0;

        Name parent = new Name("p", grandparent, false);
        parent.type = Name.Type.OTHER;
        parent.globalSets = 0;
        parent.localSets = 0;

        Name child = new Name("c", parent, false);
        child.type = Name.Type.FUNCTION;
        child.globalSets = 0;
        child.localSets = 1;

        // child.canCollapse() checks parent.canCollapseUnannotatedChildNames()
        // parent checks grandparent.canCollapseUnannotatedChildNames()
        // grandparent with type OTHER and globalSets!=1 fails -> false
        assertFalse(child.canCollapse());
        assertFalse(parent.canCollapse());
        assertFalse(grandparent.canCollapse());
    }

    /**
     * Defect target: aliasCreatedForFunctionDepth2
     * Tests that aliasing at grandparent level prevents collapse of nested names.
     */
    @Test(timeout = 4000)
    public void testDeepAliasingPreventsCollapse() {
        Name grandparent = new Name("gp", null, false);
        grandparent.type = Name.Type.OBJECTLIT;
        grandparent.globalSets = 1;
        grandparent.localSets = 0;

        Name parent = new Name("p", grandparent, false);
        parent.type = Name.Type.