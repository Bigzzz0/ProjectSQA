/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.mozilla.javascript.CodeGenerator
 *
 * Core Decision Branches & Conditions Targeted:
 * 1. compile():
 *    - returnFunction = true vs false
 *    - scriptOrFn resolution (top-level vs first function node)
 *    - Strict mode propagation
 * 2. generateFunctionICode():
 *    - Function types (FUNCTION_STATEMENT, FUNCTION_EXPRESSION, FUNCTION_EXPRESSION_STATEMENT)
 *    - Function naming (null vs non-null)
 *    - Activation frame requirements (itsNeedsActivation = true/false)
 *    - Generator functions (isGenerator() -> Icode_GENERATOR + lineno)
 * 3. generateICodeFromTree() / generateNestedFunctions() / generateRegExpLiterals():
 *    - Nested functions array population (count == 0 vs count > 0)
 *    - RegExp literals compilation via ScriptRuntime RegExpProxy
 *    - String table compaction & iterators (strings.size() == 0 vs > 0, > 4, > 0xFF)
 *    - Double table compaction (0 entries vs entries present)
 *    - Exception table compaction & resizing
 *    - itsMaxVars, itsMaxLocals, itsMaxStack calculation
 * 4. visitStatement():
 *    - SWITCH statement: multi-case evaluation, DUP, SHEQ, IFEQ_POP, POP
 *    - TRY / CATCH / FINALLY: exception table offsets, local scope allocation, finally register
 *    - THROW / RETHROW
 *    - RETURN: generator end vs value return vs undefined return (Icode_RETUNDEF)
 *    - ENTERWITH / LEAVEWITH
 *    - LOCAL_BLOCK & LOCAL_CLEAR
 *    - ENUM_INIT_KEYS / VALUES / ARRAY
 * 5. visitExpression():
 *    - Logic operators: AND, OR (short-circuit jumps)
 *    - Ternary HOOK (? :)
 *    - Property ops: GETPROP, GETPROPNOWARN, SETPROP, SETPROP_OP (compound assignment)
 *    - Element ops: GETELEM, SETELEM, SETELEM_OP
 *    - Calls: CALL, NEW, REF_CALL, tail-call optimization vs try-block inhibition
 *    - Numbers: 0.0, -0.0 (1.0/num < 0 check), 1.0, short int, general int, floating-point doubles
 *    - Variable operations: GETVAR, SETVAR, SETCONSTVAR (indices < 128 vs >= 128)
 *    - Unary ops: VOID, POS, NEG, NOT, BITNOT, TYPEOF, TYPEOFNAME
 *    - Literals: ARRAYLIT (dense vs sparse with SKIP_INDEXES), OBJECTLIT (getters, setters, standard props)
 *    - XML / E4X: ESCXMLATTR, ESCXMLTEXT, DEFAULTNAMESPACE, DOTQUERY
 *    - Array Comprehension, Yield expressions
 * 6. Edge cases & Defensive Code Paths:
 *    - badTree() on illegal AST node types