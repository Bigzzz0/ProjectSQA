package com.fasterxml.jackson.databind.introspect;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.*;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Targeted Branches & Conditions:
 * 1. AnnotationMap.get(): null _annotations, non-null _annotations, existing key, missing key
 * 2. AnnotationMap.annotations(): null map, empty map, populated map
 * 3. AnnotationMap.merge(): 
 *    - primary null => return secondary
 *    - primary._annotations null => return secondary
 *    - primary._annotations empty => return secondary
 *    - secondary null => return primary
 *    - secondary._annotations null => return primary
 *    - secondary._annotations empty => return primary
 *    - both non-null/non-empty => merge with primary overriding secondary
 *    - Defect targeting: When primary has a different annotation for same type as secondary,
 *      primary should win (tests ordering and overriding behavior)
 * 4. AnnotationMap.size(): null map => 0, non-null map => correct count
 * 5. AnnotationMap.addIfNotPresent(): null _annotations => add; existing type => skip; new type => add
 * 6. AnnotationMap._add(): null _annotations => create map; return value when previous exists and equals/is different
 * 7. AnnotationMap.toString(): null _annotations => "[null]", non-null => map toString
 * 
 * Defect-Targeting Test: testMixinWithBundlesBehavior
 * - The Defects4J bug: MixinsWithBundlesTest fails because when merging annotations,
 *   secondary annotation overrides primary incorrectly instead of primary overriding secondary.
 *   Expected: annotation from primary should win over annotation from secondary for same type.
 *   Bug: secondary annotation wins over primary.
 * - This test creates two AnnotationMaps with different annotations of the same type,
 *   merges them, and asserts primary's value wins.
 */
public class AnnotationMapDeepseekTest {

    // Custom annotations for testing
    @Retention(RetentionPolicy.RUNTIME)
    public @interface TestAnnotation {
        String value() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface OtherAnnotation {
        String value() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface PriorityAnnotation {
        int priority() default 0;
    }

    // Test data
    private static final TestAnnotation ANN_BAR = new TestAnnotation() {
        @Override public Class<? extends Annotation> annotationType() { return TestAnnotation.class; }
        @Override public String value() { return "bar"; }
        @Override public int hashCode() { return "bar".hashCode(); }
        @Override public boolean equals(Object obj) {
            if (!(obj instanceof TestAnnotation)) return false;
            return "bar".equals(((TestAnnotation) obj).value());
        }
        @Override public String toString() { return "@TestAnnotation(value=bar)"; }
    };

    private static final TestAnnotation ANN_STUFF = new TestAnnotation() {
        @Override public Class<? extends Annotation> annotationType() { return TestAnnotation.class; }
        @Override public String value() { return "stuff"; }
        @Override public int hashCode() { return "stuff".hashCode(); }
        @Override public boolean equals(Object obj) {
            if (!(obj instanceof TestAnnotation)) return false;
            return "stuff".equals(((TestAnnotation) obj).value());
        }
        @Override public String toString() { return "@TestAnnotation(value=stuff)"; }
    };

    private static final OtherAnnotation ANN_OTHER = new OtherAnnotation() {
        @Override public Class<? extends Annotation> annotationType() { return OtherAnnotation.class; }
        @Override public String value() { return "other"; }
        @Override public int hashCode() { return "other".hashCode(); }
        @Override public boolean equals(Object obj) {
            if (!(obj instanceof OtherAnnotation)) return false;
            return "other".equals(((OtherAnnotation) obj).value());
        }
        @Override public String toString() { return "@OtherAnnotation(value=other)"; }
    };

    private static final PriorityAnnotation ANN_LOW = new PriorityAnnotation() {
        @Override public Class<? extends Annotation> annotationType() { return PriorityAnnotation.class; }
        @Override public int priority() { return 0; }
        @Override public int hashCode() { return 0; }
        @Override public boolean equals(Object obj) {
            if (!(obj instanceof PriorityAnnotation)) return false;
            return priority() == ((PriorityAnnotation) obj).priority();
        }
        @Override public String toString() { return "@PriorityAnnotation(priority=0)"; }
    };

    private static final PriorityAnnotation ANN_HIGH = new PriorityAnnotation() {
        @Override public Class<? extends Annotation> annotationType() { return PriorityAnnotation.class; }
        @Override public int priority() { return 100; }
        @Override public int hashCode() { return 100; }
        @Override public boolean equals(Object obj) {
            if (!(obj instanceof PriorityAnnotation)) return false;
            return priority() == ((PriorityAnnotation) obj).priority();
        }
        @Override public String toString() { return "@PriorityAnnotation(priority=100)"; }
    };

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testGetFromEmptyMap() {
        AnnotationMap map = new AnnotationMap();
        assertNull("Should return null for empty map", map.get(TestAnnotation.class));
        assertEquals("Size should be 0", 0, map.size());
    }

    @Test(timeout = 4000)
    public void testGetExistingAnnotation() {
        AnnotationMap map = new AnnotationMap();
        map.add(ANN_BAR);
        TestAnnotation result = map.get(TestAnnotation.class);
        assertNotNull("Should find annotation", result);
        assertEquals("Should return bar", "bar", result.value());
        assertEquals("Size should be 1", 1, map.size());
    }

    @Test(timeout = 4000)
    public void testGetNonExistentAnnotation() {
        AnnotationMap map = new AnnotationMap();
        map.add(ANN_BAR);
        assertNull("Should return null for non-existent type", map.get(OtherAnnotation.class));
    }

    @Test(timeout = 4000)
    public void testAnnotationsIterableNonEmpty() {
        AnnotationMap map = new AnnotationMap();
        map.add(ANN_BAR);
        map.add(ANN_OTHER);
        Iterable<Annotation> annotations = map.annotations();
        assertNotNull("annotations() should not return null", annotations);
        int count = 0;
        for (Annotation ann : annotations) {
            count++;
            assertNotNull("Annotation should not be null", ann);
        }
        assertEquals("Should have 2 annotations", 2, count);
    }

    @Test(timeout = 4000)
    public void testAnnotationsIterableEmpty() {
        AnnotationMap map = new AnnotationMap();
        Iterable<Annotation> annotations = map.annotations();
        assertNotNull("annotations() should return empty list", annotations);
        int count = 0;
        for (Annotation ann : annotations) {
            count++;
        }
        assertEquals("Should have 0 annotations", 0, count);
    }

    @Test(timeout = 4000)
    public void testAnnotationsIterableNullMap() {
        AnnotationMap map = new AnnotationMap();
        // Directly set _annotations to null via reflection to test that path
        // Use private field access - but we can also test by creating with no annotations
        // Actually the constructor leaves _annotations as null
        Iterable<Annotation> annotations = map.annotations();
        assertNotNull("annotations() should return empty list when _annotations is null", annotations);
        int count = 0;
        for (Annotation ann : annotations) {
            count++;
        }
        assertEquals("Should have 0 annotations", 0, count);
    }

    // ==================== Partition B: BVA & Extremes ====================

    @Test(timeout = 4000)
    public void testSizeNullMap() {
        AnnotationMap map = new AnnotationMap();
        assertEquals("Size of null map should be 0", 0, map.size());
    }

    @Test(timeout = 4000)
    public void testSizeAfterAddAndRemoveViaPut() {
        AnnotationMap map = new AnnotationMap();
        assertEquals("Initial size should be 0", 0, map.size());
        map.add(ANN_BAR);
        assertEquals("After adding 1, size=1", 1, map.size());
        map.add(ANN_OTHER);
        assertEquals("After adding 2, size=2", 2, map.size());
        // Add same type again - should overwrite but keep same count
        map.add(ANN_STUFF);
        assertEquals("After adding same type, size=2", 2, map.size());
    }

    @Test(timeout = 4000)
    public void testMergeWithNullPrimary() {
        AnnotationMap secondary = new AnnotationMap();
        secondary.add(ANN_BAR);
        AnnotationMap result = AnnotationMap.merge(null, secondary);
        assertNotNull("Result should not be null", result);
        assertEquals("Result should contain bar", "bar", result.get(TestAnnotation.class).value());
    }

    @Test(timeout = 4000)
    public void testMergeWithNullSecondary() {
        AnnotationMap primary = new AnnotationMap();
        primary.add(ANN_BAR);
        AnnotationMap result = AnnotationMap.merge(primary, null);
        assertNotNull("Result should not be null", result);
        assertEquals("Result should contain bar", "bar", result.get(TestAnnotation.class).value());
    }

    @Test(timeout = 4000)
    public void testMergeBothNull() {
        AnnotationMap result = AnnotationMap.merge(null, null);
        assertNull("Both null should return null", result);
    }

    @Test(timeout = 4000)
    public void testMergeEmptyPrimary() {
        AnnotationMap primary = new AnnotationMap();
        AnnotationMap secondary = new AnnotationMap();
        secondary.add(ANN_BAR);
        AnnotationMap result = AnnotationMap.merge(primary, secondary);
        assertNotNull("Result should not be null", result);
        assertEquals("Result should contain bar", "bar", result.get(TestAnnotation.class).value());
    }

    @Test(timeout = 4000)
    public void testMergeEmptySecondary() {
        AnnotationMap primary = new AnnotationMap();
        primary.add(ANN_BAR);
        AnnotationMap secondary = new AnnotationMap();
        AnnotationMap result = AnnotationMap.merge(primary, secondary);
        assertNotNull("Result should not be null", result);
        assertEquals("Result should contain bar", "bar", result.get(TestAnnotation.class).value());
    }

    @Test(timeout = 4000)
    public void testMergeBothNullMapsInside() {
        AnnotationMap primary = new AnnotationMap();
        AnnotationMap secondary = new AnnotationMap();
        // both have null _annotations
        AnnotationMap result = AnnotationMap.merge(primary, secondary);
        assertNull("Result should be null when both have null _annotations", result);
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    @Test(timeout = 4000)
    public void testMergePrimaryOverridesSecondary() {
        // This test targets the known Defects4J bug where secondary annotation
        // incorrectly overrides primary annotation during merge.
        // Expected behavior: primary should override secondary.
        AnnotationMap primary = new AnnotationMap();
        AnnotationMap secondary = new AnnotationMap();
        
        // Both maps have an annotation of the same type (TestAnnotation) but with different values
        primary.add(ANN_BAR);
        secondary.add(ANN_STUFF);
        
        // Primary should override secondary in the merge
        AnnotationMap result = AnnotationMap.merge(primary, secondary);
        
        // The result should contain the annotation from primary (bar)
        TestAnnotation resultAnn = result.get(TestAnnotation.class);
        assertNotNull("Result should contain TestAnnotation", resultAnn);
        assertEquals("Primary annotation should override secondary - expected 'bar'", 
                     "bar", resultAnn.value());
    }

    @Test(timeout = 4000)
    public void testMergePrimaryOverridesSecondaryDifferentTypes() {
        // More complex scenario: multiple annotations, primary should override 
        // secondary only for same annotation types
        AnnotationMap primary = new AnnotationMap();
        AnnotationMap secondary = new AnnotationMap();
        
        primary.add(ANN_BAR);      // TestAnnotation with bar
        primary.add(ANN_LOW);      // PriorityAnnotation with 0
        secondary.add(ANN_STUFF);  // TestAnnotation with stuff
        secondary.add(ANN_OTHER);  // OtherAnnotation
        
        AnnotationMap result = AnnotationMap.merge(primary, secondary);
        
        // TestAnnotation should come from primary (bar)
        assertEquals("TestAnnotation should be from primary", "bar", 
                     result.get(TestAnnotation.class).value());
        // PriorityAnnotation should come from primary (low)
        assertEquals("PriorityAnnotation should be from primary", 0, 
                     result.get(PriorityAnnotation.class).priority());
        // OtherAnnotation should come from secondary (since primary doesn't have it)
        assertEquals("OtherAnnotation should be from secondary", "other", 
                     result.get(OtherAnnotation.class).value());
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000)
    public void testAddIfNotPresentNewAnnotation() {
        AnnotationMap map = new AnnotationMap();
        assertTrue("Should add new annotation", map.addIfNotPresent(ANN_BAR));
        assertEquals("Annotation should be present", "bar", map.get(TestAnnotation.class).value());
    }

    @Test(timeout = 4000)
    public void testAddIfNotPresentExistingAnnotation() {
        AnnotationMap map = new AnnotationMap();
        map.add(ANN_BAR);
        TestAnnotation differentAnn = new TestAnnotation() {
            @Override public Class<? extends Annotation> annotationType() { return TestAnnotation.class; }
            @Override public String value() { return "different"; }
            @Override public int hashCode() { return "different".hashCode(); }
            @Override public boolean equals(Object obj) { return false; }
            @Override public String toString() { return "@TestAnnotation(value=different)"; }
        };
        assertFalse("Should not add if type already exists", map.addIfNotPresent(differentAnn));
        assertEquals("Original annotation should remain", "bar", map.get(TestAnnotation.class).value());
    }

    @Test(timeout = 4000)
    public void testAddIfNotPresentNullMap() {
        AnnotationMap map = new AnnotationMap();
        // _annotations is null initially
        assertTrue("Should add when _annotations is null", map.addIfNotPresent(ANN_BAR));
        assertEquals("Annotation should be present", "bar", map.get(TestAnnotation.class).value());
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testToStringNullMap() {
        AnnotationMap map = new AnnotationMap();
        assertEquals("toString for null map", "[null]", map.toString());
    }

    @Test(timeout = 4000)
    public void testToStringNonEmptyMap() {
        AnnotationMap map = new AnnotationMap();
        map.add(ANN_BAR);
        String str = map.toString();
        assertNotNull("toString should not be null", str);
        assertTrue("toString should contain annotation type", 
                   str.contains("TestAnnotation") || str.contains("@TestAnnotation"));
    }

    @Test(timeout = 4000)
    public void testMultipleAddOverwrite() {
        AnnotationMap map = new AnnotationMap();
        map.add(ANN_BAR);
        // Overwrite with same type
        map.add(ANN_STUFF);
        TestAnnotation result = map.get(TestAnnotation.class);
        assertEquals("Should be overwritten with stuff", "stuff", result.value());
        assertEquals("Size should remain 1", 1, map.size());
    }

    @Test(timeout = 4000)
    public void testChainOfOperations() {
        AnnotationMap map = new AnnotationMap();
        assertTrue("addIfNotPresent should succeed", map.addIfNotPresent(ANN_BAR));
        assertFalse("addIfNotPresent should fail for existing type", map.addIfNotPresent(ANN_STUFF));
        assertTrue("add should succeed", map.add(ANN_OTHER));
        assertEquals("Should have 2 annotations", 2, map.size());
        assertNotNull("Should get TestAnnotation", map.get(TestAnnotation.class));
        assertNotNull("Should get OtherAnnotation", map.get(OtherAnnotation.class));
    }

    @Test(timeout = 4000)
    public void testMergeResultIndependent() {
        // Verify that merging returns a NEW AnnotationMap independent from inputs
        AnnotationMap primary = new AnnotationMap();
        AnnotationMap secondary = new AnnotationMap();
        primary.add(ANN_BAR);
        secondary.add(ANN_OTHER);
        
        AnnotationMap result = AnnotationMap.merge(primary, secondary);
        
        // Modify original maps
        primary.add(ANN_STUFF);
        secondary.add(ANN_LOW);
        
        // Result should not have been affected
        assertEquals("Result should still have bar", "bar", result.get(TestAnnotation.class).value());
        assertEquals("Result should still have other", "other", result.get(OtherAnnotation.class).value());
        assertNull("Result should not have low priority", result.get(PriorityAnnotation.class));
    }
}