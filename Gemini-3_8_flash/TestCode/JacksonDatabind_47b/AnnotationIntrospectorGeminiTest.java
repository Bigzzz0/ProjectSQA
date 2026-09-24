package com.fasterxml.jackson.databind;

import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.annotation.Annotation;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.ObjectIdInfo;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.type.TypeFactory;

/*
 * [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------------
 * Target Class: com.fasterxml.jackson.databind.AnnotationIntrospector
 *
 * PARTITION A: Core Functional Logic & State Transitions
 * - ReferenceProperty: Type enum (MANAGED_REFERENCE, BACK_REFERENCE), factory methods, getters, booleans.
 * - Introspector collection: allIntrospectors(), allIntrospectors(Collection), pair(a1, a2), nopInstance().
 * - Protected extension points: _findAnnotation, _hasAnnotation, _hasOneOf.
 * - findEnumValues: name array preservation vs delegation to findEnumValue().
 *
 * PARTITION B: Boundary Value Analysis (BVA) & Extremes
 * - Enum arrays: empty arrays, arrays with null elements, arrays with pre-populated names.
 * - VisibilityChecker pass-through: baseline instance returned unmodified.
 * - Default/NOP method behaviors: findSerializationInclusion, findPropertyInclusion, findObjectReferenceInfo.
 *
 * PARTITION C: Defect-Targeted Branch Zone (Defects4J Issue / databind#1178, databind#1231)
 * - refineSerializationType:
 *     * Specializing main serialization type (BaseClass -> SubClass): The defective implementation calls
 *       tf.constructGeneralizedType() unconditionally, throwing JsonMappingException "Failed to widen type...".
 *       The fix requires checking currRaw.isAssignableFrom(serClass) and calling tf.constructSpecializedType().
 *     * Unrelated main types (e.g., Long -> String): Defective version threw "Failed to widen type...",
 *       whereas it should validate relationship or explicitly throw mapping exception noting "types not related".
 *
 * PARTITION D: Exception & Defensive Guard Paths
 * - refineSerializationType: Map-like key type refinement (same class static typing, widening, narrowing, unrelated).
 * - refineSerializationType: Content type refinement (same class static typing, widening, narrowing, unrelated).
 * - refineDeserializationType: Type narrowing, narrowing failure (widening/incompatible type),
 *   key type narrowing failure, content type narrowing failure.
 *
 * PARTITION E: Object Lifecycle & Contract Integrity
 * - Default stub implementations: verifying no-op and null-returning contract of abstract base methods.
 * ---------------------------------------------------------------------------------------------------------
 */
public class AnnotationIntrospectorGeminiTest {

    @Deprecated
    private static class SampleAnnotatedBean {
        @SuppressWarnings("unused")
        public String field;
    }

    private enum SampleEnum {
        VALUE_A, VALUE_B
    }

    private static class TestIntrospector extends AnnotationIntrospector {
        private static final long serialVersionUID = 1L;

        private Class<?> _serType;
        private Class<?> _serKeyType;
        private Class<?> _serContentType;

        private Class<?> _deserType;
        private Class<?> _deserKeyType;
        private Class<?> _deserContentType;

        public void setSerType(Class<?> cls) { _serType = cls; }
        public void setSerKeyType(Class<?> cls) { _serKeyType = cls; }
        public void setSerContentType(Class<?> cls) { _serContentType = cls; }

        public void setDeserType(Class<?> cls) { _deserType = cls; }
        public void setDeserKeyType(Class<?> cls) { _deserKeyType = cls; }
        public void setDeserContentType(Class<?> cls) { _deserContentType = cls; }

        @Override
        public Version version() {
            return Version.unknownVersion();
        }

        @Override
        public Class<?> findSerializationType(Annotated a) {
            return _serType;
        }

        @Override
        public Class<?> findSerializationKeyType(Annotated am, JavaType baseType) {
            return _serKeyType;
        }

        @Override
        public Class<?> findSerializationContentType(Annotated am, JavaType baseType) {
            return _serContentType;
        }

        @Override
        public Class<?> findDeserializationType(Annotated am, JavaType baseType) {
            return _deserType;
        }

        @Override
        public Class<?> findDeserializationKeyType(Annotated am, JavaType baseKeyType) {
            return _deserKeyType;
        }

        @Override
        public Class<?> findDeserializationContentType(Annotated am, JavaType baseContentType) {
            return _deserContentType;
        }

        // Expose protected methods for white-box testing
        public <A extends Annotation> A testFindAnnotation(Annotated a, Class<A> cls) {
            return _findAnnotation(a, cls);
        }

        public boolean testHasAnnotation(Annotated a, Class<? extends Annotation> cls) {
            return _hasAnnotation(a, cls);
        }

        public boolean testHasOneOf(Annotated a, Class<? extends Annotation>[] classes) {
            return _hasOneOf(a, classes);
        }
    }

    private final ObjectMapper mapper = new ObjectMapper();
    private final SerializationConfig serConfig = mapper.getSerializationConfig();
    private final DeserializationConfig deserConfig = mapper.getDeserializationConfig();
    private final TypeFactory typeFactory = serConfig.getTypeFactory();
    private final AnnotatedClass sampleAnnotatedClass = serConfig.introspect(
            typeFactory.constructType(SampleAnnotatedBean.class)).getClassInfo();

    /*
     * --------------------------------------------------------------------------------
     * Partition A: Core Functional Logic & State Transitions
     * --------------------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testReferencePropertyStateAndFactoryMethods() {
        AnnotationIntrospector.ReferenceProperty managed = AnnotationIntrospector.ReferenceProperty.managed("parentRef");
        assertNotNull(managed);
        assertEquals(AnnotationIntrospector.ReferenceProperty.Type.MANAGED_REFERENCE, managed.getType());
        assertEquals("parentRef", managed.getName());
        assertTrue(managed.isManagedReference());
        assertFalse(managed.isBackReference());

        AnnotationIntrospector.ReferenceProperty back = AnnotationIntrospector.ReferenceProperty.back("childRef");
        assertNotNull(back);
        assertEquals(AnnotationIntrospector.ReferenceProperty.Type.BACK_REFERENCE, back.getType());
        assertEquals("childRef", back.getName());
        assertFalse(back.isManagedReference());
        assertTrue(back.isBackReference());
    }

    @Test(timeout = 4000)
    public void testIntrospectorCollectionAndPairing() {
        TestIntrospector introspector = new TestIntrospector();
        Collection<AnnotationIntrospector> col = introspector.allIntrospectors();
        assertEquals(1, col.size());
        assertTrue(col.contains(introspector));

        List<AnnotationIntrospector> targetList = new ArrayList<AnnotationIntrospector>();
        Collection<AnnotationIntrospector> returned = introspector.allIntrospectors(targetList);
        assertSame(targetList, returned);
        assertEquals(1, targetList.size());
        assertSame(introspector, targetList.get(0));

        AnnotationIntrospector nop = AnnotationIntrospector.nopInstance();
        assertNotNull(nop);

        AnnotationIntrospector pair = AnnotationIntrospector.pair(introspector, nop);
        assertNotNull(pair);
        assertTrue(pair instanceof AnnotationIntrospectorPair);
    }

    @Test(timeout = 4000)
    public void testProtectedAnnotationLookupHelpers() {
        TestIntrospector introspector = new TestIntrospector();

        Deprecated dep = introspector.testFindAnnotation(sampleAnnotatedClass, Deprecated.class);
        assertNotNull("SampleAnnotatedBean has @Deprecated annotation", dep);

        Override nonExistent = introspector.testFindAnnotation(sampleAnnotatedClass, Override.class);
        assertNull(nonExistent);

        assertTrue(introspector.testHasAnnotation(sampleAnnotatedClass, Deprecated.class));
        assertFalse(introspector.testHasAnnotation(sampleAnnotatedClass, Override.class));

        @SuppressWarnings("unchecked")
        Class<? extends Annotation>[] matchAny = new Class[] { Override.class, Deprecated.class };
        assertTrue(introspector.testHasOneOf(sampleAnnotatedClass, matchAny));

        @SuppressWarnings("unchecked")
        Class<? extends Annotation>[] matchNone = new Class[] { Override.class };
        assertFalse(introspector.testHasOneOf(sampleAnnotatedClass, matchNone));
    }

    @Test(timeout = 4000)
    public void testFindEnumValuesDelegationAndPreservation() {
        TestIntrospector introspector = new TestIntrospector();
        SampleEnum[] values = SampleEnum.values();
        String[] names = new String[] { "PRE_SET_A", null };

        String[] resolved = introspector.findEnumValues(SampleEnum.class, values, names);
        assertSame(names, resolved);
        assertEquals("PRE_SET_A", resolved[0]);
        assertEquals(SampleEnum.VALUE_B.name(), resolved[1]);
    }

    /*
     * --------------------------------------------------------------------------------
     * Partition B: Boundary Value Analysis (BVA) & Extremes
     * --------------------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testFindEnumValuesEmptyArray() {
        TestIntrospector introspector = new TestIntrospector();
        String[] resolved = introspector.findEnumValues(SampleEnum.class, new SampleEnum[0], new String[0]);
        assertEquals(0, resolved.length);
    }

    @Test(timeout = 4000)
    public void testAutoDetectVisibilityPassThrough() {
        TestIntrospector introspector = new TestIntrospector();
        VisibilityChecker<?> checker = VisibilityChecker.Std.defaultInstance();
        VisibilityChecker<?> result = introspector.findAutoDetectVisibility(sampleAnnotatedClass, checker);
        assertSame("Base introspector must return visibility checker unmodified", checker, result);
    }

    @Test(timeout = 4000)
    public void testObjectReferenceInfoPassThrough() {
        TestIntrospector introspector = new TestIntrospector();
        ObjectIdInfo info = new ObjectIdInfo(PropertyName.construct("id"), Object.class, null, null);
        ObjectIdInfo result = introspector.findObjectReferenceInfo(sampleAnnotatedClass, info);
        assertSame(info, result);
    }

    @Test(timeout = 4000)
    public void testInclusionDefaults() {
        TestIntrospector introspector = new TestIntrospector();
        JsonInclude.Include inc = introspector.findSerializationInclusion(sampleAnnotatedClass, JsonInclude.Include.NON_ABSENT);
        assertEquals(JsonInclude.Include.NON_ABSENT, inc);

        JsonInclude.Include contentInc = introspector.findSerializationInclusionForContent(sampleAnnotatedClass, JsonInclude.Include.NON_EMPTY);
        assertEquals(JsonInclude.Include.NON_EMPTY, contentInc);

        JsonInclude.Value propInc = introspector.findPropertyInclusion(sampleAnnotatedClass);
        assertEquals(JsonInclude.Value.empty(), propInc);
    }

    @Test(timeout = 4000)
    public void testDeprecatedPropertiesToIgnoreDelegation() {
        TestIntrospector introspector = new TestIntrospector();
        assertNull(introspector.findPropertiesToIgnore(sampleAnnotatedClass));
        assertNull(introspector.findPropertiesToIgnore(sampleAnnotatedClass, true));
        assertNull(introspector.findPropertiesToIgnore(sampleAnnotatedClass, false));
    }

    /*
     * --------------------------------------------------------------------------------
     * Partition C: Defect-Targeted Branch Zone (databind#1178, databind#1231)
     * --------------------------------------------------------------------------------
     */

    /**
     * TARGETS DEFECT: In refineSerializationType(), when an annotation specifies a subtype/specialized class
     * for the main type, the defective version calls tf.constructGeneralizedType(), triggering an
     * IllegalArgumentException / JsonMappingException: "Failed to widen type...".
     * The fixed version must specialize the type properly.
     */
    @Test(timeout = 4000)
    public void testRefineSerializationTypeSpecializationDefect() throws JsonMappingException {
        TestIntrospector introspector = new TestIntrospector();
        JavaType baseType = typeFactory.constructType(Number.class);
        introspector.setSerType(Integer.class); // Integer is a subtype of Number

        JavaType refinedType = introspector.refineSerializationType(serConfig, sampleAnnotatedClass, baseType);
        assertNotNull(refinedType);
        assertEquals("Should specialize Number to Integer without throwing widening error",
                Integer.class, refinedType.getRawClass());
    }

    /**
     * TARGETS DEFECT: In refineSerializationType(), when an annotation specifies a completely unrelated type
     * (e.g., Long -> String), the fixed version must fail indicating "not related", rather than
     * confusingly reporting a failed widening attempt.
     */
    @Test(timeout = 4000)
    public void testRefineSerializationTypeUnrelatedDefect() {
        TestIntrospector introspector = new TestIntrospector();
        JavaType baseType = typeFactory.constructType(Long.class);
        introspector.setSerType(String.class); // Long and String are unrelated

        try {
            introspector.refineSerializationType(serConfig, sampleAnnotatedClass, baseType);
            fail("Expected JsonMappingException for unrelated serialization types");
        } catch (JsonMappingException e) {
            String msg = e.getMessage();
            assertTrue("Expected message to state types are not related, got: " + msg,
                    msg != null && msg.contains("not related"));
        }
    }

    /*
     * --------------------------------------------------------------------------------
     * Partition D: Exception & Defensive Guard Paths
     * --------------------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testRefineSerializationTypeStaticTypingAndGeneralization() throws JsonMappingException {
        TestIntrospector introspector = new TestIntrospector();

        // 1. Same raw class -> forces static typing
        JavaType strType = typeFactory.constructType(String.class);
        introspector.setSerType(String.class);
        JavaType staticTyping = introspector.refineSerializationType(serConfig, sampleAnnotatedClass, strType);
        assertEquals(String.class, staticTyping.getRawClass());
        assertTrue(staticTyping.useStaticType());

        // 2. Supertype -> generalization
        JavaType intType = typeFactory.constructType(Integer.class);
        introspector.setSerType(Number.class);
        JavaType generalized = introspector.refineSerializationType(serConfig, sampleAnnotatedClass, intType);
        assertEquals(Number.class, generalized.getRawClass());
    }

    @Test(timeout = 4000)
    public void testRefineSerializationTypeKeyTypePaths() throws JsonMappingException {
        TestIntrospector introspector = new TestIntrospector();
        JavaType mapType = typeFactory.constructMapType(Map.class, Number.class, Object.class);

        // Same class -> static typing on key
        introspector.setSerKeyType(Number.class);
        JavaType keyStatic = introspector.refineSerializationType(serConfig, sampleAnnotatedClass, mapType);
        assertTrue(keyStatic.getKeyType().useStaticType());

        // Narrowing/specializing key type
        introspector.setSerKeyType(Integer.class);
        JavaType keySpecialized = introspector.refineSerializationType(serConfig, sampleAnnotatedClass, mapType);
        assertEquals(Integer.class, keySpecialized.getKeyType().getRawClass());

        // Generalizing key type
        JavaType specificMap = typeFactory.constructMapType(Map.class, Integer.class, Object.class);
        introspector.setSerKeyType(Number.class);
        JavaType keyGeneralized = introspector.refineSerializationType(serConfig, sampleAnnotatedClass, specificMap);
        assertEquals(Number.class, keyGeneralized.getKeyType().getRawClass());

        // Unrelated key types
        introspector.setSerKeyType(String.class);
        try {
            introspector.refineSerializationType(serConfig, sampleAnnotatedClass, specificMap);
            fail("Expected JsonMappingException for unrelated key types");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("not related"));
        }
    }

    @Test(timeout = 4000)
    public void testRefineSerializationTypeContentTypePaths() throws JsonMappingException {
        TestIntrospector introspector = new TestIntrospector();
        JavaType listType = typeFactory.constructCollectionType(List.class, Number.class);

        // Same class -> static typing on content
        introspector.setSerContentType(Number.class);
        JavaType contentStatic = introspector.refineSerializationType(serConfig, sampleAnnotatedClass, listType);
        assertTrue(contentStatic.getContentType().useStaticType());

        // Narrowing/specializing content type
        introspector.setSerContentType(Integer.class);
        JavaType contentSpecialized = introspector.refineSerializationType(serConfig, sampleAnnotatedClass, listType);
        assertEquals(Integer.class, contentSpecialized.getContentType().getRawClass());

        // Generalizing content type
        JavaType specificList = typeFactory.constructCollectionType(List.class, Integer.class);
        introspector.setSerContentType(Number.class);
        JavaType contentGeneralized = introspector.refineSerializationType(serConfig, sampleAnnotatedClass, specificList);
        assertEquals(Number.class, contentGeneralized.getContentType().getRawClass());

        // Unrelated content types
        introspector.setSerContentType(String.class);
        try {
            introspector.refineSerializationType(serConfig, sampleAnnotatedClass, specificList);
            fail("Expected JsonMappingException for unrelated content types");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("not related"));
        }
    }

    @Test(timeout = 4000)
    public void testRefineDeserializationTypePaths() throws JsonMappingException {
        TestIntrospector introspector = new TestIntrospector();
        JavaType numType = typeFactory.constructType(Number.class);

        // Unmodified when null or same class
        assertSame(numType, introspector.refineDeserializationType(deserConfig, sampleAnnotatedClass, numType));
        introspector.setDeserType(Number.class);
        assertSame(numType, introspector.refineDeserializationType(deserConfig, sampleAnnotatedClass, numType));

        // Narrowing
        introspector.setDeserType(Integer.class);
        JavaType narrowed = introspector.refineDeserializationType(deserConfig, sampleAnnotatedClass, numType);
        assertEquals(Integer.class, narrowed.getRawClass());

        // Incompatible narrowing (e.g. widening or unrelated)
        JavaType intType = typeFactory.constructType(Integer.class);
        introspector.setDeserType(Number.class);
        try {
            introspector.refineDeserializationType(deserConfig, sampleAnnotatedClass, intType);
            fail("Expected JsonMappingException when failing to narrow deserialization type");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Failed to narrow type"));
        }
    }

    @Test(timeout = 4000)
    public void testRefineDeserializationTypeKeyAndContentFailures() {
        TestIntrospector introspector = new TestIntrospector();
        JavaType mapType = typeFactory.constructMapType(Map.class, Integer.class, Integer.class);

        // Key narrowing failure
        introspector.setDeserKeyType(Number.class);
        try {
            introspector.refineDeserializationType(deserConfig, sampleAnnotatedClass, mapType);
            fail("Expected JsonMappingException when narrowing key type fails");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Failed to narrow key type"));
        }

        // Content narrowing failure
        introspector.setDeserKeyType(null);
        introspector.setDeserContentType(Number.class);
        try {
            introspector.refineDeserializationType(deserConfig, sampleAnnotatedClass, mapType);
            fail("Expected JsonMappingException when narrowing value type fails");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Failed to narrow value type"));
        }
    }

    /*
     * --------------------------------------------------------------------------------
     * Partition E: Object Lifecycle & Contract Integrity (Default Stubs)
     * --------------------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testBaseIntrospectorDefaultImplementations() {
        AnnotationIntrospector ai = AnnotationIntrospector.nopInstance();

        assertFalse(ai.isAnnotationBundle(null));
        assertNull(ai.findObjectIdInfo(sampleAnnotatedClass));
        assertNull(ai.findRootName(sampleAnnotatedClass));
        assertNull(ai.findIgnoreUnknownProperties(sampleAnnotatedClass));
        assertNull(ai.isIgnorableType(sampleAnnotatedClass));
        assertNull(ai.findFilterId(sampleAnnotatedClass));
        assertNull(ai.findNamingStrategy(sampleAnnotatedClass));
        assertNull(ai.findClassDescription(sampleAnnotatedClass));
        assertNull(ai.findTypeResolver(serConfig, sampleAnnotatedClass, null));
        assertNull(ai.findPropertyTypeResolver(serConfig, null, null));
        assertNull(ai.findPropertyContentTypeResolver(serConfig, null, null));
        assertNull(ai.findSubtypes(sampleAnnotatedClass));
        assertNull(ai.findTypeName(sampleAnnotatedClass));
        assertNull(ai.isTypeId(null));
        assertNull(ai.findReferenceType(null));
        assertNull(ai.findUnwrappingNameTransformer(null));
        assertFalse(ai.hasIgnoreMarker(null));
        assertNull(ai.findInjectableValueId(null));
        assertNull(ai.hasRequiredMarker(null));
        assertNull(ai.findViews(sampleAnnotatedClass));
        assertNull(ai.findFormat((Annotated) sampleAnnotatedClass));
        assertNull(ai.findWrapperName(sampleAnnotatedClass));
        assertNull(ai.findPropertyDefaultValue(sampleAnnotatedClass));
        assertNull(ai.findPropertyDescription(sampleAnnotatedClass));
        assertNull(ai.findPropertyIndex(sampleAnnotatedClass));
        assertNull(ai.findImplicitPropertyName(null));
        assertNull(ai.findPropertyAccess(sampleAnnotatedClass));
        assertNull(ai.resolveSetterConflict(serConfig, null, null));

        assertNull(ai.findSerializer(sampleAnnotatedClass));
        assertNull(ai.findKeySerializer(sampleAnnotatedClass));
        assertNull(ai.findContentSerializer(sampleAnnotatedClass));
        assertNull(ai.findNullSerializer(sampleAnnotatedClass));
        assertNull(ai.findSerializationTyping(sampleAnnotatedClass));
        assertNull(ai.findSerializationConverter(sampleAnnotatedClass));
        assertNull(ai.findSerializationContentConverter(null));
        assertNull(ai.findSerializationPropertyOrder(sampleAnnotatedClass));
        assertNull(ai.findSerializationSortAlphabetically(sampleAnnotatedClass));
        assertNull(ai.findNameForSerialization(sampleAnnotatedClass));
        assertFalse(ai.hasAsValueAnnotation(null));

        List<BeanPropertyWriter> virtualProps = new ArrayList<BeanPropertyWriter>();
        ai.findAndAddVirtualProperties(serConfig, sampleAnnotatedClass, virtualProps);
        assertTrue(virtualProps.isEmpty());

        assertNull(ai.findDeserializer(sampleAnnotatedClass));
        assertNull(ai.findKeyDeserializer(sampleAnnotatedClass));
        assertNull(ai.findContentDeserializer(sampleAnnotatedClass));
        assertNull(ai.findDeserializationConverter(sampleAnnotatedClass));
        assertNull(ai.findDeserializationContentConverter(null));
        assertNull(ai.findValueInstantiator(sampleAnnotatedClass));
        assertNull(ai.findPOJOBuilder(sampleAnnotatedClass));
        assertNull(ai.findPOJOBuilderConfig(sampleAnnotatedClass));
        assertNull(ai.findNameForDeserialization(sampleAnnotatedClass));
        assertFalse(ai.hasAnySetterAnnotation(null));
        assertFalse(ai.hasAnyGetterAnnotation(null));
        assertFalse(ai.hasCreatorAnnotation(sampleAnnotatedClass));
        assertNull(ai.findCreatorBinding(sampleAnnotatedClass));
    }
}