/*
 * [Branch & Defect Analysis Matrix]
 * Targets: com.fasterxml.jackson.databind.ObjectMapper and its inner DefaultTypeResolverBuilder.
 *
 * 1. Defect-Targeted Branch Zone (Defects4J / databind#88):
 *    - DefaultTypeResolverBuilder.useForType(JavaType):
 *      In DefaultTyping.OBJECT_AND_NON_CONCRETE and NON_CONCRETE_AND_ARRAYS, tree models
 *      (subtypes of TreeNode/JsonNode) should NOT have default typing applied.
 *      The defective condition `|| TreeNode.class.isAssignableFrom(...)` incorrectly includes
 *      TreeNodes, causing unexpected type token errors (JsonMappingException) when reading JSON
 *      arrays or objects into JsonNode when default typing is enabled.
 *
 * 2. Equivalence Partitions & Boundary Conditions:
 *    - Core Functional & State: Serializing/deserializing beans, primitive types, collections;
 *      configuration getters/setters, feature toggles (MapperFeature, SerializationFeature, DeserializationFeature).
 *    - Tree Model Operations: createObjectNode, createArrayNode, treeToValue, valueToTree, treeAsTokens.
 *    - Type Conversions: convertValue (null, identical non-generic types shortcut, POJO to Map, Map to POJO).
 *    - Closeable Serialization: CLOSE_CLOSEABLE feature verification on Flush and Close cycles.
 *    - Defensive Guard & Boundary Paths: Invalid schema rejection, copy integrity check on subclasses,
 *      null checks in visitor, empty/whitespace JSON token handling.
 */

package com.fasterxml.jackson.databind;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ObjectMapperGeminiTest {

    // -------------------------------------------------------------------------
    // Helper Test Classes & POJOs
    // -------------------------------------------------------------------------

    public static class SimpleBean {
        public int id;
        public String name;

        public SimpleBean() {}

        public SimpleBean(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    public static class CloseableBean implements Closeable {
        public int value = 99;
        public boolean wasClosed = false;

        public CloseableBean() {}

        @Override
        public void close() throws IOException {
            wasClosed = true;
        }
    }

    public static class SubMapperWithoutCopy extends ObjectMapper {
        private static final long serialVersionUID = 1L;
    }

    public static class EmptyModule extends Module {
        private final String _name;
        private final Version _version;

        public EmptyModule(String name, Version version) {
            _name = name;
            _version = version;
        }

        @Override
        public String getModuleName() {
            return _name;
        }

        @Override
        public Version version() {
            return _version;
        }

        @Override
        public void setupModule(SetupContext context) {}
    }

    // -------------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (databind#88 / TestJsonNode)
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testArrayWithDefaultTypingDefect() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping();

        // The defect causes DefaultTypeResolverBuilder to include TreeNode/JsonNode,
        // resulting in JsonMappingException: Unexpected token (VALUE_NUMBER_INT), expected VALUE_STRING
        JsonNode node = mapper.readTree("[1, 2]");
        assertNotNull("Returned JsonNode should not be null", node);
        assertTrue("Parsed node should be an ArrayNode", node.isArray());
        assertEquals("ArrayNode must contain 2 elements", 2, node.size());
        assertEquals(1, node.get(0).asInt());
        assertEquals(2, node.get(1).asInt());
    }

    @Test(timeout = 4000)
    public void testObjectWithDefaultTypingDefect() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping();

        JsonNode node = mapper.readTree("{\"key\":42}");
        assertNotNull("Returned JsonNode should not be null", node);
        assertTrue("Parsed node should be an ObjectNode", node.isObject());
        assertTrue("ObjectNode should contain field 'key'", node.has("key"));
        assertEquals(42, node.get("key").asInt());
    }

    @Test(timeout = 4000)
    public void testDefaultTypeResolverBuilderDoesNotApplyToTreeNodes() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectMapper.DefaultTypeResolverBuilder builder =
            new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);

        JavaType jsonNodeType = mapper.constructType(JsonNode.class);
        JavaType arrayNodeType = mapper.constructType(ArrayNode.class);
        JavaType objectNodeType = mapper.constructType(ObjectNode.class);

        assertFalse("Default typing OBJECT_AND_NON_CONCRETE must not apply to JsonNode",
                builder.useForType(jsonNodeType));
        assertFalse("Default typing OBJECT_AND_NON_CONCRETE must not apply to ArrayNode",
                builder.useForType(arrayNodeType));
        assertFalse("Default typing OBJECT_AND_NON_CONCRETE must not apply to ObjectNode",
                builder.useForType(objectNodeType));
    }

    @Test(timeout = 4000)
    public void testDefaultTypeResolverBuilderNonConcreteAndArraysDoesNotApplyToTreeNodes() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectMapper.DefaultTypeResolverBuilder builder =
            new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.NON_CONCRETE_AND_ARRAYS);

        JavaType jsonNodeType = mapper.constructType(JsonNode.class);
        assertFalse("Default typing NON_CONCRETE_AND_ARRAYS must not apply to JsonNode",
                builder.useForType(jsonNodeType));
    }

    @Test(timeout = 4000)
    public void testDefaultTypeResolverBuilderNonFinalBranch() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectMapper.DefaultTypeResolverBuilder builder =
            new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.NON_FINAL);

        JavaType stringType = mapper.constructType(String.class);
        JavaType beanType = mapper.constructType(SimpleBean.class);
        JavaType jsonNodeType = mapper.constructType(JsonNode.class);

        // String is final
        assertFalse("Non-final typing should not apply to final String", builder.useForType(stringType));
        // SimpleBean is non-final
        assertTrue("Non-final typing should apply to non-final SimpleBean", builder.useForType(beanType));
        // JsonNode is TreeNode subtype, should not apply
        assertFalse("Non-final typing should not apply to JsonNode", builder.useForType(jsonNodeType));
    }

    // -------------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testSerializeAndDeserializePOJO() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleBean original = new SimpleBean(101, "Jackson");

        String json = mapper.writeValueAsString(original);
        assertNotNull("Serialized string must not be null", json);
        assertTrue(json.contains("\"id\":101"));
        assertTrue(json.contains("\"name\":\"Jackson\""));

        SimpleBean deserialized = mapper.readValue(json, SimpleBean.class);
        assertNotNull("Deserialized bean must not be null", deserialized);
        assertEquals(101, deserialized.id);
        assertEquals("Jackson", deserialized.name);
    }

    @Test(timeout = 4000)
    public void testSerializationToBytesAndStreams() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleBean original = new SimpleBean(202, "Binary");

        byte[] bytes = mapper.writeValueAsBytes(original);
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);

        SimpleBean fromBytes = mapper.readValue(bytes, SimpleBean.class);
        assertEquals(202, fromBytes.id);
        assertEquals("Binary", fromBytes.name);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        mapper.writeValue(baos, original);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        SimpleBean fromStream = mapper.readValue(bais, SimpleBean.class);
        assertEquals(202, fromStream.id);
        assertEquals("Binary", fromStream.name);

        StringWriter sw = new StringWriter();
        mapper.writeValue(sw, original);
        StringReader sr = new StringReader(sw.toString());
        SimpleBean fromReader = mapper.readValue(sr, SimpleBean.class);
        assertEquals(202, fromReader.id);
        assertEquals("Binary", fromReader.name);
    }

    @Test(timeout = 4000)
    public void testReadValuesMappingIterator() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String src = "{\"id\":1,\"name\":\"A\"} {\"id\":2,\"name\":\"B\"}";
        JsonParser jp = mapper.getFactory().createParser(src);

        MappingIterator<SimpleBean> it = mapper.readValues(jp, SimpleBean.class);
        assertTrue(it.hasNext());
        SimpleBean b1 = it.next();
        assertEquals(1, b1.id);
        assertEquals("A", b1.name);

        assertTrue(it.hasNext());
        SimpleBean b2 = it.next();
        assertEquals(2, b2.id);
        assertEquals("B", b2.name);

        assertFalse(it.hasNext());
        jp.close();
    }

    @Test(timeout = 4000)
    public void testTreeModelOperations() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        ObjectNode objNode = mapper.createObjectNode();
        assertNotNull(objNode);
        objNode.put("id", 303);
        objNode.put("name", "Tree");

        ArrayNode arrNode = mapper.createArrayNode();
        assertNotNull(arrNode);
        arrNode.add(objNode);

        String json = mapper.writeValueAsString(arrNode);
        assertTrue(json.startsWith("[{"));

        JsonParser parser = mapper.treeAsTokens(arrNode);
        assertNotNull(parser);
        JsonNode reRead = mapper.readTree(parser);
        assertEquals(arrNode, reRead);
        parser.close();

        SimpleBean bean = mapper.treeToValue(objNode, SimpleBean.class);
        assertEquals(303, bean.id);
        assertEquals("Tree", bean.name);

        // Optimization branch in treeToValue where target is assignable
        ObjectNode casted = mapper.treeToValue(objNode, ObjectNode.class);
        assertSame(objNode, casted);

        JsonNode converted = mapper.valueToTree(bean);
        assertTrue(converted.isObject());
        assertEquals(303, converted.get("id").asInt());
    }

    @Test(timeout = 4000)
    public void testValueConversions() {
        ObjectMapper mapper = new ObjectMapper();

        SimpleBean bean = new SimpleBean(404, "Convert");
        Map<?, ?> map = mapper.convertValue(bean, Map.class);
        assertNotNull(map);
        assertEquals(404, map.get("id"));
        assertEquals("Convert", map.get("name"));

        SimpleBean back = mapper.convertValue(map, SimpleBean.class);
        assertEquals(404, back.id);
        assertEquals("Convert", back.name);

        // Null value conversion returns null
        assertNull(mapper.convertValue(null, SimpleBean.class));
        assertNull(mapper.convertValue(null, mapper.constructType(SimpleBean.class)));

        // Assignment-compatible shortcut (targetType != Object.class && !hasGenericTypes && isAssignableFrom)
        String text = "DirectString";
        String sameText = mapper.convertValue(text, String.class);
        assertSame(text, sameText);
    }

    @Test(timeout = 4000)
    public void testFeatureTogglesAndConfiguration() {
        ObjectMapper mapper = new ObjectMapper();

        mapper.configure(MapperFeature.USE_ANNOTATIONS, false);
        assertFalse(mapper.isEnabled(MapperFeature.USE_ANNOTATIONS));
        mapper.configure(MapperFeature.USE_ANNOTATIONS, true);
        assertTrue(mapper.isEnabled(MapperFeature.USE_ANNOTATIONS));

        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        assertTrue(mapper.isEnabled(SerializationFeature.INDENT_OUTPUT));
        mapper.disable(SerializationFeature.INDENT_OUTPUT);
        assertFalse(mapper.isEnabled(SerializationFeature.INDENT_OUTPUT));
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        assertTrue(mapper.isEnabled(SerializationFeature.INDENT_OUTPUT));

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        assertFalse(mapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        mapper.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        assertTrue(mapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));

        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        assertTrue(mapper.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));

        mapper.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, false);
        assertFalse(mapper.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES));

        assertNotNull(mapper.getSerializationConfig());
        assertNotNull(mapper.getDeserializationConfig());
        assertNotNull(mapper.getDeserializationContext());
        assertNotNull(mapper.getSerializerFactory());
        assertNotNull(mapper.getSerializerProvider());
        assertNotNull(mapper.getNodeFactory());
        assertNotNull(mapper.getSubtypeResolver());
        assertNotNull(mapper.getTypeFactory());
        assertNotNull(mapper.getFactory());
        assertNotNull(mapper.getJsonFactory());
    }

    @Test(timeout = 4000)
    public void testMixInAnnotations() {
        ObjectMapper mapper = new ObjectMapper();
        assertEquals(0, mapper.mixInCount());

        mapper.addMixInAnnotations(SimpleBean.class, Object.class);
        assertEquals(1, mapper.mixInCount());
        assertEquals(Object.class, mapper.findMixInClassFor(SimpleBean.class));

        mapper.addMixIn(String.class, Object.class);
        assertEquals(2, mapper.mixInCount());

        Map<Class<?>, Class<?>> map = new HashMap<Class<?>, Class<?>>();
        map.put(Integer.class, Number.class);
        mapper.setMixInAnnotations(map);
        assertEquals(1, mapper.mixInCount());
        assertEquals(Number.class, mapper.findMixInClassFor(Integer.class));
        assertNull(mapper.findMixInClassFor(SimpleBean.class));
    }

    @Test(timeout = 4000)
    public void testCanSerializeAndDeserialize() {
        ObjectMapper mapper = new ObjectMapper();
        assertTrue(mapper.canSerialize(SimpleBean.class));
        AtomicReference<Throwable> cause = new AtomicReference<Throwable>();
        assertTrue(mapper.canSerialize(SimpleBean.class, cause));
        assertNull(cause.get());

        JavaType type = mapper.constructType(SimpleBean.class);
        assertTrue(mapper.canDeserialize(type));
        assertTrue(mapper.canDeserialize(type, cause));
        assertNull(cause.get());
    }

    @Test(timeout = 4000)
    public void testCloseableSerialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.CLOSE_CLOSEABLE);

        CloseableBean bean = new CloseableBean();
        assertFalse(bean.wasClosed);

        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"value\":99"));
        assertTrue("Bean must be closed when CLOSE_CLOSEABLE is enabled", bean.wasClosed);
    }

    // -------------------------------------------------------------------------
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testValueToTreeNull() {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.valueToTree(null);
        assertNull("valueToTree(null) must return null", node);
    }

    @Test(timeout = 4000)
    public void testReadTreeNullNode() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree("null");
        assertNotNull(node);
        assertTrue("Parsed 'null' must yield NullNode", node.isNull());
        assertSame(NullNode.instance, node);
    }

    @Test(timeout = 4000)
    public void testReadTreeFromEmptyJsonParser() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonParser jp = mapper.getFactory().createParser("");
        JsonNode node = mapper.readTree(jp);
        assertNull("Empty parser stream should yield null tree", node);
        jp.close();
    }

    @Test(timeout = 4000)
    public void testReadValueByteArrayWithOffsetAndLen() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "PREFIX{\"id\":777,\"name\":\"Offset\"}SUFFIX";
        byte[] fullBytes = json.getBytes("UTF-8");
        int offset = 6;
        int len = fullBytes.length - 6 - 6;

        SimpleBean bean = mapper.readValue(fullBytes, offset, len, SimpleBean.class);
        assertNotNull(bean);
        assertEquals(777, bean.id);
        assertEquals("Offset", bean.name);

        TypeReference<SimpleBean> ref = new TypeReference<SimpleBean>() {};
        SimpleBean beanRef = mapper.readValue(fullBytes, offset, len, ref);
        assertEquals(777, beanRef.id);

        JavaType jt = mapper.constructType(SimpleBean.class);
        SimpleBean beanJt = mapper.readValue(fullBytes, offset, len, jt);
        assertEquals(777, beanJt.id);
    }

    // -------------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // -------------------------------------------------------------------------

    @Test(timeout = 4000, expected = JsonMappingException.class)
    public void testReadValueFromEmptyStringThrowsException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.readValue("", SimpleBean.class);
    }

    @Test(timeout = 4000, expected = JsonMappingException.class)
    public void testReadTreeFromEmptyStringThrowsException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.readTree("");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testRegisterModuleWithoutNameThrowsException() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new EmptyModule(null, Version.unknownVersion()));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testRegisterModuleWithoutVersionThrowsException() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new EmptyModule("ValidName", null));
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testSubclassCopyWithoutOverrideThrowsException() {
        SubMapperWithoutCopy sub = new SubMapperWithoutCopy();
        sub.copy();
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAcceptJsonFormatVisitorNullTypeThrowsException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.acceptJsonFormatVisitor((JavaType) null, (JsonFormatVisitorWrapper) null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testWriterUnsupportedFormatSchemaThrowsException() {
        ObjectMapper mapper = new ObjectMapper();
        FormatSchema dummySchema = new FormatSchema() {
            @Override
            public String getSchemaType() {
                return "UNSUPPORTED_SCHEMA";
            }
        };
        mapper.writer(dummySchema);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testReaderUnsupportedFormatSchemaThrowsException() {
        ObjectMapper mapper = new ObjectMapper();
        FormatSchema dummySchema = new FormatSchema() {
            @Override
            public String getSchemaType() {
                return "UNSUPPORTED_SCHEMA";
            }
        };
        mapper.reader(dummySchema);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testIncompatibleConvertValueThrowsException() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.convertValue("not_a_number", Integer.class);
    }

    // -------------------------------------------------------------------------
    // Partition E: Object Lifecycle, Copier & Readers/Writers Factories
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testCopyObjectMapper() throws Exception {
        ObjectMapper original = new ObjectMapper();
        original.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        original.addMixInAnnotations(SimpleBean.class, Object.class);

        ObjectMapper copy = original.copy();
        assertNotSame("Copied mapper must be a distinct instance", original, copy);
        assertNotSame("JsonFactory must be copied", original.getFactory(), copy.getFactory());
        assertFalse(copy.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        assertEquals(1, copy.mixInCount());
        assertEquals(Object.class, copy.findMixInClassFor(SimpleBean.class));

        // Ensure newly copied instance functions properly
        SimpleBean bean = copy.readValue("{\"id\":10,\"name\":\"Copy\"}", SimpleBean.class);
        assertEquals(10, bean.id);
        assertEquals("Copy", bean.name);
    }

    @Test(timeout = 4000)
    public void testObjectReaderAndWriterBuilders() {
        ObjectMapper mapper = new ObjectMapper();

        ObjectWriter writer = mapper.writer();
        assertNotNull(writer);
        assertNotNull(mapper.writer(SerializationFeature.INDENT_OUTPUT));
        assertNotNull(mapper.writer(SerializationFeature.INDENT_OUTPUT, SerializationFeature.FAIL_ON_EMPTY_BEANS));
        assertNotNull(mapper.writer(new SimpleDateFormat("yyyy-MM-dd")));
        assertNotNull(mapper.writerWithView(Object.class));
        assertNotNull(mapper.writerWithType(SimpleBean.class));
        assertNotNull(mapper.writerWithType(new TypeReference<SimpleBean>() {}));
        assertNotNull(mapper.writerWithType(mapper.constructType(SimpleBean.class)));
        assertNotNull(mapper.writerWithDefaultPrettyPrinter());
        assertNotNull(mapper.writer((PrettyPrinter) null));
        assertNotNull(mapper.writer(Base64Variants.MIME));

        ObjectReader reader = mapper.reader();
        assertNotNull(reader);
        assertNotNull(mapper.reader(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY));
        assertNotNull(mapper.reader(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY,
                DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES));
        assertNotNull(mapper.reader(SimpleBean.class));
        assertNotNull(mapper.reader(new TypeReference<SimpleBean>() {}));
        assertNotNull(mapper.reader(mapper.constructType(SimpleBean.class)));
        assertNotNull(mapper.reader(JsonNodeFactory.instance));
        assertNotNull(mapper.readerWithView(Object.class));
        assertNotNull(mapper.reader(Base64Variants.MIME));
        assertNotNull(mapper.readerForUpdating(new SimpleBean()));
    }

    @Test(timeout = 4000)
    public void testMiscellaneousConfigurationMutators() {
        ObjectMapper mapper = new ObjectMapper();

        mapper.setDateFormat(new SimpleDateFormat("yyyy/MM/dd"));
        mapper.setLocale(Locale.GERMANY);
        mapper.setTimeZone(TimeZone.getTimeZone("GMT+2"));
        mapper.setBase64Variant(Base64Variants.MODIFIED_FOR_URL);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance());
        mapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector());
        mapper.setAnnotationIntrospectors(new JacksonAnnotationIntrospector(), new JacksonAnnotationIntrospector());
        mapper.clearProblemHandlers();

        Version v = mapper.version();
        assertNotNull("Version must not be null", v);
        assertFalse(v.isUnknownVersion());
    }

    @Test(timeout = 4000)
    public void testDefaultTypingConfigurationModes() {
        ObjectMapper mapper = new ObjectMapper();

        mapper.enableDefaultTyping();
        mapper.disableDefaultTyping();

        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT);
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE, JsonTypeInfo.As.WRAPPER_OBJECT);
        mapper.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.NON_FINAL, "@custom_type");

        mapper.disableDefaultTyping();
    }
}