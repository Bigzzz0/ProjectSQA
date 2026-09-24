/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.fasterxml.jackson.databind.ser.PropertyBuilder
 *
 * Decision / Branch Coverage Points:
 * - Constructor:
 *     * Initialization with _beanDesc.findPropertyInclusion
 *     * _annotationIntrospector retrieval from config
 * - buildWriter():
 *     * serializationType calculation (null vs non-null via findSerializationType)
 *     * contentTypeSer != null:
 *         - serializationType == null -> fallback to declaredType
 *         - ct == null -> throws IllegalStateException
 *         - ct != null -> withContentTypeHandler(contentTypeSer)
 *     * inclusion value handling:
 *         - USE_DEFAULTS -> coerced to ALWAYS
 *         - NON_DEFAULT:
 *             - _defaultInclusion == NON_DEFAULT -> getPropertyDefaultValue()
 *             - _defaultInclusion != NON_DEFAULT -> getDefaultValue()
 *             - valueToSuppress == null -> suppressNulls = true
 *             - valueToSuppress isArray -> ArrayBuilders.getArrayComparator()
 *         - NON_ABSENT:
 *             - suppressNulls = true
 *             - declaredType.isReferenceType() -> MARKER_FOR_EMPTY
 *             - declaredType not reference type
 *         - NON_EMPTY:
 *             - suppressNulls = true, valueToSuppress = MARKER_FOR_EMPTY
 *         - NON_NULL:
 *             - suppressNulls = true
 *         - ALWAYS / default:
 *             - containerType && !WRITE_EMPTY_JSON_ARRAYS -> MARKER_FOR_EMPTY
 *             - containerType && WRITE_EMPTY_JSON_ARRAYS -> no suppression
 *     * Custom null serializer check (findNullSerializer != null vs null)
 *     * Unwrapping inspector check (findUnwrappingNameTransformer != null vs null)
 * - findSerializationType():
 *     * refineSerializationType changes secondary != declaredType
 *         - serClass isAssignableFrom rawDeclared (valid super-type)
 *         - rawDeclared isAssignableFrom serClass (subtype relaxation)
 *         - neither -> throws IllegalArgumentException
 *     * findSerializationTyping overrides (STATIC vs DYNAMIC vs DEFAULT_TYPING vs null)
 *     * useStaticTyping true (withStaticTyping) vs false (null)
 * - getDefaultBean():
 *     * _defaultBean cached vs null
 *     * instantiateBean returns null -> sets NO_DEFAULT_MARKER -> returns null
 * - getPropertyDefaultValue():
 *     * defaultBean == null -> fallback to getDefaultValue(type)
 *     * member.getValue throws exception -> invokes _throwWrapped()
 * - getDefaultValue():
 *     * primitive / wrapper type -> ClassUtil.defaultValue
 *     * containerType or referenceType -> JsonInclude.Include.NON_EMPTY
 *     * String -> ""
 *     * other Object -> null
 * - _throwWrapped():
 *     * unwrap causes, check Error vs RuntimeException vs wrap into IllegalArgumentException
 *
 * Defect Targeting:
 * - Issue [databind#1256] (TestJDKAtomicTypes::testEmpty1256):
 *   Serialization of empty AtomicReference / Reference types under NON_EMPTY, NON_ABSENT,
 *   or NON_DEFAULT inclusions to prevent outputting null fields (e.g. {"a":null} -> {}).
 */

package com.fasterxml.jackson.databind.ser;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class PropertyBuilderGeminiTest {

    // =========================================================================
    // Test Dummy POJOs
    // =========================================================================

    public static class EmptyPojo {
    }

    public static class SimplePrimitivesPojo {
        public int intVal = 42;
        public boolean boolVal = true;
        public double doubleVal = 3.14;
        public String strVal = "hello";
    }

    public static class ArraysPojo {
        public int[] intArray = new int[]{1, 2, 3};
        public String[] strArray = new String[]{"a", "b"};
    }

    public static class ContainersPojo {
        public List<String> list = new ArrayList<String>();
        public Map<String, String> map = new HashMap<String, String>();
    }

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public static class ClassNonDefaultPojo {
        public String field1 = "def1";
        public String field2;
        public int intField = 0;
    }

    public static class PropIncludePojo {
        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        public String nonDef = "";

        @JsonInclude(JsonInclude.Include.NON_ABSENT)
        public AtomicReference<String> nonAbsentRef = new AtomicReference<String>();

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        public List<String> nonEmptyList = new ArrayList<String>();

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public String nonNullVal = null;

        @JsonInclude(JsonInclude.Include.ALWAYS)
        public String alwaysVal = null;

        @JsonInclude(JsonInclude.Include.USE_DEFAULTS)
        public String useDefaultsVal = null;
    }

    public static class AtomicContext1256 {
        public AtomicReference<String> a = new AtomicReference<String>();
    }

    public static class StaticTypingPojo {
        @JsonSerialize(typing = JsonSerialize.Typing.STATIC)
        public CharSequence staticSeq = "static";

        @JsonSerialize(typing = JsonSerialize.Typing.DYNAMIC)
        public CharSequence dynamicSeq = "dynamic";

        @JsonSerialize(typing = JsonSerialize.Typing.DEFAULT_TYPING)
        public CharSequence defaultSeq = "default";
    }

    public static class SuperTypeRefinePojo {
        @JsonSerialize(as = CharSequence.class)
        public String validSuper = "text";
    }

    public static class SubTypeRefinePojo {
        @JsonSerialize(as = String.class)
        public CharSequence validSub = "subText";
    }

    public static class IncompatibleTypeRefinePojo {
        @JsonSerialize(as = Integer.class)
        public String invalidType = "fail";
    }

    public static class ThrowingGetterPojo {
        public String getExplosive() {
            throw new IllegalStateException("Boom");
        }
    }

    public static class ThrowingErrorGetterPojo {
        public String getExplosiveError() {
            throw new AssertionError("Fatal Boom");
        }
    }

    public static class NoDefaultConstructorPojo {
        public String val;
        public NoDefaultConstructorPojo(String v) {
            this.val = v;
        }
    }

    public static class CustomNullSerializer extends JsonSerializer<Object> {
        @Override
        public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString("CUSTOM_NULL");
        }
    }

    public static class NullSerializerPojo {
        @JsonSerialize(nullsUsing = CustomNullSerializer.class)
        public String nullField = null;
    }

    public static class UnwrappedPojo {
        public static class Inner {
            public String name = "innerVal";
        }
        @JsonUnwrapped(prefix = "pre_")
        public Inner inner = new Inner();
    }

    public static class RefinedReferencePojo {
        @JsonSerialize(as = AtomicReference.class)
        @JsonInclude(JsonInclude.Include.NON_ABSENT)
        public Serializable refField = new AtomicReference<String>();
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testClassAnnotationsRetrieval() {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        JavaType type = mapper.constructType(SimplePrimitivesPojo.class);
        BasicBeanDescription beanDesc = BasicBeanDescription.forSerialization(
                new BasicBeanDescription(config, type, AnnotatedClass.constructWithoutSuperTypes(SimplePrimitivesPojo.class, config), Collections.<BeanPropertyDefinition>emptyList())
        );
        PropertyBuilder builder = new PropertyBuilder(config, beanDesc);
        assertNotNull(builder.getClassAnnotations());
    }

    @Test(timeout = 4000)
    public void testDefaultBeanLazyInitializationAndCaching() {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        JavaType type = mapper.constructType(SimplePrimitivesPojo.class);
        BeanDescription beanDesc = config.introspect(type);
        PropertyBuilder builder = new PropertyBuilder(config, beanDesc);

        Object def1 = builder.getDefaultBean();
        assertNotNull(def1);
        assertTrue(def1 instanceof SimplePrimitivesPojo);

        Object def2 = builder.getDefaultBean();
        assertSame("Cached instance should be returned on subsequent calls", def1, def2);
    }

    @Test(timeout = 4000)
    public void testDefaultBeanWhenNoDefaultConstructor() {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        JavaType type = mapper.constructType(NoDefaultConstructorPojo.class);
        BeanDescription beanDesc = config.introspect(type);
        PropertyBuilder builder = new PropertyBuilder(config, beanDesc);

        Object def = builder.getDefaultBean();
        assertNull("Bean without default constructor should yield null default bean without throwing", def);

        // Verify second invocation hits NO_DEFAULT_MARKER branch and returns null
        assertNull(builder.getDefaultBean());
    }

    @Test(timeout = 4000)
    public void testGetDefaultValueForPrimitiveTypes() {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        BeanDescription beanDesc = config.introspect(mapper.constructType(EmptyPojo.class));
        PropertyBuilder builder = new PropertyBuilder(config, beanDesc);

        assertEquals(0, builder.getDefaultValue(mapper.constructType(int.class)));
        assertEquals(0, builder.getDefaultValue(mapper.constructType(Integer.class)));
        assertEquals(false, builder.getDefaultValue(mapper.constructType(boolean.class)));
        assertEquals(false, builder.getDefaultValue(mapper.constructType(Boolean.class)));
        assertEquals(0.0, builder.getDefaultValue(mapper.constructType(double.class)));
        assertEquals(0.0, builder.getDefaultValue(mapper.constructType(Double.class)));
        assertEquals((byte) 0, builder.getDefaultValue(mapper.constructType(byte.class)));
        assertEquals((short) 0, builder.getDefaultValue(mapper.constructType(short.class)));
        assertEquals(0L, builder.getDefaultValue(mapper.constructType(long.class)));
        assertEquals(0.0f, builder.getDefaultValue(mapper.constructType(float.class)));
        assertEquals('\0', builder.getDefaultValue(mapper.constructType(char.class)));
    }

    @Test(timeout = 4000)
    public void testGetDefaultValueForContainersAndStrings() {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        BeanDescription beanDesc = config.introspect(mapper.constructType(EmptyPojo.class));
        PropertyBuilder builder = new PropertyBuilder(config, beanDesc);

        assertEquals("", builder.getDefaultValue(mapper.constructType(String.class)));
        assertEquals(JsonInclude.Include.NON_EMPTY, builder.getDefaultValue(mapper.constructType(List.class)));
        assertEquals(JsonInclude.Include.NON_EMPTY, builder.getDefaultValue(mapper.constructType(Map.class)));
        assertEquals(JsonInclude.Include.NON_EMPTY, builder.getDefaultValue(mapper.constructType(int[].class)));
        assertEquals(JsonInclude.Include.NON_EMPTY, builder.getDefaultValue(mapper.constructType(AtomicReference.class)));
        assertNull(builder.getDefaultValue(mapper.constructType(Object.class)));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Inclusion Branches
    // =========================================================================

    @Test(timeout = 4000)
    public void testBuildWriterWithClassLevelNonDefaultInclusion() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ClassNonDefaultPojo pojo = new ClassNonDefaultPojo();
        pojo.field1 = "def1"; // default value
        pojo.field2 = "notNull"; // non-default
        pojo.intField = 0; // default value

        String json = mapper.writeValueAsString(pojo);
        assertFalse("field1 should be suppressed as it matches default", json.contains("\"field1\""));
        assertFalse("intField should be suppressed as it matches default (0)", json.contains("\"intField\""));
        assertTrue("field2 should be included", json.contains("\"field2\":\"notNull\""));
    }

    @Test(timeout = 4000)
    public void testBuildWriterWithArrayDefaultComparator() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);

        ArraysPojo pojo = new ArraysPojo();
        // default intArray is {1,2,3}, strArray is {"a","b"}
        String json = mapper.writeValueAsString(pojo);
        assertEquals("{}", json);

        // modify array element
        pojo.intArray[0] = 99;
        String jsonModified = mapper.writeValueAsString(pojo);
        assertTrue("Modified array should be serialized", jsonModified.contains("\"intArray\":[99,2,3]"));
        assertFalse("Unmodified array should still be omitted", jsonModified.contains("\"strArray\""));
    }

    @Test(timeout = 4000)
    public void testBuildWriterWithWriteEmptyJsonArraysFeature() throws Exception {
        ObjectMapper mapperEnabled = new ObjectMapper();
        mapperEnabled.enable(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS);
        String jsonEnabled = mapperEnabled.writeValueAsString(new ContainersPojo());
        assertTrue(jsonEnabled.contains("\"list\":[]"));

        ObjectMapper mapperDisabled = new ObjectMapper();
        mapperDisabled.disable(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS);
        String jsonDisabled = mapperDisabled.writeValueAsString(new ContainersPojo());
        assertFalse("Empty collection should be suppressed when WRITE_EMPTY_JSON_ARRAYS is disabled",
                jsonDisabled.contains("\"list\""));
    }

    @Test(timeout = 4000)
    public void testBuildWriterWithPropertyInclusions() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        PropIncludePojo pojo = new PropIncludePojo();
        pojo.nonDef = ""; // default value for String -> suppressed
        pojo.nonEmptyList = Collections.emptyList(); // empty -> suppressed
        pojo.nonNullVal = null; // null -> suppressed
        pojo.alwaysVal = null; // ALWAYS -> included as null
        pojo.useDefaultsVal = "present"; // included

        String json = mapper.writeValueAsString(pojo);
        assertFalse(json.contains("\"nonDef\""));
        assertFalse(json.contains("\"nonAbsentRef\""));
        assertFalse(json.contains("\"nonEmptyList\""));
        assertFalse(json.contains("\"nonNullVal\""));
        assertTrue(json.contains("\"alwaysVal\":null"));
        assertTrue(json.contains("\"useDefaultsVal\":\"present\""));
    }

    @Test(timeout = 4000)
    public void testCustomNullSerializerAndUnwrappedWriter() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        String nullJson = mapper.writeValueAsString(new NullSerializerPojo());
        assertTrue(nullJson.contains("\"nullField\":\"CUSTOM_NULL\""));

        String unwrappedJson = mapper.writeValueAsString(new UnwrappedPojo());
        assertTrue(unwrappedJson.contains("\"pre_name\":\"innerVal\""));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Issue databind#1256)
    // =========================================================================

    /**
     * Targets known defect databind#1256 (TestJDKAtomicTypes::testEmpty1256):
     * Ensures empty AtomicReference properties are properly suppressed under NON_EMPTY,
     * NON_ABSENT, or NON_DEFAULT rather than serializing as {"a":null}.
     */
    @Test(timeout = 4000)
    public void testEmpty1256DefectNonEmpty() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = mapper.writeValueAsString(new AtomicContext1256());
        assertEquals("Expected empty JSON {} when AtomicReference is empty under NON_EMPTY", "{}", json);
    }

    @Test(timeout = 4000)
    public void testEmpty1256DefectNonAbsent() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_ABSENT);
        String json = mapper.writeValueAsString(new AtomicContext1256());
        assertEquals("Expected empty JSON {} when AtomicReference is empty under NON_ABSENT", "{}", json);
    }

    @Test(timeout = 4000)
    public void testEmpty1256DefectNonDefault() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
        String json = mapper.writeValueAsString(new AtomicContext1256());
        assertEquals("Expected empty JSON {} when AtomicReference is empty under NON_DEFAULT", "{}", json);
    }

    @Test(timeout = 4000)
    public void testRefinedReferenceTypeNonAbsent() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(new RefinedReferencePojo());
        assertEquals("Refined reference type property with empty AtomicReference should be suppressed", "{}", json);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testFindSerializationTypeRefinementBranches() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // 1. Valid super-type refinement: String as CharSequence
        String jsonSuper = mapper.writeValueAsString(new SuperTypeRefinePojo());
        assertTrue(jsonSuper.contains("\"validSuper\":\"text\""));

        // 2. Sub-type relaxation refinement: CharSequence as String
        String jsonSub = mapper.writeValueAsString(new SubTypeRefinePojo());
        assertTrue(jsonSub.contains("\"validSub\":\"subText\""));

        // 3. Incompatible refinement: String as Integer -> must throw IllegalArgumentException
        try {
            mapper.writeValueAsString(new IncompatibleTypeRefinePojo());
            fail("Expected IllegalArgumentException when serializing with incompatible concrete-type annotation");
        } catch (JsonMappingException e) {
            assertTrue(e.getCause() instanceof IllegalArgumentException);
            assertTrue(e.getCause().getMessage().contains("not a super-type of"));
        }
    }

    @Test(timeout = 4000)
    public void testStaticTypingBranches() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(new StaticTypingPojo());
        assertTrue(json.contains("\"staticSeq\":\"static\""));
        assertTrue(json.contains("\"dynamicSeq\":\"dynamic\""));
        assertTrue(json.contains("\"defaultSeq\":\"default\""));
    }

    @Test(timeout = 4000)
    public void testThrowWrappedWithRuntimeException() {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        BeanDescription beanDesc = config.introspect(mapper.constructType(ThrowingGetterPojo.class));
        PropertyBuilder builder = new PropertyBuilder(config, beanDesc);

        BeanPropertyDefinition prop = null;
        for (BeanPropertyDefinition p : beanDesc.findProperties()) {
            if ("explosive".equals(p.getName())) {
                prop = p;
                break;
            }
        }
        assertNotNull(prop);

        try {
            builder.getPropertyDefaultValue(prop.getName(), prop.getGetter(), prop.getPrimaryType());
            fail("Expected RuntimeException from throwing getter");
        } catch (RuntimeException e) {
            assertEquals("Boom", e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testThrowWrappedWithError() {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        BeanDescription beanDesc = config.introspect(mapper.constructType(ThrowingErrorGetterPojo.class));
        PropertyBuilder builder = new PropertyBuilder(config, beanDesc);

        BeanPropertyDefinition prop = null;
        for (BeanPropertyDefinition p : beanDesc.findProperties()) {
            if ("explosiveError".equals(p.getName())) {
                prop = p;
                break;
            }
        }
        assertNotNull(prop);

        try {
            builder.getPropertyDefaultValue(prop.getName(), prop.getGetter(), prop.getPrimaryType());
            fail("Expected AssertionError to be unwrapped and rethrown directly");
        } catch (AssertionError e) {
            assertEquals("Fatal Boom", e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testThrowWrappedWithCheckedExceptionWrapsToIllegalArgumentException() {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        BeanDescription beanDesc = config.introspect(mapper.constructType(EmptyPojo.class));
        PropertyBuilder builder = new PropertyBuilder(config, beanDesc);

        Exception checkedEx = new Exception("Checked failure");
        try {
            builder._throwWrapped(checkedEx, "dummyProp", new EmptyPojo());
            fail("Expected IllegalArgumentException wrapping checked exception");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Failed to get property 'dummyProp'"));
        }
    }

    @Test(timeout = 4000)
    public void testContentTypeSerializerWithoutContentTypeThrowsIllegalStateException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        BeanDescription beanDesc = config.introspect(mapper.constructType(SimplePrimitivesPojo.class));
        PropertyBuilder builder = new PropertyBuilder(config, beanDesc);

        SerializerProvider prov = mapper.getSerializerProviderInstance();
        BeanPropertyDefinition propDef = beanDesc.findProperties().get(0);
        JavaType nonContainerType = mapper.constructType(int.class);

        // Create a dummy TypeSerializer for content
        TypeSerializer dummyContentTypeSer = mapper.getSerializerFactory()
                .createTypeSerializer(config, mapper.constructType(String.class));

        try {
            builder.buildWriter(prov, propDef, nonContainerType, null, null, dummyContentTypeSer,
                    propDef.getAccessor(), false);
            fail("Expected IllegalStateException when contentTypeSer is non-null for type without content");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("has no content"));
        }
    }
}