/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.fasterxml.jackson.databind.ser.AnyGetterWriter
 *
 * Decision / Branch Coverage Targets:
 * 1. Constructor:
 *    - Valid BeanProperty, AnnotatedMember, MapSerializer references.
 *    - Null serializer / property handling during instantiation.
 * 2. getAndSerialize(Object bean, JsonGenerator gen, SerializerProvider provider):
 *    - Branch: _accessor.getValue(bean) == null -> returns immediately (no-op).
 *    - Branch: !(value instanceof Map<?,?>) -> throws JsonMappingException with diagnostic message.
 *    - Branch: value instanceof Map<?,?> && _mapSerializer != null -> calls _mapSerializer.serializeFields.
 *    - Branch: value instanceof Map<?,?> && _mapSerializer == null -> falls through cleanly.
 * 3. getAndFilter(Object bean, JsonGenerator gen, SerializerProvider provider, PropertyFilter filter):
 *    - Branch: _accessor.getValue(bean) == null -> returns immediately (no-op).
 *    - Branch: !(value instanceof Map<?,?>) -> throws JsonMappingException with diagnostic message.
 *    - Branch: value instanceof Map<?,?> && _mapSerializer != null -> calls _mapSerializer.serializeFilteredFields.
 *    - Branch: value instanceof Map<?,?> && _mapSerializer == null -> falls through cleanly.
 * 4. resolve(SerializerProvider provider):
 *    - Calls provider.handlePrimaryContextualization(_mapSerializer, _property) and updates _mapSerializer.
 *
 * Known Defect Analysis (Defects4J Ground Truth - TestAnyGetter::testIssue705):
 * - Defect Issue #705: AnyGetterWriter previously assumed and restricted serialization strictly
 *   to MapSerializer. When a custom serializer was configured on the @JsonAnyGetter method,
 *   standard MapSerializer was erroneously used instead of delegating to the custom serializer,
 *   failing with ComparisonFailure: expected:<{"[stuff":"[key/value]]"}> but was:<{"[key":"value]"}>.
 * - Test testIssue705 directly replicates this ground truth failure condition via ObjectMapper.
 */
package com.fasterxml.jackson.databind.ser;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.AnnotationMap;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.databind.ser.std.MapSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class AnyGetterWriterGeminiTest {

    // =========================================================================
    // Helper Test Classes & Mock Infrastructure
    // =========================================================================

    static class DummyBeanWithMap {
        private final Map<String, Object> map;

        public DummyBeanWithMap(Map<String, Object> map) {
            this.map = map;
        }

        public Map<String, Object> any() {
            return map;
        }
    }

    static class DummyBeanWithNonMap {
        private final Object notAMap;

        public DummyBeanWithNonMap(Object notAMap) {
            this.notAMap = notAMap;
        }

        public Object any() {
            return notAMap;
        }
    }

    static class Issue705CustomSerializer extends JsonSerializer<Map<String, String>> {
        @Override
        public void serialize(Map<String, String> value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException {
            for (Map.Entry<String, String> entry : value.entrySet()) {
                gen.writeFieldName("stuff");
                gen.writeString("[" + entry.getKey() + "/" + entry.getValue() + "]");
            }
        }
    }

    static class Issue705Bean {
        protected Map<String, String> stuff;

        public Issue705Bean(String key, String value) {
            stuff = new LinkedHashMap<String, String>();
            stuff.put(key, value);
        }

        @JsonAnyGetter
        @JsonSerialize(using = Issue705CustomSerializer.class)
        public Map<String, String> getStuff() {
            return stuff;
        }
    }

    @JsonFilter("testFilter")
    static class FilteredBean {
        private final Map<String, Object> map = new LinkedHashMap<String, Object>();

        public FilteredBean(String k1, Object v1, String k2, Object v2) {
            map.put(k1, v1);
            map.put(k2, v2);
        }

        @JsonAnyGetter
        public Map<String, Object> any() {
            return map;
        }
    }

    private AnnotatedMethod createAnnotatedMethod(Class<?> targetClass, String methodName) throws Exception {
        Method method = targetClass.getMethod(methodName);
        return new AnnotatedMethod(null, method, new AnnotationMap(), null);
    }

    private MapSerializer createMapSerializer(ObjectMapper mapper) {
        JavaType mapType = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
        return MapSerializer.construct(new String[0], mapType, false, null, null, null, null);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & Normal Operations
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetAndSerializeWithValidMap() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AnnotatedMethod method = createAnnotatedMethod(DummyBeanWithMap.class, "any");
        MapSerializer mapSer = createMapSerializer(mapper);
        BeanProperty.Std prop = new BeanProperty.Std(new PropertyName("any"),
                TypeFactory.defaultInstance().constructType(Map.class), null, null, method, false);

        AnyGetterWriter writer = new AnyGetterWriter(prop, method, mapSer);

        Map<String, Object> data = new LinkedHashMap<String, Object>();
        data.put("foo", "bar");
        data.put("count", 42);
        DummyBeanWithMap bean = new DummyBeanWithMap(data);

        StringWriter sw = new StringWriter();
        JsonGenerator gen = mapper.getFactory().createGenerator(sw);
        SerializerProvider provider = mapper.getSerializerProviderInstance();

        gen.writeStartObject();
        writer.getAndSerialize(bean, gen, provider);
        gen.writeEndObject();
        gen.close();

        assertEquals("{\"foo\":\"bar\",\"count\":42}", sw.toString());
    }

    @Test(timeout = 4000)
    public void testGetAndFilterWithPropertyFilter() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AnnotatedMethod method = createAnnotatedMethod(DummyBeanWithMap.class, "any");
        MapSerializer mapSer = createMapSerializer(mapper);
        BeanProperty.Std prop = new BeanProperty.Std(new PropertyName("any"),
                TypeFactory.defaultInstance().constructType(Map.class), null, null, method, false);

        AnyGetterWriter writer = new AnyGetterWriter(prop, method, mapSer);

        Map<String, Object> data = new LinkedHashMap<String, Object>();
        data.put("keep", "value1");
        data.put("filterOut", "value2");
        DummyBeanWithMap bean = new DummyBeanWithMap(data);

        PropertyFilter filter = SimpleBeanPropertyFilter.serializeAllExcept("filterOut");

        StringWriter sw = new StringWriter();
        JsonGenerator gen = mapper.getFactory().createGenerator(sw);
        SerializerProvider provider = mapper.getSerializerProviderInstance();

        gen.writeStartObject();
        writer.getAndFilter(bean, gen, provider, filter);
        gen.writeEndObject();
        gen.close();

        assertEquals("{\"keep\":\"value1\"}", sw.toString());
    }

    @Test(timeout = 4000)
    public void testResolveContextualization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AnnotatedMethod method = createAnnotatedMethod(DummyBeanWithMap.class, "any");
        MapSerializer mapSer = createMapSerializer(mapper);
        BeanProperty.Std prop = new BeanProperty.Std(new PropertyName("any"),
                TypeFactory.defaultInstance().constructType(Map.class), null, null, method, false);

        AnyGetterWriter writer = new AnyGetterWriter(prop, method, mapSer);
        SerializerProvider provider = mapper.getSerializerProviderInstance();

        writer.resolve(provider);

        // Verify writer remains fully functional post-resolve
        StringWriter sw = new StringWriter();
        JsonGenerator gen = mapper.getFactory().createGenerator(sw);
        gen.writeStartObject();
        DummyBeanWithMap bean = new DummyBeanWithMap(Collections.<String, Object>singletonMap("a", "b"));
        writer.getAndSerialize(bean, gen, provider);
        gen.writeEndObject();
        gen.close();

        assertEquals("{\"a\":\"b\"}", sw.toString());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetAndSerializeWhenValueIsNull() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AnnotatedMethod method = createAnnotatedMethod(DummyBeanWithMap.class, "any");
        MapSerializer mapSer = createMapSerializer(mapper);
        BeanProperty.Std prop = new BeanProperty.Std(new PropertyName("any"),
                TypeFactory.defaultInstance().constructType(Map.class), null, null, method, false);

        AnyGetterWriter writer = new AnyGetterWriter(prop, method, mapSer);
        DummyBeanWithMap bean = new DummyBeanWithMap(null);

        StringWriter sw = new StringWriter();
        JsonGenerator gen = mapper.getFactory().createGenerator(sw);
        SerializerProvider provider = mapper.getSerializerProviderInstance();

        gen.writeStartObject();
        writer.getAndSerialize(bean, gen, provider);
        gen.writeEndObject();
        gen.close();

        assertEquals("{}", sw.toString());
    }

    @Test(timeout = 4000)
    public void testGetAndFilterWhenValueIsNull() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AnnotatedMethod method = createAnnotatedMethod(DummyBeanWithMap.class, "any");
        MapSerializer mapSer = createMapSerializer(mapper);
        BeanProperty.Std prop = new BeanProperty.Std(new PropertyName("any"),
                TypeFactory.defaultInstance().constructType(Map.class), null, null, method, false);

        AnyGetterWriter writer = new AnyGetterWriter(prop, method, mapSer);
        DummyBeanWithMap bean = new DummyBeanWithMap(null);

        StringWriter sw = new StringWriter();
        JsonGenerator gen = mapper.getFactory().createGenerator(sw);
        SerializerProvider provider = mapper.getSerializerProviderInstance();
        PropertyFilter filter = SimpleBeanPropertyFilter.serializeAll();

        gen.writeStartObject();
        writer.getAndFilter(bean, gen, provider, filter);
        gen.writeEndObject();
        gen.close();

        assertEquals("{}", sw.toString());
    }

    @Test(timeout = 4000)
    public void testGetAndSerializeWithEmptyMap() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AnnotatedMethod method = createAnnotatedMethod(DummyBeanWithMap.class, "any");
        MapSerializer mapSer = createMapSerializer(mapper);
        BeanProperty.Std prop = new BeanProperty.Std(new PropertyName("any"),
                TypeFactory.defaultInstance().constructType(Map.class), null, null, method, false);

        AnyGetterWriter writer = new AnyGetterWriter(prop, method, mapSer);
        DummyBeanWithMap bean = new DummyBeanWithMap(Collections.<String, Object>emptyMap());

        StringWriter sw = new StringWriter();
        JsonGenerator gen = mapper.getFactory().createGenerator(sw);
        SerializerProvider provider = mapper.getSerializerProviderInstance();

        gen.writeStartObject();
        writer.getAndSerialize(bean, gen, provider);
        gen.writeEndObject();
        gen.close();

        assertEquals("{}", sw.toString());
    }

    @Test(timeout = 4000)
    public void testGetAndSerializeWhenSerializerIsNull() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AnnotatedMethod method = createAnnotatedMethod(DummyBeanWithMap.class, "any");
        BeanProperty.Std prop = new BeanProperty.Std(new PropertyName("any"),
                TypeFactory.defaultInstance().constructType(Map.class), null, null, method, false);

        AnyGetterWriter writer = new AnyGetterWriter(prop, method, null);
        DummyBeanWithMap bean = new DummyBeanWithMap(Collections.<String, Object>singletonMap("k", "v"));

        StringWriter sw = new StringWriter();
        JsonGenerator gen = mapper.getFactory().createGenerator(sw);
        SerializerProvider provider = mapper.getSerializerProviderInstance();

        gen.writeStartObject();
        writer.getAndSerialize(bean, gen, provider);
        gen.writeEndObject();
        gen.close();

        // Null serializer branch falls through gracefully without writing properties
        assertEquals("{}", sw.toString());
    }

    @Test(timeout = 4000)
    public void testGetAndFilterWhenSerializerIsNull() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AnnotatedMethod method = createAnnotatedMethod(DummyBeanWithMap.class, "any");
        BeanProperty.Std prop = new BeanProperty.Std(new PropertyName("any"),
                TypeFactory.defaultInstance().constructType(Map.class), null, null, method, false);

        AnyGetterWriter writer = new AnyGetterWriter(prop, method, null);
        DummyBeanWithMap bean = new DummyBeanWithMap(Collections.<String, Object>singletonMap("k", "v"));

        StringWriter sw = new StringWriter();
        JsonGenerator gen = mapper.getFactory().createGenerator(sw);
        SerializerProvider provider = mapper.getSerializerProviderInstance();
        PropertyFilter filter = SimpleBeanPropertyFilter.serializeAll();

        gen.writeStartObject();
        writer.getAndFilter(bean, gen, provider, filter);
        gen.writeEndObject();
        gen.close();

        assertEquals("{}", sw.toString());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Issue 705)
    // =========================================================================

    /**
     * Targets Defects4J ground truth defect com.fasterxml.jackson.databind.ser.TestAnyGetter::testIssue705
     * Failure: expected:<{"[stuff":"[key/value]]"}> but was:<{"[key":"value]"}>
     * Occurs when @JsonAnyGetter is combined with a custom serializer (@JsonSerialize(using=...)).
     */
    @Test(timeout = 4000)
    public void testIssue705() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Issue705Bean bean = new Issue705Bean("key", "value");
        String json = mapper.writeValueAsString(bean);
        assertEquals("{\"stuff\":\"[key/value]\"}", json);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetAndSerializeThrowsWhenNotMap() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AnnotatedMethod method = createAnnotatedMethod(DummyBeanWithNonMap.class, "any");
        MapSerializer mapSer = createMapSerializer(mapper);
        BeanProperty.Std prop = new BeanProperty.Std(new PropertyName("any"),
                TypeFactory.defaultInstance().constructType(String.class), null, null, method, false);

        AnyGetterWriter writer = new AnyGetterWriter(prop, method, mapSer);
        DummyBeanWithNonMap bean = new DummyBeanWithNonMap("not a map");

        StringWriter sw = new StringWriter();
        JsonGenerator gen = mapper.getFactory().createGenerator(sw);
        SerializerProvider provider = mapper.getSerializerProviderInstance();

        try {
            gen.writeStartObject();
            writer.getAndSerialize(bean, gen, provider);
            fail("Expected JsonMappingException because returned value is not a java.util.Map");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Value returned by 'any-getter'"));
            assertTrue(e.getMessage().contains("not java.util.Map but java.lang.String"));
        } finally {
            gen.close();
        }
    }

    @Test(timeout = 4000)
    public void testGetAndFilterThrowsWhenNotMap() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AnnotatedMethod method = createAnnotatedMethod(DummyBeanWithNonMap.class, "any");
        MapSerializer mapSer = createMapSerializer(mapper);
        BeanProperty.Std prop = new BeanProperty.Std(new PropertyName("any"),
                TypeFactory.defaultInstance().constructType(Integer.class), null, null, method, false);

        AnyGetterWriter writer = new AnyGetterWriter(prop, method, mapSer);
        DummyBeanWithNonMap bean = new DummyBeanWithNonMap(Integer.valueOf(1234));

        StringWriter sw = new StringWriter();
        JsonGenerator gen = mapper.getFactory().createGenerator(sw);
        SerializerProvider provider = mapper.getSerializerProviderInstance();
        PropertyFilter filter = SimpleBeanPropertyFilter.serializeAll();

        try {
            gen.writeStartObject();
            writer.getAndFilter(bean, gen, provider, filter);
            fail("Expected JsonMappingException because returned value is not a java.util.Map");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Value returned by 'any-getter'"));
            assertTrue(e.getMessage().contains("not java.util.Map but java.lang.Integer"));
        } finally {
            gen.close();
        }
    }

    // =========================================================================
    // Partition E: End-to-End Serialization with FilterProvider
    // =========================================================================

    @Test(timeout = 4000)
    public void testEndToEndFilteringWithObjectMapper() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleFilterProvider filterProvider = new SimpleFilterProvider();
        filterProvider.addFilter("testFilter", SimpleBeanPropertyFilter.filterOutAllExcept("k1"));
        mapper.setFilterProvider(filterProvider);

        FilteredBean bean = new FilteredBean("k1", "value1", "k2", "value2");
        String json = mapper.writeValueAsString(bean);
        assertEquals("{\"k1\":\"value1\"}", json);
    }
}