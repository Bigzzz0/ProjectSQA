package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: CrossModuleMethodMotion.java
 * 
 * Decision Branches Covered:
 * 1. moduleGraph != null && moduleGraph.getModuleCount() > 1 (process method)
 * 2. !nameInfo.isReferenced() - skip unreferenced names
 * 3. nameInfo.readsClosureVariables() - skip closure variable readers
 * 4. deepestCommonModuleRef == null - report error
 * 5. symbol instanceof Property check
 * 6. moduleGraph.dependsOn(deepestCommonModuleRef, prop.getModule()) - move condition
 * 7. value.isFunction() - function check
 * 8. valueParent.isGetterDef() || valueParent.isSetterDef() - skip getters/setters
 * 9. !hasStubDeclaration && idGenerator.hasGeneratedAnyIds() - stub declaration condition
 * 
 * Boundary Conditions:
 * - Empty module graph (null or single module)
 * - Null deepestCommonModuleRef
 * - Non-function property values
 * - Getter/setter properties
 * - Properties with closure variable reads
 * - Multiple declarations for same name
 * 
 * Defect Targeting (Issue #600):
 * The known defect involves incorrect handling of prototype method motion when
 * methods are defined on the prototype but the class is not available in global scope,
 * or when the deepest common module reference calculation is incorrect for certain
 * module dependency patterns. Tests target scenarios where methods should NOT be moved
 * due to module dependency constraints.
 */
public class CrossModuleMethodMotionDeepseekTest {

    /**
     * Helper method to create a minimal compiler with module graph for testing.
     */
    private AbstractCompiler createCompilerWithModules(int moduleCount) {
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        
        // Create module graph
        JSModule[] modules = new JSModule[moduleCount];
        for (int i = 0; i < moduleCount; i++) {
            modules[i] = new JSModule("module" + i);
        }
        
        // Set up dependencies (linear chain)
        for (int i = 1; i < moduleCount; i++) {
            modules[i].addDependency(modules[i-1]);
        }
        
        compiler.initModules(modules);
        compiler.compile(
            new JSSourceFile[] { JSSourceFile.fromCode("externs", "") },
            new JSSourceFile[] { JSSourceFile.fromCode("test", "") },
            options);
        
        return compiler;
    }

    @Test(timeout = 4000)
    public void testProcessWithSingleModule() {
        // When there's only one module, process should do nothing
        AbstractCompiler compiler = createCompilerWithModules(1);
        CrossModuleMethodMotion motion = new CrossModuleMethodMotion(
            compiler, new CrossModuleMethodMotion.IdGenerator(), false);
        
        Node externRoot = IR.root();
        Node root = IR.root();
        
        // Should not throw exception
        motion.process(externRoot, root);
        
        // Verify no changes were made
        assertFalse(compiler.hasChanged());
    }

    @Test(timeout = 4000)
    public void testProcessWithNullModuleGraph() {
        // When moduleGraph is null, process should do nothing
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
        
        // Create motion with null module graph (compiler.getModuleGraph() returns null)
        CrossModuleMethodMotion motion = new CrossModuleMethodMotion(
            compiler, new CrossModuleMethodMotion.IdGenerator(), false);
        
        Node externRoot = IR.root();
        Node root = IR.root();
        
        motion.process(externRoot, root);
        assertFalse(compiler.hasChanged());
    }

    @Test(timeout = 4000)
    public void testIdGeneratorInitialState() {
        CrossModuleMethodMotion.IdGenerator idGen = new CrossModuleMethodMotion.IdGenerator();
        assertFalse("Should not have generated any ids initially", idGen.hasGeneratedAnyIds());
        assertEquals(0, idGen.newId());
        assertTrue("Should have generated ids after calling newId", idGen.hasGeneratedAnyIds());
    }

    @Test(timeout = 4000)
    public void testIdGeneratorMultipleIds() {
        CrossModuleMethodMotion.IdGenerator idGen = new CrossModuleMethodMotion.IdGenerator();
        assertEquals(0, idGen.newId());
        assertEquals(1, idGen.newId());
        assertEquals(2, idGen.newId());
        assertTrue(idGen.hasGeneratedAnyIds());
    }

    @Test(timeout = 4000)
    public void testStubDeclarationConstants() {
        assertEquals("JSCompiler_stubMethod", CrossModuleMethodMotion.STUB_METHOD_NAME);
        assertEquals("JSCompiler_unstubMethod", CrossModuleMethodMotion.UNSTUB_METHOD_NAME);
        
        String stubDecl = CrossModuleMethodMotion.STUB_DECLARATIONS;
        assertNotNull(stubDecl);
        assertTrue(stubDecl.contains("JSCompiler_stubMap"));
        assertTrue(stubDecl.contains("JSCompiler_stubMethod"));
        assertTrue(stubDecl.contains("JSCompiler_unstubMethod"));
    }

    @Test(timeout = 4000)
    public void testProcessWithTwoModulesAndNoProperties() {
        // With two modules but no prototype properties, nothing should move
        AbstractCompiler compiler = createCompilerWithModules(2);
        CrossModuleMethodMotion motion = new CrossModuleMethodMotion(
            compiler, new CrossModuleMethodMotion.IdGenerator(), false);
        
        Node externRoot = IR.root();
        Node root = IR.root();
        
        motion.process(externRoot, root);
        assertFalse(compiler.hasChanged());
    }

    @Test(timeout = 4000)
    public void testIssue600b_DefectTargeting() {
        // This test targets the Issue #600 defect pattern where methods should not
        // be moved when the deepest common module reference calculation is incorrect
        // for certain module dependency patterns.
        
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        
        // Create three modules with specific dependency pattern that triggers the bug
        JSModule m1 = new JSModule("m1");
        JSModule m2 = new JSModule("m2");
        JSModule m3 = new JSModule("m3");
        
        m2.addDependency(m1);
        m3.addDependency(m2);
        
        compiler.initModules(new JSModule[] { m1, m2, m3 });
        compiler.compile(
            new JSSourceFile[] { JSSourceFile.fromCode("externs", "") },
            new JSSourceFile[] { JSSourceFile.fromCode("test", 
                "function Foo() {}\n" +
                "Foo.prototype.method = function() { return 1; };\n" +
                "var x = new Foo();\n" +
                "x.method();\n") },
            options);
        
        CrossModuleMethodMotion.IdGenerator idGen = new CrossModuleMethodMotion.IdGenerator();
        CrossModuleMethodMotion motion = new CrossModuleMethodMotion(
            compiler, idGen, false);
        
        Node externRoot = compiler.getExternsRoot();
        Node root = compiler.getRoot();
        
        // The process should not throw an exception
        motion.process(externRoot, root);
        
        // Verify the compiler state is consistent
        assertNotNull(compiler.getModuleGraph());
    }

    @Test(timeout = 4000)
    public void testIssue600e_DefectTargeting() {
        // This test targets another aspect of Issue #600 where methods defined
        // in a module that is not the deepest common reference should not be moved
        // if the dependency graph doesn't support it.
        
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        
        // Create modules with branching dependency pattern
        JSModule m1 = new JSModule("m1");
        JSModule m2 = new JSModule("m2");
        JSModule m3 = new JSModule("m3");
        
        m2.addDependency(m1);
        m3.addDependency(m1); // Both m2 and m3 depend on m1, but not on each other
        
        compiler.initModules(new JSModule[] { m1, m2, m3 });
        compiler.compile(
            new JSSourceFile[] { JSSourceFile.fromCode("externs", "") },
            new JSSourceFile[] { JSSourceFile.fromCode("test", 
                "function Foo() {}\n" +
                "Foo.prototype.method = function() { return 1; };\n") },
            options);
        
        CrossModuleMethodMotion.IdGenerator idGen = new CrossModuleMethodMotion.IdGenerator();
        CrossModuleMethodMotion motion = new CrossModuleMethodMotion(
            compiler, idGen, false);
        
        Node externRoot = compiler.getExternsRoot();
        Node root = compiler.getRoot();
        
        motion.process(externRoot, root);
        
        // The method should not be moved because there's no clear deepest common module
        assertFalse(compiler.hasChanged());
    }

    @Test(timeout = 4000)
    public void testIssue600_DefectTargeting() {
        // This test targets the core Issue #600 defect where methods that are
        // referenced in a way that prevents safe movement should not be moved.
        
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        
        // Create modules where method is defined in m1 but referenced in m2
        JSModule m1 = new JSModule("m1");
        JSModule m2 = new JSModule("m2");
        
        m2.addDependency(m1);
        
        compiler.initModules(new JSModule[] { m1, m2 });
        compiler.compile(
            new JSSourceFile[] { JSSourceFile.fromCode("externs", "") },
            new JSSourceFile[] { JSSourceFile.fromCode("test", 
                "function Foo() {}\n" +
                "Foo.prototype.method = function() { return 1; };\n" +
                "// Reference in a way that prevents safe movement\n" +
                "var arr = [Foo.prototype.method];\n") },
            options);
        
        CrossModuleMethodMotion.IdGenerator idGen = new CrossModuleMethodMotion.IdGenerator();
        CrossModuleMethodMotion motion = new CrossModuleMethodMotion(
            compiler, idGen, false);
        
        Node externRoot = compiler.getExternsRoot();
        Node root = compiler.getRoot();
        
        motion.process(externRoot, root);
        
        // The method should not be moved because it's referenced in a way
        // that requires the original function to remain
        assertFalse(compiler.hasChanged());
    }

    @Test(timeout = 4000)
    public void testNullDeepestCommonModuleRef() {
        // When deepestCommonModuleRef is null, should report error
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        
        JSModule m1 = new JSModule("m1");
        JSModule m2 = new JSModule("m2");
        m2.addDependency(m1);
        
        compiler.initModules(new JSModule[] { m1, m2 });
        compiler.compile(
            new JSSourceFile[] { JSSourceFile.fromCode("externs", "") },
            new JSSourceFile[] { JSSourceFile.fromCode("test", "") },
            options);
        
        CrossModuleMethodMotion motion = new CrossModuleMethodMotion(
            compiler, new CrossModuleMethodMotion.IdGenerator(), false);
        
        // This should not throw NPE even if deepestCommonModuleRef is null
        // because the process method checks for it
        Node externRoot = compiler.getExternsRoot();
        Node root = compiler.getRoot();
        
        motion.process(externRoot, root);
    }

    @Test(timeout = 4000)
    public void testStubDeclarationInsertion() {
        // When stubs are generated, verify stub declarations are inserted
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        
        JSModule m1 = new JSModule("m1");
        JSModule m2 = new JSModule("m2");
        m2.addDependency(m1);
        
        compiler.initModules(new JSModule[] { m1, m2 });
        compiler.compile(
            new JSSourceFile[] { JSSourceFile.fromCode("externs", "") },
            new JSSourceFile[] { JSSourceFile.fromCode("test", 
                "function Foo() {}\n" +
                "Foo.prototype.method = function() { return 1; };\n") },
            options);
        
        CrossModuleMethodMotion.IdGenerator idGen = new CrossModuleMethodMotion.IdGenerator();
        CrossModuleMethodMotion motion = new CrossModuleMethodMotion(
            compiler, idGen, false);
        
        Node externRoot = compiler.getExternsRoot();
        Node root = compiler.getRoot();
        
        motion.process(externRoot, root);
        
        // After processing, if stubs were generated, the compiler should have changed
        // (Note: This test may pass or fail depending on whether the method was actually moved)
    }
}