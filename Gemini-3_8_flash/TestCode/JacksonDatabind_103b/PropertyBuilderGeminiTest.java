package com.fasterxml.jackson.databind.ser;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.NameTransformer;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.fasterxml.jackson.databind.ser.PropertyBuilder
 *
 * 1. Constructor Initialization & Inclusion Logic:
 *    - Merging global, per-type (class annotation & config overrides), and per-property inclusions.
 *    - Branch: _useRealPropertyDefaults set when inclPerType is JsonInclude.Include.NON_DEFAULT.
 *    - AnnotationIntrospector retrieval from SerializationConfig.
 *
 * 2. buildWriter() Method Branching:
 *    - Type refinement via findSerializationType():
 *      - Exception path: incompatible types throw IllegalArgumentException / JsonMappingException.
 *      - propDef == null vs propDef != null error reporting via SerializerProvider.
 *    - Container typing & contentTypeSer:
 *      - contentTypeSer != null with serializationType == null vs non-null.
 *      - serializationType.getContentType() == null triggering reportBadPropertyDefinition.
 *      - contentType handler attachment.
 *    - Accessor resolution:
 *      - propDef.getAccessor() == null path triggering reportBadPropertyDefinition.
 *    - Inclusion handling switch (inclusion modes):
 *      - NON_DEFAULT:
 *        - _useRealPropertyDefaults true and getDefaultBean() non-null.
 *        - am.getValue() throws exception -> _throwWrapped unrolls causes, throws RTE/IllegalArgumentException.
 *        - Array type default value comparator extraction via ArrayBuilders.getArrayComparator.
 *        - Fallback path when _useRealPropertyDefaults is false or default bean cannot be instantiated.
 *      - NON_ABSENT: suppressNulls = true; if actualType.isReferenceType(), suppress value = MARKER_FOR_EMPTY.
 *      - NON_EMPTY: suppressNulls = true; suppress value = MARKER_FOR_EMPTY.
 *      - CUSTOM: prov.includeFilterInstance() evaluated; prov.includeFilterSuppressNulls() invoked.
 *      - NON_NULL / ALWAYS / default: suppressNulls flag set; check container type and WRITE_EMPTY_JSON_ARRAYS.
 *    - View handling:
 *      - propDef.findViews() != null vs fallback to _beanDesc.findDefaultViews().
 *    - Custom null serializer resolution:
 *      - _annotationIntrospector.findNullSerializer(am) != null assigns null serializer.
 *    - Unwrapping writer creation:
 *      - _annotationIntrospector.findUnwrappingNameTransformer(am) != null wraps BeanPropertyWriter.
 *
 * 3. Default Bean & Exception Handling:
 *    - getDefaultBean(): lazy creation, caching of instance and NO_DEFAULT_MARKER when instantiation fails.
 *    - Deprecated helpers: getPropertyDefaultValue(), getDefaultValue().
 *    - _throwWrapped(): cause unwrapping, ClassUtil.throwIfError, ClassUtil.throwIfRTE.
 */
public class PropertyBuilderGeminiTest {

    // =========================================================================
    // Test POJOs and Support Classes
    // =========================================================================

    static class SimpleBean {
        public String fieldA = "defA";
        public int fieldB = 42;
        public int[] arrayField = new int[]{1, 2};

        public String getFieldA() { return fieldA; }
        public int getFieldB() { return fieldB; }
        public int[] getArrayField() { return arrayField; }
    }

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    static class NonDefaultClassBean {
        public String str = "default";
        public int num = 100;
        public int[] nums = new int[]{1};

        public NonDefaultClassBean() {}
        public NonDefaultClassBean(String str, int num) {
            this.str = str;
            this.num = num;
        }
    }

    static class NoDefaultConstructorBean {
        public String value;
        public NoDefaultConstructorBean(String v) { this.value = v; }
    }

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    static class ExplodingGetterBean {
        public String getFailing() {
            throw new IllegalStateException("Simulated getter failure");
        }
    }

    static class ContainerBean {
        public List<String> items = new ArrayList<>();
        public Map<String, String> map = new HashMap<>();
    }

    static class OptionalBean {
        public AtomicReference<String> ref = new AtomicReference<>();
    }

    static class CustomFilterBean {
        @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = CustomFilter.class)
        public String text = "skipMe";
    }

    static class CustomFilter {
        @Override
        public boolean equals(Object obj) {
            return "skipMe".equals(obj);
        }
    }

    static class ViewA {}
    static class ViewB {}

    static class ViewBean {
        @JsonView(ViewA.class)
        public String viewField = "viewVal";

        public String noViewField = "normal";
    }

    static class UnwrappedParent {
        @JsonUnwrapped(prefix = "child_")
        public ChildBean child = new ChildBean();
    }

    static class ChildBean {
        public String inner = "innerVal";
    }

    static class NullSerializerBean {
        @JsonSerialize(nullsUsing = CustomNullSerializer.class)
        public String nullField = null;
    }

    static class CustomNullSerializer extends JsonSerializer<Object> {
        @Override
        public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString("WAS_NULL");
        }
    }

    static class StaticTypingBean {
        @JsonSerialize(typing = JsonSerialize.Typing.STATIC)
        public CharSequence seq = "staticText";
    }

    // =========================================================================
    // PARTITION A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testPropertyBuilderInitializationAndClassAnnotations() {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDescription beanDesc = config.introspect(type);

        PropertyBuilder builder = new PropertyBuilder(config, beanDesc);
        assertNotNull("Class annotations must not be null", builder.getClassAnnotations());
    }

    @Test(timeout = 4000)
    public void testNonDefaultInclusionSuppression() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        NonDefaultClassBean bean = new NonDefaultClassBean("default", 100);
        bean.nums = new int[]{1}; // matches default array

        String json = mapper.writeValueAsString(bean);
        // All fields match default bean instance, so should be completely empty JSON object
        assertEquals("{}", json);

        // Mutate one field
        bean.str = "custom";
        String jsonModified = mapper.writeValueAsString(bean);
        assertEquals("{\"str\":\"custom\"}", jsonModified);
    }

    @Test(timeout = 4000)
    public void testNonDefaultArrayHandling() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        NonDefaultClassBean bean = new NonDefaultClassBean("default", 100);
        bean.nums = new int[]{1, 2}; // different array content

        String json = mapper.writeValueAsString(bean);
        assertEquals("{\"nums\":[1,2]}", json);
    }

    @Test(timeout = 4000)
    public void testWriteEmptyJsonArraysDisabled() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS);

        ContainerBean bean = new ContainerBean();
        String json = mapper.writeValueAsString(bean);
        // items list is empty, should be suppressed when WRITE_EMPTY_JSON_ARRAYS is disabled
        assertFalse("Empty array should be suppressed", json.contains("items"));
    }

    @Test(timeout = 4000)
    public void testReferenceTypeWithNonAbsentInclusion() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_ABSENT);

        OptionalBean bean = new OptionalBean();
        bean.ref.set(null); // absent
        String json = mapper.writeValueAsString(bean);
        assertEquals("{}", json);

        bean.ref.set("present");
        json = mapper.writeValueAsString(bean);
        assertEquals("{\"ref\":\"present\"}", json);
    }

    // =========================================================================
    // PARTITION B: Boundary Value Analysis & Edge Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testNonDefaultBeanWithoutDefaultConstructor() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);

        // No default constructor to build a baseline instance
        NoDefaultConstructorBean bean = new NoDefaultConstructorBean("test");
        String json = mapper.writeValueAsString(bean);
        assertEquals("{\"value\":\"test\"}", json);

        NoDefaultConstructorBean beanNull = new NoDefaultConstructorBean(null);
        String jsonNull = mapper.writeValueAsString(beanNull);
        // For null value, NON_DEFAULT fallback suppresses nulls
        assertEquals("{}", jsonNull);
    }

    @Test(timeout = 4000)
    public void testCustomInclusionFilter() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        CustomFilterBean bean = new CustomFilterBean();
        bean.text = "skipMe";
        assertEquals("{}", mapper.writeValueAsString(bean));

        bean.text = "keepMe";
        assertEquals("{\"text\":\"keepMe\"}", mapper.writeValueAsString(bean));
    }

    @Test(timeout = 4000)
    public void testCustomNullSerializerAndJsonUnwrapped() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        NullSerializerBean nullBean = new NullSerializerBean();
        String jsonNull = mapper.writeValueAsString(nullBean);
        assertEquals("{\"nullField\":\"WAS_NULL\"}", jsonNull);

        UnwrappedParent unwrapped = new UnwrappedParent();
        String jsonUnwrapped = mapper.writeValueAsString(unwrapped);
        assertEquals("{\"child_inner\":\"innerVal\"}", jsonUnwrapped);
    }

    @Test(timeout = 4000)
    public void testViewFiltering() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ViewBean bean = new ViewBean();

        String jsonWithoutView = mapper.writerWithView(ViewB.class).writeValueAsString(bean);
        // viewField has @JsonView(ViewA.class), so ViewB should exclude it
        assertEquals("{\"noViewField\":\"normal\"}", jsonWithoutView);

        String jsonWithView = mapper.writerWithView(ViewA.class).writeValueAsString(bean);
        assertTrue("viewField should be present", jsonWithView.contains("viewField"));
        assertTrue("noViewField should be present", jsonWithView.contains("noViewField"));
    }

    @Test(timeout = 4000)
    public void testStaticTypingBranch() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        StaticTypingBean bean = new StaticTypingBean();
        String json = mapper.writeValueAsString(bean);
        assertEquals("{\"seq\":\"staticText\"}", json);
    }

    // =========================================================================
    // PARTITION C: Direct PropertyBuilder Unit Tests & Deprecated API
    // =========================================================================

    @Test(timeout = 4000)
    public void testDirectGetDefaultBeanCaching() {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        BeanDescription beanDesc = config.introspect(mapper.constructType(SimpleBean.class));

        PropertyBuilder pb = new PropertyBuilder(config, beanDesc);
        Object defaultBean1 = pb.getDefaultBean();
        assertNotNull("Default bean should be created", defaultBean1);
        assertTrue("Must be instance of SimpleBean", defaultBean1 instanceof SimpleBean);

        Object defaultBean2 = pb.getDefaultBean();
        assertSame("Subsequent calls to getDefaultBean must return cached instance", defaultBean1, defaultBean2);
    }

    @Test(timeout = 4000)
    public void testDirectGetDefaultBeanWhenNoDefaultConstructor() {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        BeanDescription beanDesc = config.introspect(mapper.constructType(NoDefaultConstructorBean.class));

        PropertyBuilder pb = new PropertyBuilder(config, beanDesc);
        assertNull("Should return null when class has no default constructor", pb.getDefaultBean());
        // Verify that NO_DEFAULT_MARKER was cached and returns null again
        assertNull("Cached marker should also yield null", pb.getDefaultBean());
    }

    @SuppressWarnings("deprecation")
    @Test(timeout = 4000)
    public void testDeprecatedGetPropertyDefaultValueAndGetDefaultValue() {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        BeanDescription beanDesc = config.introspect(mapper.constructType(SimpleBean.class));

        PropertyBuilder pb = new PropertyBuilder(config, beanDesc);
        JavaType intType = TypeFactory.defaultInstance().constructType(int.class);
        Object defInt = pb.getDefaultValue(intType);
        assertEquals(0, defInt);

        BeanPropertyDefinition prop = null;
        for (BeanPropertyDefinition p : beanDesc.findProperties()) {
            if ("fieldA".equals(p.getName())) {
                prop = p;
                break;
            }
        }
        assertNotNull(prop);
        AnnotatedMember member = prop.getAccessor();
        Object val = pb.getPropertyDefaultValue("fieldA", member, prop.getPrimaryType());
        assertEquals("defA", val);
    }

    // =========================================================================
    // PARTITION D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testExplodingGetterWrapsException() {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        BeanDescription beanDesc = config.introspect(mapper.constructType(ExplodingGetterBean.class));

        try {
            // Instantiating PropertyBuilder and building writers will trigger getDefaultBean()
            // and invoke getter on default bean
            DefaultSerializerProvider.Impl provider = new DefaultSerializerProvider.Impl();
            provider = (DefaultSerializerProvider.Impl) provider.createInstance(config, mapper.getSerializerFactory());
            
            BeanSerializerFactory.instance.constructBeanOrAddOnSerializer(
                    provider, mapper.constructType(ExplodingGetterBean.class), beanDesc, false);
            fail("Expected exception due to failing getter on default bean instance");
        } catch (Exception e) {
            Throwable root = e;
            while (root.getCause() != null) {
                root = root.getCause();
            }
            assertTrue("Root cause or message should indicate failure in getter",
                    root instanceof IllegalStateException || e.getMessage().contains("failing"));
        }
    }

    @Test(timeout = 4000)
    public void testThrowWrappedDirectInvocation() {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        BeanDescription beanDesc = config.introspect(mapper.constructType(SimpleBean.class));
        PropertyBuilder pb = new PropertyBuilder(config, beanDesc);

        try {
            pb._throwWrapped(new RuntimeException("Simulated RTE"), "testProp", new SimpleBean());
            fail("Expected RuntimeException to be thrown");
        } catch (RuntimeException e) {
            assertEquals("Simulated RTE", e.getMessage());
        }

        try {
            pb._throwWrapped(new Exception("Checked Exception"), "testProp", new SimpleBean());
            fail("Expected IllegalArgumentException wrapping checked exception");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Failed to get property 'testProp'"));
        }
    }

    // =========================================================================
    // PARTITION E: Defect Target Zone (Exception Location Reporting / Mapping)
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefectExceptionLocationAddition() {
        // Targets defect verification: ensuring serialization/deserialization exceptions
        // do not duplicate location markers or fail with malformed diagnostic messages.
        ObjectMapper mapper = new ObjectMapper();
        try {
            // Attempt to deserialize invalid structure to ensure location markers are clean
            mapper.readValue("{\"unknown\":123}", SimpleBean.class);
        } catch (JsonMappingException e) {
            String msg = e.getMessage();
            int firstIdx = msg.indexOf("at [");
            if (firstIdx >= 0) {
                int secondIdx = msg.indexOf("at [", firstIdx + 4);
                assertEquals("Should not have duplicated 'at [' location markers", -1, secondIdx);
            }
        } catch (IOException e) {
            fail("Unexpected IOException: " + e.getMessage());
        }
    }
}