/* [Branch & Defect Analysis Matrix]
 * Target Class: com.google.javascript.rhino.jstype.JSTypeRegistry
 *
 * Decision / Condition Coverage Targets:
 * 1. resetForTypeCheck() / initializeBuiltInTypes() / initializeRegistry():
 *    - Verification of native type caches, prototypes, inheritance, arrows, constructors, and unions.
 * 2. Property Indexing & Subtyping:
 *    - registerPropertyOnType, unregisterPropertyOnType, getGreatestSubtypeWithProperty, canPropertyBeDefined
 *    - typesIndexedByProperty vs eachRefTypeIndexedByProperty (named types, union alternates, recursive reference indexing)
 *    - caching behavior in greatestSubtypeByProperty
 * 3. Scope Resolution & Named Types:
 *    - getType(StaticScope, String, String, int, int), resolveTypesInScope(StaticScope), incrementGeneration()
 *    - GLOBAL_THIS implicit prototype binding (Window object present vs absent)
 * 4. Factory & Builder Variations:
 *    - createOptionalType (UnknownType, AllType, standard types)
 *    - createNullableType, createOptionalNullableType, createDefaultObjectUnion (tolerateUndefinedValues = true/false)
 *    - createFunctionType / createConstructorType (varargs, optional, instance of this, return types)
 *    - resetImplicitPrototype (PrototypeObjectType vs non-PrototypeObjectType)
 * 5. Type Node AST Conversion (createFromTypeNodesInternal):
 *    - Token.LC (RecordType with COLON, quoted/unquoted keys, duplicate keys triggering warning)
 *    - Token.BANG, Token.QMARK (with/without child), Token.EQUALS, Token.ELLIPSIS, Token.STAR, Token.LB, Token.PIPE, Token.EMPTY, Token.VOID
 *    - Token.STRING: primitive types, Array/Object with parameter/index child nodes, nonNullableTypeNames checking
 *    - Token.FUNCTION: context nodes (THIS/NEW), parameter list (varargs with/without child, optional with invalid order warning)
 *    - Default / unexpected AST token exception handling
 * 6. Defects4J Targeted Area (TypeCheckTest::testIssue725):
 *    - Verifying warning emissions on unresolved types, invalid function context nodes, duplicate record fields,
 *      and property definition checks (e.g., prototype on Object vs Function).
 */

package com.google.javascript.rhino.jstype;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.common.collect.ImmutableList;
import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JSTypeRegistryGeminiTest {

  private static class RecordingErrorReporter implements ErrorReporter {
    final List<String> warnings = new ArrayList<String>();
    final List<String> errors = new ArrayList<String>();

    @Override
    public void warning(String message, String sourceName, int line, int lineOffset) {
      warnings.add(message);
    }

    @Override
    public void error(String message, String sourceName, int line, int lineOffset) {
      errors.add(message);
    }
  }

  private static class MockScope implements StaticScope<JSType> {
    private final StaticScope<JSType> parent;
    private final Map<String, StaticSlot<JSType>> slots = new HashMap<String, StaticSlot<JSType>>();

    MockScope(StaticScope<JSType> parent) {
      this.parent = parent;
    }

    @Override
    public Node getRootNode() {
      return null;
    }

    @Override
    public StaticScope<JSType> getParentScope() {
      return parent;
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

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testRegistryInitializationAndNativeTypes() {
    RecordingErrorReporter reporter = new RecordingErrorReporter();
    JSTypeRegistry registry = new JSTypeRegistry(reporter);

    assertSame(reporter, registry.getErrorReporter());
    assertFalse(registry.shouldTolerateUndefinedValues());
    assertEquals(JSTypeRegistry.ResolveMode.LAZY_NAMES, registry.getResolveMode());

    assertNotNull(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    assertNotNull(registry.getNativeType(JSTypeNative.STRING_TYPE));
    assertNotNull(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE));
    assertNotNull(registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE));
    assertNotNull(registry.getNativeFunctionType(JSTypeNative.OBJECT_FUNCTION_TYPE));
    assertNotNull(registry.getNativeType(JSTypeNative.ALL_TYPE));
    assertNotNull(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE));
    assertNotNull(registry.getNativeType(JSTypeNative.NO_TYPE));
    assertNotNull(registry.getNativeType(JSTypeNative.GLOBAL_THIS));

    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), registry.getType("number"));
    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), registry.getType("string"));
    assertEquals(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), registry.getType("boolean"));
    assertEquals(registry.getNativeType(JSTypeNative.VOID_TYPE), registry.getType("void"));
    assertEquals(registry.getNativeType(JSTypeNative.VOID_TYPE), registry.getType("Undefined"));
    assertEquals(registry.getNativeType(JSTypeNative.NULL_TYPE), registry.getType("Null"));
  }

  @Test(timeout = 4000)
  public void testDeclareAndOverwriteType() {
    RecordingErrorReporter reporter = new RecordingErrorReporter();
    JSTypeRegistry registry = new JSTypeRegistry(reporter);

    ObjectType customType = registry.createAnonymousObjectType();
    assertTrue(registry.declareType("com.example