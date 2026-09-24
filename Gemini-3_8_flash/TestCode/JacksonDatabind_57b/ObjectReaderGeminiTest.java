package com.fasterxml.jackson.databind;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.*;
import java.net.URL;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.type.ResolvedType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.cfg.ContextAttributes;
import com.fasterxml.jackson.databind.deser.DataFormatReaders;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/*
 * [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------
 * Defect Targeted:
 * - ReadValuesTest::testRootBeans failure (CharConversionException: Invalid UTF-32 character 0x2261223a)
 *   Root cause: Multi-value sequence parsing on byte-based sources (InputStream / byte[]) with different
 *   encodings (UTF-8, UTF-16, UTF-32) where parser initialization or framing advances stream tokens
 *   incorrectly across root bean boundaries.
 *
 * Decision / Condition Coverage Zones:
 * 1. Partition A: Core Functional Logic & State Transitions
 *    - readValue / readValues over Parser, String, InputStream, Reader, byte[], File, URL.
 *    - Type binding via JavaType, Class, TypeReference, and ResolvedType overloads.
 *    - Caching and pre-fetching of root deserializers (_rootDeserializers map).
 * 2. Partition B: Boundary Value Analysis & Extremes
 *    - Empty JSON string, empty input streams, null values, empty object/array tokens.
 *    - Root values updating vs constructing new instances with withValueToUpdate().
 *    - ArrayType rejection on withValueToUpdate().
 * 3. Partition C: Defect-Targeted Branch Zone
 *    - testRootBeansDefectInputStream: Multi-value sequence reading from byte sources with UTF-8 / UTF-32.
 *    - testRootBeansDefectByteArray: Multi-value sequence reading from byte array directly and with offsets.
 * 4. Partition D: Exception & Defensive Guard Paths
 *    - DataFormatReaders with character-based inputs (Reader, String, JsonNode) triggering JsonParseException.
 *    - Unknown / unmatchable format streams triggering format detection exception.
 *    - FormatSchema type verification failure on incompatible schema types.
 *    - Root name unwrapping error paths (START_OBJECT missing, FIELD_NAME missing, name mismatch, END_OBJECT missing).
 *    - Unsupported writeTree / writeValue operations.
 *    - Reading without configured JavaType.
 * 5. Partition E: Object Lifecycle, Fluent Mutants & Config Integrity
 *    - Mutant factory methods (with/without features, attributes, schema, factories, views).
 *    - JsonPointer filtering with at(String) and at(JsonPointer).
 *    - TreeCodec implementation (createArrayNode, createObjectNode, treeAsTokens, readTree).
 */
public class ObjectReaderGeminiTest {

    public static class Bean {
        public int a;

        public Bean() { }
        public Bean(int a) { this.a = a; }
    }

    public static class TargetBean {
        public int a = 1;
        public String b = "default";

        public TargetBean() { }
    }

    @JsonRootName("root")
    public static class RootBean {
        public int val;

        public RootBean() { }
        public RootBean(int val) { this.val = val; }
    }

    /*
     * =========================================================================
     * Partition C: Defect-Targeted Branch Zone (Defects4J Known Regressions)
     * =========================================================================
     */

    @Test(timeout = 4000)
    public void testRootBeansDefectInputStream() throws Exception {
        final String json = "{\"a\":1}{\"a\":2 }  {\"a\":3 }";
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.readerFor(Bean.class);

        // Targeted assertion: multi-bean root sequence over byte streams with multiple encodings
        String[] encodings = new String[] { "UTF-8", "UTF-16BE", "UTF-16LE", "UTF-32BE", "UTF-32LE" };
        for (String enc : encodings) {
            byte[] bytes = json.getBytes(enc);
            try (MappingIterator<Bean> it = reader.readValues(new ByteArrayInputStream(bytes))) {
                assertTrue("Should have first element for encoding: " + enc, it.hasNext());
                assertEquals(1, it.next().a);
                assertTrue("Should have second element for encoding: " + enc, it.hasNext());
                assertEquals(2, it.next().a);
                assertTrue("Should have third element for encoding: " + enc, it.hasNext());
                assertEquals(3, it.next().a);
                assertFalse("Should reach end for encoding: " + enc, it.hasNext());
            }
        }
    }

    @Test(timeout = 4000)
    public void testRootBeansDefectByteArray() throws Exception {
        final String json = "{\"a\":1}{\"a\":2 }  {\"a\":3 }";
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.readerFor(Bean.class);

        byte[] bytesUtf8 = json.getBytes("UTF-8");

        // Sequence read from full byte array
        try (MappingIterator<Bean> it = reader.readValues(bytesUtf8)) {
            assertTrue(it.hasNext());
            assertEquals(1, it.next().a);
            assertTrue(it.hasNext());
            assertEquals(2, it.next().a);
            assertTrue(it.hasNext());
            assertEquals(3, it.next().a);
            assertFalse(it.hasNext());
        }

        // Sequence read from byte array with slice offset and length
        try (MappingIterator<Bean> itSlice = reader.readValues(bytesUtf8, 0, bytesUtf8.length)) {
            assertTrue(itSlice.hasNext());
            assertEquals(1, itSlice.next().a);
            assertTrue(itSlice.hasNext());
            assertEquals(2, itSlice.next().a);
            assertTrue(itSlice.hasNext());
            assertEquals(3, itSlice.next().a);
            assertFalse(itSlice.hasNext());
        }
    }

    /*
     * =========================================================================
     * Partition A: Core Functional Logic & State Transitions
     * =========================================================================
     */

    @Test(timeout = 4000)
    public void testReadValueBasicTypesAndOverloads() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.reader();
        JavaType beanType = mapper.constructType(Bean.class);

        // String source
        Bean b1 = reader.forType(Bean.class).readValue("{\"a\":10}");
        assertEquals(10, b1.a);

        // InputStream source
        Bean b2 = reader.forType(Bean.class).readValue(new ByteArrayInputStream("{\"a\":20}".getBytes("UTF-8")));
        assertEquals(20, b2.a);

        // Reader source
        Bean b3 = reader.forType(Bean.class).readValue(new StringReader("{\"a\":30}"));
        assertEquals(30, b3.a);

        // Parser overloads
        try (JsonParser p = mapper.getFactory().createParser("{\"a\":40}")) {
            Bean b4 = reader.readValue(p, Bean.class);
            assertEquals(40, b4.a);
        }

        try (JsonParser p = mapper.getFactory().createParser("{\"a\":50}")) {
            Bean b5 = reader.readValue(p, beanType);
            assertEquals(50, b5.a);
        }

        try (JsonParser p = mapper.getFactory().createParser("{\"a\":60}")) {
            Bean b6 = reader.readValue(p, (ResolvedType) beanType);
            assertEquals(60, b6.a);
        }

        try (JsonParser p = mapper.getFactory().createParser("{\"a\":70}")) {
            Bean b7 = reader.readValue(p, new TypeReference<Bean>() {});
            assertEquals(70, b7.a);
        }
    }

    @Test(timeout = 4000)
    public void testReadValuesWithJsonParserOverloads() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.reader();
        JavaType beanType = mapper.constructType(Bean.class);
        String jsonArray = "[{\"a\":1},{\"a\":2}]";

        try (JsonParser p = mapper.getFactory().createParser(jsonArray);
             MappingIterator<Bean> it = reader.readValues(p, Bean.class)) {
            assertTrue(it.hasNext());
            assertEquals(1, it.next().a);
            assertTrue(it.hasNext());
            assertEquals(2, it.next().a);
        }

        try (JsonParser p = mapper.getFactory().createParser(jsonArray);
             MappingIterator<Bean> it = reader.readValues(p, beanType)) {
            assertTrue(it.hasNext());
            assertEquals(1, it.next().a);
        }

        try (JsonParser p = mapper.getFactory().createParser(jsonArray);
             MappingIterator<Bean> it = reader.readValues(p, (ResolvedType) beanType)) {
            assertTrue(it.hasNext());
            assertEquals(1, it.next().a);
        }

        try (JsonParser p = mapper.getFactory().createParser(jsonArray);
             MappingIterator<Bean> it = reader.readValues(p, new TypeReference<Bean>() {})) {
            assertTrue(it.hasNext());
            assertEquals(1, it.next().a);
        }
    }

    @Test(timeout = 4000)
    public void testFileAndUrlBinding() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.readerFor(Bean.class);

        File tempFile = File.createTempFile("jackson_reader_test", ".json");
        tempFile.deleteOnExit();

        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write("{\"a\":999}".getBytes("UTF-8"));
        }

        // File readValue & readValues
        Bean fromFile = reader.readValue(tempFile);
        assertEquals(999, fromFile.a);

        try (MappingIterator<Bean> itFile = reader.readValues(tempFile)) {
            assertTrue(itFile.hasNext());
            assertEquals(999, itFile.next().a);
        }

        // URL readValue & readValues
        URL fileUrl = tempFile.toURI().toURL();
        Bean fromUrl = reader.readValue(fileUrl);
        assertEquals(999, fromUrl.a);

        try (MappingIterator<Bean> itUrl = reader.readValues(fileUrl)) {
            assertTrue(itUrl.hasNext());
            assertEquals(999, itUrl.next().a);
        }
    }

    @Test(timeout = 4000)
    public void testJsonPointerFiltering() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String nestedJson = "{\"wrapper\":{\"target\":{\"a\":123}}}";

        // at(String)
        ObjectReader filterReaderStr = mapper.readerFor(Bean.class).at("/wrapper/target");
        Bean b1 = filterReaderStr.readValue(nestedJson);
        assertNotNull(b1);
        assertEquals(123, b1.a);

        // at(JsonPointer)
        JsonPointer ptr = JsonPointer.compile("/wrapper/target");
        ObjectReader filterReaderPtr = mapper.readerFor(Bean.class).at(ptr);
        Bean b2 = filterReaderPtr.readValue(nestedJson);
        assertNotNull(b2);
        assertEquals(123, b2.a);
    }

    @Test(timeout = 4000)
    public void testTreeCodecOperations() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.reader();

        ArrayNode arr = reader.createArrayNode();
        assertNotNull(arr);
        assertTrue(arr.isArray());

        ObjectNode obj = reader.createObjectNode();
        assertNotNull(obj);
        assertTrue(obj.isObject());
        obj.put("a", 77);

        // treeToValue
        Bean bean = reader.treeToValue(obj, Bean.class);
        assertNotNull(bean);
        assertEquals(77, bean.a);

        // readTree from parser, stream, reader, string
        JsonNode n1 = reader.readTree("{\"a\":1}");
        assertEquals(1, n1.get("a").asInt());

        JsonNode n2 = reader.readTree(new StringReader("{\"a\":2}"));
        assertEquals(2, n2.get("a").asInt());

        JsonNode n3 = reader.readTree(new ByteArrayInputStream("{\"a\":3}".getBytes("UTF-8")));
        assertEquals(3, n3.get("a").asInt());

        try (JsonParser p = mapper.getFactory().createParser("{\"a\":4}")) {
            JsonNode n4 = reader.readTree(p);
            assertEquals(4, n4.get("a").asInt());
        }

        // readValue(JsonNode)
        Bean beanFromNode = reader.forType(Bean.class).readValue(obj);
        assertEquals(77, beanFromNode.a);

        // treeAsTokens
        try (JsonParser tokens = reader.treeAsTokens(obj)) {
            assertNotNull(tokens);
            assertEquals(JsonToken.START_OBJECT, tokens.nextToken());
        }
    }

    /*
     * =========================================================================
     * Partition B: Boundary Value Analysis (BVA) & Extremes
     * =========================================================================
     */

    @Test(timeout = 4000)
    public void testValueToUpdateTransitions() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TargetBean target = new TargetBean();
        target.a = 5;
        target.b = "initial";

        ObjectReader reader = mapper.readerForUpdating(target);

        // Update with partial fields
        TargetBean result = reader.readValue("{\"b\":\"modified\"}");
        assertSame(target, result);
        assertEquals(5, target.a);
        assertEquals("modified", target.b);

        // Update when input is JSON null
        TargetBean resultNull = reader.readValue("null");
        assertSame(target, resultNull);
        assertEquals("modified", target.b);

        // Self-assignment idempotency
        assertSame(reader, reader.withValueToUpdate(target));
    }

    @Test(timeout = 4000)
    public void testEmptyAndNullTokenBinding() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.readerFor(Bean.class);

        // Binding JSON null token
        Bean nullBean = reader.readValue("null");
        assertNull(nullBean);

        // Tree binding null token returns NullNode
        JsonNode nullNode = reader.readTree("null");
        assertEquals(NullNode.instance, nullNode);

        // Empty document throws JsonMappingException (end-of-input)
        try {
            reader.readValue("");
            fail("Expected JsonMappingException on empty input");
        } catch (JsonMappingException expected) {
            assertTrue(expected.getMessage().contains("No content to map due to end-of-input"));
        }
    }

    @Test(timeout = 4000)
    public void testRootUnwrappingSuccess() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.readerFor(RootBean.class)
                .with(DeserializationFeature.UNWRAP_ROOT_VALUE);

        RootBean rb = reader.readValue("{\"root\":{\"val\":42}}");
        assertNotNull(rb);
        assertEquals(42, rb.val);

        // Tree unwrapping
        JsonNode tree = mapper.reader().with(DeserializationFeature.UNWRAP_ROOT_VALUE)
                .readTree("{\"JsonNode\":{\"x\":1}}");
        assertNotNull(tree);
        assertEquals(1, tree.get("x").asInt());
    }

    /*
     * =========================================================================
     * Partition D: Exception & Defensive Guard Paths
     * =========================================================================
     */

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testUpdateNullValueThrows() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.reader().withValueToUpdate(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testUpdateArrayValueThrows() {
        ObjectMapper mapper = new ObjectMapper();
        int[] array = new int[] { 1, 2, 3 };
        mapper.readerFor(int[].class).withValueToUpdate(array);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testWriteTreeUnsupported() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.reader().writeTree(null, null);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testWriteValueUnsupported() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.reader().writeValue(null, null);
    }

    @Test(timeout = 4000)
    public void testMissingValueTypeForDeserializerThrows() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader noTypeReader = mapper.reader((JavaType) null);
        try {
            noTypeReader.readValue("123");
            fail("Expected JsonMappingException for missing value type");
        } catch (JsonMappingException expected) {
            assertTrue(expected.getMessage().contains("No value type configured"));
        }
    }

    @Test(timeout = 4000)
    public void testIncompatibleFormatSchemaThrows() {
        ObjectMapper mapper = new ObjectMapper();
        FormatSchema unsupportedSchema = new FormatSchema() {
            @Override
            public String getSchemaType() {
                return "unsupported";
            }
        };

        try {
            mapper.reader().with(unsupportedSchema);
            fail("Expected IllegalArgumentException for unsupported FormatSchema");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("Can not use FormatSchema"));
        }
    }

    @Test(timeout = 4000)
    public void testRootUnwrappingFailures() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.readerFor(RootBean.class)
                .with(DeserializationFeature.UNWRAP_ROOT_VALUE);

        // Failure 1: Current token not START_OBJECT
        try {
            reader.readValue("123");
            fail("Expected JsonMappingException for non START_OBJECT wrapper");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Current token not START_OBJECT"));
        }

        // Failure 2: Field name does not match expected root name
        try {
            reader.readValue("{\"wrongName\":{\"val\":1}}");
            fail("Expected JsonMappingException for mismatched root name");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Root name 'wrongName' does not match expected"));
        }

        // Failure 3: Trailing token after unwrapping is not END_OBJECT
        try {
            reader.readValue("{\"root\":{\"val\":1}, \"extra\":2}");
            fail("Expected JsonMappingException for extra trailing content before END_OBJECT");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Current token not END_OBJECT"));
        }
    }

    @Test(timeout = 4000)
    public void testDataFormatReadersUndetectableSources() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader r = mapper.readerFor(Bean.class);
        ObjectReader detectorReader = r.withFormatDetection(r);

        // String source rejection
        try {
            detectorReader.readValue("{\"a\":1}");
            fail("Should fail on String input with format detection");
        } catch (JsonParseException expected) {
            assertTrue(expected.getMessage().contains("must be byte- not char-based"));
        }

        // Reader source rejection
        try {
            detectorReader.readValue(new StringReader("{\"a\":1}"));
            fail("Should fail on Reader input with format detection");
        } catch (JsonParseException expected) {
            assertTrue(expected.getMessage().contains("must be byte- not char-based"));
        }

        // JsonNode source rejection
        try {
            detectorReader.readValue(mapper.createObjectNode());
            fail("Should fail on JsonNode input with format detection");
        } catch (JsonParseException expected) {
            assertTrue(expected.getMessage().contains("must be byte- not char-based"));
        }

        // readValues(String) rejection
        try {
            detectorReader.readValues("{\"a\":1}");
            fail("Should fail on readValues(String) with format detection");
        } catch (JsonParseException expected) {
            assertTrue(expected.getMessage().contains("must be byte- not char-based"));
        }

        // readValues(Reader) rejection
        try {
            detectorReader.readValues(new StringReader("{\"a\":1}"));
            fail("Should fail on readValues(Reader) with format detection");
        } catch (JsonParseException expected) {
            assertTrue(expected.getMessage().contains("must be byte- not char-based"));
        }
    }

    @Test(timeout = 4000)
    public void testDataFormatReadersUnknownFormatThrows() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader r = mapper.readerFor(Bean.class);
        ObjectReader detectorReader = r.withFormatDetection(r);

        byte[] garbage = new byte[] { (byte) 0xFF, (byte) 0xFE, 0x12, 0x34 };
        try {
            detectorReader.readValue(garbage);
            fail("Expected JsonParseException for unrecognizable format");
        } catch (IOException expected) {
            assertTrue(expected instanceof JsonParseException);
            assertTrue(expected.getMessage().contains("Can not detect format from input"));
        }
    }

    /*
     * =========================================================================
     * Partition E: Object Lifecycle & Mutant Factory Contract Integrity
     * =========================================================================
     */

    @Test(timeout = 4000)
    public void testMutantFactoriesAndConfigurations() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader base = mapper.reader();

        // DeserializationFeature mutants
        ObjectReader r1 = base.with(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        assertTrue(r1.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        ObjectReader r2 = r1.without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        assertFalse(r2.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));

        ObjectReader r3 = base.with(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT,
                DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        assertTrue(r3.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));
        ObjectReader r4 = r3.without(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT,
                DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        assertFalse(r4.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));

        ObjectReader r5 = base.withFeatures(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
        assertTrue(r5.isEnabled(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES));
        ObjectReader r6 = r5.withoutFeatures(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
        assertFalse(r6.isEnabled(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES));

        // JsonParser.Feature mutants
        ObjectReader rp1 = base.with(JsonParser.Feature.ALLOW_COMMENTS);
        assertTrue(rp1.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));
        ObjectReader rp2 = rp1.without(JsonParser.Feature.ALLOW_COMMENTS);
        assertFalse(rp2.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));

        ObjectReader rp3 = base.withFeatures(JsonParser.Feature.ALLOW_YAML_COMMENTS);
        assertTrue(rp3.isEnabled(JsonParser.Feature.ALLOW_YAML_COMMENTS));
        ObjectReader rp4 = rp3.withoutFeatures(JsonParser.Feature.ALLOW_YAML_COMMENTS);
        assertFalse(rp4.isEnabled(JsonParser.Feature.ALLOW_YAML_COMMENTS));

        // FormatFeature mutants
        FormatFeature dummyFeature = new FormatFeature() {
            @Override
            public boolean enabledByDefault() { return false; }
            @Override
            public int getMask() { return 1; }
            @Override
            public boolean enabledIn(int flags) { return (flags & 1) != 0; }
        };
        assertNotNull(base.with(dummyFeature));
        assertNotNull(base.withFeatures(dummyFeature));
        assertNotNull(base.without(dummyFeature));
        assertNotNull(base.withoutFeatures(dummyFeature));

        // Type methods and identity checks
        JavaType jt = mapper.constructType(Bean.class);
        ObjectReader rt1 = base.forType(jt);
        assertSame(rt1, rt1.forType(jt));
        assertNotNull(base.forType(Bean.class));
        assertNotNull(base.forType(new TypeReference<Bean>() {}));

        // Deprecated withType aliases
        assertNotNull(base.withType(jt));
        assertNotNull(base.withType(Bean.class));
        assertNotNull(base.withType((java.lang.reflect.Type) Bean.class));
        assertNotNull(base.withType(new TypeReference<Bean>() {}));

        // InjectableValues
        InjectableValues.Std iv = new InjectableValues.Std();
        ObjectReader withIv = base.with(iv);
        assertSame(iv, withIv.getInjectableValues());
        assertSame(withIv, withIv.with(iv));

        // JsonFactory mutant
        JsonFactory f = new JsonFactory();
        ObjectReader withF = base.with(f);
        assertSame(f, withF.getFactory());
        assertSame(withF, withF.with(f));

        // Root names
        assertNotNull(base.withRootName("custom"));
        assertNotNull(base.withRootName(PropertyName.construct("customProp")));
        assertNotNull(base.withoutRootName());

        // ContextAttributes
        ContextAttributes attrs = ContextAttributes.getEmpty().withSharedAttribute("k1", "v1");
        ObjectReader withAttrs = base.with(attrs);
        assertEquals("v1", withAttrs.getAttributes().getAttribute("k1"));

        Map<String, Object> map = new HashMap<>();
        map.put("k2", "v2");
        ObjectReader withMap = base.withAttributes(map);
        assertEquals("v2", withMap.getAttributes().getAttribute("k2"));

        ObjectReader withSingle = base.withAttribute("k3", "v3");
        assertEquals("v3", withSingle.getAttributes().getAttribute("k3"));
        ObjectReader withoutSingle = withSingle.withoutAttribute("k3");
        assertNull(withoutSingle.getAttributes().getAttribute("k3"));

        // Format detection mutant
        DataFormatReaders dfReaders = new DataFormatReaders(base);
        ObjectReader withDet = base.withFormatDetection(dfReaders);
        assertNotNull(withDet);

        // Simple accessors
        assertNotNull(base.getConfig());
        assertNotNull(base.getTypeFactory());
        assertNotNull(base.version());
        assertNotNull(base.withView(Object.class));
        assertNotNull(base.with(Locale.US));
        assertNotNull(base.with(TimeZone.getTimeZone("UTC")));
        assertNotNull(base.withHandler(new DeserializationProblemHandler() {}));
        assertNotNull(base.with(Base64Variants.MIME));
        assertNotNull(base.with(JsonNodeFactory.instance));
        assertSame(base, base.with(base.getConfig()));
    }
}