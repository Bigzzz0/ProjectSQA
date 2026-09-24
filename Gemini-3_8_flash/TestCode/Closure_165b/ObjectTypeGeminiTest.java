/* [Branch & Defect Analysis Matrix]
 * ====================================================================================
 * Class Under Test: com.google.javascript.rhino.jstype.ObjectType
 * Target Defect: Closure-70 / TypeCheckTest#testIssue725 (Scope / Property Redefinition)
 *
 * Decision / Condition Matrix Covered:
 * 1. Scope / Hierarchy:
 *    - getRootNode() == null (always)
 *    - getParentScope() returns getImplicitPrototype()
 *    - getSlot(name) vs getOwnSlot(name): hasOwnProperty(name) ? getSlot(name) : null
 *    - hasOwnDeclaredProperty(name): hasOwnProperty(name) && isPropertyTypeDeclared(name)
 *
 * 2. Prototype Chain & Cycles:
 *    - detectImplicitPrototypeCycle():
 *      - Chain without cycle (linear hierarchy A -> B -> C -> null) => false; ensures visited reset
 *      - Self cycle (A -> A) => true
 *      - Two-node cycle (A -> B -> A) => true
 *      - Multi-node subcycle (A -> B -> C -> B) => true
 *    - isImplicitPrototype(target):
 *      - Match on self => true
 *      - Match on ancestor => true
 *      - Unrelated type / null => false
 *
 * 3. Reference Names & Delegates:
 *    - getNormalizedReferenceName() & getDisplayName():
 *      - null reference name => null
 *      - name without '(' => exact name
 *      - name with '(' => prefix before '('
 *      - name starting with '(' => empty string
 *    - createDelegateSuffix("foo") => "(foo)"
 *
 * 4. Property Inferred vs Declared & Type Supertyping (Defect Zone - Issue 725):
 *    - defineDeclaredProperty: inferred=false, registered in registry
 *    - defineInferredProperty:
 *      - property does not exist => uses given type directly
 *      - property exists on type => type.getLeastSupertype(existingType)
 *      - existing property type is null => fallback to given type
 *    - findPropertyType(name): hasProperty(name) ? getPropertyType(name) : null
 *
 * 5. Caching & Unknown Resolution:
 *    - isUnknownType() / hasCachedValues() / clearCachedValues():
 *      - unknown cache hits (!unknown)
 *      - implicitProto == null => resolved false
 *      - implicitProto isNativeObjectType => checks extended interfaces
 *      - implicitProto unknown => propagates implicitProto.isUnknownType()
 *      - extended interface unknown => unknown = true
 *
 * 6. Equality & Casting:
 *    - testForEquality:
 *      - identical reference => TRUE
 *      - subtype of OBJECT_NUMBER_STRING_BOOLEAN => UNKNOWN
 *      - not subtype of OBJECT_NUMBER_STRING_BOOLEAN => FALSE
 *    - cast(JSType): null => null; ObjectType => ObjectType; primitive => null
 *
 * 7. Property Static Inner Class:
 *    - All accessors: name, type, inferred, node, docInfo, symbol, declaration
 *    - node null vs non-null (getSourceFile, isFromExterns, getDeclaration)
 *    - serialization round-trip
 * ====================================================================================
 */

package com.google.javascript.rhino.jstype;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SimpleErrorReporter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ObjectTypeGeminiTest {

  private final JSTypeRegistry registry = new JSTypeRegistry(new SimpleErrorReporter());

  /**
   * Controlled concrete implementation of abstract ObjectType for unit testing
   * branches and contracts.
   */
  private static class StubObjectType extends ObjectType {
    private static final long serialVersionUID = 1L;

    private ObjectType implicitPrototype;
    private final Map<String, Property> slotMap = new HashMap<>();
    private final Map<String, JSType> propertyTypeMap = new HashMap<>();
    private final Set<String> declaredProps = new HashSet<>();
    private final Set<String> ownProps = new HashSet<>();
    private String referenceName;
    private FunctionType constructor;
    private FunctionType ownerFunction;
    private final List<ObjectType> extendedInterfaces = new ArrayList<>();
    private boolean nativeObjectType = false;

    StubObjectType(JSTypeRegistry registry, String referenceName, ObjectType implicitPrototype) {
      super(registry);
      this.referenceName = referenceName;
      this.implicitPrototype = implicitPrototype;
    }

    void setImplicitPrototype(ObjectType proto) {
      this.implicitPrototype = proto;
    }

    void setNativeObjectType(boolean val) {
      this.nativeObjectType = val;
    }

    @Override
    public boolean isNativeObjectType() {
      return nativeObjectType;
    }

    void addExtendedInterface(ObjectType iface) {
      this.extendedInterfaces.add(iface);
    }

    @Override
    public Iterable<ObjectType> getCtorExtendedInterfaces() {
      return extendedInterfaces;
    }

    void setOwnerFunctionExplicit(FunctionType fn) {
      this.ownerFunction = fn;
    }

    @Override
    public FunctionType getOwnerFunction() {
      return ownerFunction;
    }

    @Override
    public Property getSlot(String name) {
      if (slotMap.containsKey(name)) {
        return slotMap.get(name);
      }
      if (implicitPrototype != null) {
        return implicitPrototype.getSlot(name);
      }
      return null;
    }

    @Override
    public String getReferenceName() {
      return referenceName;
    }

    @Override
    public FunctionType getConstructor() {
      return constructor;
    }

    void setConstructor(FunctionType ctor) {
      this.constructor = ctor;
    }

    @Override
    public ObjectType getImplicitPrototype() {
      return implicitPrototype;
    }

    @Override
    boolean defineProperty(String propertyName, JSType type, boolean inferred, Node propertyNode) {
      Property prop = new Property(propertyName, type, inferred, propertyNode);
      slotMap.put(propertyName, prop);
      propertyTypeMap.put(propertyName, type);
      ownProps.add(propertyName);
      if (!inferred) {
        declaredProps.add(propertyName);
      } else {
        declaredProps.remove(propertyName);
      }
      return true;
    }

    @Override
    public JSType getPropertyType(String propertyName) {
      if (propertyTypeMap.containsKey(propertyName)) {
        return propertyTypeMap.get(propertyName);
      }
      if (implicitPrototype != null && implicitPrototype.hasProperty(propertyName)) {
        return implicitPrototype.getPropertyType(propertyName);
      }
      return getNativeType(JSTypeNative.UNKNOWN_TYPE);
    }

    @Override
    public boolean hasProperty(String propertyName) {
      return ownProps.contains(propertyName) ||
          (implicitPrototype != null && implicitPrototype.hasProperty(propertyName));
    }

    @Override
    public boolean hasOwnProperty(String propertyName) {
      return ownProps.contains(propertyName);
    }

    @Override
    public boolean isPropertyTypeInferred(String propertyName) {
      return !declaredProps.contains(propertyName);
    }

    @Override
    public boolean isPropertyTypeDeclared(String propertyName) {
      return declaredProps.contains(propertyName);
    }

    @Override
    public int getPropertiesCount() {
      return ownProps.size();
    }

    @Override
    void collectPropertyNames(Set<String> props) {
      props.addAll(ownProps);
      if (implicitPrototype != null) {
        implicitPrototype.collectPropertyNames(props);
      }
    }
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testCorePropertiesAndDefaults() {
    StubObjectType obj = new StubObjectType(registry, "Person", null);

    assertNull("Root node of ObjectType must be null", obj.getRootNode());
    assertNull("Scope typeOfThis must be null", obj.getTypeOfThis());
    assertNull("Parameter type defaults to null", obj.getParameterType());
    assertNull("Index type defaults to null", obj.getIndexType());
    assertNull("Property node defaults to null", obj.getPropertyNode("prop"));
    assertNull("Own property JSDoc defaults to null", obj.getOwnPropertyJSDocInfo("prop"));
    assertFalse("Default removeProperty is false", obj.removeProperty("prop"));
    assertFalse("Default hasReferenceName is false", obj.hasReferenceName());
    assertFalse("Default isPropertyInExterns is false", obj.isPropertyInExterns("prop"));
    assertEquals(Collections.emptySet(), obj.getOwnPropertyNames());
    assertEquals(BooleanLiteralSet.TRUE, obj.getPossibleToBooleanOutcomes());
    assertTrue(obj.isObject());
    assertFalse(obj.isNativeObjectType());
    assertFalse(obj.isFunctionPrototypeType());

    // setPropertyJSDocInfo default is a safe no-op
    obj.setPropertyJSDocInfo("prop", new JSDocInfo());
    obj.setOwnerFunction(null);
  }

  @Test(timeout = 4000)
  public void testParentScopeHierarchy() {
    StubObjectType parent = new StubObjectType(registry, "Parent", null);
    StubObjectType child = new StubObjectType(registry, "Child", parent);

    assertNull("Parent scope of parent is null", parent.getParentScope());
    assertSame("Parent scope of child must be its implicit prototype", parent, child.getParentScope());
  }

  @Test(timeout = 4000)
  public void testJSDocInfoInheritanceChain() {
    StubObjectType grandparent = new StubObjectType(registry, "Grandparent", null);
    StubObjectType parent = new StubObjectType(registry, "Parent", grandparent);
    StubObjectType child = new StubObjectType(registry, "Child", parent);

    assertNull(child.getJSDocInfo());

    JSDocInfo gpDoc = new JSDocInfo();
    grandparent.setJSDocInfo(gpDoc);
    assertSame("DocInfo should resolve through prototype chain", gpDoc, child.getJSDocInfo());

    JSDocInfo parentDoc = new JSDocInfo();
    parent.setJSDocInfo(parentDoc);
    assertSame("DocInfo should resolve to closest prototype in chain", parentDoc, child.getJSDocInfo());

    JSDocInfo childDoc = new JSDocInfo();
    child.setJSDocInfo(childDoc);
    assertSame("Own docInfo takes precedence over prototype chain", childDoc, child.getJSDocInfo());

    child.setJSDocInfo(null);
    assertSame("Reverting own docInfo falls back to parent", parentDoc, child.getJSDocInfo());
  }

  @Test(timeout = 4000)
  public void testNormalizedReferenceNameAndDisplayName() {
    StubObjectType regular = new StubObjectType(registry, "MyClass", null);
    assertEquals("MyClass", regular.getNormalizedReferenceName());
    assertEquals("MyClass", regular.getDisplayName());

    StubObjectType delegate = new StubObjectType(registry, "MyClass(delegateSuffix)", null);
    assertEquals("MyClass", delegate.getNormalizedReferenceName());
    assertEquals("MyClass", delegate.getDisplayName());

    StubObjectType anonPrefix = new StubObjectType(registry, "(anon)", null);
    assertEquals("", anonPrefix.getNormalizedReferenceName());

    StubObjectType anonymous = new StubObjectType(registry, null, null);
    assertNull(anonymous.getNormalizedReferenceName());
    assertNull(anonymous.getDisplayName());

    assertEquals("(delegate_suffix)", ObjectType.createDelegateSuffix("delegate_suffix"));
  }

  @Test(timeout = 4000)
  public void testPropertyNamesCollection() {
    StubObjectType base = new StubObjectType(registry, "Base", null);
    base.defineProperty("zProp", registry.getNativeType(JSTypeNative.NUMBER_TYPE), false, null);
    base.defineProperty("aProp", registry.getNativeType(JSTypeNative.STRING_TYPE), false, null);

    StubObjectType derived = new StubObjectType(registry, "Derived", base);
    derived.defineProperty("mProp", registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), false, null);

    Set<String> allProps = derived.getPropertyNames();
    assertEquals(3, allProps.size());
    // Tree-set sorting verification
    List<String> list = new ArrayList<>(allProps);
    assertEquals("aProp", list.get(0));
    assertEquals("mProp", list.get(1));
    assertEquals("zProp", list.get(2));
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testDetectImplicitPrototypeCycleScenarios() {
    // Case 1: Null prototype
    StubObjectType standalone = new StubObjectType(registry, "A", null);
    assertFalse(standalone.detectImplicitPrototypeCycle());

    // Case 2: Linear acyclic chain A -> B -> C -> null
    StubObjectType c = new StubObjectType(registry, "C", null);
    StubObjectType b = new StubObjectType(registry, "B", c);
    StubObjectType a = new StubObjectType(registry, "A", b);
    assertFalse(a.detectImplicitPrototypeCycle());
    assertFalse(b.detectImplicitPrototypeCycle());
    // Subsequent run must also be false, ensuring visited flags were properly reset
    assertFalse(a.detectImplicitPrototypeCycle());

    // Case 3: Self-cycle A -> A
    StubObjectType selfLoop = new StubObjectType(registry, "Self", null);
    selfLoop.setImplicitPrototype(selfLoop);
    assertTrue(selfLoop.detectImplicitPrototypeCycle());

    // Case 4: Two-node cycle A -> B -> A
    StubObjectType nodeA = new StubObjectType(registry, "NodeA", null);
    StubObjectType nodeB = new StubObjectType(registry, "NodeB", nodeA);
    nodeA.setImplicitPrototype(nodeB);
    assertTrue(nodeA.detectImplicitPrototypeCycle());
    assertTrue(nodeB.detectImplicitPrototypeCycle());

    // Case 5: Deep subcycle A -> B -> C -> B
    StubObjectType subA = new StubObjectType(registry, "SubA", null);
    StubObjectType subB = new StubObjectType(registry, "SubB", null);
    StubObjectType subC = new StubObjectType(registry, "SubC", subB);
    subB.setImplicitPrototype(subC);
    subA.setImplicitPrototype(subB);
    assertTrue(subA.detectImplicitPrototypeCycle());
  }

  @Test(timeout = 4000)
  public void testIsImplicitPrototypeEquivalence() {
    StubObjectType protoGrandParent = new StubObjectType(registry, "GP", null);
    StubObjectType protoParent = new StubObjectType(registry, "P", protoGrandParent);
    StubObjectType instance = new StubObjectType(registry, "I", protoParent);
    StubObjectType stranger = new StubObjectType(registry, "Stranger", null);

    assertTrue("Self equivalence in prototype chain must be true", instance.isImplicitPrototype(instance));
    assertTrue("Parent in chain must be true", instance.isImplicitPrototype(protoParent));
    assertTrue("Grandparent in chain must be true", instance.isImplicitPrototype(protoGrandParent));
    assertFalse("Unrelated object must be false", instance.isImplicitPrototype(stranger));
    assertFalse("Null prototype must be false", instance.isImplicitPrototype(null));
  }

  @Test(timeout = 4000)
  public void testUnknownTypeResolutionAndCaching() {
    StubObjectType obj = new StubObjectType(registry, "T", null);
    assertFalse("Initially hasCachedValues is false", obj.hasCachedValues());

    // Base resolved without prototype
    assertFalse("isUnknownType should be false for concrete prototype", obj.isUnknownType());
    assertTrue("hasCachedValues should now be true", obj.hasCachedValues());

    obj.clearCachedValues();
    assertFalse("After clearCachedValues, hasCachedValues is false", obj.hasCachedValues());

    // Native implicit prototype path
    StubObjectType nativeProto = new StubObjectType(registry, "NativeProto", null);
    nativeProto.setNativeObjectType(true);
    StubObjectType childOfNative = new StubObjectType(registry, "ChildNative", nativeProto);
    assertFalse(childOfNative.isUnknownType());

    // Interface with unknown type forces parent to unknown
    StubObjectType unknownIface = new StubObjectType(registry, "UnknownIface", null) {
      private static final long serialVersionUID = 1L;
      @Override public boolean isUnknownType() { return true; }
    };
    StubObjectType ifaceChild = new StubObjectType(registry, "IfaceChild", null);
    ifaceChild.addExtendedInterface(unknownIface);
    assertTrue(ifaceChild.isUnknownType());

    // Non-native unknown implicit prototype
    StubObjectType unknownProto = new StubObjectType(registry, "UnknownProto", null) {
      private static final long serialVersionUID = 1L;
      @Override public boolean isUnknownType() { return true; }
    };
    StubObjectType childOfUnknownProto = new StubObjectType(registry, "ChildUnknown", unknownProto);
    assertTrue(childOfUnknownProto.isUnknownType());
  }

  @Test(timeout = 4000)
  public void testCastMethod() {
    assertNull("cast(null) must return null", ObjectType.cast(null));

    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    assertNull("cast(PrimitiveType) must return null", ObjectType.cast(numberType));

    StubObjectType stubObj = new StubObjectType(registry, "Obj", null);
    assertSame("cast(ObjectType) must return self", stubObj, ObjectType.cast(stubObj));

    JSType nativeObj = registry.getNativeType(JSTypeNative.OBJECT_TYPE);
    assertNotNull(ObjectType.cast(nativeObj));
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Issue 725 Regression Check)
  // =========================================================================

  /**
   * Targets the defect condition identified in TypeCheckTest#testIssue725
   * involving inferred vs declared property redefinition across prototype and local slots.
   */
  @Test(timeout = 4000)
  public void testIssue725_InferredPropertyRedefinitionOnPrototypeHierarchy() {
    StubObjectType prototype = new StubObjectType(registry, "BaseProto", null);
    Node declNode = new Node(0);
    // Explicitly declare property 'x' as number on prototype
    prototype.defineDeclaredProperty("x", registry.getNativeType(JSTypeNative.NUMBER_TYPE), declNode);

    StubObjectType instance = new StubObjectType(registry, "Instance", prototype);
    assertTrue("Instance must inherit property 'x'", instance.hasProperty("x"));
    assertFalse("Instance must not have own property 'x' before definition", instance.hasOwnProperty("x"));
    assertFalse("Instance must not have own declared property 'x'", instance.hasOwnDeclaredProperty("x"));
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), instance.findPropertyType("x"));

    // Redefine inferred property 'x' on instance with String type
    Node inferNode = new Node(0);
    boolean success = instance.defineInferredProperty("x", registry.getNativeType(JSTypeNative.STRING_TYPE), inferNode);
    assertTrue(success);

    // Verify state after redefinition
    assertTrue("Instance now has own property", instance.hasOwnProperty("x"));
    assertTrue("Instance property must be inferred", instance.isPropertyTypeInferred("x"));
    assertFalse("Instance property is not declared", instance.isPropertyTypeDeclared("x"));
    assertFalse("Instance still has no own DECLARED property", instance.hasOwnDeclaredProperty("x"));

    // Verify least supertype resolution: Number | String
    JSType resolvedType = instance.getPropertyType("x");
    assertTrue("Resolved type must be a union type", resolvedType.isUnionType());
    assertTrue("Resolved type must be supertype of number",
        resolvedType.isSubtype(registry.getNativeType(JSTypeNative.NUMBER_TYPE)) == false);
    assertTrue("Number must be a subtype of the union",
        registry.getNativeType(JSTypeNative.NUMBER_TYPE).isSubtype(resolvedType));
    assertTrue("String must be a subtype of the union",
        registry.getNativeType(JSTypeNative.STRING_TYPE).isSubtype(resolvedType));

    // Verify own slot vs prototype slot isolation
    Property ownSlot = instance.getOwnSlot("x");
    assertNotNull("Own slot must be present on instance", ownSlot);
    assertSame(inferNode, ownSlot.getNode());
    assertTrue(ownSlot.isTypeInferred());

    Property protoSlot = prototype.getOwnSlot("x");
    assertNotNull(protoSlot);
    assertSame(declNode, protoSlot.getNode());
    assertFalse(protoSlot.isTypeInferred());
  }

  @Test(timeout = 4000)
  public void testDefineInferredPropertyWhenOriginalTypeIsNull() {
    StubObjectType mockObj = new StubObjectType(registry, "MockNullProp", null) {
      private static final long serialVersionUID = 1L;
      @Override public boolean hasProperty(String name) { return "special".equals(name); }
      @Override public JSType getPropertyType(String name) { return null; }
    };

    boolean defined = mockObj.defineInferredProperty("special", registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), null);
    assertTrue(defined);
    assertEquals(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), mockObj.getPropertyType("special"));
  }

  // =========================================================================
  // Partition D: Equality, Subtyping & Visitor Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testTestForEqualityBranches() {
    StubObjectType objA = new StubObjectType(registry, "A", null);
    StubObjectType objB = new StubObjectType(registry, "B", null);

    // Super identity match
    assertEquals(TernaryValue.TRUE, objA.testForEquality(objA));

    // Comparable to other object type => UNKNOWN
    assertEquals(TernaryValue.UNKNOWN, objA.testForEquality(objB));

    // Comparable to primitive string/number => UNKNOWN
    assertEquals(TernaryValue.UNKNOWN, objA.testForEquality(registry.getNativeType(JSTypeNative.STRING_TYPE)));
    assertEquals(TernaryValue.UNKNOWN, objA.testForEquality(registry.getNativeType(JSTypeNative.NUMBER_TYPE)));

    // Comparable to null/undefined handled by super => FALSE
    assertEquals(TernaryValue.FALSE, objA.testForEquality(registry.getNativeType(JSTypeNative.NULL_TYPE)));
    assertEquals(TernaryValue.FALSE, objA.testForEquality(registry.getNativeType(JSTypeNative.VOID_TYPE)));

    // Non-subtype branch: custom type returning false for isSubtype(OBJECT_NUMBER_STRING_BOOLEAN)
    JSType customNonSubtype = new StubObjectType(registry, "Custom", null) {
      private static final long serialVersionUID = 1L;
      @Override public boolean isSubtype(JSType that) { return false; }
    };
    assertEquals(TernaryValue.FALSE, objA.testForEquality(customNonSubtype));
  }

  @Test(timeout = 4000)
  public void testVisitDynamicProxy() {
    StubObjectType obj = new StubObjectType(registry, "Visitable", null);

    @SuppressWarnings("unchecked")
    Visitor<String> visitor = (Visitor<String>) Proxy.newProxyInstance(
        Visitor.class.getClassLoader(),
        new Class<?>[] { Visitor.class },
        new InvocationHandler() {
          @Override
          public Object invoke(Object proxy, Method method, Object[] args) {
            if ("caseObjectType".equals(method.getName())) {
              return "visited:" + ((ObjectType) args[0]).getReferenceName();
            }
            return null;
          }
        });

    assertEquals("visited:Visitable", obj.visit(visitor));
  }

  @Test(timeout = 4000)
  public void testOwnerFunctionAndInterfaceDefaults() {
    StubObjectType obj = new StubObjectType(registry, "FnProto", null);
    assertNull(obj.getOwnerFunction());
    assertFalse(obj.isFunctionPrototypeType());

    FunctionType fnType = (FunctionType) registry.getNativeType(JSTypeNative.FUNCTION_TYPE);
    obj.setOwnerFunctionExplicit(fnType);
    assertSame(fnType, obj.getOwnerFunction());
    assertTrue(obj.isFunctionPrototypeType());

    assertFalse(obj.getCtorImplementedInterfaces().iterator().hasNext());
    assertFalse(obj.getCtorExtendedInterfaces().iterator().hasNext());
  }

  @Test(timeout = 4000)
  public void testFindPropertyTypeAndSlots() {
    StubObjectType obj = new StubObjectType(registry, "Target", null);
    obj.defineDeclaredProperty("active", registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), null);

    assertEquals(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), obj.findPropertyType("active"));
    assertNull("Non-existent property returns null", obj.findPropertyType("missing"));

    assertNotNull(obj.getOwnSlot("active"));
    assertNull(obj.getOwnSlot("missing"));
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Property Inner Class Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testPropertyInnerClassWithNullNode() {
    ObjectType.Property prop = new ObjectType.Property(
        "id", registry.getNativeType(JSTypeNative.NUMBER_TYPE), true, null);

    assertEquals("id", prop.getName());
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), prop.getType());
    assertTrue(prop.isTypeInferred());
    assertNull(prop.getNode());
    assertNull(prop.getSourceFile());
    assertNull(prop.getDeclaration());
    assertSame("getSymbol must return self", prop, prop.getSymbol());
    assertFalse("isFromExterns with null node must be false", prop.isFromExterns());
    assertNull(prop.getJSDocInfo());

    // Mutators
    prop.setType(registry.getNativeType(JSTypeNative.STRING_TYPE));
    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), prop.getType());

    JSDocInfo info = new JSDocInfo();
    prop.setJSDocInfo(info);
    assertSame(info, prop.getJSDocInfo());

    Node dummy = new Node(0);
    prop.setNode(dummy);
    assertSame(dummy, prop.getNode());
    assertSame("With node set, getDeclaration returns self", prop, prop.getDeclaration());
    assertFalse(prop.isFromExterns());
  }

  @Test(timeout = 4000)
  public void testPropertyInnerClassSerialization() throws Exception {
    ObjectType.Property prop = new ObjectType.Property(
        "serialProp", registry.getNativeType(JSTypeNative.STRING_TYPE), false, null);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(baos);
    oos.writeObject(prop);
    oos.close();

    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    ObjectInputStream ois = new ObjectInputStream(bais);
    ObjectType.Property deserialized = (ObjectType.Property) ois.readObject();

    assertEquals("serialProp", deserialized.getName());
    assertFalse(deserialized.isTypeInferred());
    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), deserialized.getType());
    assertNull(deserialized.getNode());
  }
}