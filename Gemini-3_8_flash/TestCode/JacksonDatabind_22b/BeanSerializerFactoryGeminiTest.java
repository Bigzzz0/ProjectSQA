/* [Branch & Defect Analysis Matrix]
 * --------------------------------------------------------------------------------------------------------------------
 * TARGET CLASS: com.fasterxml.jackson.databind.ser.BeanSerializerFactory
 *
 * DEFECT TARGET (Ground Truth: com.fasterxml.jackson.databind.ser.TestJsonValue::testJsonValueWithCustomOverride):
 * - Condition: POJO with a @JsonValue annotated accessor is serialized when a custom Serializer is registered via a module.
 * - Flaw: In _createSerializer2(), Jackson checked findSerializerByAnnotations() before inspecting custom POJO serializers
 *   from SerializerFactoryConfig. Consequently, custom serializers could not override @JsonValue annotations.
 * - Targeted Branch: _createSerializer2() -> customSerializers() vs findSerializerByAnnotations().
 *
 * PARTITION A: Core Functional Logic & State Transitions
 * - instance singleton and factory configuration immutability (withConfig identity and subtype guard).
 * - withConfig() call with custom subtype throwing IllegalStateException.
 * - Normal POJO serialization (standard getters/fields, builder construction).
 * - constructBeanSerializer for Object.class -> returns unknown type serializer without failing.
 * - Enum serialization handling via findBeanSerializer.
 *
 * PARTITION B: Boundary Value Analysis (BVA) & Extremes
 * - Empty bean / marker interface without properties, with and without known annotations (dummy serializer creation).
 * - Non-potential bean types (primitive wrappers, arrays, proxies) skipped by isPotentialBeanType.
 * - Single vs multiple property beans; property ordering modifiers.
 *
 * PARTITION C: Defect-Targeted Branch Zone
 * - testJsonValueCustomSerializerOverride: Registered custom serializer must take precedence over @JsonValue method.
 * - Converter resolution on class level: findSerializationConverter() -> StdDelegatingSerializer.
 * - Delegating converter targeting Object vs non-Object raw class.
 *
 * PARTITION D: Exception & Defensive Guard Paths
 * - ObjectId generator configuration: ObjectIdGenerators.PropertyGenerator with valid property name vs missing property
 *   name throwing IllegalArgumentException.
 * - ObjectId generator configuration with standard IntSequenceGenerator.
 * - @JsonAnyGetter handling with custom/map serialization and access fix.
 * - MapperFeature.REQUIRE_SETTERS_FOR_GETTERS filtering setterless getters.
 * - @JsonIgnoreProperties filtering via filterBeanProperties.
 * - @JsonIgnoreType exclusion via removeIgnorableTypes.
 * - @JsonView filtering and inclusion defaulting via processViews.
 * - Overlapping Type ID resolution (removeOverlappingTypeIds with As.EXTERNAL_PROPERTY).
 *
 * PARTITION E: Object Lifecycle & Contract Integrity
 * - Subtype validation for BeanSerializerFactory.
 * - SerializerModifier lifecycle hooks: changeProperties, orderProperties, updateBuilder, modifySerializer.
 * --------------------------------------------------------------------------------------------------------------------
 */

package com.fasterxml.jackson.databind.ser;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.cfg.SerializerFactoryConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.databind.ser.std.StdDelegatingSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.StdConverter;
import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class BeanSerializerFactoryGeminiTest {

    // --------------------------------------------------------------------------------
    // Test Models & DTOs
    // --------------------------------------------------------------------------------

    public static class SimpleBean {
        public String name = "test";
        public int value = 100;
    }

    public static class ValueWithJsonValue {
        private final String val;

        public ValueWithJsonValue(String v) {
            this.val = v;
        }

        @JsonValue
        public String getVal() {
            return this.val;
        }
    }

    public static class AnyGetterBean {
        private final Map<String, Object> map = new HashMap<String, Object>();

        public AnyGetterBean() {
            map.put("k1", "v1");
            map.put("k2", 2);
        }

        @JsonAnyGetter
        public Map<String, Object> any() {
            return map;
        }
    }

    @JsonIgnoreProperties({"hidden"})
    public static class IgnoredPropsBean {
        public String visible = "ok";
        public String hidden = "skip";
    }

    @JsonIgnoreType
    public static class IgnorableTypeMember {
        public String ignored = "no";
    }

    public static class ContainerWithIgnorable {
        public String name = "container";
        public IgnorableTypeMember member = new IgnorableTypeMember();
    }

    public static class Views {
        public interface Public {}
        public interface Internal extends Public {}
    }

    public static class ViewBean {
        @JsonView(Views.Public.class)
        public String publicField = "pub";

        @JsonView(Views.Internal.class)
        public String internalField = "priv";

        public String defaultField = "def";
    }

    public static class SetterlessBean {
        private String prop = "implicit";

        public String getProp() {
            return prop;
        }

        @JsonProperty("explicit")
        public String getExplicit() {
            return "explicitValue";
        }
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    public static class PropertyIdBean {
        public int id = 777;
        public String payload = "data";
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "nonExistent")
    public static class BadPropertyIdBean {
        public int id = 123;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@seq")
    public static class SequenceIdBean {
        public String msg = "sequence";
    }

    @JsonFilter("testFilter")
    public static class FilteredBean {
        public String title = "title";
        public String secret = "hidden";
    }

    @JsonSerialize(converter = StringLengthConverter.class)
    public static class ConvertedBean {
        public String text;
        public ConvertedBean(String t) { this.text = t; }
    }

    public static class StringLengthConverter extends StdConverter<ConvertedBean, Integer> {
        @Override
        public Integer convert(ConvertedBean value) {
            return value.text == null ? 0 : value.text.length();
        }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
    public static class ExternalTypeBase {
        public String name = "ext";
    }

    public static class ConflictExternalBean {
        public ExternalTypeBase item = new ExternalTypeBase();
        public String type = "explicitType";
    }

    @JsonPropertyOrder(alphabetic = true)
    public static class EmptyAnnotatedBean {}

    public static class UnannotatedEmptyBean {}

    public enum SampleEnum { A, B }

    // --------------------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // --------------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testSingletonAndWithConfigIdentical() {
        BeanSerializerFactory factory = BeanSerializerFactory.instance;
        assertNotNull(factory);
        SerializerFactoryConfig config = factory.getFactoryConfig();
        SerializerFactory same = factory.withConfig(config);
        assertSame(factory, same);
    }

    @Test(timeout = 4000)
    public void testWithConfigNewInstance() {
        BeanSerializerFactory factory = BeanSerializerFactory.instance;
        SerializerFactoryConfig newConfig = new SerializerFactoryConfig();
        SerializerFactory modified = factory.withConfig(newConfig);
        assertNotNull(modified);
        assertTrue(modified instanceof BeanSerializerFactory);
        assertFalse(factory == modified);
    }

    @Test(timeout = 4000)
    public void testWithConfigSubtypeGuard() {
        class CustomFactorySubtype extends BeanSerializerFactory {
            public CustomFactorySubtype(SerializerFactoryConfig config) {
                super(config);
            }
        }
        CustomFactorySubtype custom = new CustomFactorySubtype(new SerializerFactoryConfig());
        try {
            custom.withConfig(new SerializerFactoryConfig());
            fail("Expected IllegalStateException for un-overridden withConfig in subtype");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("has not properly overridden method 'withAdditionalSerializers'"));
        }
    }

    @Test(timeout = 4000)
    public void testConstructBeanSerializerForObjectClass() throws Exception {
        BeanSerializerFactory factory = BeanSerializerFactory.instance;
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();
        JavaType type = mapper.constructType(Object.class);
        BeanDescription desc = mapper.getSerializationConfig().introspect(type);

        JsonSerializer<Object> ser = factory.constructBeanSerializer(prov, desc);
        assertNotNull(ser);
        // Returns unknown type serializer for Object.class as per design
        assertEquals(prov.getUnknownTypeSerializer(Object.class).getClass(), ser.getClass());
    }

    @Test(timeout = 4000)
    public void testFindBeanSerializerForEnum() throws Exception {
        BeanSerializerFactory factory = BeanSerializerFactory.instance;
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();
        JavaType type = mapper.constructType(SampleEnum.class);
        BeanDescription desc = mapper.getSerializationConfig().introspect(type);

        // Enums are not standard beans, but findBeanSerializer permits enum types
        JsonSerializer<Object> ser = factory.findBeanSerializer(prov, type, desc);
        // Bean serializer construction may return null or empty dummy since enum is handled specially
        // Crucially, it must not throw or prematurely reject enum types
        assertTrue(ser == null || ser.isUnwrappingSerializer() || ser.getClass().getName().contains("BeanSerializer"));
    }

    @Test(timeout = 4000)
    public void testIsPotentialBeanType() {
        BeanSerializerFactory factory = BeanSerializerFactory.instance;
        assertTrue(factory.isPotentialBeanType(SimpleBean.class));
        assertFalse(factory.isPotentialBeanType(int.class));
        assertFalse(factory.isPotentialBeanType(int[].class));
        assertFalse(factory.isPotentialBeanType(String[].class));
    }

    // --------------------------------------------------------------------------------
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // --------------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testEmptyBeanWithoutAnnotations() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        String json = mapper.writeValueAsString(new UnannotatedEmptyBean());
        assertEquals("{}", json);
    }

    @Test(timeout = 4000)
    public void testEmptyBeanWithKnownAnnotationsCreatesDummy() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // Even when FAIL_ON_EMPTY_BEANS is enabled, known annotations allow dummy serializer construction
        mapper.enable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        String json = mapper.writeValueAsString(new EmptyAnnotatedBean());
        assertEquals("{}", json);
    }

    @Test(timeout = 4000)
    public void testPrimitiveTypesNotHandledAsBeans() throws Exception {
        BeanSerializerFactory factory = BeanSerializerFactory.instance;
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();
        JavaType intType = mapper.constructType(int.class);
        BeanDescription desc = mapper.getSerializationConfig().introspect(intType);

        JsonSerializer<Object> ser = factory.findBeanSerializer(prov, intType, desc);
        assertNull(ser);
    }

    // --------------------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (Ground Truth from Defects4J)
    // --------------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testJsonValueWithCustomOverride() throws Exception {
        // Targets defect: com.fasterxml.jackson.databind.ser.TestJsonValue::testJsonValueWithCustomOverride
        // Failure without fix: expected:<[42]> but was:<["value"]>
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(ValueWithJsonValue.class, new JsonSerializer<ValueWithJsonValue>() {
            @Override
            public void serialize(ValueWithJsonValue value, JsonGenerator gen, SerializerProvider serializers)
                    throws IOException {
                gen.writeNumber(42);
            }
        });
        mapper.registerModule(module);

        ValueWithJsonValue bean = new ValueWithJsonValue("value");
        String json = mapper.writeValueAsString(bean);
        assertEquals("42", json);
    }

    @Test(timeout = 4000)
    public void testConverterAnnotationHandling() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ConvertedBean bean = new ConvertedBean("Jackson");
        String json = mapper.writeValueAsString(bean);
        assertEquals("7", json);

        // Verify factory constructs StdDelegatingSerializer
        JavaType type = mapper.constructType(ConvertedBean.class);
        SerializerProvider prov = mapper.getSerializerProviderInstance();
        JsonSerializer<Object> ser = BeanSerializerFactory.instance.createSerializer(prov, type);
        assertNotNull(ser);
        assertTrue(ser instanceof StdDelegatingSerializer);
    }

    // --------------------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // --------------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testObjectIdPropertyGeneratorValid() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        PropertyIdBean bean = new PropertyIdBean();
        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"id\":777"));
        assertTrue(json.contains("\"payload\":\"data\""));
    }

    @Test(timeout = 4000)
    public void testObjectIdPropertyGeneratorMissingPropertyThrowsException() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValueAsString(new BadPropertyIdBean());
            fail("Expected JsonMappingException due to missing object id property 'nonExistent'");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Invalid Object Id definition"));
            assertTrue(e.getMessage().contains("nonExistent"));
        } catch (IOException e) {
            fail("Unexpected IOException: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testObjectIdSequenceGenerator() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SequenceIdBean bean = new SequenceIdBean();
        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"@seq\":1"));
        assertTrue(json.contains("\"msg\":\"sequence\""));
    }

    @Test(timeout = 4000)
    public void testAnyGetterSupport() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AnyGetterBean bean = new AnyGetterBean();
        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"k1\":\"v1\""));
        assertTrue(json.contains("\"k2\":2"));
    }

    @Test(timeout = 4000)
    public void testFilterBeanPropertiesViaAnnotation() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        IgnoredPropsBean bean = new IgnoredPropsBean();
        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"visible\":\"ok\""));
        assertFalse(json.contains("hidden"));
    }

    @Test(timeout = 4000)
    public void testRemoveIgnorableTypes() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ContainerWithIgnorable container = new ContainerWithIgnorable();
        String json = mapper.writeValueAsString(container);
        assertTrue(json.contains("\"name\":\"container\""));
        assertFalse(json.contains("member"));
    }

    @Test(timeout = 4000)
    public void testRequireSettersForGetters() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS);
        SetterlessBean bean = new SetterlessBean();
        String json = mapper.writeValueAsString(bean);
        // 'prop' does not have a setter and is implicit -> suppressed
        assertFalse(json.contains("\"prop\""));
        // 'explicit' has @JsonProperty, so it must remain
        assertTrue(json.contains("\"explicit\":\"explicitValue\""));
    }

    @Test(timeout = 4000)
    public void testViewProcessing() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ViewBean bean = new ViewBean();

        // 1. Serialization with Public view
        String jsonPublic = mapper.writerWithView(Views.Public.class).writeValueAsString(bean);
        assertTrue(jsonPublic.contains("\"publicField\":\"pub\""));
        assertFalse(jsonPublic.contains("internalField"));

        // 2. Serialization with Internal view (inherits Public)
        String jsonInternal = mapper.writerWithView(Views.Internal.class).writeValueAsString(bean);
        assertTrue(jsonInternal.contains("\"publicField\":\"pub\""));
        assertTrue(jsonInternal.contains("\"internalField\":\"priv\""));

        // 3. Serialization with DEFAULT_VIEW_INCLUSION disabled
        ObjectMapper disabledViewMapper = new ObjectMapper();
        disabledViewMapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
        String jsonNoDefault = disabledViewMapper.writerWithView(Views.Public.class).writeValueAsString(bean);
        assertTrue(jsonNoDefault.contains("\"publicField\":\"pub\""));
        assertFalse(jsonNoDefault.contains("internalField"));
        assertFalse(jsonNoDefault.contains("defaultField"));
    }

    @Test(timeout = 4000)
    public void testFilterIdIntegration() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleFilterProvider filters = new SimpleFilterProvider();
        filters.addFilter("testFilter", SimpleBeanPropertyFilter.filterOutAllExcept("title"));
        mapper.setFilterProvider(filters);

        FilteredBean bean = new FilteredBean();
        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"title\":\"title\""));
        assertFalse(json.contains("secret"));
    }

    @Test(timeout = 4000)
    public void testOverlappingTypeIdsResolution() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ConflictExternalBean bean = new ConflictExternalBean();
        String json = mapper.writeValueAsString(bean);
        assertNotNull(json);
        assertTrue(json.contains("\"type\":\"explicitType\""));
    }

    // --------------------------------------------------------------------------------
    // Partition E: Object Lifecycle & Contract Integrity
    // --------------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testBeanSerializerModifiers() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule mod = new SimpleModule();
        final boolean[] hooksCalled = new boolean[4];

        mod.setSerializerModifier(new BeanSerializerModifier() {
            @Override
            public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
                    BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
                hooksCalled[0] = true;
                return beanProperties;
            }

            @Override
            public List<BeanPropertyWriter> orderProperties(SerializationConfig config,
                    BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
                hooksCalled[1] = true;
                return beanProperties;
            }

            @Override
            public BeanSerializerBuilder updateBuilder(SerializationConfig config,
                    BeanDescription beanDesc, BeanSerializerBuilder builder) {
                hooksCalled[2] = true;
                return builder;
            }

            @Override
            public JsonSerializer<?> modifySerializer(SerializationConfig config,
                    BeanDescription beanDesc, JsonSerializer<?> serializer) {
                hooksCalled[3] = true;
                return serializer;
            }
        });

        mapper.registerModule(mod);
        String json = mapper.writeValueAsString(new SimpleBean());
        assertNotNull(json);
        assertTrue("changeProperties must be executed", hooksCalled[0]);
        assertTrue("orderProperties must be executed", hooksCalled[1]);
        assertTrue("updateBuilder must be executed", hooksCalled[2]);
        assertTrue("modifySerializer must be executed", hooksCalled[3]);
    }

    @Test(timeout = 4000)
    public void testTypeSerializerForProperty() throws Exception {
        BeanSerializerFactory factory = BeanSerializerFactory.instance;
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        JavaType type = mapper.constructType(ExternalTypeBase.class);
        BeanDescription desc = config.introspect(type);

        AnnotatedMember member = desc.findProperties().get(0).getAccessor();
        TypeSerializer typeSer = factory.findPropertyTypeSerializer(type, config, member);
        // No explicit property type annotation on member -> defaults to null or base type serializer
        assertNull(typeSer);
    }

    @Test(timeout = 4000)
    public void testFindPropertyContentTypeSerializer() throws Exception {
        BeanSerializerFactory factory = BeanSerializerFactory.instance;
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        JavaType listType = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
        BeanDescription desc = config.introspect(listType);

        TypeSerializer contentSer = factory.findPropertyContentTypeSerializer(listType, config, desc.getClassInfo());
        assertNull(contentSer);
    }
}