import com.google.javascript.jscomp.*;
import com.google.javascript.rhino.Node;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for the Normalize pass, specifically targeting the bug where
 * valid catch-block variable shadowing causes an internal compiler error
 * or an incorrect CATCH_BLOCK_VAR_ERROR diagnostic.
 */
public class NormalizeTest {

  /**
   * Tests that a catch parameter shadowing a function-scoped var does not
   * produce an error. This is valid JavaScript and should be normalized
   * without reporting CATCH_BLOCK_VAR_ERROR.
   */
  @Test
  public void testCatchBlockWithVarDeclaration() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    String code = "var e = 1; try { throw 0; } catch(e) { e; }";
    compiler.init(code, code, options);
    Node externs = compiler.getExternsRoot();
    Node js = compiler.getJsRoot();

    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externs, js);

    assertTrue("Expected no errors, but got: " + compiler.getErrors(),
        compiler.getErrors().length == 0);
  }

  /**
   * Tests that a catch block in externs does not cause an error.
   * Externs are environment declarations and should not be subject to
   * the same normalization checks as user code.
   */
  @Test
  public void testExternsWithCatchBlock() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    String externs = "try { throw 0; } catch(e) { e; }";
    String code = "var x = 1;";
    compiler.init(externs, code, options);
    Node externsRoot = compiler.getExternsRoot();
    Node jsRoot = compiler.getJsRoot();

    Normalize normalize = new Normalize(compiler, false);
    normalize.process(externsRoot, jsRoot);

    assertTrue("Expected no errors, but got: " + compiler.getErrors(),
        compiler.getErrors().length == 0);
  }

  /**
   * Tests that Normalize does not throw an internal exception when
   * encountering a catch parameter that shadows a var declaration.
   */
  @Test
  public void testNoInternalExceptionForCatchShadowing() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    String code = "var e = 1; try { throw 0; } catch(e) { var e = 2; }";
    compiler.init(code, code, options);
    Node externs = compiler.getExternsRoot();
    Node js = compiler.getJsRoot();

    try {
      Normalize normalize = new Normalize(compiler, false);
      normalize.process(externs, js);
    } catch (RuntimeException e) {
      fail("Normalize threw an unexpected exception: " + e.getMessage());
    }
  }
}