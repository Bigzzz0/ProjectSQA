import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.javascript.jscomp.CheckLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.JSError;
import com.google.javascript.jscomp.Result;
import com.google.javascript.jscomp.SourceFile;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

/**
 * Tests for {@code CheckSideEffects} focusing on a known bug with nested
 * comma expressions used as the callee of a call.
 *
 * <p>In the buggy implementation, when a comma expression is nested inside
 * another comma expression that is the callee of a call, the inner comma's
 * non-last child is incorrectly treated as having its value used.  For
 * example, in {@code (x = 1, y, z)()}, the value of {@code y} is discarded
 * by the outer comma, so {@code y} should be reported as useless code.
 * The buggy pass suppresses the warning because it sees a CALL somewhere
 * up the ancestor chain.
 */
public class CheckSideEffectsDeepseekTest {

  private JSError[] getWarnings(String code) {
    CompilerOptions options = new CompilerOptions();
    options.setCheckSideEffects(CheckLevel.WARNING);
    options.setCheckSymbols(false);
    options.setCheckTypes(false);

    Compiler compiler = new Compiler();
    List<SourceFile> externs = Collections.emptyList();
    List<SourceFile> inputs =
        Collections.singletonList(SourceFile.fromCode("test", code));
    Result result = compiler.compile(externs, inputs, options);
    return result.warnings;
  }

  @Test
  public void testSimpleUselessExpressionWarns() {
    JSError[] warnings = getWarnings("x;");
    assertEquals("Expected one useless-code warning for 'x;'",
        1, warnings.length);
    assertTrue(warnings[0].getDescription().contains("lacks side-effects"));
  }

  @Test
  public void testSideEffectAssignmentDoesNotWarn() {
    JSError[] warnings = getWarnings("x = 1;");
    assertEquals("Assignment has side effects and should not warn",
        0, warnings.length);
  }

  @Test
  public void testEvalCommaDoesNotWarn() {
    // The first element of the comma expression is intentionally discarded
    // to force an indirect eval.  This is a recognised idiom and must not
    // produce a useless-code warning.
    JSError[] warnings = getWarnings("(0, eval)('x');");
    assertEquals("Indirect eval idiom should not warn", 0, warnings.length);
  }

  @Test
  public void testNestedCommaInCalleeWarnsForDiscardedExpression() {
    // AST for (x = 1, y, z)():
    //   CALL( COMMA( COMMA(ASSIGN(x, 1), y), z ) )
    //
    // The value of 'y' is discarded by the outer comma, so 'y' is useless
    // code.  The buggy ancestor walk sees the CALL and incorrectly assumes
    // the value is used, suppressing the warning.
    JSError[] warnings = getWarnings("(x = 1, y, z)();");
    assertEquals(
        "Expected one useless-code warning for discarded expression 'y'",
        1, warnings.length);
    assertTrue(warnings[0].getDescription().contains("lacks side-effects"));
  }
}