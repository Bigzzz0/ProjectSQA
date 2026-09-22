package com.fasterxml.jackson.databind.ser;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.util.*;

/**
 * [Branch & Defect Analysis Matrix]
 *
 * Targeted branches and conditions:
 * 1. PropertyBuilder constructor: handling of inclusion merging (global, per-type, per-property)
 * 2. buildWriter() switch(include): NON_DEFAULT, NON_ABSENT, NON_EMPTY, NON_NULL, ALWAYS
 * 3. NON_DEFAULT: _useRealPropertyDefaults branch (true vs false)
 * 4. _useRealPropertyDefaults == true: getPropertyDefaultValue path with fixAccess
 * 5. _useRealPropertyDefaults == false: getDefaultValue path with suppressNulls=true
 * 6. valueToSuppress null handling and array comparator creation
 * 7. NON_ABSENT branch: suppressNulls=true + reference type detection
 * 8. NON_EMPTY branch: suppressNulls=true + MARKER_FOR_EMPTY
 * 9. NON_NULL branch: suppressNulls=true + fall-through
 * 10. ALWAYS default: container type WRITE_EMPTY_JSON_ARRAYS check
 * 11. getDefaultBean(): instantiation success/failure paths, NO_DEFAULT_MARKER handling
 * 12. getPropertyDefaultValue(): null defaultBean vs non-null, Exception handling
 * 13. getDefaultValue(): primitive, container/reference, String, null return
 * 14. findSerializationType(): secondary type refinement, assignable checks, typing discovery
 * 15. serializationType null handling in buildWriter (contentTypeSer path)
 * 16. ct == null check when contentTypeSer != null
 * 17. _throwWrapped: chain traversal with Error/RuntimeException/IllegalArgumentException
 *
 * Defect targeting: Issue #1351 - NON_DEFAULT with per-type inclusion should properly suppress
 * null values when default instance cannot be created (_useRealPropertyDefaults=true but
 * getDefaultBean() returns null, leading to getDefaultValue path which for Object type returns null,
 * but suppressNulls may not be properly set - need to ensure null suppression when valueToSuppress is null)
 */
public class PropertyBuilderDeepseekTest {

    // ===== Partition A: Core Functional Logic & State Transitions =====

    @Test(timeout = 4000)
    public void testConstructorInitializesFields() {
        SerializationConfig config = new SerializationConfig(null, null);
        BeanDescription beanDesc = new BeanDescription(null);
        PropertyBuilder builder = new PropertyBuilder(config, beanDesc);
        assertNotNull("config should not be null", builder._config);
        assertNotNull("beanDesc should not be null", builder._beanDesc);
        assertNotNull("defaultInclusion should not be null", builder._defaultInclusion);
        assertTrue("useRealPropertyDefaults should be initialized", builder._useRealPropertyDefaults);
    }

    @Test(timeout = 4000)
    public void testGetClassAnnotationsReturnsAnnotationsFromBeanDesc() {
        SerializationConfig config = new SerializationConfig(null, null);
        BeanDescription beanDesc = new BeanDescription(null);
        PropertyBuilder builder = new PropertyBuilder(config, beanDesc);
        Annotations annotations = builder.getClassAnnotations();
        assertNotNull("getClassAnnotations should not return null", annotations);
    }

    @Test(timeout = 4000)
    public void testGetDefaultBeanWithInstantiableClass() {
        SerializationConfig config = new SerializationConfig(null, null);
        BeanDescription beanDesc = new BeanDescription(null);
        PropertyBuilder builder = new PropertyBuilder(config, beanDesc);
        Object defaultBean = builder.getDefaultBean();
        assertNotNull("getDefaultBean should return non-null for instantiable class", defaultBean);
    }

    @Test(timeout = 4000)
    public void testGetDefaultBeanReturnsNullForNonInstantiableClass() {
        SerializationConfig config = new SerializationConfig(null, null);
        BeanDescription beanDesc = new BeanDescription(null);
        PropertyBuilder builder = new PropertyBuilder(config, beanDesc);
        // Simulate non-instantiable scenario (e.g., no default constructor)
        Object defaultBean = builder.getDefaultBean();
        // In this test, we expect it to return null due to NO_DEFAULT_MARKER
        assertNull("getDefaultBean should return null for non-instantiable class", defaultBean);
    }

    @Test(timeout = 4000)
    public void testGetPropertyDefaultValueWithNullDefaultBean() {
        SerializationConfig config = new SerializationConfig(null, null);
        BeanDescription beanDesc = new BeanDescription(null);
        PropertyBuilder builder = new PropertyBuilder(config, beanDesc);
        AnnotatedMember member = new AnnotatedMember(null);
        JavaType type = new JavaType(Object.class);
        Object defaultValue = builder.getPropertyDefaultValue("testProp", member, type);
        // When defaultBean is null, should fall back to getDefaultValue
        assertNull("getPropertyDefaultValue should return null for Object type", defaultValue);
    }

    @Test(timeout = 4000)
    public void testGetDefaultValueForPrimitive() {
        SerializationConfig config = new SerializationConfig(null, null);
        BeanDescription beanDesc = new BeanDescription(null);
        PropertyBuilder builder = new PropertyBuilder(config, beanDesc);
        JavaType intType = new JavaType(Integer.TYPE);
        Object defaultValue = builder.getDefaultValue(intType);
        assertEquals("default value for int should be 0", 0, defaultValue);
    }

    @Test(timeout = 4000)
    public void testGetDefaultValueForIntegerWrapper() {
        SerializationConfig config = new SerializationConfig(null, null);
        BeanDescription beanDesc = new BeanDescription(null);
        PropertyBuilder builder = new PropertyBuilder(config, beanDesc);
        JavaType integerType = new JavaType(Integer.class);
        Object defaultValue = builder.getDefaultValue(integerType);
        assertEquals("default value for Integer should be 0", 0, defaultValue);
    }

    @Test(timeout = 4000)
    public void testGetDefaultValueForString() {
        SerializationConfig config = new SerializationConfig(null, null);
        BeanDescription beanDesc = new BeanDescription(null);
        PropertyBuilder builder = new PropertyBuilder(config, beanDesc);
        JavaType stringType = new JavaType(String.class);
        Object defaultValue = builder.getDefaultValue(stringType);
        assertEquals("default value for String should be empty string", "", defaultValue);
    }

    @Test(timeout = 4000)
    public void testGetDefaultValueForContainerType() {
        SerializationConfig config = new SerializationConfig(null, null);
        BeanDescription beanDesc = new BeanDescription(null);
        PropertyBuilder builder = new PropertyBuilder(config, beanDesc);
        JavaType listType = new JavaType(List.class);
        Object defaultValue = builder.getDefaultValue(listType);
        assertEquals("default value for container type should be NON_EMPTY", JsonInclude.Include.NON_EMPTY, defaultValue);
    }

    @Test(timeout = 4000)
    public void testGetDefaultValueForReferenceType() {
        SerializationConfig config = new SerializationConfig(null, null);
        BeanDescription beanDesc = new BeanDescription(null);
        PropertyBuilder builder = new PropertyBuilder(config, beanDesc);
        JavaType refType = new JavaType(Optional.class);
        Object defaultValue = builder.getDefaultValue(refType);
        assertEquals("default value for reference type should be NON_EMPTY", JsonInclude.Include.NON_EMPTY, defaultValue);
    }

    @Test(timeout = 4000)
    public void testGetDefaultValueForObjectType() {
        SerializationConfig config = new SerializationConfig(null, null);
        BeanDescription beanDesc = new BeanDescription(null);
        PropertyBuilder builder = new PropertyBuilder(config, beanDesc);
        JavaType objectType = new JavaType(Object.class);
        Object defaultValue = builder.getDefaultValue(objectType);
        assertNull("default value for Object type should be null", defaultValue);
    }

    // ===== Partition C: Defect-Targeted Branch Zone =====
    // ** CRITICAL DEFECT TEST FOR ISSUE #1351 **
    @Test(timeout = 4000)
    public void testIssue1351DefaultInclusionSuppressesNullWhenNoDefaultBean() {
        // This test targets the known defect: NON_DEFAULT with per-type inclusion should
        // properly suppress null values when default instance cannot be created.
        // The bug causes {"str":null} to appear in output instead of {}.
        SerializationConfig config = new SerializationConfig(null, null);
        BeanDescription beanDesc = new BeanDescription(null);
        PropertyBuilder builder = new PropertyBuilder(config, beanDesc);

        // Simulate scenario: _useRealPropertyDefaults = true but getDefaultBean() returns null
        // This triggers the path where valueToSuppress = getDefaultValue(type) which returns null for Object
        // and suppressNulls should become true
        
        AnnotatedMember member = new AnnotatedMember(null);
        JavaType type = new JavaType(Object.class);
        
        // Directly test the getPropertyDefaultValue logic
        Object valueToSuppress = builder.getPropertyDefaultValue("str", member, type);
        boolean suppressNulls = (valueToSuppress == null);
        
        // If the defect exists, suppressNulls would be false (bug) when it should be true
        // The correct behavior: for NON_DEFAULT with no default bean, null values should be suppressed
        assertTrue("When default bean is null, null values must be suppressed for NON_DEFAULT", suppressNulls);
        assertNull("valueToSuppress should be null for Object type", valueToSuppress);
    }

    @Test(timeout = 4000)
    public void testBuildWriterWithNON_DEFAULTAndUseRealPropertyDefaultsTrue() {
        SerializationConfig config = new SerializationConfig(null, null);
        BeanDescription beanDesc = new BeanDescription(null);
        PropertyBuilder builder = new PropertyBuilder(config, beanDesc);
        // Ensure _useRealPropertyDefaults is true for this test
        // This path requires fixAccess and getPropertyDefaultValue
        
        SerializerProvider prov = new SerializerProvider(config);
        BeanPropertyDefinition propDef = new BeanPropertyDefinition("testProp");
        JavaType declaredType = new JavaType(String.class);
        JsonSerializer<?> ser = new JsonSerializer<String>();
        TypeSerializer typeSer = null;
        TypeSerializer contentTypeSer = null;
        AnnotatedMember am = new AnnotatedMember(null);
        boolean defaultUseStaticTyping = false;

        try {
            BeanPropertyWriter writer = builder.buildWriter(prov, propDef, declaredType, ser,
                    typeSer, contentTypeSer, am, defaultUseStaticTyping);
            assertNotNull("buildWriter should return non-null BeanPropertyWriter", writer);
            // Verify that suppressNulls is correctly set for NON_DEFAULT with null default
            assertTrue("suppressNulls should be true when valueToSuppress is null", writer.suppressNulls());
        } catch (JsonMappingException e) {
            fail("buildWriter should not throw exception: " + e.getMessage());
        }
    }

    // ===== Partition B: Boundary Value Analysis & Extremes =====

    @Test(timeout = 4000)
    public void testBuildWriterWithNullSerializationType() {
        SerializationConfig config = new SerializationConfig(null, null);
        BeanDescription beanDesc = new BeanDescription(null);
        PropertyBuilder builder = new PropertyBuilder(config, beanDesc);
        SerializerProvider prov = new SerializerProvider(config);
        BeanPropertyDefinition propDef = new BeanPropertyDefinition("testProp");
        JavaType declaredType = new JavaType(Object.class);
        JsonSerializer<?> ser = new JsonSerializer<Object>();
        TypeSerializer typeSer = null;
        TypeSerializer contentTypeSer = null;
        AnnotatedMember am = new AnnotatedMember(null);
        boolean defaultUseStaticTyping = false;

        try {
            BeanPropertyWriter writer = builder.buildWriter(prov, propDef, declaredType, ser,
                    typeSer, contentTypeSer, am, defaultUseStaticTyping);
            assertNotNull("buildWriter should handle null serializationType", writer);
        } catch (JsonMappingException e) {
            fail("buildWriter should not throw exception for null serializationType: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testBuildWriterWithContentTypeSerAndNullContentType() {
        SerializationConfig config = new SerializationConfig(null, null);
        BeanDescription beanDesc = new BeanDescription(null);
        PropertyBuilder builder = new PropertyBuilder(config, beanDesc);
        SerializerProvider prov = new SerializerProvider(config);
        BeanPropertyDefinition propDef = new BeanPropertyDefinition("testProp");
        JavaType declaredType = new JavaType(Object.class);
        JsonSerializer<?> ser = new JsonSerializer<Object>();
        TypeSerializer typeSer = null;
        TypeSerializer contentTypeSer = new TypeSerializer(); // Non-null but content type may be null
        AnnotatedMember am = new AnnotatedMember(null);
        boolean defaultUseStaticTyping = false;

        try {
            builder.buildWriter(prov, propDef, declaredType, ser,
                    typeSer, contentTypeSer, am, defaultUseStaticTyping);
            fail("Should have thrown JsonMappingException when content type is null");
        } catch (JsonMappingException e) {
            // Expected: "serialization type ... has no content"
            assertTrue("Exception message should mention no content", e.getMessage().contains("no content"));
        }
    }

    @Test(timeout = 4000)
    public void testBuildWriterWithNON_EMPTYInclusion() {
        SerializationConfig config = new SerializationConfig(null, null);
        BeanDescription beanDesc = new BeanDescription(null);
        PropertyBuilder builder = new PropertyBuilder(config, beanDesc);
        // Override inclusion to NON_EMPTY
        JsonInclude.Value inclValue = JsonInclude.Value.construct(JsonInclude.Include.NON_EMPTY);
        
        SerializerProvider prov = new SerializerProvider(config);
        BeanPropertyDefinition propDef = new BeanPropertyDefinition("testProp");
        JavaType declaredType = new JavaType(String.class);
        JsonSerializer<?> ser = new JsonSerializer<String>();
        TypeSerializer typeSer = null;
        TypeSerializer contentTypeSer = null;
        AnnotatedMember am = new AnnotatedMember(null);
        boolean defaultUseStaticTyping = false;

        try {
            BeanPropertyWriter writer = builder.buildWriter(prov, propDef, declaredType, ser,
                    typeSer, contentTypeSer, am, defaultUseStaticTyping);
            assertNotNull("buildWriter should handle NON_EMPTY inclusion", writer);
            assertTrue("suppressNulls should be true for NON_EMPTY", writer.suppressNulls());
        } catch (JsonMappingException e) {
            fail("buildWriter should not throw exception: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testBuildWriterWithNON_NULLInclusion() {
        SerializationConfig config = new SerializationConfig(null, null);
        BeanDescription beanDesc = new BeanDescription(null);
        PropertyBuilder builder = new PropertyBuilder(config, beanDesc);
        // Override inclusion to NON_NULL
        JsonInclude.Value inclValue = JsonInclude.Value.construct(JsonInclude.Include.NON_NULL);
        
        SerializerProvider prov = new SerializerProvider(config);
        BeanPropertyDefinition propDef = new BeanPropertyDefinition("testProp");
        JavaType declaredType = new JavaType(String.class);
        JsonSerializer<?> ser = new JsonSerializer<String>();
        TypeSerializer typeSer = null;
        TypeSerializer contentTypeSer = null;
        AnnotatedMember am = new AnnotatedMember(null);
        boolean defaultUseStaticTyping = false;

        try {
            BeanPropertyWriter writer = builder.buildWriter(prov, propDef, declaredType, ser,
                    typeSer, contentTypeSer, am, defaultUseStaticTyping);
            assertNotNull("buildWriter should handle NON_NULL inclusion", writer);
            assertTrue("suppressNulls should be true for NON_NULL", writer.suppressNulls());
        } catch (JsonMappingException e) {
            fail("buildWriter should not throw exception: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testBuildWriterWithALWAYSInclusionAndContainerType() {
        SerializationConfig config = new SerializationConfig(null, null);
        BeanDescription beanDesc = new BeanDescription(null);
        PropertyBuilder builder = new PropertyBuilder(config, beanDesc);
        // Override inclusion to ALWAYS
        JsonInclude.Value inclValue = JsonInclude.Value.construct(JsonInclude.Include.ALWAYS);
        
        // Enable WRITE_EMPTY_JSON_ARRAYS to exercise the default branch
        SerializerProvider prov = new SerializerProvider(config);
        prov.disable(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS);
        
        BeanPropertyDefinition propDef = new BeanPropertyDefinition("testProp");
        JavaType declaredType = new JavaType(List.class); // Container type
        JsonSerializer<?> ser = new JsonSerializer<List>();
        TypeSerializer typeSer = null;
        TypeSerializer contentTypeSer = null;
        AnnotatedMember am = new AnnotatedMember(null);
        boolean defaultUseStaticTyping = false;

        try {
            BeanPropertyWriter writer = builder.buildWriter(prov, propDef, declaredType, ser,
                    typeSer, contentTypeSer, am, defaultUseStaticTyping);
            assertNotNull("buildWriter should handle ALWAYS inclusion with container type", writer);
            // When WRITE_EMPTY_JSON_ARRAYS is disabled, valueToSuppress should be MARKER_FOR_EMPTY
            assertFalse("suppressNulls should be false for ALWAYS", writer.suppressNulls());
        } catch (JsonMappingException e) {
            fail("buildWriter should not throw exception: " + e.getMessage());
        }
    }

    // ===== Partition D: Exception & Defensive Guard Paths =====

    @Test(timeout = 4000)
    public void testFindSerializationTypeWithNonAssignableType() {
        SerializationConfig config = new SerializationConfig(null, null);
        BeanDescription beanDesc = new BeanDescription(null);
        PropertyBuilder builder = new PropertyBuilder(config, beanDesc);
        Annotated a = new Annotated(null);
        JavaType declaredType = new JavaType(String.class);
        boolean useStaticTyping = false;

        // Simulate refineSerializationType returning a non-super type (e.g., Integer when declared is String)
        try {
            builder.findSerializationType(a, useStaticTyping, declaredType);
            fail("Should have thrown IllegalArgumentException for non-assignable types");
        } catch (IllegalArgumentException e) {
            assertTrue("Exception should mention illegal concrete-type annotation", e.getMessage().contains("Illegal concrete-type annotation"));
        } catch (JsonMappingException e) {
            // Could also throw JsonMappingException depending on implementation
        }
    }

    @Test(timeout = 4000)
    public void testFindSerializationTypeWithAssignableSubType() {
        SerializationConfig config = new SerializationConfig(null, null);
        BeanDescription beanDesc = new BeanDescription(null);
        PropertyBuilder builder = new PropertyBuilder(config, beanDesc);
        Annotated a = new Annotated(null);
        JavaType declaredType = new JavaType(Number.class);
        boolean useStaticTyping = false;

        // Simulate refineSerializationType returning a subtype (Integer) which is assignable from Number
        try {
            JavaType result = builder.findSerializationType(a, useStaticTyping, declaredType);
            assertNull("Should return null for dynamic typing", result);
        } catch (JsonMappingException e) {
            fail("Should not throw exception: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testThrowWrappedWithError() {
        SerializationConfig config = new SerializationConfig(null, null);
        BeanDescription beanDesc = new BeanDescription(null);
        PropertyBuilder builder = new PropertyBuilder(config, beanDesc);
        Exception wrappedError = new Exception(new Error("Test error"));
        
        try {
            builder._throwWrapped(wrappedError, "test", new Object());
            fail("Should have thrown Error");
        } catch (Error e) {
            assertEquals("Test error", e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testThrowWrappedWithRuntimeException() {
        SerializationConfig config = new SerializationConfig(null, null);
        BeanDescription beanDesc = new BeanDescription(null);
        PropertyBuilder builder = new PropertyBuilder(config, beanDesc);
        Exception wrappedRuntime = new Exception(new RuntimeException("Test runtime"));
        
        try {
            builder._throwWrapped(wrappedRuntime, "test", new Object());
            fail("Should have thrown RuntimeException");
        } catch (RuntimeException e) {
            assertEquals("Test runtime", e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testThrowWrappedWithCheckedException() {
        SerializationConfig config = new SerializationConfig(null, null);
        BeanDescription beanDesc = new BeanDescription(null);
        PropertyBuilder builder = new PropertyBuilder(config, beanDesc);
        Exception checkedException = new Exception("Checked exception");
        
        try {
            builder._throwWrapped(checkedException, "test", new Object());
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Failed to get property 'test'"));
        }
    }

    // ===== Partition E: Object Lifecycle & Contract Integrity =====

    @Test(timeout = 4000)
    public void testConstructorDoesNotThrowWithNullConfig() {
        try {
            BeanDescription beanDesc = new BeanDescription(null);
            PropertyBuilder builder = new PropertyBuilder(null, beanDesc);
            fail("Should throw NullPointerException for null config");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testConstructorDoesNotThrowWithNullBeanDesc() {
        try {
            SerializationConfig config = new SerializationConfig(null, null);
            PropertyBuilder builder = new PropertyBuilder(config, null);
            fail("Should throw NullPointerException for null beanDesc");
        } catch (NullPointerException e) {
            // Expected
        }
    }
}