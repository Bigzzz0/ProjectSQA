package org.mockito.internal.matchers;

import org.hamcrest.Description;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import static org.junit.Assert.*;

public class SameDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Target class: Same (org.mockito.internal.matchers.Same)
     * 
     * Branches identified:
     * 1. matches(Object actual): 
     *    - Branch 1: wanted == actual (true/false)
     *    - Covers identity comparison for same reference, different objects, null vs non-null
     * 2. describeTo(Description):
     *    - Branch 2: wanted instanceof String (true/false)
     *    - Branch 3: wanted instanceof Character (true/false)
     *    - Branch 4: appendText calls with different argument types (String, Character, other)
     * 3. appendQuoting(Description):
     *    - Branch 5: wanted instanceof String (true/false)
     *    - Branch 6: wanted instanceof Character (true/false)
     *    - Branch 7: neither String nor Character (no quoting)
     * 
     * Defect targeting:
     * - Known defect: NPE when null is passed to Same constructor and matches() is called
     *   with null actual. The defect is in describeTo() when wanted is null, 
     *   because wanted.toString() throws NullPointerException.
     * - The test should verify that when null is passed to Same, the matcher 
     *   correctly handles null in matches() and describeTo() without throwing NPE.
     * 
     * Boundary values:
     * - null wanted object
     * - null actual object
     * - String wanted (quoted with ")
     * - Character wanted (quoted with ')
     * - Integer wanted (no quoting)
     * - Same reference (identity match)
     * - Different reference (identity mismatch)
     * 
     * Exception paths:
     * - describeTo with null wanted should not throw NPE (defect)
     * - matches with null actual should return true when wanted is null
     */

    // ==================== Partition A: Core Functional Logic & State Transitions ====================
    
    @Test(timeout = 4000)
    public void testMatchesWithSameReference() {
        Object obj = new Object();
        Same matcher = new Same(obj);
        
        assertTrue("Should match same reference", matcher.matches(obj));
        assertTrue("Should be an ArgumentMatcher", matcher instanceof ArgumentMatcher);
    }
    
    @Test(timeout = 4000)
    public void testMatchesWithDifferentReference() {
        Object obj1 = new Object();
        Object obj2 = new Object();
        Same matcher = new Same(obj1);
        
        assertFalse("Should not match different reference", matcher.matches(obj2));
    }
    
    @Test(timeout = 4000)
    public void testMatchesWithNullWantedAndNullActual() {
        Same matcher = new Same(null);
        
        assertTrue("Null should match null", matcher.matches(null));
    }
    
    @Test(timeout = 4000)
    public void testMatchesWithNullWantedAndNonNullActual() {
        Same matcher = new Same(null);
        
        assertFalse("Null should not match non-null", matcher.matches(new Object()));
    }
    
    @Test(timeout = 4000)
    public void testMatchesWithNonNullWantedAndNullActual() {
        Same matcher = new Same(new Object());
        
        assertFalse("Non-null should not match null", matcher.matches(null));
    }
    
    @Test(timeout = 4000)
    public void testMatchesWithStringWanted() {
        String wanted = "test";
        Same matcher = new Same(wanted);
        
        assertTrue("Should match same string reference", matcher.matches(wanted));
        assertFalse("Should not match equal but different string", matcher.matches(new String("test")));
    }
    
    @Test(timeout = 4000)
    public void testMatchesWithCharacterWanted() {
        Character wanted = 'a';
        Same matcher = new Same(wanted);
        
        assertTrue("Should match same character reference", matcher.matches(wanted));
        assertFalse("Should not match different character", matcher.matches('b'));
    }
    
    @Test(timeout = 4000)
    public void testMatchesWithIntegerWanted() {
        Integer wanted = 42;
        Same matcher = new Same(wanted);
        
        assertTrue("Should match same integer reference", matcher.matches(wanted));
        assertFalse("Should not match different integer", matcher.matches(43));
    }
    
    // ==================== Partition B: Boundary Value Analysis (BVA) & Extremes ====================
    
    @Test(timeout = 4000)
    public void testMatchesWithEmptyString() {
        String wanted = "";
        Same matcher = new Same(wanted);
        
        assertTrue("Should match empty string reference", matcher.matches(wanted));
        assertFalse("Should not match different empty string", matcher.matches(new String("")));
    }
    
    @Test(timeout = 4000)
    public void testMatchesWithNullCharacter() {
        Character wanted = null;
        Same matcher = new Same(wanted);
        
        assertTrue("Null character should match null", matcher.matches(null));
        assertFalse("Null character should not match non-null", matcher.matches('a'));
    }
    
    @Test(timeout = 4000)
    public void testMatchesWithMaxInteger() {
        Integer wanted = Integer.MAX_VALUE;
        Same matcher = new Same(wanted);
        
        assertTrue("Should match max integer reference", matcher.matches(wanted));
        assertFalse("Should not match different max integer", matcher.matches(Integer.MAX_VALUE - 1));
    }
    
    @Test(timeout = 4000)
    public void testMatchesWithMinInteger() {
        Integer wanted = Integer.MIN_VALUE;
        Same matcher = new Same(wanted);
        
        assertTrue("Should match min integer reference", matcher.matches(wanted));
        assertFalse("Should not match different min integer", matcher.matches(Integer.MIN_VALUE + 1));
    }
    
    @Test(timeout = 4000)
    public void testMatchesWithZeroInteger() {
        Integer wanted = 0;
        Same matcher = new Same(wanted);
        
        assertTrue("Should match zero integer reference", matcher.matches(wanted));
        assertFalse("Should not match different zero integer", matcher.matches(0));
    }
    
    // ==================== Partition C: Defect-Targeted Branch Zone ====================
    
    /**
     * Defect: NPE when null is passed to Same and describeTo is called.
     * The original implementation calls wanted.toString() which throws NPE when wanted is null.
     * This test verifies that describeTo handles null gracefully.
     */
    @Test(timeout = 4000)
    public void testDescribeToWithNullWanted() {
        Same matcher = new Same(null);
        DescriptionStub description = new DescriptionStub();
        
        try {
            matcher.describeTo(description);
            // If we reach here, no exception was thrown - this is the expected behavior
            // The defect would cause an NPE here
            String result = description.getText();
            assertNotNull("Description should not be null", result);
            assertTrue("Description should contain 'same('", result.contains("same("));
            assertTrue("Description should contain ')'", result.endsWith(")"));
        } catch (NullPointerException e) {
            fail("Should not throw NPE when describing null wanted: " + e.getMessage());
        }
    }
    
    /**
     * Direct test for the known defect scenario:
     * shouldNotThrowNPEWhenNullPassedToSame
     * This test verifies that matches(null) works correctly with null wanted
     * and that describeTo doesn't throw NPE.
     */
    @Test(timeout = 4000)
    public void testShouldNotThrowNPEWhenNullPassedToSame() {
        Same matcher = new Same(null);
        
        // Test matches with null
        assertTrue("matches(null) should return true when wanted is null", matcher.matches(null));
        
        // Test describeTo with null wanted - this is where the defect occurs
        DescriptionStub description = new DescriptionStub();
        try {
            matcher.describeTo(description);
            // If we reach here, no NPE was thrown
            String result = description.getText();
            assertNotNull("Description should not be null", result);
            // The description should contain "same(" and ")" even with null wanted
            assertTrue("Description should contain 'same('", result.contains("same("));
            assertTrue("Description should end with ')'", result.endsWith(")"));
        } catch (NullPointerException e) {
            fail("NPE thrown when describing null wanted: " + e.getMessage());
        }
    }
    
    @Test(timeout = 4000)
    public void testDescribeToWithStringWanted() {
        String wanted = "hello";
        Same matcher = new Same(wanted);
        DescriptionStub description = new DescriptionStub();
        
        matcher.describeTo(description);
        String result = description.getText();
        
        assertEquals("same(\"hello\")", result);
        assertTrue("Should contain quotes for string", result.contains("\""));
    }
    
    @Test(timeout = 4000)
    public void testDescribeToWithCharacterWanted() {
        Character wanted = 'x';
        Same matcher = new Same(wanted);
        DescriptionStub description = new DescriptionStub();
        
        matcher.describeTo(description);
        String result = description.getText();
        
        assertEquals("same('x')", result);
        assertTrue("Should contain single quotes for character", result.contains("'"));
    }
    
    @Test(timeout = 4000)
    public void testDescribeToWithIntegerWanted() {
        Integer wanted = 123;
        Same matcher = new Same(wanted);
        DescriptionStub description = new DescriptionStub();
        
        matcher.describeTo(description);
        String result = description.getText();
        
        assertEquals("same(123)", result);
        assertFalse("Should not contain quotes for integer", result.contains("\""));
        assertFalse("Should not contain single quotes for integer", result.contains("'"));
    }
    
    @Test(timeout = 4000)
    public void testDescribeToWithEmptyStringWanted() {
        String wanted = "";
        Same matcher = new Same(wanted);
        DescriptionStub description = new DescriptionStub();
        
        matcher.describeTo(description);
        String result = description.getText();
        
        assertEquals("same(\"\")", result);
    }
    
    @Test(timeout = 4000)
    public void testDescribeToWithNullCharacterWanted() {
        Character wanted = null;
        Same matcher = new Same(wanted);
        DescriptionStub description = new DescriptionStub();
        
        try {
            matcher.describeTo(description);
            // If we reach here, no exception was thrown
            String result = description.getText();
            assertNotNull("Description should not be null", result);
            assertTrue("Description should contain 'same('", result.contains("same("));
        } catch (NullPointerException e) {
            fail("Should not throw NPE when describing null character: " + e.getMessage());
        }
    }
    
    // ==================== Partition D: Exception & Defensive Guard Paths ====================
    
    @Test(timeout = 4000)
    public void testDescribeToWithNullWantedAndNullDescription() {
        Same matcher = new Same(null);
        
        try {
            matcher.describeTo(null);
            fail("Should throw NullPointerException when description is null");
        } catch (NullPointerException e) {
            // Expected - description cannot be null
        }
    }
    
    @Test(timeout = 4000)
    public void testMatchesWithNullWantedAndNullActual() {
        Same matcher = new Same(null);
        
        assertTrue("Null should match null", matcher.matches(null));
    }
    
    @Test(timeout = 4000)
    public void testMatchesWithNullWantedAndNonNullActual() {
        Same matcher = new Same(null);
        
        assertFalse("Null should not match non-null", matcher.matches(new Object()));
    }
    
    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================
    
    @Test(timeout = 4000)
    public void testSerialization() throws IOException, ClassNotFoundException {
        String wanted = "serializable";
        Same matcher = new Same(wanted);
        
        // Serialize
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(matcher);
        oos.close();
        
        // Deserialize
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Same deserialized = (Same) ois.readObject();
        ois.close();
        
        assertNotNull("Deserialized matcher should not be null", deserialized);
        assertTrue("Deserialized matcher should match same reference", deserialized.matches(wanted));
        assertFalse("Deserialized matcher should not match different reference", deserialized.matches(new String("serializable")));
    }
    
    @Test(timeout = 4000)
    public void testSerializationWithNullWanted() throws IOException, ClassNotFoundException {
        Same matcher = new Same(null);
        
        // Serialize
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(matcher);
        oos.close();
        
        // Deserialize
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Same deserialized = (Same) ois.readObject();
        ois.close();
        
        assertNotNull("Deserialized matcher should not be null", deserialized);
        assertTrue("Deserialized matcher should match null", deserialized.matches(null));
        assertFalse("Deserialized matcher should not match non-null", deserialized.matches(new Object()));
    }
    
    @Test(timeout = 4000)
    public void testSerializableInterface() {
        Same matcher = new Same("test");
        assertTrue("Should implement Serializable", matcher instanceof Serializable);
    }
    
    @Test(timeout = 4000)
    public void testSerialVersionUID() throws Exception {
        java.lang.reflect.Field field = Same.class.getDeclaredField("serialVersionUID");
        field.setAccessible(true);
        long serialVersionUID = field.getLong(null);
        assertEquals("SerialVersionUID should be -1226959355938572597L", -1226959355938572597L, serialVersionUID);
    }
    
    @Test(timeout = 4000)
    public void testMultipleInstancesWithSameWanted() {
        Object wanted = new Object();
        Same matcher1 = new Same(wanted);
        Same matcher2 = new Same(wanted);
        
        assertTrue("Both matchers should match the same reference", matcher1.matches(wanted));
        assertTrue("Both matchers should match the same reference", matcher2.matches(wanted));
        assertTrue("Both matchers should match each other's wanted", matcher1.matches(wanted) && matcher2.matches(wanted));
    }
    
    @Test(timeout = 4000)
    public void testMatcherWithDifferentWantedObjects() {
        Object wanted1 = new Object();
        Object wanted2 = new Object();
        Same matcher1 = new Same(wanted1);
        Same matcher2 = new Same(wanted2);
        
        assertTrue("Matcher1 should match wanted1", matcher1.matches(wanted1));
        assertFalse("Matcher1 should not match wanted2", matcher1.matches(wanted2));
        assertTrue("Matcher2 should match wanted2", matcher2.matches(wanted2));
        assertFalse("Matcher2 should not match wanted1", matcher2.matches(wanted1));
    }
    
    // ==================== Helper class for Description ====================
    
    private static class DescriptionStub implements Description {
        private StringBuilder sb = new StringBuilder();
        
        @Override
        public void appendText(String text) {
            sb.append(text);
        }
        
        @Override
        public void appendValue(Object value) {
            sb.append(String.valueOf(value));
        }
        
        @Override
        public void appendValueList(String start, String separator, String end, Object... values) {
            sb.append(start);
            for (int i = 0; i < values.length; i++) {
                if (i > 0) {
                    sb.append(separator);
                }
                sb.append(String.valueOf(values[i]));
            }
            sb.append(end);
        }
        
        @Override
        public void appendList(String start, String separator, String end, Iterable<?> values) {
            sb.append(start);
            boolean first = true;
            for (Object value : values) {
                if (!first) {
                    sb.append(separator);
                }
                sb.append(String.valueOf(value));
                first = false;
            }
            sb.append(end);
        }
        
        @Override
        public Description appendDescriptionOf(org.hamcrest.SelfDescribing value) {
            value.describeTo(this);
            return this;
        }
        
        public String getText() {
            return sb.toString();
        }
    }
}