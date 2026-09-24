package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.jscomp.NodeTraversal;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSType;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.FunctionPrototypeType;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

/**
 * [Branch & Defect Analysis Matrix]
 * Coverage goals:
 * - normalizeClassType: constructor, prototype, unknown, null
 * - getTypeDeprecationInfo: null type, non-deprecated, deprecated with/without reason, prototype chain
 * - isDeprecatedFunction: FUNCTION node with deprecated type, non-function, non-deprecated
 * - isValidPrivateConstructorAccess: NEW vs others (CALL, NAME, INSTANCEOF)
 * - dereference: null, non-null
 * - shouldEmitDeprecationWarning: global vs non-global, CALL, NEW, assignment
 * - checkNameDeprecation: deprecated var with/without reason, not deprecated, definition/constructor skip
 * - checkPropertyDeprecation: deprecated property with/without reason, constructor skip
 * - checkConstructorDeprecation: deprecated class, constructor
 * - checkNameVisibility: private global access allowed in same file, disallowed in different file, except for constructor accesses
 * - checkPropertyVisibility: override detection (PRIVATE_OVERRIDE), private property access from same file, different file with/without same class, protected property access from subclass vs non-subclass
 * - checkConstantProperty: reassignment detection, constant property set, prototype constant
 * Defect target: Overriding private property should emit PRIVATE_OVERRIDE error when override is from different file.
 */
public class CheckAccessControlsDeepseekTest {

  private static final Method normalizeClassType;
  private static final Method getTypeDeprecationInfo;
  private static final Method isDeprecatedFunction;
  private static final Method isValidPrivateConstructorAccess;
  private static final Method dereference;
  private static final Method shouldEmitDeprecationWarning;

  static {
    try {
      normalizeClassType = CheckAccessControls.class.getDeclaredMethod("normalizeClassType", JSType.class);
      normalizeClassType.setAccessible(true);
      getTypeDeprecationInfo = CheckAccessControls.class.getDeclaredMethod("getTypeDeprecationInfo", JSType.class);
      getTypeDeprecationInfo.setAccessible(true);
      isDeprecatedFunction = CheckAccessControls.class.getDeclaredMethod("isDeprecatedFunction", Node.class, Node.class);
      isDeprecatedFunction.setAccessible(true);
      isValidPrivateConstructorAccess = CheckAccessControls.class.getDeclaredMethod("isValidPrivateConstructorAccess", Node.class);
      isValidPrivateConstructorAccess.setAccessible(true);
      dereference = CheckAccessControls.class.getDeclaredMethod("dereference", JSType.class);
      dereference.setAccessible(true);
      shouldEmitDeprecationWarning = CheckAccessControls.class.getDeclaredMethod("shouldEmitDeprecationWarning", NodeTraversal.class, Node.class, Node.class);
      shouldEmitDeprecationWarning.setAccessible(true);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  private static final Visibility PRIVATE = Visibility.PRIVATE;
  private static final Visibility PROTECTED = Visibility.PROTECTED;
  private static final Visibility PUBLIC = Visibility.PUBLIC;

  private AbstractCompiler compiler;
  private CheckAccessControls pass;
  private JSTypeRegistry registry;
  private JSType unknownType;
  private JSType numberType;
  private ObjectType objectType;
  private ObjectType instanceType;
  private ObjectType prototypeType;

  @Before
  public void setUp() {
    compiler = new TestCompiler();
    pass = new CheckAccessControls(compiler);
    registry = compiler.getTypeRegistry();
    unknownType = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    objectType = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
    // Create a simple class type for testing
    instanceType = registry.createAnonymousObjectType();
    prototypeType = instanceType.getImplicitPrototype();
  }

  // ---------- Partition A: Core Functional Logic ----------

  @Test(timeout = 4000)
  public void testNormalizeClassTypeWithConstructor() throws Exception {
    FunctionType ctor = registry.createConstructorType("Foo", null, null, null);
    JSType result = (JSType) normalizeClassType.invoke(pass, ctor);
    assertNotNull("Expected instance type", result);
    assertTrue("Should be instance type", result.isInstanceType());
  }

  @Test(timeout = 4000)
  public void testNormalizeClassTypeWithFunctionPrototype() throws Exception {
    FunctionType ctor = registry.createConstructorType("Foo", null, null, null);
    FunctionPrototypeType proto = ctor.getPrototype();
    JSType result = (JSType) normalizeClassType.invoke(pass, proto);
    assertNotNull(result);
    assertTrue(result.isInstanceType());
  }

  @Test(timeout = 4000)
  public void testNormalizeClassTypeWithInstanceType() throws Exception {
    JSType result = (JSType) normalizeClassType.invoke(pass, instanceType);
    assertSame(instanceType, result);
  }

  @Test(timeout = 4000)
  public void testNormalizeClassTypeWithNull() throws Exception {
    JSType result = (JSType) normalizeClassType.invoke(pass, (JSType) null);
    assertNull(result);
  }

  @Test(timeout = 4000)
  public void testNormalizeClassTypeWithUnknownType() throws Exception {
    JSType result = (JSType) normalizeClassType.invoke(pass, unknownType);
    assertSame(unknownType, result);
  }

  @Test(timeout = 4000)
  public void testGetTypeDeprecationInfoNullType() throws Exception {
    assertNull(getTypeDeprecationInfo.invoke(pass, (JSType) null));
  }

  @Test(timeout = 4000)
  public void testGetTypeDeprecationInfoNonDeprecated() throws Exception {
    JSType type = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    assertNull(getTypeDeprecationInfo.invoke(pass, type));
  }

  @Test(timeout = 4000)
  public void testGetTypeDeprecationInfoDeprecatedNoReason() throws Exception {
    FunctionType ctor = registry.createConstructorType("Dep", null, null, null);
    JSDocInfo.Builder builder = JSDocInfo.Builder.mutable();
    builder.setDeprecated(true);
    ctor.setJSDocInfo(builder.build());
    String result = (String) getTypeDeprecationInfo.invoke(pass, ctor);
    assertEquals("", result);
  }

  @Test(timeout = 4000)
  public void testGetTypeDeprecationInfoDeprecatedWithReason() throws Exception {
    FunctionType ctor = registry.createConstructorType("Dep", null, null, null);
    JSDocInfo.Builder builder = JSDocInfo.Builder.mutable();
    builder.setDeprecated(true);
    builder.setDeprecationReason("Use new API");
    ctor.setJSDocInfo(builder.build());
    String result = (String) getTypeDeprecationInfo.invoke(pass, ctor);
    assertEquals("Use new API", result);
  }

  @Test(timeout = 4000)
  public void testGetTypeDeprecationInfoInheritedDeprecation() throws Exception {
    ObjectType obj = registry.createAnonymousObjectType();
    ObjectType proto = obj.getImplicitPrototype();
    JSDocInfo.Builder builder = JSDocInfo.Builder.mutable();
    builder.setDeprecated(true);
    proto.setJSDocInfo(builder.build());
    String result = (String) getTypeDeprecationInfo.invoke(pass, obj);
    assertEquals("", result);
  }

  @Test(timeout = 4000)
  public void testIsDeprecatedFunctionDeprecated() throws Exception {
    Node funcNode = new Node(Token.FUNCTION);
    FunctionType funcType = registry.createFunctionType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    JSDocInfo.Builder builder = JSDocInfo.Builder.mutable();
    builder.setDeprecated(true);
    funcType.setJSDocInfo(builder.build());
    funcNode.setJSType(funcType);
    boolean result = (boolean) isDeprecatedFunction.invoke(pass, funcNode, new Node(Token.NAME));
    assertTrue(result);
  }

  @Test(timeout = 4000)
  public void testIsDeprecatedFunctionNonDeprecated() throws Exception {
    Node funcNode = new Node(Token.FUNCTION);
    FunctionType funcType = registry.createFunctionType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    funcNode.setJSType(funcType);
    boolean result = (boolean) isDeprecatedFunction.invoke(pass, funcNode, new Node(Token.NAME));
    assertFalse(result);
  }

  @Test(timeout = 4000)
  public void testIsDeprecatedFunctionNonFunction() throws Exception {
    Node nameNode = new Node(Token.NAME);
    boolean result = (boolean) isDeprecatedFunction.invoke(pass, nameNode, new Node(Token.NAME));
    assertFalse(result);
  }

  @Test(timeout = 4000)
  public void testIsValidPrivateConstructorAccessNew() throws Exception {
    Node newNode = new Node(Token.NEW);
    assertFalse(isValidPrivateConstructorAccess.invoke(pass, newNode));
  }

  @Test(timeout = 4000)
  public void testIsValidPrivateConstructorAccessCall() throws Exception {
    Node callNode = new Node(Token.CALL);
    assertTrue(isValidPrivateConstructorAccess.invoke(pass, callNode));
  }

  @Test(timeout = 4000)
  public void testIsValidPrivateConstructorAccessInstanceOf() throws Exception {
    Node instanceofNode = new Node(Token.INSTANCEOF);
    assertTrue(isValidPrivateConstructorAccess.invoke(pass, instanceofNode));
  }

  @Test(timeout = 4000)
  public void testDereference() throws Exception {
    JSType input = numberType;
    JSType result = (JSType) dereference.invoke(pass, input);
    assertNotNull(result);
  }

  @Test(timeout = 4000)
  public void testDereferenceNull() throws Exception {
    assertNull(dereference.invoke(pass, (JSType) null));
  }

  // ---------- Partition B: Boundary & Null Handling ----------

  @Test(timeout = 4000)
  public void testShouldEmitDeprecationWarningGlobalScopeNonCall() throws Exception {
    // In global scope, non-CALL/NEW should return false
    NodeTraversal t = createTraversal(true /* global */);
    Node n = new Node(Token.NAME);
    Node parent = new Node(Token.EXPR_RESULT, n);
    boolean result = (boolean) shouldEmitDeprecationWarning.invoke(pass, t, n, parent);
    assertFalse(result);
  }

  @Test(timeout = 4000)
  public void testShouldEmitDeprecationWarningGlobalScopeCall() throws Exception {
    NodeTraversal t = createTraversal(true);
    Node n = new Node(Token.NAME, "foo");
    Node callNode = new Node(Token.CALL, n);
    boolean result = (boolean) shouldEmitDeprecationWarning.invoke(pass, t, n, callNode);
    // Inside canAccessDeprecatedTypes will be false (no deprecated depth, not in deprecated class)
    assertTrue(result);
  }

  @Test(timeout = 4000)
  public void testShouldEmitDeprecationWarningNonGlobalScope() throws Exception {
    NodeTraversal t = createTraversal(false);
    Node n = new Node(Token.GETPROP);
    Node parent = new Node(Token.ASSIGN, n, new Node(Token.NUMBER, 1));
    boolean result = (boolean) shouldEmitDeprecationWarning.invoke(pass, t, n, parent);
    // In non-global scope, assignment to deprecated property should suppress warning
    assertTrue(result);
  }

  @Test(timeout = 4000)
  public void testShouldEmitDeprecationWarningAssignmentToDeprecatedPropertyInGlobal() throws Exception {
    // In global scope, GETPROP as first child of ASSIGN should return false
    NodeTraversal t = createTraversal(true);
    Node getprop = new Node(Token.GETPROP);
    Node assign = new Node(Token.ASSIGN, getprop, new Node(Token.NUMBER, 1));
    boolean result = (boolean) shouldEmitDeprecationWarning.invoke(pass, t, getprop, assign);
    assertFalse(result);
  }

  // ---------- Partition C: Defect-Targeted Branch Zone ----------

  @Test(timeout = 4000)
  public void testCheckPropertyVisibilityPrivateOverrideDifferentFile() {
    // Simulate: Override private property from a different file -> PRIVATE_OVERRIDE error
    NodeTraversal t = createTraversal(true, "other.js"); // global scope, different file
    Node nameNode = new Node(Token.NAME, "Foo");
    Node getprop = new Node(Token.GETPROP, nameNode, Node.newString("myProp"));
    // Parent is ASSIGN
    Node assign = new Node(Token.ASSIGN, getprop, new Node(Token.FUNCTION));
    // Set up object type with private property
    JSDocInfo propDoc = JSDocInfo.Builder.mutable().setVisibility(PRIVATE).setSourceName("Foo.js").build();
    objectType = registry.createAnonymousObjectType();
    objectType.defineDeclaredProperty("myProp", numberType, propDoc);
    nameNode.setJSType(objectType);
    getprop.getFirstChild().setJSType(objectType);
    // Invoke visit to trigger checkPropertyVisibility
    pass.visit(t, getprop, assign);
    testCompiler tc = (testCompiler) compiler;
    assertEquals("Expected one PRIVATE_OVERRIDE error", 1, tc.errors.size());
    assertTrue(tc.errors.get(0).contains("Overriding private property"));
  }

  @Test(timeout = 4000)
  public void testCheckPropertyVisibilityPrivateAccessSameFile() {
    // private access in same file should be allowed
    NodeTraversal t = createTraversal(false, "same.js");
    Node nameNode = new Node(Token.NAME, "obj");
    Node getprop = new Node(Token.GETPROP, nameNode, Node.newString("x"));
    Node parent = new Node(Token.GETPROP, getprop); // just some parent
    JSDocInfo propDoc = JSDocInfo.Builder.mutable().setVisibility(PRIVATE).setSourceName("same.js").build();
    objectType = registry.createAnonymousObjectType();
    objectType.defineDeclaredProperty("x", numberType, propDoc);
    nameNode.setJSType(objectType);
    getprop.getFirstChild().setJSType(objectType);
    pass.visit(t, getprop, parent);
    assertTrue("No errors expected", ((testCompiler) compiler).errors.isEmpty());
  }

  @Test(timeout = 4000)
  public void testCheckPropertyVisibilityPrivateDifferentFileDifferentClass() {
    // private property, different file, different class -> BAD_PRIVATE_PROPERTY_ACCESS
    NodeTraversal t = createTraversal(false, "other.js");
    Node nameNode = new Node(Token.NAME, "obj");
    Node getprop = new Node(Token.GETPROP, nameNode, Node.newString("y"));
    Node parent = new Node(Token.GETPROP, getprop);
    JSDocInfo propDoc = JSDocInfo.Builder.mutable().setVisibility(PRIVATE).setSourceName("original.js").build();
    objectType = registry.createAnonymousObjectType();
    objectType.defineDeclaredProperty("y", numberType, propDoc);
    nameNode.setJSType(objectType);
    getprop.getFirstChild().setJSType(objectType);
    // currentClass is null (methodDepth = 0)
    pass.visit(t, getprop, parent);
    testCompiler tc = (testCompiler) compiler;
    assertEquals(1, tc.errors.size());
    assertTrue(tc.errors.get(0).contains("Access to private property"));
  }

  @Test(timeout = 4000)
  public void testCheckPropertyVisibilityProtectedAccessSameFile() {
    // protected in same file -> allowed
    NodeTraversal t = createTraversal(false, "same.js");
    Node nameNode = new Node(Token.NAME, "obj");
    Node getprop = new Node(Token.GETPROP, nameNode, Node.newString("z"));
    Node parent = new Node(Token.GETPROP, getprop);
    JSDocInfo propDoc = JSDocInfo.Builder.mutable().setVisibility(PROTECTED).setSourceName("same.js").build();
    objectType = registry.createAnonymousObjectType();
    objectType.defineDeclaredProperty("z", numberType, propDoc);
    nameNode.setJSType(objectType);
    getprop.getFirstChild().setJSType(objectType);
    pass.visit(t, getprop, parent);
    assertTrue(((testCompiler) compiler).errors.isEmpty());
  }

  @Test(timeout = 4000)
  public void testCheckPropertyVisibilityProtectedAccessDifferentFileNotSubtype() {
    // protected in different file, currentClass not subtype -> error
    NodeTraversal t = createTraversal(false, "other.js");
    Node nameNode = new Node(Token.NAME, "obj");
    Node getprop = new Node(Token.GETPROP, nameNode, Node.newString("w"));
    Node parent = new Node(Token.GETPROP, getprop);
    JSDocInfo propDoc = JSDocInfo.Builder.mutable().setVisibility(PROTECTED).setSourceName("base.js").build();
    objectType = registry.createAnonymousObjectType();
    objectType.defineDeclaredProperty("w", numberType, propDoc);
    nameNode.setJSType(objectType);
    getprop.getFirstChild().setJSType(objectType);
    // currentClass is null -> not subtype
    pass.visit(t, getprop, parent);
    testCompiler tc = (testCompiler) compiler;
    assertEquals(1, tc.errors.size());
    assertTrue(tc.errors.get(0).contains("Access to protected property"));
  }

  @Test(timeout = 4000)
  public void testCheckNameVisibilityPrivateGlobalAccessSameFile() {
    NodeTraversal t = createTraversal(true, "file.js");
    Node nameNode = new Node(Token.NAME, "myVar");
    Node parent = new Node(Token.EXPR_RESULT, nameNode);
    JSDocInfo docInfo = JSDocInfo.Builder.mutable().setVisibility(PRIVATE).setSourceName("file.js").build();
    Scope.Var var = createVar("myVar", docInfo);
    // Set scope to contain var
    TestScope scope = new TestScope();
    scope.varMap.put("myVar", var);
    ((testCompiler) compiler).setScope(scope);
    pass.visit(t, nameNode, parent);
    assertTrue(((testCompiler) compiler).errors.isEmpty());
  }

  @Test(timeout = 4000)
  public void testCheckNameVisibilityPrivateGlobalAccessDifferentFileNotConstructor() {
    NodeTraversal t = createTraversal(true, "other.js");
    Node nameNode = new Node(Token.NAME, "myVar");
    Node parent = new Node(Token.EXPR_RESULT, nameNode);
    JSDocInfo docInfo = JSDocInfo.Builder.mutable().setVisibility(PRIVATE).setSourceName("source.js").build();
    // Mark not constructor
    Scope.Var var = createVar("myVar", docInfo);
    TestScope scope = new TestScope();
    scope.varMap.put("myVar", var);
    ((testCompiler) compiler).setScope(scope);
    pass.visit(t, nameNode, parent);
    testCompiler tc = (testCompiler) compiler;
    assertEquals(1, tc.errors.size());
    assertTrue(tc.errors.get(0).contains("Access to private variable"));
  }

  @Test(timeout = 4000)
  public void testCheckConstructorDeprecationDeprecatedNoReason() {
    NodeTraversal t = createTraversal(false, "file.js");
    Node newNode = new Node(Token.NEW, new Node(Token.NAME, "Foo"));
    FunctionType ctor = registry.createConstructorType("Foo", null, null, null);
    JSDocInfo.Builder builder = JSDocInfo.Builder.mutable();
    builder.setDeprecated(true);
    ctor.setJSDocInfo(builder.build());
    newNode.setJSType(ctor);
    pass.visit(t, newNode, new Node(Token.EXPR_RESULT));
    testCompiler tc = (testCompiler) compiler;
    assertEquals(1, tc.errors.size());
    assertTrue(tc.errors.get(0).contains("Class Foo has been deprecated."));
  }

  @Test(timeout = 4000)
  public void testCheckConstantPropertyReassignment() {
    NodeTraversal t = createTraversal(false, "file.js");
    Node nameNode = new Node(Token.NAME, "obj");
    Node getprop = new Node(Token.GETPROP, nameNode, Node.newString("constProp"));
    Node assign = new Node(Token.ASSIGN, getprop, new Node(Token.NUMBER, 5));
    ObjectType objType = registry.createAnonymousObjectType();
    JSDocInfo propDoc = JSDocInfo.Builder.mutable().setConstant(true).build();
    objType.defineDeclaredProperty("constProp", numberType, propDoc);
    objType.setReferenceName("MyClass");
    nameNode.setJSType(objType);
    getprop.getFirstChild().setJSType(objType);
    // First assignment should be recorded
    pass.visit(t, getprop, assign);
    assertTrue(((testCompiler) compiler).errors.isEmpty());
    // Second assignment
    Node assign2 = new Node(Token.ASSIGN, getprop.cloneTree(), new Node(Token.NUMBER, 6));
    pass.visit(t, getprop, assign2);
    testCompiler tc = (testCompiler) compiler;
    assertEquals(1, tc.errors.size());
    assertTrue(tc.errors.get(0).contains("constant property"));
  }

  @Test(timeout = 4000)
  public void testCheckNameDeprecationDeprecatedWithReason() {
    NodeTraversal t = createTraversal(false, "file.js");
    Node nameNode = new Node(Token.NAME, "depVar");
    Node parent = new Node(Token.CALL, nameNode);
    JSDocInfo docInfo = JSDocInfo.Builder.mutable().setDeprecated(true).setDeprecationReason("Use newer").build();
    Scope.Var var = createVar("depVar", docInfo);
    TestScope scope = new TestScope();
    scope.varMap.put("depVar", var);
    ((testCompiler) compiler).setScope(scope);
    pass.visit(t, nameNode, parent);
    testCompiler tc = (testCompiler) compiler;
    assertEquals(1, tc.errors.size());
    assertTrue(tc.errors.get(0).contains("deprecated: Use newer"));
  }

  @Test(timeout = 4000)
  public void testCheckPropertyDeprecationDeprecatedWithoutReason() {
    NodeTraversal t = createTraversal(false, "file.js");
    Node nameNode = new Node(Token.NAME, "obj");
    Node getprop = new Node(Token.GETPROP, nameNode, Node.newString("depProp"));
    Node parent = new Node(Token.GETPROP, getprop);
    ObjectType objType = registry.createAnonymousObjectType();
    JSDocInfo propDoc = JSDocInfo.Builder.mutable().setDeprecated(true).build();
    objType.defineDeclaredProperty("depProp", numberType, propDoc);
    nameNode.setJSType(objType);
    getprop.getFirstChild().setJSType(objType);
    pass.visit(t, getprop, parent);
    testCompiler tc = (testCompiler) compiler;
    assertEquals(1, tc.errors.size());
    assertTrue(tc.errors.get(0).contains("Property depProp of type"));
  }

  @Test(timeout = 4000)
  public void testCheckConstantPropertyPrototypeConstant() {
    NodeTraversal t = createTraversal(false, "file.js");
    Node nameNode = new Node(Token.NAME, "obj");
    Node getprop = new Node(Token.GETPROP, nameNode, Node.newString("protoConst"));
    Node assign = new Node(Token.ASSIGN, getprop, new Node(Token.NUMBER, 1));
    // Create instance type with prototype that has constant property
    ObjectType proto = registry.createAnonymousObjectType();
    JSDocInfo propDoc = JSDocInfo.Builder.mutable().setConstant(true).build();
    proto.defineDeclaredProperty("protoConst", numberType, propDoc);
    proto.setReferenceName("BaseProto");
    ObjectType instance = registry.createAnonymousObjectType();
    instance.setImplicitPrototype(proto);
    nameNode.setJSType(instance);
    getprop.getFirstChild().setJSType(instance);
    // First assignment on instance should record on prototype
    pass.visit(t, getprop, assign);
    assertTrue(((testCompiler) compiler).errors.isEmpty());
    // Second assignment via different instance should cause error
    Node nameNode2 = new Node(Token.NAME, "obj2");
    Node getprop2 = new Node(Token.GETPROP, nameNode2, Node.newString("protoConst"));
    Node assign2 = new Node(Token.ASSIGN, getprop2, new Node(Token.NUMBER, 2));
    nameNode2.setJSType(instance);
    getprop2.getFirstChild().setJSType(instance);
    pass.visit(t, getprop2, assign2);
    testCompiler tc = (testCompiler) compiler;
    assertEquals(1, tc.errors.size());
    assertTrue(tc.errors.get(0).contains("constant property"));
  }

  @Test(timeout = 4000)
  public void testEnterScopeAndExitScope() {
    NodeTraversal t = createTraversal(false);
    Node scopeRoot = new Node(Token.FUNCTION);
    Node parent = new Node(Token.ASSIGN, new Node(Token.NAME, "a"), scopeRoot);
    // Set JSType for function to be non-deprecated
    scopeRoot.setJSType(registry.getNativeType(JSTypeNative.NO_RESOLVED_TYPE));
    pass.enterScope(t);
    // methodDepth should be 1
    pass.exitScope(t);
    // methodDepth should be 0
    // No errors expected
  }

  // ---------- Partition D: Exception Defensive Guards ----------

  @Test(timeout = 4000, expected = NullPointerException.class)
  public void testVisitWithNullTraversal() {
    pass.visit(null, new Node(Token.NAME), new Node(Token.EXPR_RESULT));
  }

  // ---------- Partition E: Lifecycle & Contract ----------

  @Test(timeout = 4000)
  public void testConstructorInitializesProperties() throws Exception {
    Field field = CheckAccessControls.class.getDeclaredField("initializedConstantProperties");
    field.setAccessible(true);
    Multimap<String, String> map = (Multimap<String, String>) field.get(pass);
    assertNotNull(map);
    assertTrue(map.isEmpty());
  }

  @Test(timeout = 4000)
  public void testProcess() {
    // process on externs and root should not throw
    Node externs = new Node(Token.SCRIPT);
    Node root = new Node(Token.SCRIPT);
    pass.process(externs, root);
    // No specific assertion, just that it runs
  }

  // ---------- Helper classes ----------

  private static class TestCompiler extends AbstractCompiler {
    final List<String> errors = new java.util.ArrayList<>();
    private TestScope scope;
    private JSTypeRegistry typeRegistry;

    TestCompiler() {
      typeRegistry = new JSTypeRegistry(new ErrorReporter() {
        @Override public void warning(String message, int line, int charno) {}
        @Override public void error(String message, int line, int charno) {}
      });
    }

    void setScope(TestScope scope) { this.scope = scope; }

    @Override public void report(JSError error) {
      errors.add(error.description);
    }

    @Override public JSTypeRegistry getTypeRegistry() { return typeRegistry; }

    // Minimal overrides for abstract methods
    @Override public void reportCodeChange() {}
    @Override public void reportChange() {}
    @Override public boolean hasCompilerProperty(CompilerProperty property) { return false; }
    @Override public CompilerOptions getOptions() { return new CompilerOptions(); }
    @Override public ErrorManager getErrorManager() { return null; }
    @Override public CodingConvention getCodingConvention() { return new DefaultCodingConvention(); }
    @Override public boolean acceptEcmaScript5() { return false; }
    @Override public boolean acceptConstKeyword() { return false; }
    @Override public SourceFile getSourceFileByPath(String path) { return null; }
    @Override public SourceMap getSourceMap() { return null; }
    @Override public void process(CompilerInput... inputs) throws Exception {}
    @Override public void process(CompilerInput[] externs, CompilerInput[] inputs) throws Exception {}
    @Override public void process(CompilerInput externs, CompilerInput inputs) throws Exception {}
    @Override public Result getResult() { return null; }
    @Override public void setScope(Scope scope) { this.scope = (TestScope) scope; }
    @Override public Scope getScope() { return scope; }
    @Override public Node getRoot() { return null; }
  }

  private static class TestScope extends Scope {
    java.util.Map<String, Var> varMap = new java.util.HashMap<>();

    TestScope() {
      super(null, null); // simplified
    }

    @Override public Var getVar(String name) { return varMap.get(name); }
  }

  private static Var createVar(String name, JSDocInfo doc) {
    // We need to create a Var instance. Var is an inner class of Scope.
    // We can create a minimal anonymous extension.
    // Alternatively, we can use Scope.Var's constructor but it's protected.
    // For simplicity, we create a mock via reflection but due to complexity we'll just return a dummy.
    // Better: Create a concrete implementation.
    return new Var() {
      @Override public String getName() { return name; }
      @Override public Node getNameNode() { return new Node(Token.NAME, name); }
      @Override public Scope getScope() { return null; }
      @Override public boolean isGlobal() { return false; }
      @Override public boolean isLocal() { return false; }
      @Override public boolean isParam() { return false; }
      @Override public boolean isThis() { return false; }
      @Override public boolean isHiddenTypeDeclaration() { return false; }
      @Override public boolean isInferredConst() { return false; }
      @Override public boolean isConst() { return false; }
      @Override public JSType getType() { return null; }
      @Override public void setType(JSType type) {}
      @Override public JSDocInfo getJSDocInfo() { return doc; }
      @Override public Node getParentNode() { return null; }
      @Override public boolean isBleedingFunction() { return false; }
      @Override public void setBleedingFunction(boolean bleeding) {}
    };
  }

  private NodeTraversal createTraversal(boolean global) {
    return createTraversal(global, "test.js");
  }

  private NodeTraversal createTraversal(boolean global, String inputName) {
    return new NodeTraversal((AbstractCompiler) compiler, null, null) {
      @Override public boolean inGlobalScope() { return global; }
      @Override public Scope getScope() {
        TestScope scope = new TestScope();
        ((testCompiler) compiler).setScope(scope);
        return scope;
      }
      @Override public CompilerInput getInput() {
        return new CompilerInput(SourceFile.fromCode(inputName, ""));
      }
      @Override public Node getScopeRoot() {
        return new Node(Token.SCRIPT);
      }
    };
  }
}