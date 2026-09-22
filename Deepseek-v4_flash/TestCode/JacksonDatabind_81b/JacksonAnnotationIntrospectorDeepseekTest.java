package com.fasterxml.jackson.databind.introspect;

import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.*;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.*;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.TypeResolutionContext;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: JacksonAnnotationIntrospector
 * - Partitions:
 *   A: Core functional logic (findEnumValue, findSerializationType, etc.)
 *   B: Boundary values (null annotations, empty strings, MAX/MIN)
 *   C: Defect-targeted branch: refineSerializationType with primitive↔wrapper
 *   D: Exception/defensive paths (illegal argument, null config)
 *   E: Object lifecycle (readResolve, constructor)
 * 
 * Known Defect (TypeCoercion1592):
 *   refineSerializationType throws JsonMappingException when refining int to Integer
 *   because primitive and wrapper classes are not assignable. Expected: handle gracefully.
 * 
 * Test strategy:
 *   - Directly invoke refineSerializationType with an Annotated that carries @JsonSerialize(as=Integer.class)
 *     on an int field. Assert no exception is thrown and resulting type is correct.
 *   - Cover all major public methods with edge cases.
 */
public class JacksonAnnotationIntrospectorDeepseekTest {

    // ---------- Partition A: Core Functional Logic ----------

    @Test(timeout = 4000)
    public void testVersion() {
        JacksonAnnotationIntrospector inst = new JacksonAnnotationIntrospector();
        assertNotNull("Version should not be null", inst.version());
        assertEquals("Version should match package version",
                com.fasterxml.jackson.databind.cfg.PackageVersion.VERSION, inst.version());
    }

    @Test(timeout = 4000)
    public void testFindEnumValueWithJsonProperty() throws Exception {
        JacksonAnnotationIntrospector inst = new JacksonAnnotationIntrospector();
        // Enum with @JsonProperty on a constant
        assertEquals("FOO", inst.findEnumValue(TestEnum.WITH_ANNOT));
        assertEquals("BAR", inst.findEnumValue(TestEnum.WITH_ANNOT_BAR));
        // Enum without annotation => name()
        assertEquals("NO_ANNOT", inst.findEnumValue(TestEnum.NO_ANNOT));
    }

    @Test(timeout = 4000)
    public void testFindEnumValues() {
        JacksonAnnotationIntrospector inst = new JacksonAnnotationIntrospector();
        Class<?> enumClass = TestEnum.class;
        Enum<?>[] values = TestEnum.values();
        String[] names = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            names[i] = values[i].name();
        }
        String[] result = inst.findEnumValues(enumClass, values, names);
        // Check that WITH_ANNOT got "FOO" and WITH_ANNOT_BAR got "BAR"
        assertEquals("FOO", result[0]);
        assertEquals("BAR", result[2]);
        // Others unchanged
        assertEquals("NO_ANNOT", result[1]);
    }

    @Test(timeout = 4000)
    public void testFindDefaultEnumValue() {
        JacksonAnnotationIntrospector inst = new JacksonAnnotationIntrospector();
        Enum<?> def = inst.findDefaultEnumValue((Class) TestEnum.class);
        assertNull("No @JsonEnumDefaultValue present", def);
    }

    @Test(timeout = 4000)
    public void testFindRootName() {
        JacksonAnnotationIntrospector inst = new JacksonAnnotationIntrospector();
        // Use a class with @JsonRootName
        AnnotatedClass ac = AnnotatedClass.construct(RootBean.class, null, null);
        PropertyName name = inst.findRootName(ac);
        assertNotNull("Root name should be present", name);
        assertEquals("rootTest", name.getSimpleName());
        assertNull("No namespace", name.getNamespace());
    }

    @Test(timeout = 4000)
    public void testFindPropertyIgnorals() {
        JacksonAnnotationIntrospector inst = new JacksonAnnotationIntrospector();
        // On a class with @JsonIgnoreProperties
        AnnotatedClass ac = AnnotatedClass.construct(IgnoralsBean.class, null, null);
        JsonIgnoreProperties.Value v = inst.findPropertyIgnorals(ac);
        assertNotNull(v);
        assertTrue("Should ignore 'hidden'", v.getIgnoreUnknown());
        assertTrue(v.getIgnored().contains("a"));
    }

    @Test(timeout = 4000)
    public void testIsIgnorableType() {
        JacksonAnnotationIntrospector inst = new JacksonAnnotationIntrospector();
        AnnotatedClass ac = AnnotatedClass.construct(IgnorableBean.class, null, null);
        Boolean result = inst.isIgnorableType(ac);
        assertTrue("Should be ignorable", result);
    }

    @Test(timeout = 4000)
    public void testFindFilterId() {
        JacksonAnnotationIntrospector inst = new JacksonAnnotationIntrospector();
        AnnotatedClass ac = AnnotatedClass.construct(FilterBean.class, null, null);
        Object id = inst.findFilterId(ac);
        assertEquals("myFilter", id);
    }

    @Test(timeout = 4000)
    public void testFindNamingStrategy() {
        JacksonAnnotationIntrospector inst = new JacksonAnnotationIntrospector();
        AnnotatedClass ac = AnnotatedClass.construct(NamingBean.class, null, null);
        Object strategy = inst.findNamingStrategy(ac);
        assertNotNull(strategy);
        assertEquals(TestNamingStrategy.class, strategy);
    }

    @Test(timeout = 4000)
    public void testFindClassDescription() {
        JacksonAnnotationIntrospector inst = new JacksonAnnotationIntrospector();
        AnnotatedClass ac = AnnotatedClass.construct(DescBean.class, null, null);
        String desc = inst.findClassDescription(ac);
        assertEquals("A test bean", desc);
    }

    // ---------- Partition B: Boundary Value Analysis ----------

    @Test(timeout = 4000)
    public void testFindEnumValueWithNullField() throws Exception {
        JacksonAnnotationIntrospector inst = new JacksonAnnotationIntrospector();
        // For an enum constant that has no field (uses synthetic), we still call name()
        assertEquals("NO_ANNOT", inst.findEnumValue(TestEnum.NO_ANNOT));
    }

    @Test(timeout = 4000)
    public void testFindSerializationSortAlphabeticallyFalse() {
        JacksonAnnotationIntrospector inst = new JacksonAnnotationIntrospector();
        // Class with @JsonPropertyOrder(alphabetic=false) should return null (only true is significant)
        AnnotatedClass ac = AnnotatedClass.construct(OrderFalseBean.class, null, null);
        assertNull("alphabetic false should give null", inst.findSerializationSortAlphabetically(ac));
    }

    @Test(timeout = 4000)
    public void testFindPropertyIndexUnset() {
        JacksonAnnotationIntrospector inst = new JacksonAnnotationIntrospector();
        // Using a field without @JsonProperty
        // We need an AnnotatedField; use TestBean field
        try {
            Field field = TestBean.class.getDeclaredField("unannotatedField");
            AnnotatedField af = AnnotatedField.construct(null, field, null);
            Integer idx = inst.findPropertyIndex(af);
            assertNull("Index should be null when not set", idx);
        } catch (Exception e) {
            fail("Setup error: " + e.getMessage());
        }
    }

    // ---------- Partition C: Defect-Targeted Branch (TypeCoercion1592) ----------

    @Test(timeout = 4000)
    public void testRefineSerializationTypePrimitiveToWrapper() throws Exception {
        // This targets the known defect: refineSerializationType with int to Integer
        JacksonAnnotationIntrospector inst = new JacksonAnnotationIntrospector();

        // Create a simple class with an int field annotated with @JsonSerialize(as = Integer.class)
        class IntFieldBean {
            @JsonSerialize(as = Integer.class)
            public int value;
        }

        Field field = IntFieldBean.class.getField("value");
        // Build a minimal MapperConfig using ObjectMapper.getSerializationConfig()
        // We need a MapperConfig; reuse from a simple ObjectMapper
        ObjectMapper mapper = new ObjectMapper();
        MapperConfig<?> config = mapper.getSerializationConfig();
        TypeFactory tf = config.getTypeFactory();
        JavaType baseType = tf.constructType(int.class);

        // Create AnnotatedField
        TypeResolutionContext resolver = new TypeResolutionContext.Basic(tf, config.getAnnotationIntrospector());
        AnnotatedField af = new AnnotatedField(resolver, field, null);

        // This call should NOT throw an exception
        JavaType refined = null;
        try {
            refined = inst.refineSerializationType(config, af, baseType);
        } catch (JsonMappingException e) {
            fail("refineSerializationType should not throw for int->Integer: " + e.getMessage());
        }
        assertNotNull("Returned type should not be null", refined);
        // Expect the type to be Integer (since we refined) or stay as int with static typing?
        // Actually the given annotation says as=Integer.class, so the refined type should be Integer
        // Check that the refined type's raw class is Integer
        assertEquals("Expected refined type to be Integer", Integer.class, refined.getRawClass());
    }

    @Test(timeout = 4000)
    public void testRefineDeserializationTypePrimitiveToWrapper() throws Exception {
        JacksonAnnotationIntrospector inst = new JacksonAnnotationIntrospector();

        class IntFieldBean {
            @JsonDeserialize(as = Integer.class)
            public int value;
        }

        Field field = IntFieldBean.class.getField("value");
        ObjectMapper mapper = new ObjectMapper();
        MapperConfig<?> config = mapper.getDeserializationConfig();
        TypeFactory tf = config.getTypeFactory();
        JavaType baseType = tf.constructType(int.class);

        TypeResolutionContext resolver = new TypeResolutionContext.Basic(tf, config.getAnnotationIntrospector());
        AnnotatedField af = new AnnotatedField(resolver, field, null);

        // This should also not throw (deserialization side)
        JavaType refined = null;
        try {
            refined = inst.refineDeserializationType(config, af, baseType);
        } catch (JsonMappingException e) {
            fail("refineDeserializationType should not throw for int->Integer: " + e.getMessage());
        }
        assertNotNull(refined);
        assertEquals("Expected refined deserialization type to be Integer", Integer.class, refined.getRawClass());
    }

    // ---------- Partition D: Exception & Defensive Guard Paths ----------

    @Test(timeout = 4000)
    public void testFindTypeResolverForContainerPropertyReturnsNull() {
        JacksonAnnotationIntrospector inst = new JacksonAnnotationIntrospector();
        // For a property that is container type, findPropertyTypeResolver should return null
        // We need an annotated member on a container field
        // Use a simple test: construct AnnotatedField for a Map property
        try {
            Field mapField = ContainerBean.class.getDeclaredField("mapField");
            AnnotatedField af = AnnotatedField.construct(null, mapField, null);
            ObjectMapper mapper = new ObjectMapper();
            MapperConfig<?> config = mapper.getSerializationConfig();
            JavaType containerType = config.getTypeFactory().constructType(Map.class);
            TypeResolverBuilder<?> b = inst.findPropertyTypeResolver(config, af, containerType);
            assertNull("For container property, should return null", b);
        } catch (Exception e) {
            fail("Setup error: " + e.getMessage());
        }
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testFindPropertyContentTypeResolverThrowsOnNullContent() {
        JacksonAnnotationIntrospector inst = new JacksonAnnotationIntrospector();
        // Call with a type that has no content type
        ObjectMapper mapper = new ObjectMapper();
        MapperConfig<?> config = mapper.getSerializationConfig();
        JavaType nonContainer = config.getTypeFactory().constructType(String.class);
        // We need an AnnotatedMember, dummy
        inst.findPropertyContentTypeResolver(config, null, nonContainer);
    }

    @Test(timeout = 4000)
    public void testIsAnnotationBundle() {
        JacksonAnnotationIntrospector inst = new JacksonAnnotationIntrospector();
        // Test with an annotation that is a bundle
        // @JacksonAnnotationsInside is a meta-annotation; create a dummy annotation?
        // Use a class that has a meta-annotation
        assertTrue("Should be bundle", inst.isAnnotationBundle(DummyBundle.class.getAnnotation(Deprecated.class)));
        // Actually easier: test with a real bundle annotation:
        // We can use @JsonIgnoreProperties? No. Let's just test with non-bundle
        assertFalse("Regular annotation should not be bundle", inst.isAnnotationBundle(Override.class.getAnnotation(Override.class)));
    }

    // ---------- Partition E: Object Lifecycle ----------

    @Test(timeout = 4000)
    public void testConstructorAndConfig() {
        JacksonAnnotationIntrospector inst = new JacksonAnnotationIntrospector();
        assertTrue("Default constructor should set _cfgConstructorPropertiesImpliesCreator to true",
                inst._cfgConstructorPropertiesImpliesCreator);
        inst.setConstructorPropertiesImpliesCreator(false);
        assertFalse("After setting to false, should be false", inst._cfgConstructorPropertiesImpliesCreator);
    }

    @Test(timeout = 4000)
    public void testReadResolve() {
        JacksonAnnotationIntrospector inst = new JacksonAnnotationIntrospector();
        inst._annotationsInside = null; // simulate deserialization
        inst.readResolve();
        assertNotNull("_annotationsInside should be re-created", inst._annotationsInside);
    }

    // ------------------------------------------------------------
    // Helper classes for annotations
    // ------------------------------------------------------------

    enum TestEnum {
        @JsonProperty("FOO") WITH_ANNOT,
        NO_ANNOT,
        @JsonProperty("BAR") WITH_ANNOT_BAR
    }

    @JsonRootName("rootTest")
    static class RootBean {
        public int x;
    }

    @JsonIgnoreProperties(value = {"a"}, ignoreUnknown = true)
    static class IgnoralsBean {
        public int a,b;
    }

    @JsonIgnoreType(true)
    static class IgnorableBean {
        public int x;
    }

    @JsonFilter("myFilter")
    static class FilterBean {
        public int x;
    }

    @JsonNaming(TestNamingStrategy.class)
    static class NamingBean {
        public int x;
    }

    static class TestNamingStrategy extends PropertyNamingStrategy {
        @Override
        public String nameForField(MapperConfig<?> config, AnnotatedField field, String defaultName) {
            return defaultName;
        }
    }

    @JsonClassDescription("A test bean")
    static class DescBean {
        public int x;
    }

    @JsonPropertyOrder(alphabetic = false)
    static class OrderFalseBean {
        public int a;
        public int b;
    }

    static class TestBean {
        @JsonSerialize(as = Integer.class)
        public int value;

        public int unannotatedField;
    }

    static class ContainerBean {
        public Map<String, String> mapField;
    }

    @java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE})
    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    @JacksonAnnotationsInside
    @interface DummyBundle {
    }

    @DummyBundle
    static class BundleAnnot {
    }
}