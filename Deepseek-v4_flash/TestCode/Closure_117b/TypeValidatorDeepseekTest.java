package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import org.junit.Test;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * - Constructor initializes allValueTypes and nullOrUndefined unions.
 * - expect* methods branch on JSType context matches; success paths are
 *   exercised without needing a NodeTraversal.
 * - getReadableJSTypeName has a GETPROP prototype-climbing branch and a
 *   fallback based on the GETPROP node's qualified name/type.
 * - Defect targeted from TypeCheckTest.testIssue1047:
 *   expected "Property p never defined on C[2]" but buggy code reports
 *   "Property p never defined on C[3.c2_]" (a structural/anonymous type
 *   name is shown instead of the constructor's readable type name).
 * - TypeMismatch equality and toString contract are validated.
 */
public class TypeValidatorDeepseekTest {

  private Compiler newCompiler() {
    return new Compiler();
  }

  private JSTypeRegistry registry(Compiler compiler) {
    return compiler.getTypeRegistry();
  }

  private void assertTypeCheckWarning(String js, String mustContain,
      String mustNotContain) {
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    options.setWarningLevel(DiagnosticGroups.CHECK_TYPES, CheckLevel.WARNING);

    Compiler compiler = newCompiler();
    compiler.compile(
        SourceFile.fromCode("externs.js", ""),
        SourceFile.fromCode("test.js", js),
        options);

    JSError[] errors = compiler.getErrors();
    assertEquals("Unexpected compiler errors: " + java.util.Arrays.toString(errors),
        0, errors.length);

    JSError[] warnings = compiler.getWarnings();
    assertTrue("Expected at least one type warning, got none", warnings.length > 0);

    String description = warnings[0].getDescription();
    assertTrue("Expected warning containing '" + mustContain + "' but was: " + description,
        description.contains(mustContain));
    if (mustNotContain != null) {
      assertFalse("Did not expect '" + mustNotContain + "' in warning: " + description,
          description.contains(mustNotContain));
    }
  }

  @Test(timeout = 4000)
  public void testIssue1047() {
    String js =
        "/** @constructor */\n" +
        "function C() {}\n" +
        "C.prototype = { c2_: function() {} };\n" +
        "var x = new C();\n" +
        "x.p;\n";
    assertTypeCheckWarning(js, "Property p never defined on C", "C[3.c2_]");
  }

  @Test(timeout = 4000)
  public void testConstructorAndMismatchesEmpty() {
    Compiler compiler = newCompiler();
    TypeValidator validator = new TypeValidator(compiler);
    assertNotNull(validator);
    assertFalse(validator.getMismatches().iterator().hasNext());
  }

  @Test(timeout = 4000)
  public void testExpectObjectAcceptsObject() {
    Compiler compiler = newCompiler();
    JSTypeRegistry registry = registry(compiler);
    TypeValidator validator = new TypeValidator(compiler);
    JSType objectType = registry.getNativeType(JSTypeNative.OBJECT_TYPE);
    assertTrue(validator.expectObject(null, null, objectType, "msg"));
  }

  @Test(timeout = 4000)
  public void testExpectActualObjectAcceptsObject() {
    Compiler compiler = newCompiler();
    JSTypeRegistry registry = registry(compiler);
    TypeValidator validator = new TypeValidator(compiler);
    JSType objectType = registry.getNativeType(JSTypeNative.OBJECT_TYPE);
    validator.expectActualObject(null, null, objectType, "msg");
  }

  @Test(timeout = 4000)
  public void testExpectAnyObjectAcceptsObject() {
    Compiler compiler = newCompiler();
    JSTypeRegistry registry = registry(compiler);
    TypeValidator validator = new TypeValidator(compiler);
    JSType objectType = registry.getNativeType(JSTypeNative.OBJECT_TYPE);
    validator.expectAnyObject(null, null, objectType, "msg");
  }

  @Test(timeout = 4000)
  public void testExpectStringAcceptsString() {
    Compiler compiler = newCompiler();
    JSTypeRegistry registry = registry(compiler);
    TypeValidator validator = new TypeValidator(compiler);
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    validator.expectString(null, null, stringType, "msg");
  }

  @Test(timeout = 4000)
  public void testExpectNumberAcceptsNumber() {
    Compiler compiler = newCompiler();
    JSTypeRegistry registry = registry(compiler);
    TypeValidator validator = new TypeValidator(compiler);
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    validator.expectNumber(null, null, numberType, "msg");
  }

  @Test(timeout = 4000)
  public void testExpectBitwiseableAcceptsNumber() {
    Compiler compiler = newCompiler();
    JSTypeRegistry registry = registry(compiler);
    TypeValidator validator = new TypeValidator(compiler);
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    validator.expectBitwiseable(null, null, numberType, "msg");
  }

  @Test(timeout = 4000)
  public void testExpectStringOrNumberAcceptsNumber() {
    Compiler compiler = newCompiler();
    JSTypeRegistry registry = registry(compiler);
    TypeValidator validator = new TypeValidator(compiler);
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    validator.expectStringOrNumber(null, null, numberType, "msg");
  }

  @Test(timeout = 4000)
  public void testExpectNotNullOrUndefinedAcceptsObject() {
    Compiler compiler = newCompiler();
    JSTypeRegistry registry = registry(compiler);
    TypeValidator validator = new TypeValidator(compiler);
    JSType objectType = registry.getNativeType(JSTypeNative.OBJECT_TYPE);
    assertTrue(validator.expectNotNullOrUndefined(null, null, objectType, "msg", objectType));
  }

  @Test(timeout = 4000)
  public void testExpectNotNullOrUndefinedAcceptsUnionWithNull() {
    Compiler compiler = newCompiler();
    JSTypeRegistry registry = registry(compiler);
    TypeValidator validator = new TypeValidator(compiler);
    JSType nullType = registry.getNativeType(JSTypeNative.NULL_TYPE);
    JSType objectType = registry.getNativeType(JSTypeNative.OBJECT_TYPE);
    JSType unionType = registry.createUnionType(nullType, objectType);
    assertTrue(validator.expectNotNullOrUndefined(null, null, unionType, "msg", objectType));
  }

  @Test(timeout = 4000)
  public void testExpectSwitchMatchesCaseAcceptsSameType() {
    Compiler compiler = newCompiler();
    JSTypeRegistry registry = registry(compiler);
    TypeValidator validator = new TypeValidator(compiler);
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    validator.expectSwitchMatchesCase(null, null, stringType, stringType);
  }

  @Test(timeout = 4000)
  public void testIndexMatchUnknownWithString() {
    Compiler compiler = newCompiler();
    JSTypeRegistry registry = registry(compiler);
    TypeValidator validator = new TypeValidator(compiler);
    Node node = new Node(Token.GETELEM,
        Node.newString(Token.NAME, "obj"),
        Node.newString(Token.STRING, "key"));
    JSType unknownType = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    validator.expectIndexMatch(null, node, unknownType, stringType);
  }

  @Test(timeout = 4000)
  public void testIndexMatchObjectWithString() {
    Compiler compiler = newCompiler();
    JSTypeRegistry registry = registry(compiler);
    TypeValidator validator = new TypeValidator(compiler);
    Node node = new Node(Token.GETELEM,
        Node.newString(Token.NAME, "obj"),
        Node.newString(Token.STRING, "key"));
    JSType objectType = registry.getNativeType(JSTypeNative.OBJECT_TYPE);
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    validator.expectIndexMatch(null, node, objectType, stringType);
  }

  @Test(timeout = 4000)
  public void testCanAssignToAcceptsSubtype() {
    Compiler compiler = newCompiler();
    JSTypeRegistry registry = registry(compiler);
    TypeValidator validator = new TypeValidator(compiler);
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType unionType = registry.createUnionType(stringType, numberType);
    assertTrue(validator.expectCanAssignTo(null, null, stringType, unionType, "msg"));
  }

  @Test(timeout = 4000)
  public void testCanAssignToPropertyOfAcceptsSubtype() {
    Compiler compiler = newCompiler();
    JSTypeRegistry registry = registry(compiler);
    TypeValidator validator = new TypeValidator(compiler);
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType unionType = registry.createUnionType(stringType, numberType);
    assertTrue(validator.expectCanAssignToPropertyOf(
        null, null, stringType, unionType, null, "prop"));
  }

  @Test(timeout = 4000)
  public void testArgumentMatchesParameterAcceptsSubtype() {
    Compiler compiler = newCompiler();
    JSTypeRegistry registry = registry(compiler);
    TypeValidator validator = new TypeValidator(compiler);
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType unionType = registry.createUnionType(stringType, numberType);
    validator.expectArgumentMatchesParameter(null, null, stringType, unionType, null, 0);
  }

  @Test(timeout = 4000)
  public void testCanOverrideAcceptsSubtype() {
    Compiler compiler = newCompiler();
    JSTypeRegistry registry = registry(compiler);
    TypeValidator validator = new TypeValidator(compiler);
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType unionType = registry.createUnionType(stringType, numberType);
    validator.expectCanOverride(null, null, stringType, unionType, "prop", unionType);
  }

  @Test(timeout = 4000)
  public void testCanCastAcceptsSameType() {
    Compiler compiler = newCompiler();
    JSTypeRegistry registry = registry(compiler);
    TypeValidator validator = new TypeValidator(compiler);
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    validator.expectCanCast(null, null, stringType, stringType);
  }

  @Test(timeout = 4000)
  public void testReadableNameForSimpleConstructor() {
    Compiler compiler = newCompiler();
    JSTypeRegistry registry = registry(compiler);
    TypeValidator validator = new TypeValidator(compiler);
    FunctionType ctor = registry.createConstructorType(
        "C", null, registry.getNativeType(JSTypeNative.UNKNOWN_TYPE));
    ObjectType instance = ctor.getInstanceType();

    Node nameNode = Node.newString(Token.NAME, "v");
    nameNode.setJSType(instance);

    assertEquals(instance.toString(), validator.getReadableJSTypeName(nameNode, true));
  }

  @Test(timeout = 4000)
  public void testReadableNameForMissingPropertyUsesReceiverType() {
    Compiler compiler = newCompiler();
    JSTypeRegistry registry = registry(compiler);
    TypeValidator validator = new TypeValidator(compiler);
    FunctionType ctor = registry.createConstructorType(
        "C", null, registry.getNativeType(JSTypeNative.UNKNOWN_TYPE));
    ObjectType instance = ctor.getInstanceType();

    Node receiver = Node.newString(Token.NAME, "v");
    receiver.setJSType(instance);

    Node getprop = new Node(Token.GETPROP, receiver, Node.newString(Token.STRING, "p"));
    getprop.setJSType(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE));

    assertEquals(instance.toString(), validator.getReadableJSTypeName(getprop, true));
  }

  @Test(timeout = 4000)
  public void testTypeMismatchEqualsAndHashContractForSameInstance() {
    Compiler compiler = newCompiler();
    JSTypeRegistry registry = registry(compiler);
    TypeValidator validator = new TypeValidator(compiler);
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSError error = JSError.make(
        "source", null, TypeValidator.TYPE_MISMATCH_WARNING, "msg");

    TypeValidator.TypeMismatch mismatch =
        new TypeValidator.TypeMismatch(stringType, numberType, error);
    assertEquals(mismatch, mismatch);
    assertEquals(mismatch.hashCode(), mismatch.hashCode());
    assertFalse(mismatch.equals(null));
    assertFalse(mismatch.equals("other"));
  }

  @Test(timeout = 4000)
  public void testTypeMismatchSwappedEquals() {
    Compiler compiler = newCompiler();
    JSTypeRegistry registry = registry(compiler);
    TypeValidator validator = new TypeValidator(compiler);
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSError error = JSError.make(
        "source", null, TypeValidator.TYPE_MISMATCH_WARNING, "msg");

    TypeValidator.TypeMismatch mismatch1 =
        new TypeValidator.TypeMismatch(stringType, numberType, error);
    TypeValidator.TypeMismatch mismatch2 =
        new TypeValidator.TypeMismatch(numberType, stringType, error);

    assertEquals(mismatch1, mismatch2);
  }

  @Test(timeout = 4000)
  public void testTypeMismatchToString() {
    Compiler compiler = newCompiler();
    JSTypeRegistry registry = registry(compiler);
    TypeValidator validator = new TypeValidator(compiler);
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSError error = JSError.make(
        "source", null, TypeValidator.TYPE_MISMATCH_WARNING, "msg");

    TypeValidator.TypeMismatch mismatch =
        new TypeValidator.TypeMismatch(stringType, numberType, error);

    assertTrue(mismatch.toString().startsWith("("));
    assertTrue(mismatch.toString().contains(","));
    assertTrue(mismatch.toString().endsWith(")"));
  }
}