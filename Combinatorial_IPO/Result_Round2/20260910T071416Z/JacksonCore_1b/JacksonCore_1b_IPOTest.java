package com.fasterxml.jackson.core;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.math.BigDecimal;

/** Generated from approved defect-focused scenarios using native IPO. */
public class JacksonCore_1b_IPOTest {

    @Test(timeout = 4000)
    public void test_non_numeric_decimal_001() throws Exception {
        // Native IPO combination: backend=reader, token=nan, feature_configuration=factory
        JsonFactory factory = new JsonFactory();
        boolean configureFactory = true;
        if (configureFactory) { factory.enable(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS); }
        String json = "[ " + "NaN" + " ]";
        JsonParser parser = factory.createParser(new StringReader(json));
        if (!configureFactory) { parser.enable(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS); }
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        try {
            parser.getDecimalValue();
            fail("Expected non-finite value to be rejected as BigDecimal");
        } catch (NumberFormatException expected) {
            assertTrue(expected.getMessage().indexOf("BigDecimal") >= 0);
        } finally {
            parser.close();
        }
    }

    @Test(timeout = 4000)
    public void test_non_numeric_decimal_002() throws Exception {
        // Native IPO combination: backend=reader, token=positive_infinity, feature_configuration=parser
        JsonFactory factory = new JsonFactory();
        boolean configureFactory = false;
        if (configureFactory) { factory.enable(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS); }
        String json = "[ " + "+Infinity" + " ]";
        JsonParser parser = factory.createParser(new StringReader(json));
        if (!configureFactory) { parser.enable(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS); }
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        try {
            parser.getDecimalValue();
            fail("Expected non-finite value to be rejected as BigDecimal");
        } catch (NumberFormatException expected) {
            assertTrue(expected.getMessage().indexOf("BigDecimal") >= 0);
        } finally {
            parser.close();
        }
    }

    @Test(timeout = 4000)
    public void test_non_numeric_decimal_003() throws Exception {
        // Native IPO combination: backend=reader, token=negative_infinity, feature_configuration=factory
        JsonFactory factory = new JsonFactory();
        boolean configureFactory = true;
        if (configureFactory) { factory.enable(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS); }
        String json = "[ " + "-Infinity" + " ]";
        JsonParser parser = factory.createParser(new StringReader(json));
        if (!configureFactory) { parser.enable(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS); }
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        try {
            parser.getDecimalValue();
            fail("Expected non-finite value to be rejected as BigDecimal");
        } catch (NumberFormatException expected) {
            assertTrue(expected.getMessage().indexOf("BigDecimal") >= 0);
        } finally {
            parser.close();
        }
    }

    @Test(timeout = 4000)
    public void test_non_numeric_decimal_004() throws Exception {
        // Native IPO combination: backend=stream, token=nan, feature_configuration=parser
        JsonFactory factory = new JsonFactory();
        boolean configureFactory = false;
        if (configureFactory) { factory.enable(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS); }
        String json = "[ " + "NaN" + " ]";
        JsonParser parser = factory.createParser(new ByteArrayInputStream(json.getBytes("UTF-8")));
        if (!configureFactory) { parser.enable(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS); }
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        try {
            parser.getDecimalValue();
            fail("Expected non-finite value to be rejected as BigDecimal");
        } catch (NumberFormatException expected) {
            assertTrue(expected.getMessage().indexOf("BigDecimal") >= 0);
        } finally {
            parser.close();
        }
    }

    @Test(timeout = 4000)
    public void test_non_numeric_decimal_005() throws Exception {
        // Native IPO combination: backend=stream, token=positive_infinity, feature_configuration=factory
        JsonFactory factory = new JsonFactory();
        boolean configureFactory = true;
        if (configureFactory) { factory.enable(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS); }
        String json = "[ " + "+Infinity" + " ]";
        JsonParser parser = factory.createParser(new ByteArrayInputStream(json.getBytes("UTF-8")));
        if (!configureFactory) { parser.enable(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS); }
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        try {
            parser.getDecimalValue();
            fail("Expected non-finite value to be rejected as BigDecimal");
        } catch (NumberFormatException expected) {
            assertTrue(expected.getMessage().indexOf("BigDecimal") >= 0);
        } finally {
            parser.close();
        }
    }

    @Test(timeout = 4000)
    public void test_non_numeric_decimal_006() throws Exception {
        // Native IPO combination: backend=stream, token=negative_infinity, feature_configuration=parser
        JsonFactory factory = new JsonFactory();
        boolean configureFactory = false;
        if (configureFactory) { factory.enable(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS); }
        String json = "[ " + "-Infinity" + " ]";
        JsonParser parser = factory.createParser(new ByteArrayInputStream(json.getBytes("UTF-8")));
        if (!configureFactory) { parser.enable(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS); }
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        try {
            parser.getDecimalValue();
            fail("Expected non-finite value to be rejected as BigDecimal");
        } catch (NumberFormatException expected) {
            assertTrue(expected.getMessage().indexOf("BigDecimal") >= 0);
        } finally {
            parser.close();
        }
    }

    @Test(timeout = 4000)
    public void test_non_numeric_decimal_007() throws Exception {
        // Native IPO combination: backend=stream, token=nan, feature_configuration=factory
        JsonFactory factory = new JsonFactory();
        boolean configureFactory = true;
        if (configureFactory) { factory.enable(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS); }
        String json = "[ " + "NaN" + " ]";
        JsonParser parser = factory.createParser(new ByteArrayInputStream(json.getBytes("UTF-8")));
        if (!configureFactory) { parser.enable(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS); }
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        try {
            parser.getDecimalValue();
            fail("Expected non-finite value to be rejected as BigDecimal");
        } catch (NumberFormatException expected) {
            assertTrue(expected.getMessage().indexOf("BigDecimal") >= 0);
        } finally {
            parser.close();
        }
    }

}
