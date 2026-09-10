package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

/** Generated from approved defect-focused scenarios using native IPO. */
public class Closure_1b_IPOTest extends CompilerTestCase {

private boolean removeGlobal;
    private boolean preserveNames;
    public Closure_1b_IPOTest() { super("function alert() {}"); enableNormalize(); }
    protected CompilerPass getProcessor(final Compiler compiler) {
        return new RemoveUnusedVars(compiler, removeGlobal, preserveNames, false);
    }

    @Test(timeout = 4000)
    public void test_remove_unused_with_global_policy_001() throws Exception {
        // Native IPO combination: remove_globals=keep, program=global, preserve_names=off
        removeGlobal = false;
        preserveNames = false;
        boolean functionProgram = false;
        if (!functionProgram) {
            if (removeGlobal) { test("var x=1", ""); } else { testSame("var x=1"); }
        } else {
            String source = "var y=function(x){var z;}";
            if (removeGlobal) { test(source, ""); } else { test(source, "var y=function(x){}"); }
        }
        assertTrue(true);
    }

    @Test(timeout = 4000)
    public void test_remove_unused_with_global_policy_002() throws Exception {
        // Native IPO combination: remove_globals=keep, program=function, preserve_names=on
        removeGlobal = false;
        preserveNames = true;
        boolean functionProgram = true;
        if (!functionProgram) {
            if (removeGlobal) { test("var x=1", ""); } else { testSame("var x=1"); }
        } else {
            String source = "var y=function(x){var z;}";
            if (removeGlobal) { test(source, ""); } else { test(source, "var y=function(x){}"); }
        }
        assertTrue(true);
    }

    @Test(timeout = 4000)
    public void test_remove_unused_with_global_policy_003() throws Exception {
        // Native IPO combination: remove_globals=remove, program=global, preserve_names=on
        removeGlobal = true;
        preserveNames = true;
        boolean functionProgram = false;
        if (!functionProgram) {
            if (removeGlobal) { test("var x=1", ""); } else { testSame("var x=1"); }
        } else {
            String source = "var y=function(x){var z;}";
            if (removeGlobal) { test(source, ""); } else { test(source, "var y=function(x){}"); }
        }
        assertTrue(true);
    }

    @Test(timeout = 4000)
    public void test_remove_unused_with_global_policy_004() throws Exception {
        // Native IPO combination: remove_globals=remove, program=function, preserve_names=off
        removeGlobal = true;
        preserveNames = false;
        boolean functionProgram = true;
        if (!functionProgram) {
            if (removeGlobal) { test("var x=1", ""); } else { testSame("var x=1"); }
        } else {
            String source = "var y=function(x){var z;}";
            if (removeGlobal) { test(source, ""); } else { test(source, "var y=function(x){}"); }
        }
        assertTrue(true);
    }

    @Test(timeout = 4000)
    public void test_remove_unused_with_global_policy_005() throws Exception {
        // Native IPO combination: remove_globals=keep, program=function, preserve_names=off
        removeGlobal = false;
        preserveNames = false;
        boolean functionProgram = true;
        if (!functionProgram) {
            if (removeGlobal) { test("var x=1", ""); } else { testSame("var x=1"); }
        } else {
            String source = "var y=function(x){var z;}";
            if (removeGlobal) { test(source, ""); } else { test(source, "var y=function(x){}"); }
        }
        assertTrue(true);
    }

}
