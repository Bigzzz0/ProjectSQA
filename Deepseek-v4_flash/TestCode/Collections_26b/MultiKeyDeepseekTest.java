package org.apache.commons.collections4.keyvalue;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class MultiKeyDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Constructors:
     * - 2,3,4,5 key constructors delegate to array constructor with makeClone=false
     * - array constructor: null check (throws IAE), makeClone true/false branches
     * - calculateHashCode: null key vs non-null key branches, XOR accumulation
     * 
     * Methods:
     * - getKeys(): returns clone (defensive copy)
     * - getKey(index): valid index, negative index, index == size, index > size
     * - size(): returns keys.length
     * - equals(): same reference, null, non-MultiKey, same keys, different keys, 
     *             different lengths, null elements
     * - hashCode(): cached value, recalculated after deserialization
     * - toString(): array representation
     * 
     * Defect Target (testEqualsAfterSerializationOfDerivedClass):
     * The bug occurs when a subclass of MultiKey is serialized and deserialized.
     * The readResolve() method calls calculateHashCode(keys) but the keys field
     * in the subclass may not be properly initialized or the method is not
     * accessible/overridden correctly. The test verifies that after
     * deserialization, the hash code is correctly recalculated and equals()
     * works properly for derived classes.
     */
    
    // ==================== Partition A: Core Functional Logic & State Transitions ====================
    
    @Test(timeout = 4000)
    public void testTwoKeyConstructorAndGetters() {
        MultiKey<String> mk = new MultiKey<String>("a", "b");
        assertEquals(2, mk.size());
        assertEquals("a", mk.getKey(0));
        assertEquals("b", mk.getKey(1));
        assertArrayEquals(new String[]{"a", "b"}, mk.getKeys());
    }
    
    @Test(timeout = 4000)
    public void testThreeKeyConstructorAndGetters() {
        MultiKey<Integer> mk = new MultiKey<Integer>(1, 2, 3);
        assertEquals(3, mk.size());
        assertEquals(Integer.valueOf(1), mk.getKey(0));
        assertEquals(Integer.valueOf(2), mk.getKey(1));
        assertEquals(Integer.valueOf(3), mk.getKey(2));
    }
    
    @Test(timeout = 4000)
    public void testFourKeyConstructorAndGetters() {
        MultiKey<Object> mk = new MultiKey<Object>("x", 1, true, null);
        assertEquals(4, mk.size());
        assertEquals("x", mk.getKey(0));
        assertEquals(1, mk.getKey(1));
        assertEquals(true, mk.getKey(2));
        assertNull(mk.getKey(3));
    }
    
    @Test(timeout = 4000)
    public void testFiveKeyConstructorAndGetters() {
        MultiKey<String> mk = new MultiKey<String>("a", "b", "c", "d", "e");
        assertEquals(5, mk.size());
        assertEquals("a", mk.getKey(0));
        assertEquals("e", mk.getKey(4));
    }
    
    @Test(timeout = 4000)
    public void testArrayConstructorWithClone() {
        String[] keys = {"a", "b", "c"};
        MultiKey<String> mk = new MultiKey<String>(keys);
        keys[0] = "changed";
        assertEquals("a", mk.getKey(0)); // original array should be cloned
        assertArrayEquals(new String[]{"a", "b", "c"}, mk.getKeys());
    }
    
    @Test(timeout = 4000)
    public void testArrayConstructorWithoutClone() {
        String[] keys = {"a", "b"};
        MultiKey<String> mk = new MultiKey<String>(keys, false);
        keys[0] = "changed";
        assertEquals("changed", mk.getKey(0)); // no clone, so changes reflect
    }
    
    @Test(timeout = 4000)
    public void testGetKeysReturnsClone() {
        MultiKey<String> mk = new MultiKey<String>("a", "b");
        String[] keys = mk.getKeys();
        keys[0] = "modified";
        assertEquals("a", mk.getKey(0)); // original unaffected
    }
    
    @Test(timeout = 4000)
    public void testSizeWithVariousLengths() {
        assertEquals(2, new MultiKey<String>("a", "b").size());
        assertEquals(3, new MultiKey<String>("a", "b", "c").size());
        assertEquals(4, new MultiKey<String>("a", "b", "c", "d").size());
        assertEquals(5, new MultiKey<String>("a", "b", "c", "d", "e").size());
    }
    
    // ==================== Partition B: Boundary Value Analysis & Extremes ====================
    
    @Test(timeout = 4000)
    public void testNullKeysAllowed() {
        MultiKey<String> mk = new MultiKey<String>(null, "b", null);
        assertEquals(3, mk.size());
        assertNull(mk.getKey(0));
        assertNull(mk.getKey(2));
    }
    
    @Test(timeout = 4000)
    public void testAllNullKeys() {
        MultiKey<Object> mk = new MultiKey<Object>(null, null);
        assertEquals(2, mk.size());
        assertNull(mk.getKey(0));
        assertNull(mk.getKey(1));
    }
    
    @Test(timeout = 4000)
    public void testEmptyArrayConstructor() {
        MultiKey<Object> mk = new MultiKey<Object>(new Object[0]);
        assertEquals(0, mk.size());
        assertEquals(0, mk.hashCode());
    }
    
    @Test(timeout = 4000)
    public void testNullArrayConstructorThrows() {
        try {
            new MultiKey<String>((String[]) null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
    
    @Test(timeout = 4000)
    public void testNullArrayConstructorWithFlagThrows() {
        try {
            new MultiKey<String>((String[]) null, true);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
    
    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testGetKeyNegativeIndex() {
        MultiKey<String> mk = new MultiKey<String>("a", "b");
        mk.getKey(-1);
    }
    
    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testGetKeyIndexEqualToSize() {
        MultiKey<String> mk = new MultiKey<String>("a", "b");
        mk.getKey(2);
    }
    
    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testGetKeyIndexGreaterThanSize() {
        MultiKey<String> mk = new MultiKey<String>("a", "b");
        mk.getKey(5);
    }
    
    @Test(timeout = 4000)
    public void testGetKeyBoundaryValidIndex() {
        MultiKey<String> mk = new MultiKey<String>("a", "b", "c");
        assertEquals("a", mk.getKey(0));
        assertEquals("c", mk.getKey(2));
    }
    
    // ==================== Partition C: Defect-Targeted Branch Zone ====================
    
    @Test(timeout = 4000)
    public void testEqualsAfterSerializationOfDerivedClass() throws IOException, ClassNotFoundException {
        // Create a derived class that adds a field
        DerivedMultiKey<String> original = new DerivedMultiKey<String>("key1", "key2");
        original.extraField = "extra";
        
        // Serialize
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();
        
        // Deserialize
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        DerivedMultiKey<?> deserialized = (DerivedMultiKey<?>) ois.readObject();
        ois.close();
        
        // The bug: after deserialization, the hash code is 0 instead of the correct value
        // because readResolve() in the parent class may not be called or the keys
        // are not properly restored for derived classes
        assertEquals("Hash code should be recalculated after deserialization", 
                     original.hashCode(), deserialized.hashCode());
        
        // The equals method should work correctly
        assertEquals("Deserialized object should equal original", original, deserialized);
        assertEquals("Original should equal deserialized", deserialized, original);
        
        // Verify the extra field is preserved
        assertEquals("extra", deserialized.extraField);
    }
    
    @Test(timeout = 4000)
    public void testEqualsAfterSerializationOfDerivedClassWithNullKeys() throws IOException, ClassNotFoundException {
        DerivedMultiKey<String> original = new DerivedMultiKey<String>(null, "value");
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        DerivedMultiKey<?> deserialized = (DerivedMultiKey<?>) ois.readObject();
        ois.close();
        
        assertEquals(original.hashCode(), deserialized.hashCode());
        assertEquals(original, deserialized);
    }
    
    // ==================== Partition D: Exception & Defensive Guard Paths ====================
    
    @Test(timeout = 4000)
    public void testEqualsWithNullObject() {
        MultiKey<String> mk = new MultiKey<String>("a", "b");
        assertFalse(mk.equals(null));
    }
    
    @Test(timeout = 4000)
    public void testEqualsWithNonMultiKey() {
        MultiKey<String> mk = new MultiKey<String>("a", "b");
        assertFalse(mk.equals("not a multikey"));
        assertFalse(mk.equals(new Object()));
    }
    
    @Test(timeout = 4000)
    public void testEqualsWithSameReference() {
        MultiKey<String> mk = new MultiKey<String>("a", "b");
        assertTrue(mk.equals(mk));
    }
    
    @Test(timeout = 4000)
    public void testEqualsWithDifferentLengths() {
        MultiKey<String> mk1 = new MultiKey<String>("a", "b");
        MultiKey<String> mk2 = new MultiKey<String>("a", "b", "c");
        assertFalse(mk1.equals(mk2));
        assertFalse(mk2.equals(mk1));
    }
    
    @Test(timeout = 4000)
    public void testEqualsWithSameKeys() {
        MultiKey<String> mk1 = new MultiKey<String>("a", "b");
        MultiKey<String> mk2 = new MultiKey<String>("a", "b");
        assertTrue(mk1.equals(mk2));
        assertTrue(mk2.equals(mk1));
    }
    
    @Test(timeout = 4000)
    public void testEqualsWithDifferentKeys() {
        MultiKey<String> mk1 = new MultiKey<String>("a", "b");
        MultiKey<String> mk2 = new MultiKey<String>("a", "c");
        assertFalse(mk1.equals(mk2));
    }
    
    @Test(timeout = 4000)
    public void testEqualsWithNullElements() {
        MultiKey<String> mk1 = new MultiKey<String>(null, "b");
        MultiKey<String> mk2 = new MultiKey<String>(null, "b");
        MultiKey<String> mk3 = new MultiKey<String>("a", null);
        assertTrue(mk1.equals(mk2));
        assertFalse(mk1.equals(mk3));
    }
    
    @Test(timeout = 4000)
    public void testEqualsWithMixedNullAndNonNull() {
        MultiKey<String> mk1 = new MultiKey<String>(null, "b");
        MultiKey<String> mk2 = new MultiKey<String>("a", "b");
        assertFalse(mk1.equals(mk2));
    }
    
    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================
    
    @Test(timeout = 4000)
    public void testHashCodeConsistency() {
        MultiKey<String> mk = new MultiKey<String>("a", "b");
        int h1 = mk.hashCode();
        int h2 = mk.hashCode();
        assertEquals(h1, h2);
    }
    
    @Test(timeout = 4000)
    public void testHashCodeWithNullKeys() {
        MultiKey<String> mk1 = new MultiKey<String>(null, "b");
        MultiKey<String> mk2 = new MultiKey<String>(null, "b");
        assertEquals(mk1.hashCode(), mk2.hashCode());
    }
    
    @Test(timeout = 4000)
    public void testHashCodeDifferentForDifferentKeys() {
        MultiKey<String> mk1 = new MultiKey<String>("a", "b");
        MultiKey<String> mk2 = new MultiKey<String>("a", "c");
        assertNotEquals(mk1.hashCode(), mk2.hashCode());
    }
    
    @Test(timeout = 4000)
    public void testHashCodeEqualsContract() {
        MultiKey<String> mk1 = new MultiKey<String>("a", "b");
        MultiKey<String> mk2 = new MultiKey<String>("a", "b");
        assertEquals(mk1.hashCode(), mk2.hashCode());
        assertTrue(mk1.equals(mk2));
    }
    
    @Test(timeout = 4000)
    public void testToString() {
        MultiKey<String> mk = new MultiKey<String>("a", "b");
        assertEquals("MultiKey[a, b]", mk.toString());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithNull() {
        MultiKey<String> mk = new MultiKey<String>(null, "b");
        assertEquals("MultiKey[null, b]", mk.toString());
    }
    
    @Test(timeout = 4000)
    public void testSerializationRoundTrip() throws IOException, ClassNotFoundException {
        MultiKey<String> original = new MultiKey<String>("a", "b");
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        MultiKey<?> deserialized = (MultiKey<?>) ois.readObject();
        ois.close();
        
        assertEquals(original, deserialized);
        assertEquals(original.hashCode(), deserialized.hashCode());
        assertArrayEquals(original.getKeys(), deserialized.getKeys());
    }
    
    @Test(timeout = 4000)
    public void testSerializationWithNullKeys() throws IOException, ClassNotFoundException {
        MultiKey<String> original = new MultiKey<String>(null, "b", null);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        MultiKey<?> deserialized = (MultiKey<?>) ois.readObject();
        ois.close();
        
        assertEquals(original, deserialized);
        assertEquals(original.hashCode(), deserialized.hashCode());
    }
    
    @Test(timeout = 4000)
    public void testHashCodeAfterDeserialization() throws IOException, ClassNotFoundException {
        MultiKey<String> original = new MultiKey<String>("a", "b");
        int originalHash = original.hashCode();
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        MultiKey<?> deserialized = (MultiKey<?>) ois.readObject();
        ois.close();
        
        assertEquals(originalHash, deserialized.hashCode());
    }
    
    // Helper class for testing derived class serialization
    private static class DerivedMultiKey<K> extends MultiKey<K> implements Serializable {
        private static final long serialVersionUID = 1L;
        public String extraField;
        
        public DerivedMultiKey(K key1, K key2) {
            super(key1, key2);
        }
    }
}