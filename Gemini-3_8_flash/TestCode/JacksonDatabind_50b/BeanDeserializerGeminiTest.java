/* [Branch & Defect Analysis Matrix]
 * Target Class: com.fasterxml.jackson.databind.deser.BeanDeserializer
 *
 * Decision Branches & Conditions Targeted:
 * 1. deserialize(JsonParser, DeserializationContext):
 *    - isExpectedStartObjectToken() -> vanilla vs non-vanilla (ObjectIdReader != null, deserializeFromObject).
 *    - _deserializeOther() branches:
 *      - VALUE_STRING -> deserializeFromString()
 *      - VALUE_NUMBER_INT -> deserializeFromNumber()
 *      - VALUE_NUMBER_FLOAT -> deserializeFromDouble()
 *      - VALUE_EMBEDDED_OBJECT -> deserializeFromEmbedded()
 *      - VALUE_TRUE / VALUE_FALSE -> deserializeFromBoolean()
 *      - VALUE_NULL -> deserializeFromNull() (custom codec check)
 *      - START_ARRAY -> deserializeFromArray() (delegating creator)
 *      - FIELD_NAME / END_OBJECT -> vanillaDeserialize vs deserializeWithObjectId vs deserializeFromObject
 *      - default -> ctxt.handleUnexpectedToken()
 * 2. deserialize(JsonParser, DeserializationContext, Object bean) [Updating]:
 *    - injectables != null, unwrappedPropertyHandler != null, externalTypeIdHandler != null
 *    - isExpectedStartObjectToken() true vs false (FIELD_NAME vs non-FIELD_NAME)
 *    - _needViewProcessing true with activeView match/mismatch
 *    - SettableBeanProperty found vs null (handleUnknownVanilla)
 * 3. deserializeFromObject(JsonParser, DeserializationContext):
 *    - _objectIdReader != null && maySerializeAsObject() && isValidReferencePropertyName()
 *    - _nonStandardCreation true (unwrapped, externalTypeId, nonDefault, injectables)
 *    - canReadObjectId() true (native ID)
 *    - _needViewProcessing with active view
 * 4. _deserializeUsingPropertyBased():
 *    - Creator properties assigned until completion
 *    - Creator returning null -> _creatorReturnedNullException()
 *    - Creator returning polymorphic subtype -> handlePolymorphic()
 *    - ID property via buffer.readIdProperty()
 *    - Regular property buffering with forward reference handling (Defects4J #1261)
 *    - Ignorable property handling (_ignorableProps)
 *    - AnySetter property buffering (_anySetter)
 *    - Unknown property collection into TokenBuffer
 * 5. deserializeWithUnwrapped() and deserializeUsingPropertyBasedWithUnwrapped():
 *    - Delegating creator path
 *    - Standard unwrapped bean creation & reader updating
 *    - View filtering within unwrapped
 *    - AnySetter handling within unwrapped
 * 6. deserializeWithExternalTypeId() and deserializeUsingPropertyBasedWithExternalTypeId():
 *    - Delegating creator precedence
 *    - Type property value vs regular property value
 *    - Complete resolution with/without creator buffer
 * 7. Life-cycle & Mutator methods:
 *    - unwrappingDeserializer(), withObjectIdReader(), withIgnorableProperties(),
 *      withBeanProperties(), asArrayDeserializer()
 *
 * Known Defect Targeted:
 * - Defects4J databind #1261: Forward reference resolution with Object Identity during
 *   property-based deserialization (Parent/Child circular reference in collections).
 */

package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.impl.ObjectIdReader;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.util.NameTransformer;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;

public class BeanDeserializerGeminiTest {

    private final ObjectMapper mapper = new ObjectMapper();

    // =========================================================================
    // Views and Test Data Structures
    // =========================================================================

    interface PublicView {}
    interface InternalView extends PublicView {}

    static class SimpleBean {
        public String name;
        public int age;

        public SimpleBean() {}

        public SimpleBean(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }

    static class ViewBean {
        @JsonView(PublicView.class)
        public String pub;

        @JsonView(InternalView.class)
        public String priv;
    }

    static class IgnorableBean {
        public String kept;
        @JsonIgnore
        public String ignored;
    }

    static class AnySetterBean {
        public String id;
        public Map<String, Object> extra = new HashMap<>();

        @JsonAnySetter
        public void addExtra(String key, Object value) {
            extra.put(key, value);
        }
    }

    static class StringCreatorBean {
        public final String value;

        @JsonCreator
        public StringCreatorBean(String v) {
            this.value = v;
        }
    }

    static class NumberCreatorBean {
        public final long intVal;
        public final double floatVal;

        @JsonCreator
        public NumberCreatorBean(long v) {
            this.intVal = v;
            this.floatVal = 0.0;
        }

        @JsonCreator
        public NumberCreatorBean(double v) {
            this.intVal = 0;
            this.floatVal = v;
        }
    }

    static class BooleanCreatorBean {
        public final boolean flag;

        @JsonCreator
        public BooleanCreatorBean(boolean b) {
            this.flag = b;
        }
    }

    static class ArrayDelegatingBean {
        public final List<String> items;

        @JsonCreator
        public ArrayDelegatingBean(List<String> items) {
            this.items = items;
        }
    }

    static class NullReturningCreatorBean {
        @JsonCreator
        public static NullReturningCreatorBean create(@JsonProperty("name") String name) {
            return null; // Triggers _creatorReturnedNullException
        }
    }

    static class PolymorphicBase {
        public String type;

        @JsonCreator
        public static PolymorphicBase create(@JsonProperty("type") String type, @JsonProperty("extra") String extra) {
            if ("sub".equals(type)) {
                return new PolymorphicSub(type, extra);
            }
            PolymorphicBase base = new PolymorphicBase();
            base.type = type;
            return base;
        }
    }

    static class PolymorphicSub extends PolymorphicBase {
        public String extra;

        public PolymorphicSub(String type, String extra) {
            this.type = type;
            this.extra = extra;
        }
    }

    static class UnwrappedLocation {
        public double lat;
        public double lon;
    }

    static class UnwrappedBean {
        public String id;
        @JsonUnwrapped
        public UnwrappedLocation location;
    }

    static class UnwrappedWithCreatorBean {
        public final String id;
        @JsonUnwrapped
        public final UnwrappedLocation location;

        @JsonCreator
        public UnwrappedWithCreatorBean(@JsonProperty("id") String id, @JsonProperty("lat") double lat, @JsonProperty("lon") double lon) {
            this.id = id;
            this.location = new UnwrappedLocation();
            this.location.lat = lat;
            this.location.lon = lon;
        }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "extType")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = SubExt1.class, name = "type1"),
            @JsonSubTypes.Type(value = SubExt2.class, name = "type2")
    })
    interface ExternalBase {}

    static class SubExt1 implements ExternalBase {
        public int val1;
    }

    static class SubExt2 implements ExternalBase {
        public String val2;
    }

    static class ExternalContainer {
        public String extType;
        public ExternalBase bean;
    }

    static class ExternalCreatorContainer {
        public final String extType;
        public final ExternalBase bean;

        @JsonCreator
        public ExternalCreatorContainer(@JsonProperty("extType") String extType, @JsonProperty("bean") ExternalBase bean) {
            this.extType = extType;
            this.bean = bean;
        }
    }

    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    static class ArrayFormatBean {
        public String a;
        public int b;
    }

    // Structures for databind#1261
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    public static class Parent1261 {
        public int id;
        public List<Child1261> children;

        public Parent1261() {}

        @JsonCreator
        public Parent1261(@JsonProperty("id") int id) {
            this.id = id;
        }
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    public static class Child1261 {
        public int id;
        public Parent1261 parent;

        public Child1261() {}

        @JsonCreator
        public Child1261(@JsonProperty("id") int id, @JsonProperty("parent") Parent1261 parent) {
            this.id = id;
            this.parent = parent;
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testVanillaDeserializationSuccess() throws Exception {
        String json = "{\"name\":\"Alice\",\"age\":30}";
        SimpleBean result = mapper.readValue(json, SimpleBean.class);
        assertNotNull(result);
        assertEquals("Alice", result.name);
        assertEquals(30, result.age);
    }

    @Test(timeout = 4000)
    public void testUpdatingDeserializationExistingObject() throws Exception {
        SimpleBean existing = new SimpleBean("Bob", 20);
        String json = "{\"age\":25}";
        SimpleBean updated = mapper.readerForUpdating(existing).readValue(json);
        assertSame(existing, updated);
        assertEquals("Bob", updated.name);
        assertEquals(25, updated.age);
    }

    @Test(timeout = 4000)
    public void testUpdatingDeserializationEmptyObject() throws Exception {
        SimpleBean existing = new SimpleBean("Charlie", 40);
        String json = "{}";
        SimpleBean updated = mapper.readerForUpdating(existing).readValue(json);
        assertSame(existing, updated);
        assertEquals("Charlie", updated.name);
        assertEquals(40, updated.age);
    }

    @Test(timeout = 4000)
    public void testArrayFormatDeserialization() throws Exception {
        String json = "[\"hello\",42]";
        ArrayFormatBean result = mapper.readValue(json, ArrayFormatBean.class);
        assertNotNull(result);
        assertEquals("hello", result.a);
        assertEquals(42, result.b);
    }

    @Test(timeout = 4000)
    public void testIgnorablePropertiesSkipped() throws Exception {
        String json = "{\"kept\":\"ok\",\"ignored\":\"skipMe\"}";
        IgnorableBean result = mapper.readValue(json, IgnorableBean.class);
        assertNotNull(result);
        assertEquals("ok", result.kept);
        assertNull(result.ignored);
    }

    @Test(timeout = 4000)
    public void testAnySetterHandling() throws Exception {
        String json = "{\"id\":\"item1\",\"foo\":\"bar\",\"num\":123}";
        AnySetterBean result = mapper.readValue(json, AnySetterBean.class);
        assertNotNull(result);
        assertEquals("item1", result.id);
        assertEquals("bar", result.extra.get("foo"));
        assertEquals(123, result.extra.get("num"));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Token Handling
    // =========================================================================

    @Test(timeout = 4000)
    public void testDeserializeFromStringToken() throws Exception {
        StringCreatorBean bean = mapper.readValue("\"stringValue\"", StringCreatorBean.class);
        assertNotNull(bean);
        assertEquals("stringValue", bean.value);
    }

    @Test(timeout = 4000)
    public void testDeserializeFromIntegerToken() throws Exception {
        NumberCreatorBean bean = mapper.readValue("12345", NumberCreatorBean.class);
        assertNotNull(bean);
        assertEquals(12345L, bean.intVal);
    }

    @Test(timeout = 4000)
    public void testDeserializeFromFloatToken() throws Exception {
        NumberCreatorBean bean = mapper.readValue("12.34", NumberCreatorBean.class);
        assertNotNull(bean);
        assertEquals(12.34, bean.floatVal, 0.0001);
    }

    @Test(timeout = 4000)
    public void testDeserializeFromBooleanToken() throws Exception {
        BooleanCreatorBean trueBean = mapper.readValue("true", BooleanCreatorBean.class);
        assertNotNull(trueBean);
        assertTrue(trueBean.flag);

        BooleanCreatorBean falseBean = mapper.readValue("false", BooleanCreatorBean.class);
        assertNotNull(falseBean);
        assertFalse(falseBean.flag);
    }

    @Test(timeout = 4000)
    public void testDeserializeFromArrayToken() throws Exception {
        ArrayDelegatingBean bean = mapper.readValue("[\"x\",\"y\",\"z\"]", ArrayDelegatingBean.class);
        assertNotNull(bean);
        assertEquals(Arrays.asList("x", "y", "z"), bean.items);
    }

    @Test(timeout = 4000)
    public void testDeserializeFromNullTokenNonXml() throws Exception {
        // Standard parser without custom codec returns null for VALUE_NULL token
        SimpleBean result = mapper.readValue("null", SimpleBean.class);
        assertNull(result);
    }

    @Test(timeout = 4000)
    public void testActiveViewsPublicOnly() throws Exception {
        String json = "{\"pub\":\"publicVal\",\"priv\":\"privateVal\"}";
        ViewBean bean = mapper.readerWithView(PublicView.class).forType(ViewBean.class).readValue(json);
        assertNotNull(bean);
        assertEquals("publicVal", bean.pub);
        assertNull(bean.priv);
    }

    @Test(timeout = 4000)
    public void testActiveViewsInternalAccess() throws Exception {
        String json = "{\"pub\":\"publicVal\",\"priv\":\"privateVal\"}";
        ViewBean bean = mapper.readerWithView(InternalView.class).forType(ViewBean.class).readValue(json);
        assertNotNull(bean);
        assertEquals("publicVal", bean.pub);
        assertEquals("privateVal", bean.priv);
    }

    @Test(timeout = 4000)
    public void testActiveViewsOnUpdating() throws Exception {
        ViewBean existing = new ViewBean();
        String json = "{\"pub\":\"newPub\",\"priv\":\"newPriv\"}";
        ViewBean updated = mapper.readerWithView(PublicView.class).forType(ViewBean.class)
                .readerForUpdating(existing).readValue(json);
        assertSame(existing, updated);
        assertEquals("newPub", updated.pub);
        assertNull(updated.priv);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Zone (Defects4J #1261)
    // =========================================================================

    @Test(timeout = 4000)
    public void testObjectIds1261DefectTarget() throws Exception {
        // Ground truth bug: Forward reference resolution within lists during property-based creator execution
        final String json = "{\"id\":1,\"children\":[{\"id\":100,\"parent\":1}]}";
        Parent1261 parent = mapper.readValue(json, Parent1261.class);
        assertNotNull("Parent should deserialize successfully", parent);
        assertEquals(1, parent.id);
        assertNotNull("Children list should be populated", parent.children);
        assertEquals(1, parent.children.size());

        Child1261 child = parent.children.get(0);
        assertNotNull("Child should deserialize successfully", child);
        assertEquals(100, child.id);
        assertSame("Child's parent reference must resolve to outer Parent", parent, child.parent);
    }

    @Test(timeout = 4000)
    public void testObjectIds1261ReversedPropertyOrder() throws Exception {
        // Test when children appear before id in creator-based bean
        final String json = "{\"children\":[{\"id\":200,\"parent\":2}],\"id\":2}";
        Parent1261 parent = mapper.readValue(json, Parent1261.class);
        assertNotNull(parent);
        assertEquals(2, parent.id);
        assertNotNull(parent.children);
        assertEquals(1, parent.children.size());
        Child1261 child = parent.children.get(0);
        assertEquals(200, child.id);
        assertSame(parent, child.parent);
    }

    // =========================================================================
    // Partition D: Advanced Features & Edge Exception Branches
    // =========================================================================

    @Test(timeout = 4000)
    public void testPolymorphicPropertyBasedCreator() throws Exception {
        String json = "{\"type\":\"sub\",\"extra\":\"bonus\"}";
        PolymorphicBase result = mapper.readValue(json, PolymorphicBase.class);
        assertNotNull(result);
        assertTrue(result instanceof PolymorphicSub);
        assertEquals("sub", result.type);
        assertEquals("bonus", ((PolymorphicSub) result).extra);
    }

    @Test(timeout = 4000)
    public void testCreatorReturningNullThrowsMappingException() throws Exception {
        try {
            mapper.readValue("{\"name\":\"test\"}", NullReturningCreatorBean.class);
            fail("Expected JsonMappingException due to creator returning null");
        } catch (JsonMappingException expected) {
            assertTrue(expected.getMessage().contains("JSON Creator returned null")
                    || expected.getMessage().contains("null"));
        }
    }

    @Test(timeout = 4000)
    public void testUnwrappedDeserializationDefault() throws Exception {
        String json = "{\"id\":\"u1\",\"lat\":12.5,\"lon\":-45.2}";
        UnwrappedBean bean = mapper.readValue(json, UnwrappedBean.class);
        assertNotNull(bean);
        assertEquals("u1", bean.id);
        assertNotNull(bean.location);
        assertEquals(12.5, bean.location.lat, 0.001);
        assertEquals(-45.2, bean.location.lon, 0.001);
    }

    @Test(timeout = 4000)
    public void testUnwrappedDeserializationUpdating() throws Exception {
        UnwrappedBean existing = new UnwrappedBean();
        existing.id = "original";
        String json = "{\"lat\":55.5,\"lon\":33.3}";
        UnwrappedBean updated = mapper.readerForUpdating(existing).readValue(json);
        assertSame(existing, updated);
        assertEquals("original", updated.id);
        assertNotNull(updated.location);
        assertEquals(55.5, updated.location.lat, 0.001);
    }

    @Test(timeout = 4000)
    public void testUnwrappedDeserializationWithCreator() throws Exception {
        String json = "{\"id\":\"c1\",\"lat\":10.0,\"lon\":20.0}";
        UnwrappedWithCreatorBean bean = mapper.readValue(json, UnwrappedWithCreatorBean.class);
        assertNotNull(bean);
        assertEquals("c1", bean.id);
        assertNotNull(bean.location);
        assertEquals(10.0, bean.location.lat, 0.001);
        assertEquals(20.0, bean.location.lon, 0.001);
    }

    @Test(timeout = 4000)
    public void testExternalTypeIdDeserialization() throws Exception {
        String json = "{\"extType\":\"type1\",\"bean\":{\"val1\":999}}";
        ExternalContainer container = mapper.readValue(json, ExternalContainer.class);
        assertNotNull(container);
        assertEquals("type1", container.extType);
        assertTrue(container.bean instanceof SubExt1);
        assertEquals(999, ((SubExt1) container.bean).val1);
    }

    @Test(timeout = 4000)
    public void testExternalTypeIdDeserializationWithCreator() throws Exception {
        String json = "{\"bean\":{\"val2\":\"data\"},\"extType\":\"type2\"}";
        ExternalCreatorContainer container = mapper.readValue(json, ExternalCreatorContainer.class);
        assertNotNull(container);
        assertEquals("type2", container.extType);
        assertTrue(container.bean instanceof SubExt2);
        assertEquals("data", ((SubExt2) container.bean).val2);
    }

    @Test(timeout = 4000)
    public void testUnexpectedTokenThrowsException() throws Exception {
        try {
            // Passing boolean into SimpleBean (no boolean creator defined)
            mapper.readValue("true", SimpleBean.class);
            fail("Expected JsonMappingException for unexpected token");
        } catch (JsonMappingException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testUnknownPropertyFailsWhenConfigured() throws Exception {
        ObjectMapper strictMapper = new ObjectMapper();
        strictMapper.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        try {
            strictMapper.readValue("{\"unknownProp\":\"bad\"}", SimpleBean.class);
            fail("Expected UnrecognizedPropertyException");
        } catch (UnrecognizedPropertyException expected) {
            assertEquals("unknownProp", expected.getPropertyName());
        }
    }

    // =========================================================================
    // Partition E: Internal Lifecycle & Mutator Methods
    // =========================================================================

    @Test(timeout = 4000)
    public void testBeanDeserializerInternalMutators() throws Exception {
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(SimpleBean.class);
        JsonDeserializer<?> deser = mapper.findRootValueDeserializer(type);

        assertTrue("Root value deserializer should be BeanDeserializer", deser instanceof BeanDeserializer);
        BeanDeserializer beanDeser = (BeanDeserializer) deser;

        // Test unwrappingDeserializer
        NameTransformer transformer = NameTransformer.simpleTransformer("prefix_", "");
        JsonDeserializer<Object> unwrapping = beanDeser.unwrappingDeserializer(transformer);
        assertNotNull(unwrapping);
        assertTrue(unwrapping instanceof BeanDeserializer);

        // Test withObjectIdReader
        BeanDeserializer withOid = beanDeser.withObjectIdReader(null);
        assertNotNull(withOid);

        // Test withIgnorableProperties
        Set<String> ignorables = new HashSet<>(Collections.singletonList("dummyProp"));
        BeanDeserializer withIgnored = beanDeser.withIgnorableProperties(ignorables);
        assertNotNull(withIgnored);

        // Test asArrayDeserializer
        BeanDeserializerBase asArray = beanDeser.asArrayDeserializer();
        assertNotNull(asArray);
        assertTrue(asArray instanceof BeanAsArrayDeserializer);

        // Test _creatorReturnedNullException lazy initialization
        Exception npe1 = beanDeser._creatorReturnedNullException();
        assertNotNull(npe1);
        assertTrue(npe1 instanceof NullPointerException);
        assertEquals("JSON Creator returned null", npe1.getMessage());
        Exception npe2 = beanDeser._creatorReturnedNullException();
        assertSame("Should return cached exception instance", npe1, npe2);
    }
}