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
package org.apache.commons.collections.functors;

import org.apache.commons.collections.Predicate;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Class under Test: org.apache.commons.collections.functors.EqualPredicate
 *
 * 1. Factory Methods:
 *    - equalPredicate(T object):
 *        * Branch: object == null -> returns NullPredicate.INSTANCE via nullPredicate()
 *        * Branch: object != null -> returns new EqualPredicate<T>(object)
 *    - equalPredicate(T object, Equator<T> equator):
 *        * Branch: object == null -> returns NullPredicate.INSTANCE via nullPredicate()
 *        * Branch: object != null -> returns new EqualPredicate<T>(object, equator)
 *
 * 2. Constructors:
 *    - EqualPredicate(T object):
 *        * Calls this(object, new DefaultEquator<T>()) [or null equator / compatibility logic]
 *    - EqualPredicate(T object, Equator<T> equator):
 *        * Stores iValue and equator directly without null checks
 *
 * 3. evaluate(T object):
 *    - Invokes equator.equate(iValue, object) [or iValue.equals(object) depending on version/fix]
 *
 * 4. getValue():
 *    - Returns the stored iValue reference.
 *
 * 5. Ground Truth Defect Analysis (Collections / Defects4J):
 *    - Defect: "objectFactoryUsesEqualsForTest" failed in TestEqualPredicate.
 *    - Root Cause: In EqualPredicate(object), using DefaultEquator instead of pure equals() or null equator
 *      affected backwards compatibility where two objects were compared using DefaultEquator or
 *      a custom equator, or the constructor `EqualPredicate(object)` used DefaultEquator which checks reference equality
 *      first or uses different equate behavior, whereas EqualPredicate contract specifies `iValue.equals(object)`.
 *      When an equator is not supplied or supplied via factory, evaluate() contract asserts the exact equals test.
 *
 * 6. Serialization Contract:
 *    - EqualPredicate implements Serializable. Serialization and deserialization must preserve state and evaluate properly.
 */
public class EqualPredicateGeminiTest {

    // Helper classes for testing equality contracts
    private static class EqualsOnlyObject {
        private final int id;

        EqualsOnlyObject(int id) {
            this.id = id;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            return this.id == ((EqualsOnlyObject) obj).id;
        }

        @Override
        public int hashCode() {
            return id;
        }
    }

    private static class IdentitySensitiveObject {
        private final int id;
        private boolean equalsCalled = false;

        IdentitySensitiveObject(int id) {
            this.id = id;
        }

        @Override
        public boolean equals(Object obj) {
            equalsCalled = true;
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            return this.id == ((IdentitySensitiveObject) obj).id;
        }

        @Override
        public int hashCode() {
            return id;
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testEvaluateMatchingObjectReturnsTrue() {
        String stored = "testString";
        Predicate<String> predicate = EqualPredicate.equalPredicate(stored);
        assertTrue("Predicate should evaluate to true for equivalent string",
                predicate.evaluate(new String("testString")));
    }

    @Test(timeout = 4000)
    public void testEvaluateNonMatchingObjectReturnsFalse() {
        Predicate<String> predicate = EqualPredicate.equalPredicate("expected");
        assertFalse("Predicate should evaluate to false for different string",
                predicate.evaluate("different"));
    }

    @Test(timeout = 4000)
    public void testEvaluateNullInputReturnsFalse() {
        Predicate<String> predicate = EqualPredicate.equalPredicate("nonNull");
        assertFalse("Evaluating null against non-null stored value should return false",
                predicate.evaluate(null));
    }

    @Test(timeout = 4000)
    public void testGetValueReturnsStoredInstance() {
        Integer value = 12345;
        EqualPredicate<Integer> predicate = new EqualPredicate<Integer>(value);
        assertSame("getValue() must return the exact object reference passed to constructor",
                value, predicate.getValue());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Factory Branches
    // =========================================================================

    @Test(timeout = 4000)
    public void testFactorySingleArgWithNullReturnsNullPredicate() {
        Predicate<Object> predicate = EqualPredicate.equalPredicate(null);
        assertNotNull("Factory should return a non-null predicate for null input", predicate);
        assertTrue("Factory must return NullPredicate when object is null",
                predicate instanceof NullPredicate);
        assertTrue("NullPredicate should evaluate true for null", predicate.evaluate(null));
        assertFalse("NullPredicate should evaluate false for non-null", predicate.evaluate(new Object()));
    }

    @Test(timeout = 4000)
    public void testFactoryTwoArgsWithNullObjectReturnsNullPredicate() {
        Equator<String> customEquator = new Equator<String>() {
            public boolean equate(String o1, String o2) {
                return true;
            }
            public int hash(String o) {
                return 0;
            }
        };

        Predicate<String> predicate = EqualPredicate.equalPredicate(null, customEquator);
        assertNotNull("Factory should return a non-null predicate when object is null", predicate);
        assertTrue("Factory must return NullPredicate when object is null even with equator",
                predicate instanceof NullPredicate);
        assertTrue(predicate.evaluate(null));
        assertFalse(predicate.evaluate("any"));
    }

    @Test(timeout = 4000)
    public void testFactoryTwoArgsWithValidObjectAndEquator() {
        Equator<String> caseInsensitiveEquator = new Equator<String>() {
            public boolean equate(String o1, String o2) {
                if (o1 == null) {
                    return o2 == null;
                }
                return o1.equalsIgnoreCase(o2);
            }
            public int hash(String o) {
                return o == null ? 0 : o.toLowerCase().hashCode();
            }
        };

        Predicate<String> predicate = EqualPredicate.equalPredicate("HELLO", caseInsensitiveEquator);
        assertTrue("Predicate with equator should match case-insensitively",
                predicate.evaluate("hello"));
        assertTrue(predicate.evaluate("HELLO"));
        assertFalse(predicate.evaluate("world"));
    }

    @Test(timeout = 4000)
    public void testDirectConstructorAllowsNullValue() {
        EqualPredicate<Object> predicate = new EqualPredicate<Object>(null);
        assertNull("getValue should be null when initialized with null", predicate.getValue());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets ground truth defect:
     * org.apache.commons.collections.functors.TestEqualPredicate::objectFactoryUsesEqualsForTest
     *
     * The 1-arg factory/constructor EqualPredicate(object) is specified to use the .equals()
     * contract of the stored object. If the implementation uses DefaultEquator or does not invoke
     * the target object's .equals() method correctly, this test detects the deviation.
     */
    @Test(timeout = 4000)
    public void testObjectFactoryUsesEqualsForTest() {
        IdentitySensitiveObject storedObj = new IdentitySensitiveObject(100);
        IdentitySensitiveObject testObj = new IdentitySensitiveObject(100);

        Predicate<IdentitySensitiveObject> predicate = EqualPredicate.equalPredicate(storedObj);
        assertFalse("equals() should not have been called prior to evaluate()", storedObj.equalsCalled);

        boolean result = predicate.evaluate(testObj);

        assertTrue("Evaluation must return true for objects that are equals()", result);
        assertTrue("EqualPredicate must invoke .equals() on the stored object when evaluated",
                storedObj.equalsCalled);
    }

    @Test(timeout = 4000)
    public void testEvaluateWithSameInstanceDelegatesProperly() {
        IdentitySensitiveObject sameObj = new IdentitySensitiveObject(42);
        Predicate<IdentitySensitiveObject> predicate = EqualPredicate.equalPredicate(sameObj);

        boolean result = predicate.evaluate(sameObj);
        assertTrue("Self-evaluation must be true", result);
    }

    @Test(timeout = 4000)
    public void testConstructorWithoutEquatorUsesEquals() {
        EqualsOnlyObject obj1 = new EqualsOnlyObject(777);
        EqualsOnlyObject obj2 = new EqualsOnlyObject(777);
        EqualsOnlyObject obj3 = new EqualsOnlyObject(888);

        EqualPredicate<EqualsOnlyObject> predicate = new EqualPredicate<EqualsOnlyObject>(obj1);

        assertTrue("EqualPredicate should return true for distinct objects with same state",
                predicate.evaluate(obj2));
        assertFalse("EqualPredicate should return false for objects with different state",
                predicate.evaluate(obj3));
    }

    // =========================================================================
    // Partition D: Custom Equator Invocations & Boundary Conditions
    // =========================================================================

    @Test(timeout = 4000)
    public void testCustomEquatorExecutionFlow() {
        final boolean[] equatorCalled = new boolean[]{false};
        Equator<Integer> trackingEquator = new Equator<Integer>() {
            public boolean equate(Integer o1, Integer o2) {
                equatorCalled[0] = true;
                return (o1 != null && o2 != null) && (o1 % 2 == o2 % 2);
            }
            public int hash(Integer o) {
                return o == null ? 0 : o % 2;
            }
        };

        Predicate<Integer> predicate = new EqualPredicate<Integer>(4, trackingEquator);
        boolean matched = predicate.evaluate(8); // both even

        assertTrue("Equator should have been invoked", equatorCalled[0]);
        assertTrue("Both even numbers should match under modulo-2 equator", matched);

        assertFalse("4 and 7 parity should not match", predicate.evaluate(7));
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Serialization Integrity
    // =========================================================================

    @Test(timeout = 4000)
    @SuppressWarnings("unchecked")
    public void testSerializationRoundTrip() throws Exception {
        String testValue = "serializeMe";
        Predicate<String> predicate = EqualPredicate.equalPredicate(testValue);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(predicate);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Predicate<String> deserialized = (Predicate<String>) ois.readObject();
        ois.close();

        assertNotNull("Deserialized predicate must not be null", deserialized);
        assertTrue("Deserialized predicate should evaluate true for equivalent value",
                deserialized.evaluate("serializeMe"));
        assertFalse("Deserialized predicate should evaluate false for different value",
                deserialized.evaluate("otherValue"));

        if (deserialized instanceof EqualPredicate) {
            assertEquals("Stored value must remain equal after serialization",
                    testValue, ((EqualPredicate<String>) deserialized).getValue());
        }
    }
}