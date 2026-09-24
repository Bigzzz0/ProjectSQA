package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

import java.util.Set;

public class ProcessClosurePrimitivesDeepseekTest {

    /*
     * [Branch & Defect Analysis Matrix]
     * 
     * Target: ProcessClosurePrimitives
     * 
     * Key branches and boundaries:
     * 1. Constructor: null/valid preprocessorSymbolTable, requiresLevel ON/OFF
     * 2. process(): defineCalls replacement, providedNames replacement, unrecognizedRequires reporting
     * 3. visit(): CALL (base, define, require, provide, exportSymbol, addDependency, setCssNameMapping),
     *    ASSIGN, NAME, EXPR_RESULT, FUNCTION, GETPROP
     * 4. processRequireCall: null arg, non-string arg, extra args, unrecognized require, cross-module require
     * 5. processProvideCall: null arg, invalid name, duplicate provide, prefix registration
     * 6. processDefineCall: null args, non-string name, missing @define annotation, invalid name
     * 7. processBaseClassCall: missing this, non-this first arg, missing enclosing method, constructor vs method
     * 8. processSetCssNameMapping: null arg, non-objectlit, non-string values, invalid style, BY_PART hyphen check, BY_WHOLE n^2 check
     * 9. verify* methods: null, wrong type, too many args
     * 10. ProvidedName.replace(): duplicate definition, var conversion, implicit provides, module insertion
     * 11. Defect: MISSING_PROVIDE_ERROR when require is processed before provide in same pass
     *     - The bug: when a require is seen before a provide, it's added to unrecognizedRequires.
     *       Later, when the provide is processed, the require is NOT removed from unrecognizedRequires,
     *       causing a spurious MISSING_PROVIDE_ERROR.
     * 
     * Partitions:
     * A: Core functional logic (process, visit, provide/require/define handling)
     * B: Boundary values (null args, empty strings, invalid identifiers, too many args)
     * C: Defect-targeted (require-before-provide scenario)
     * D: Exception/defensive paths (invalid CSS mapping, bad base class calls)
     * E: Lifecycle/contract (getExportedVariableNames, state after process)
     */

    private Compiler createCompiler() {
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        options.setCheckRequires(CheckLevel.ERROR);
        compiler.initOptions(options);
        return compiler;
    }

    private ProcessClosurePrimitives createPass(Compiler compiler) {
        return new ProcessClosurePrimitives(compiler, null, CheckLevel.ERROR);
    }

    private Node parseAndTraverse(Compiler compiler, String code) {
        Node root = compiler.parseTestCode(code);
        assertNotNull("Parsing failed", root);
        ProcessClosurePrimitives pass = createPass(compiler);
        pass.process(null, root);
        return root;
    }

    // ==================== PARTITION A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testProcessWithSimpleProvide() {
        Compiler compiler = createCompiler();
        Node root = parseAndTraverse(compiler, "goog.provide('foo.bar');");
        assertNotNull(root);
        // The provide should be replaced with a namespace declaration
        assertTrue("Expected code change", compiler.hasErrors() == false);
    }

    @Test(timeout = 4000)
    public void testProcessWithRequireAndProvide() {
        Compiler compiler = createCompiler();
        Node root = parseAndTraverse(compiler,
            "goog.provide('foo.bar'); goog.require('foo.bar');");
        assertNotNull(root);
        // No errors expected when require matches provide
        assertEquals("Expected no errors", 0, compiler.getErrorCount());
    }

    @Test(timeout = 4000)
    public void testProcessWithExportSymbol() {
        Compiler compiler = createCompiler();
        Node root = parseAndTraverse(compiler,
            "goog.exportSymbol('foo.bar', baz);");
        assertNotNull(root);
        ProcessClosurePrimitives pass = createPass(compiler);
        Set<String> exported = pass.getExportedVariableNames();
        assertTrue("Expected foo in exported variables", exported.contains("foo"));
    }

    @Test(timeout = 4000)
    public void testProcessWithExportSymbolNoDot() {
        Compiler compiler = createCompiler();
        Node root = parseAndTraverse(compiler,
            "goog.exportSymbol('foo', baz);");
        assertNotNull(root);
        ProcessClosurePrimitives pass = createPass(compiler);
        Set<String> exported = pass.getExportedVariableNames();
        assertTrue("Expected foo in exported variables", exported.contains("foo"));
    }

    @Test(timeout = 4000)
    public void testProcessWithAddDependency() {
        Compiler compiler = createCompiler();
        Node root = parseAndTraverse(compiler,
            "goog.addDependency('file.js', ['foo.bar'], []);");
        assertNotNull(root);
        // Should not throw, and should replace with a number node
        assertTrue("Expected no errors", compiler.getErrorCount() == 0);
    }

    @Test(timeout = 4000)
    public void testProcessWithDefineCall() {
        Compiler compiler = createCompiler();
        Node root = parseAndTraverse(compiler,
            "/** @define {boolean} */ var FLAG = goog.define('FLAG', true);");
        assertNotNull(root);
        // Define call should be processed without errors
        assertTrue("Expected no errors", compiler.getErrorCount() == 0);
    }

    @Test(timeout = 4000)
    public void testProcessWithTypedefDefinition() {
        Compiler compiler = createCompiler();
        Node root = parseAndTraverse(compiler,
            "goog.provide('foo.bar'); /** @typedef {number} */ foo.bar;");
        assertNotNull(root);
        // Typedef should be handled without errors
        assertTrue("Expected no errors", compiler.getErrorCount() == 0);
    }

    @Test(timeout = 4000)
    public void testProcessWithFunctionDeclarationOnProvidedName() {
        Compiler compiler = createCompiler();
        Node root = parseAndTraverse(compiler,
            "goog.provide('foo'); function foo() {}");
        assertNotNull(root);
        // Should report FUNCTION_NAMESPACE_ERROR
        assertTrue("Expected function namespace error", compiler.getErrorCount() > 0);
    }

    @Test(timeout = 4000)
    public void testProcessWithBaseClassCallConstructor() {
        Compiler compiler = createCompiler();
        Node root = parseAndTraverse(compiler,
            "function Foo() { goog.base(this); } goog.inherits(Foo, BaseFoo);");
        assertNotNull(root);
        // Should rewrite goog.base to BaseFoo.call
        assertTrue("Expected no errors", compiler.getErrorCount() == 0);
    }

    @Test(timeout = 4000)
    public void testProcessWithBaseClassCallMethod() {
        Compiler compiler = createCompiler();
        Node root = parseAndTraverse(compiler,
            "function Foo() {} Foo.prototype.bar = function() { goog.base(this, 'bar'); };");
        assertNotNull(root);
        // Should rewrite goog.base to superClass_ call
        assertTrue("Expected no errors", compiler.getErrorCount() == 0);
    }

    // ==================== PARTITION B: Boundary Value Analysis ====================

    @Test(timeout = 4000)
    public void testProcessWithNullArgumentToProvide() {
        Compiler compiler = createCompiler();
        Node root = parseAndTraverse(compiler, "goog.provide();");
        assertNotNull(root);
        // Should report NULL_ARGUMENT_ERROR
        assertTrue("Expected null argument error", compiler.getErrorCount() > 0);
    }

    @Test(timeout = 4000)
    public void testProcessWithNonStringArgumentToProvide() {
        Compiler compiler = createCompiler();
        Node root = parseAndTraverse(compiler, "goog.provide(123);");
        assertNotNull(root);
        // Should report INVALID_ARGUMENT_ERROR
        assertTrue("Expected invalid argument error", compiler.getErrorCount() > 0);
    }

    @Test(timeout = 4000)
    public void testProcessWithTooManyArgumentsToProvide() {
        Compiler compiler = createCompiler();
        Node root = parseAndTraverse(compiler, "goog.provide('foo', 'bar');");
        assertNotNull(root);
        // Should report TOO_MANY_ARGUMENTS_ERROR
        assertTrue("Expected too many arguments error", compiler.getErrorCount() > 0);
    }

    @Test(timeout = 4000)
    public void testProcessWithInvalidProvideName() {
        Compiler compiler = createCompiler();
        Node root = parseAndTraverse(compiler, "goog.provide('foo-bar');");
        assertNotNull(root);
        // Should report INVALID_PROVIDE_ERROR
        assertTrue("Expected invalid provide error", compiler.getErrorCount() > 0);
    }

    @Test(timeout = 4000)
    public void testProcessWithDuplicateProvide() {
        Compiler compiler = createCompiler();
        Node root = parseAndTraverse(compiler,
            "goog.provide('foo'); goog.provide('foo');");
        assertNotNull(root);
        // Should report DUPLICATE_NAMESPACE_ERROR
        assertTrue("Expected duplicate namespace error", compiler.getErrorCount() > 0);
    }

    @Test(timeout = 4000)
    public void testProcessWithNullArgumentToRequire() {
        Compiler compiler = createCompiler();
        Node root = parseAndTraverse(compiler, "goog.require();");
        assertNotNull(root);
        // Should report NULL_ARGUMENT_ERROR
        assertTrue("Expected null argument error", compiler.getErrorCount() > 0);
    }

    @Test(timeout = 4000)
    public void testProcessWithNonStringArgumentToRequire() {
        Compiler compiler = createCompiler();
        Node root = parseAndTraverse(compiler, "goog.require(123);");
        assertNotNull(root);
        // Should report INVALID_ARGUMENT_ERROR
        assertTrue("Expected invalid argument error", compiler.getErrorCount() > 0);
    }

    @Test(timeout = 4000)
    public void testProcessWithMissingProvide() {
        Compiler compiler = createCompiler();
        Node root = parseAndTraverse(compiler, "goog.require('foo.bar');");
        assertNotNull(root);
        // Should report MISSING_PROVIDE_ERROR
        assertTrue("Expected missing provide error", compiler.getErrorCount() > 0);
    }

    @Test(timeout = 4000)
    public void testProcessWithSetCssNameMappingNullArg() {
        Compiler compiler = createCompiler();
        Node root = parseAndTraverse(compiler, "goog.setCssNameMapping();");
        assertNotNull(root);
        // Should report NULL_ARGUMENT_ERROR
        assertTrue("Expected null argument error", compiler.getErrorCount() > 0);
    }

    @Test(timeout = 4000)
    public void testProcessWithSetCssNameMappingNonObject() {
        Compiler compiler = createCompiler();
        Node root = parseAndTraverse(compiler, "goog.setCssNameMapping('foo');");
        assertNotNull(root);
        // Should report EXPECTED_OBJECTLIT_ERROR
        assertTrue("Expected object literal error", compiler.getErrorCount() > 0);
    }

    @Test(timeout = 4000)
    public void testProcessWithSetCssNameMappingNonStringValue() {
        Compiler compiler = createCompiler();
        Node root = parseAndTraverse(compiler,
            "goog.setCssNameMapping({foo: 123});");
        assertNotNull(root);
        // Should report NON_STRING_PASSED_TO_SET_CSS_NAME_MAPPING_ERROR
        assertTrue("Expected non-string value error", compiler.getErrorCount() > 0);
    }

    @Test(timeout = 4000)
    public void testProcessWithSetCssNameMappingInvalidStyle() {
        Compiler compiler = createCompiler();
        Node root = parseAndTraverse(compiler,
            "goog.setCssNameMapping({foo: 'bar'}, 'INVALID');");
        assertNotNull(root);
        // Should report INVALID_STYLE_ERROR
        assertTrue("Expected invalid style error", compiler.getErrorCount() > 0);
    }

    @Test(timeout = 4000)
    public void testProcessWithSetCssNameMappingByPartHyphen() {
        Compiler compiler = createCompiler();
        Node root = parseAndTraverse(compiler,
            "goog.setCssNameMapping({'foo-bar': 'baz'}, 'BY_PART');");
        assertNotNull(root);
        // Should report INVALID_CSS_RENAMING_MAP warning
        assertTrue("Expected css renaming map warning", compiler.getWarningCount() > 0);
    }

    @Test(timeout = 4000)
    public void testProcessWithSetCssNameMappingByWhole() {
        Compiler compiler = createCompiler();
        Node root = parseAndTraverse(compiler,
            "goog.setCssNameMapping({'a': 'x', 'b': 'y', 'a-b': 'z'}, 'BY_WHOLE');");
        assertNotNull(root);
        // Should report INVALID_CSS_RENAMING_MAP warning because a-b != x-y
        assertTrue("Expected css renaming map warning", compiler.getWarningCount() > 0);
    }

    // ==================== PARTITION C: Defect-Targeted Branch Zone ====================

    @Test(timeout = 4000)
    public void testRequireBeforeProvideInSamePass() {
        // This is the defect scenario: require is processed before provide
        // The bug: unrecognizedRequires is not cleaned up when the provide is later seen
        Compiler compiler = createCompiler();
        Node root = parseAndTraverse(compiler,
            "goog.require('namespace.Class1'); goog.provide('namespace.Class1');");
        assertNotNull(root);
        // Expected behavior: no errors because the provide exists
        // Defective behavior: reports MISSING_PROVIDE_ERROR
        assertEquals("Expected no errors when require is followed by provide",
            0, compiler.getErrorCount());
    }

    @Test(timeout = 4000)
    public void testRequireBeforeProvideWithDefinition() {
        Compiler compiler = createCompiler();
        Node root = parseAndTraverse(compiler,
            "goog.require('namespace.Class1'); goog.provide('namespace.Class1'); namespace.Class1 = function() {};");
        assertNotNull(root);
        // Expected: no errors, the provide is explicit
        assertEquals("Expected no errors when provide follows require",
            0, compiler.getErrorCount());
    }

    @Test(timeout = 4000)
    public void testRequireBeforeImplicitProvide() {
        Compiler compiler = createCompiler();
        Node root = parseAndTraverse(compiler,
            "goog.require('goog');");
        assertNotNull(root);
        // 'goog' is implicitly provided, should not error
        assertEquals("Expected no errors for implicit goog provide",
            0, compiler.getErrorCount());
    }

    // ==================== PARTITION D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000)
    public void testBaseClassCallWithNonThisFirstArg() {
        Compiler compiler = createCompiler();
        Node root = parseAndTraverse(compiler,
            "function Foo() { goog.base(bar); }");
        assertNotNull(root);
        // Should report BASE_CLASS_ERROR
        assertTrue("Expected base class error", compiler.getErrorCount() > 0);
    }

    @Test(timeout = 4000)
    public void testBaseClassCallWithNoEnclosingMethod() {
        Compiler compiler = createCompiler();
        Node root = parseAndTraverse(compiler,
            "var x = goog.base(this);");
        assertNotNull(root);
        // Should report BASE_CLASS_ERROR
        assertTrue("Expected base class error", compiler.getErrorCount() > 0);
    }

    @Test(timeout = 4000)
    public void testBaseClassCallWithNoInherits() {
        Compiler compiler = createCompiler();
        Node root = parseAndTraverse(compiler,
            "function Foo() { goog.base(this); }");
        assertNotNull(root);
        // Should report BASE_CLASS_ERROR
        assertTrue("Expected base class error", compiler.getErrorCount() > 0);
    }

    @Test(timeout = 4000)
    public void testBaseClassCallWithNonStringMethodName() {
        Compiler compiler = createCompiler();
        Node root = parseAndTraverse(compiler,
            "function Foo() {} Foo.prototype.bar = function() { goog.base(this, 123); };");
        assertNotNull(root);
        // Should report BASE_CLASS_ERROR
        assertTrue("Expected base class error", compiler.getErrorCount() > 0);
    }

    @Test(timeout = 4000)
    public void testBaseClassCallWithMismatchedMethodName() {
        Compiler compiler = createCompiler();
        Node root = parseAndTraverse(compiler,
            "function Foo() {} Foo.prototype.bar = function() { goog.base(this, 'baz'); };");
        assertNotNull(root);
        // Should report BASE_CLASS_ERROR
        assertTrue("Expected base class error", compiler.getErrorCount() > 0);
    }

    @Test(timeout = 4000)
    public void testGetPropGoogBaseOutsideCall() {
        Compiler compiler = createCompiler();
        Node root = parseAndTraverse(compiler,
            "var x = goog.base;");
        assertNotNull(root);
        // Should report BASE_CLASS_ERROR
        assertTrue("Expected base class error", compiler.getErrorCount() > 0);
    }

    @Test(timeout = 4000)
    public void testDefineCallWithMissingAnnotation() {
        Compiler compiler = createCompiler();
        Node root = parseAndTraverse(compiler,
            "var FLAG = goog.define('FLAG', true);");
        assertNotNull(root);
        // Should report MISSING_DEFINE_ANNOTATION
        assertTrue("Expected missing define annotation error", compiler.getErrorCount() > 0);
    }

    @Test(timeout = 4000)
    public void testDefineCallWithInvalidName() {
        Compiler compiler = createCompiler();
        Node root = parseAndTraverse(compiler,
            "/** @define {boolean} */ var FLAG = goog.define('foo-bar', true);");
        assertNotNull(root);
        // Should report INVALID_DEFINE_NAME_ERROR
        assertTrue("Expected invalid define name error", compiler.getErrorCount() > 0);
    }

    // ==================== PARTITION E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testGetExportedVariableNamesEmptyInitially() {
        Compiler compiler = createCompiler();
        ProcessClosurePrimitives pass = createPass(compiler);
        Set<String> exported = pass.getExportedVariableNames();
        assertNotNull("Exported variables set should not be null", exported);
        assertTrue("Expected empty set initially", exported.isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetExportedVariableNamesAfterMultipleExports() {
        Compiler compiler = createCompiler();
        Node root = parseAndTraverse(compiler,
            "goog.exportSymbol('foo.bar', x); goog.exportSymbol('baz', y);");
        assertNotNull(root);
        ProcessClosurePrimitives pass = createPass(compiler);
        Set<String> exported = pass.getExportedVariableNames();
        assertTrue("Expected foo in exported variables", exported.contains("foo"));
        assertTrue("Expected baz in exported variables", exported.contains("baz"));
        assertEquals("Expected exactly 2 exported variables", 2, exported.size());
    }

    @Test(timeout = 4000)
    public void testProcessWithNullExterns() {
        Compiler compiler = createCompiler();
        Node root = compiler.parseTestCode("var x = 1;");
        assertNotNull(root);
        ProcessClosurePrimitives pass = createPass(compiler);
        // Should not throw with null externs
        pass.process(null, root);
        assertTrue("Expected no errors", compiler.getErrorCount() == 0);
    }

    @Test(timeout = 4000)
    public void testHotSwapScript() {
        Compiler compiler = createCompiler();
        Node root = compiler.parseTestCode("goog.provide('foo');");
        assertNotNull(root);
        ProcessClosurePrimitives pass = createPass(compiler);
        // Should not throw
        pass.hotSwapScript(root, null);
        assertTrue("Expected no errors", compiler.getErrorCount() == 0);
    }

    @Test(timeout = 4000)
    public void testProcessWithRequiresLevelOff() {
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        options.setCheckRequires(CheckLevel.OFF);
        compiler.initOptions(options);
        Node root = compiler.parseTestCode("goog.require('foo.bar');");
        assertNotNull(root);
        ProcessClosurePrimitives pass = new ProcessClosurePrimitives(compiler, null, CheckLevel.OFF);
        pass.process(null, root);
        // No errors expected when requires level is off
        assertEquals("Expected no errors", 0, compiler.getErrorCount());
    }
}