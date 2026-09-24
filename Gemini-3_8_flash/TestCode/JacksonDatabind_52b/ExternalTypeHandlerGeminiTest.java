package com.fasterxml.jackson.databind.deser.impl;

/* [Branch & Defect Analysis Matrix]
 * Target Class: com.fasterxml.jackson.databind.deser.impl.ExternalTypeHandler
 *
 * Decision / Condition Branch Points:
 * 1. handleTypePropertyValue:
 *    - nameToPropertyIndex lookup null -> returns false
 *    - !prop.hasTypePropertyName(propName) -> returns false
 *    - canDeserialize = (bean != null) && (_tokens[index] != null) -> true (immediate deserializeAndSet), false (buffer typeId)
 * 2. handlePropertyValue:
 *    - nameToPropertyIndex lookup null -> returns false
 *    - prop.hasTypePropertyName(propName) -> buffer typeId, canDeserialize check
 *    - !prop.hasTypePropertyName(propName) -> buffer tokens, canDeserialize check
 *    - canDeserialize -> immediate deserializeAndSet and clean up arrays
 * 3. complete(JsonParser, DeserializationContext, Object):
 *    - typeId == null && tokens == null -> continue (both missing allowed)
 *    - typeId == null && tokens != null:
 *      * tokens.firstToken() scalar -> deserializeIfNatural returns non-null (set & continue)
 *      * !hasDefaultType() -> reportMappingException("Missing external type id property '%s'")
 *      * hasDefaultType() -> typeId = getDefaultTypeId()
 *    - typeId != null && tokens == null -> reportMappingException("Missing property '%s' for external type id '%s'")
 *    - VALUE_NULL token handling in _deserializeAndSet -> property.set(bean, null)
 * 4. complete(JsonParser, DeserializationContext, PropertyValueBuffer, PropertyBasedCreator):
 *    - typeId == null && tokens == null -> continue
 *    - typeId == null && tokens != null && !hasDefaultType() -> reportMappingException
 *    - typeId == null && tokens != null && hasDefaultType() -> getDefaultTypeId()
 *    - typeId != null && tokens == null -> reportMappingException
 *    - creatorIndex >= 0 -> assignParameter to creator buffer
 *    - [databind#999] typeProp != null && typeProp.getCreatorIndex() >= 0 -> buffer.assignParameter(typeProp, typeId)
 *    - creator.build(ctxt, buffer), then set non-creator properties
 * 5. Builder & Lifecycle:
 *    - start() creates a new instance with fresh _typeIds and _tokens arrays
 *    - Builder.build() initializes mapping and property arrays
 *
 * Known Defect Target:
 * - [databind#999]: When external type id is also bound to a creator parameter,
 *   the type property was not assigned into the PropertyValueBuffer during creation,
 *   leaving the creator property null.
 */

import java.io.IOException;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;

public class ExternalTypeHandlerGeminiTest
{
    // =========================================================================
    // Test Models and Polymorphic Fixtures
    // =========================================================================

    static class PolyBase {
        public int id;
    }

    static class PolyA extends PolyBase {
        public String name;
    }

    static class PolyB extends PolyBase {
        public double value;
    }

    static class StandardBean {
        public String type;

        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
        @JsonSubTypes({
            @JsonSubTypes.Type(value = PolyA.class, name = "a"),
            @JsonSubTypes.Type(value = PolyB.class, name = "b")
        })
        public PolyBase poly;
    }

    static class DefaultImplBean {
        public String type;

        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type", defaultImpl = PolyA.class)
        @JsonSubTypes({
            @JsonSubTypes.Type(value = PolyB.class, name = "b")
        })
        public PolyBase poly;
    }

    static class NaturalBean {
        public String type;

        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
        public Object natural;
    }

    static class CreatorBean {
        final String type;
        final PolyBase poly;

        @JsonCreator
        public CreatorBean(
                @JsonProperty("type") String type,
                @JsonProperty("poly")
                @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
                @JsonSubTypes({
                    @JsonSubTypes.Type(value = PolyA.class, name = "a"),
                    @JsonSubTypes.Type(value = PolyB.class, name = "b")
                })
                PolyBase poly) {
            this.type = type;
            this.poly = poly;
        }
    }

    static class CreatorDefaultImplBean {
        final String type;
        final PolyBase poly;

        @JsonCreator
        public CreatorDefaultImplBean(
                @JsonProperty("type") String type,
                @JsonProperty("poly")
                @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type", defaultImpl = PolyA.class)
                @JsonSubTypes({
                    @JsonSubTypes.Type(value = PolyB.class, name = "b")
                })
                PolyBase poly) {
            this.type = type;
            this.poly = poly;
        }
    }

    static class Defect999Item {
        final String type;
        final Object value;

        @JsonCreator
        public Defect999Item(
                @JsonProperty("type") String type,
                @JsonProperty("value")
                @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
                @JsonSubTypes({
                    @JsonSubTypes.Type(value = PolyA.class, name = "foo")
                })
                Object value) {
            this.type = type;
            this.value = value;
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testTypeBeforeValueProperty() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"type\":\"a\",\"poly\":{\"id\":10,\"name\":\"Alpha\"}}";

        StandardBean bean = mapper.readValue(json, StandardBean.class);
        assertNotNull(bean);
        assertEquals("a", bean.type);
        assertTrue(bean.poly instanceof PolyA);
        PolyA polyA = (PolyA) bean.poly;
        assertEquals(10, polyA.id);
        assertEquals("Alpha", polyA.name);
    }

    @Test(timeout = 4000)
    public void testValueBeforeTypeProperty() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"poly\":{\"id\":20,\"value\":3.14},\"type\":\"b\"}";

        StandardBean bean = mapper.readValue(json, StandardBean.class);
        assertNotNull(bean);
        assertEquals("b", bean.type);
        assertTrue(bean.poly instanceof PolyB);
        PolyB polyB = (PolyB) bean.poly;
        assertEquals(20, polyB.id);
        assertEquals(3.14, polyB.value, 0.0001);
    }

    @Test(timeout = 4000)
    public void testValueNullHandlingWithNonNullTypeId() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"type\":\"a\",\"poly\":null}";

        StandardBean bean = mapper.readValue(json, StandardBean.class);
        assertNotNull(bean);
        assertEquals("a", bean.type);
        assertNull(bean.poly);
    }

    @Test(timeout = 4000)
    public void testDefaultImplResolutionWhenTypeOmitted() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"poly\":{\"id\":30,\"name\":\"DefaultResolved\"}}";

        DefaultImplBean bean = mapper.readValue(json, DefaultImplBean.class);
        assertNotNull(bean);
        assertTrue(bean.poly instanceof PolyA);
        PolyA polyA = (PolyA) bean.poly;
        assertEquals(30, polyA.id);
        assertEquals("DefaultResolved", polyA.name);
    }

    @Test(timeout = 4000)
    public void testCreatorDefaultImplResolutionWhenTypeOmitted() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"poly\":{\"id\":35,\"name\":\"CreatorDefault\"}}";

        CreatorDefaultImplBean bean = mapper.readValue(json, CreatorDefaultImplBean.class);
        assertNotNull(bean);
        assertTrue(bean.poly instanceof PolyA);
        PolyA polyA = (PolyA) bean.poly;
        assertEquals(35, polyA.id);
        assertEquals("CreatorDefault", polyA.name);
    }

    @Test(timeout = 4000)
    public void testNaturalTypeScalarStringResolution() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"natural\":\"just-a-plain-string\"}";

        NaturalBean bean = mapper.readValue(json, NaturalBean.class);
        assertNotNull(bean);
        assertEquals("just-a-plain-string", bean.natural);
    }

    @Test(timeout = 4000)
    public void testNaturalTypeScalarIntegerResolution() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"natural\":12345}";

        NaturalBean bean = mapper.readValue(json, NaturalBean.class);
        assertNotNull(bean);
        assertEquals(12345, bean.natural);
    }

    @Test(timeout = 4000)
    public void testNaturalTypeScalarBooleanResolution() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"natural\":true}";

        NaturalBean bean = mapper.readValue(json, NaturalBean.class);
        assertNotNull(bean);
        assertEquals(Boolean.TRUE, bean.natural);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyJsonPayloadSucceeds() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        StandardBean bean = mapper.readValue("{}", StandardBean.class);
        assertNotNull(bean);
        assertNull(bean.type);
        assertNull(bean.poly);
    }

    @Test(timeout = 4000)
    public void testCreatorEmptyJsonPayloadSucceeds() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        CreatorBean bean = mapper.readValue("{}", CreatorBean.class);
        assertNotNull(bean);
        assertNull(bean.type);
        assertNull(bean.poly);
    }

    @Test(timeout = 4000)
    public void testCreatorNullPolyWithValueNullToken() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"type\":\"a\",\"poly\":null}";

        CreatorBean bean = mapper.readValue(json, CreatorBean.class);
        assertNotNull(bean);
        assertNull(bean.poly);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (databind#999)
    // =========================================================================

    @Test(timeout = 4000)
    public void testExternalTypeIdAsCreatorProperty_Defect999_ValueFirst() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"value\":{\"id\":99,\"name\":\"target\"},\"type\":\"foo\"}";

        Defect999Item item = mapper.readValue(json, Defect999Item.class);
        assertNotNull(item);
        assertEquals("foo", item.type);
        assertNotNull(item.value);
        assertTrue(item.value instanceof PolyA);
        assertEquals(99, ((PolyA) item.value).id);
        assertEquals("target", ((PolyA) item.value).name);
    }

    @Test(timeout = 4000)
    public void testExternalTypeIdAsCreatorProperty_Defect999_TypeFirst() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"type\":\"foo\",\"value\":{\"id\":100,\"name\":\"target100\"}}";

        Defect999Item item = mapper.readValue(json, Defect999Item.class);
        assertNotNull(item);
        assertEquals("foo", item.type);
        assertNotNull(item.value);
        assertTrue(item.value instanceof PolyA);
        assertEquals(100, ((PolyA) item.value).id);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testMissingTypeIdThrowsMappingException() {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"poly\":{\"id\":1,\"name\":\"NoType\"}}";

        try {
            mapper.readValue(json, StandardBean.class);
            fail("Expected JsonMappingException for missing type id");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Missing external type id property 'type'"));
        } catch (IOException e) {
            fail("Unexpected IOException: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testMissingPropertyThrowsMappingException() {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"type\":\"a\"}";

        try {
            mapper.readValue(json, StandardBean.class);
            fail("Expected JsonMappingException for missing property value");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Missing property 'poly' for external type id 'type'"));
        } catch (IOException e) {
            fail("Unexpected IOException: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testCreatorMissingTypeIdThrowsMappingException() {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"poly\":{\"id\":2,\"name\":\"NoTypeCreator\"}}";

        try {
            mapper.readValue(json, CreatorBean.class);
            fail("Expected JsonMappingException for missing type id in creator");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Missing external type id property 'type'"));
        } catch (IOException e) {
            fail("Unexpected IOException: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testCreatorMissingPropertyThrowsMappingException() {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"type\":\"a\"}";

        try {
            mapper.readValue(json, CreatorBean.class);
            fail("Expected JsonMappingException for missing property in creator");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Missing property 'poly' for external type id 'type'"));
        } catch (IOException e) {
            fail("Unexpected IOException: " + e.getMessage());
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Unit Handler Verification
    // =========================================================================

    @Test(timeout = 4000)
    public void testBuilderBuildAndStartLifecycle() throws IOException {
        ExternalTypeHandler.Builder builder = new ExternalTypeHandler.Builder();
        @SuppressWarnings("deprecation")
        ExternalTypeHandler handler = builder.build();
        assertNotNull(handler);

        ExternalTypeHandler started = handler.start();
        assertNotNull(started);
        assertNotSame(handler, started);

        JsonFactory factory = new JsonFactory();
        JsonParser p = factory.createParser("{}");

        // Property unknown to handler returns false
        assertFalse(started.handlePropertyValue(p, null, "unknownProp", new Object()));
        assertFalse(started.handleTypePropertyValue(p, null, "unknownTypeProp", new Object()));
        p.close();
    }
}