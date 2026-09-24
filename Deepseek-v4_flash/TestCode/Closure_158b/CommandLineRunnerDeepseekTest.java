package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * [Branch & Defect Analysis Matrix]
 *
 * Target: CommandLineRunner flag parsing, config validation, option creation,
 * extern creation, and warning-guard ordering.
 *
 * Critical defect branches:
 *  - --jscomp_error / --jscomp_warning / --jscomp_off ordering must be
 *    preserved across interleaved command-line flags.
 *  - A later --jscomp_error must override an earlier --jscomp_off.
 *  - A later --jscomp_warning must override an earlier --jscomp_off.
 *  - A later --jscomp_off must override both earlier error/warning guards.
 *
 * Additional decision branches exercised:
 *  - BooleanOptionHandler true/false synonyms and bare-boolean behavior.
 *  - CmdLineException, IOException and --help paths.
 *  - --flagfile success, nested --flagfile rejection, and missing-file failure.
 *  - createOptions debug/generate_exports/formatting/closure-pass branches.
 *  - createExterns custom-only vs default extern loading.
 *  - The private processArgs equals-sign and quote-unwrapping transformation.
 */
public class CommandLineRunnerDeepseekTest {

  private static class TestCommandLineRunner extends CommandLineRunner {
    TestCommandLineRunner(String[] args, PrintStream out, PrintStream err) {
      super(args, out, err);
    }

    CompilerOptions exposeCreateOptions() {
      return createOptions();
    }

    Compiler exposeCreateCompiler() {
      return createCompiler();
    }

    List<JSSourceFile> exposeCreateExterns() throws Exception {
      return createExterns();
    }
  }

  private static CommandLineRunner parse(String... args) throws Exception {
    return new CommandLineRunner(
        args,
        new PrintStream(new ByteArrayOutputStream()),
        new PrintStream(new ByteArrayOutputStream()));
  }

  private static CommandLineRunner newRunner(
      String[] args, ByteArrayOutputStream out, ByteArrayOutputStream err)
      throws Exception {
    return new CommandLineRunner(
        args,
        new PrintStream(out, true, "UTF-8"),
        new PrintStream(err, true, "UTF-8"));
  }

  private static Object getFlags(CommandLineRunner runner) throws Exception {
    Field f = CommandLineRunner.class.getDeclaredField("flags");
    f.setAccessible(true);
    return f.get(runner);
  }

  private static Object getField(CommandLineRunner runner, String fieldName)
      throws Exception {
    return getField(getFlags(runner), fieldName);
  }

  private static Object getField(Object owner, String fieldName) throws Exception {
    Field f = owner.getClass().getDeclaredField(fieldName);
    f.setAccessible(true);
    return f.get(owner);
  }

  private static boolean getBool(CommandLineRunner runner, String fieldName)
      throws Exception {
    return ((Boolean) getField(runner, fieldName)).booleanValue();
  }

  private static String getString(CommandLineRunner runner, String fieldName)
      throws Exception {
    return (String) getField(runner, fieldName);
  }

  private static int getInt(CommandLineRunner runner, String fieldName)
      throws Exception {
    return ((Integer) getField(runner, fieldName)).intValue();
  }

  private static String getEnumName(CommandLineRunner runner, String fieldName)
      throws Exception {
    return ((Enum<?>) getField(runner, fieldName)).name();
  }

  @SuppressWarnings("unchecked")
  private static List<String> getStringList(
      CommandLineRunner runner, String fieldName) throws Exception {
    return (List<String>) getField(runner, fieldName);
  }

  private static String runCompile(String... warningGuardArgs) throws Exception {
    File js = File.createTempFile("cmdline-runner-deepseek", ".js");
    File outFile = File.createTempFile("cmdline-runner-deepseek", ".out.js");
    try {
      Files.write(js.toPath(),
          "var x = y;\n".getBytes(StandardCharsets.UTF_8));
      List<String> args = new ArrayList<String>();
      args.add("--js");
      args.add(js.getAbsolutePath());
      args.add("--js_output_file");
      args.add(outFile.getAbsolutePath());
      args.addAll(Arrays.asList(warningGuardArgs));

      ByteArrayOutputStream outBaos = new ByteArrayOutputStream();
      ByteArrayOutputStream errBaos = new ByteArrayOutputStream();
      PrintStream outPs = new PrintStream(outBaos, true, "UTF-8");
      PrintStream errPs = new PrintStream(errBaos, true, "UTF-8");
      CommandLineRunner runner =
          new CommandLineRunner(args.toArray(new String[args.size()]), outPs, errPs);
      assertTrue("Config should be valid: " + errBaos.toString("UTF-8"),
          runner.shouldRunCompiler());
      runner.run();
      outPs.flush();
      errPs.flush();
      return errBaos.toString("UTF-8");
    } finally {
      Files.deleteIfExists(js.toPath());
      Files.deleteIfExists(outFile.toPath());
    }
  }

  @Test(timeout = 4000)
  public void testConstructorWithEmptyArgsIsValid() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    CommandLineRunner runner = newRunner(new String[0], out, err);
    assertNotNull(runner);
    assertTrue(runner.shouldRunCompiler());
    assertEquals("", err.toString("UTF-8"));
  }

  @Test(timeout = 4000)
  public void testInvalidFlagDisablesRunner() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    CommandLineRunner runner = newRunner(new String[] {"--unknown-flag"}, out, err);
    assertFalse(runner.shouldRunCompiler());
    assertTrue(err.toString("UTF-8").trim().length() > 0);
  }

  @Test(timeout = 4000)
  public void testHelpDisablesRunner() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    CommandLineRunner runner = newRunner(new String[] {"--help"}, out, err);
    assertFalse(runner.shouldRunCompiler());
    assertTrue(err.toString("UTF-8").contains("--help"));
  }

  @Test(timeout = 4000)
  public void testVersionPrintsVersionAndKeepsRunnerValid() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    CommandLineRunner runner = newRunner(new String[] {"--version"}, out, err);
    assertTrue(runner.shouldRunCompiler());
    assertTrue(err.toString("UTF-8").contains("Version:"));
  }

  @Test(timeout = 4000)
  public void testBooleanFlagSynonyms() throws Exception {
    assertTrue(getBool(parse("--debug"), "debug"));
    assertTrue(getBool(parse("--debug=on"), "debug"));
    assertFalse(getBool(parse("--debug=off"), "debug"));
    assertTrue(getBool(parse("--debug=yes"), "debug"));
    assertFalse(getBool(parse("--debug=no"), "debug"));
    assertTrue(getBool(parse("--debug=1"), "debug"));
    assertFalse(getBool(parse("--debug=0"), "debug"));
  }

  @Test(timeout = 4000)
  public void testEnumAndStringFlagsAreParsed() throws Exception {
    CommandLineRunner runner = parse(
        "--compilation_level=ADVANCED",
        "--warning_level=VERBOSE",
        "--charset=UTF-8",
        "--language_in=ECMASCRIPT5_STRICT",
        "--summary_detail_level=3",
        "--logging_level=FINE");
    assertEquals("ADVANCED", getEnumName(runner, "compilation_level"));
    assertEquals("VERBOSE", getEnumName(runner, "warning_level"));
    assertEquals("UTF-8", getString(runner, "charset"));
    assertEquals("ECMASCRIPT5_STRICT", getString(runner, "language_in"));
    assertEquals(3, getInt(runner, "summary_detail_level"));
    assertEquals("FINE", getString(runner, "logging_level"));
  }

  @Test(timeout = 4000)
  public void testJsAndModuleListsAreParsed() throws Exception {
    CommandLineRunner runner = parse(
        "--js=a.js", "--js=b.js",
        "--module=m:1:dep", "--module=n:2",
        "--externs=e1.js",
        "--jscomp_error=checkVars",
        "--jscomp_off=globalThis");
    assertEquals(Arrays.asList("a.js", "b.js"), getStringList(runner, "js"));
    assertEquals(Arrays.asList("m:1:dep", "n:2"), getStringList(runner, "module"));
    assertEquals(Arrays.asList("e1.js"), getStringList(runner, "externs"));
    assertEquals(Arrays.asList("checkVars"), getStringList(runner, "jscomp_error"));
    assertEquals(Arrays.asList("globalThis"), getStringList(runner, "jscomp_off"));
  }

  @Test(timeout = 4000)
  public void testQuotedValuesAreUnwrapped() throws Exception {
    CommandLineRunner runner = parse(
        "--js_output_file=\"out file.js\"",
        "--output_wrapper=\"(function(){%output%})();\"",
        "--define='FOO'");
    assertEquals("out file.js", getString(runner, "js_output_file"));
    assertEquals("(function(){%output%})();", getString(runner, "output_wrapper"));
    assertEquals(Arrays.asList("FOO"), getStringList(runner, "define"));
  }

  @Test(timeout = 4000)
  public void testProcessArgsPrivateMethod() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    CommandLineRunner runner = newRunner(new String[0], out, err);
    Method m = CommandLineRunner.class.getDeclaredMethod(
        "processArgs", String[].class);
    m.setAccessible(true);
    String[] input =
        new String[] {"--js=file.js", "--charset=\"UTF-8\"", "plain"};
    String[] result = (String[]) m.invoke(runner, (Object) input);
    assertArrayEquals(
        new String[] {"--js", "file.js", "--charset", "UTF-8", "plain"},
        result);
  }

  @Test(timeout = 4000)
  public void testFlagFileIsParsed() throws Exception {
    File flagFile = File.createTempFile("cl-runner-flags", ".txt");
    try {
      Files.write(flagFile.toPath(),
          "--compilation_level ADVANCED\n--debug true\n"
              .getBytes(Charset.defaultCharset()));
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      ByteArrayOutputStream err = new ByteArrayOutputStream();
      CommandLineRunner runner =
          newRunner(new String[] {"--flagfile", flagFile.getAbsolutePath()}, out, err);
      assertTrue(runner.shouldRunCompiler());
      assertEquals("ADVANCED", getEnumName(runner, "compilation_level"));
      assertTrue(getBool(runner, "debug"));
    } finally {
      Files.deleteIfExists(flagFile.toPath());
    }
  }

  @Test(timeout = 4000)
  public void testFlagFileRejectsNestedFlagfile() throws Exception {
    File flagFile = File.createTempFile("cl-runner-nested-flag", ".txt");
    try {
      Files.write(flagFile.toPath(),
          "--flagfile /tmp/should/not/loop\n".getBytes(Charset.defaultCharset()));
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      ByteArrayOutputStream err = new ByteArrayOutputStream();
      CommandLineRunner runner =
          newRunner(new String[] {"--flagfile", flagFile.getAbsolutePath()}, out, err);
      assertFalse(runner.shouldRunCompiler());
      assertTrue(err.toString("UTF-8").contains("--flagfile"));
    } finally {
      Files.deleteIfExists(flagFile.toPath());
    }
  }

  @Test(timeout = 4000)
  public void testFlagFileReadErrorDisablesRunner() throws Exception {
    File missing = File.createTempFile("cl-runner-missing", ".txt");
    assertTrue(missing.delete());
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    CommandLineRunner runner =
        newRunner(new String[] {"--flagfile", missing.getAbsolutePath()}, out, err);
    assertFalse(runner.shouldRunCompiler());
    assertTrue(err.toString("UTF-8").contains("read error"));
  }

  @Test(timeout = 4000)
  public void testDefaultExternsAreLoadedInOrder() throws Exception {
    List<JSSourceFile> externs = CommandLineRunner.getDefaultExterns();
    assertNotNull(externs);
    assertTrue(externs.size() > 0);
    assertEquals("externs.zip//es3.js", externs.get(0).getName());
  }

  @Test(timeout = 4000)
  public void testCreateOptionsAppliesFormattingAndClosurePass() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    TestCommandLineRunner runner = new TestCommandLineRunner(
        new String[] {
            "--formatting=PRETTY_PRINT",
            "--process_closure_primitives=false"
        },
        new PrintStream(out, true, "UTF-8"),
        new PrintStream(err, true, "UTF-8"));
    CompilerOptions options = runner.exposeCreateOptions();
    assertTrue(options.prettyPrint);
    assertFalse(options.closurePass);
  }

  @Test(timeout = 4000)
  public void testCreateOptionsAppliesDebugGenerateExportsAndSecondFormatting()
      throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    TestCommandLineRunner runner = new TestCommandLineRunner(
        new String[] {
            "--debug",
            "--generate_exports",
            "--formatting=PRINT_INPUT_DELIMITER",
            "--compilation_level=ADVANCED",
            "--warning_level=QUIET"
        },
        new PrintStream(out, true, "UTF-8"),
        new PrintStream(err, true, "UTF-8"));
    CompilerOptions options = runner.exposeCreateOptions();
    assertTrue(options.printInputDelimiter);
    assertFalse(options.prettyPrint);
  }

  @Test(timeout = 4000)
  public void testCreateCompilerReturnsNonNullCompiler() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    TestCommandLineRunner runner = new TestCommandLineRunner(
        new String[0],
        new PrintStream(out, true, "UTF-8"),
        new PrintStream(err, true, "UTF-8"));
    assertNotNull(runner.exposeCreateCompiler());
  }

  @Test(timeout = 4000)
  public void testCreateExternsUsesOnlyCustomExternsWhenRequested()
      throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    TestCommandLineRunner runner = new TestCommandLineRunner(
        new String[] {"--use_only_custom_externs"},
        new PrintStream(out, true, "UTF-8"),
        new PrintStream(err, true, "UTF-8"));
    List<JSSourceFile> externs = runner.exposeCreateExterns();
    assertNotNull(externs);
    assertTrue(externs.isEmpty());
  }

  @Test(timeout = 4000)
  public void testCreateExternsIncludesDefaultExternsByDefault() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    TestCommandLineRunner runner = new TestCommandLineRunner(
        new String[0],
        new PrintStream(out, true, "UTF-8"),
        new PrintStream(err, true, "UTF-8"));
    List<JSSourceFile> externs = runner.exposeCreateExterns();
    assertNotNull(externs);
    assertTrue(externs.size() > 0);
    assertEquals("externs.zip//es3.js", externs.get(0).getName());
  }

  @Test(timeout = 4000)
  public void testWarningGuardOrdering1() throws Exception {
    String err = runCompile(
        "--jscomp_error=undefinedVars",
        "--jscomp_off=undefinedVars");
    assertFalse("Expected no diagnostic, but got:\n" + err,
        err.contains("undefined"));
  }

  @Test(timeout = 4000)
  public void testWarningGuardOrdering2() throws Exception {
    String err = runCompile(
        "--jscomp_off=undefinedVars",
        "--jscomp_error=undefinedVars");
    assertTrue("Expected error after --jscomp_error, but got:\n" + err,
        err.contains("undefined"));
    assertTrue("Expected ERROR level diagnostic, but got:\n" + err,
        err.contains("ERROR"));
  }

  @Test(timeout = 4000)
  public void testWarningGuardOrdering3() throws Exception {
    String err = runCompile(
        "--jscomp_warning=undefinedVars",
        "--jscomp_off=undefinedVars");
    assertFalse("Expected no diagnostic, but got:\n" + err,
        err.contains("undefined"));
  }

  @Test(timeout = 4000)
  public void testWarningGuardOrdering4() throws Exception {
    String err = runCompile(
        "--jscomp_off=undefinedVars",
        "--jscomp_warning=undefinedVars");
    assertTrue("Expected warning after --jscomp_warning, but got:\n" + err,
        err.contains("undefined"));
    assertTrue("Expected WARNING level diagnostic, but got:\n" + err,
        err.contains("WARNING"));
  }
}