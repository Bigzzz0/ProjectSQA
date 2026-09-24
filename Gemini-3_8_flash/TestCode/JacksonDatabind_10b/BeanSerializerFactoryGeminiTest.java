package com.fasterxml.jackson.databind.ser;

import java.io.IOException;
import java.util.*;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.cfg.SerializerFactoryConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import com.fasterxml.jackson.databind.util.StdConverter;

/* [Branch & Defect Analysis Matrix]
 * Target Class: BeanSerializerFactory
 *
 * Decision / Condition Coverage Targets:
 * 1. withConfig(SerializerFactoryConfig):
 *    - if (_factoryConfig == config) -> returns same instance.
 *    - if (getClass() != BeanSerializerFactory.class) -> throws IllegalStateException.
 *    - normal path -> returns new BeanSerializerFactory(config).
 * 2. createSerializer:
 *    - explicit serializer via annotation on class -> returns custom serializer.
 *    - modifyTypeByAnnotation -> changes type / forces static typing / re-introspects.
 *    - converter present on bean -> StdDelegatingSerializer handling, issue #288 / #359 check.
 * 3. _createSerializer2:
 *    - findSerializerByAnnotations != null -> return.
 *    - isContainerType == true vs false (custom Serializers iteration).
 *    - findSerializerByLookup -> findSerializerByPrimaryType -> findBeanSerializer -> findSerializerByAddonType -> unknownTypeSerializer.
 *    - post-processing via BeanSerializerModifier.
 * 4. constructBeanSerializer & constructObjectIdHandler:
 *    - beanClass == Object.class -> returns unknownTypeSerializer.
 *    - properties null or empty / annotations only -> builder.createDummy().
 *    - ObjectIdGenerators.PropertyGenerator vs other generator types.
 *    - @JsonAnyGetter handling with custom serializer / MapSerializer (Issue #705 target).
 *    - View processing: DEFAULT_VIEW_INCLUSION enabled/disabled, views found vs none.
 *    - Property filtering: @JsonIgnoreProperties, @JsonIgnoreType, removeSetterlessGetters.
 * 5. Type Serializer helpers:
 *    - findPropertyTypeSerializer and findPropertyContentTypeSerializer.
 *
 * Known Defect (Defects4J - TestAnyGetter::testIssue705):
 * - Under @JsonAnyGetter with custom serializer or converter on the getter method,
 *   BeanSerializerFactory should respect custom serializer rather than defaulting to raw MapSerializer.
 */
public class BeanSerializerFactoryGeminiTest {

    private final ObjectMapper mapper = new ObjectMapper();

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Issue #705 / TestAnyGetter)
    // =========================================================================

    public static class Issue705CustomSerializer extends JsonSerializer<Map<String, String>> {
        @Override
        public void serialize(Map<String, String> value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException {
            for (Map.Entry<String, String> entry : value.entrySet()) {
                gen.writeFieldName("stuff");
                gen.writeString("[" + entry.getKey() + "/" + entry.getValue() + "]");
            }
        }
    }

    public static class Issue705Bean {
        protected Map<String, String> stuff = new LinkedHashMap<String, String>();

        public Issue705Bean(String key, String value) {
            stuff.put(key, value);
        }

        @JsonAnyGetter
        @JsonSerialize(using = Issue705CustomSerializer.class)
        public Map<String, String> getStuff() {
            return stuff;
        }
    }

    @Test(timeout = 4000)
    public void testIssue705AnyGetterCustomSerializerDefect() throws Exception {
        Issue705Bean bean = new Issue705Bean("key", "value");
        String json = mapper.writeValueAsString(bean);
        assertEquals("{\"stuff\":\"[key/value]\"}", json);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    public static class SimplePerson {
        private String name;
        private int age;

        public SimplePerson(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() { return name; }
        public int getAge() { return age; }
    }

    @Test(timeout = 4000)
    public void testStandardBeanSerialization() throws Exception {
        SimplePerson person = new SimplePerson("Alice", 30);
        String json = mapper.writeValueAsString(person);
        assertTrue(json.contains("\"name\":\"Alice\""));
        assertTrue(json.contains("\"age\":30"));
    }

    public static class AnyGetterBean {
        private Map<String, Object> extra = new LinkedHashMap<String, Object>();

        public void add(String k, Object v) { extra.put(k, v); }

        @JsonAnyGetter
        public Map<String, Object> getExtra() { return extra; }
    }

    @Test(timeout = 4000)
    public void testAnyGetterDefaultSerialization() throws Exception {
        AnyGetterBean bean = new AnyGetterBean();
        bean.add("k1", "v1");
        bean.add("k2", 42);
        String json = mapper.writeValueAsString(bean);
        assertEquals("{\"k1\":\"v1\",\"k2\":42}", json);
    }

    public static class Views {
        public static class Public {}
        public static class Internal extends Public {}
    }

    public static class ViewBean {
        @com.fasterxml.jackson.annotation.JsonView(Views.Public.class)
        public String pub = "pubVal";

        @com.fasterxml.jackson.annotation.JsonView(Views.Internal.class)
        public String secret = "privVal";
    }

    @Test(timeout = 4000)
    public void testViewFiltering() throws Exception {
        ViewBean bean = new ViewBean();
        String publicJson = mapper.writerWithView(Views.Public.class).writeValueAsString(bean);
        assertTrue(publicJson.contains("\"pub\":\"pubVal\""));
        assertFalse(publicJson.contains("\"secret\""));

        String internalJson = mapper.writerWithView(Views.Internal.class).writeValueAsString(bean);
        assertTrue(internalJson.contains("\"pub\":\"pubVal\""));
        assertTrue(internalJson.contains("\"secret\":\"privVal\""));
    }

    public static class ViewBeanNoInclusion {
        public String unmarked = "unmarkedVal";

        @com.fasterxml.jackson.annotation.JsonView(Views.Public.class)
        public String pub = "pubVal";
    }

    @Test(timeout = 4000)
    public void testViewFilteringWithoutDefaultInclusion() throws Exception {
        ObjectMapper customMapper = new ObjectMapper();
        customMapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
        ViewBeanNoInclusion bean = new ViewBeanNoInclusion();
        String json = customMapper.writerWithView(Views.Public.class).writeValueAsString(bean);
        assertTrue(json.contains("\"pub\":\"pubVal\""));
        assertFalse(json.contains("\"unmarked\""));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Configuration Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testWithConfigSameInstance() {
        BeanSerializerFactory factory = BeanSerializerFactory.instance;
        SerializerFactoryConfig config = factory.getFactoryConfig();
        SerializerFactory sameFactory = factory.withConfig(config);
        assertSame(factory, sameFactory);
    }

    @Test(timeout = 4000)
    public void testWithConfigNewConfig() {
        BeanSerializerFactory factory = BeanSerializerFactory.instance;
        SerializerFactoryConfig newConfig = new SerializerFactoryConfig();
        SerializerFactory newFactory = factory.withConfig(newConfig);
        assertNotSame(factory, newFactory);
        assertTrue(newFactory instanceof BeanSerializerFactory);
    }

    private static class SubFactory extends BeanSerializerFactory {
        public SubFactory() {
            super(null);
        }
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testWithConfigSubtypeWithoutOverrideThrows() {
        SubFactory subFactory = new SubFactory();
        subFactory.withConfig(new SerializerFactoryConfig());
    }

    @Test(timeout = 4000)
    public void testEmptyBeanSerialization() throws Exception {
        class EmptyClass {}
        ObjectMapper lenientMapper = new ObjectMapper();
        lenientMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        String json = lenientMapper.writeValueAsString(new EmptyClass());
        assertEquals("{}", json);
    }

    @Test(timeout = 4000)
    public void testPlainObjectSerialization() throws Exception {
        SerializerProvider prov = mapper.getSerializerProviderInstance();
        JavaType type = mapper.constructType(Object.class);
        JsonSerializer<Object> ser = BeanSerializerFactory.instance.createSerializer(prov, type);
        assertNotNull(ser);
        // Plain Object should resolve to unknown type serializer
        assertEquals("com.fasterxml.jackson.databind.ser.impl.UnknownSerializer", ser.getClass().getName());
    }

    // =========================================================================
    // Partition D: Annotations & Object Identity Handling
    // =========================================================================

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    public static class PropertyIdBean {
        public int id;
        public String name;
        public PropertyIdBean next;

        public PropertyIdBean(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    @Test(timeout = 4000)
    public void testPropertyObjectIdHandler() throws Exception {
        PropertyIdBean o1 = new PropertyIdBean(1, "first");
        PropertyIdBean o2 = new PropertyIdBean(2, "second");
        o1.next = o2;
        o2.next = o1; // cyclic reference

        String json = mapper.writeValueAsString(o1);
        assertTrue(json.contains("\"id\":1"));
        assertTrue(json.contains("\"id\":2"));
        // o2.next should reference o1 by id
        assertTrue(json.contains("\"next\":1"));
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
    public static class IntSequenceIdBean {
        public String val;
        public IntSequenceIdBean loop;

        public IntSequenceIdBean(String val) {
            this.val = val;
        }
    }

    @Test(timeout = 4000)
    public void testIntSequenceObjectIdHandler() throws Exception {
        IntSequenceIdBean b = new IntSequenceIdBean("cycle");
        b.loop = b;
        String json = mapper.writeValueAsString(b);
        assertTrue(json.contains("\"@id\":1"));
        assertTrue(json.contains("\"loop\":1"));
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "nonExistentProp")
    public static class InvalidPropertyIdBean {
        public int id = 5;
    }

    @Test(timeout = 4000)
    public void testInvalidPropertyObjectIdThrows() {
        try {
            mapper.writeValueAsString(new InvalidPropertyIdBean());
            fail("Expected exception due to invalid property generator");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("can not find property with name 'nonExistentProp'"));
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }

    // =========================================================================
    // Partition E: Filtering, Converters, & Modifiers
    // =========================================================================

    @JsonIgnoreType
    public static class IgnoredType {
        public String secret = "ignored";
    }

    public static class BeanWithIgnoredType {
        public String normal = "visible";
        public IgnoredType ignored = new IgnoredType();
    }

    @Test(timeout = 4000)
    public void testRemoveIgnorableTypes() throws Exception {
        BeanWithIgnoredType bean = new BeanWithIgnoredType();
        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"normal\":\"visible\""));
        assertFalse(json.contains("ignored"));
    }

    @JsonIgnoreProperties({"banned"})
    public static class IgnoredPropBean {
        public String kept = "keep";
        public String banned = "drop";
    }

    @Test(timeout = 4000)
    public void testFilterBeanPropertiesIgnoredProperties() throws Exception {
        IgnoredPropBean bean = new IgnoredPropBean();
        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"kept\":\"keep\""));
        assertFalse(json.contains("banned"));
    }

    @JsonFilter("filterCustom")
    public static class FilteredPojo {
        public String a = "1";
        public String b = "2";
    }

    @Test(timeout = 4000)
    public void testFindFilterId() throws Exception {
        SimpleFilterProvider filters = new SimpleFilterProvider();
        filters.addFilter("filterCustom", SimpleBeanPropertyFilter.filterOutAllExcept("a"));
        String json = mapper.writer(filters).writeValueAsString(new FilteredPojo());
        assertEquals("{\"a\":\"1\"}", json);
    }

    public static class BeanWithConverter {
        @JsonSerialize(converter = UppercaseConverter.class)
        public String text = "hello";
    }

    public static class UppercaseConverter extends StdConverter<String, String> {
        @Override
        public String convert(String value) {
            return value.toUpperCase();
        }
    }

    @Test(timeout = 4000)
    public void testPropertyConverter() throws Exception {
        BeanWithConverter bean = new BeanWithConverter();
        String json = mapper.writeValueAsString(bean);
        assertEquals("{\"text\":\"HELLO\"}", json);
    }

    public static class ClassLevelConverterBeanTarget {
        public String out;
        public ClassLevelConverterBeanTarget(String out) { this.out = out; }
    }

    @JsonSerialize(converter = ClassLevelConverter.class)
    public static class ClassLevelConverterBeanSource {
        public String in = "converted";
    }

    public static class ClassLevelConverter extends StdConverter<ClassLevelConverterBeanSource, ClassLevelConverterBeanTarget> {
        @Override
        public ClassLevelConverterBeanTarget convert(ClassLevelConverterBeanSource value) {
            return new ClassLevelConverterBeanTarget(value.in.toUpperCase());
        }
    }

    @Test(timeout = 4000)
    public void testClassLevelConverter() throws Exception {
        ClassLevelConverterBeanSource bean = new ClassLevelConverterBeanSource();
        String json = mapper.writeValueAsString(bean);
        assertEquals("{\"out\":\"CONVERTED\"}", json);
    }

    public static class SetterlessBean {
        public String getReadOnly() {
            return "readOnly";
        }
    }

    @Test(timeout = 4000)
    public void testRequireSettersForGetters() throws Exception {
        ObjectMapper strictMapper = new ObjectMapper();
        strictMapper.enable(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS);
        strictMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        String json = strictMapper.writeValueAsString(new SetterlessBean());
        assertEquals("{}", json);
    }

    @Test(timeout = 4000)
    public void testSerializerModifierInterception() throws Exception {
        ObjectMapper modMapper = new ObjectMapper();
        modMapper.setSerializerFactory(modMapper.getSerializerFactory().withSerializerModifier(
                new BeanSerializerModifier() {
                    @Override
                    public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
                            BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
                        return beanProperties;
                    }

                    @Override
                    public List<BeanPropertyWriter> orderProperties(SerializationConfig config,
                            BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
                        Collections.reverse(beanProperties);
                        return beanProperties;
                    }
                }
        ));

        SimplePerson person = new SimplePerson("Bob", 25);
        String json = modMapper.writeValueAsString(person);
        // Ordering should place age before name
        assertTrue(json.indexOf("\"age\":25") < json.indexOf("\"name\":\"Bob\""));
    }

    public static class CustomAnnotatedSerializer extends JsonSerializer<String> {
        @Override
        public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString("PREFIX:" + value);
        }
    }

    public static class BeanWithCustomFieldSerializer {
        @JsonSerialize(using = CustomAnnotatedSerializer.class)
        public String title = "sample";
    }

    @Test(timeout = 4000)
    public void testAnnotatedFieldSerializer() throws Exception {
        String json = mapper.writeValueAsString(new BeanWithCustomFieldSerializer());
        assertEquals("{\"title\":\"PREFIX:sample\"}", json);
    }
}