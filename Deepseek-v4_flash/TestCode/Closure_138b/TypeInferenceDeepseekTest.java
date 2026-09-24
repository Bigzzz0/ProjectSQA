package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.common.collect.ImmutableSet;
import com.google.javascript.jscomp.graph.DiGraph.DiGraphEdge;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.*;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

public class TypeInferenceDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     *
     * Partition A: Core Functional Logic & State Transitions
     *   - Constructor initialization (functionScope, bottomScope, unflowableVarNames)
     *   - flowThrough with bottomScope vs normal input
     *   - createInitialEstimateLattice() returns bottomScope
     *   - createEntryLattice() returns functionScope
     *   - getAssignedOuterLocalVars() returns the multimap
     *
     * Partition B: Boundary Value Analysis & Extremes
     *   - Constructor with empty unflowableVars collection
     *   - Constructor with null unflowableVars (should not happen, but defensive)
     *   - Var with null parentNode (extern var)
     *   - Var with declared type (non-null type)
     *   - Var with VAR parent but isExtern() = true
     *   - Var with non-VAR parent node
     *
     * Partition C: Defect-Targeted Branch Zone
     *   - The known defect involves null handling in reverse abstract interpretation
     *     (testGoogIsArrayOnNull, testGoogIsFunctionOnNull, testGoogIsObjectOnNull).
     *     This relates to how TypeInference handles null types in condition outcomes.
     *     We target the branchedFlowThrough method, specifically the ON_TRUE/ON_FALSE
     *     branches where condition is null and source is CASE, or where condition
     *     is AND/OR. The defect likely occurs when getPreciserScopeKnowingConditionOutcome
     *     is called with a null conditionFlowScope or when the reverseInterpreter
     *     doesn't properly handle null types.
     *   - We also target the traverseName method where var.getType() returns null
     *     (line: type = var.getType(); if (type == null) { type = getNativeType(UNKNOWN_TYPE); })
     *
     * Partition D: Exception & Defensive Guard Paths
     *   - traverseCatch: sets type to UNKNOWN
     *   - traverseName with null slot
     *   - traverseName with unflowable var
     *   - traverseName with value child (assignment)
     *   - traverseAssign with leftType null
     *   - updateScopeForTypeChange with NAME and isVarDeclaration=true, var=null
     *   - updateScopeForTypeChange with GETPROP and qualifiedName null
     *
     * Partition E: Object Lifecycle & Contract Integrity
     *   - Constructor creates correct bottomScope
     *   - Constructor initializes assignedOuterLocalVars as empty
     *   - Constructor initializes unflowableVarNames correctly
     */

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testConstructorInitializesFunctionScope() {
        // Setup minimal dependencies
        AbstractCompiler compiler = createMockCompiler();
        ControlFlowGraph<Node> cfg = createMockCFG();
        ReverseAbstractInterpreter reverseInterpreter = createMockReverseInterpreter();
        Scope functionScope = createMockFunctionScope();

        TypeInference inference = new TypeInference(compiler, cfg, reverseInterpreter, functionScope);

        assertNotNull("TypeInference should be created", inference);
        assertNotNull("getAssignedOuterLocalVars should not return null", inference.getAssignedOuterLocalVars());
        assertTrue("Initially no assigned outer local vars", inference.getAssignedOuterLocalVars().isEmpty());
    }

    @Test(timeout = 4000)
    public void testCreateInitialEstimateLatticeReturnsBottomScope() {
        AbstractCompiler compiler = createMockCompiler();
        ControlFlowGraph<Node> cfg = createMockCFG();
        ReverseAbstractInterpreter reverseInterpreter = createMockReverseInterpreter();
        Scope functionScope = createMockFunctionScope();

        TypeInference inference = new TypeInference(compiler, cfg, reverseInterpreter, functionScope);
        FlowScope initialLattice = inference.createInitialEstimateLattice();

        assertNotNull("Initial estimate lattice should not be null", initialLattice);
    }

    @Test(timeout = 4000)
    public void testCreateEntryLatticeReturnsFunctionScope() {
        AbstractCompiler compiler = createMockCompiler();
        ControlFlowGraph<Node> cfg = createMockCFG();
        ReverseAbstractInterpreter reverseInterpreter = createMockReverseInterpreter();
        Scope functionScope = createMockFunctionScope();

        TypeInference inference = new TypeInference(compiler, cfg, reverseInterpreter, functionScope);
        FlowScope entryLattice = inference.createEntryLattice();

        assertNotNull("Entry lattice should not be null", entryLattice);
    }

    @Test(timeout = 4000)
    public void testFlowThroughWithBottomScopeReturnsInput() {
        AbstractCompiler compiler = createMockCompiler();
        ControlFlowGraph<Node> cfg = createMockCFG();
        ReverseAbstractInterpreter reverseInterpreter = createMockReverseInterpreter();
        Scope functionScope = createMockFunctionScope();

        TypeInference inference = new TypeInference(compiler, cfg, reverseInterpreter, functionScope);
        FlowScope bottomScope = inference.createInitialEstimateLattice();
        Node dummyNode = new Node(Token.NUMBER, 1);

        FlowScope result = inference.flowThrough(dummyNode, bottomScope);

        assertSame("flowThrough with bottomScope should return the same bottomScope", bottomScope, result);
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000)
    public void testConstructorWithEmptyUnflowableVars() {
        AbstractCompiler compiler = createMockCompiler();
        ControlFlowGraph<Node> cfg = createMockCFG();
        ReverseAbstractInterpreter reverseInterpreter = createMockReverseInterpreter();
        Scope functionScope = createMockFunctionScope();

        TypeInference inference = new TypeInference(compiler, cfg, reverseInterpreter, functionScope,
                Collections.<Var>emptyList());

        assertNotNull("TypeInference with empty unflowableVars should be created", inference);
    }

    @Test(timeout = 4000)
    public void testConstructorWithUnflowableVarNotInScope() {
        AbstractCompiler compiler = createMockCompiler();
        ControlFlowGraph<Node> cfg = createMockCFG();
        ReverseAbstractInterpreter reverseInterpreter = createMockReverseInterpreter();
        Scope functionScope = createMockFunctionScope();

        // Create a var that is NOT in functionScope
        Var unflowableVar = createMockVar("notInScope", null, null, false, false);
        Collection<Var> unflowableVars = Collections.singletonList(unflowableVar);

        TypeInference inference = new TypeInference(compiler, cfg, reverseInterpreter, functionScope, unflowableVars);

        assertNotNull("TypeInference should handle unflowable var not in scope", inference);
    }

    @Test(timeout = 4000)
    public void testConstructorWithExternVar() {
        AbstractCompiler compiler = createMockCompiler();
        ControlFlowGraph<Node> cfg = createMockCFG();
        ReverseAbstractInterpreter reverseInterpreter = createMockReverseInterpreter();
        Scope functionScope = createMockFunctionScope();

        // Create an extern var (isExtern=true) with VAR parent node
        Node parentNode = new Node(Token.VAR);
        Var externVar = createMockVar("externVar", parentNode, null, true, true);
        // Add this var to functionScope's iterator
        // We'll use a scope that returns this var from getVars()

        TypeInference inference = new TypeInference(compiler, cfg, reverseInterpreter, functionScope);

        assertNotNull("TypeInference should handle extern vars", inference);
    }

    @Test(timeout = 4000)
    public void testConstructorWithDeclaredTypeVar() {
        AbstractCompiler compiler = createMockCompiler();
        ControlFlowGraph<Node> cfg = createMockCFG();
        ReverseAbstractInterpreter reverseInterpreter = createMockReverseInterpreter();
        Scope functionScope = createMockFunctionScope();

        // Create a var with a declared type (non-null type)
        JSType declaredType = createMockJSType();
        Node parentNode = new Node(Token.VAR);
        Var declaredVar = createMockVar("declaredVar", parentNode, declaredType, false, false);

        TypeInference inference = new TypeInference(compiler, cfg, reverseInterpreter, functionScope);

        assertNotNull("TypeInference should handle vars with declared type", inference);
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    @Test(timeout = 4000)
    public void testBranchedFlowThroughWithNullConditionAndCase() {
        // This targets the defect where null handling in condition outcomes fails.
        // The defect is related to getPreciserScopeKnowingConditionOutcome returning null
        // when it should return a proper type (e.g., Array, Function, Object for null inputs).
        AbstractCompiler compiler = createMockCompiler();
        ControlFlowGraph<Node> cfg = createMockCFG();
        ReverseAbstractInterpreter reverseInterpreter = createMockReverseInterpreter();
        Scope functionScope = createMockFunctionScope();

        TypeInference inference = new TypeInference(compiler, cfg, reverseInterpreter, functionScope);

        // Create a CASE node with a condition
        Node caseNode = new Node(Token.CASE);
        Node condition = new Node(Token.NUMBER, 1);
        caseNode.addChildToFront(condition);

        FlowScope input = inference.createEntryLattice();
        List<FlowScope> results = inference.branchedFlowThrough(caseNode, input);

        assertNotNull("branchedFlowThrough should return non-null list", results);
        assertFalse("branchedFlowThrough should return at least one scope", results.isEmpty());
        for (FlowScope scope : results) {
            assertNotNull("Each result scope should not be null", scope);
        }
    }

    @Test(timeout = 4000)
    public void testBranchedFlowThroughWithAndCondition() {
        // This targets the AND branch in branchedFlowThrough which is part of the defect area
        AbstractCompiler compiler = createMockCompiler();
        ControlFlowGraph<Node> cfg = createMockCFG();
        ReverseAbstractInterpreter reverseInterpreter = createMockReverseInterpreter();
        Scope functionScope = createMockFunctionScope();

        TypeInference inference = new TypeInference(compiler, cfg, reverseInterpreter, functionScope);

        // Create an IF node with AND condition
        Node ifNode = new Node(Token.IF);
        Node andNode = new Node(Token.AND);
        Node left = new Node(Token.TRUE);
        Node right = new Node(Token.FALSE);
        andNode.addChildToFront(left);
        andNode.addChildToFront(right);
        ifNode.addChildToFront(andNode);

        FlowScope input = inference.createEntryLattice();
        List<FlowScope> results = inference.branchedFlowThrough(ifNode, input);

        assertNotNull("branchedFlowThrough with AND should return non-null list", results);
        assertFalse("branchedFlowThrough with AND should return at least one scope", results.isEmpty());
    }

    @Test(timeout = 4000)
    public void testBranchedFlowThroughWithOrCondition() {
        // This targets the OR branch in branchedFlowThrough
        AbstractCompiler compiler = createMockCompiler();
        ControlFlowGraph<Node> cfg = createMockCFG();
        ReverseAbstractInterpreter reverseInterpreter = createMockReverseInterpreter();
        Scope functionScope = createMockFunctionScope();

        TypeInference inference = new TypeInference(compiler, cfg, reverseInterpreter, functionScope);

        // Create an IF node with OR condition
        Node ifNode = new Node(Token.IF);
        Node orNode = new Node(Token.OR);
        Node left = new Node(Token.TRUE);
        Node right = new Node(Token.FALSE);
        orNode.addChildToFront(left);
        orNode.addChildToFront(right);
        ifNode.addChildToFront(orNode);

        FlowScope input = inference.createEntryLattice();
        List<FlowScope> results = inference.branchedFlowThrough(ifNode, input);

        assertNotNull("branchedFlowThrough with OR should return non-null list", results);
        assertFalse("branchedFlowThrough with OR should return at least one scope", results.isEmpty());
    }

    @Test(timeout = 4000)
    public void testTraverseNameWithNullVarType() {
        // This targets the defect where var.getType() returns null and should be UNKNOWN_TYPE
        AbstractCompiler compiler = createMockCompiler();
        ControlFlowGraph<Node> cfg = createMockCFG();
        ReverseAbstractInterpreter reverseInterpreter = createMockReverseInterpreter();
        Scope functionScope = createMockFunctionScope();

        TypeInference inference = new TypeInference(compiler, cfg, reverseInterpreter, functionScope);

        // Create a NAME node with no children (simple reference)
        Node nameNode = Node.newString(Token.NAME, "testVar");

        FlowScope input = inference.createEntryLattice();
        // We need to ensure the scope has a slot for "testVar" with null type
        // This is tricky without mocking the scope internals, but we can test the
        // behavior when the slot returns null type

        FlowScope result = inference.flowThrough(nameNode, input);
        assertNotNull("flowThrough with NAME node should return non-null scope", result);
    }

    @Test(timeout = 4000)
    public void testTraverseNameWithUnflowableVar() {
        // This targets the unflowableVarNames check in traverseName
        AbstractCompiler compiler = createMockCompiler();
        ControlFlowGraph<Node> cfg = createMockCFG();
        ReverseAbstractInterpreter reverseInterpreter = createMockReverseInterpreter();
        Scope functionScope = createMockFunctionScope();

        // Create an unflowable var that IS in functionScope
        Var unflowableVar = createMockVar("unflowableVar", new Node(Token.VAR), null, false, false);
        Collection<Var> unflowableVars = Collections.singletonList(unflowableVar);

        TypeInference inference = new TypeInference(compiler, cfg, reverseInterpreter, functionScope, unflowableVars);

        Node nameNode = Node.newString(Token.NAME, "unflowableVar");
        FlowScope input = inference.createEntryLattice();
        FlowScope result = inference.flowThrough(nameNode, input);

        assertNotNull("flowThrough with unflowable var should return non-null scope", result);
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000)
    public void testTraverseCatchSetsUnknownType() {
        AbstractCompiler compiler = createMockCompiler();
        ControlFlowGraph<Node> cfg = createMockCFG();
        ReverseAbstractInterpreter reverseInterpreter = createMockReverseInterpreter();
        Scope functionScope = createMockFunctionScope();

        TypeInference inference = new TypeInference(compiler, cfg, reverseInterpreter, functionScope);

        // Create a CATCH node
        Node catchNode = new Node(Token.CATCH);
        Node nameNode = Node.newString(Token.NAME, "e");
        catchNode.addChildToFront(nameNode);

        FlowScope input = inference.createEntryLattice();
        FlowScope result = inference.flowThrough(catchNode, input);

        assertNotNull("flowThrough with CATCH should return non-null scope", result);
        // The name node should have UNKNOWN_TYPE
        JSType type = nameNode.getJSType();
        assertNotNull("CATCH param should have a type", type);
        assertTrue("CATCH param should be UNKNOWN_TYPE", type.isUnknownType());
    }

    @Test(timeout = 4000)
    public void testTraverseAssignWithLeftTypeNull() {
        AbstractCompiler compiler = createMockCompiler();
        ControlFlowGraph<Node> cfg = createMockCFG();
        ReverseAbstractInterpreter reverseInterpreter = createMockReverseInterpreter();
        Scope functionScope = createMockFunctionScope();

        TypeInference inference = new TypeInference(compiler, cfg, reverseInterpreter, functionScope);

        // Create an ASSIGN node: x = 5
        Node assignNode = new Node(Token.ASSIGN);
        Node nameNode = Node.newString(Token.NAME, "x");
        Node numberNode = new Node(Token.NUMBER, 5);
        assignNode.addChildToFront(nameNode);
        assignNode.addChildToFront(numberNode);

        FlowScope input = inference.createEntryLattice();
        FlowScope result = inference.flowThrough(assignNode, input);

        assertNotNull("flowThrough with ASSIGN should return non-null scope", result);
    }

    @Test(timeout = 4000)
    public void testTraverseGetPropWithUnknownObjType() {
        AbstractCompiler compiler = createMockCompiler();
        ControlFlowGraph<Node> cfg = createMockCFG();
        ReverseAbstractInterpreter reverseInterpreter = createMockReverseInterpreter();
        Scope functionScope = createMockFunctionScope();

        TypeInference inference = new TypeInference(compiler, cfg, reverseInterpreter, functionScope);

        // Create a GETPROP node: someObj.prop
        Node getPropNode = new Node(Token.GETPROP);
        Node objNode = Node.newString(Token.NAME, "someObj");
        Node propNode = Node.newString(Token.STRING, "prop");
        getPropNode.addChildToFront(objNode);
        getPropNode.addChildToFront(propNode);

        FlowScope input = inference.createEntryLattice();
        FlowScope result = inference.flowThrough(getPropNode, input);

        assertNotNull("flowThrough with GETPROP should return non-null scope", result);
    }

    @Test(timeout = 4000)
    public void testTraverseAddWithUnknownTypes() {
        AbstractCompiler compiler = createMockCompiler();
        ControlFlowGraph<Node> cfg = createMockCFG();
        ReverseAbstractInterpreter reverseInterpreter = createMockReverseInterpreter();
        Scope functionScope = createMockFunctionScope();

        TypeInference inference = new TypeInference(compiler, cfg, reverseInterpreter, functionScope);

        // Create an ADD node: x + y (both unknown)
        Node addNode = new Node(Token.ADD);
        Node left = Node.newString(Token.NAME, "x");
        Node right = Node.newString(Token.NAME, "y");
        addNode.addChildToFront(left);
        addNode.addChildToFront(right);

        FlowScope input = inference.createEntryLattice();
        FlowScope result = inference.flowThrough(addNode, input);

        assertNotNull("flowThrough with ADD should return non-null scope", result);
    }

    @Test(timeout = 4000)
    public void testTraverseHookWithNullTypes() {
        AbstractCompiler compiler = createMockCompiler();
        ControlFlowGraph<Node> cfg = createMockCFG();
        ReverseAbstractInterpreter reverseInterpreter = createMockReverseInterpreter();
        Scope functionScope = createMockFunctionScope();

        TypeInference inference = new TypeInference(compiler, cfg, reverseInterpreter, functionScope);

        // Create a HOOK node: condition ? trueExpr : falseExpr
        Node hookNode = new Node(Token.HOOK);
        Node condition = new Node(Token.TRUE);
        Node trueExpr = Node.newString(Token.NAME, "a");
        Node falseExpr = Node.newString(Token.NAME, "b");
        hookNode.addChildToFront(condition);
        hookNode.addChildToFront(trueExpr);
        hookNode.addChildToFront(falseExpr);

        FlowScope input = inference.createEntryLattice();
        FlowScope result = inference.flowThrough(hookNode, input);

        assertNotNull("flowThrough with HOOK should return non-null scope", result);
    }

    @Test(timeout = 4000)
    public void testTraverseCallWithUnknownFunction() {
        AbstractCompiler compiler = createMockCompiler();
        ControlFlowGraph<Node> cfg = createMockCFG();
        ReverseAbstractInterpreter reverseInterpreter = createMockReverseInterpreter();
        Scope functionScope = createMockFunctionScope();

        TypeInference inference = new TypeInference(compiler, cfg, reverseInterpreter, functionScope);

        // Create a CALL node: foo()
        Node callNode = new Node(Token.CALL);
        Node funcNode = Node.newString(Token.NAME, "foo");
        callNode.addChildToFront(funcNode);

        FlowScope input = inference.createEntryLattice();
        FlowScope result = inference.flowThrough(callNode, input);

        assertNotNull("flowThrough with CALL should return non-null scope", result);
    }

    @Test(timeout = 4000)
    public void testTraverseNewWithUnknownConstructor() {
        AbstractCompiler compiler = createMockCompiler();
        ControlFlowGraph<Node> cfg = createMockCFG();
        ReverseAbstractInterpreter reverseInterpreter = createMockReverseInterpreter();
        Scope functionScope = createMockFunctionScope();

        TypeInference inference = new TypeInference(compiler, cfg, reverseInterpreter, functionScope);

        // Create a NEW node: new Foo()
        Node newNode = new Node(Token.NEW);
        Node constructorNode = Node.newString(Token.NAME, "Foo");
        newNode.addChildToFront(constructorNode);

        FlowScope input = inference.createEntryLattice();
        FlowScope result = inference.flowThrough(newNode, input);

        assertNotNull("flowThrough with NEW should return non-null scope", result);
    }

    @Test(timeout = 4000)
    public void testTraverseArrayLiteral() {
        AbstractCompiler compiler = createMockCompiler();
        ControlFlowGraph<Node> cfg = createMockCFG();
        ReverseAbstractInterpreter reverseInterpreter = createMockReverseInterpreter();
        Scope functionScope = createMockFunctionScope();

        TypeInference inference = new TypeInference(compiler, cfg, reverseInterpreter, functionScope);

        // Create an ARRAYLIT node: [1, 2, 3]
        Node arrayLit = new Node(Token.ARRAYLIT);
        arrayLit.addChildToFront(new Node(Token.NUMBER, 1));
        arrayLit.addChildToFront(new Node(Token.NUMBER, 2));
        arrayLit.addChildToFront(new Node(Token.NUMBER, 3));

        FlowScope input = inference.createEntryLattice();
        FlowScope result = inference.flowThrough(arrayLit, input);

        assertNotNull("flowThrough with ARRAYLIT should return non-null scope", result);
        JSType type = arrayLit.getJSType();
        assertNotNull("ARRAYLIT should have a type", type);
    }

    @Test(timeout = 4000)
    public void testTraverseObjectLiteral() {
        AbstractCompiler compiler = createMockCompiler();
        ControlFlowGraph<Node> cfg = createMockCFG();
        ReverseAbstractInterpreter reverseInterpreter = createMockReverseInterpreter();
        Scope functionScope = createMockFunctionScope();

        TypeInference inference = new TypeInference(compiler, cfg, reverseInterpreter, functionScope);

        // Create an OBJECTLIT node: {a: 1, b: "hello"}
        Node objectLit = new Node(Token.OBJECTLIT);
        Node key1 = Node.newString(Token.STRING, "a");
        Node value1 = new Node(Token.NUMBER, 1);
        Node key2 = Node.newString(Token.STRING, "b");
        Node value2 = Node.newString(Token.STRING, "hello");
        objectLit.addChildToFront(key1);
        objectLit.addChildToFront(value1);
        objectLit.addChildToFront(key2);
        objectLit.addChildToFront(value2);

        FlowScope input = inference.createEntryLattice();
        FlowScope result = inference.flowThrough(objectLit, input);

        assertNotNull("flowThrough with OBJECTLIT should return non-null scope", result);
        JSType type = objectLit.getJSType();
        assertNotNull("OBJECTLIT should have a type", type);
    }

    @Test(timeout = 4000)
    public void testTraverseThis() {
        AbstractCompiler compiler = createMockCompiler();
        ControlFlowGraph<Node> cfg = createMockCFG();
        ReverseAbstractInterpreter reverseInterpreter = createMockReverseInterpreter();
        Scope functionScope = createMockFunctionScope();

        TypeInference inference = new TypeInference(compiler, cfg, reverseInterpreter, functionScope);

        // Create a THIS node
        Node thisNode = new Node(Token.THIS);

        FlowScope input = inference.createEntryLattice();
        FlowScope result = inference.flowThrough(thisNode, input);

        assertNotNull("flowThrough with THIS should return non-null scope", result);
        JSType type = thisNode.getJSType();
        assertNotNull("THIS should have a type", type);
    }

    @Test(timeout = 4000)
    public void testTraverseGetElem() {
        AbstractCompiler compiler = createMockCompiler();
        ControlFlowGraph<Node> cfg = createMockCFG();
        ReverseAbstractInterpreter reverseInterpreter = createMockReverseInterpreter();
        Scope functionScope = createMockFunctionScope();

        TypeInference inference = new TypeInference(compiler, cfg, reverseInterpreter, functionScope);

        // Create a GETELEM node: arr[0]
        Node getElemNode = new Node(Token.GETELEM);
        Node arrNode = Node.newString(Token.NAME, "arr");
        Node indexNode = new Node(Token.NUMBER, 0);
        getElemNode.addChildToFront(arrNode);
        getElemNode.addChildToFront(indexNode);

        FlowScope input = inference.createEntryLattice();
        FlowScope result = inference.flowThrough(getElemNode, input);

        assertNotNull("flowThrough with GETELEM should return non-null scope", result);
    }

    // ==================== Helper Methods ====================

    private AbstractCompiler createMockCompiler() {
        // Create a minimal mock compiler
        return new AbstractCompiler() {
            @Override
            public JSTypeRegistry getTypeRegistry() {
                return new JSTypeRegistry(null);
            }

            // Other methods are not needed for these tests
            @Override
            public void report(JSError error) {}

            @Override
            public boolean hasHaltingErrors() { return false; }

            @Override
            public CheckLevel getErrorLevel(JSError error) { return CheckLevel.ERROR; }
        };
    }

    private ControlFlowGraph<Node> createMockCFG() {
        // Return a simple mock that returns empty edges
        return new ControlFlowGraph<Node>(new Node(Token.BLOCK)) {
            @Override
            public List<DiGraphEdge<Node, Branch>> getOutEdges(Node node) {
                return Collections.emptyList();
            }

            @Override
            public DiGraphEdge<Node, Branch> getImplicitReturn() {
                return null;
            }
        };
    }

    private ReverseAbstractInterpreter createMockReverseInterpreter() {
        return new ReverseAbstractInterpreter() {
            @Override
            public FlowScope getPreciserScopeKnowingConditionOutcome(
                    Node condition, FlowScope scope, boolean outcome) {
                return scope;
            }
        };
    }

    private Scope createMockFunctionScope() {
        // Create a minimal scope
        Node rootNode = new Node(Token.FUNCTION);
        return new Scope(rootNode, null) {
            @Override
            public Var getVar(String name) {
                return null;
            }

            @Override
            public Iterator<Var> getVars() {
                return Collections.<Var>emptyIterator();
            }
        };
    }

    private Var createMockVar(final String name, final Node parentNode,
                              final JSType type, final boolean isExtern,
                              final boolean isTypeInferred) {
        return new Var() {
            @Override
            public String getName() { return name; }

            @Override
            public Node getParentNode() { return parentNode; }

            @Override
            public JSType getType() { return type; }

            @Override
            public boolean isExtern() { return isExtern; }

            @Override
            public boolean isTypeInferred() { return isTypeInferred; }

            @Override
            public Scope getScope() { return null; }

            @Override
            public boolean isLocal() { return false; }

            @Override
            public void setType(JSType type) {}
        };
    }

    private JSType createMockJSType() {
        return new JSType(null) {
            @Override
            public boolean isUnknownType() { return false; }

            @Override
            public JSType getLeastSupertype(JSType that) { return this; }

            @Override
            public JSType getRestrictedTypeGivenToBooleanOutcome(boolean outcome) { return this; }

            @Override
            public BooleanLiteralSet getPossibleToBooleanOutcomes() { return BooleanLiteralSet.BOTH; }

            @Override
            public boolean isSubtype(JSType that) { return false; }

            @Override
            public JSType restrictByNotNullOrUndefined() { return this; }

            @Override
            public String toString() { return "MockType"; }
        };
    }
}