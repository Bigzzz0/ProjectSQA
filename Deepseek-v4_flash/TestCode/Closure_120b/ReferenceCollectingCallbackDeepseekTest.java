package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.rhino.InputId;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import java.util.Iterator;

public class ReferenceCollectingCallbackDeepseekTest {

  /* [Branch & Defect Analysis Matrix]
   *
   * Target class: ReferenceCollectingCallback (inner classes: ReferenceCollection, Reference, BasicBlock)
   *
   * Known defect (Defects4J #1053): isWellDefined() returns true when a variable is used
   * before its assignment within the same basic block. The method only checks that the
   * init block provably executes before each reference block; it does not enforce position
   * order inside the same block.
   *
   * Test partitions:
   * A. Core reference collection operations (add, get, iterate)
   * B. isWellDefined() – empty, single var decl, init assignment, use-before-assign
   * C. isEscaped() – same scope vs different scopes
   * D. getInitializingReference() – var with init, var without, assignment after decl, use-before
   * E. isAssignedOnceInLifetime() / getOneAndOnlyAssignment()
   * F. isNeverAssigned()
   * G. firstReferenceIsAssigningDeclaration()
   * H. BasicBlock.provablyExecutesBefore() – same, parent, hoisted, disjoint
   * I. BasicBlock.isGlobalScopeBlock()
   * J. Reference class: isDeclaration, isVarDeclaration, isInitializingDeclaration,
   *    isSimpleAssignmentToName, isLvalue, getAssignedValue, cloneWithNewScope
   * K. Defect-revealing test: use-before-assignment in same block
   */

  // Helper to create a simple Root node for BasicBlock
  private Node createRootNode(int type) {
    return new Node(type);
  }

  // Helper to create a var declaration node (e.g., "var x;")
  private Node createVarDeclNoInit(String name) {
    Node var = new Node(Token.VAR);
    Node nameNode = Node.newString(Token.NAME, name);
    var.addChildToBack(nameNode);
    return var;
  }

  // Helper to create a var declaration with initialization (e.g., "var x = 1;")
  private Node createVarDeclWithInit(String name) {
    Node var = new Node(Token.VAR);
    Node nameNode = Node.newString(Token.NAME, name);
    Node expr = Node.newNumber(1);
    nameNode.addChildToBack(expr);
    var.addChildToBack(nameNode);
    return var;
  }

  // Helper to create an assignment node (e.g., "x = 1;")
  private Node createAssignment(String name, Node value) {
    Node assign = new Node(Token.ASSIGN);
    Node nameNode = Node.newString(Token.NAME, name);
    assign.addChildToFront(nameNode);
    assign.addChildToBack(value);
    return assign;
  }

  // Helper to create a simple Name reference (use)
  private Node createNameNode(String name) {
    return Node.newString(Token.NAME, name);
  }

  // Helper to create a BasicBlock with given parent and isFunction/isLoop flags
  private BasicBlock createBlock(BasicBlock parent, Node root, boolean isFunction, boolean isLoop) {
    // BasicBlock constructor sets isFunction based on root.isFunction() and isLoop based on parent type.
    // To control flags we need to set child manually? Actually BasicBlock extracts flags from root.
    // We'll override by creating a subclass? No, we can set root type accordingly or use reflection.
    // For simplicity, we rely on constructor logic: set root type to FUNCTION or parent type to DO/WHILE/FOR.
    // We'll create blocks with proper node types.
    return new BasicBlock(parent, root);
  }

  // Create a reference object with specific nameNode and basicBlock
  private Reference createReference(Node nameNode, BasicBlock block, NodeTraversal t, InputId inputId) {
    // Using the static createRefForTest does not allow custom block. We'll use constructor via reflection or call it directly.
    // Constructor is private, but we can use the package-private constructor? Reference has package-private constructor.
    // Actually Reference(Node nameNode, NodeTraversal t, BasicBlock basicBlock) is package-private? It's default.
    // Since we are in same package, we can call it.
    // But we don't have a NodeTraversal. We can pass null for t? But then t.getScope() will NPE.
    // Instead, we can use the factory method Reference.newBleedingFunction or createRefForTest.
    // createRefForTest returns a Reference with null block and null scope.
    // That's not suitable for testing block-dependent methods.
    // Alternative: we can create a mock NodeTraversal? Not allowed.
    // We can create a minimal NodeTraversal instance by building a Scope and Compiler? Too heavy.
    // For testing ReferenceCollection methods, we only need the nameNode and basicBlock.
    // We can create a Reference object using the private constructor via reflection? That's complex.
    // Instead, we can subclass Reference? Not possible because it's final.
    // We might have to accept using createRefForTest and then set the basicBlock via reflection.
    // Or we can test the methods that do not depend on scope/basicBlock? 
    // Actually, isWellDefined uses getBasicBlock() which returns the block used in addReference.
    // So we need references with actual blocks.
    // I think we can modify the test to call addReference on a ReferenceCollectingCallback instance.
    // That would traverse an AST and build referenceMap.
    // But we need to parse JavaScript code, which is heavyweight.
    // Given the constraints, the best approach is to test the inner classes by constructing
    // Reference objects with the package-private constructor that takes (Node, BasicBlock, Scope, InputId).
    // We can pass null for scope and inputId and accept that some methods may throw if called.
    // isInitializingDeclaration checks getParent() which does not need scope. 
    // isDeclaration checks parent and grandparent, which works with the nodes we create.
    // So we can use constructor with null scope/inputId.
    // The constructor private Reference(Node, BasicBlock, Scope, InputId) is private.
    // But there is also Reference(Node nameNode, NodeTraversal t, BasicBlock basicBlock) – we don't have t.
    // We can create a dummy NodeTraversal by extending it? Abstract class.
    // Or we can use the static method createRefForTest which gives a Reference with null block.
    // That severely limits testing of well-definedness.
    // Let's check: isWellDefined accesses getBasicBlock() and uses it to compare blocks.
    // If block is null, then provablyExecutesBefore will NPE (since it calls thatBlock.getBasicBlock()).
    // So we must have non-null blocks.
    // Therefore, I will create a minimal NodeTraversal object using a simple implementation.
    // We can create a NodeTraversal that returns a dummy scope and input.
    // Actually, NodeTraversal is an interface? It's a class in jscomp. We can instantiate it?
    // The constructor is package-private? NodeTraversal has public constructor? It is public.
    // NodeTraversal(AbstractCompiler compiler, ScopeCreator scopeCreator, Callback callback)
    // Very heavy.
    // Alternatively, we can test the ReferenceCollection methods by creating
    // Reference objects using the package-private constructor that takes (Node, BasicBlock, Scope, InputId).
    // That constructor is private? No, it's actually private. Wait, the code shows:
    // private Reference(Node nameNode, BasicBlock basicBlock, Scope scope, InputId inputId) { ... }
    // That is private. But there is also Reference(Node nameNode, NodeTraversal t, BasicBlock basicBlock) 
    // which is package-private? It is omitted from the source but the actual file likely has it.
    // In the given source, the only public constructor is the one that takes NodeTraversal.
    // And createRefForTest is static and uses the private constructor.
    // So we cannot directly create a Reference with custom block from outside.
    // This means our tests must rely on using an actual compilation to get valid references, which is infeasible.
    // 
    // Given the difficulty, I will focus the test on the operations that can be exercised
    // through the public API without needing to instantiate Reference objects directly.
    // For ReferenceCollection, we can create it and add references via its add() method,
    // but we need Reference objects. We can get those from an actual traversal simulation.
    // Alternatively, we can test ReferenceCollection logic by creating a subclass?
    // ReferenceCollection is non-final, we can subclass it for testing.
    // But add() is package-private, not public? It is default (void add(Reference ref)).
    // We can call it from same package.
    // We can create a Reference object using createRefForTest, but that has null block.
    // For testing isWellDefined with blocks, we might need to create a mock Block that always returns true/false.
    // We can create a testing subclass of BasicBlock to control provablyExecutesBefore.
    // BasicBlock is final! So we cannot subclass.
    // At this point, we must accept that we cannot perfectly unit test the complex dependencies.
    // 
    // Therefore, I will write tests that exercise the outer class's behavior through integration-like
    // but simplified scenarios using the static createRefForTest and then test the ReferenceCollection
    // methods that do not require blocks (like isNeverAssigned, firstReferenceIsAssigningDeclaration, etc.)
    // For block-dependent methods, I will write tests that rely on the actual implementation
    // by using a minimal compilation. But we don't have a compiler.
    // I'll fall back to testing the simple methods and adding a comment about the defect.
    // 
    // However, the instructions require a test that reveals the defect. So I must find a way.
    // Let's inspect the code more: isWellDefined uses references.get(0).isDeclaration() and
    // references.get(0).getBasicBlock(). If getBasicBlock() returns null, we get NPE.
    // So we must use references with non-null basic blocks.
    // I'll create a custom ReferenceCollection subclass that overrides iterator to return
    // references with fake blocks? That might work.
    // We can create a BasicBlock via the constructor (which is public) and use it.
    // Then we need to create Reference objects. Since the constructor that takes NodeTraversal
    // is the only one accessible, we can pass a null NodeTraversal? That will cause NPE when calling t.getScope().
    // But the constructor does not immediately call t.getScope()? Let's see the code:
    // Reference(Node nameNode, NodeTraversal t, BasicBlock basicBlock) {
    //   this(nameNode, basicBlock, t.getScope(), t.getInput().getInputId());
    // }
    // It immediately calls t.getScope() and t.getInput(). So passing null will NPE.
    // So we must provide a non-null NodeTraversal.
    // We can create a dummy NodeTraversal by using a Compiler. But we can use the
    // NodeTraversal class from the test package? There is no test package.
    // I think we can use Mockito? Not allowed.
    // 
    // Given the constraints, I will write tests that focus on the ReferenceCollection class
    // by constructing Reference objects using the package-private constructor via reflection.
    // This is allowed as long as we don't use mocking frameworks. We can use setAccessible.
    // But the instruction says no mocking or third-party libraries, but reflection is part of Java.
    // I'll use reflection to call the private constructor.
    // 
    // Let's do that to create Reference objects for testing.
    // 
  
  */

  @Test(timeout = 4000)
  public void testReferenceCollectionEmpty() {
    ReferenceCollection coll = new ReferenceCollection();
    assertFalse("Empty collection should not be well-defined", coll.isWellDefined());
    assertFalse("Empty collection should not be escaped", coll.isEscaped());
    assertTrue("Empty collection should be never assigned", coll.isNeverAssigned());
    assertFalse("Empty collection first ref not assigning decl", coll.firstReferenceIsAssigningDeclaration());
    assertNull("getInitializingReference should be null", coll.getInitializingReference());
    assertNull("getInitializingReferenceForConstants should be null", coll.getInitializingReferenceForConstants());
  }

  @Test(timeout = 4000)
  public void testReferenceCollectionSingleVarDeclWithInit() throws Exception {
    ReferenceCollection coll = new ReferenceCollection();
    // Create a nameNode for var declaration with init
    Node varNode = new Node(Token.VAR);
    Node nameNode = Node.newString(Token.NAME, "x");
    Node initVal = Node.newNumber(1);
    nameNode.addChildToBack(initVal);
    varNode.addChildToBack(nameNode);
    BasicBlock dummyBlock = createDummyBlock();

    Reference ref = createReferenceViaReflection(nameNode, dummyBlock);
    coll.add(ref);

    assertTrue("Single var with init should be well-defined", coll.isWellDefined());
    assertNotNull("getInitializingReference should be non-null", coll.getInitializingReference());
    assertSame("Initializing reference should be the only ref", ref, coll.getInitializingReference());
    assertNotNull("getInitializingReferenceForConstants same", coll.getInitializingReferenceForConstants());
    assertFalse("Not escaped", coll.isEscaped());
    assertFalse("Not never assigned", coll.isNeverAssigned());
    assertTrue("First ref is assigning decl", coll.firstReferenceIsAssigningDeclaration());
  }

  @Test(timeout = 4000)
  public void testReferenceCollectionVarDeclNoInit() throws Exception {
    ReferenceCollection coll = new ReferenceCollection();
    Node varNode = new Node(Token.VAR);
    Node nameNode = Node.newString(Token.NAME, "y");
    varNode.addChildToBack(nameNode);
    BasicBlock block = createDummyBlock();
    Reference ref = createReferenceViaReflection(nameNode, block);
    coll.add(ref);

    assertFalse("Var decl without init should not be well-defined", coll.isWellDefined());
    assertNull("getInitializingReference should be null", coll.getInitializingReference());
    assertTrue("Not escaped", !coll.isEscaped()); // isEscaped false because same scope? Actually scope not set, so ref.scope null, second ref will cause NPE. We only have one ref, so isEscaped: scope null, loop not entered, returns false.
    assertFalse("Never assigned? Actually isNeverAssigned: checks isLvalue... var decl without init is Lvalue? isLvalue checks parent type == VAR && nameNode.getFirstChild() != null. Here firstChild is null, so isLvalue returns false. So isNeverAssigned returns true." , coll.isNeverAssigned());
    // Actually isNeverAssigned: for each ref, if ref.isLvalue() or ref.isInitializingDeclaration() => return false.
    // Our ref: isLvalue false (as above), isInitializingDeclaration false (since isDeclaration true but parent.isVar() true and nameNode.getFirstChild() null).
    // So isNeverAssigned returns true.
    assertTrue("isNeverAssigned should be true", coll.isNeverAssigned());
  }

  @Test(timeout = 4000)
  public void testReferenceCollectionAssignmentAfterDecl() throws Exception {
    ReferenceCollection coll = new ReferenceCollection();
    // var x;
    Node varNode = new Node(Token.VAR);
    Node nameNodeDecl = Node.newString(Token.NAME, "x");
    varNode.addChildToBack(nameNodeDecl);
    BasicBlock block = createDummyBlock();
    Reference refDecl = createReferenceViaReflection(nameNodeDecl, block);
    coll.add(refDecl);

    // x = 1;
    Node assignNode = new Node(Token.ASSIGN);
    Node nameNodeAssign = Node.newString(Token.NAME, "x");
    Node val = Node.newNumber(1);
    assignNode.addChildToFront(nameNodeAssign);
    assignNode.addChildToBack(val);
    // The nameNodeAssign is same name but different node; getParent() returns assignNode.
    Reference refAssign = createReferenceViaReflection(nameNodeAssign, block);
    coll.add(refAssign);

    assertTrue("Assignment after decl should be well-defined (same block)", coll.isWellDefined());
    assertEquals("Initializing ref should be the assignment", refAssign, coll.getInitializingReference());
  }

  @Test(timeout = 4000)
  public void testReferenceCollectionUseBeforeAssignment() throws Exception {
    ReferenceCollection coll = new ReferenceCollection();
    // var x;
    Node varNode = new Node(Token.VAR);
    Node nameNodeDecl = Node.newString(Token.NAME, "x");
    varNode.addChildToBack(nameNodeDecl);
    BasicBlock block = createDummyBlock();
    Reference refDecl = createReferenceViaReflection(nameNodeDecl, block);
    coll.add(refDecl);

    // use x;
    Node nameNodeUse = Node.newString(Token.NAME, "x");
    Reference refUse = createReferenceViaReflection(nameNodeUse, block);
    coll.add(refUse);

    // x = 1;
    Node assignNode = new Node(Token.ASSIGN);
    Node nameNodeAssign = Node.newString(Token.NAME, "x");
    Node val = Node.newNumber(1);
    assignNode.addChildToFront(nameNodeAssign);
    assignNode.addChildToBack(val);
    Reference refAssign = createReferenceViaReflection(nameNodeAssign, block);
    coll.add(refAssign);

    // Defect: isWellDefined currently returns true because initBlock provablyExecutesBefore all other blocks,
    // but the use occurs before assignment in source order. This test should fail if the defect is present.
    assertFalse("Variable used before assignment should not be well-defined", coll.isWellDefined());
  }

  @Test(timeout = 4000)
  public void testReferenceCollectionIsEscaped() throws Exception {
    ReferenceCollection coll = new ReferenceCollection();
    BasicBlock block1 = createDummyBlock();
    BasicBlock block2 = createDummyBlock();
    Node nameNode = Node.newString(Token.NAME, "z");
    Reference ref1 = createReferenceViaReflection(nameNode, block1);
    // Set scope to different dummy scopes? We can't set scope; but isEscaped compares ref.scope references.
    // Since we passed null scope via reflection, all refs will have null scope, so comparison `scope != ref.scope` becomes false (both null).
    // To test escaped, we need different scope objects. We can create minimal Scope objects.
    // Scope is a class in jscomp. We can create Scope with root node.
    // Scope(Scope parent, Node rootNode). We'll create two different Scope instances.
    Scope scope1 = new Scope(null, new Node(Token.BLOCK));
    Scope scope2 = new Scope(null, new Node(Token.BLOCK));
    Reference refEscaped1 = createReferenceViaReflection(nameNode, block1, scope1);
    Reference refEscaped2 = createReferenceViaReflection(nameNode, block2, scope2);
    coll.add(refEscaped1);
    coll.add(refEscaped2);
    assertTrue("References in different scopes should be escaped", coll.isEscaped());
  }

  @Test(timeout = 4000)
  public void testReferenceCollectionMultipleAssignments() throws Exception {
    ReferenceCollection coll = new ReferenceCollection();
    BasicBlock block = createDummyBlock();
    // Decl with init
    Node varNode = new Node(Token.VAR);
    Node nameNode = Node.newString(Token.NAME, "a");
    Node initVal = Node.newNumber(1);
    nameNode.addChildToBack(initVal);
    varNode.addChildToBack(nameNode);
    Reference refInit = createReferenceViaReflection(nameNode, block);
    coll.add(refInit);
    // Second assignment
    Node assignNode = new Node(Token.ASSIGN);
    Node nameNode2 = Node.newString(Token.NAME, "a");
    assignNode.addChildToFront(nameNode2);
    assignNode.addChildToBack(Node.newNumber(2));
    Reference refAssign = createReferenceViaReflection(nameNode2, block);
    coll.add(refAssign);
    assertFalse("Variable assigned twice should not be assigned once in lifetime", coll.isAssignedOnceInLifetime());
    assertFalse("isNeverAssigned should be false", coll.isNeverAssigned());
  }

  @Test(timeout = 4000)
  public void testReferenceCollectionNeverAssigned() throws Exception {
    ReferenceCollection coll = new ReferenceCollection();
    BasicBlock block = createDummyBlock();
    Node nameNode = Node.newString(Token.NAME, "b");
    Reference ref = createReferenceViaReflection(nameNode, block);
    coll.add(ref);
    assertTrue("Only a read: never assigned", coll.isNeverAssigned());
  }

  @Test(timeout = 4000)
  public void testBasicBlockSameBlockProvablyExecutesBefore() {
    Node root = new Node(Token.SCRIPT);
    BasicBlock block = new BasicBlock(null, root);   // global block
    assertTrue("Same block should provably execute before itself", block.provablyExecutesBefore(block));
  }

  @Test(timeout = 4000)
  public void testBasicBlockParentChildProvablyExecutesBefore() {
    Node rootParent = new Node(Token.BLOCK);
    Node rootChild = new Node(Token.BLOCK);
    BasicBlock parent = new BasicBlock(null, rootParent);
    BasicBlock child = new BasicBlock(parent, rootChild);
    assertTrue("Parent should provably execute before child", parent.provablyExecutesBefore(child));
    assertFalse("Child should not provably execute before parent", child.provablyExecutesBefore(parent));
  }

  @Test(timeout = 4000)
  public void testBasicBlockHoistedDoesNotExecuteBefore() {
    // Create a hoisted block: a function declaration is hoisted.
    // But BasicBlock sets isHoisted based on NodeUtil.isHoistedFunctionDeclaration(root).
    // We need to create a Function node that is a child of a SCRIPT? Actually, a hoisted function is one where
    // root is a FUNCTION node and  its parent is a SCRIPT or BLOCK? The method checks NodeUtil.isHoistedFunctionDeclaration,
    // which checks if func's parent is a SCRIPT or BLOCK. So we need to create such a node.
    Node funcNode = new Node(Token.FUNCTION);
    Node script = new Node(Token.SCRIPT);
    script.addChildToBack(funcNode);
    BasicBlock hoistedBlock = new BasicBlock(null, funcNode);   // root is function node, but parent? The parent of root is script, so it's hoisted.
    BasicBlock normalBlock = new BasicBlock(null, new Node(Token.BLOCK));
    // The normalBlock is a sibling? They have no parent relationship. provablyExecutesBefore will iterate up from thatBlock.
    // Since hoistedBlock is not an ancestor, it will hit null and return false. That's intended.
    // We need a scenario where they are in ancestor relationship but hoisted block is in between.
    // Create a chain: rootParent -> hoistedBlock (child) -> normal Grandchild? But hoistedBlock is a leaf? We can't add a child because BasicBlock's parent is the node's parent, not the block's node.
    // Better: two blocks where one is an ancestor but there is a hoisted block along the path.
    // Let's create: globalBlock (parent) -> hoistedChild (child) -> grandchild (child of hoistedChild node).
    // But hoistedChild's root is a function, and grandchild's root is a block inside the function? The parent of grandchild's root is the function node, so hoistedChild is the parent block.
    // In provablyExecutesBefore, the loops goes up from thatBlock using getParent(). So if we check global vs grandchild, we go grandchild -> hoistedChild (getParent()) -> then we check if hoistedChild is hoisted, if so, return false.
    Node funcRoot = new Node(Token.FUNCTION);
    Node blockInsideFunc = new Node(Token.BLOCK);
    funcRoot.addChildToBack(blockInsideFunc);
    BasicBlock globalBlock = new BasicBlock(null, new Node(Token.SCRIPT));
    BasicBlock hoistedBlockAncestor = new BasicBlock(globalBlock, funcRoot);  // parent = global, root = func -> hoisted
    BasicBlock grandchildBlock = new BasicBlock(hoistedBlockAncestor, blockInsideFunc);
    // parent chain: grandchild -> hoistedBlockAncestor -> globalBlock
    // Now check globalBlock before grandchild: should be false because hoistedBlockAncestor is hoisted
    assertFalse("Global block should not provably execute before a block inside a hoisted function",
        globalBlock.provablyExecutesBefore(grandchildBlock));
    // However, hoistedBlockAncestor before grandchild: the path goes grandchild -> hoistedBlockAncestor == hoistedBlockAncestor, so it returns true without checking hoisted? Actually loop checks currentBlock.isHoisted() only for blocks between thatBlock and this. Here, when checking hoistedBlock before grandchild, currentBlock starts as grandchild, then becomes hoistedBlock, then loop condition currentBlock != this? this is hoistedBlock, so it stops and returns true. It does not check hoistedness because it doesn't go past. That's incorrect: a block cannot provably execute before a block inside itself? Actually it can: the function block starts before any code inside it. So hoistedBlock before grandchild should be true. That's fine.
    // We want a scenario where parent is separated by a hoisted block. That case is covered by globalBlock before grandchild -> returns false.
  }

  @Test(timeout = 4000)
  public void testBasicBlockIsGlobalScopeBlock() {
    Node root = new Node(Token.SCRIPT);
    BasicBlock global = new BasicBlock(null, root);
    assertTrue("Block with null parent should be global", global.isGlobalScopeBlock());
    BasicBlock nonGlobal = new BasicBlock(global, new Node(Token.BLOCK));
    assertFalse("Block with parent should not be global", nonGlobal.isGlobalScopeBlock());
  }

  @Test(timeout = 4000)
  public void testReferenceIsDeclaration() throws Exception {
    // var x;
    Node varNode = new Node(Token.VAR);
    Node nameNode = Node.newString(Token.NAME, "x");
    varNode.addChildToBack(nameNode);
    BasicBlock block = createDummyBlock();
    Reference ref = createReferenceViaReflection(nameNode, block);
    assertTrue("Name inside VAR should be declaration", ref.isDeclaration());

    // function f() {}
    Node funcNode = new Node(Token.FUNCTION);
    Node funcName = Node.newString(Token.NAME, "f");
    funcNode.addChildToFront(funcName);
    ref = createReferenceViaReflection(funcName, block);
    assertTrue("Function name should be declaration", ref.isDeclaration());

    // catch(e) { }
    Node catchNode = new Node(Token.CATCH);
    Node catchName = Node.newString(Token.NAME, "e");
    catchNode.addChildToFront(catchName);
    ref = createReferenceViaReflection(catchName, block);
    assertTrue("Catch variable should be declaration", ref.isDeclaration());

    // parameter: function(a) {}
    Node paramList = new Node(Token.PARAM_LIST);
    Node paramName = Node.newString(Token.NAME, "a");
    paramList.addChildToBack(paramName);
    Node funcNode2 = new Node(Token.FUNCTION);
    funcNode2.addChildToFront(paramList);
    // The reference node is paramName, its parent is paramList, grandparent is function.
    ref = createReferenceViaReflection(paramName, block);
    assertTrue("Parameter should be declaration", ref.isDeclaration());
  }

  @Test(timeout = 4000)
  public void testReferenceIsVarDeclaration() throws Exception {
    Node varNode = new Node(Token.VAR);
    Node nameNode = Node.newString(Token.NAME, "y");
    varNode.addChildToBack(nameNode);
    BasicBlock block = createDummyBlock();
    Reference ref = createReferenceViaReflection(nameNode, block);
    assertTrue("Name under VAR should be var declaration", ref.isVarDeclaration());

    // Non-var parent: assign
    Node assignNode = new Node(Token.ASSIGN);
    Node nameNode2 = Node.newString(Token.NAME, "y");
    assignNode.addChildToFront(nameNode2);
    assignNode.addChildToBack(Node.newNumber(1));
    ref = createReferenceViaReflection(nameNode2, block);
    assertFalse("Name under ASSIGN should not be var declaration", ref.isVarDeclaration());
  }

  @Test(timeout = 4000)
  public void testReferenceIsInitializingDeclaration() throws Exception {
    // var x = 1; -> initializing
    Node varNode = new Node(Token.VAR);
    Node nameNode = Node.newString(Token.NAME, "x");
    nameNode.addChildToBack(Node.newNumber(1));
    varNode.addChildToBack(nameNode);
    BasicBlock block = createDummyBlock();
    Reference ref = createReferenceViaReflection(nameNode, block);
    assertTrue("Var with init should be initializing declaration", ref.isInitializingDeclaration());

    // var x; -> not initializing
    Node varNode2 = new Node(Token.VAR);
    Node nameNode2 = Node.newString(Token.NAME, "x");
    varNode2.addChildToBack(nameNode2);
    ref = createReferenceViaReflection(nameNode2, block);
    assertFalse("Var without init should not be initializing declaration", ref.isInitializingDeclaration());

    // function f() {} -> initializing (since parent is FUNCTION, not VAR)
    Node funcNode = new Node(Token.FUNCTION);
    Node funcName = Node.newString(Token.NAME, "f");
    funcNode.addChildToFront(funcName);
    ref = createReferenceViaReflection(funcName, block);
    assertTrue("Function declaration is initializing", ref.isInitializingDeclaration());

    // catch(e) {} -> initializing
    Node catchNode = new Node(Token.CATCH);
    Node catchName = Node.newString(Token.NAME, "e");
    catchNode.addChildToFront(catchName);
    ref = createReferenceViaReflection(catchName, block);
    assertTrue("Catch variable is initializing", ref.isInitializingDeclaration());
  }

  @Test(timeout = 4000)
  public void testReferenceIsSimpleAssignmentToName() throws Exception {
    Node assignNode = new Node(Token.ASSIGN);
    Node nameNode = Node.newString(Token.NAME, "a");
    assignNode.addChildToFront(nameNode);
    assignNode.addChildToBack(Node.newNumber(1));
    BasicBlock block = createDummyBlock();
    Reference ref = createReferenceViaReflection(nameNode, block);
    assertTrue("Assignment where name is first child should be simple assignment", ref.isSimpleAssignmentToName());
  }

  @Test(timeout = 4000)
  public void testReferenceIsLvalue() throws Exception {
    // assignment
    Node assignNode = new Node(Token.ASSIGN);
    Node nameNode = Node.newString(Token.NAME, "a");
    assignNode.addChildToFront(nameNode);
    assignNode.addChildToBack(Node.newNumber(1));
    BasicBlock block = createDummyBlock();
    Reference ref = createReferenceViaReflection(nameNode, block);
    assertTrue("Assignment should be lvalue", ref.isLvalue());

    // var with init
    Node varNode = new Node(Token.VAR);
    Node nameNode2 = Node.newString(Token.NAME, "b");
    nameNode2.addChildToBack(Node.newNumber(2));
    varNode.addChildToBack(nameNode2);
    ref = createReferenceViaReflection(nameNode2, block);
    assertTrue("Var with init should be lvalue", ref.isLvalue());

    // inc
    Node incNode = new Node(Token.INC);
    Node nameNode3 = Node.newString(Token.NAME, "c");
    incNode.addChildToFront(nameNode3);
    ref = createReferenceViaReflection(nameNode3, block);
    assertTrue("INC should be lvalue", ref.isLvalue());

    // for-in lhs
    Node forInNode = new Node(Token.FOR);
    Node nameNode4 = Node.newString(Token.NAME, "d");
    forInNode.addChildToFront(nameNode4);
    Node iter = new Node(Token.NAME, "obj");
    forInNode.addChildToBack(iter);
    // The for-in node is of type FOR, but isLhsOfForInExpression checks NodeUtil.isForIn(parent) && parent.getFirstChild() == nameNode.
    // NodeUtil.isForIn checks parent.getType() == Token.FOR? Actually isForIn checks for FOR_IN token? There is Token.FOR_IN in Rhino? Not sure. We'll skip.
    // Instead, test that a simple name read is not lvalue.
    Node readNode = Node.newString(Token.NAME, "e");
    ref = createReferenceViaReflection(readNode, block);
    assertFalse("Plain name read should not be lvalue", ref.isLvalue());
  }

  @Test(timeout = 4000)
  public void testReferenceGetAssignedValue() throws Exception {
    // function declaration -> returns function node
    Node funcNode = new Node(Token.FUNCTION);
    Node funcName = Node.newString(Token.NAME, "f");
    funcNode.addChildToFront(funcName);
    BasicBlock block = createDummyBlock();
    Reference ref = createReferenceViaReflection(funcName, block);
    assertEquals("Function declaration assigned value is function node", funcNode, ref.getAssignedValue());

    // assignment -> returns the rvalue
    Node assignNode = new Node(Token.ASSIGN);
    Node nameNode = Node.newString(Token.NAME, "g");
    Node rvalue = Node.newNumber(42);
    assignNode.addChildToFront(nameNode);
    assignNode.addChildToBack(rvalue);
    ref = createReferenceViaReflection(nameNode, block);
    assertEquals("Assignment assigned value", rvalue, ref.getAssignedValue());
  }

  @Test(timeout = 4000)
  public void testReferenceCollectionGetInitializingReferenceForConstants() throws Exception {
    ReferenceCollection coll = new ReferenceCollection();
    BasicBlock block = createDummyBlock();
    // var x;
    Node varNode = new Node(Token.VAR);
    Node nameNode = Node.newString(Token.NAME, "x");
    varNode.addChildToBack(nameNode);
    Reference refDecl = createReferenceViaReflection(nameNode, block);
    coll.add(refDecl);
    // x = 1 later
    Node assignNode = new Node(Token.ASSIGN);
    Node nameNode2 = Node.newString(Token.NAME, "x");
    assignNode.addChildToFront(nameNode2);
    assignNode.addChildToBack(Node.newNumber(1));
    Reference refAssign = createReferenceViaReflection(nameNode2, block);
    coll.add(refAssign);
    // The initializing reference for constant should be the assignment (index 1)
    assertEquals("Constant initializing ref should be assignment", refAssign, coll.getInitializingReferenceForConstants());
  }

  @Test(timeout = 4000)
  public void testReferenceCloneWithNewScope() throws Exception {
    BasicBlock block = createDummyBlock();
    Node nameNode = Node.newString(Token.NAME, "x");
    Scope originalScope = new Scope(null, new Node(Token.BLOCK));
    Reference ref = createReferenceViaReflection(nameNode, block, originalScope);
    Scope newScope = new Scope(null, new Node(Token.BLOCK));
    Reference cloned = ref.cloneWithNewScope(newScope);
    assertNotSame("Cloned reference should have new scope", ref.getScope(), cloned.getScope());
    assertSame("New scope should be the one passed", newScope, cloned.getScope());
    assertEquals("Node should remain same", ref.getNode(), cloned.getNode());
  }

  // ==================== Utility methods ====================

  private BasicBlock createDummyBlock() {
    return new BasicBlock(null, new Node(Token.SCRIPT));
  }

  // Creates a Reference using reflection to access the private constructor (Node, BasicBlock, Scope, InputId)
  private Reference createReferenceViaReflection(Node nameNode, BasicBlock block) throws Exception {
    return createReferenceViaReflection(nameNode, block, null);
  }

  private Reference createReferenceViaReflection(Node nameNode, BasicBlock block, Scope scope) throws Exception {
    java.lang.reflect.Constructor<Reference> constructor =
        Reference.class.getDeclaredConstructor(Node.class, BasicBlock.class, Scope.class, InputId.class);
    constructor.setAccessible(true);
    return constructor.newInstance(nameNode, block, scope, null);
  }
}