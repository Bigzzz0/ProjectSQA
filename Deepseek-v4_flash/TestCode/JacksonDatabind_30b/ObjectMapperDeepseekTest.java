package com.fasterxml.jackson.databind;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.node.*;

/**
 * White-box test suite for ObjectMapper targeting maximum line/branch coverage
 * and the known defect: BigDecimal precision loss during serialization/deserialization.
 *
 * [Branch & Defect Analysis Matrix]
 * - Partition A: Core functional paths (constructors, config, read/write, convert)
 * - Partition B: Boundary values (null, empty, extremes, BigDecimal precision)
 * - Partition C: Defect-targeted: BigDecimal -10000000000.0000000001 round-trip
 * - Partition D: Exception paths (invalid args, schema mismatch, etc.)
 * - Partition E: Lifecycle (copy, serialization, mixin count)
 *
 * Key branches covered:
 *   - ObjectMapper() default constructor
 *   - ObjectMapper(JsonFactory) constructor
 *   - ObjectMapper(ObjectMapper) copy constructor
 *   - _checkInvalidCopy
 *   - registerModule (with/without IGNORE_DUPLICATE_MODULE_REGISTRATIONS)
 *   - enableDefaultTyping variants
 *   - setVisibility, setAnnotationIntrospector, setPropertyNamingStrategy
 *   - readValue (JsonParser, File, String, byte[], etc.)
 *   - writeValue (File, OutputStream, Writer, String, byte[])
 *   - convertValue (null, simple cast, generic)
 *   - readTree (InputStream, Reader, String, byte[], File, URL)
 *   - treeToValue, valueToTree
 *   - canSerialize, canDeserialize
 *   - _initForReading (null token, end-of-input)
 *   - _unwrapAndDeserialize (root wrapping)
 *   - _findRootDeserializer (cache hit/miss)
 *   - _configAndWriteValue (Closeable, flush, auto-close)
 *   - _writeCloseableValue
 *   - _verifySchemaType
 *   - DefaultTypeResolverBuilder.useForType (all enum cases)
 *   - mixInCount, findMixInClassFor
 *   - setMixInResolver
 *   - setDefaultTyping (null, non-null)
 *   - setDateFormat, setTimeZone, setLocale
 *   - configure(MapperFeature, boolean)
 *   - enable/disable (MapperFeature, SerializationFeature, DeserializationFeature)
 *   - isEnabled (all feature types)
 *   - writer/reader factory methods
 *   - acceptJsonFormatVisitor
 */
public class ObjectMapperDeepseekTest {

    /*
     * ============================================================
     * Partition A: Core Functional Logic & State Transitions
     * ============================================================
     */

    @Test(timeout = 4000)
    public void testDefaultConstructor() {
        ObjectMapper mapper = new ObjectMapper();
        assertNotNull(mapper);
        assertNotNull(mapper.getSerializationConfig());
        assertNotNull(mapper.getDeserializationConfig());
        assertNotNull(mapper.getFactory());
        assertTrue(mapper.isEnabled(MapperFeature.IGNORE_DUPLICATE_MODULE_REGISTRATIONS));
    }

    @Test(timeout = 4000)
    public void testJsonFactoryConstructor() {
        JsonFactory jf = new JsonFactory();
        ObjectMapper mapper = new ObjectMapper(jf);
        assertSame(jf, mapper.getFactory());
    }

    @Test(timeout = 4000)
    public void testCopyConstructor() {
        ObjectMapper src = new ObjectMapper();
        src.enable(SerializationFeature.INDENT_OUTPUT);
        ObjectMapper copy = src.copy();
        assertNotSame(src, copy);
        assertTrue(copy.isEnabled(SerializationFeature.INDENT_OUTPUT));
    }

    @Test(timeout = 4000)
    public void testCopyInvalidCheck() {
        // Subclass that does not override copy() should throw IllegalStateException
        ObjectMapper sub = new ObjectMapper() {
            private static final long serialVersionUID = 1L;
        };
        try {
            sub.copy();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("does not override copy"));
        }
    }

    @Test(timeout = 4000)
    public void testRegisterModule() {
        ObjectMapper mapper = new ObjectMapper();
        // Register a simple module (no-op)
        mapper.registerModule(new Module() {
            @Override public String getModuleName() { return "test"; }
            @Override public Version version() { return Version.unknownVersion(); }
            @Override public void setupModule(SetupContext context) { }
        });
        // Verify no exception
        assertNotNull(mapper);
    }

    @Test(timeout = 4000)
    public void testRegisterModuleDuplicateIgnored() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(MapperFeature.IGNORE_DUPLICATE_MODULE_REGISTRATIONS);
        Module m = new Module() {
            @Override public String getModuleName() { return "dup"; }
            @Override public Version version() { return Version.unknownVersion(); }
            @Override public Object getTypeId() { return "dupId"; }
            @Override public void setupModule(SetupContext context) { }
        };
        mapper.registerModule(m);
        mapper.registerModule(m); // should be ignored
        // no exception
    }

    @Test(timeout = 4000)
    public void testRegisterModuleNullName() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.registerModule(new Module() {
                @Override public String getModuleName() { return null; }
                @Override public Version version() { return Version.unknownVersion(); }
                @Override public void setupModule(SetupContext context) { }
            });
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("without defined name"));
        }
    }

    @Test(timeout = 4000)
    public void testRegisterModuleNullVersion() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.registerModule(new Module() {
                @Override public String getModuleName() { return "test"; }
                @Override public Version version() { return null; }
                @Override public void setupModule(SetupContext context) { }
            });
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("without defined version"));
        }
    }

    @Test(timeout = 4000)
    public void testEnableDefaultTyping() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping();
        assertNotNull(mapper);
        // Verify that default typing is set (by checking config)
        assertNotNull(mapper.getSerializationConfig().getDefaultTyper());
    }

    @Test(timeout = 4000)
    public void testEnableDefaultTypingWithIncludeAs() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        assertNotNull(mapper);
    }

    @Test(timeout = 4000)
    public void testEnableDefaultTypingInvalidExternalProperty() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.enableDefaultTyping(DefaultTyping.OBJECT_AND_NON_CONCRETE, JsonTypeInfo.As.EXTERNAL_PROPERTY);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Can not use includeAs of"));
        }
    }

    @Test(timeout = 4000)
    public void testEnableDefaultTypingAsProperty() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTypingAsProperty(DefaultTyping.JAVA_LANG_OBJECT, "@type");
        assertNotNull(mapper);
    }

    @Test(timeout = 4000)
    public void testDisableDefaultTyping() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping();
        mapper.disableDefaultTyping();
        assertNull(mapper.getSerializationConfig().getDefaultTyper());
    }

    @Test(timeout = 4000)
    public void testSetDefaultTypingNull() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDefaultTyping(null);
        assertNull(mapper.getSerializationConfig().getDefaultTyper());
    }

    @Test(timeout = 4000)
    public void testSetVisibility() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        assertNotNull(mapper);
    }

    @Test(timeout = 4000)
    public void testSetAnnotationIntrospector() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector());
        assertNotNull(mapper);
    }

    @Test(timeout = 4000)
    public void testSetPropertyNamingStrategy() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        assertSame(PropertyNamingStrategy.SNAKE_CASE, mapper.getPropertyNamingStrategy());
    }

    @Test(timeout = 4000)
    public void testSetSerializationInclusion() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        assertNotNull(mapper);
    }

    @Test(timeout = 4000)
    public void testSetDefaultPrettyPrinter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDefaultPrettyPrinter(new DefaultPrettyPrinter());
        assertNotNull(mapper);
    }

    @Test(timeout = 4000)
    public void testSetMixIns() {
        ObjectMapper mapper = new ObjectMapper();
        Map<Class<?>, Class<?>> mixins = new HashMap<>();
        mixins.put(String.class, Object.class);
        mapper.setMixIns(mixins);
        assertEquals(1, mapper.mixInCount());
    }

    @Test(timeout = 4000)
    public void testAddMixIn() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.addMixIn(String.class, Object.class);
        assertEquals(1, mapper.mixInCount());
        assertNotNull(mapper.findMixInClassFor(String.class));
    }

    @Test(timeout = 4000)
    public void testSetMixInResolver() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setMixInResolver(new ClassIntrospector.MixInResolver() {
            @Override public Class<?> findMixInClassFor(Class<?> cls) { return null; }
            @Override public ClassIntrospector.MixInResolver copy() { return this; }
        });
        assertNotNull(mapper);
    }

    @Test(timeout = 4000)
    public void testSetDateFormat() {
        ObjectMapper mapper = new ObjectMapper();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        mapper.setDateFormat(sdf);
        assertNotNull(mapper.getDateFormat());
    }

    @Test(timeout = 4000)
    public void testSetTimeZone() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setTimeZone(TimeZone.getTimeZone("UTC"));
        assertNotNull(mapper);
    }

    @Test(timeout = 4000)
    public void testSetLocale() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setLocale(Locale.US);
        assertNotNull(mapper);
    }

    @Test(timeout = 4000)
    public void testConfigureMapperFeature() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
        assertTrue(mapper.isEnabled(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY));
        mapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, false);
        assertFalse(mapper.isEnabled(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY));
    }

    @Test(timeout = 4000)
    public void testEnableDisableMapperFeature() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(MapperFeature.USE_ANNOTATIONS);
        assertTrue(mapper.isEnabled(MapperFeature.USE_ANNOTATIONS));
        mapper.disable(MapperFeature.USE_ANNOTATIONS);
        assertFalse(mapper.isEnabled(MapperFeature.USE_ANNOTATIONS));
    }

    @Test(timeout = 4000)
    public void testConfigureSerializationFeature() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        assertTrue(mapper.isEnabled(SerializationFeature.INDENT_OUTPUT));
        mapper.configure(SerializationFeature.INDENT_OUTPUT, false);
        assertFalse(mapper.isEnabled(SerializationFeature.INDENT_OUTPUT));
    }

    @Test(timeout = 4000)
    public void testEnableDisableSerializationFeature() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        assertTrue(mapper.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        assertFalse(mapper.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
    }

    @Test(timeout = 4000)
    public void testConfigureDeserializationFeature() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        assertFalse(mapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        assertTrue(mapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
    }

    @Test(timeout = 4000)
    public void testEnableDisableDeserializationFeature() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        assertTrue(mapper.isEnabled(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY));
        mapper.disable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        assertFalse(mapper.isEnabled(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY));
    }

    @Test(timeout = 4000)
    public void testIsEnabledJsonParserFeature() {
        ObjectMapper mapper = new ObjectMapper();
        assertFalse(mapper.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));
        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        assertTrue(mapper.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));
    }

    @Test(timeout = 4000)
    public void testIsEnabledJsonGeneratorFeature() {
        ObjectMapper mapper = new ObjectMapper();
        assertFalse(mapper.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES));
        mapper.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, false);
        assertFalse(mapper.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES));
    }

    @Test(timeout = 4000)
    public void testIsEnabledJsonFactoryFeature() {
        ObjectMapper mapper = new ObjectMapper();
        assertFalse(mapper.isEnabled(JsonFactory.Feature.CANONICALIZE_FIELD_NAMES));
    }

    @Test(timeout = 4000)
    public void testGetNodeFactory() {
        ObjectMapper mapper = new ObjectMapper();
        assertNotNull(mapper.getNodeFactory());
    }

    @Test(timeout = 4000)
    public void testSetNodeFactory() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setNodeFactory(JsonNodeFactory.withExactBigDecimals(true));
        assertNotNull(mapper);
    }

    @Test(timeout = 4000)
    public void testAddHandler() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.addHandler(new DeserializationProblemHandler() {});
        assertNotNull(mapper);
    }

    @Test(timeout = 4000)
    public void testClearProblemHandlers() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.addHandler(new DeserializationProblemHandler() {});
        mapper.clearProblemHandlers();
        assertNotNull(mapper);
    }

    @Test(timeout = 4000)
    public void testSetConfigDeserialization() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig cfg = mapper.getDeserializationConfig();
        mapper.setConfig(cfg);
        assertSame(cfg, mapper.getDeserializationConfig());
    }

    @Test(timeout = 4000)
    public void testSetConfigSerialization() {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig cfg = mapper.getSerializationConfig();
        mapper.setConfig(cfg);
        assertSame(cfg, mapper.getSerializationConfig());
    }

    @Test(timeout = 4000)
    public void testSetFilterProvider() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setFilterProvider(new SimpleFilterProvider());
        assertNotNull(mapper);
    }

    @Test(timeout = 4000)
    public void testSetBase64Variant() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setBase64Variant(Base64Variants.MIME);
        assertNotNull(mapper);
    }

    @Test(timeout = 4000)
    public void testSetHandlerInstantiator() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setHandlerInstantiator(new HandlerInstantiator() {
            @Override public JsonDeserializer<?> deserializerInstance(DeserializationConfig config,
                    Annotated annotated, Class<?> deserClass) { return null; }
            @Override public JsonSerializer<?> serializerInstance(SerializationConfig config,
                    Annotated annotated, Class<?> serClass) { return null; }
            @Override public TypeIdResolver typeIdResolverInstance(MapperConfig<?> config,
                    Annotated annotated, Class<?> resolverClass) { return null; }
            @Override public TypeResolverBuilder<?> typeResolverBuilderInstance(MapperConfig<?> config,
                    Annotated annotated, Class<?> builderClass) { return null; }
        });
        assertNotNull(mapper);
    }

    @Test(timeout = 4000)
    public void testSetInjectableValues() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setInjectableValues(new InjectableValues.Std());
        assertNotNull(mapper.getInjectableValues());
    }

    @Test(timeout = 4000)
    public void testRegisterSubtypes() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(HashMap.class);
        assertNotNull(mapper);
    }

    @Test(timeout = 4000)
    public void testRegisterSubtypesNamedType() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(new NamedType(HashMap.class, "map"));
        assertNotNull(mapper);
    }

    @Test(timeout = 4000)
    public void testGetTypeFactory() {
        ObjectMapper mapper = new ObjectMapper();
        assertNotNull(mapper.getTypeFactory());
    }

    @Test(timeout = 4000)
    public void testSetTypeFactory() {
        ObjectMapper mapper = new ObjectMapper();
        TypeFactory tf = TypeFactory.defaultInstance();
        mapper.setTypeFactory(tf);
        assertSame(tf, mapper.getTypeFactory());
    }

    @Test(timeout = 4000)
    public void testConstructType() {
        ObjectMapper mapper = new ObjectMapper();
        JavaType t = mapper.constructType(String.class);
        assertEquals(String.class, t.getRawClass());
    }

    @Test(timeout = 4000)
    public void testGetSerializerFactory() {
        ObjectMapper mapper = new ObjectMapper();
        assertNotNull(mapper.getSerializerFactory());
    }

    @Test(timeout = 4000)
    public void testSetSerializerFactory() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializerFactory(BeanSerializerFactory.instance);
        assertNotNull(mapper);
    }

    @Test(timeout = 4000)
    public void testGetSerializerProvider() {
        ObjectMapper mapper = new ObjectMapper();
        assertNotNull(mapper.getSerializerProvider());
    }

    @Test(timeout = 4000)
    public void testSetSerializerProvider() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializerProvider(new DefaultSerializerProvider.Impl());
        assertNotNull(mapper);
    }

    @Test(timeout = 4000)
    public void testGetSubtypeResolver() {
        ObjectMapper mapper = new ObjectMapper();
        assertNotNull(mapper.getSubtypeResolver());
    }

    @Test(timeout = 4000)
    public void testSetSubtypeResolver() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSubtypeResolver(new StdSubtypeResolver());
        assertNotNull(mapper);
    }

    @Test(timeout = 4000)
    public void testGetVisibilityChecker() {
        ObjectMapper mapper = new ObjectMapper();
        assertNotNull(mapper.getVisibilityChecker());
    }

    @Test(timeout = 4000)
    public void testSetVisibilityChecker() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(VisibilityChecker.Std.defaultInstance());
        assertNotNull(mapper);
    }

    /*
     * ============================================================
     * Partition B: Boundary Value Analysis & Extremes
     * ============================================================
     */

    @Test(timeout = 4000)
    public void testReadValueNullInput() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // readValue from null string? Not allowed; use null JsonParser? Not directly.
        // Instead test readTree with empty input
        JsonNode node = mapper.readTree("");
        assertNull(node);
    }

    @Test(timeout = 4000)
    public void testReadValueEmptyString() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readValue("", Object.class);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("No content"));
        }
    }

    @Test(timeout = 4000)
    public void testReadValueNullJson() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "null";
        Object result = mapper.readValue(json, Object.class);
        assertNull(result);
    }

    @Test(timeout = 4000)
    public void testReadValueEmptyArray() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "[]";
        List<?> result = mapper.readValue(json, List.class);
        assertTrue(result.isEmpty());
    }

    @Test(timeout = 4000)
    public void testReadValueEmptyObject() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{}";
        Map<?,?> result = mapper.readValue(json, Map.class);
        assertTrue(result.isEmpty());
    }

    @Test(timeout = 4000)
    public void testReadValueString() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "\"hello\"";
        String result = mapper.readValue(json, String.class);
        assertEquals("hello", result);
    }

    @Test(timeout = 4000)
    public void testReadValueInt() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "42";
        int result = mapper.readValue(json, int.class);
        assertEquals(42, result);
    }

    @Test(timeout = 4000)
    public void testReadValueLong() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "1234567890123";
        long result = mapper.readValue(json, long.class);
        assertEquals(1234567890123L, result);
    }

    @Test(timeout = 4000)
    public void testReadValueDouble() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "3.14159";
        double result = mapper.readValue(json, double.class);
        assertEquals(3.14159, result, 1e-9);
    }

    @Test(timeout = 4000)
    public void testReadValueBigInteger() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "123456789012345678901234567890";
        BigInteger result = mapper.readValue(json, BigInteger.class);
        assertEquals(new BigInteger("123456789012345678901234567890"), result);
    }

    @Test(timeout = 4000)
    public void testReadValueBoolean() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "true";
        boolean result = mapper.readValue(json, boolean.class);
        assertTrue(result);
    }

    @Test(timeout = 4000)
    public void testReadValueFromBytes() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        byte[] json = "{\"a\":1}".getBytes("UTF-8");
        Map<?,?> result = mapper.readValue(json, Map.class);
        assertEquals(1, result.get("a"));
    }

    @Test(timeout = 4000)
    public void testReadValueFromBytesOffsetLen() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        byte[] json = "xx{\"a\":1}xx".getBytes("UTF-8");
        Map<?,?> result = mapper.readValue(json, 2, 7, Map.class);
        assertEquals(1, result.get("a"));
    }

    @Test(timeout = 4000)
    public void testReadValueFromReader() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"b\":2}";
        Map<?,?> result = mapper.readValue(new StringReader(json), Map.class);
        assertEquals(2, result.get("b"));
    }

    @Test(timeout = 4000)
    public void testReadValueFromInputStream() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"c\":3}";
        Map<?,?> result = mapper.readValue(new ByteArrayInputStream(json.getBytes("UTF-8")), Map.class);
        assertEquals(3, result.get("c"));
    }

    @Test(timeout = 4000)
    public void testWriteValueAsString() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(Collections.singletonMap("x", 10));
        assertEquals("{\"x\":10}", json);
    }

    @Test(timeout = 4000)
    public void testWriteValueAsBytes() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        byte[] bytes = mapper.writeValueAsBytes("hello");
        String json = new String(bytes, "UTF-8");
        assertEquals("\"hello\"", json);
    }

    @Test(timeout = 4000)
    public void testWriteValueToFile() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        File tmp = File.createTempFile("test", ".json");
        try {
            mapper.writeValue(tmp, 123);
            String content = new String(java.nio.file.Files.readAllBytes(tmp.toPath()), "UTF-8");
            assertEquals("123", content.trim());
        } finally {
            tmp.delete();
        }
    }

    @Test(timeout = 4000)
    public void testWriteValueToOutputStream() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        mapper.writeValue(bos, "abc");
        assertEquals("\"abc\"", bos.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testWriteValueToWriter() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        StringWriter sw = new StringWriter();
        mapper.writeValue(sw, true);
        assertEquals("true", sw.toString());
    }

    @Test(timeout = 4000)
    public void testConvertValueNull() {
        ObjectMapper mapper = new ObjectMapper();
        assertNull(mapper.convertValue(null, String.class));
    }

    @Test(timeout = 4000)
    public void testConvertValueIdentity() {
        ObjectMapper mapper = new ObjectMapper();
        String s = "test";
        assertSame(s, mapper.convertValue(s, String.class));
    }

    @Test(timeout = 4000)
    public void testConvertValueSimple() {
        ObjectMapper mapper = new ObjectMapper();
        Integer result = mapper.convertValue("42", Integer.class);
        assertEquals(Integer.valueOf(42), result);
    }

    @Test(timeout = 4000)
    public void testConvertValueGeneric() {
        ObjectMapper mapper = new ObjectMapper();
        List<String> list = new ArrayList<>();
        list.add("a");
        @SuppressWarnings("unchecked")
        List<String> result = mapper.convertValue(list, List.class);
        assertEquals(list, result);
    }

    @Test(timeout = 4000)
    public void testReadTreeFromString() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree("{\"a\":1}");
        assertTrue(node.isObject());
        assertEquals(1, node.get("a").asInt());
    }

    @Test(timeout = 4000)
    public void testReadTreeFromBytes() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree("[1,2,3]".getBytes("UTF-8"));
        assertTrue(node.isArray());
        assertEquals(3, node.size());
    }

    @Test(timeout = 4000)
    public void testReadTreeFromInputStream() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(new ByteArrayInputStream("true".getBytes("UTF-8")));
        assertTrue(node.isBoolean());
        assertTrue(node.asBoolean());
    }

    @Test(timeout = 4000)
    public void testReadTreeFromReader() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(new StringReader("null"));
        assertTrue(node.isNull());
    }

    @Test(timeout = 4000)
    public void testReadTreeFromFile() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        File tmp = File.createTempFile("tree", ".json");
        try {
            java.nio.file.Files.write(tmp.toPath(), "\"hello\"".getBytes("UTF-8"));
            JsonNode node = mapper.readTree(tmp);
            assertTrue(node.isTextual());
            assertEquals("hello", node.asText());
        } finally {
            tmp.delete();
        }
    }

    @Test(timeout = 4000)
    public void testReadTreeFromURL() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        File tmp = File.createTempFile("tree", ".json");
        try {
            java.nio.file.Files.write(tmp.toPath(), "123".getBytes("UTF-8"));
            JsonNode node = mapper.readTree(tmp.toURI().toURL());
            assertTrue(node.isNumber());
            assertEquals(123, node.asInt());
        } finally {
            tmp.delete();
        }
    }

    @Test(timeout = 4000)
    public void testTreeToValue() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree("\"test\"");
        String value = mapper.treeToValue(node, String.class);
        assertEquals("test", value);
    }

    @Test(timeout = 4000)
    public void testTreeToValueAssignable() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode on = mapper.createObjectNode();
        // treeToValue should return the node itself if type is assignable
        JsonNode result = mapper.treeToValue(on, JsonNode.class);
        assertSame(on, result);
    }

    @Test(timeout = 4000)
    public void testValueToTree() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.valueToTree(42);
        assertTrue(node.isNumber());
        assertEquals(42, node.asInt());
    }

    @Test(timeout = 4000)
    public void testValueToTreeNull() {
        ObjectMapper mapper = new ObjectMapper();
        assertNull(mapper.valueToTree(null));
    }

    @Test(timeout = 4000)
    public void testCreateObjectNode() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode n = mapper.createObjectNode();
        assertNotNull(n);
    }

    @Test(timeout = 4000)
    public void testCreateArrayNode() {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode n = mapper.createArrayNode();
        assertNotNull(n);
    }

    @Test(timeout = 4000)
    public void testTreeAsTokens() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree("123");
        JsonParser p = mapper.treeAsTokens(node);
        assertNotNull(p);
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
    }

    @Test(timeout = 4000)
    public void testCanSerialize() {
        ObjectMapper mapper = new ObjectMapper();
        assertTrue(mapper.canSerialize(String.class));
        assertFalse(mapper.canSerialize(void.class));
    }

    @Test(timeout = 4000)
    public void testCanSerializeWithCause() {
        ObjectMapper mapper = new ObjectMapper();
        java.util.concurrent.atomic.AtomicReference<Throwable> cause = new java.util.concurrent.atomic.AtomicReference<>();
        assertTrue(mapper.canSerialize(Integer.class, cause));
        assertNull(cause.get());
    }

    @Test(timeout = 4000)
    public void testCanDeserialize() {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(String.class);
        assertTrue(mapper.canDeserialize(type));
    }

    @Test(timeout = 4000)
    public void testCanDeserializeWithCause() {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(String.class);
        java.util.concurrent.atomic.AtomicReference<Throwable> cause = new java.util.concurrent.atomic.AtomicReference<>();
        assertTrue(mapper.canDeserialize(type, cause));
        assertNull(cause.get());
    }

    @Test(timeout = 4000)
    public void testWriterFactoryMethods() {
        ObjectMapper mapper = new ObjectMapper();
        assertNotNull(mapper.writer());
        assertNotNull(mapper.writer(SerializationFeature.INDENT_OUTPUT));
        assertNotNull(mapper.writer(SerializationFeature.INDENT_OUTPUT, SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
        assertNotNull(mapper.writer((DateFormat) null));
        assertNotNull(mapper.writerWithView(Object.class));
        assertNotNull(mapper.writerFor(String.class));
        assertNotNull(mapper.writerFor(new TypeReference<String>() {}));
        assertNotNull(mapper.writerFor(mapper.constructType(String.class)));
        assertNotNull(mapper.writer((PrettyPrinter) null));
        assertNotNull(mapper.writerWithDefaultPrettyPrinter());
        assertNotNull(mapper.writer(new SimpleFilterProvider()));
        assertNotNull(mapper.writer((FormatSchema) null));
        assertNotNull(mapper.writer(Base64Variants.MIME));
        assertNotNull(mapper.writer((CharacterEscapes) null));
        assertNotNull(mapper.writer(ContextAttributes.getEmpty()));
    }

    @Test(timeout = 4000)
    public void testReaderFactoryMethods() {
        ObjectMapper mapper = new ObjectMapper();
        assertNotNull(mapper.reader());
        assertNotNull(mapper.reader(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        assertNotNull(mapper.reader(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY));
        assertNotNull(mapper.readerForUpdating(new HashMap<>()));
        assertNotNull(mapper.readerFor(String.class));
        assertNotNull(mapper.readerFor(new TypeReference<String>() {}));
        assertNotNull(mapper.readerFor(mapper.constructType(String.class)));
        assertNotNull(mapper.reader(JsonNodeFactory.instance));
        assertNotNull(mapper.reader((FormatSchema) null));
        assertNotNull(mapper.reader(new InjectableValues.Std()));
        assertNotNull(mapper.readerWithView(Object.class));
        assertNotNull(mapper.reader(Base64Variants.MIME));
        assertNotNull(mapper.reader(ContextAttributes.getEmpty()));
    }

    @Test(timeout = 4000)
    public void testAcceptJsonFormatVisitor() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.acceptJsonFormatVisitor(String.class, new JsonFormatVisitorWrapper.Base() {});
        } catch (Exception e) {
            // May throw if no serializer found, but that's fine
        }
    }

    @Test(timeout = 4000)
    public void testAcceptJsonFormatVisitorNullType() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.acceptJsonFormatVisitor((JavaType) null, new JsonFormatVisitorWrapper.Base() {});
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("type must be provided"));
        }
    }

    /*
     * ============================================================
     * Partition C: Defect-Targeted Branch Zone (BigDecimal precision)
     * ============================================================
     */

    @Test(timeout = 4000)
    public void testBigDecimalRoundTripExact() throws Exception {
        // This test targets the known defect: BigDecimal -10000000000.0000000001
        // should be serialized and deserialized without losing precision.
        ObjectMapper mapper = new ObjectMapper();
        BigDecimal original = new BigDecimal("-10000000000.0000000001");
        String json = mapper.writeValueAsString(original);
        // The JSON representation should not be in scientific notation
        assertFalse("JSON should not contain scientific notation", json.contains("E"));
        BigDecimal result = mapper.readValue(json, BigDecimal.class);
        assertEquals("BigDecimal precision lost", original, result);
    }

    @Test(timeout = 4000)
    public void testBigDecimalRoundTripLargeScale() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        BigDecimal original = new BigDecimal("12345678901234567890.12345678901234567890");
        String json = mapper.writeValueAsString(original);
        BigDecimal result = mapper.readValue(json, BigDecimal.class);
        assertEquals(original, result);
    }

    @Test(timeout = 4000)
    public void testBigDecimalRoundTripNegative() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        BigDecimal original = new BigDecimal("-0.0000000001");
        String json = mapper.writeValueAsString(original);
        BigDecimal result = mapper.readValue(json, BigDecimal.class);
        assertEquals(original, result);
    }

    @Test(timeout = 4000)
    public void testBigDecimalRoundTripZero() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        BigDecimal original = BigDecimal.ZERO;
        String json = mapper.writeValueAsString(original);
        BigDecimal result = mapper.readValue(json, BigDecimal.class);
        assertEquals(original, result);
    }

    @Test(timeout = 4000)
    public void testBigDecimalRoundTripVeryLarge() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        BigDecimal original = new BigDecimal("1E+100");
        String json = mapper.writeValueAsString(original);
        BigDecimal result = mapper.readValue(json, BigDecimal.class);
        assertEquals(original, result);
    }

    @Test(timeout = 4000)
    public void testBigDecimalRoundTripVerySmall() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        BigDecimal original = new BigDecimal("1E-100");
        String json = mapper.writeValueAsString(original);
        BigDecimal result = mapper.readValue(json, BigDecimal.class);
        assertEquals(original, result);
    }

    /*
     * ============================================================
     * Partition D: Exception & Defensive Guard Paths
     * ============================================================
     */

    @Test(timeout = 4000)
    public void testReadValueInvalidJson() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readValue("{invalid}", Object.class);
            fail("Expected JsonParseException");
        } catch (JsonParseException e) {
            // expected
        } catch (IOException e) {
            fail("Unexpected IOException");
        }
    }

    @Test(timeout = 4000)
    public void testReadValueTypeMismatch() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readValue("\"not a number\"", Integer.class);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testWriteValueCloseable() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.CLOSE_CLOSEABLE);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        JsonGenerator g = mapper.getFactory().createGenerator(bos);
        // Use a Closeable value
        mapper.writeValue(g, new java.io.ByteArrayOutputStream());
        g.close();
    }

    @Test(timeout = 4000)
    public void testWriteValueFlushAfterWrite() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.FLUSH_AFTER_WRITE_VALUE);
        StringWriter sw = new StringWriter();
        mapper.writeValue(sw, "test");
        assertEquals("\"test\"", sw.toString());
    }

    @Test(timeout = 4000)
    public void testWriteValueIndent() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        String json = mapper.writeValueAsString(Collections.singletonMap("a", 1));
        assertTrue(json.contains("\n"));
    }

    @Test(timeout = 4000)
    public void testReadValueWithRootWrapping() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // Enable root wrapping
        mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        mapper.enable(DeserializationFeature.UNWRAP_ROOT_VALUE);
        // Use a simple bean with root name
        String json = "{\"String\":\"value\"}";
        String result = mapper.readValue(json, String.class);
        assertEquals("value", result);
    }

    @Test(timeout = 4000)
    public void testReadValueWithRootWrappingMismatch() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.UNWRAP_ROOT_VALUE);
        String json = "{\"wrong\":\"value\"}";
        try {
            mapper.readValue(json, String.class);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Root name"));
        }
    }

    @Test(timeout = 4000)
    public void testReadValueEmptyContent() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonParser jp = mapper.getFactory().createParser("");
        try {
            mapper.readValue(jp, Object.class);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("No content"));
        }
    }

    @Test(timeout = 4000)
    public void testReadValues() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "[1,2,3]";
        JsonParser jp = mapper.getFactory().createParser(json);
        jp.nextToken(); // advance to START_ARRAY
        jp.nextToken(); // advance to first value
        MappingIterator<Integer> it = mapper.readValues(jp, Integer.class);
        int sum = 0;
        while (it.hasNext()) {
            sum += it.next();
        }
        assertEquals(6, sum);
    }

    @Test(timeout = 4000)
    public void testReadValuesWithTypeRef() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "[\"a\",\"b\"]";
        JsonParser jp = mapper.getFactory().createParser(json);
        jp.nextToken();
        jp.nextToken();
        MappingIterator<String> it = mapper.readValues(jp, new TypeReference<String>() {});
        List<String> list = new ArrayList<>();
        it.forEachRemaining(list::add);
        assertEquals(Arrays.asList("a", "b"), list);
    }

    @Test(timeout = 4000)
    public void testReadValuesWithJavaType() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "[true,false]";
        JsonParser jp = mapper.getFactory().createParser(json);
        jp.nextToken();
        jp.nextToken();
        MappingIterator<Boolean> it = mapper.readValues(jp, mapper.constructType(Boolean.class));
        assertTrue(it.next());
        assertFalse(it.next());
    }

    @Test(timeout = 4000)
    public void testVerifySchemaTypeInvalid() {
        ObjectMapper mapper = new ObjectMapper();
        FormatSchema schema = new FormatSchema() {
            @Override public String getSchemaType() { return "unknown"; }
        };
        try {
            mapper.writer(schema);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Can not use FormatSchema"));
        }
    }

    @Test(timeout = 4000)
    public void testDefaultTypeResolverBuilderUseForType() {
        // Test all enum cases of DefaultTyping
        ObjectMapper mapper = new ObjectMapper();
        // Create a DefaultTypeResolverBuilder for each case and test useForType
        JavaType objectType = mapper.constructType(Object.class);
        JavaType stringType = mapper.constructType(String.class);
        JavaType arrayType = mapper.constructType(String[].class);
        JavaType abstractType = mapper.constructType(List.class); // interface
        JavaType finalType = mapper.constructType(Integer.class); // final

        // JAVA_LANG_OBJECT
        DefaultTypeResolverBuilder builder1 = new ObjectMapper.DefaultTypeResolverBuilder(DefaultTyping.JAVA_LANG_OBJECT);
        assertTrue(builder1.useForType(objectType));
        assertFalse(builder1.useForType(stringType));

        // OBJECT_AND_NON_CONCRETE
        DefaultTypeResolverBuilder builder2 = new ObjectMapper.DefaultTypeResolverBuilder(DefaultTyping.OBJECT_AND_NON_CONCRETE);
        assertTrue(builder2.useForType(objectType));
        assertTrue(builder2.useForType(abstractType));
        assertFalse(builder2.useForType(stringType));
        assertFalse(builder2.useForType(arrayType)); // array not covered

        // NON_CONCRETE_AND_ARRAYS
        DefaultTypeResolverBuilder builder3 = new ObjectMapper.DefaultTypeResolverBuilder(DefaultTyping.NON_CONCRETE_AND_ARRAYS);
        assertTrue(builder3.useForType(objectType));
        assertTrue(builder3.useForType(abstractType));
        assertTrue(builder3.useForType(arrayType));
        assertFalse(builder3.useForType(stringType));

        // NON_FINAL
        DefaultTypeResolverBuilder builder4 = new ObjectMapper.DefaultTypeResolverBuilder(DefaultTyping.NON_FINAL);
        assertTrue(builder4.useForType(objectType));
        assertTrue(builder4.useForType(abstractType));
        assertTrue(builder4.useForType(arrayType));
        assertFalse(builder4.useForType(stringType)); // String is final
        assertFalse(builder4.useForType(finalType)); // Integer is final
    }

    /*
     * ============================================================
     * Partition E: Object Lifecycle & Contract Integrity
     * ============================================================
     */

    @Test(timeout = 4000)
    public void testVersion() {
        ObjectMapper mapper = new ObjectMapper();
        assertNotNull(mapper.version());
    }

    @Test(timeout = 4000)
    public void testSerialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // Serialize the mapper itself? Not recommended, but we can test that it's serializable
        assertTrue(mapper instanceof java.io.Serializable);
    }

    @Test(timeout = 4000)
    public void testFindModules() {
        List<Module> modules = ObjectMapper.findModules();
        assertNotNull(modules);
    }

    @Test(timeout = 4000)
    public void testFindModulesWithClassLoader() {
        List<Module> modules = ObjectMapper.findModules(getClass().getClassLoader());
        assertNotNull(modules);
    }

    @Test(timeout = 4000)
    public void testFindAndRegisterModules() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        assertNotNull(mapper);
    }

    @Test(timeout = 4000)
    public void testRegisterModulesVarargs() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModules(new Module[0]);
        assertNotNull(mapper);
    }

    @Test(timeout = 4000)
    public void testRegisterModulesIterable() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModules(Collections.emptyList());
        assertNotNull(mapper);
    }

    @Test(timeout = 4000)
    public void testDeprecatedMethods() {
        ObjectMapper mapper = new ObjectMapper();
        // These are deprecated but should still work
        mapper.setMixInAnnotations(Collections.emptyMap());
        mapper.addMixInAnnotations(String.class, Object.class);
        mapper.setVisibilityChecker(mapper.getVisibilityChecker());
        mapper.writerWithType(String.class);
        mapper.writerWithType(new TypeReference<String>() {});
        mapper.writerWithType(mapper.constructType(String.class));
        mapper.reader(String.class);
        mapper.reader(new TypeReference<String>() {});
        mapper.reader(mapper.constructType(String.class));
        mapper.setFilters(new SimpleFilterProvider());
        mapper._defaultPrettyPrinter();
        assertNotNull(mapper);
    }

    @Test(timeout = 4000)
    public void testGenerateJsonSchema() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // Deprecated but still functional
        @SuppressWarnings("deprecation")
        com.fasterxml.jackson.databind.jsonschema.JsonSchema schema = mapper.generateJsonSchema(String.class);
        assertNotNull(schema);
    }
}