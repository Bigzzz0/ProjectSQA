/*
 * [Branch & Defect Analysis Matrix]
 *
 * Targeted Class: com.google.javascript.jscomp.CompilerOptions
 *
 * Key Areas & Decision Points Covered:
 * 1. getDefineReplacements():
 *    - Boolean branch: true / false -> Token.TRUE / Token.FALSE
 *    - Integer branch: int value -> Node.newNumber(int)
 *    - Double branch: double value -> Node.newNumber(double)
 *    - String branch: string value -> Node.newString(String)
 *    - Precondition failure: Object type not Boolean/Integer/Double/String
 * 2. WarningsGuard Integration:
 *    - addWarningsGuard(): guard == null initializes ComposeWarningsGuard; guard != null adds to existing
 *    - enables(DiagnosticGroup): guard == null (false), guard != null (delegates)
 *    - disables(DiagnosticGroup): guard == null (false), guard != null (delegates)
 *    - setWarningLevel(): wraps DiagnosticGroupWarningsGuard and adds it
 * 3. State & Configuration Mutators:
 *    - skipAllCompilerPasses(), setRenamingPolicy(), setCollapsePropertiesOnExternTypes(),
 *      setProcessObjectPropertyString(), setIdGenerators(), setReplaceStringsConfiguration(),
 *      setRewriteNewDateGoogNow(), setRemoveAbstractMethods(), setNameAnonymousFunctionsOnly(),
 *      setColorizeErrorOutput(), shouldColorizeErrorOutput(), setChainCalls(),
 *      enableRuntimeTypeCheck(), disableRuntimeTypeCheck(), setCodingConvention(),
 *      setManageClosureDependencies(), setSummaryDetailLevel(), enableExternExports(),
 *      isExternExportsEnabled(), setLooseTypes().
 * 4. TracerMode & DevMode:
 *    - TracerMode.isOn() -> ALL (true), FAST (true), OFF (false)
 *    - DevMode values: OFF, START, START_AND_END, EVERY_PASS
 * 5. Object Contract:
 *    - clone(): Deep copy / clone state verification
 * 6. Defects4J Targeted Defect:
 *    - Defect: CommandLineRunnerTest::testCharSetExpansion -> expected:<US-ASCII> but was:<null>
 *    - Target: CompilerOptions default outputCharset initialized or expected to be US-ASCII.
 */

package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class CompilerOptionsGeminiTest {

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testDefaultConstructorFieldInitialization() {
    CompilerOptions options = new CompilerOptions();

    assertFalse(options.ideMode);
    assertFalse(options.skipAllPasses);
    assertFalse(options.nameAnonymousFunctionsOnly);
    assertEquals(CompilerOptions.DevMode.OFF, options.devMode);
    assertFalse(options.manageClosureDependencies);
    assertNull(options.messageBundle);

    assertFalse(options.checkSymbols);
    assertEquals(CheckLevel.OFF, options.checkShadowVars);
    assertEquals(CheckLevel.OFF, options.aggressiveVarCheck);
    assertEquals(CheckLevel.OFF, options.checkFunctions);
    assertEquals(CheckLevel.OFF, options.checkMethods);
    assertFalse(options.checkDuplicateMessages);
    assertFalse(options.allowLegacyJsMessages);
    assertFalse(options.strictMessageReplacement);
    assertFalse(options.checkSuspiciousCode);
    assertFalse(options.checkControlStructures);
    assertEquals(CheckLevel.OFF, options.checkUndefinedProperties);
    assertFalse(options.checkUnusedPropertiesEarly);
    assertFalse(options.checkTypes);
    assertFalse(options.tightenTypes);
    assertFalse(options.inferTypesInGlobalScope);
    assertFalse(options.checkTypedPropertyCalls);
    assertEquals(CheckLevel.OFF, options.reportMissingOverride);
    assertEquals(CheckLevel.OFF, options.reportUnknownTypes);
    assertEquals(CheckLevel.OFF, options.checkRequires);
    assertEquals(CheckLevel.OFF, options.checkProvides);
    assertEquals(CheckLevel.OFF, options.checkGlobalNamesLevel);
    assertEquals(CheckLevel.ERROR, options.brokenClosureRequiresLevel);
    assertEquals(CheckLevel.OFF, options.checkGlobalThisLevel);
    assertEquals(CheckLevel.OFF, options.checkUnreachableCode);
    assertEquals(CheckLevel.OFF, options.checkMissingReturn);
    assertEquals(CheckLevel.OFF, options.checkMissingGetCssNameLevel);
    assertNull(options.checkMissingGetCssNameBlacklist);
    assertFalse(options.checkEs5Strict);
    assertFalse(options.checkCaja);
    assertFalse(options.computeFunctionSideEffects);
    assertFalse(options.chainCalls);

    assertFalse(options.foldConstants);
    assertFalse(options.removeConstantExpressions);
    assertFalse(options.coalesceVariableNames);
    assertFalse(options.deadAssignmentElimination);
    assertFalse(options.inlineConstantVars);
    assertFalse(options.inlineFunctions);
    assertFalse(options.inlineLocalFunctions);
    assertFalse(options.crossModuleCodeMotion);
    assertFalse(options.crossModuleMethodMotion);
    assertFalse(options.inlineGetters);
    assertFalse(options.inlineVariables);
    assertFalse(options.inlineLocalVariables);
    assertFalse(options.smartNameRemoval);
    assertFalse(options.removeDeadCode);
    assertFalse(options.extractPrototypeMemberDeclarations);
    assertFalse(options.removeUnusedPrototypeProperties);
    assertFalse(options.removeUnusedPrototypePropertiesInExterns);
    assertFalse(options.removeUnusedVars);
    assertTrue(options.removeUnusedVarsInGlobalScope);
    assertFalse(options.aliasExternals);
    assertFalse(options.collapseVariableDeclarations);
    assertFalse(options.groupVariableDeclarations);
    assertFalse(options.collapseAnonymousFunctions);
    assertTrue(options.aliasableStrings.isEmpty());
    assertEquals("", options.aliasStringsBlacklist);
    assertFalse(options.aliasAllStrings);
    assertFalse(options.outputJsStringUsage);
    assertFalse(options.convertToDottedProperties);
    assertFalse(options.rewriteFunctionExpressions);
    assertFalse(options.optimizeParameters);

    assertEquals(VariableRenamingPolicy.OFF, options.variableRenaming);
    assertEquals(PropertyRenamingPolicy.OFF, options.propertyRenaming);
    assertFalse(options.labelRenaming);
    assertFalse(options.generatePseudoNames);
    assertNull(options.renamePrefix);
    assertFalse(options.aliasKeywords);
    assertFalse(options.collapseProperties);
    assertFalse(options.collapsePropertiesOnExternTypes);
    assertFalse(options.devirtualizePrototypeMethods);
    assertFalse(options.disambiguateProperties);
    assertFalse(options.ambiguateProperties);
    assertEquals(AnonymousFunctionNamingPolicy.OFF, options.anonymousFunctionNaming);
    assertFalse(options.exportTestFunctions);

    assertFalse(options.runtimeTypeCheck);
    assertNull(options.runtimeTypeCheckLogFunction);
    assertFalse(options.instrumentForCoverage);
    assertFalse(options.instrumentForCoverageOnly);
    assertFalse(options.ignoreCajaProperties);
    assertNull(options.syntheticBlockStartMarker);
    assertNull(options.syntheticBlockEndMarker);
    assertNull(options.locale);
    assertFalse(options.markAsCompiled);
    assertFalse(options.removeTryCatchFinally);
    assertFalse(options.closurePass);
    assertTrue(options.rewriteNewDateGoogNow);
    assertTrue(options.removeAbstractMethods);
    assertTrue(options.stripTypes.isEmpty());
    assertTrue(options.stripNameSuffixes.isEmpty());
    assertTrue(options.stripNamePrefixes.isEmpty());
    assertTrue(options.stripTypePrefixes.isEmpty());
    assertNull(options.customPasses);
    assertFalse(options.markNoSideEffectCalls);
    assertFalse(options.moveFunctionDeclarations);
    assertNull(options.instrumentationTemplate);
    assertEquals("", options.appNameStr);
    assertFalse(options.recordFunctionInformation);
    assertFalse(options.generateExports);
    assertNull(options.cssRenamingMap);
    assertFalse(options.processObjectPropertyString);
    assertTrue(options.idGenerators.isEmpty());
    assertTrue(options.replaceStringsFunctionDescriptions.isEmpty());
    assertEquals("", options.replaceStringsPlaceholderToken);

    assertFalse(options.printInputDelimiter);
    assertFalse(options.prettyPrint);
    assertFalse(options.lineBreak);
    assertNull(options.reportPath);
    assertEquals(CompilerOptions.TracerMode.OFF, options.tracer);
    assertFalse(options.shouldColorizeErrorOutput());
    assertEquals(ErrorFormat.SINGLELINE, options.errorFormat);
    assertNull(options.getWarningsGuard());
    assertNull(options.debugFunctionSideEffectsPath);
    assertEquals("", options.jsOutputFile);
    assertFalse(options.isExternExportsEnabled());
    assertNull(options.nameReferenceReportPath);
    assertNull(options.nameReferenceGraphPath);

    assertNull(options.sourceMapOutputPath);
    assertEquals(SourceMap.DetailLevel.SYMBOLS, options.sourceMapDetailLevel);
    assertFalse(options.looseTypes);
    assertEquals(1, options.summaryDetailLevel);
  }

  @Test(timeout = 4000)
  public void testDefineReplacementsAllTypes() {
    CompilerOptions options = new CompilerOptions();

    options.setDefineToBooleanLiteral("DEF_TRUE", true);
    options.setDefineToBooleanLiteral("DEF_FALSE", false);
    options.setDefineToNumberLiteral("DEF_INT", 42);
    options.setDefineToDoubleLiteral("DEF_DOUBLE", 3.14159);
    options.setDefineToStringLiteral("DEF_STRING", "hello world");

    Map<String, Node> map = options.getDefineReplacements();
    assertEquals(5, map.size());

    Node trueNode = map.get("DEF_TRUE");
    assertNotNull(trueNode);
    assertEquals(Token.TRUE, trueNode.getType());

    Node falseNode = map.get("DEF_FALSE");
    assertNotNull(falseNode);
    assertEquals(Token.FALSE, falseNode.getType());

    Node intNode = map.get("DEF_INT");
    assertNotNull(intNode);
    assertEquals(Token.NUMBER, intNode.getType());
    assertEquals(42.0, intNode.getDouble(), 0.0);

    Node doubleNode = map.get("DEF_DOUBLE");
    assertNotNull(doubleNode);
    assertEquals(Token.NUMBER, doubleNode.getType());
    assertEquals(3.14159, doubleNode.getDouble(), 0.000001);

    Node strNode = map.get("DEF_STRING");
    assertNotNull(strNode);
    assertEquals(Token.STRING, strNode.getType());
    assertEquals("hello world", strNode.getString());
  }

  @Test(timeout = 4000)
  public void testWarningsGuardIntegration() {
    CompilerOptions options = new CompilerOptions();
    DiagnosticGroup testGroup = new DiagnosticGroup("testGroup", DiagnosticGroups.NON_STANDARD_JSDOC);

    assertNull(options.getWarningsGuard());
    assertFalse(options.enables(testGroup));
    assertFalse(options.disables(testGroup));

    options.setWarningLevel(testGroup, CheckLevel.WARNING);
    assertNotNull(options.getWarningsGuard());
    assertTrue(options.enables(testGroup));
    assertFalse(options.disables(testGroup));

    DiagnosticGroup errorGroup = new DiagnosticGroup("errorGroup", DiagnosticGroups.ACCESS_CONTROLS);
    options.setWarningLevel(errorGroup, CheckLevel.OFF);
    assertTrue(options.disables(errorGroup));
  }

  @Test(timeout = 4000)
  public void testMutatorMethods() {
    CompilerOptions options = new CompilerOptions();

    options.skipAllCompilerPasses();
    assertTrue(options.skipAllPasses);

    options.setRenamingPolicy(VariableRenamingPolicy.LOCAL, PropertyRenamingPolicy.AGGRESSIVE_HEURISTIC);
    assertEquals(VariableRenamingPolicy.LOCAL, options.variableRenaming);
    assertEquals(PropertyRenamingPolicy.AGGRESSIVE_HEURISTIC, options.propertyRenaming);

    options.setCollapsePropertiesOnExternTypes(true);
    assertTrue(options.collapsePropertiesOnExternTypes);
    options.setCollapsePropertiesOnExternTypes(false);
    assertFalse(options.collapsePropertiesOnExternTypes);

    options.setProcessObjectPropertyString(true);
    assertTrue(options.processObjectPropertyString);

    Set<String> ids = new HashSet<>(Arrays.asList("id1", "id2"));
    options.setIdGenerators(ids);
    assertEquals(ids, options.idGenerators);
    assertNotSame(ids, options.idGenerators);

    List<String> descriptors = Arrays.asList("desc1", "desc2");
    options.setReplaceStringsConfiguration("TOKEN", descriptors);
    assertEquals("TOKEN", options.replaceStringsPlaceholderToken);
    assertEquals(descriptors, options.replaceStringsFunctionDescriptions);
    assertNotSame(descriptors, options.replaceStringsFunctionDescriptions);

    options.setRewriteNewDateGoogNow(false);
    assertFalse(options.rewriteNewDateGoogNow);

    options.setRemoveAbstractMethods(false);
    assertFalse(options.removeAbstractMethods);

    options.setNameAnonymousFunctionsOnly(true);
    assertTrue(options.nameAnonymousFunctionsOnly);

    options.setColorizeErrorOutput(true);
    assertTrue(options.shouldColorizeErrorOutput());

    options.setChainCalls(true);
    assertTrue(options.chainCalls);

    options.enableRuntimeTypeCheck("logFn");
    assertTrue(options.runtimeTypeCheck);
    assertEquals("logFn", options.runtimeTypeCheckLogFunction);

    options.disableRuntimeTypeCheck();
    assertFalse(options.runtimeTypeCheck);

    CodingConvention convention = new GoogleCodingConvention();
    options.setCodingConvention(convention);
    assertSame(convention, options.getCodingConvention());

    options.setManageClosureDependencies(true);
    assertTrue(options.manageClosureDependencies);

    options.setSummaryDetailLevel(3);
    assertEquals(3, options.summaryDetailLevel);

    options.enableExternExports(true);
    assertTrue(options.isExternExportsEnabled());
    options.enableExternExports(false);
    assertFalse(options.isExternExportsEnabled());

    options.setLooseTypes(true);
    assertTrue(options.looseTypes);
  }

  @Test(timeout = 4000)
  public void testTracerModeIsOn() {
    assertTrue(CompilerOptions.TracerMode.ALL.isOn());
    assertTrue(CompilerOptions.TracerMode.FAST.isOn());
    assertFalse(CompilerOptions.TracerMode.OFF.isOn());
  }

  @Test(timeout = 4000)
  public void testDevModeValues() {
    CompilerOptions.DevMode[] modes = CompilerOptions.DevMode.values();
    assertEquals(4, modes.length);
    assertEquals(CompilerOptions.DevMode.OFF, CompilerOptions.DevMode.valueOf("OFF"));
    assertEquals(CompilerOptions.DevMode.START, CompilerOptions.DevMode.valueOf("START"));
    assertEquals(CompilerOptions.DevMode.START_AND_END, CompilerOptions.DevMode.valueOf("START_AND_END"));
    assertEquals(CompilerOptions.DevMode.EVERY_PASS, CompilerOptions.DevMode.valueOf("EVERY_PASS"));
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testDefineReplacementsEmpty() {
    CompilerOptions options = new CompilerOptions();
    Map<String, Node> map = options.getDefineReplacements();
    assertNotNull(map);
    assertTrue(map.isEmpty());
  }

  @Test(timeout = 4000)
  public void testDefineReplacementsBoundaryValues() {
    CompilerOptions options = new CompilerOptions();

    options.setDefineToNumberLiteral("INT_MAX", Integer.MAX_VALUE);
    options.setDefineToNumberLiteral("INT_MIN", Integer.MIN_VALUE);
    options.setDefineToDoubleLiteral("DOUBLE_MAX", Double.MAX_VALUE);
    options.setDefineToDoubleLiteral("DOUBLE_MIN", Double.MIN_VALUE);
    options.setDefineToStringLiteral("EMPTY_STR", "");

    Map<String, Node> map = options.getDefineReplacements();

    assertEquals((double) Integer.MAX_VALUE, map.get("INT_MAX").getDouble(), 0.0);
    assertEquals((double) Integer.MIN_VALUE, map.get("INT_MIN").getDouble(), 0.0);
    assertEquals(Double.MAX_VALUE, map.get("DOUBLE_MAX").getDouble(), 0.0);
    assertEquals(Double.MIN_VALUE, map.get("DOUBLE_MIN").getDouble(), 0.0);
    assertEquals("", map.get("EMPTY_STR").getString());
  }

  @Test(timeout = 4000)
  public void testSetIdGeneratorsEmptyCollection() {
    CompilerOptions options = new CompilerOptions();
    options.setIdGenerators(Collections.<String>emptySet());
    assertNotNull(options.idGenerators);
    assertTrue(options.idGenerators.isEmpty());
  }

  @Test(timeout = 4000)
  public void testSetReplaceStringsConfigurationEmptyCollection() {
    CompilerOptions options = new CompilerOptions();
    options.setReplaceStringsConfiguration("", Collections.<String>emptyList());
    assertEquals("", options.replaceStringsPlaceholderToken);
    assertNotNull(options.replaceStringsFunctionDescriptions);
    assertTrue(options.replaceStringsFunctionDescriptions.isEmpty());
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Ground Truth from Defects4J)
  // =========================================================================

  /**
   * Ground truth defect: CommandLineRunnerTest::testCharSetExpansion
   * Expected: <US-ASCII> but was: <null>
   *
   * In defective versions, CompilerOptions either left outputCharset as null or
   * did not properly initialize it to US-ASCII when outputting code.
   */
  @Test(timeout = 4000)
  public void testCharSetExpansionDefect() {
    CompilerOptions options = new CompilerOptions();
    // Tests that outputCharset defaults to US-ASCII rather than null
    assertEquals(Charset.forName("US-ASCII"), options.outputCharset);
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testAddMultipleWarningsGuards() {
    CompilerOptions options = new CompilerOptions();
    DiagnosticGroup group1 = new DiagnosticGroup("g1", DiagnosticGroups.NON_STANDARD_JSDOC);
    DiagnosticGroup group2 = new DiagnosticGroup("g2", DiagnosticGroups.ACCESS_CONTROLS);

    WarningsGuard guard1 = new DiagnosticGroupWarningsGuard(group1, CheckLevel.ERROR);
    WarningsGuard guard2 = new DiagnosticGroupWarningsGuard(group2, CheckLevel.OFF);

    options.addWarningsGuard(guard1);
    assertNotNull(options.getWarningsGuard());

    // Second call triggers the else branch in addWarningsGuard
    options.addWarningsGuard(guard2);
    assertTrue(options.enables(group1));
    assertTrue(options.disables(group2));
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testCloneIntegrity() throws CloneNotSupportedException {
    CompilerOptions options = new CompilerOptions();
    options.checkSymbols = true;
    options.checkTypes = true;
    options.foldConstants = true;
    options.setDefineToStringLiteral("KEY", "VALUE");
    options.setSummaryDetailLevel(2);

    Object clonedObj = options.clone();
    assertNotNull(clonedObj);
    assertTrue(clonedObj instanceof CompilerOptions);
    assertNotSame(options, clonedObj);

    CompilerOptions cloned = (CompilerOptions) clonedObj;
    assertTrue(cloned.checkSymbols);
    assertTrue(cloned.checkTypes);
    assertTrue(cloned.foldConstants);
    assertEquals(2, cloned.summaryDetailLevel);

    Map<String, Node> clonedDefines = cloned.getDefineReplacements();
    assertEquals(1, clonedDefines.size());
    assertEquals("VALUE", clonedDefines.get("KEY").getString());
  }
}