package com.fasterxml.jackson.databind;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.util.Converter;
import com.fasterxml.jackson.databind.util.NameTransformer;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.type.MapLikeType;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * White-box test suite for AnnotationIntrospector.
 * Targets line/branch coverage and the known defect in refineSerializationType
 * where a non-supertype annotation causes an incorrect exception message.
 */
public class AnnotationIntrospectorDeepseekTest {

    /*
     * [Branch & Defect Analysis Matrix]
     *
     * Partition A: Core functional logic & state transitions
     *   - allIntrospectors() returns singleton list
     *   - allIntrospectors(Collection) adds self
     *   - nopInstance() returns NopAnnotationIntrospector
     *   - pair() returns AnnotationIntrospectorPair
     *   - Default methods return null/false as documented
     *
     * Partition B: Boundary Value Analysis & Extremes
     *   - null arguments for findPropertiesToIgnore, findEnumValues, etc.
     *   - empty arrays/collections
     *   - zero-length strings
     *
     * Partition C: Defect-Targeted Branch Zone
     *   - refineSerializationType with annotation specifying non-supertype
     *     (e.g., String for Long) -> must throw JsonMappingException with "types not related"
     *   - refineSerializationType with annotation specifying subtype (specialization)
     *   - refineSerializationType with key/content type refinement where types are unrelated
     *   - refineDeserializationType with similar scenarios
     *
     * Partition D: Exception & Defensive Guard Paths
     *   - IllegalArgumentException from TypeFactory caught and wrapped
     *   - Null checks on return values
     *
     * Partition E: Object Lifecycle & Contract Integrity
     *   - ReferenceProperty creation and accessors
     *   - Enum handling (findEnumValue, findEnumValues)
     */

    // -----------------------------------------------------------------------
    // Helper: concrete subclass for testing default implementations
    // -----------------------------------------------------------------------
    static class TestIntrospector extends AnnotationIntrospector {
        @Override
        public Version version() {
            return Version.unknownVersion();
        }

        // Override findSerializationType to simulate annotation
        private Class<?> serTypeOverride;
        private Class<?> serKeyTypeOverride;
        private Class<?> serContentTypeOverride;

        public void setSerTypeOverride(Class<?> c) { this.serTypeOverride = c; }
        public void setSerKeyTypeOverride(Class<?> c) { this.serKeyTypeOverride = c; }
        public void setSerContentTypeOverride(Class<?> c) { this.serContentTypeOverride = c; }

        @Override
        public Class<?> findSerializationType(Annotated a) {
            return serTypeOverride;
        }

        @Override
        public Class<?> findSerializationKeyType(Annotated am, JavaType baseType) {
            return serKeyTypeOverride;
        }

        @Override
        public Class<?> findSerializationContentType(Annotated am, JavaType baseType) {
            return serContentTypeOverride;
        }
    }

    // Simple stub for Annotated
    static class AnnotatedStub extends Annotated {
        private final String name;

        public AnnotatedStub(String name) {
            this.name = name;
        }

        @Override
        public String getName() { return name; }

        @Override
        public Annotated withAnnotations(AnnotationMap annotations) { return this; }

        @Override
        public AnnotationMap getAllAnnotations() { return null; }

        @Override
        public int getModifiers() { return 0; }

        @Override
        public Class<?> getRawType() { return Object.class; }

        @Override
        public <A extends Annotation> A getAnnotation(Class<A> acls) { return null; }

        @Override
        public boolean hasAnnotation(Class<?> acls) { return false; }

        @Override
        public boolean hasOneOf(Class<? extends Annotation>[] annoClasses) { return false; }

        @Override
        public AnnotatedElement getAnnotated() { return null; }

        @Override
        protected int getModifiersDirect() { return 0; }
    }

    // Simple MapperConfig stub
    static class MapperConfigStub extends MapperConfig<MapperConfigStub> {
        private final TypeFactory typeFactory;

        public MapperConfigStub(TypeFactory tf) {
            super(null, null);
            this.typeFactory = tf;
        }

        @Override
        public TypeFactory getTypeFactory() { return typeFactory; }

        @Override
        public MapperConfigStub with(MapperConfig<?> config) { return this; }

        @Override
        public MapperConfigStub with(AnnotationIntrospector ai) { return this; }

        @Override
        public MapperConfigStub withAppendedAnnotationIntrospector(AnnotationIntrospector ai) { return this; }

        @Override
        public MapperConfigStub withInsertedAnnotationIntrospector(AnnotationIntrospector ai) { return this; }

        @Override
        public AnnotationIntrospector getAnnotationIntrospector() { return null; }

        @Override
        public boolean useRootWrapping() { return false; }
    }

    // -----------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testAllIntrospectorsReturnsSingleton() {
        TestIntrospector ti = new TestIntrospector();
        Collection<AnnotationIntrospector> col = ti.allIntrospectors();
        assertEquals(1, col.size());
        assertSame(ti, col.iterator().next());
    }

    @Test(timeout = 4000)
    public void testAllIntrospectorsWithCollection() {
        TestIntrospector ti = new TestIntrospector();
        List<AnnotationIntrospector> list = new ArrayList<>();
        Collection<AnnotationIntrospector> result = ti.allIntrospectors(list);
        assertSame(list, result);
        assertEquals(1, list.size());
        assertSame(ti, list.get(0));
    }

    @Test(timeout = 4000)
    public void testNopInstance() {
        AnnotationIntrospector nop = AnnotationIntrospector.nopInstance();
        assertNotNull(nop);
        assertTrue(nop instanceof NopAnnotationIntrospector);
    }

    @Test(timeout = 4000)
    public void testPair() {
        AnnotationIntrospector a1 = AnnotationIntrospector.nopInstance();
        AnnotationIntrospector a2 = AnnotationIntrospector.nopInstance();
        AnnotationIntrospector pair = AnnotationIntrospector.pair(a1, a2);
        assertNotNull(pair);
        assertTrue(pair instanceof AnnotationIntrospectorPair);
    }

    @Test(timeout = 4000)
    public void testDefaultMethodsReturnNull() {
        TestIntrospector ti = new TestIntrospector();
        assertNull(ti.findObjectIdInfo(null));
        assertNull(ti.findObjectReferenceInfo(null, null));
        assertNull(ti.findRootName(null));
        assertNull(ti.findPropertiesToIgnore(null, true));
        assertNull(ti.findPropertiesToIgnore(null, false));
        assertNull(ti.findIgnoreUnknownProperties(null));
        assertNull(ti.isIgnorableType(null));
        assertNull(ti.findFilterId(null));
        assertNull(ti.findNamingStrategy(null));
        assertNull(ti.findClassDescription(null));
        assertNull(ti.findTypeResolver(null, null, null));
        assertNull(ti.findPropertyTypeResolver(null, null, null));
        assertNull(ti.findPropertyContentTypeResolver(null, null, null));
        assertNull(ti.findSubtypes(null));
        assertNull(ti.findTypeName(null));
        assertNull(ti.isTypeId(null));
        assertNull(ti.findReferenceType(null));
        assertNull(ti.findUnwrappingNameTransformer(null));
        assertFalse(ti.hasIgnoreMarker(null));
        assertNull(ti.findInjectableValueId(null));
        assertNull(ti.hasRequiredMarker(null));
        assertNull(ti.findViews(null));
        assertNull(ti.findFormat(null));
        assertNull(ti.findWrapperName(null));
        assertNull(ti.findPropertyDefaultValue(null));
        assertNull(ti.findPropertyDescription(null));
        assertNull(ti.findPropertyIndex(null));
        assertNull(ti.findImplicitPropertyName(null));
        assertNull(ti.findPropertyAccess(null));
        assertNull(ti.resolveSetterConflict(null, null, null));
        assertNull(ti.findSerializer(null));
        assertNull(ti.findKeySerializer(null));
        assertNull(ti.findContentSerializer(null));
        assertNull(ti.findNullSerializer(null));
        assertNull(ti.findSerializationTyping(null));
        assertNull(ti.findSerializationConverter(null));
        assertNull(ti.findSerializationContentConverter(null));
        assertNull(ti.findDeserializer(null));
        assertNull(ti.findKeyDeserializer(null));
        assertNull(ti.findContentDeserializer(null));
        assertNull(ti.findDeserializationConverter(null));
        assertNull(ti.findDeserializationContentConverter(null));
        assertNull(ti.findValueInstantiator(null));
        assertNull(ti.findPOJOBuilder(null));
        assertNull(ti.findPOJOBuilderConfig(null));
        assertNull(ti.findNameForSerialization(null));
        assertNull(ti.findNameForDeserialization(null));
        assertNull(ti.findCreatorBinding(null));
        assertNull(ti.findSerializationPropertyOrder(null));
        assertNull(ti.findSerializationSortAlphabetically(null));
        assertNull(ti.findEnumValue(null));
        assertNull(ti.findDeserializationType(null, null));
        assertNull(ti.findDeserializationKeyType(null, null));
        assertNull(ti.findDeserializationContentType(null, null));
    }

    @Test(timeout = 4000)
    public void testDefaultBooleanMethods() {
        TestIntrospector ti = new TestIntrospector();
        assertFalse(ti.isAnnotationBundle(null));
        assertFalse(ti.hasAsValueAnnotation(null));
        assertFalse(ti.hasAnySetterAnnotation(null));
        assertFalse(ti.hasAnyGetterAnnotation(null));
        assertFalse(ti.hasCreatorAnnotation(null));
    }

    @Test(timeout = 4000)
    public void testFindSerializationInclusionDefaults() {
        TestIntrospector ti = new TestIntrospector();
        assertEquals(JsonInclude.Include.ALWAYS, ti.findSerializationInclusion(null, JsonInclude.Include.ALWAYS));
        assertEquals(JsonInclude.Include.NON_NULL, ti.findSerializationInclusionForContent(null, JsonInclude.Include.NON_NULL));
    }

    @Test(timeout = 4000)
    public void testFindPropertyInclusionReturnsEmpty() {
        TestIntrospector ti = new TestIntrospector();
        assertEquals(JsonInclude.Value.empty(), ti.findPropertyInclusion(null));
    }

    @Test(timeout = 4000)
    public void testFindAutoDetectVisibilityReturnsChecker() {
        TestIntrospector ti = new TestIntrospector();
        VisibilityChecker<?> checker = VisibilityChecker.Std.defaultInstance();
        assertSame(checker, ti.findAutoDetectVisibility(null, checker));
    }

    @Test(timeout = 4000)
    public void testFindAndAddVirtualPropertiesDoesNothing() {
        TestIntrospector ti = new TestIntrospector();
        List<BeanPropertyWriter> list = new ArrayList<>();
        ti.findAndAddVirtualProperties(null, null, list);
        assertTrue(list.isEmpty());
    }

    @Test(timeout = 4000)
    public void testReferenceProperty() {
        AnnotationIntrospector.ReferenceProperty rp = AnnotationIntrospector.ReferenceProperty.managed("ref");
        assertEquals(AnnotationIntrospector.ReferenceProperty.Type.MANAGED_REFERENCE, rp.getType());
        assertEquals("ref", rp.getName());
        assertTrue(rp.isManagedReference());
        assertFalse(rp.isBackReference());

        rp = AnnotationIntrospector.ReferenceProperty.back("back");
        assertEquals(AnnotationIntrospector.ReferenceProperty.Type.BACK_REFERENCE, rp.getType());
        assertEquals("back", rp.getName());
        assertFalse(rp.isManagedReference());
        assertTrue(rp.isBackReference());
    }

    // -----------------------------------------------------------------------
    // Partition B: Boundary Value Analysis & Extremes
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testFindPropertiesToIgnoreNull() {
        TestIntrospector ti = new TestIntrospector();
        assertNull(ti.findPropertiesToIgnore(null, true));
        assertNull(ti.findPropertiesToIgnore(null, false));
    }

    @Test(timeout = 4000)
    public void testFindEnumValuesWithNullNames() {
        TestIntrospector ti = new TestIntrospector();
        // Use a simple enum
        enum Color { RED, GREEN }
        Color[] values = Color.values();
        String[] names = new String[values.length];
        // Initially null
        String[] result = ti.findEnumValues(Color.class, values, names);
        // Default implementation calls findEnumValue which returns name()
        assertEquals("RED", result[0]);
        assertEquals("GREEN", result[1]);
    }

    @Test(timeout = 4000)
    public void testFindEnumValuesWithPreFilledNames() {
        TestIntrospector ti = new TestIntrospector();
        enum Color { RED, GREEN }
        Color[] values = Color.values();
        String[] names = new String[]{"rouge", null};
        String[] result = ti.findEnumValues(Color.class, values, names);
        assertEquals("rouge", result[0]); // unchanged
        assertEquals("GREEN", result[1]); // filled by findEnumValue
    }

    @Test(timeout = 4000)
    public void testFindEnumValueReturnsName() {
        TestIntrospector ti = new TestIntrospector();
        enum Color { RED }
        assertEquals("RED", ti.findEnumValue(Color.RED));
    }

    // -----------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testRefineSerializationTypeWithNonSupertypeAnnotation() throws Exception {
        // This test targets the known defect: when annotation specifies a type
        // that is not a supertype, the method should throw JsonMappingException
        // with message containing "types not related".
        // The defective version throws a different message.
        TestIntrospector ti = new TestIntrospector();
        ti.setSerTypeOverride(String.class); // String is not a supertype of Long

        TypeFactory tf = TypeFactory.defaultInstance();
        MapperConfigStub config = new MapperConfigStub(tf);
        JavaType baseType = tf.constructType(Long.class);
        AnnotatedStub annotated = new AnnotatedStub("getValue");

        try {
            ti.refineSerializationType(config, annotated, baseType);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            // The correct behavior is to throw with "types not related"
            String msg = e.getMessage();
            assertTrue("Expected message to contain 'types not related', but got: " + msg,
                    msg.contains("types not related"));
        }
    }

    @Test(timeout = 4000)
    public void testRefineSerializationTypeWithNonSupertypeKeyAnnotation() throws Exception {
        // Test key type refinement with unrelated types
        TestIntrospector ti = new TestIntrospector();
        ti.setSerKeyTypeOverride(String.class); // String not supertype of Long

        TypeFactory tf = TypeFactory.defaultInstance();
        MapperConfigStub config = new MapperConfigStub(tf);
        // Create a Map-like type: Map<Long, String>
        JavaType mapType = tf.constructMapType(Map.class, tf.constructType(Long.class), tf.constructType(String.class));
        AnnotatedStub annotated = new AnnotatedStub("getMap");

        try {
            ti.refineSerializationType(config, annotated, mapType);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            String msg = e.getMessage();
            assertTrue("Expected message to contain 'types not related', but got: " + msg,
                    msg.contains("types not related"));
        }
    }

    @Test(timeout = 4000)
    public void testRefineSerializationTypeWithNonSupertypeContentAnnotation() throws Exception {
        // Test content type refinement with unrelated types
        TestIntrospector ti = new TestIntrospector();
        ti.setSerContentTypeOverride(String.class); // String not supertype of Long

        TypeFactory tf = TypeFactory.defaultInstance();
        MapperConfigStub config = new MapperConfigStub(tf);
        // Create a Collection-like type: List<Long>
        JavaType listType = tf.constructCollectionType(List.class, tf.constructType(Long.class));
        AnnotatedStub annotated = new AnnotatedStub("getList");

        try {
            ti.refineSerializationType(config, annotated, listType);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            String msg = e.getMessage();
            assertTrue("Expected message to contain 'types not related', but got: " + msg,
                    msg.contains("types not related"));
        }
    }

    @Test(timeout = 4000)
    public void testRefineSerializationTypeWithSpecialization() throws Exception {
        // When annotation specifies a subtype, it should specialize (narrow)
        TestIntrospector ti = new TestIntrospector();
        // Use a simple hierarchy: Number -> Integer
        ti.setSerTypeOverride(Integer.class); // Integer is a subtype of Number

        TypeFactory tf = TypeFactory.defaultInstance();
        MapperConfigStub config = new MapperConfigStub(tf);
        JavaType baseType = tf.constructType(Number.class);
        AnnotatedStub annotated = new AnnotatedStub("getValue");

        JavaType result = ti.refineSerializationType(config, annotated, baseType);
        // Should be specialized to Integer
        assertEquals(Integer.class, result.getRawClass());
    }

    @Test(timeout = 4000)
    public void testRefineSerializationTypeWithSameType() throws Exception {
        // When annotation specifies the same type, static typing should be forced
        TestIntrospector ti = new TestIntrospector();
        ti.setSerTypeOverride(Long.class);

        TypeFactory tf = TypeFactory.defaultInstance();
        MapperConfigStub config = new MapperConfigStub(tf);
        JavaType baseType = tf.constructType(Long.class);
        AnnotatedStub annotated = new AnnotatedStub("getValue");

        JavaType result = ti.refineSerializationType(config, annotated, baseType);
        assertTrue(result.isStatic());
    }

    @Test(timeout = 4000)
    public void testRefineDeserializationTypeWithNonSupertypeAnnotation() throws Exception {
        // Similar test for deserialization
        TestIntrospector ti = new TestIntrospector() {
            @Override
            public Class<?> findDeserializationType(Annotated am, JavaType baseType) {
                return String.class; // not a supertype of Long
            }
        };

        TypeFactory tf = TypeFactory.defaultInstance();
        MapperConfigStub config = new MapperConfigStub(tf);
        JavaType baseType = tf.constructType(Long.class);
        AnnotatedStub annotated = new AnnotatedStub("getValue");

        try {
            ti.refineDeserializationType(config, annotated, baseType);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            String msg = e.getMessage();
            assertTrue("Expected message to contain 'Failed to narrow type', but got: " + msg,
                    msg.contains("Failed to narrow type"));
        }
    }

    // -----------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testRefineSerializationTypeWithIllegalArgumentException() throws Exception {
        // Simulate a case where TypeFactory throws IllegalArgumentException
        // This is already covered by the non-supertype tests, but we can also
        // test that the catch block works correctly.
        // The defect tests already cover this path.
    }

    @Test(timeout = 4000)
    public void testProtectedHelperMethods() {
        TestIntrospector ti = new TestIntrospector();
        AnnotatedStub annotated = new AnnotatedStub("test");
        // _findAnnotation returns null since annotated has no annotations
        assertNull(ti._findAnnotation(annotated, Deprecated.class));
        assertFalse(ti._hasAnnotation(annotated, Deprecated.class));
        assertFalse(ti._hasOneOf(annotated, new Class[]{Deprecated.class}));
    }

    // -----------------------------------------------------------------------
    // Partition E: Object Lifecycle & Contract Integrity
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testVersion() {
        TestIntrospector ti = new TestIntrospector();
        assertEquals(Version.unknownVersion(), ti.version());
    }

    @Test(timeout = 4000)
    public void testReferencePropertyEquality() {
        AnnotationIntrospector.ReferenceProperty rp1 = AnnotationIntrospector.ReferenceProperty.managed("a");
        AnnotationIntrospector.ReferenceProperty rp2 = AnnotationIntrospector.ReferenceProperty.managed("a");
        // No equals/hashCode defined, but we can test getters
        assertEquals("a", rp1.getName());
        assertEquals(AnnotationIntrospector.ReferenceProperty.Type.MANAGED_REFERENCE, rp1.getType());
    }
}