package com.fasterxml.jackson.databind.jsontype.impl;

/* [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Target Class: com.fasterxml.jackson.databind.jsontype.impl.SubTypeValidator
 * Defects4J Defect: IllegalTypesCheckTest::testJDKTypes1872 -> java.lang.NullPointerException
 *
 * Decision / Condition Coverage Targets:
 * 1. Decision: `_cfgIllegalClassNames.contains(full)`
 *    - Branch TRUE: Known nasty class blocked immediately (e.g. FileHandler, UnicastRemoteObject).
 *    - Branch FALSE: Proceed to prefix inspection.
 * 2. Decision: `full.startsWith(PREFIX_STRING)`
 *    - Branch TRUE: Spring framework package detection ("org.springframework.").
 *    - Branch FALSE: Valid non-Spring type -> returns cleanly.
 * 3. Loop & Condition: `for (Class<?> cls = raw; cls != Object.class; cls = cls.getSuperclass())`
 *    - Normal class hierarchy traversal: inspects simple names up to Object.class.
 *    - Match "AbstractPointcutAdvisor" -> breaks main_check, throws JsonMappingException.
 *    - Match "AbstractApplicationContext" -> breaks main_check, throws JsonMappingException.
 *    - Defect Branch: If `raw` is an interface (`raw.isInterface() == true`), `raw.getSuperclass()`
 *      returns null! Defective loop check `cls != Object.class` evaluates `null != Object.class` (true),
 *      and calling `cls.getSimpleName()` throws java.lang.NullPointerException.
 * 4. Exception Generation: `throw JsonMappingException.from(ctxt, ...)`
 *    - Security message verification: "Illegal type (...) to deserialize: prevented for security reasons".
 * ====================================================================================================
 */

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SubTypeValidatorGeminiTest {

    // ================================================================================================
    // Dynamic In-Memory Bytecode Generator Helper
    // ================================================================================================

    /**
     * Minimal ClassLoader capable of generating valid synthetic classes and interfaces at runtime
     * under arbitrary package namespaces without third-party bytecode libraries.
     */
    static class DynamicClassLoader extends ClassLoader {
        private final java.util.Map<String, Class<?>> definedClasses = new java.util.HashMap<String, Class<?>>();

        public DynamicClassLoader(ClassLoader parent) {
            super(parent);
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            Class<?> c = definedClasses.get(name);
            if (c != null) {
                if (resolve) {
                    resolveClass(c);
                }
                return c;
            }
            return super.loadClass(name, resolve);
        }

        public Class<?> createClass(String className, String superClassName, boolean isInterface) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(baos);
                dos.writeInt(0xCAFEBABE); // Magic
                dos.writeShort(0);        // Minor version
                dos.writeShort(49);       // Major version (Java 5+)
                dos.writeShort(5);        // Constant pool count (1..4)
                
                // CP 1: CONSTANT_Class (this)
                dos.writeByte(7);
                dos.writeShort(2);
                // CP 2: CONSTANT_Utf8 (this class name)
                dos.writeByte(1);
                dos.writeUTF(className.replace('.', '/'));
                // CP 3: CONSTANT_Class (super)
                dos.writeByte(7);
                dos.writeShort(4);
                // CP 4: CONSTANT_Utf8 (super class name)
                dos.writeByte(1);
                dos.writeUTF(superClassName.replace('.', '/'));

                int flags = 0x0001 | 0x0400; // ACC_PUBLIC | ACC_ABSTRACT
                if (isInterface) {
                    flags |= 0x0200;         // ACC_INTERFACE
                } else {
                    flags |= 0x0020;         // ACC_SUPER
                }
                dos.writeShort(flags);
                dos.writeShort(1); // this_class
                dos.writeShort(3); // super_class
                dos.writeShort(0); // interfaces_count
                dos.writeShort(0); // fields_count
                dos.writeShort(0); // methods_count
                dos.writeShort(0); // attributes_count

                byte[] bytes = baos.toByteArray();
                Class<?> defined = defineClass(className, bytes, 0, bytes.length);
                definedClasses.put(className, defined);
                return defined;
            } catch (Exception e) {
                throw new RuntimeException("Failed to dynamically define class: " + className, e);
            }
        }
    }

    // ================================================================================================
    // Partition A: Core Functional Logic & State Transitions
    // ================================================================================================

    @Test(timeout = 4000)
    public void testInstanceSingleton() {
        SubTypeValidator instance1 = SubTypeValidator.instance();
        SubTypeValidator instance2 = SubTypeValidator.instance();
        assertNotNull("SubTypeValidator instance must not be null", instance1);
        assertSame("SubTypeValidator.instance() must return singleton instance", instance1, instance2);
    }

    @Test(timeout = 4000)
    public void testProtectedConstructorInstantiation() {
        SubTypeValidator validator = new SubTypeValidator();
        assertNotNull(validator);
        assertNotNull(validator._cfgIllegalClassNames);
        assertEquals(SubTypeValidator.DEFAULT_NO_DESER_CLASS_NAMES, validator._cfgIllegalClassNames);
    }

    @Test(timeout = 4000)
    public void testValidNonSpringTypesAllowed() throws Exception {
        SubTypeValidator validator = SubTypeValidator.instance();
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        Class<?>[] safeClasses = new Class<?>[] {
            String.class,
            Object.class,
            Integer.class,
            java.util.HashMap.class,
            java.util.ArrayList.class
        };

        for (Class<?> safeCls : safeClasses) {
            JavaType type = mapper.constructType(safeCls);
            // Should pass without throwing any exception
            validator.validateSubType(ctxt, type);
        }
    }

    @Test(timeout = 4000)
    public void testValidSpringClassAllowed() throws Exception {
        SubTypeValidator validator = SubTypeValidator.instance();
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        DynamicClassLoader loader = new DynamicClassLoader(getClass().getClassLoader());
        Class<?> harmlessSpringClass = loader.createClass(
                "org.springframework.beans.HarmlessPojoBean",
                "java.lang.Object",
                false);

        JavaType type = mapper.constructType(harmlessSpringClass);
        // Valid Spring bean hierarchy not extending advisor or context must be accepted
        validator.validateSubType(ctxt, type);
    }

    // ================================================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // ================================================================================================

    @Test(timeout = 4000)
    public void testDefaultNoDeserClassNamesImmutability() {
        Set<String> set = SubTypeValidator.DEFAULT_NO_DESER_CLASS_NAMES;
        assertNotNull(set);
        try {
            set.add("com.example.ExploitClass");
            fail("DEFAULT_NO_DESER_CLASS_NAMES must be an unmodifiable set");
        } catch (UnsupportedOperationException expected) {
            // Expected contract
        }
    }

    @Test(timeout = 4000)
    public void testDefaultNoDeserClassNamesContentsCompleteness() {
        Set<String> set = SubTypeValidator.DEFAULT_NO_DESER_CLASS_NAMES;
        assertEquals(17, set.size());

        String[] expectedTypes = new String[] {
            "org.apache.commons.collections.functors.InvokerTransformer",
            "org.apache.commons.collections.functors.InstantiateTransformer",
            "org.apache.commons.collections4.functors.InvokerTransformer",
            "org.apache.commons.collections4.functors.InstantiateTransformer",
            "org.codehaus.groovy.runtime.ConvertedClosure",
            "org.codehaus.groovy.runtime.MethodClosure",
            "org.springframework.beans.factory.ObjectFactory",
            "com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl",
            "org.apache.xalan.xsltc.trax.TemplatesImpl",
            "com.sun.rowset.JdbcRowSetImpl",
            "java.util.logging.FileHandler",
            "java.rmi.server.UnicastRemoteObject",
            "org.springframework.beans.factory.config.PropertyPathFactoryBean",
            "com.mchange.v2.c3p0.JndiRefForwardingDataSource",
            "com.mchange.v2.c3p0.WrapperConnectionPoolDataSource",
            "org.apache.tomcat.dbcp.dbcp2.BasicDataSource",
            "com.sun.org.apache.bcel.internal.util.ClassLoader"
        };

        for (String expected : expectedTypes) {
            assertTrue("Expected blocked set to contain: " + expected, set.contains(expected));
        }
    }

    @Test(timeout = 4000)
    public void testCustomIllegalClassNamesConfiguration() throws Exception {
        SubTypeValidator validator = new SubTypeValidator();
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        Set<String> customIllegal = new HashSet<String>();
        customIllegal.add(String.class.getName());
        validator._cfgIllegalClassNames = customIllegal;

        JavaType stringType = mapper.constructType(String.class);
        try {
            validator.validateSubType(ctxt, stringType);
            fail("Expected String to be blocked by custom illegal set");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Illegal type (java.lang.String) to deserialize"));
        }

        // Default JDK dangerous types should now pass on this custom instance
        JavaType fileHandlerType = mapper.constructType(java.util.logging.FileHandler.class);
        validator.validateSubType(ctxt, fileHandlerType);
    }

    // ================================================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // ================================================================================================

    /**
     * Targets Defects4J known defect:
     * com.fasterxml.jackson.databind.interop.IllegalTypesCheckTest::testJDKTypes1872
     *
     * Deserialization of dangerous JDK classes must be prevented for security reasons
     * with an explicit JsonMappingException, rather than causing NullPointerException or instantiation.
     */
    @Test(timeout = 4000)
    public void testJDKTypes1872() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping();

        Class<?>[] dangerousJdkClasses = new Class<?>[] {
            java.util.logging.FileHandler.class,
            java.rmi.server.UnicastRemoteObject.class
        };

        for (Class<?> cls : dangerousJdkClasses) {
            String json = mapper.writeValueAsString(new Object[] { cls.getName(), Collections.emptyMap() });
            try {
                mapper.readValue(json, Object.class);
                fail("Should not allow deserialization of: " + cls.getName());
            } catch (JsonMappingException e) {
                assertTrue("Exception message must contain 'prevented for security reasons', got: " + e.getMessage(),
                        e.getMessage().contains("prevented for security reasons"));
            }
        }
    }

    @Test(timeout = 4000)
    public void testJDKTypes1872DirectValidation() throws Exception {
        SubTypeValidator validator = SubTypeValidator.instance();
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        Class<?>[] dangerousJdkClasses = new Class<?>[] {
            java.util.logging.FileHandler.class,
            java.rmi.server.UnicastRemoteObject.class
        };

        for (Class<?> cls : dangerousJdkClasses) {
            JavaType type = mapper.constructType(cls);
            try {
                validator.validateSubType(ctxt, type);
                fail("Direct validation should reject: " + cls.getName());
            } catch (JsonMappingException e) {
                String expectedMsg = "Illegal type (" + cls.getName() + ") to deserialize: prevented for security reasons";
                assertTrue("Expected [" + expectedMsg + "] but got [" + e.getMessage() + "]",
                        e.getMessage().contains(expectedMsg));
            }
        }
    }

    /**
     * Directly tests the defect in the Spring hierarchy check loop:
     * When raw type is an interface under "org.springframework.", cls.getSuperclass() returns null.
     * The defective loop condition `cls != Object.class` evaluates `null != Object.class` (true),
     * resulting in a NullPointerException when calling cls.getSimpleName().
     */
    @Test(timeout = 4000)
    public void testSpringInterfaceLoopDoesNotThrowNullPointerException() throws Exception {
        SubTypeValidator validator = SubTypeValidator.instance();
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        DynamicClassLoader loader = new DynamicClassLoader(getClass().getClassLoader());
        Class<?> springInterface = loader.createClass(
                "org.springframework.context.CustomTestInterface",
                "java.lang.Object",
                true);

        JavaType type = mapper.constructType(springInterface);

        // Under defective logic, this call fails with java.lang.NullPointerException
        validator.validateSubType(ctxt, type);
    }

    // ================================================================================================
    // Partition D: Exception & Defensive Guard Paths
    // ================================================================================================

    @Test(timeout = 4000)
    public void testSpringAbstractPointcutAdvisorDirectBlocked() throws Exception {
        SubTypeValidator validator = SubTypeValidator.instance();
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        DynamicClassLoader loader = new DynamicClassLoader(getClass().getClassLoader());
        Class<?> advisorClass = loader.createClass(
                "org.springframework.aop.AbstractPointcutAdvisor",
                "java.lang.Object",
                false);

        JavaType type = mapper.constructType(advisorClass);
        try {
            validator.validateSubType(ctxt, type);
            fail("Expected AbstractPointcutAdvisor to be blocked");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("prevented for security reasons"));
            assertTrue(e.getMessage().contains("AbstractPointcutAdvisor"));
        }
    }

    @Test(timeout = 4000)
    public void testSpringAbstractPointcutAdvisorSubclassBlocked() throws Exception {
        SubTypeValidator validator = SubTypeValidator.instance();
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        DynamicClassLoader loader = new DynamicClassLoader(getClass().getClassLoader());
        Class<?> advisorBase = loader.createClass(
                "org.springframework.aop.support.AbstractPointcutAdvisor",
                "java.lang.Object",
                false);
        Class<?> advisorSub = loader.createClass(
                "org.springframework.aop.support.DefaultBeanFactoryPointcutAdvisor",
                advisorBase.getName(),
                false);

        JavaType type = mapper.constructType(advisorSub);
        try {
            validator.validateSubType(ctxt, type);
            fail("Expected subclass of AbstractPointcutAdvisor to be blocked");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("prevented for security reasons"));
            assertTrue(e.getMessage().contains("DefaultBeanFactoryPointcutAdvisor"));
        }
    }

    @Test(timeout = 4000)
    public void testSpringAbstractApplicationContextDirectBlocked() throws Exception {
        SubTypeValidator validator = SubTypeValidator.instance();
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        DynamicClassLoader loader = new DynamicClassLoader(getClass().getClassLoader());
        Class<?> appContextClass = loader.createClass(
                "org.springframework.context.support.AbstractApplicationContext",
                "java.lang.Object",
                false);

        JavaType type = mapper.constructType(appContextClass);
        try {
            validator.validateSubType(ctxt, type);
            fail("Expected AbstractApplicationContext to be blocked");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("prevented for security reasons"));
            assertTrue(e.getMessage().contains("AbstractApplicationContext"));
        }
    }

    @Test(timeout = 4000)
    public void testSpringAbstractApplicationContextMultiLevelSubclassBlocked() throws Exception {
        SubTypeValidator validator = SubTypeValidator.instance();
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        DynamicClassLoader loader = new DynamicClassLoader(getClass().getClassLoader());
        Class<?> level1 = loader.createClass(
                "org.springframework.context.support.AbstractApplicationContext",
                "java.lang.Object",
                false);
        Class<?> level2 = loader.createClass(
                "org.springframework.context.support.AbstractRefreshableApplicationContext",
                level1.getName(),
                false);
        Class<?> level3 = loader.createClass(
                "org.springframework.context.support.FileSystemXmlApplicationContext",
                level2.getName(),
                false);

        JavaType type = mapper.constructType(level3);
        try {
            validator.validateSubType(ctxt, type);
            fail("Expected deeply nested subclass of AbstractApplicationContext to be blocked");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("prevented for security reasons"));
            assertTrue(e.getMessage().contains("FileSystemXmlApplicationContext"));
        }
    }

    @Test(timeout = 4000)
    public void testNonSpringClassWithSameSimpleNameAllowed() throws Exception {
        SubTypeValidator validator = SubTypeValidator.instance();
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        DynamicClassLoader loader = new DynamicClassLoader(getClass().getClassLoader());
        Class<?> nonSpringContext = loader.createClass(
                "com.mycompany.app.AbstractApplicationContext",
                "java.lang.Object",
                false);

        JavaType type = mapper.constructType(nonSpringContext);
        // Non-spring class sharing simple name should NOT be blocked by Spring check rule
        validator.validateSubType(ctxt, type);
    }

    @Test(timeout = 4000)
    public void testStandardJdkRowSetBlocked() throws Exception {
        SubTypeValidator validator = SubTypeValidator.instance();
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        try {
            Class<?> rowSetCls = Class.forName("com.sun.rowset.JdbcRowSetImpl");
            JavaType type = mapper.constructType(rowSetCls);
            try {
                validator.validateSubType(ctxt, type);
                fail("Expected JdbcRowSetImpl to be blocked");
            } catch (JsonMappingException e) {
                assertTrue(e.getMessage().contains("prevented for security reasons"));
            }
        } catch (ClassNotFoundException ignored) {
            // Optional JDK internal class might not be exported in certain modular runtimes
        }
    }

    // ================================================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // ================================================================================================

    @Test(timeout = 4000)
    public void testPrefixConstantIntegrity() {
        assertEquals("org.springframework.", SubTypeValidator.PREFIX_STRING);
    }
}