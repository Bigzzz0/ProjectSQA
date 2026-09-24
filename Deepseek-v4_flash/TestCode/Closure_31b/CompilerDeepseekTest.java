import com.google.javascript.jscomp.*;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.rhino.Node;
import org.junit.Test;
import static org.junit.Assert.*;

public class CompilerWhitespaceModeTest {

  @Test
  public void testDependencySortingWhitespaceMode() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setDependencyMode(CompilerOptions.DependencyMode.SORT_ONLY);
    options.setPrettyPrint(false);
    options.setPrintInputDelimiter(false);

    SourceFile externs = SourceFile.fromCode("externs.js", "");
    SourceFile a = SourceFile.fromCode("a.js", "goog.provide('a');");
    SourceFile b = SourceFile.fromCode("b.js", "goog.require('a');goog.provide('b');");

    Result result = compiler.compile(
        new SourceFile[] {externs}, new SourceFile[] {a, b}, options);

    assertTrue("Compilation failed", result.success);

    String output = compiler.toSource();
    // In whitespace mode, the output should be minified (no newlines)
    // and the dependency order should place a before b.
    assertEquals(
        "goog.provide('a');goog.require('a');goog.provide('b');",
        output);
  }
}