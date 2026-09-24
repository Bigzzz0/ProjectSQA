package com.fasterxml.jackson.databind.jsontype.impl;

/* [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------------
 * Target Class: com.fasterxml.jackson.databind.jsontype.impl.AsPropertyTypeDeserializer
 * Extends: AsArrayTypeDeserializer
 * Inclusion Mechanism: JsonTypeInfo.As.PROPERTY (or As.EXISTING_PROPERTY)
 *
 * Branch & Decision Coverage Map:
 * 1. Constructor variants:
 *    - (JavaType, TypeIdResolver, String, boolean, JavaType) -> defaults to As.PROPERTY
 *    - (JavaType, TypeIdResolver, String, boolean, JavaType, As) -> custom inclusion (e.g., As.EXISTING_PROPERTY)
 *    - Copy constructor (AsPropertyTypeDeserializer, BeanProperty)
 * 2. forProperty(BeanProperty prop):
 *    - Branch: prop == _property -> returns this (same instance)
 *    - Branch: prop != _property -> returns new AsPropertyTypeDeserializer(this, prop)
 * 3. getTypeInclusion():
 *    - Returns _inclusion (As.PROPERTY or configured As)
 * 4. deserializeTypedFromObject(JsonParser, DeserializationContext):
 *    - Branch: p.canReadTypeId() == true && p.getTypeId() != null -> _deserializeWithNativeTypeId
 *    - Branch: p.canReadTypeId() == false || p.getTypeId() == null
 *    - Token check:
 *      * p.getCurrentToken() == JsonToken.START_OBJECT -> advances to next token
 *      * t != JsonToken.FIELD_NAME (e.g., VALUE_STRING, START_ARRAY) -> _deserializeTypedUsingDefaultImpl(p, ctxt, null)
 *    - Field scanning loop (t == JsonToken.FIELD_NAME):
 *      * Field name equals _typePropertyName -> returns _deserializeTypedForId(p, ctxt, tb)
 *      * Field name != _typePropertyName:
 *        - tb == null -> instantiates TokenBuffer(p, ctxt)
 *        - tb.writeFieldName(name), tb.copyCurrentStructure(p)
 *    - Fallthrough after loop (type id property not found in object):
 *      * returns _deserializeTypedUsingDefaultImpl(p, ctxt, tb)
 * 5. _deserializeTypedForId(JsonParser, DeserializationContext, TokenBuffer):
 *    - typeId = p.getText()
 *    - _typeIdVisible == true -> writes fieldName and string typeId back into TokenBuffer
 *    - _typeIdVisible == false -> does not write typeId to TokenBuffer
 *    - tb != null -> creates JsonParserSequence.createFlattened(false, tb.asParser(p), p)
 *    - advances past type id value token (p.nextToken())
 *    - invokes deser.deserialize(p, ctxt)
 * 6. _deserializeTypedUsingDefaultImpl(JsonParser, DeserializationContext, TokenBuffer):
 *    - deser = _findDefaultImplDeserializer(ctxt) != null:
 *      * tb != null -> tb.writeEndObject(), p = tb.asParser(p), p.nextToken(), deser.deserialize
 *      * tb == null -> deser.deserialize
 *    - deser == null:
 *      * TypeDeserializer.deserializeIfNatural(p, ctxt, _baseType) != null -> returns natural result
 *      * p.getCurrentToken() == JsonToken.START_ARRAY -> super.deserializeTypedFromAny
 *      * Fallthrough: reports wrong token exception ("missing property...")
 * 7. deserializeTypedFromAny(JsonParser, DeserializationContext):
 *    - Branch: p.getCurrentToken() == JsonToken.START_ARRAY -> super.deserializeTypedFromArray
 *    - Branch: p.getCurrentToken() != JsonToken.START_ARRAY -> deserializeTypedFromObject
 *
 * Known Defect (Defects4J ground truth):
 * - Jackson Databind Issue #1533: When DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT is enabled,
 *   deserializing an empty string ("") as a polymorphic type using AsPropertyTypeDeserializer should
 *   deserialize to null rather than failing with JsonMappingException (VALUE_STRING unexpected token,
 *   expected FIELD_NAME).
 * ---------------------------------------------------------------------------------------------------------
 */

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class AsPropertyTypeDeserializerGeminiTest {

    // =========================================================================
    // Supporting Domain Classes for Polymorphic Testing
    // =========================================================================

    @JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "type")
    @JsonSubTypes({
        @JsonSubTypes.Type(value = AnimalImpl.class, name = "animal"),
        @JsonSubTypes.Type(value = DogImpl.class, name = "dog")
    })
    static abstract class BaseAnimal {
        public String name;
    }

    @JsonTypeName("animal")
    static class AnimalImpl extends BaseAnimal {
        public int age;
    }

    @JsonTypeName("dog")
    static class DogImpl extends BaseAnimal {
        public boolean bark;
    }

    @JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "type", defaultImpl = DefaultCat.class)
    @JsonSubTypes({
        @JsonSubTypes.Type(value = SpecificCat.class, name = "specific")
    })
    static abstract class CatWithDefault {
        public String name;
    }

    static class DefaultCat extends CatWithDefault {
        public int lives = 9;
    }

    static class SpecificCat extends CatWithDefault {
        public String breed;
    }

    @JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "type", visible = true)
    @JsonSubTypes({
        @JsonSubTypes.Type(value = VisibleTypeIdBean.class, name = "visible")
    })
    static class VisibleTypeIdBean {
        public String type;
        public String data;
    }

    @JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "type")
    static class NaturalStringHolder {
    }

    // =========================================================================
    // PARTITION C: Defect-Targeted Branch Zone (Defects4J Issue #1533)
    // =========================================================================

    /**
     * Target Defect Ground Truth:
     * com.fasterxml.jackson.databind.jsontype.TestPolymorphicWithDefaultImpl::testWithEmptyStringAsNullObject1533
     *
     * When ACCEPT_EMPTY_STRING_AS_NULL_OBJECT is enabled, deserializing an empty string ""
     * for a polymorphic object with As.PROPERTY inclusion must return null without throwing:
     * "JsonMappingException: Unexpected token (VALUE_STRING), expected FIELD_NAME: missing property 'type'..."
     */
    @Test(timeout = 4000)
    public void testDefectEmptyStringAsNullObjectWithAsProperty1533() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

        // Deserializing empty string for polymorphic type without defaultImpl
        BaseAnimal result = mapper.readValue("\"\"", BaseAnimal.class);
        assertNull("Empty string should deserialize to null when ACCEPT_EMPTY_STRING_AS_NULL_OBJECT is enabled", result);
    }

    @Test(timeout = 4000)
    public void testDefectWhitespaceStringAsNullObjectWithAsProperty1533() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

        // Also test whitespace string
        BaseAnimal result = mapper.readValue("\"   \"", BaseAnimal.class);
        assertNull("Whitespace string should deserialize to null when ACCEPT_EMPTY_STRING_AS_NULL_OBJECT is enabled", result);
    }

    // =========================================================================
    // PARTITION A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testTypeIdAsFirstProperty() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"type\":\"dog\",\"name\":\"Rex\",\"bark\":true}";
        BaseAnimal animal = mapper.readValue(json, BaseAnimal.class);

        assertNotNull(animal);
        assertTrue(animal instanceof DogImpl);
        DogImpl dog = (DogImpl) animal;
        assertEquals("Rex", dog.name);
        assertTrue(dog.bark);
    }

    @Test(timeout = 4000)
    public void testTypeIdAsLastPropertyRequiringTokenBufferBuffering() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // Properties before 'type' force buffering into TokenBuffer
        String json = "{\"name\":\"Fido\",\"bark\":false,\"type\":\"dog\"}";
        BaseAnimal animal = mapper.readValue(json, BaseAnimal.class);

        assertNotNull(animal);
        assertTrue(animal instanceof DogImpl);
        DogImpl dog = (DogImpl) animal;
        assertEquals("Fido", dog.name);
        assertFalse(dog.bark);
    }

    @Test(timeout = 4000)
    public void testTypeIdVisibleProperty() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"data\":\"payload\",\"type\":\"visible\"}";
        VisibleTypeIdBean bean = mapper.readValue(json, VisibleTypeIdBean.class);

        assertNotNull(bean);
        assertEquals("visible", bean.type);
        assertEquals("payload", bean.data);
    }

    @Test(timeout = 4000)
    public void testDefaultImplWhenTypeIdMissingInObject() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // No "type" property provided; should fall back to DefaultCat
        String json = "{\"name\":\"Tom\",\"lives\":7}";
        CatWithDefault cat = mapper.readValue(json, CatWithDefault.class);

        assertNotNull(cat);
        assertTrue(cat instanceof DefaultCat);
        assertEquals("Tom", cat.name);
        assertEquals(7, ((DefaultCat) cat).lives);
    }

    @Test(timeout = 4000)
    public void testDefaultImplWhenObjectIsEmpty() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{}";
        CatWithDefault cat = mapper.readValue(json, CatWithDefault.class);

        assertNotNull(cat);
        assertTrue(cat instanceof DefaultCat);
        assertEquals(9, ((DefaultCat) cat).lives);
    }

    @Test(timeout = 4000)
    public void testSpecificImplWhenTypeIdPresent() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"type\":\"specific\",\"name\":\"Sylvester\",\"breed\":\"Persian\"}";
        CatWithDefault cat = mapper.readValue(json, CatWithDefault.class);

        assertNotNull(cat);
        assertTrue(cat instanceof SpecificCat);
        assertEquals("Sylvester", cat.name);
        assertEquals("Persian", ((SpecificCat) cat).breed);
    }

    @Test(timeout = 4000)
    public void testDeserializeTypedFromAnyArrayWrapper() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // Deserializing a List of polymorphic animals
        String json = "[{\"type\":\"animal\",\"name\":\"Mickey\",\"age\":3}]";
        JavaType listType = mapper.getTypeFactory().constructCollectionType(List.class, BaseAnimal.class);
        List<BaseAnimal> list = mapper.readValue(json, listType);

        assertNotNull(list);
        assertEquals(1, list.size());
        assertTrue(list.get(0) instanceof AnimalImpl);
        assertEquals("Mickey", list.get(0).name);
    }

    // =========================================================================
    // PARTITION B: Boundary Value Analysis (BVA) & Low-Level API Tests
    // =========================================================================

    @Test(timeout = 4000)
    public void testTypeInclusionGettersAndConstructors() {
        JavaType baseType = TypeFactory.defaultInstance().constructType(BaseAnimal.class);
        AsPropertyTypeDeserializer deserDefault = new AsPropertyTypeDeserializer(
                baseType, null, "type", false, null);
        assertEquals(As.PROPERTY, deserDefault.getTypeInclusion());

        AsPropertyTypeDeserializer deserCustom = new AsPropertyTypeDeserializer(
                baseType, null, "type", false, null, As.EXISTING_PROPERTY);
        assertEquals(As.EXISTING_PROPERTY, deserCustom.getTypeInclusion());
    }

    @Test(timeout = 4000)
    public void testForPropertyReturnsSameOrClone() {
        JavaType baseType = TypeFactory.defaultInstance().constructType(BaseAnimal.class);
        AsPropertyTypeDeserializer deser = new AsPropertyTypeDeserializer(
                baseType, null, "type", false, null);

        // Same property (null == _property) -> returns this
        TypeDeserializer same = deser.forProperty(null);
        assertSame(deser, same);

        // Different property dummy
        BeanProperty.Bogus bogusProp = new BeanProperty.Bogus();
        TypeDeserializer clone = deser.forProperty(bogusProp);
        assertNotSame(deser, clone);
        assertTrue(clone instanceof AsPropertyTypeDeserializer);
        assertEquals(As.PROPERTY, clone.getTypeInclusion());
    }

    @Test(timeout = 4000)
    public void testDeserializeTypedFromAnyDelegationToObject() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType baseType = mapper.constructType(CatWithDefault.class);
        TypeDeserializer typeDeser = mapper.getDeserializationConfig().findTypeDeserializer(baseType);

        assertNotNull(typeDeser);
        assertTrue(typeDeser instanceof AsPropertyTypeDeserializer);

        String json = "{\"name\":\"Simba\"}";
        JsonParser p = mapper.getFactory().createParser(json);
        DeserializationContext ctxt = mapper.getDeserializationContext();
        p.nextToken(); // Move to START_OBJECT

        // deserializeTypedFromAny on non-array should delegate to deserializeTypedFromObject
        Object result = typeDeser.deserializeTypedFromAny(p, ctxt);
        assertNotNull(result);
        assertTrue(result instanceof DefaultCat);
        p.close();
    }

    // =========================================================================
    // PARTITION D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testMissingTypeIdWithoutDefaultImplThrowsException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"name\":\"UnknownAnimal\"}";

        try {
            mapper.readValue(json, BaseAnimal.class);
            fail("Expected JsonMappingException for missing type id property without defaultImpl");
        } catch (JsonMappingException e) {
            String msg = e.getMessage();
            assertTrue("Exception message should state missing property: " + msg,
                    msg.contains("missing property 'type'") || msg.contains("Could not resolve type id"));
        }
    }

    @Test(timeout = 4000)
    public void testUnknownTypeIdThrowsException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"type\":\"alien_creature\",\"name\":\"Zorg\"}";

        try {
            mapper.readValue(json, BaseAnimal.class);
            fail("Expected JsonMappingException for unknown type id");
        } catch (JsonMappingException e) {
            assertTrue("Exception message should indicate unknown type id: " + e.getMessage(),
                    e.getMessage().contains("alien_creature") || e.getMessage().contains("Could not resolve type id"));
        }
    }

    @Test(timeout = 4000)
    public void testUnexpectedScalarTokenWithoutEmptyStringConfigThrows() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // ACCEPT_EMPTY_STRING_AS_NULL_OBJECT is disabled by default
        try {
            mapper.readValue("\"some_string\"", BaseAnimal.class);
            fail("Expected JsonMappingException when scalar string encountered instead of object/array");
        } catch (JsonMappingException e) {
            assertTrue("Exception expected for wrong token: " + e.getMessage(),
                    e.getMessage().contains("missing property 'type'")
                            || e.getMessage().contains("Cannot deserialize")
                            || e.getMessage().contains("Can not deserialize"));
        }
    }

    @Test(timeout = 4000)
    public void testUnexpectedBooleanTokenThrows() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readValue("true", BaseAnimal.class);
            fail("Expected JsonMappingException when boolean encountered instead of object/array");
        } catch (JsonMappingException e) {
            assertNotNull(e.getMessage());
        }
    }

    // =========================================================================
    // PARTITION E: Multiple Nested Properties & Native Type ID Guard
    // =========================================================================

    @Test(timeout = 4000)
    public void testMultipleBufferedPropertiesBeforeAndAfterTypeId() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // Multiple properties before AND after type id
        String json = "{\"a\":1,\"b\":{\"inner\":\"val\"},\"c\":[1,2],\"type\":\"dog\",\"bark\":true,\"name\":\"Spike\"}";
        BaseAnimal animal = mapper.readValue(json, BaseAnimal.class);

        assertNotNull(animal);
        assertTrue(animal instanceof DogImpl);
        DogImpl dog = (DogImpl) animal;
        assertEquals("Spike", dog.name);
        assertTrue(dog.bark);
    }
}