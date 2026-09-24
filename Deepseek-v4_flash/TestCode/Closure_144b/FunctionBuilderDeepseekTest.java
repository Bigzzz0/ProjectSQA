package com.google.javascript.rhino.jstype;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;

/**
 * Test suite for FunctionBuilder.
 *
 * [Branch & Defect Analysis Matrix]
 * 
 * Branch/Decision Targets:
 * - WithName: null, empty, non-empty string
 * - WithSourceNode: null, non-null Node
 * - WithParams: null, non-null FunctionParamBuilder (calls params.build())
 * - WithParamsNode: null, non-null Node
 * - WithReturnType: null, concrete JSType
 * - WithInferredReturnType: sets inferredReturnType=true, returnType
 * - WithTypeOfThis: null, concrete ObjectType
 * - WithTemplateName: null, empty, non-empty string
 * - ForConstructor: sets isConstructor=true
 * - ForNativeType: sets isNativeType=true (package-private)
 * - CopyFromOtherFunction: copies all fields from a FunctionType, including inferred return type
 * - Build: constructs FunctionType with all fields; ArrowType receives inferredReturnType flag
 * 
 * Boundary Conditions:
 * - All setter methods accept null (defensive)
 * - WithParams: null FunctionParamBuilder -> potential NPE if params.build() called after null
 *   (Note: withParams does not guard against null; test for defensive handling assumed)
 * - WithParamsNode: null Node
 * - WithReturnType: null JSType
 * - WithInferredReturnType: null JSType (inferred but no type)
 * - WithTypeOfThis: null ObjectType
 * - CopyFromOtherFunction: source function type with null fields
 * - Build: all fields potentially null except registry
 *
 * Defect Target (from Defects4J ground truth):
 * - Inferred return types are displayed as "?" instead of the actual type (e.g., "undefined").
 *   The bug likely lies in how ArrowType handles the inferredReturnType flag: when true,
 *   the return type is replaced by the unknown type.
 *   Tests must verify that a function built with an inferred return type retains that type,
 *   and that the return type's string representation is not "?".
 *   Also, copyFromOtherFunction must preserve the inferred return type correctly.
 *
 * Partitions:
 * A: Core functional logic – setter chaining and build
 * B: Boundary values – null, empty strings, extremes
 * C: Defect-targeted – inferred return type propagation
 * D: Exception/defensive – null arguments passed to setters (if not guarded, expect NPE?)
 * E: Object lifecycle – copyFromOtherFunction integrity
 */
public class FunctionBuilderDeepseekTest {

    private JSTypeRegistry createRegistry() {
        return new JSTypeRegistry();
    }

    // ========== Partition A: Core Functional Logic & State Transitions ==========

    @Test(timeout = 4000)
    public void testDefaultBuild() {
        JSTypeRegistry registry = createRegistry();
        FunctionBuilder builder = new FunctionBuilder(registry);
        FunctionType ft = builder.build();
        assertNotNull("Built function type must not be null", ft);
        assertNull("Default name should be null", ft.getReferenceName());
        assertFalse("Default not a constructor", ft.isConstructor());
    }

    @Test(timeout = 4000)
    public void testWithName() {
        JSTypeRegistry registry = createRegistry();
        FunctionBuilder builder = new FunctionBuilder(registry);
        String name = "myFunc";
        builder.withName(name);
        FunctionType ft = builder.build();
        assertEquals("Name should be set", name, ft.getReferenceName());
    }

    @Test(timeout = 4000)
    public void testChainedSetters() {
        JSTypeRegistry registry = createRegistry();
        FunctionBuilder builder = new FunctionBuilder(registry)
            .withName("testFunc")
            .withSourceNode(null) // Node is complex, use null
            .withReturnType(registry.getNativeType(JSTypeNative.NUMBER_TYPE))
            .withTypeOfThis(registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE))
            .withTemplateName("T")
            .forConstructor();
        FunctionType ft = builder.build();
        assertEquals("Name", "testFunc", ft.getReferenceName());
        assertTrue("Should be constructor", ft.isConstructor());
        assertEquals("Return type should be number", "number", ft.getReturnType().toString());
        assertEquals("Template type name", "T", ft.getTemplateTypeName());
    }

    // ========== Partition B: Boundary Value Analysis & Extremes ==========

    @Test(timeout = 4000)
    public void testBuildWithNullFields() {
        JSTypeRegistry registry = createRegistry();
        FunctionBuilder builder = new FunctionBuilder(registry)
            .withName(null)
            .withSourceNode(null)
            .withReturnType(null)
            .withTypeOfThis(null)
            .withTemplateName(null);
        FunctionType ft = builder.build();
        assertNotNull(ft);
        assertNull(ft.getReferenceName());
        assertNull(ft.getReturnType()); // ArrowType may have null return type
    }

    @Test(timeout = 4000)
    public void testEmptyTemplateName() {
        JSTypeRegistry registry = createRegistry();
        FunctionBuilder builder = new FunctionBuilder(registry)
            .withTemplateName("");
        FunctionType ft = builder.build();
        assertEquals("Empty template name should be stored", "", ft.getTemplateTypeName());
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    @Test(timeout = 4000)
    public void testInferredReturnTypeIsNotUnknown() {
        // Target defect: inferred return type should NOT display as "?"
        JSTypeRegistry registry = createRegistry();
        JSType voidType = registry.getNativeType(JSTypeNative.VOID_TYPE); // "undefined"
        FunctionBuilder builder = new FunctionBuilder(registry)
            .withInferredReturnType(voidType);
        FunctionType ft = builder.build();
        JSType builtReturnType = ft.getReturnType();
        assertNotNull("Return type must not be null", builtReturnType);
        assertFalse("Inferred return type must not be unknown type",
                    builtReturnType.isUnknownType());
        // The actual type should be void/undefined
        assertEquals("Return type should be 'undefined'", "undefined", builtReturnType.toString());
    }

    @Test(timeout = 4000)
    public void testInferredReturnTypeIsSetInArrowType() {
        // Verify that the ArrowType constructed inside build() receives the inferred flag
        // and returns the correct type. This is critical for the defect.
        JSTypeRegistry registry = createRegistry();
        JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        FunctionBuilder builder = new FunctionBuilder(registry)
            .withInferredReturnType(numberType);
        FunctionType ft = builder.build();
        // The ArrowType is not directly accessible, but we can check FunctionType's return type
        assertEquals("Return type should be number", numberType, ft.getReturnType());
    }

    @Test(timeout = 4000)
    public void testCopyFromOtherFunctionPreservesInferredReturnType() {
        // Build a function with inferred return type, then copy it via copyFromOtherFunction
        JSTypeRegistry registry = createRegistry();
        JSType voidType = registry.getNativeType(JSTypeNative.VOID_TYPE);
        FunctionType original = new FunctionBuilder(registry)
            .withInferredReturnType(voidType)
            .withName("orig")
            .build();

        // Copy using builder
        FunctionBuilder copyBuilder = new FunctionBuilder(registry);
        copyBuilder.copyFromOtherFunction(original);
        FunctionType copy = copyBuilder.build();

        assertNotNull("Copy must have return type", copy.getReturnType());
        assertFalse("Copy's return type should not be unknown",
                    copy.getReturnType().isUnknownType());
        assertEquals("Copy's return type should be undefined",
                     "undefined", copy.getReturnType().toString());
        assertEquals("Copy should keep the name", "orig", copy.getReferenceName());
    }

    @Test(timeout = 4000)
    public void testCopyFromOtherFunctionWithExplicitReturnType() {
        // Non-inferred copy
        JSTypeRegistry registry = createRegistry();
        JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
        FunctionType original = new FunctionBuilder(registry)
            .withReturnType(stringType)
            .build();
        FunctionBuilder copyBuilder = new FunctionBuilder(registry);
        copyBuilder.copyFromOtherFunction(original);
        FunctionType copy = copyBuilder.build();
        assertEquals("Copy should have string return type", stringType, copy.getReturnType());
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testWithParamsNullThrowsNPE() {
        // The withParams method calls params.build() directly without null check.
        // Passing null FunctionParamBuilder will cause NPE.
        JSTypeRegistry registry = createRegistry();
        new FunctionBuilder(registry).withParams(null);
    }

    @Test(timeout = 4000)
    public void testWithParamsNodeNullBuilds() {
        // withParamsNode should accept null and store it; build should handle null parameters node
        JSTypeRegistry registry = createRegistry();
        FunctionBuilder builder = new FunctionBuilder(registry).withParamsNode(null);
        FunctionType ft = builder.build();
        assertNotNull(ft);
        // The parameters node should be null; ArrowType may or may not handle it
        // No exception expected
    }

    @Test(timeout = 4000)
    public void testWithReturnTypeNullBuilds() {
        JSTypeRegistry registry = createRegistry();
        FunctionBuilder builder = new FunctionBuilder(registry).withReturnType(null);
        FunctionType ft = builder.build();
        assertNull("Return type should be null when set to null", ft.getReturnType());
    }

    @Test(timeout = 4000)
    public void testWithInferredReturnTypeNullBuilds() {
        JSTypeRegistry registry = createRegistry();
        FunctionBuilder builder = new FunctionBuilder(registry).withInferredReturnType(null);
        FunctionType ft = builder.build();
        // inferredReturnType is true, but returnType is null
        assertNull("Return type should be null", ft.getReturnType());
    }

    @Test(timeout = 4000)
    public void testWithTypeOfThisNullBuilds() {
        JSTypeRegistry registry = createRegistry();
        FunctionBuilder builder = new FunctionBuilder(registry).withTypeOfThis(null);
        FunctionType ft = builder.build();
        assertNull("Type of this should be null", ft.getTypeOfThis());
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testCopyFromOtherFunctionCopyConstructorAndNativeFlags() {
        JSTypeRegistry registry = createRegistry();
        FunctionType original = new FunctionBuilder(registry)
            .forConstructor()
            .build();
        FunctionBuilder copyBuilder = new FunctionBuilder(registry);
        copyBuilder.copyFromOtherFunction(original);
        FunctionType copy = copyBuilder.build();
        assertTrue("Copy should be constructor", copy.isConstructor());
    }

    @Test(timeout = 4000)
    public void testCopyFromOtherFunctionCopyTypeOfThis() {
        JSTypeRegistry registry = createRegistry();
        ObjectType objType = registry.getNativeObjectType(JSTypeNative.ARRAY_TYPE);
        FunctionType original = new FunctionBuilder(registry)
            .withTypeOfThis(objType)
            .build();
        FunctionBuilder copyBuilder = new FunctionBuilder(registry);
        copyBuilder.copyFromOtherFunction(original);
        FunctionType copy = copyBuilder.build();
        assertEquals("Type of this should be copied", objType, copy.getTypeOfThis());
    }

    @Test(timeout = 4000)
    public void testForNativeType() {
        // forNativeType is package-private; we can access it from the same package.
        JSTypeRegistry registry = createRegistry();
        FunctionBuilder builder = new FunctionBuilder(registry);
        builder.forNativeType(); // returns FunctionBuilder
        FunctionType ft = builder.build();
        // isNativeType is not exposed on FunctionType, but the flag is passed.
        // We can verify indirectly that no exception occurs.
        assertNotNull(ft);
    }

    @Test(timeout = 4000)
    public void testBuildMultipleTimes() {
        // Builder can be reused? The fields are not reset. Check that build creates a new instance each time.
        JSTypeRegistry registry = createRegistry();
        FunctionBuilder builder = new FunctionBuilder(registry)
            .withName("A");
        FunctionType ft1 = builder.build();
        builder.withName("B");
        FunctionType ft2 = builder.build();
        assertEquals("First function name should be A", "A", ft1.getReferenceName());
        assertEquals("Second function name should be B", "B", ft2.getReferenceName());
    }
}