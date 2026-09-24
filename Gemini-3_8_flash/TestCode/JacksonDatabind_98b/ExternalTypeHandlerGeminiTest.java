package com.fasterxml.jackson.databind.deser.impl;

/* [Branch & Defect Analysis Matrix]
 * Target Class: com.fasterxml.jackson.databind.deser.impl.ExternalTypeHandler
 *
 * 1. DEFECT-TARGETED ZONE (databind#1328):
 *    - complete(p, ctxt, buffer, creator) assigning non-String typeId parameter:
 *      When an external type id is bound to a creator parameter whose type is not java.lang.String
 *      (e.g., an Enum), buffer.assignParameter(typeProp, typeId) directly passes the String typeId,
 *      resulting in IllegalArgumentException / InvalidDefinitionException (argument type mismatch)
 *      upon creator.build(ctxt, buffer).
 *
 * 2. BRANCH & FLOW COVERAGE:
 *    - Order Independence: Type property before value property vs. Value property before type property.
 *    - Shared Type Property (databind#291): Multiple properties sharing identical external type property name,
 *      exercising List<Integer> branches in _nameToPropertyIndex for handleTypePropertyValue & handlePropertyValue.
 *    - Value Null Handling (databind#942): _deserialize and _deserializeAndSet handling VALUE_NULL token.
 *    - Missing Type ID with defaultImpl (databind#94): fallback to getDefaultTypeId().
 *    - Missing Type ID without defaultImpl: triggers reportInputMismatch for missing type id.
 *    - Missing Property for Type ID:
 *      - With FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY enabled (exception expected).
 *      - With FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY disabled (soft completion).
 *    - Natural Types (databind#118): Scalar values deserialized via deserializeIfNatural when type id missing.
 *    - Property-based Creators: External type property used as creator parameter along with polymorphic value.
 *    - Passthrough / Unhandled Properties: Unmatched property names returning false.
 *    - Builder & Lifecycle: ExternalTypeHandler.builder(), start(), and immutability reset.
 */

import java.io.IOException;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ExternalTypeHandlerGeminiTest {

    private final ObjectMapper mapper = new ObjectMapper();

    // =========================================================================
    // Partition C: Defect-Targeted Zone (databind#1328)
    // =========================================================================

    public enum AnimalType {
        DOG, CAT
    }

    public interface Animal { }

    public static class Dog implements Animal {
        public String name;
        public Dog() { }
        public Dog(String n) { this.name = n; }
    }

    public static class Cat implements Animal {
        public String color;
        public Cat() { }
        public Cat(String c) { this.color = c; }
    }

    public static class AnimalAndTypeHolder {
        public final AnimalType type;
        public final Animal animal;

        @JsonCreator
        public AnimalAndTypeHolder(
                @JsonProperty("type") AnimalType type,
                @JsonProperty("animal")
                @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
                @JsonSubTypes({
                    @JsonSubTypes.Type(value = Dog.class, name = "DOG"),
                    @JsonSubTypes.Type(value = Cat.class, name = "CAT")
                })
                Animal animal) {
            this.type = type;
            this.animal = animal;
        }
    }

    /**
     * Targets databind#1328: External type property bound to an Enum creator parameter.
     * When external type id is parsed, it must be properly coerced/deserialized to the Enum type,
     * not passed as a raw String into the PropertyValueBuffer.
     */
    @Test(timeout = 4000)
    public void testExternalTypeIdWithEnumInCreator() throws Exception {
        String json = "{\"type\":\"DOG\",\"animal\":{\"name\":\"Rex\"}}";
        AnimalAndTypeHolder result = mapper.readValue(json, AnimalAndTypeHolder.class);
        assertNotNull(result);
        assertEquals(AnimalType.DOG, result.type);
        assertTrue(result.animal instanceof Dog);
        assertEquals("Rex", ((Dog) result.animal).name);
    }

    @Test(timeout = 4000)
    public void testExternalTypeIdWithEnumInCreatorReversedOrder() throws Exception {
        String json = "{\"animal\":{\"name\":\"Bella\"},\"type\":\"DOG\"}";
        AnimalAndTypeHolder result = mapper.readValue(json, AnimalAndTypeHolder.class);
        assertNotNull(result);
        assertEquals(AnimalType.DOG, result.type);
        assertTrue(result.animal instanceof Dog);
        assertEquals("Bella", ((Dog) result.animal).name);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    public static class StandardHolder {
        public String type;

        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
        @JsonSubTypes({
            @JsonSubTypes.Type(value = Dog.class, name = "dog"),
            @JsonSubTypes.Type(value = Cat.class, name = "cat")
        })
        public Animal animal;
    }

    @Test(timeout = 4000)
    public void testTypeBeforeValueFieldAssignment() throws Exception {
        String json = "{\"type\":\"dog\",\"animal\":{\"name\":\"Buddy\"}}";
        StandardHolder holder = mapper.readValue(json, StandardHolder.class);
        assertNotNull(holder);
        assertEquals("dog", holder.type);
        assertTrue(holder.animal instanceof Dog);
        assertEquals("Buddy", ((Dog) holder.animal).name);
    }

    @Test(timeout = 4000)
    public void testValueBeforeTypeFieldAssignment() throws Exception {
        String json = "{\"animal\":{\"color\":\"white\"},\"type\":\"cat\"}";
        StandardHolder holder = mapper.readValue(json, StandardHolder.class);
        assertNotNull(holder);
        assertEquals("cat", holder.type);
        assertTrue(holder.animal instanceof Cat);
        assertEquals("white", ((Cat) holder.animal).color);
    }

    // Multi-property mapping sharing same type property (databind#291)
    public static class MultiSharedTypeHolder {
        public String type;

        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
        @JsonSubTypes({
            @JsonSubTypes.Type(value = Dog.class, name = "dog"),
            @JsonSubTypes.Type(value = Cat.class, name = "cat")
        })
        public Animal first;

        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
        @JsonSubTypes({
            @JsonSubTypes.Type(value = Dog.class, name = "dog"),
            @JsonSubTypes.Type(value = Cat.class, name = "cat")
        })
        public Animal second;
    }

    @Test(timeout = 4000)
    public void testSharedExternalTypeIdTypeFirst() throws Exception {
        String json = "{\"type\":\"dog\",\"first\":{\"name\":\"D1\"},\"second\":{\"name\":\"D2\"}}";
        MultiSharedTypeHolder holder = mapper.readValue(json, MultiSharedTypeHolder.class);
        assertNotNull(holder);
        assertEquals("dog", holder.type);
        assertTrue(holder.first instanceof Dog);
        assertEquals("D1", ((Dog) holder.first).name);
        assertTrue(holder.second instanceof Dog);
        assertEquals("D2", ((Dog) holder.second).name);
    }

    @Test(timeout = 4000)
    public void testSharedExternalTypeIdTypeLast() throws Exception {
        String json = "{\"first\":{\"color\":\"black\"},\"second\":{\"color\":\"orange\"},\"type\":\"cat\"}";
        MultiSharedTypeHolder holder = mapper.readValue(json, MultiSharedTypeHolder.class);
        assertNotNull(holder);
        assertEquals("cat", holder.type);
        assertTrue(holder.first instanceof Cat);
        assertEquals("black", ((Cat) holder.first).color);
        assertTrue(holder.second instanceof Cat);
        assertEquals("orange", ((Cat) holder.second).color);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Null Handling
    // =========================================================================

    @Test(timeout = 4000)
    public void testExternalPropertyWithExplicitNullValue() throws Exception {
        String json = "{\"type\":\"dog\",\"animal\":null}";
        StandardHolder holder = mapper.readValue(json, StandardHolder.class);
        assertNotNull(holder);
        assertEquals("dog", holder.type);
        assertNull(holder.animal);
    }

    @Test(timeout = 4000)
    public void testExternalPropertyWithExplicitNullValueTypeLast() throws Exception {
        String json = "{\"animal\":null,\"type\":\"dog\"}";
        StandardHolder holder = mapper.readValue(json, StandardHolder.class);
        assertNotNull(holder);
        assertEquals("dog", holder.type);
        assertNull(holder.animal);
    }

    public static class CreatorWithNullHolder {
        public final String type;
        public final Animal animal;

        @JsonCreator
        public CreatorWithNullHolder(
                @JsonProperty("type") String type,
                @JsonProperty("animal")
                @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
                @JsonSubTypes({
                    @JsonSubTypes.Type(value = Dog.class, name = "dog")
                })
                Animal animal) {
            this.type = type;
            this.animal = animal;
        }
    }

    @Test(timeout = 4000)
    public void testCreatorPropertyWithNullAnimal() throws Exception {
        String json = "{\"type\":\"dog\",\"animal\":null}";
        CreatorWithNullHolder holder = mapper.readValue(json, CreatorWithNullHolder.class);
        assertNotNull(holder);
        assertEquals("dog", holder.type);
        assertNull(holder.animal);
    }

    // Default Implementation (defaultImpl) handling
    public static class DefaultImplHolder {
        public String type;

        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
                      property = "type", defaultImpl = Dog.class)
        @JsonSubTypes({
            @JsonSubTypes.Type(value = Cat.class, name = "cat")
        })
        public Animal animal;
    }

    @Test(timeout = 4000)
    public void testDefaultImplWhenTypePropertyIsMissing() throws Exception {
        String json = "{\"animal\":{\"name\":\"DefaultDog\"}}";
        DefaultImplHolder holder = mapper.readValue(json, DefaultImplHolder.class);
        assertNotNull(holder);
        assertTrue(holder.animal instanceof Dog);
        assertEquals("DefaultDog", ((Dog) holder.animal).name);
    }

    // Natural Type handling without explicit type id (databind#118)
    public static class NaturalTypeHolder {
        public String type;

        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
        public Object data;
    }

    @Test(timeout = 4000)
    public void testNaturalTypeScalarIntegerWithoutTypeId() throws Exception {
        String json = "{\"data\":12345}";
        NaturalTypeHolder holder = mapper.readValue(json, NaturalTypeHolder.class);
        assertNotNull(holder);
        assertEquals(12345, holder.data);
    }

    @Test(timeout = 4000)
    public void testNaturalTypeScalarBooleanWithoutTypeId() throws Exception {
        String json = "{\"data\":true}";
        NaturalTypeHolder holder = mapper.readValue(json, NaturalTypeHolder.class);
        assertNotNull(holder);
        assertEquals(Boolean.TRUE, holder.data);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testMissingTypeIdThrowsExceptionWhenNoDefaultImpl() throws Exception {
        String json = "{\"animal\":{\"name\":\"NoType\"}}";
        try {
            mapper.readValue(json, StandardHolder.class);
            fail("Expected JsonMappingException for missing external type id");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Missing external type id property 'type'"));
        }
    }

    @Test(timeout = 4000)
    public void testMissingPropertyWithFeatureFailOnMissing() throws Exception {
        ObjectMapper failMapper = new ObjectMapper();
        failMapper.enable(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY);

        String json = "{\"type\":\"dog\"}";
        try {
            failMapper.readValue(json, StandardHolder.class);
            fail("Expected exception when external property value is missing");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Missing property 'animal' for external type id 'type'"));
        }
    }

    @Test(timeout = 4000)
    public void testMissingPropertyWithoutFeatureReturnsIncompleteBean() throws Exception {
        ObjectMapper laxMapper = new ObjectMapper();
        laxMapper.disable(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY);

        String json = "{\"type\":\"dog\"}";
        StandardHolder holder = laxMapper.readValue(json, StandardHolder.class);
        assertNotNull(holder);
        assertNull(holder.animal);
    }

    public static class CreatorMissingPropHolder {
        @JsonCreator
        public CreatorMissingPropHolder(
                @JsonProperty("type") String type,
                @JsonProperty("animal")
                @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
                @JsonSubTypes({
                    @JsonSubTypes.Type(value = Dog.class, name = "dog")
                })
                Animal animal) { }
    }

    @Test(timeout = 4000)
    public void testCreatorMissingPropertyThrowsException() throws Exception {
        String json = "{\"type\":\"dog\"}";
        try {
            mapper.readValue(json, CreatorMissingPropHolder.class);
            fail("Expected JsonMappingException when creator value property is missing");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Missing property 'animal' for external type id 'type'"));
        }
    }

    @Test(timeout = 4000)
    public void testCreatorMissingTypeIdThrowsException() throws Exception {
        String json = "{\"animal\":{\"name\":\"NoType\"}}";
        try {
            mapper.readValue(json, CreatorMissingPropHolder.class);
            fail("Expected JsonMappingException when creator external type id is missing");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Missing external type id property 'type'"));
        }
    }

    // =========================================================================
    // Partition E: Handler Lifecycle & Builder State Tests
    // =========================================================================

    public static class EmptyBean { }

    @Test(timeout = 4000)
    public void testBuilderAndStartLifecycle() {
        ExternalTypeHandler.Builder builder = ExternalTypeHandler.builder(
                mapper.constructType(EmptyBean.class));
        assertNotNull(builder);

        BeanPropertyMap emptyMap = BeanPropertyMap.construct(
                java.util.Collections.emptyList(), false);
        ExternalTypeHandler handler = builder.build(emptyMap);
        assertNotNull(handler);

        ExternalTypeHandler started = handler.start();
        assertNotNull(started);
        assertNotSame("start() must create a distinct instance", handler, started);
    }

    @Test(timeout = 4000)
    public void testUnrelatedPropertiesIgnored() throws IOException {
        ExternalTypeHandler.Builder builder = ExternalTypeHandler.builder(
                mapper.constructType(EmptyBean.class));
        BeanPropertyMap emptyMap = BeanPropertyMap.construct(
                java.util.Collections.emptyList(), false);
        ExternalTypeHandler handler = builder.build(emptyMap).start();

        com.fasterxml.jackson.core.JsonParser p = mapper.getFactory().createParser("{}");
        p.nextToken();
        com.fasterxml.jackson.databind.DeserializationContext ctxt = mapper.getDeserializationContext();

        assertFalse(handler.handlePropertyValue(p, ctxt, "unknownProp", new EmptyBean()));
        assertFalse(handler.handleTypePropertyValue(p, ctxt, "unknownProp", new EmptyBean()));
        p.close();
    }
}