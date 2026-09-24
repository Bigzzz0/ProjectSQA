package com.fasterxml.jackson.databind.jsontype.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.util.JsonParserDelegate;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;

/*
 [Branch & Defect Analysis Matrix]
 -------------------------------------------------------------------------------------------------------------------
 Decision / Branch Point                         | Test Method(s)
 -------------------------------------------------------------------------------------------------------------------
 1. canReadTypeId() == true && typeId != null    | testDeserializeWithNativeTypeIdNonNull
 2. canReadTypeId() == true && typeId == null    | testDeserializeWithNativeTypeIdNullFallsBack
 3. currentToken == START_OBJECT (normal path)   | testDeserializeTypedFromObject, testDeserializeTypedFromScalar, etc.
 4. currentToken == FIELD_NAME (Defect Target!)  | testDeserializeWhenParserAlreadyAtFieldName, testDefectWrapperObjectWithObjectId
 5. currentToken != START_OBJECT & != FIELD_NAME | testWrongTokenExceptionNotObjectOrFieldName
 6. nextToken() != FIELD_NAME (e.g. empty {})    | testWrongTokenExceptionEmptyObjectNoTypeId
 7. _typeIdVisible == true && START_OBJECT       | testTypeIdVisibleWithObject
 8. _typeIdVisible == true && !START_OBJECT      | testTypeIdVisibleWithScalarSkipsTokenBuffer
 9. nextToken() != END_OBJECT (trailing token)   | testWrongTokenExceptionMissingClosingEndObject
 10. forProperty(prop == _property) vs new prop  | testForPropertyContract
 11. Serialization & lifecycle integrity         | testSerializationAndTypeInclusion
 -------------------------------------------------------------------------------------------------------------------
*/

public class AsWrapperTypeDeserializerGeminiTest {

    // --- Test Fixtures ---

    public static class BaseClass {
    }

    public static class SubClass extends BaseClass {
        public int val;
        public String type;

        public SubClass() {}
        public SubClass(int val) { this.val = val; }
    }

    public static class ScalarHolder extends BaseClass {
        public String text;

        @JsonCreator
        public ScalarHolder(String text) {
            this.text = text;
        }
    }

    public static class ArrayHolder extends BaseClass {
        public List<String> items;

        @JsonCreator
        public ArrayHolder(List<String> items) {
            this.items = items;
        }
    }

    // Fixtures targeting Defects4J defect: WrapperObjectWithObjectIdTest
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
    @JsonSubTypes({@JsonSubTypes.Type(value = Computer.class, name = "computer")})
    public static class Computer {
        public List<Display> displays = new ArrayList<Display>();
        public Computer() {}
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
    @JsonSubTypes({@JsonSubTypes.Type(value = Display.class, name = "display")})
    public static class Display {
        public Computer computer;
        public Display() {}
    }

    // Custom ParserDelegate to simulate native type IDs
    private static class NativeTypeIdParser extends JsonParserDelegate {
        private final Object nativeTypeId;

        public NativeTypeIdParser(JsonParser p, Object nativeTypeId) {
            super(p);
            this.nativeTypeId = nativeTypeId;
        }

        @Override
        public boolean canReadTypeId() {
            return true;
        }

        @Override
        public Object getTypeId() {
            return nativeTypeId;
        }
    }

    private AsWrapperTypeDeserializer createDeserializer(ObjectMapper mapper, JavaType baseType,
                                                         boolean typeIdVisible, Class<?> defaultImpl) {
        ClassNameIdResolver idRes = new ClassNameIdResolver(baseType, mapper.getTypeFactory());
        return new AsWrapperTypeDeserializer(baseType, idRes, "type", typeIdVisible, defaultImpl);
    }

    private DefaultDeserializationContext createDeserializationContext(ObjectMapper mapper, JsonParser p) {
        return ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(mapper.getDeserializationConfig(), p, null);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Direct defect trigger:
     * When deserialization begins with the parser already positioned at FIELD_NAME (the type id),
     * AsWrapperTypeDeserializer must not reject it with an unexpected token exception.
     */
    @Test(timeout = 4000)
    public void testDeserializeWhenParserAlreadyAtFieldName() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType baseType = mapper.constructType(BaseClass.class);
        AsWrapperTypeDeserializer typeDeser = createDeserializer(mapper, baseType, false, null);

        String json = "{\"" + SubClass.class.getName() + "\":{\"val\":99}}";
        JsonParser p = mapper.getFactory().createParser(json);
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken()); // Parser advanced to FIELD_NAME

        DefaultDeserializationContext ctxt = createDeserializationContext(mapper, p);
        Object result = typeDeser.deserializeTypedFromObject(p, ctxt);

        assertNotNull("Expected deserialized instance", result);
        assertTrue("Expected instance of SubClass", result instanceof SubClass);
        assertEquals(99, ((SubClass) result).val);
        p.close();
    }

    /**
     * Full integration defect trigger from Defects4J:
     * WrapperObjectWithObjectIdTest::testSimple fails with:
     * Unexpected token (FIELD_NAME), expected START_OBJECT: need JSON Object to contain As.WRAPPER_OBJECT
     */
    @Test(timeout = 4000)
    public void testDefectWrapperObjectWithObjectId() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Computer c = new Computer();
        Display d = new Display();
        c.displays.add(d);
        d.computer = c;

        String json = mapper.writeValueAsString(c);
        Computer c2 = mapper.readValue(json, Computer.class);

        assertNotNull("Computer should deserialize successfully", c2);
        assertEquals(1, c2.displays.size());
        assertNotNull(c2.displays.get(0));
        assertSame("Circular reference should resolve to the same root instance", c2, c2.displays.get(0).computer);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDeserializeTypedFromObject() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType baseType = mapper.constructType(BaseClass.class);
        AsWrapperTypeDeserializer typeDeser = createDeserializer(mapper, baseType, false, null);

        String json = "{\"" + SubClass.class.getName() + "\":{\"val\":42}}";
        JsonParser p = mapper.getFactory().createParser(json);
        p.nextToken(); // At START_OBJECT

        DefaultDeserializationContext ctxt = createDeserializationContext(mapper, p);
        Object result = typeDeser.deserializeTypedFromObject(p, ctxt);

        assertNotNull(result);
        assertTrue(result instanceof SubClass);
        assertEquals(42, ((SubClass) result).val);
        p.close();
    }

    @Test(timeout = 4000)
    public void testDeserializeTypedFromScalar() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType baseType = mapper.constructType(BaseClass.class);
        AsWrapperTypeDeserializer typeDeser = createDeserializer(mapper, baseType, false, null);

        String json = "{\"" + ScalarHolder.class.getName() + "\":\"sample text\"}";
        JsonParser p = mapper.getFactory().createParser(json);
        p.nextToken(); // At START_OBJECT

        DefaultDeserializationContext ctxt = createDeserializationContext(mapper, p);
        Object result = typeDeser.deserializeTypedFromScalar(p, ctxt);

        assertNotNull(result);
        assertTrue(result instanceof ScalarHolder);
        assertEquals("sample text", ((ScalarHolder) result).text);
        p.close();
    }

    @Test(timeout = 4000)
    public void testDeserializeTypedFromArray() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType baseType = mapper.constructType(BaseClass.class);
        AsWrapperTypeDeserializer typeDeser = createDeserializer(mapper, baseType, false, null);

        String json = "{\"" + ArrayHolder.class.getName() + "\":[\"item1\",\"item2\"]}";
        JsonParser p = mapper.getFactory().createParser(json);
        p.nextToken(); // At START_OBJECT

        DefaultDeserializationContext ctxt = createDeserializationContext(mapper, p);
        Object result = typeDeser.deserializeTypedFromArray(p, ctxt);

        assertNotNull(result);
        assertTrue(result instanceof ArrayHolder);
        assertEquals(2, ((ArrayHolder) result).items.size());
        assertEquals("item1", ((ArrayHolder) result).items.get(0));
        assertEquals("item2", ((ArrayHolder) result).items.get(1));
        p.close();
    }

    @Test(timeout = 4000)
    public void testDeserializeTypedFromAny() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType baseType = mapper.constructType(BaseClass.class);
        AsWrapperTypeDeserializer typeDeser = createDeserializer(mapper, baseType, false, null);

        String json = "{\"" + SubClass.class.getName() + "\":{\"val\":100}}";
        JsonParser p = mapper.getFactory().createParser(json);
        p.nextToken(); // At START_OBJECT

        DefaultDeserializationContext ctxt = createDeserializationContext(mapper, p);
        Object result = typeDeser.deserializeTypedFromAny(p, ctxt);

        assertNotNull(result);
        assertTrue(result instanceof SubClass);
        assertEquals(100, ((SubClass) result).val);
        p.close();
    }

    @Test(timeout = 4000)
    public void testTypeIdVisibleWithObject() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType baseType = mapper.constructType(BaseClass.class);
        // typeIdVisible = true
        AsWrapperTypeDeserializer typeDeser = createDeserializer(mapper, baseType, true, null);

        String typeId = SubClass.class.getName();
        String json = "{\"" + typeId + "\":{\"val\":77}}";
        JsonParser p = mapper.getFactory().createParser(json);
        p.nextToken();

        DefaultDeserializationContext ctxt = createDeserializationContext(mapper, p);
        Object result = typeDeser.deserializeTypedFromObject(p, ctxt);

        assertNotNull(result);
        assertTrue(result instanceof SubClass);
        SubClass sub = (SubClass) result;
        assertEquals(77, sub.val);
        assertEquals("Type property should have been populated", typeId, sub.type);
        p.close();
    }

    @Test(timeout = 4000)
    public void testTypeIdVisibleWithScalarSkipsTokenBuffer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType baseType = mapper.constructType(BaseClass.class);
        // typeIdVisible = true, but value is scalar string (cannot inject type id property)
        AsWrapperTypeDeserializer typeDeser = createDeserializer(mapper, baseType, true, null);

        String json = "{\"" + ScalarHolder.class.getName() + "\":\"hello\"}";
        JsonParser p = mapper.getFactory().createParser(json);
        p.nextToken();

        DefaultDeserializationContext ctxt = createDeserializationContext(mapper, p);
        Object result = typeDeser.deserializeTypedFromScalar(p, ctxt);

        assertNotNull(result);
        assertTrue(result instanceof ScalarHolder);
        assertEquals("hello", ((ScalarHolder) result).text);
        p.close();
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Error Token Branches
    // =========================================================================

    @Test(timeout = 4000)
    public void testWrongTokenExceptionNotObjectOrFieldName() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType baseType = mapper.constructType(BaseClass.class);
        AsWrapperTypeDeserializer typeDeser = createDeserializer(mapper, baseType, false, null);

        // Parser starts at START_ARRAY instead of START_OBJECT
        String json = "[\"invalid\"]";
        JsonParser p = mapper.getFactory().createParser(json);
        p.nextToken(); // At START_ARRAY

        DefaultDeserializationContext ctxt = createDeserializationContext(mapper, p);
        try {
            typeDeser.deserializeTypedFromObject(p, ctxt);
            fail("Expected JsonMappingException for non-object token");
        } catch (JsonMappingException e) {
            assertTrue("Exception message should state need JSON Object",
                    e.getMessage().contains("need JSON Object to contain As.WRAPPER_OBJECT"));
        } finally {
            p.close();
        }
    }

    @Test(timeout = 4000)
    public void testWrongTokenExceptionEmptyObjectNoTypeId() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType baseType = mapper.constructType(BaseClass.class);
        AsWrapperTypeDeserializer typeDeser = createDeserializer(mapper, baseType, false, null);

        // Empty object: START_OBJECT followed immediately by END_OBJECT (no FIELD_NAME)
        String json = "{}";
        JsonParser p = mapper.getFactory().createParser(json);
        p.nextToken(); // At START_OBJECT

        DefaultDeserializationContext ctxt = createDeserializationContext(mapper, p);
        try {
            typeDeser.deserializeTypedFromObject(p, ctxt);
            fail("Expected JsonMappingException for missing type id field");
        } catch (JsonMappingException e) {
            assertTrue("Exception message should indicate missing type id string",
                    e.getMessage().contains("need JSON String that contains type id"));
        } finally {
            p.close();
        }
    }

    @Test(timeout = 4000)
    public void testWrongTokenExceptionMissingClosingEndObject() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType baseType = mapper.constructType(BaseClass.class);
        AsWrapperTypeDeserializer typeDeser = createDeserializer(mapper, baseType, false, null);

        // Wrapper object contains a second extra field instead of closing END_OBJECT
        String json = "{\"" + SubClass.class.getName() + "\":{\"val\":10},\"extra\":999}";
        JsonParser p = mapper.getFactory().createParser(json);
        p.nextToken(); // At START_OBJECT

        DefaultDeserializationContext ctxt = createDeserializationContext(mapper, p);
        try {
            typeDeser.deserializeTypedFromObject(p, ctxt);
            fail("Expected JsonMappingException for missing closing END_OBJECT");
        } catch (JsonMappingException e) {
            assertTrue("Exception message should mention expected closing END_OBJECT",
                    e.getMessage().contains("expected closing END_OBJECT"));
        } finally {
            p.close();
        }
    }

    // =========================================================================
    // Partition D: Native Type ID & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testDeserializeWithNativeTypeIdNonNull() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType baseType = mapper.constructType(BaseClass.class);
        AsWrapperTypeDeserializer typeDeser = createDeserializer(mapper, baseType, false, null);

        String json = "{\"val\":88}";
        JsonParser baseParser = mapper.getFactory().createParser(json);
        baseParser.nextToken(); // At START_OBJECT

        NativeTypeIdParser p = new NativeTypeIdParser(baseParser, SubClass.class.getName());
        DefaultDeserializationContext ctxt = createDeserializationContext(mapper, p);

        Object result = typeDeser.deserializeTypedFromObject(p, ctxt);
        assertNotNull(result);
        assertTrue(result instanceof SubClass);
        assertEquals(88, ((SubClass) result).val);
        p.close();
    }

    @Test(timeout = 4000)
    public void testDeserializeWithNativeTypeIdNullFallsBack() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType baseType = mapper.constructType(BaseClass.class);
        AsWrapperTypeDeserializer typeDeser = createDeserializer(mapper, baseType, false, null);

        String json = "{\"" + SubClass.class.getName() + "\":{\"val\":55}}";
        JsonParser baseParser = mapper.getFactory().createParser(json);
        baseParser.nextToken(); // At START_OBJECT

        // canReadTypeId returns true, but getTypeId() returns null -> must fallback to normal parsing
        NativeTypeIdParser p = new NativeTypeIdParser(baseParser, null);
        DefaultDeserializationContext ctxt = createDeserializationContext(mapper, p);

        Object result = typeDeser.deserializeTypedFromObject(p, ctxt);
        assertNotNull(result);
        assertTrue(result instanceof SubClass);
        assertEquals(55, ((SubClass) result).val);
        p.close();
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testForPropertyContract() {
        ObjectMapper mapper = new ObjectMapper();
        JavaType baseType = mapper.constructType(BaseClass.class);
        AsWrapperTypeDeserializer typeDeser = createDeserializer(mapper, baseType, false, null);

        // prop == _property (both null) returns this
        TypeDeserializer same = typeDeser.forProperty(null);
        assertSame("forProperty with same property reference must return this", typeDeser, same);

        // prop != _property returns new instance
        BeanProperty prop = new BeanProperty.Bogus();
        TypeDeserializer contextual = typeDeser.forProperty(prop);
        assertNotSame("forProperty with different property must return a new instance", typeDeser, contextual);
        assertSame(prop, contextual.getProperty());

        // second call on contextual with same prop must return contextual
        TypeDeserializer contextualSame = contextual.forProperty(prop);
        assertSame("forProperty with matching property on contextual instance must return this",
                contextual, contextualSame);
    }

    @Test(timeout = 4000)
    public void testSerializationAndTypeInclusion() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType baseType = mapper.constructType(BaseClass.class);
        AsWrapperTypeDeserializer typeDeser = createDeserializer(mapper, baseType, true, SubClass.class);

        assertEquals(JsonTypeInfo.As.WRAPPER_OBJECT, typeDeser.getTypeInclusion());
        assertEquals(SubClass.class, typeDeser.getDefaultImpl());

        // Verify java.io.Serializable implementation
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(typeDeser);
        oos.flush();

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        Object deserializedObj = ois.readObject();

        assertNotNull(deserializedObj);
        assertTrue(deserializedObj instanceof AsWrapperTypeDeserializer);
        AsWrapperTypeDeserializer roundtripped = (AsWrapperTypeDeserializer) deserializedObj;
        assertEquals(JsonTypeInfo.As.WRAPPER_OBJECT, roundtripped.getTypeInclusion());
    }
}