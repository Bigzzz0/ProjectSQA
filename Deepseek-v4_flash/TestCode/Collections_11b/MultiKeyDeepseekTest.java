package org.apache.commons.collections.keyvalue;

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
     * Target Class: MultiKey
     * 
     * Decision Branches:
     * 1. Constructor with null array -> IllegalArgumentException
     * 2. Constructor with makeClone=true -> clones array
     * 3. Constructor with makeClone=false -> assigns array directly
     * 4. calculateHashCode: null key vs non-null key
     * 5. equals: same reference, MultiKey instance, non-MultiKey
     * 6. getKey: valid index, invalid index (IndexOutOfBounds)
     * 7. size: various array lengths
     * 8. getKeys: returns clone (modification isolation)
     * 
     * Boundary Values:
     * - null keys in various positions
     * - empty array
     * - single element array
     * - arrays of length 2, 3, 4, 5
     * - index 0, last index, negative index, out-of-bounds index
     * 
     * Defect Target (from Defects4J):
     * - testEqualsAfterSerialization: expected:<2> but was:<null>
     *   The bug is that after deserialization, the hashCode field is not
     *   recalculated (remains 0/null), causing equals to fail when used
     *   in hash-based collections. The class is missing the readResolve
     *   or readObject method to recalculate hashCode after deserialization.
     */
    
    // ==================== Partition A: Core Functional Logic ====================
    
    @Test(timeout = 4000)
    public void testTwoKeyConstructorAndAccessors() {
        MultiKey mk = new MultiKey("key1", "key2");
        assertEquals(2, mk.size());
        assertEquals("key1", mk.getKey(0));
        assertEquals("key2", mk.getKey(1));
        assertNotNull(mk.getKeys());
        assertEquals(2, mk.getKeys().length);
    }
    
    @Test(timeout = 4000)
    public void testThreeKeyConstructorAndAccessors() {
        MultiKey mk = new MultiKey("a", "b", "c");
        assertEquals(3, mk.size());
        assertEquals("a", mk.getKey(0));
        assertEquals("b", mk.getKey(1));
        assertEquals("c", mk.getKey(2));
    }
    
    @Test(timeout = 4000)
    public void testFourKeyConstructorAndAccessors() {
        MultiKey mk = new MultiKey(1, 2, 3, 4);
        assertEquals(4, mk.size());
        assertEquals(1, mk.getKey(0));
        assertEquals(4, mk.getKey(3));
    }
    
    @Test(timeout = 4000)
    public void testFiveKeyConstructorAndAccessors() {
        MultiKey mk = new MultiKey("a", "b", "c", "d", "e");
        assertEquals(5, mk.size());
        assertEquals("a", mk.getKey(0));
        assertEquals("e", mk.getKey(4));
    }
    
    @Test(timeout = 4000)
    public void testArrayConstructorWithClone() {
        Object[] keys = {"x", "y", "z"};
        MultiKey mk = new MultiKey(keys);
        // Modify original array - should not affect MultiKey
        keys[0] = "modified";
        assertEquals("x", mk.getKey(0));
        assertEquals(3, mk.size());
    }
    
    @Test(timeout = 4000)
    public void testArrayConstructorWithoutClone() {
        Object[] keys = {"x", "y"};
        MultiKey mk = new MultiKey(keys, false);
        // Direct assignment - changes to array reflect in MultiKey
        keys[0] = "modified";
        assertEquals("modified", mk.getKey(0));
    }
    
    @Test(timeout = 4000)
    public void testGetKeysReturnsClone() {
        MultiKey mk = new MultiKey("a", "b");
        Object[] keys = mk.getKeys();
        keys[0] = "changed";
        assertEquals("a", mk.getKey(0));
    }
    
    @Test(timeout = 4000)
    public void testSizeForVariousLengths() {
        assertEquals(1, new MultiKey(new Object[]{"only"}).size());
        assertEquals(2, new MultiKey("a", "b").size());
        assertEquals(3, new MultiKey("a", "b", "c").size());
        assertEquals(4, new MultiKey("a", "b", "c", "d").size());
        assertEquals(5, new MultiKey("a", "b", "c", "d", "e").size());
    }
    
    // ==================== Partition B: Boundary Value Analysis ====================
    
    @Test(timeout = 4000)
    public void testNullKeysInVariousPositions() {
        MultiKey mk1 = new MultiKey(null, "b");
        assertEquals(2, mk1.size());
        assertNull(mk1.getKey(0));
        assertEquals("b", mk1.getKey(1));
        
        MultiKey mk2 = new MultiKey("a", null);
        assertNull(mk2.getKey(1));
        
        MultiKey mk3 = new MultiKey(null, null);
        assertNull(mk3.getKey(0));
        assertNull(mk3.getKey(1));
        
        MultiKey mk4 = new MultiKey(null, "b", null);
        assertNull(mk4.getKey(0));
        assertNull(mk4.getKey(2));
    }
    
    @Test(timeout = 4000)
    public void testEmptyArrayConstructor() {
        MultiKey mk = new MultiKey(new Object[0]);
        assertEquals(0, mk.size());
        assertEquals(0, mk.getKeys().length);
        assertEquals(0, mk.hashCode());
    }
    
    @Test(timeout = 4000)
    public void testSingleElementArray() {
        MultiKey mk = new MultiKey(new Object[]{"solo"});
        assertEquals(1, mk.size());
        assertEquals("solo", mk.getKey(0));
    }
    
    @Test(timeout = 4000)
    public void testGetKeyBoundaryIndices() {
        MultiKey mk = new MultiKey("a", "b", "c");
        assertEquals("a", mk.getKey(0));  // first index
        assertEquals("c", mk.getKey(2));  // last index
    }
    
    @Test(timeout = 4000)
    public void testHashCodeWithNullKeys() {
        MultiKey mk1 = new MultiKey(null, "b");
        MultiKey mk2 = new MultiKey("a", null);
        MultiKey mk3 = new MultiKey(null, null);
        
        // Null keys contribute 0 to hash
        assertEquals("b".hashCode(), mk1.hashCode());
        assertEquals("a".hashCode(), mk2.hashCode());
        assertEquals(0, mk3.hashCode());
    }
    
    @Test(timeout = 4000)
    public void testHashCodeWithMultipleKeys() {
        MultiKey mk = new MultiKey("a", "b", "c");
        int expected = "a".hashCode() ^ "b".hashCode() ^ "c".hashCode();
        assertEquals(expected, mk.hashCode());
    }
    
    // ==================== Partition C: Defect-Targeted Branch Zone ====================
    
    @Test(timeout = 4000)
    public void testEqualsAfterSerialization() throws IOException, ClassNotFoundException {
        // Create MultiKey with 2 keys
        MultiKey original = new MultiKey("key1", "key2");
        
        // Serialize
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();
        
        // Deserialize
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        MultiKey deserialized = (MultiKey) ois.readObject();
        ois.close();
        
        // The defect: after deserialization, hashCode is not recalculated
        // (remains 0), so equals fails when used in hash-based collections
        assertEquals("Deserialized MultiKey should equal original", original, deserialized);
        assertEquals("Hash codes should match after deserialization", 
                     original.hashCode(), deserialized.hashCode());
        
        // Test usage in hash-based collection
        java.util.Map<MultiKey, String> map = new java.util.HashMap<>();
        map.put(original, "value");
        assertEquals("value", map.get(deserialized));
    }
    
    @Test(timeout = 4000)
    public void testEqualsAfterSerializationWithNullKeys() throws IOException, ClassNotFoundException {
        MultiKey original = new MultiKey(null, "key2");
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        MultiKey deserialized = (MultiKey) ois.readObject();
        ois.close();
        
        assertEquals(original, deserialized);
        assertEquals(original.hashCode(), deserialized.hashCode());
    }
    
    @Test(timeout = 4000)
    public void testEqualsAfterSerializationWithThreeKeys() throws IOException, ClassNotFoundException {
        MultiKey original = new MultiKey("a", "b", "c");
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        MultiKey deserialized = (MultiKey) ois.readObject();
        ois.close();
        
        assertEquals(original, deserialized);
        assertEquals(original.hashCode(), deserialized.hashCode());
    }
    
    // ==================== Partition D: Exception & Defensive Guard Paths ====================
    
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNullArrayConstructorThrowsException() {
        new MultiKey((Object[]) null);
    }
    
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNullArrayConstructorWithCloneFlagThrowsException() {
        new MultiKey(null, true);
    }
    
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNullArrayConstructorWithoutCloneFlagThrowsException() {
        new MultiKey(null, false);
    }
    
    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetKeyNegativeIndex() {
        MultiKey mk = new MultiKey("a", "b");
        mk.getKey(-1);
    }
    
    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetKeyIndexEqualToSize() {
        MultiKey mk = new MultiKey("a", "b");
        mk.getKey(2);
    }
    
    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetKeyIndexGreaterThanSize() {
        MultiKey mk = new MultiKey("a", "b");
        mk.getKey(10);
    }
    
    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetKeyOnEmptyArray() {
        MultiKey mk = new MultiKey(new Object[0]);
        mk.getKey(0);
    }
    
    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================
    
    @Test(timeout = 4000)
    public void testEqualsSameReference() {
        MultiKey mk = new MultiKey("a", "b");
        assertTrue(mk.equals(mk));
    }
    
    @Test(timeout = 4000)
    public void testEqualsWithEqualKeys() {
        MultiKey mk1 = new MultiKey("a", "b");
        MultiKey mk2 = new MultiKey("a", "b");
        assertTrue(mk1.equals(mk2));
        assertTrue(mk2.equals(mk1));
    }
    
    @Test(timeout = 4000)
    public void testEqualsWithDifferentKeys() {
        MultiKey mk1 = new MultiKey("a", "b");
        MultiKey mk2 = new MultiKey("a", "c");
        assertFalse(mk1.equals(mk2));
    }
    
    @Test(timeout = 4000)
    public void testEqualsWithDifferentLengths() {
        MultiKey mk1 = new MultiKey("a", "b");
        MultiKey mk2 = new MultiKey("a", "b", "c");
        assertFalse(mk1.equals(mk2));
    }
    
    @Test(timeout = 4000)
    public void testEqualsWithNullKeys() {
        MultiKey mk1 = new MultiKey(null, "b");
        MultiKey mk2 = new MultiKey(null, "b");
        MultiKey mk3 = new MultiKey("a", "b");
        
        assertTrue(mk1.equals(mk2));
        assertFalse(mk1.equals(mk3));
    }
    
    @Test(timeout = 4000)
    public void testEqualsWithNonMultiKeyObject() {
        MultiKey mk = new MultiKey("a", "b");
        assertFalse(mk.equals("not a multikey"));
        assertFalse(mk.equals(null));
        assertFalse(mk.equals(new Object()));
    }
    
    @Test(timeout = 4000)
    public void testHashCodeConsistency() {
        MultiKey mk1 = new MultiKey("a", "b");
        MultiKey mk2 = new MultiKey("a", "b");
        
        assertEquals(mk1.hashCode(), mk2.hashCode());
        // Hash code should be stable across calls
        assertEquals(mk1.hashCode(), mk1.hashCode());
    }
    
    @Test(timeout = 4000)
    public void testHashCodeWithDifferentKeyOrders() {
        MultiKey mk1 = new MultiKey("a", "b");
        MultiKey mk2 = new MultiKey("b", "a");
        
        // XOR is commutative, so hash codes are equal
        assertEquals(mk1.hashCode(), mk2.hashCode());
        // But equals should be false
        assertFalse(mk1.equals(mk2));
    }
    
    @Test(timeout = 4000)
    public void testToString() {
        MultiKey mk = new MultiKey("a", "b");
        assertEquals("MultiKey[a, b]", mk.toString());
        
        MultiKey mkNull = new MultiKey(null, "b");
        assertEquals("MultiKey[null, b]", mkNull.toString());
        
        MultiKey mkEmpty = new MultiKey(new Object[0]);
        assertEquals("MultiKey[]", mkEmpty.toString());
    }
    
    @Test(timeout = 4000)
    public void testSerializationRoundTrip() throws IOException, ClassNotFoundException {
        MultiKey original = new MultiKey("key1", 42, null);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        MultiKey deserialized = (MultiKey) ois.readObject();
        ois.close();
        
        assertEquals(original.size(), deserialized.size());
        assertEquals(original.getKey(0), deserialized.getKey(0));
        assertEquals(original.getKey(1), deserialized.getKey(1));
        assertEquals(original.getKey(2), deserialized.getKey(2));
        assertTrue(original.equals(deserialized));
    }
    
    @Test(timeout = 4000)
    public void testSerializationWithNullKeys() throws IOException, ClassNotFoundException {
        MultiKey original = new MultiKey(null, null);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        MultiKey deserialized = (MultiKey) ois.readObject();
        ois.close();
        
        assertEquals(original, deserialized);
        assertEquals(original.hashCode(), deserialized.hashCode());
    }
    
    @Test(timeout = 4000)
    public void testSerializableInterface() {
        assertTrue(Serializable.class.isAssignableFrom(MultiKey.class));
    }
    
    @Test(timeout = 4000)
    public void testKeysArrayIsFinal() throws Exception {
        java.lang.reflect.Field field = MultiKey.class.getDeclaredField("keys");
        assertTrue(java.lang.reflect.Modifier.isFinal(field.getModifiers()));
    }
    
    @Test(timeout = 4000)
    public void testHashCodeFieldIsTransient() throws Exception {
        java.lang.reflect.Field field = MultiKey.class.getDeclaredField("hashCode");
        assertTrue(java.lang.reflect.Modifier.isTransient(field.getModifiers()));
    }
    
    @Test(timeout = 4000)
    public void testHashCodeCached() {
        MultiKey mk = new MultiKey("a", "b");
        int first = mk.hashCode();
        int second = mk.hashCode();
        assertEquals(first, second);
    }
    
    @Test(timeout = 4000)
    public void testEqualsWithArrayKeys() {
        Object[] arr1 = {"a", "b"};
        Object[] arr2 = {"a", "b"};
        
        MultiKey mk1 = new MultiKey(arr1);
        MultiKey mk2 = new MultiKey(arr2);
        
        // Arrays.equals is used, so content comparison
        assertTrue(mk1.equals(mk2));
    }
    
    @Test(timeout = 4000)
    public void testGetKeysDoesNotExposeInternalArray() {
        MultiKey mk = new MultiKey("a", "b");
        Object[] keys = mk.getKeys();
        keys[0] = "changed";
        keys[1] = "changed";
        
        // Internal array should be unaffected
        assertEquals("a", mk.getKey(0));
        assertEquals("b", mk.getKey(1));
    }
    
    @Test(timeout = 4000)
    public void testConstructorWithClonePreservesOriginalArray() {
        Object[] original = {"a", "b"};
        MultiKey mk = new MultiKey(original, true);
        original[0] = "changed";
        
        assertEquals("a", mk.getKey(0));
        assertEquals("b", mk.getKey(1));
    }
    
    @Test(timeout = 4000)
    public void testConstructorWithoutCloneDoesNotPreserveOriginalArray() {
        Object[] original = {"a", "b"};
        MultiKey mk = new MultiKey(original, false);
        original[0] = "changed";
        
        assertEquals("changed", mk.getKey(0));
        assertEquals("b", mk.getKey(1));
    }
    
    @Test(timeout = 4000)
    public void testEqualsWithSameContentDifferentInstances() {
        MultiKey mk1 = new MultiKey(new Object[]{"a", "b"}, true);
        MultiKey mk2 = new MultiKey(new Object[]{"a", "b"}, true);
        
        assertTrue(mk1.equals(mk2));
        assertEquals(mk1.hashCode(), mk2.hashCode());
    }
    
    @Test(timeout = 4000)
    public void testHashCodeWithIntegerKeys() {
        MultiKey mk = new MultiKey(1, 2, 3);
        int expected = 1 ^ 2 ^ 3;
        assertEquals(expected, mk.hashCode());
    }
    
    @Test(timeout = 4000)
    public void testHashCodeWithMixedTypes() {
        MultiKey mk = new MultiKey("string", 42, null, true);
        int expected = "string".hashCode() ^ 42 ^ 0 ^ Boolean.TRUE.hashCode();
        assertEquals(expected, mk.hashCode());
    }
    
    @Test(timeout = 4000)
    public void testSerializationPreservesKeyOrder() throws IOException, ClassNotFoundException {
        MultiKey original = new MultiKey("first", "second", "third");
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        MultiKey deserialized = (MultiKey) ois.readObject();
        ois.close();
        
        assertEquals("first", deserialized.getKey(0));
        assertEquals("second", deserialized.getKey(1));
        assertEquals("third", deserialized.getKey(2));
    }
    
    @Test(timeout = 4000)
    public void testSerializationWithComplexObjects() throws IOException, ClassNotFoundException {
        java.util.ArrayList<String> list = new java.util.ArrayList<>();
        list.add("item");
        MultiKey original = new MultiKey(list, new java.util.HashMap<String, String>());
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        MultiKey deserialized = (MultiKey) ois.readObject();
        ois.close();
        
        assertEquals(original, deserialized);
        assertEquals(original.hashCode(), deserialized.hashCode());
    }
}