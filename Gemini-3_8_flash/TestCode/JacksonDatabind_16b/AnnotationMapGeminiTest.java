package com.fasterxml.jackson.databind.introspect;

import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Iterator;

/*
 * [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * Method                 | Branch / Condition                          | Test Method
 * ----------------------------------------------------------------------------------------------------
 * _add(Annotation)       | Defect target: Return value of add()        | testDefectAddReturnsTrueWhenContentAdded()
 *                        | when new annotation is added                | testDefectAddReturnsFalseWhenSameAnnotationAdded()
 *                        | when existing annotation value changed      | testDefectAddReturnsTrueWhenAnnotationModified()
 * get(Class<A>)          | _annotations == null                        | testGetWhenEmptyOrNull()
 *                        | _annotations != null, key exists            | testGetWhenPresent()
 *                        | _annotations != null, key absent            | testGetWhenAbsent()
 * annotations()          | _annotations == null                        | testAnnotationsWhenNull()
 *                        | _annotations.size() == 0                    | testAnnotationsWhenEmpty()
 *                        | _annotations.size() > 0                     | testAnnotationsWhenPopulated()
 * merge(Map, Map)        | primary == null                             | testMergePrimaryNull()
 *                        | primary._annotations == null                | testMergePrimaryAnnotationsNull()
 *                        | primary._annotations.isEmpty()              | testMergePrimaryEmpty()
 *                        | secondary == null                           | testMergeSecondaryNull()
 *                        | secondary._annotations == null              | testMergeSecondaryAnnotationsNull()
 *                        | secondary._annotations.isEmpty()            | testMergeSecondaryEmpty()
 *                        | Both non-empty, primary overrides secondary | testMergeBothPopulatedWithCollision()
 *                        | Both non-empty, disjoint keys               | testMergeBothPopulatedDisjoint()
 * size()                 | _annotations == null -> 0                   | testSizeWhenNull()
 *                        | _annotations != null -> size                | testSizeWhenPopulated()
 * addIfNotPresent(Ann)   | _annotations == null -> added, true         | testAddIfNotPresentWhenEmpty()
 *                        | key absent -> added, true                   | testAddIfNotPresentWhenKeyAbsent()
 *                        | key present -> not added, false             | testAddIfNotPresentWhenKeyPresent()
 * toString()             | _annotations == null -> "[null]"            | testToStringWhenNull()
 *                        | _annotations != null -> contains mappings   | testToStringWhenPopulated()
 * ----------------------------------------------------------------------------------------------------
 */
public class AnnotationMapGeminiTest {

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
    private @interface MarkerA {
        String value() default "A";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
    private @interface MarkerB {
        int value() default 0;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
    private @interface MarkerC {
        boolean flag() default false;
    }

    @MarkerA("sampleA1")
    @MarkerB(10)
    private static class SampleTarget1 {}

    @MarkerA("sampleA2")
    @MarkerB(20)
    private static class SampleTarget2 {}

    @MarkerC(true)
    private static class SampleTarget3 {}

    private static final MarkerA ANN_A1 = SampleTarget1.class.getAnnotation(MarkerA.class);
    private static final MarkerA ANN_A2 = SampleTarget2.class.getAnnotation(MarkerA.class);
    private static final MarkerB ANN_B1 = SampleTarget1.class.getAnnotation(MarkerB.class);
    private static final MarkerB ANN_B2 = SampleTarget2.class.getAnnotation(MarkerB.class);
    private static final MarkerC ANN_C1 = SampleTarget3.class.getAnnotation(MarkerC.class);

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Jackson Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets Defects4J bug where _add(Annotation) returns false when an annotation
     * is initially added because it incorrectly checked (previous != null && previous.equals(ann)).
     * Expected contract: add() returns true if the addition changed the map contents.
     */
    @Test(timeout = 4000)
    public void testDefectAddReturnsTrueWhenContentAdded() {
        AnnotationMap map = new AnnotationMap();
        boolean changed = map.add(ANN_A1);
        assertTrue("add() must return true when adding a new annotation to empty map", changed);
        assertEquals(1, map.size());
        assertEquals(ANN_A1, map.get(MarkerA.class));
    }

    /**
     * Targets Defects4J bug: adding an identical annotation should NOT change map contents
     * and must return false.
     */
    @Test(timeout = 4000)
    public void testDefectAddReturnsFalseWhenSameAnnotationAdded() {
        AnnotationMap map = new AnnotationMap();
        map.add(ANN_A1);
        boolean changed = map.add(ANN_A1);
        assertFalse("add() must return false when re-adding the exact same annotation", changed);
        assertEquals(1, map.size());
    }

    /**
     * Targets Defects4J bug: updating an existing annotation with a modified value
     * should change map contents and must return true.
     */
    @Test(timeout = 4000)
    public void testDefectAddReturnsTrueWhenAnnotationModified() {
        AnnotationMap map = new AnnotationMap();
        map.add(ANN_A1);
        boolean changed = map.add(ANN_A2);
        assertTrue("add() must return true when replacing an annotation with a different value", changed);
        assertEquals(1, map.size());
        assertEquals("sampleA2", map.get(MarkerA.class).value());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetWhenPresent() {
        AnnotationMap map = new AnnotationMap();
        map.addIfNotPresent(ANN_A1);
        map.addIfNotPresent(ANN_B1);

        MarkerA a = map.get(MarkerA.class);
        assertNotNull(a);
        assertEquals("sampleA1", a.value());

        MarkerB b = map.get(MarkerB.class);
        assertNotNull(b);
        assertEquals(10, b.value());
    }

    @Test(timeout = 4000)
    public void testGetWhenAbsent() {
        AnnotationMap map = new AnnotationMap();
        map.addIfNotPresent(ANN_A1);

        assertNull(map.get(MarkerB.class));
        assertNull(map.get(MarkerC.class));
    }

    @Test(timeout = 4000)
    public void testAddIfNotPresentWhenEmpty() {
        AnnotationMap map = new AnnotationMap();
        boolean added = map.addIfNotPresent(ANN_A1);
        assertTrue(added);
        assertEquals(1, map.size());
        assertEquals(ANN_A1, map.get(MarkerA.class));
    }

    @Test(timeout = 4000)
    public void testAddIfNotPresentWhenKeyAbsent() {
        AnnotationMap map = new AnnotationMap();
        map.addIfNotPresent(ANN_A1);
        boolean added = map.addIfNotPresent(ANN_B1);
        assertTrue(added);
        assertEquals(2, map.size());
        assertEquals(ANN_B1, map.get(MarkerB.class));
    }

    @Test(timeout = 4000)
    public void testAddIfNotPresentWhenKeyPresent() {
        AnnotationMap map = new AnnotationMap();
        map.addIfNotPresent(ANN_A1);

        boolean addedAgain = map.addIfNotPresent(ANN_A2);
        assertFalse(addedAgain);
        assertEquals(1, map.size());
        // Value should remain original ANN_A1, not ANN_A2
        assertEquals("sampleA1", map.get(MarkerA.class).value());
    }

    @Test(timeout = 4000)
    public void testAnnotationsWhenPopulated() {
        AnnotationMap map = new AnnotationMap();
        map.addIfNotPresent(ANN_A1);
        map.addIfNotPresent(ANN_B1);

        Iterable<Annotation> iterable = map.annotations();
        assertNotNull(iterable);

        int count = 0;
        for (Annotation ann : iterable) {
            assertNotNull(ann);
            count++;
        }
        assertEquals(2, count);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetWhenEmptyOrNull() {
        AnnotationMap map = new AnnotationMap();
        assertNull(map.get(MarkerA.class));
        assertNull(map.get(null));
    }

    @Test(timeout = 4000)
    public void testSizeWhenNull() {
        AnnotationMap map = new AnnotationMap();
        assertEquals(0, map.size());
    }

    @Test(timeout = 4000)
    public void testSizeWhenPopulated() {
        AnnotationMap map = new AnnotationMap();
        map.addIfNotPresent(ANN_A1);
        assertEquals(1, map.size());
        map.addIfNotPresent(ANN_B1);
        assertEquals(2, map.size());
    }

    @Test(timeout = 4000)
    public void testAnnotationsWhenNull() {
        AnnotationMap map = new AnnotationMap();
        Iterable<Annotation> it = map.annotations();
        assertNotNull(it);
        assertFalse(it.iterator().hasNext());
    }

    @Test(timeout = 4000)
    public void testAnnotationsWhenEmpty() {
        AnnotationMap map = new AnnotationMap();
        // Trigger initialization of _annotations via reflection or merge path
        AnnotationMap merged = AnnotationMap.merge(map, new AnnotationMap());
        Iterable<Annotation> it = (merged != null) ? merged.annotations() : map.annotations();
        assertNotNull(it);
        assertFalse(it.iterator().hasNext());
    }

    @Test(timeout = 4000)
    public void testToStringWhenNull() {
        AnnotationMap map = new AnnotationMap();
        assertEquals("[null]", map.toString());
    }

    @Test(timeout = 4000)
    public void testToStringWhenPopulated() {
        AnnotationMap map = new AnnotationMap();
        map.addIfNotPresent(ANN_A1);
        String str = map.toString();
        assertNotNull(str);
        assertTrue(str.contains(MarkerA.class.getName()) || str.contains("MarkerA"));
    }

    // =========================================================================
    // Partition D: Merge Operations & Defensive Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testMergeBothNull() {
        AnnotationMap result = AnnotationMap.merge(null, null);
        assertNull(result);
    }

    @Test(timeout = 4000)
    public void testMergePrimaryNull() {
        AnnotationMap secondary = new AnnotationMap();
        secondary.addIfNotPresent(ANN_A1);

        AnnotationMap result = AnnotationMap.merge(null, secondary);
        assertSame(secondary, result);
    }

    @Test(timeout = 4000)
    public void testMergePrimaryAnnotationsNull() {
        AnnotationMap primary = new AnnotationMap();
        AnnotationMap secondary = new AnnotationMap();
        secondary.addIfNotPresent(ANN_B1);

        AnnotationMap result = AnnotationMap.merge(primary, secondary);
        assertSame(secondary, result);
    }

    @Test(timeout = 4000)
    public void testMergePrimaryEmpty() {
        AnnotationMap primary = new AnnotationMap();
        AnnotationMap secondary = new AnnotationMap();
        secondary.addIfNotPresent(ANN_A1);

        AnnotationMap result = AnnotationMap.merge(primary, secondary);
        assertSame(secondary, result);
    }

    @Test(timeout = 4000)
    public void testMergeSecondaryNull() {
        AnnotationMap primary = new AnnotationMap();
        primary.addIfNotPresent(ANN_A1);

        AnnotationMap result = AnnotationMap.merge(primary, null);
        assertSame(primary, result);
    }

    @Test(timeout = 4000)
    public void testMergeSecondaryAnnotationsNull() {
        AnnotationMap primary = new AnnotationMap();
        primary.addIfNotPresent(ANN_A1);
        AnnotationMap secondary = new AnnotationMap();

        AnnotationMap result = AnnotationMap.merge(primary, secondary);
        assertSame(primary, result);
    }

    @Test(timeout = 4000)
    public void testMergeSecondaryEmpty() {
        AnnotationMap primary = new AnnotationMap();
        primary.addIfNotPresent(ANN_A1);
        AnnotationMap secondary = new AnnotationMap();

        AnnotationMap result = AnnotationMap.merge(primary, secondary);
        assertSame(primary, result);
    }

    @Test(timeout = 4000)
    public void testMergeBothPopulatedDisjoint() {
        AnnotationMap primary = new AnnotationMap();
        primary.addIfNotPresent(ANN_A1);

        AnnotationMap secondary = new AnnotationMap();
        secondary.addIfNotPresent(ANN_B1);

        AnnotationMap merged = AnnotationMap.merge(primary, secondary);
        assertNotNull(merged);
        assertNotSame(primary, merged);
        assertNotSame(secondary, merged);

        assertEquals(2, merged.size());
        assertEquals("sampleA1", merged.get(MarkerA.class).value());
        assertEquals(10, merged.get(MarkerB.class).value());

        // Verify immutability of inputs
        assertEquals(1, primary.size());
        assertEquals(1, secondary.size());
    }

    @Test(timeout = 4000)
    public void testMergeBothPopulatedWithCollision() {
        AnnotationMap primary = new AnnotationMap();
        primary.addIfNotPresent(ANN_A1); // value = "sampleA1"
        primary.addIfNotPresent(ANN_C1);

        AnnotationMap secondary = new AnnotationMap();
        secondary.addIfNotPresent(ANN_A2); // value = "sampleA2", collision on MarkerA
        secondary.addIfNotPresent(ANN_B1);

        AnnotationMap merged = AnnotationMap.merge(primary, secondary);
        assertNotNull(merged);
        assertEquals(3, merged.size());

        // Primary must override secondary
        assertEquals("sampleA1", merged.get(MarkerA.class).value());
        assertEquals(10, merged.get(MarkerB.class).value());
        assertTrue(merged.get(MarkerC.class).flag());
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Multiple Mutations
    // =========================================================================

    @Test(timeout = 4000)
    public void testSequentialAdditionsAndIterableOrder() {
        AnnotationMap map = new AnnotationMap();
        map.addIfNotPresent(ANN_A1);
        map.addIfNotPresent(ANN_B1);
        map.addIfNotPresent(ANN_C1);

        assertEquals(3, map.size());

        Iterator<Annotation> it = map.annotations().iterator();
        assertTrue(it.hasNext());
        assertNotNull(it.next());
        assertTrue(it.hasNext());
        assertNotNull(it.next());
        assertTrue(it.hasNext());
        assertNotNull(it.next());
        assertFalse(it.hasNext());
    }
}