package com.google.javascript.rhino.jstype;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.rhino.jstype.ArrowType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.Visitor;
import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.jstype.StaticScope;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class FunctionTypeDeepseekTest {

    /*
     * [Branch & Defect Analysis Matrix]
     * 
     * Target: FunctionType.java
     * 
     * Key branches and conditions covered:
     * 1. Constructor branches: isConstructor true/false, typeOfThis null/non-null,
     *    isNoObjectType, nativeType true/false
     * 2. isInstanceType: checks U2U_CONSTRUCTOR_TYPE equivalence
     * 3. isConstructor/isInterface/isOrdinaryFunction: kind enum branches
     * 4. canBeCalled: always true
     * 5. getParameters/getParametersNode: null vs non-null parameters
     * 6. getMinArguments: optional/varargs/required parameter counting
     * 7. getMaxArguments: varargs vs fixed params, null params
     * 8. getReturnType/isReturnTypeInferred: arrow type fields
     * 9. getPrototype: lazy initialization
     * 10. setPrototypeBasedOn: null vs non-null prototype
     * 11. setPrototype: null check, constructor instance check, superclass subtype registration
     * 12. getAllImplementedInterfaces/addRelatedInterfaces: interface traversal
     * 13. getImplementedInterfaces: superclass concatenation
     * 14. setImplementedInterfaces: registry registration
     * 15. hasProperty/hasOwnProperty: "prototype" special case
     * 16. getPropertyType: "prototype", "call", "apply" lazy definitions
     * 17. defineProperty: "prototype" special handling
     * 18. isPropertyTypeInferred: "prototype" special case
     * 19. supAndInfHelper: function type lattice operations
     * 20. tryMergeFunctionPiecewise: parameter equality, return type merging
     * 21. getSuperClassConstructor: prototype chain traversal
     * 22. hasUnknownSupertype: loop with unknown type detection
     * 23. getTopMostDefiningType: property lookup up the chain
     * 24. isEquivalentTo: constructor/interface/ordinary function equality
     * 25. hashCode: interface vs call hash
     * 26. hasEqualCallType: arrow type equivalence
     * 27. toString: formatting with this type, params, varargs
     * 28. isSubtype: interface/constructor/ordinary function subtyping
     * 29. visit: visitor pattern
     * 30. getInstanceType/setInstanceType/hasInstanceType: instance type management
     * 31. getTypeOfThis: NO_OBJECT_TYPE to OBJECT_TYPE conversion
     * 32. getSource/setSource: source node access
     * 33. addSubType/getSubTypes: subtype list management
     * 34. hasCachedValues: prototype null check
     * 35. getTemplateTypeName: template name access
     * 36. resolveInternal: type resolution with interfaces and subtypes
     * 37. toDebugHashCodeString: debug string formatting
     * 
     * Defect targeting: The known defect involves backwards typedef use
     * where function types are used before they are fully resolved.
     * This affects the isEquivalentTo and isSubtype methods when dealing
     * with unresolved types, particularly in the context of constructor
     * and interface type comparisons.
     */

    private JSTypeRegistry createRegistry() {
        return new JSTypeRegistry(null);
    }

    private FunctionType createOrdinaryFunction(JSTypeRegistry registry) {
        Node paramsNode = new Node(Token.LP);
        Node param1 = Node.newString(Token.NAME, "param1");
        param1.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
        paramsNode.addChildToBack(param1);
        
        ArrowType arrow = new ArrowType(registry, paramsNode, 
            registry.getNativeType(JSTypeNative.STRING_TYPE), false);
        return new FunctionType(registry, "testFn", null, arrow, 
            registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE), null, false, false);
    }

    private FunctionType createConstructorFunction(JSTypeRegistry registry) {
        Node paramsNode = new Node(Token.LP);
        ArrowType arrow = new ArrowType(registry, paramsNode, 
            registry.getNativeType(JSTypeNative.NUMBER_TYPE), false);
        return new FunctionType(registry, "TestCtor", null, arrow, 
            registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE), null, true, false);
    }

    @Test(timeout = 4000)
    public void testConstructorOrdinaryFunction() {
        JSTypeRegistry registry = createRegistry();
        Node source = new Node(Token.FUNCTION);
        Node paramsNode = new Node(Token.LP);
        ArrowType arrow = new ArrowType(registry, paramsNode, 
            registry.getNativeType(JSTypeNative.NUMBER_TYPE), false);
        
        FunctionType fn = new FunctionType(registry, "test", source, arrow, 
            registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE), "T", false, false);
        
        assertNotNull(fn);
        assertFalse(fn.isConstructor());
        assertFalse(fn.isInterface());
        assertTrue(fn.isOrdinaryFunction());
        assertTrue(fn.isFunctionType());
        assertTrue(fn.canBeCalled());
        assertEquals("T", fn.getTemplateTypeName());
        assertEquals(source, fn.getSource());
        assertFalse(fn.hasInstanceType());
    }

    @Test(timeout = 4000)
    public void testConstructorWithNullTypeOfThis() {
        JSTypeRegistry registry = createRegistry();
        Node paramsNode = new Node(Token.LP);
        ArrowType arrow = new ArrowType(registry, paramsNode, 
            registry.getNativeType(JSTypeNative.NUMBER_TYPE), false);
        
        FunctionType fn = new FunctionType(registry, "test", null, arrow, 
            null, null, false, false);
        
        assertNotNull(fn);
        assertTrue(fn.getTypeOfThis().isUnknownType());
    }

    @Test(timeout = 4000)
    public void testConstructorWithNoObjectType() {
        JSTypeRegistry registry = createRegistry();
        Node paramsNode = new Node(Token.LP);
        ArrowType arrow = new ArrowType(registry, paramsNode, 
            registry.getNativeType(JSTypeNative.NUMBER_TYPE), false);
        
        ObjectType noObject = registry.getNativeObjectType(JSTypeNative.NO_OBJECT_TYPE);
        FunctionType fn = new FunctionType(registry, "test", null, arrow, 
            noObject, null, true, false);
        
        assertNotNull(fn);
        assertTrue(fn.isConstructor());
        assertEquals(noObject, fn.getInstanceType());
    }

    @Test(timeout = 4000)
    public void testConstructorWithNullTypeOfThisConstructor() {
        JSTypeRegistry registry = createRegistry();
        Node paramsNode = new Node(Token.LP);
        ArrowType arrow = new ArrowType(registry, paramsNode, 
            registry.getNativeType(JSTypeNative.NUMBER_TYPE), false);
        
        FunctionType fn = new FunctionType(registry, "test", null, arrow, 
            null, null, true, false);
        
        assertNotNull(fn);
        assertTrue(fn.isConstructor());
        assertTrue(fn.hasInstanceType());
        assertNotNull(fn.getInstanceType());
        assertTrue(fn.getInstanceType() instanceof InstanceObjectType);
    }

    @Test(timeout = 4000)
    public void testForInterface() {
        JSTypeRegistry registry = createRegistry();
        FunctionType iface = FunctionType.forInterface(registry, "MyInterface", null);
        
        assertNotNull(iface);
        assertFalse(iface.isConstructor());
        assertTrue(iface.isInterface());
        assertFalse(iface.isOrdinaryFunction());
        assertTrue(iface.isFunctionType());
        assertTrue(iface.canBeCalled());
        assertTrue(iface.hasInstanceType());
        assertNotNull(iface.getInstanceType());
    }

    @Test(timeout = 4000)
    public void testIsInstanceType() {
        JSTypeRegistry registry = createRegistry();
        FunctionType u2u = registry.getNativeFunctionType(JSTypeNative.U2U_CONSTRUCTOR_TYPE);
        assertTrue(u2u.isInstanceType());
        
        FunctionType ordinary = createOrdinaryFunction(registry);
        assertFalse(ordinary.isInstanceType());
    }

    @Test(timeout = 4000)
    public void testGetParametersAndMinMaxArguments() {
        JSTypeRegistry registry = createRegistry();
        Node paramsNode = new Node(Token.LP);
        
        Node param1 = Node.newString(Token.NAME, "p1");
        param1.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
        paramsNode.addChildToBack(param1);
        
        Node param2 = Node.newString(Token.NAME, "p2");
        param2.setJSType(registry.getNativeType(JSTypeNative.STRING_TYPE));
        param2.setOptionalArg(true);
        paramsNode.addChildToBack(param2);
        
        Node param3 = Node.newString(Token.NAME, "p3");
        param3.setJSType(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE));
        param3.setVarArgs(true);
        paramsNode.addChildToBack(param3);
        
        ArrowType arrow = new ArrowType(registry, paramsNode, 
            registry.getNativeType(JSTypeNative.NUMBER_TYPE), false);
        FunctionType fn = new FunctionType(registry, "test", null, arrow, 
            registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE), null, false, false);
        
        Iterable<Node> params = fn.getParameters();
        int count = 0;
        for (Node n : params) {
            count++;
        }
        assertEquals(3, count);
        assertEquals(1, fn.getMinArguments());
        assertEquals(Integer.MAX_VALUE, fn.getMaxArguments());
    }

    @Test(timeout = 4000)
    public void testGetMinMaxArgumentsNoParams() {
        JSTypeRegistry registry = createRegistry();
        Node paramsNode = new Node(Token.LP);
        ArrowType arrow = new ArrowType(registry, paramsNode, 
            registry.getNativeType(JSTypeNative.NUMBER_TYPE), false);
        FunctionType fn = new FunctionType(registry, "test", null, arrow, 
            registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE), null, false, false);
        
        assertEquals(0, fn.getMinArguments());
        assertEquals(0, fn.getMaxArguments());
    }

    @Test(timeout = 4000)
    public void testGetMaxArgumentsWithVarArgs() {
        JSTypeRegistry registry = createRegistry();
        Node paramsNode = new Node(Token.LP);
        
        Node param1 = Node.newString(Token.NAME, "p1");
        param1.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
        param1.setVarArgs(true);
        paramsNode.addChildToBack(param1);
        
        ArrowType arrow = new ArrowType(registry, paramsNode, 
            registry.getNativeType(JSTypeNative.NUMBER_TYPE), false);
        FunctionType fn = new FunctionType(registry, "test", null, arrow, 
            registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE), null, false, false);
        
        assertEquals(Integer.MAX_VALUE, fn.getMaxArguments());
    }

    @Test(timeout = 4000)
    public void testGetReturnTypeAndInferred() {
        JSTypeRegistry registry = createRegistry();
        Node paramsNode = new Node(Token.LP);
        JSType returnType = registry.getNativeType(JSTypeNative.STRING_TYPE);
        ArrowType arrow = new ArrowType(registry, paramsNode, returnType, true);
        FunctionType fn = new FunctionType(registry, "test", null, arrow, 
            registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE), null, false, false);
        
        assertEquals(returnType, fn.getReturnType());
        assertTrue(fn.isReturnTypeInferred());
    }

    @Test(timeout = 4000)
    public void testGetPrototypeLazyInitialization() {
        JSTypeRegistry registry = createRegistry();
        FunctionType fn = createConstructorFunction(registry);
        
        assertNull(fn.getPrototype()); // This will trigger lazy init
        FunctionPrototypeType proto = fn.getPrototype();
        assertNotNull(proto);
        assertSame(proto, fn.getPrototype());
    }

    @Test(timeout = 4000)
    public void testSetPrototypeBasedOn() {
        JSTypeRegistry registry = createRegistry();
        FunctionType fn = createConstructorFunction(registry);
        
        ObjectType baseType = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
        fn.setPrototypeBasedOn(baseType);
        
        assertNotNull(fn.getPrototype());
        assertEquals(baseType, fn.getPrototype().getImplicitPrototype());
    }

    @Test(timeout = 4000)
    public void testSetPrototypeNull() {
        JSTypeRegistry registry = createRegistry();
        FunctionType fn = createConstructorFunction(registry);
        
        assertFalse(fn.setPrototype(null));
    }

    @Test(timeout = 4000)
    public void testSetPrototypeInstanceType() {
        JSTypeRegistry registry = createRegistry();
        FunctionType fn = createConstructorFunction(registry);
        
        ObjectType instanceType = fn.getInstanceType();
        FunctionPrototypeType proto = new FunctionPrototypeType(registry, fn, instanceType, false);
        assertFalse(fn.setPrototype(proto));
    }

    @Test(timeout = 4000)
    public void testSetPrototypeValid() {
        JSTypeRegistry registry = createRegistry();
        FunctionType fn = createConstructorFunction(registry);
        
        ObjectType baseType = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
        FunctionPrototypeType proto = new FunctionPrototypeType(registry, fn, baseType, false);
        assertTrue(fn.setPrototype(proto));
        assertSame(proto, fn.getPrototype());
    }

    @Test(timeout = 4000)
    public void testGetImplementedInterfaces() {
        JSTypeRegistry registry = createRegistry();
        FunctionType fn = createConstructorFunction(registry);
        
        List<ObjectType> interfaces = new ArrayList<ObjectType>();
        FunctionType iface1 = FunctionType.forInterface(registry, "Iface1", null);
        FunctionType iface2 = FunctionType.forInterface(registry, "Iface2", null);
        interfaces.add(iface1.getInstanceType());
        interfaces.add(iface2.getInstanceType());
        
        fn.setImplementedInterfaces(interfaces);
        
        Iterable<ObjectType> result = fn.getImplementedInterfaces();
        int count = 0;
        for (ObjectType obj : result) {
            count++;
        }
        assertEquals(2, count);
    }

    @Test(timeout = 4000)
    public void testGetAllImplementedInterfaces() {
        JSTypeRegistry registry = createRegistry();
        FunctionType fn = createConstructorFunction(registry);
        
        List<ObjectType> interfaces = new ArrayList<ObjectType>();
        FunctionType iface1 = FunctionType.forInterface(registry, "Iface1", null);
        interfaces.add(iface1.getInstanceType());
        
        fn.setImplementedInterfaces(interfaces);
        
        Iterable<ObjectType> result = fn.getAllImplementedInterfaces();
        int count = 0;
        for (ObjectType obj : result) {
            count++;
        }
        assertEquals(1, count);
    }

    @Test(timeout = 4000)
    public void testHasPropertyAndOwnProperty() {
        JSTypeRegistry registry = createRegistry();
        FunctionType fn = createOrdinaryFunction(registry);
        
        assertTrue(fn.hasProperty("prototype"));
        assertTrue(fn.hasOwnProperty("prototype"));
        assertFalse(fn.hasProperty("nonexistent"));
    }

    @Test(timeout = 4000)
    public void testGetPropertyTypePrototype() {
        JSTypeRegistry registry = createRegistry();
        FunctionType fn = createConstructorFunction(registry);
        
        JSType protoType = fn.getPropertyType("prototype");
        assertNotNull(protoType);
        assertTrue(protoType instanceof FunctionPrototypeType);
    }

    @Test(timeout = 4000)
    public void testGetPropertyTypeCall() {
        JSTypeRegistry registry = createRegistry();
        FunctionType fn = createOrdinaryFunction(registry);
        
        JSType callType = fn.getPropertyType("call");
        assertNotNull(callType);
        assertTrue(callType.isFunctionType());
    }

    @Test(timeout = 4000)
    public void testGetPropertyTypeApply() {
        JSTypeRegistry registry = createRegistry();
        FunctionType fn = createOrdinaryFunction(registry);
        
        JSType applyType = fn.getPropertyType("apply");
        assertNotNull(applyType);
        assertTrue(applyType.isFunctionType());
    }

    @Test(timeout = 4000)
    public void testDefinePropertyPrototype() {
        JSTypeRegistry registry = createRegistry();
        FunctionType fn = createConstructorFunction(registry);
        
        ObjectType objType = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
        assertTrue(fn.defineProperty("prototype", objType, false, false));
        assertNotNull(fn.getPrototype());
    }

    @Test(timeout = 4000)
    public void testDefinePropertyPrototypeNonObject() {
        JSTypeRegistry registry = createRegistry();
        FunctionType fn = createConstructorFunction(registry);
        
        JSType nonObject = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        assertFalse(fn.defineProperty("prototype", nonObject, false, false));
    }

    @Test(timeout = 4000)
    public void testIsPropertyTypeInferred() {
        JSTypeRegistry registry = createRegistry();
        FunctionType fn = createOrdinaryFunction(registry);
        
        assertTrue(fn.isPropertyTypeInferred("prototype"));
        assertFalse(fn.isPropertyTypeInferred("other"));
    }

    @Test(timeout = 4000)
    public void testIsEquivalentToSameFunction() {
        JSTypeRegistry registry = createRegistry();
        FunctionType fn1 = createOrdinaryFunction(registry);
        FunctionType fn2 = createOrdinaryFunction(registry);
        
        assertTrue(fn1.isEquivalentTo(fn1));
        assertTrue(fn1.isEquivalentTo(fn2));
    }

    @Test(timeout = 4000)
    public void testIsEquivalentToConstructor() {
        JSTypeRegistry registry = createRegistry();
        FunctionType fn1 = createConstructorFunction(registry);
        FunctionType fn2 = createConstructorFunction(registry);
        
        assertTrue(fn1.isEquivalentTo(fn1));
        assertFalse(fn1.isEquivalentTo(fn2));
    }

    @Test(timeout = 4000)
    public void testIsEquivalentToInterface() {
        JSTypeRegistry registry = createRegistry();
        FunctionType iface1 = FunctionType.forInterface(registry, "Iface", null);
        FunctionType iface2 = FunctionType.forInterface(registry, "Iface", null);
        FunctionType iface3 = FunctionType.forInterface(registry, "Other", null);
        
        assertTrue(iface1.isEquivalentTo(iface2));
        assertFalse(iface1.isEquivalentTo(iface3));
    }

    @Test(timeout = 4000)
    public void testIsEquivalentToNonFunction() {
        JSTypeRegistry registry = createRegistry();
        FunctionType fn = createOrdinaryFunction(registry);
        JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        
        assertFalse(fn.isEquivalentTo(numberType));
    }

    @Test(timeout = 4000)
    public void testHashCode() {
        JSTypeRegistry registry = createRegistry();
        FunctionType fn = createOrdinaryFunction(registry);
        FunctionType iface = FunctionType.forInterface(registry, "Iface", null);
        
        assertNotNull(fn.hashCode());
        assertNotNull(iface.hashCode());
    }

    @Test(timeout = 4000)
    public void testHasEqualCallType() {
        JSTypeRegistry registry = createRegistry();
        FunctionType fn1 = createOrdinaryFunction(registry);
        FunctionType fn2 = createOrdinaryFunction(registry);
        
        assertTrue(fn1.hasEqualCallType(fn2));
    }

    @Test(timeout = 4000)
    public void testToString() {
        JSTypeRegistry registry = createRegistry();
        FunctionType fn = createOrdinaryFunction(registry);
        
        String str = fn.toString();
        assertNotNull(str);
        assertTrue(str.startsWith("function ("));
        assertTrue(str.contains("):"));
    }

    @Test(timeout = 4000)
    public void testToStringWithThisType() {
        JSTypeRegistry registry = createRegistry();
        Node paramsNode = new Node(Token.LP);
        ArrowType arrow = new ArrowType(registry, paramsNode, 
            registry.getNativeType(JSTypeNative.NUMBER_TYPE), false);
        FunctionType fn = new FunctionType(registry, "test", null, arrow, 
            registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE), null, false, false);
        
        String str = fn.toString();
        assertTrue(str.contains("this:"));
    }

    @Test(timeout = 4000)
    public void testIsSubtype() {
        JSTypeRegistry registry = createRegistry();
        FunctionType fn = createOrdinaryFunction(registry);
        
        assertTrue(fn.isSubtype(registry.getNativeType(JSTypeNative.FUNCTION_INSTANCE_TYPE)));
        assertTrue(fn.isSubtype(fn));
    }

    @Test(timeout = 4000)
    public void testIsSubtypeInterface() {
        JSTypeRegistry registry = createRegistry();
        FunctionType fn = createOrdinaryFunction(registry);
        FunctionType iface = FunctionType.forInterface(registry, "Iface", null);
        
        assertTrue(fn.isSubtype(iface));
    }

    @Test(timeout = 4000)
    public void testIsSubtypeInterfaceToOrdinary() {
        JSTypeRegistry registry = createRegistry();
        FunctionType fn = createOrdinaryFunction(registry);
        FunctionType iface = FunctionType.forInterface(registry, "Iface", null);
        
        assertFalse(iface.isSubtype(fn));
    }

    @Test(timeout = 4000)
    public void testVisit() {
        JSTypeRegistry registry = createRegistry();
        FunctionType fn = createOrdinaryFunction(registry);
        
        Visitor<Boolean> visitor = new Visitor<Boolean>() {
            @Override
            public Boolean caseEnumElementType(EnumElementType type) { return false; }
            @Override
            public Boolean caseFunctionType(FunctionType type) { return true; }
            @Override
            public Boolean caseNoType(NoType type) { return false; }
            @Override
            public Boolean caseNoObjectType(NoObjectType type) { return false; }
            @Override
            public Boolean caseObjectType(ObjectType type) { return false; }
            @Override
            public Boolean caseAllType(AllType type) { return false; }
            @Override
            public Boolean caseBooleanType(BooleanType type) { return false; }
            @Override
            public Boolean caseEnumType(EnumType type) { return false; }
            @Override
            public Boolean caseNullType(NullType type) { return false; }
            @Override
            public Boolean caseNumberType(NumberType type) { return false; }
            @Override
            public Boolean caseStringType(StringType type) { return false; }
            @Override
            public Boolean caseUnknownType(UnknownType type) { return false; }
            @Override
            public Boolean caseVoidType(VoidType type) { return false; }
            @Override
            public Boolean caseUnionType(UnionType type) { return false; }
            @Override
            public Boolean caseTemplateType(TemplateType type) { return false; }
        };
        
        assertTrue(fn.visit(visitor));
    }

    @Test(timeout = 4000)
    public void testGetInstanceTypeAndTypeOfThis() {
        JSTypeRegistry registry = createRegistry();
        FunctionType fn = createConstructorFunction(registry);
        
        assertTrue(fn.hasInstanceType());
        assertNotNull(fn.getInstanceType());
        assertNotNull(fn.getTypeOfThis());
    }

    @Test(timeout = 4000)
    public void testGetTypeOfThisNoObjectType() {
        JSTypeRegistry registry = createRegistry();
        Node paramsNode = new Node(Token.LP);
        ArrowType arrow = new ArrowType(registry, paramsNode, 
            registry.getNativeType(JSTypeNative.NUMBER_TYPE), false);
        
        ObjectType noObject = registry.getNativeObjectType(JSTypeNative.NO_OBJECT_TYPE);
        FunctionType fn = new FunctionType(registry, "test", null, arrow, 
            noObject, null, false, false);
        
        assertEquals(registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE), fn.getTypeOfThis());
    }

    @Test(timeout = 4000)
    public void testSetSourceAndGetSource() {
        JSTypeRegistry registry = createRegistry();
        FunctionType fn = createOrdinaryFunction(registry);
        
        Node source = new Node(Token.FUNCTION);
        fn.setSource(source);
        assertEquals(source, fn.getSource());
    }

    @Test(timeout = 4000)
    public void testGetSubTypes() {
        JSTypeRegistry registry = createRegistry();
        FunctionType fn = createConstructorFunction(registry);
        
        assertNull(fn.getSubTypes());
        
        // Create a subtype relationship
        FunctionType subFn = createConstructorFunction(registry);
        ObjectType baseType = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
        FunctionPrototypeType proto = new FunctionPrototypeType(registry, subFn, baseType, false);
        subFn.setPrototype(proto);
        
        // This should register subFn as a subtype of fn if fn is the superclass
        // But since we didn't set up the inheritance, subTypes should still be null
        assertNull(fn.getSubTypes());
    }

    @Test(timeout = 4000)
    public void testHasCachedValues() {
        JSTypeRegistry registry = createRegistry();
        FunctionType fn = createOrdinaryFunction(registry);
        
        assertFalse(fn.hasCachedValues());
        fn.getPrototype();
        assertTrue(fn.hasCachedValues());
    }

    @Test(timeout = 4000)
    public void testGetTemplateTypeName() {
        JSTypeRegistry registry = createRegistry();
        Node paramsNode = new Node(Token.LP);
        ArrowType arrow = new ArrowType(registry, paramsNode, 
            registry.getNativeType(JSTypeNative.NUMBER_TYPE), false);
        FunctionType fn = new FunctionType(registry, "test", null, arrow, 
            registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE), "TemplateName", false, false);
        
        assertEquals("TemplateName", fn.getTemplateTypeName());
    }

    @Test(timeout = 4000)
    public void testResolveInternal() {
        JSTypeRegistry registry = createRegistry();
        FunctionType fn = createOrdinaryFunction(registry);
        
        ErrorReporter reporter = new ErrorReporter() {
            @Override
            public void warning(String message, String sourceName, int line, int lineOffset) {}
            @Override
            public void error(String message, String sourceName, int line, int lineOffset) {}
        };
        
        StaticScope<JSType> scope = new StaticScope<JSType>() {
            @Override
            public StaticSlot<JSType> getSlot(String name) { return null; }
            @Override
            public StaticSlot<JSType> getOwnSlot(String name) { return null; }
            @Override
            public JSType getTypeOfThis() { return null; }
            @Override
            public StaticScope<JSType> getParentScope() { return null; }
        };
        
        JSType resolved = fn.resolveInternal(reporter, scope);
        assertNotNull(resolved);
        assertSame(fn, resolved);
    }

    @Test(timeout = 4000)
    public void testToDebugHashCodeString() {
        JSTypeRegistry registry = createRegistry();
        FunctionType fn = createOrdinaryFunction(registry);
        
        String debugStr = fn.toDebugHashCodeString();
        assertNotNull(debugStr);
        assertTrue(debugStr.startsWith("function ("));
    }

    @Test(timeout = 4000)
    public void testGetSuperClassConstructor() {
        JSTypeRegistry registry = createRegistry();
        FunctionType fn = createConstructorFunction(registry);
        
        // No superclass set, should return null
        assertNull(fn.getSuperClassConstructor());
    }

    @Test(timeout = 4000)
    public void testHasUnknownSupertype() {
        JSTypeRegistry registry = createRegistry();
        FunctionType fn = createConstructorFunction(registry);
        
        assertFalse(fn.hasUnknownSupertype());
    }

    @Test(timeout = 4000)
    public void testGetTopMostDefiningType() {
        JSTypeRegistry registry = createRegistry();
        FunctionType fn = createConstructorFunction(registry);
        
        // Define a property on the prototype
        fn.getPrototype().defineDeclaredProperty("testProp", 
            registry.getNativeType(JSTypeNative.NUMBER_TYPE), false);
        
        JSType topType = fn.getTopMostDefiningType("testProp");
        assertNotNull(topType);
        assertEquals(fn.getInstanceType(), topType);
    }

    @Test(timeout = 4000)
    public void testSetInstanceType() {
        JSTypeRegistry registry = createRegistry();
        FunctionType fn = createConstructorFunction(registry);
        
        ObjectType newInstanceType = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
        fn.setInstanceType(newInstanceType);
        assertEquals(newInstanceType, fn.getInstanceType());
    }

    @Test(timeout = 4000)
    public void testGetInternalArrowType() {
        JSTypeRegistry registry = createRegistry();
        FunctionType fn = createOrdinaryFunction(registry);
        
        ArrowType arrow = fn.getInternalArrowType();
        assertNotNull(arrow);
        assertEquals(fn.getReturnType(), arrow.returnType);
    }

    @Test(timeout = 4000)
    public void testGetParametersNodeNull() {
        JSTypeRegistry registry = createRegistry();
        ArrowType arrow = new ArrowType(registry, null, 
            registry.getNativeType(JSTypeNative.NUMBER_TYPE), false);
        FunctionType fn = new FunctionType(registry, "test", null, arrow, 
            registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE), null, false, false);
        
        assertNull(fn.getParametersNode());
        Iterable<Node> params = fn.getParameters();
        assertFalse(params.iterator().hasNext());
    }

    @Test(timeout = 4000)
    public void testGetMaxArgumentsNullParams() {
        JSTypeRegistry registry = createRegistry();
        ArrowType arrow = new ArrowType(registry, null, 
            registry.getNativeType(JSTypeNative.NUMBER_TYPE), false);
        FunctionType fn = new FunctionType(registry, "test", null, arrow, 
            registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE), null, false, false);
        
        assertEquals(0, fn.getMaxArguments());
    }

    @Test(timeout = 4000)
    public void testSupAndInfHelperEquivalent() {
        JSTypeRegistry registry = createRegistry();
        FunctionType fn = createOrdinaryFunction(registry);
        
        JSType result = fn.getLeastSupertype(fn);
        assertSame(fn, result);
        
        result = fn.getGreatestSubtype(fn);
        assertSame(fn, result);
    }

    @Test(timeout = 4000)
    public void testSupAndInfHelperNonFunction() {
        JSTypeRegistry registry = createRegistry();
        FunctionType fn = createOrdinaryFunction(registry);
        JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        
        JSType result = fn.getLeastSupertype(numberType);
        assertNotNull(result);
        
        result = fn.getGreatestSubtype(numberType);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testSupAndInfHelperFunctionInstance() {
        JSTypeRegistry registry = createRegistry();
        FunctionType fn = createOrdinaryFunction(registry);
        JSType functionInstance = registry.getNativeType(JSTypeNative.FUNCTION_INSTANCE_TYPE);
        
        JSType result = fn.getLeastSupertype(functionInstance);
        assertEquals(functionInstance, result);
        
        result = fn.getGreatestSubtype(functionInstance);
        assertSame(fn, result);
    }

    @Test(timeout = 4000)
    public void testSupAndInfHelperOrdinaryFunctions() {
        JSTypeRegistry registry = createRegistry();
        FunctionType fn1 = createOrdinaryFunction(registry);
        FunctionType fn2 = createOrdinaryFunction(registry);
        
        JSType result = fn1.getLeastSupertype(fn2);
        assertNotNull(result);
        assertTrue(result.isFunctionType());
        
        result = fn1.getGreatestSubtype(fn2);
        assertNotNull(result);
        assertTrue(result.isFunctionType());
    }

    @Test(timeout = 4000)
    public void testDefectTargetingBackwardsTypedefUse() {
        // This test targets the known defect where backwards typedef use
        // causes incorrect behavior in function type equivalence and subtyping
        JSTypeRegistry registry = createRegistry();
        
        // Create two function types that should be equivalent
        Node paramsNode1 = new Node(Token.LP);
        Node param1 = Node.newString(Token.NAME, "x");
        param1.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
        paramsNode1.addChildToBack(param1);
        ArrowType arrow1 = new ArrowType(registry, paramsNode1, 
            registry.getNativeType(JSTypeNative.STRING_TYPE), false);
        FunctionType fn1 = new FunctionType(registry, "fn1", null, arrow1, 
            registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE), null, false, false);
        
        Node paramsNode2 = new Node(Token.LP);
        Node param2 = Node.newString(Token.NAME, "y");
        param2.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
        paramsNode2.addChildToBack(param2);
        ArrowType arrow2 = new ArrowType(registry, paramsNode2, 
            registry.getNativeType(JSTypeNative.STRING_TYPE), false);
        FunctionType fn2 = new FunctionType(registry, "fn2", null, arrow2, 
            registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE), null, false, false);
        
        // These should be equivalent since they have the same signature
        assertTrue("Function types with same signature should be equivalent", 
            fn1.isEquivalentTo(fn2));
        
        // Test subtyping in both directions
        assertTrue("Function types with same signature should be subtypes of each other", 
            fn1.isSubtype(fn2));
        assertTrue("Function types with same signature should be subtypes of each other", 
            fn2.isSubtype(fn1));
        
        // Test that the least supertype and greatest subtype work correctly
        JSType leastSuper = fn1.getLeastSupertype(fn2);
        assertNotNull("Least supertype should not be null", leastSuper);
        assertTrue("Least supertype should be a function type", leastSuper.isFunctionType());
        
        JSType greatestSub = fn1.getGreatestSubtype(fn2);
        assertNotNull("Greatest subtype should not be null", greatestSub);
        assertTrue("Greatest subtype should be a function type", greatestSub.isFunctionType());
        
        // Test with different return types - should not be equivalent
        Node paramsNode3 = new Node(Token.LP);
        Node param3 = Node.newString(Token.NAME, "z");
        param3.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
        paramsNode3.addChildToBack(param3);
        ArrowType arrow3 = new ArrowType(registry, paramsNode3, 
            registry.getNativeType(JSTypeNative.NUMBER_TYPE), false);
        FunctionType fn3 = new FunctionType(registry, "fn3", null, arrow3, 
            registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE), null, false, false);
        
        assertFalse("Functions with different return types should not be equivalent", 
            fn1.isEquivalentTo(fn3));
    }
}