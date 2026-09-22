package com.fasterxml.jackson.databind.jsontype.impl;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Branches in validateSubType():
 * 1. _cfgIllegalClassNames.contains(full) -> break -> throw JsonMappingException
 * 2. full.startsWith(PREFIX_STRING) == false -> return (normal)
 * 3. full.startsWith(PREFIX_STRING) == true -> enter for loop over superclasses
 *    3a. cls is null (interface) -> NullPointerException (DEFECT)
 *    3b. cls.getSimpleName() equals "AbstractPointcutAdvisor" -> break main_check -> throw
 *    3c. cls.getSimpleName() equals "AbstractApplicationContext" -> break main_check -> throw
 *    3d. none of the above, continue loop until Object -> return (normal)
 * 
 * Boundary conditions:
 * - null raw class? Not possible from JavaType.
 * - Interface raw class with Spring prefix triggers defect.
 * - Class raw class with Spring prefix and dangerous superclass triggers exception.
 * - Class raw class with Spring prefix and safe superclass returns normally.
 * - Class raw class without Spring prefix returns normally.
 * - Class raw class in illegal set throws exception.
 * 
 * Defect: When raw is an interface and full starts with "org.springframework.",
 * the for loop iterates over superclasses, but getSuperclass() returns null,
 * causing NullPointerException on cls.getSimpleName().
 * Expected: Should skip loop for interfaces (or check cls != null).
 */
public class SubTypeValidatorDeepseekTest {

    // ===== Partition A: Core Functional Logic & State Transitions =====

    @Test(timeout = 4000)
    public void testSingletonInstance() {
        assertNotNull(SubTypeValidator.instance());
        assertSame(SubTypeValidator.instance(), SubTypeValidator.instance());
    }

    @Test(timeout = 4000)
    public void testDefaultIllegalClassNames() {
        // Verify that the default set contains known dangerous classes
        // We can't access private field, but we can test behavior
        // Use a class from the set to trigger exception
        JavaType type = TypeFactory.defaultInstance().constructType(
            org.apache.commons.collections.functors.InvokerTransformer.class);
        try {
            SubTypeValidator.instance().validateSubType(null, type);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Illegal type"));
        }
    }

    // ===== Partition B: Boundary Value Analysis & Extremes =====

    @Test(timeout = 4000)
    public void testNullDeserializationContext() throws JsonMappingException {
        // Passing null ctxt is safe when validation passes (no exception thrown)
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        SubTypeValidator.instance().validateSubType(null, type);
        // No exception expected
    }

    @Test(timeout = 4000)
    public void testNonSpringClass() throws JsonMappingException {
        // Class name does not start with "org.springframework."
        JavaType type = TypeFactory.defaultInstance().constructType(java.util.ArrayList.class);
        SubTypeValidator.instance().validateSubType(null, type);
        // Should return normally
    }

    @Test(timeout = 4000)
    public void testInterfaceNonSpring() throws JsonMappingException {
        // Interface, but name does not start with Spring prefix
        JavaType type = TypeFactory.defaultInstance().constructType(java.lang.Comparable.class);
        SubTypeValidator.instance().validateSubType(null, type);
        // Should return normally
    }

    // ===== Partition C: Defect-Targeted Branch Zone =====

    @Test(timeout = 4000)
    public void testSpringInterfaceCausesNPE() throws JsonMappingException {
        // This test targets the known defect: interface with Spring prefix causes NullPointerException
        // Expected correct behavior: no exception (interface should be ignored)
        // Buggy version throws NullPointerException, causing test failure.
        JavaType type = TypeFactory.defaultInstance().constructType(
            org.springframework.beans.factory.BeanFactory.class);
        SubTypeValidator.instance().validateSubType(null, type);
        // If we reach here, no exception -> test passes (bug absent)
    }

    @Test(timeout = 4000)
    public void testSpringClassWithDangerousSuperclass() throws JsonMappingException {
        // Class that extends AbstractPointcutAdvisor (dangerous)
        JavaType type = TypeFactory.defaultInstance().constructType(
            org.springframework.aop.support.DefaultPointcutAdvisor.class);
        try {
            SubTypeValidator.instance().validateSubType(null, type);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Illegal type"));
        }
    }

    @Test(timeout = 4000)
    public void testSpringClassWithApplicationContextSuperclass() throws JsonMappingException {
        // Class that extends AbstractApplicationContext (dangerous)
        JavaType type = TypeFactory.defaultInstance().constructType(
            org.springframework.context.support.ClassPathXmlApplicationContext.class);
        try {
            SubTypeValidator.instance().validateSubType(null, type);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Illegal type"));
        }
    }

    @Test(timeout = 4000)
    public void testSpringClassSafeSuperclass() throws JsonMappingException {
        // Spring class that does not have dangerous superclass
        JavaType type = TypeFactory.defaultInstance().constructType(
            org.springframework.util.StringUtils.class);
        SubTypeValidator.instance().validateSubType(null, type);
        // Should return normally
    }

    // ===== Partition D: Exception & Defensive Guard Paths =====

    @Test(timeout = 4000, expected = JsonMappingException.class)
    public void testIllegalClassFromSet() throws JsonMappingException {
        // Class directly in the illegal set
        JavaType type = TypeFactory.defaultInstance().constructType(
            com.sun.rowset.JdbcRowSetImpl.class);
        SubTypeValidator.instance().validateSubType(null, type);
    }

    @Test(timeout = 4000, expected = JsonMappingException.class)
    public void testAnotherIllegalClass() throws JsonMappingException {
        JavaType type = TypeFactory.defaultInstance().constructType(
            java.rmi.server.UnicastRemoteObject.class);
        SubTypeValidator.instance().validateSubType(null, type);
    }

    // ===== Partition E: Object Lifecycle & Contract Integrity =====

    @Test(timeout = 4000)
    public void testInstanceConsistency() {
        SubTypeValidator v1 = SubTypeValidator.instance();
        SubTypeValidator v2 = SubTypeValidator.instance();
        assertSame(v1, v2);
        // No equals/hashCode defined, but we can test that instance is singleton
    }

    // Additional edge: class that is an array? Arrays have superclass Object, so safe.
    @Test(timeout = 4000)
    public void testArrayClass() throws JsonMappingException {
        JavaType type = TypeFactory.defaultInstance().constructType(String[].class);
        SubTypeValidator.instance().validateSubType(null, type);
        // Should return normally
    }
}