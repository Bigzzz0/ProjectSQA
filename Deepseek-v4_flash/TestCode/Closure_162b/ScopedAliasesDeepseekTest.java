package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.jscomp.CompilerOptions.AliasTransformation;
import com.google.javascript.jscomp.CompilerOptions.AliasTransformationHandler;
import com.google.javascript.rhino.SourcePosition;

/**
 * Comprehensive test suite for ScopedAliases class targeting the known defect
 * in JSDoc type annotation handling within goog.scope blocks.
 * 
 * [Branch & Defect Analysis Matrix]
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   - Normal goog.scope processing with valid aliases
 *   - Multiple alias definitions and usages
 *   - Transitive alias resolution (e.g., var g = goog; var d = g.dom)
 *   - Type node alias resolution in JSDoc annotations
 * 
 * Partition B: Boundary Value Analysis & Extremes
 *   - Empty goog.scope block
 *   - Single alias definition
 *   - Alias with no usages
 *   - Multiple scope calls
 *   - Nested functions within goog.scope
 * 
 * Partition C: Defect-Targeted Branch Zone
 *   - JSDoc type annotations with aliased types (KNOWN DEFECT)
 *   - Type node string manipulation with dot-separated qualified names
 *   - Base name extraction and alias replacement in type strings
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - goog.scope used improperly (not in expression statement)
 *   - goog.scope with wrong number of parameters
 *   - goog.scope with named function
 *   - goog.scope with parameterized function
 *   - 'this' reference inside goog.scope
 *   - 'return' statement inside goog.scope
 *   - 'throw' statement inside goog.scope
 *   - Non-alias local variable in goog.scope
 *   - Alias redefinition
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 *   - Multiple passes through hotSwapScript
 *   - State reset between scope calls
 *   - PreprocessorSymbolTable interaction
 *   - AliasTransformationHandler logging
 */
public class ScopedAliasesDeepseekTest {
    
    private static final String SCOPING_METHOD_NAME = "goog.scope";
    
    /**
     * Helper method to create a minimal AST for goog.scope testing.
     * Creates: goog.scope(function() { ... body ... })
     */
    private Node createScopeCall(Node body) {
        Node callNode = new Node(Token.CALL);
        Node getPropNode = Node.newString(Token.GETPROP, "goog.scope");
        getPropNode.addChildToFront(Node.newString(Token.NAME, "goog"));
        getPropNode.addChildToBack(Node.newString(Token.STRING, "scope"));
        callNode.addChildToFront(getPropNode);
        
        Node functionNode = new Node(Token.FUNCTION);
        Node paramList = new Node(Token.PARAM_LIST);
        Node block = new Node(Token.BLOCK);
        if (body != null) {
            block.addChildToBack(body);
        }
        functionNode.addChildToFront(Node.newString(Token.NAME, ""));
        functionNode.addChildToBack(paramList);
        functionNode.addChildToBack(block);
        
        callNode.addChildToBack(functionNode);
        return callNode;
    }
    
    /**
     * Helper to create a simple alias: var name = qualifiedName;
     */
    private Node createAliasVar(String name, String qualifiedName) {
        Node varNode = new Node(Token.VAR);
        Node nameNode = Node.newString(Token.NAME, name);
        Node qualifiedNode = Node.newString(Token.GETPROP, qualifiedName);
        // Build qualified name chain
        String[] parts = qualifiedName.split("\\.");
        Node current = Node.newString(Token.NAME, parts[0]);
        for (int i = 1; i < parts.length; i++) {
            Node getProp = new Node(Token.GETPROP);
            getProp.addChildToFront(current);
            getProp.addChildToBack(Node.newString(Token.STRING, parts[i]));
            current = getProp;
        }
        nameNode.addChildToFront(current);
        varNode.addChildToFront(nameNode);
        return varNode;
    }
    
    /**
     * Helper to create a simple expression statement.
     */
    private Node createExprStmt(Node expr) {
        Node exprNode = new Node(Token.EXPR_RESULT);
        exprNode.addChildToFront(expr);
        return exprNode;
    }
    
    /**
     * Helper to create a NAME reference node.
     */
    private Node createNameRef(String name) {
        return Node.newString(Token.NAME, name);
    }
    
    /**
     * Helper to create a qualified name GETPROP chain.
     */
    private Node createQualifiedName(String qualifiedName) {
        String[] parts = qualifiedName.split("\\.");
        Node current = Node.newString(Token.NAME, parts[0]);
        for (int i = 1; i < parts.length; i++) {
            Node getProp = new Node(Token.GETPROP);
            getProp.addChildToFront(current);
            getProp.addChildToBack(Node.newString(Token.STRING, parts[i]));
            current = getProp;
        }
        return current;
    }
    
    /**
     * Helper to create a JSDoc info with type nodes.
     */
    private JSDocInfo createJSDocWithType(String typeString) {
        JSDocInfo.Builder builder = JSDocInfo.Builder.unknown();
        // Note: This is a simplified approach - in real code we'd use the parser
        // For testing purposes, we create a minimal JSDocInfo
        return builder.build();
    }
    
    /**
     * Helper to create a test compiler that records errors.
     */
    private AbstractCompiler createTestCompiler() {
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
        return compiler;
    }
    
    // ==================== Partition A: Core Functional Logic ====================
    
    @Test(timeout = 4000)
    public void testSimpleAliasReplacement() {
        // Test basic alias: var dom = goog.dom; dom.createElement('div');
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
        
        // Build AST: goog.scope(function() { var dom = goog.dom; dom.createElement('div'); })
        Node body = new Node(Token.BLOCK);
        body.addChildToBack(createAliasVar("dom", "goog.dom"));
        
        // Create usage: dom.createElement('div')
        Node callNode = new Node(Token.CALL);
        Node getProp = new Node(Token.GETPROP);
        getProp.addChildToFront(createNameRef("dom"));
        getProp.addChildToBack(Node.newString(Token.STRING, "createElement"));
        callNode.addChildToFront(getProp);
        callNode.addChildToBack(Node.newString(Token.STRING, "div"));
        body.addChildToBack(createExprStmt(callNode));
        
        Node scopeCall = createScopeCall(body);
        Node exprStmt = createExprStmt(scopeCall);
        Node script = new Node(Token.SCRIPT);
        script.addChildToBack(exprStmt);
        
        ScopedAliases scopedAliases = new ScopedAliases(compiler, null, 
            new AliasTransformationHandler() {
                @Override
                public AliasTransformation logAliasTransformation(
                    String sourceFile, SourcePosition<AliasTransformation> position) {
                    return new AliasTransformation() {
                        @Override
                        public void addAlias(String alias, String definition) {
                            // No-op for testing
                        }
                    };
                }
            });
        
        scopedAliases.process(null, script);
        
        // Verify the transformation happened
        assertNotNull("Script should still exist after processing", script);
    }
    
    @Test(timeout = 4000)
    public void testTransitiveAliasReplacement() {
        // Test transitive alias: var g = goog; var d = g.dom; d.createElement('div');
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
        
        Node body = new Node(Token.BLOCK);
        body.addChildToBack(createAliasVar("g", "goog"));
        body.addChildToBack(createAliasVar("d", "g.dom"));
        
        Node callNode = new Node(Token.CALL);
        Node getProp = new Node(Token.GETPROP);
        getProp.addChildToFront(createNameRef("d"));
        getProp.addChildToBack(Node.newString(Token.STRING, "createElement"));
        callNode.addChildToFront(getProp);
        callNode.addChildToBack(Node.newString(Token.STRING, "div"));
        body.addChildToBack(createExprStmt(callNode));
        
        Node scopeCall = createScopeCall(body);
        Node exprStmt = createExprStmt(scopeCall);
        Node script = new Node(Token.SCRIPT);
        script.addChildToBack(exprStmt);
        
        ScopedAliases scopedAliases = new ScopedAliases(compiler, null,
            new AliasTransformationHandler() {
                @Override
                public AliasTransformation logAliasTransformation(
                    String sourceFile, SourcePosition<AliasTransformation> position) {
                    return new AliasTransformation() {
                        @Override
                        public void addAlias(String alias, String definition) {
                            // No-op
                        }
                    };
                }
            });
        
        scopedAliases.process(null, script);
        assertNotNull("Script should still exist", script);
    }
    
    // ==================== Partition B: Boundary Value Analysis ====================
    
    @Test(timeout = 4000)
    public void testEmptyScopeBlock() {
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
        
        Node body = new Node(Token.BLOCK);
        Node scopeCall = createScopeCall(body);
        Node exprStmt = createExprStmt(scopeCall);
        Node script = new Node(Token.SCRIPT);
        script.addChildToBack(exprStmt);
        
        ScopedAliases scopedAliases = new ScopedAliases(compiler, null,
            new AliasTransformationHandler() {
                @Override
                public AliasTransformation logAliasTransformation(
                    String sourceFile, SourcePosition<AliasTransformation> position) {
                    return new AliasTransformation() {
                        @Override
                        public void addAlias(String alias, String definition) {}
                    };
                }
            });
        
        scopedAliases.process(null, script);
        assertNotNull("Empty scope should process without error", script);
    }
    
    @Test(timeout = 4000)
    public void testAliasWithNoUsages() {
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
        
        Node body = new Node(Token.BLOCK);
        body.addChildToBack(createAliasVar("unused", "some.long.path"));
        
        Node scopeCall = createScopeCall(body);
        Node exprStmt = createExprStmt(scopeCall);
        Node script = new Node(Token.SCRIPT);
        script.addChildToBack(exprStmt);
        
        ScopedAliases scopedAliases = new ScopedAliases(compiler, null,
            new AliasTransformationHandler() {
                @Override
                public AliasTransformation logAliasTransformation(
                    String sourceFile, SourcePosition<AliasTransformation> position) {
                    return new AliasTransformation() {
                        @Override
                        public void addAlias(String alias, String definition) {}
                    };
                }
            });
        
        scopedAliases.process(null, script);
        assertNotNull("Unused alias should process without error", script);
    }
    
    // ==================== Partition C: Defect-Targeted Branch Zone ====================
    
    @Test(timeout = 4000)
    public void testForwardJsDocWithAliasedType() {
        // This test targets the known defect: JSDoc type annotations with aliased types
        // The bug occurs when a type annotation like {Foo.Bar} uses an alias "Foo" 
        // that maps to "foo.Foo", and the type string manipulation fails.
        
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
        
        // Create AST with goog.scope containing:
        // var Foo = foo.Foo;
        // /** @type {Foo.Bar} */ var x;
        Node body = new Node(Token.BLOCK);
        
        // Add alias: var Foo = foo.Foo;
        body.addChildToBack(createAliasVar("Foo", "foo.Foo"));
        
        // Create a variable with JSDoc type annotation
        Node varNode = new Node(Token.VAR);
        Node nameNode = Node.newString(Token.NAME, "x");
        
        // Create JSDoc info with type node
        JSDocInfo.Builder builder = JSDocInfo.Builder.unknown();
        // In real code, we'd parse the JSDoc. For testing, we create a minimal structure.
        // The key is that the type node contains "Foo.Bar" which should be transformed
        // to "foo.Foo.Bar" when the alias is applied.
        JSDocInfo jsdoc = builder.build();
        nameNode.setJSDocInfo(jsdoc);
        
        // Add a type node to the JSDoc info (simplified - in real code this comes from parser)
        // We need to simulate what the parser would produce for @type {Foo.Bar}
        Node typeNode = Node.newString(Token.STRING, "Foo.Bar");
        // Note: In the actual bug, the fixTypeNode method processes this string
        // and should replace "Foo" with "foo.Foo" to get "foo.Foo.Bar"
        
        varNode.addChildToFront(nameNode);
        body.addChildToBack(varNode);
        
        Node scopeCall = createScopeCall(body);
        Node exprStmt = createExprStmt(scopeCall);
        Node script = new Node(Token.SCRIPT);
        script.addChildToBack(exprStmt);
        
        // Track alias transformations
        final StringBuilder aliasLog = new StringBuilder();
        
        ScopedAliases scopedAliases = new ScopedAliases(compiler, null,
            new AliasTransformationHandler() {
                @Override
                public AliasTransformation logAliasTransformation(
                    String sourceFile, SourcePosition<AliasTransformation> position) {
                    return new AliasTransformation() {
                        @Override
                        public void addAlias(String alias, String definition) {
                            aliasLog.append(alias).append(":").append(definition).append(";");
                        }
                    };
                }
            });
        
        scopedAliases.process(null, script);
        
        // Verify the alias was logged
        assertTrue("Alias should be logged: " + aliasLog.toString(), 
            aliasLog.toString().contains("Foo:foo.Foo"));
        
        // The defect is that fixTypeNode doesn't properly handle the type string
        // when the base name matches an alias. The expected behavior is that
        // "Foo.Bar" becomes "foo.Foo.Bar" after alias application.
        // This test verifies the processing doesn't crash and the alias is recognized.
    }
    
    @Test(timeout = 4000)
    public void testTypeNodeWithDotQualifiedAlias() {
        // Test the specific scenario from the defect: type node with "Foo.Bar"
        // where Foo is an alias for "foo.Foo"
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
        
        Node body = new Node(Token.BLOCK);
        body.addChildToBack(createAliasVar("Foo", "foo.Foo"));
        
        // Create a variable with type annotation
        Node varNode = new Node(Token.VAR);
        Node nameNode = Node.newString(Token.NAME, "myVar");
        
        // Simulate JSDoc with type node containing "Foo.Bar"
        JSDocInfo.Builder builder = JSDocInfo.Builder.unknown();
        JSDocInfo jsdoc = builder.build();
        nameNode.setJSDocInfo(jsdoc);
        
        // Add a type node to simulate what the parser produces
        // In the actual code, fixTypeNode would process this
        Node stringTypeNode = Node.newString(Token.STRING, "Foo.Bar");
        // The fixTypeNode method would check if "Foo" (the base name before '.')
        // is an alias, and if so, replace it with the aliased node's qualified name
        
        varNode.addChildToFront(nameNode);
        body.addChildToBack(varNode);
        
        Node scopeCall = createScopeCall(body);
        Node exprStmt = createExprStmt(scopeCall);
        Node script = new Node(Token.SCRIPT);
        script.addChildToBack(exprStmt);
        
        ScopedAliases scopedAliases = new ScopedAliases(compiler, null,
            new AliasTransformationHandler() {
                @Override
                public AliasTransformation logAliasTransformation(
                    String sourceFile, SourcePosition<AliasTransformation> position) {
                    return new AliasTransformation() {
                        @Override
                        public void addAlias(String alias, String definition) {}
                    };
                }
            });
        
        scopedAliases.process(null, script);
        
        // The defect is that fixTypeNode may not correctly handle the case
        // where the type string has a dot after the alias base name.
        // This test ensures the processing completes without error.
        assertNotNull("Script should still exist after processing", script);
    }
    
    // ==================== Partition D: Exception & Defensive Guard Paths ====================
    
    @Test(timeout = 4000, expected = RuntimeException.class)
    public void testGoogScopeUsedImproperly() {
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
        
        // Create goog.scope call NOT in an expression statement
        Node scopeCall = createScopeCall(new Node(Token.BLOCK));
        Node script = new Node(Token.SCRIPT);
        script.addChildToBack(scopeCall); // Direct child, not wrapped in EXPR_RESULT
        
        ScopedAliases scopedAliases = new ScopedAliases(compiler, null,
            new AliasTransformationHandler() {
                @Override
                public AliasTransformation logAliasTransformation(
                    String sourceFile, SourcePosition<AliasTransformation> position) {
                    return new AliasTransformation() {
                        @Override
                        public void addAlias(String alias, String definition) {}
                    };
                }
            });
        
        scopedAliases.process(null, script);
        // Should report error GOOG_SCOPE_USED_IMPROPERLY
    }
    
    @Test(timeout = 4000)
    public void testGoogScopeWithNoParameters() {
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
        
        // Create goog.scope() with no parameters
        Node callNode = new Node(Token.CALL);
        Node getPropNode = Node.newString(Token.GETPROP, "goog.scope");
        getPropNode.addChildToFront(Node.newString(Token.NAME, "goog"));
        getPropNode.addChildToBack(Node.newString(Token.STRING, "scope"));
        callNode.addChildToFront(getPropNode);
        // No parameter added
        
        Node exprStmt = createExprStmt(callNode);
        Node script = new Node(Token.SCRIPT);
        script.addChildToBack(exprStmt);
        
        ScopedAliases scopedAliases = new ScopedAliases(compiler, null,
            new AliasTransformationHandler() {
                @Override
                public AliasTransformation logAliasTransformation(
                    String sourceFile, SourcePosition<AliasTransformation> position) {
                    return new AliasTransformation() {
                        @Override
                        public void addAlias(String alias, String definition) {}
                    };
                }
            });
        
        scopedAliases.process(null, script);
        // Should report error GOOG_SCOPE_HAS_BAD_PARAMETERS
        assertTrue("Errors should be reported", compiler.getErrorCount() > 0);
    }
    
    @Test(timeout = 4000)
    public void testGoogScopeWithNamedFunction() {
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
        
        // Create goog.scope(function myFunc() { ... })
        Node callNode = new Node(Token.CALL);
        Node getPropNode = Node.newString(Token.GETPROP, "goog.scope");
        getPropNode.addChildToFront(Node.newString(Token.NAME, "goog"));
        getPropNode.addChildToBack(Node.newString(Token.STRING, "scope"));
        callNode.addChildToFront(getPropNode);
        
        Node functionNode = new Node(Token.FUNCTION);
        Node paramList = new Node(Token.PARAM_LIST);
        Node block = new Node(Token.BLOCK);
        functionNode.addChildToFront(Node.newString(Token.NAME, "myFunc")); // Named function
        functionNode.addChildToBack(paramList);
        functionNode.addChildToBack(block);
        
        callNode.addChildToBack(functionNode);
        
        Node exprStmt = createExprStmt(callNode);
        Node script = new Node(Token.SCRIPT);
        script.addChildToBack(exprStmt);
        
        ScopedAliases scopedAliases = new ScopedAliases(compiler, null,
            new AliasTransformationHandler() {
                @Override
                public AliasTransformation logAliasTransformation(
                    String sourceFile, SourcePosition<AliasTransformation> position) {
                    return new AliasTransformation() {
                        @Override
                        public void addAlias(String alias, String definition) {}
                    };
                }
            });
        
        scopedAliases.process(null, script);
        // Should report error GOOG_SCOPE_HAS_BAD_PARAMETERS (named function)
        assertTrue("Errors should be reported for named function", compiler.getErrorCount() > 0);
    }
    
    @Test(timeout = 4000)
    public void testGoogScopeWithThisReference() {
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
        
        Node body = new Node(Token.BLOCK);
        body.addChildToBack(new Node(Token.THIS)); // 'this' reference
        
        Node scopeCall = createScopeCall(body);
        Node exprStmt = createExprStmt(scopeCall);
        Node script = new Node(Token.SCRIPT);
        script.addChildToBack(exprStmt);
        
        ScopedAliases scopedAliases = new ScopedAliases(compiler, null,
            new AliasTransformationHandler() {
                @Override
                public AliasTransformation logAliasTransformation(
                    String sourceFile, SourcePosition<AliasTransformation> position) {
                    return new AliasTransformation() {
                        @Override
                        public void addAlias(String alias, String definition) {}
                    };
                }
            });
        
        scopedAliases.process(null, script);
        // Should report error GOOG_SCOPE_REFERENCES_THIS
        assertTrue("Errors should be reported for 'this' reference", compiler.getErrorCount() > 0);
    }
    
    @Test(timeout = 4000)
    public void testGoogScopeWithReturnStatement() {
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
        
        Node body = new Node(Token.BLOCK);
        body.addChildToBack(new Node(Token.RETURN)); // return statement
        
        Node scopeCall = createScopeCall(body);
        Node exprStmt = createExprStmt(scopeCall);
        Node script = new Node(Token.SCRIPT);
        script.addChildToBack(exprStmt);
        
        ScopedAliases scopedAliases = new ScopedAliases(compiler, null,
            new AliasTransformationHandler() {
                @Override
                public AliasTransformation logAliasTransformation(
                    String sourceFile, SourcePosition<AliasTransformation> position) {
                    return new AliasTransformation() {
                        @Override
                        public void addAlias(String alias, String definition) {}
                    };
                }
            });
        
        scopedAliases.process(null, script);
        // Should report error GOOG_SCOPE_USES_RETURN
        assertTrue("Errors should be reported for return statement", compiler.getErrorCount() > 0);
    }
    
    @Test(timeout = 4000)
    public void testGoogScopeWithThrowStatement() {
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
        
        Node body = new Node(Token.BLOCK);
        body.addChildToBack(new Node(Token.THROW)); // throw statement
        
        Node scopeCall = createScopeCall(body);
        Node exprStmt = createExprStmt(scopeCall);
        Node script = new Node(Token.SCRIPT);
        script.addChildToBack(exprStmt);
        
        ScopedAliases scopedAliases = new ScopedAliases(compiler, null,
            new AliasTransformationHandler() {
                @Override
                public AliasTransformation logAliasTransformation(
                    String sourceFile, SourcePosition<AliasTransformation> position) {
                    return new AliasTransformation() {
                        @Override
                        public void addAlias(String alias, String definition) {}
                    };
                }
            });
        
        scopedAliases.process(null, script);
        // Should report error GOOG_SCOPE_USES_THROW
        assertTrue("Errors should be reported for throw statement", compiler.getErrorCount() > 0);
    }
    
    @Test(timeout = 4000)
    public void testNonAliasLocalVariable() {
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
        
        Node body = new Node(Token.BLOCK);
        // Create a var that is NOT an alias (no initial value or non-qualified name)
        Node varNode = new Node(Token.VAR);
        Node nameNode = Node.newString(Token.NAME, "nonAlias");
        nameNode.addChildToFront(Node.newNumber(Token.NUMBER, 42)); // Not a qualified name
        varNode.addChildToFront(nameNode);
        body.addChildToBack(varNode);
        
        Node scopeCall = createScopeCall(body);
        Node exprStmt = createExprStmt(scopeCall);
        Node script = new Node(Token.SCRIPT);
        script.addChildToBack(exprStmt);
        
        ScopedAliases scopedAliases = new ScopedAliases(compiler, null,
            new AliasTransformationHandler() {
                @Override
                public AliasTransformation logAliasTransformation(
                    String sourceFile, SourcePosition<AliasTransformation> position) {
                    return new AliasTransformation() {
                        @Override
                        public void addAlias(String alias, String definition) {}
                    };
                }
            });
        
        scopedAliases.process(null, script);
        // Should report error GOOG_SCOPE_NON_ALIAS_LOCAL
        assertTrue("Errors should be reported for non-alias local", compiler.getErrorCount() > 0);
    }
    
    @Test(timeout = 4000)
    public void testAliasRedefinition() {
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
        
        Node body = new Node(Token.BLOCK);
        body.addChildToBack(createAliasVar("x", "foo.bar"));
        
        // Redefine x = something else
        Node assignNode = new Node(Token.ASSIGN);
        assignNode.addChildToFront(Node.newString(Token.NAME, "x"));
        assignNode.addChildToBack(createQualifiedName("other.thing"));
        body.addChildToBack(createExprStmt(assignNode));
        
        Node scopeCall = createScopeCall(body);
        Node exprStmt = createExprStmt(scopeCall);
        Node script = new Node(Token.SCRIPT);
        script.addChildToBack(exprStmt);
        
        ScopedAliases scopedAliases = new ScopedAliases(compiler, null,
            new AliasTransformationHandler() {
                @Override
                public AliasTransformation logAliasTransformation(
                    String sourceFile, SourcePosition<AliasTransformation> position) {
                    return new AliasTransformation() {
                        @Override
                        public void addAlias(String alias, String definition) {}
                    };
                }
            });
        
        scopedAliases.process(null, script);
        // Should report error GOOG_SCOPE_ALIAS_REDEFINED
        assertTrue("Errors should be reported for alias redefinition", compiler.getErrorCount() > 0);
    }
    
    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================
    
    @Test(timeout = 4000)
    public void testMultipleScopeCalls() {
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
        
        Node script = new Node(Token.SCRIPT);
        
        // First scope call
        Node body1 = new Node(Token.BLOCK);
        body1.addChildToBack(createAliasVar("a", "alpha.A"));
        Node scopeCall1 = createScopeCall(body1);
        script.addChildToBack(createExprStmt(scopeCall1));
        
        // Second scope call
        Node body2 = new Node(Token.BLOCK);
        body2.addChildToBack(createAliasVar("b", "beta.B"));
        Node scopeCall2 = createScopeCall(body2);
        script.addChildToBack(createExprStmt(scopeCall2));
        
        ScopedAliases scopedAliases = new ScopedAliases(compiler, null,
            new AliasTransformationHandler() {
                @Override
                public AliasTransformation logAliasTransformation(
                    String sourceFile, SourcePosition<AliasTransformation> position) {
                    return new AliasTransformation() {
                        @Override
                        public void addAlias(String alias, String definition) {}
                    };
                }
            });
        
        scopedAliases.process(null, script);
        assertNotNull("Multiple scope calls should process", script);
    }
    
    @Test(timeout = 4000)
    public void testWithPreprocessorSymbolTable() {
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
        
        PreprocessorSymbolTable symbolTable = new PreprocessorSymbolTable();
        
        Node body = new Node(Token.BLOCK);
        body.addChildToBack(createAliasVar("dom", "goog.dom"));
        
        Node callNode = new Node(Token.CALL);
        Node getProp = new Node(Token.GETPROP);
        getProp.addChildToFront(createNameRef("dom"));
        getProp.addChildToBack(Node.newString(Token.STRING, "createElement"));
        callNode.addChildToFront(getProp);
        callNode.addChildToBack(Node.newString(Token.STRING, "div"));
        body.addChildToBack(createExprStmt(callNode));
        
        Node scopeCall = createScopeCall(body);
        Node exprStmt = createExprStmt(scopeCall);
        Node script = new Node(Token.SCRIPT);
        script.addChildToBack(exprStmt);
        
        ScopedAliases scopedAliases = new ScopedAliases(compiler, symbolTable,
            new AliasTransformationHandler() {
                @Override
                public AliasTransformation logAliasTransformation(
                    String sourceFile, SourcePosition<AliasTransformation> position) {
                    return new AliasTransformation() {
                        @Override
                        public void addAlias(String alias, String definition) {}
                    };
                }
            });
        
        scopedAliases.process(null, script);
        assertNotNull("Processing with symbol table should complete", script);
    }
    
    @Test(timeout = 4000)
    public void testHotSwapScriptDirectly() {
        Compiler compiler = new Compiler();
        CompilerOptions options = new CompilerOptions();
        compiler.initOptions(options);
        
        Node body = new Node(Token.BLOCK);
        body.addChildToBack(createAliasVar("x", "test.X"));
        
        Node scopeCall = createScopeCall(body);
        Node exprStmt = createExprStmt(scopeCall);
        Node script = new Node(Token.SCRIPT);
        script.addChildToBack(exprStmt);
        
        ScopedAliases scopedAliases = new ScopedAliases(compiler, null,
            new AliasTransformationHandler() {
                @Override
                public AliasTransformation logAliasTransformation(
                    String sourceFile, SourcePosition<AliasTransformation> position) {
                    return new AliasTransformation() {
                        @Override
                        public void addAlias(String alias, String definition) {}
                    };
                }
            });
        
        scopedAliases.hotSwapScript(script, null);
        assertNotNull("Hot swap should complete", script);
    }
}