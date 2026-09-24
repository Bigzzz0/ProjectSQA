package com.fasterxml.jackson.databind.deser.std;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.fasterxml.jackson.databind.deser.std.NullifyingDeserializer
 *
 * Decision / Branch Coverage:
 * 1. deserialize(JsonParser p, DeserializationContext ctxt):
 *    - p.currentToken() == START_OBJECT, START_ARRAY, scalar values -> p.skipChildren() and returns null.
 *    - p.currentToken() == FIELD_NAME -> Defect point! When parser points to FIELD_NAME (e.g. unknown
 *      type recovery where type property was consumed), p.skipChildren() alone fails to consume the
 *      subsequent object fields, causing UnrecognizedPropertyException on enclosing beans.
 * 2. deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer):
 *    - case ID_START_ARRAY  -> typeDeserializer.deserializeTypedFromAny(p, ctxt)
 *    - case ID_START_OBJECT -> typeDeserializer.deserializeTypedFromAny(p, ctxt)
 *    - case ID_FIELD_NAME   -> typeDeserializer.deserializeTypedFromAny(p, ctxt)
 *    - default              -> returns null (tested with ID_STRING, ID_NUMBER_INT, ID_TRUE, ID_NULL)
 * 3. Lifecycle & Contract:
 *    - NullifyingDeserializer.instance singleton validity.
 *    - Explicit constructor instantiation & handledType validation (Object.class).
 */
public class NullifyingDeserializerGeminiTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final JsonFactory factory = new JsonFactory();

    // -------------------------------------------------------------------------
    // Stub TypeDeserializer for deterministic unit testing of deserializeWithType
    // -------------------------------------------------------------------------
    private static class StubTypeDeserializer extends TypeDeserializer {
        boolean calledTypedFromAny = false;

        @Override
        public TypeDeserializer forProperty(BeanProperty prop) {
            return this;
        }

        @Override
        public JsonTypeInfo.As getTypeInclusion() {
            return JsonTypeInfo.As.PROPERTY;
        }

        @Override
        public String getPropertyName() {
            return "@type";
        }

        @Override
        public TypeIdResolver getTypeIdResolver() {
            return null;
        }

        @Override
        public Class<?> getDefaultImpl() {
            return Void.class;
        }

        @Override
        public Object deserializeTypedFromObject(JsonParser p, DeserializationContext ctxt) {
            return null;
        }

        @Override
        public Object deserializeTypedFromArray(JsonParser p, DeserializationContext ctxt) {
            return null;
        }

        @Override
        public Object deserializeTypedFromScalar(JsonParser p, DeserializationContext ctxt) {
            return null;
        }

        @Override
        public Object deserializeTypedFromAny(JsonParser p, DeserializationContext ctxt) {
            calledTypedFromAny = true;
            return "invoked-deserializeTypedFromAny";
        }
    }

    // -------------------------------------------------------------------------
    // Domain Models for Defect Reproduction (Defects4J Unknown Type ID Recovery)
    // -------------------------------------------------------------------------
    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "@type",
        defaultImpl = Void.class
    )
    @JsonSubTypes({})
    static class Item {
        public String name;
    }

    static class CallRecord {
        public int version;
        public Item item;
        public Item item2;
        public String application;
    }

    /*
    /**********************************************************
    /* Partition A: Core Functional Logic & State Transitions
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testDeserializeNormalObjectReturnsNull() throws Exception {
        JsonParser p = factory.createParser("{\"key\": \"value\", \"nested\": [1, 2, 3]}");
        assertNotNull(p.nextToken()); // START_OBJECT

        Object result = NullifyingDeserializer.instance.deserialize(p, mapper.getDeserializationContext());
        assertNull(result);
    }

    @Test(timeout = 4000)
    public void testDeserializeArrayReturnsNull() throws Exception {
        JsonParser p = factory.createParser("[1, 2, 3, {\"nested\": true}]");
        assertNotNull(p.nextToken()); // START_ARRAY

        Object result = NullifyingDeserializer.instance.deserialize(p, mapper.getDeserializationContext());
        assertNull(result);
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeStartObjectBranch() throws Exception {
        JsonParser p = factory.createParser("{\"foo\": \"bar\"}");
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonTokenId.ID_START_OBJECT, p.getCurrentTokenId());

        StubTypeDeserializer td = new StubTypeDeserializer();
        Object result = NullifyingDeserializer.instance.deserializeWithType(p, mapper.getDeserializationContext(), td);

        assertTrue(td.calledTypedFromAny);
        assertEquals("invoked-deserializeTypedFromAny", result);
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeStartArrayBranch() throws Exception {
        JsonParser p = factory.createParser("[\"item1\", \"item2\"]");
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonTokenId.ID_START_ARRAY, p.getCurrentTokenId());

        StubTypeDeserializer td = new StubTypeDeserializer();
        Object result = NullifyingDeserializer.instance.deserializeWithType(p, mapper.getDeserializationContext(), td);

        assertTrue(td.calledTypedFromAny);
        assertEquals("invoked-deserializeTypedFromAny", result);
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeFieldNameBranch() throws Exception {
        JsonParser p = factory.createParser("{\"subField\": 123}");
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals(JsonTokenId.ID_FIELD_NAME, p.getCurrentTokenId());

        StubTypeDeserializer td = new StubTypeDeserializer();
        Object result = NullifyingDeserializer.instance.deserializeWithType(p, mapper.getDeserializationContext(), td);

        assertTrue(td.calledTypedFromAny);
        assertEquals("invoked-deserializeTypedFromAny", result);
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeDefaultBranchReturnsNullForScalars() throws Exception {
        StubTypeDeserializer td = new StubTypeDeserializer();

        // String scalar
        JsonParser pString = factory.createParser("\"hello\"");
        assertEquals(JsonToken.VALUE_STRING, pString.nextToken());
        Object resultString = NullifyingDeserializer.instance.deserializeWithType(pString, mapper.getDeserializationContext(), td);
        assertNull(resultString);
        assertFalse(td.calledTypedFromAny);

        // Int scalar
        JsonParser pInt = factory.createParser("42");
        assertEquals(JsonToken.VALUE_NUMBER_INT, pInt.nextToken());
        Object resultInt = NullifyingDeserializer.instance.deserializeWithType(pInt, mapper.getDeserializationContext(), td);
        assertNull(resultInt);

        // Boolean scalar
        JsonParser pBool = factory.createParser("true");
        assertEquals(JsonToken.VALUE_TRUE, pBool.nextToken());
        Object resultBool = NullifyingDeserializer.instance.deserializeWithType(pBool, mapper.getDeserializationContext(), td);
        assertNull(resultBool);

        // Null scalar
        JsonParser pNull = factory.createParser("null");
        assertEquals(JsonToken.VALUE_NULL, pNull.nextToken());
        Object resultNull = NullifyingDeserializer.instance.deserializeWithType(pNull, mapper.getDeserializationContext(), td);
        assertNull(resultNull);
    }

    /*
    /**********************************************************
    /* Partition B: Boundary Value Analysis (BVA) & Extremes
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testDeserializeEmptyObjectAndArray() throws Exception {
        JsonParser pObj = factory.createParser("{}");
        assertEquals(JsonToken.START_OBJECT, pObj.nextToken());
        assertNull(NullifyingDeserializer.instance.deserialize(pObj, mapper.getDeserializationContext()));

        JsonParser pArr = factory.createParser("[]");
        assertEquals(JsonToken.START_ARRAY, pArr.nextToken());
        assertNull(NullifyingDeserializer.instance.deserialize(pArr, mapper.getDeserializationContext()));
    }

    @Test(timeout = 4000)
    public void testDeserializeScalarBoundaries() throws Exception {
        // Integer MAX boundary
        JsonParser pMaxInt = factory.createParser(String.valueOf(Integer.MAX_VALUE));
        assertEquals(JsonToken.VALUE_NUMBER_INT, pMaxInt.nextToken());
        assertNull(NullifyingDeserializer.instance.deserialize(pMaxInt, mapper.getDeserializationContext()));

        // Empty String boundary
        JsonParser pEmptyStr = factory.createParser("\"\"");
        assertEquals(JsonToken.VALUE_STRING, pEmptyStr.nextToken());
        assertNull(NullifyingDeserializer.instance.deserialize(pEmptyStr, mapper.getDeserializationContext()));
    }

    /*
    /**********************************************************
    /* Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    /**********************************************************
     */

    /**
     * Fault-revealing test: Targets the defect where NullifyingDeserializer.deserialize
     * fails when the JsonParser is currently pointing at a FIELD_NAME token.
     * When recovering from an unknown polymorphic type ID (with defaultImpl = Void.class),
     * the type property was consumed, placing the parser directly at the next FIELD_NAME.
     * Calling skipChildren() on FIELD_NAME in the buggy version does not consume the remaining
     * object properties, bleeding "location" into CallRecord and throwing UnrecognizedPropertyException.
     */
    @Test(timeout = 4000)
    public void testDefectUnknownTypeIDRecoveryWithDefaultImpl() throws Exception {
        String json = "{\n"
                + "  \"version\": 1,\n"
                + "  \"item\": {\n"
                + "    \"@type\": \"bogus\",\n"
                + "    \"name\": \"foo\",\n"
                + "    \"location\": \"bar\"\n"
                + "  },\n"
                + "  \"item2\": null,\n"
                + "  \"application\": \"test\"\n"
                + "}";

        CallRecord record = mapper.readValue(json, CallRecord.class);

        assertNotNull("Record should be successfully deserialized despite unknown polymorphic type ID", record);
        assertEquals(1, record.version);
        assertNull("Polymorphic property with Void defaultImpl must be null", record.item);
        assertNull("Item2 must be null", record.item2);
        assertEquals("test", record.application);
    }

    @Test(timeout = 4000)
    public void testDefectSkipWhenParserPositionedAtFieldName() throws Exception {
        JsonParser p = factory.createParser("{\"field1\": \"val1\", \"field2\": 999}");
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("field1", p.getCurrentName());

        // In the buggy implementation, calling deserialize here does p.skipChildren(), which is a no-op
        // on FIELD_NAME, failing to advance to END_OBJECT.
        Object result = NullifyingDeserializer.instance.deserialize(p, mapper.getDeserializationContext());
        assertNull(result);

        // Expected correct behavior: parser should consume the object tokens up to END_OBJECT
        assertEquals(JsonToken.END_OBJECT, p.getCurrentToken());
    }

    /*
    /**********************************************************
    /* Partition D: Exception & Defensive Guard Paths
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testDeserializeWithTypeNullParserThrowsNullPointerException() throws IOException {
        StubTypeDeserializer td = new StubTypeDeserializer();
        try {
            NullifyingDeserializer.instance.deserializeWithType(null, mapper.getDeserializationContext(), td);
            fail("Expected NullPointerException when JsonParser is null");
        } catch (NullPointerException expected) {
            // Success
        }
    }

    @Test(timeout = 4000)
    public void testDeserializeNullParserThrowsNullPointerException() throws IOException {
        try {
            NullifyingDeserializer.instance.deserialize(null, mapper.getDeserializationContext());
            fail("Expected NullPointerException when JsonParser is null");
        } catch (NullPointerException expected) {
            // Success
        }
    }

    /*
    /**********************************************************
    /* Partition E: Object Lifecycle & Contract Integrity
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testConstructorAndHandledType() {
        NullifyingDeserializer customInstance = new NullifyingDeserializer();
        assertNotNull(customInstance);
        assertEquals(Object.class, customInstance.handledType());

        assertNotNull(NullifyingDeserializer.instance);
        assertEquals(Object.class, NullifyingDeserializer.instance.handledType());
    }
}