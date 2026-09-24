package com.fasterxml.jackson.databind;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.FormatSchema;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.ResolvedType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.cfg.ContextAttributes;
import com.fasterxml.jackson.databind.deser.DataFormatReaders;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.type.TypeFactory;

/*
 * [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * TARGET CLASS: com.fasterxml.jackson.databind.ObjectReader
 *
 * PARTITION A: Core Functional Logic & State Transitions
 * - Basic readValue / readValues overloads across String, InputStream, byte[], Reader, File, URL, JsonNode
 * - TreeCodec operations: readTree, createArrayNode, createObjectNode, treeAsTokens, treeToValue
 * - Overridden ObjectCodec unsupported methods: writeTree, writeValue
 * - Fluent configuration chains: DeserializationFeature, JsonParser.Feature, FormatSchema, InjectableValues,
 *   JsonNodeFactory, JsonFactory codec re-linking, RootName, Views, Locale, TimeZone, ProblemHandler, Base64Variant
 * - ContextAttributes handling: with(ContextAttributes), withAttributes(Map), withAttribute, withoutAttribute
 * - Accessors: isEnabled, getConfig, getFactory, getJsonFactory, getTypeFactory, getAttributes, version
 *
 * PARTITION B: Boundary Value Analysis (BVA) & Extremes
 * - Empty content parsing -> JsonMappingException ("No content to map due to end-of-input")
 * - Value null binding: with null valueToUpdate vs non-null valueToUpdate
 * - Array / Object end boundaries: JsonToken.END_ARRAY / END_OBJECT returns existing valueToUpdate
 * - Identity preservation checks: same config, schema, factory, injectable values, valueToUpdate returns this
 * - Multi-read mapping iterator over wrapped array vs unwrapped sequence of objects
 *
 * PARTITION C: Defect-Targeted Branch Zone (Defects4J Ground Truth: TestUpdateValue::testIssue744)
 * - Defect: In withValueToUpdate(Object), if an ObjectReader was initialized with an existing type
 *   (or via readerForUpdating), updating to a new target bean of a different type failed to reset/update
 *   _valueType and _rootDeserializer. This resulted in UnrecognizedPropertyException because the deserializer
 *   for the old type was mistakenly applied to the new object.
 * - Targeted Tests: testIssue744WithValueToUpdateClearsOrUpdatesType, testIssue744WithValueToUpdateWithType
 *
 * PARTITION D: Exception & Defensive Guard Paths
 * - Guard against updating array values: constructor and withValueToUpdate reject array types
 * - Guard against updating null values: withValueToUpdate(null) throws IllegalArgumentException
 * - Unsupported FormatSchema detection: _verifySchemaType throws IllegalArgumentException
 * - Undetectable sources in DataFormatReaders: char sources (Reader, String, JsonNode) throw JsonParseException
 * - Data format detection failure on byte array / stream throws JsonParseException
 * - Root name unwrapping mismatches: START_OBJECT, FIELD_NAME, actual vs expected name, and trailing END_OBJECT
 * - No value type configured: _findRootDeserializer throws JsonMappingException when _valueType is null
 *
 * PARTITION E: Object Lifecycle & Subclass Extension Hooks
 * - Subclassing hooks: _new, _newIterator, _initForReading, _initForMultiRead
 * ----------------------------------------------------------------------------------------------------
 */
public class ObjectReaderGeminiTest {

    // Test fixture POJOs
    public static class TargetBean {
        public int x;
        public String y;

        public TargetBean() { }
        public TargetBean(int x, String y) {
            this.x = x;
            this.y = y;
        }
    }

    public static class DataA {
        public int i;
    }

    public static class DataB {
        public DataA da;
        public int k;
    }

    public static class CustomSubclassReader extends ObjectReader {
        private static final long serialVersionUID = 1L;

        public CustomSubclassReader(ObjectMapper mapper, DeserializationConfig config) {
            super(mapper, config);
        }

        public CustomSubclassReader(ObjectReader base, JsonFactory f) {
            super(base, f);
        }

        public CustomSubclassReader(ObjectReader base, DeserializationConfig config) {
            super(base, config);
        }

        public CustomSubclassReader(ObjectReader base, DeserializationConfig config,
                JavaType valueType, JsonDeserializer<Object> rootDeser, Object valueToUpdate,
                FormatSchema schema, InjectableValues injectableValues,
                DataFormatReaders dataFormatReaders) {
            super(base, config, valueType, rootDeser, valueToUpdate, schema, injectableValues, dataFormatReaders);
        }

        @Override
        protected ObjectReader _new(ObjectReader base, JsonFactory f) {
            return new CustomSubclassReader(base, f);
        }

        @Override
        protected ObjectReader _new(ObjectReader base, DeserializationConfig config) {
            return new CustomSubclassReader(base, config);
        }

        @Override
        protected ObjectReader _new(ObjectReader base, DeserializationConfig config,
                JavaType valueType, JsonDeserializer<Object> rootDeser, Object valueToUpdate,
                FormatSchema schema, InjectableValues injectableValues,
                DataFormatReaders dataFormatReaders) {
            return new CustomSubclassReader(base, config, valueType, rootDeser, valueToUpdate, schema, injectableValues, dataFormatReaders);
        }

        @Override
        public <T> MappingIterator<T> _newIterator(JavaType valueType,
                JsonParser parser, DeserializationContext ctxt,
                JsonDeserializer<?> deser, boolean parserManaged, Object valueToUpdate) {
            return super._newIterator(valueType, parser, ctxt, deser, parserManaged, valueToUpdate);
        }
    }

    /*
     * ====================================================================================================
     * PARTITION A: Core Functional Logic & State Transitions
     * ====================================================================================================
     */

    @Test(timeout = 4000)
    public void testBasicReadValueFromString() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.reader(TargetBean.class);
        TargetBean bean = reader.readValue("{\"x\":10,\"y\":\"gemini\"}");
        assertNotNull(bean);
        assertEquals(10, bean.x);
        assertEquals("gemini", bean.y);
    }

    @Test(timeout = 4000)
    public void testReadValueParserAndOverloads() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.reader();
        String json = "{\"x\":20,\"y\":\"test\"}";

        try (JsonParser p1 = mapper.getFactory().createParser(json)) {
            TargetBean b1 = reader.readValue(p1, TargetBean.class);
            assertEquals(20, b1.x);
        }

        try (JsonParser p2 = mapper.getFactory().createParser(json)) {
            TargetBean b2 = reader.readValue(p2, new TypeReference<TargetBean>() {});
            assertEquals("test", b2.y);
        }

        try (JsonParser p3 = mapper.getFactory().createParser(json)) {
            JavaType type = mapper.constructType(TargetBean.class);
            TargetBean b3 = reader.readValue(p3, (ResolvedType) type);
            assertEquals(20, b3.x);
        }

        try (JsonParser p4 = mapper.getFactory().createParser(json)) {
            JavaType type = mapper.constructType(TargetBean.class);
            TargetBean b4 = reader.readValue(p4, type);
            assertEquals("test", b4.y);
        }
    }

    @Test(timeout = 4000)
    public void testReadValuesParserAndOverloads() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.reader();
        String json = "{\"x\":1} {\"x\":2}";

        try (JsonParser p1 = mapper.getFactory().createParser(json)) {
            java.util.Iterator<TargetBean> it = reader.readValues(p1, TargetBean.class);
            assertTrue(it.hasNext());
            assertEquals(1, it.next().x);
            assertTrue(it.hasNext());
            assertEquals(2, it.next().x);
        }

        try (JsonParser p2 = mapper.getFactory().createParser(json)) {
            java.util.Iterator<TargetBean> it = reader.readValues(p2, new TypeReference<TargetBean>() {});
            assertTrue(it.hasNext());
            assertEquals(1, it.next().x);
        }

        try (JsonParser p3 = mapper.getFactory().createParser(json)) {
            JavaType type = mapper.constructType(TargetBean.class);
            java.util.Iterator<TargetBean> it = reader.readValues(p3, (ResolvedType) type);
            assertTrue(it.hasNext());
            assertEquals(1, it.next().x);
        }

        try (JsonParser p4 = mapper.getFactory().createParser(json)) {
            JavaType type = mapper.constructType(TargetBean.class);
            java.util.Iterator<TargetBean> it = reader.readValues(p4, type);
            assertTrue(it.hasNext());
            assertEquals(1, it.next().x);
        }
    }

    @Test(timeout = 4000)
    public void testReadValueFromVariousSources() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.reader(TargetBean.class);
        String json = "{\"x\":42,\"y\":\"source\"}";

        // InputStream
        try (ByteArrayInputStream bais = new ByteArrayInputStream(json.getBytes("UTF-8"))) {
            TargetBean b = reader.readValue(bais);
            assertEquals(42, b.x);
        }

        // Reader
        try (StringReader sr = new StringReader(json)) {
            TargetBean b = reader.readValue(sr);
            assertEquals("source", b.y);
        }

        // byte[] full and slice
        byte[] bytes = json.getBytes("UTF-8");
        TargetBean bArr = reader.readValue(bytes);
        assertEquals(42, bArr.x);

        TargetBean bArrSlice = reader.readValue(bytes, 0, bytes.length);
        assertEquals("source", bArrSlice.y);

        // File and URL
        File tempFile = File.createTempFile("jackson-reader-test", ".json");
        tempFile.deleteOnExit();
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(bytes);
        }

        TargetBean bFile = reader.readValue(tempFile);
        assertEquals(42, bFile.x);

        TargetBean bUrl = reader.readValue(tempFile.toURI().toURL());
        assertEquals(42, bUrl.x);

        // JsonNode
        JsonNode node = mapper.readTree(json);
        TargetBean bNode = reader.readValue(node);
        assertEquals(42, bNode.x);

        tempFile.delete();
    }

    @Test(timeout = 4000)
    public void testReadValuesFromVariousSources() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.reader(Integer.class);
        String jsonSequence = "1 2 3";
        byte[] bytes = jsonSequence.getBytes("UTF-8");

        try (MappingIterator<Integer> it = reader.readValues(jsonSequence)) {
            assertEquals(Integer.valueOf(1), it.next());
            assertEquals(Integer.valueOf(2), it.next());
            assertEquals(Integer.valueOf(3), it.next());
        }

        try (MappingIterator<Integer> it = reader.readValues(new StringReader(jsonSequence))) {
            assertEquals(Integer.valueOf(1), it.next());
        }

        try (MappingIterator<Integer> it = reader.readValues(new ByteArrayInputStream(bytes))) {
            assertEquals(Integer.valueOf(1), it.next());
        }

        try (MappingIterator<Integer> it = reader.readValues(bytes)) {
            assertEquals(Integer.valueOf(1), it.next());
        }

        try (MappingIterator<Integer> it = reader.readValues(bytes, 0, bytes.length)) {
            assertEquals(Integer.valueOf(1), it.next());
        }

        File tempFile = File.createTempFile("jackson-values-test", ".json");
        tempFile.deleteOnExit();
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(bytes);
        }

        try (MappingIterator<Integer> it = reader.readValues(tempFile)) {
            assertEquals(Integer.valueOf(1), it.next());
        }

        try (MappingIterator<Integer> it = reader.readValues(tempFile.toURI().toURL())) {
            assertEquals(Integer.valueOf(1), it.next());
        }

        tempFile.delete();
    }

    @Test(timeout = 4000)
    public void testTreeOperations() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.reader();

        JsonNode arr = reader.createArrayNode();
        assertTrue(arr.isArray());

        JsonNode obj = reader.createObjectNode();
        assertTrue(obj.isObject());

        JsonNode parsedTree = reader.readTree("{\"k\":\"v\"}");
        assertTrue(parsedTree.isObject());
        assertEquals("v", parsedTree.get("k").asText());

        try (StringReader sr = new StringReader("{\"a\":1}")) {
            JsonNode treeFromReader = reader.readTree(sr);
            assertEquals(1, treeFromReader.get("a").asInt());
        }

        try (ByteArrayInputStream bais = new ByteArrayInputStream("{\"b\":2}".getBytes("UTF-8"))) {
            JsonNode treeFromStream = reader.readTree(bais);
            assertEquals(2, treeFromStream.get("b").asInt());
        }

        try (JsonParser p = reader.treeAsTokens(parsedTree)) {
            JsonNode boundTree = reader.readTree(p);
            assertEquals("v", boundTree.get("k").asText());
        }

        TargetBean bean = reader.treeToValue(mapper.readTree("{\"x\":77,\"y\":\"tree\"}"), TargetBean.class);
        assertEquals(77, bean.x);
        assertEquals("tree", bean.y);
    }

    @Test(timeout = 4000)
    public void testUnsupportedOperations() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.reader();

        try {
            reader.writeTree(null, null);
            fail("writeTree should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {
            assertNotNull(expected);
        }

        try {
            reader.writeValue(null, null);
            fail("writeValue should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {
            assertEquals("Not implemented for ObjectReader", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testFluentFeatureConfiguration() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.reader();

        // DeserializationFeature fluent methods
        ObjectReader r1 = reader.with(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);
        assertTrue(r1.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));

        ObjectReader r2 = r1.with(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        assertTrue(r2.isEnabled(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY));
        assertTrue(r2.isEnabled(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS));

        ObjectReader r3 = r2.withFeatures(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);
        assertTrue(r3.isEnabled(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL));

        ObjectReader r4 = r3.without(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);
        assertFalse(r4.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));

        ObjectReader r5 = r4.without(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        assertFalse(r5.isEnabled(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY));

        ObjectReader r6 = r5.withoutFeatures(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);
        assertFalse(r6.isEnabled(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL));

        // JsonParser.Feature fluent methods
        ObjectReader rp1 = reader.with(JsonParser.Feature.ALLOW_COMMENTS);
        assertTrue(rp1.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));

        ObjectReader rp2 = rp1.withFeatures(JsonParser.Feature.ALLOW_SINGLE_QUOTES, JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
        assertTrue(rp2.isEnabled(JsonParser.Feature.ALLOW_SINGLE_QUOTES));
        assertTrue(rp2.isEnabled(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES));

        ObjectReader rp3 = rp2.without(JsonParser.Feature.ALLOW_COMMENTS);
        assertFalse(rp3.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));

        ObjectReader rp4 = rp3.without(JsonParser.Feature.ALLOW_SINGLE_QUOTES, JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
        assertFalse(rp4.isEnabled(JsonParser.Feature.ALLOW_SINGLE_QUOTES));

        ObjectReader rp5 = rp2.withoutFeatures(JsonParser.Feature.ALLOW_SINGLE_QUOTES, JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
        assertFalse(rp5.isEnabled(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES));
    }

    @Test(timeout = 4000)
    public void testFluentSettingsAndAccessors() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.reader();

        assertNotNull(reader.version());
        assertNotNull(reader.getConfig());
        assertNotNull(reader.getFactory());
        @SuppressWarnings("deprecation")
        JsonFactory deprecatedFactory = reader.getJsonFactory();
        assertSame(reader.getFactory(), deprecatedFactory);
        assertNotNull(reader.getTypeFactory());
        assertTrue(reader.isEnabled(MapperFeature.AUTO_DETECT_FIELDS));

        // Views, Locale, TimeZone, ProblemHandler, Base64Variant
        ObjectReader configured = reader.withView(String.class)
                .with(Locale.FRENCH)
                .with(TimeZone.getTimeZone("UTC"))
                .with(Base64Variants.MODIFIED_FOR_URL);

        assertEquals(String.class, configured.getConfig().getActiveView());
        assertEquals(Locale.FRENCH, configured.getConfig().getLocale());
        assertEquals(TimeZone.getTimeZone("UTC"), configured.getConfig().getTimeZone());
        assertEquals(Base64Variants.MODIFIED_FOR_URL, configured.getConfig().getBase64Variant());

        DeserializationProblemHandler handler = new DeserializationProblemHandler() {};
        ObjectReader rHandler = configured.withHandler(handler);
        assertNotNull(rHandler.getConfig().getProblemHandlers());

        // ContextAttributes
        ContextAttributes attrs = ContextAttributes.getEmpty().withSharedAttribute("attr1", "val1");
        ObjectReader rAttrs = reader.with(attrs);
        assertEquals("val1", rAttrs.getAttributes().getAttribute("attr1"));

        Map<Object, Object> map = new HashMap<>();
        map.put("attr2", "val2");
        ObjectReader rAttrs2 = rAttrs.withAttributes(map);
        assertEquals("val2", rAttrs2.getAttributes().getAttribute("attr2"));

        ObjectReader rAttrs3 = rAttrs2.withAttribute("attr3", "val3");
        assertEquals("val3", rAttrs3.getAttributes().getAttribute("attr3"));

        ObjectReader rAttrs4 = rAttrs3.withoutAttribute("attr3");
        assertNull(rAttrs4.getAttributes().getAttribute("attr3"));

        // JsonFactory codec re-linking
        JsonFactory customFactory = new JsonFactory();
        ObjectReader rWithFactory = reader.with(customFactory);
        assertSame(customFactory, rWithFactory.getFactory());
        assertSame(rWithFactory, customFactory.getCodec());

        // JsonNodeFactory
        JsonNodeFactory jnf = new JsonNodeFactory(true);
        ObjectReader rJnf = reader.with(jnf);
        assertSame(jnf, rJnf.getConfig().getNodeFactory());

        // Root name
        ObjectReader rRoot = reader.withRootName("myRoot");
        assertEquals("myRoot", rRoot.getConfig().getRootName());
    }

    @Test(timeout = 4000)
    public void testDeprecatedAndTypeResolutionMethods() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.reader();
        JavaType javaType = mapper.constructType(TargetBean.class);

        assertNotNull(reader.forType(TargetBean.class));
        assertNotNull(reader.forType(javaType));
        assertNotNull(reader.forType(new TypeReference<TargetBean>() {}));

        @SuppressWarnings("deprecation")
        ObjectReader r1 = reader.withType(TargetBean.class);
        assertNotNull(r1);

        @SuppressWarnings("deprecation")
        ObjectReader r2 = reader.withType(javaType);
        assertNotNull(r2);

        @SuppressWarnings("deprecation")
        ObjectReader r3 = reader.withType((java.lang.reflect.Type) TargetBean.class);
        assertNotNull(r3);

        @SuppressWarnings("deprecation")
        ObjectReader r4 = reader.withType(new TypeReference<TargetBean>() {});
        assertNotNull(r4);
    }

    /*
     * ====================================================================================================
     * PARTITION B: Boundary Value Analysis (BVA) & Extremes
     * ====================================================================================================
     */

    @Test(timeout = 4000)
    public void testEmptyContentThrowsJsonMappingException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.reader(TargetBean.class);
        try {
            reader.readValue("");
            fail("Expected JsonMappingException for empty content");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("No content to map due to end-of-input"));
        }
    }

    @Test(timeout = 4000)
    public void testReadNullTokenHandling() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.reader(TargetBean.class);

        // Binding null when valueToUpdate is null
        TargetBean resultNull = reader.readValue("null");
        assertNull(resultNull);

        // Binding null when valueToUpdate is non-null returns original valueToUpdate
        TargetBean target = new TargetBean(15, "preserved");
        ObjectReader updatingReader = reader.withValueToUpdate(target);
        TargetBean updated = updatingReader.readValue("null");
        assertSame(target, updated);
        assertEquals(15, updated.x);
        assertEquals("preserved", updated.y);

        // Tree binding for null
        JsonNode nullNode = mapper.reader().readTree("null");
        assertTrue(nullNode.isNull());
    }

    @Test(timeout = 4000)
    public void testReadEndObjectOrEndArrayToken() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TargetBean target = new TargetBean(88, "end");
        ObjectReader reader = mapper.readerForUpdating(target);

        // Point parser directly to END_OBJECT
        try (JsonParser p = mapper.getFactory().createParser("{}")) {
            assertEquals(JsonToken.START_OBJECT, p.nextToken());
            assertEquals(JsonToken.END_OBJECT, p.nextToken());
            TargetBean res = reader.readValue(p);
            assertSame(target, res);
            assertEquals(88, res.x);
        }

        // Tree binding for empty object returns NullNode
        try (JsonParser p2 = mapper.getFactory().createParser("{}")) {
            p2.nextToken();
            p2.nextToken(); // END_OBJECT
            JsonNode tree = mapper.reader().readTree(p2);
            assertSame(NullNode.instance, tree);
        }
    }

    @Test(timeout = 4000)
    public void testIdentityShortCircuits() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.reader(TargetBean.class);

        // Configuration with same config
        assertSame(reader, reader.with(reader.getConfig()));

        // JsonFactory with same factory
        assertSame(reader, reader.with(reader.getFactory()));

        // forType with same type
        assertSame(reader, reader.forType(mapper.constructType(TargetBean.class)));

        // with(InjectableValues) with same instance
        InjectableValues iv = new InjectableValues.Std();
        ObjectReader rIv = reader.with(iv);
        assertSame(rIv, rIv.with(iv));

        // withValueToUpdate with same instance
        TargetBean target = new TargetBean(1, "x");
        ObjectReader rUp = reader.withValueToUpdate(target);
        assertSame(rUp, rUp.withValueToUpdate(target));
    }

    @Test(timeout = 4000)
    public void testRootUnwrappingSuccess() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.reader(TargetBean.class)
                .with(DeserializationFeature.UNWRAP_ROOT_VALUE)
                .withRootName("customRoot");

        TargetBean bean = reader.readValue("{\"customRoot\":{\"x\":5,\"y\":\"unwrapped\"}}");
        assertNotNull(bean);
        assertEquals(5, bean.x);
        assertEquals("unwrapped", bean.y);

        // Read as tree with root unwrapping
        ObjectReader treeReader = mapper.reader()
                .with(DeserializationFeature.UNWRAP_ROOT_VALUE)
                .withRootName("treeRoot");
        JsonNode tree = treeReader.readTree("{\"treeRoot\":{\"k\":\"v\"}}");
        assertEquals("v", tree.get("k").asText());
    }

    /*
     * ====================================================================================================
     * PARTITION C: Defect-Targeted Branch Zone (Defects4J: TestUpdateValue::testIssue744)
     * ====================================================================================================
     */

    /**
     * Targets Defects4J bug where withValueToUpdate(Object) did not update the root deserializer
     * when an ObjectReader previously configured with another type was updated with a new target value.
     */
    @Test(timeout = 4000)
    public void testIssue744WithValueToUpdateClearsOrUpdatesType() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DataB db = new DataB();
        DataA da = new DataA();

        // 1. Initially construct reader for DataB
        ObjectReader readerB = mapper.readerForUpdating(db);

        // 2. Transition reader to update DataA instead
        ObjectReader readerA = readerB.withValueToUpdate(da);

        // In the defective version, readerA retains DataB's valueType and rootDeserializer,
        // throwing UnrecognizedPropertyException: Unrecognized field "i" (class DataB).
        // Correct behavior: readerA uses DataA deserializer and updates da.i to 42.
        DataA result = readerA.readValue("{\"i\": 42}");
        assertSame(da, result);
        assertEquals(42, da.i);
    }

    @Test(timeout = 4000)
    public void testIssue744WithValueToUpdateWithType() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DataA da = new DataA();

        // Reader explicitly typed to DataB, then switched to update DataA
        ObjectReader reader = mapper.reader(DataB.class).withValueToUpdate(da);
        DataA result = reader.readValue("{\"i\": 99}");
        assertSame(da, result);
        assertEquals(99, da.i);
    }

    /*
     * ====================================================================================================
     * PARTITION D: Exception & Defensive Guard Paths
     * ====================================================================================================
     */

    @Test(timeout = 4000)
    public void testCannotUpdateNullValue() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.reader().withValueToUpdate(null);
            fail("Expected IllegalArgumentException for updating null value");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("cat not update null value"));
        }
    }

    @Test(timeout = 4000)
    public void testCannotUpdateArrayValue() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.reader(int[].class).withValueToUpdate(new int[]{1, 2});
            fail("Expected IllegalArgumentException when updating array");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Can not update an array value"));
        }
    }

    @Test(timeout = 4000)
    public void testUnsupportedFormatSchemaThrowsException() {
        ObjectMapper mapper = new ObjectMapper();
        FormatSchema unsupportedSchema = new FormatSchema() {
            @Override
            public String getSchemaType() {
                return "UNSUPPORTED_TEST_SCHEMA";
            }
        };

        try {
            mapper.reader().with(unsupportedSchema);
            fail("Expected IllegalArgumentException for unsupported FormatSchema");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Can not use FormatSchema of type"));
        }
    }

    @Test(timeout = 4000)
    public void testNoValueTypeConfiguredThrowsJsonMappingException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // Plain reader has no value type configured
        ObjectReader reader = mapper.reader();
        try (JsonParser p = mapper.getFactory().createParser("123")) {
            reader.readValue(p);
            fail("Expected JsonMappingException when no value type is configured");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("No value type configured for ObjectReader"));
        }
    }

    @Test(timeout = 4000)
    public void testRootUnwrappingFailures() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.reader(TargetBean.class)
                .with(DeserializationFeature.UNWRAP_ROOT_VALUE)
                .withRootName("expectedRoot");

        // 1. Non-START_OBJECT token
        try {
            reader.readValue("[1, 2]");
            fail("Expected failure when root token is not START_OBJECT");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Current token not START_OBJECT"));
        }

        // 2. Empty wrapper object (no FIELD_NAME)
        try {
            reader.readValue("{}");
            fail("Expected failure when wrapper has no field name");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Current token not FIELD_NAME"));
        }

        // 3. Mismatched root name
        try {
            reader.readValue("{\"wrongRoot\":{\"x\":1}}");
            fail("Expected failure when root name does not match expected");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Root name 'wrongRoot' does not match expected"));
        }

        // 4. Trailing token inside wrapper is not END_OBJECT
        try {
            reader.readValue("{\"expectedRoot\":{\"x\":1}, \"extra\":2}");
            fail("Expected failure when wrapper object has extra content");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Current token not END_OBJECT"));
        }
    }

    @Test(timeout = 4000)
    public void testDataFormatDetectionUndetectableSources() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.reader(TargetBean.class);
        ObjectReader detector = reader.withFormatDetection(reader);

        try {
            detector.readValue(new StringReader("{}"));
            fail("Expected JsonParseException for char Reader with format detection");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("must be byte- not char-based"));
        }

        try {
            detector.readValue("{}");
            fail("Expected JsonParseException for String with format detection");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("must be byte- not char-based"));
        }

        try {
            detector.readValue(NullNode.instance);
            fail("Expected JsonParseException for JsonNode with format detection");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("must be byte- not char-based"));
        }

        try {
            detector.readTree(new StringReader("{}"));
            fail("Expected JsonParseException for readTree(Reader) with format detection");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("must be byte- not char-based"));
        }

        try {
            detector.readTree("{}");
            fail("Expected JsonParseException for readTree(String) with format detection");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("must be byte- not char-based"));
        }

        try {
            detector.readValues(new StringReader("{}"));
            fail("Expected JsonParseException for readValues(Reader) with format detection");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("must be byte- not char-based"));
        }

        try {
            detector.readValues("{}");
            fail("Expected JsonParseException for readValues(String) with format detection");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("must be byte- not char-based"));
        }
    }

    @Test(timeout = 4000)
    public void testDataFormatDetectionFailureOnInvalidBytes() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.reader(TargetBean.class);
        ObjectReader detector = reader.withFormatDetection(reader);

        byte[] invalidBytes = new byte[]{ (byte) 0xFE, (byte) 0xFF, (byte) 0x00, (byte) 0x01 };
        try {
            detector.readValue(invalidBytes);
            fail("Expected JsonParseException when format detection cannot match format");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("Can not detect format from input"));
        } catch (IOException e) {
            fail("Unexpected IOException: " + e.getMessage());
        }
    }

    /*
     * ====================================================================================================
     * PARTITION E: Object Lifecycle & Subclass Extension Hooks
     * ====================================================================================================
     */

    @Test(timeout = 4000)
    public void testDataFormatDetectionSuccess() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.reader(TargetBean.class);
        ObjectReader detector = reader.withFormatDetection(reader);

        byte[] jsonBytes = "{\"x\":33,\"y\":\"detected\"}".getBytes("UTF-8");

        TargetBean fromBytes = detector.readValue(jsonBytes);
        assertEquals(33, fromBytes.x);
        assertEquals("detected", fromBytes.y);

        try (ByteArrayInputStream bais = new ByteArrayInputStream(jsonBytes)) {
            TargetBean fromStream = detector.readValue(bais);
            assertEquals(33, fromStream.x);
        }

        try (ByteArrayInputStream bais = new ByteArrayInputStream(jsonBytes)) {
            JsonNode tree = detector.readTree(bais);
            assertEquals(33, tree.get("x").asInt());
        }

        try (MappingIterator<TargetBean> it = detector.readValues(jsonBytes, 0, jsonBytes.length)) {
            assertTrue(it.hasNext());
            assertEquals(33, it.next().x);
        }

        try (MappingIterator<TargetBean> it = detector.readValues(new ByteArrayInputStream(jsonBytes))) {
            assertTrue(it.hasNext());
            assertEquals(33, it.next().x);
        }

        File tempFile = File.createTempFile("jackson-detect-test", ".json");
        tempFile.deleteOnExit();
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(jsonBytes);
        }

        TargetBean fromFile = detector.readValue(tempFile);
        assertEquals(33, fromFile.x);

        TargetBean fromUrl = detector.readValue(tempFile.toURI().toURL());
        assertEquals(33, fromUrl.x);

        try (MappingIterator<TargetBean> it = detector.readValues(tempFile)) {
            assertTrue(it.hasNext());
            assertEquals(33, it.next().x);
        }

        try (MappingIterator<TargetBean> it = detector.readValues(tempFile.toURI().toURL())) {
            assertTrue(it.hasNext());
            assertEquals(33, it.next().x);
        }

        tempFile.delete();
    }

    @Test(timeout = 4000)
    public void testSubclassExtensionHooks() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        CustomSubclassReader customReader = new CustomSubclassReader(mapper, mapper.getDeserializationConfig());

        ObjectReader rWithFactory = customReader.with(new JsonFactory());
        assertTrue(rWithFactory instanceof CustomSubclassReader);

        ObjectReader rWithConfig = customReader.with(mapper.getDeserializationConfig().without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        assertTrue(rWithConfig instanceof CustomSubclassReader);

        ObjectReader rWithType = customReader.forType(TargetBean.class);
        assertTrue(rWithType instanceof CustomSubclassReader);

        TargetBean bean = rWithType.readValue("{\"x\":12,\"y\":\"custom\"}");
        assertNotNull(bean);
        assertEquals(12, bean.x);

        try (JsonParser p = mapper.getFactory().createParser("{\"x\":1}{\"x\":2}")) {
            MappingIterator<TargetBean> it = customReader._newIterator(
                    mapper.constructType(TargetBean.class),
                    p,
                    customReader.createDeserializationContext(p, customReader.getConfig()),
                    customReader._findRootDeserializer(customReader.createDeserializationContext(p, customReader.getConfig()), mapper.constructType(TargetBean.class)),
                    false,
                    null
            );
            assertTrue(it.hasNext());
            assertEquals(1, it.next().x);
        }
    }
}