package com.fasterxml.jackson.databind.jsontype.impl;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.type.TypeFactory;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.fasterxml.jackson.databind.jsontype.impl.SubTypeValidator
 *
 * Decision / Condition Matrix:
 * 1. _cfgIllegalClassNames.contains(full):
 *    - TRUE:  Throws JsonMappingException ("Illegal type (...) to deserialize...").
 *    - FALSE: Falls through to Spring/interface checks.
 * 2. raw.isInterface():
 *    - TRUE:  Empty statement (allowed, falls through to return).
 *    - FALSE: Evaluates full.startsWith(PREFIX_SPRING).
 * 3. full.startsWith(PREFIX_SPRING):
 *    - TRUE:  Iterates over superclass hierarchy ((cls != null) && (cls != Object.class)).
 *             Condition A: cls.getSimpleName().equals("AbstractPointcutAdvisor") -> breaks main_check (rejected).
 *             Condition B: cls.getSimpleName().equals("AbstractApplicationContext") -> breaks main_check (rejected).
 *             Condition C: Neither matches -> returns normally (allowed).
 *    - FALSE: Returns normally (allowed).
 * 4. Ground Truth Defect (Defects4J - Databind #1931 / testC3P0Types):
 *    - C3P0 connection pool types (e.g., com.mchange.v2.c3p0.ComboPooledDataSource)
 *      were commented out / unhandled in SubTypeValidator. Deserialization of such types
 *      must be rejected with JsonMappingException containing "Illegal type".
 *      On the defective version, validateSubType returns normally without throwing, exposing the bug.
 */
public class SubTypeValidatorGeminiTest {

    private SubTypeValidator validator;

    private static final DynamicClassLoader DYNAMIC_CL =
            new DynamicClassLoader(SubTypeValidatorGeminiTest.class.getClassLoader());

    @Before
    public void setUp() {
        validator = SubTypeValidator.instance();
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testSingletonInstanceContract() {
        SubTypeValidator first = SubTypeValidator.instance();
        SubTypeValidator second = SubTypeValidator.instance();
        assertNotNull("Singleton instance must not be null", first);
        assertSame("SubTypeValidator.instance() must return the identical singleton instance", first, second);
    }

    @Test(timeout = 4000)
    public void testValidateAllowedJdkTypes() throws Exception {
        JavaType stringType = TypeFactory.defaultInstance().constructType(String.class);
        JavaType intType = TypeFactory.defaultInstance().constructType(Integer.class);
        JavaType mapType = TypeFactory.defaultInstance().constructType(java.util.HashMap.class);

        // These standard types should pass without throwing any exception
        validator.validateSubType(null, stringType);
        validator.validateSubType(null, intType);
        validator.validateSubType(null, mapType);
    }

    @Test(timeout = 4000)
    public void testValidateAllowedInterfaces() throws Exception {
        JavaType listInterface = TypeFactory.defaultInstance().constructType(java.util.List.class);
        JavaType serializableInterface = TypeFactory.defaultInstance().constructType(java.io.Serializable.class);

        validator.validateSubType(null, listInterface);
        validator.validateSubType(null, serializableInterface);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testRootObjectClassAllowed() throws Exception {
        JavaType objectType = TypeFactory.defaultInstance().constructType(Object.class);
        validator.validateSubType(null, objectType);
    }

    @Test(timeout = 4000)
    public void testSpringInterfaceAllowed() throws Exception {
        // Any interface under org.springframework. must be permitted by raw.isInterface() branch
        Class<?> springInterface = DYNAMIC_CL.createClass(
                "org.springframework.context.ApplicationContext", null, true);
        JavaType springInterfaceType = TypeFactory.defaultInstance().constructType(springInterface);

        validator.validateSubType(null, springInterfaceType);
    }

    @Test(timeout = 4000)
    public void testSpringBenignClassAllowed() throws Exception {
        // A benign class under org.springframework. that doesn't inherit from blocked base types
        Class<?> safeSpringClass = DYNAMIC_CL.createClass(
                "org.springframework.beans.factory.SafeSpringBean", null, false);
        JavaType safeType = TypeFactory.defaultInstance().constructType(safeSpringClass);

        validator.validateSubType(null, safeType);
    }

    @Test(timeout = 4000)
    public void testSpringDeepHierarchyWithoutBlockedTypeAllowed() throws Exception {
        Class<?> base = DYNAMIC_CL.createClass("org.springframework.core.BaseBean", null, false);
        Class<?> mid = DYNAMIC_CL.createClass("org.springframework.core.MidBean", base.getName(), false);
        Class<?> leaf = DYNAMIC_CL.createClass("org.springframework.core.LeafBean", mid.getName(), false);

        JavaType leafType = TypeFactory.defaultInstance().constructType(leaf);
        validator.validateSubType(null, leafType);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J: testC3P0Types)
    // =========================================================================

    /**
     * Targets Defects4J bug where C3P0 types like ComboPooledDataSource are not rejected.
     * Expected: JsonMappingException thrown indicating "Illegal type (...) to deserialize".
     */
    @Test(timeout = 4000)
    public void testDefectC3P0ComboPooledDataSourceBlocked() throws Exception {
        Class<?> c3p0Class = DYNAMIC_CL.createClass(
                "com.mchange.v2.c3p0.ComboPooledDataSource", null, false);
        JavaType c3p0Type = TypeFactory.defaultInstance().constructType(c3p0Class);

        try {
            validator.validateSubType(null, c3p0Type);
            fail("Defect: SubTypeValidator failed to reject C3P0 type: " + c3p0Class.getName());
        } catch (JsonMappingException e) {
            String msg = e.getMessage();
            assertNotNull("Exception message should not be null", msg);
            assertTrue("Expected message to contain 'Illegal type', got: " + msg,
                    msg.contains("Illegal type"));
            assertTrue("Expected message to contain class name, got: " + msg,
                    msg.contains(c3p0Class.getName()));
        }
    }

    @Test(timeout = 4000)
    public void testDefectC3P0JndiRefForwardingDataSourceBlocked() throws Exception {
        Class<?> c3p0Class = DYNAMIC_CL.createClass(
                "com.mchange.v2.c3p0.JndiRefForwardingDataSource", null, false);
        JavaType c3p0Type = TypeFactory.defaultInstance().constructType(c3p0Class);

        try {
            validator.validateSubType(null, c3p0Type);
            fail("Defect: SubTypeValidator failed to reject C3P0 JndiRefForwardingDataSource");
        } catch (JsonMappingException e) {
            assertTrue("Expected message to contain 'Illegal type', got: " + e.getMessage(),
                    e.getMessage().contains("Illegal type"));
        }
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testBlockedJdkLoggingFileHandler() {
        JavaType type = TypeFactory.defaultInstance().constructType(java.util.logging.FileHandler.class);
        try {
            validator.validateSubType(null, type);
            fail("Expected JsonMappingException for java.util.logging.FileHandler");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Illegal type (java.util.logging.FileHandler) to deserialize"));
        }
    }

    @Test(timeout = 4000)
    public void testBlockedJdkRemoteObject() {
        JavaType type = TypeFactory.defaultInstance().constructType(java.rmi.server.UnicastRemoteObject.class);
        try {
            validator.validateSubType(null, type);
            fail("Expected JsonMappingException for java.rmi.server.UnicastRemoteObject");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Illegal type (java.rmi.server.UnicastRemoteObject) to deserialize"));
        }
    }

    @Test(timeout = 4000)
    public void testBlockedJdbcRowSetImpl() {
        try {
            Class<?> clazz = Class.forName("com.sun.rowset.JdbcRowSetImpl");
            JavaType type = TypeFactory.defaultInstance().constructType(clazz);
            validator.validateSubType(null, type);
            fail("Expected JsonMappingException for com.sun.rowset.JdbcRowSetImpl");
        } catch (ClassNotFoundException ignored) {
            // JVM may not contain com.sun.rowset.JdbcRowSetImpl
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Illegal type (com.sun.rowset.JdbcRowSetImpl) to deserialize"));
        }
    }

    @Test(timeout = 4000)
    public void testBlockedCommonsCollectionsInvokerTransformer() throws Exception {
        Class<?> clazz = DYNAMIC_CL.createClass(
                "org.apache.commons.collections.functors.InvokerTransformer", null, false);
        JavaType type = TypeFactory.defaultInstance().constructType(clazz);
        try {
            validator.validateSubType(null, type);
            fail("Expected JsonMappingException for InvokerTransformer");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Illegal type (" + clazz.getName() + ") to deserialize"));
        }
    }

    @Test(timeout = 4000)
    public void testBlockedSpringAbstractPointcutAdvisorHierarchy() throws Exception {
        Class<?> advisorBase = DYNAMIC_CL.createClass(
                "org.springframework.aop.AbstractPointcutAdvisor", null, false);
        Class<?> customAdvisor = DYNAMIC_CL.createClass(
                "org.springframework.aop.support.MyPointcutAdvisor", advisorBase.getName(), false);

        JavaType type = TypeFactory.defaultInstance().constructType(customAdvisor);
        try {
            validator.validateSubType(null, type);
            fail("Expected JsonMappingException for subclass of AbstractPointcutAdvisor");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Illegal type (" + customAdvisor.getName() + ") to deserialize"));
        }
    }

    @Test(timeout = 4000)
    public void testBlockedSpringAbstractApplicationContextHierarchy() throws Exception {
        Class<?> appContextBase = DYNAMIC_CL.createClass(
                "org.springframework.context.AbstractApplicationContext", null, false);
        Class<?> childContext = DYNAMIC_CL.createClass(
                "org.springframework.context.support.CustomXmlApplicationContext", appContextBase.getName(), false);

        JavaType type = TypeFactory.defaultInstance().constructType(childContext);
        try {
            validator.validateSubType(null, type);
            fail("Expected JsonMappingException for subclass of AbstractApplicationContext");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Illegal type (" + childContext.getName() + ") to deserialize"));
        }
    }

    @Test(timeout = 4000)
    public void testBlockedSpringDirectMatchAbstractPointcutAdvisor() throws Exception {
        Class<?> advisorBase = DYNAMIC_CL.createClass(
                "org.springframework.aop.framework.AbstractPointcutAdvisor", null, false);

        JavaType type = TypeFactory.defaultInstance().constructType(advisorBase);
        try {
            validator.validateSubType(null, type);
            fail("Expected JsonMappingException for direct AbstractPointcutAdvisor class");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Illegal type (" + advisorBase.getName() + ") to deserialize"));
        }
    }

    @Test(timeout = 4000)
    public void testBlockedSpringDirectMatchAbstractApplicationContext() throws Exception {
        Class<?> appContextBase = DYNAMIC_CL.createClass(
                "org.springframework.context.support.AbstractApplicationContext", null, false);

        JavaType type = TypeFactory.defaultInstance().constructType(appContextBase);
        try {
            validator.validateSubType(null, type);
            fail("Expected JsonMappingException for direct AbstractApplicationContext class");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Illegal type (" + appContextBase.getName() + ") to deserialize"));
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Subclass Extensibility
    // =========================================================================

    @Test(timeout = 4000)
    public void testSubclassCustomIllegalClassNamesOverride() throws Exception {
        // Subclass to verify that protected field _cfgIllegalClassNames can be modified
        SubTypeValidator customValidator = new SubTypeValidator() {
            {
                Set<String> customSet = new HashSet<String>();
                customSet.add("com.example.security.MaliciousExploit");
                _cfgIllegalClassNames = Collections.unmodifiableSet(customSet);
            }
        };

        Class<?> exploitClass = DYNAMIC_CL.createClass("com.example.security.MaliciousExploit", null, false);
        JavaType exploitType = TypeFactory.defaultInstance().constructType(exploitClass);

        try {
            customValidator.validateSubType(null, exploitType);
            fail("Expected custom illegal type to be rejected");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Illegal type (" + exploitClass.getName() + ") to deserialize"));
        }

        // Standard JDK FileHandler is not in this custom set, so it should be allowed
        JavaType fileHandlerType = TypeFactory.defaultInstance().constructType(java.util.logging.FileHandler.class);
        customValidator.validateSubType(null, fileHandlerType);
    }

    @Test(timeout = 4000)
    public void testSubclassEmptyIllegalClassNames() throws Exception {
        SubTypeValidator lenientValidator = new SubTypeValidator() {
            {
                _cfgIllegalClassNames = Collections.emptySet();
            }
        };

        JavaType fileHandlerType = TypeFactory.defaultInstance().constructType(java.util.logging.FileHandler.class);
        // With empty set, FileHandler should be allowed
        lenientValidator.validateSubType(null, fileHandlerType);
    }

    // =========================================================================
    // Helper: Dynamic Bytecode Generator (JDK 1.5+ bytecode compatible)
    // =========================================================================

    public static class DynamicClassLoader extends ClassLoader {
        public DynamicClassLoader(ClassLoader parent) {
            super(parent);
        }

        public synchronized Class<?> createClass(String name, String superName, boolean isInterface) {
            try {
                return loadClass(name);
            } catch (ClassNotFoundException ignored) {
            }
            byte[] bytes = dumpClass(name.replace('.', '/'),
                    superName == null ? "java/lang/Object" : superName.replace('.', '/'),
                    isInterface);
            return defineClass(name, bytes, 0, bytes.length);
        }

        private byte[] dumpClass(String slashName, String slashSuper, boolean isInterface) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                DataOutputStream out = new DataOutputStream(baos);
                out.writeInt(0xCAFEBABE); // Magic
                out.writeShort(0);        // Minor version
                out.writeShort(49);       // Major version (Java 5)

                if (isInterface) {
                    out.writeShort(5);    // Constant pool count: 4 entries (1..4)
                    // #1 Class slashName
                    out.writeByte(7); out.writeShort(2);
                    // #2 Utf8 slashName
                    out.writeByte(1); out.writeUTF(slashName);
                    // #3 Class slashSuper
                    out.writeByte(7); out.writeShort(4);
                    // #4 Utf8 slashSuper
                    out.writeByte(1); out.writeUTF(slashSuper);

                    out.writeShort(0x0601); // ACC_PUBLIC | ACC_INTERFACE | ACC_ABSTRACT
                    out.writeShort(1);      // this_class (#1)
                    out.writeShort(3);      // super_class (#3)
                    out.writeShort(0);      // interfaces_count
                    out.writeShort(0);      // fields_count
                    out.writeShort(0);      // methods_count
                    out.writeShort(0);      // attributes_count
                } else {
                    out.writeShort(10);   // Constant pool count: 9 entries (1..9)
                    // #1 Class slashName
                    out.writeByte(7); out.writeShort(2);
                    // #2 Utf8 slashName
                    out.writeByte(1); out.writeUTF(slashName);
                    // #3 Class slashSuper
                    out.writeByte(7); out.writeShort(4);
                    // #4 Utf8 slashSuper
                    out.writeByte(1); out.writeUTF(slashSuper);
                    // #5 Utf8 <init>
                    out.writeByte(1); out.writeUTF("<init>");
                    // #6 Utf8 ()V
                    out.writeByte(1); out.writeUTF("()V");
                    // #7 Utf8 Code
                    out.writeByte(1); out.writeUTF("Code");
                    // #8 Methodref #3.#9
                    out.writeByte(10); out.writeShort(3); out.writeShort(9);
                    // #9 NameAndType #5:#6
                    out.writeByte(12); out.writeShort(5); out.writeShort(6);

                    out.writeShort(0x0021); // ACC_PUBLIC | ACC_SUPER
                    out.writeShort(1);      // this_class (#1)
                    out.writeShort(3);      // super_class (#3)
                    out.writeShort(0);      // interfaces_count
                    out.writeShort(0);      // fields_count
                    out.writeShort(1);      // methods_count = 1 (<init>)

                    // method: public void <init>()
                    out.writeShort(0x0001); // ACC_PUBLIC
                    out.writeShort(5);      // name_index ("<init>")
                    out.writeShort(6);      // descriptor_index ("()V")
                    out.writeShort(1);      // attributes_count = 1 ("Code")

                    // Code attribute
                    out.writeShort(7);      // attribute_name_index ("Code")
                    out.writeInt(17);       // attribute_length: 2+2+4+5+2+2 = 17
                    out.writeShort(1);      // max_stack = 1
                    out.writeShort(1);      // max_locals = 1
                    out.writeInt(5);        // code_length = 5
                    out.writeByte(0x2A);    // aload_0
                    out.writeByte(0xB7);    // invokespecial #8
                    out.writeShort(8);
                    out.writeByte(0xB1);    // return
                    out.writeShort(0);      // exception_table_length = 0
                    out.writeShort(0);      // attributes_count = 0

                    out.writeShort(0);      // class attributes_count = 0
                }
                return baos.toByteArray();
            } catch (IOException e) {
                throw new RuntimeException("Failed to generate dynamic class bytecode", e);
            }
        }
    }
}