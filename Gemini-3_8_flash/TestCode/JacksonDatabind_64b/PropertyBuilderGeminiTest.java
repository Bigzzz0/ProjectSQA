package com.fasterxml.jackson.databind.ser;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.fasterxml.jackson.databind.ser.PropertyBuilder
 *
 * Target Defect:
 * - com.fasterxml.jackson.databind.filter.JsonIncludeTest::testIssue1351
 *   Failure Symptom: ComparisonFailure: expected:<{[]}> but was:<{["str":null]}>
 *   Root Cause: When a POJO has @JsonInclude(Include.NON_DEFAULT) but lacks a default no-argument
 *   constructor, _useRealPropertyDefaults is true, but getDefaultBean() returns null. PropertyBuilder's
 *   getPropertyDefaultValue() falls back to getDefaultValue(actualType) (e.g., "" for String),
 *   while suppressNulls remains false! Consequently, a null value does not equal "" and is not suppressed,
 *   causing unexpected serializations such as {"str":null} instead of {}.
 *
 * Logic Branches Covered:
 * - Constructor: Merge of global defaults, per-type inclusion, and determination of _useRealPropertyDefaults.
 * - getClassAnnotations(): Access to BeanDescription annotations.
 * - getDefaultValue(): Coverage across all 8 primitives, Container/Reference types (NON_EMPTY),
 *   String (""), and general Objects (null).
 * - getDefaultBean(): Cache behavior, normal instantiation, and NO_DEFAULT_MARKER handling when no-arg ctor is missing.
 * - getPropertyDefaultValue(): Normal access via reflection, fallback to getDefaultValue() when defaultBean is null.
 * - _throwWrapped(): Error unwrapping, RuntimeException unwrapping, and checked Exception wrapping into IllegalArgumentException.
 * - findSerializationType(): useStaticTyping flag, subtype/supertype checking, incompatible concrete-type error,
 *   typing annotation handling (STATIC vs DYNAMIC).
 * - buildWriter(): Inclusion modes (NON_DEFAULT, NON_ABSENT, NON_EMPTY, NON_NULL, ALWAYS, USE_DEFAULTS),
 *   Array comparator handling, custom null serializers, unwrapped properties, and WRITE_EMPTY_JSON_ARRAYS.
 */
public class PropertyBuilderGeminiTest {

    // =========================================================================
    // Test POJOs and Helper Classes
    // =========================================================================

    public static class SimpleBean {
        public int count = 42;
        public String name = "default";
        public List<String> items = Arrays.asList("a", "b");
    }

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public static class Issue1351Bean {
        public final String str;

        @JsonCreator
        public Issue1351Bean(@JsonProperty("str") String str) {
            this.str = str;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public static class NonDefaultPojo {
        public int x = 10;
        public String text = "abc";
        public int[] array = new int[]{1, 2};
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class NonNullBean {
        public String present = "value";
        public String missing = null;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public static class NonEmptyBean {
        public String emptyStr = "";
        public List<String> emptyList = Collections.emptyList();
        public String text = "non-empty";
    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public static class NonAbsentBean {
        public AtomicReference<String> absentRef = new AtomicReference<String>();
        public AtomicReference<String> presentRef = new AtomicReference<String>("content");
        public String nullString = null;
    }

    public static class PropertyInclusionOverrideBean {
        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        public int count = 0;

        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        public String text = "";

        @JsonInclude(JsonInclude.Include.USE_DEFAULTS)
        public String reset = null;
    }

    public static class ContainerArrayBean {
        public List<String> emptyList = Collections.emptyList();
    }

    public static class CustomNullSerializer extends JsonSerializer<Object> {
        @Override
        public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString("CUSTOM_NULL");
        }
    }

    public static class NullSerializerBean {
        @JsonSerialize(nullsUsing = CustomNullSerializer.class)
        public String customNullVal = null;
    }

    public static class UnwrappedChild {
        public int x = 5;
        public int y = 10;
    }

    public static class UnwrappedParent {
        @JsonUnwrapped
        public UnwrappedChild child = new UnwrappedChild();
    }

    public static class StaticTypingBean {
        @JsonSerialize(typing = JsonSerialize.Typing.STATIC)
        public CharSequence seq = "static";
    }

    public static class DynamicTypingBean {
        @JsonSerialize(typing = JsonSerialize.Typing.DYNAMIC)
        public CharSequence seq = "dynamic";
    }

    public static class SuperTypeBean {
        @JsonSerialize(as = CharSequence.class)
        public String text = "super";
    }

    public static class SubTypeBean {
        @JsonSerialize(as = String.class)
        public CharSequence text = "sub";
    }

    public static class IncompatibleTypeBean {
        @JsonSerialize(as = Integer.class)
        public String text = "invalid";
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetClassAnnotations() {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        BeanDescription desc = config.introspect(mapper.constructType(SimpleBean.class));
        PropertyBuilder pb = new PropertyBuilder(config, desc);

        assertNotNull("Annotations should not be null", pb.getClassAnnotations());
        assertEquals(desc.getClassAnnotations().size(), pb.getClassAnnotations().size());
    }

    @Test(timeout = 4000)
    public void testDefaultBeanCaching() {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        BeanDescription desc = config.introspect(mapper.constructType(SimpleBean.class));
        PropertyBuilder pb = new PropertyBuilder(config, desc);

        Object bean1 = pb.getDefaultBean();
        assertNotNull("Default bean should be instantiated", bean1);
        assertTrue("Bean should be of expected type", bean1 instanceof SimpleBean);

        Object bean2 = pb.getDefaultBean();
        assertSame("Subsequent calls to getDefaultBean must return the cached instance", bean1, bean2);
    }

    @Test(timeout = 4000)
    public void testGetDefaultBeanWithoutDefaultConstructor() {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        BeanDescription desc = config.introspect(mapper.constructType(Issue1351Bean.class));
        PropertyBuilder pb = new PropertyBuilder(config, desc);

        assertNull("Default bean must be null when no-arg constructor is missing", pb.getDefaultBean());
        assertNull("Cached NO_DEFAULT_MARKER must resolve to null repeatedly", pb.getDefaultBean());
    }

    @Test(timeout = 4000)
    public void testGetPropertyDefaultValueNormal() {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        BeanDescription desc = config.introspect(mapper.constructType(SimpleBean.class));
        PropertyBuilder pb = new PropertyBuilder(config, desc);

        BeanPropertyDefinition prop = desc.findProperties().get(0);
        AnnotatedMember member = prop.getAccessor();
        Object val = pb.getPropertyDefaultValue(prop.getName(), member, member.getType());
        assertNotNull("Property default value should be extracted from default bean", val);
    }

    @Test(timeout = 4000)
    public void testGetPropertyDefaultValueWhenDefaultBeanIsNull() {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        BeanDescription desc = config.introspect(mapper.constructType(Issue1351Bean.class));
        PropertyBuilder pb = new PropertyBuilder(config, desc);

        BeanPropertyDefinition prop = desc.findProperties().get(0);
        AnnotatedMember member = prop.getAccessor();
        Object val = pb.getPropertyDefaultValue(prop.getName(), member, member.getType());
        assertEquals("Fallback to getDefaultValue for String when bean is null should be empty string", "", val);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetDefaultValueAllPrimitives() {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        BeanDescription desc = config.introspect(mapper.constructType(SimpleBean.class));
        PropertyBuilder pb = new PropertyBuilder(config, desc);
        TypeFactory tf = mapper.getTypeFactory();

        assertEquals(0, pb.getDefaultValue(tf.constructType(int.class)));
        assertEquals(false, pb.getDefaultValue(tf.constructType(boolean.class)));
        assertEquals((byte) 0, pb.getDefaultValue(tf.constructType(byte.class)));
        assertEquals((short) 0, pb.getDefaultValue(tf.constructType(short.class)));
        assertEquals((char) 0, pb.getDefaultValue(tf.constructType(char.class)));
        assertEquals(0L, pb.getDefaultValue(tf.constructType(long.class)));
        assertEquals(0.0f, pb.getDefaultValue(tf.constructType(float.class)));
        assertEquals(0.0d, pb.getDefaultValue(tf.constructType(double.class)));
    }

    @Test(timeout = 4000)
    public void testGetDefaultValueStructuredAndObjectTypes() {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        BeanDescription desc = config.introspect(mapper.constructType(SimpleBean.class));
        PropertyBuilder pb = new PropertyBuilder(config, desc);
        TypeFactory tf = mapper.getTypeFactory();

        assertEquals("", pb.getDefaultValue(tf.constructType(String.class)));
        assertEquals(JsonInclude.Include.NON_EMPTY, pb.getDefaultValue(tf.constructType(List.class)));
        assertEquals(JsonInclude.Include.NON_EMPTY, pb.getDefaultValue(tf.constructType(Map.class)));
        assertEquals(JsonInclude.Include.NON_EMPTY, pb.getDefaultValue(tf.constructType(int[].class)));
        assertEquals(JsonInclude.Include.NON_EMPTY, pb.getDefaultValue(tf.constructType(AtomicReference.class)));
        assertNull(pb.getDefaultValue(tf.constructType(Object.class)));
    }

    @Test(timeout = 4000)
    public void testFindSerializationTypeTypingAnnotation() throws JsonMappingException {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();

        // STATIC typing annotation
        BeanDescription staticDesc = config.introspect(mapper.constructType(StaticTypingBean.class));
        PropertyBuilder pbStatic = new PropertyBuilder(config, staticDesc);
        AnnotatedMember amStatic = staticDesc.findProperties().get(0).getAccessor();
        JavaType staticType = pbStatic.findSerializationType(amStatic, false, amStatic.getType());
        assertNotNull(staticType);
        assertTrue(staticType.useStaticTyping());

        // DYNAMIC typing annotation overriding default static typing flag
        BeanDescription dynamicDesc = config.introspect(mapper.constructType(DynamicTypingBean.class));
        PropertyBuilder pbDynamic = new PropertyBuilder(config, dynamicDesc);
        AnnotatedMember amDynamic = dynamicDesc.findProperties().get(0).getAccessor();
        JavaType dynamicType = pbDynamic.findSerializationType(amDynamic, true, amDynamic.getType());
        assertNull("Dynamic typing override should return null when no static typing applies", dynamicType);
    }

    @Test(timeout = 4000)
    public void testFindSerializationTypeHierarchyRefinement() throws JsonMappingException {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();

        // Refine to super-type (String -> CharSequence)
        BeanDescription superDesc = config.introspect(mapper.constructType(SuperTypeBean.class));
        PropertyBuilder pbSuper = new PropertyBuilder(config, superDesc);
        AnnotatedMember amSuper = superDesc.findProperties().get(0).getAccessor();
        JavaType superType = pbSuper.findSerializationType(amSuper, false, amSuper.getType());
        assertNotNull(superType);
        assertEquals(CharSequence.class, superType.getRawClass());
        assertTrue(superType.useStaticTyping());

        // Refine to sub-type (CharSequence -> String)
        BeanDescription subDesc = config.introspect(mapper.constructType(SubTypeBean.class));
        PropertyBuilder pbSub = new PropertyBuilder(config, subDesc);
        AnnotatedMember amSub = subDesc.findProperties().get(0).getAccessor();
        JavaType subType = pbSub.findSerializationType(amSub, false, amSub.getType());
        assertNotNull(subType);
        assertEquals(String.class, subType.getRawClass());
        assertTrue(subType.useStaticTyping());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Issue 1351)
    // =========================================================================

    /**
     * Dedicated defect reproduction test for Jackson-databind issue 1351.
     * When a bean defines @JsonInclude(JsonInclude.Include.NON_DEFAULT) and lacks a default
     * no-argument constructor, serializing a property with null value must suppress that
     * property entirely (producing "{}"), rather than emitting {"str":null}.
     */
    @Test(timeout = 4000)
    public void testIssue1351() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Issue1351Bean bean = new Issue1351Bean(null);
        String json = mapper.writeValueAsString(bean);
        assertEquals("Expected empty JSON object suppression for null with NON_DEFAULT on class without default ctor", "{}", json);
    }

    @Test(timeout = 4000)
    public void testNonDefaultSuppressionWithRealDefaults() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        NonDefaultPojo defaultPojo = new NonDefaultPojo();
        // Default values: x=10, text="abc", array=[1, 2] -> all should be suppressed
        String jsonDefault = mapper.writeValueAsString(defaultPojo);
        assertEquals("{}", jsonDefault);

        // Modified values should not be suppressed
        NonDefaultPojo modifiedPojo = new NonDefaultPojo();
        modifiedPojo.x = 20;
        modifiedPojo.text = "xyz";
        modifiedPojo.array = new int[]{1, 3};
        String jsonModified = mapper.writeValueAsString(modifiedPojo);
        assertTrue(jsonModified.contains("\"x\":20"));
        assertTrue(jsonModified.contains("\"text\":\"xyz\""));
        assertTrue(jsonModified.contains("\"array\":[1,3]"));
    }

    @Test(timeout = 4000)
    public void testPerPropertyNonDefaultAndUseDefaults() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        PropertyInclusionOverrideBean bean = new PropertyInclusionOverrideBean();
        bean.count = 0;   // primitive int default -> suppressed
        bean.text = "";   // String default -> suppressed
        bean.reset = null; // USE_DEFAULTS reverts to ALWAYS -> included as null

        String json = mapper.writeValueAsString(bean);
        assertFalse(json.contains("\"count\""));
        assertFalse(json.contains("\"text\""));
        assertTrue("USE_DEFAULTS should cause null to be written as null", json.contains("\"reset\":null"));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testThrowWrappedWithDirectAndNestedError() {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        BeanDescription desc = config.introspect(mapper.constructType(SimpleBean.class));
        PropertyBuilder pb = new PropertyBuilder(config, desc);

        InvocationTargetException nestedError = new InvocationTargetException(new OutOfMemoryError("OOM"));
        try {
            pb._throwWrapped(nestedError, "testProp", new Object());
            fail("Should rethrow unwrapped Error");
        } catch (OutOfMemoryError err) {
            assertEquals("OOM", err.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testThrowWrappedWithDirectAndNestedRuntimeException() {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        BeanDescription desc = config.introspect(mapper.constructType(SimpleBean.class));
        PropertyBuilder pb = new PropertyBuilder(config, desc);

        InvocationTargetException nestedRuntime = new InvocationTargetException(
                new InvocationTargetException(new IllegalStateException("Nested state problem")));
        try {
            pb._throwWrapped(nestedRuntime, "testProp", new Object());
            fail("Should rethrow unwrapped RuntimeException");
        } catch (IllegalStateException ex) {
            assertEquals("Nested state problem", ex.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testThrowWrappedWithCheckedException() {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        BeanDescription desc = config.introspect(mapper.constructType(SimpleBean.class));
        PropertyBuilder pb = new PropertyBuilder(config, desc);

        Exception checked = new Exception("Checked failure");
        try {
            pb._throwWrapped(checked, "dummyProperty", "TargetObject");
            fail("Should wrap checked Exception in IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
            assertTrue(iae.getMessage().contains("Failed to get property 'dummyProperty'"));
            assertTrue(iae.getMessage().contains("TargetObject"));
        }
    }

    @Test(timeout = 4000)
    public void testFindSerializationTypeIncompatibleAnnotation() throws JsonMappingException {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        BeanDescription desc = config.introspect(mapper.constructType(IncompatibleTypeBean.class));
        PropertyBuilder pb = new PropertyBuilder(config, desc);
        AnnotatedMember am = desc.findProperties().get(0).getAccessor();

        try {
            pb.findSerializationType(am, false, am.getType());
            fail("Should fail when annotation references an incompatible type");
        } catch (IllegalArgumentException | JsonMappingException e) {
            assertTrue(e.getMessage().contains("Integer not a super-type") || e.getMessage().contains("not related"));
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Inclusion Variations
    // =========================================================================

    @Test(timeout = 4000)
    public void testInclusionNonNull() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        NonNullBean bean = new NonNullBean();
        String json = mapper.writeValueAsString(bean);

        assertTrue(json.contains("\"present\":\"value\""));
        assertFalse(json.contains("missing"));
    }

    @Test(timeout = 4000)
    public void testInclusionNonEmpty() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        NonEmptyBean bean = new NonEmptyBean();
        String json = mapper.writeValueAsString(bean);

        assertFalse(json.contains("emptyStr"));
        assertFalse(json.contains("emptyList"));
        assertTrue(json.contains("\"text\":\"non-empty\""));
    }

    @Test(timeout = 4000)
    public void testInclusionNonAbsent() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        NonAbsentBean bean = new NonAbsentBean();
        String json = mapper.writeValueAsString(bean);

        assertFalse(json.contains("absentRef"));
        assertFalse(json.contains("nullString"));
        assertTrue(json.contains("\"presentRef\":\"content\""));
    }

    @Test(timeout = 4000)
    public void testSuppressEmptyContainerArraysFeature() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS);

        ContainerArrayBean bean = new ContainerArrayBean();
        String json = mapper.writeValueAsString(bean);
        assertEquals("{}", json);
    }

    @Test(timeout = 4000)
    public void testCustomNullSerializerAndUnwrapping() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // Custom Null Serializer
        NullSerializerBean nullBean = new NullSerializerBean();
        String jsonNull = mapper.writeValueAsString(nullBean);
        assertTrue(jsonNull.contains("\"customNullVal\":\"CUSTOM_NULL\""));

        // Unwrapped property writer
        UnwrappedParent parent = new UnwrappedParent();
        String jsonUnwrapped = mapper.writeValueAsString(parent);
        assertTrue(jsonUnwrapped.contains("\"x\":5"));
        assertTrue(jsonUnwrapped.contains("\"y\":10"));
        assertFalse(jsonUnwrapped.contains("child"));
    }
}