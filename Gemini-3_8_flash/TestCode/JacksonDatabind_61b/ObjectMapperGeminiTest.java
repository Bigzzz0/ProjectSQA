/* [Branch & Defect Analysis Matrix]
 * Target Class: com.fasterxml.jackson.databind.ObjectMapper
 * Defect Under Test: [databind#1395] Default typing inadvertently treating primitive types
 * as non-concrete (due to Modifier.ABSTRACT on primitive classes in the JVM).
 * When enableDefaultTyping() is enabled, primitives (e.g., long, int) in non-final or
 * object-and-non-concrete modes caused JsonMappingException:
 * "Class java.lang.Long not subtype of [simple type, class long]".
 *
 * Targeted Partitions & Branches:
 * - Partition A: Core Read/Write Operations (String, byte[], streams, trees, data binding)
 * - Partition B: DefaultTyping & TypeResolverBuilder branches (JAVA_LANG_OBJECT,
 *   OBJECT_AND_NON_CONCRETE, NON_CONCRETE_AND_ARRAYS, NON_FINAL, primitive type guard)
 * - Partition C: Defect reproduction: default typing with long/primitive properties
 * - Partition D: Custom configurations, fluent feature toggles, mix-ins, copy lifecycle
 * - Partition E: ObjectReader and ObjectWriter factory delegation
 */

package com.fasterxml.jackson.databind;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class ObjectMapperGeminiTest {

    // Helper POJOs for testing
    public static class SimpleBean {
        public String name;
        public int value;

        public SimpleBean() {}

        public SimpleBean(String name, int value) {
            this.name = name;
            this.value = value;
        }
    }

    public static class DataWithPrimitive {
        public long key;

        public DataWithPrimitive() {}

        public DataWithPrimitive(long key) {
            this.key = key;
        }
    }

    public static class ContainerBean {
        public Object longAsField;

        public ContainerBean() {}

        public ContainerBean(Object obj) {
            this.longAsField = obj;
        }
    }

    /*
     * ----------------------------------------------------------------------
     * Partition C: Defect-Targeted Zone (databind#1395 / Defects4J Target)
     * ----------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testDefaultTypingWithPrimitivesInMap() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("longAsField", new DataWithPrimitive(123456789L));

        String json = mapper.writeValueAsString(map);
        assertNotNull(json);

        TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {};
        HashMap<String, Object> deserialized = mapper.readValue(json, typeRef);

        assertNotNull(deserialized);
        Object dataObj = deserialized.get("longAsField");
        assertTrue("Expected DataWithPrimitive instance, got: " + (dataObj == null ? "null" : dataObj.getClass()),
                dataObj instanceof DataWithPrimitive);
        assertEquals(123456789L, ((DataWithPrimitive) dataObj).key);
    }

    @Test(timeout = 4000)
    public void testDefaultTypingWithPrimitiveLongDirectField() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);

        ContainerBean container = new ContainerBean(new DataWithPrimitive(999L));
        String json = mapper.writeValueAsString(container);

        ContainerBean result = mapper.readValue(json, ContainerBean.class);
        assertNotNull(result);
        assertTrue(result.longAsField instanceof DataWithPrimitive);
        assertEquals(999L, ((DataWithPrimitive) result.longAsField).key);
    }

    @Test(timeout = 4000)
    public void testDefaultTypeResolverBuilderUseForTypePrimitives() {
        ObjectMapper.DefaultTypeResolverBuilder builder =
                new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);

        JavaType longType = TypeFactory.defaultInstance().constructType(long.class);
        JavaType intType = TypeFactory.defaultInstance().constructType(int.class);
        JavaType booleanType = TypeFactory.defaultInstance().constructType(boolean.class);

        assertFalse("Primitive long should not be typed by default", builder.useForType(longType));
        assertFalse("Primitive int should not be typed by default", builder.useForType(intType));
        assertFalse("Primitive boolean should not be typed by default", builder.useForType(booleanType));
    }

    /*
     * ----------------------------------------------------------------------
     * Partition A: Core Functional Logic & State Transitions
     * ----------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testBasicSerializationAndDeserialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleBean bean = new SimpleBean("alpha", 42);

        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"name\":\"alpha\""));
        assertTrue(json.contains("\"value\":42"));

        SimpleBean deserialized = mapper.readValue(json, SimpleBean.class);
        assertEquals("alpha", deserialized.name);
        assertEquals(42, deserialized.value);
    }

    @Test(timeout = 4000)
    public void testWriteValueAsBytesAndReadFromBytes() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleBean bean = new SimpleBean("bytes", 100);

        byte[] bytes = mapper.writeValueAsBytes(bean);
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);

        SimpleBean deserialized = mapper.readValue(bytes, SimpleBean.class);
        assertEquals("bytes", deserialized.name);
        assertEquals(100, deserialized.value);

        SimpleBean deserializedOffset = mapper.readValue(bytes, 0, bytes.length, SimpleBean.class);
        assertEquals("bytes", deserializedOffset.name);
    }

    @Test(timeout = 4000)
    public void testStreamSerializationAndDeserialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleBean bean = new SimpleBean("stream", 55);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        mapper.writeValue(baos, bean);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        SimpleBean deserialized = mapper.readValue(bais, SimpleBean.class);
        assertEquals("stream", deserialized.name);
        assertEquals(55, deserialized.value);
    }

    @Test(timeout = 4000)
    public void testWriterAndReaderSerialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleBean bean = new SimpleBean("rw", 77);

        StringWriter sw = new StringWriter();
        mapper.writeValue(sw, bean);

        StringReader sr = new StringReader(sw.toString());
        SimpleBean deserialized = mapper.readValue(sr, SimpleBean.class);
        assertEquals("rw", deserialized.name);
        assertEquals(77, deserialized.value);
    }

    @Test(timeout = 4000)
    public void testTreeModelOperations() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        ObjectNode objNode = mapper.createObjectNode();
        objNode.put("str", "hello");
        objNode.put("num", 123);

        ArrayNode arrNode = mapper.createArrayNode();
        arrNode.add("elem1");
        arrNode.add(2);
        objNode.set("list", arrNode);

        String json = mapper.writeValueAsString(objNode);
        JsonNode readNode = mapper.readTree(json);

        assertTrue(readNode.isObject());
        assertEquals("hello", readNode.get("str").asText());
        assertEquals(123, readNode.get("num").asInt());
        assertTrue(readNode.get("list").isArray());
        assertEquals(2, readNode.get("list").size());

        SimpleBean bean = new SimpleBean("tree", 9);
        JsonNode treeFromBean = mapper.valueToTree(bean);
        assertEquals("tree", treeFromBean.get("name").asText());
        assertEquals(9, treeFromBean.get("value").asInt());

        SimpleBean converted = mapper.treeToValue(treeFromBean, SimpleBean.class);
        assertEquals("tree", converted.name);
        assertEquals(9, converted.value);
    }

    @Test(timeout = 4000)
    public void testConvertValue() {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("name", "conv");
        map.put("value", 88);

        SimpleBean bean = mapper.convertValue(map, SimpleBean.class);
        assertEquals("conv", bean.name);
        assertEquals(88, bean.value);

        Map<?, ?> convertedMap = mapper.convertValue(bean, Map.class);
        assertEquals("conv", convertedMap.get("name"));
        assertEquals(88, convertedMap.get("value"));

        assertNull(mapper.convertValue(null, SimpleBean.class));
    }

    /*
     * ----------------------------------------------------------------------
     * Partition B: Boundary Value Analysis & Edge Cases
     * ----------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testReadTreeFromNullOrEmptyInput() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        JsonNode nullNode = mapper.readTree("null");
        assertTrue(nullNode.isNull());

        JsonNode emptyNode = mapper.readTree("");
        assertNull(emptyNode);

        JsonNode streamNullNode = mapper.readTree(new ByteArrayInputStream("null".getBytes(StandardCharsets.UTF_8)));
        assertTrue(streamNullNode.isNull());

        JsonNode readerNullNode = mapper.readTree(new StringReader("null"));
        assertTrue(readerNullNode.isNull());
    }

    @Test(timeout = 4000)
    public void testNullArgumentConversions() {
        ObjectMapper mapper = new ObjectMapper();
        assertNull(mapper.valueToTree(null));
        assertNull(mapper.convertValue(null, SimpleBean.class));
        assertNull(mapper.convertValue(null, new TypeReference<SimpleBean>() {}));
        assertNull(mapper.convertValue(null, mapper.constructType(SimpleBean.class)));
    }

    @Test(timeout = 4000)
    public void testCanSerializeAndCanDeserialize() {
        ObjectMapper mapper = new ObjectMapper();

        assertTrue(mapper.canSerialize(SimpleBean.class));
        assertTrue(mapper.canDeserialize(mapper.constructType(SimpleBean.class)));

        AtomicReference<Throwable> cause = new AtomicReference<Throwable>();
        assertTrue(mapper.canSerialize(SimpleBean.class, cause));
        assertNull(cause.get());

        assertTrue(mapper.canDeserialize(mapper.constructType(SimpleBean.class), cause));
        assertNull(cause.get());
    }

    /*
     * ----------------------------------------------------------------------
     * Partition D: Configuration & Fluent Feature Toggles
     * ----------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testFeatureToggles() {
        ObjectMapper mapper = new ObjectMapper();

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        assertFalse(mapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));

        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        assertTrue(mapper.isEnabled(SerializationFeature.INDENT_OUTPUT));

        mapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
        assertTrue(mapper.isEnabled(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY));

        mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        assertTrue(mapper.isEnabled(SerializationFeature.WRITE_ENUMS_USING_TO_STRING));

        mapper.disable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        assertFalse(mapper.isEnabled(SerializationFeature.WRITE_ENUMS_USING_TO_STRING));

        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);
        assertTrue(mapper.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));

        mapper.disable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);
        assertFalse(mapper.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));
    }

    @Test(timeout = 4000)
    public void testJsonParserAndGeneratorFeatureConfiguration() {
        ObjectMapper mapper = new ObjectMapper();

        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        assertTrue(mapper.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));

        mapper.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, false);
        assertFalse(mapper.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES));

        mapper.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
        assertTrue(mapper.isEnabled(JsonParser.Feature.ALLOW_SINGLE_QUOTES));

        mapper.disable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
        assertFalse(mapper.isEnabled(JsonParser.Feature.ALLOW_SINGLE_QUOTES));
    }

    @Test(timeout = 4000)
    public void testLocaleAndTimeZoneConfiguration() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setLocale(Locale.GERMANY);
        assertEquals(Locale.GERMANY, mapper.getSerializationConfig().getLocale());

        TimeZone tz = TimeZone.getTimeZone("GMT+2");
        mapper.setTimeZone(tz);
        assertEquals(tz, mapper.getSerializationConfig().getTimeZone());

        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        mapper.setDateFormat(df);
        assertEquals(df, mapper.getDateFormat());
    }

    @Test(timeout = 4000)
    public void testSetSerializationInclusion() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        assertEquals(JsonInclude.Include.NON_NULL,
                mapper.getSerializationConfig().getDefaultPropertyInclusion().getValueInclusion());
    }

    @Test(timeout = 4000)
    public void testMixInAnnotations() {
        ObjectMapper mapper = new ObjectMapper();
        assertEquals(0, mapper.mixInCount());

        mapper.addMixIn(SimpleBean.class, Object.class);
        assertEquals(1, mapper.mixInCount());
        assertEquals(Object.class, mapper.findMixInClassFor(SimpleBean.class));

        mapper.setMixIns(Collections.<Class<?>, Class<?>>emptyMap());
        assertEquals(0, mapper.mixInCount());
    }

    @Test(timeout = 4000)
    public void testSubtypeRegistration() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(SimpleBean.class);
        mapper.registerSubtypes(new NamedType(DataWithPrimitive.class, "dataWithPrim"));
        assertNotNull(mapper.getSubtypeResolver());
    }

    @Test(timeout = 4000)
    public void testConfigOverride() {
        ObjectMapper mapper = new ObjectMapper();
        assertNotNull(mapper.configOverride(Integer.class));
    }

    /*
     * ----------------------------------------------------------------------
     * Partition E: Lifecycle, ObjectReader, ObjectWriter & Copy Integrity
     * ----------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testCopyObjectMapper() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.addMixIn(SimpleBean.class, Object.class);

        ObjectMapper copy = mapper.copy();
        assertNotSame(mapper, copy);
        assertFalse(copy.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        assertEquals(1, copy.mixInCount());

        SimpleBean bean = new SimpleBean("copyTest", 123);
        String json = copy.writeValueAsString(bean);
        SimpleBean read = copy.readValue(json, SimpleBean.class);
        assertEquals("copyTest", read.name);
    }

    @Test(timeout = 4000)
    public void testWriterAndReaderConstruction() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        ObjectWriter writer = mapper.writer();
        assertNotNull(writer);

        ObjectWriter writerWithFeature = mapper.writer(SerializationFeature.INDENT_OUTPUT);
        assertTrue(writerWithFeature.isEnabled(SerializationFeature.INDENT_OUTPUT));

        ObjectReader reader = mapper.readerFor(SimpleBean.class);
        assertNotNull(reader);

        SimpleBean bean = new SimpleBean("readerWriter", 333);
        String json = writer.writeValueAsString(bean);
        SimpleBean read = reader.readValue(json);
        assertEquals("readerWriter", read.name);
        assertEquals(333, read.value);
    }

    @Test(timeout = 4000)
    public void testReaderForUpdating() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleBean target = new SimpleBean("initial", 1);

        ObjectReader updater = mapper.readerForUpdating(target);
        updater.readValue("{\"name\":\"updated\"}");

        assertEquals("updated", target.name);
        assertEquals(1, target.value);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEnableDefaultTypingExternalPropertyThrowsIAE() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE,
                JsonTypeInfo.As.EXTERNAL_PROPERTY);
    }

    @Test(timeout = 4000)
    public void testNodeFactoryAndClearProblemHandlers() {
        ObjectMapper mapper = new ObjectMapper();
        JsonNodeFactory nf = JsonNodeFactory.instance;
        mapper.setNodeFactory(nf);
        assertSame(nf, mapper.getNodeFactory());

        mapper.clearProblemHandlers();
        assertNotNull(mapper.getDeserializationConfig());
    }

    @Test(timeout = 4000)
    public void testVersionAndJsonFactoryAccess() {
        ObjectMapper mapper = new ObjectMapper();
        assertNotNull(mapper.version());
        assertNotNull(mapper.getFactory());
        assertSame(mapper.getFactory(), mapper.getJsonFactory());
    }
}