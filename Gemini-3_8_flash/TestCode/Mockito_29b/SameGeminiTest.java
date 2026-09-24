/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: org.mockito.internal.matchers.Same
 *
 * Methods Under Test:
 * 1. public Same(Object wanted)
 * 2. public boolean matches(Object actual)
 *    - Branch: wanted == actual -> returns true
 *    - Branch: wanted != actual -> returns false
 *    - Boundaries: both null, one null, same instance, equal but distinct instances
 * 3. public void describeTo(Description description)
 *    - Branch wanted instanceof String: appends quotes "\""
 *    - Branch wanted instanceof Character: appends quotes "'"
 *    - Branch wanted is any other Object: no quotes
 *    - Defect Branch wanted == null: triggers wanted.toString() throwing NPE on buggy code.
 *      Expected correct behavior: appends "null" without throwing NullPointerException -> "same(null)".
 * 4. Serialization integrity: Same implements Serializable.
 */
package org.mockito.internal.matchers;

import org.hamcrest.Description;
import org.hamcrest.StringDescription;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.Assert.*;

public class SameGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testMatchesReturnsTrueForIdenticalObjectReference() {
        Object target = new Object();
        Same matcher = new Same(target);

        assertTrue("Matcher should match identical reference", matcher.matches(target));
    }

    @Test(timeout = 4000)
    public void testMatchesReturnsFalseForEqualButDistinctInstances() {
        String wanted = new String("test_string");
        String actual = new String("test_string");

        // Verify precondition that these are distinct object references
        assertNotSame("Precondition failed: instances must be distinct", wanted, actual);

        Same matcher = new Same(wanted);
        assertFalse("Matcher must enforce reference equality (==), not equals()", matcher.matches(actual));
    }

    @Test(timeout = 4000)
    public void testMatchesReturnsFalseForDifferentObjects() {
        Object wanted = new Object();
        Object actual = new Object();

        Same matcher = new Same(wanted);
        assertFalse("Matcher should return false for different objects", matcher.matches(actual));
    }

    @Test(timeout = 4000)
    public void testDescribeToStringValue() {
        Same matcher = new Same("hello");
        Description description = new StringDescription();

        matcher.describeTo(description);

        assertEquals("String values should be quoted with double quotes", "same(\"hello\")", description.toString());
    }

    @Test(timeout = 4000)
    public void testDescribeToCharacterValue() {
        Same matcher = new Same('z');
        Description description = new StringDescription();

        matcher.describeTo(description);

        assertEquals("Character values should be quoted with single quotes", "same('z')", description.toString());
    }

    @Test(timeout = 4000)
    public void testDescribeToNonStringNonCharObject() {
        Integer number = 42;
        Same matcher = new Same(number);
        Description description = new StringDescription();

        matcher.describeTo(description);

        assertEquals("Non-string/char objects should not have quotation marks", "same(42)", description.toString());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testMatchesBothWantedAndActualNull() {
        Same matcher = new Same(null);

        assertTrue("Matcher should match when both wanted and actual are null", matcher.matches(null));
    }

    @Test(timeout = 4000)
    public void testMatchesWantedNullActualNonNull() {
        Same matcher = new Same(null);

        assertFalse("Matcher should return false when wanted is null and actual is non-null", matcher.matches(new Object()));
    }

    @Test(timeout = 4000)
    public void testMatchesWantedNonNullActualNull() {
        Same matcher = new Same("not-null");

        assertFalse("Matcher should return false when wanted is non-null and actual is null", matcher.matches(null));
    }

    @Test(timeout = 4000)
    public void testDescribeToEmptyString() {
        Same matcher = new Same("");
        Description description = new StringDescription();

        matcher.describeTo(description);

        assertEquals("Empty string should be enclosed in double quotes", "same(\"\")", description.toString());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets Defects4J bug: NPEWithCertainMatchersTest::shouldNotThrowNPEWhenNullPassedToSame.
     * When `wanted` is null, calling `describeTo` invokes `wanted.toString()` in defective code,
     * throwing a NullPointerException instead of gracefully outputting "same(null)".
     */
    @Test(timeout = 4000)
    public void testDescribeToShouldNotThrowNPEWhenNullPassedToSame() {
        Same matcher = new Same(null);
        Description description = new StringDescription();

        matcher.describeTo(description);

        assertEquals("When wanted is null, description should be 'same(null)' without throwing NPE",
                "same(null)", description.toString());
    }

    // =========================================================================
    // Partition D: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testSerializationIntegrityWithNonNullValue() throws Exception {
        String testValue = "serializable_test";
        Same original = new Same(testValue);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.flush();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object deserialized = ois.readObject();

        assertNotNull("Deserialized object should not be null", deserialized);
        assertTrue("Deserialized object must be an instance of Same", deserialized instanceof Same);

        Description desc = new StringDescription();
        ((Same) deserialized).describeTo(desc);
        assertEquals("same(\"serializable_test\")", desc.toString());
    }

    @Test(timeout = 4000)
    public void testSerializationIntegrityWithNullValue() throws Exception {
        Same original = new Same(null);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.flush();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object deserialized = ois.readObject();

        assertNotNull("Deserialized object should not be null", deserialized);
        assertTrue("Deserialized object must be an instance of Same", deserialized instanceof Same);
        assertTrue("Deserialized matcher with null wanted should match null", ((Same) deserialized).matches(null));
    }
}