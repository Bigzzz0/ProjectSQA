package com.google.javascript.jscomp;

/* [Branch & Defect Analysis Matrix]
 * Target Class: com.google.javascript.jscomp.TypeCheck
 *
 * Focus Branches & Decision Points:
 * 1. visitParameterList() / visitCall() / visitNew() -> WRONG_ARGUMENT_COUNT:
 *    - minArgs > numArgs || maxArgs < numArgs
 *    - Known Defect (testFunctionArguments16): Function call argument count validation when arguments
 *      are missing or when parameter types/counts mismatch (optional vs var_args vs mandatory).
 * 2. Binary Operators (Token.BITNOT, LSH, RSH, URSH, DIV, MOD, MUL, SUB, BITAND, BITOR, BITXOR, ADD):
 *    - Operator validations with Int32, Uint32, Bitwiseable contexts.
 * 3. Comparisons & Equality (Token.EQ, NE, SHEQ, SHNE, LT, LE, GT, GE):
 *    - Deterministic test warnings (DETERMINISTIC_TEST, DETERMINISTIC_TEST_NO_RESULT).
 *    - Restrict by not null/undefined and shallow equality checks.
 * 4. Property Access & Assignments (Token.GETPROP, Token.ASSIGN):
 *    - Accessing properties on undefined/void, non-existent properties, enum element validation.
 *    - Prototype assignments, constructor prototype override with non-object.
 *    - Interface assignments: method bodies must be empty, only abstractMethod or empty function allowed.
 * 5. Method/Function Overrides & Interfaces:
 *    - Superclass property hidden without @override, interface property hidden without @override.
 *    - Type mismatches in overridden methods (HIDDEN_SUPERCLASS_PROPERTY_MISMATCH).
 *    - Unknown @override when no superclass or interface defines the member.
 * 6. Constructors & Callables (Token.NEW, Token.CALL):
 *    - Instantiating non-constructor (NOT_A_CONSTRUCTOR).
 *    - Calling non-callable expression (NOT_CALLABLE).
 *    - Calling non-native constructor without "new" (CONSTRUCTOR_NOT_CALLABLE).
 * 7. Type Accounting (getTypedPercent):
 *    - Null count, unknown count, typed count, percentage computation boundary (0 total vs positive total).
 * 8. @notypecheck annotation handling on scripts, blocks, functions, vars.
 */

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;

public class TypeCheckGeminiTest {

  private Compiler compiler;
  private JSTypeRegistry registry;
  private Scope topScope;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.checkTypes = true;
    compiler.initOptions(options);
    registry = compiler.getTypeRegistry();
  }

  private TypeCheck check(String js) {
    return check("", js, CheckLevel.WARNING, CheckLevel.OFF);
  }

  private TypeCheck check(String js, CheckLevel missingOverride, CheckLevel reportUnknown) {
    return check("", js, missingOverride, reportUnknown);
  }

  private TypeCheck check(String externs, String js, CheckLevel missingOverride, CheckLevel reportUnknown) {
    Node externsNode = compiler.parseTestCode(externs);
    Node jsNode = compiler.parseTestCode(js);
    Node root = new Node(Token.BLOCK, externsNode, jsNode);

    ClosureReverseAbstractInterpreter interpreter =
        new ClosureReverseAbstractInterpreter(
            compiler.getCodingConvention(), registry);

    TypeCheck tc = new TypeCheck(
        compiler, interpreter, registry, missingOverride, reportUnknown);
    topScope = tc.processForTesting(externsNode, jsNode);
    return tc;
  }

  private boolean hasWarning(DiagnosticType type) {
    for (JSError error : compiler.getWarnings()) {
      if (error.getType() == type)