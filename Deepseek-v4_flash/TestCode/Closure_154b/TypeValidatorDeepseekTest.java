package com.google.javascript.jscomp;

import static org.junit.Assert.*;
import org.junit.Test;

import com.google.javascript.jscomp.TypeValidator.TypeMismatch;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.*;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.jscomp.AbstractCompiler;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.JSError;
import com.google.javascript.jscomp.NodeTraversal;
import com.google.javascript.jscomp.Scope;

import java.util.List;
import java.util.ArrayList;

/**
 * Tests for TypeValidator, targeting known defects and core validation logic.
 */
public class TypeValidatorDeepseekTest {

    // Generate a fresh TypeValidator with a real compiler and fresh registry.
    private TypeValidator createValidator() {
        Compiler compiler = new Compiler();
        compiler.initOptions(new CompilerOptions());
        TypeValidator validator = new TypeValidator(compiler);
        validator.setShouldReport(false); // avoid compiler.report side effects
        return validator;
    }

    // A minimal NodeTraversal stub that only needs to provide a source name.
    private static class StubTraversal extends NodeTraversal {
        private final String sourceName;
        StubTraversal(AbstractCompiler compiler, String sourceName) {
            super(compiler, (Scope) null);
            this.sourceName = sourceName;
        }
        @Override public String getSourceName() { return sourceName; }
        @Override public JSError makeError(Node n, DiagnosticType type, String... args) {
            return JSError.make(sourceName, n, type, args);
        }
    }

    // ------------------------------------------------------------------------
    // Partition A: Core functional logic & state transitions
    // ------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testConstructor_initialState() {
        TypeValidator v = createValidator();
        assertNotNull(v.getMismatches());
        assertFalse(v.getMismatches().iterator().hasNext());
    }

    @Test(timeout = 4000)
    public void testSetShouldReport_switchFlag() {
        TypeValidator v = createValidator();
        v.setShouldReport(true);
        // We don't have direct getter, but ensure no exceptions here.
        v.setShouldReport(false);
        assertNotNull(v.getMismatches());
    }

    // ------------------------------------------------------------------------
    // Partition B: Boundary value analysis & extremes
    // ------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testExpectObject_acceptsObjectType() {
        TypeValidator v = createValidator();
        JSTypeRegistry reg = v.typeRegistry; // package-private access
        JSType objType = reg.getNativeType(JSTypeNative.OBJECT_TYPE);
        NodeTraversal t = new StubTraversal(v.compiler, "test");
        Node n = new Node(Token.EXPR_RESULT);
        assertTrue(v.expectObject(t, n, objType, "msg"));
        assertFalse(v.mismatches.iterator().hasNext());
    }

    @Test(timeout = 4000)
    public void testExpectObject_rejectsNumberType() {
        TypeValidator v = createValidator();
        JSTypeRegistry reg = v.typeRegistry;
        JSType numType = reg.getNativeType(JSTypeNative.NUMBER_TYPE);
        NodeTraversal t = new StubTraversal(v.compiler, "test");
        Node n = new Node(Token.EXPR_RESULT);
        assertFalse(v.expectObject(t, n, numType, "msg"));
        List<TypeMismatch> mismatches = v.mismatches;
        assertEquals(1, mismatches.size());
    }

    @Test(timeout = 4000)
    public void testExpectActualObject_acceptsObject() {
        TypeValidator v = createValidator();
        JSTypeRegistry reg = v.typeRegistry;
        JSType objType = reg.getNativeType(JSTypeNative.OBJECT_TYPE);
        NodeTraversal t = new StubTraversal(v.compiler, "test");
        Node n = new Node(Token.EXPR_RESULT);
        v.expectActualObject(t, n, objType, "msg");
        assertEquals(0, v.mismatches.size());
    }

    @Test(timeout = 4000)
    public void testExpectActualObject_rejectsNumber() {
        TypeValidator v = createValidator();
        JSTypeRegistry reg = v.typeRegistry;
        JSType numType = reg.getNativeType(JSTypeNative.NUMBER_TYPE);
        NodeTraversal t = new StubTraversal(v.compiler, "test");
        Node n = new Node(Token.EXPR_RESULT);
        v.expectActualObject(t, n, numType, "msg");
        assertEquals(1, v.mismatches.size());
    }

    @Test(timeout = 4000)
    public void testExpectAnyObject_acceptsObjectOrEmpty() {
        TypeValidator v = createValidator();
        JSTypeRegistry reg = v.typeRegistry;
        JSType objType = reg.getNativeType(JSTypeNative.OBJECT_TYPE);
        NodeTraversal t = new StubTraversal(v.compiler, "test");
        Node n = new Node(Token.EXPR_RESULT);
        v.expectAnyObject(t, n, objType, "msg");
        assertEquals(0, v.mismatches.size());
    }

    @Test(timeout = 4000)
    public void testExpectAnyObject_rejectsNumber() {
        TypeValidator v = createValidator();
        JSTypeRegistry reg = v.typeRegistry;
        JSType numType = reg.getNativeType(JSTypeNative.NUMBER_TYPE);
        NodeTraversal t = new StubTraversal(v.compiler, "test");
        Node n = new Node(Token.EXPR_RESULT);
        v.expectAnyObject(t, n, numType, "msg");
        assertEquals(1, v.mismatches.size());
    }

    @Test(timeout = 4000)
    public void testExpectString_acceptsString() {
        TypeValidator v = createValidator();
        JSTypeRegistry reg = v.typeRegistry;
        JSType strType = reg.getNativeType(JSTypeNative.STRING_TYPE);
        NodeTraversal t = new StubTraversal(v.compiler, "test");
        Node n = new Node(Token.EXPR_RESULT);
        v.expectString(t, n, strType, "msg");
        assertEquals(0, v.mismatches.size());
    }

    @Test(timeout = 4000)
    public void testExpectString_rejectsNumber() {
        TypeValidator v = createValidator();
        JSTypeRegistry reg = v.typeRegistry;
        JSType numType = reg.getNativeType(JSTypeNative.NUMBER_TYPE);
        NodeTraversal t = new StubTraversal(v.compiler, "test");
        Node n = new Node(Token.EXPR_RESULT);
        v.expectString(t, n, numType, "msg");
        assertEquals(1, v.mismatches.size());
    }

    @Test(timeout = 4000)
    public void testExpectNumber_acceptsNumber() {
        TypeValidator v = createValidator();
        JSTypeRegistry reg = v.typeRegistry;
        JSType numType = reg.getNativeType(JSTypeNative.NUMBER_TYPE);
        NodeTraversal t = new StubTraversal(v.compiler, "test");
        Node n = new Node(Token.EXPR_RESULT);
        v.expectNumber(t, n, numType, "msg");
        assertEquals(0, v.mismatches.size());
    }

    @Test(timeout = 4000)
    public void testExpectNumber_rejectsString() {
        TypeValidator v = createValidator();
        JSTypeRegistry reg = v.typeRegistry;
        JSType strType = reg.getNativeType(JSTypeNative.STRING_TYPE);
        NodeTraversal t = new StubTraversal(v.compiler, "test");
        Node n = new Node(Token.EXPR_RESULT);
        v.expectNumber(t, n, strType, "msg");
        assertEquals(1, v.mismatches.size());
    }

    @Test(timeout = 4000)
    public void testExpectBitwiseable_acceptsNumber() {
        TypeValidator v = createValidator();
        JSTypeRegistry reg = v.typeRegistry;
        JSType numType = reg.getNativeType(JSTypeNative.NUMBER_TYPE);
        NodeTraversal t = new StubTraversal(v.compiler, "test");
        Node n = new Node(Token.EXPR_RESULT);
        v.expectBitwiseable(t, n, numType, "msg");
        assertEquals(0, v.mismatches.size());
    }

    @Test(timeout = 4000)
    public void testExpectBitwiseable_rejectsObject() {
        TypeValidator v = createValidator();
        JSTypeRegistry reg = v.typeRegistry;
        JSType objType = reg.getNativeType(JSTypeNative.OBJECT_TYPE);
        NodeTraversal t = new StubTraversal(v.compiler, "test");
        Node n = new Node(Token.EXPR_RESULT);
        v.expectBitwiseable(t, n, objType, "msg");
        assertEquals(1, v.mismatches.size());
    }

    @Test(timeout = 4000)
    public void testExpectStringOrNumber_acceptsNumber() {
        TypeValidator v = createValidator();
        JSTypeRegistry reg = v.typeRegistry;
        JSType numType = reg.getNativeType(JSTypeNative.NUMBER_TYPE);
        NodeTraversal t = new StubTraversal(v.compiler, "test");
        Node n = new Node(Token.EXPR_RESULT);
        v.expectStringOrNumber(t, n, numType, "msg");
        assertEquals(0, v.mismatches.size());
    }

    @Test(timeout = 4000)
    public void testExpectStringOrNumber_rejectsObject() {
        TypeValidator v = createValidator();
        JSTypeRegistry reg = v.typeRegistry;
        JSType objType = reg.getNativeType(JSTypeNative.OBJECT_TYPE);
        NodeTraversal t = new StubTraversal(v.compiler, "test");
        Node n = new Node(Token.EXPR_RESULT);
        v.expectStringOrNumber(t, n, objType, "msg");
        assertEquals(1, v.mismatches.size());
    }

    @Test(timeout = 4000)
    public void testExpectNotNullOrUndefined_acceptsNumber() {
        TypeValidator v = createValidator();
        JSTypeRegistry reg = v.typeRegistry;
        JSType numType = reg.getNativeType(JSTypeNative.NUMBER_TYPE);
        NodeTraversal t = new StubTraversal(v.compiler, "test");
        Node n = new Node(Token.EXPR_RESULT);
        assertTrue(v.expectNotNullOrUndefined(t, n, numType, "msg", numType));
        assertEquals(0, v.mismatches.size());
    }

    @Test(timeout = 4000)
    public void testExpectNotNullOrUndefined_rejectsNullType() {
        TypeValidator v = createValidator();
        JSTypeRegistry reg = v.typeRegistry;
        JSType nullType = reg.getNativeType(JSTypeNative.NULL_TYPE);
        NodeTraversal t = new StubTraversal(v.compiler, "test");
        Node n = new Node(Token.EXPR_RESULT);
        assertFalse(v.expectNotNullOrUndefined(t, n, nullType, "msg", nullType));
        assertEquals(1, v.mismatches.size());
    }

    @Test(timeout = 4000)
    public void testExpectCanAssignTo_assignableTypes() {
        TypeValidator v = createValidator();
        JSTypeRegistry reg = v.typeRegistry;
        JSType numType = reg.getNativeType(JSTypeNative.NUMBER_TYPE);
        NodeTraversal t = new StubTraversal(v.compiler, "test");
        Node n = new Node(Token.EXPR_RESULT);
        assertTrue(v.expectCanAssignTo(t, n, numType, numType, "msg"));
        assertEquals(0, v.mismatches.size());
    }

    @Test(timeout = 4000)
    public void testExpectCanAssignTo_nonAssignableTypes() {
        TypeValidator v = createValidator();
        JSTypeRegistry reg = v.typeRegistry;
        JSType numType = reg.getNativeType(JSTypeNative.NUMBER_TYPE);
        JSType strType = reg.getNativeType(JSTypeNative.STRING_TYPE);
        NodeTraversal t = new StubTraversal(v.compiler, "test");
        Node n = new Node(Token.EXPR_RESULT);
        assertFalse(v.expectCanAssignTo(t, n, numType, strType, "msg"));
        assertEquals(1, v.mismatches.size());
    }

    @Test(timeout = 4000)
    public void testExpectCanAssignToPropertyOf_assignable() {
        TypeValidator v = createValidator();
        JSTypeRegistry reg = v.typeRegistry;
        JSType numType = reg.getNativeType(JSTypeNative.NUMBER_TYPE);
        NodeTraversal t = new StubTraversal(v.compiler, "test");
        Node n = new Node(Token.EXPR_RESULT);
        Node owner = new Node(Token.GETPROP);
        assertTrue(v.expectCanAssignToPropertyOf(t, n, numType, numType, owner, "prop"));
        assertEquals(0, v.mismatches.size());
    }

    @Test(timeout = 4000)
    public void testExpectCanAssignToPropertyOf_nonAssignable() {
        TypeValidator v = createValidator();
        JSTypeRegistry reg = v.typeRegistry;
        JSType numType = reg.getNativeType(JSTypeNative.NUMBER_TYPE);
        JSType strType = reg.getNativeType(JSTypeNative.STRING_TYPE);
        NodeTraversal t = new StubTraversal(v.compiler, "test");
        Node n = new Node(Token.EXPR_RESULT);
        Node owner = new Node(Token.GETPROP);
        assertFalse(v.expectCanAssignToPropertyOf(t, n, numType, strType, owner, "prop"));
        assertEquals(1, v.mismatches.size());
    }

    @Test(timeout = 4000)
    public void testExpectArgumentMatchesParameter_match() {
        TypeValidator v = createValidator();
        JSTypeRegistry reg = v.typeRegistry;
        JSType numType = reg.getNativeType(JSTypeNative.NUMBER_TYPE);
        NodeTraversal t = new StubTraversal(v.compiler, "test");
        Node n = new Node(Token.NAME); // dummy
        Node callNode = new Node(Token.CALL);
        callNode.addChildToFront(new Node(Token.NAME, "fn"));
        v.expectArgumentMatchesParameter(t, n, numType, numType, callNode, 0);
        assertEquals(0, v.mismatches.size());
    }

    @Test(timeout = 4000)
    public void testExpectArgumentMatchesParameter_mismatch() {
        TypeValidator v = createValidator();
        JSTypeRegistry reg = v.typeRegistry;
        JSType numType = reg.getNativeType(JSTypeNative.NUMBER_TYPE);
        JSType strType = reg.getNativeType(JSTypeNative.STRING_TYPE);
        NodeTraversal t = new StubTraversal(v.compiler, "test");
        Node n = new Node(Token.NAME);
        Node callNode = new Node(Token.CALL);
        callNode.addChildToFront(new Node(Token.NAME, "fn"));
        v.expectArgumentMatchesParameter(t, n, numType, strType, callNode, 0);
        assertEquals(1, v.mismatches.size());
    }

    @Test(timeout = 4000)
    public void testExpectCanOverride_assignable() {
        TypeValidator v = createValidator();
        JSTypeRegistry reg = v.typeRegistry;
        JSType numType = reg.getNativeType(JSTypeNative.NUMBER_TYPE);
        NodeTraversal t = new StubTraversal(v.compiler, "test");
        Node n = new Node(Token.EXPR_RESULT);
        v.expectCanOverride(t, n, numType, numType, "prop", numType);
        assertEquals(0, v.mismatches.size());
    }

    @Test(timeout = 4000)
    public void testExpectCanOverride_nonAssignable() {
        TypeValidator v = createValidator();
        JSTypeRegistry reg = v.typeRegistry;
        JSType numType = reg.getNativeType(JSTypeNative.NUMBER_TYPE);
        JSType strType = reg.getNativeType(JSTypeNative.STRING_TYPE);
        NodeTraversal t = new StubTraversal(v.compiler, "test");
        Node n = new Node(Token.EXPR_RESULT);
        v.expectCanOverride(t, n, numType, strType, "prop", strType);
        assertEquals(1, v.mismatches.size());
    }

    // ------------------------------------------------------------------------
    // Partition C: Defect-targeted branch zone (interface inheritance)
    // ------------------------------------------------------------------------

    /**
     * Targets Defect4J bug: testInterfaceInheritanceCheck12 was expecting a
     * warning when an interface method is not implemented, but none was produced.
     * We construct a minimal interface/class pair and verify that a mismatch
     * is registered when the implementing class lacks the interface method.
     */
    @Test(timeout = 4000)
    public void testExpectAllInterfaceProperties_missingProperty() {
        TypeValidator v = createValidator();
        JSTypeRegistry reg = v.typeRegistry;

        // Create an interface type with a property "foo".
        ObjectType ifaceProto = reg.createObjectType("Interface.prototype", null);
        ifaceProto.defineProperty("foo", reg.getNativeType(JSTypeNative.NUMBER_TYPE), false, null);

        FunctionType iface = reg.createFunctionType(
            reg.getNativeType(JSTypeNative.UNKNOWN_TYPE), new JSType[0]);
        iface.setPrototype(ifaceProto);

        // Create a class type implementing the interface, but without "foo".
        FunctionType clazz = reg.createFunctionType(
            reg.getNativeType(JSTypeNative.UNKNOWN_TYPE), new JSType[0]);
        clazz.addImplementedInterface(iface);

        NodeTraversal t = new StubTraversal(v.compiler, "test");
        Node n = new Node(Token.FUNCTION);
        v.expectAllInterfaceProperties(t, n, clazz);

        // Since the class does not implement "foo", a mismatch must be recorded.
        assertFalse("Expected at least one mismatch for missing interface property",
            v.mismatches.isEmpty());
    }

    @Test(timeout = 4000)
    public void testExpectAllInterfaceProperties_allPropertiesPresent() {
        TypeValidator v = createValidator();
        JSTypeRegistry reg = v.typeRegistry;

        ObjectType ifaceProto = reg.createObjectType("Interface.prototype", null);
        ifaceProto.defineProperty("bar", reg.getNativeType(JSTypeNative.NUMBER_TYPE), false, null);

        FunctionType iface = reg.createFunctionType(
            reg.getNativeType(JSTypeNative.UNKNOWN_TYPE), new JSType[0]);
        iface.setPrototype(ifaceProto);

        FunctionType clazz = reg.createFunctionType(
            reg.getNativeType(JSTypeNative.UNKNOWN_TYPE), new JSType[0]);
        // Make the class also have "bar".
        clazz.getInstanceType().defineProperty("bar",
            reg.getNativeType(JSTypeNative.NUMBER_TYPE), false, null);
        clazz.addImplementedInterface(iface);

        NodeTraversal t = new StubTraversal(v.compiler, "test");
        Node n = new Node(Token.FUNCTION);
        v.expectAllInterfaceProperties(t, n, clazz);
        assertTrue(v.mismatches.isEmpty());
    }

    // ------------------------------------------------------------------------
    // Partition D: Exception & defensive guard paths
    // ------------------------------------------------------------------------

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testExpectObject_nullJSTypeThrows() {
        TypeValidator v = createValidator();
        NodeTraversal t = new StubTraversal(v.compiler, "test");
        Node n = new Node(Token.EXPR_RESULT);
        v.expectObject(t, n, null, "msg");
    }

    @Test(timeout = 4000)
    public void testRegisterMismatch_nullTypesNoThrow() {
        TypeValidator v = createValidator();
        // Calling a method that registers a mismatch with null should not throw.
        // We use expectCanAssignTo with null rightType (will NPE, but we catch?).
        // Instead, call private registerMismatch via reflection? We'll just call a method.
        JSType numType = v.typeRegistry.getNativeType(JSTypeNative.NUMBER_TYPE);
        NodeTraversal t = new StubTraversal(v.compiler, "test");
        Node n = new Node(Token.EXPR_RESULT);
        v.expectCanAssignTo(t, n, null, numType, "msg");
        // Should not have thrown; mismatch may or may not be added.
        // We only verify no exception occurred.
        assertNotNull(v.getMismatches());
    }

    // ------------------------------------------------------------------------
    // Partition E: TypeMismatch inner class
    // ------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testTypeMismatch_equals_symmetric() {
        JSType typeA = createValidator().typeRegistry.getNativeType(JSTypeNative.NUMBER_TYPE);
        JSType typeB = createValidator().typeRegistry.getNativeType(JSTypeNative.STRING_TYPE);
        TypeMismatch m1 = new TypeMismatch(typeA, typeB);
        TypeMismatch m2 = new TypeMismatch(typeA, typeB);
        TypeMismatch m3 = new TypeMismatch(typeB, typeA);
        assertEquals(m1, m2);
        assertEquals(m1.hashCode(), m2.hashCode());
        assertEquals(m1, m3); // order-insensitive
    }

    @Test(timeout = 4000)
    public void testTypeMismatch_equals_differentTypes() {
        TypeValidator v = createValidator();
        JSType num = v.typeRegistry.getNativeType(JSTypeNative.NUMBER_TYPE);
        JSType str = v.typeRegistry.getNativeType(JSTypeNative.STRING_TYPE);
        TypeMismatch m1 = new TypeMismatch(num, str);
        TypeMismatch m2 = new TypeMismatch(num, num);
        assertNotEquals(m1, m2);
    }

    @Test(timeout = 4000)
    public void testTypeMismatch_toString() {
        TypeValidator v = createValidator();
        JSType num = v.typeRegistry.getNativeType(JSTypeNative.NUMBER_TYPE);
        JSType str = v.typeRegistry.getNativeType(JSTypeNative.STRING_TYPE);
        TypeMismatch m = new TypeMismatch(num, str);
        assertTrue(m.toString().contains("number"));
        assertTrue(m.toString().contains("string"));
    }
}