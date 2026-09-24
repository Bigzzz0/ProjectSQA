package com.google.javascript.rhino.jstype;

/* [Branch & Defect Analysis Matrix]
 * Target Class: com.google.javascript.rhino.jstype.NamedType
 *
 * Branch & Partition Coverage:
 * -----------------------------------------------------------------------------------------
 * Partition A: Core Functional Logic & State Transitions
 *   - Constructor: Preconditions.checkNotNull(reference) guard and fields initialization.
 *   - getReferenceName, toStringHelper(boolean), hasReferenceName, isNamedType, isNominalType.
 *   - defineProperty:
 *     - branch (!isResolved()): Buffers property into propertyContinuations.
 *     - branch (isResolved()): Delegated to super.defineProperty.
 *   - finishPropertyContinuations:
 *     - referencedObjType != null && !referencedObjType.isUnknownType(): Commits buffered properties.
 *     - referencedObjType == null || isUnknownType(): Clears continuations without commit.
 *   - setValidator:
 *     - branch (!isResolved()): Buffers validator in this.validator.
 *     - branch (isResolved()): Delegates to super.setValidator(validator).
 *
 * Partition B: Boundary Value Analysis (BVA) & Extremes
 *   - Constructor null reference -> NullPointerException.
 *   - lookupViaProperties:
 *     - componentNames[0].length() == 0 (e.g. empty string or leading dot ".foo") -> returns null.
 *     - enclosing.getSlot(name) == null -> returns null.
 *     - slot.getType() == null -> triggers getTypedefType() -> handleUnresolvedType().
 *     - slot.getType().isAllType() == true -> returns null.
 *     - slot.getType().isNoType() == true -> returns null.
 *     - Multi-component traversal ("a.b.c"):
 *       - intermediate component is not ObjectType (e.g. NumberType) -> returns null.
 *       - intermediate component name empty ("a..b") -> returns null.
 *       - successful navigation through nested ObjectType properties.
 *
 * Partition C: Defect-Targeted Branch Zone (Defects4J Closure-4 Ground Truth)
 *   - Closure-4: Resolution of named types in inheritance / interface cycles causing StackOverflowError
 *     or failing to report "Cycle detected in inheritance chain of type ...".
 *   - checkEnumElementCycle: referencedType instanceof EnumElementType and getPrimitiveType() == this.
 *   - detectImplicitPrototypeCycle after resolveViaRegistry and resolveViaProperties:
 *     Triggers handleTypeCycle, resets to UNKNOWN_TYPE, and emits cycle warning.
 *
 * Partition D: Exception & Defensive Guard Paths
 *   - resolveViaProperties resolution outcomes:
 *     - FunctionType (constructor or interface) -> resolves to instanceType.
 *     - NoObjectType -> resolves to NO_OBJECT_TYPE instance.
 *     - EnumType -> resolves to getElementsType().
 *     - fallback (null / unknown / non-nominal) -> handleUnresolvedType:
 *       - isLastGeneration() == true && isForwardDeclaredType() -> NO_RESOLVED_TYPE, validator applied.
 *       - isLastGeneration() == true && !isForwardDeclaredType() -> warning emitted.
 *       - isLastGeneration() == false -> setResolvedTypeInternal(this).
 *
 * Partition E: Object Lifecycle & Contract Integrity
 *   - hashCode integrity: contract matching reference.hashCode().
 *   - resolution generation: registry.isLastGeneration() returns getReferencedType() vs this.
 */

import com.google.common.base.Predicate;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SimpleErrorReporter;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class NamedTypeGeminiTest {

  private JSTypeRegistry registry;
  private TestErrorReporter errorReporter;
  private TestScope scope;

  private static class TestErrorReporter extends SimpleErrorReporter {
    final List<String> warnings = new ArrayList<>();
    final List<String> errors = new ArrayList<>();

    @Override
    public void warning(String message, String sourceName, int line, int lineOffset) {
      warnings.add(message);
    }

    @Override
    public void error(String message, String sourceName, int line, int lineOffset) {
      errors.add(message);
    }
  }

  private static class TestScope implements StaticScope<JSType> {
    private final Map<String, StaticSlot<JSType>> slots = new HashMap<>();

    void addSlot(String name, JSType type) {
      slots.put(name, new SimpleSlot(name, type, false));
    }

    @Override
    public Node getRootNode() {
      return null;
    }

    @Override
    public StaticScope<JSType> getParentScope() {
      return null;
    }

    @Override
    public StaticSlot<JSType> getSlot(String name) {
      return slots.get(name);
    }

    @Override
    public StaticSlot<JSType> getOwnSlot(String name) {
      return slots.get(name);
    }

    @Override
    public JSType getTypeOfThis() {
      return null;
    }
  }

  @Before
  public void setUp() {
    errorReporter = new TestErrorReporter();
    registry = new JSTypeRegistry(errorReporter);
    scope = new TestScope();
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testBasicGettersAndContractIntegrity() {
    NamedType namedType = new NamedType(registry, "MyCustomType", "sample.js", 12, 4);

    assertEquals("MyCustomType", namedType.getReferenceName());
    assertTrue(namedType.hasReferenceName());
    assertTrue(namedType.isNamedType());
    assertTrue(namedType.isNominalType());
    assertEquals("MyCustomType", namedType.toStringHelper(false));
    assertEquals("MyCustomType", namedType.toStringHelper(true));
    assertEquals("MyCustomType".hashCode(), namedType.hashCode());
    assertNotNull(namedType.getReferencedType());
    assertTrue(namedType.getReferencedType().isUnknownType());
  }

  @Test(timeout = 4000)
  public void testDefinePropertyBufferedBeforeResolutionAndCommittedAfter() {
    ObjectType targetObj = registry.createAnonymousObjectType();
    registry.declareType("TargetObj", targetObj);

    NamedType namedType = new NamedType(registry, "TargetObj", "sample.js", 1, 0);

    // Buffer property when unresolved
    Node propNode = new Node(0);
    boolean definedBefore = namedType.defineProperty("propA",
        registry.getNativeType(JSTypeNative.NUMBER_TYPE), false, propNode);
    assertTrue(definedBefore);
    assertFalse(targetObj.hasProperty("propA"));

    // Resolve to targetObj - continuations should commit
    registry.setLastGeneration(true);
    JSType resolved = namedType.resolve(errorReporter, scope);

    assertEquals(targetObj, resolved);
    assertTrue(targetObj.hasProperty("propA"));
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), targetObj.getPropertyType("propA"));

    // Define property when already resolved -> goes directly to super.defineProperty
    boolean definedAfter = namedType.defineProperty("propB",
        registry.getNativeType(JSTypeNative.STRING_TYPE), true, propNode);
    assertTrue(definedAfter);
    assertTrue(targetObj.hasProperty("propB"));
  }

  @Test(timeout = 4000)
  public void testFinishPropertyContinuationsWithUnknownTypeDoesNotCommit() {
    NamedType namedType = new NamedType(registry, "UnknownTarget", "sample.js", 1, 0);
    namedType.defineProperty("bufferedProp",
        registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), false, null);

    registry.setLastGeneration(false);
    namedType.resolve(errorReporter, scope);

    // Continuations cleared without throwing exception
    assertFalse(namedType.hasProperty("bufferedProp"));
  }

  @Test(timeout = 4000)
  public void testSetValidatorBeforeAndAfterResolution() {
    final boolean[] validatorCalled = new boolean[]{false};
    Predicate<JSType> validator = new Predicate<JSType>() {
      @Override
      public boolean apply(JSType input) {
        validatorCalled[0] = true;
        return true;
      }
    };

    ObjectType target = registry.createAnonymousObjectType();
    registry.declareType("ValidType", target);

    NamedType namedType = new NamedType(registry, "ValidType", "sample.js", 1, 0);
    assertTrue(namedType.setValidator(validator));
    assertFalse(validatorCalled[0]);

    // Validator should be executed upon resolution
    registry.setLastGeneration(true);
    namedType.resolve(errorReporter, scope);
    assertTrue(validatorCalled[0]);

    // Set validator after resolution delegates to super
    boolean secondarySet = namedType.setValidator(new Predicate<JSType>() {
      @Override
      public boolean apply(JSType input) {
        return true;
      }
    });
    assertTrue(secondarySet);
  }

  @Test(timeout = 4000)
  public void testResolveViaRegistryReturnsThisWhenNotLastGeneration() {
    ObjectType target = registry.createAnonymousObjectType();
    registry.declareType("GenType", target);

    NamedType namedType = new NamedType(registry, "GenType", "sample.js", 1, 0);
    registry.setLastGeneration(false);

    JSType result = namedType.resolve(errorReporter, scope);
    assertSame(namedType, result);
    assertSame(target, namedType.getReferencedType());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(expected = NullPointerException.class, timeout = 4000)
  public void testConstructorNullReferenceThrowsException() {
    new NamedType(registry, null, "sample.js", 1, 1);
  }

  @Test(timeout = 4000)
  public void testLookupViaPropertiesEmptyComponentNameReturnsNull() {
    NamedType emptyName = new NamedType(registry, "", "sample.js", 1, 0);
    registry.setLastGeneration(true);
    emptyName.resolve(errorReporter, scope);
    assertEquals(1, errorReporter.warnings.size());
    assertTrue(errorReporter.warnings.get(0).contains("Unknown type "));
  }

  @Test(timeout = 4000)
  public void testLookupViaPropertiesLeadingDotReturnsNull() {
    NamedType leadingDot = new NamedType(registry, ".nested", "sample.js", 1, 0);
    registry.setLastGeneration(true);
    leadingDot.resolve(errorReporter, scope);
    assertEquals(1, errorReporter.warnings.size());
  }

  @Test(timeout = 4000)
  public void testLookupViaPropertiesIntermediateEmptyReturnsNull() {
    ObjectType root = registry.createAnonymousObjectType();
    scope.addSlot("a", root);

    NamedType doubleDot = new NamedType(registry, "a..b", "sample.js", 1, 0);
    registry.setLastGeneration(true);
    doubleDot.resolve(errorReporter, scope);
    assertEquals(1, errorReporter.warnings.size());
  }

  @Test(timeout = 4000)
  public void testLookupViaPropertiesSlotNotFoundReturnsNull() {
    NamedType notFound = new NamedType(registry, "missingSymbol", "sample.js", 1, 0);
    registry.setLastGeneration(true);
    notFound.resolve(errorReporter, scope);
    assertEquals(1, errorReporter.warnings.size());
  }

  @Test(timeout = 4000)
  public void testLookupViaPropertiesSlotTypeAllOrNoTypeReturnsNull() {
    scope.addSlot("allTypeSlot", registry.getNativeType(JSTypeNative.ALL_TYPE));
    scope.addSlot("noTypeSlot", registry.getNativeType(JSTypeNative.NO_TYPE));

    NamedType namedAll = new NamedType(registry, "allTypeSlot", "sample.js", 1, 0);
    NamedType namedNo = new NamedType(registry, "noTypeSlot", "sample.js", 1, 0);

    registry.setLastGeneration(true);
    namedAll.resolve(errorReporter, scope);
    namedNo.resolve(errorReporter, scope);

    assertEquals(2, errorReporter.warnings.size());
  }

  @Test(timeout = 4000)
  public void testLookupViaPropertiesIntermediatePrimitiveReturnsNull() {
    scope.addSlot("numPrimitive", registry.getNativeType(JSTypeNative.NUMBER_TYPE));

    NamedType namedProp = new NamedType(registry, "numPrimitive.child", "sample.js", 1, 0);
    registry.setLastGeneration(true);
    namedProp.resolve(errorReporter, scope);

    assertEquals(1, errorReporter.warnings.size());
  }

  @Test(timeout = 4000)
  public void testLookupViaPropertiesDeepNestedSuccess() {
    ObjectType level1 = registry.createAnonymousObjectType();
    ObjectType level2 = registry.createAnonymousObjectType();
    FunctionType level3Ctor = registry.createConstructorType("Level3Ctor", null, null, null);

    level1.defineDeclaredProperty("l2", level2, null);
    level2.defineDeclaredProperty("l3", level3Ctor, null);
    scope.addSlot("l1", level1);

    NamedType namedType = new NamedType(registry, "l1.l2.l3", "sample.js", 1, 0);
    registry.setLastGeneration(true);
    JSType resolved = namedType.resolve(errorReporter, scope);

    assertSame(level3Ctor.getInstanceType(), resolved);
    assertEquals(0, errorReporter.warnings.size());
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Closure-4 Ground Truth & Cycles)
  // =========================================================================

  /**
   * Targets Defects4J Closure-4 failure condition:
   * StackOverflowError & Cycle detection failure during type resolution loop
   * (e.g. testConversionFromInterfaceToRecursiveConstructor & testImplementsLoop).
   */
  @Test(timeout = 4000)
  public void testImplementsLoop_defectRevealingCycleDetection() {
    // Create an interface FunctionType
    FunctionType ifaceType = registry.createInterfaceType("LoopIface", null);
    registry.declareType("LoopIface", ifaceType);
    scope.addSlot("LoopIface", ifaceType);

    NamedType namedType = new NamedType(registry, "LoopIface", "sample.js", 2, 5);

    // Introduce an inheritance loop on the instance prototype
    ObjectType instanceType = ifaceType.getInstanceType();
    instanceType.setImplicitPrototype(namedType);

    registry.setLastGeneration(true);
    JSType resolved = namedType.resolve(errorReporter, scope);

    // The resolution must detect the cycle, handle it gracefully without
    // throwing StackOverflowError, reset to UnknownType, and emit a warning.
    assertNotNull(resolved);
    assertTrue("Cycle warning must be recorded", errorReporter.warnings.size() > 0);
    boolean foundCycleWarning = false;
    for (String w : errorReporter.warnings) {
      if (w.contains("Cycle detected in inheritance chain of type LoopIface")) {
        foundCycleWarning = true;
        break;
      }
    }
    assertTrue("Expected cycle detection warning", foundCycleWarning);
    assertTrue(namedType.getReferencedType().isUnknownType());
  }

  @Test(timeout = 4000)
  public void testDetectImplicitPrototypeCycleViaRegistry() {
    ObjectType protoA = registry.createAnonymousObjectType();
    ObjectType protoB = registry.createAnonymousObjectType();
    protoA.setImplicitPrototype(protoB);
    protoB.setImplicitPrototype(protoA); // Direct prototype cycle

    registry.declareType("CyclicProto", protoA);
    NamedType namedType = new NamedType(registry, "CyclicProto", "sample.js", 10, 2);

    registry.setLastGeneration(true);
    namedType.resolve(errorReporter, scope);

    assertEquals(1, errorReporter.warnings.size());
    assertTrue(errorReporter.warnings.get(0).contains("Cycle detected in inheritance chain of type CyclicProto"));
    assertTrue(namedType.getReferencedType().isUnknownType());
  }

  @Test(timeout = 4000)
  public void testDetectImplicitPrototypeCycleViaProperties() {
    ObjectType protoCycle = registry.createAnonymousObjectType();
    protoCycle.setImplicitPrototype(protoCycle); // Self prototype cycle

    FunctionType ctor = registry.createConstructorType("CyclicCtor", null, null, null);
    ctor.getInstanceType().setImplicitPrototype(protoCycle);
    scope.addSlot("CyclicCtor", ctor);

    NamedType namedType = new NamedType(registry, "CyclicCtor", "sample.js", 15, 3);
    registry.setLastGeneration(true);
    namedType.resolve(errorReporter, scope);

    assertEquals(1, errorReporter.warnings.size());
    assertTrue(errorReporter.warnings.get(0).contains("Cycle detected in inheritance chain of type CyclicCtor"));
    assertTrue(namedType.getReferencedType().isUnknownType());
  }

  @Test(timeout = 4000)
  public void testCheckEnumElementCycle() {
    NamedType namedType = new NamedType(registry, "CycleEnum", "sample.js", 3, 1);
    EnumType enumType = registry.createEnumType("CycleEnum", null, namedType);

    registry.declareType("CycleEnum", enumType.getElementsType());

    registry.setLastGeneration(true);
    namedType.resolve(errorReporter, scope);

    assertEquals(1, errorReporter.warnings.size());
    assertTrue(errorReporter.warnings.get(0).contains("Cycle detected in inheritance chain of type CycleEnum"));
    assertTrue(namedType.getReferencedType().isUnknownType());
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testResolveViaPropertiesConstructorAndInterface() {
    FunctionType ctor = registry.createConstructorType("CtorA", null, null, null);
    FunctionType iface = registry.createInterfaceType("IfaceB", null);

    scope.addSlot("CtorA", ctor);
    scope.addSlot("IfaceB", iface);

    NamedType namedCtor = new NamedType(registry, "CtorA", "sample.js", 1, 0);
    NamedType namedIface = new NamedType(registry, "IfaceB", "sample.js", 2, 0);

    registry.setLastGeneration(true);
    JSType resCtor = namedCtor.resolve(errorReporter, scope);
    JSType resIface = namedIface.resolve(errorReporter, scope);

    assertSame(ctor.getInstanceType(), resCtor);
    assertSame(iface.getInstanceType(), resIface);
    assertEquals(0, errorReporter.warnings.size());
  }

  @Test(timeout = 4000)
  public void testResolveViaPropertiesNoObjectType() {
    JSType noObj = registry.getNativeType(JSTypeNative.NO_OBJECT_TYPE);
    scope.addSlot("noObjSymbol", noObj);

    NamedType namedType = new NamedType(registry, "noObjSymbol", "sample.js", 1, 0);
    registry.setLastGeneration(true);
    JSType resolved = namedType.resolve(errorReporter, scope);

    assertSame(registry.getNativeFunctionType(JSTypeNative.NO_OBJECT_TYPE).getInstanceType(), resolved);
  }

  @Test(timeout = 4000)
  public void testResolveViaPropertiesEnumType() {
    EnumType enumType = registry.createEnumType("StatusEnum", null,
        registry.getNativeType(JSTypeNative.STRING_TYPE));
    scope.addSlot("StatusEnum", enumType);

    NamedType namedType = new NamedType(registry, "StatusEnum", "sample.js", 1, 0);
    registry.setLastGeneration(true);
    JSType resolved = namedType.resolve(errorReporter, scope);

    assertSame(enumType.getElementsType(), resolved);
  }

  @Test(timeout = 4000)
  public void testHandleUnresolvedTypeForwardDeclared() {
    registry.forwardDeclareType("ForwardDeclaredLib");
    NamedType namedType = new NamedType(registry, "ForwardDeclaredLib", "sample.js", 1, 0);

    final boolean[] validatorInvoked = new boolean[]{false};
    namedType.setValidator(new Predicate<JSType>() {
      @Override
      public boolean apply(JSType input) {
        validatorInvoked[0] = true;
        return true;
      }
    });

    registry.setLastGeneration(true);
    JSType resolved = namedType.resolve(errorReporter, scope);

    assertSame(registry.getNativeObjectType(JSTypeNative.NO_RESOLVED_TYPE), resolved);
    assertTrue(validatorInvoked[0]);
    assertEquals(0, errorReporter.warnings.size());
  }

  @Test(timeout = 4000)
  public void testHandleUnresolvedTypeNotLastGenerationReturnsThis() {
    NamedType namedType = new NamedType(registry, "PendingForwardType", "sample.js", 1, 0);
    registry.setLastGeneration(false);

    JSType resolved = namedType.resolve(errorReporter, scope);

    assertSame(namedType, resolved);
    assertEquals(0, errorReporter.warnings.size());
  }

  @Test(timeout = 4000)
  public void testGetTypedefTypeWithNullSlotType() {
    NamedType namedType = new NamedType(registry, "TypedefSlot", "sample.js", 4, 1);
    SimpleSlot slotWithNull = new SimpleSlot("TypedefSlot", null, false);

    registry.setLastGeneration(true);
    JSType result = namedType.getTypedefType(errorReporter, slotWithNull, "TypedefSlot");

    assertNull(result);
    assertEquals(1, errorReporter.warnings.size());
    assertTrue(errorReporter.warnings.get(0).contains("Unknown type TypedefSlot"));
  }

  @Test(timeout = 4000)
  public void testGetTypedefTypeWithValidType() {
    NamedType namedType = new NamedType(registry, "ValidTypedef", "sample.js", 1, 0);
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    SimpleSlot slot = new SimpleSlot("ValidTypedef", numberType, false);

    JSType result = namedType.getTypedefType(errorReporter, slot, "ValidTypedef");

    assertSame(numberType, result);
    assertEquals(0, errorReporter.warnings.size());
  }
}