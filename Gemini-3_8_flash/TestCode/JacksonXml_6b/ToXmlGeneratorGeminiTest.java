package com.fasterxml.jackson.dataformat.xml.ser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.codehaus.stax2.XMLStreamWriter2;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.core.util.BufferRecycler;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlPrettyPrinter;
import com.fasterxml.jackson.dataformat.xml.util.DefaultXmlPrettyPrinter;

/*
 * [Branch & Defect Analysis Matrix]
 * Targets: ToXmlGenerator
 *
 * Partition A: Core Functional Logic & State Transitions
 * - Feature toggles: WRITE_XML_DECLARATION, WRITE_XML_1_1, enabledByDefault, collectDefaults.
 * - initGenerator() branches: 1.1 declaration, 1.0 declaration, disabled, already initialized, prolog linefeed.
 * - Element stack: nested writeStartObject, writeEndObject, writeStartArray, writeEndArray.
 * - Wrapped values: startWrappedValue / finishWrappedValue (with and without pretty printer, null wrapper).
 * - Primitive outputs: writeBoolean, writeNull, writeNumber (int, long, double, float, BigInteger, BigDecimal).
 * - BigDecimal with/without Feature.WRITE_BIGDECIMAL_AS_PLAIN.
 *
 * Partition B: Boundary Value Analysis (BVA) & Extremes
 * - writeBinary: null data -> writeNull, full slice vs partial slice (toFullBuffer logic).
 * - Empty string, zero length char arrays, unwrapped values, CDATA flags.
 * - writeRaw / writeRawValue variations: String, char[], offset + len, single char.
 * - overrideFormatFeatures mask calculations.
 *
 * Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
 * - writeBinary(InputStream, int) / writeBinary(Base64Variant, InputStream, int):
 *   Defects4J defect where binary stream serialization fails with JsonMappingException /
 *   UnsupportedOperationException ("Operation not supported by generator of type ToXmlGenerator").
 *
 * Partition D: Exception & Defensive Guard Paths
 * - handleMissingName() -> IllegalStateException when no QName is set before writeString/writeNumber.
 * - Unexpected context writes: writeEndObject when in root, writeEndArray when in root.
 * - Repeated field name error in invalid state.
 * - writeRaw on emulated Stax2 stream -> _reportUnimplementedStax2.
 * - writeRawUTF8String and writeUTF8String -> UnsupportedOperationException.
 *
 * Partition E: Object Lifecycle & Contract Integrity
 * - flush() with and without Feature.FLUSH_PASSED_TO_STREAM.
 * - close() with AUTO_CLOSE_JSON_CONTENT, auto closing open arrays/objects.
 * - canWriteFormattedNumbers, inRoot, getOutputBuffered, getOutputTarget, getStaxWriter.
 */
public class ToXmlGeneratorGeminiTest {

    private ToXmlGenerator createWoodstoxGenerator(StringWriter sw) throws IOException {
        XmlFactory factory = new XmlFactory();
        return (ToXmlGenerator) factory.createGenerator(sw);
    }

    private ToXmlGenerator createEmulatedGenerator(StringWriter sw) throws Exception {
        BufferRecycler br = new BufferRecycler();
        IOContext ioContext = new IOContext(br, sw, false);
        XMLOutputFactory xof = XMLOutputFactory.newFactory();
        XMLStreamWriter standardWriter = xof.createXMLStreamWriter(sw);
        return new ToXmlGenerator(ioContext, 0, 0, null, standardWriter);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Binary Stream Serialization)
    // =========================================================================

    @Test(timeout = 4000)
    public void testBinaryStreamSerializationDefectTarget() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator gen = createWoodstoxGenerator(sw);

        byte[] payload = new byte[] { 1, 2, 3, 4, 5 };
        InputStream in = new ByteArrayInputStream(payload);

        gen.initGenerator();
        gen.setNextName(new QName("data"));
        
        // This targets the defect where streaming binary input causes an UnsupportedOperationException
        int written = gen.writeBinary(Base64Variants.MIME, in, payload.length);
        gen.close();

        assertEquals(payload.length, written);
        String xml = sw.toString();
        assertTrue(xml.contains("<data>"));
        assertTrue(xml.contains("</data>"));
    }

    @Test(timeout = 4000)
    public void testBinaryStreamWithZeroBytesDefectTarget() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator gen = createWoodstoxGenerator(sw);

        InputStream in = new ByteArrayInputStream(new byte[0]);
        gen.initGenerator();
        gen.setNextName(new QName("emptyData"));
        
        int written = gen.writeBinary(in, 0);
        gen.close();

        assertEquals(0, written);
        String xml = sw.toString();
        assertTrue(xml.contains("emptyData"));
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testFeatureDefaultsAndBitmasks() {
        assertEquals(0, ToXmlGenerator.Feature.collectDefaults());
        for (ToXmlGenerator.Feature f : ToXmlGenerator.Feature.values()) {
            assertFalse(f.enabledByDefault());
            assertTrue(f.getMask() > 0);
            assertTrue(f.enabledIn(f.getMask()));
            assertFalse(f.enabledIn(0));
        }
    }

    @Test(timeout = 4000)
    public void testFeatureEnableDisableAndConfigure() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator gen = createWoodstoxGenerator(sw);

        assertFalse(gen.isEnabled(ToXmlGenerator.Feature.WRITE_XML_DECLARATION));
        gen.enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION);
        assertTrue(gen.isEnabled(ToXmlGenerator.Feature.WRITE_XML_DECLARATION));

        gen.disable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION);
        assertFalse(gen.isEnabled(ToXmlGenerator.Feature.WRITE_XML_DECLARATION));

        gen.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        assertTrue(gen.isEnabled(ToXmlGenerator.Feature.WRITE_XML_DECLARATION));

        gen.overrideFormatFeatures(0, ToXmlGenerator.Feature.WRITE_XML_DECLARATION.getMask());
        assertFalse(gen.isEnabled(ToXmlGenerator.Feature.WRITE_XML_DECLARATION));
    }

    @Test(timeout = 4000)
    public void testInitGeneratorXml10Declaration() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator gen = createWoodstoxGenerator(sw);
        gen.enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION);
        gen.initGenerator();
        // second call must be a no-op
        gen.initGenerator();

        gen.setNextName(new QName("root"));
        gen.writeStartObject();
        gen.writeEndObject();
        gen.close();

        String xml = sw.toString();
        assertTrue(xml.startsWith("<?xml version=\"1.0\""));
        assertTrue(xml.contains("<root"));
    }

    @Test(timeout = 4000)
    public void testInitGeneratorXml11Declaration() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator gen = createWoodstoxGenerator(sw);
        gen.enable(ToXmlGenerator.Feature.WRITE_XML_1_1);
        gen.initGenerator();

        gen.setNextName(new QName("root11"));
        gen.writeStartObject();
        gen.writeEndObject();
        gen.close();

        String xml = sw.toString();
        assertTrue(xml.startsWith("<?xml version=\"1.1\""));
    }

    @Test(timeout = 4000)
    public void testNestedElementsAndFieldNames() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator gen = createWoodstoxGenerator(sw);

        gen.setNextName(new QName("urn:ns", "root", "p"));
        gen.writeStartObject();

        gen.writeFieldName(new SerializedString("child"));
        gen.writeString("value");

        gen.writeStringField("simpleChild", "text");
        gen.writeEndObject();
        gen.close();

        String xml = sw.toString();
        assertTrue(xml.contains("xmlns:p=\"urn:ns\""));
        assertTrue(xml.contains("child>value</child>"));
        assertTrue(xml.contains("simpleChild>text</simpleChild>"));
    }

    @Test(timeout = 4000)
    public void testAttributesAndCData() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator gen = createWoodstoxGenerator(sw);

        gen.setNextName(new QName("item"));
        gen.writeStartObject();

        gen.setNextName(new QName("id"));
        gen.setNextIsAttribute(true);
        gen.writeString("12345");

        gen.setNextName(new QName("idChar"));
        gen.setNextIsAttribute(true);
        gen.writeString(new char[] { 'a', 'b', 'c' }, 0, 3);

        gen.setNextName(new QName("content"));
        gen.setNextIsCData(true);
        gen.writeString("raw & unescaped");

        gen.setNextName(new QName("contentChars"));
        gen.setNextIsCData(true);
        gen.writeString("more <cdata>".toCharArray(), 0, 12);

        gen.writeEndObject();
        gen.close();

        String xml = sw.toString();
        assertTrue(xml.contains("id=\"12345\""));
        assertTrue(xml.contains("idChar=\"abc\""));
        assertTrue(xml.contains("<![CDATA[raw & unescaped]]>"));
        assertTrue(xml.contains("<![CDATA[more <cdata>]]>"));
    }

    @Test(timeout = 4000)
    public void testUnwrappedElements() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator gen = createWoodstoxGenerator(sw);

        gen.setNextName(new QName("container"));
        gen.writeStartObject();

        gen.setNextIsUnwrapped(true);
        gen.writeString("unwrapped_text");

        gen.setNextIsUnwrapped(true);
        gen.writeString("unwrapped_chars".toCharArray(), 0, 15);

        gen.setNextIsUnwrapped(true);
        gen.setNextIsCData(true);
        gen.writeString("unwrapped_cdata");

        gen.setNextIsUnwrapped(true);
        gen.setNextIsCData(true);
        gen.writeString("unwrapped_cdata_chars".toCharArray(), 0, 21);

        gen.writeEndObject();
        gen.close();

        String xml = sw.toString();
        assertTrue(xml.contains("unwrapped_text"));
        assertTrue(xml.contains("unwrapped_chars"));
        assertTrue(xml.contains("<![CDATA[unwrapped_cdata]]>"));
        assertTrue(xml.contains("<![CDATA[unwrapped_cdata_chars]]>"));
    }

    @Test(timeout = 4000)
    public void testPrimitiveTypesStandardAndAttributes() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator gen = createWoodstoxGenerator(sw);

        gen.setNextName(new QName("primitives"));
        gen.writeStartObject();

        // Standard elements
        gen.setNextName(new QName("bVal"));
        gen.writeBoolean(true);

        gen.setNextName(new QName("nVal"));
        gen.writeNull();

        gen.setNextName(new QName("iVal"));
        gen.writeNumber(42);

        gen.setNextName(new QName("lVal"));
        gen.writeNumber(1234567890123L);

        gen.setNextName(new QName("dVal"));
        gen.writeNumber(12.34d);

        gen.setNextName(new QName("fVal"));
        gen.writeNumber(5.67f);

        gen.setNextName(new QName("bigDec"));
        gen.writeNumber(new BigDecimal("123.456"));

        gen.setNextName(new QName("bigInt"));
        gen.writeNumber(new BigInteger("999999999999999999"));

        gen.setNextName(new QName("encodedNum"));
        gen.writeNumber("888");

        // Attributes
        gen.setNextName(new QName("bAttr"));
        gen.setNextIsAttribute(true);
        gen.writeBoolean(false);

        gen.setNextName(new QName("nAttr"));
        gen.setNextIsAttribute(true);
        gen.writeNull(); // should be omitted

        gen.setNextName(new QName("iAttr"));
        gen.setNextIsAttribute(true);
        gen.writeNumber(101);

        gen.setNextName(new QName("lAttr"));
        gen.setNextIsAttribute(true);
        gen.writeNumber(202L);

        gen.setNextName(new QName("dAttr"));
        gen.setNextIsAttribute(true);
        gen.writeNumber(303.3);

        gen.setNextName(new QName("fAttr"));
        gen.setNextIsAttribute(true);
        gen.writeNumber(404.4f);

        gen.setNextName(new QName("decAttr"));
        gen.setNextIsAttribute(true);
        gen.writeNumber(new BigDecimal("505.5"));

        gen.setNextName(new QName("intAttr"));
        gen.setNextIsAttribute(true);
        gen.writeNumber(new BigInteger("606"));

        gen.writeEndObject();
        gen.close();

        String xml = sw.toString();
        assertTrue(xml.contains("<bVal>true</bVal>"));
        assertTrue(xml.contains("<nVal"));
        assertTrue(xml.contains("<iVal>42</iVal>"));
        assertTrue(xml.contains("<lVal>1234567890123</lVal>"));
        assertTrue(xml.contains("<dVal>12.34</dVal>"));
        assertTrue(xml.contains("<fVal>5.67</fVal>"));
        assertTrue(xml.contains("<bigDec>123.456</bigDec>"));
        assertTrue(xml.contains("<bigInt>999999999999999999</bigInt>"));
        assertTrue(xml.contains("<encodedNum>888</encodedNum>"));
        assertTrue(xml.contains("bAttr=\"false\""));
        assertFalse(xml.contains("nAttr="));
        assertTrue(xml.contains("iAttr=\"101\""));
        assertTrue(xml.contains("lAttr=\"202\""));
        assertTrue(xml.contains("dAttr=\"303.3\""));
        assertTrue(xml.contains("fAttr=\"404.4\""));
        assertTrue(xml.contains("decAttr=\"505.5\""));
        assertTrue(xml.contains("intAttr=\"606\""));
    }

    @Test(timeout = 4000)
    public void testPrimitivesUnwrapped() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator gen = createWoodstoxGenerator(sw);

        gen.setNextName(new QName("unwrappedPrimitives"));
        gen.writeStartObject();

        gen.setNextIsUnwrapped(true);
        gen.writeBoolean(true);

        gen.setNextIsUnwrapped(true);
        gen.writeNull();

        gen.setNextIsUnwrapped(true);
        gen.writeNumber(77);

        gen.setNextIsUnwrapped(true);
        gen.writeNumber(88L);

        gen.setNextIsUnwrapped(true);
        gen.writeNumber(99.9d);

        gen.setNextIsUnwrapped(true);
        gen.writeNumber(11.1f);

        gen.setNextIsUnwrapped(true);
        gen.writeNumber(new BigDecimal("22.2"));

        gen.setNextIsUnwrapped(true);
        gen.writeNumber(new BigInteger("33"));

        gen.writeEndObject();
        gen.close();

        String xml = sw.toString();
        assertTrue(xml.contains("true"));
        assertTrue(xml.contains("77"));
        assertTrue(xml.contains("88"));
        assertTrue(xml.contains("99.9"));
        assertTrue(xml.contains("11.1"));
        assertTrue(xml.contains("22.2"));
        assertTrue(xml.contains("33"));
    }

    @Test(timeout = 4000)
    public void testBigDecimalAsPlain() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator gen = createWoodstoxGenerator(sw);
        gen.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);

        gen.setNextName(new QName("plain"));
        gen.writeStartObject();

        gen.setNextName(new QName("decElem"));
        gen.writeNumber(new BigDecimal("1e-5"));

        gen.setNextName(new QName("decAttr"));
        gen.setNextIsAttribute(true);
        gen.writeNumber(new BigDecimal("2e-5"));

        gen.setNextIsUnwrapped(true);
        gen.writeNumber(new BigDecimal("3e-5"));

        gen.writeEndObject();
        gen.close();

        String xml = sw.toString();
        assertTrue(xml.contains("<decElem>0.00001</decElem>"));
        assertTrue(xml.contains("decAttr=\"0.00002\""));
        assertTrue(xml.contains("0.00003"));
    }

    @Test(timeout = 4000)
    public void testWrappedValueLifecycle() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator gen = createWoodstoxGenerator(sw);

        QName wrapper = new QName("items");
        QName item = new QName("item");

        gen.startWrappedValue(wrapper, item);
        gen.writeString("item1");
        gen.setNextName(item);
        gen.writeString("item2");
        gen.finishWrappedValue(wrapper, item);

        // wrapped value with null wrapper
        gen.startWrappedValue(null, new QName("itemStandalone"));
        gen.writeString("alone");
        gen.finishWrappedValue(null, new QName("itemStandalone"));

        gen.close();

        String xml = sw.toString();
        assertTrue(xml.contains("<items><item>item1</item><item>item2</item></items>"));
        assertTrue(xml.contains("<itemStandalone>alone</itemStandalone>"));
    }

    @Test(timeout = 4000)
    public void testArraysAndObjects() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator gen = createWoodstoxGenerator(sw);

        gen.setNextName(new QName("arrayRoot"));
        gen.writeStartArray();
        gen.setNextName(new QName("elem1"));
        gen.writeString("v1");
        gen.setNextName(new QName("elem2"));
        gen.writeString("v2");
        gen.writeEndArray();

        gen.close();
        String xml = sw.toString();
        assertTrue(xml.contains("<elem1>v1</elem1><elem2>v2</elem2>"));
    }

    @Test(timeout = 4000)
    public void testPrettyPrinterPaths() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator gen = createWoodstoxGenerator(sw);
        XmlPrettyPrinter pp = new DefaultXmlPrettyPrinter();
        gen.setPrettyPrinter(pp);

        gen.setNextName(new QName("ppRoot"));
        gen.writeStartObject();

        gen.setNextName(new QName("leaf"));
        gen.writeString("hello");

        gen.setNextName(new QName("leafChars"));
        gen.writeString("world".toCharArray(), 0, 5);

        gen.setNextName(new QName("leafNull"));
        gen.writeNull();

        gen.setNextName(new QName("leafInt"));
        gen.writeNumber(1);

        gen.setNextName(new QName("leafLong"));
        gen.writeNumber(2L);

        gen.setNextName(new QName("leafDouble"));
        gen.writeNumber(3.0);

        gen.setNextName(new QName("leafFloat"));
        gen.writeNumber(4.0f);

        gen.setNextName(new QName("leafDec"));
        gen.writeNumber(new BigDecimal("5.0"));

        gen.setNextName(new QName("leafBigInt"));
        gen.writeNumber(BigInteger.valueOf(6));

        gen.setNextName(new QName("leafBin"));
        gen.writeBinary(new byte[] { 7, 8 }, 0, 2);

        gen.setNextName(new QName("leafBool"));
        gen.writeBoolean(true);

        gen.setNextName(new QName("arr"));
        gen.writeStartArray();
        gen.writeEndArray();

        gen.startWrappedValue(new QName("wrapper"), new QName("item"));
        gen.writeString("wrapped");
        gen.finishWrappedValue(new QName("wrapper"), new QName("item"));

        gen.writeEndObject();
        gen.close();

        String xml = sw.toString();
        assertTrue(xml.contains("<ppRoot>"));
        assertTrue(xml.contains("<leaf>hello</leaf>"));
        assertTrue(xml.contains("<leafNull"));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testWriteBinaryVariations() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator gen = createWoodstoxGenerator(sw);

        gen.setNextName(new QName("binRoot"));
        gen.writeStartObject();

        // Null binary -> writeNull
        gen.setNextName(new QName("nullBin"));
        gen.writeBinary(null, 0, 0);

        // Attribute binary with offset and full length
        byte[] full = new byte[] { 1, 2, 3 };
        gen.setNextName(new QName("fullBinAttr"));
        gen.setNextIsAttribute(true);
        gen.writeBinary(full, 0, 3);

        // Attribute binary with slice (triggers toFullBuffer arraycopy)
        byte[] buffer = new byte[] { 0, 10, 20, 30, 0 };
        gen.setNextName(new QName("sliceBinAttr"));
        gen.setNextIsAttribute(true);
        gen.writeBinary(buffer, 1, 3);

        // Unwrapped binary
        gen.setNextIsUnwrapped(true);
        gen.writeBinary(full, 0, 3);

        gen.writeEndObject();
        gen.close();

        String xml = sw.toString();
        assertTrue(xml.contains("<nullBin"));
        assertTrue(xml.contains("fullBinAttr="));
        assertTrue(xml.contains("sliceBinAttr="));
    }

    @Test(timeout = 4000)
    public void testWriteRawMethodsOnNativeWriter() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator gen = createWoodstoxGenerator(sw);

        gen.setNextName(new QName("rawRoot"));
        gen.writeStartObject();

        gen.setNextName(new QName("rawValueStr"));
        gen.writeRawValue("<raw>inside</raw>");

        gen.setNextName(new QName("rawValueStrSlice"));
        gen.writeRawValue("prefix<raw>slice</raw>suffix", 6, 17);

        gen.setNextName(new QName("rawValueChars"));
        gen.writeRawValue("<raw>chars</raw>".toCharArray(), 0, 16);

        gen.writeRaw("<!-- raw comment -->");
        gen.writeRaw("<!-- slice comment -->", 0, 22);
        gen.writeRaw("<!-- char comment -->".toCharArray(), 0, 21);
        gen.writeRaw('!');

        // raw value as attribute
        gen.setNextName(new QName("rawAttr"));
        gen.setNextIsAttribute(true);
        gen.writeRawValue("attrVal");

        gen.setNextName(new QName("rawAttrSlice"));
        gen.setNextIsAttribute(true);
        gen.writeRawValue("xxattrVal2yy", 2, 8);

        gen.setNextName(new QName("rawAttrChars"));
        gen.setNextIsAttribute(true);
        gen.writeRawValue("attrVal3".toCharArray(), 0, 8);

        gen.writeEndObject();
        gen.close();

        String xml = sw.toString();
        assertTrue(xml.contains("<raw>inside</raw>"));
        assertTrue(xml.contains("<raw>slice</raw>"));
        assertTrue(xml.contains("<!-- raw comment -->"));
        assertTrue(xml.contains("rawAttr=\"attrVal\""));
        assertTrue(xml.contains("rawAttrSlice=\"attrVal2\""));
        assertTrue(xml.contains("rawAttrChars=\"attrVal3\""));
    }

    @Test(timeout = 4000)
    public void testSetNextNameIfMissing() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator gen = createWoodstoxGenerator(sw);

        assertTrue(gen.setNextNameIfMissing(new QName("first")));
        assertFalse(gen.setNextNameIfMissing(new QName("second")));

        gen.writeString("test");
        gen.close();
        assertTrue(sw.toString().contains("<first>test</first>"));
    }

    @Test(timeout = 4000)
    public void testNullNumbers() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator gen = createWoodstoxGenerator(sw);

        gen.setNextName(new QName("nullDec"));
        gen.writeNumber((BigDecimal) null);

        gen.setNextName(new QName("nullInt"));
        gen.writeNumber((BigInteger) null);

        gen.close();
        String xml = sw.toString();
        assertTrue(xml.contains("<nullDec"));
        assertTrue(xml.contains("<nullInt"));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testMissingNameThrowsException() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator gen = createWoodstoxGenerator(sw);
        gen.writeString("fails because no name was provided");
    }

    @Test(timeout = 4000, expected = JsonGenerationException.class)
    public void testWriteEndObjectWithoutStartObject() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator gen = createWoodstoxGenerator(sw);
        gen.writeEndObject();
    }

    @Test(timeout = 4000, expected = JsonGenerationException.class)
    public void testWriteEndArrayInRoot() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator gen = createWoodstoxGenerator(sw);
        gen.writeEndArray();
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testWriteRawUTF8StringUnsupported() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator gen = createWoodstoxGenerator(sw);
        gen.writeRawUTF8String(new byte[] { 1 }, 0, 1);
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testWriteUTF8StringUnsupported() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator gen = createWoodstoxGenerator(sw);
        gen.writeUTF8String(new byte[] { 1 }, 0, 1);
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testWriteRawValueSerializableUnsupported() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator gen = createWoodstoxGenerator(sw);
        gen.writeRawValue(new SerializedString("test"));
    }

    @Test(timeout = 4000)
    public void testReportUnimplementedStax2OnEmulatedWriter() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator gen = createEmulatedGenerator(sw);

        try {
            gen.writeRaw("test");
            fail("Expected JsonGenerationException for writeRaw on emulated Stax2 writer");
        } catch (JsonGenerationException e) {
            assertTrue(e.getMessage().contains("does not implement Stax2 API natively"));
        }

        try {
            gen.setNextName(new QName("elem"));
            gen.writeRawValue("val");
            fail("Expected JsonGenerationException for writeRawValue on emulated Stax2 writer");
        } catch (JsonGenerationException e) {
            assertTrue(e.getMessage().contains("does not implement Stax2 API natively"));
        }
    }

    @Test(timeout = 4000)
    public void testWriteRepeatedFieldNameErrorPath() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator gen = createWoodstoxGenerator(sw);

        gen.setNextName(new QName("root"));
        gen.writeStartObject();
        gen.setNextName(new QName("field"));
        gen.writeFieldName("field");

        try {
            // Already expecting value, cannot write another field name
            gen.writeRepeatedFieldName();
            fail("Expected JsonGenerationException for field name when expecting value");
        } catch (JsonGenerationException expected) {
            assertTrue(expected.getMessage().contains("expecting a value"));
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testLifecycleAndContextMethods() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator gen = createWoodstoxGenerator(sw);

        assertTrue(gen.inRoot());
        assertTrue(gen.canWriteFormattedNumbers());
        assertEquals(-1, gen.getOutputBuffered());
        assertNotNull(gen.getOutputTarget());
        assertTrue(gen.getStaxWriter() instanceof XMLStreamWriter2);

        gen.setNextName(new QName("root"));
        gen.writeStartObject();
        assertFalse(gen.inRoot());
        gen.writeEndObject();
        assertTrue(gen.inRoot());

        gen.close();
    }

    @Test(timeout = 4000)
    public void testAutoCloseOpenJsonContent() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator gen = createWoodstoxGenerator(sw);
        gen.enable(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT);

        gen.setNextName(new QName("unclosedRoot"));
        gen.writeStartObject();
        gen.setNextName(new QName("nestedArray"));
        gen.writeStartArray();

        // Closing generator must auto-close nested array and root object
        gen.close();

        String xml = sw.toString();
        assertTrue(xml.contains("<unclosedRoot>"));
        assertTrue(xml.contains("</unclosedRoot>"));
    }

    @Test(timeout = 4000)
    public void testFlushPassedToStreamFeature() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator gen = createWoodstoxGenerator(sw);

        gen.disable(JsonGenerator.Feature.FLUSH_PASSED_TO_STREAM);
        gen.flush();

        gen.enable(JsonGenerator.Feature.FLUSH_PASSED_TO_STREAM);
        gen.flush();

        gen.close();
    }
}