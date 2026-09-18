package org.apache.commons.collections.keyvalue;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/*
 * [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------
 * Branch / Condition Tested                        | Partition & Test Target
 * ---------------------------------------------------------------------------------------------------
 * keys == null in MultiKey(keys, makeClone)         | Partition D: throws IllegalArgumentException
 * makeClone == true vs false                       | Partition A & B: verify array cloning/aliasing
 * 2, 3, 4, 5-argument constructors                 | Partition A: delegation to (Object[], false)
 * calculateHashCode: key == null vs key != null     | Partition B: XOR hashing with null and non-null keys
 * getKey(index): index valid vs out-of-bounds       | Partition A & D: bounds checking
 * getKeys(): clone integrity                        | Partition A: mutation of returned array does not leak
 * equals: other == this                            | Partition E: reflexivity branch
 * equals: other not instanceof MultiKey            | Partition E: type mismatch branch
 * equals: Arrays.equals(keys, other.keys)          | Partition E: symmetric, length, and element equality
 * hashCode: consistency with equals contract       | Partition E: hash code equality
 * Defect: transient hashCode after deserialization | Partition C: deserialized MultiKey has non-zero hashCode
 *         and retrieves mapped value from HashMap  | Ground truth: TestMultiKey::testEqualsAfterSerialization
 * ---------------------------------------------------------------------------------------------------
 */
public class MultiKeyGeminiTest {

    // -------------------------------------------------------------------------
    // Helper Serialization Methods
    // -------------------------------------------------------------------------
    private byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);
        oos.flush();
        oos.close();
        return baos.toByteArray();
    }

    private Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object result = ois.readObject();
        ois.close();
        return result;
    }

    // -------------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testTwoArgConstructorAndGetters() {
        MultiKey mk = new MultiKey("k1", "k2");
        assertEquals(2, mk.size());
        assertEquals("k1", mk.getKey(0));
        assertEquals("k2", mk.getKey(1));

        Object[] keys = mk.getKeys();
        assertArrayEquals(new Object[]{"k1", "k2"}, keys);
    }

    @Test(timeout = 4000)
    public void testThreeArgConstructorAndGetters() {
        MultiKey mk = new MultiKey("k1", "k2", "k3");
        assertEquals(3, mk.size());
        assertEquals("k1", mk.getKey(0));
        assertEquals("k2", mk.getKey(1));
        assertEquals("k3", mk.getKey(2));
    }

    @Test(timeout = 4000)
    public void testFourArgConstructorAndGetters() {
        MultiKey mk = new MultiKey("k1", "k2", "k3", "k4");
        assertEquals(4, mk.size());
        assertEquals("k1", mk.getKey(0));
        assertEquals("k2", mk.getKey(1));
        assertEquals("k3", mk.getKey(2));
        assertEquals("k4", mk.getKey(3));
    }

    @Test(timeout = 4000)
    public void testFiveArgConstructorAndGetters() {
        MultiKey mk = new MultiKey("k1", "k2", "k3", "k4", "k5");
        assertEquals(5, mk.size());
        assertEquals("k1", mk.getKey(0));
        assertEquals("k2", mk.getKey(1));
        assertEquals("k3", mk.getKey(2));
        assertEquals("k4", mk.getKey(3));
        assertEquals("k5", mk.getKey(4));
    }

    @Test(timeout = 4000)
    public void testArrayConstructorDefaultClones() {
        Object[] source = new Object[]{"a", "b"};
        MultiKey mk = new MultiKey(source);
        source[0] = "mutated";
        assertEquals("a", mk.getKey(0));
    }

    @Test(timeout = 4000)
    public void testArrayConstructorCloneOptionTrue() {
        Object[] source = new Object[]{"x", "y"};
        MultiKey mk = new MultiKey(source, true);
        source[0] = "changed";
        assertEquals("x", mk.getKey(0));
    }

    @Test(timeout = 4000)
    public void testArrayConstructorCloneOptionFalse() {
        Object[] source = new Object[]{"x", "y"};
        MultiKey mk = new MultiKey(source, false);
        source[0] = "changed";
        assertEquals("changed", mk.getKey(0));
    }

    @Test(timeout = 4000)
    public void testGetKeysReturnsClone() {
        MultiKey mk = new MultiKey("a", "b");
        Object[] retrievedKeys = mk.getKeys();
        retrievedKeys[0] = "mutated";
        assertEquals("a", mk.getKey(0));
    }

    // -------------------------------------------------------------------------
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testEmptyArray() {
        MultiKey mk = new MultiKey(new Object[0]);
        assertEquals(0, mk.size());
        assertEquals(0, mk.getKeys().length);
        assertEquals(0, mk.hashCode());
        assertEquals("MultiKey[]", mk.toString());
    }

    @Test(timeout = 4000)
    public void testAllNullKeysTwoArgs() {
        MultiKey mk = new MultiKey(null, null);
        assertEquals(2, mk.size());
        assertNull(mk.getKey(0));
        assertNull(mk.getKey(1));
        assertEquals(0, mk.hashCode());
        assertEquals("MultiKey[null, null]", mk.toString());
    }

    @Test(timeout = 4000)
    public void testMixedNullAndNonNullKeys() {
        String key1 = "alpha";
        MultiKey mk = new MultiKey(key1, null, "beta");
        assertEquals(3, mk.size());
        assertEquals("alpha", mk.getKey(0));
        assertNull(mk.getKey(1));
        assertEquals("beta", mk.getKey(2));

        int expectedHash = key1.hashCode() ^ "beta".hashCode();
        assertEquals(expectedHash, mk.hashCode());
    }

    @Test(timeout = 4000)
    public void testToStringFormat() {
        MultiKey mk = new MultiKey("one", "two", "three");
        assertEquals("MultiKey[one, two, three]", mk.toString());
    }

    // -------------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testEqualsAfterSerialization() throws Exception {
        // MultiKey caches hashCode in a transient field. If readResolve or readObject is missing
        // or fails to recalculate hashCode, the hashCode becomes 0 after deserialization,
        // causing map lookups to fail and return null.
        MultiKey mk1 = new MultiKey("key1", "key2");
        Map<MultiKey, Integer> map = new HashMap<MultiKey, Integer>();
        map.put(mk1, 2);

        byte[] serializedData = serialize(mk1);
        MultiKey deserializedMk = (MultiKey) deserialize(serializedData);

        assertEquals("Deserialized MultiKey must equal the original MultiKey", mk1, deserializedMk);
        assertEquals("Deserialized MultiKey hashCode must match original hashCode", mk1.hashCode(), deserializedMk.hashCode());
        assertEquals("Map lookup using deserialized MultiKey must return original value",
                Integer.valueOf(2), map.get(deserializedMk));
    }

    @Test(timeout = 4000)
    public void testSerializationRoundTripPreservesKeys() throws Exception {
        MultiKey original = new MultiKey(1, 2, 3, 4, 5);
        byte[] bytes = serialize(original);
        MultiKey copy = (MultiKey) deserialize(bytes);

        assertEquals(original.size(), copy.size());
        for (int i = 0; i < original.size(); i++) {
            assertEquals(original.getKey(i), copy.getKey(i));
        }
        assertEquals(original, copy);
        assertEquals(original.hashCode(), copy.hashCode());
    }

    // -------------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // -------------------------------------------------------------------------

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorNullArrayThrowsException() {
        new MultiKey((Object[]) null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorNullArrayWithCloneTrueThrowsException() {
        new MultiKey((Object[]) null, true);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorNullArrayWithCloneFalseThrowsException() {
        new MultiKey((Object[]) null, false);
    }

    @Test(timeout = 4000, expected = ArrayIndexOutOfBoundsException.class)
    public void testGetKeyNegativeIndexThrowsException() {
        MultiKey mk = new MultiKey("a", "b");
        mk.getKey(-1);
    }

    @Test(timeout = 4000, expected = ArrayIndexOutOfBoundsException.class)
    public void testGetKeyIndexEqualToSizeThrowsException() {
        MultiKey mk = new MultiKey("a", "b");
        mk.getKey(2);
    }

    @Test(timeout = 4000, expected = ArrayIndexOutOfBoundsException.class)
    public void testGetKeyIndexGreaterThanSizeThrowsException() {
        MultiKey mk = new MultiKey("a", "b");
        mk.getKey(5);
    }

    // -------------------------------------------------------------------------
    // Partition E: Object Lifecycle & Contract Integrity
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testEqualsSameInstance() {
        MultiKey mk = new MultiKey("a", "b");
        assertTrue(mk.equals(mk));
    }

    @Test(timeout = 4000)
    public void testEqualsNullObject() {
        MultiKey mk = new MultiKey("a", "b");
        assertFalse(mk.equals(null));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentClass() {
        MultiKey mk = new MultiKey("a", "b");
        assertFalse(mk.equals("string-object"));
        assertFalse(mk.equals(new Object()));
    }

    @Test(timeout = 4000)
    public void testEqualsSameKeys() {
        MultiKey mk1 = new MultiKey("a", "b");
        MultiKey mk2 = new MultiKey("a", "b");
        assertTrue(mk1.equals(mk2));
        assertTrue(mk2.equals(mk1));
        assertEquals(mk1.hashCode(), mk2.hashCode());
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentLengths() {
        MultiKey mk1 = new MultiKey("a", "b");
        MultiKey mk2 = new MultiKey("a", "b", "c");
        assertFalse(mk1.equals(mk2));
        assertFalse(mk2.equals(mk1));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentKeys() {
        MultiKey mk1 = new MultiKey("a", "b");
        MultiKey mk2 = new MultiKey("a", "c");
        assertFalse(mk1.equals(mk2));
        assertFalse(mk2.equals(mk1));
    }

    @Test(timeout = 4000)
    public void testEqualsWithNullKeys() {
        MultiKey mk1 = new MultiKey("a", null);
        MultiKey mk2 = new MultiKey("a", null);
        MultiKey mk3 = new MultiKey("a", "b");
        MultiKey mk4 = new MultiKey(null, "a");

        assertTrue(mk1.equals(mk2));
        assertEquals(mk1.hashCode(), mk2.hashCode());

        assertFalse(mk1.equals(mk3));
        assertFalse(mk1.equals(mk4));
    }

    @Test(timeout = 4000)
    public void testHashCodeCalculationMatchesKeyXor() {
        Integer i1 = 1234;
        Integer i2 = 5678;
        Integer i3 = 9012;
        MultiKey mk = new MultiKey(i1, i2, i3);

        int expectedHashCode = i1.hashCode() ^ i2.hashCode() ^ i3.hashCode();
        assertEquals(expectedHashCode, mk.hashCode());
    }
}