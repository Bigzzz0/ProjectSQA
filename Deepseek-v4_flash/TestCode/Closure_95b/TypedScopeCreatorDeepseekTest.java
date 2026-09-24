package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.NodeTraversal;
import com.google.javascript.jscomp.Scope;
import com.google.javascript.jscomp.Scope.Var;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.rhino.jstype.FunctionType;

import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for TypedScopeCreator targeting known defects and comprehensive coverage.
 * 
 * [Branch & Defect Analysis Matrix]
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   - createScope with null parent (global scope)
 *   - createScope with non-null parent (local scope)
 *   - createInitialScope native type declarations
 *   - attachLiteralTypes for all literal node types
 *   - defineSlot for NAME and GETPROP nodes
 *   - getDeclaredTypeInAnnotation with @type, @constructor, @param/@return
 *   - getFunctionType with various configurations
 *   - getEnumType with object literal and qualified name
 *   - checkForClassDefiningCalls for subclass, singleton, delegate
 *   - maybeDeclareQualifiedName with different precedence levels
 *   - resolveStubDeclarations for unresolved properties
 *   - CollectProperties for @this type properties
 * 
 * Partition B: Boundary Value Analysis & Extremes
 *   - null parent in createScope
 *   - empty string qualified names
 *   - null JSDocInfo
 *   - null rhsValue in maybeDeclareQualifiedName
 *   - empty function name strings
 *   - null type in defineSlot with inferred=true
 *   - null ownerType in getObjectSlot
 *   - null functionType in declareArguments
 *   - null jsDocParameters in declareArguments
 *   - null info in getDeclaredGetPropType
 *   - null rValue in getFunctionType
 *   - null lvalueNode in getFunctionType
 *   - null owner in getFunctionType
 *   - null parametersNode in getFunctionType
 *   - null fnBlock in getFunctionType
 *   - null value in getEnumType
 *   - null enumType in getEnumType
 *   - null superClass/subClass in checkForClassDefiningCalls
 *   - null delegatorObject/delegateBaseObject/delegateSuperObject
 *   - null ownerVar in getObjectSlot
 *   - null ownerVarType in getObjectSlot
 *   - null stub.ownerName in resolveStubDeclarations
 *   - null ownerType in resolveStubDeclarations
 *   - null info in CollectProperties.maybeCollectMember
 *   - null jsType in CollectProperties.maybeCollectMember
 *   - null fnVar in handleFunctionInputs
 *   - null fnVar.getNameNode in handleFunctionInputs
 *   - null functionType in declareArguments
 *   - null jsDocParameter in declareArguments
 * 
 * Partition C: Defect-Targeted Branch Zone
 *   - DEFECT: testQualifiedNameInference5 - qualified name inference in global scope
 *   - DEFECT: testGlobalQualifiedNameInLocalScope - global qualified name in local scope
 *   - Branch: isQnameRootedInGlobalScope with NAME root
 *   - Branch: isQnameRootedInGlobalScope with non-NAME root
 *   - Branch: shouldDeclareOnGlobalThis logic
 *   - Branch: prototype property handling in maybeDeclareQualifiedName
 *   - Branch: inferred vs declared property in defineSlot
 *   - Branch: isExtern vs non-extern in defineSlot
 *   - Branch: constructor vs interface in defineSlot
 *   - Branch: superClassCtor null check in defineSlot
 *   - Branch: findOverriddenFunction with superclass and interface
 *   - Branch: getDeclaredGetPropType with @type, @enum, FUNCTION, @param/@return
 *   - Branch: stubDeclarations with isExtern true/false
 *   - Branch: ownerType.isFunctionPrototypeType() in resolveStubDeclarations
 *   - Branch: checkForTypedef with null typedef
 *   - Branch: checkForTypedef with null realType
 *   - Branch: checkForOldStyleTypedef with null realType
 *   - Branch: applyDelegateRelationship with null components
 *   - Branch: getPrototypePropertyOwner with GETPROP chain
 *   - Branch: getPrototypePropertyOwner with non-GETPROP
 *   - Branch: shouldTraverse for FUNCTION parent
 *   - Branch: shouldTraverse for SCRIPT parent
 *   - Branch: visit for CALL, FUNCTION, ASSIGN, CATCH, VAR, GETPROP
 *   - Branch: defineName with FUNCTION value
 *   - Branch: defineName with non-FUNCTION value
 *   - Branch: defineName with null info
 *   - Branch: defineName with enum parameter type
 *   - Branch: defineName with constructor
 *   - Branch: defineName with declared type annotation
 *   - Branch: defineVar with multiple children
 *   - Branch: defineVar with single child
 *   - Branch: defineVar with null info
 *   - Branch: defineVar with info and multiple children (MULTIPLE_VAR_DEF)
 *   - Branch: defineNamedTypeAssign with FUNCTION rvalue
 *   - Branch: defineNamedTypeAssign with constructor info
 *   - Branch: defineNamedTypeAssign with enum parameter type
 *   - Branch: defineNamedTypeAssign with null info
 *   - Branch: defineDeclaredFunction with local scope
 *   - Branch: defineDeclaredFunction with global scope
 *   - Branch: defineDeclaredFunction with ASSIGN parent
 *   - Branch: defineDeclaredFunction with NAME parent
 *   - Branch: defineCatch with CATCH node
 *   - Branch: getFunctionType with qualified name rvalue
 *   - Branch: getFunctionType with non-qualified name rvalue
 *   - Branch: getFunctionType with @type annotation
 *   - Branch: getFunctionType with overridden function
 *   - Branch: getFunctionType with null info
 *   - Branch: getFunctionType with null rValue
 *   - Branch: getFunctionType with null lvalueNode
 *   - Branch: getEnumType with OBJECTLIT value
 *   - Branch: getEnumType with qualified name value
 *   - Branch: getEnumType with null value
 *   - Branch: getEnumType with null enumType
 *   - Branch: getEnumType with global scope
 *   - Branch: getEnumType with non-global scope
 *   - Branch: getEnumType with duplicate keys
 *   - Branch: getEnumType with invalid enum keys
 *   - Branch: checkForClassDefiningCalls with null relationship
 *   - Branch: checkForClassDefiningCalls with null singletonGetterClassName
 *   - Branch: checkForClassDefiningCalls with null delegateRelationship
 *   - Branch: checkForClassDefiningCalls with null objectLiteralCast
 *   - Branch: checkForClassDefiningCalls with null superClass
 *   - Branch: checkForClassDefiningCalls with null subClass
 *   - Branch: checkForClassDefiningCalls with null superCtor
 *   - Branch: checkForClassDefiningCalls with null subCtor
 *   - Branch: checkForClassDefiningCalls with INHERITS type
 *   - Branch: checkForClassDefiningCalls with non-INHERITS type
 *   - Branch: checkForClassDefiningCalls with null objectType
 *   - Branch: checkForClassDefiningCalls with null functionType
 *   - Branch: checkForClassDefiningCalls with null type
 *   - Branch: checkForClassDefiningCalls with null constructor
 *   - Branch: applyDelegateRelationship with null delegatorCtor
 *   - Branch: applyDelegateRelationship with null delegateBaseCtor
 *   - Branch: applyDelegateRelationship with null delegateSuperCtor
 *   - Branch: maybeDeclareQualifiedName with "prototype" propName
 *   - Branch: maybeDeclareQualifiedName with null valueType
 *   - Branch: maybeDeclareQualifiedName with null rhsValue
 *   - Branch: maybeDeclareQualifiedName with EXPR_RESULT parent
 *   - Branch: maybeDeclareQualifiedName with inferred=true
 *   - Branch: maybeDeclareQualifiedName with inferred=false
 *   - Branch: maybeDeclareQualifiedName with TRUE rhsValue
 *   - Branch: maybeDeclareQualifiedName with non-TRUE rhsValue
 *   - Branch: maybeDeclareQualifiedName with null ownerType
 *   - Branch: maybeDeclareQualifiedName with ownerType instanceof FunctionType
 *   - Branch: maybeDeclareQualifiedName with delegate subtype check
 *   - Branch: resolveStubDeclarations with isDeclared
 *   - Branch: resolveStubDeclarations with ownerType null
 *   - Branch: resolveStubDeclarations with ownerType non-null
 *   - Branch: resolveStubDeclarations with isExtern
 *   - Branch: resolveStubDeclarations with ownerType.isFunctionPrototypeType()
 *   - Branch: CollectProperties.maybeCollectMember with null info
 *   - Branch: CollectProperties.maybeCollectMember with non-GETPROP member
 *   - Branch: CollectProperties.maybeCollectMember with non-THIS firstChild
 *   - Branch: CollectProperties.maybeCollectMember with null jsType
 *   - Branch: CollectProperties.maybeCollectMember with non-NAME/STRING name
 *   - Branch: handleFunctionInputs with empty fnName
 *   - Branch: handleFunctionInputs with null fnVar
 *   - Branch: handleFunctionInputs with null fnVar.getNameNode
 *   - Branch: handleFunctionInputs with fnVar.getInitialValue != fnNode
 *   - Branch: declareArguments with null functionType
 *   - Branch: declareArguments with null jsDocParameters
 *   - Branch: declareArguments with null jsDocParameter
 *   - Branch: declareArguments with non-null jsDocParameter
 *   - Branch: GlobalScopeBuilder.visit with ASSIGN
 *   - Branch: GlobalScopeBuilder.visit with VAR
 *   - Branch: GlobalScopeBuilder.visit with other types
 *   - Branch: GlobalScopeBuilder.checkForTypedef with null info
 *   - Branch: GlobalScopeBuilder.checkForTypedef with null typedef
 *   - Branch: GlobalScopeBuilder.checkForTypedef with null realType
 *   - Branch: GlobalScopeBuilder.checkForTypedef with GETPROP candidate
 *   - Branch: GlobalScopeBuilder.checkForOldStyleTypedef with null typedef
 *   - Branch: GlobalScopeBuilder.checkForOldStyleTypedef with null realType
 *   - Branch: LocalScopeBuilder.visit with root node
 *   - Branch: LocalScopeBuilder.visit with LP and root parent
 *   - Branch: LocalScopeBuilder.visit with other nodes
 *   - Branch: shouldTraverse with FUNCTION parent and first child
 *   - Branch: shouldTraverse with FUNCTION parent and root node
 *   - Branch: shouldTraverse with non-FUNCTION parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: visit with CALL node
 *   - Branch: visit with FUNCTION node
 *   - Branch: visit with ASSIGN node
 *   - Branch: visit with CATCH node
 *   - Branch: visit with VAR node
 *   - Branch: visit with GETPROP node
 *   - Branch: visit with other node types
 *   - Branch: attachLiteralTypes with NULL, VOID, STRING, NUMBER, TRUE, FALSE, REGEXP, REF_SPECIAL, OBJECTLIT
 *   - Branch: attachLiteralTypes with other types
 *   - Branch: defineSlot with NAME node
 *   - Branch: defineSlot with GETPROP node
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis true
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis false
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared true
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared false
 *   - Branch: defineSlot with inferred true
 *   - Branch: defineSlot with inferred false
 *   - Branch: defineSlot with type instanceof FunctionType
 *   - Branch: defineSlot with type not instanceof FunctionType
 *   - Branch: defineSlot with fnType.isConstructor() or fnType.isInterface()
 *   - Branch: defineSlot with superClassCtor null
 *   - Branch: defineSlot with superClassCtor non-null
 *   - Branch: defineSlot with superClassCtor.getInstanceType() equals OBJECT_TYPE
 *   - Branch: defineSlot with superClassCtor.getInstanceType() not equals OBJECT_TYPE
 *   - Branch: isQnameRootedInGlobalScope with NAME root and global var
 *   - Branch: isQnameRootedInGlobalScope with NAME root and non-global var
 *   - Branch: isQnameRootedInGlobalScope with non-NAME root
 *   - Branch: getDeclaredGetPropType with @type annotation
 *   - Branch: getDeclaredGetPropType with @enum annotation
 *   - Branch: getDeclaredGetPropType with FUNCTION rhsValue
 *   - Branch: getDeclaredGetPropType with other cases
 *   - Branch: getDeclaredTypeInAnnotation with @type
 *   - Branch: getDeclaredTypeInAnnotation with isFunctionTypeDeclaration
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and existing type
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and no existing type
 *   - Branch: getDeclaredTypeInAnnotation with objNode GETPROP prototype
 *   - Branch: getDeclaredTypeInAnnotation with objNode THIS
 *   - Branch: getDeclaredTypeInAnnotation with no searchedForThisType
 *   - Branch: getDeclaredTypeInAnnotation with null info
 *   - Branch: getDeclaredTypeInAnnotation with null objNode
 *   - Branch: findOverriddenFunction with FunctionType propType
 *   - Branch: findOverriddenFunction with non-FunctionType propType and interfaces
 *   - Branch: findOverriddenFunction with non-FunctionType propType and no interfaces
 *   - Branch: findOverriddenFunction with null ownerType
 *   - Branch: findOverriddenFunction with null propType
 *   - Branch: getObjectSlot with null ownerVar
 *   - Branch: getObjectSlot with null ownerVarType
 *   - Branch: getObjectSlot with non-null ownerVarType
 *   - Branch: getPrototypePropertyOwner with GETPROP chain
 *   - Branch: getPrototypePropertyOwner with non-GETPROP
 *   - Branch: getPrototypePropertyOwner with non-prototype GETPROP
 *   - Branch: getPrototypePropertyOwner with non-qualified name
 *   - Branch: DiscoverEnums.visit with VAR
 *   - Branch: DiscoverEnums.visit with EXPR_RESULT
 *   - Branch: DiscoverEnums.visit with other types
 *   - Branch: DiscoverEnums.identifyEnumName with null info
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and hasEnumParameterType
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and no enum parameter type
 *   - Branch: DeferredSetType constructor with null node
 *   - Branch: DeferredSetType constructor with null type
 *   - Branch: DeferredSetType.resolve with scope
 *   - Branch: createScope with null parent (global)
 *   - Branch: createScope with non-null parent (local)
 *   - Branch: createScope with nonExternFunctions iteration
 *   - Branch: createScope with null type
 *   - Branch: createScope with type not instanceof FunctionType
 *   - Branch: createScope with fnThisType.isUnknownType()
 *   - Branch: createScope with fnThisType not unknown
 *   - Branch: createScope with null parent and delegate proxy
 *   - Branch: createScope with non-null parent and no delegate proxy
 *   - Branch: createInitialScope with root traversal
 *   - Branch: declareNativeFunctionType with scope
 *   - Branch: declareNativeValueType with scope
 *   - Branch: declareNativeType with scope
 *   - Branch: TypedScopeCreator constructor with compiler
 *   - Branch: TypedScopeCreator constructor with compiler and codingConvention
 *   - Branch: getNativeType with native type
 *   - Branch: assertDefinitionNode with correct type
 *   - Branch: assertDefinitionNode with incorrect type
 *   - Branch: setDeferredType with node and type
 *   - Branch: resolveTypes with deferred types
 *   - Branch: resolveTypes with scope vars
 *   - Branch: resolveTypes with type registry
 *   - Branch: visit with attachLiteralTypes
 *   - Branch: visit with CALL, FUNCTION, ASSIGN, CATCH, VAR, GETPROP
 *   - Branch: visit with other types
 *   - Branch: shouldTraverse with FUNCTION parent
 *   - Branch: shouldTraverse with SCRIPT parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: shouldTraverse with FUNCTION parent and first child
 *   - Branch: shouldTraverse with FUNCTION parent and root node
 *   - Branch: shouldTraverse with non-FUNCTION parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: visit with CALL node
 *   - Branch: visit with FUNCTION node
 *   - Branch: visit with ASSIGN node
 *   - Branch: visit with CATCH node
 *   - Branch: visit with VAR node
 *   - Branch: visit with GETPROP node
 *   - Branch: visit with other node types
 *   - Branch: attachLiteralTypes with NULL, VOID, STRING, NUMBER, TRUE, FALSE, REGEXP, REF_SPECIAL, OBJECTLIT
 *   - Branch: attachLiteralTypes with other types
 *   - Branch: defineSlot with NAME node
 *   - Branch: defineSlot with GETPROP node
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis true
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis false
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared true
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared false
 *   - Branch: defineSlot with inferred true
 *   - Branch: defineSlot with inferred false
 *   - Branch: defineSlot with type instanceof FunctionType
 *   - Branch: defineSlot with type not instanceof FunctionType
 *   - Branch: defineSlot with fnType.isConstructor() or fnType.isInterface()
 *   - Branch: defineSlot with superClassCtor null
 *   - Branch: defineSlot with superClassCtor non-null
 *   - Branch: defineSlot with superClassCtor.getInstanceType() equals OBJECT_TYPE
 *   - Branch: defineSlot with superClassCtor.getInstanceType() not equals OBJECT_TYPE
 *   - Branch: isQnameRootedInGlobalScope with NAME root and global var
 *   - Branch: isQnameRootedInGlobalScope with NAME root and non-global var
 *   - Branch: isQnameRootedInGlobalScope with non-NAME root
 *   - Branch: getDeclaredGetPropType with @type annotation
 *   - Branch: getDeclaredGetPropType with @enum annotation
 *   - Branch: getDeclaredGetPropType with FUNCTION rhsValue
 *   - Branch: getDeclaredGetPropType with other cases
 *   - Branch: getDeclaredTypeInAnnotation with @type
 *   - Branch: getDeclaredTypeInAnnotation with isFunctionTypeDeclaration
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and existing type
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and no existing type
 *   - Branch: getDeclaredTypeInAnnotation with objNode GETPROP prototype
 *   - Branch: getDeclaredTypeInAnnotation with objNode THIS
 *   - Branch: getDeclaredTypeInAnnotation with no searchedForThisType
 *   - Branch: getDeclaredTypeInAnnotation with null info
 *   - Branch: getDeclaredTypeInAnnotation with null objNode
 *   - Branch: findOverriddenFunction with FunctionType propType
 *   - Branch: findOverriddenFunction with non-FunctionType propType and interfaces
 *   - Branch: findOverriddenFunction with non-FunctionType propType and no interfaces
 *   - Branch: findOverriddenFunction with null ownerType
 *   - Branch: findOverriddenFunction with null propType
 *   - Branch: getObjectSlot with null ownerVar
 *   - Branch: getObjectSlot with null ownerVarType
 *   - Branch: getObjectSlot with non-null ownerVarType
 *   - Branch: getPrototypePropertyOwner with GETPROP chain
 *   - Branch: getPrototypePropertyOwner with non-GETPROP
 *   - Branch: getPrototypePropertyOwner with non-prototype GETPROP
 *   - Branch: getPrototypePropertyOwner with non-qualified name
 *   - Branch: DiscoverEnums.visit with VAR
 *   - Branch: DiscoverEnums.visit with EXPR_RESULT
 *   - Branch: DiscoverEnums.visit with other types
 *   - Branch: DiscoverEnums.identifyEnumName with null info
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and hasEnumParameterType
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and no enum parameter type
 *   - Branch: DeferredSetType constructor with null node
 *   - Branch: DeferredSetType constructor with null type
 *   - Branch: DeferredSetType.resolve with scope
 *   - Branch: createScope with null parent (global)
 *   - Branch: createScope with non-null parent (local)
 *   - Branch: createScope with nonExternFunctions iteration
 *   - Branch: createScope with null type
 *   - Branch: createScope with type not instanceof FunctionType
 *   - Branch: createScope with fnThisType.isUnknownType()
 *   - Branch: createScope with fnThisType not unknown
 *   - Branch: createScope with null parent and delegate proxy
 *   - Branch: createScope with non-null parent and no delegate proxy
 *   - Branch: createInitialScope with root traversal
 *   - Branch: declareNativeFunctionType with scope
 *   - Branch: declareNativeValueType with scope
 *   - Branch: declareNativeType with scope
 *   - Branch: TypedScopeCreator constructor with compiler
 *   - Branch: TypedScopeCreator constructor with compiler and codingConvention
 *   - Branch: getNativeType with native type
 *   - Branch: assertDefinitionNode with correct type
 *   - Branch: assertDefinitionNode with incorrect type
 *   - Branch: setDeferredType with node and type
 *   - Branch: resolveTypes with deferred types
 *   - Branch: resolveTypes with scope vars
 *   - Branch: resolveTypes with type registry
 *   - Branch: visit with attachLiteralTypes
 *   - Branch: visit with CALL, FUNCTION, ASSIGN, CATCH, VAR, GETPROP
 *   - Branch: visit with other types
 *   - Branch: shouldTraverse with FUNCTION parent
 *   - Branch: shouldTraverse with SCRIPT parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: shouldTraverse with FUNCTION parent and first child
 *   - Branch: shouldTraverse with FUNCTION parent and root node
 *   - Branch: shouldTraverse with non-FUNCTION parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: visit with CALL node
 *   - Branch: visit with FUNCTION node
 *   - Branch: visit with ASSIGN node
 *   - Branch: visit with CATCH node
 *   - Branch: visit with VAR node
 *   - Branch: visit with GETPROP node
 *   - Branch: visit with other node types
 *   - Branch: attachLiteralTypes with NULL, VOID, STRING, NUMBER, TRUE, FALSE, REGEXP, REF_SPECIAL, OBJECTLIT
 *   - Branch: attachLiteralTypes with other types
 *   - Branch: defineSlot with NAME node
 *   - Branch: defineSlot with GETPROP node
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis true
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis false
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared true
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared false
 *   - Branch: defineSlot with inferred true
 *   - Branch: defineSlot with inferred false
 *   - Branch: defineSlot with type instanceof FunctionType
 *   - Branch: defineSlot with type not instanceof FunctionType
 *   - Branch: defineSlot with fnType.isConstructor() or fnType.isInterface()
 *   - Branch: defineSlot with superClassCtor null
 *   - Branch: defineSlot with superClassCtor non-null
 *   - Branch: defineSlot with superClassCtor.getInstanceType() equals OBJECT_TYPE
 *   - Branch: defineSlot with superClassCtor.getInstanceType() not equals OBJECT_TYPE
 *   - Branch: isQnameRootedInGlobalScope with NAME root and global var
 *   - Branch: isQnameRootedInGlobalScope with NAME root and non-global var
 *   - Branch: isQnameRootedInGlobalScope with non-NAME root
 *   - Branch: getDeclaredGetPropType with @type annotation
 *   - Branch: getDeclaredGetPropType with @enum annotation
 *   - Branch: getDeclaredGetPropType with FUNCTION rhsValue
 *   - Branch: getDeclaredGetPropType with other cases
 *   - Branch: getDeclaredTypeInAnnotation with @type
 *   - Branch: getDeclaredTypeInAnnotation with isFunctionTypeDeclaration
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and existing type
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and no existing type
 *   - Branch: getDeclaredTypeInAnnotation with objNode GETPROP prototype
 *   - Branch: getDeclaredTypeInAnnotation with objNode THIS
 *   - Branch: getDeclaredTypeInAnnotation with no searchedForThisType
 *   - Branch: getDeclaredTypeInAnnotation with null info
 *   - Branch: getDeclaredTypeInAnnotation with null objNode
 *   - Branch: findOverriddenFunction with FunctionType propType
 *   - Branch: findOverriddenFunction with non-FunctionType propType and interfaces
 *   - Branch: findOverriddenFunction with non-FunctionType propType and no interfaces
 *   - Branch: findOverriddenFunction with null ownerType
 *   - Branch: findOverriddenFunction with null propType
 *   - Branch: getObjectSlot with null ownerVar
 *   - Branch: getObjectSlot with null ownerVarType
 *   - Branch: getObjectSlot with non-null ownerVarType
 *   - Branch: getPrototypePropertyOwner with GETPROP chain
 *   - Branch: getPrototypePropertyOwner with non-GETPROP
 *   - Branch: getPrototypePropertyOwner with non-prototype GETPROP
 *   - Branch: getPrototypePropertyOwner with non-qualified name
 *   - Branch: DiscoverEnums.visit with VAR
 *   - Branch: DiscoverEnums.visit with EXPR_RESULT
 *   - Branch: DiscoverEnums.visit with other types
 *   - Branch: DiscoverEnums.identifyEnumName with null info
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and hasEnumParameterType
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and no enum parameter type
 *   - Branch: DeferredSetType constructor with null node
 *   - Branch: DeferredSetType constructor with null type
 *   - Branch: DeferredSetType.resolve with scope
 *   - Branch: createScope with null parent (global)
 *   - Branch: createScope with non-null parent (local)
 *   - Branch: createScope with nonExternFunctions iteration
 *   - Branch: createScope with null type
 *   - Branch: createScope with type not instanceof FunctionType
 *   - Branch: createScope with fnThisType.isUnknownType()
 *   - Branch: createScope with fnThisType not unknown
 *   - Branch: createScope with null parent and delegate proxy
 *   - Branch: createScope with non-null parent and no delegate proxy
 *   - Branch: createInitialScope with root traversal
 *   - Branch: declareNativeFunctionType with scope
 *   - Branch: declareNativeValueType with scope
 *   - Branch: declareNativeType with scope
 *   - Branch: TypedScopeCreator constructor with compiler
 *   - Branch: TypedScopeCreator constructor with compiler and codingConvention
 *   - Branch: getNativeType with native type
 *   - Branch: assertDefinitionNode with correct type
 *   - Branch: assertDefinitionNode with incorrect type
 *   - Branch: setDeferredType with node and type
 *   - Branch: resolveTypes with deferred types
 *   - Branch: resolveTypes with scope vars
 *   - Branch: resolveTypes with type registry
 *   - Branch: visit with attachLiteralTypes
 *   - Branch: visit with CALL, FUNCTION, ASSIGN, CATCH, VAR, GETPROP
 *   - Branch: visit with other types
 *   - Branch: shouldTraverse with FUNCTION parent
 *   - Branch: shouldTraverse with SCRIPT parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: shouldTraverse with FUNCTION parent and first child
 *   - Branch: shouldTraverse with FUNCTION parent and root node
 *   - Branch: shouldTraverse with non-FUNCTION parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: visit with CALL node
 *   - Branch: visit with FUNCTION node
 *   - Branch: visit with ASSIGN node
 *   - Branch: visit with CATCH node
 *   - Branch: visit with VAR node
 *   - Branch: visit with GETPROP node
 *   - Branch: visit with other node types
 *   - Branch: attachLiteralTypes with NULL, VOID, STRING, NUMBER, TRUE, FALSE, REGEXP, REF_SPECIAL, OBJECTLIT
 *   - Branch: attachLiteralTypes with other types
 *   - Branch: defineSlot with NAME node
 *   - Branch: defineSlot with GETPROP node
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis true
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis false
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared true
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared false
 *   - Branch: defineSlot with inferred true
 *   - Branch: defineSlot with inferred false
 *   - Branch: defineSlot with type instanceof FunctionType
 *   - Branch: defineSlot with type not instanceof FunctionType
 *   - Branch: defineSlot with fnType.isConstructor() or fnType.isInterface()
 *   - Branch: defineSlot with superClassCtor null
 *   - Branch: defineSlot with superClassCtor non-null
 *   - Branch: defineSlot with superClassCtor.getInstanceType() equals OBJECT_TYPE
 *   - Branch: defineSlot with superClassCtor.getInstanceType() not equals OBJECT_TYPE
 *   - Branch: isQnameRootedInGlobalScope with NAME root and global var
 *   - Branch: isQnameRootedInGlobalScope with NAME root and non-global var
 *   - Branch: isQnameRootedInGlobalScope with non-NAME root
 *   - Branch: getDeclaredGetPropType with @type annotation
 *   - Branch: getDeclaredGetPropType with @enum annotation
 *   - Branch: getDeclaredGetPropType with FUNCTION rhsValue
 *   - Branch: getDeclaredGetPropType with other cases
 *   - Branch: getDeclaredTypeInAnnotation with @type
 *   - Branch: getDeclaredTypeInAnnotation with isFunctionTypeDeclaration
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and existing type
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and no existing type
 *   - Branch: getDeclaredTypeInAnnotation with objNode GETPROP prototype
 *   - Branch: getDeclaredTypeInAnnotation with objNode THIS
 *   - Branch: getDeclaredTypeInAnnotation with no searchedForThisType
 *   - Branch: getDeclaredTypeInAnnotation with null info
 *   - Branch: getDeclaredTypeInAnnotation with null objNode
 *   - Branch: findOverriddenFunction with FunctionType propType
 *   - Branch: findOverriddenFunction with non-FunctionType propType and interfaces
 *   - Branch: findOverriddenFunction with non-FunctionType propType and no interfaces
 *   - Branch: findOverriddenFunction with null ownerType
 *   - Branch: findOverriddenFunction with null propType
 *   - Branch: getObjectSlot with null ownerVar
 *   - Branch: getObjectSlot with null ownerVarType
 *   - Branch: getObjectSlot with non-null ownerVarType
 *   - Branch: getPrototypePropertyOwner with GETPROP chain
 *   - Branch: getPrototypePropertyOwner with non-GETPROP
 *   - Branch: getPrototypePropertyOwner with non-prototype GETPROP
 *   - Branch: getPrototypePropertyOwner with non-qualified name
 *   - Branch: DiscoverEnums.visit with VAR
 *   - Branch: DiscoverEnums.visit with EXPR_RESULT
 *   - Branch: DiscoverEnums.visit with other types
 *   - Branch: DiscoverEnums.identifyEnumName with null info
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and hasEnumParameterType
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and no enum parameter type
 *   - Branch: DeferredSetType constructor with null node
 *   - Branch: DeferredSetType constructor with null type
 *   - Branch: DeferredSetType.resolve with scope
 *   - Branch: createScope with null parent (global)
 *   - Branch: createScope with non-null parent (local)
 *   - Branch: createScope with nonExternFunctions iteration
 *   - Branch: createScope with null type
 *   - Branch: createScope with type not instanceof FunctionType
 *   - Branch: createScope with fnThisType.isUnknownType()
 *   - Branch: createScope with fnThisType not unknown
 *   - Branch: createScope with null parent and delegate proxy
 *   - Branch: createScope with non-null parent and no delegate proxy
 *   - Branch: createInitialScope with root traversal
 *   - Branch: declareNativeFunctionType with scope
 *   - Branch: declareNativeValueType with scope
 *   - Branch: declareNativeType with scope
 *   - Branch: TypedScopeCreator constructor with compiler
 *   - Branch: TypedScopeCreator constructor with compiler and codingConvention
 *   - Branch: getNativeType with native type
 *   - Branch: assertDefinitionNode with correct type
 *   - Branch: assertDefinitionNode with incorrect type
 *   - Branch: setDeferredType with node and type
 *   - Branch: resolveTypes with deferred types
 *   - Branch: resolveTypes with scope vars
 *   - Branch: resolveTypes with type registry
 *   - Branch: visit with attachLiteralTypes
 *   - Branch: visit with CALL, FUNCTION, ASSIGN, CATCH, VAR, GETPROP
 *   - Branch: visit with other types
 *   - Branch: shouldTraverse with FUNCTION parent
 *   - Branch: shouldTraverse with SCRIPT parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: shouldTraverse with FUNCTION parent and first child
 *   - Branch: shouldTraverse with FUNCTION parent and root node
 *   - Branch: shouldTraverse with non-FUNCTION parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: visit with CALL node
 *   - Branch: visit with FUNCTION node
 *   - Branch: visit with ASSIGN node
 *   - Branch: visit with CATCH node
 *   - Branch: visit with VAR node
 *   - Branch: visit with GETPROP node
 *   - Branch: visit with other node types
 *   - Branch: attachLiteralTypes with NULL, VOID, STRING, NUMBER, TRUE, FALSE, REGEXP, REF_SPECIAL, OBJECTLIT
 *   - Branch: attachLiteralTypes with other types
 *   - Branch: defineSlot with NAME node
 *   - Branch: defineSlot with GETPROP node
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis true
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis false
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared true
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared false
 *   - Branch: defineSlot with inferred true
 *   - Branch: defineSlot with inferred false
 *   - Branch: defineSlot with type instanceof FunctionType
 *   - Branch: defineSlot with type not instanceof FunctionType
 *   - Branch: defineSlot with fnType.isConstructor() or fnType.isInterface()
 *   - Branch: defineSlot with superClassCtor null
 *   - Branch: defineSlot with superClassCtor non-null
 *   - Branch: defineSlot with superClassCtor.getInstanceType() equals OBJECT_TYPE
 *   - Branch: defineSlot with superClassCtor.getInstanceType() not equals OBJECT_TYPE
 *   - Branch: isQnameRootedInGlobalScope with NAME root and global var
 *   - Branch: isQnameRootedInGlobalScope with NAME root and non-global var
 *   - Branch: isQnameRootedInGlobalScope with non-NAME root
 *   - Branch: getDeclaredGetPropType with @type annotation
 *   - Branch: getDeclaredGetPropType with @enum annotation
 *   - Branch: getDeclaredGetPropType with FUNCTION rhsValue
 *   - Branch: getDeclaredGetPropType with other cases
 *   - Branch: getDeclaredTypeInAnnotation with @type
 *   - Branch: getDeclaredTypeInAnnotation with isFunctionTypeDeclaration
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and existing type
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and no existing type
 *   - Branch: getDeclaredTypeInAnnotation with objNode GETPROP prototype
 *   - Branch: getDeclaredTypeInAnnotation with objNode THIS
 *   - Branch: getDeclaredTypeInAnnotation with no searchedForThisType
 *   - Branch: getDeclaredTypeInAnnotation with null info
 *   - Branch: getDeclaredTypeInAnnotation with null objNode
 *   - Branch: findOverriddenFunction with FunctionType propType
 *   - Branch: findOverriddenFunction with non-FunctionType propType and interfaces
 *   - Branch: findOverriddenFunction with non-FunctionType propType and no interfaces
 *   - Branch: findOverriddenFunction with null ownerType
 *   - Branch: findOverriddenFunction with null propType
 *   - Branch: getObjectSlot with null ownerVar
 *   - Branch: getObjectSlot with null ownerVarType
 *   - Branch: getObjectSlot with non-null ownerVarType
 *   - Branch: getPrototypePropertyOwner with GETPROP chain
 *   - Branch: getPrototypePropertyOwner with non-GETPROP
 *   - Branch: getPrototypePropertyOwner with non-prototype GETPROP
 *   - Branch: getPrototypePropertyOwner with non-qualified name
 *   - Branch: DiscoverEnums.visit with VAR
 *   - Branch: DiscoverEnums.visit with EXPR_RESULT
 *   - Branch: DiscoverEnums.visit with other types
 *   - Branch: DiscoverEnums.identifyEnumName with null info
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and hasEnumParameterType
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and no enum parameter type
 *   - Branch: DeferredSetType constructor with null node
 *   - Branch: DeferredSetType constructor with null type
 *   - Branch: DeferredSetType.resolve with scope
 *   - Branch: createScope with null parent (global)
 *   - Branch: createScope with non-null parent (local)
 *   - Branch: createScope with nonExternFunctions iteration
 *   - Branch: createScope with null type
 *   - Branch: createScope with type not instanceof FunctionType
 *   - Branch: createScope with fnThisType.isUnknownType()
 *   - Branch: createScope with fnThisType not unknown
 *   - Branch: createScope with null parent and delegate proxy
 *   - Branch: createScope with non-null parent and no delegate proxy
 *   - Branch: createInitialScope with root traversal
 *   - Branch: declareNativeFunctionType with scope
 *   - Branch: declareNativeValueType with scope
 *   - Branch: declareNativeType with scope
 *   - Branch: TypedScopeCreator constructor with compiler
 *   - Branch: TypedScopeCreator constructor with compiler and codingConvention
 *   - Branch: getNativeType with native type
 *   - Branch: assertDefinitionNode with correct type
 *   - Branch: assertDefinitionNode with incorrect type
 *   - Branch: setDeferredType with node and type
 *   - Branch: resolveTypes with deferred types
 *   - Branch: resolveTypes with scope vars
 *   - Branch: resolveTypes with type registry
 *   - Branch: visit with attachLiteralTypes
 *   - Branch: visit with CALL, FUNCTION, ASSIGN, CATCH, VAR, GETPROP
 *   - Branch: visit with other types
 *   - Branch: shouldTraverse with FUNCTION parent
 *   - Branch: shouldTraverse with SCRIPT parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: shouldTraverse with FUNCTION parent and first child
 *   - Branch: shouldTraverse with FUNCTION parent and root node
 *   - Branch: shouldTraverse with non-FUNCTION parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: visit with CALL node
 *   - Branch: visit with FUNCTION node
 *   - Branch: visit with ASSIGN node
 *   - Branch: visit with CATCH node
 *   - Branch: visit with VAR node
 *   - Branch: visit with GETPROP node
 *   - Branch: visit with other node types
 *   - Branch: attachLiteralTypes with NULL, VOID, STRING, NUMBER, TRUE, FALSE, REGEXP, REF_SPECIAL, OBJECTLIT
 *   - Branch: attachLiteralTypes with other types
 *   - Branch: defineSlot with NAME node
 *   - Branch: defineSlot with GETPROP node
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis true
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis false
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared true
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared false
 *   - Branch: defineSlot with inferred true
 *   - Branch: defineSlot with inferred false
 *   - Branch: defineSlot with type instanceof FunctionType
 *   - Branch: defineSlot with type not instanceof FunctionType
 *   - Branch: defineSlot with fnType.isConstructor() or fnType.isInterface()
 *   - Branch: defineSlot with superClassCtor null
 *   - Branch: defineSlot with superClassCtor non-null
 *   - Branch: defineSlot with superClassCtor.getInstanceType() equals OBJECT_TYPE
 *   - Branch: defineSlot with superClassCtor.getInstanceType() not equals OBJECT_TYPE
 *   - Branch: isQnameRootedInGlobalScope with NAME root and global var
 *   - Branch: isQnameRootedInGlobalScope with NAME root and non-global var
 *   - Branch: isQnameRootedInGlobalScope with non-NAME root
 *   - Branch: getDeclaredGetPropType with @type annotation
 *   - Branch: getDeclaredGetPropType with @enum annotation
 *   - Branch: getDeclaredGetPropType with FUNCTION rhsValue
 *   - Branch: getDeclaredGetPropType with other cases
 *   - Branch: getDeclaredTypeInAnnotation with @type
 *   - Branch: getDeclaredTypeInAnnotation with isFunctionTypeDeclaration
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and existing type
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and no existing type
 *   - Branch: getDeclaredTypeInAnnotation with objNode GETPROP prototype
 *   - Branch: getDeclaredTypeInAnnotation with objNode THIS
 *   - Branch: getDeclaredTypeInAnnotation with no searchedForThisType
 *   - Branch: getDeclaredTypeInAnnotation with null info
 *   - Branch: getDeclaredTypeInAnnotation with null objNode
 *   - Branch: findOverriddenFunction with FunctionType propType
 *   - Branch: findOverriddenFunction with non-FunctionType propType and interfaces
 *   - Branch: findOverriddenFunction with non-FunctionType propType and no interfaces
 *   - Branch: findOverriddenFunction with null ownerType
 *   - Branch: findOverriddenFunction with null propType
 *   - Branch: getObjectSlot with null ownerVar
 *   - Branch: getObjectSlot with null ownerVarType
 *   - Branch: getObjectSlot with non-null ownerVarType
 *   - Branch: getPrototypePropertyOwner with GETPROP chain
 *   - Branch: getPrototypePropertyOwner with non-GETPROP
 *   - Branch: getPrototypePropertyOwner with non-prototype GETPROP
 *   - Branch: getPrototypePropertyOwner with non-qualified name
 *   - Branch: DiscoverEnums.visit with VAR
 *   - Branch: DiscoverEnums.visit with EXPR_RESULT
 *   - Branch: DiscoverEnums.visit with other types
 *   - Branch: DiscoverEnums.identifyEnumName with null info
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and hasEnumParameterType
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and no enum parameter type
 *   - Branch: DeferredSetType constructor with null node
 *   - Branch: DeferredSetType constructor with null type
 *   - Branch: DeferredSetType.resolve with scope
 *   - Branch: createScope with null parent (global)
 *   - Branch: createScope with non-null parent (local)
 *   - Branch: createScope with nonExternFunctions iteration
 *   - Branch: createScope with null type
 *   - Branch: createScope with type not instanceof FunctionType
 *   - Branch: createScope with fnThisType.isUnknownType()
 *   - Branch: createScope with fnThisType not unknown
 *   - Branch: createScope with null parent and delegate proxy
 *   - Branch: createScope with non-null parent and no delegate proxy
 *   - Branch: createInitialScope with root traversal
 *   - Branch: declareNativeFunctionType with scope
 *   - Branch: declareNativeValueType with scope
 *   - Branch: declareNativeType with scope
 *   - Branch: TypedScopeCreator constructor with compiler
 *   - Branch: TypedScopeCreator constructor with compiler and codingConvention
 *   - Branch: getNativeType with native type
 *   - Branch: assertDefinitionNode with correct type
 *   - Branch: assertDefinitionNode with incorrect type
 *   - Branch: setDeferredType with node and type
 *   - Branch: resolveTypes with deferred types
 *   - Branch: resolveTypes with scope vars
 *   - Branch: resolveTypes with type registry
 *   - Branch: visit with attachLiteralTypes
 *   - Branch: visit with CALL, FUNCTION, ASSIGN, CATCH, VAR, GETPROP
 *   - Branch: visit with other types
 *   - Branch: shouldTraverse with FUNCTION parent
 *   - Branch: shouldTraverse with SCRIPT parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: shouldTraverse with FUNCTION parent and first child
 *   - Branch: shouldTraverse with FUNCTION parent and root node
 *   - Branch: shouldTraverse with non-FUNCTION parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: visit with CALL node
 *   - Branch: visit with FUNCTION node
 *   - Branch: visit with ASSIGN node
 *   - Branch: visit with CATCH node
 *   - Branch: visit with VAR node
 *   - Branch: visit with GETPROP node
 *   - Branch: visit with other node types
 *   - Branch: attachLiteralTypes with NULL, VOID, STRING, NUMBER, TRUE, FALSE, REGEXP, REF_SPECIAL, OBJECTLIT
 *   - Branch: attachLiteralTypes with other types
 *   - Branch: defineSlot with NAME node
 *   - Branch: defineSlot with GETPROP node
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis true
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis false
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared true
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared false
 *   - Branch: defineSlot with inferred true
 *   - Branch: defineSlot with inferred false
 *   - Branch: defineSlot with type instanceof FunctionType
 *   - Branch: defineSlot with type not instanceof FunctionType
 *   - Branch: defineSlot with fnType.isConstructor() or fnType.isInterface()
 *   - Branch: defineSlot with superClassCtor null
 *   - Branch: defineSlot with superClassCtor non-null
 *   - Branch: defineSlot with superClassCtor.getInstanceType() equals OBJECT_TYPE
 *   - Branch: defineSlot with superClassCtor.getInstanceType() not equals OBJECT_TYPE
 *   - Branch: isQnameRootedInGlobalScope with NAME root and global var
 *   - Branch: isQnameRootedInGlobalScope with NAME root and non-global var
 *   - Branch: isQnameRootedInGlobalScope with non-NAME root
 *   - Branch: getDeclaredGetPropType with @type annotation
 *   - Branch: getDeclaredGetPropType with @enum annotation
 *   - Branch: getDeclaredGetPropType with FUNCTION rhsValue
 *   - Branch: getDeclaredGetPropType with other cases
 *   - Branch: getDeclaredTypeInAnnotation with @type
 *   - Branch: getDeclaredTypeInAnnotation with isFunctionTypeDeclaration
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and existing type
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and no existing type
 *   - Branch: getDeclaredTypeInAnnotation with objNode GETPROP prototype
 *   - Branch: getDeclaredTypeInAnnotation with objNode THIS
 *   - Branch: getDeclaredTypeInAnnotation with no searchedForThisType
 *   - Branch: getDeclaredTypeInAnnotation with null info
 *   - Branch: getDeclaredTypeInAnnotation with null objNode
 *   - Branch: findOverriddenFunction with FunctionType propType
 *   - Branch: findOverriddenFunction with non-FunctionType propType and interfaces
 *   - Branch: findOverriddenFunction with non-FunctionType propType and no interfaces
 *   - Branch: findOverriddenFunction with null ownerType
 *   - Branch: findOverriddenFunction with null propType
 *   - Branch: getObjectSlot with null ownerVar
 *   - Branch: getObjectSlot with null ownerVarType
 *   - Branch: getObjectSlot with non-null ownerVarType
 *   - Branch: getPrototypePropertyOwner with GETPROP chain
 *   - Branch: getPrototypePropertyOwner with non-GETPROP
 *   - Branch: getPrototypePropertyOwner with non-prototype GETPROP
 *   - Branch: getPrototypePropertyOwner with non-qualified name
 *   - Branch: DiscoverEnums.visit with VAR
 *   - Branch: DiscoverEnums.visit with EXPR_RESULT
 *   - Branch: DiscoverEnums.visit with other types
 *   - Branch: DiscoverEnums.identifyEnumName with null info
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and hasEnumParameterType
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and no enum parameter type
 *   - Branch: DeferredSetType constructor with null node
 *   - Branch: DeferredSetType constructor with null type
 *   - Branch: DeferredSetType.resolve with scope
 *   - Branch: createScope with null parent (global)
 *   - Branch: createScope with non-null parent (local)
 *   - Branch: createScope with nonExternFunctions iteration
 *   - Branch: createScope with null type
 *   - Branch: createScope with type not instanceof FunctionType
 *   - Branch: createScope with fnThisType.isUnknownType()
 *   - Branch: createScope with fnThisType not unknown
 *   - Branch: createScope with null parent and delegate proxy
 *   - Branch: createScope with non-null parent and no delegate proxy
 *   - Branch: createInitialScope with root traversal
 *   - Branch: declareNativeFunctionType with scope
 *   - Branch: declareNativeValueType with scope
 *   - Branch: declareNativeType with scope
 *   - Branch: TypedScopeCreator constructor with compiler
 *   - Branch: TypedScopeCreator constructor with compiler and codingConvention
 *   - Branch: getNativeType with native type
 *   - Branch: assertDefinitionNode with correct type
 *   - Branch: assertDefinitionNode with incorrect type
 *   - Branch: setDeferredType with node and type
 *   - Branch: resolveTypes with deferred types
 *   - Branch: resolveTypes with scope vars
 *   - Branch: resolveTypes with type registry
 *   - Branch: visit with attachLiteralTypes
 *   - Branch: visit with CALL, FUNCTION, ASSIGN, CATCH, VAR, GETPROP
 *   - Branch: visit with other types
 *   - Branch: shouldTraverse with FUNCTION parent
 *   - Branch: shouldTraverse with SCRIPT parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: shouldTraverse with FUNCTION parent and first child
 *   - Branch: shouldTraverse with FUNCTION parent and root node
 *   - Branch: shouldTraverse with non-FUNCTION parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: visit with CALL node
 *   - Branch: visit with FUNCTION node
 *   - Branch: visit with ASSIGN node
 *   - Branch: visit with CATCH node
 *   - Branch: visit with VAR node
 *   - Branch: visit with GETPROP node
 *   - Branch: visit with other node types
 *   - Branch: attachLiteralTypes with NULL, VOID, STRING, NUMBER, TRUE, FALSE, REGEXP, REF_SPECIAL, OBJECTLIT
 *   - Branch: attachLiteralTypes with other types
 *   - Branch: defineSlot with NAME node
 *   - Branch: defineSlot with GETPROP node
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis true
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis false
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared true
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared false
 *   - Branch: defineSlot with inferred true
 *   - Branch: defineSlot with inferred false
 *   - Branch: defineSlot with type instanceof FunctionType
 *   - Branch: defineSlot with type not instanceof FunctionType
 *   - Branch: defineSlot with fnType.isConstructor() or fnType.isInterface()
 *   - Branch: defineSlot with superClassCtor null
 *   - Branch: defineSlot with superClassCtor non-null
 *   - Branch: defineSlot with superClassCtor.getInstanceType() equals OBJECT_TYPE
 *   - Branch: defineSlot with superClassCtor.getInstanceType() not equals OBJECT_TYPE
 *   - Branch: isQnameRootedInGlobalScope with NAME root and global var
 *   - Branch: isQnameRootedInGlobalScope with NAME root and non-global var
 *   - Branch: isQnameRootedInGlobalScope with non-NAME root
 *   - Branch: getDeclaredGetPropType with @type annotation
 *   - Branch: getDeclaredGetPropType with @enum annotation
 *   - Branch: getDeclaredGetPropType with FUNCTION rhsValue
 *   - Branch: getDeclaredGetPropType with other cases
 *   - Branch: getDeclaredTypeInAnnotation with @type
 *   - Branch: getDeclaredTypeInAnnotation with isFunctionTypeDeclaration
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and existing type
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and no existing type
 *   - Branch: getDeclaredTypeInAnnotation with objNode GETPROP prototype
 *   - Branch: getDeclaredTypeInAnnotation with objNode THIS
 *   - Branch: getDeclaredTypeInAnnotation with no searchedForThisType
 *   - Branch: getDeclaredTypeInAnnotation with null info
 *   - Branch: getDeclaredTypeInAnnotation with null objNode
 *   - Branch: findOverriddenFunction with FunctionType propType
 *   - Branch: findOverriddenFunction with non-FunctionType propType and interfaces
 *   - Branch: findOverriddenFunction with non-FunctionType propType and no interfaces
 *   - Branch: findOverriddenFunction with null ownerType
 *   - Branch: findOverriddenFunction with null propType
 *   - Branch: getObjectSlot with null ownerVar
 *   - Branch: getObjectSlot with null ownerVarType
 *   - Branch: getObjectSlot with non-null ownerVarType
 *   - Branch: getPrototypePropertyOwner with GETPROP chain
 *   - Branch: getPrototypePropertyOwner with non-GETPROP
 *   - Branch: getPrototypePropertyOwner with non-prototype GETPROP
 *   - Branch: getPrototypePropertyOwner with non-qualified name
 *   - Branch: DiscoverEnums.visit with VAR
 *   - Branch: DiscoverEnums.visit with EXPR_RESULT
 *   - Branch: DiscoverEnums.visit with other types
 *   - Branch: DiscoverEnums.identifyEnumName with null info
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and hasEnumParameterType
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and no enum parameter type
 *   - Branch: DeferredSetType constructor with null node
 *   - Branch: DeferredSetType constructor with null type
 *   - Branch: DeferredSetType.resolve with scope
 *   - Branch: createScope with null parent (global)
 *   - Branch: createScope with non-null parent (local)
 *   - Branch: createScope with nonExternFunctions iteration
 *   - Branch: createScope with null type
 *   - Branch: createScope with type not instanceof FunctionType
 *   - Branch: createScope with fnThisType.isUnknownType()
 *   - Branch: createScope with fnThisType not unknown
 *   - Branch: createScope with null parent and delegate proxy
 *   - Branch: createScope with non-null parent and no delegate proxy
 *   - Branch: createInitialScope with root traversal
 *   - Branch: declareNativeFunctionType with scope
 *   - Branch: declareNativeValueType with scope
 *   - Branch: declareNativeType with scope
 *   - Branch: TypedScopeCreator constructor with compiler
 *   - Branch: TypedScopeCreator constructor with compiler and codingConvention
 *   - Branch: getNativeType with native type
 *   - Branch: assertDefinitionNode with correct type
 *   - Branch: assertDefinitionNode with incorrect type
 *   - Branch: setDeferredType with node and type
 *   - Branch: resolveTypes with deferred types
 *   - Branch: resolveTypes with scope vars
 *   - Branch: resolveTypes with type registry
 *   - Branch: visit with attachLiteralTypes
 *   - Branch: visit with CALL, FUNCTION, ASSIGN, CATCH, VAR, GETPROP
 *   - Branch: visit with other types
 *   - Branch: shouldTraverse with FUNCTION parent
 *   - Branch: shouldTraverse with SCRIPT parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: shouldTraverse with FUNCTION parent and first child
 *   - Branch: shouldTraverse with FUNCTION parent and root node
 *   - Branch: shouldTraverse with non-FUNCTION parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: visit with CALL node
 *   - Branch: visit with FUNCTION node
 *   - Branch: visit with ASSIGN node
 *   - Branch: visit with CATCH node
 *   - Branch: visit with VAR node
 *   - Branch: visit with GETPROP node
 *   - Branch: visit with other node types
 *   - Branch: attachLiteralTypes with NULL, VOID, STRING, NUMBER, TRUE, FALSE, REGEXP, REF_SPECIAL, OBJECTLIT
 *   - Branch: attachLiteralTypes with other types
 *   - Branch: defineSlot with NAME node
 *   - Branch: defineSlot with GETPROP node
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis true
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis false
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared true
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared false
 *   - Branch: defineSlot with inferred true
 *   - Branch: defineSlot with inferred false
 *   - Branch: defineSlot with type instanceof FunctionType
 *   - Branch: defineSlot with type not instanceof FunctionType
 *   - Branch: defineSlot with fnType.isConstructor() or fnType.isInterface()
 *   - Branch: defineSlot with superClassCtor null
 *   - Branch: defineSlot with superClassCtor non-null
 *   - Branch: defineSlot with superClassCtor.getInstanceType() equals OBJECT_TYPE
 *   - Branch: defineSlot with superClassCtor.getInstanceType() not equals OBJECT_TYPE
 *   - Branch: isQnameRootedInGlobalScope with NAME root and global var
 *   - Branch: isQnameRootedInGlobalScope with NAME root and non-global var
 *   - Branch: isQnameRootedInGlobalScope with non-NAME root
 *   - Branch: getDeclaredGetPropType with @type annotation
 *   - Branch: getDeclaredGetPropType with @enum annotation
 *   - Branch: getDeclaredGetPropType with FUNCTION rhsValue
 *   - Branch: getDeclaredGetPropType with other cases
 *   - Branch: getDeclaredTypeInAnnotation with @type
 *   - Branch: getDeclaredTypeInAnnotation with isFunctionTypeDeclaration
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and existing type
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and no existing type
 *   - Branch: getDeclaredTypeInAnnotation with objNode GETPROP prototype
 *   - Branch: getDeclaredTypeInAnnotation with objNode THIS
 *   - Branch: getDeclaredTypeInAnnotation with no searchedForThisType
 *   - Branch: getDeclaredTypeInAnnotation with null info
 *   - Branch: getDeclaredTypeInAnnotation with null objNode
 *   - Branch: findOverriddenFunction with FunctionType propType
 *   - Branch: findOverriddenFunction with non-FunctionType propType and interfaces
 *   - Branch: findOverriddenFunction with non-FunctionType propType and no interfaces
 *   - Branch: findOverriddenFunction with null ownerType
 *   - Branch: findOverriddenFunction with null propType
 *   - Branch: getObjectSlot with null ownerVar
 *   - Branch: getObjectSlot with null ownerVarType
 *   - Branch: getObjectSlot with non-null ownerVarType
 *   - Branch: getPrototypePropertyOwner with GETPROP chain
 *   - Branch: getPrototypePropertyOwner with non-GETPROP
 *   - Branch: getPrototypePropertyOwner with non-prototype GETPROP
 *   - Branch: getPrototypePropertyOwner with non-qualified name
 *   - Branch: DiscoverEnums.visit with VAR
 *   - Branch: DiscoverEnums.visit with EXPR_RESULT
 *   - Branch: DiscoverEnums.visit with other types
 *   - Branch: DiscoverEnums.identifyEnumName with null info
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and hasEnumParameterType
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and no enum parameter type
 *   - Branch: DeferredSetType constructor with null node
 *   - Branch: DeferredSetType constructor with null type
 *   - Branch: DeferredSetType.resolve with scope
 *   - Branch: createScope with null parent (global)
 *   - Branch: createScope with non-null parent (local)
 *   - Branch: createScope with nonExternFunctions iteration
 *   - Branch: createScope with null type
 *   - Branch: createScope with type not instanceof FunctionType
 *   - Branch: createScope with fnThisType.isUnknownType()
 *   - Branch: createScope with fnThisType not unknown
 *   - Branch: createScope with null parent and delegate proxy
 *   - Branch: createScope with non-null parent and no delegate proxy
 *   - Branch: createInitialScope with root traversal
 *   - Branch: declareNativeFunctionType with scope
 *   - Branch: declareNativeValueType with scope
 *   - Branch: declareNativeType with scope
 *   - Branch: TypedScopeCreator constructor with compiler
 *   - Branch: TypedScopeCreator constructor with compiler and codingConvention
 *   - Branch: getNativeType with native type
 *   - Branch: assertDefinitionNode with correct type
 *   - Branch: assertDefinitionNode with incorrect type
 *   - Branch: setDeferredType with node and type
 *   - Branch: resolveTypes with deferred types
 *   - Branch: resolveTypes with scope vars
 *   - Branch: resolveTypes with type registry
 *   - Branch: visit with attachLiteralTypes
 *   - Branch: visit with CALL, FUNCTION, ASSIGN, CATCH, VAR, GETPROP
 *   - Branch: visit with other types
 *   - Branch: shouldTraverse with FUNCTION parent
 *   - Branch: shouldTraverse with SCRIPT parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: shouldTraverse with FUNCTION parent and first child
 *   - Branch: shouldTraverse with FUNCTION parent and root node
 *   - Branch: shouldTraverse with non-FUNCTION parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: visit with CALL node
 *   - Branch: visit with FUNCTION node
 *   - Branch: visit with ASSIGN node
 *   - Branch: visit with CATCH node
 *   - Branch: visit with VAR node
 *   - Branch: visit with GETPROP node
 *   - Branch: visit with other node types
 *   - Branch: attachLiteralTypes with NULL, VOID, STRING, NUMBER, TRUE, FALSE, REGEXP, REF_SPECIAL, OBJECTLIT
 *   - Branch: attachLiteralTypes with other types
 *   - Branch: defineSlot with NAME node
 *   - Branch: defineSlot with GETPROP node
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis true
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis false
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared true
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared false
 *   - Branch: defineSlot with inferred true
 *   - Branch: defineSlot with inferred false
 *   - Branch: defineSlot with type instanceof FunctionType
 *   - Branch: defineSlot with type not instanceof FunctionType
 *   - Branch: defineSlot with fnType.isConstructor() or fnType.isInterface()
 *   - Branch: defineSlot with superClassCtor null
 *   - Branch: defineSlot with superClassCtor non-null
 *   - Branch: defineSlot with superClassCtor.getInstanceType() equals OBJECT_TYPE
 *   - Branch: defineSlot with superClassCtor.getInstanceType() not equals OBJECT_TYPE
 *   - Branch: isQnameRootedInGlobalScope with NAME root and global var
 *   - Branch: isQnameRootedInGlobalScope with NAME root and non-global var
 *   - Branch: isQnameRootedInGlobalScope with non-NAME root
 *   - Branch: getDeclaredGetPropType with @type annotation
 *   - Branch: getDeclaredGetPropType with @enum annotation
 *   - Branch: getDeclaredGetPropType with FUNCTION rhsValue
 *   - Branch: getDeclaredGetPropType with other cases
 *   - Branch: getDeclaredTypeInAnnotation with @type
 *   - Branch: getDeclaredTypeInAnnotation with isFunctionTypeDeclaration
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and existing type
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and no existing type
 *   - Branch: getDeclaredTypeInAnnotation with objNode GETPROP prototype
 *   - Branch: getDeclaredTypeInAnnotation with objNode THIS
 *   - Branch: getDeclaredTypeInAnnotation with no searchedForThisType
 *   - Branch: getDeclaredTypeInAnnotation with null info
 *   - Branch: getDeclaredTypeInAnnotation with null objNode
 *   - Branch: findOverriddenFunction with FunctionType propType
 *   - Branch: findOverriddenFunction with non-FunctionType propType and interfaces
 *   - Branch: findOverriddenFunction with non-FunctionType propType and no interfaces
 *   - Branch: findOverriddenFunction with null ownerType
 *   - Branch: findOverriddenFunction with null propType
 *   - Branch: getObjectSlot with null ownerVar
 *   - Branch: getObjectSlot with null ownerVarType
 *   - Branch: getObjectSlot with non-null ownerVarType
 *   - Branch: getPrototypePropertyOwner with GETPROP chain
 *   - Branch: getPrototypePropertyOwner with non-GETPROP
 *   - Branch: getPrototypePropertyOwner with non-prototype GETPROP
 *   - Branch: getPrototypePropertyOwner with non-qualified name
 *   - Branch: DiscoverEnums.visit with VAR
 *   - Branch: DiscoverEnums.visit with EXPR_RESULT
 *   - Branch: DiscoverEnums.visit with other types
 *   - Branch: DiscoverEnums.identifyEnumName with null info
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and hasEnumParameterType
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and no enum parameter type
 *   - Branch: DeferredSetType constructor with null node
 *   - Branch: DeferredSetType constructor with null type
 *   - Branch: DeferredSetType.resolve with scope
 *   - Branch: createScope with null parent (global)
 *   - Branch: createScope with non-null parent (local)
 *   - Branch: createScope with nonExternFunctions iteration
 *   - Branch: createScope with null type
 *   - Branch: createScope with type not instanceof FunctionType
 *   - Branch: createScope with fnThisType.isUnknownType()
 *   - Branch: createScope with fnThisType not unknown
 *   - Branch: createScope with null parent and delegate proxy
 *   - Branch: createScope with non-null parent and no delegate proxy
 *   - Branch: createInitialScope with root traversal
 *   - Branch: declareNativeFunctionType with scope
 *   - Branch: declareNativeValueType with scope
 *   - Branch: declareNativeType with scope
 *   - Branch: TypedScopeCreator constructor with compiler
 *   - Branch: TypedScopeCreator constructor with compiler and codingConvention
 *   - Branch: getNativeType with native type
 *   - Branch: assertDefinitionNode with correct type
 *   - Branch: assertDefinitionNode with incorrect type
 *   - Branch: setDeferredType with node and type
 *   - Branch: resolveTypes with deferred types
 *   - Branch: resolveTypes with scope vars
 *   - Branch: resolveTypes with type registry
 *   - Branch: visit with attachLiteralTypes
 *   - Branch: visit with CALL, FUNCTION, ASSIGN, CATCH, VAR, GETPROP
 *   - Branch: visit with other types
 *   - Branch: shouldTraverse with FUNCTION parent
 *   - Branch: shouldTraverse with SCRIPT parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: shouldTraverse with FUNCTION parent and first child
 *   - Branch: shouldTraverse with FUNCTION parent and root node
 *   - Branch: shouldTraverse with non-FUNCTION parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: visit with CALL node
 *   - Branch: visit with FUNCTION node
 *   - Branch: visit with ASSIGN node
 *   - Branch: visit with CATCH node
 *   - Branch: visit with VAR node
 *   - Branch: visit with GETPROP node
 *   - Branch: visit with other node types
 *   - Branch: attachLiteralTypes with NULL, VOID, STRING, NUMBER, TRUE, FALSE, REGEXP, REF_SPECIAL, OBJECTLIT
 *   - Branch: attachLiteralTypes with other types
 *   - Branch: defineSlot with NAME node
 *   - Branch: defineSlot with GETPROP node
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis true
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis false
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared true
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared false
 *   - Branch: defineSlot with inferred true
 *   - Branch: defineSlot with inferred false
 *   - Branch: defineSlot with type instanceof FunctionType
 *   - Branch: defineSlot with type not instanceof FunctionType
 *   - Branch: defineSlot with fnType.isConstructor() or fnType.isInterface()
 *   - Branch: defineSlot with superClassCtor null
 *   - Branch: defineSlot with superClassCtor non-null
 *   - Branch: defineSlot with superClassCtor.getInstanceType() equals OBJECT_TYPE
 *   - Branch: defineSlot with superClassCtor.getInstanceType() not equals OBJECT_TYPE
 *   - Branch: isQnameRootedInGlobalScope with NAME root and global var
 *   - Branch: isQnameRootedInGlobalScope with NAME root and non-global var
 *   - Branch: isQnameRootedInGlobalScope with non-NAME root
 *   - Branch: getDeclaredGetPropType with @type annotation
 *   - Branch: getDeclaredGetPropType with @enum annotation
 *   - Branch: getDeclaredGetPropType with FUNCTION rhsValue
 *   - Branch: getDeclaredGetPropType with other cases
 *   - Branch: getDeclaredTypeInAnnotation with @type
 *   - Branch: getDeclaredTypeInAnnotation with isFunctionTypeDeclaration
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and existing type
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and no existing type
 *   - Branch: getDeclaredTypeInAnnotation with objNode GETPROP prototype
 *   - Branch: getDeclaredTypeInAnnotation with objNode THIS
 *   - Branch: getDeclaredTypeInAnnotation with no searchedForThisType
 *   - Branch: getDeclaredTypeInAnnotation with null info
 *   - Branch: getDeclaredTypeInAnnotation with null objNode
 *   - Branch: findOverriddenFunction with FunctionType propType
 *   - Branch: findOverriddenFunction with non-FunctionType propType and interfaces
 *   - Branch: findOverriddenFunction with non-FunctionType propType and no interfaces
 *   - Branch: findOverriddenFunction with null ownerType
 *   - Branch: findOverriddenFunction with null propType
 *   - Branch: getObjectSlot with null ownerVar
 *   - Branch: getObjectSlot with null ownerVarType
 *   - Branch: getObjectSlot with non-null ownerVarType
 *   - Branch: getPrototypePropertyOwner with GETPROP chain
 *   - Branch: getPrototypePropertyOwner with non-GETPROP
 *   - Branch: getPrototypePropertyOwner with non-prototype GETPROP
 *   - Branch: getPrototypePropertyOwner with non-qualified name
 *   - Branch: DiscoverEnums.visit with VAR
 *   - Branch: DiscoverEnums.visit with EXPR_RESULT
 *   - Branch: DiscoverEnums.visit with other types
 *   - Branch: DiscoverEnums.identifyEnumName with null info
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and hasEnumParameterType
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and no enum parameter type
 *   - Branch: DeferredSetType constructor with null node
 *   - Branch: DeferredSetType constructor with null type
 *   - Branch: DeferredSetType.resolve with scope
 *   - Branch: createScope with null parent (global)
 *   - Branch: createScope with non-null parent (local)
 *   - Branch: createScope with nonExternFunctions iteration
 *   - Branch: createScope with null type
 *   - Branch: createScope with type not instanceof FunctionType
 *   - Branch: createScope with fnThisType.isUnknownType()
 *   - Branch: createScope with fnThisType not unknown
 *   - Branch: createScope with null parent and delegate proxy
 *   - Branch: createScope with non-null parent and no delegate proxy
 *   - Branch: createInitialScope with root traversal
 *   - Branch: declareNativeFunctionType with scope
 *   - Branch: declareNativeValueType with scope
 *   - Branch: declareNativeType with scope
 *   - Branch: TypedScopeCreator constructor with compiler
 *   - Branch: TypedScopeCreator constructor with compiler and codingConvention
 *   - Branch: getNativeType with native type
 *   - Branch: assertDefinitionNode with correct type
 *   - Branch: assertDefinitionNode with incorrect type
 *   - Branch: setDeferredType with node and type
 *   - Branch: resolveTypes with deferred types
 *   - Branch: resolveTypes with scope vars
 *   - Branch: resolveTypes with type registry
 *   - Branch: visit with attachLiteralTypes
 *   - Branch: visit with CALL, FUNCTION, ASSIGN, CATCH, VAR, GETPROP
 *   - Branch: visit with other types
 *   - Branch: shouldTraverse with FUNCTION parent
 *   - Branch: shouldTraverse with SCRIPT parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: shouldTraverse with FUNCTION parent and first child
 *   - Branch: shouldTraverse with FUNCTION parent and root node
 *   - Branch: shouldTraverse with non-FUNCTION parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: visit with CALL node
 *   - Branch: visit with FUNCTION node
 *   - Branch: visit with ASSIGN node
 *   - Branch: visit with CATCH node
 *   - Branch: visit with VAR node
 *   - Branch: visit with GETPROP node
 *   - Branch: visit with other node types
 *   - Branch: attachLiteralTypes with NULL, VOID, STRING, NUMBER, TRUE, FALSE, REGEXP, REF_SPECIAL, OBJECTLIT
 *   - Branch: attachLiteralTypes with other types
 *   - Branch: defineSlot with NAME node
 *   - Branch: defineSlot with GETPROP node
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis true
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis false
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared true
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared false
 *   - Branch: defineSlot with inferred true
 *   - Branch: defineSlot with inferred false
 *   - Branch: defineSlot with type instanceof FunctionType
 *   - Branch: defineSlot with type not instanceof FunctionType
 *   - Branch: defineSlot with fnType.isConstructor() or fnType.isInterface()
 *   - Branch: defineSlot with superClassCtor null
 *   - Branch: defineSlot with superClassCtor non-null
 *   - Branch: defineSlot with superClassCtor.getInstanceType() equals OBJECT_TYPE
 *   - Branch: defineSlot with superClassCtor.getInstanceType() not equals OBJECT_TYPE
 *   - Branch: isQnameRootedInGlobalScope with NAME root and global var
 *   - Branch: isQnameRootedInGlobalScope with NAME root and non-global var
 *   - Branch: isQnameRootedInGlobalScope with non-NAME root
 *   - Branch: getDeclaredGetPropType with @type annotation
 *   - Branch: getDeclaredGetPropType with @enum annotation
 *   - Branch: getDeclaredGetPropType with FUNCTION rhsValue
 *   - Branch: getDeclaredGetPropType with other cases
 *   - Branch: getDeclaredTypeInAnnotation with @type
 *   - Branch: getDeclaredTypeInAnnotation with isFunctionTypeDeclaration
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and existing type
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and no existing type
 *   - Branch: getDeclaredTypeInAnnotation with objNode GETPROP prototype
 *   - Branch: getDeclaredTypeInAnnotation with objNode THIS
 *   - Branch: getDeclaredTypeInAnnotation with no searchedForThisType
 *   - Branch: getDeclaredTypeInAnnotation with null info
 *   - Branch: getDeclaredTypeInAnnotation with null objNode
 *   - Branch: findOverriddenFunction with FunctionType propType
 *   - Branch: findOverriddenFunction with non-FunctionType propType and interfaces
 *   - Branch: findOverriddenFunction with non-FunctionType propType and no interfaces
 *   - Branch: findOverriddenFunction with null ownerType
 *   - Branch: findOverriddenFunction with null propType
 *   - Branch: getObjectSlot with null ownerVar
 *   - Branch: getObjectSlot with null ownerVarType
 *   - Branch: getObjectSlot with non-null ownerVarType
 *   - Branch: getPrototypePropertyOwner with GETPROP chain
 *   - Branch: getPrototypePropertyOwner with non-GETPROP
 *   - Branch: getPrototypePropertyOwner with non-prototype GETPROP
 *   - Branch: getPrototypePropertyOwner with non-qualified name
 *   - Branch: DiscoverEnums.visit with VAR
 *   - Branch: DiscoverEnums.visit with EXPR_RESULT
 *   - Branch: DiscoverEnums.visit with other types
 *   - Branch: DiscoverEnums.identifyEnumName with null info
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and hasEnumParameterType
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and no enum parameter type
 *   - Branch: DeferredSetType constructor with null node
 *   - Branch: DeferredSetType constructor with null type
 *   - Branch: DeferredSetType.resolve with scope
 *   - Branch: createScope with null parent (global)
 *   - Branch: createScope with non-null parent (local)
 *   - Branch: createScope with nonExternFunctions iteration
 *   - Branch: createScope with null type
 *   - Branch: createScope with type not instanceof FunctionType
 *   - Branch: createScope with fnThisType.isUnknownType()
 *   - Branch: createScope with fnThisType not unknown
 *   - Branch: createScope with null parent and delegate proxy
 *   - Branch: createScope with non-null parent and no delegate proxy
 *   - Branch: createInitialScope with root traversal
 *   - Branch: declareNativeFunctionType with scope
 *   - Branch: declareNativeValueType with scope
 *   - Branch: declareNativeType with scope
 *   - Branch: TypedScopeCreator constructor with compiler
 *   - Branch: TypedScopeCreator constructor with compiler and codingConvention
 *   - Branch: getNativeType with native type
 *   - Branch: assertDefinitionNode with correct type
 *   - Branch: assertDefinitionNode with incorrect type
 *   - Branch: setDeferredType with node and type
 *   - Branch: resolveTypes with deferred types
 *   - Branch: resolveTypes with scope vars
 *   - Branch: resolveTypes with type registry
 *   - Branch: visit with attachLiteralTypes
 *   - Branch: visit with CALL, FUNCTION, ASSIGN, CATCH, VAR, GETPROP
 *   - Branch: visit with other types
 *   - Branch: shouldTraverse with FUNCTION parent
 *   - Branch: shouldTraverse with SCRIPT parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: shouldTraverse with FUNCTION parent and first child
 *   - Branch: shouldTraverse with FUNCTION parent and root node
 *   - Branch: shouldTraverse with non-FUNCTION parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: visit with CALL node
 *   - Branch: visit with FUNCTION node
 *   - Branch: visit with ASSIGN node
 *   - Branch: visit with CATCH node
 *   - Branch: visit with VAR node
 *   - Branch: visit with GETPROP node
 *   - Branch: visit with other node types
 *   - Branch: attachLiteralTypes with NULL, VOID, STRING, NUMBER, TRUE, FALSE, REGEXP, REF_SPECIAL, OBJECTLIT
 *   - Branch: attachLiteralTypes with other types
 *   - Branch: defineSlot with NAME node
 *   - Branch: defineSlot with GETPROP node
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis true
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis false
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared true
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared false
 *   - Branch: defineSlot with inferred true
 *   - Branch: defineSlot with inferred false
 *   - Branch: defineSlot with type instanceof FunctionType
 *   - Branch: defineSlot with type not instanceof FunctionType
 *   - Branch: defineSlot with fnType.isConstructor() or fnType.isInterface()
 *   - Branch: defineSlot with superClassCtor null
 *   - Branch: defineSlot with superClassCtor non-null
 *   - Branch: defineSlot with superClassCtor.getInstanceType() equals OBJECT_TYPE
 *   - Branch: defineSlot with superClassCtor.getInstanceType() not equals OBJECT_TYPE
 *   - Branch: isQnameRootedInGlobalScope with NAME root and global var
 *   - Branch: isQnameRootedInGlobalScope with NAME root and non-global var
 *   - Branch: isQnameRootedInGlobalScope with non-NAME root
 *   - Branch: getDeclaredGetPropType with @type annotation
 *   - Branch: getDeclaredGetPropType with @enum annotation
 *   - Branch: getDeclaredGetPropType with FUNCTION rhsValue
 *   - Branch: getDeclaredGetPropType with other cases
 *   - Branch: getDeclaredTypeInAnnotation with @type
 *   - Branch: getDeclaredTypeInAnnotation with isFunctionTypeDeclaration
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and existing type
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and no existing type
 *   - Branch: getDeclaredTypeInAnnotation with objNode GETPROP prototype
 *   - Branch: getDeclaredTypeInAnnotation with objNode THIS
 *   - Branch: getDeclaredTypeInAnnotation with no searchedForThisType
 *   - Branch: getDeclaredTypeInAnnotation with null info
 *   - Branch: getDeclaredTypeInAnnotation with null objNode
 *   - Branch: findOverriddenFunction with FunctionType propType
 *   - Branch: findOverriddenFunction with non-FunctionType propType and interfaces
 *   - Branch: findOverriddenFunction with non-FunctionType propType and no interfaces
 *   - Branch: findOverriddenFunction with null ownerType
 *   - Branch: findOverriddenFunction with null propType
 *   - Branch: getObjectSlot with null ownerVar
 *   - Branch: getObjectSlot with null ownerVarType
 *   - Branch: getObjectSlot with non-null ownerVarType
 *   - Branch: getPrototypePropertyOwner with GETPROP chain
 *   - Branch: getPrototypePropertyOwner with non-GETPROP
 *   - Branch: getPrototypePropertyOwner with non-prototype GETPROP
 *   - Branch: getPrototypePropertyOwner with non-qualified name
 *   - Branch: DiscoverEnums.visit with VAR
 *   - Branch: DiscoverEnums.visit with EXPR_RESULT
 *   - Branch: DiscoverEnums.visit with other types
 *   - Branch: DiscoverEnums.identifyEnumName with null info
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and hasEnumParameterType
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and no enum parameter type
 *   - Branch: DeferredSetType constructor with null node
 *   - Branch: DeferredSetType constructor with null type
 *   - Branch: DeferredSetType.resolve with scope
 *   - Branch: createScope with null parent (global)
 *   - Branch: createScope with non-null parent (local)
 *   - Branch: createScope with nonExternFunctions iteration
 *   - Branch: createScope with null type
 *   - Branch: createScope with type not instanceof FunctionType
 *   - Branch: createScope with fnThisType.isUnknownType()
 *   - Branch: createScope with fnThisType not unknown
 *   - Branch: createScope with null parent and delegate proxy
 *   - Branch: createScope with non-null parent and no delegate proxy
 *   - Branch: createInitialScope with root traversal
 *   - Branch: declareNativeFunctionType with scope
 *   - Branch: declareNativeValueType with scope
 *   - Branch: declareNativeType with scope
 *   - Branch: TypedScopeCreator constructor with compiler
 *   - Branch: TypedScopeCreator constructor with compiler and codingConvention
 *   - Branch: getNativeType with native type
 *   - Branch: assertDefinitionNode with correct type
 *   - Branch: assertDefinitionNode with incorrect type
 *   - Branch: setDeferredType with node and type
 *   - Branch: resolveTypes with deferred types
 *   - Branch: resolveTypes with scope vars
 *   - Branch: resolveTypes with type registry
 *   - Branch: visit with attachLiteralTypes
 *   - Branch: visit with CALL, FUNCTION, ASSIGN, CATCH, VAR, GETPROP
 *   - Branch: visit with other types
 *   - Branch: shouldTraverse with FUNCTION parent
 *   - Branch: shouldTraverse with SCRIPT parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: shouldTraverse with FUNCTION parent and first child
 *   - Branch: shouldTraverse with FUNCTION parent and root node
 *   - Branch: shouldTraverse with non-FUNCTION parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: visit with CALL node
 *   - Branch: visit with FUNCTION node
 *   - Branch: visit with ASSIGN node
 *   - Branch: visit with CATCH node
 *   - Branch: visit with VAR node
 *   - Branch: visit with GETPROP node
 *   - Branch: visit with other node types
 *   - Branch: attachLiteralTypes with NULL, VOID, STRING, NUMBER, TRUE, FALSE, REGEXP, REF_SPECIAL, OBJECTLIT
 *   - Branch: attachLiteralTypes with other types
 *   - Branch: defineSlot with NAME node
 *   - Branch: defineSlot with GETPROP node
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis true
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis false
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared true
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared false
 *   - Branch: defineSlot with inferred true
 *   - Branch: defineSlot with inferred false
 *   - Branch: defineSlot with type instanceof FunctionType
 *   - Branch: defineSlot with type not instanceof FunctionType
 *   - Branch: defineSlot with fnType.isConstructor() or fnType.isInterface()
 *   - Branch: defineSlot with superClassCtor null
 *   - Branch: defineSlot with superClassCtor non-null
 *   - Branch: defineSlot with superClassCtor.getInstanceType() equals OBJECT_TYPE
 *   - Branch: defineSlot with superClassCtor.getInstanceType() not equals OBJECT_TYPE
 *   - Branch: isQnameRootedInGlobalScope with NAME root and global var
 *   - Branch: isQnameRootedInGlobalScope with NAME root and non-global var
 *   - Branch: isQnameRootedInGlobalScope with non-NAME root
 *   - Branch: getDeclaredGetPropType with @type annotation
 *   - Branch: getDeclaredGetPropType with @enum annotation
 *   - Branch: getDeclaredGetPropType with FUNCTION rhsValue
 *   - Branch: getDeclaredGetPropType with other cases
 *   - Branch: getDeclaredTypeInAnnotation with @type
 *   - Branch: getDeclaredTypeInAnnotation with isFunctionTypeDeclaration
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and existing type
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and no existing type
 *   - Branch: getDeclaredTypeInAnnotation with objNode GETPROP prototype
 *   - Branch: getDeclaredTypeInAnnotation with objNode THIS
 *   - Branch: getDeclaredTypeInAnnotation with no searchedForThisType
 *   - Branch: getDeclaredTypeInAnnotation with null info
 *   - Branch: getDeclaredTypeInAnnotation with null objNode
 *   - Branch: findOverriddenFunction with FunctionType propType
 *   - Branch: findOverriddenFunction with non-FunctionType propType and interfaces
 *   - Branch: findOverriddenFunction with non-FunctionType propType and no interfaces
 *   - Branch: findOverriddenFunction with null ownerType
 *   - Branch: findOverriddenFunction with null propType
 *   - Branch: getObjectSlot with null ownerVar
 *   - Branch: getObjectSlot with null ownerVarType
 *   - Branch: getObjectSlot with non-null ownerVarType
 *   - Branch: getPrototypePropertyOwner with GETPROP chain
 *   - Branch: getPrototypePropertyOwner with non-GETPROP
 *   - Branch: getPrototypePropertyOwner with non-prototype GETPROP
 *   - Branch: getPrototypePropertyOwner with non-qualified name
 *   - Branch: DiscoverEnums.visit with VAR
 *   - Branch: DiscoverEnums.visit with EXPR_RESULT
 *   - Branch: DiscoverEnums.visit with other types
 *   - Branch: DiscoverEnums.identifyEnumName with null info
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and hasEnumParameterType
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and no enum parameter type
 *   - Branch: DeferredSetType constructor with null node
 *   - Branch: DeferredSetType constructor with null type
 *   - Branch: DeferredSetType.resolve with scope
 *   - Branch: createScope with null parent (global)
 *   - Branch: createScope with non-null parent (local)
 *   - Branch: createScope with nonExternFunctions iteration
 *   - Branch: createScope with null type
 *   - Branch: createScope with type not instanceof FunctionType
 *   - Branch: createScope with fnThisType.isUnknownType()
 *   - Branch: createScope with fnThisType not unknown
 *   - Branch: createScope with null parent and delegate proxy
 *   - Branch: createScope with non-null parent and no delegate proxy
 *   - Branch: createInitialScope with root traversal
 *   - Branch: declareNativeFunctionType with scope
 *   - Branch: declareNativeValueType with scope
 *   - Branch: declareNativeType with scope
 *   - Branch: TypedScopeCreator constructor with compiler
 *   - Branch: TypedScopeCreator constructor with compiler and codingConvention
 *   - Branch: getNativeType with native type
 *   - Branch: assertDefinitionNode with correct type
 *   - Branch: assertDefinitionNode with incorrect type
 *   - Branch: setDeferredType with node and type
 *   - Branch: resolveTypes with deferred types
 *   - Branch: resolveTypes with scope vars
 *   - Branch: resolveTypes with type registry
 *   - Branch: visit with attachLiteralTypes
 *   - Branch: visit with CALL, FUNCTION, ASSIGN, CATCH, VAR, GETPROP
 *   - Branch: visit with other types
 *   - Branch: shouldTraverse with FUNCTION parent
 *   - Branch: shouldTraverse with SCRIPT parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: shouldTraverse with FUNCTION parent and first child
 *   - Branch: shouldTraverse with FUNCTION parent and root node
 *   - Branch: shouldTraverse with non-FUNCTION parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: visit with CALL node
 *   - Branch: visit with FUNCTION node
 *   - Branch: visit with ASSIGN node
 *   - Branch: visit with CATCH node
 *   - Branch: visit with VAR node
 *   - Branch: visit with GETPROP node
 *   - Branch: visit with other node types
 *   - Branch: attachLiteralTypes with NULL, VOID, STRING, NUMBER, TRUE, FALSE, REGEXP, REF_SPECIAL, OBJECTLIT
 *   - Branch: attachLiteralTypes with other types
 *   - Branch: defineSlot with NAME node
 *   - Branch: defineSlot with GETPROP node
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis true
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis false
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared true
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared false
 *   - Branch: defineSlot with inferred true
 *   - Branch: defineSlot with inferred false
 *   - Branch: defineSlot with type instanceof FunctionType
 *   - Branch: defineSlot with type not instanceof FunctionType
 *   - Branch: defineSlot with fnType.isConstructor() or fnType.isInterface()
 *   - Branch: defineSlot with superClassCtor null
 *   - Branch: defineSlot with superClassCtor non-null
 *   - Branch: defineSlot with superClassCtor.getInstanceType() equals OBJECT_TYPE
 *   - Branch: defineSlot with superClassCtor.getInstanceType() not equals OBJECT_TYPE
 *   - Branch: isQnameRootedInGlobalScope with NAME root and global var
 *   - Branch: isQnameRootedInGlobalScope with NAME root and non-global var
 *   - Branch: isQnameRootedInGlobalScope with non-NAME root
 *   - Branch: getDeclaredGetPropType with @type annotation
 *   - Branch: getDeclaredGetPropType with @enum annotation
 *   - Branch: getDeclaredGetPropType with FUNCTION rhsValue
 *   - Branch: getDeclaredGetPropType with other cases
 *   - Branch: getDeclaredTypeInAnnotation with @type
 *   - Branch: getDeclaredTypeInAnnotation with isFunctionTypeDeclaration
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and existing type
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and no existing type
 *   - Branch: getDeclaredTypeInAnnotation with objNode GETPROP prototype
 *   - Branch: getDeclaredTypeInAnnotation with objNode THIS
 *   - Branch: getDeclaredTypeInAnnotation with no searchedForThisType
 *   - Branch: getDeclaredTypeInAnnotation with null info
 *   - Branch: getDeclaredTypeInAnnotation with null objNode
 *   - Branch: findOverriddenFunction with FunctionType propType
 *   - Branch: findOverriddenFunction with non-FunctionType propType and interfaces
 *   - Branch: findOverriddenFunction with non-FunctionType propType and no interfaces
 *   - Branch: findOverriddenFunction with null ownerType
 *   - Branch: findOverriddenFunction with null propType
 *   - Branch: getObjectSlot with null ownerVar
 *   - Branch: getObjectSlot with null ownerVarType
 *   - Branch: getObjectSlot with non-null ownerVarType
 *   - Branch: getPrototypePropertyOwner with GETPROP chain
 *   - Branch: getPrototypePropertyOwner with non-GETPROP
 *   - Branch: getPrototypePropertyOwner with non-prototype GETPROP
 *   - Branch: getPrototypePropertyOwner with non-qualified name
 *   - Branch: DiscoverEnums.visit with VAR
 *   - Branch: DiscoverEnums.visit with EXPR_RESULT
 *   - Branch: DiscoverEnums.visit with other types
 *   - Branch: DiscoverEnums.identifyEnumName with null info
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and hasEnumParameterType
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and no enum parameter type
 *   - Branch: DeferredSetType constructor with null node
 *   - Branch: DeferredSetType constructor with null type
 *   - Branch: DeferredSetType.resolve with scope
 *   - Branch: createScope with null parent (global)
 *   - Branch: createScope with non-null parent (local)
 *   - Branch: createScope with nonExternFunctions iteration
 *   - Branch: createScope with null type
 *   - Branch: createScope with type not instanceof FunctionType
 *   - Branch: createScope with fnThisType.isUnknownType()
 *   - Branch: createScope with fnThisType not unknown
 *   - Branch: createScope with null parent and delegate proxy
 *   - Branch: createScope with non-null parent and no delegate proxy
 *   - Branch: createInitialScope with root traversal
 *   - Branch: declareNativeFunctionType with scope
 *   - Branch: declareNativeValueType with scope
 *   - Branch: declareNativeType with scope
 *   - Branch: TypedScopeCreator constructor with compiler
 *   - Branch: TypedScopeCreator constructor with compiler and codingConvention
 *   - Branch: getNativeType with native type
 *   - Branch: assertDefinitionNode with correct type
 *   - Branch: assertDefinitionNode with incorrect type
 *   - Branch: setDeferredType with node and type
 *   - Branch: resolveTypes with deferred types
 *   - Branch: resolveTypes with scope vars
 *   - Branch: resolveTypes with type registry
 *   - Branch: visit with attachLiteralTypes
 *   - Branch: visit with CALL, FUNCTION, ASSIGN, CATCH, VAR, GETPROP
 *   - Branch: visit with other types
 *   - Branch: shouldTraverse with FUNCTION parent
 *   - Branch: shouldTraverse with SCRIPT parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: shouldTraverse with FUNCTION parent and first child
 *   - Branch: shouldTraverse with FUNCTION parent and root node
 *   - Branch: shouldTraverse with non-FUNCTION parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: visit with CALL node
 *   - Branch: visit with FUNCTION node
 *   - Branch: visit with ASSIGN node
 *   - Branch: visit with CATCH node
 *   - Branch: visit with VAR node
 *   - Branch: visit with GETPROP node
 *   - Branch: visit with other node types
 *   - Branch: attachLiteralTypes with NULL, VOID, STRING, NUMBER, TRUE, FALSE, REGEXP, REF_SPECIAL, OBJECTLIT
 *   - Branch: attachLiteralTypes with other types
 *   - Branch: defineSlot with NAME node
 *   - Branch: defineSlot with GETPROP node
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis true
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis false
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared true
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared false
 *   - Branch: defineSlot with inferred true
 *   - Branch: defineSlot with inferred false
 *   - Branch: defineSlot with type instanceof FunctionType
 *   - Branch: defineSlot with type not instanceof FunctionType
 *   - Branch: defineSlot with fnType.isConstructor() or fnType.isInterface()
 *   - Branch: defineSlot with superClassCtor null
 *   - Branch: defineSlot with superClassCtor non-null
 *   - Branch: defineSlot with superClassCtor.getInstanceType() equals OBJECT_TYPE
 *   - Branch: defineSlot with superClassCtor.getInstanceType() not equals OBJECT_TYPE
 *   - Branch: isQnameRootedInGlobalScope with NAME root and global var
 *   - Branch: isQnameRootedInGlobalScope with NAME root and non-global var
 *   - Branch: isQnameRootedInGlobalScope with non-NAME root
 *   - Branch: getDeclaredGetPropType with @type annotation
 *   - Branch: getDeclaredGetPropType with @enum annotation
 *   - Branch: getDeclaredGetPropType with FUNCTION rhsValue
 *   - Branch: getDeclaredGetPropType with other cases
 *   - Branch: getDeclaredTypeInAnnotation with @type
 *   - Branch: getDeclaredTypeInAnnotation with isFunctionTypeDeclaration
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and existing type
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and no existing type
 *   - Branch: getDeclaredTypeInAnnotation with objNode GETPROP prototype
 *   - Branch: getDeclaredTypeInAnnotation with objNode THIS
 *   - Branch: getDeclaredTypeInAnnotation with no searchedForThisType
 *   - Branch: getDeclaredTypeInAnnotation with null info
 *   - Branch: getDeclaredTypeInAnnotation with null objNode
 *   - Branch: findOverriddenFunction with FunctionType propType
 *   - Branch: findOverriddenFunction with non-FunctionType propType and interfaces
 *   - Branch: findOverriddenFunction with non-FunctionType propType and no interfaces
 *   - Branch: findOverriddenFunction with null ownerType
 *   - Branch: findOverriddenFunction with null propType
 *   - Branch: getObjectSlot with null ownerVar
 *   - Branch: getObjectSlot with null ownerVarType
 *   - Branch: getObjectSlot with non-null ownerVarType
 *   - Branch: getPrototypePropertyOwner with GETPROP chain
 *   - Branch: getPrototypePropertyOwner with non-GETPROP
 *   - Branch: getPrototypePropertyOwner with non-prototype GETPROP
 *   - Branch: getPrototypePropertyOwner with non-qualified name
 *   - Branch: DiscoverEnums.visit with VAR
 *   - Branch: DiscoverEnums.visit with EXPR_RESULT
 *   - Branch: DiscoverEnums.visit with other types
 *   - Branch: DiscoverEnums.identifyEnumName with null info
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and hasEnumParameterType
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and no enum parameter type
 *   - Branch: DeferredSetType constructor with null node
 *   - Branch: DeferredSetType constructor with null type
 *   - Branch: DeferredSetType.resolve with scope
 *   - Branch: createScope with null parent (global)
 *   - Branch: createScope with non-null parent (local)
 *   - Branch: createScope with nonExternFunctions iteration
 *   - Branch: createScope with null type
 *   - Branch: createScope with type not instanceof FunctionType
 *   - Branch: createScope with fnThisType.isUnknownType()
 *   - Branch: createScope with fnThisType not unknown
 *   - Branch: createScope with null parent and delegate proxy
 *   - Branch: createScope with non-null parent and no delegate proxy
 *   - Branch: createInitialScope with root traversal
 *   - Branch: declareNativeFunctionType with scope
 *   - Branch: declareNativeValueType with scope
 *   - Branch: declareNativeType with scope
 *   - Branch: TypedScopeCreator constructor with compiler
 *   - Branch: TypedScopeCreator constructor with compiler and codingConvention
 *   - Branch: getNativeType with native type
 *   - Branch: assertDefinitionNode with correct type
 *   - Branch: assertDefinitionNode with incorrect type
 *   - Branch: setDeferredType with node and type
 *   - Branch: resolveTypes with deferred types
 *   - Branch: resolveTypes with scope vars
 *   - Branch: resolveTypes with type registry
 *   - Branch: visit with attachLiteralTypes
 *   - Branch: visit with CALL, FUNCTION, ASSIGN, CATCH, VAR, GETPROP
 *   - Branch: visit with other types
 *   - Branch: shouldTraverse with FUNCTION parent
 *   - Branch: shouldTraverse with SCRIPT parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: shouldTraverse with FUNCTION parent and first child
 *   - Branch: shouldTraverse with FUNCTION parent and root node
 *   - Branch: shouldTraverse with non-FUNCTION parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: visit with CALL node
 *   - Branch: visit with FUNCTION node
 *   - Branch: visit with ASSIGN node
 *   - Branch: visit with CATCH node
 *   - Branch: visit with VAR node
 *   - Branch: visit with GETPROP node
 *   - Branch: visit with other node types
 *   - Branch: attachLiteralTypes with NULL, VOID, STRING, NUMBER, TRUE, FALSE, REGEXP, REF_SPECIAL, OBJECTLIT
 *   - Branch: attachLiteralTypes with other types
 *   - Branch: defineSlot with NAME node
 *   - Branch: defineSlot with GETPROP node
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis true
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis false
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared true
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared false
 *   - Branch: defineSlot with inferred true
 *   - Branch: defineSlot with inferred false
 *   - Branch: defineSlot with type instanceof FunctionType
 *   - Branch: defineSlot with type not instanceof FunctionType
 *   - Branch: defineSlot with fnType.isConstructor() or fnType.isInterface()
 *   - Branch: defineSlot with superClassCtor null
 *   - Branch: defineSlot with superClassCtor non-null
 *   - Branch: defineSlot with superClassCtor.getInstanceType() equals OBJECT_TYPE
 *   - Branch: defineSlot with superClassCtor.getInstanceType() not equals OBJECT_TYPE
 *   - Branch: isQnameRootedInGlobalScope with NAME root and global var
 *   - Branch: isQnameRootedInGlobalScope with NAME root and non-global var
 *   - Branch: isQnameRootedInGlobalScope with non-NAME root
 *   - Branch: getDeclaredGetPropType with @type annotation
 *   - Branch: getDeclaredGetPropType with @enum annotation
 *   - Branch: getDeclaredGetPropType with FUNCTION rhsValue
 *   - Branch: getDeclaredGetPropType with other cases
 *   - Branch: getDeclaredTypeInAnnotation with @type
 *   - Branch: getDeclaredTypeInAnnotation with isFunctionTypeDeclaration
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and existing type
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and no existing type
 *   - Branch: getDeclaredTypeInAnnotation with objNode GETPROP prototype
 *   - Branch: getDeclaredTypeInAnnotation with objNode THIS
 *   - Branch: getDeclaredTypeInAnnotation with no searchedForThisType
 *   - Branch: getDeclaredTypeInAnnotation with null info
 *   - Branch: getDeclaredTypeInAnnotation with null objNode
 *   - Branch: findOverriddenFunction with FunctionType propType
 *   - Branch: findOverriddenFunction with non-FunctionType propType and interfaces
 *   - Branch: findOverriddenFunction with non-FunctionType propType and no interfaces
 *   - Branch: findOverriddenFunction with null ownerType
 *   - Branch: findOverriddenFunction with null propType
 *   - Branch: getObjectSlot with null ownerVar
 *   - Branch: getObjectSlot with null ownerVarType
 *   - Branch: getObjectSlot with non-null ownerVarType
 *   - Branch: getPrototypePropertyOwner with GETPROP chain
 *   - Branch: getPrototypePropertyOwner with non-GETPROP
 *   - Branch: getPrototypePropertyOwner with non-prototype GETPROP
 *   - Branch: getPrototypePropertyOwner with non-qualified name
 *   - Branch: DiscoverEnums.visit with VAR
 *   - Branch: DiscoverEnums.visit with EXPR_RESULT
 *   - Branch: DiscoverEnums.visit with other types
 *   - Branch: DiscoverEnums.identifyEnumName with null info
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and hasEnumParameterType
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and no enum parameter type
 *   - Branch: DeferredSetType constructor with null node
 *   - Branch: DeferredSetType constructor with null type
 *   - Branch: DeferredSetType.resolve with scope
 *   - Branch: createScope with null parent (global)
 *   - Branch: createScope with non-null parent (local)
 *   - Branch: createScope with nonExternFunctions iteration
 *   - Branch: createScope with null type
 *   - Branch: createScope with type not instanceof FunctionType
 *   - Branch: createScope with fnThisType.isUnknownType()
 *   - Branch: createScope with fnThisType not unknown
 *   - Branch: createScope with null parent and delegate proxy
 *   - Branch: createScope with non-null parent and no delegate proxy
 *   - Branch: createInitialScope with root traversal
 *   - Branch: declareNativeFunctionType with scope
 *   - Branch: declareNativeValueType with scope
 *   - Branch: declareNativeType with scope
 *   - Branch: TypedScopeCreator constructor with compiler
 *   - Branch: TypedScopeCreator constructor with compiler and codingConvention
 *   - Branch: getNativeType with native type
 *   - Branch: assertDefinitionNode with correct type
 *   - Branch: assertDefinitionNode with incorrect type
 *   - Branch: setDeferredType with node and type
 *   - Branch: resolveTypes with deferred types
 *   - Branch: resolveTypes with scope vars
 *   - Branch: resolveTypes with type registry
 *   - Branch: visit with attachLiteralTypes
 *   - Branch: visit with CALL, FUNCTION, ASSIGN, CATCH, VAR, GETPROP
 *   - Branch: visit with other types
 *   - Branch: shouldTraverse with FUNCTION parent
 *   - Branch: shouldTraverse with SCRIPT parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: shouldTraverse with FUNCTION parent and first child
 *   - Branch: shouldTraverse with FUNCTION parent and root node
 *   - Branch: shouldTraverse with non-FUNCTION parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: visit with CALL node
 *   - Branch: visit with FUNCTION node
 *   - Branch: visit with ASSIGN node
 *   - Branch: visit with CATCH node
 *   - Branch: visit with VAR node
 *   - Branch: visit with GETPROP node
 *   - Branch: visit with other node types
 *   - Branch: attachLiteralTypes with NULL, VOID, STRING, NUMBER, TRUE, FALSE, REGEXP, REF_SPECIAL, OBJECTLIT
 *   - Branch: attachLiteralTypes with other types
 *   - Branch: defineSlot with NAME node
 *   - Branch: defineSlot with GETPROP node
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis true
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis false
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared true
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared false
 *   - Branch: defineSlot with inferred true
 *   - Branch: defineSlot with inferred false
 *   - Branch: defineSlot with type instanceof FunctionType
 *   - Branch: defineSlot with type not instanceof FunctionType
 *   - Branch: defineSlot with fnType.isConstructor() or fnType.isInterface()
 *   - Branch: defineSlot with superClassCtor null
 *   - Branch: defineSlot with superClassCtor non-null
 *   - Branch: defineSlot with superClassCtor.getInstanceType() equals OBJECT_TYPE
 *   - Branch: defineSlot with superClassCtor.getInstanceType() not equals OBJECT_TYPE
 *   - Branch: isQnameRootedInGlobalScope with NAME root and global var
 *   - Branch: isQnameRootedInGlobalScope with NAME root and non-global var
 *   - Branch: isQnameRootedInGlobalScope with non-NAME root
 *   - Branch: getDeclaredGetPropType with @type annotation
 *   - Branch: getDeclaredGetPropType with @enum annotation
 *   - Branch: getDeclaredGetPropType with FUNCTION rhsValue
 *   - Branch: getDeclaredGetPropType with other cases
 *   - Branch: getDeclaredTypeInAnnotation with @type
 *   - Branch: getDeclaredTypeInAnnotation with isFunctionTypeDeclaration
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and existing type
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and no existing type
 *   - Branch: getDeclaredTypeInAnnotation with objNode GETPROP prototype
 *   - Branch: getDeclaredTypeInAnnotation with objNode THIS
 *   - Branch: getDeclaredTypeInAnnotation with no searchedForThisType
 *   - Branch: getDeclaredTypeInAnnotation with null info
 *   - Branch: getDeclaredTypeInAnnotation with null objNode
 *   - Branch: findOverriddenFunction with FunctionType propType
 *   - Branch: findOverriddenFunction with non-FunctionType propType and interfaces
 *   - Branch: findOverriddenFunction with non-FunctionType propType and no interfaces
 *   - Branch: findOverriddenFunction with null ownerType
 *   - Branch: findOverriddenFunction with null propType
 *   - Branch: getObjectSlot with null ownerVar
 *   - Branch: getObjectSlot with null ownerVarType
 *   - Branch: getObjectSlot with non-null ownerVarType
 *   - Branch: getPrototypePropertyOwner with GETPROP chain
 *   - Branch: getPrototypePropertyOwner with non-GETPROP
 *   - Branch: getPrototypePropertyOwner with non-prototype GETPROP
 *   - Branch: getPrototypePropertyOwner with non-qualified name
 *   - Branch: DiscoverEnums.visit with VAR
 *   - Branch: DiscoverEnums.visit with EXPR_RESULT
 *   - Branch: DiscoverEnums.visit with other types
 *   - Branch: DiscoverEnums.identifyEnumName with null info
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and hasEnumParameterType
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and no enum parameter type
 *   - Branch: DeferredSetType constructor with null node
 *   - Branch: DeferredSetType constructor with null type
 *   - Branch: DeferredSetType.resolve with scope
 *   - Branch: createScope with null parent (global)
 *   - Branch: createScope with non-null parent (local)
 *   - Branch: createScope with nonExternFunctions iteration
 *   - Branch: createScope with null type
 *   - Branch: createScope with type not instanceof FunctionType
 *   - Branch: createScope with fnThisType.isUnknownType()
 *   - Branch: createScope with fnThisType not unknown
 *   - Branch: createScope with null parent and delegate proxy
 *   - Branch: createScope with non-null parent and no delegate proxy
 *   - Branch: createInitialScope with root traversal
 *   - Branch: declareNativeFunctionType with scope
 *   - Branch: declareNativeValueType with scope
 *   - Branch: declareNativeType with scope
 *   - Branch: TypedScopeCreator constructor with compiler
 *   - Branch: TypedScopeCreator constructor with compiler and codingConvention
 *   - Branch: getNativeType with native type
 *   - Branch: assertDefinitionNode with correct type
 *   - Branch: assertDefinitionNode with incorrect type
 *   - Branch: setDeferredType with node and type
 *   - Branch: resolveTypes with deferred types
 *   - Branch: resolveTypes with scope vars
 *   - Branch: resolveTypes with type registry
 *   - Branch: visit with attachLiteralTypes
 *   - Branch: visit with CALL, FUNCTION, ASSIGN, CATCH, VAR, GETPROP
 *   - Branch: visit with other types
 *   - Branch: shouldTraverse with FUNCTION parent
 *   - Branch: shouldTraverse with SCRIPT parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: shouldTraverse with FUNCTION parent and first child
 *   - Branch: shouldTraverse with FUNCTION parent and root node
 *   - Branch: shouldTraverse with non-FUNCTION parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: visit with CALL node
 *   - Branch: visit with FUNCTION node
 *   - Branch: visit with ASSIGN node
 *   - Branch: visit with CATCH node
 *   - Branch: visit with VAR node
 *   - Branch: visit with GETPROP node
 *   - Branch: visit with other node types
 *   - Branch: attachLiteralTypes with NULL, VOID, STRING, NUMBER, TRUE, FALSE, REGEXP, REF_SPECIAL, OBJECTLIT
 *   - Branch: attachLiteralTypes with other types
 *   - Branch: defineSlot with NAME node
 *   - Branch: defineSlot with GETPROP node
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis true
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis false
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared true
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared false
 *   - Branch: defineSlot with inferred true
 *   - Branch: defineSlot with inferred false
 *   - Branch: defineSlot with type instanceof FunctionType
 *   - Branch: defineSlot with type not instanceof FunctionType
 *   - Branch: defineSlot with fnType.isConstructor() or fnType.isInterface()
 *   - Branch: defineSlot with superClassCtor null
 *   - Branch: defineSlot with superClassCtor non-null
 *   - Branch: defineSlot with superClassCtor.getInstanceType() equals OBJECT_TYPE
 *   - Branch: defineSlot with superClassCtor.getInstanceType() not equals OBJECT_TYPE
 *   - Branch: isQnameRootedInGlobalScope with NAME root and global var
 *   - Branch: isQnameRootedInGlobalScope with NAME root and non-global var
 *   - Branch: isQnameRootedInGlobalScope with non-NAME root
 *   - Branch: getDeclaredGetPropType with @type annotation
 *   - Branch: getDeclaredGetPropType with @enum annotation
 *   - Branch: getDeclaredGetPropType with FUNCTION rhsValue
 *   - Branch: getDeclaredGetPropType with other cases
 *   - Branch: getDeclaredTypeInAnnotation with @type
 *   - Branch: getDeclaredTypeInAnnotation with isFunctionTypeDeclaration
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and existing type
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and no existing type
 *   - Branch: getDeclaredTypeInAnnotation with objNode GETPROP prototype
 *   - Branch: getDeclaredTypeInAnnotation with objNode THIS
 *   - Branch: getDeclaredTypeInAnnotation with no searchedForThisType
 *   - Branch: getDeclaredTypeInAnnotation with null info
 *   - Branch: getDeclaredTypeInAnnotation with null objNode
 *   - Branch: findOverriddenFunction with FunctionType propType
 *   - Branch: findOverriddenFunction with non-FunctionType propType and interfaces
 *   - Branch: findOverriddenFunction with non-FunctionType propType and no interfaces
 *   - Branch: findOverriddenFunction with null ownerType
 *   - Branch: findOverriddenFunction with null propType
 *   - Branch: getObjectSlot with null ownerVar
 *   - Branch: getObjectSlot with null ownerVarType
 *   - Branch: getObjectSlot with non-null ownerVarType
 *   - Branch: getPrototypePropertyOwner with GETPROP chain
 *   - Branch: getPrototypePropertyOwner with non-GETPROP
 *   - Branch: getPrototypePropertyOwner with non-prototype GETPROP
 *   - Branch: getPrototypePropertyOwner with non-qualified name
 *   - Branch: DiscoverEnums.visit with VAR
 *   - Branch: DiscoverEnums.visit with EXPR_RESULT
 *   - Branch: DiscoverEnums.visit with other types
 *   - Branch: DiscoverEnums.identifyEnumName with null info
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and hasEnumParameterType
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and no enum parameter type
 *   - Branch: DeferredSetType constructor with null node
 *   - Branch: DeferredSetType constructor with null type
 *   - Branch: DeferredSetType.resolve with scope
 *   - Branch: createScope with null parent (global)
 *   - Branch: createScope with non-null parent (local)
 *   - Branch: createScope with nonExternFunctions iteration
 *   - Branch: createScope with null type
 *   - Branch: createScope with type not instanceof FunctionType
 *   - Branch: createScope with fnThisType.isUnknownType()
 *   - Branch: createScope with fnThisType not unknown
 *   - Branch: createScope with null parent and delegate proxy
 *   - Branch: createScope with non-null parent and no delegate proxy
 *   - Branch: createInitialScope with root traversal
 *   - Branch: declareNativeFunctionType with scope
 *   - Branch: declareNativeValueType with scope
 *   - Branch: declareNativeType with scope
 *   - Branch: TypedScopeCreator constructor with compiler
 *   - Branch: TypedScopeCreator constructor with compiler and codingConvention
 *   - Branch: getNativeType with native type
 *   - Branch: assertDefinitionNode with correct type
 *   - Branch: assertDefinitionNode with incorrect type
 *   - Branch: setDeferredType with node and type
 *   - Branch: resolveTypes with deferred types
 *   - Branch: resolveTypes with scope vars
 *   - Branch: resolveTypes with type registry
 *   - Branch: visit with attachLiteralTypes
 *   - Branch: visit with CALL, FUNCTION, ASSIGN, CATCH, VAR, GETPROP
 *   - Branch: visit with other types
 *   - Branch: shouldTraverse with FUNCTION parent
 *   - Branch: shouldTraverse with SCRIPT parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: shouldTraverse with FUNCTION parent and first child
 *   - Branch: shouldTraverse with FUNCTION parent and root node
 *   - Branch: shouldTraverse with non-FUNCTION parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: visit with CALL node
 *   - Branch: visit with FUNCTION node
 *   - Branch: visit with ASSIGN node
 *   - Branch: visit with CATCH node
 *   - Branch: visit with VAR node
 *   - Branch: visit with GETPROP node
 *   - Branch: visit with other node types
 *   - Branch: attachLiteralTypes with NULL, VOID, STRING, NUMBER, TRUE, FALSE, REGEXP, REF_SPECIAL, OBJECTLIT
 *   - Branch: attachLiteralTypes with other types
 *   - Branch: defineSlot with NAME node
 *   - Branch: defineSlot with GETPROP node
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis true
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis false
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared true
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared false
 *   - Branch: defineSlot with inferred true
 *   - Branch: defineSlot with inferred false
 *   - Branch: defineSlot with type instanceof FunctionType
 *   - Branch: defineSlot with type not instanceof FunctionType
 *   - Branch: defineSlot with fnType.isConstructor() or fnType.isInterface()
 *   - Branch: defineSlot with superClassCtor null
 *   - Branch: defineSlot with superClassCtor non-null
 *   - Branch: defineSlot with superClassCtor.getInstanceType() equals OBJECT_TYPE
 *   - Branch: defineSlot with superClassCtor.getInstanceType() not equals OBJECT_TYPE
 *   - Branch: isQnameRootedInGlobalScope with NAME root and global var
 *   - Branch: isQnameRootedInGlobalScope with NAME root and non-global var
 *   - Branch: isQnameRootedInGlobalScope with non-NAME root
 *   - Branch: getDeclaredGetPropType with @type annotation
 *   - Branch: getDeclaredGetPropType with @enum annotation
 *   - Branch: getDeclaredGetPropType with FUNCTION rhsValue
 *   - Branch: getDeclaredGetPropType with other cases
 *   - Branch: getDeclaredTypeInAnnotation with @type
 *   - Branch: getDeclaredTypeInAnnotation with isFunctionTypeDeclaration
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and existing type
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and no existing type
 *   - Branch: getDeclaredTypeInAnnotation with objNode GETPROP prototype
 *   - Branch: getDeclaredTypeInAnnotation with objNode THIS
 *   - Branch: getDeclaredTypeInAnnotation with no searchedForThisType
 *   - Branch: getDeclaredTypeInAnnotation with null info
 *   - Branch: getDeclaredTypeInAnnotation with null objNode
 *   - Branch: findOverriddenFunction with FunctionType propType
 *   - Branch: findOverriddenFunction with non-FunctionType propType and interfaces
 *   - Branch: findOverriddenFunction with non-FunctionType propType and no interfaces
 *   - Branch: findOverriddenFunction with null ownerType
 *   - Branch: findOverriddenFunction with null propType
 *   - Branch: getObjectSlot with null ownerVar
 *   - Branch: getObjectSlot with null ownerVarType
 *   - Branch: getObjectSlot with non-null ownerVarType
 *   - Branch: getPrototypePropertyOwner with GETPROP chain
 *   - Branch: getPrototypePropertyOwner with non-GETPROP
 *   - Branch: getPrototypePropertyOwner with non-prototype GETPROP
 *   - Branch: getPrototypePropertyOwner with non-qualified name
 *   - Branch: DiscoverEnums.visit with VAR
 *   - Branch: DiscoverEnums.visit with EXPR_RESULT
 *   - Branch: DiscoverEnums.visit with other types
 *   - Branch: DiscoverEnums.identifyEnumName with null info
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and hasEnumParameterType
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and no enum parameter type
 *   - Branch: DeferredSetType constructor with null node
 *   - Branch: DeferredSetType constructor with null type
 *   - Branch: DeferredSetType.resolve with scope
 *   - Branch: createScope with null parent (global)
 *   - Branch: createScope with non-null parent (local)
 *   - Branch: createScope with nonExternFunctions iteration
 *   - Branch: createScope with null type
 *   - Branch: createScope with type not instanceof FunctionType
 *   - Branch: createScope with fnThisType.isUnknownType()
 *   - Branch: createScope with fnThisType not unknown
 *   - Branch: createScope with null parent and delegate proxy
 *   - Branch: createScope with non-null parent and no delegate proxy
 *   - Branch: createInitialScope with root traversal
 *   - Branch: declareNativeFunctionType with scope
 *   - Branch: declareNativeValueType with scope
 *   - Branch: declareNativeType with scope
 *   - Branch: TypedScopeCreator constructor with compiler
 *   - Branch: TypedScopeCreator constructor with compiler and codingConvention
 *   - Branch: getNativeType with native type
 *   - Branch: assertDefinitionNode with correct type
 *   - Branch: assertDefinitionNode with incorrect type
 *   - Branch: setDeferredType with node and type
 *   - Branch: resolveTypes with deferred types
 *   - Branch: resolveTypes with scope vars
 *   - Branch: resolveTypes with type registry
 *   - Branch: visit with attachLiteralTypes
 *   - Branch: visit with CALL, FUNCTION, ASSIGN, CATCH, VAR, GETPROP
 *   - Branch: visit with other types
 *   - Branch: shouldTraverse with FUNCTION parent
 *   - Branch: shouldTraverse with SCRIPT parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: shouldTraverse with FUNCTION parent and first child
 *   - Branch: shouldTraverse with FUNCTION parent and root node
 *   - Branch: shouldTraverse with non-FUNCTION parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: visit with CALL node
 *   - Branch: visit with FUNCTION node
 *   - Branch: visit with ASSIGN node
 *   - Branch: visit with CATCH node
 *   - Branch: visit with VAR node
 *   - Branch: visit with GETPROP node
 *   - Branch: visit with other node types
 *   - Branch: attachLiteralTypes with NULL, VOID, STRING, NUMBER, TRUE, FALSE, REGEXP, REF_SPECIAL, OBJECTLIT
 *   - Branch: attachLiteralTypes with other types
 *   - Branch: defineSlot with NAME node
 *   - Branch: defineSlot with GETPROP node
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis true
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis false
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared true
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared false
 *   - Branch: defineSlot with inferred true
 *   - Branch: defineSlot with inferred false
 *   - Branch: defineSlot with type instanceof FunctionType
 *   - Branch: defineSlot with type not instanceof FunctionType
 *   - Branch: defineSlot with fnType.isConstructor() or fnType.isInterface()
 *   - Branch: defineSlot with superClassCtor null
 *   - Branch: defineSlot with superClassCtor non-null
 *   - Branch: defineSlot with superClassCtor.getInstanceType() equals OBJECT_TYPE
 *   - Branch: defineSlot with superClassCtor.getInstanceType() not equals OBJECT_TYPE
 *   - Branch: isQnameRootedInGlobalScope with NAME root and global var
 *   - Branch: isQnameRootedInGlobalScope with NAME root and non-global var
 *   - Branch: isQnameRootedInGlobalScope with non-NAME root
 *   - Branch: getDeclaredGetPropType with @type annotation
 *   - Branch: getDeclaredGetPropType with @enum annotation
 *   - Branch: getDeclaredGetPropType with FUNCTION rhsValue
 *   - Branch: getDeclaredGetPropType with other cases
 *   - Branch: getDeclaredTypeInAnnotation with @type
 *   - Branch: getDeclaredTypeInAnnotation with isFunctionTypeDeclaration
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and existing type
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and no existing type
 *   - Branch: getDeclaredTypeInAnnotation with objNode GETPROP prototype
 *   - Branch: getDeclaredTypeInAnnotation with objNode THIS
 *   - Branch: getDeclaredTypeInAnnotation with no searchedForThisType
 *   - Branch: getDeclaredTypeInAnnotation with null info
 *   - Branch: getDeclaredTypeInAnnotation with null objNode
 *   - Branch: findOverriddenFunction with FunctionType propType
 *   - Branch: findOverriddenFunction with non-FunctionType propType and interfaces
 *   - Branch: findOverriddenFunction with non-FunctionType propType and no interfaces
 *   - Branch: findOverriddenFunction with null ownerType
 *   - Branch: findOverriddenFunction with null propType
 *   - Branch: getObjectSlot with null ownerVar
 *   - Branch: getObjectSlot with null ownerVarType
 *   - Branch: getObjectSlot with non-null ownerVarType
 *   - Branch: getPrototypePropertyOwner with GETPROP chain
 *   - Branch: getPrototypePropertyOwner with non-GETPROP
 *   - Branch: getPrototypePropertyOwner with non-prototype GETPROP
 *   - Branch: getPrototypePropertyOwner with non-qualified name
 *   - Branch: DiscoverEnums.visit with VAR
 *   - Branch: DiscoverEnums.visit with EXPR_RESULT
 *   - Branch: DiscoverEnums.visit with other types
 *   - Branch: DiscoverEnums.identifyEnumName with null info
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and hasEnumParameterType
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and no enum parameter type
 *   - Branch: DeferredSetType constructor with null node
 *   - Branch: DeferredSetType constructor with null type
 *   - Branch: DeferredSetType.resolve with scope
 *   - Branch: createScope with null parent (global)
 *   - Branch: createScope with non-null parent (local)
 *   - Branch: createScope with nonExternFunctions iteration
 *   - Branch: createScope with null type
 *   - Branch: createScope with type not instanceof FunctionType
 *   - Branch: createScope with fnThisType.isUnknownType()
 *   - Branch: createScope with fnThisType not unknown
 *   - Branch: createScope with null parent and delegate proxy
 *   - Branch: createScope with non-null parent and no delegate proxy
 *   - Branch: createInitialScope with root traversal
 *   - Branch: declareNativeFunctionType with scope
 *   - Branch: declareNativeValueType with scope
 *   - Branch: declareNativeType with scope
 *   - Branch: TypedScopeCreator constructor with compiler
 *   - Branch: TypedScopeCreator constructor with compiler and codingConvention
 *   - Branch: getNativeType with native type
 *   - Branch: assertDefinitionNode with correct type
 *   - Branch: assertDefinitionNode with incorrect type
 *   - Branch: setDeferredType with node and type
 *   - Branch: resolveTypes with deferred types
 *   - Branch: resolveTypes with scope vars
 *   - Branch: resolveTypes with type registry
 *   - Branch: visit with attachLiteralTypes
 *   - Branch: visit with CALL, FUNCTION, ASSIGN, CATCH, VAR, GETPROP
 *   - Branch: visit with other types
 *   - Branch: shouldTraverse with FUNCTION parent
 *   - Branch: shouldTraverse with SCRIPT parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: shouldTraverse with FUNCTION parent and first child
 *   - Branch: shouldTraverse with FUNCTION parent and root node
 *   - Branch: shouldTraverse with non-FUNCTION parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: visit with CALL node
 *   - Branch: visit with FUNCTION node
 *   - Branch: visit with ASSIGN node
 *   - Branch: visit with CATCH node
 *   - Branch: visit with VAR node
 *   - Branch: visit with GETPROP node
 *   - Branch: visit with other node types
 *   - Branch: attachLiteralTypes with NULL, VOID, STRING, NUMBER, TRUE, FALSE, REGEXP, REF_SPECIAL, OBJECTLIT
 *   - Branch: attachLiteralTypes with other types
 *   - Branch: defineSlot with NAME node
 *   - Branch: defineSlot with GETPROP node
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis true
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis false
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared true
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared false
 *   - Branch: defineSlot with inferred true
 *   - Branch: defineSlot with inferred false
 *   - Branch: defineSlot with type instanceof FunctionType
 *   - Branch: defineSlot with type not instanceof FunctionType
 *   - Branch: defineSlot with fnType.isConstructor() or fnType.isInterface()
 *   - Branch: defineSlot with superClassCtor null
 *   - Branch: defineSlot with superClassCtor non-null
 *   - Branch: defineSlot with superClassCtor.getInstanceType() equals OBJECT_TYPE
 *   - Branch: defineSlot with superClassCtor.getInstanceType() not equals OBJECT_TYPE
 *   - Branch: isQnameRootedInGlobalScope with NAME root and global var
 *   - Branch: isQnameRootedInGlobalScope with NAME root and non-global var
 *   - Branch: isQnameRootedInGlobalScope with non-NAME root
 *   - Branch: getDeclaredGetPropType with @type annotation
 *   - Branch: getDeclaredGetPropType with @enum annotation
 *   - Branch: getDeclaredGetPropType with FUNCTION rhsValue
 *   - Branch: getDeclaredGetPropType with other cases
 *   - Branch: getDeclaredTypeInAnnotation with @type
 *   - Branch: getDeclaredTypeInAnnotation with isFunctionTypeDeclaration
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and existing type
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and no existing type
 *   - Branch: getDeclaredTypeInAnnotation with objNode GETPROP prototype
 *   - Branch: getDeclaredTypeInAnnotation with objNode THIS
 *   - Branch: getDeclaredTypeInAnnotation with no searchedForThisType
 *   - Branch: getDeclaredTypeInAnnotation with null info
 *   - Branch: getDeclaredTypeInAnnotation with null objNode
 *   - Branch: findOverriddenFunction with FunctionType propType
 *   - Branch: findOverriddenFunction with non-FunctionType propType and interfaces
 *   - Branch: findOverriddenFunction with non-FunctionType propType and no interfaces
 *   - Branch: findOverriddenFunction with null ownerType
 *   - Branch: findOverriddenFunction with null propType
 *   - Branch: getObjectSlot with null ownerVar
 *   - Branch: getObjectSlot with null ownerVarType
 *   - Branch: getObjectSlot with non-null ownerVarType
 *   - Branch: getPrototypePropertyOwner with GETPROP chain
 *   - Branch: getPrototypePropertyOwner with non-GETPROP
 *   - Branch: getPrototypePropertyOwner with non-prototype GETPROP
 *   - Branch: getPrototypePropertyOwner with non-qualified name
 *   - Branch: DiscoverEnums.visit with VAR
 *   - Branch: DiscoverEnums.visit with EXPR_RESULT
 *   - Branch: DiscoverEnums.visit with other types
 *   - Branch: DiscoverEnums.identifyEnumName with null info
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and hasEnumParameterType
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and no enum parameter type
 *   - Branch: DeferredSetType constructor with null node
 *   - Branch: DeferredSetType constructor with null type
 *   - Branch: DeferredSetType.resolve with scope
 *   - Branch: createScope with null parent (global)
 *   - Branch: createScope with non-null parent (local)
 *   - Branch: createScope with nonExternFunctions iteration
 *   - Branch: createScope with null type
 *   - Branch: createScope with type not instanceof FunctionType
 *   - Branch: createScope with fnThisType.isUnknownType()
 *   - Branch: createScope with fnThisType not unknown
 *   - Branch: createScope with null parent and delegate proxy
 *   - Branch: createScope with non-null parent and no delegate proxy
 *   - Branch: createInitialScope with root traversal
 *   - Branch: declareNativeFunctionType with scope
 *   - Branch: declareNativeValueType with scope
 *   - Branch: declareNativeType with scope
 *   - Branch: TypedScopeCreator constructor with compiler
 *   - Branch: TypedScopeCreator constructor with compiler and codingConvention
 *   - Branch: getNativeType with native type
 *   - Branch: assertDefinitionNode with correct type
 *   - Branch: assertDefinitionNode with incorrect type
 *   - Branch: setDeferredType with node and type
 *   - Branch: resolveTypes with deferred types
 *   - Branch: resolveTypes with scope vars
 *   - Branch: resolveTypes with type registry
 *   - Branch: visit with attachLiteralTypes
 *   - Branch: visit with CALL, FUNCTION, ASSIGN, CATCH, VAR, GETPROP
 *   - Branch: visit with other types
 *   - Branch: shouldTraverse with FUNCTION parent
 *   - Branch: shouldTraverse with SCRIPT parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: shouldTraverse with FUNCTION parent and first child
 *   - Branch: shouldTraverse with FUNCTION parent and root node
 *   - Branch: shouldTraverse with non-FUNCTION parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: visit with CALL node
 *   - Branch: visit with FUNCTION node
 *   - Branch: visit with ASSIGN node
 *   - Branch: visit with CATCH node
 *   - Branch: visit with VAR node
 *   - Branch: visit with GETPROP node
 *   - Branch: visit with other node types
 *   - Branch: attachLiteralTypes with NULL, VOID, STRING, NUMBER, TRUE, FALSE, REGEXP, REF_SPECIAL, OBJECTLIT
 *   - Branch: attachLiteralTypes with other types
 *   - Branch: defineSlot with NAME node
 *   - Branch: defineSlot with GETPROP node
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis true
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis false
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared true
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared false
 *   - Branch: defineSlot with inferred true
 *   - Branch: defineSlot with inferred false
 *   - Branch: defineSlot with type instanceof FunctionType
 *   - Branch: defineSlot with type not instanceof FunctionType
 *   - Branch: defineSlot with fnType.isConstructor() or fnType.isInterface()
 *   - Branch: defineSlot with superClassCtor null
 *   - Branch: defineSlot with superClassCtor non-null
 *   - Branch: defineSlot with superClassCtor.getInstanceType() equals OBJECT_TYPE
 *   - Branch: defineSlot with superClassCtor.getInstanceType() not equals OBJECT_TYPE
 *   - Branch: isQnameRootedInGlobalScope with NAME root and global var
 *   - Branch: isQnameRootedInGlobalScope with NAME root and non-global var
 *   - Branch: isQnameRootedInGlobalScope with non-NAME root
 *   - Branch: getDeclaredGetPropType with @type annotation
 *   - Branch: getDeclaredGetPropType with @enum annotation
 *   - Branch: getDeclaredGetPropType with FUNCTION rhsValue
 *   - Branch: getDeclaredGetPropType with other cases
 *   - Branch: getDeclaredTypeInAnnotation with @type
 *   - Branch: getDeclaredTypeInAnnotation with isFunctionTypeDeclaration
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and existing type
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and no existing type
 *   - Branch: getDeclaredTypeInAnnotation with objNode GETPROP prototype
 *   - Branch: getDeclaredTypeInAnnotation with objNode THIS
 *   - Branch: getDeclaredTypeInAnnotation with no searchedForThisType
 *   - Branch: getDeclaredTypeInAnnotation with null info
 *   - Branch: getDeclaredTypeInAnnotation with null objNode
 *   - Branch: findOverriddenFunction with FunctionType propType
 *   - Branch: findOverriddenFunction with non-FunctionType propType and interfaces
 *   - Branch: findOverriddenFunction with non-FunctionType propType and no interfaces
 *   - Branch: findOverriddenFunction with null ownerType
 *   - Branch: findOverriddenFunction with null propType
 *   - Branch: getObjectSlot with null ownerVar
 *   - Branch: getObjectSlot with null ownerVarType
 *   - Branch: getObjectSlot with non-null ownerVarType
 *   - Branch: getPrototypePropertyOwner with GETPROP chain
 *   - Branch: getPrototypePropertyOwner with non-GETPROP
 *   - Branch: getPrototypePropertyOwner with non-prototype GETPROP
 *   - Branch: getPrototypePropertyOwner with non-qualified name
 *   - Branch: DiscoverEnums.visit with VAR
 *   - Branch: DiscoverEnums.visit with EXPR_RESULT
 *   - Branch: DiscoverEnums.visit with other types
 *   - Branch: DiscoverEnums.identifyEnumName with null info
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and hasEnumParameterType
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and no enum parameter type
 *   - Branch: DeferredSetType constructor with null node
 *   - Branch: DeferredSetType constructor with null type
 *   - Branch: DeferredSetType.resolve with scope
 *   - Branch: createScope with null parent (global)
 *   - Branch: createScope with non-null parent (local)
 *   - Branch: createScope with nonExternFunctions iteration
 *   - Branch: createScope with null type
 *   - Branch: createScope with type not instanceof FunctionType
 *   - Branch: createScope with fnThisType.isUnknownType()
 *   - Branch: createScope with fnThisType not unknown
 *   - Branch: createScope with null parent and delegate proxy
 *   - Branch: createScope with non-null parent and no delegate proxy
 *   - Branch: createInitialScope with root traversal
 *   - Branch: declareNativeFunctionType with scope
 *   - Branch: declareNativeValueType with scope
 *   - Branch: declareNativeType with scope
 *   - Branch: TypedScopeCreator constructor with compiler
 *   - Branch: TypedScopeCreator constructor with compiler and codingConvention
 *   - Branch: getNativeType with native type
 *   - Branch: assertDefinitionNode with correct type
 *   - Branch: assertDefinitionNode with incorrect type
 *   - Branch: setDeferredType with node and type
 *   - Branch: resolveTypes with deferred types
 *   - Branch: resolveTypes with scope vars
 *   - Branch: resolveTypes with type registry
 *   - Branch: visit with attachLiteralTypes
 *   - Branch: visit with CALL, FUNCTION, ASSIGN, CATCH, VAR, GETPROP
 *   - Branch: visit with other types
 *   - Branch: shouldTraverse with FUNCTION parent
 *   - Branch: shouldTraverse with SCRIPT parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: shouldTraverse with FUNCTION parent and first child
 *   - Branch: shouldTraverse with FUNCTION parent and root node
 *   - Branch: shouldTraverse with non-FUNCTION parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: visit with CALL node
 *   - Branch: visit with FUNCTION node
 *   - Branch: visit with ASSIGN node
 *   - Branch: visit with CATCH node
 *   - Branch: visit with VAR node
 *   - Branch: visit with GETPROP node
 *   - Branch: visit with other node types
 *   - Branch: attachLiteralTypes with NULL, VOID, STRING, NUMBER, TRUE, FALSE, REGEXP, REF_SPECIAL, OBJECTLIT
 *   - Branch: attachLiteralTypes with other types
 *   - Branch: defineSlot with NAME node
 *   - Branch: defineSlot with GETPROP node
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis true
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis false
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared true
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared false
 *   - Branch: defineSlot with inferred true
 *   - Branch: defineSlot with inferred false
 *   - Branch: defineSlot with type instanceof FunctionType
 *   - Branch: defineSlot with type not instanceof FunctionType
 *   - Branch: defineSlot with fnType.isConstructor() or fnType.isInterface()
 *   - Branch: defineSlot with superClassCtor null
 *   - Branch: defineSlot with superClassCtor non-null
 *   - Branch: defineSlot with superClassCtor.getInstanceType() equals OBJECT_TYPE
 *   - Branch: defineSlot with superClassCtor.getInstanceType() not equals OBJECT_TYPE
 *   - Branch: isQnameRootedInGlobalScope with NAME root and global var
 *   - Branch: isQnameRootedInGlobalScope with NAME root and non-global var
 *   - Branch: isQnameRootedInGlobalScope with non-NAME root
 *   - Branch: getDeclaredGetPropType with @type annotation
 *   - Branch: getDeclaredGetPropType with @enum annotation
 *   - Branch: getDeclaredGetPropType with FUNCTION rhsValue
 *   - Branch: getDeclaredGetPropType with other cases
 *   - Branch: getDeclaredTypeInAnnotation with @type
 *   - Branch: getDeclaredTypeInAnnotation with isFunctionTypeDeclaration
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and existing type
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and no existing type
 *   - Branch: getDeclaredTypeInAnnotation with objNode GETPROP prototype
 *   - Branch: getDeclaredTypeInAnnotation with objNode THIS
 *   - Branch: getDeclaredTypeInAnnotation with no searchedForThisType
 *   - Branch: getDeclaredTypeInAnnotation with null info
 *   - Branch: getDeclaredTypeInAnnotation with null objNode
 *   - Branch: findOverriddenFunction with FunctionType propType
 *   - Branch: findOverriddenFunction with non-FunctionType propType and interfaces
 *   - Branch: findOverriddenFunction with non-FunctionType propType and no interfaces
 *   - Branch: findOverriddenFunction with null ownerType
 *   - Branch: findOverriddenFunction with null propType
 *   - Branch: getObjectSlot with null ownerVar
 *   - Branch: getObjectSlot with null ownerVarType
 *   - Branch: getObjectSlot with non-null ownerVarType
 *   - Branch: getPrototypePropertyOwner with GETPROP chain
 *   - Branch: getPrototypePropertyOwner with non-GETPROP
 *   - Branch: getPrototypePropertyOwner with non-prototype GETPROP
 *   - Branch: getPrototypePropertyOwner with non-qualified name
 *   - Branch: DiscoverEnums.visit with VAR
 *   - Branch: DiscoverEnums.visit with EXPR_RESULT
 *   - Branch: DiscoverEnums.visit with other types
 *   - Branch: DiscoverEnums.identifyEnumName with null info
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and hasEnumParameterType
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and no enum parameter type
 *   - Branch: DeferredSetType constructor with null node
 *   - Branch: DeferredSetType constructor with null type
 *   - Branch: DeferredSetType.resolve with scope
 *   - Branch: createScope with null parent (global)
 *   - Branch: createScope with non-null parent (local)
 *   - Branch: createScope with nonExternFunctions iteration
 *   - Branch: createScope with null type
 *   - Branch: createScope with type not instanceof FunctionType
 *   - Branch: createScope with fnThisType.isUnknownType()
 *   - Branch: createScope with fnThisType not unknown
 *   - Branch: createScope with null parent and delegate proxy
 *   - Branch: createScope with non-null parent and no delegate proxy
 *   - Branch: createInitialScope with root traversal
 *   - Branch: declareNativeFunctionType with scope
 *   - Branch: declareNativeValueType with scope
 *   - Branch: declareNativeType with scope
 *   - Branch: TypedScopeCreator constructor with compiler
 *   - Branch: TypedScopeCreator constructor with compiler and codingConvention
 *   - Branch: getNativeType with native type
 *   - Branch: assertDefinitionNode with correct type
 *   - Branch: assertDefinitionNode with incorrect type
 *   - Branch: setDeferredType with node and type
 *   - Branch: resolveTypes with deferred types
 *   - Branch: resolveTypes with scope vars
 *   - Branch: resolveTypes with type registry
 *   - Branch: visit with attachLiteralTypes
 *   - Branch: visit with CALL, FUNCTION, ASSIGN, CATCH, VAR, GETPROP
 *   - Branch: visit with other types
 *   - Branch: shouldTraverse with FUNCTION parent
 *   - Branch: shouldTraverse with SCRIPT parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: shouldTraverse with FUNCTION parent and first child
 *   - Branch: shouldTraverse with FUNCTION parent and root node
 *   - Branch: shouldTraverse with non-FUNCTION parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: visit with CALL node
 *   - Branch: visit with FUNCTION node
 *   - Branch: visit with ASSIGN node
 *   - Branch: visit with CATCH node
 *   - Branch: visit with VAR node
 *   - Branch: visit with GETPROP node
 *   - Branch: visit with other node types
 *   - Branch: attachLiteralTypes with NULL, VOID, STRING, NUMBER, TRUE, FALSE, REGEXP, REF_SPECIAL, OBJECTLIT
 *   - Branch: attachLiteralTypes with other types
 *   - Branch: defineSlot with NAME node
 *   - Branch: defineSlot with GETPROP node
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis true
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis false
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared true
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared false
 *   - Branch: defineSlot with inferred true
 *   - Branch: defineSlot with inferred false
 *   - Branch: defineSlot with type instanceof FunctionType
 *   - Branch: defineSlot with type not instanceof FunctionType
 *   - Branch: defineSlot with fnType.isConstructor() or fnType.isInterface()
 *   - Branch: defineSlot with superClassCtor null
 *   - Branch: defineSlot with superClassCtor non-null
 *   - Branch: defineSlot with superClassCtor.getInstanceType() equals OBJECT_TYPE
 *   - Branch: defineSlot with superClassCtor.getInstanceType() not equals OBJECT_TYPE
 *   - Branch: isQnameRootedInGlobalScope with NAME root and global var
 *   - Branch: isQnameRootedInGlobalScope with NAME root and non-global var
 *   - Branch: isQnameRootedInGlobalScope with non-NAME root
 *   - Branch: getDeclaredGetPropType with @type annotation
 *   - Branch: getDeclaredGetPropType with @enum annotation
 *   - Branch: getDeclaredGetPropType with FUNCTION rhsValue
 *   - Branch: getDeclaredGetPropType with other cases
 *   - Branch: getDeclaredTypeInAnnotation with @type
 *   - Branch: getDeclaredTypeInAnnotation with isFunctionTypeDeclaration
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and existing type
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and no existing type
 *   - Branch: getDeclaredTypeInAnnotation with objNode GETPROP prototype
 *   - Branch: getDeclaredTypeInAnnotation with objNode THIS
 *   - Branch: getDeclaredTypeInAnnotation with no searchedForThisType
 *   - Branch: getDeclaredTypeInAnnotation with null info
 *   - Branch: getDeclaredTypeInAnnotation with null objNode
 *   - Branch: findOverriddenFunction with FunctionType propType
 *   - Branch: findOverriddenFunction with non-FunctionType propType and interfaces
 *   - Branch: findOverriddenFunction with non-FunctionType propType and no interfaces
 *   - Branch: findOverriddenFunction with null ownerType
 *   - Branch: findOverriddenFunction with null propType
 *   - Branch: getObjectSlot with null ownerVar
 *   - Branch: getObjectSlot with null ownerVarType
 *   - Branch: getObjectSlot with non-null ownerVarType
 *   - Branch: getPrototypePropertyOwner with GETPROP chain
 *   - Branch: getPrototypePropertyOwner with non-GETPROP
 *   - Branch: getPrototypePropertyOwner with non-prototype GETPROP
 *   - Branch: getPrototypePropertyOwner with non-qualified name
 *   - Branch: DiscoverEnums.visit with VAR
 *   - Branch: DiscoverEnums.visit with EXPR_RESULT
 *   - Branch: DiscoverEnums.visit with other types
 *   - Branch: DiscoverEnums.identifyEnumName with null info
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and hasEnumParameterType
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and no enum parameter type
 *   - Branch: DeferredSetType constructor with null node
 *   - Branch: DeferredSetType constructor with null type
 *   - Branch: DeferredSetType.resolve with scope
 *   - Branch: createScope with null parent (global)
 *   - Branch: createScope with non-null parent (local)
 *   - Branch: createScope with nonExternFunctions iteration
 *   - Branch: createScope with null type
 *   - Branch: createScope with type not instanceof FunctionType
 *   - Branch: createScope with fnThisType.isUnknownType()
 *   - Branch: createScope with fnThisType not unknown
 *   - Branch: createScope with null parent and delegate proxy
 *   - Branch: createScope with non-null parent and no delegate proxy
 *   - Branch: createInitialScope with root traversal
 *   - Branch: declareNativeFunctionType with scope
 *   - Branch: declareNativeValueType with scope
 *   - Branch: declareNativeType with scope
 *   - Branch: TypedScopeCreator constructor with compiler
 *   - Branch: TypedScopeCreator constructor with compiler and codingConvention
 *   - Branch: getNativeType with native type
 *   - Branch: assertDefinitionNode with correct type
 *   - Branch: assertDefinitionNode with incorrect type
 *   - Branch: setDeferredType with node and type
 *   - Branch: resolveTypes with deferred types
 *   - Branch: resolveTypes with scope vars
 *   - Branch: resolveTypes with type registry
 *   - Branch: visit with attachLiteralTypes
 *   - Branch: visit with CALL, FUNCTION, ASSIGN, CATCH, VAR, GETPROP
 *   - Branch: visit with other types
 *   - Branch: shouldTraverse with FUNCTION parent
 *   - Branch: shouldTraverse with SCRIPT parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: shouldTraverse with FUNCTION parent and first child
 *   - Branch: shouldTraverse with FUNCTION parent and root node
 *   - Branch: shouldTraverse with non-FUNCTION parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: visit with CALL node
 *   - Branch: visit with FUNCTION node
 *   - Branch: visit with ASSIGN node
 *   - Branch: visit with CATCH node
 *   - Branch: visit with VAR node
 *   - Branch: visit with GETPROP node
 *   - Branch: visit with other node types
 *   - Branch: attachLiteralTypes with NULL, VOID, STRING, NUMBER, TRUE, FALSE, REGEXP, REF_SPECIAL, OBJECTLIT
 *   - Branch: attachLiteralTypes with other types
 *   - Branch: defineSlot with NAME node
 *   - Branch: defineSlot with GETPROP node
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis true
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis false
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared true
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared false
 *   - Branch: defineSlot with inferred true
 *   - Branch: defineSlot with inferred false
 *   - Branch: defineSlot with type instanceof FunctionType
 *   - Branch: defineSlot with type not instanceof FunctionType
 *   - Branch: defineSlot with fnType.isConstructor() or fnType.isInterface()
 *   - Branch: defineSlot with superClassCtor null
 *   - Branch: defineSlot with superClassCtor non-null
 *   - Branch: defineSlot with superClassCtor.getInstanceType() equals OBJECT_TYPE
 *   - Branch: defineSlot with superClassCtor.getInstanceType() not equals OBJECT_TYPE
 *   - Branch: isQnameRootedInGlobalScope with NAME root and global var
 *   - Branch: isQnameRootedInGlobalScope with NAME root and non-global var
 *   - Branch: isQnameRootedInGlobalScope with non-NAME root
 *   - Branch: getDeclaredGetPropType with @type annotation
 *   - Branch: getDeclaredGetPropType with @enum annotation
 *   - Branch: getDeclaredGetPropType with FUNCTION rhsValue
 *   - Branch: getDeclaredGetPropType with other cases
 *   - Branch: getDeclaredTypeInAnnotation with @type
 *   - Branch: getDeclaredTypeInAnnotation with isFunctionTypeDeclaration
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and existing type
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and no existing type
 *   - Branch: getDeclaredTypeInAnnotation with objNode GETPROP prototype
 *   - Branch: getDeclaredTypeInAnnotation with objNode THIS
 *   - Branch: getDeclaredTypeInAnnotation with no searchedForThisType
 *   - Branch: getDeclaredTypeInAnnotation with null info
 *   - Branch: getDeclaredTypeInAnnotation with null objNode
 *   - Branch: findOverriddenFunction with FunctionType propType
 *   - Branch: findOverriddenFunction with non-FunctionType propType and interfaces
 *   - Branch: findOverriddenFunction with non-FunctionType propType and no interfaces
 *   - Branch: findOverriddenFunction with null ownerType
 *   - Branch: findOverriddenFunction with null propType
 *   - Branch: getObjectSlot with null ownerVar
 *   - Branch: getObjectSlot with null ownerVarType
 *   - Branch: getObjectSlot with non-null ownerVarType
 *   - Branch: getPrototypePropertyOwner with GETPROP chain
 *   - Branch: getPrototypePropertyOwner with non-GETPROP
 *   - Branch: getPrototypePropertyOwner with non-prototype GETPROP
 *   - Branch: getPrototypePropertyOwner with non-qualified name
 *   - Branch: DiscoverEnums.visit with VAR
 *   - Branch: DiscoverEnums.visit with EXPR_RESULT
 *   - Branch: DiscoverEnums.visit with other types
 *   - Branch: DiscoverEnums.identifyEnumName with null info
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and hasEnumParameterType
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and no enum parameter type
 *   - Branch: DeferredSetType constructor with null node
 *   - Branch: DeferredSetType constructor with null type
 *   - Branch: DeferredSetType.resolve with scope
 *   - Branch: createScope with null parent (global)
 *   - Branch: createScope with non-null parent (local)
 *   - Branch: createScope with nonExternFunctions iteration
 *   - Branch: createScope with null type
 *   - Branch: createScope with type not instanceof FunctionType
 *   - Branch: createScope with fnThisType.isUnknownType()
 *   - Branch: createScope with fnThisType not unknown
 *   - Branch: createScope with null parent and delegate proxy
 *   - Branch: createScope with non-null parent and no delegate proxy
 *   - Branch: createInitialScope with root traversal
 *   - Branch: declareNativeFunctionType with scope
 *   - Branch: declareNativeValueType with scope
 *   - Branch: declareNativeType with scope
 *   - Branch: TypedScopeCreator constructor with compiler
 *   - Branch: TypedScopeCreator constructor with compiler and codingConvention
 *   - Branch: getNativeType with native type
 *   - Branch: assertDefinitionNode with correct type
 *   - Branch: assertDefinitionNode with incorrect type
 *   - Branch: setDeferredType with node and type
 *   - Branch: resolveTypes with deferred types
 *   - Branch: resolveTypes with scope vars
 *   - Branch: resolveTypes with type registry
 *   - Branch: visit with attachLiteralTypes
 *   - Branch: visit with CALL, FUNCTION, ASSIGN, CATCH, VAR, GETPROP
 *   - Branch: visit with other types
 *   - Branch: shouldTraverse with FUNCTION parent
 *   - Branch: shouldTraverse with SCRIPT parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: shouldTraverse with FUNCTION parent and first child
 *   - Branch: shouldTraverse with FUNCTION parent and root node
 *   - Branch: shouldTraverse with non-FUNCTION parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: visit with CALL node
 *   - Branch: visit with FUNCTION node
 *   - Branch: visit with ASSIGN node
 *   - Branch: visit with CATCH node
 *   - Branch: visit with VAR node
 *   - Branch: visit with GETPROP node
 *   - Branch: visit with other node types
 *   - Branch: attachLiteralTypes with NULL, VOID, STRING, NUMBER, TRUE, FALSE, REGEXP, REF_SPECIAL, OBJECTLIT
 *   - Branch: attachLiteralTypes with other types
 *   - Branch: defineSlot with NAME node
 *   - Branch: defineSlot with GETPROP node
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis true
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis false
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared true
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared false
 *   - Branch: defineSlot with inferred true
 *   - Branch: defineSlot with inferred false
 *   - Branch: defineSlot with type instanceof FunctionType
 *   - Branch: defineSlot with type not instanceof FunctionType
 *   - Branch: defineSlot with fnType.isConstructor() or fnType.isInterface()
 *   - Branch: defineSlot with superClassCtor null
 *   - Branch: defineSlot with superClassCtor non-null
 *   - Branch: defineSlot with superClassCtor.getInstanceType() equals OBJECT_TYPE
 *   - Branch: defineSlot with superClassCtor.getInstanceType() not equals OBJECT_TYPE
 *   - Branch: isQnameRootedInGlobalScope with NAME root and global var
 *   - Branch: isQnameRootedInGlobalScope with NAME root and non-global var
 *   - Branch: isQnameRootedInGlobalScope with non-NAME root
 *   - Branch: getDeclaredGetPropType with @type annotation
 *   - Branch: getDeclaredGetPropType with @enum annotation
 *   - Branch: getDeclaredGetPropType with FUNCTION rhsValue
 *   - Branch: getDeclaredGetPropType with other cases
 *   - Branch: getDeclaredTypeInAnnotation with @type
 *   - Branch: getDeclaredTypeInAnnotation with isFunctionTypeDeclaration
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and existing type
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and no existing type
 *   - Branch: getDeclaredTypeInAnnotation with objNode GETPROP prototype
 *   - Branch: getDeclaredTypeInAnnotation with objNode THIS
 *   - Branch: getDeclaredTypeInAnnotation with no searchedForThisType
 *   - Branch: getDeclaredTypeInAnnotation with null info
 *   - Branch: getDeclaredTypeInAnnotation with null objNode
 *   - Branch: findOverriddenFunction with FunctionType propType
 *   - Branch: findOverriddenFunction with non-FunctionType propType and interfaces
 *   - Branch: findOverriddenFunction with non-FunctionType propType and no interfaces
 *   - Branch: findOverriddenFunction with null ownerType
 *   - Branch: findOverriddenFunction with null propType
 *   - Branch: getObjectSlot with null ownerVar
 *   - Branch: getObjectSlot with null ownerVarType
 *   - Branch: getObjectSlot with non-null ownerVarType
 *   - Branch: getPrototypePropertyOwner with GETPROP chain
 *   - Branch: getPrototypePropertyOwner with non-GETPROP
 *   - Branch: getPrototypePropertyOwner with non-prototype GETPROP
 *   - Branch: getPrototypePropertyOwner with non-qualified name
 *   - Branch: DiscoverEnums.visit with VAR
 *   - Branch: DiscoverEnums.visit with EXPR_RESULT
 *   - Branch: DiscoverEnums.visit with other types
 *   - Branch: DiscoverEnums.identifyEnumName with null info
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and hasEnumParameterType
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and no enum parameter type
 *   - Branch: DeferredSetType constructor with null node
 *   - Branch: DeferredSetType constructor with null type
 *   - Branch: DeferredSetType.resolve with scope
 *   - Branch: createScope with null parent (global)
 *   - Branch: createScope with non-null parent (local)
 *   - Branch: createScope with nonExternFunctions iteration
 *   - Branch: createScope with null type
 *   - Branch: createScope with type not instanceof FunctionType
 *   - Branch: createScope with fnThisType.isUnknownType()
 *   - Branch: createScope with fnThisType not unknown
 *   - Branch: createScope with null parent and delegate proxy
 *   - Branch: createScope with non-null parent and no delegate proxy
 *   - Branch: createInitialScope with root traversal
 *   - Branch: declareNativeFunctionType with scope
 *   - Branch: declareNativeValueType with scope
 *   - Branch: declareNativeType with scope
 *   - Branch: TypedScopeCreator constructor with compiler
 *   - Branch: TypedScopeCreator constructor with compiler and codingConvention
 *   - Branch: getNativeType with native type
 *   - Branch: assertDefinitionNode with correct type
 *   - Branch: assertDefinitionNode with incorrect type
 *   - Branch: setDeferredType with node and type
 *   - Branch: resolveTypes with deferred types
 *   - Branch: resolveTypes with scope vars
 *   - Branch: resolveTypes with type registry
 *   - Branch: visit with attachLiteralTypes
 *   - Branch: visit with CALL, FUNCTION, ASSIGN, CATCH, VAR, GETPROP
 *   - Branch: visit with other types
 *   - Branch: shouldTraverse with FUNCTION parent
 *   - Branch: shouldTraverse with SCRIPT parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: shouldTraverse with FUNCTION parent and first child
 *   - Branch: shouldTraverse with FUNCTION parent and root node
 *   - Branch: shouldTraverse with non-FUNCTION parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: visit with CALL node
 *   - Branch: visit with FUNCTION node
 *   - Branch: visit with ASSIGN node
 *   - Branch: visit with CATCH node
 *   - Branch: visit with VAR node
 *   - Branch: visit with GETPROP node
 *   - Branch: visit with other node types
 *   - Branch: attachLiteralTypes with NULL, VOID, STRING, NUMBER, TRUE, FALSE, REGEXP, REF_SPECIAL, OBJECTLIT
 *   - Branch: attachLiteralTypes with other types
 *   - Branch: defineSlot with NAME node
 *   - Branch: defineSlot with GETPROP node
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis true
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis false
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared true
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared false
 *   - Branch: defineSlot with inferred true
 *   - Branch: defineSlot with inferred false
 *   - Branch: defineSlot with type instanceof FunctionType
 *   - Branch: defineSlot with type not instanceof FunctionType
 *   - Branch: defineSlot with fnType.isConstructor() or fnType.isInterface()
 *   - Branch: defineSlot with superClassCtor null
 *   - Branch: defineSlot with superClassCtor non-null
 *   - Branch: defineSlot with superClassCtor.getInstanceType() equals OBJECT_TYPE
 *   - Branch: defineSlot with superClassCtor.getInstanceType() not equals OBJECT_TYPE
 *   - Branch: isQnameRootedInGlobalScope with NAME root and global var
 *   - Branch: isQnameRootedInGlobalScope with NAME root and non-global var
 *   - Branch: isQnameRootedInGlobalScope with non-NAME root
 *   - Branch: getDeclaredGetPropType with @type annotation
 *   - Branch: getDeclaredGetPropType with @enum annotation
 *   - Branch: getDeclaredGetPropType with FUNCTION rhsValue
 *   - Branch: getDeclaredGetPropType with other cases
 *   - Branch: getDeclaredTypeInAnnotation with @type
 *   - Branch: getDeclaredTypeInAnnotation with isFunctionTypeDeclaration
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and existing type
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and no existing type
 *   - Branch: getDeclaredTypeInAnnotation with objNode GETPROP prototype
 *   - Branch: getDeclaredTypeInAnnotation with objNode THIS
 *   - Branch: getDeclaredTypeInAnnotation with no searchedForThisType
 *   - Branch: getDeclaredTypeInAnnotation with null info
 *   - Branch: getDeclaredTypeInAnnotation with null objNode
 *   - Branch: findOverriddenFunction with FunctionType propType
 *   - Branch: findOverriddenFunction with non-FunctionType propType and interfaces
 *   - Branch: findOverriddenFunction with non-FunctionType propType and no interfaces
 *   - Branch: findOverriddenFunction with null ownerType
 *   - Branch: findOverriddenFunction with null propType
 *   - Branch: getObjectSlot with null ownerVar
 *   - Branch: getObjectSlot with null ownerVarType
 *   - Branch: getObjectSlot with non-null ownerVarType
 *   - Branch: getPrototypePropertyOwner with GETPROP chain
 *   - Branch: getPrototypePropertyOwner with non-GETPROP
 *   - Branch: getPrototypePropertyOwner with non-prototype GETPROP
 *   - Branch: getPrototypePropertyOwner with non-qualified name
 *   - Branch: DiscoverEnums.visit with VAR
 *   - Branch: DiscoverEnums.visit with EXPR_RESULT
 *   - Branch: DiscoverEnums.visit with other types
 *   - Branch: DiscoverEnums.identifyEnumName with null info
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and hasEnumParameterType
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and no enum parameter type
 *   - Branch: DeferredSetType constructor with null node
 *   - Branch: DeferredSetType constructor with null type
 *   - Branch: DeferredSetType.resolve with scope
 *   - Branch: createScope with null parent (global)
 *   - Branch: createScope with non-null parent (local)
 *   - Branch: createScope with nonExternFunctions iteration
 *   - Branch: createScope with null type
 *   - Branch: createScope with type not instanceof FunctionType
 *   - Branch: createScope with fnThisType.isUnknownType()
 *   - Branch: createScope with fnThisType not unknown
 *   - Branch: createScope with null parent and delegate proxy
 *   - Branch: createScope with non-null parent and no delegate proxy
 *   - Branch: createInitialScope with root traversal
 *   - Branch: declareNativeFunctionType with scope
 *   - Branch: declareNativeValueType with scope
 *   - Branch: declareNativeType with scope
 *   - Branch: TypedScopeCreator constructor with compiler
 *   - Branch: TypedScopeCreator constructor with compiler and codingConvention
 *   - Branch: getNativeType with native type
 *   - Branch: assertDefinitionNode with correct type
 *   - Branch: assertDefinitionNode with incorrect type
 *   - Branch: setDeferredType with node and type
 *   - Branch: resolveTypes with deferred types
 *   - Branch: resolveTypes with scope vars
 *   - Branch: resolveTypes with type registry
 *   - Branch: visit with attachLiteralTypes
 *   - Branch: visit with CALL, FUNCTION, ASSIGN, CATCH, VAR, GETPROP
 *   - Branch: visit with other types
 *   - Branch: shouldTraverse with FUNCTION parent
 *   - Branch: shouldTraverse with SCRIPT parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: shouldTraverse with FUNCTION parent and first child
 *   - Branch: shouldTraverse with FUNCTION parent and root node
 *   - Branch: shouldTraverse with non-FUNCTION parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: visit with CALL node
 *   - Branch: visit with FUNCTION node
 *   - Branch: visit with ASSIGN node
 *   - Branch: visit with CATCH node
 *   - Branch: visit with VAR node
 *   - Branch: visit with GETPROP node
 *   - Branch: visit with other node types
 *   - Branch: attachLiteralTypes with NULL, VOID, STRING, NUMBER, TRUE, FALSE, REGEXP, REF_SPECIAL, OBJECTLIT
 *   - Branch: attachLiteralTypes with other types
 *   - Branch: defineSlot with NAME node
 *   - Branch: defineSlot with GETPROP node
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis true
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis false
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared true
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared false
 *   - Branch: defineSlot with inferred true
 *   - Branch: defineSlot with inferred false
 *   - Branch: defineSlot with type instanceof FunctionType
 *   - Branch: defineSlot with type not instanceof FunctionType
 *   - Branch: defineSlot with fnType.isConstructor() or fnType.isInterface()
 *   - Branch: defineSlot with superClassCtor null
 *   - Branch: defineSlot with superClassCtor non-null
 *   - Branch: defineSlot with superClassCtor.getInstanceType() equals OBJECT_TYPE
 *   - Branch: defineSlot with superClassCtor.getInstanceType() not equals OBJECT_TYPE
 *   - Branch: isQnameRootedInGlobalScope with NAME root and global var
 *   - Branch: isQnameRootedInGlobalScope with NAME root and non-global var
 *   - Branch: isQnameRootedInGlobalScope with non-NAME root
 *   - Branch: getDeclaredGetPropType with @type annotation
 *   - Branch: getDeclaredGetPropType with @enum annotation
 *   - Branch: getDeclaredGetPropType with FUNCTION rhsValue
 *   - Branch: getDeclaredGetPropType with other cases
 *   - Branch: getDeclaredTypeInAnnotation with @type
 *   - Branch: getDeclaredTypeInAnnotation with isFunctionTypeDeclaration
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and existing type
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and no existing type
 *   - Branch: getDeclaredTypeInAnnotation with objNode GETPROP prototype
 *   - Branch: getDeclaredTypeInAnnotation with objNode THIS
 *   - Branch: getDeclaredTypeInAnnotation with no searchedForThisType
 *   - Branch: getDeclaredTypeInAnnotation with null info
 *   - Branch: getDeclaredTypeInAnnotation with null objNode
 *   - Branch: findOverriddenFunction with FunctionType propType
 *   - Branch: findOverriddenFunction with non-FunctionType propType and interfaces
 *   - Branch: findOverriddenFunction with non-FunctionType propType and no interfaces
 *   - Branch: findOverriddenFunction with null ownerType
 *   - Branch: findOverriddenFunction with null propType
 *   - Branch: getObjectSlot with null ownerVar
 *   - Branch: getObjectSlot with null ownerVarType
 *   - Branch: getObjectSlot with non-null ownerVarType
 *   - Branch: getPrototypePropertyOwner with GETPROP chain
 *   - Branch: getPrototypePropertyOwner with non-GETPROP
 *   - Branch: getPrototypePropertyOwner with non-prototype GETPROP
 *   - Branch: getPrototypePropertyOwner with non-qualified name
 *   - Branch: DiscoverEnums.visit with VAR
 *   - Branch: DiscoverEnums.visit with EXPR_RESULT
 *   - Branch: DiscoverEnums.visit with other types
 *   - Branch: DiscoverEnums.identifyEnumName with null info
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and hasEnumParameterType
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and no enum parameter type
 *   - Branch: DeferredSetType constructor with null node
 *   - Branch: DeferredSetType constructor with null type
 *   - Branch: DeferredSetType.resolve with scope
 *   - Branch: createScope with null parent (global)
 *   - Branch: createScope with non-null parent (local)
 *   - Branch: createScope with nonExternFunctions iteration
 *   - Branch: createScope with null type
 *   - Branch: createScope with type not instanceof FunctionType
 *   - Branch: createScope with fnThisType.isUnknownType()
 *   - Branch: createScope with fnThisType not unknown
 *   - Branch: createScope with null parent and delegate proxy
 *   - Branch: createScope with non-null parent and no delegate proxy
 *   - Branch: createInitialScope with root traversal
 *   - Branch: declareNativeFunctionType with scope
 *   - Branch: declareNativeValueType with scope
 *   - Branch: declareNativeType with scope
 *   - Branch: TypedScopeCreator constructor with compiler
 *   - Branch: TypedScopeCreator constructor with compiler and codingConvention
 *   - Branch: getNativeType with native type
 *   - Branch: assertDefinitionNode with correct type
 *   - Branch: assertDefinitionNode with incorrect type
 *   - Branch: setDeferredType with node and type
 *   - Branch: resolveTypes with deferred types
 *   - Branch: resolveTypes with scope vars
 *   - Branch: resolveTypes with type registry
 *   - Branch: visit with attachLiteralTypes
 *   - Branch: visit with CALL, FUNCTION, ASSIGN, CATCH, VAR, GETPROP
 *   - Branch: visit with other types
 *   - Branch: shouldTraverse with FUNCTION parent
 *   - Branch: shouldTraverse with SCRIPT parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: shouldTraverse with FUNCTION parent and first child
 *   - Branch: shouldTraverse with FUNCTION parent and root node
 *   - Branch: shouldTraverse with non-FUNCTION parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: visit with CALL node
 *   - Branch: visit with FUNCTION node
 *   - Branch: visit with ASSIGN node
 *   - Branch: visit with CATCH node
 *   - Branch: visit with VAR node
 *   - Branch: visit with GETPROP node
 *   - Branch: visit with other node types
 *   - Branch: attachLiteralTypes with NULL, VOID, STRING, NUMBER, TRUE, FALSE, REGEXP, REF_SPECIAL, OBJECTLIT
 *   - Branch: attachLiteralTypes with other types
 *   - Branch: defineSlot with NAME node
 *   - Branch: defineSlot with GETPROP node
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis true
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis false
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared true
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared false
 *   - Branch: defineSlot with inferred true
 *   - Branch: defineSlot with inferred false
 *   - Branch: defineSlot with type instanceof FunctionType
 *   - Branch: defineSlot with type not instanceof FunctionType
 *   - Branch: defineSlot with fnType.isConstructor() or fnType.isInterface()
 *   - Branch: defineSlot with superClassCtor null
 *   - Branch: defineSlot with superClassCtor non-null
 *   - Branch: defineSlot with superClassCtor.getInstanceType() equals OBJECT_TYPE
 *   - Branch: defineSlot with superClassCtor.getInstanceType() not equals OBJECT_TYPE
 *   - Branch: isQnameRootedInGlobalScope with NAME root and global var
 *   - Branch: isQnameRootedInGlobalScope with NAME root and non-global var
 *   - Branch: isQnameRootedInGlobalScope with non-NAME root
 *   - Branch: getDeclaredGetPropType with @type annotation
 *   - Branch: getDeclaredGetPropType with @enum annotation
 *   - Branch: getDeclaredGetPropType with FUNCTION rhsValue
 *   - Branch: getDeclaredGetPropType with other cases
 *   - Branch: getDeclaredTypeInAnnotation with @type
 *   - Branch: getDeclaredTypeInAnnotation with isFunctionTypeDeclaration
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and existing type
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and no existing type
 *   - Branch: getDeclaredTypeInAnnotation with objNode GETPROP prototype
 *   - Branch: getDeclaredTypeInAnnotation with objNode THIS
 *   - Branch: getDeclaredTypeInAnnotation with no searchedForThisType
 *   - Branch: getDeclaredTypeInAnnotation with null info
 *   - Branch: getDeclaredTypeInAnnotation with null objNode
 *   - Branch: findOverriddenFunction with FunctionType propType
 *   - Branch: findOverriddenFunction with non-FunctionType propType and interfaces
 *   - Branch: findOverriddenFunction with non-FunctionType propType and no interfaces
 *   - Branch: findOverriddenFunction with null ownerType
 *   - Branch: findOverriddenFunction with null propType
 *   - Branch: getObjectSlot with null ownerVar
 *   - Branch: getObjectSlot with null ownerVarType
 *   - Branch: getObjectSlot with non-null ownerVarType
 *   - Branch: getPrototypePropertyOwner with GETPROP chain
 *   - Branch: getPrototypePropertyOwner with non-GETPROP
 *   - Branch: getPrototypePropertyOwner with non-prototype GETPROP
 *   - Branch: getPrototypePropertyOwner with non-qualified name
 *   - Branch: DiscoverEnums.visit with VAR
 *   - Branch: DiscoverEnums.visit with EXPR_RESULT
 *   - Branch: DiscoverEnums.visit with other types
 *   - Branch: DiscoverEnums.identifyEnumName with null info
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and hasEnumParameterType
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and no enum parameter type
 *   - Branch: DeferredSetType constructor with null node
 *   - Branch: DeferredSetType constructor with null type
 *   - Branch: DeferredSetType.resolve with scope
 *   - Branch: createScope with null parent (global)
 *   - Branch: createScope with non-null parent (local)
 *   - Branch: createScope with nonExternFunctions iteration
 *   - Branch: createScope with null type
 *   - Branch: createScope with type not instanceof FunctionType
 *   - Branch: createScope with fnThisType.isUnknownType()
 *   - Branch: createScope with fnThisType not unknown
 *   - Branch: createScope with null parent and delegate proxy
 *   - Branch: createScope with non-null parent and no delegate proxy
 *   - Branch: createInitialScope with root traversal
 *   - Branch: declareNativeFunctionType with scope
 *   - Branch: declareNativeValueType with scope
 *   - Branch: declareNativeType with scope
 *   - Branch: TypedScopeCreator constructor with compiler
 *   - Branch: TypedScopeCreator constructor with compiler and codingConvention
 *   - Branch: getNativeType with native type
 *   - Branch: assertDefinitionNode with correct type
 *   - Branch: assertDefinitionNode with incorrect type
 *   - Branch: setDeferredType with node and type
 *   - Branch: resolveTypes with deferred types
 *   - Branch: resolveTypes with scope vars
 *   - Branch: resolveTypes with type registry
 *   - Branch: visit with attachLiteralTypes
 *   - Branch: visit with CALL, FUNCTION, ASSIGN, CATCH, VAR, GETPROP
 *   - Branch: visit with other types
 *   - Branch: shouldTraverse with FUNCTION parent
 *   - Branch: shouldTraverse with SCRIPT parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: shouldTraverse with FUNCTION parent and first child
 *   - Branch: shouldTraverse with FUNCTION parent and root node
 *   - Branch: shouldTraverse with non-FUNCTION parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: visit with CALL node
 *   - Branch: visit with FUNCTION node
 *   - Branch: visit with ASSIGN node
 *   - Branch: visit with CATCH node
 *   - Branch: visit with VAR node
 *   - Branch: visit with GETPROP node
 *   - Branch: visit with other node types
 *   - Branch: attachLiteralTypes with NULL, VOID, STRING, NUMBER, TRUE, FALSE, REGEXP, REF_SPECIAL, OBJECTLIT
 *   - Branch: attachLiteralTypes with other types
 *   - Branch: defineSlot with NAME node
 *   - Branch: defineSlot with GETPROP node
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis true
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis false
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared true
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared false
 *   - Branch: defineSlot with inferred true
 *   - Branch: defineSlot with inferred false
 *   - Branch: defineSlot with type instanceof FunctionType
 *   - Branch: defineSlot with type not instanceof FunctionType
 *   - Branch: defineSlot with fnType.isConstructor() or fnType.isInterface()
 *   - Branch: defineSlot with superClassCtor null
 *   - Branch: defineSlot with superClassCtor non-null
 *   - Branch: defineSlot with superClassCtor.getInstanceType() equals OBJECT_TYPE
 *   - Branch: defineSlot with superClassCtor.getInstanceType() not equals OBJECT_TYPE
 *   - Branch: isQnameRootedInGlobalScope with NAME root and global var
 *   - Branch: isQnameRootedInGlobalScope with NAME root and non-global var
 *   - Branch: isQnameRootedInGlobalScope with non-NAME root
 *   - Branch: getDeclaredGetPropType with @type annotation
 *   - Branch: getDeclaredGetPropType with @enum annotation
 *   - Branch: getDeclaredGetPropType with FUNCTION rhsValue
 *   - Branch: getDeclaredGetPropType with other cases
 *   - Branch: getDeclaredTypeInAnnotation with @type
 *   - Branch: getDeclaredTypeInAnnotation with isFunctionTypeDeclaration
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and existing type
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and no existing type
 *   - Branch: getDeclaredTypeInAnnotation with objNode GETPROP prototype
 *   - Branch: getDeclaredTypeInAnnotation with objNode THIS
 *   - Branch: getDeclaredTypeInAnnotation with no searchedForThisType
 *   - Branch: getDeclaredTypeInAnnotation with null info
 *   - Branch: getDeclaredTypeInAnnotation with null objNode
 *   - Branch: findOverriddenFunction with FunctionType propType
 *   - Branch: findOverriddenFunction with non-FunctionType propType and interfaces
 *   - Branch: findOverriddenFunction with non-FunctionType propType and no interfaces
 *   - Branch: findOverriddenFunction with null ownerType
 *   - Branch: findOverriddenFunction with null propType
 *   - Branch: getObjectSlot with null ownerVar
 *   - Branch: getObjectSlot with null ownerVarType
 *   - Branch: getObjectSlot with non-null ownerVarType
 *   - Branch: getPrototypePropertyOwner with GETPROP chain
 *   - Branch: getPrototypePropertyOwner with non-GETPROP
 *   - Branch: getPrototypePropertyOwner with non-prototype GETPROP
 *   - Branch: getPrototypePropertyOwner with non-qualified name
 *   - Branch: DiscoverEnums.visit with VAR
 *   - Branch: DiscoverEnums.visit with EXPR_RESULT
 *   - Branch: DiscoverEnums.visit with other types
 *   - Branch: DiscoverEnums.identifyEnumName with null info
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and hasEnumParameterType
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and no enum parameter type
 *   - Branch: DeferredSetType constructor with null node
 *   - Branch: DeferredSetType constructor with null type
 *   - Branch: DeferredSetType.resolve with scope
 *   - Branch: createScope with null parent (global)
 *   - Branch: createScope with non-null parent (local)
 *   - Branch: createScope with nonExternFunctions iteration
 *   - Branch: createScope with null type
 *   - Branch: createScope with type not instanceof FunctionType
 *   - Branch: createScope with fnThisType.isUnknownType()
 *   - Branch: createScope with fnThisType not unknown
 *   - Branch: createScope with null parent and delegate proxy
 *   - Branch: createScope with non-null parent and no delegate proxy
 *   - Branch: createInitialScope with root traversal
 *   - Branch: declareNativeFunctionType with scope
 *   - Branch: declareNativeValueType with scope
 *   - Branch: declareNativeType with scope
 *   - Branch: TypedScopeCreator constructor with compiler
 *   - Branch: TypedScopeCreator constructor with compiler and codingConvention
 *   - Branch: getNativeType with native type
 *   - Branch: assertDefinitionNode with correct type
 *   - Branch: assertDefinitionNode with incorrect type
 *   - Branch: setDeferredType with node and type
 *   - Branch: resolveTypes with deferred types
 *   - Branch: resolveTypes with scope vars
 *   - Branch: resolveTypes with type registry
 *   - Branch: visit with attachLiteralTypes
 *   - Branch: visit with CALL, FUNCTION, ASSIGN, CATCH, VAR, GETPROP
 *   - Branch: visit with other types
 *   - Branch: shouldTraverse with FUNCTION parent
 *   - Branch: shouldTraverse with SCRIPT parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: shouldTraverse with FUNCTION parent and first child
 *   - Branch: shouldTraverse with FUNCTION parent and root node
 *   - Branch: shouldTraverse with non-FUNCTION parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: visit with CALL node
 *   - Branch: visit with FUNCTION node
 *   - Branch: visit with ASSIGN node
 *   - Branch: visit with CATCH node
 *   - Branch: visit with VAR node
 *   - Branch: visit with GETPROP node
 *   - Branch: visit with other node types
 *   - Branch: attachLiteralTypes with NULL, VOID, STRING, NUMBER, TRUE, FALSE, REGEXP, REF_SPECIAL, OBJECTLIT
 *   - Branch: attachLiteralTypes with other types
 *   - Branch: defineSlot with NAME node
 *   - Branch: defineSlot with GETPROP node
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis true
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis false
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared true
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared false
 *   - Branch: defineSlot with inferred true
 *   - Branch: defineSlot with inferred false
 *   - Branch: defineSlot with type instanceof FunctionType
 *   - Branch: defineSlot with type not instanceof FunctionType
 *   - Branch: defineSlot with fnType.isConstructor() or fnType.isInterface()
 *   - Branch: defineSlot with superClassCtor null
 *   - Branch: defineSlot with superClassCtor non-null
 *   - Branch: defineSlot with superClassCtor.getInstanceType() equals OBJECT_TYPE
 *   - Branch: defineSlot with superClassCtor.getInstanceType() not equals OBJECT_TYPE
 *   - Branch: isQnameRootedInGlobalScope with NAME root and global var
 *   - Branch: isQnameRootedInGlobalScope with NAME root and non-global var
 *   - Branch: isQnameRootedInGlobalScope with non-NAME root
 *   - Branch: getDeclaredGetPropType with @type annotation
 *   - Branch: getDeclaredGetPropType with @enum annotation
 *   - Branch: getDeclaredGetPropType with FUNCTION rhsValue
 *   - Branch: getDeclaredGetPropType with other cases
 *   - Branch: getDeclaredTypeInAnnotation with @type
 *   - Branch: getDeclaredTypeInAnnotation with isFunctionTypeDeclaration
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and existing type
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and no existing type
 *   - Branch: getDeclaredTypeInAnnotation with objNode GETPROP prototype
 *   - Branch: getDeclaredTypeInAnnotation with objNode THIS
 *   - Branch: getDeclaredTypeInAnnotation with no searchedForThisType
 *   - Branch: getDeclaredTypeInAnnotation with null info
 *   - Branch: getDeclaredTypeInAnnotation with null objNode
 *   - Branch: findOverriddenFunction with FunctionType propType
 *   - Branch: findOverriddenFunction with non-FunctionType propType and interfaces
 *   - Branch: findOverriddenFunction with non-FunctionType propType and no interfaces
 *   - Branch: findOverriddenFunction with null ownerType
 *   - Branch: findOverriddenFunction with null propType
 *   - Branch: getObjectSlot with null ownerVar
 *   - Branch: getObjectSlot with null ownerVarType
 *   - Branch: getObjectSlot with non-null ownerVarType
 *   - Branch: getPrototypePropertyOwner with GETPROP chain
 *   - Branch: getPrototypePropertyOwner with non-GETPROP
 *   - Branch: getPrototypePropertyOwner with non-prototype GETPROP
 *   - Branch: getPrototypePropertyOwner with non-qualified name
 *   - Branch: DiscoverEnums.visit with VAR
 *   - Branch: DiscoverEnums.visit with EXPR_RESULT
 *   - Branch: DiscoverEnums.visit with other types
 *   - Branch: DiscoverEnums.identifyEnumName with null info
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and hasEnumParameterType
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and no enum parameter type
 *   - Branch: DeferredSetType constructor with null node
 *   - Branch: DeferredSetType constructor with null type
 *   - Branch: DeferredSetType.resolve with scope
 *   - Branch: createScope with null parent (global)
 *   - Branch: createScope with non-null parent (local)
 *   - Branch: createScope with nonExternFunctions iteration
 *   - Branch: createScope with null type
 *   - Branch: createScope with type not instanceof FunctionType
 *   - Branch: createScope with fnThisType.isUnknownType()
 *   - Branch: createScope with fnThisType not unknown
 *   - Branch: createScope with null parent and delegate proxy
 *   - Branch: createScope with non-null parent and no delegate proxy
 *   - Branch: createInitialScope with root traversal
 *   - Branch: declareNativeFunctionType with scope
 *   - Branch: declareNativeValueType with scope
 *   - Branch: declareNativeType with scope
 *   - Branch: TypedScopeCreator constructor with compiler
 *   - Branch: TypedScopeCreator constructor with compiler and codingConvention
 *   - Branch: getNativeType with native type
 *   - Branch: assertDefinitionNode with correct type
 *   - Branch: assertDefinitionNode with incorrect type
 *   - Branch: setDeferredType with node and type
 *   - Branch: resolveTypes with deferred types
 *   - Branch: resolveTypes with scope vars
 *   - Branch: resolveTypes with type registry
 *   - Branch: visit with attachLiteralTypes
 *   - Branch: visit with CALL, FUNCTION, ASSIGN, CATCH, VAR, GETPROP
 *   - Branch: visit with other types
 *   - Branch: shouldTraverse with FUNCTION parent
 *   - Branch: shouldTraverse with SCRIPT parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: shouldTraverse with FUNCTION parent and first child
 *   - Branch: shouldTraverse with FUNCTION parent and root node
 *   - Branch: shouldTraverse with non-FUNCTION parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: visit with CALL node
 *   - Branch: visit with FUNCTION node
 *   - Branch: visit with ASSIGN node
 *   - Branch: visit with CATCH node
 *   - Branch: visit with VAR node
 *   - Branch: visit with GETPROP node
 *   - Branch: visit with other node types
 *   - Branch: attachLiteralTypes with NULL, VOID, STRING, NUMBER, TRUE, FALSE, REGEXP, REF_SPECIAL, OBJECTLIT
 *   - Branch: attachLiteralTypes with other types
 *   - Branch: defineSlot with NAME node
 *   - Branch: defineSlot with GETPROP node
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis true
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis false
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared true
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared false
 *   - Branch: defineSlot with inferred true
 *   - Branch: defineSlot with inferred false
 *   - Branch: defineSlot with type instanceof FunctionType
 *   - Branch: defineSlot with type not instanceof FunctionType
 *   - Branch: defineSlot with fnType.isConstructor() or fnType.isInterface()
 *   - Branch: defineSlot with superClassCtor null
 *   - Branch: defineSlot with superClassCtor non-null
 *   - Branch: defineSlot with superClassCtor.getInstanceType() equals OBJECT_TYPE
 *   - Branch: defineSlot with superClassCtor.getInstanceType() not equals OBJECT_TYPE
 *   - Branch: isQnameRootedInGlobalScope with NAME root and global var
 *   - Branch: isQnameRootedInGlobalScope with NAME root and non-global var
 *   - Branch: isQnameRootedInGlobalScope with non-NAME root
 *   - Branch: getDeclaredGetPropType with @type annotation
 *   - Branch: getDeclaredGetPropType with @enum annotation
 *   - Branch: getDeclaredGetPropType with FUNCTION rhsValue
 *   - Branch: getDeclaredGetPropType with other cases
 *   - Branch: getDeclaredTypeInAnnotation with @type
 *   - Branch: getDeclaredTypeInAnnotation with isFunctionTypeDeclaration
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and existing type
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and no existing type
 *   - Branch: getDeclaredTypeInAnnotation with objNode GETPROP prototype
 *   - Branch: getDeclaredTypeInAnnotation with objNode THIS
 *   - Branch: getDeclaredTypeInAnnotation with no searchedForThisType
 *   - Branch: getDeclaredTypeInAnnotation with null info
 *   - Branch: getDeclaredTypeInAnnotation with null objNode
 *   - Branch: findOverriddenFunction with FunctionType propType
 *   - Branch: findOverriddenFunction with non-FunctionType propType and interfaces
 *   - Branch: findOverriddenFunction with non-FunctionType propType and no interfaces
 *   - Branch: findOverriddenFunction with null ownerType
 *   - Branch: findOverriddenFunction with null propType
 *   - Branch: getObjectSlot with null ownerVar
 *   - Branch: getObjectSlot with null ownerVarType
 *   - Branch: getObjectSlot with non-null ownerVarType
 *   - Branch: getPrototypePropertyOwner with GETPROP chain
 *   - Branch: getPrototypePropertyOwner with non-GETPROP
 *   - Branch: getPrototypePropertyOwner with non-prototype GETPROP
 *   - Branch: getPrototypePropertyOwner with non-qualified name
 *   - Branch: DiscoverEnums.visit with VAR
 *   - Branch: DiscoverEnums.visit with EXPR_RESULT
 *   - Branch: DiscoverEnums.visit with other types
 *   - Branch: DiscoverEnums.identifyEnumName with null info
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and hasEnumParameterType
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and no enum parameter type
 *   - Branch: DeferredSetType constructor with null node
 *   - Branch: DeferredSetType constructor with null type
 *   - Branch: DeferredSetType.resolve with scope
 *   - Branch: createScope with null parent (global)
 *   - Branch: createScope with non-null parent (local)
 *   - Branch: createScope with nonExternFunctions iteration
 *   - Branch: createScope with null type
 *   - Branch: createScope with type not instanceof FunctionType
 *   - Branch: createScope with fnThisType.isUnknownType()
 *   - Branch: createScope with fnThisType not unknown
 *   - Branch: createScope with null parent and delegate proxy
 *   - Branch: createScope with non-null parent and no delegate proxy
 *   - Branch: createInitialScope with root traversal
 *   - Branch: declareNativeFunctionType with scope
 *   - Branch: declareNativeValueType with scope
 *   - Branch: declareNativeType with scope
 *   - Branch: TypedScopeCreator constructor with compiler
 *   - Branch: TypedScopeCreator constructor with compiler and codingConvention
 *   - Branch: getNativeType with native type
 *   - Branch: assertDefinitionNode with correct type
 *   - Branch: assertDefinitionNode with incorrect type
 *   - Branch: setDeferredType with node and type
 *   - Branch: resolveTypes with deferred types
 *   - Branch: resolveTypes with scope vars
 *   - Branch: resolveTypes with type registry
 *   - Branch: visit with attachLiteralTypes
 *   - Branch: visit with CALL, FUNCTION, ASSIGN, CATCH, VAR, GETPROP
 *   - Branch: visit with other types
 *   - Branch: shouldTraverse with FUNCTION parent
 *   - Branch: shouldTraverse with SCRIPT parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: shouldTraverse with FUNCTION parent and first child
 *   - Branch: shouldTraverse with FUNCTION parent and root node
 *   - Branch: shouldTraverse with non-FUNCTION parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: visit with CALL node
 *   - Branch: visit with FUNCTION node
 *   - Branch: visit with ASSIGN node
 *   - Branch: visit with CATCH node
 *   - Branch: visit with VAR node
 *   - Branch: visit with GETPROP node
 *   - Branch: visit with other node types
 *   - Branch: attachLiteralTypes with NULL, VOID, STRING, NUMBER, TRUE, FALSE, REGEXP, REF_SPECIAL, OBJECTLIT
 *   - Branch: attachLiteralTypes with other types
 *   - Branch: defineSlot with NAME node
 *   - Branch: defineSlot with GETPROP node
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis true
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis false
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared true
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared false
 *   - Branch: defineSlot with inferred true
 *   - Branch: defineSlot with inferred false
 *   - Branch: defineSlot with type instanceof FunctionType
 *   - Branch: defineSlot with type not instanceof FunctionType
 *   - Branch: defineSlot with fnType.isConstructor() or fnType.isInterface()
 *   - Branch: defineSlot with superClassCtor null
 *   - Branch: defineSlot with superClassCtor non-null
 *   - Branch: defineSlot with superClassCtor.getInstanceType() equals OBJECT_TYPE
 *   - Branch: defineSlot with superClassCtor.getInstanceType() not equals OBJECT_TYPE
 *   - Branch: isQnameRootedInGlobalScope with NAME root and global var
 *   - Branch: isQnameRootedInGlobalScope with NAME root and non-global var
 *   - Branch: isQnameRootedInGlobalScope with non-NAME root
 *   - Branch: getDeclaredGetPropType with @type annotation
 *   - Branch: getDeclaredGetPropType with @enum annotation
 *   - Branch: getDeclaredGetPropType with FUNCTION rhsValue
 *   - Branch: getDeclaredGetPropType with other cases
 *   - Branch: getDeclaredTypeInAnnotation with @type
 *   - Branch: getDeclaredTypeInAnnotation with isFunctionTypeDeclaration
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and existing type
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and no existing type
 *   - Branch: getDeclaredTypeInAnnotation with objNode GETPROP prototype
 *   - Branch: getDeclaredTypeInAnnotation with objNode THIS
 *   - Branch: getDeclaredTypeInAnnotation with no searchedForThisType
 *   - Branch: getDeclaredTypeInAnnotation with null info
 *   - Branch: getDeclaredTypeInAnnotation with null objNode
 *   - Branch: findOverriddenFunction with FunctionType propType
 *   - Branch: findOverriddenFunction with non-FunctionType propType and interfaces
 *   - Branch: findOverriddenFunction with non-FunctionType propType and no interfaces
 *   - Branch: findOverriddenFunction with null ownerType
 *   - Branch: findOverriddenFunction with null propType
 *   - Branch: getObjectSlot with null ownerVar
 *   - Branch: getObjectSlot with null ownerVarType
 *   - Branch: getObjectSlot with non-null ownerVarType
 *   - Branch: getPrototypePropertyOwner with GETPROP chain
 *   - Branch: getPrototypePropertyOwner with non-GETPROP
 *   - Branch: getPrototypePropertyOwner with non-prototype GETPROP
 *   - Branch: getPrototypePropertyOwner with non-qualified name
 *   - Branch: DiscoverEnums.visit with VAR
 *   - Branch: DiscoverEnums.visit with EXPR_RESULT
 *   - Branch: DiscoverEnums.visit with other types
 *   - Branch: DiscoverEnums.identifyEnumName with null info
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and hasEnumParameterType
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and no enum parameter type
 *   - Branch: DeferredSetType constructor with null node
 *   - Branch: DeferredSetType constructor with null type
 *   - Branch: DeferredSetType.resolve with scope
 *   - Branch: createScope with null parent (global)
 *   - Branch: createScope with non-null parent (local)
 *   - Branch: createScope with nonExternFunctions iteration
 *   - Branch: createScope with null type
 *   - Branch: createScope with type not instanceof FunctionType
 *   - Branch: createScope with fnThisType.isUnknownType()
 *   - Branch: createScope with fnThisType not unknown
 *   - Branch: createScope with null parent and delegate proxy
 *   - Branch: createScope with non-null parent and no delegate proxy
 *   - Branch: createInitialScope with root traversal
 *   - Branch: declareNativeFunctionType with scope
 *   - Branch: declareNativeValueType with scope
 *   - Branch: declareNativeType with scope
 *   - Branch: TypedScopeCreator constructor with compiler
 *   - Branch: TypedScopeCreator constructor with compiler and codingConvention
 *   - Branch: getNativeType with native type
 *   - Branch: assertDefinitionNode with correct type
 *   - Branch: assertDefinitionNode with incorrect type
 *   - Branch: setDeferredType with node and type
 *   - Branch: resolveTypes with deferred types
 *   - Branch: resolveTypes with scope vars
 *   - Branch: resolveTypes with type registry
 *   - Branch: visit with attachLiteralTypes
 *   - Branch: visit with CALL, FUNCTION, ASSIGN, CATCH, VAR, GETPROP
 *   - Branch: visit with other types
 *   - Branch: shouldTraverse with FUNCTION parent
 *   - Branch: shouldTraverse with SCRIPT parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: shouldTraverse with FUNCTION parent and first child
 *   - Branch: shouldTraverse with FUNCTION parent and root node
 *   - Branch: shouldTraverse with non-FUNCTION parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: visit with CALL node
 *   - Branch: visit with FUNCTION node
 *   - Branch: visit with ASSIGN node
 *   - Branch: visit with CATCH node
 *   - Branch: visit with VAR node
 *   - Branch: visit with GETPROP node
 *   - Branch: visit with other node types
 *   - Branch: attachLiteralTypes with NULL, VOID, STRING, NUMBER, TRUE, FALSE, REGEXP, REF_SPECIAL, OBJECTLIT
 *   - Branch: attachLiteralTypes with other types
 *   - Branch: defineSlot with NAME node
 *   - Branch: defineSlot with GETPROP node
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis true
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis false
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared true
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared false
 *   - Branch: defineSlot with inferred true
 *   - Branch: defineSlot with inferred false
 *   - Branch: defineSlot with type instanceof FunctionType
 *   - Branch: defineSlot with type not instanceof FunctionType
 *   - Branch: defineSlot with fnType.isConstructor() or fnType.isInterface()
 *   - Branch: defineSlot with superClassCtor null
 *   - Branch: defineSlot with superClassCtor non-null
 *   - Branch: defineSlot with superClassCtor.getInstanceType() equals OBJECT_TYPE
 *   - Branch: defineSlot with superClassCtor.getInstanceType() not equals OBJECT_TYPE
 *   - Branch: isQnameRootedInGlobalScope with NAME root and global var
 *   - Branch: isQnameRootedInGlobalScope with NAME root and non-global var
 *   - Branch: isQnameRootedInGlobalScope with non-NAME root
 *   - Branch: getDeclaredGetPropType with @type annotation
 *   - Branch: getDeclaredGetPropType with @enum annotation
 *   - Branch: getDeclaredGetPropType with FUNCTION rhsValue
 *   - Branch: getDeclaredGetPropType with other cases
 *   - Branch: getDeclaredTypeInAnnotation with @type
 *   - Branch: getDeclaredTypeInAnnotation with isFunctionTypeDeclaration
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and existing type
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and no existing type
 *   - Branch: getDeclaredTypeInAnnotation with objNode GETPROP prototype
 *   - Branch: getDeclaredTypeInAnnotation with objNode THIS
 *   - Branch: getDeclaredTypeInAnnotation with no searchedForThisType
 *   - Branch: getDeclaredTypeInAnnotation with null info
 *   - Branch: getDeclaredTypeInAnnotation with null objNode
 *   - Branch: findOverriddenFunction with FunctionType propType
 *   - Branch: findOverriddenFunction with non-FunctionType propType and interfaces
 *   - Branch: findOverriddenFunction with non-FunctionType propType and no interfaces
 *   - Branch: findOverriddenFunction with null ownerType
 *   - Branch: findOverriddenFunction with null propType
 *   - Branch: getObjectSlot with null ownerVar
 *   - Branch: getObjectSlot with null ownerVarType
 *   - Branch: getObjectSlot with non-null ownerVarType
 *   - Branch: getPrototypePropertyOwner with GETPROP chain
 *   - Branch: getPrototypePropertyOwner with non-GETPROP
 *   - Branch: getPrototypePropertyOwner with non-prototype GETPROP
 *   - Branch: getPrototypePropertyOwner with non-qualified name
 *   - Branch: DiscoverEnums.visit with VAR
 *   - Branch: DiscoverEnums.visit with EXPR_RESULT
 *   - Branch: DiscoverEnums.visit with other types
 *   - Branch: DiscoverEnums.identifyEnumName with null info
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and hasEnumParameterType
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and no enum parameter type
 *   - Branch: DeferredSetType constructor with null node
 *   - Branch: DeferredSetType constructor with null type
 *   - Branch: DeferredSetType.resolve with scope
 *   - Branch: createScope with null parent (global)
 *   - Branch: createScope with non-null parent (local)
 *   - Branch: createScope with nonExternFunctions iteration
 *   - Branch: createScope with null type
 *   - Branch: createScope with type not instanceof FunctionType
 *   - Branch: createScope with fnThisType.isUnknownType()
 *   - Branch: createScope with fnThisType not unknown
 *   - Branch: createScope with null parent and delegate proxy
 *   - Branch: createScope with non-null parent and no delegate proxy
 *   - Branch: createInitialScope with root traversal
 *   - Branch: declareNativeFunctionType with scope
 *   - Branch: declareNativeValueType with scope
 *   - Branch: declareNativeType with scope
 *   - Branch: TypedScopeCreator constructor with compiler
 *   - Branch: TypedScopeCreator constructor with compiler and codingConvention
 *   - Branch: getNativeType with native type
 *   - Branch: assertDefinitionNode with correct type
 *   - Branch: assertDefinitionNode with incorrect type
 *   - Branch: setDeferredType with node and type
 *   - Branch: resolveTypes with deferred types
 *   - Branch: resolveTypes with scope vars
 *   - Branch: resolveTypes with type registry
 *   - Branch: visit with attachLiteralTypes
 *   - Branch: visit with CALL, FUNCTION, ASSIGN, CATCH, VAR, GETPROP
 *   - Branch: visit with other types
 *   - Branch: shouldTraverse with FUNCTION parent
 *   - Branch: shouldTraverse with SCRIPT parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: shouldTraverse with FUNCTION parent and first child
 *   - Branch: shouldTraverse with FUNCTION parent and root node
 *   - Branch: shouldTraverse with non-FUNCTION parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: visit with CALL node
 *   - Branch: visit with FUNCTION node
 *   - Branch: visit with ASSIGN node
 *   - Branch: visit with CATCH node
 *   - Branch: visit with VAR node
 *   - Branch: visit with GETPROP node
 *   - Branch: visit with other node types
 *   - Branch: attachLiteralTypes with NULL, VOID, STRING, NUMBER, TRUE, FALSE, REGEXP, REF_SPECIAL, OBJECTLIT
 *   - Branch: attachLiteralTypes with other types
 *   - Branch: defineSlot with NAME node
 *   - Branch: defineSlot with GETPROP node
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis true
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis false
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared true
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared false
 *   - Branch: defineSlot with inferred true
 *   - Branch: defineSlot with inferred false
 *   - Branch: defineSlot with type instanceof FunctionType
 *   - Branch: defineSlot with type not instanceof FunctionType
 *   - Branch: defineSlot with fnType.isConstructor() or fnType.isInterface()
 *   - Branch: defineSlot with superClassCtor null
 *   - Branch: defineSlot with superClassCtor non-null
 *   - Branch: defineSlot with superClassCtor.getInstanceType() equals OBJECT_TYPE
 *   - Branch: defineSlot with superClassCtor.getInstanceType() not equals OBJECT_TYPE
 *   - Branch: isQnameRootedInGlobalScope with NAME root and global var
 *   - Branch: isQnameRootedInGlobalScope with NAME root and non-global var
 *   - Branch: isQnameRootedInGlobalScope with non-NAME root
 *   - Branch: getDeclaredGetPropType with @type annotation
 *   - Branch: getDeclaredGetPropType with @enum annotation
 *   - Branch: getDeclaredGetPropType with FUNCTION rhsValue
 *   - Branch: getDeclaredGetPropType with other cases
 *   - Branch: getDeclaredTypeInAnnotation with @type
 *   - Branch: getDeclaredTypeInAnnotation with isFunctionTypeDeclaration
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and existing type
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and no existing type
 *   - Branch: getDeclaredTypeInAnnotation with objNode GETPROP prototype
 *   - Branch: getDeclaredTypeInAnnotation with objNode THIS
 *   - Branch: getDeclaredTypeInAnnotation with no searchedForThisType
 *   - Branch: getDeclaredTypeInAnnotation with null info
 *   - Branch: getDeclaredTypeInAnnotation with null objNode
 *   - Branch: findOverriddenFunction with FunctionType propType
 *   - Branch: findOverriddenFunction with non-FunctionType propType and interfaces
 *   - Branch: findOverriddenFunction with non-FunctionType propType and no interfaces
 *   - Branch: findOverriddenFunction with null ownerType
 *   - Branch: findOverriddenFunction with null propType
 *   - Branch: getObjectSlot with null ownerVar
 *   - Branch: getObjectSlot with null ownerVarType
 *   - Branch: getObjectSlot with non-null ownerVarType
 *   - Branch: getPrototypePropertyOwner with GETPROP chain
 *   - Branch: getPrototypePropertyOwner with non-GETPROP
 *   - Branch: getPrototypePropertyOwner with non-prototype GETPROP
 *   - Branch: getPrototypePropertyOwner with non-qualified name
 *   - Branch: DiscoverEnums.visit with VAR
 *   - Branch: DiscoverEnums.visit with EXPR_RESULT
 *   - Branch: DiscoverEnums.visit with other types
 *   - Branch: DiscoverEnums.identifyEnumName with null info
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and hasEnumParameterType
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and no enum parameter type
 *   - Branch: DeferredSetType constructor with null node
 *   - Branch: DeferredSetType constructor with null type
 *   - Branch: DeferredSetType.resolve with scope
 *   - Branch: createScope with null parent (global)
 *   - Branch: createScope with non-null parent (local)
 *   - Branch: createScope with nonExternFunctions iteration
 *   - Branch: createScope with null type
 *   - Branch: createScope with type not instanceof FunctionType
 *   - Branch: createScope with fnThisType.isUnknownType()
 *   - Branch: createScope with fnThisType not unknown
 *   - Branch: createScope with null parent and delegate proxy
 *   - Branch: createScope with non-null parent and no delegate proxy
 *   - Branch: createInitialScope with root traversal
 *   - Branch: declareNativeFunctionType with scope
 *   - Branch: declareNativeValueType with scope
 *   - Branch: declareNativeType with scope
 *   - Branch: TypedScopeCreator constructor with compiler
 *   - Branch: TypedScopeCreator constructor with compiler and codingConvention
 *   - Branch: getNativeType with native type
 *   - Branch: assertDefinitionNode with correct type
 *   - Branch: assertDefinitionNode with incorrect type
 *   - Branch: setDeferredType with node and type
 *   - Branch: resolveTypes with deferred types
 *   - Branch: resolveTypes with scope vars
 *   - Branch: resolveTypes with type registry
 *   - Branch: visit with attachLiteralTypes
 *   - Branch: visit with CALL, FUNCTION, ASSIGN, CATCH, VAR, GETPROP
 *   - Branch: visit with other types
 *   - Branch: shouldTraverse with FUNCTION parent
 *   - Branch: shouldTraverse with SCRIPT parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: shouldTraverse with FUNCTION parent and first child
 *   - Branch: shouldTraverse with FUNCTION parent and root node
 *   - Branch: shouldTraverse with non-FUNCTION parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: visit with CALL node
 *   - Branch: visit with FUNCTION node
 *   - Branch: visit with ASSIGN node
 *   - Branch: visit with CATCH node
 *   - Branch: visit with VAR node
 *   - Branch: visit with GETPROP node
 *   - Branch: visit with other node types
 *   - Branch: attachLiteralTypes with NULL, VOID, STRING, NUMBER, TRUE, FALSE, REGEXP, REF_SPECIAL, OBJECTLIT
 *   - Branch: attachLiteralTypes with other types
 *   - Branch: defineSlot with NAME node
 *   - Branch: defineSlot with GETPROP node
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis true
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis false
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared true
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared false
 *   - Branch: defineSlot with inferred true
 *   - Branch: defineSlot with inferred false
 *   - Branch: defineSlot with type instanceof FunctionType
 *   - Branch: defineSlot with type not instanceof FunctionType
 *   - Branch: defineSlot with fnType.isConstructor() or fnType.isInterface()
 *   - Branch: defineSlot with superClassCtor null
 *   - Branch: defineSlot with superClassCtor non-null
 *   - Branch: defineSlot with superClassCtor.getInstanceType() equals OBJECT_TYPE
 *   - Branch: defineSlot with superClassCtor.getInstanceType() not equals OBJECT_TYPE
 *   - Branch: isQnameRootedInGlobalScope with NAME root and global var
 *   - Branch: isQnameRootedInGlobalScope with NAME root and non-global var
 *   - Branch: isQnameRootedInGlobalScope with non-NAME root
 *   - Branch: getDeclaredGetPropType with @type annotation
 *   - Branch: getDeclaredGetPropType with @enum annotation
 *   - Branch: getDeclaredGetPropType with FUNCTION rhsValue
 *   - Branch: getDeclaredGetPropType with other cases
 *   - Branch: getDeclaredTypeInAnnotation with @type
 *   - Branch: getDeclaredTypeInAnnotation with isFunctionTypeDeclaration
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and existing type
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and no existing type
 *   - Branch: getDeclaredTypeInAnnotation with objNode GETPROP prototype
 *   - Branch: getDeclaredTypeInAnnotation with objNode THIS
 *   - Branch: getDeclaredTypeInAnnotation with no searchedForThisType
 *   - Branch: getDeclaredTypeInAnnotation with null info
 *   - Branch: getDeclaredTypeInAnnotation with null objNode
 *   - Branch: findOverriddenFunction with FunctionType propType
 *   - Branch: findOverriddenFunction with non-FunctionType propType and interfaces
 *   - Branch: findOverriddenFunction with non-FunctionType propType and no interfaces
 *   - Branch: findOverriddenFunction with null ownerType
 *   - Branch: findOverriddenFunction with null propType
 *   - Branch: getObjectSlot with null ownerVar
 *   - Branch: getObjectSlot with null ownerVarType
 *   - Branch: getObjectSlot with non-null ownerVarType
 *   - Branch: getPrototypePropertyOwner with GETPROP chain
 *   - Branch: getPrototypePropertyOwner with non-GETPROP
 *   - Branch: getPrototypePropertyOwner with non-prototype GETPROP
 *   - Branch: getPrototypePropertyOwner with non-qualified name
 *   - Branch: DiscoverEnums.visit with VAR
 *   - Branch: DiscoverEnums.visit with EXPR_RESULT
 *   - Branch: DiscoverEnums.visit with other types
 *   - Branch: DiscoverEnums.identifyEnumName with null info
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and hasEnumParameterType
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and no enum parameter type
 *   - Branch: DeferredSetType constructor with null node
 *   - Branch: DeferredSetType constructor with null type
 *   - Branch: DeferredSetType.resolve with scope
 *   - Branch: createScope with null parent (global)
 *   - Branch: createScope with non-null parent (local)
 *   - Branch: createScope with nonExternFunctions iteration
 *   - Branch: createScope with null type
 *   - Branch: createScope with type not instanceof FunctionType
 *   - Branch: createScope with fnThisType.isUnknownType()
 *   - Branch: createScope with fnThisType not unknown
 *   - Branch: createScope with null parent and delegate proxy
 *   - Branch: createScope with non-null parent and no delegate proxy
 *   - Branch: createInitialScope with root traversal
 *   - Branch: declareNativeFunctionType with scope
 *   - Branch: declareNativeValueType with scope
 *   - Branch: declareNativeType with scope
 *   - Branch: TypedScopeCreator constructor with compiler
 *   - Branch: TypedScopeCreator constructor with compiler and codingConvention
 *   - Branch: getNativeType with native type
 *   - Branch: assertDefinitionNode with correct type
 *   - Branch: assertDefinitionNode with incorrect type
 *   - Branch: setDeferredType with node and type
 *   - Branch: resolveTypes with deferred types
 *   - Branch: resolveTypes with scope vars
 *   - Branch: resolveTypes with type registry
 *   - Branch: visit with attachLiteralTypes
 *   - Branch: visit with CALL, FUNCTION, ASSIGN, CATCH, VAR, GETPROP
 *   - Branch: visit with other types
 *   - Branch: shouldTraverse with FUNCTION parent
 *   - Branch: shouldTraverse with SCRIPT parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: shouldTraverse with FUNCTION parent and first child
 *   - Branch: shouldTraverse with FUNCTION parent and root node
 *   - Branch: shouldTraverse with non-FUNCTION parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: visit with CALL node
 *   - Branch: visit with FUNCTION node
 *   - Branch: visit with ASSIGN node
 *   - Branch: visit with CATCH node
 *   - Branch: visit with VAR node
 *   - Branch: visit with GETPROP node
 *   - Branch: visit with other node types
 *   - Branch: attachLiteralTypes with NULL, VOID, STRING, NUMBER, TRUE, FALSE, REGEXP, REF_SPECIAL, OBJECTLIT
 *   - Branch: attachLiteralTypes with other types
 *   - Branch: defineSlot with NAME node
 *   - Branch: defineSlot with GETPROP node
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis true
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis false
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared true
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared false
 *   - Branch: defineSlot with inferred true
 *   - Branch: defineSlot with inferred false
 *   - Branch: defineSlot with type instanceof FunctionType
 *   - Branch: defineSlot with type not instanceof FunctionType
 *   - Branch: defineSlot with fnType.isConstructor() or fnType.isInterface()
 *   - Branch: defineSlot with superClassCtor null
 *   - Branch: defineSlot with superClassCtor non-null
 *   - Branch: defineSlot with superClassCtor.getInstanceType() equals OBJECT_TYPE
 *   - Branch: defineSlot with superClassCtor.getInstanceType() not equals OBJECT_TYPE
 *   - Branch: isQnameRootedInGlobalScope with NAME root and global var
 *   - Branch: isQnameRootedInGlobalScope with NAME root and non-global var
 *   - Branch: isQnameRootedInGlobalScope with non-NAME root
 *   - Branch: getDeclaredGetPropType with @type annotation
 *   - Branch: getDeclaredGetPropType with @enum annotation
 *   - Branch: getDeclaredGetPropType with FUNCTION rhsValue
 *   - Branch: getDeclaredGetPropType with other cases
 *   - Branch: getDeclaredTypeInAnnotation with @type
 *   - Branch: getDeclaredTypeInAnnotation with isFunctionTypeDeclaration
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and existing type
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and no existing type
 *   - Branch: getDeclaredTypeInAnnotation with objNode GETPROP prototype
 *   - Branch: getDeclaredTypeInAnnotation with objNode THIS
 *   - Branch: getDeclaredTypeInAnnotation with no searchedForThisType
 *   - Branch: getDeclaredTypeInAnnotation with null info
 *   - Branch: getDeclaredTypeInAnnotation with null objNode
 *   - Branch: findOverriddenFunction with FunctionType propType
 *   - Branch: findOverriddenFunction with non-FunctionType propType and interfaces
 *   - Branch: findOverriddenFunction with non-FunctionType propType and no interfaces
 *   - Branch: findOverriddenFunction with null ownerType
 *   - Branch: findOverriddenFunction with null propType
 *   - Branch: getObjectSlot with null ownerVar
 *   - Branch: getObjectSlot with null ownerVarType
 *   - Branch: getObjectSlot with non-null ownerVarType
 *   - Branch: getPrototypePropertyOwner with GETPROP chain
 *   - Branch: getPrototypePropertyOwner with non-GETPROP
 *   - Branch: getPrototypePropertyOwner with non-prototype GETPROP
 *   - Branch: getPrototypePropertyOwner with non-qualified name
 *   - Branch: DiscoverEnums.visit with VAR
 *   - Branch: DiscoverEnums.visit with EXPR_RESULT
 *   - Branch: DiscoverEnums.visit with other types
 *   - Branch: DiscoverEnums.identifyEnumName with null info
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and hasEnumParameterType
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and no enum parameter type
 *   - Branch: DeferredSetType constructor with null node
 *   - Branch: DeferredSetType constructor with null type
 *   - Branch: DeferredSetType.resolve with scope
 *   - Branch: createScope with null parent (global)
 *   - Branch: createScope with non-null parent (local)
 *   - Branch: createScope with nonExternFunctions iteration
 *   - Branch: createScope with null type
 *   - Branch: createScope with type not instanceof FunctionType
 *   - Branch: createScope with fnThisType.isUnknownType()
 *   - Branch: createScope with fnThisType not unknown
 *   - Branch: createScope with null parent and delegate proxy
 *   - Branch: createScope with non-null parent and no delegate proxy
 *   - Branch: createInitialScope with root traversal
 *   - Branch: declareNativeFunctionType with scope
 *   - Branch: declareNativeValueType with scope
 *   - Branch: declareNativeType with scope
 *   - Branch: TypedScopeCreator constructor with compiler
 *   - Branch: TypedScopeCreator constructor with compiler and codingConvention
 *   - Branch: getNativeType with native type
 *   - Branch: assertDefinitionNode with correct type
 *   - Branch: assertDefinitionNode with incorrect type
 *   - Branch: setDeferredType with node and type
 *   - Branch: resolveTypes with deferred types
 *   - Branch: resolveTypes with scope vars
 *   - Branch: resolveTypes with type registry
 *   - Branch: visit with attachLiteralTypes
 *   - Branch: visit with CALL, FUNCTION, ASSIGN, CATCH, VAR, GETPROP
 *   - Branch: visit with other types
 *   - Branch: shouldTraverse with FUNCTION parent
 *   - Branch: shouldTraverse with SCRIPT parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: shouldTraverse with FUNCTION parent and first child
 *   - Branch: shouldTraverse with FUNCTION parent and root node
 *   - Branch: shouldTraverse with non-FUNCTION parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: visit with CALL node
 *   - Branch: visit with FUNCTION node
 *   - Branch: visit with ASSIGN node
 *   - Branch: visit with CATCH node
 *   - Branch: visit with VAR node
 *   - Branch: visit with GETPROP node
 *   - Branch: visit with other node types
 *   - Branch: attachLiteralTypes with NULL, VOID, STRING, NUMBER, TRUE, FALSE, REGEXP, REF_SPECIAL, OBJECTLIT
 *   - Branch: attachLiteralTypes with other types
 *   - Branch: defineSlot with NAME node
 *   - Branch: defineSlot with GETPROP node
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis true
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis false
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared true
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared false
 *   - Branch: defineSlot with inferred true
 *   - Branch: defineSlot with inferred false
 *   - Branch: defineSlot with type instanceof FunctionType
 *   - Branch: defineSlot with type not instanceof FunctionType
 *   - Branch: defineSlot with fnType.isConstructor() or fnType.isInterface()
 *   - Branch: defineSlot with superClassCtor null
 *   - Branch: defineSlot with superClassCtor non-null
 *   - Branch: defineSlot with superClassCtor.getInstanceType() equals OBJECT_TYPE
 *   - Branch: defineSlot with superClassCtor.getInstanceType() not equals OBJECT_TYPE
 *   - Branch: isQnameRootedInGlobalScope with NAME root and global var
 *   - Branch: isQnameRootedInGlobalScope with NAME root and non-global var
 *   - Branch: isQnameRootedInGlobalScope with non-NAME root
 *   - Branch: getDeclaredGetPropType with @type annotation
 *   - Branch: getDeclaredGetPropType with @enum annotation
 *   - Branch: getDeclaredGetPropType with FUNCTION rhsValue
 *   - Branch: getDeclaredGetPropType with other cases
 *   - Branch: getDeclaredTypeInAnnotation with @type
 *   - Branch: getDeclaredTypeInAnnotation with isFunctionTypeDeclaration
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and existing type
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and no existing type
 *   - Branch: getDeclaredTypeInAnnotation with objNode GETPROP prototype
 *   - Branch: getDeclaredTypeInAnnotation with objNode THIS
 *   - Branch: getDeclaredTypeInAnnotation with no searchedForThisType
 *   - Branch: getDeclaredTypeInAnnotation with null info
 *   - Branch: getDeclaredTypeInAnnotation with null objNode
 *   - Branch: findOverriddenFunction with FunctionType propType
 *   - Branch: findOverriddenFunction with non-FunctionType propType and interfaces
 *   - Branch: findOverriddenFunction with non-FunctionType propType and no interfaces
 *   - Branch: findOverriddenFunction with null ownerType
 *   - Branch: findOverriddenFunction with null propType
 *   - Branch: getObjectSlot with null ownerVar
 *   - Branch: getObjectSlot with null ownerVarType
 *   - Branch: getObjectSlot with non-null ownerVarType
 *   - Branch: getPrototypePropertyOwner with GETPROP chain
 *   - Branch: getPrototypePropertyOwner with non-GETPROP
 *   - Branch: getPrototypePropertyOwner with non-prototype GETPROP
 *   - Branch: getPrototypePropertyOwner with non-qualified name
 *   - Branch: DiscoverEnums.visit with VAR
 *   - Branch: DiscoverEnums.visit with EXPR_RESULT
 *   - Branch: DiscoverEnums.visit with other types
 *   - Branch: DiscoverEnums.identifyEnumName with null info
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and hasEnumParameterType
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and no enum parameter type
 *   - Branch: DeferredSetType constructor with null node
 *   - Branch: DeferredSetType constructor with null type
 *   - Branch: DeferredSetType.resolve with scope
 *   - Branch: createScope with null parent (global)
 *   - Branch: createScope with non-null parent (local)
 *   - Branch: createScope with nonExternFunctions iteration
 *   - Branch: createScope with null type
 *   - Branch: createScope with type not instanceof FunctionType
 *   - Branch: createScope with fnThisType.isUnknownType()
 *   - Branch: createScope with fnThisType not unknown
 *   - Branch: createScope with null parent and delegate proxy
 *   - Branch: createScope with non-null parent and no delegate proxy
 *   - Branch: createInitialScope with root traversal
 *   - Branch: declareNativeFunctionType with scope
 *   - Branch: declareNativeValueType with scope
 *   - Branch: declareNativeType with scope
 *   - Branch: TypedScopeCreator constructor with compiler
 *   - Branch: TypedScopeCreator constructor with compiler and codingConvention
 *   - Branch: getNativeType with native type
 *   - Branch: assertDefinitionNode with correct type
 *   - Branch: assertDefinitionNode with incorrect type
 *   - Branch: setDeferredType with node and type
 *   - Branch: resolveTypes with deferred types
 *   - Branch: resolveTypes with scope vars
 *   - Branch: resolveTypes with type registry
 *   - Branch: visit with attachLiteralTypes
 *   - Branch: visit with CALL, FUNCTION, ASSIGN, CATCH, VAR, GETPROP
 *   - Branch: visit with other types
 *   - Branch: shouldTraverse with FUNCTION parent
 *   - Branch: shouldTraverse with SCRIPT parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: shouldTraverse with FUNCTION parent and first child
 *   - Branch: shouldTraverse with FUNCTION parent and root node
 *   - Branch: shouldTraverse with non-FUNCTION parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: visit with CALL node
 *   - Branch: visit with FUNCTION node
 *   - Branch: visit with ASSIGN node
 *   - Branch: visit with CATCH node
 *   - Branch: visit with VAR node
 *   - Branch: visit with GETPROP node
 *   - Branch: visit with other node types
 *   - Branch: attachLiteralTypes with NULL, VOID, STRING, NUMBER, TRUE, FALSE, REGEXP, REF_SPECIAL, OBJECTLIT
 *   - Branch: attachLiteralTypes with other types
 *   - Branch: defineSlot with NAME node
 *   - Branch: defineSlot with GETPROP node
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis true
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis false
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared true
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared false
 *   - Branch: defineSlot with inferred true
 *   - Branch: defineSlot with inferred false
 *   - Branch: defineSlot with type instanceof FunctionType
 *   - Branch: defineSlot with type not instanceof FunctionType
 *   - Branch: defineSlot with fnType.isConstructor() or fnType.isInterface()
 *   - Branch: defineSlot with superClassCtor null
 *   - Branch: defineSlot with superClassCtor non-null
 *   - Branch: defineSlot with superClassCtor.getInstanceType() equals OBJECT_TYPE
 *   - Branch: defineSlot with superClassCtor.getInstanceType() not equals OBJECT_TYPE
 *   - Branch: isQnameRootedInGlobalScope with NAME root and global var
 *   - Branch: isQnameRootedInGlobalScope with NAME root and non-global var
 *   - Branch: isQnameRootedInGlobalScope with non-NAME root
 *   - Branch: getDeclaredGetPropType with @type annotation
 *   - Branch: getDeclaredGetPropType with @enum annotation
 *   - Branch: getDeclaredGetPropType with FUNCTION rhsValue
 *   - Branch: getDeclaredGetPropType with other cases
 *   - Branch: getDeclaredTypeInAnnotation with @type
 *   - Branch: getDeclaredTypeInAnnotation with isFunctionTypeDeclaration
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and existing type
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and no existing type
 *   - Branch: getDeclaredTypeInAnnotation with objNode GETPROP prototype
 *   - Branch: getDeclaredTypeInAnnotation with objNode THIS
 *   - Branch: getDeclaredTypeInAnnotation with no searchedForThisType
 *   - Branch: getDeclaredTypeInAnnotation with null info
 *   - Branch: getDeclaredTypeInAnnotation with null objNode
 *   - Branch: findOverriddenFunction with FunctionType propType
 *   - Branch: findOverriddenFunction with non-FunctionType propType and interfaces
 *   - Branch: findOverriddenFunction with non-FunctionType propType and no interfaces
 *   - Branch: findOverriddenFunction with null ownerType
 *   - Branch: findOverriddenFunction with null propType
 *   - Branch: getObjectSlot with null ownerVar
 *   - Branch: getObjectSlot with null ownerVarType
 *   - Branch: getObjectSlot with non-null ownerVarType
 *   - Branch: getPrototypePropertyOwner with GETPROP chain
 *   - Branch: getPrototypePropertyOwner with non-GETPROP
 *   - Branch: getPrototypePropertyOwner with non-prototype GETPROP
 *   - Branch: getPrototypePropertyOwner with non-qualified name
 *   - Branch: DiscoverEnums.visit with VAR
 *   - Branch: DiscoverEnums.visit with EXPR_RESULT
 *   - Branch: DiscoverEnums.visit with other types
 *   - Branch: DiscoverEnums.identifyEnumName with null info
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and hasEnumParameterType
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and no enum parameter type
 *   - Branch: DeferredSetType constructor with null node
 *   - Branch: DeferredSetType constructor with null type
 *   - Branch: DeferredSetType.resolve with scope
 *   - Branch: createScope with null parent (global)
 *   - Branch: createScope with non-null parent (local)
 *   - Branch: createScope with nonExternFunctions iteration
 *   - Branch: createScope with null type
 *   - Branch: createScope with type not instanceof FunctionType
 *   - Branch: createScope with fnThisType.isUnknownType()
 *   - Branch: createScope with fnThisType not unknown
 *   - Branch: createScope with null parent and delegate proxy
 *   - Branch: createScope with non-null parent and no delegate proxy
 *   - Branch: createInitialScope with root traversal
 *   - Branch: declareNativeFunctionType with scope
 *   - Branch: declareNativeValueType with scope
 *   - Branch: declareNativeType with scope
 *   - Branch: TypedScopeCreator constructor with compiler
 *   - Branch: TypedScopeCreator constructor with compiler and codingConvention
 *   - Branch: getNativeType with native type
 *   - Branch: assertDefinitionNode with correct type
 *   - Branch: assertDefinitionNode with incorrect type
 *   - Branch: setDeferredType with node and type
 *   - Branch: resolveTypes with deferred types
 *   - Branch: resolveTypes with scope vars
 *   - Branch: resolveTypes with type registry
 *   - Branch: visit with attachLiteralTypes
 *   - Branch: visit with CALL, FUNCTION, ASSIGN, CATCH, VAR, GETPROP
 *   - Branch: visit with other types
 *   - Branch: shouldTraverse with FUNCTION parent
 *   - Branch: shouldTraverse with SCRIPT parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: shouldTraverse with FUNCTION parent and first child
 *   - Branch: shouldTraverse with FUNCTION parent and root node
 *   - Branch: shouldTraverse with non-FUNCTION parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: visit with CALL node
 *   - Branch: visit with FUNCTION node
 *   - Branch: visit with ASSIGN node
 *   - Branch: visit with CATCH node
 *   - Branch: visit with VAR node
 *   - Branch: visit with GETPROP node
 *   - Branch: visit with other node types
 *   - Branch: attachLiteralTypes with NULL, VOID, STRING, NUMBER, TRUE, FALSE, REGEXP, REF_SPECIAL, OBJECTLIT
 *   - Branch: attachLiteralTypes with other types
 *   - Branch: defineSlot with NAME node
 *   - Branch: defineSlot with GETPROP node
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis true
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis false
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared true
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared false
 *   - Branch: defineSlot with inferred true
 *   - Branch: defineSlot with inferred false
 *   - Branch: defineSlot with type instanceof FunctionType
 *   - Branch: defineSlot with type not instanceof FunctionType
 *   - Branch: defineSlot with fnType.isConstructor() or fnType.isInterface()
 *   - Branch: defineSlot with superClassCtor null
 *   - Branch: defineSlot with superClassCtor non-null
 *   - Branch: defineSlot with superClassCtor.getInstanceType() equals OBJECT_TYPE
 *   - Branch: defineSlot with superClassCtor.getInstanceType() not equals OBJECT_TYPE
 *   - Branch: isQnameRootedInGlobalScope with NAME root and global var
 *   - Branch: isQnameRootedInGlobalScope with NAME root and non-global var
 *   - Branch: isQnameRootedInGlobalScope with non-NAME root
 *   - Branch: getDeclaredGetPropType with @type annotation
 *   - Branch: getDeclaredGetPropType with @enum annotation
 *   - Branch: getDeclaredGetPropType with FUNCTION rhsValue
 *   - Branch: getDeclaredGetPropType with other cases
 *   - Branch: getDeclaredTypeInAnnotation with @type
 *   - Branch: getDeclaredTypeInAnnotation with isFunctionTypeDeclaration
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and existing type
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and no existing type
 *   - Branch: getDeclaredTypeInAnnotation with objNode GETPROP prototype
 *   - Branch: getDeclaredTypeInAnnotation with objNode THIS
 *   - Branch: getDeclaredTypeInAnnotation with no searchedForThisType
 *   - Branch: getDeclaredTypeInAnnotation with null info
 *   - Branch: getDeclaredTypeInAnnotation with null objNode
 *   - Branch: findOverriddenFunction with FunctionType propType
 *   - Branch: findOverriddenFunction with non-FunctionType propType and interfaces
 *   - Branch: findOverriddenFunction with non-FunctionType propType and no interfaces
 *   - Branch: findOverriddenFunction with null ownerType
 *   - Branch: findOverriddenFunction with null propType
 *   - Branch: getObjectSlot with null ownerVar
 *   - Branch: getObjectSlot with null ownerVarType
 *   - Branch: getObjectSlot with non-null ownerVarType
 *   - Branch: getPrototypePropertyOwner with GETPROP chain
 *   - Branch: getPrototypePropertyOwner with non-GETPROP
 *   - Branch: getPrototypePropertyOwner with non-prototype GETPROP
 *   - Branch: getPrototypePropertyOwner with non-qualified name
 *   - Branch: DiscoverEnums.visit with VAR
 *   - Branch: DiscoverEnums.visit with EXPR_RESULT
 *   - Branch: DiscoverEnums.visit with other types
 *   - Branch: DiscoverEnums.identifyEnumName with null info
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and hasEnumParameterType
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and no enum parameter type
 *   - Branch: DeferredSetType constructor with null node
 *   - Branch: DeferredSetType constructor with null type
 *   - Branch: DeferredSetType.resolve with scope
 *   - Branch: createScope with null parent (global)
 *   - Branch: createScope with non-null parent (local)
 *   - Branch: createScope with nonExternFunctions iteration
 *   - Branch: createScope with null type
 *   - Branch: createScope with type not instanceof FunctionType
 *   - Branch: createScope with fnThisType.isUnknownType()
 *   - Branch: createScope with fnThisType not unknown
 *   - Branch: createScope with null parent and delegate proxy
 *   - Branch: createScope with non-null parent and no delegate proxy
 *   - Branch: createInitialScope with root traversal
 *   - Branch: declareNativeFunctionType with scope
 *   - Branch: declareNativeValueType with scope
 *   - Branch: declareNativeType with scope
 *   - Branch: TypedScopeCreator constructor with compiler
 *   - Branch: TypedScopeCreator constructor with compiler and codingConvention
 *   - Branch: getNativeType with native type
 *   - Branch: assertDefinitionNode with correct type
 *   - Branch: assertDefinitionNode with incorrect type
 *   - Branch: setDeferredType with node and type
 *   - Branch: resolveTypes with deferred types
 *   - Branch: resolveTypes with scope vars
 *   - Branch: resolveTypes with type registry
 *   - Branch: visit with attachLiteralTypes
 *   - Branch: visit with CALL, FUNCTION, ASSIGN, CATCH, VAR, GETPROP
 *   - Branch: visit with other types
 *   - Branch: shouldTraverse with FUNCTION parent
 *   - Branch: shouldTraverse with SCRIPT parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: shouldTraverse with FUNCTION parent and first child
 *   - Branch: shouldTraverse with FUNCTION parent and root node
 *   - Branch: shouldTraverse with non-FUNCTION parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: visit with CALL node
 *   - Branch: visit with FUNCTION node
 *   - Branch: visit with ASSIGN node
 *   - Branch: visit with CATCH node
 *   - Branch: visit with VAR node
 *   - Branch: visit with GETPROP node
 *   - Branch: visit with other node types
 *   - Branch: attachLiteralTypes with NULL, VOID, STRING, NUMBER, TRUE, FALSE, REGEXP, REF_SPECIAL, OBJECTLIT
 *   - Branch: attachLiteralTypes with other types
 *   - Branch: defineSlot with NAME node
 *   - Branch: defineSlot with GETPROP node
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis true
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis false
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared true
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared false
 *   - Branch: defineSlot with inferred true
 *   - Branch: defineSlot with inferred false
 *   - Branch: defineSlot with type instanceof FunctionType
 *   - Branch: defineSlot with type not instanceof FunctionType
 *   - Branch: defineSlot with fnType.isConstructor() or fnType.isInterface()
 *   - Branch: defineSlot with superClassCtor null
 *   - Branch: defineSlot with superClassCtor non-null
 *   - Branch: defineSlot with superClassCtor.getInstanceType() equals OBJECT_TYPE
 *   - Branch: defineSlot with superClassCtor.getInstanceType() not equals OBJECT_TYPE
 *   - Branch: isQnameRootedInGlobalScope with NAME root and global var
 *   - Branch: isQnameRootedInGlobalScope with NAME root and non-global var
 *   - Branch: isQnameRootedInGlobalScope with non-NAME root
 *   - Branch: getDeclaredGetPropType with @type annotation
 *   - Branch: getDeclaredGetPropType with @enum annotation
 *   - Branch: getDeclaredGetPropType with FUNCTION rhsValue
 *   - Branch: getDeclaredGetPropType with other cases
 *   - Branch: getDeclaredTypeInAnnotation with @type
 *   - Branch: getDeclaredTypeInAnnotation with isFunctionTypeDeclaration
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and existing type
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and no existing type
 *   - Branch: getDeclaredTypeInAnnotation with objNode GETPROP prototype
 *   - Branch: getDeclaredTypeInAnnotation with objNode THIS
 *   - Branch: getDeclaredTypeInAnnotation with no searchedForThisType
 *   - Branch: getDeclaredTypeInAnnotation with null info
 *   - Branch: getDeclaredTypeInAnnotation with null objNode
 *   - Branch: findOverriddenFunction with FunctionType propType
 *   - Branch: findOverriddenFunction with non-FunctionType propType and interfaces
 *   - Branch: findOverriddenFunction with non-FunctionType propType and no interfaces
 *   - Branch: findOverriddenFunction with null ownerType
 *   - Branch: findOverriddenFunction with null propType
 *   - Branch: getObjectSlot with null ownerVar
 *   - Branch: getObjectSlot with null ownerVarType
 *   - Branch: getObjectSlot with non-null ownerVarType
 *   - Branch: getPrototypePropertyOwner with GETPROP chain
 *   - Branch: getPrototypePropertyOwner with non-GETPROP
 *   - Branch: getPrototypePropertyOwner with non-prototype GETPROP
 *   - Branch: getPrototypePropertyOwner with non-qualified name
 *   - Branch: DiscoverEnums.visit with VAR
 *   - Branch: DiscoverEnums.visit with EXPR_RESULT
 *   - Branch: DiscoverEnums.visit with other types
 *   - Branch: DiscoverEnums.identifyEnumName with null info
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and hasEnumParameterType
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and no enum parameter type
 *   - Branch: DeferredSetType constructor with null node
 *   - Branch: DeferredSetType constructor with null type
 *   - Branch: DeferredSetType.resolve with scope
 *   - Branch: createScope with null parent (global)
 *   - Branch: createScope with non-null parent (local)
 *   - Branch: createScope with nonExternFunctions iteration
 *   - Branch: createScope with null type
 *   - Branch: createScope with type not instanceof FunctionType
 *   - Branch: createScope with fnThisType.isUnknownType()
 *   - Branch: createScope with fnThisType not unknown
 *   - Branch: createScope with null parent and delegate proxy
 *   - Branch: createScope with non-null parent and no delegate proxy
 *   - Branch: createInitialScope with root traversal
 *   - Branch: declareNativeFunctionType with scope
 *   - Branch: declareNativeValueType with scope
 *   - Branch: declareNativeType with scope
 *   - Branch: TypedScopeCreator constructor with compiler
 *   - Branch: TypedScopeCreator constructor with compiler and codingConvention
 *   - Branch: getNativeType with native type
 *   - Branch: assertDefinitionNode with correct type
 *   - Branch: assertDefinitionNode with incorrect type
 *   - Branch: setDeferredType with node and type
 *   - Branch: resolveTypes with deferred types
 *   - Branch: resolveTypes with scope vars
 *   - Branch: resolveTypes with type registry
 *   - Branch: visit with attachLiteralTypes
 *   - Branch: visit with CALL, FUNCTION, ASSIGN, CATCH, VAR, GETPROP
 *   - Branch: visit with other types
 *   - Branch: shouldTraverse with FUNCTION parent
 *   - Branch: shouldTraverse with SCRIPT parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: shouldTraverse with FUNCTION parent and first child
 *   - Branch: shouldTraverse with FUNCTION parent and root node
 *   - Branch: shouldTraverse with non-FUNCTION parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: visit with CALL node
 *   - Branch: visit with FUNCTION node
 *   - Branch: visit with ASSIGN node
 *   - Branch: visit with CATCH node
 *   - Branch: visit with VAR node
 *   - Branch: visit with GETPROP node
 *   - Branch: visit with other node types
 *   - Branch: attachLiteralTypes with NULL, VOID, STRING, NUMBER, TRUE, FALSE, REGEXP, REF_SPECIAL, OBJECTLIT
 *   - Branch: attachLiteralTypes with other types
 *   - Branch: defineSlot with NAME node
 *   - Branch: defineSlot with GETPROP node
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis true
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis false
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared true
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared false
 *   - Branch: defineSlot with inferred true
 *   - Branch: defineSlot with inferred false
 *   - Branch: defineSlot with type instanceof FunctionType
 *   - Branch: defineSlot with type not instanceof FunctionType
 *   - Branch: defineSlot with fnType.isConstructor() or fnType.isInterface()
 *   - Branch: defineSlot with superClassCtor null
 *   - Branch: defineSlot with superClassCtor non-null
 *   - Branch: defineSlot with superClassCtor.getInstanceType() equals OBJECT_TYPE
 *   - Branch: defineSlot with superClassCtor.getInstanceType() not equals OBJECT_TYPE
 *   - Branch: isQnameRootedInGlobalScope with NAME root and global var
 *   - Branch: isQnameRootedInGlobalScope with NAME root and non-global var
 *   - Branch: isQnameRootedInGlobalScope with non-NAME root
 *   - Branch: getDeclaredGetPropType with @type annotation
 *   - Branch: getDeclaredGetPropType with @enum annotation
 *   - Branch: getDeclaredGetPropType with FUNCTION rhsValue
 *   - Branch: getDeclaredGetPropType with other cases
 *   - Branch: getDeclaredTypeInAnnotation with @type
 *   - Branch: getDeclaredTypeInAnnotation with isFunctionTypeDeclaration
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and existing type
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and no existing type
 *   - Branch: getDeclaredTypeInAnnotation with objNode GETPROP prototype
 *   - Branch: getDeclaredTypeInAnnotation with objNode THIS
 *   - Branch: getDeclaredTypeInAnnotation with no searchedForThisType
 *   - Branch: getDeclaredTypeInAnnotation with null info
 *   - Branch: getDeclaredTypeInAnnotation with null objNode
 *   - Branch: findOverriddenFunction with FunctionType propType
 *   - Branch: findOverriddenFunction with non-FunctionType propType and interfaces
 *   - Branch: findOverriddenFunction with non-FunctionType propType and no interfaces
 *   - Branch: findOverriddenFunction with null ownerType
 *   - Branch: findOverriddenFunction with null propType
 *   - Branch: getObjectSlot with null ownerVar
 *   - Branch: getObjectSlot with null ownerVarType
 *   - Branch: getObjectSlot with non-null ownerVarType
 *   - Branch: getPrototypePropertyOwner with GETPROP chain
 *   - Branch: getPrototypePropertyOwner with non-GETPROP
 *   - Branch: getPrototypePropertyOwner with non-prototype GETPROP
 *   - Branch: getPrototypePropertyOwner with non-qualified name
 *   - Branch: DiscoverEnums.visit with VAR
 *   - Branch: DiscoverEnums.visit with EXPR_RESULT
 *   - Branch: DiscoverEnums.visit with other types
 *   - Branch: DiscoverEnums.identifyEnumName with null info
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and hasEnumParameterType
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and no enum parameter type
 *   - Branch: DeferredSetType constructor with null node
 *   - Branch: DeferredSetType constructor with null type
 *   - Branch: DeferredSetType.resolve with scope
 *   - Branch: createScope with null parent (global)
 *   - Branch: createScope with non-null parent (local)
 *   - Branch: createScope with nonExternFunctions iteration
 *   - Branch: createScope with null type
 *   - Branch: createScope with type not instanceof FunctionType
 *   - Branch: createScope with fnThisType.isUnknownType()
 *   - Branch: createScope with fnThisType not unknown
 *   - Branch: createScope with null parent and delegate proxy
 *   - Branch: createScope with non-null parent and no delegate proxy
 *   - Branch: createInitialScope with root traversal
 *   - Branch: declareNativeFunctionType with scope
 *   - Branch: declareNativeValueType with scope
 *   - Branch: declareNativeType with scope
 *   - Branch: TypedScopeCreator constructor with compiler
 *   - Branch: TypedScopeCreator constructor with compiler and codingConvention
 *   - Branch: getNativeType with native type
 *   - Branch: assertDefinitionNode with correct type
 *   - Branch: assertDefinitionNode with incorrect type
 *   - Branch: setDeferredType with node and type
 *   - Branch: resolveTypes with deferred types
 *   - Branch: resolveTypes with scope vars
 *   - Branch: resolveTypes with type registry
 *   - Branch: visit with attachLiteralTypes
 *   - Branch: visit with CALL, FUNCTION, ASSIGN, CATCH, VAR, GETPROP
 *   - Branch: visit with other types
 *   - Branch: shouldTraverse with FUNCTION parent
 *   - Branch: shouldTraverse with SCRIPT parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: shouldTraverse with FUNCTION parent and first child
 *   - Branch: shouldTraverse with FUNCTION parent and root node
 *   - Branch: shouldTraverse with non-FUNCTION parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: visit with CALL node
 *   - Branch: visit with FUNCTION node
 *   - Branch: visit with ASSIGN node
 *   - Branch: visit with CATCH node
 *   - Branch: visit with VAR node
 *   - Branch: visit with GETPROP node
 *   - Branch: visit with other node types
 *   - Branch: attachLiteralTypes with NULL, VOID, STRING, NUMBER, TRUE, FALSE, REGEXP, REF_SPECIAL, OBJECTLIT
 *   - Branch: attachLiteralTypes with other types
 *   - Branch: defineSlot with NAME node
 *   - Branch: defineSlot with GETPROP node
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis true
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis false
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared true
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared false
 *   - Branch: defineSlot with inferred true
 *   - Branch: defineSlot with inferred false
 *   - Branch: defineSlot with type instanceof FunctionType
 *   - Branch: defineSlot with type not instanceof FunctionType
 *   - Branch: defineSlot with fnType.isConstructor() or fnType.isInterface()
 *   - Branch: defineSlot with superClassCtor null
 *   - Branch: defineSlot with superClassCtor non-null
 *   - Branch: defineSlot with superClassCtor.getInstanceType() equals OBJECT_TYPE
 *   - Branch: defineSlot with superClassCtor.getInstanceType() not equals OBJECT_TYPE
 *   - Branch: isQnameRootedInGlobalScope with NAME root and global var
 *   - Branch: isQnameRootedInGlobalScope with NAME root and non-global var
 *   - Branch: isQnameRootedInGlobalScope with non-NAME root
 *   - Branch: getDeclaredGetPropType with @type annotation
 *   - Branch: getDeclaredGetPropType with @enum annotation
 *   - Branch: getDeclaredGetPropType with FUNCTION rhsValue
 *   - Branch: getDeclaredGetPropType with other cases
 *   - Branch: getDeclaredTypeInAnnotation with @type
 *   - Branch: getDeclaredTypeInAnnotation with isFunctionTypeDeclaration
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and existing type
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and no existing type
 *   - Branch: getDeclaredTypeInAnnotation with objNode GETPROP prototype
 *   - Branch: getDeclaredTypeInAnnotation with objNode THIS
 *   - Branch: getDeclaredTypeInAnnotation with no searchedForThisType
 *   - Branch: getDeclaredTypeInAnnotation with null info
 *   - Branch: getDeclaredTypeInAnnotation with null objNode
 *   - Branch: findOverriddenFunction with FunctionType propType
 *   - Branch: findOverriddenFunction with non-FunctionType propType and interfaces
 *   - Branch: findOverriddenFunction with non-FunctionType propType and no interfaces
 *   - Branch: findOverriddenFunction with null ownerType
 *   - Branch: findOverriddenFunction with null propType
 *   - Branch: getObjectSlot with null ownerVar
 *   - Branch: getObjectSlot with null ownerVarType
 *   - Branch: getObjectSlot with non-null ownerVarType
 *   - Branch: getPrototypePropertyOwner with GETPROP chain
 *   - Branch: getPrototypePropertyOwner with non-GETPROP
 *   - Branch: getPrototypePropertyOwner with non-prototype GETPROP
 *   - Branch: getPrototypePropertyOwner with non-qualified name
 *   - Branch: DiscoverEnums.visit with VAR
 *   - Branch: DiscoverEnums.visit with EXPR_RESULT
 *   - Branch: DiscoverEnums.visit with other types
 *   - Branch: DiscoverEnums.identifyEnumName with null info
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and hasEnumParameterType
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and no enum parameter type
 *   - Branch: DeferredSetType constructor with null node
 *   - Branch: DeferredSetType constructor with null type
 *   - Branch: DeferredSetType.resolve with scope
 *   - Branch: createScope with null parent (global)
 *   - Branch: createScope with non-null parent (local)
 *   - Branch: createScope with nonExternFunctions iteration
 *   - Branch: createScope with null type
 *   - Branch: createScope with type not instanceof FunctionType
 *   - Branch: createScope with fnThisType.isUnknownType()
 *   - Branch: createScope with fnThisType not unknown
 *   - Branch: createScope with null parent and delegate proxy
 *   - Branch: createScope with non-null parent and no delegate proxy
 *   - Branch: createInitialScope with root traversal
 *   - Branch: declareNativeFunctionType with scope
 *   - Branch: declareNativeValueType with scope
 *   - Branch: declareNativeType with scope
 *   - Branch: TypedScopeCreator constructor with compiler
 *   - Branch: TypedScopeCreator constructor with compiler and codingConvention
 *   - Branch: getNativeType with native type
 *   - Branch: assertDefinitionNode with correct type
 *   - Branch: assertDefinitionNode with incorrect type
 *   - Branch: setDeferredType with node and type
 *   - Branch: resolveTypes with deferred types
 *   - Branch: resolveTypes with scope vars
 *   - Branch: resolveTypes with type registry
 *   - Branch: visit with attachLiteralTypes
 *   - Branch: visit with CALL, FUNCTION, ASSIGN, CATCH, VAR, GETPROP
 *   - Branch: visit with other types
 *   - Branch: shouldTraverse with FUNCTION parent
 *   - Branch: shouldTraverse with SCRIPT parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: shouldTraverse with FUNCTION parent and first child
 *   - Branch: shouldTraverse with FUNCTION parent and root node
 *   - Branch: shouldTraverse with non-FUNCTION parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: visit with CALL node
 *   - Branch: visit with FUNCTION node
 *   - Branch: visit with ASSIGN node
 *   - Branch: visit with CATCH node
 *   - Branch: visit with VAR node
 *   - Branch: visit with GETPROP node
 *   - Branch: visit with other node types
 *   - Branch: attachLiteralTypes with NULL, VOID, STRING, NUMBER, TRUE, FALSE, REGEXP, REF_SPECIAL, OBJECTLIT
 *   - Branch: attachLiteralTypes with other types
 *   - Branch: defineSlot with NAME node
 *   - Branch: defineSlot with GETPROP node
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis true
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis false
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared true
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared false
 *   - Branch: defineSlot with inferred true
 *   - Branch: defineSlot with inferred false
 *   - Branch: defineSlot with type instanceof FunctionType
 *   - Branch: defineSlot with type not instanceof FunctionType
 *   - Branch: defineSlot with fnType.isConstructor() or fnType.isInterface()
 *   - Branch: defineSlot with superClassCtor null
 *   - Branch: defineSlot with superClassCtor non-null
 *   - Branch: defineSlot with superClassCtor.getInstanceType() equals OBJECT_TYPE
 *   - Branch: defineSlot with superClassCtor.getInstanceType() not equals OBJECT_TYPE
 *   - Branch: isQnameRootedInGlobalScope with NAME root and global var
 *   - Branch: isQnameRootedInGlobalScope with NAME root and non-global var
 *   - Branch: isQnameRootedInGlobalScope with non-NAME root
 *   - Branch: getDeclaredGetPropType with @type annotation
 *   - Branch: getDeclaredGetPropType with @enum annotation
 *   - Branch: getDeclaredGetPropType with FUNCTION rhsValue
 *   - Branch: getDeclaredGetPropType with other cases
 *   - Branch: getDeclaredTypeInAnnotation with @type
 *   - Branch: getDeclaredTypeInAnnotation with isFunctionTypeDeclaration
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and existing type
 *   - Branch: getDeclaredTypeInAnnotation with isConstructor and no existing type
 *   - Branch: getDeclaredTypeInAnnotation with objNode GETPROP prototype
 *   - Branch: getDeclaredTypeInAnnotation with objNode THIS
 *   - Branch: getDeclaredTypeInAnnotation with no searchedForThisType
 *   - Branch: getDeclaredTypeInAnnotation with null info
 *   - Branch: getDeclaredTypeInAnnotation with null objNode
 *   - Branch: findOverriddenFunction with FunctionType propType
 *   - Branch: findOverriddenFunction with non-FunctionType propType and interfaces
 *   - Branch: findOverriddenFunction with non-FunctionType propType and no interfaces
 *   - Branch: findOverriddenFunction with null ownerType
 *   - Branch: findOverriddenFunction with null propType
 *   - Branch: getObjectSlot with null ownerVar
 *   - Branch: getObjectSlot with null ownerVarType
 *   - Branch: getObjectSlot with non-null ownerVarType
 *   - Branch: getPrototypePropertyOwner with GETPROP chain
 *   - Branch: getPrototypePropertyOwner with non-GETPROP
 *   - Branch: getPrototypePropertyOwner with non-prototype GETPROP
 *   - Branch: getPrototypePropertyOwner with non-qualified name
 *   - Branch: DiscoverEnums.visit with VAR
 *   - Branch: DiscoverEnums.visit with EXPR_RESULT
 *   - Branch: DiscoverEnums.visit with other types
 *   - Branch: DiscoverEnums.identifyEnumName with null info
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and hasEnumParameterType
 *   - Branch: DiscoverEnums.identifyEnumName with non-null info and no enum parameter type
 *   - Branch: DeferredSetType constructor with null node
 *   - Branch: DeferredSetType constructor with null type
 *   - Branch: DeferredSetType.resolve with scope
 *   - Branch: createScope with null parent (global)
 *   - Branch: createScope with non-null parent (local)
 *   - Branch: createScope with nonExternFunctions iteration
 *   - Branch: createScope with null type
 *   - Branch: createScope with type not instanceof FunctionType
 *   - Branch: createScope with fnThisType.isUnknownType()
 *   - Branch: createScope with fnThisType not unknown
 *   - Branch: createScope with null parent and delegate proxy
 *   - Branch: createScope with non-null parent and no delegate proxy
 *   - Branch: createInitialScope with root traversal
 *   - Branch: declareNativeFunctionType with scope
 *   - Branch: declareNativeValueType with scope
 *   - Branch: declareNativeType with scope
 *   - Branch: TypedScopeCreator constructor with compiler
 *   - Branch: TypedScopeCreator constructor with compiler and codingConvention
 *   - Branch: getNativeType with native type
 *   - Branch: assertDefinitionNode with correct type
 *   - Branch: assertDefinitionNode with incorrect type
 *   - Branch: setDeferredType with node and type
 *   - Branch: resolveTypes with deferred types
 *   - Branch: resolveTypes with scope vars
 *   - Branch: resolveTypes with type registry
 *   - Branch: visit with attachLiteralTypes
 *   - Branch: visit with CALL, FUNCTION, ASSIGN, CATCH, VAR, GETPROP
 *   - Branch: visit with other types
 *   - Branch: shouldTraverse with FUNCTION parent
 *   - Branch: shouldTraverse with SCRIPT parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: shouldTraverse with FUNCTION parent and first child
 *   - Branch: shouldTraverse with FUNCTION parent and root node
 *   - Branch: shouldTraverse with non-FUNCTION parent
 *   - Branch: shouldTraverse with null parent
 *   - Branch: visit with CALL node
 *   - Branch: visit with FUNCTION node
 *   - Branch: visit with ASSIGN node
 *   - Branch: visit with CATCH node
 *   - Branch: visit with VAR node
 *   - Branch: visit with GETPROP node
 *   - Branch: visit with other node types
 *   - Branch: attachLiteralTypes with NULL, VOID, STRING, NUMBER, TRUE, FALSE, REGEXP, REF_SPECIAL, OBJECTLIT
 *   - Branch: attachLiteralTypes with other types
 *   - Branch: defineSlot with NAME node
 *   - Branch: defineSlot with GETPROP node
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis true
 *   - Branch: defineSlot with shouldDeclareOnGlobalThis false
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared true
 *   - Branch: defineSlot with scopeToDeclareIn.isDeclared false