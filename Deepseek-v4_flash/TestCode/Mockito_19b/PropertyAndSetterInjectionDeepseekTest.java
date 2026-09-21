package org.mockito.internal.configuration.injection;

import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: PropertyAndSetterInjection.processInjection()
 * 
 * Decision Branches Covered:
 * 1. while (fieldClass != Object.class) - loop through class hierarchy
 * 2. injectMockCandidates() - two-pass injection logic
 * 3. injectMockCandidatesOnFields() - iterator loop with field removal
 * 4. mockCandidateFilter.filterCandidate() - type/name based filtering
 * 5. FieldInitializer.initialize() - exception handling paths
 * 6. notFinalOrStatic filter - final/static field exclusion
 * 7. SuperTypesLastSorter.sort() - field ordering
 * 
 * Boundary Conditions:
 * - Null mock candidates set
 * - Empty mock candidates set
 * - Multiple mocks with same type but different names
 * - Fields with final/static modifiers
 * - Fields in superclass hierarchy
 * - Fields with no matching mocks
 * - Fields with exact name match vs type match
 * 
 * Defect-Targeted Scenario (Defects4J):
 * - When multiple mocks of the same type are available, and a field name matches
 *   one of them, the injection should select the correct mock by name.
 *   The bug causes incorrect selection when multiple candidates exist.
 *   Test: shouldInsertFieldWithCorrectNameWhenMultipleTypesAvailable()
 */
public class PropertyAndSetterInjectionDeepseekTest {

    // ==================== Test Helper Classes ====================
    
    static class SimpleClass {
        private String value;
        public void setValue(String value) { this.value = value; }
        public String getValue() { return value; }
    }
    
    static class InjectMocksTarget {
        private String name;
        private Integer count;
        
        public void setName(String name) { this.name = name; }
        public String getName() { return name; }
        public void setCount(Integer count) { this.count = count; }
        public Integer getCount() { return count; }
    }
    
    static class SubClass extends InjectMocksTarget {
        private Double price;
        public void setPrice(Double price) { this.price = price; }
        public Double getPrice() { return price; }
    }
    
    static class FinalFieldClass {
        private final String finalField = "final";
        private String normalField;
        public String getFinalField() { return finalField; }
        public void setNormalField(String normalField) { this.normalField = normalField; }
        public String getNormalField() { return normalField; }
    }
    
    static class StaticFieldClass {
        private static String staticField = "static";
        private String normalField;
        public static String getStaticField() { return staticField; }
        public void setNormalField(String normalField) { this.normalField = normalField; }
        public String getNormalField() { return normalField; }
    }
    
    // ==================== Partition A: Core Functional Logic & State Transitions ====================
    
    @Test(timeout = 4000)
    public void testProcessInjectionWithSimpleField() throws Exception {
        PropertyAndSetterInjection injector = new PropertyAndSetterInjection();
        Field targetField = SimpleClass.class.getDeclaredField("value");
        SimpleClass owner = new SimpleClass();
        Set<Object> mocks = new HashSet<>();
        mocks.add("injectedValue");
        
        boolean result = injector.processInjection(targetField, owner, mocks);
        assertTrue("Injection should have occurred", result);
        assertEquals("Field should be injected", "injectedValue", owner.getValue());
    }
    
    @Test(timeout = 4000)
    public void testProcessInjectionWithMultipleFields() throws Exception {
        PropertyAndSetterInjection injector = new PropertyAndSetterInjection();
        Field targetField = InjectMocksTarget.class.getDeclaredField("name");
        InjectMocksTarget owner = new InjectMocksTarget();
        Set<Object> mocks = new HashSet<>();
        mocks.add("testName");
        mocks.add(42);
        
        boolean result = injector.processInjection(targetField, owner, mocks);
        assertTrue("Injection should have occurred", result);
        assertEquals("Name field should be injected", "testName", owner.getName());
        assertEquals("Count field should be injected", Integer.valueOf(42), owner.getCount());
    }
    
    @Test(timeout = 4000)
    public void testProcessInjectionWithSubClassHierarchy() throws Exception {
        PropertyAndSetterInjection injector = new PropertyAndSetterInjection();
        Field targetField = SubClass.class.getDeclaredField("price");
        SubClass owner = new SubClass();
        Set<Object> mocks = new HashSet<>();
        mocks.add(99.99);
        mocks.add("subName");
        mocks.add(100);
        
        boolean result = injector.processInjection(targetField, owner, mocks);
        assertTrue("Injection should have occurred", result);
        assertEquals("Price field should be injected", Double.valueOf(99.99), owner.getPrice());
        assertEquals("Name field should be injected", "subName", owner.getName());
        assertEquals("Count field should be injected", Integer.valueOf(100), owner.getCount());
    }
    
    // ==================== Partition B: Boundary Value Analysis & Extremes ====================
    
    @Test(timeout = 4000)
    public void testProcessInjectionWithNullMockCandidates() throws Exception {
        PropertyAndSetterInjection injector = new PropertyAndSetterInjection();
        Field targetField = SimpleClass.class.getDeclaredField("value");
        SimpleClass owner = new SimpleClass();
        
        boolean result = injector.processInjection(targetField, owner, null);
        assertFalse("Injection should not occur with null mocks", result);
        assertNull("Field should remain null", owner.getValue());
    }
    
    @Test(timeout = 4000)
    public void testProcessInjectionWithEmptyMockCandidates() throws Exception {
        PropertyAndSetterInjection injector = new PropertyAndSetterInjection();
        Field targetField = SimpleClass.class.getDeclaredField("value");
        SimpleClass owner = new SimpleClass();
        Set<Object> mocks = new HashSet<>();
        
        boolean result = injector.processInjection(targetField, owner, mocks);
        assertFalse("Injection should not occur with empty mocks", result);
        assertNull("Field should remain null", owner.getValue());
    }
    
    @Test(timeout = 4000)
    public void testProcessInjectionWithFinalField() throws Exception {
        PropertyAndSetterInjection injector = new PropertyAndSetterInjection();
        Field targetField = FinalFieldClass.class.getDeclaredField("normalField");
        FinalFieldClass owner = new FinalFieldClass();
        Set<Object> mocks = new HashSet<>();
        mocks.add("injected");
        
        boolean result = injector.processInjection(targetField, owner, mocks);
        assertTrue("Injection should occur for non-final field", result);
        assertEquals("Normal field should be injected", "injected", owner.getNormalField());
        assertEquals("Final field should remain unchanged", "final", owner.getFinalField());
    }
    
    @Test(timeout = 4000)
    public void testProcessInjectionWithStaticField() throws Exception {
        PropertyAndSetterInjection injector = new PropertyAndSetterInjection();
        Field targetField = StaticFieldClass.class.getDeclaredField("normalField");
        StaticFieldClass owner = new StaticFieldClass();
        Set<Object> mocks = new HashSet<>();
        mocks.add("injected");
        
        boolean result = injector.processInjection(targetField, owner, mocks);
        assertTrue("Injection should occur for non-static field", result);
        assertEquals("Normal field should be injected", "injected", owner.getNormalField());
        assertEquals("Static field should remain unchanged", "static", StaticFieldClass.getStaticField());
    }
    
    // ==================== Partition C: Defect-Targeted Branch Zone ====================
    
    /**
     * This test directly targets the known Defects4J defect:
     * When multiple mocks of the same type are available, and a field name matches
     * one of them, the injection should select the correct mock by name.
     * The bug causes incorrect selection when multiple candidates exist.
     */
    @Test(timeout = 4000)
    public void shouldInsertFieldWithCorrectNameWhenMultipleTypesAvailable() throws Exception {
        // This is the exact scenario from the Defects4J test that fails
        PropertyAndSetterInjection injector = new PropertyAndSetterInjection();
        
        // Create a class with fields that have specific names
        class TestClass {
            private String candidate1;
            private String candidate2;
            
            public void setCandidate1(String candidate1) { this.candidate1 = candidate1; }
            public String getCandidate1() { return candidate1; }
            public void setCandidate2(String candidate2) { this.candidate2 = candidate2; }
            public String getCandidate2() { return candidate2; }
        }
        
        Field targetField = TestClass.class.getDeclaredField("candidate1");
        TestClass owner = new TestClass();
        Set<Object> mocks = new HashSet<>();
        mocks.add("candidate1_value");
        mocks.add("candidate2_value");
        
        boolean result = injector.processInjection(targetField, owner, mocks);
        assertTrue("Injection should have occurred", result);
        
        // The correct behavior: candidate1 field should get "candidate1_value" and candidate2 should get "candidate2_value"
        // The bug causes candidate2 to be null or get the wrong value
        assertEquals("candidate1 should be injected with its matching mock", "candidate1_value", owner.getCandidate1());
        assertEquals("candidate2 should be injected with its matching mock", "candidate2_value", owner.getCandidate2());
    }
    
    @Test(timeout = 4000)
    public void shouldHandleMultipleSameTypeMocksWithNameMatching() throws Exception {
        PropertyAndSetterInjection injector = new PropertyAndSetterInjection();
        
        class MultiFieldClass {
            private String firstName;
            private String lastName;
            
            public void setFirstName(String firstName) { this.firstName = firstName; }
            public String getFirstName() { return firstName; }
            public void setLastName(String lastName) { this.lastName = lastName; }
            public String getLastName() { return lastName; }
        }
        
        Field targetField = MultiFieldClass.class.getDeclaredField("firstName");
        MultiFieldClass owner = new MultiFieldClass();
        Set<Object> mocks = new HashSet<>();
        mocks.add("John");
        mocks.add("Doe");
        
        boolean result = injector.processInjection(targetField, owner, mocks);
        assertTrue("Injection should have occurred", result);
        assertEquals("firstName should be 'John'", "John", owner.getFirstName());
        assertEquals("lastName should be 'Doe'", "Doe", owner.getLastName());
    }
    
    // ==================== Partition D: Exception & Defensive Guard Paths ====================
    
    @Test(timeout = 4000)
    public void testProcessInjectionWithNonExistentField() throws Exception {
        PropertyAndSetterInjection injector = new PropertyAndSetterInjection();
        
        // Create a field that doesn't exist on the target class
        class OtherClass {
            private String otherField;
            public void setOtherField(String otherField) { this.otherField = otherField; }
        }
        
        Field nonExistentField = OtherClass.class.getDeclaredField("otherField");
        SimpleClass owner = new SimpleClass();
        Set<Object> mocks = new HashSet<>();
        mocks.add("test");
        
        // This should not throw exception, just return false
        boolean result = injector.processInjection(nonExistentField, owner, mocks);
        assertFalse("Injection should not occur for non-matching field", result);
    }
    
    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testProcessInjectionWithNullField() throws Exception {
        PropertyAndSetterInjection injector = new PropertyAndSetterInjection();
        SimpleClass owner = new SimpleClass();
        Set<Object> mocks = new HashSet<>();
        mocks.add("test");
        
        injector.processInjection(null, owner, mocks);
    }
    
    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testProcessInjectionWithNullOwner() throws Exception {
        PropertyAndSetterInjection injector = new PropertyAndSetterInjection();
        Field targetField = SimpleClass.class.getDeclaredField("value");
        Set<Object> mocks = new HashSet<>();
        mocks.add("test");
        
        injector.processInjection(targetField, null, mocks);
    }
    
    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================
    
    @Test(timeout = 4000)
    public void testProcessInjectionMultipleCalls() throws Exception {
        PropertyAndSetterInjection injector = new PropertyAndSetterInjection();
        Field targetField = SimpleClass.class.getDeclaredField("value");
        SimpleClass owner = new SimpleClass();
        Set<Object> mocks = new HashSet<>();
        mocks.add("first");
        
        // First injection
        boolean result1 = injector.processInjection(targetField, owner, mocks);
        assertTrue("First injection should occur", result1);
        assertEquals("First injection value", "first", owner.getValue());
        
        // Second injection with different mocks
        Set<Object> mocks2 = new HashSet<>();
        mocks2.add("second");
        boolean result2 = injector.processInjection(targetField, owner, mocks2);
        assertTrue("Second injection should occur", result2);
        assertEquals("Second injection value", "second", owner.getValue());
    }
    
    @Test(timeout = 4000)
    public void testProcessInjectionWithNoMatchingMocks() throws Exception {
        PropertyAndSetterInjection injector = new PropertyAndSetterInjection();
        Field targetField = SimpleClass.class.getDeclaredField("value");
        SimpleClass owner = new SimpleClass();
        Set<Object> mocks = new HashSet<>();
        mocks.add(123); // Integer, not String
        
        boolean result = injector.processInjection(targetField, owner, mocks);
        assertFalse("Injection should not occur with non-matching type", result);
        assertNull("Field should remain null", owner.getValue());
    }
    
    @Test(timeout = 4000)
    public void testProcessInjectionWithPartialMatch() throws Exception {
        PropertyAndSetterInjection injector = new PropertyAndSetterInjection();
        
        class PartialClass {
            private String name;
            private Integer age;
            
            public void setName(String name) { this.name = name; }
            public String getName() { return name; }
            public void setAge(Integer age) { this.age = age; }
            public Integer getAge() { return age; }
        }
        
        Field targetField = PartialClass.class.getDeclaredField("name");
        PartialClass owner = new PartialClass();
        Set<Object> mocks = new HashSet<>();
        mocks.add("testName");
        // No Integer mock for age field
        
        boolean result = injector.processInjection(targetField, owner, mocks);
        assertTrue("Injection should occur for name field", result);
        assertEquals("Name should be injected", "testName", owner.getName());
        assertNull("Age should remain null", owner.getAge());
    }
}