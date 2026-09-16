package com.fasterxml.jackson.dataformat.xml.ser;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Class Under Test: com.fasterxml.jackson.dataformat.xml.ser.XmlSerializerProvider
 *
 * Targeted Decision Branches:
 * 1. serializeValue(gen, value):
 *    - value == null -> calls _serializeXmlNull(gen) -> root name from config vs default ROOT_NAME_FOR_NULL
 *    - value != null:
 *      - _asXmlGenerator(gen) != null (normal ToXmlGenerator flow):
 *        - rootName from config vs rootName from _rootNameLookup
 *        - TypeUtil.isIndexedType(cls) == true -> _startRootArray + gen.writeEndObject()
 *        - TypeUtil.isIndexedType(cls) == false -> single object serialization
 *      - _asXmlGenerator(gen) == null (TokenBuffer flow) -> asArray = false, no root init, no writeEndObject()
 *      - Exception handling: RuntimeException wrapped via _wrapAsIOE, IOException propagated
 * 2. serializeValue(gen, value, rootType, ser):
 *    - value == null -> calls _serializeXmlNull(gen)
 *    - value != null:
 *      - rootName from config vs rootName from _rootNameLookup with rootType
 *      - ser == null -> findTypedValueSerializer(rootType, true, null)
 *      - ser != null -> uses custom provided serializer
 *      - TypeUtil.isIndexedType(rootType) == true -> array wrapping with writeEndObject()
 *      - TypeUtil.isIndexedType(rootType) == false -> no array wrapping
 * 3. _asXmlGenerator(gen):
 *    - gen instanceof ToXmlGenerator -> returns cast generator
 *    - gen instanceof TokenBuffer -> returns null
 *    - other JsonGenerator type -> throws JsonMappingException with diagnostic message
 * 4. _rootNameFromConfig():
 *    - config.getFullRootName() == null -> returns null
 *    - PropertyName has empty/null namespace -> returns QName(simpleName)
 *    - PropertyName has non-empty namespace -> returns QName(namespace, simpleName)
 * 5. _initWithRootName(xgen, rootName):
 *    - xgen.setNextNameIfMissing == false and inRoot == true -> insists via setNextName
 *    - namespace != null && namespace.length() > 0 -> calls setDefaultNamespace
 *    - namespace == null || empty -> skips setDefaultNamespace
 * 6. _wrapAsIOE(gen, exception):
 *    - exception is IOException -> returned directly
 *    - exception has message -> JsonMappingException with original message
 *    - exception has null message -> JsonMappingException with "[no message for ...]"
 * 7. Copy Constructor & copy():
 *    - Defect [dataformat-xml#282]: XmlSerializerProvider(XmlSerializerProvider src) must NOT
 *      share _rootNameLookup instance with source, otherwise configuration changes on copy
 *      (e.g., MapperFeature.USE_ANNOTATIONS disabled) fail because cached names are reused.
 */

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.xml.namespace.QName;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.util.XmlRootNameLookup;

public class XmlSerializerProviderGeminiTest {

    // Helper POJOs for testing various serialization paths
    @JsonRootName("AnnotatedName")
    static class Pojo282 {
        public int a = 3;
    }

    @JsonRootName(value = "customRoot", namespace = "http://example.org/ns")
    static class NsBean {
        public String field = "value";
    }

    static class SimpleBean {
        public String name;
        public SimpleBean(String name) { this.name = name; }
    }

    /*
     * -------------------------------------------------------------------------
     * Partition A: Core Functional Logic & State Transitions
     * -------------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testSerializeValueNormalObject() throws Exception {
        XmlMapper mapper = new XmlMapper();
        XmlSerializerProvider blueprint = (XmlSerializerProvider) mapper.getSerializerProvider();
        XmlSerializerProvider prov = (XmlSerializerProvider) blueprint.createInstance(
                mapper.getSerializationConfig(), mapper.getSerializerFactory());

        StringWriter sw = new StringWriter();
        ToXmlGenerator xgen = (ToXmlGenerator) mapper.getFactory().createGenerator(sw);
        prov.serializeValue(xgen, new SimpleBean("testValue"));
        xgen.close();

        String xml = sw.toString();
        assertTrue("XML should contain SimpleBean root element", xml.contains("<SimpleBean>"));
        assertTrue("XML should contain field value", xml.contains("<name>testValue</name>"));
    }

    @Test(timeout = 4000)
    public void testSerializeValueIndexedTypeArray() throws Exception {
        XmlMapper mapper = new XmlMapper();
        XmlSerializerProvider blueprint = (XmlSerializerProvider) mapper.getSerializerProvider();
        XmlSerializerProvider prov = (XmlSerializerProvider) blueprint.createInstance(
                mapper.getSerializationConfig(), mapper.getSerializerFactory());

        StringWriter sw = new StringWriter();
        ToXmlGenerator xgen = (ToXmlGenerator) mapper.getFactory().createGenerator(sw);
        prov.serializeValue(xgen, Arrays.asList("elemA", "elemB"));
        xgen.close();

        String xml = sw.toString();
        assertTrue("Should contain array item tags", xml.contains("<item>elemA</item>"));
        assertTrue("Should contain second item tag", xml.contains("<item>elemB</item>"));
    }

    @Test(timeout = 4000)
    public void testSerializeValueWithRootTypeAndCustomSerializer() throws Exception {
        XmlMapper mapper = new XmlMapper();
        XmlSerializerProvider blueprint = (XmlSerializerProvider) mapper.getSerializerProvider();
        XmlSerializerProvider prov = (XmlSerializerProvider) blueprint.createInstance(
                mapper.getSerializationConfig(), mapper.getSerializerFactory());

        JavaType strType = mapper.constructType(String.class);
        JsonSerializer<Object> customSer = new JsonSerializer<Object>() {
            @Override
            public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                gen.writeString("PREFIX:" + value);
            }
        };

        StringWriter sw = new StringWriter();
        ToXmlGenerator xgen = (ToXmlGenerator) mapper.getFactory().createGenerator(sw);
        prov.serializeValue(xgen, "plainString", strType, customSer);
        xgen.close();

        String xml = sw.toString();
        assertTrue("Output should include custom serializer prefix", xml.contains("PREFIX:plainString"));
    }

    @Test(timeout = 4000)
    public void testSerializeValueWithRootTypeIndexed() throws Exception {
        XmlMapper mapper = new XmlMapper();
        XmlSerializerProvider blueprint = (XmlSerializerProvider) mapper.getSerializerProvider();
        XmlSerializerProvider prov = (XmlSerializerProvider) blueprint.createInstance(
                mapper.getSerializationConfig(), mapper.getSerializerFactory());

        JavaType listType = mapper.getTypeFactory().constructCollectionType(List.class, String.class);
        StringWriter sw = new StringWriter();
        ToXmlGenerator xgen = (ToXmlGenerator) mapper.getFactory().createGenerator(sw);
        prov.serializeValue(xgen, Collections.singletonList("itemVal"), listType, null);
        xgen.close();

        String xml = sw.toString();
        assertTrue("Should wrap indexed JavaType into item element", xml.contains("<item>itemVal</item>"));
    }

    @Test(timeout = 4000)
    public void testSerializeValueWithNamespace() throws Exception {
        XmlMapper mapper = new XmlMapper();
        XmlSerializerProvider blueprint = (XmlSerializerProvider) mapper.getSerializerProvider();
        XmlSerializerProvider prov = (XmlSerializerProvider) blueprint.createInstance(
                mapper.getSerializationConfig(), mapper.getSerializerFactory());

        StringWriter sw = new StringWriter();
        ToXmlGenerator xgen = (ToXmlGenerator) mapper.getFactory().createGenerator(sw);
        prov.serializeValue(xgen, new NsBean());
        xgen.close();

        String xml = sw.toString();
        assertTrue("Should contain declared namespace", xml.contains("http://example.org/ns"));
        assertTrue("Should contain root tag name customRoot", xml.contains("customRoot"));
    }

    /*
     * -------------------------------------------------------------------------
     * Partition B: Boundary Value Analysis (BVA) & Extremes
     * -------------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testSerializeXmlNullDefaultRoot() throws Exception {
        XmlMapper mapper = new XmlMapper();
        XmlSerializerProvider blueprint = (XmlSerializerProvider) mapper.getSerializerProvider();
        XmlSerializerProvider prov = (XmlSerializerProvider) blueprint.createInstance(
                mapper.getSerializationConfig(), mapper.getSerializerFactory());

        StringWriter sw = new StringWriter();
        ToXmlGenerator xgen = (ToXmlGenerator) mapper.getFactory().createGenerator(sw);
        prov.serializeValue(xgen, null);
        xgen.close();

        String xml = sw.toString();
        assertTrue("Null serialization should yield <null/>", xml.contains("<null"));
    }

    @Test(timeout = 4000)
    public void testSerializeXmlNullWithConfiguredRoot() throws Exception {
        XmlMapper mapper = new XmlMapper();
        SerializationConfig cfg = mapper.getSerializationConfig().withRootName(PropertyName.construct("customNull", null));
        XmlSerializerProvider blueprint = (XmlSerializerProvider) mapper.getSerializerProvider();
        XmlSerializerProvider prov = (XmlSerializerProvider) blueprint.createInstance(cfg, mapper.getSerializerFactory());

        StringWriter sw = new StringWriter();
        ToXmlGenerator xgen = (ToXmlGenerator) mapper.getFactory().createGenerator(sw);
        prov.serializeValue(xgen, null);
        xgen.close();

        String xml = sw.toString();
        assertTrue("Null serialization with config root should yield <customNull/>", xml.contains("<customNull"));
    }

    @Test(timeout = 4000)
    public void testSerializeNullWithRootType() throws Exception {
        XmlMapper mapper = new XmlMapper();
        XmlSerializerProvider blueprint = (XmlSerializerProvider) mapper.getSerializerProvider();
        XmlSerializerProvider prov = (XmlSerializerProvider) blueprint.createInstance(
                mapper.getSerializationConfig(), mapper.getSerializerFactory());

        StringWriter sw = new StringWriter();
        ToXmlGenerator xgen = (ToXmlGenerator) mapper.getFactory().createGenerator(sw);
        prov.serializeValue(xgen, null, mapper.constructType(String.class), null);
        xgen.close();

        String xml = sw.toString();
        assertTrue("Null value with rootType should serialize as null element", xml.contains("<null"));
    }

    @Test(timeout = 4000)
    public void testSerializeValueUsingTokenBuffer() throws Exception {
        XmlMapper mapper = new XmlMapper();
        XmlSerializerProvider blueprint = (XmlSerializerProvider) mapper.getSerializerProvider();
        XmlSerializerProvider prov = (XmlSerializerProvider) blueprint.createInstance(
                mapper.getSerializationConfig(), mapper.getSerializerFactory());

        TokenBuffer tb = new TokenBuffer(mapper, false);
        prov.serializeValue(tb, new SimpleBean("tbData"));

        assertNotNull("TokenBuffer should have recorded tokens", tb.firstToken());

        // Also test TokenBuffer with null value
        TokenBuffer tbNull = new TokenBuffer(mapper, false);
        prov.serializeValue(tbNull, null);
        assertNotNull("TokenBuffer with null should record tokens", tbNull.firstToken());
    }

    @Test(timeout = 4000)
    public void testSerializeValueWithRootTypeUsingTokenBuffer() throws Exception {
        XmlMapper mapper = new XmlMapper();
        XmlSerializerProvider blueprint = (XmlSerializerProvider) mapper.getSerializerProvider();
        XmlSerializerProvider prov = (XmlSerializerProvider) blueprint.createInstance(
                mapper.getSerializationConfig(), mapper.getSerializerFactory());

        TokenBuffer tb = new TokenBuffer(mapper, false);
        JavaType strType = mapper.constructType(String.class);
        prov.serializeValue(tb, "tokenTypeData", strType, null);
        assertNotNull("TokenBuffer should have tokens for rootType serialization", tb.firstToken());
    }

    /*
     * -------------------------------------------------------------------------
     * Partition C: Defect-Targeted Branch Zone (Issue #282 / MapperCopyTest)
     * -------------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testCopyWithDefect282AnnotationIndependence() throws Exception {
        XmlMapper mapper1 = new XmlMapper();
        Pojo282 p = new Pojo282();
        String xml1 = mapper1.writeValueAsString(p);
        assertTrue("mapper1 must output annotated root name 'AnnotatedName'", xml1.contains("AnnotatedName"));

        XmlMapper mapper2 = mapper1.copy();
        mapper2.disable(MapperFeature.USE_ANNOTATIONS);
        String xml2 = mapper2.writeValueAsString(p);

        // Ground-truth defect assertion from Defects4J [dataformat-xml#282]:
        // In the defective version, copy() reuses the source's _rootNameLookup cache,
        // erroneously emitting <AnnotatedName> instead of <Pojo282>.
        assertFalse("Should NOT use name 'AnnotatedName' but 'Pojo282', xml = " + xml2,
                xml2.contains("AnnotatedName"));
        assertTrue("mapper2 should output default class name 'Pojo282', xml = " + xml2,
                xml2.contains("Pojo282"));
    }

    @Test(timeout = 4000)
    public void testCopyDoesNotShareRootNameLookupInstance() {
        XmlRootNameLookup lookup = new XmlRootNameLookup();
        XmlSerializerProvider prov = new XmlSerializerProvider(lookup);
        XmlSerializerProvider copy = (XmlSerializerProvider) prov.copy();

        // The copy must own an independent XmlRootNameLookup instance to prevent cross-configuration cache leaks
        assertNotSame("XmlSerializerProvider.copy() must NOT share _rootNameLookup instance with src",
                prov._rootNameLookup, copy._rootNameLookup);
    }

    /*
     * -------------------------------------------------------------------------
     * Partition D: Exception & Defensive Guard Paths
     * -------------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testAsXmlGeneratorRejectsNonXmlNonTokenBuffer() throws Exception {
        XmlMapper mapper = new XmlMapper();
        XmlSerializerProvider prov = (XmlSerializerProvider) mapper.getSerializerProvider();

        JsonFactory jsonFactory = new JsonFactory();
        JsonGenerator jsonGen = jsonFactory.createGenerator(new StringWriter());

        try {
            prov._asXmlGenerator(jsonGen);
            fail("Expected JsonMappingException for unsupported generator type");
        } catch (JsonMappingException e) {
            assertTrue("Exception message must describe unsupported generator type",
                    e.getMessage().contains("XmlMapper does not with generators of type other than ToXmlGenerator"));
        }
    }

    @Test(timeout = 4000)
    public void testSerializeValueWrapsRuntimeException() throws Exception {
        XmlMapper mapper = new XmlMapper();
        XmlSerializerProvider blueprint = (XmlSerializerProvider) mapper.getSerializerProvider();
        XmlSerializerProvider prov = (XmlSerializerProvider) blueprint.createInstance(
                mapper.getSerializationConfig(), mapper.getSerializerFactory());

        JsonSerializer<Object> throwingSer = new JsonSerializer<Object>() {
            @Override
            public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                throw new IllegalStateException("Deliberate failure in test");
            }
        };

        StringWriter sw = new StringWriter();
        ToXmlGenerator xgen = (ToXmlGenerator) mapper.getFactory().createGenerator(sw);
        try {
            prov.serializeValue(xgen, "failVal", mapper.constructType(String.class), throwingSer);
            fail("Expected IOException wrapping the IllegalStateException");
        } catch (IOException e) {
            assertTrue("Expected JsonMappingException wrapper", e instanceof JsonMappingException);
            assertTrue("Wrapped exception message should propagate", e.getMessage().contains("Deliberate failure in test"));
            assertTrue("Cause should be IllegalStateException", e.getCause() instanceof IllegalStateException);
        }
    }

    @Test(timeout = 4000)
    public void testWrapAsIOEBranchCoverage() {
        XmlSerializerProvider prov = new XmlSerializerProvider(new XmlRootNameLookup());

        // Branch 1: e instanceof IOException -> returns e directly
        IOException originalIOE = new IOException("disk error");
        IOException wrapped1 = prov._wrapAsIOE(null, originalIOE);
        assertSame("Should return the exact IOException instance", originalIOE, wrapped1);

        // Branch 2: e not IOException, message != null
        RuntimeException reWithMsg = new RuntimeException("custom msg");
        IOException wrapped2 = prov._wrapAsIOE(null, reWithMsg);
        assertTrue(wrapped2 instanceof JsonMappingException);
        assertEquals("custom msg", wrapped2.getMessage());
        assertSame(reWithMsg, wrapped2.getCause());

        // Branch 3: e not IOException, message == null
        NullPointerException npeNoMsg = new NullPointerException();
        IOException wrapped3 = prov._wrapAsIOE(null, npeNoMsg);
        assertTrue(wrapped3 instanceof JsonMappingException);
        assertEquals("[no message for java.lang.NullPointerException]", wrapped3.getMessage());
        assertSame(npeNoMsg, wrapped3.getCause());
    }

    /*
     * -------------------------------------------------------------------------
     * Partition E: Object Lifecycle & Contract Integrity
     * -------------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testRootNameFromConfigBranches() {
        XmlMapper mapper = new XmlMapper();
        XmlSerializerProvider blueprint = (XmlSerializerProvider) mapper.getSerializerProvider();

        // Branch 1: getFullRootName() == null
        XmlSerializerProvider prov1 = (XmlSerializerProvider) blueprint.createInstance(
                mapper.getSerializationConfig(), mapper.getSerializerFactory());
        assertNull("Should be null when config full root name is null", prov1._rootNameFromConfig());

        // Branch 2: PropertyName with null namespace
        SerializationConfig cfg2 = mapper.getSerializationConfig().withRootName(PropertyName.construct("rootSimple", null));
        XmlSerializerProvider prov2 = (XmlSerializerProvider) blueprint.createInstance(cfg2, mapper.getSerializerFactory());
        assertEquals(new QName("rootSimple"), prov2._rootNameFromConfig());

        // Branch 3: PropertyName with empty namespace string
        SerializationConfig cfg3 = mapper.getSerializationConfig().withRootName(new PropertyName("rootEmptyNs", ""));
        XmlSerializerProvider prov3 = (XmlSerializerProvider) blueprint.createInstance(cfg3, mapper.getSerializerFactory());
        assertEquals(new QName("rootEmptyNs"), prov3._rootNameFromConfig());

        // Branch 4: PropertyName with non-empty namespace
        SerializationConfig cfg4 = mapper.getSerializationConfig().withRootName(new PropertyName("rootWithNs", "http://domain.test/ns"));
        XmlSerializerProvider prov4 = (XmlSerializerProvider) blueprint.createInstance(cfg4, mapper.getSerializerFactory());
        assertEquals(new QName("http://domain.test/ns", "rootWithNs"), prov4._rootNameFromConfig());
    }

    @Test(timeout = 4000)
    public void testInitWithRootNameAndStartRootArray() throws Exception {
        XmlMapper mapper = new XmlMapper();
        XmlSerializerProvider prov = (XmlSerializerProvider) mapper.getSerializerProvider();

        StringWriter sw = new StringWriter();
        ToXmlGenerator xgen = (ToXmlGenerator) mapper.getFactory().createGenerator(sw);

        // Test _initWithRootName with namespace
        prov._initWithRootName(xgen, new QName("http://example.com/ns2", "testRoot"));

        // Test _startRootArray directly
        prov._startRootArray(xgen, new QName("items"));
        xgen.writeString("arrayContent");
        xgen.writeEndObject();
        xgen.close();

        String xml = sw.toString();
        assertTrue("XML should include item element from _startRootArray", xml.contains("<item>arrayContent</item>"));
    }

    @Test(timeout = 4000)
    public void testCreateInstanceAndCopyConstructors() {
        XmlRootNameLookup rootLookup = new XmlRootNameLookup();
        XmlSerializerProvider prov = new XmlSerializerProvider(rootLookup);
        assertNotNull(prov);

        XmlMapper mapper = new XmlMapper();
        DefaultSerializerProvider copy = prov.copy();
        assertNotNull("copy() result should not be null", copy);
        assertTrue("copy() must return XmlSerializerProvider", copy instanceof XmlSerializerProvider);

        DefaultSerializerProvider instance = prov.createInstance(
                mapper.getSerializationConfig(), mapper.getSerializerFactory());
        assertNotNull("createInstance() result should not be null", instance);
        assertTrue("createInstance() must return XmlSerializerProvider", instance instanceof XmlSerializerProvider);
    }
}