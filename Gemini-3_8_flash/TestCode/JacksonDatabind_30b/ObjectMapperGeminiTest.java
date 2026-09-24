package com.fasterxml.jackson.databind;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.cfg.ContextAttributes;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.databind.type.TypeFactory;

/*
 * [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * Partition A: Core Functional Logic & State Transitions
 * - Construction variants: default, custom JsonFactory, custom SerializerProvider / DeserializationContext.
 * - Configuration state & fluent builders: configure(), enable(), disable() for MapperFeature,
 *   SerializationFeature, DeserializationFeature, JsonParser.Feature, JsonGenerator.Feature.
 * - Readers / Writers factory configuration variants (views, filters, pretty printers, schemas, type refs).
 * - Read/Write operations via Streaming, Byte Array, String, File, Reader, InputStream, and URL.
 * - Tree Model operations: createObjectNode, createArrayNode, treeToValue (including identity cast),
 *   valueToTree, readTree variants, writeTree variants.
 *
 * Partition B: Boundary Value Analysis (BVA) & Extremes
 * - Null handling: readTree(empty/null), convertValue(null), valueToTree(null), null pretty printer.
 * - Type assignment shortcuts: convertValue with matching non-generic target (identity bypass).
 * - Empty content handling: readValue on empty String / Stream throwing "end-of-input" JsonMappingException.
 * - DefaultTypeResolverBuilder: evaluation of useForType across JAVA_LANG_OBJECT, OBJECT_AND_NON_CONCRETE,
 *   NON_CONCRETE_AND_ARRAYS, NON_FINAL with array unwrapping, TreeNode exclusions, and final class guards.
 *
 * Partition C: Defect-Targeted Branch Zone (Defects4J #965)
 * - Targets: com.fasterxml.jackson.databind.jsontype.TestExternalId::testBigDecimal965
 * - Condition: Deserialization of high-precision floating numbers (e.g., -10000000000.0000000001)
 *   buffered via TokenBuffer when using As.EXTERNAL_PROPERTY polymorphic type info.
 * - Assertion: Verifies BigDecimal precision is retained rather than truncated to double scientific notation.
 *
 * Partition D: Exception & Defensive Guard Paths
 * - Invalid copy() invoked on a subclass that does not override copy() -> IllegalStateException.
 * - enableDefaultTyping with As.EXTERNAL_PROPERTY -> IllegalArgumentException.
 * - Module registration guards: null name or null version -> IllegalArgumentException.
 * - FormatSchema type verification failure -> IllegalArgumentException.
 * - Root name unwrapping mismatches and non-START_OBJECT input -> JsonMappingException.
 * - acceptJsonFormatVisitor with null JavaType -> IllegalArgumentException.
 *
 * Partition E: Object Lifecycle & Contract Integrity
 * - ObjectMapper.copy(): deep copy verification for configuration independence.
 * - Module registration lifecycle and setupContext callbacks.
 * - Closeable serialization lifecycle (CLOSE_CLOSEABLE feature).
 */
public class ObjectMapperGeminiTest {

    // --------------------------------------------------------------------
    // Test Fixtures & POJOs
    // --------------------------------------------------------------------

    public static class SampleBean {
        public String name;
        public int age;

        public SampleBean() {}

        public SampleBean(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }

    public static class CloseableBean implements Closeable {
        public String value = "active";
        public boolean closed = false;

        @Override
        public void close() throws IOException {
            closed = true;
        }
    }

    public static class SubclassWithoutCopy extends ObjectMapper {
        private static final long serialVersionUID = 1L;
    }

    @JsonFilter("filterBean")
    public static class FilteredBean {
        public String included = "yes";
        public String excluded = "no";
    }

    public static class Views {
        public static class Public {}
        public static class Internal extends Public {}
    }

    public static class ViewedBean {
        @JsonView(Views.Public.class)
        public String pub = "pubVal";

        @JsonView(Views.Internal.class)
        public String priv = "privVal";
    }

    public static class BigDecimalHolder965 {
        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
        @JsonSubTypes({
            @JsonSubTypes.Type(value = BigDecimal.class, name = "bd")
        })
        public Object value;
        public String type;

        public BigDecimalHolder965() {}
        public BigDecimalHolder965(Object v, String t) {
            this.value = v;
            this.type = t;
        }
    }

    // --------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // --------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testConstructorsAndConfigGetters() {
        ObjectMapper mapper = new ObjectMapper();
        assertNotNull(mapper.getFactory());
        assertNotNull(mapper.getJsonFactory());
        assertNotNull(mapper.getSerializationConfig());
        assertNotNull(mapper.getDeserializationConfig());
        assertNotNull(mapper.getDeserializationContext());
        assertNotNull(mapper.getSerializerFactory());
        assertNotNull(mapper.getSerializerProvider());
        assertNotNull(mapper.getTypeFactory());
        assertNotNull(mapper.getNodeFactory());
        assertNotNull(mapper.getVisibilityChecker());
        assertNotNull(mapper.getSubtypeResolver());

        ObjectMapper customMapper = new ObjectMapper(new MappingJsonFactory());
        assertNotNull(customMapper.getFactory());
    }

    @Test(timeout = 4000)
    public void testFeatureToggles() {
        ObjectMapper mapper = new ObjectMapper();

        mapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
        assertTrue(mapper.isEnabled(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY));
        mapper.disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
        assertFalse(mapper.isEnabled(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY));
        mapper.enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
        assertTrue(mapper.isEnabled(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY));

        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        assertTrue(mapper.isEnabled(SerializationFeature.INDENT_OUTPUT));
        mapper.disable(SerializationFeature.INDENT_OUTPUT);
        assertFalse(mapper.isEnabled(SerializationFeature.INDENT_OUTPUT));
        mapper.enable(SerializationFeature.INDENT_OUTPUT, SerializationFeature.FAIL_ON_EMPTY_BEANS);
        assertTrue(mapper.isEnabled(SerializationFeature.INDENT_OUTPUT));
        assertTrue(mapper.isEnabled(SerializationFeature.FAIL_ON_EMPTY_BEANS));
        mapper.disable(SerializationFeature.INDENT_OUTPUT, SerializationFeature.FAIL_ON_EMPTY_BEANS);
        assertFalse(mapper.isEnabled(SerializationFeature.INDENT_OUTPUT));

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        assertFalse(mapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        mapper.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        assertTrue(mapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        assertTrue(mapper.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));
        mapper.disable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        assertFalse(mapper.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));

        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        assertTrue(mapper.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));
        mapper.disable(JsonParser.Feature.ALLOW_COMMENTS);
        assertFalse(mapper.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));
        mapper.enable(JsonParser.Feature.ALLOW_COMMENTS);
        assertTrue(mapper.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));

        mapper.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, false);
        assertFalse(mapper.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES));
        mapper.enable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
        assertTrue(mapper.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES));
        mapper.disable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
        assertFalse(mapper.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES));

        assertTrue(mapper.isEnabled(JsonFactory.Feature.CANONICALIZE_FIELD_NAMES));
    }

    @Test(timeout = 4000)
    public void testSerializationAndDeserializationBasic() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SampleBean bean = new SampleBean("Alice", 30);

        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("\"name\":\"Alice\""));
        assertTrue(json.contains("\"age\":30"));

        byte[] bytes = mapper.writeValueAsBytes(bean);
        SampleBean fromBytes = mapper.readValue(bytes, SampleBean.class);
        assertEquals("Alice", fromBytes.name);
        assertEquals(30, fromBytes.age);

        SampleBean fromString = mapper.readValue(json, SampleBean.class);
        assertEquals("Alice", fromString.name);
        assertEquals(30, fromString.age);

        SampleBean fromTypeRef = mapper.readValue(json, new TypeReference<SampleBean>() {});
        assertEquals("Alice", fromTypeRef.name);

        JavaType javaType = mapper.constructType(SampleBean.class);
        SampleBean fromJavaType = mapper.readValue(json, javaType);
        assertEquals("Alice", fromJavaType.name);
    }

    @Test(timeout = 4000)
    public void testStreamAndFileOperations() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SampleBean bean = new SampleBean("Charlie", 40);

        File tempFile = File.createTempFile("jackson_test", ".json");
        tempFile.deleteOnExit();

        mapper.writeValue(tempFile, bean);
        SampleBean fromFileClass = mapper.readValue(tempFile, SampleBean.class);
        assertEquals("Charlie", fromFileClass.name);

        SampleBean fromFileTypeRef = mapper.readValue(tempFile, new TypeReference<SampleBean>() {});
        assertEquals(40, fromFileTypeRef.age);

        SampleBean fromFileJavaType = mapper.readValue(tempFile, mapper.constructType(SampleBean.class));
        assertEquals("Charlie", fromFileJavaType.name);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        mapper.writeValue(out, bean);
        SampleBean fromStream = mapper.readValue(new ByteArrayInputStream(out.toByteArray()), SampleBean.class);
        assertEquals("Charlie", fromStream.name);

        StringWriter writer = new StringWriter();
        mapper.writeValue(writer, bean);
        SampleBean fromReader = mapper.readValue(new StringReader(writer.toString()), SampleBean.class);
        assertEquals("Charlie", fromReader.name);

        URL url = tempFile.toURI().toURL();
        SampleBean fromUrl = mapper.readValue(url, SampleBean.class);
        assertEquals("Charlie", fromUrl.name);
        SampleBean fromUrlTypeRef = mapper.readValue(url, new TypeReference<SampleBean>() {});
        assertEquals("Charlie", fromUrlTypeRef.name);
        SampleBean fromUrlJavaType = mapper.readValue(url, mapper.constructType(SampleBean.class));
        assertEquals(40, fromUrlJavaType.age);
    }

    @Test(timeout = 4000)
    public void testTreeModelOperations() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode objNode = mapper.createObjectNode();
        objNode.put("name", "David");
        objNode.put("age", 22);

        ArrayNode arrNode = mapper.createArrayNode();
        arrNode.add(objNode);

        assertEquals(1, arrNode.size());

        JsonParser parser = mapper.treeAsTokens(objNode);
        assertNotNull(parser);
        JsonNode readNode = mapper.readTree(parser);
        assertEquals("David", readNode.get("name").asText());

        ObjectNode castNode = mapper.treeToValue(objNode, ObjectNode.class);
        assertSame(objNode, castNode);

        SampleBean bean = mapper.treeToValue(objNode, SampleBean.class);
        assertEquals("David", bean.name);
        assertEquals(22, bean.age);

        JsonNode convertedNode = mapper.valueToTree(bean);
        assertEquals("David", convertedNode.get("name").asText());

        StringWriter sw = new StringWriter();
        JsonGenerator g = mapper.getFactory().createGenerator(sw);
        mapper.writeTree(g, objNode);
        assertTrue(sw.toString().contains("\"David\""));

        StringWriter sw2 = new StringWriter();
        JsonGenerator g2 = mapper.getFactory().createGenerator(sw2);
        mapper.writeTree(g2, (TreeNode) objNode);
        assertTrue(sw2.toString().contains("\"David\""));
    }

    @Test(timeout = 4000)
    public void testReadTreeVariants() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"k\":\"v\"}";

        JsonNode fromString = mapper.readTree(json);
        assertEquals("v", fromString.get("k").asText());

        JsonNode fromBytes = mapper.readTree(json.getBytes("UTF-8"));
        assertEquals("v", fromBytes.get("k").asText());

        JsonNode fromStream = mapper.readTree(new ByteArrayInputStream(json.getBytes("UTF-8")));
        assertEquals("v", fromStream.get("k").asText());

        JsonNode fromReader = mapper.readTree(new StringReader(json));
        assertEquals("v", fromReader.get("k").asText());

        File f = File.createTempFile("tree_test", ".json");
        f.deleteOnExit();
        mapper.writeValue(f, fromString);
        JsonNode fromFile = mapper.readTree(f);
        assertEquals("v", fromFile.get("k").asText());

        JsonNode fromUrl = mapper.readTree(f.toURI().toURL());
        assertEquals("v", fromUrl.get("k").asText());
    }

    @Test(timeout = 4000)
    public void testReadValuesSequence() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"name\":\"A\",\"age\":1}{\"name\":\"B\",\"age\":2}";
        JsonParser p = mapper.getFactory().createParser(json);

        MappingIterator<SampleBean> it = mapper.readValues(p, SampleBean.class);
        assertTrue(it.hasNext());
        SampleBean b1 = it.next();
        assertEquals("A", b1.name);
        assertTrue(it.hasNext());
        SampleBean b2 = it.next();
        assertEquals("B", b2.name);
        assertFalse(it.hasNext());
    }

    @Test(timeout = 4000)
    public void testObjectReaderAndWriterBuilders() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        ObjectWriter writer = mapper.writer();
        assertNotNull(writer);
        assertNotNull(mapper.writer(SerializationFeature.INDENT_OUTPUT));
        assertNotNull(mapper.writer(SerializationFeature.INDENT_OUTPUT, SerializationFeature.FLUSH_AFTER_WRITE_VALUE));
        assertNotNull(mapper.writer(new SimpleDateFormat("yyyy-MM-dd")));
        assertNotNull(mapper.writerWithView(Views.Public.class));
        assertNotNull(mapper.writerFor(SampleBean.class));
        assertNotNull(mapper.writerFor(new TypeReference<SampleBean>() {}));
        assertNotNull(mapper.writerFor(mapper.constructType(SampleBean.class)));
        assertNotNull(mapper.writer((PrettyPrinter) null));
        assertNotNull(mapper.writerWithDefaultPrettyPrinter());
        assertNotNull(mapper.writer(Base64Variants.MIME));
        assertNotNull(mapper.writer(new CharacterEscapes() {
            private static final long serialVersionUID = 1L;
            @Override
            public int[] getEscapeCodesForAscii() { return standardAsciiEscapesForJSON(); }
            @Override
            public SerializableString getEscapeSequence(int ch) { return null; }
        }));
        assertNotNull(mapper.writer(ContextAttributes.getEmpty()));

        ObjectReader reader = mapper.reader();
        assertNotNull(reader);
        assertNotNull(mapper.reader(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        assertNotNull(mapper.reader(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT));
        assertNotNull(mapper.readerForUpdating(new SampleBean()));
        assertNotNull(mapper.readerFor(SampleBean.class));
        assertNotNull(mapper.readerFor(new TypeReference<SampleBean>() {}));
        assertNotNull(mapper.readerFor(mapper.constructType(SampleBean.class)));
        assertNotNull(mapper.reader(JsonNodeFactory.instance));
        assertNotNull(mapper.reader(new InjectableValues.Std()));
        assertNotNull(mapper.readerWithView(Views.Public.class));
        assertNotNull(mapper.reader(Base64Variants.MIME));
        assertNotNull(mapper.reader(ContextAttributes.getEmpty()));
    }

    @Test(timeout = 4000)
    public void testViewsAndFilters() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ViewedBean vb = new ViewedBean();

        String pubOnly = mapper.writerWithView(Views.Public.class).writeValueAsString(vb);
        assertTrue(pubOnly.contains("pubVal"));
        assertFalse(pubOnly.contains("privVal"));

        String internal = mapper.writerWithView(Views.Internal.class).writeValueAsString(vb);
        assertTrue(internal.contains("pubVal"));
        assertTrue(internal.contains("privVal"));

        SimpleFilterProvider filterProvider = new SimpleFilterProvider()
                .addFilter("filterBean", SimpleBeanPropertyFilter.filterOutAllExcept("included"));
        mapper.setFilterProvider(filterProvider);

        FilteredBean fb = new FilteredBean();
        String filteredJson = mapper.writeValueAsString(fb);
        assertTrue(filteredJson.contains("included"));
        assertFalse(filteredJson.contains("excluded"));
    }

    // --------------------------------------------------------------------
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // --------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testNullAndEmptyInputs() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        assertNull(mapper.convertValue(null, SampleBean.class));
        assertNull(mapper.convertValue(null, mapper.constructType(SampleBean.class)));
        assertNull(mapper.valueToTree(null));

        JsonParser emptyParser = mapper.getFactory().createParser("");
        assertNull(mapper.readTree(emptyParser));

        JsonParser nullTokenParser = mapper.getFactory().createParser("null");
        JsonNode nullNode = mapper.readTree(nullTokenParser);
        assertNotNull(nullNode);
        assertTrue(nullNode.isNull());

        SampleBean original = new SampleBean("Self", 1);
        SampleBean sameInstance = mapper.convertValue(original, SampleBean.class);
        assertSame(original, sameInstance);

        try {
            mapper.readValue("", SampleBean.class);
            fail("Expected JsonMappingException on empty content");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("No content to map"));
        }
    }

    @Test(timeout = 4000)
    public void testDefaultTypeResolverBuilderCoverage() {
        TypeFactory tf = TypeFactory.defaultInstance();

        ObjectMapper.DefaultTypeResolverBuilder jloTyper =
                new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT);
        assertTrue(jloTyper.useForType(tf.constructType(Object.class)));
        assertFalse(jloTyper.useForType(tf.constructType(String.class)));

        ObjectMapper.DefaultTypeResolverBuilder objNonConcreteTyper =
                new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);
        assertTrue(objNonConcreteTyper.useForType(tf.constructType(Object.class)));
        assertTrue(objNonConcreteTyper.useForType(tf.constructType(List.class)));
        assertFalse(objNonConcreteTyper.useForType(tf.constructType(String.class)));
        assertFalse(objNonConcreteTyper.useForType(tf.constructType(JsonNode.class)));

        ObjectMapper.DefaultTypeResolverBuilder arraysTyper =
                new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.NON_CONCRETE_AND_ARRAYS);
        assertTrue(arraysTyper.useForType(tf.constructType(List[].class)));
        assertFalse(arraysTyper.useForType(tf.constructType(String[].class)));

        ObjectMapper.DefaultTypeResolverBuilder nonFinalTyper =
                new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.NON_FINAL);
        assertTrue(nonFinalTyper.useForType(tf.constructType(SampleBean.class)));
        assertTrue(nonFinalTyper.useForType(tf.constructType(SampleBean[].class)));
        assertFalse(nonFinalTyper.useForType(tf.constructType(String.class)));
        assertFalse(nonFinalTyper.useForType(tf.constructType(JsonNode.class)));
    }

    @Test(timeout = 4000)
    public void testCanSerializeAndCanDeserialize() {
        ObjectMapper mapper = new ObjectMapper();
        assertTrue(mapper.canSerialize(SampleBean.class));

        AtomicReference<Throwable> cause = new AtomicReference<Throwable>();
        assertTrue(mapper.canSerialize(SampleBean.class, cause));
        assertNull(cause.get());

        JavaType type = mapper.constructType(SampleBean.class);
        assertTrue(mapper.canDeserialize(type));
        assertTrue(mapper.canDeserialize(type, cause));
        assertNull(cause.get());
    }

    // --------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (Defects4J #965)
    // --------------------------------------------------------------------

    /**
     * Target Defect: Defects4J bug #965
     * Underlying failure: When deserializing a BigDecimal property buffered by TokenBuffer
     * in polymorphic / external property scenarios, floating point precision must not be
     * degraded to a double representation (e.g. -1.0E+10 instead of -10000000000.0000000001).
     */
    @Test(timeout = 4000)
    public void testBigDecimal965() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);

        BigDecimal exp = new BigDecimal("-10000000000.0000000001");
        String json = "{\"value\": -10000000000.0000000001, \"type\": \"bd\"}";

        BigDecimalHolder965 result = mapper.readValue(json, BigDecimalHolder965.class);
        assertNotNull(result);
        assertNotNull(result.value);

        assertEquals("Expected BigDecimal = " + exp + "; got back BigDecimal = " + result.value,
                exp, result.value);
    }

    @Test(timeout = 4000)
    public void testBigDecimal965WithoutFeatureFlag() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        BigDecimal exp = new BigDecimal("-10000000000.0000000001");
        String json = "{\"value\": -10000000000.0000000001, \"type\": \"bd\"}";

        BigDecimalHolder965 result = mapper.readValue(json, BigDecimalHolder965.class);
        assertNotNull(result);
        assertEquals("Expected BigDecimal = " + exp + "; got back BigDecimal = " + result.value,
                exp, result.value);
    }

    // --------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // --------------------------------------------------------------------

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testCheckInvalidCopyThrowsOnSubclass() {
        SubclassWithoutCopy uncopyable = new SubclassWithoutCopy();
        uncopyable.copy();
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEnableDefaultTypingExternalPropertyThrows() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.EXTERNAL_PROPERTY);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testRegisterModuleWithoutNameThrows() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new Module() {
            @Override
            public String getModuleName() { return null; }
            @Override
            public Version version() { return Version.unknownVersion(); }
            @Override
            public void setupModule(SetupContext context) {}
        });
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testRegisterModuleWithoutVersionThrows() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new Module() {
            @Override
            public String getModuleName() { return "TestModule"; }
            @Override
            public Version version() { return null; }
            @Override
            public void setupModule(SetupContext context) {}
        });
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAcceptJsonFormatVisitorNullTypeThrows() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.acceptJsonFormatVisitor((JavaType) null, null);
    }

    @Test(timeout = 4000)
    public void testRootUnwrappingFailures() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.UNWRAP_ROOT_VALUE);

        try {
            mapper.readValue("{\"WrongRoot\":{\"name\":\"X\",\"age\":1}}", SampleBean.class);
            fail("Expected JsonMappingException due to root name mismatch");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("does not match expected"));
        } catch (IOException e) {
            fail("Unexpected IOException: " + e.getMessage());
        }

        try {
            mapper.readValue("\"A String\"", SampleBean.class);
            fail("Expected JsonMappingException due to non-START_OBJECT token");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Current token not START_OBJECT"));
        } catch (IOException e) {
            fail("Unexpected IOException: " + e.getMessage());
        }
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testVerifySchemaTypeFailure() {
        ObjectMapper mapper = new ObjectMapper();
        FormatSchema dummySchema = new FormatSchema() {
            @Override
            public String getSchemaType() {
                return "UNSUPPORTED_SCHEMA";
            }
        };
        mapper.writer(dummySchema);
    }

    // --------------------------------------------------------------------
    // Partition E: Object Lifecycle & Contract Integrity
    // --------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testCopyLifecycleAndIsolation() {
        ObjectMapper orig = new ObjectMapper();
        orig.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
        orig.addMixIn(SampleBean.class, ViewedBean.class);

        ObjectMapper copy = orig.copy();
        assertNotSame(orig, copy);
        assertNotSame(orig.getFactory(), copy.getFactory());
        assertEquals(orig.isEnabled(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY),
                     copy.isEnabled(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY));

        copy.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, false);
        assertTrue(orig.isEnabled(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY));
        assertFalse(copy.isEnabled(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY));

        assertEquals(1, copy.mixInCount());
        assertEquals(ViewedBean.class, copy.findMixInClassFor(SampleBean.class));
    }

    @Test(timeout = 4000)
    public void testModuleRegistrationAndDuplicateIgnore() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(MapperFeature.IGNORE_DUPLICATE_MODULE_REGISTRATIONS);

        final AtomicBoolean setupExecuted = new AtomicBoolean(false);
        Module module = new Module() {
            @Override
            public String getModuleName() { return "TestMod"; }
            @Override
            public Version version() { return new Version(1, 0, 0, null, "grp", "art"); }
            @Override
            public Object getTypeId() { return "TestModTypeId"; }
            @Override
            public void setupModule(SetupContext context) {
                assertNotNull(context.getMapperVersion());
                assertSame(mapper, context.getOwner());
                setupExecuted.set(true);
            }
        };

        mapper.registerModule(module);
        assertTrue(setupExecuted.get());

        setupExecuted.set(false);
        mapper.registerModule(module);
        assertFalse("Second registration should be ignored when IGNORE_DUPLICATE_MODULE_REGISTRATIONS is on",
                setupExecuted.get());

        mapper.registerModules(Collections.<Module>emptyList());
    }

    @Test(timeout = 4000)
    public void testCloseableAutoClosing() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.CLOSE_CLOSEABLE);

        CloseableBean bean = new CloseableBean();
        assertFalse(bean.closed);

        String json = mapper.writeValueAsString(bean);
        assertTrue(json.contains("active"));
        assertTrue("Closeable object should be closed after serialization", bean.closed);
    }

    @Test(timeout = 4000)
    public void testMiscellaneousConfigModifiers() {
        ObjectMapper mapper = new ObjectMapper();

        DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        mapper.setDateFormat(df);
        assertSame(df, mapper.getDateFormat());

        Locale locale = Locale.GERMANY;
        mapper.setLocale(locale);
        assertEquals(locale, mapper.getSerializationConfig().getLocale());

        TimeZone tz = TimeZone.getTimeZone("GMT+2");
        mapper.setTimeZone(tz);
        assertEquals(tz, mapper.getSerializationConfig().getTimeZone());

        PropertyNamingStrategy pns = PropertyNamingStrategy.SNAKE_CASE;
        mapper.setPropertyNamingStrategy(pns);
        assertSame(pns, mapper.getPropertyNamingStrategy());

        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        assertEquals(JsonInclude.Include.NON_NULL, mapper.getSerializationConfig().getSerializationInclusion());

        DeserializationProblemHandler handler = new DeserializationProblemHandler() {};
        mapper.addHandler(handler);
        mapper.clearProblemHandlers();

        mapper.setBase64Variant(Base64Variants.MODIFIED_FOR_URL);
        assertEquals(Base64Variants.MODIFIED_FOR_URL, mapper.getSerializationConfig().getBase64Variant());

        mapper.registerSubtypes(new NamedType(SampleBean.class, "sample"));
        mapper.registerSubtypes(SampleBean.class);
    }
}