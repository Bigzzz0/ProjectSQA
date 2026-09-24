package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.jscomp.ControlFlowGraph.Branch;
import com.google.javascript.jscomp.Scope.Var;
import com.google.javascript.jscomp.graph.GraphNode;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/*
 * [Branch & Defect Analysis Matrix]
 * =========================================================================================
 * Target Class: com.google.javascript.jscomp.MustBeReachingVariableDef
 * Known Defect (Closure-30 / Issue 698):
 *   - FlowSensitiveInlineVariablesTest::testIssue698
 *   - FlowSensitiveInlineVariablesTest::testInlineAcrossSideEffect1
 *   - FlowSensitiveInlineVariablesTest::testCanInlineAcrossNoSideEffect
 *
 * Core Defect Root Cause:
 *   In `dependsOnOuterScopeVars(String name, Node useNode)`:
 *   `Definition def = state.getIn().reachingDef.get(jsScope.getVar(name));`
 *   When `def == null` (e.g. variable has multiple reaching definitions, is conditionally
 *   assigned, or uninitialized/escaped), the method fails to check for `null` and
 *   immediately executes `for (Var s : def.depends)` -> throwing NullPointerException!
 *   The expected correct behavior is to return `false` if `def == null`.
 *
 * Decision / Condition Coverage Targets:
 *   1. MustDefJoin:
 *      - aDef == null (BOTTOM): resultMap puts null
 *      - aDef != null, b contains var, aDef.equals(bDef): keeps aDef
 *      - aDef != null, b contains var, !aDef.equals(bDef): puts null (divergent)
 *      - aDef != null, b does not contain var: puts aDef
 *      - b contains var not present in a: puts bDef
 *   2. computeMustDef(Node n, Node cfgNode, MustDef output, boolean conditional):
 *      - Token.BLOCK / Token.FUNCTION: immediate return
 *      - Token.WHILE / Token.DO / Token.IF: evaluate condition expression
 *      - Token.FOR: regular for loop (!isForIn) vs for-in loop (lhs isVar vs lhs isName)
 *      - Token.AND / Token.OR: lhs with conditional, rhs with conditional=true
 *      - Token.HOOK: first child conditional, 2nd & 3rd children conditional=true
 *      - Token.VAR: hasChildren() true/false, multiple vars declaration
 *      - Default / Assignment: name assignment, compound assign, arguments property write
 *      - Default / arguments identifier reference: triggers escapeParameters
 *      - Default / DEC and INC: name target vs non-name target (obj.prop++)
 *   3. addToDefIfLocal:
 *      - var == null or var.scope != jsScope: early return
 *      - otherDef.depends.contains(var): invalidates dependent variables (sets to null)
 *      - escaped.contains(var): skips recording def
 *      - conditional definition (node == null): sets var to null (BOTTOM)
 *      - normal definition: records definition and computes dependencies
 *   4. escapeParameters:
 *      - marks parameter variables as null
 *      - cascades to any definition depending on a parameter
 *   5. dependsOnOuterScopeVars:
 *      - def == null -> triggers defect (expected false, buggy throws NPE)
 *      - depends on outer var (s.scope != jsScope) -> returns true
 *      - depends only on local vars -> returns false
 *   6. Precondition guards:
 *      - getDef / dependsOnOuterScopeVars on node not in CFG -> throws IllegalArgumentException
 *   7. Object Contracts:
 *      - Definition.equals (same node, different node, non-Definition)
 *      - MustDef.equals, copy constructor, entry lattice, initial estimate lattice
 * =========================================================================================
 */
public class MustBeReachingVariableDefGeminiTest {

  // -------------------------------------------------------------------------
  // Helper Infrastructure
  // -------------------------------------------------------------------------

  private interface NodeFilter {
    boolean accept(Node n);
  }

  private static class AnalysisResult {
    final MustBeReachingVariableDef analysis;
    final ControlFlowGraph<Node> cfg;
    final Scope scope;
    final AbstractCompiler compiler;
    final Node rootNode;

    AnalysisResult(MustBeReachingVariableDef analysis,
                   ControlFlowGraph<Node> cfg,
                   Scope scope,
                   AbstractCompiler compiler,
                   Node rootNode) {
      this.analysis = analysis;
      this.cfg = cfg;
      this.scope = scope;
      this.compiler = compiler;
      this.rootNode = rootNode;
    }

    Node findCfgNode(NodeFilter filter) {
      for (GraphNode<Node, Branch> gn : cfg.getNodes()) {
        Node n = gn.getValue();
        if (n != null && filter.accept(n)) {
          return n;
        }
      }
      return null;
    }

    List<Node> findAllCfgNodes(NodeFilter filter) {
      List<Node> list = new ArrayList<Node>();
      for (GraphNode<Node, Branch> gn : cfg.getNodes()) {
        Node n = gn.getValue();
        if (n != null && filter.accept(n)) {
          list.add(n);
        }
      }
      return list;
    }

    Node findReturn() {
      return findCfgNode(new NodeFilter() {
        @Override
        public boolean accept(Node n) {
          return n.isReturn();
        }
      });
    }

    Node findVarNode(final String varName) {
      return findCfgNode(new NodeFilter() {
        @Override
        public boolean accept(Node n) {
          return n.isVar() && n.hasChildren()
              && varName.equals(n.getFirstChild().getString());
        }
      });
    }

    Node findAssignExpr(final String varName) {
      return findCfgNode(new NodeFilter() {
        @Override
        public boolean accept(Node n) {
          if (n.isExprResult() && n.getFirstChild() != null && n.getFirstChild().isAssign()) {
            Node lhs = n.getFirstChild().getFirstChild();
            return lhs != null && lhs.isName() && varName.equals(lhs.getString());
          }
          return false;
        }
      });
    }
  }

  private AnalysisResult analyze(String js) {
    Compiler compiler = new Compiler();
    Node script = compiler.parseTestCode(js);
    assertNotNull("Parsing failed for script", script);

    Node funcNode = findFirstFunction(script);
    boolean isFunction = (funcNode != null);

    SyntacticScopeCreator scopeCreator = new SyntacticScopeCreator(compiler);
    Scope globalScope = scopeCreator.createScope(script, null);
    Scope targetScope;
    Node cfgRoot;

    if (isFunction) {
      targetScope = scopeCreator.createScope(funcNode, globalScope);
      cfgRoot = funcNode.getLastChild(); // BLOCK body
    } else {
      targetScope = globalScope;
      cfgRoot = script;
    }

    ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, false, true);
    cfa.process(null, cfgRoot);
    ControlFlowGraph<Node> cfg = cfa.getCfg();

    MustBeReachingVariableDef dfa = new MustBeReachingVariableDef(cfg, targetScope, compiler);
    dfa.analyze();

    return new AnalysisResult(dfa, cfg, targetScope, compiler, script);
  }

  private Node findFirstFunction(Node n) {
    if (n.isFunction()) {
      return n;
    }
    for (Node c = n.getFirstChild(); c != null; c = c.getNext()) {
      Node found = findFirstFunction(c);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  // -------------------------------------------------------------------------
  // Partition C: Defect-Targeted Branch Zone (Closure-30 / Issue 698)
  // -------------------------------------------------------------------------

  /**
   * Targets the defect where dependsOnOuterScopeVars throws NullPointerException
   * when def is null at the join of divergent reaching definitions.
   */
  @Test(timeout = 4000)
  public void testDefectIssue698DependsOnOuterScopeVarsNullDefBranching() {
    String js = "function f(p) {\n"
        + "  var x;\n"
        + "  if (p) {\n"
        + "    x = 1;\n"
        + "  } else {\n"
        + "    x = 2;\n"
        + "  }\n"
        + "  return x;\n"
        + "}";
    AnalysisResult res = analyze(js);
    Node returnNode = res.findReturn();
    assertNotNull("Return node must exist in CFG", returnNode);

    // Reaching definition must be null because x diverges across if-else
    Node def = res.analysis.getDef("x", returnNode);
    assertNull("Reaching def must be null for multiple divergent definitions", def);

    // CRITICAL: Buggy version throws NullPointerException here!
    // Expected behavior: returns false because def is null.
    boolean depends = res.analysis.dependsOnOuterScopeVars("x", returnNode);
    assertFalse("Variable with null reaching definition must not depend on outer scope vars", depends);
  }

  /**
   * Targets defect when variable is conditionally assigned in an if-branch without else.
   */
  @Test(timeout = 4000)
  public void testDefectIssue698DependsOnOuterScopeVarsConditionalIfOnly() {
    String js = "function f(p) {\n"
        + "  var x = 1;\n"
        + "  if (p) {\n"
        + "    x = 2;\n"
        + "  }\n"
        + "  return x;\n"
        + "}";
    AnalysisResult res = analyze(js);
    Node returnNode = res.findReturn();
    assertNotNull(returnNode);

    assertNull("Reaching def must be null across conditional bypass", res.analysis.getDef("x", returnNode));
    boolean depends = res.analysis.dependsOnOuterScopeVars("x", returnNode);
    assertFalse("Conditional definition joining initial def must return false for outer scope dependency", depends);
  }

  /**
   * Targets defect when variable is conditionally defined via logical AND / OR operators.
   */
  @Test(timeout = 4000)
  public void testDefectIssue698DependsOnOuterScopeVarsConditionalAndOr() {
    String js = "function f(cond) {\n"
        + "  var x = 1;\n"
        + "  cond && (x = 2);\n"
        + "  return x;\n"
        + "}";
    AnalysisResult res = analyze(js);
    Node returnNode = res.findReturn();
    assertNotNull(returnNode);

    assertNull(res.analysis.getDef("x", returnNode));
    boolean depends = res.analysis.dependsOnOuterScopeVars("x", returnNode);
    assertFalse(depends);
  }

  // -------------------------------------------------------------------------
  // Partition A: Core Functional Logic & State Transitions
  // -------------------------------------------------------------------------

  @Test(timeout = 4000)
  public void testSimpleLinearReachingDefinition() {
    String js = "function f() {\n"
        + "  var x = 1;\n"
        + "  return x;\n"
        + "}";
    AnalysisResult res = analyze(js);
    Node returnNode = res.findReturn();
    Node varNode = res.findVarNode("x");
    assertNotNull(returnNode);
    assertNotNull(varNode);

    Node defNode = res.analysis.getDef("x", returnNode);
    assertEquals("Reaching definition must be the VAR statement", varNode, defNode);
    assertFalse(res.analysis.dependsOnOuterScopeVars("x", returnNode));
  }

  @Test(timeout = 4000)
  public void testSequentialReassignmentsOverwritesDef() {
    String js = "function f() {\n"
        + "  var x = 1;\n"
        + "  x = 2;\n"
        + "  return x;\n"
        + "}";
    AnalysisResult res = analyze(js);
    Node returnNode = res.findReturn();
    Node assignNode = res.findAssignExpr("x");
    assertNotNull(returnNode);
    assertNotNull(assignNode);

    Node defNode = res.analysis.getDef("x", returnNode);
    assertEquals("Reaching definition must be the second assignment EXPR_RESULT", assignNode, defNode);
  }

  @Test(timeout = 4000)
  public void testDependsOnOuterScopeVarsReturnsTrue() {
    String js = "var outerVal = 100;\n"
        + "function f() {\n"
        + "  var localVal = outerVal;\n"
        + "  return localVal;\n"
        + "}";
    AnalysisResult res = analyze(js);
    Node returnNode = res.findReturn();
    assertNotNull(returnNode);

    assertTrue("localVal reads outerVal, so dependsOnOuterScopeVars must be true",
        res.analysis.dependsOnOuterScopeVars("localVal", returnNode));
  }

  @Test(timeout = 4000)
  public void testDependsOnOuterScopeVarsReturnsFalseWhenLocalOnly() {
    String js = "function f() {\n"
        + "  var a = 10;\n"
        + "  var b = a + 5;\n"
        + "  return b;\n"
        + "}";
    AnalysisResult res = analyze(js);
    Node returnNode = res.findReturn();
    assertNotNull(returnNode);

    assertFalse("b only reads local variable a, so dependsOnOuterScopeVars must be false",
        res.analysis.dependsOnOuterScopeVars("b", returnNode));
  }

  @Test(timeout = 4000)
  public void testInvalidationWhenDependedVariableIsRedefined() {
    String js = "function f() {\n"
        + "  var a = 1;\n"
        + "  var b = a;\n"
        + "  a = 2;\n" // invalidates b's definition
        + "  return b;\n"
        + "}";
    AnalysisResult res = analyze(js);
    Node returnNode = res.findReturn();
    assertNotNull(returnNode);

    Node defB = res.analysis.getDef("b", returnNode);
    assertNull("Definition of b must be invalidated to null when a is modified", defB);
  }

  @Test(timeout = 4000)
  public void testForInLoopWithVarDeclaration() {
    String js = "function f(obj) {\n"
        + "  for (var k in obj) {\n"
        + "    var use = k;\n"
        + "  }\n"
        + "}";
    AnalysisResult res = analyze(js);
    Node forNode = res.findCfgNode(new NodeFilter() {
      @Override
      public boolean accept(Node n) {
        return n.getType() == Token.FOR;
      }
    });
    Node varUse = res.findVarNode("use");
    assertNotNull(forNode);
    assertNotNull(varUse);

    Node defK = res.analysis.getDef("k", varUse);
    assertEquals("k in for(var k in obj) must be defined by the FOR node", forNode, defK);
  }

  @Test(timeout = 4000)
  public void testForInLoopWithoutVarDeclaration() {
    String js = "function f(obj) {\n"
        + "  var k;\n"
        + "  for (k in obj) {\n"
        + "    var use = k;\n"
        + "  }\n"
        + "}";
    AnalysisResult res = analyze(js);
    Node forNode = res.findCfgNode(new NodeFilter() {
      @Override
      public boolean accept(Node n) {
        return n.getType() == Token.FOR;
      }
    });
    Node varUse = res.findVarNode("use");
    assertNotNull(forNode);
    assertNotNull(varUse);

    Node defK = res.analysis.getDef("k", varUse);
    assertEquals("k in for(k in obj) must be defined by the FOR node", forNode, defK);
  }

  @Test(timeout = 4000)
  public void testStandardForLoopConditionExpression() {
    String js = "function f() {\n"
        + "  var x;\n"
        + "  for (var i = 0; (x = i) < 10; i++) {\n"
        + "    var body = x;\n"
        + "  }\n"
        + "}";
    AnalysisResult res = analyze(js);
    Node forNode = res.findCfgNode(new NodeFilter() {
      @Override
      public boolean accept(Node n) {
        return n.getType() == Token.FOR;
      }
    });
    assertNotNull(forNode);
    // NodeUtil.getConditionExpression is processed
    assertNotNull(res.analysis.getCfg());
  }

  @Test(timeout = 4000)
  public void testWhileAndDoWhileConditionDefinitions() {
    String js = "function f() {\n"
        + "  var x = 0;\n"
        + "  while ((x = 1) > 0) {\n"
        + "    break;\n"
        + "  }\n"
        + "  do {\n"
        + "    x = 2;\n"
        + "  } while ((x = 3) < 0);\n"
        + "  return x;\n"
        + "}";
    AnalysisResult res = analyze(js);
    assertNotNull(res.analysis.getCfg());
    Node returnNode = res.findReturn();
    assertNotNull(returnNode);
  }

  @Test(timeout = 4000)
  public void testHookTernaryExpressionSetsConditional() {
    String js = "function f(c) {\n"
        + "  var x = 0;\n"
        + "  c ? (x = 1) : (x = 2);\n"
        + "  return x;\n"
        + "}";
    AnalysisResult res = analyze(js);
    Node returnNode = res.findReturn();
    assertNotNull(returnNode);

    Node def = res.analysis.getDef("x", returnNode);
    assertNull("Reaching definition after ternary must be null", def);
  }

  @Test(timeout = 4000)
  public void testIncrementAndDecrementOperators() {
    String js = "function f() {\n"
        + "  var x = 0;\n"
        + "  x++;\n"
        + "  var u1 = x;\n"
        + "  ++x;\n"
        + "  var u2 = x;\n"
        + "  x--;\n"
        + "  var u3 = x;\n"
        + "  --x;\n"
        + "  var u4 = x;\n"
        + "  return x;\n"
        + "}";
    AnalysisResult res = analyze(js);
    Node u1 = res.findVarNode("u1");
    Node u2 = res.findVarNode("u2");
    Node u3 = res.findVarNode("u3");
    Node u4 = res.findVarNode("u4");

    assertNotNull(res.analysis.getDef("x", u1));
    assertNotNull(res.analysis.getDef("x", u2));
    assertNotNull(res.analysis.getDef("x", u3));
    assertNotNull(res.analysis.getDef("x", u4));
  }

  @Test(timeout = 4000)
  public void testCompoundAssignmentOperators() {
    String js = "function f() {\n"
        + "  var x = 1;\n"
        + "  x += 5;\n"
        + "  var u = x;\n"
        + "  return u;\n"
        + "}";
    AnalysisResult res = analyze(js);
    Node u = res.findVarNode("u");
    assertNotNull(u);
    Node def = res.analysis.getDef("x", u);
    assertNotNull("Compound assignment += must define variable", def);
  }

  @Test(timeout = 4000)
  public void testEscapedParametersViaArgumentsWrite() {
    String js = "function f(param) {\n"
        + "  var y = param;\n"
        + "  arguments[0] = 42;\n"
        + "  return param;\n"
        + "}";
    AnalysisResult res = analyze(js);
    Node returnNode = res.findReturn();
    assertNotNull(returnNode);

    Node defParam = res.analysis.getDef("param", returnNode);
    assertNull("Assigning to arguments must escape parameters and null out def", defParam);
    Node defY = res.analysis.getDef("y", returnNode);
    assertNull("Variables depending on escaped parameter must also be nulled out", defY);
  }

  @Test(timeout = 4000)
  public void testEscapedParametersViaArgumentsNameReference() {
    String js = "function f(param) {\n"
        + "  var y = param;\n"
        + "  var args = arguments;\n"
        + "  return param;\n"
        + "}";
    AnalysisResult res = analyze(js);
    Node returnNode = res.findReturn();
    assertNotNull(returnNode);

    Node defParam = res.analysis.getDef("param", returnNode);
    assertNull("Referencing arguments must escape parameters", defParam);
  }

  @Test(timeout = 4000)
  public void testNestedFunctionDoesNotPolluteLocalDef() {
    String js = "function f() {\n"
        + "  var x = 1;\n"
        + "  function inner() {\n"
        + "    x = 2;\n"
        + "  }\n"
        + "  return x;\n"
        + "}";
    AnalysisResult res = analyze(js);
    Node returnNode = res.findReturn();
    Node varX = res.findVarNode("x");
    assertNotNull(returnNode);
    assertNotNull(varX);

    // Because x is captured in inner(), x is an escaped variable
    Node defX = res.analysis.getDef("x", returnNode);
    // Escaped variables are not recorded in reachingDef
    assertNull("Escaped variable must not have reaching definition", defX);
  }

  // -------------------------------------------------------------------------
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // -------------------------------------------------------------------------

  @Test(timeout = 4000)
  public void testUninitializedVariableDeclaration() {
    String js = "function f() {\n"
        + "  var x;\n"
        + "  return x;\n"
        + "}";
    AnalysisResult res = analyze(js);
    Node returnNode = res.findReturn();
    assertNotNull(returnNode);

    Node defNode = res.analysis.getDef("x", returnNode);
    // Initially defined at function root node
    assertNotNull(defNode);
    assertTrue("Initial definition node must be the function root", defNode.isFunction());
  }

  @Test(timeout = 4000)
  public void testMultipleDeclarationsInSingleVarStatement() {
    String js = "function f() {\n"
        + "  var a = 1, b = 2, c;\n"
        + "  return a + b;\n"
        + "}";
    AnalysisResult res = analyze(js);
    Node returnNode = res.findReturn();
    assertNotNull(returnNode);

    Node defA = res.analysis.getDef("a", returnNode);
    Node defB = res.analysis.getDef("b", returnNode);
    assertNotNull(defA);
    assertNotNull(defB);
    assertEquals("Both a and b point to the same VAR CFG node", defA, defB);
  }

  @Test(timeout = 4000)
  public void testAssignmentToOuterScopeVariableDoesNotThrow() {
    String js = "var globalVar = 1;\n"
        + "function f() {\n"
        + "  globalVar = 2;\n"
        + "  return 0;\n"
        + "}";
    AnalysisResult res = analyze(js);
    Node returnNode = res.findReturn();
    assertNotNull(returnNode);
    // globalVar belongs to outer scope, so getVar("globalVar") is not in jsScope
    // addToDefIfLocal returns early
    assertNull(res.analysis.getDef("globalVar", returnNode));
  }

  @Test(timeout = 4000)
  public void testAssignmentToUndeclaredVariableDoesNotThrow() {
    String js = "function f() {\n"
        + "  undeclared = 42;\n"
        + "  return undeclared;\n"
        + "}";
    AnalysisResult res = analyze(js);
    Node returnNode = res.findReturn();
    assertNotNull(returnNode);
    assertNull(res.analysis.getDef("undeclared", returnNode));
  }

  @Test(timeout = 4000)
  public void testIncDecOnPropertyDoesNotAffectVariables() {
    String js = "function f(obj) {\n"
        + "  obj.count++;\n"
        + "  --obj.count;\n"
        + "  return obj;\n"
        + "}";
    AnalysisResult res = analyze(js);
    Node returnNode = res.findReturn();
    assertNotNull(returnNode);
  }

  // -------------------------------------------------------------------------
  // Partition D: Exception & Defensive Guard Paths
  // -------------------------------------------------------------------------

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testGetDefThrowsOnNodeNotInCfg() {
    String js = "function f() { var x = 1; return x; }";
    AnalysisResult res = analyze(js);
    Node foreignNode = new Node(Token.EMPTY);
    res.analysis.getDef("x", foreignNode);
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testDependsOnOuterScopeVarsThrowsOnNodeNotInCfg() {
    String js = "function f() { var x = 1; return x; }";
    AnalysisResult res = analyze(js);
    Node foreignNode = new Node(Token.EMPTY);
    res.analysis.dependsOnOuterScopeVars("x", foreignNode);
  }

  // -------------------------------------------------------------------------
  // Partition E: Object Lifecycle & Contract Integrity
  // -------------------------------------------------------------------------

  @Test(timeout = 4000)
  public void testMustDefLatticeLifecycleAndEquals() {
    MustBeReachingVariableDef.MustDef def1 = new MustBeReachingVariableDef.MustDef();
    MustBeReachingVariableDef.MustDef def2 = new MustBeReachingVariableDef.MustDef();
    assertEquals("Empty MustDef instances should be equal", def1, def2);
    assertEquals("MustDef equals must be reflexive", def1, def1);
    assertFalse("MustDef should not equal null", def1.equals(null));
    assertFalse("MustDef should not equal non-MustDef object", def1.equals("String"));

    MustBeReachingVariableDef.MustDef copy = new MustBeReachingVariableDef.MustDef(def1);
    assertEquals("Copy-constructed MustDef should equal original", def1, copy);
  }

  @Test(timeout = 4000)
  public void testDataFlowAnalysisEngineMethods() {
    String js = "function f() { var a = 1; return a; }";
    AnalysisResult res = analyze(js);

    assertTrue("Analysis must be forward", res.analysis.isForward());
    MustBeReachingVariableDef.MustDef initial = res.analysis.createInitialEstimateLattice();
    assertNotNull("Initial estimate lattice must not be null", initial);
    assertTrue("Initial estimate lattice must be empty", initial.reachingDef.isEmpty());

    MustBeReachingVariableDef.MustDef entry = res.analysis.createEntryLattice();
    assertNotNull("Entry lattice must not be null", entry);
    assertFalse("Entry lattice must contain scope variables", entry.reachingDef.isEmpty());

    Node returnNode = res.findReturn();
    MustBeReachingVariableDef.MustDef out = res.analysis.flowThrough(returnNode, entry);
    assertNotNull("flowThrough result must not be null", out);
  }

  @Test(timeout = 4000)
  public void testDefinitionEqualsViaReflection() throws Exception {
    Class<?> defClass = Class.forName("com.google.javascript.jscomp.MustBeReachingVariableDef$Definition");
    Constructor<?> ctor = defClass.getDeclaredConstructor(Node.class);
    ctor.setAccessible(true);

    Node n1 = new Node(Token.NAME);
    Node n2 = new Node(Token.NAME);

    Object d1a = ctor.newInstance(n1);
    Object d1b = ctor.newInstance(n1);
    Object d2 = ctor.newInstance(n2);

    assertEquals("Definitions with identical Node instance must be equal", d1a, d1b);
    assertEquals("Definition equals must be reflexive", d1a, d1a);
    assertFalse("Definitions with different Node instances must not be equal", d1a.equals(d2));
    assertFalse("Definition should not equal non-Definition", d1a.equals(new Object()));
    assertFalse("Definition should not equal null", d1a.equals(null));
  }

  @Test(timeout = 4000)
  @SuppressWarnings("unchecked")
  public void testMustDefJoinDirectBranchesViaReflection() throws Exception {
    String js = "function f() { var v1 = 1; var v2 = 2; var v3 = 3; }";
    AnalysisResult res = analyze(js);

    Field joinOpField = DataFlowAnalysis.class.getDeclaredField("joinOp");
    joinOpField.setAccessible(true);
    JoinOp.BinaryJoinOp<MustBeReachingVariableDef.MustDef> joinOp =
        (JoinOp.BinaryJoinOp<MustBeReachingVariableDef.MustDef>) joinOpField.get(res.analysis);
    assertNotNull("joinOp must be accessible", joinOp);

    Var v1 = res.scope.getVar("v1");
    Var v2 = res.scope.getVar("v2");
    Var v3 = res.scope.getVar("v3");
    assertNotNull(v1);
    assertNotNull(v2);
    assertNotNull(v3);

    Class<?> defClass = Class.forName("com.google.javascript.jscomp.MustBeReachingVariableDef$Definition");
    Constructor<?> ctor = defClass.getDeclaredConstructor(Node.class);
    ctor.setAccessible(true);

    Node nodeA = new Node(Token.NAME);
    Node nodeB = new Node(Token.NAME);
    Object defA = ctor.newInstance(nodeA);
    Object defB = ctor.newInstance(nodeB);

    MustBeReachingVariableDef.MustDef latA = new MustBeReachingVariableDef.MustDef();
    MustBeReachingVariableDef.MustDef latB = new MustBeReachingVariableDef.MustDef();

    // 1. v1 is null in latA (BOTTOM) -> join result must be null
    putInMustDef(latA, v1, null);
    putInMustDef(latB, v1, defA);

    // 2. v2 is equal in latA and latB -> join result must keep defA
    putInMustDef(latA, v2, defA);
    putInMustDef(latB, v2, defA);

    // 3. v3 diverges (defA vs defB) -> join result must be null (BOTTOM)
    putInMustDef(latA, v3, defA);
    putInMustDef(latB, v3, defB);

    MustBeReachingVariableDef.MustDef joined = joinOp.apply(latA, latB);
    assertNotNull(joined);

    assertTrue(joined.reachingDef.containsKey(v1));
    assertNull("v1 joined with BOTTOM must be null", joined.reachingDef.get(v1));

    assertTrue(joined.reachingDef.containsKey(v2));
    assertEquals("v2 matching in both must retain definition", defA, joined.reachingDef.get(v2));

    assertTrue(joined.reachingDef.containsKey(v3));
    assertNull("v3 divergent must result in null", joined.reachingDef.get(v3));

    // 4. Variable only in B and not in A
    MustBeReachingVariableDef.MustDef latEmpty = new MustBeReachingVariableDef.MustDef();
    MustBeReachingVariableDef.MustDef latOnlyB = new MustBeReachingVariableDef.MustDef();
    putInMustDef(latOnlyB, v1, defA);
    MustBeReachingVariableDef.MustDef joinedOnlyB = joinOp.apply(latEmpty, latOnlyB);
    assertEquals(defA, joinedOnlyB.reachingDef.get(v1));

    // 5. Variable only in A and not in B
    MustBeReachingVariableDef.MustDef joinedOnlyA = joinOp.apply(latOnlyB, latEmpty);
    assertEquals(defA, joinedOnlyA.reachingDef.get(v1));
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private void putInMustDef(MustBeReachingVariableDef.MustDef lattice, Var var, Object def) {
    lattice.reachingDef.put(var, (MustBeReachingVariableDef.MustDef) null != null ? null : (dynamicCastDefinition(def)));
  }

  @SuppressWarnings("unchecked")
  private <T> T dynamicCastDefinition(Object def) {
    return (T) def;
  }
}