package com.google.javascript.jscomp;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

public class InlineVariablesTest {

    private String process(InlineVariables.Mode mode, boolean inlineAllStrings, String js) {
        Compiler compiler = new Compiler();
        compiler.initOptions(new CompilerOptions());
        Node externs = new Node(Token.EMPTY);
        Node root = compiler.parseSyntheticCode(js);
        assertNotNull("Failed to parse: " + js, root);
        new InlineVariables(compiler, mode, inlineAllStrings).process(externs, root);
        return compiler.toSource(root);
    }

    private static void assertContains(String source, String expected) {
        assertTrue("Expected source to contain: " + expected + " but was: " + source,
                source.contains(expected));
    }

    private static void assertNotContains(String source, String unexpected) {
        assertFalse("Expected source not to contain: " + unexpected + " but was: " + source,
                source.contains(unexpected));
    }

    @Test
    public void testExternalIssue1053() {
        String js = "function f() { var x = {}; var y = x.foo; y(); }";
        String result = process(InlineVariables.Mode.ALL, true, js);
        assertContains(result, "var y = x.foo");
        assertContains(result, "y()");
        assertNotContains(result, "x.foo()");
    }

    @Test
    public void testInlineLocalVariable() {
        String result = process(InlineVariables.Mode.ALL, true,
                "function f() { var x = 1; alert(x); }");
        assertNotContains(result, "var x");
        assertContains(result, "alert(1)");
    }

    @Test
    public void testInlineGlobalVariableInAllMode() {
        String result = process(InlineVariables.Mode.ALL, true, "var x = 1; alert(x);");
        assertNotContains(result, "var x");
        assertContains(result, "alert(1)");
    }

    @Test
    public void testLocalsOnlyDoesNotInlineGlobal() {
        String result = process(InlineVariables.Mode.LOCALS_ONLY, true, "var x = 1; alert(x);");
        assertContains(result, "var x = 1");
    }

    @Test
    public void testConstantsOnlyDoesNotInlineNonConstant() {
        String result = process(InlineVariables.Mode.CONSTANTS_ONLY, true,
                "function f() { var x = 1; alert(x); }");
        assertContains(result, "var x = 1");
    }

    @Test
    public void testConstantsOnlyInlinesConstant() {
        String result = process(InlineVariables.Mode.CONSTANTS_ONLY, true,
                "function f() { var GREETING = 'hi'; alert(GREETING); }");
        assertNotContains(result, "GREETING");
        assertContains(result, "hi");
    }

    @Test
    public void testNoInlineModifiedVariable() {
        String result = process(InlineVariables.Mode.ALL, true,
                "function f() { var x = 1; x = 2; alert(x); }");
        assertContains(result, "var x = 1");
        assertContains(result, "x = 2");
    }

    @Test
    public void testNoInlineMultipleReferences() {
        String result = process(InlineVariables.Mode.ALL, true,
                "function f() { var x = 1; alert(x); alert(x); }");
        assertContains(result, "var x = 1");
    }

    @Test
    public void testNoInlineLValueAssignment() {
        String result = process(InlineVariables.Mode.ALL, true,
                "function f() { var x = 1; x = 2; }");
        assertContains(result, "var x = 1");
    }

    @Test
    public void testNoInlineCapturedByInnerFunction() {
        String result = process(InlineVariables.Mode.ALL, true,
                "function f() { var x = 1; function g() { alert(x); } return g; }");
        assertContains(result, "var x = 1");
    }

    @Test
    public void testInlineGetPropWithoutSideEffects() {
        String result = process(InlineVariables.Mode.ALL, true,
                "function f() { var x = obj; alert(x); }");
        assertNotContains(result, "var x");
        assertContains(result, "alert(obj)");
    }

    @Test
    public void testNoInlineGetPropAcrossSideEffects() {
        String result = process(InlineVariables.Mode.ALL, true,
                "function f() { var x = obj; sideEffect(); alert(x); }");
        assertContains(result, "var x = obj");
    }

    @Test
    public void testNoInlineForInLoopVariable() {
        String result = process(InlineVariables.Mode.ALL, true,
                "function f() { var x; for (x in obj) { alert(x); } }");
        assertContains(result, "var x");
    }
}