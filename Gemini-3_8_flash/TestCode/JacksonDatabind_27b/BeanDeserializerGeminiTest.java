/*
 * [Branch & Defect Analysis Matrix]
 *
 * Targeted Class: com.fasterxml.jackson.databind.deser.BeanDeserializer
 *
 * Targeted Branches & Methods:
 * 1. deserialize(JsonParser, DeserializationContext):
 *    - p.isExpectedStartObjectToken() -> true (_vanillaProcessing true / false), false (_deserializeOther)
 * 2. _deserializeOther(JsonParser, DeserializationContext, JsonToken):
 *    - VALUE_STRING, VALUE_NUMBER_INT, VALUE_NUMBER_FLOAT, VALUE_EMBEDDED_OBJECT,
 *      VALUE_TRUE, VALUE_FALSE, START_ARRAY, FIELD_NAME, END_OBJECT, default (mappingException).
 * 3. deserialize(JsonParser, DeserializationContext, Object bean) [Updating]:
 *    - injectables, unwrappedPropertyHandler, externalTypeIdHandler, view processing, unknown properties.
 * 4. deserializeFromObject(JsonParser, DeserializationContext):
 *    - objectIdReader.maySerializeAsObject(), nonStandardCreation, valueInstantiator.createUsingDefault,
 *      canReadObjectId, view processing, unknown properties fallback.
 * 5. _deserializeUsingPropertyBased(JsonParser, DeserializationContext):
 *    - creator properties, readIdProperty, regular properties, ignorableProps, anySetter, unknown buffer,
 *      polymorphic type check.
 * 6. deserializeWithExternalTypeId / deserializeUsingPropertyBasedWithExternalTypeId:
 *    - creator properties with external type id (scalar vs buffered), completing external type handler.
 * 7. Copy-constructors & Mutators:
 *    - unwrappingDeserializer (subclass guard getClass() != BeanDeserializer.class vs exact class),
 *      withObjectIdReader, withIgnorableProperties, asArrayDeserializer.
 *
 * Critical Defect Targeted (Defects4J):
 * - Issue #928: com.fasterxml.jackson.databind.jsontype.TestExternalId::testInverseExternalId928
 *   In `deserializeUsingPropertyBasedWithExternalTypeId`:
 *   When an external type property arrives after or along with a CreatorProperty, passing `buffer`
 *   to `ext.handlePropertyValue(...)` incorrectly triggers an attempt to set on `CreatorProperty`,
 *   resulting in:
 *   java.lang.IllegalStateException: No fallback setter/field defined: can not use creator property
 *   for com.fasterxml.jackson.databind.deser.CreatorProperty.
 */

package com.fasterxml.jackson.databind.deser;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.impl.ObjectIdReader;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.util.NameTransformer;

public class BeanDeserializerGeminiTest {

    // =========================================================================
    // Test POJOs
    // =========================================================================

    public static class SimpleBean {
        public int x;
        public String y;

        public SimpleBean() { }
        public SimpleBean(int x, String y) {
            this.x = x;
            this.y = y;
        }
    }

    public static class CreatorBean {
        public final int a;
        public final String b;
        public int c;

        @JsonCreator
        public CreatorBean(@JsonProperty("a") int a, @JsonProperty("b") String b) {
            this.a = a;
            this.b = b;
        }

        public void setC(int c) {
            this.c = c;
        }
    }

    @JsonIgnoreProperties({"dummy", "ignored"})
    public static class IgnorableBean {
        public int id;
        public String name;
    }

    public static class AnySetterBean {
        public int id;
        private final Map<String, Object> any = new HashMap<String, Object>();

        @JsonAnySetter
        public void setAny(String key, Object val) {
            any.put(key, val);
        }

        public Map<String, Object> getAny() {
            return any;
        }
    }

    public static class Location {
        public double lat;
        public double lon;
    }

    public static class UnwrappedBean {
        public String name;
        @JsonUnwrapped
        public Location loc;
    }

    public static class UnwrappedCreatorBean {
        public final String name;
        @JsonUnwrapped
        public Location loc;

        @JsonCreator
        public UnwrappedCreatorBean(@JsonProperty("name") String name) {
            this.name = name;
        }
    }

    public static class Views {
        public static class Public {}
        public static class Internal extends Public {}
    }

    public static class ViewBean {
        @JsonView(Views.Public.class)
        public String pub;

        @JsonView(Views.Internal.class)
        public String priv;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    public static class IdBean {
        public int id;
        public IdBean next;
    }

    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    public static class ArrayFormatBean {
        public int a;
        public String b;
    }

    public static class DelegatingStringBean {
        public final String val;

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public DelegatingStringBean(String s) {
            this.val = s;
        }
    }

    public static class DelegatingIntBean {
        public final int val;

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public DelegatingIntBean(int v) {
            this.val = v;
        }
    }

    public static class DelegatingDoubleBean {
        public final double val;

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public DelegatingDoubleBean(double v) {
            this.val = v;
        }
    }

    public static class DelegatingBooleanBean {
        public final boolean val;

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public DelegatingBooleanBean(boolean v) {
            this.val = v;
        }
    }

    public static class DelegatingArrayBean {
        public final List<String> items;

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public DelegatingArrayBean(List<String> list) {
            this.items = list;
        }
    }

    public static class ThrowingSetterBean {
        public void setBoom(String val) {
            throw new IllegalArgumentException("Forced setter explosion");
        }
    }

    public static class ThrowingCreatorBean {
        @JsonCreator
        public ThrowingCreatorBean(@JsonProperty("v") String v) {
            throw new IllegalStateException("Forced creator explosion");
        }
    }

    public static class ExternalIdNormalBean {
        public String extType;

        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "extType")
        @JsonSubTypes({
            @JsonSubTypes.Type(value = String.class, name = "s"),
            @JsonSubTypes.Type(value = Integer.class, name = "i")
        })
        public Object extValue;
    }

    // Class for defect reproduction (Defects4J #928)
    public static class InvExt928 {
        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
        @JsonSubTypes({
            @JsonSubTypes.Type(value = String.class, name = "s"),
            @JsonSubTypes.Type(value = Integer.class, name = "i")
        })
        public final Object value;
        public final String type;

        @JsonCreator
        public InvExt928(@JsonProperty("value") Object v, @JsonProperty("type") String t) {
            this.value = v;
            this.type = t;
        }
    }

    public static class SubBeanDeserializer extends BeanDeserializer {
        private static final long serialVersionUID = 1L;

        public SubBeanDeserializer(BeanDeserializer src) {
            super(src);
        }

        public Object callMissingToken(JsonParser p, DeserializationContext ctxt) throws IOException {
            return _missingToken(p, ctxt);
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testVanillaDeserializationSuccess() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"x\":42,\"y\":\"hello\"}";
        SimpleBean bean = mapper.readValue(json, SimpleBean.class);
        assertNotNull(bean);
        assertEquals(42, bean.x);
        assertEquals("hello", bean.y);
    }

    @Test(timeout = 4000)
    public void testPropertyBasedCreatorDeserialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"b\":\"testVal\",\"a\":100,\"c\":200}";
        CreatorBean bean = mapper.readValue(json, CreatorBean.class);
        assertNotNull(bean);
        assertEquals(100, bean.a);
        assertEquals("testVal", bean.b);
        assertEquals(200, bean.c);
    }

    @Test(timeout = 4000)
    public void testDeserializationWithUpdatingExistingBean() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleBean existing = new SimpleBean(1, "orig");
        SimpleBean updated = mapper.readerForUpdating(existing).readValue("{\"x\":99}");
        assertSame(existing, updated);
        assertEquals(99, existing.x);
        assertEquals("orig", existing.y);
    }

    @Test(timeout = 4000)
    public void testDelegatingCreators() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        DelegatingStringBean strBean = mapper.readValue("\"foo\"", DelegatingStringBean.class);
        assertEquals("foo", strBean.val);

        DelegatingIntBean intBean = mapper.readValue("123", DelegatingIntBean.class);
        assertEquals(123, intBean.val);

        DelegatingDoubleBean dblBean = mapper.readValue("45.67", DelegatingDoubleBean.class);
        assertEquals(45.67, dblBean.val, 0.0001);

        DelegatingBooleanBean boolBeanTrue = mapper.readValue("true", DelegatingBooleanBean.class);
        assertTrue(boolBeanTrue.val);

        DelegatingBooleanBean boolBeanFalse = mapper.readValue("false", DelegatingBooleanBean.class);
        assertFalse(boolBeanFalse.val);

        DelegatingArrayBean arrBean = mapper.readValue("[\"item1\",\"item2\"]", DelegatingArrayBean.class);
        assertNotNull(arrBean.items);
        assertEquals(2, arrBean.items.size());
        assertEquals("item1", arrBean.items.get(0));
    }

    @Test(timeout = 4000)
    public void testAsArrayDeserializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "[10, \"arrayVal\"]";
        ArrayFormatBean bean = mapper.readValue(json, ArrayFormatBean.class);
        assertNotNull(bean);
        assertEquals(10, bean.a);
        assertEquals("arrayVal", bean.b);
    }

    @Test(timeout = 4000)
    public void testObjectIdHandling() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"id\":1,\"next\":{\"id\":2,\"next\":1}}";
        IdBean bean = mapper.readValue(json, IdBean.class);
        assertNotNull(bean);
        assertEquals(1, bean.id);
        assertNotNull(bean.next);
        assertEquals(2, bean.next.id);
        assertSame(bean, bean.next.next);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyJsonObject() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleBean bean = mapper.readValue("{}", SimpleBean.class);
        assertNotNull(bean);
        assertEquals(0, bean.x);
        assertNull(bean.y);
    }

    @Test(timeout = 4000)
    public void testNullValuedProperties() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleBean bean = mapper.readValue("{\"x\":0,\"y\":null}", SimpleBean.class);
        assertNotNull(bean);
        assertEquals(0, bean.x);
        assertNull(bean.y);
    }

    @Test(timeout = 4000)
    public void testIgnoreProperties() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"id\":5,\"dummy\":\"foo\",\"ignored\":123,\"name\":\"valid\"}";
        IgnorableBean bean = mapper.readValue(json, IgnorableBean.class);
        assertNotNull(bean);
        assertEquals(5, bean.id);
        assertEquals("valid", bean.name);
    }

    @Test(timeout = 4000)
    public void testAnySetterHandling() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"id\":10,\"extra1\":\"val1\",\"extra2\":999}";
        AnySetterBean bean = mapper.readValue(json, AnySetterBean.class);
        assertNotNull(bean);
        assertEquals(10, bean.id);
        assertEquals("val1", bean.getAny().get("extra1"));
        assertEquals(999, bean.getAny().get("extra2"));
    }

    @Test(timeout = 4000)
    public void testUnwrappedProperties() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"name\":\"home\",\"lat\":37.7749,\"lon\":-122.4194}";
        UnwrappedBean bean = mapper.readValue(json, UnwrappedBean.class);
        assertNotNull(bean);
        assertEquals("home", bean.name);
        assertNotNull(bean.loc);
        assertEquals(37.7749, bean.loc.lat, 0.0001);
        assertEquals(-122.4194, bean.loc.lon, 0.0001);
    }

    @Test(timeout = 4000)
    public void testUnwrappedWithPropertyBasedCreator() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"name\":\"home\",\"lat\":34.05,\"lon\":-118.24}";
        UnwrappedCreatorBean bean = mapper.readValue(json, UnwrappedCreatorBean.class);
        assertNotNull(bean);
        assertEquals("home", bean.name);
        assertNotNull(bean.loc);
        assertEquals(34.05, bean.loc.lat, 0.0001);
        assertEquals(-118.24, bean.loc.lon, 0.0001);
    }

    @Test(timeout = 4000)
    public void testViewProcessing() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"pub\":\"publicVal\",\"priv\":\"privateVal\"}";

        ViewBean pubBean = mapper.readerWithView(Views.Public.class).forType(ViewBean.class).readValue(json);
        assertNotNull(pubBean);
        assertEquals("publicVal", pubBean.pub);
        assertNull(pubBean.priv);

        ViewBean internalBean = mapper.readerWithView(Views.Internal.class).forType(ViewBean.class).readValue(json);
        assertNotNull(internalBean);
        assertEquals("publicVal", internalBean.pub);
        assertEquals("privateVal", internalBean.priv);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (JacksonDatabind #928)
    // =========================================================================

    /**
     * Targets Jackson Issue #928:
     * When external type id is used with a @JsonCreator and the payload property appears
     * BEFORE the external type id property (inverse order), BeanDeserializer's
     * deserializeUsingPropertyBasedWithExternalTypeId passes 'buffer' into
     * ext.handlePropertyValue(...) causing CreatorProperty set() to fail with
     * IllegalStateException.
     */
    @Test(timeout = 4000)
    public void testInverseExternalId928() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // Payload "value" comes BEFORE type id "type"
        String jsonInverse = "{\"value\":\"foo\",\"type\":\"s\"}";
        InvExt928 obj = mapper.readValue(jsonInverse, InvExt928.class);
        assertNotNull(obj);
        assertEquals("foo", obj.value);
        assertEquals("s", obj.type);

        // Also verify integer subtype resolution in inverse order
        String jsonInverseInt = "{\"value\":456,\"type\":\"i\"}";
        InvExt928 objInt = mapper.readValue(jsonInverseInt, InvExt928.class);
        assertNotNull(objInt);
        assertEquals(456, objInt.value);
        assertEquals("i", objInt.type);

        // Forward order should also succeed
        String jsonForward = "{\"type\":\"s\",\"value\":\"bar\"}";
        InvExt928 objForward = mapper.readValue(jsonForward, InvExt928.class);
        assertNotNull(objForward);
        assertEquals("bar", objForward.value);
        assertEquals("s", objForward.type);
    }

    @Test(timeout = 4000)
    public void testExternalTypeIdWithoutCreator() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // Normal POJO with external type property appearing first
        String json1 = "{\"extType\":\"s\",\"extValue\":\"helloWorld\"}";
        ExternalIdNormalBean b1 = mapper.readValue(json1, ExternalIdNormalBean.class);
        assertNotNull(b1);
        assertEquals("s", b1.extType);
        assertEquals("helloWorld", b1.extValue);

        // Inverse order with non-creator POJO
        String json2 = "{\"extValue\":789,\"extType\":\"i\"}";
        ExternalIdNormalBean b2 = mapper.readValue(json2, ExternalIdNormalBean.class);
        assertNotNull(b2);
        assertEquals("i", b2.extType);
        assertEquals(789, b2.extValue);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000, expected = JsonMappingException.class)
    public void testUnknownPropertyExceptionWhenEnabled() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.readValue("{\"unknownProp\":123}", SimpleBean.class);
    }

    @Test(timeout = 4000)
    public void testUnknownPropertyIgnoredWhenDisabled() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleBean bean = mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                                .readValue("{\"unknownProp\":123,\"x\":5}", SimpleBean.class);
        assertNotNull(bean);
        assertEquals(5, bean.x);
    }

    @Test(timeout = 4000, expected = JsonMappingException.class)
    public void testInvalidTokenMappingException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // SimpleBean has no scalar creator, so raw boolean token throws mapping exception
        mapper.readValue("true", SimpleBean.class);
    }

    @Test(timeout = 4000, expected = JsonMappingException.class)
    public void testArrayTokenWithoutArrayCreatorThrows() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.readValue("[1,2,3]", SimpleBean.class);
    }

    @Test(timeout = 4000)
    public void testSetterExceptionWrapped() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readValue("{\"boom\":\"val\"}", ThrowingSetterBean.class);
            fail("Expected JsonMappingException wrapping setter failure");
        } catch (JsonMappingException e) {
            assertTrue(e.getCause() instanceof IllegalArgumentException);
            assertEquals("Forced setter explosion", e.getCause().getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testCreatorExceptionWrapped() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readValue("{\"v\":\"val\"}", ThrowingCreatorBean.class);
            fail("Expected JsonMappingException wrapping creator failure");
        } catch (JsonMappingException e) {
            assertTrue(e.getCause() instanceof IllegalStateException);
            assertEquals("Forced creator explosion", e.getCause().getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testMissingTokenMethodThrows() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(SimpleBean.class);
        DeserializationConfig config = mapper.getDeserializationConfig();
        JsonParser p = mapper.getFactory().createParser("{}");
        DefaultDeserializationContext ctxt = ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(config, p, mapper.getInjectableValues());

        JsonDeserializer<Object> deser = ctxt.findRootValueDeserializer(type);
        assertTrue(deser instanceof BeanDeserializer);

        SubBeanDeserializer subDeser = new SubBeanDeserializer((BeanDeserializer) deser);
        try {
            subDeser.callMissingToken(p, ctxt);
            fail("Expected JsonMappingException from _missingToken");
        } catch (JsonMappingException expected) {
            assertNotNull(expected.getMessage());
        } finally {
            p.close();
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testUnwrappingDeserializerAndSubclassBehavior() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(SimpleBean.class);
        DeserializationConfig config = mapper.getDeserializationConfig();
        DefaultDeserializationContext ctxt = ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(config, mapper.getFactory().createParser("{}"), mapper.getInjectableValues());

        JsonDeserializer<Object> deser = ctxt.findRootValueDeserializer(type);
        assertTrue(deser instanceof BeanDeserializer);
        BeanDeserializer bd = (BeanDeserializer) deser;

        NameTransformer transformer = NameTransformer.simpleTransformer("prefix_", null);
        JsonDeserializer<Object> unwrapped = bd.unwrappingDeserializer(transformer);
        assertNotNull(unwrapped);
        assertTrue(unwrapped instanceof BeanDeserializer);
        assertNotSame(bd, unwrapped);

        // Branch: getClass() != BeanDeserializer.class returns 'this'
        SubBeanDeserializer subDeser = new SubBeanDeserializer(bd);
        JsonDeserializer<Object> subUnwrapped = subDeser.unwrappingDeserializer(transformer);
        assertSame(subDeser, subUnwrapped);
    }

    @Test(timeout = 4000)
    public void testWithMutators() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(SimpleBean.class);
        DeserializationConfig config = mapper.getDeserializationConfig();
        DefaultDeserializationContext ctxt = ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(config, mapper.getFactory().createParser("{}"), mapper.getInjectableValues());

        BeanDeserializer bd = (BeanDeserializer) ctxt.findRootValueDeserializer(type);

        BeanDeserializer withOir = bd.withObjectIdReader(null);
        assertNotNull(withOir);
        assertNotSame(bd, withOir);

        HashSet<String> ign = new HashSet<String>(Collections.singletonList("propX"));
        BeanDeserializer withIgn = bd.withIgnorableProperties(ign);
        assertNotNull(withIgn);
        assertNotSame(bd, withIgn);

        BeanDeserializerBase asArray = bd.asArrayDeserializer();
        assertNotNull(asArray);
    }
}