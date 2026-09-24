/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: SemanticReverseAbstractInterpreter
 *
 * 1. typeof Operator Detection & Refinement Branch:
 *    - Token.EQ, NE, SHEQ, SHNE, CASE with left or right being typeof and other being string.
 *    - Condition outcome true vs false with positive (EQ/SHEQ/CASE) vs negative (NE/SHNE).
 *    - Operand refinable vs non-refinable in blindScope.
 *
 * 2. Logical Operators (Short-circuiting vs Non-short-circuiting):
 *    - Token.AND:
 *      * outcome = true  -> caseAndOrNotShortCircuiting(condition=true)
 *      * outcome = false -> caseAndOrMaybeShortCircuiting(condition=true)
 *    - Token.OR:
 *      * outcome = false -> caseAndOrNotShortCircuiting(condition=false)
 *      * outcome = true  -> caseAndOrMaybeShortCircuiting(condition=false)
 *    - Branch in caseAndOrNotShortCircuiting when restrictedLeftType == null.
 *    - Branch in caseAndOrMaybeShortCircuiting when slots do not match or cannot be resolved.
 *
 * 3. Equality and Relational Operators:
 *    - EQ, NE, SHEQ, SHNE with outcome true and false (inversion mappings).
 *    - Relational: LE, LT, GE, GT with outcome = true (INEQ) and outcome = false (no-op).
 *    - In INEQ: stripping undefined from left and right types.
 *
 * 4. Token.INSTANCEOF:
 *    - Left operand refinable vs unrefinable.
 *    - Right operand function type vs unknown/non-function object type.
 *    - RestrictByTrueInstanceOfResultVisitor: top type, unknown type, object type, union type, function type.
 *    - RestrictByFalseInstanceOfResultVisitor: object type (subtype vs non-subtype), union type.
 *
 * 5.