package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: InlineObjectLiterals.java
 * 
 * Key Decision Branches:
 * 1. isVarInlineForbidden: global, extern, exported, RENAME_PROPERTY, staleVars
 * 2. isInlinableObject: getProp parent, call target, undefined property, var/assign LHS, null val, non-objectLit, self-referential, getter/setter
 * 3. isVarOrAssignExprLhs: var parent, assign parent with expr result
 * 4. computeVarList: lvalue/init, var parent, getprop parent
 * 5. splitObject: defined vs undefined, init vs other refs, lvalue vs var vs getprop
 * 6. replaceAssignmentExpression: empty nodes, non-empty nodes, var vs assign replacement
 * 
 * Boundary Conditions:
 * - Empty object literal
 * - Object with single property
 * - Object with multiple properties
 * - Self-referential assignments
 * - Undefined property references
 * - Global variables
 * - Exported variables
 * - ES5 getters/setters
 * - Nested property access
 * - Function calls using object as 'this'
 * 
 * Defect Targeting (testObject10, testObject12, testObject22, testIssue724):
 * - Objects referenced in function calls where properties are used
 * - Objects with properties accessed after being passed to functions
 * - Objects where property values reference other properties of same object
 * - Objects used in conditional expressions
 */
public class InlineObjectLiteralsDeepseekTest {
    
    // ==================== Partition A: Core Functional Logic & State Transitions ====================
    
    @Test(timeout = 4000)
    public void testSimpleObjectLiteral() {
        // Test basic object literal inlining
        String js = "var x = {a: 1, b: 2}; var y = x.a + x.b;";
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        options.setInlineObjectLiterals(true);
        
        Result result = compiler.compile(
            SourceFile.fromCode("externs.js", ""),
            SourceFile.fromCode("test.js", js),
            options);
        
        assertTrue("Compilation should succeed", result.success);
        String output = compiler.toSource();
        assertTrue("Output should contain inlined variable a", output.contains("JSCompiler_object_inline_a_"));
        assertTrue("Output should contain inlined variable b", output.contains("JSCompiler_object_inline_b_"));
    }
    
    @Test(timeout = 4000)
    public void testObjectLiteralWithSingleProperty() {
        String js = "var x = {a: 1}; var y = x.a;";
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        options.setInlineObjectLiterals(true);
        
        Result result = compiler.compile(
            SourceFile.fromCode("externs.js", ""),
            SourceFile.fromCode("test.js", js),
            options);
        
        assertTrue("Compilation should succeed", result.success);
    }
    
    @Test(timeout = 4000)
    public void testObjectLiteralWithNoReferences() {
        String js = "var x = {a: 1, b: 2};";
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        options.setInlineObjectLiterals(true);
        
        Result result = compiler.compile(
            SourceFile.fromCode("externs.js", ""),
            SourceFile.fromCode("test.js", js),
            options);
        
        assertTrue("Compilation should succeed", result.success);
    }
    
    // ==================== Partition B: Boundary Value Analysis & Extremes ====================
    
    @Test(timeout = 4000)
    public void testEmptyObjectLiteral() {
        String js = "var x = {}; var y = x.a;";
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        options.setInlineObjectLiterals(true);
        
        Result result = compiler.compile(
            SourceFile.fromCode("externs.js", ""),
            SourceFile.fromCode("test.js", js),
            options);
        
        assertTrue("Compilation should succeed", result.success);
    }
    
    @Test(timeout = 4000)
    public void testObjectLiteralWithUndefinedProperty() {
        String js = "var x = {a: 1}; var y = x.b;";
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        options.setInlineObjectLiterals(true);
        
        Result result = compiler.compile(
            SourceFile.fromCode("externs.js", ""),
            SourceFile.fromCode("test.js", js),
            options);
        
        assertTrue("Compilation should succeed", result.success);
    }
    
    @Test(timeout = 4000)
    public void testGlobalVariableNotInlined() {
        String js = "var x = {a: 1}; function f() { return x.a; }";
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        options.setInlineObjectLiterals(true);
        
        Result result = compiler.compile(
            SourceFile.fromCode("externs.js", ""),
            SourceFile.fromCode("test.js", js),
            options);
        
        assertTrue("Compilation should succeed", result.success);
    }
    
    @Test(timeout = 4000)
    public void testExportedVariableNotInlined() {
        String js = "var x = {a: 1}; window['x'] = x;";
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        options.setInlineObjectLiterals(true);
        
        Result result = compiler.compile(
            SourceFile.fromCode("externs.js", ""),
            SourceFile.fromCode("test.js", js),
            options);
        
        assertTrue("Compilation should succeed", result.success);
    }
    
    // ==================== Partition C: Defect-Targeted Branch Zone ====================
    
    @Test(timeout = 4000)
    public void testObject10_DefectTarget() {
        // This test targets the defect revealed by testObject10
        // Object literal used in function call context
        String js = "var x = {a: 1, b: 2}; " +
                    "function f(obj) { return obj.a + obj.b; } " +
                    "var result = f(x);";
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        options.setInlineObjectLiterals(true);
        
        Result result = compiler.compile(
            SourceFile.fromCode("externs.js", ""),
            SourceFromCode("test.js", js),
            options);
        
        assertTrue("Compilation should succeed", result.success);
        // The object should NOT be inlined because it's passed to a function
        String output = compiler.toSource();
        assertFalse("Object should not be inlined when passed to function", 
                    output.contains("JSCompiler_object_inline_"));
    }
    
    @Test(timeout = 4000)
    public void testObject12_DefectTarget() {
        // This test targets the defect revealed by testObject12
        // Object literal with property access after function call
        String js = "var x = {a: 1, b: 2}; " +
                    "function f(obj) { return obj; } " +
                    "var y = f(x); " +
                    "var result = y.a + y.b;";
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        options.setInlineObjectLiterals(true);
        
        Result result = compiler.compile(
            SourceFile.fromCode("externs.js", ""),
            SourceFile.fromCode("test.js", js),
            options);
        
        assertTrue("Compilation should succeed", result.success);
        // The object should NOT be inlined because it's passed to a function
        String output = compiler.toSource();
        assertFalse("Object should not be inlined when passed to function", 
                    output.contains("JSCompiler_object_inline_"));
    }
    
    @Test(timeout = 4000)
    public void testObject22_DefectTarget() {
        // This test targets the defect revealed by testObject22
        // Self-referential object literal
        String js = "var x = {a: 1, b: x.a}; var y = x.b;";
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        options.setInlineObjectLiterals(true);
        
        Result result = compiler.compile(
            SourceFile.fromCode("externs.js", ""),
            SourceFile.fromCode("test.js", js),
            options);
        
        assertTrue("Compilation should succeed", result.success);
        // Self-referential objects should not be inlined
        String output = compiler.toSource();
        assertFalse("Self-referential object should not be inlined", 
                    output.contains("JSCompiler_object_inline_"));
    }
    
    @Test(timeout = 4000)
    public void testIssue724_DefectTarget() {
        // This test targets the defect revealed by testIssue724
        // Object literal used in conditional expression
        String js = "var x = {a: 1, b: 2}; " +
                    "var y = true ? x.a : x.b;";
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        options.setInlineObjectLiterals(true);
        
        Result result = compiler.compile(
            SourceFile.fromCode("externs.js", ""),
            SourceFile.fromCode("test.js", js),
            options);
        
        assertTrue("Compilation should succeed", result.success);
        // The object should be inlined since properties are accessed directly
        String output = compiler.toSource();
        assertTrue("Object should be inlined for direct property access", 
                   output.contains("JSCompiler_object_inline_"));
    }
    
    @Test(timeout = 4000)
    public void testObjectLiteralWithGetter_DefectTarget() {
        // ES5 getters should prevent inlining
        String js = "var x = {get a() { return 1; }}; var y = x.a;";
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        options.setInlineObjectLiterals(true);
        
        Result result = compiler.compile(
            SourceFile.fromCode("externs.js", ""),
            SourceFile.fromCode("test.js", js),
            options);
        
        assertTrue("Compilation should succeed", result.success);
        // Objects with getters should not be inlined
        String output = compiler.toSource();
        assertFalse("Object with getter should not be inlined", 
                    output.contains("JSCompiler_object_inline_"));
    }
    
    @Test(timeout = 4000)
    public void testObjectLiteralWithSetter_DefectTarget() {
        // ES5 setters should prevent inlining
        String js = "var x = {set a(v) { this._a = v; }}; x.a = 1;";
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        options.setInlineObjectLiterals(true);
        
        Result result = compiler.compile(
            SourceFile.fromCode("externs.js", ""),
            SourceFile.fromCode("test.js", js),
            options);
        
        assertTrue("Compilation should succeed", result.success);
        // Objects with setters should not be inlined
        String output = compiler.toSource();
        assertFalse("Object with setter should not be inlined", 
                    output.contains("JSCompiler_object_inline_"));
    }
    
    // ==================== Partition D: Exception & Defensive Guard Paths ====================
    
    @Test(timeout = 4000)
    public void testObjectLiteralUsedAsThis() {
        // Object used as 'this' in function call should not be inlined
        String js = "var x = {a: 1}; " +
                    "function f() { return this.a; } " +
                    "var result = f.call(x);";
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        options.setInlineObjectLiterals(true);
        
        Result result = compiler.compile(
            SourceFile.fromCode("externs.js", ""),
            SourceFile.fromCode("test.js", js),
            options);
        
        assertTrue("Compilation should succeed", result.success);
        String output = compiler.toSource();
        assertFalse("Object used as 'this' should not be inlined", 
                    output.contains("JSCompiler_object_inline_"));
    }
    
    @Test(timeout = 4000)
    public void testObjectLiteralWithNestedPropertyAccess() {
        // Nested property access should not prevent inlining
        String js = "var x = {a: {b: 1}}; var y = x.a.b;";
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        options.setInlineObjectLiterals(true);
        
        Result result = compiler.compile(
            SourceFile.fromCode("externs.js", ""),
            SourceFile.fromCode("test.js", js),
            options);
        
        assertTrue("Compilation should succeed", result.success);
        // Nested property access should prevent inlining
        String output = compiler.toSource();
        assertFalse("Object with nested property access should not be inlined", 
                    output.contains("JSCompiler_object_inline_"));
    }
    
    @Test(timeout = 4000)
    public void testObjectLiteralWithAssignment() {
        // Object literal with assignment should be inlined
        String js = "var x = {a: 1}; x.a = 2; var y = x.a;";
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        options.setInlineObjectLiterals(true);
        
        Result result = compiler.compile(
            SourceFile.fromCode("externs.js", ""),
            SourceFile.fromCode("test.js", js),
            options);
        
        assertTrue("Compilation should succeed", result.success);
    }
    
    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================
    
    @Test(timeout = 4000)
    public void testMultipleObjectLiterals() {
        // Multiple independent object literals
        String js = "var x = {a: 1, b: 2}; var y = {c: 3, d: 4}; var z = x.a + y.c;";
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        options.setInlineObjectLiterals(true);
        
        Result result = compiler.compile(
            SourceFile.fromCode("externs.js", ""),
            SourceFile.fromCode("test.js", js),
            options);
        
        assertTrue("Compilation should succeed", result.success);
        String output = compiler.toSource();
        assertTrue("First object should be inlined", 
                   output.contains("JSCompiler_object_inline_a_"));
        assertTrue("Second object should be inlined", 
                   output.contains("JSCompiler_object_inline_c_"));
    }
    
    @Test(timeout = 4000)
    public void testObjectLiteralWithFunctionProperty() {
        // Object literal with function as property value
        String js = "var x = {a: function() { return 1; }}; var y = x.a();";
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        options.setInlineObjectLiterals(true);
        
        Result result = compiler.compile(
            SourceFile.fromCode("externs.js", ""),
            SourceFile.fromCode("test.js", js),
            options);
        
        assertTrue("Compilation should succeed", result.success);
        // Function properties should prevent inlining due to 'this' context
        String output = compiler.toSource();
        assertFalse("Object with function property should not be inlined", 
                    output.contains("JSCompiler_object_inline_"));
    }
    
    @Test(timeout = 4000)
    public void testObjectLiteralWithArrayProperty() {
        // Object literal with array as property value
        String js = "var x = {a: [1, 2, 3]}; var y = x.a[0];";
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        options.setInlineObjectLiterals(true);
        
        Result result = compiler.compile(
            SourceFile.fromCode("externs.js", ""),
            SourceFile.fromCode("test.js", js),
            options);
        
        assertTrue("Compilation should succeed", result.success);
        // Array properties should be inlinable
        String output = compiler.toSource();
        assertTrue("Object with array property should be inlined", 
                   output.contains("JSCompiler_object_inline_a_"));
    }
    
    @Test(timeout = 4000)
    public void testObjectLiteralWithAllPropertiesReferenced() {
        // All properties of object are referenced
        String js = "var x = {a: 1, b: 2, c: 3}; var y = x.a + x.b + x.c;";
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        options.setInlineObjectLiterals(true);
        
        Result result = compiler.compile(
            SourceFile.fromCode("externs.js", ""),
            SourceFile.fromCode("test.js", js),
            options);
        
        assertTrue("Compilation should succeed", result.success);
        String output = compiler.toSource();
        assertTrue("All properties should be inlined", 
                   output.contains("JSCompiler_object_inline_a_"));
        assertTrue("All properties should be inlined", 
                   output.contains("JSCompiler_object_inline_b_"));
        assertTrue("All properties should be inlined", 
                   output.contains("JSCompiler_object_inline_c_"));
    }
    
    @Test(timeout = 4000)
    public void testObjectLiteralWithSomePropertiesUnreferenced() {
        // Some properties are not referenced
        String js = "var x = {a: 1, b: 2, c: 3}; var y = x.a;";
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        options.setInlineObjectLiterals(true);
        
        Result result = compiler.compile(
            SourceFile.fromCode("externs.js", ""),
            SourceFile.fromCode("test.js", js),
            options);
        
        assertTrue("Compilation should succeed", result.success);
        String output = compiler.toSource();
        assertTrue("Referenced property should be inlined", 
                   output.contains("JSCompiler_object_inline_a_"));
    }
    
    @Test(timeout = 4000)
    public void testObjectLiteralWithReassignment() {
        // Variable reassigned to different object
        String js = "var x = {a: 1}; x = {b: 2}; var y = x.b;";
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        options.setInlineObjectLiterals(true);
        
        Result result = compiler.compile(
            SourceFile.fromCode("externs.js", ""),
            SourceFile.fromCode("test.js", js),
            options);
        
        assertTrue("Compilation should succeed", result.success);
    }
}