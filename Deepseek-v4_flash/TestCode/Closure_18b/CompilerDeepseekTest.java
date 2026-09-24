import static org.junit.Assert.*;
import org.junit.Test;

import com.google.javascript.jscomp.*;
import java.util.*;

public class CompilerDeepseekTest {

  /**
   * Directly targets the dependency-sorting defect.
   * If sorting is broken, the input order remains unsorted and this test fails.
   */
  @Test(timeout = 4000)
  public void testDependencySorting() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();

    DependencyOptions depOptions = new DependencyOptions();
    depOptions.setDependencySorting(true);
    options.setDependencyOptions(depOptions);

    List<SourceFile> inputs = new ArrayList<>();
    inputs.add(SourceFile.fromCode("root.js", "goog.require('b');"));
    inputs.add(SourceFile.fromCode("b.js", "goog.provide('b');"));
    inputs.add(SourceFile.fromCode("a.js", "goog.provide('a');"));

    Result result = compiler.compile(
        Collections.<SourceFile>emptyList(), inputs, options);

    assertTrue("Compilation should succeed", result.success);

    List<String> names = new ArrayList<>();
    for (CompilerInput input : compiler.getInputs()) {
      names.add(input.getName());
    }

    assertTrue("Dependency sorting should place b.js before root.js",
        names.indexOf("b.js") < names.indexOf("root.js"));
  }

  @Test
  public void testCompileSimple() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();

    Result result = compiler.compile(
        Collections.<SourceFile>emptyList(),
        Collections.singletonList(SourceFile.fromCode("test.js", "var x = 1;")),
        options);

    assertTrue(result.success);
  }

  @Test
  public void testNewExternInput() {
    Compiler compiler = new Compiler();
    CompilerInput input = compiler.newExternInput("extern.js");

    assertNotNull(input);
    assertEquals("extern.js", input.getName());
    assertEquals(1, compiler.getExterns().size());
    assertTrue(compiler.getInputsById().containsKey("extern.js"));
  }
}