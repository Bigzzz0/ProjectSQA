package com.google.javascript.jscomp;

import static org.junit.Assert.assertTrue;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class PeepholeOptimizationsPassTest {

  @Test(timeout = 4000)
  public void testTraversalContinuesAfterRemoval() {
    // Build a simple script: [expr a, expr b]
    Node root = new Node(Token.SCRIPT);
    Node a = new Node(Token.EXPR_RESULT, new Node(Token.NAME, "a"));
    Node b = new Node(Token.EXPR_RESULT, new Node(Token.NAME, "b"));
    root.addChildToBack(a);
    root.addChildToBack(b);

    Compiler compiler = new Compiler();
    RemoveFirstOptimization opt = new RemoveFirstOptimization();
    PeepholeOptimizationsPass pass =
        new PeepholeOptimizationsPass(compiler, opt);

    pass.process(null, root);

    // The second statement must have been visited.
    assertTrue("Expected second child to be visited", opt.visited.contains("b"));
  }

  /**
   * A peephole optimization that removes the first expression statement
   * (the one containing the name "a") and records every name it visits.
   */
  private static class RemoveFirstOptimization extends AbstractPeepholeOptimization {
    private boolean removed = false;
    private final List<String> visited = new ArrayList<>();

    @Override
    public Node optimizeSubtree(Node node) {
      if (!removed && node.isExprResult() && node.getParent() != null
          && node.getParent().isScript()) {
        Node nameNode = node.getFirstChild();
        if (nameNode != null && nameNode.isName()
            && "a".equals(nameNode.getString())) {
          node.getParent().removeChild(node);
          removed = true;
          return null;
        }
      }

      if (node.isName()) {
        visited.add(node.getString());
      }
      return node;
    }
  }
}