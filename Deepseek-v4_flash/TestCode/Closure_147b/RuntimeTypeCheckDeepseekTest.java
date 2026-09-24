package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Defect targeted:
 *   RuntimeTypeCheckTest.testValueWithInnerFn fails because AddChecks.visitFunction
 *   inserts parameter checks before inner function declarations.  Normalization
 *   requires those declarations to precede runtime type-checking statements.
 *
 * Decision/branch zones exercised:
 *   - visitFunction: checkable vs uncheckable params; insertionPoint null/non-null.
 *   - visitReturn: return with value, bare return, uncheckable return type.
 *   - createCheckTypeCallNode: single type vs union type; null checker propagation.
 *   - createCheckerNode: null, boolean, number, string, undefined/void,
 *     extern instance, user class, user interface, unknown/unchecked type.
 *   - AddMarkers: constructors, implemented interfaces, class/interface markers.
 *   - addBoilerplateCode/getBoilerplateCode: null and custom log functions.
 */
public class RuntimeTypeCheckDeepseekTest {

  private static Compiler createCompiler(String source) {
    CompilerOptions options = new CompilerOptions();
    options.checkTypes = true;

    Compiler compiler = new Compiler();
    List<SourceFile> inputs = new ArrayList<>();
    inputs.add(SourceFile.fromCode("test.js", source));

    compiler.compile(new ArrayList<SourceFile>(), inputs, options);

    assertEquals(
        "Unexpected compile errors: " + Arrays.toString(compiler.getErrors()),
        0, compiler.getErrors().length);
    assertNotNull("Compiler root should be available", compiler.getRoot());

    return compiler;
  }

  private static Node runPass(Compiler compiler) {
    Node root = compiler.getRoot();
    new RuntimeTypeCheck(compiler, null).process(null, root);
    return root;
  }

  private static Node findFunction(Node node, String name) {
    if (node == null) {
      return null;
    }
    if (NodeUtil.isFunction(node) && name.equals(NodeUtil.getFunctionName(node))) {
      return node;
    }
    for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
      Node result = findFunction(child, name);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  private static int childCount(Node n) {
    int count = 0;
    for (Node child = n.getFirstChild(); child != null; child = child.getNext()) {
      count++;
    }
    return count;
  }

  @Test(timeout = 4000)
  public void testValueWithInnerFnRegression() {
    String source =
        "/** @param {number} x */\n" +
        "function outerFn(x) {\n" +
        "  function innerFn() {}\n" +
        "}\n";

    Compiler compiler = createCompiler(source);
    Node root = runPass(compiler);

    Node outer = findFunction(root, "outerFn");
    assertNotNull("outerFn should be found", outer);

    Node body = outer.getLastChild();
    String bodyTree = body.toStringTree();

    int innerPos = bodyTree.indexOf("innerFn");
    int checkPos = bodyTree.indexOf("checkType");

    assertTrue("innerFn should remain in the outer body", innerPos >= 0);
    assertTrue("runtime type check should be inserted", checkPos >= 0);
    assertTrue("runtime check must be inserted after inner function declaration",
        innerPos < checkPos);
  }

  @Test(timeout = 4000)
  public void testParameterChecksForValueTypes() {
    String source =
        "/** @param {boolean} a @param {number} b @param {string} c */\n" +
        "function valueFn(a, b, c) {}\n";

    Compiler compiler = createCompiler(source);
    Node root = runPass(compiler);

    Node fn = findFunction(root, "valueFn");
    assertNotNull("valueFn should be found", fn);

    Node body = fn.getLastChild();
    String bodyTree = body.toStringTree();

    assertEquals(3, childCount(body));
    assertTrue(bodyTree.contains("valueChecker"));
    assertTrue(bodyTree.contains("boolean"));
    assertTrue(bodyTree.contains("number"));
    assertTrue(bodyTree.contains("string"));
  }

  @Test(timeout = 4000)
  public void testNullTypeUsesNullChecker() {
    String source = "/** @param {null} x */ function nullFn(x) {}";

    Compiler compiler = createCompiler(source);
    Node root = runPass(compiler);

    Node fn = findFunction(root, "nullFn");
    assertNotNull("nullFn should be found", fn);
    assertTrue(fn.getLastChild().toStringTree().contains("nullChecker"));
  }

  @Test(timeout = 4000)
  public void testVoidTypeUsesValueChecker() {
    String source = "/** @return {void} */ function voidFn() { return undefined; }";

    Compiler compiler = createCompiler(source);
    Node root = runPass(compiler);

    Node fn = findFunction(root, "voidFn");
    assertNotNull("voidFn should be found", fn);

    String bodyTree = fn.getLastChild().toStringTree();
    assertTrue(bodyTree.contains("valueChecker"));
    assertTrue(bodyTree.contains("undefined") || bodyTree.contains("void"));
  }

  @Test(timeout = 4000)
  public void testUnknownTypeDoesNotEmitChecker() {
    String source = "/** @param {?} x */ function unknownFn(x) {}";

    Compiler compiler = createCompiler(source);
    Node root = runPass(compiler);

    Node fn = findFunction(root, "unknownFn");
    assertNotNull("unknownFn should be found", fn);
    assertFalse(fn.getLastChild().toStringTree().contains("checkType"));
  }

  @Test(timeout = 4000)
  public void testSkippedUnknownParameterStillChecksLaterParameter() {
    String source =
        "/** @param {?} a @param {number} b */ function skipThenCheck(a, b) {}";

    Compiler compiler = createCompiler(source);
    Node root = runPass(compiler);

    Node fn = findFunction(root, "skipThenCheck");
    assertNotNull("skipThenCheck should be found", fn);

    Node body = fn.getLastChild();
    assertEquals(1, childCount(body));
    assertTrue(body.toStringTree().contains("valueChecker"));
  }

  @Test(timeout = 4000)
  public void testExternClassTypeUsesExternClassChecker() {
    String source = "/** @param {!Object} x */ function externFn(x) {}";

    Compiler compiler = createCompiler(source);
    Node root = runPass(compiler);

    Node fn = findFunction(root, "externFn");
    assertNotNull("externFn should be found", fn);

    String bodyTree = fn.getLastChild().toStringTree();
    assertTrue(bodyTree.contains("externClassChecker"));
    assertTrue(bodyTree.contains("Object"));
  }

  @Test(timeout = 4000)
  public void testUserDefinedClassUsesClassChecker() {
    String source =
        "/** @constructor */\n" +
        "function Foo() {}\n" +
        "/** @param {!Foo} x */\n" +
        "function classFn(x) {}\n";

    Compiler compiler = createCompiler(source);
    Node root = runPass(compiler);

    assertTrue(root.toStringTree().contains("instance_of__Foo"));

    Node fn = findFunction(root, "classFn");
    assertNotNull("classFn should be found", fn);

    String bodyTree = fn.getLastChild().toStringTree();
    assertTrue(bodyTree.contains("classChecker"));
    assertTrue(bodyTree.contains("Foo"));
  }

  @Test(timeout = 4000)
  public void testInterfaceTypeUsesInterfaceChecker() {
    String source =
        "/** @interface */\n" +
        "function I() {}\n" +
        "/** @param {!I} x */\n" +
        "function interfaceFn(x) {}\n";

    Compiler compiler = createCompiler(source);
    Node root = runPass(compiler);

    Node fn = findFunction(root, "interfaceFn");
    assertNotNull("interfaceFn should be found", fn);

    String bodyTree = fn.getLastChild().toStringTree();
    assertTrue(bodyTree.contains("interfaceChecker"));
    assertTrue(bodyTree.contains("I"));
  }

  @Test(timeout = 4000)
  public void testUnionTypeCreatesMultipleCheckers() {
    String source = "/** @param {string|number} x */ function unionFn(x) {}";

    Compiler compiler = createCompiler(source);
    Node root = runPass(compiler);

    Node fn = findFunction(root, "unionFn");
    assertNotNull("unionFn should be found", fn);

    String bodyTree = fn.getLastChild().toStringTree();
    assertTrue(bodyTree.contains("valueChecker"));
    assertTrue(bodyTree.contains("string"));
    assertTrue(bodyTree.contains("number"));
  }

  @Test(timeout = 4000)
  public void testReturnValueCheck() {
    String source = "/** @return {string} */ function returningFn() { return \"x\"; }";

    Compiler compiler = createCompiler(source);
    Node root = runPass(compiler);

    Node fn = findFunction(root, "returningFn");
    assertNotNull("returningFn should be found", fn);

    Node body = fn.getLastChild();
    Node ret = body.getFirstChild();
    assertEquals(Token.RETURN, ret.getType());
    assertNotNull(ret.getFirstChild());
    assertEquals(Token.CALL, ret.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testBareReturnIsLeftAlone() {
    String source = "function bareReturnFn() { return; }";

    Compiler compiler = createCompiler(source);
    Node root = runPass(compiler);

    Node fn = findFunction(root, "bareReturnFn");
    assertNotNull("bareReturnFn should be found", fn);

    Node ret = fn.getLastChild().getFirstChild();
    assertEquals(Token.RETURN, ret.getType());
    assertNull(ret.getFirstChild());
  }

  @Test(timeout = 4000)
  public void testUncheckableReturnTypeDoesNotInsertCheck() {
    String source = "/** @return {?} */ function unknownReturnFn() { return null; }";

    Compiler compiler = createCompiler(source);
    Node root = runPass(compiler);

    Node fn = findFunction(root, "unknownReturnFn");
    assertNotNull("unknownReturnFn should be found", fn);

    Node ret = fn.getLastChild().getFirstChild();
    assertEquals(Token.RETURN, ret.getType());
    assertNotNull(ret.getFirstChild());
    assertTrue(ret.getFirstChild().getType() != Token.CALL);
  }

  @Test(timeout = 4000)
  public void testImplementedInterfaceMarkerIsAdded() {
    String source =
        "/** @interface */\n" +
        "function I() {}\n" +
        "/** @constructor @implements {I} */\n" +
        "function C() {}\n";

    Compiler compiler = createCompiler(source);
    Node root = runPass(compiler);

    String rootTree = root.toStringTree();
    assertTrue(rootTree.contains("instance_of__C"));
    assertTrue(rootTree.contains("implements__I"));
  }

  @Test(timeout = 4000)
  public void testCustomLogFunctionIsSubstituted() {
    String source = "var x = 1;";

    Compiler compiler = createCompiler(source);
    Node root = compiler.getRoot();

    RuntimeTypeCheck pass = new RuntimeTypeCheck(
        compiler, "function myLogBroker(warning, expr) {}");
    pass.process(null, root);

    assertTrue(compiler.getRoot().toStringTree().contains("myLogBroker"));
  }

  @Test(timeout = 4000)
  public void testGetBoilerplateCodeWithNullLogFunction() {
    Compiler compiler = createCompiler("var x = 1;");

    Node boilerplate = RuntimeTypeCheck.getBoilerplateCode(compiler, null);
    assertNotNull("boilerplate should be parsed", boilerplate);
    assertTrue(boilerplate.toStringTree().contains("warning"));
  }

  @Test(timeout = 4000)
  public void testProcessWithNoTypedFunctionsDoesNotCrash() {
    Compiler compiler = createCompiler("var x = 1;");
    Node root = runPass(compiler);
    assertNotNull(root);
  }
}