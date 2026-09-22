/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.math3.random;

import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.math3.random.BitsStreamGenerator
 *
 * Branch & Logic Matrix:
 * 1. nextBoolean():
 *    - next(1) != 0 -> true (bit 1) vs false (bit 0)
 * 2. nextBytes(byte[]):
 *    - Empty array (bytes.length == 0): loops bypassed
 *    - Length < 4 (e.g., 1, 2, 3 bytes): full 4-byte loop bypassed, remainder loop executed
 *    - Length == 4: full 4-byte loop executed once, remainder loop 0 iterations
 *    - Length > 4 (e.g., 5, 7, 8): both full 4-byte and remainder loops executed
 * 3. nextDouble():
 *    - Combines two 26-bit integers with 2^-52 factor. Range [0.0, 1.0).
 * 4. nextFloat():
 *    - Uses 23-bit integer with 2^-23 factor. Range [0.0, 1.0f).
 * 5. nextGaussian():
 *    - Double.isNaN(nextGaussian) is true -> computes pair (alpha, r), returns first, caches second.
 *    - Double.isNaN(nextGaussian) is false -> returns cached value, resets cache to Double.NaN.
 * 6. clear():
 *    - Clears cached nextGaussian back to Double.NaN.
 * 7. nextInt():
 *    - Full 32-bit integer generation.
 * 8. nextInt(int n):
 *    - Boundary: n <= 0 -> throws NotStrictlyPositiveException (0, -1, Integer.MIN_VALUE).
 *    - Power of 2: (n & -n) == n -> fast branch ((n * (long) next(31)) >> 31).
 *    - Non-power of 2: rejection loop while (bits - val + (n - 1) < 0).
 *      - Normal loop (no rejection on first draw).
 *      - Rejection path executed when bits close to Integer.MAX_VALUE causes overflow.
 * 9. nextLong():
 *    - Generates 64 bits from two next(32) calls (high 32 bits and low 32 bits).
 *
 * Known Defect Analysis (Ground Truth):
 * - Defects4J Distribution Clone Failures (GammaDistributionTest, NormalDistributionTest, LogNormalDistributionTest):
 *   When distributions are cloned or serialized, the underlying BitsStreamGenerator must preserve
 *   its internal state, specifically `nextGaussian` and must implement `Serializable`. If BitsStreamGenerator
 *   does not implement `java.io.Serializable`, deserialization resets the superclass state via the no-arg
 *   constructor, clearing `nextGaussian` and desynchronizing subsequent `sample()` calls.
 */
public class BitsStreamGeneratorGeminiTest {

    /**
     * Concrete test implementation of BitsStreamGenerator with controllable bit outputs.
     */
    private static class DummyGenerator extends BitsStreamGenerator implements Serializable {
        private static final long serialVersionUID = 1L;
        private int[] bitSequence;
        private int index = 0;
        private long seedValue = 0L;

        public DummyGenerator() {
            super();
        }

        public DummyGenerator(int... bitSequence) {
            super();
            this.bitSequence = bitSequence;
        }

        public void setSequence(int... bitSequence) {
            this.bitSequence = bitSequence;
            this.index = 0;
        }

        @Override
        public void setSeed(int seed) {
            this.seedValue = seed;
            clear();
        }

        @Override
        public void setSeed(int[] seed) {
            this.seedValue = (seed != null && seed.length > 0) ? seed[0] : 0L;
            clear();
        }

        @Override
        public void setSeed(long seed) {
            this.seedValue = seed;
            clear();
        }

        @Override
        protected int next(int bits) {
            int val;
            if (bitSequence != null && bitSequence.length > 0) {
                val = bitSequence[index % bitSequence.length];
                index++;
            } else {
                // Linear congruential step for reproducible non-trivial sequence
                seedValue = (seedValue * 2862933555777941757L + 7046029254386353087L);
                val = (int) (seedValue >>> (64 - bits));
            }
            if (bits < 32) {
                val &= (1 << bits) - 1;
            }
            return val;
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testNextBoolean() {
        DummyGenerator generator = new DummyGenerator(1, 0, 1, 0);
        assertTrue(generator.nextBoolean());
        assertFalse(generator.nextBoolean());
        assertTrue(generator.nextBoolean());
        assertFalse(generator.nextBoolean());
    }

    @Test(timeout = 4000)
    public void testNextInt() {
        DummyGenerator generator = new DummyGenerator(0x12345678, -1, 0);
        assertEquals(0x12345678, generator.nextInt());
        assertEquals(-1, generator.nextInt());
        assertEquals(0, generator.nextInt());
    }

    @Test(timeout = 4000)
    public void testNextLong() {
        // nextLong calls next(32) twice: high then low
        DummyGenerator generator = new DummyGenerator(0x12345678, 0x0abcdef0);
        long result = generator.nextLong();
        assertEquals(0x123456780abcdef0L, result);
    }

    @Test(timeout = 4000)
    public void testNextFloat() {
        DummyGenerator generator = new DummyGenerator(0, (1 << 23) - 1);
        float min = generator.nextFloat();
        assertEquals(0.0f, min, 1e-7f);

        float max = generator.nextFloat();
        assertTrue(max < 1.0f);
        assertTrue(max > 0.99999f);
    }

    @Test(timeout = 4000)
    public void testNextDouble() {
        // High 26 bits = 0, Low 26 bits = 0 -> 0.0
        DummyGenerator generator = new DummyGenerator(0, 0);
        double val0 = generator.nextDouble();
        assertEquals(0.0, val0, 1e-15);

        // Max 26 bits each -> strictly less than 1.0
        generator.setSequence((1 << 26) - 1, (1 << 26) - 1);
        double valMax = generator.nextDouble();
        assertTrue(valMax < 1.0);
        assertTrue(valMax > 0.999999999999);
    }

    @Test(timeout = 4000)
    public void testNextGaussianPairAndCache() {
        DummyGenerator generator = new DummyGenerator();
        generator.setSeed(42L);

        // First call computes a pair and caches the second
        double g1 = generator.nextGaussian();
        // Second call retrieves cached value
        double g2 = generator.nextGaussian();
        assertNotEquals(g1, g2, 1e-9);

        // Third call computes a fresh pair
        double g3 = generator.nextGaussian();
        assertNotEquals(g2, g3, 1e-9);

        // clear() should flush the cached gaussian
        generator.setSeed(42L);
        double g1Ref = generator.nextGaussian();
        assertEquals(g1, g1Ref, 1e-15);

        generator.clear();
        // After clear, nextGaussian must compute a fresh pair instead of returning cached g2
        double gAfterClear = generator.nextGaussian();
        assertNotEquals(g2, gAfterClear, 1e-9);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testNextBytesZeroLength() {
        DummyGenerator generator = new DummyGenerator(0xFFFFFFFF);
        byte[] empty = new byte[0];
        generator.nextBytes(empty);
        assertEquals(0, empty.length);
    }

    @Test(timeout = 4000)
    public void testNextBytesLengths1To8() {
        // Checks all branches in nextBytes: lengths less than 4, exactly 4, and greater than 4
        for (int len = 1; len <= 8; len++) {
            DummyGenerator generator = new DummyGenerator(0x04030201, 0x08070605);
            byte[] bytes = new byte[len];
            generator.nextBytes(bytes);
            for (int j = 0; j < len; j++) {
                assertEquals((byte) (j + 1), bytes[j]);
            }
        }
    }

    @Test(timeout = 4000)
    public void testNextIntPowerOfTwo() {
        // Powers of 2 follow: (int) ((n * (long) next(31)) >> 31)
        DummyGenerator generator = new DummyGenerator(0, (1 << 31) - 1);
        int lower = generator.nextInt(16);
        assertEquals(0, lower);

        int upper = generator.nextInt(16);
        assertEquals(15, upper);

        // Test boundary power of 2: 2^30
        generator.setSequence((1 << 31) - 1);
        int pow2 = generator.nextInt(1 << 30);
        assertEquals((1 << 30) - 1, pow2);
    }

    @Test(timeout = 4000)
    public void testNextIntNonPowerOfTwoAndRejection() {
        // Rejection path condition: (bits - val + (n - 1) < 0) where val = bits % n
        // For n = 3: Integer.MAX_VALUE = 2147483647.
        // 2147483647 % 3 = 1.
        // bits - val + (n - 1) = 2147483647 - 1 + 2 = 2147483648 -> overflows to Integer.MIN_VALUE (< 0)!
        // This triggers the while rejection loop.
        int badBits = Integer.MAX_VALUE;
        int goodBits = 10;
        DummyGenerator generator = new DummyGenerator(badBits, goodBits);

        int result = generator.nextInt(3);
        // The rejected badBits is discarded; goodBits (10 % 3 = 1) is returned.
        assertEquals(1, result);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Math Distribution Clone & Serializable)
    // =========================================================================

    /**
     * Target Defect: BitsStreamGenerator must implement Serializable and preserve its state
     * (specifically `nextGaussian`) across Java serialization / clone operations.
     */
    @Test(timeout = 4000)
    public void testDefectBitsStreamGeneratorImplementsSerializable() {
        DummyGenerator generator = new DummyGenerator();
        assertTrue("BitsStreamGenerator must implement java.io.Serializable",
                   generator instanceof Serializable);
    }

    /**
     * Target Defect: Preserving nextGaussian cache across serialization.
     * When a single sample is drawn from a normal distribution, one gaussian is returned
     * and the other is cached in nextGaussian. Cloning/deserializing must maintain this
     * cached value so the cloned generator returns the exact same second value.
     */
    @Test(timeout = 4000)
    public void testDefectNextGaussianPreservedOnSerialization() throws Exception {
        DummyGenerator original = new DummyGenerator();
        original.setSeed(987654321L);

        // First call populates cache
        double firstVal = original.nextGaussian();
        assertFalse(Double.isNaN(firstVal));

        // Serialize original generator
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(original);
        oos.close();

        // Deserialize cloned generator
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        DummyGenerator restored = (DummyGenerator) ois.readObject();
        ois.close();

        // The second gaussian value MUST be identical between original and restored clone
        double originalSecond = original.nextGaussian();
        double restoredSecond = restored.nextGaussian();

        assertEquals("Cached nextGaussian state must be preserved after serialization",
                     originalSecond, restoredSecond, 0.0);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = NotStrictlyPositiveException.class, timeout = 4000)
    public void testNextIntZeroThrowsException() {
        DummyGenerator generator = new DummyGenerator(123);
        generator.nextInt(0);
    }

    @Test(expected = NotStrictlyPositiveException.class, timeout = 4000)
    public void testNextIntNegativeThrowsException() {
        DummyGenerator generator = new DummyGenerator(123);
        generator.nextInt(-5);
    }

    @Test(expected = NotStrictlyPositiveException.class, timeout = 4000)
    public void testNextIntMinNegativeThrowsException() {
        DummyGenerator generator = new DummyGenerator(123);
        generator.nextInt(Integer.MIN_VALUE);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testSetSeedVariants() {
        DummyGenerator generator = new DummyGenerator();
        generator.setSeed(100);
        assertEquals(100L, generator.seedValue);

        generator.setSeed(new int[]{200, 300});
        assertEquals(200L, generator.seedValue);

        generator.setSeed(new int[0]);
        assertEquals(0L, generator.seedValue);

        generator.setSeed(9999999999L);
        assertEquals(9999999999L, generator.seedValue);
    }
}