/* [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Target Class: com.google.javascript.jscomp.Scope & com.google.javascript.jscomp.Scope.Var
 *
 * 1. Scope Constructors:
 *    - Scope(Scope parent, Node rootNode):
 *      - Branch: parent == null -> NullPointerException (Preconditions.checkNotNull)
 *      - Branch: rootNode == parent.rootNode -> IllegalArgumentException (Preconditions.checkArgument)
 *      - Branch: rootNode.getJSType() is FunctionType -> thisType = ((FunctionType) nodeType).getTypeOfThis()
 *      - Branch: rootNode.getJSType() is non-FunctionType -> thisType = parent.thisType
 *      - Branch: rootNode.getJSType() == null -> thisType = parent.thisType
 *      - State: depth = parent.depth + 1, isBottom = false
 *    - Scope(Node rootNode, AbstractCompiler compiler):
 *      - State: parent = null, depth = 0, isBottom = false, thisType = compiler.getTypeRegistry().getNativeObjectType(GLOBAL_THIS)
 *    - Scope(Node rootNode, ObjectType thisType):
 *      - State: parent = null, depth = 0, isBottom = true, thisType = thisType
 *
 * 2. Scope Query & Traversal Methods:
 *    - getDepth(), isBottom(), getRootNode(), getParent(), getParentScope(), getTypeOfThis()
 *    - getGlobalScope():
 *      - Loop: while (result.getParent() != null)
 *      - Branch: invoked on global scope (parent == null) -> returns this
 *      - Branch: invoked on nested / grandchild scope -> returns root global Scope
 *    - isGlobal(), isLocal(): parent == null vs parent != null
 *
 * 3. Variable Declaration & Undeclaration:
 *    - declare(String name, Node nameNode, JSType type, CompilerInput input) [delegates with inferred = true]
 *    - declare(String name, Node nameNode, JSType type, CompilerInput input, boolean inferred):
 *      - Branch: name == null -> IllegalStateException
 *      - Branch: name.isEmpty() -> IllegalStateException
 *      - Branch: vars.get(name) != null (duplicate declaration) -> IllegalStateException
 *      - Branch: JSDocInfo is present vs absent on nameNode
 *    - undeclare(Var var):
 *      - Branch: var.scope != this -> IllegalStateException
 *      - Branch: vars.get(var.name) != var (already removed or foreign) -> IllegalStateException
 *      - Removal: vars.remove(var.name)
 *
 * 4. Slot & Scope Variable Resolution:
 *    - getSlot(name), getOwnSlot(name)
 *    - getVar(name):
 *      - Branch: found in current scope -> returns Var
 *      - Branch: not found & parent != null -> recurses up parent hierarchy
 *      - Branch: not found & parent == null -> returns null
 *    - isDeclared(name, recurse):
 *      - Branch: vars.containsKey(name) -> returns true
 *      - Branch: !vars.containsKey(name) & parent != null & recurse == true -> recurses up parent
 *      - Branch: !vars.containsKey(name) & parent != null & recurse == false -> returns false
 *      - Branch: !vars.containsKey(name) & parent == null -> returns false
 *    - getVars(): returns Iterator over vars in insertion order
 *    - getVarCount(): returns vars.size()
 *
 * 5. Scope.Var Logic:
 *    - getName(), getScope(), getParentNode(), getNameNode(), getJSDocInfo(), getType(), isTypeInferred()
 *    - isBleedingFunction():
 *      - Branch: NodeUtil.isFunctionExpression(getParentNode()) is true vs false
 *      - Branch: nameNode == null -> getParentNode() == null -> false
 *    - isGlobal(), isLocal(): delegates to scope.isGlobal(), scope.isLocal()
 *    - isExtern():
 *      - Branch: input == null -> true
 *      - Branch: input != null && input.isExtern() == true -> true
 *      - Branch: input != null && input.isExtern() == false -> false
 *    - isConst():
 *      - Branch: nameNode == null -> false
 *      - Branch: nameNode != null && NodeUtil.isConstantName(nameNode) is true vs false
 *    - isDefine(): returns isDefine flag based on JSDocInfo.isDefine()
 *    - getInitialValue():
 *      - Branch: parent.getType() == Token.FUNCTION -> returns parent
 *      - Branch: parent.getType() == Token.ASSIGN -> returns parent.getLastChild()
 *      - Branch: parent.getType() == Token.VAR -> returns nameNode.getFirstChild()
 *      - Branch: other parent type (e.g. PARAM_LIST) -> returns null
 *      - Branch: parent == null -> NullPointerException (edge/defensive check)
 *    - setType(JSType):
 *      - Branch: isTypeInferred() == true -> sets type
 *      - Branch: isTypeInferred() == false -> IllegalStateException
 *    - resolveType(ErrorReporter):
 *      - Branch: type != null -> type.resolve(reporter, scope)
 *      - Branch: type == null -> no-op
 *    - getInputName():
 *      - Branch: input == null -> "<non-file>"
 *      - Branch: input != null -> input.getName()
 *    - isNoShadow():
 *      - Branch: info != null && info.isNoShadow() == true -> true
 *      - Branch: info != null && info.isNoShadow() == false -> false
 *      - Branch: info == null -> false
 *    - equals(Object), hashCode(), toString():
 *      - Branch: other !(instanceof Var) -> false
 *      - Branch: otherVar.nameNode == nameNode -> true vs false
 *
 * 6. Defect-Targeted Ground Truth:
 *    - InlineVariablesTest::testArgumentsModifiedInInnerFunction / testIssue378ModifiedArguments
 *      Scope variable resolution for "arguments" across outer function, inner function,
 *      and parameter shadowing boundaries.
 * ====================================================================================================
 */

package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSDocInfoBuilder;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SimpleErrorReporter;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.rhino.jstype.StaticSlot;

import java.util.Iterator;
import org.junit.Test;

public class ScopeGeminiTest {

  // ==================================================================================================
  // PARTITION A: Core Functional Logic & State Transitions
  // ==================================================================================================

  @Test(timeout = 4000)
  public void testGlobalScopeCreationAndProperties() {
    Compiler compiler = new Compiler();
    Node root = new Node(Token.BLOCK);
    Scope globalScope = new Scope(root, compiler);

    assertTrue(globalScope.isGlobal());
    assertFalse(globalScope.isLocal());
    assertFalse(globalScope.isBottom());
    assertEquals(0, globalScope.getDepth());
    assertEquals(root, globalScope.getRootNode());
    assertNull(globalScope.getParent());
    assertNull(globalScope.getParentScope());
    assertEquals(globalScope, globalScope.getGlobalScope());
    assertNotNull(globalScope.getTypeOfThis());
    assertEquals(0, globalScope.getVarCount());
  }

  @Test(timeout = 4000)
  public void testNestedScopeHierarchyAndDepth() {
    Compiler compiler = new Compiler();
    Node globalRoot = new Node(Token.BLOCK);
    Scope globalScope = new Scope(globalRoot, compiler);

    Node fnRoot1 = new Node(Token.FUNCTION, Node.newString(Token.NAME, "fn1"),
        new Node(Token.PARAM_LIST), new Node(Token.BLOCK));
    Scope childScope = new Scope(globalScope, fnRoot1);

    Node fnRoot2 = new Node(Token.FUNCTION, Node.newString(Token.NAME, "fn2"),
        new Node(Token.PARAM_LIST), new Node(Token.BLOCK));
    Scope grandChildScope = new Scope(childScope, fnRoot2);

    assertEquals(0, globalScope.getDepth());
    assertEquals(1, childScope.getDepth());
    assertEquals(2, grandChildScope.getDepth());

    assertFalse(childScope.isGlobal());
    assertTrue(childScope.isLocal());
    assertFalse(grandChildScope.isGlobal());
    assertTrue(grandChildScope.isLocal());

    assertEquals(globalScope, childScope.getParent());
    assertEquals(globalScope, childScope.getParentScope());
    assertEquals(childScope, grandChildScope.getParent());
    assertEquals(childScope, grandChildScope.getParentScope());

    assertEquals(globalScope, grandChildScope.getGlobalScope());
    assertEquals(globalScope, childScope.getGlobalScope());
  }

  @Test(timeout = 4000)
  public void testChildScopeInheritsThisTypeFromParentWhenNotFunctionType() {
    Compiler compiler = new Compiler();
    Node globalRoot = new Node(Token.BLOCK);
    Scope globalScope = new Scope(globalRoot, compiler);

    // rootNode with no JSType
    Node childRoot1 = new Node(Token.BLOCK);
    Scope childScope1 = new Scope(globalScope, childRoot1);
    assertEquals(globalScope.getTypeOfThis(), childScope1.getTypeOfThis());

    // rootNode with non-FunctionType JSType (e.g. NUMBER_TYPE)
    Node childRoot2 = new Node(Token.BLOCK);
    JSType numberType = compiler.getTypeRegistry().getNativeType(JSTypeNative.NUMBER_TYPE);
    childRoot2.setJSType(numberType);
    Scope childScope2 = new Scope(globalScope, childRoot2);
    assertEquals(globalScope.getTypeOfThis(), childScope2.getTypeOfThis());
  }

  @Test(timeout = 4000)
  public void testChildScopeExtractsThisTypeFromFunctionType() {
    Compiler compiler = new Compiler();
    Node globalRoot = new Node(Token.BLOCK);
    Scope globalScope = new Scope(globalRoot, compiler);

    Node fnRoot = new Node(Token.FUNCTION, Node.newString(Token.NAME, "f"),
        new Node(Token.PARAM_LIST), new Node(Token.BLOCK));
    FunctionType fnType = compiler.getTypeRegistry().createFunctionType(
        compiler.getTypeRegistry().getNativeType(JSTypeNative.NUMBER_TYPE),
        new Node(Token.PARAM_LIST));
    fnRoot.setJSType(fnType);

    Scope fnScope = new Scope(globalScope, fnRoot);
    assertEquals(fnType.getTypeOfThis(), fnScope.getTypeOfThis());
  }

  @Test(timeout = 4000)
  public void testDeclareAndRetrieveVariables() {
    Compiler compiler = new Compiler();
    Node root = new Node(Token.BLOCK);
    Scope scope = new Scope(root, compiler);

    Node xNode = Node.newString(Token.NAME, "x");
    CompilerInput input = new CompilerInput(new SyntheticAst("input.js"), false);
    JSType numType = compiler.getTypeRegistry().getNativeType(JSTypeNative.NUMBER_TYPE);

    Scope.Var varX = scope.declare("x", xNode, numType, input);
    assertNotNull(varX);
    assertEquals("x", varX.getName());
    assertEquals(xNode, varX.getNameNode());
    assertEquals(numType, varX.getType());
    assertEquals(scope, varX.getScope());
    assertEquals(0, varX.index);
    assertEquals("input.js", varX.getInputName());
    assertFalse(varX.isExtern());
    assertTrue(varX.isTypeInferred());
    assertTrue(varX.isGlobal());
    assertFalse(varX.isLocal());

    assertEquals(1, scope.getVarCount());
    assertEquals(varX, scope.getVar("x"));
    assertEquals(varX, scope.getSlot("x"));
    assertEquals(varX, scope.getOwnSlot("x"));

    // Second declaration to test index incrementation
    Node yNode = Node.newString(Token.NAME, "y");
    Scope.Var varY = scope.declare("y", yNode, null, null, false);
    assertEquals(1, varY.index);
    assertFalse(varY.isTypeInferred());
    assertEquals(2, scope.getVarCount());

    Iterator<Scope.Var> iterator = scope.getVars();
    assertTrue(iterator.hasNext());
    assertEquals(varX, iterator.next());
    assertTrue(iterator.hasNext());
    assertEquals(varY, iterator.next());
    assertFalse(iterator.hasNext());
  }

  @Test(timeout = 4000)
  public void testUndeclareVariable() {
    Compiler compiler = new Compiler();
    Scope scope = new Scope(new Node(Token.BLOCK), compiler);

    Node nodeA = Node.newString(Token.NAME, "a");
    Scope.Var varA = scope.declare("a", nodeA, null, null);
    assertEquals(1, scope.getVarCount());
    assertEquals(varA, scope.getVar("a"));

    scope.undeclare(varA);
    assertEquals(0, scope.getVarCount());
    assertNull(scope.getVar("a"));
    assertNull(scope.getOwnSlot("a"));
    assertNull(scope.getSlot("a"));
  }

  @Test(timeout = 4000)
  public void testIsDeclaredWithAndWithoutRecursion() {
    Compiler compiler = new Compiler();
    Scope global = new Scope(new Node(Token.BLOCK), compiler);
    Node fnRoot = new Node(Token.FUNCTION, Node.newString(Token.NAME, "child"),
        new Node(Token.PARAM_LIST), new Node(Token.BLOCK));
    Scope local = new Scope(global, fnRoot);

    global.declare("globalVar", Node.newString(Token.NAME, "globalVar"), null, null);
    local.declare("localVar", Node.newString(Token.NAME, "localVar"), null, null);

    // Current scope variable
    assertTrue(local.isDeclared("localVar", false));
    assertTrue(local.isDeclared("localVar", true));

    // Parent scope variable
    assertFalse(local.isDeclared("globalVar", false));
    assertTrue(local.isDeclared("globalVar", true));

    // Undeclared variable
    assertFalse(local.isDeclared("unknownVar", false));
    assertFalse(local.isDeclared("unknownVar", true));
    assertFalse(global.isDeclared("unknownVar", true));
    assertFalse(global.isDeclared("unknownVar", false));
  }

  @Test(timeout = 4000)
  public void testGetVarTraversesParentHierarchy() {
    Compiler compiler = new Compiler();
    Scope global = new Scope(new Node(Token.BLOCK), compiler);
    Node childRoot = new Node(Token.FUNCTION, Node.newString(Token.NAME, "c"),
        new Node(Token.PARAM_LIST), new Node(Token.BLOCK));
    Scope child = new Scope(global, childRoot);
    Node grandChildRoot = new Node(Token.FUNCTION, Node.newString(Token.NAME, "gc"),
        new Node(Token.PARAM_LIST), new Node(Token.BLOCK));
    Scope grandchild = new Scope(child, grandChildRoot);

    Scope.Var gVar = global.declare("g", Node.newString(Token.NAME, "g"), null, null);
    Scope.Var cVar = child.declare("c", Node.newString(Token.NAME, "c"), null, null);

    // grandchild sees own, parent, and global
    assertEquals(gVar, grandchild.getVar("g"));
    assertEquals(cVar, grandchild.getVar("c"));
    assertNull(grandchild.getOwnSlot("g"));
    assertNull(grandchild.getOwnSlot("c"));
    assertNull(grandchild.getVar("nonExistent"));

    // global doesn't see child
    assertNull(global.getVar("c"));
  }

  // ==================================================================================================
  // PARTITION B: Boundary Value Analysis (BVA) & Extremes
  // ==================================================================================================

  @Test(timeout = 4000)
  public void testBottomScopeLatticeElement() {
    Node root = new Node(Token.BLOCK);
    Scope bottomScope = new Scope(root, (ObjectType) null);

    assertTrue(bottomScope.isBottom());
    assertTrue(bottomScope.isGlobal());
    assertFalse(bottomScope.isLocal());
    assertEquals(0, bottomScope.getDepth());
    assertEquals(root, bottomScope.getRootNode());
    assertNull(bottomScope.getTypeOfThis());
    assertNull(bottomScope.getParent());
    assertEquals(0, bottomScope.getVarCount());
  }

  @Test(timeout = 4000)
  public void testVarWithNullInputAndNullNameNode() {
    Compiler compiler = new Compiler();
    Scope scope = new Scope(new Node(Token.BLOCK), compiler);

    Scope.Var nativeVar = scope.declare("nativeVar", null, null, null);
    assertNull(nativeVar.getNameNode());
    assertNull(nativeVar.getParentNode());
    assertEquals("<non-file>", nativeVar.getInputName());
    assertTrue(nativeVar.isExtern());
    assertFalse(nativeVar.isConst());
    assertFalse(nativeVar.isBleedingFunction());
    assertFalse(nativeVar.isNoShadow());
    assertFalse(nativeVar.isDefine());
    assertNull(nativeVar.getJSDocInfo());
  }

  @Test(timeout = 4000)
  public void testVarExternInputBoundary() {
    Compiler compiler = new Compiler();
    Scope scope = new Scope(new Node(Token.BLOCK), compiler);

    CompilerInput externInput = new CompilerInput(new SyntheticAst("extern.js"), true);
    Scope.Var externVar = scope.declare("ext", Node.newString(Token.NAME, "ext"), null, externInput);
    assertTrue(externVar.isExtern());
    assertEquals("extern.js", externVar.getInputName());

    CompilerInput normalInput = new CompilerInput(new SyntheticAst("normal.js"), false);
    Scope.Var normalVar = scope.declare("norm", Node.newString(Token.NAME, "norm"), null, normalInput);
    assertFalse(normalVar.isExtern());
    assertEquals("normal.js", normalVar.getInputName());
  }

  @Test(timeout = 4000)
  public void testVarIsConstBranches() {
    Compiler compiler = new Compiler();
    Scope scope = new Scope(new Node(Token.BLOCK), compiler);

    Node constNode = Node.newString(Token.NAME, "CONSTANT_NAME");
    Scope.Var constVar = scope.declare("CONSTANT_NAME", constNode, null, null);
    assertTrue(constVar.isConst());

    Node regularNode = Node.newString(Token.NAME, "regularName");
    Scope.Var regVar = scope.declare("regularName", regularNode, null, null);
    assertFalse(regVar.isConst());
  }

  @Test(timeout = 4000)
  public void testVarInitialValueBranches() {
    Compiler compiler = new Compiler();
    Scope scope = new Scope(new Node(Token.BLOCK), compiler);

    // 1. Parent is Token.FUNCTION
    Node fnName = Node.newString(Token.NAME, "fnVar");
    Node fnNode = new Node(Token.FUNCTION, fnName, new Node(Token.PARAM_LIST), new Node(Token.BLOCK));
    Scope.Var fnVar = scope.declare("fnVar", fnName, null, null);
    assertEquals(fnNode, fnVar.getInitialValue());

    // 2. Parent is Token.ASSIGN
    Node assignName = Node.newString(Token.NAME, "assignedVar");
    Node valueNode = Node.newNumber(42.0);
    new Node(Token.ASSIGN, assignName, valueNode);
    Scope.Var assignVar = scope.declare("assignedVar", assignName, null, null);
    assertEquals(valueNode, assignVar.getInitialValue());

    // 3. Parent is Token.VAR
    Node varName = Node.newString(Token.NAME, "declaredVar");
    Node initVal = Node.newString("val");
    varName.addChildToBack(initVal);
    new Node(Token.VAR, varName);
    Scope.Var declaredVar = scope.declare("declaredVar", varName, null, null);
    assertEquals(initVal, declaredVar.getInitialValue());

    // 4. Parent is Token.PARAM_LIST (other type)
    Node paramName = Node.newString(Token.NAME, "paramVar");
    new Node(Token.PARAM_LIST, paramName);
    Scope.Var paramVar = scope.declare("paramVar", paramName, null, null);
    assertNull(paramVar.getInitialValue());
  }

  @Test(timeout = 4000)
  public void testVarIsBleedingFunctionBranches() {
    Compiler compiler = new Compiler();
    Scope global = new Scope(new Node(Token.BLOCK), compiler);

    // Function expression assigned to a variable -> bleeding function name
    Node fnName = Node.newString(Token.NAME, "bleedingFn");
    Node fnNode = new Node(Token.FUNCTION, fnName, new Node(Token.PARAM_LIST), new Node(Token.BLOCK));
    new Node(Token.ASSIGN, Node.newString(Token.NAME, "target"), fnNode);

    Scope fnScope = new Scope(global, fnNode);
    Scope.Var bleedingVar = fnScope.declare("bleedingFn", fnName, null, null);
    assertTrue(bleedingVar.isBleedingFunction());
    assertTrue(bleedingVar.isLocal());
    assertFalse(bleedingVar.isGlobal());

    // Normal VAR declaration -> not a bleeding function
    Node normName = Node.newString(Token.NAME, "normVar");
    new Node(Token.VAR, normName);
    Scope.Var normVar = fnScope.declare("normVar", normName, null, null);
    assertFalse(normVar.isBleedingFunction());
  }

  @Test(timeout = 4000)
  public void testVarNoShadowAndJSDocBranches() {
    Compiler compiler = new Compiler();
    Scope scope = new Scope(new Node(Token.BLOCK), compiler);

    // 1. info == null
    Node noInfoNode = Node.newString(Token.NAME, "noInfo");
    Scope.Var noInfoVar = scope.declare("noInfo", noInfoNode, null, null);
    assertFalse(noInfoVar.isNoShadow());
    assertNull(noInfoVar.getJSDocInfo());

    // 2. info != null && info.isNoShadow() == false
    JSDocInfoBuilder builderWithoutNoShadow = new JSDocInfoBuilder(false);
    JSDocInfo infoWithoutNoShadow = builderWithoutNoShadow.build(null);
    Node withInfoNode = Node.newString(Token.NAME, "withInfo");
    withInfoNode.setJSDocInfo(infoWithoutNoShadow);
    Scope.Var withInfoVar = scope.declare("withInfo", withInfoNode, null, null);
    assertFalse(withInfoVar.isNoShadow());
    assertEquals(infoWithoutNoShadow, withInfoVar.getJSDocInfo());

    // 3. info != null && info.isNoShadow() == true
    JSDocInfoBuilder builderWithNoShadow = new JSDocInfoBuilder(false);
    builderWithNoShadow.recordNoShadow();
    JSDocInfo infoWithNoShadow = builderWithNoShadow.build(null);
    Node noShadowNode = Node.newString(Token.NAME, "noShadow");
    noShadowNode.setJSDocInfo(infoWithNoShadow);
    Scope.Var noShadowVar = scope.declare("noShadow", noShadowNode, null, null);
    assertTrue(noShadowVar.isNoShadow());
    assertEquals(infoWithNoShadow, noShadowVar.getJSDocInfo());
  }

  @Test(timeout = 4000)
  public void testVarSetTypeAndResolveType() {
    Compiler compiler = new Compiler();
    Scope scope = new Scope(new Node(Token.BLOCK), compiler);

    JSType numType = compiler.getTypeRegistry().getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType strType = compiler.getTypeRegistry().getNativeType(JSTypeNative.STRING_TYPE);

    // Var with inferred type can have setType called
    Node infNode = Node.newString(Token.NAME, "inferred");
    Scope.Var inferredVar = scope.declare("inferred", infNode, numType, null, true);
    assertEquals(numType, inferredVar.getType());
    inferredVar.setType(strType);
    assertEquals(strType, inferredVar.getType());

    // Resolve type when type != null
    ErrorReporter reporter = new SimpleErrorReporter();
    inferredVar.resolveType(reporter);
    assertEquals(strType, inferredVar.getType());

    // Resolve type when type == null
    Node nullTypeNode = Node.newString(Token.NAME, "nullType");
    Scope.Var nullTypeVar = scope.declare("nullType", nullTypeNode, null, null, true);
    assertNull(nullTypeVar.getType());
    nullTypeVar.resolveType(reporter);
    assertNull(nullTypeVar.getType());
  }

  // ==================================================================================================
  // PARTITION C: Defect-Targeted Branch Zone (Arguments Handling & Scoping)
  // ==================================================================================================

  /**
   * Targets defects documented in InlineVariablesTest (testArgumentsModifiedInInnerFunction,
   * testArgumentsModifiedInOuterFunction, testIssue378ModifiedArguments1/2, testIssue378EscapedArguments):
   * Tests lexical scoping and shadowing integrity of "arguments" across outer and inner function scopes.
   */
  @Test(timeout = 4000)
  public void testArgumentsVariableShadowingInNestedFunctionScopes() {
    Compiler compiler = new Compiler();
    Scope globalScope = new Scope(new Node(Token.BLOCK), compiler);

    // Outer function f(a)
    Node outerFn = new Node(Token.FUNCTION, Node.newString(Token.NAME, "f"),
        new Node(Token.PARAM_LIST), new Node(Token.BLOCK));
    Scope outerScope = new Scope(globalScope, outerFn);

    // Implicit/explicit "arguments" in outer function
    Node outerArgsNode = Node.newString(Token.NAME, "arguments");
    Scope.Var outerArgs = outerScope.declare("arguments", outerArgsNode, null, null);

    // Inner function g()
    Node innerFn = new Node(Token.FUNCTION, Node.newString(Token.NAME, "g"),
        new Node(Token.PARAM_LIST), new Node(Token.BLOCK));
    Scope innerScope = new Scope(outerScope, innerFn);

    // Before inner function defines its own "arguments", it should resolve to outer arguments
    assertTrue(innerScope.isDeclared("arguments", true));
    assertFalse(innerScope.isDeclared("arguments", false));
    assertEquals(outerArgs, innerScope.getVar("arguments"));
    assertEquals(outerArgs, outerScope.getVar("arguments"));

    // When inner function declares its own "arguments"
    Node innerArgsNode = Node.newString(Token.NAME, "arguments");
    Scope.Var innerArgs = innerScope.declare("arguments", innerArgsNode, null, null);

    // Shadowing must occur cleanly: inner scope sees innerArgs, outer scope sees outerArgs
    assertTrue(innerScope.isDeclared("arguments", false));
    assertTrue(innerScope.isDeclared("arguments", true));
    assertEquals(innerArgs, innerScope.getVar("arguments"));
    assertEquals(innerArgs, innerScope.getOwnSlot("arguments"));
    assertEquals(outerArgs, outerScope.getVar("arguments"));
    assertEquals(outerArgs, outerScope.getOwnSlot("arguments"));
    assertNotEquals(innerArgs, outerArgs);
  }

  @Test(timeout = 4000)
  public void testArgumentsModificationAndUndeclareIntegrity() {
    Compiler compiler = new Compiler();
    Scope globalScope = new Scope(new Node(Token.BLOCK), compiler);

    Node fnRoot = new Node(Token.FUNCTION, Node.newString(Token.NAME, "outer"),
        new Node(Token.PARAM_LIST), new Node(Token.BLOCK));
    Scope fnScope = new Scope(globalScope, fnRoot);

    Node argsNode = Node.newString(Token.NAME, "arguments");
    Scope.Var argsVar = fnScope.declare("arguments", argsNode, null, null);

    // Parameter 'b' declared after arguments
    Node bNode = Node.newString(Token.NAME, "b");
    Scope.Var bVar = fnScope.declare("b", bNode, null, null);
    assertEquals(0, argsVar.index);
    assertEquals(1, bVar.index);

    // Removing arguments allows re-declaring without corrupting scope state
    fnScope.undeclare(argsVar);
    assertNull(fnScope.getVar("arguments"));
    assertFalse(fnScope.isDeclared("arguments", false));

    Node newArgsNode = Node.newString(Token.NAME, "arguments");
    Scope.Var reDeclaredArgs = fnScope.declare("arguments", newArgsNode, null, null);
    assertEquals(reDeclaredArgs, fnScope.getVar("arguments"));
    assertEquals(1, reDeclaredArgs.index); // index matches new size - 1
  }

  // ==================================================================================================
  // PARTITION D: Exception & Defensive Guard Paths
  // ==================================================================================================

  @Test(expected = NullPointerException.class, timeout = 4000)
  public void testChildScopeConstructorWithNullParentThrowsException() {
    new Scope(null, new Node(Token.FUNCTION));
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testChildScopeWithSameRootNodeAsParentThrowsException() {
    Compiler compiler = new Compiler();
    Node sharedRoot = new Node(Token.BLOCK);
    Scope parent = new Scope(sharedRoot, compiler);
    new Scope(parent, sharedRoot);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testDeclareWithNullNameThrowsException() {
    Compiler compiler = new Compiler();
    Scope scope = new Scope(new Node(Token.BLOCK), compiler);
    scope.declare(null, Node.newString(Token.NAME, "x"), null, null);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testDeclareWithEmptyNameThrowsException() {
    Compiler compiler = new Compiler();
    Scope scope = new Scope(new Node(Token.BLOCK), compiler);
    scope.declare("", Node.newString(Token.NAME, "x"), null, null);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testDeclareDuplicateVariableThrowsException() {
    Compiler compiler = new Compiler();
    Scope scope = new Scope(new Node(Token.BLOCK), compiler);
    scope.declare("duplicate", Node.newString(Token.NAME, "duplicate"), null, null);
    scope.declare("duplicate", Node.newString(Token.NAME, "duplicate"), null, null);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testUndeclareVarFromDifferentScopeThrowsException() {
    Compiler compiler = new Compiler();
    Scope globalScope = new Scope(new Node(Token.BLOCK), compiler);
    Node childRoot = new Node(Token.FUNCTION, Node.newString(Token.NAME, "fn"),
        new Node(Token.PARAM_LIST), new Node(Token.BLOCK));
    Scope childScope = new Scope(globalScope, childRoot);

    Scope.Var childVar = childScope.declare("childVar", Node.newString(Token.NAME, "childVar"), null, null);
    globalScope.undeclare(childVar);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testUndeclareNonExistentVarThrowsException() {
    Compiler compiler = new Compiler();
    Scope scope = new Scope(new Node(Token.BLOCK), compiler);
    Scope.Var varA = scope.declare("a", Node.newString(Token.NAME, "a"), null, null);
    scope.undeclare(varA);
    // Attempting to undeclare already undeclared variable
    scope.undeclare(varA);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testSetTypeOnDeclaredVariableThrowsException() {
    Compiler compiler = new Compiler();
    Scope scope = new Scope(new Node(Token.BLOCK), compiler);
    JSType numType = compiler.getTypeRegistry().getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType strType = compiler.getTypeRegistry().getNativeType(JSTypeNative.STRING_TYPE);

    Scope.Var declaredVar = scope.declare("decl", Node.newString(Token.NAME, "decl"), numType, null, false);
    declaredVar.setType(strType);
  }

  @Test(expected = NullPointerException.class, timeout = 4000)
  public void testGetInitialValueThrowsWhenParentIsNull() {
    Compiler compiler = new Compiler();
    Scope scope = new Scope(new Node(Token.BLOCK), compiler);
    Node orphanNode = Node.newString(Token.NAME, "orphan");
    Scope.Var var = scope.declare("orphan", orphanNode, null, null);
    var.getInitialValue();
  }

  // ==================================================================================================
  // PARTITION E: Object Lifecycle & Contract Integrity (equals, hashCode, toString)
  // ==================================================================================================

  @Test(timeout = 4000)
  public void testVarEqualsAndHashCodeContract() {
    Compiler compiler = new Compiler();
    Scope scope = new Scope(new Node(Token.BLOCK), compiler);

    Node n1 = Node.newString(Token.NAME, "var1");
    Node n2 = Node.newString(Token.NAME, "var2");

    Scope.Var v1 = scope.declare("var1", n1, null, null);
    Scope.Var v2 = scope.declare("var2", n2, null, null);

    // Reflexive
    assertTrue(v1.equals(v1));
    assertEquals(v1.hashCode(), v1.hashCode());
    assertEquals(n1.hashCode(), v1.hashCode());

    // Symmetric & Distinct
    assertFalse(v1.equals(v2));
    assertFalse(v2.equals(v1));

    // Non-Var instances
    assertFalse(v1.equals(null));
    assertFalse(v1.equals("A String Object"));
    assertFalse(v1.equals(new Object()));
  }

  @Test(timeout = 4000)
  public void testVarToString() {
    Compiler compiler = new Compiler();
    Scope scope = new Scope(new Node(Token.BLOCK), compiler);
    Scope.Var myVar = scope.declare("myVar", Node.newString(Token.NAME, "myVar"), null, null);
    assertEquals("Scope.Var myVar", myVar.toString());
  }
}