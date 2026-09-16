/*
 * [Branch & Defect Analysis Matrix]
 *
 * Class Under Test: com.fasterxml.jackson.dataformat.xml.ser.XmlSerializerProvider
 *
 * 1. DEFECT INVESTIGATION (Defects4J - RootNameTest::testDynamicRootName):
 *    - Issue: When serializing a Java null value using an explicitly configured root name via
 *      ObjectWriter (e.g. writer().withRootName("rudy").writeValueAsString(null)),
 *      _serializeXmlNull unconditionally forces ROOT_NAME_FOR_NULL (new QName("null")),
 *      ignoring any root name configured in _config (_rootNameFromConfig()).
 *    - Manifestation: Expected XML root tag "<rudy/>", but received "<null/>".
 *    - Targeted Test: testDynamicRootNameWithNullValue() explicitly triggers serialization
 *      of null with a configured root name to expose this defect.
 *
 * 2. BRANCH & LOGICAL COVERAGE PLAN:
 *    - serializeValue(gen, value):
 *        * Branch: value == null -> delegates to _serializeXmlNull()
 *        * Branch: gen instanceof ToXmlGenerator vs non-XmlGenerator (TokenBuffer vs invalid generator)
 *        * Branch: _rootNameFromConfig() returns null vs non-null (with and without namespace)
 *        * Branch: asArray == true (indexed types: List, Array) vs false (POJO, Map)
 *        * Exception branches: IOException thrown as-is, RuntimeException wrapped in JsonMappingException
 *    - serializeValue(gen, value, rootType):
 *        * Branch: value == null -> delegates to _serializeXmlNull()
 *        * Branch: _rootNameFromConfig() != null vs null
 *        * Branch: TypeUtil.isIndexedType(rootType) true vs false
 *        * Exception handling for custom serializers throwing IOException / RuntimeException
 *    - serializeValue(gen, value, rootType, ser):
 *        * Branch: ser != null vs ser == null (resolves typed value serializer)
 *    - _asXmlGenerator(gen):
 *        * Branch: gen instanceof ToXmlGenerator -> cast and return
 *        * Branch: gen instanceof TokenBuffer -> returns null
 *        * Branch: other generator (e.g. UTF8JsonGenerator) -> throws JsonMappingException
 *    - _rootNameFromConfig():
 *        * Branch: _config.getFullRootName() == null -> returns null
 *        * Branch: PropertyName has null or empty namespace -> new QName(simpleName)
 *        * Branch: PropertyName has non-empty namespace -> new QName(ns, simpleName)
 *    - _initWithRootName(xgen, rootName):
 *        * Branch: setNextNameIfMissing false & inRoot true -> calls setNextName
 *        * Branch: rootName.getNamespaceURI() != null && length > 0 -> setDefaultNamespace
 *    - Lifecycle / createInstance:
 *        * Verifies copy constructor maintains rootNameLookup and creates valid child instance.
 */

package com.fasterxml.jackson.dataformat.xml.ser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.namespace.QName;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.databind.ser.SerializerFactory;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.util.XmlRootNameLookup;

public class XmlSerializerProviderGeminiTest {

    // Helper dummy classes for testing
    @JacksonXmlRootElement(localName = "annotatedPojo", namespace = "http://example.com/ns")
    static class AnnotatedPojo {
        public String name = "test";
    }

    static class PlainPojo {
        public int id = 42;
    }

    static class RuntimeExceptionThrowingSerializer extends JsonSerializer<Object> {
        @Override
        public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) {
            throw new IllegalStateException("Simulated runtime failure");
        }
    }

    static class IOExceptionThrowingSerializer extends JsonSerializer<Object> {
        @Override
        public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            throw new IOException("Simulated IO failure");
        }
    }

    /*
    /**********************************************************************
    /* Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    /**********************************************************************
     */

    /**
     * TARGET DEFECT: RootNameTest::testDynamicRootName
     * When serializing null with an explicitly specified root name via ObjectWriter,
     * XmlSerializerProvider._serializeXmlNull() must respect the configured root name
     * instead of unconditionally falling back to "<null/>".
     */
    @Test(timeout = 4000)
    public void testDynamicRootNameWithNullValue() throws Exception {
        XmlMapper mapper = new XmlMapper();
        ObjectWriter writer = mapper.writer().withRootName("rudy");
        String xml = writer.writeValueAsString(null);
        assertNotNull("Serialized XML should not be null", xml);
        assertTrue("Expected root tag to be <rudy/> but got: " + xml, xml.contains("<rudy") && xml.contains("/>"));
        assertFalse("Root tag must not be <null/> when explicit root name is configured", xml.contains("<null"));
    }

    /**
     * Additional check for dynamic root name with namespace when value is null.
     */
    @Test(timeout = 4000)
    public void testDynamicRootNameWithNamespaceAndNullValue() throws Exception {
        XmlMapper mapper = new XmlMapper();
        PropertyName pName = new PropertyName("customRoot", "http://custom.namespace");
        ObjectWriter writer = mapper.writer().withRootName(pName);
        String xml = writer.writeValueAsString(null);
        assertNotNull("Serialized XML should not be null", xml);
        assertTrue("XML should contain customRoot, got: " + xml, xml.contains("customRoot"));
        assertFalse("XML should not contain null tag, got: " + xml, xml.contains("<null"));
    }

    /*
    /**********************************************************************
    /* Partition A: Core Functional Logic & State Transitions
    /**********************************************************************
     */

    @Test(timeout = 4000)
    public void testDefaultSerializationOfNull() throws Exception {
        XmlMapper mapper = new XmlMapper();
        String xml = mapper.writeValueAsString(null);
        assertNotNull(xml);
        assertTrue("Default serialization of null should produce <null/> tag", xml.contains("<null/>"));
    }

    @Test(timeout = 4000)
    public void testSerializeIndexedTypeArray() throws Exception {
        XmlMapper mapper = new XmlMapper();
        String[] data = new String[] { "alpha", "beta" };
        String xml = mapper.writeValueAsString(data);
        assertNotNull(xml);
        assertTrue("XML array should contain <item>", xml.contains("<item>alpha</item>"));
        assertTrue("XML array should contain second <item>", xml.contains("<item>beta</item>"));
    }

    @Test(timeout = 4000)
    public void testSerializeIndexedTypeListWithRootName() throws Exception {
        XmlMapper mapper = new XmlMapper();
        List<String> list = new ArrayList<String>(Arrays.asList("one", "two"));
        String xml = mapper.writer().withRootName("items").writeValueAsString(list);
        assertNotNull(xml);
        assertTrue("XML root should be items", xml.startsWith("<items>"));
        assertTrue("XML should end with </items>", xml.endsWith("</items>"));
        assertTrue("XML should contain items", xml.contains("<item>one</item>"));
    }

    @Test(timeout = 4000)
    public void testSerializeValueWithRootType() throws Exception {
        XmlMapper mapper = new XmlMapper();
        JavaType type = mapper.getTypeFactory().constructType(PlainPojo.class);
        PlainPojo pojo = new PlainPojo();
        String xml = mapper.writerFor(type).writeValueAsString(pojo);
        assertNotNull(xml);
        assertTrue("Should serialize PlainPojo id", xml.contains("<id>42</id>"));
    }

    @Test(timeout = 4000)
    public void testSerializeValueWithCustomSerializer() throws Exception {
        XmlMapper mapper = new XmlMapper();
        JavaType type = mapper.getTypeFactory().constructType(PlainPojo.class);
        XmlSerializerProvider provider = (XmlSerializerProvider) mapper.getSerializerProviderInstance();
        StringWriter sw = new StringWriter();
        ToXmlGenerator xgen = mapper.getFactory().createGenerator(sw);

        JsonSerializer<Object> customSer = new JsonSerializer<Object>() {
            @Override
            public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                gen.writeStartObject();
                gen.writeStringField("custom", "val");
                gen.writeEndObject();
            }
        };

        provider.serializeValue(xgen, new PlainPojo(), type, customSer);
        xgen.close();
        String xml = sw.toString();
        assertTrue("Custom serializer should write custom field", xml.contains("<custom>val</custom>"));
    }

    @Test(timeout = 4000)
    public void testSerializationWithNamespaceAnnotation() throws Exception {
        XmlMapper mapper = new XmlMapper();
        AnnotatedPojo pojo = new AnnotatedPojo();
        String xml = mapper.writeValueAsString(pojo);
        assertNotNull(xml);
        assertTrue("XML must contain custom namespace", xml.contains("http://example.com/ns"));
        assertTrue("XML must contain root element name annotatedPojo", xml.contains("annotatedPojo"));
    }

    @Test(timeout = 4000)
    public void testSerializationWithTokenBuffer() throws Exception {
        XmlMapper mapper = new XmlMapper();
        XmlSerializerProvider provider = (XmlSerializerProvider) mapper.getSerializerProviderInstance();
        TokenBuffer buffer = new TokenBuffer(mapper, false);

        // serializeValue through TokenBuffer (_asXmlGenerator returns null, asArray is false)
        PlainPojo pojo = new PlainPojo();
        provider.serializeValue(buffer, pojo);

        assertNotNull(buffer);
        // Verify buffer holds the serialized content by converting
        PlainPojo result = mapper.readValue(buffer.asParser(), PlainPojo.class);
        assertEquals(42, result.id);
    }

    /*
    /**********************************************************************
    /* Partition B: Boundary Value Analysis (BVA) & Extremes
    /**********************************************************************
     */

    @Test(timeout = 4000)
    public void testSerializeValueNullWithJavaType() throws Exception {
        XmlMapper mapper = new XmlMapper();
        JavaType type = mapper.getTypeFactory().constructType(PlainPojo.class);
        String xml = mapper.writerFor(type).writeValueAsString(null);
        assertNotNull(xml);
        assertTrue("Null with type should write null tag", xml.contains("<null/>"));
    }

    @Test(timeout = 4000)
    public void testSerializeValueNullWithExplicitSerializer() throws Exception {
        XmlMapper mapper = new XmlMapper();
        XmlSerializerProvider provider = (XmlSerializerProvider) mapper.getSerializerProviderInstance();
        StringWriter sw = new StringWriter();
        ToXmlGenerator xgen = mapper.getFactory().createGenerator(sw);
        JavaType type = mapper.getTypeFactory().constructType(PlainPojo.class);

        provider.serializeValue(xgen, null, type, new RuntimeExceptionThrowingSerializer());
        xgen.close();
        String xml = sw.toString();
        assertTrue("Even with explicit serializer, null value delegates to _serializeXmlNull", xml.contains("<null/>"));
    }

    @Test(timeout = 4000)
    public void testSerializeEmptyList() throws Exception {
        XmlMapper mapper = new XmlMapper();
        List<String> emptyList = new ArrayList<String>();
        String xml = mapper.writeValueAsString(emptyList);
        assertNotNull(xml);
        assertTrue("Empty list should produce valid XML element", xml.contains("<ArrayList"));
    }

    @Test(timeout = 4000)
    public void testSerializeEmptyArray() throws Exception {
        XmlMapper mapper = new XmlMapper();
        Object[] emptyArray = new Object[0];
        String xml = mapper.writeValueAsString(emptyArray);
        assertNotNull(xml);
        assertTrue("Empty array should produce valid XML element", xml.contains("<Object"));
    }

    /*
    /**********************************************************************
    /* Partition D: Exception & Defensive Guard Paths
    /**********************************************************************
     */

    @Test(timeout = 4000)
    public void testUnsupportedGeneratorThrowsJsonMappingException() throws Exception {
        XmlSerializerProvider provider = new XmlSerializerProvider(new XmlRootNameLookup());
        ObjectMapper jsonMapper = new ObjectMapper();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator invalidGen = jsonMapper.getFactory().createGenerator(out);

        try {
            provider._asXmlGenerator(invalidGen);
            fail("Expected JsonMappingException for non-XML non-TokenBuffer generator");
        } catch (JsonMappingException expected) {
            assertTrue("Exception message should indicate generator type incompatibility",
                    expected.getMessage().contains("XmlMapper does not with generators of type other than ToXmlGenerator"));
        }
    }

    @Test(timeout = 4000)
    public void testSerializeValueWrapsRuntimeException() throws Exception {
        XmlMapper mapper = new XmlMapper();
        XmlSerializerProvider provider = (XmlSerializerProvider) mapper.getSerializerProviderInstance();
        StringWriter sw = new StringWriter();
        ToXmlGenerator xgen = mapper.getFactory().createGenerator(sw);
        JavaType type = mapper.getTypeFactory().constructType(PlainPojo.class);

        try {
            provider.serializeValue(xgen, new PlainPojo(), type, new RuntimeExceptionThrowingSerializer());
            fail("Expected JsonMappingException wrapping RuntimeException");
        } catch (JsonMappingException e) {
            assertTrue("Wrapped message should contain original cause message",
                    e.getMessage().contains("Simulated runtime failure"));
            assertEquals(IllegalStateException.class, e.getCause().getClass());
        }
    }

    @Test(timeout = 4000)
    public void testSerializeValuePassesIOExceptionUnwrapped() throws Exception {
        XmlMapper mapper = new XmlMapper();
        XmlSerializerProvider provider = (XmlSerializerProvider) mapper.getSerializerProviderInstance();
        StringWriter sw = new StringWriter();
        ToXmlGenerator xgen = mapper.getFactory().createGenerator(sw);
        JavaType type = mapper.getTypeFactory().constructType(PlainPojo.class);

        try {
            provider.serializeValue(xgen, new PlainPojo(), type, new IOExceptionThrowingSerializer());
            fail("Expected IOException to be thrown as-is");
        } catch (IOException e) {
            assertEquals("Simulated IO failure", e.getMessage());
            assertFalse("IOException should not be wrapped into JsonMappingException", e instanceof JsonMappingException);
        }
    }

    @Test(timeout = 4000)
    public void testSerializeValueUntypedWrapsRuntimeException() throws Exception {
        XmlMapper mapper = new XmlMapper();
        XmlSerializerProvider provider = (XmlSerializerProvider) mapper.getSerializerProviderInstance();
        StringWriter sw = new StringWriter();
        ToXmlGenerator xgen = mapper.getFactory().createGenerator(sw);

        // Using a custom class that fails inside serializer
        Object failingObject = new Object() {
            @SuppressWarnings("unused")
            public String getBadValue() {
                throw new RuntimeException("Property getter failed");
            }
        };

        try {
            provider.serializeValue(xgen, failingObject);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException expected) {
            assertTrue("Should wrap runtime exception", expected.getMessage().contains("Property getter failed"));
        }
    }

    /*
    /**********************************************************************
    /* Partition E: Object Lifecycle & Contract Integrity
    /**********************************************************************
     */

    @Test(timeout = 4000)
    public void testCreateInstanceAndCopyConstructor() throws Exception {
        XmlRootNameLookup lookup = new XmlRootNameLookup();
        XmlSerializerProvider provider = new XmlSerializerProvider(lookup);

        XmlMapper mapper = new XmlMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        SerializerFactory factory = mapper.getSerializerFactory();

        DefaultSerializerProvider instance = provider.createInstance(config, factory);
        assertNotNull("createInstance should return non-null", instance);
        assertTrue("createInstance should return XmlSerializerProvider", instance instanceof XmlSerializerProvider);

        XmlSerializerProvider xmlInstance = (XmlSerializerProvider) instance;
        assertSame("Copy constructor should preserve rootNameLookup", lookup, xmlInstance._rootNameLookup);
    }

    @Test(timeout = 4000)
    public void testRootNameFromConfigBranches() throws Exception {
        XmlMapper mapper = new XmlMapper();
        XmlSerializerProvider provider = (XmlSerializerProvider) mapper.getSerializerProviderInstance();

        // 1. FullRootName is null
        QName name1 = provider._rootNameFromConfig();
        assertNull("Default config has null root name", name1);

        // 2. FullRootName has simple name without namespace
        XmlMapper mapperSimpleRoot = new XmlMapper();
        mapperSimpleRoot.setConfig(mapperSimpleRoot.getSerializationConfig().withRootName(PropertyName.construct("customRoot")));
        XmlSerializerProvider providerSimple = (XmlSerializerProvider) mapperSimpleRoot.getSerializerProviderInstance();
        QName name2 = providerSimple._rootNameFromConfig();
        assertNotNull(name2);
        assertEquals("customRoot", name2.getLocalPart());
        assertEquals("", name2.getNamespaceURI());

        // 3. FullRootName has namespace and simple name
        XmlMapper mapperNsRoot = new XmlMapper();
        mapperNsRoot.setConfig(mapperNsRoot.getSerializationConfig().withRootName(new PropertyName("customRoot", "http://my.ns")));
        XmlSerializerProvider providerNs = (XmlSerializerProvider) mapperNsRoot.getSerializerProviderInstance();
        QName name3 = providerNs._rootNameFromConfig();
        assertNotNull(name3);
        assertEquals("customRoot", name3.getLocalPart());
        assertEquals("http://my.ns", name3.getNamespaceURI());
    }

    @Test(timeout = 4000)
    public void testSerializeValueWithRootTypeAndNullSerializerParam() throws Exception {
        XmlMapper mapper = new XmlMapper();
        XmlSerializerProvider provider = (XmlSerializerProvider) mapper.getSerializerProviderInstance();
        StringWriter sw = new StringWriter();
        ToXmlGenerator xgen = mapper.getFactory().createGenerator(sw);
        JavaType type = TypeFactory.defaultInstance().constructType(PlainPojo.class);

        // Explicitly passing null as the 4th parameter (ser), verifying provider finds typed value serializer
        provider.serializeValue(xgen, new PlainPojo(), type, null);
        xgen.close();
        String xml = sw.toString();
        assertTrue("Serialized output should contain PlainPojo id", xml.contains("<id>42</id>"));
    }
}