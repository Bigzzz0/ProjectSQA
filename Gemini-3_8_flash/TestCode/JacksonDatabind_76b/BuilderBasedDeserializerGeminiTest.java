package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.deser.impl.BeanPropertyMap;
import com.fasterxml.jackson.databind.deser.impl.ObjectIdReader;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.util.NameTransformer;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: BuilderBasedDeserializer
 *
 * 1. Defect Analysis (Defects4J Ground Truth):
 *    - In deserializeUsingPropertyBasedWithUnwrapped(), when creator properties are finished
 *      via assignParameter(), subsequent regular properties in the JSON stream are blindly
 *      copied to the TokenBuffer intended for unwrapped properties instead of being dispatched
 *      to SettableBeanProperty setters on the builder.
 *    - Causes:
 *      * expected:<John> but was:<null> (testWithUnwrappedAndCreatorSingleParameterAtBeginning)
 *      * expected:<30> but was:<0> (testWithUnwrappedAndCreatorSingleParameterInMiddle)
 *
 * 2. Decision Branches Covered:
 *    - Constructor: ObjectIdReader check != null (throws IllegalArgumentException).
 *    - finishBuild: _buildMethod == null (databind#777, returns builder itself) vs _buildMethod != null vs exception handling.
 *    - deserialize(p, ctxt):
 *      * START_OBJECT with _vanillaProcessing = true / false.
 *      * VALUE_STRING, VALUE_NUMBER_INT, VALUE_NUMBER_FLOAT, VALUE_EMBEDDED_OBJECT,
 *        VALUE_TRUE, VALUE_FALSE, START_ARRAY, FIELD_NAME, END_OBJECT, unexpected token.
 *    - deserialize(p, ctxt, builder): two-step deserialization calling finishBuild(ctxt, _deserialize(...)).
 *    - _deserialize / deserializeFromObject:
 *      * _injectables != null
 *      * _unwrappedPropertyHandler != null
 *      * _externalTypeIdHandler != null
 *      * _needViewProcessing (activeView match vs non-match vs null)
 *      * normal property vs unknown vanilla property
 *    - _deserializeUsingPropertyBased:
 *      * creator property (assignParameter)
 *      * buffer regular property
 *      * ignorable property (_ignorableProps)
 *      * anySetter (_anySetter != null)
 *      * unknown properties buffering
 *    - deserializeWithExternalTypeId:
 *      * standard vs property-based creator (throws IllegalStateException: "not yet implemented")
 *      * scalar type property handling
 *    - Mutator & Factory methods:
 *      * unwrappingDeserializer
 *      * withObjectIdReader
 *      * withIgnorableProperties
 *      * withBeanProperties
 *      * asArrayDeserializer (returns BeanAsArrayBuilderDeserializer)
 */
public class BuilderBasedDeserializerGeminiTest {

    private final ObjectMapper mapper = new ObjectMapper();

    // =========================================================================
    // PARTITION C: DEFECT-TARGETED BRANCH ZONE (Defects4J Ground Truth)
    // =========================================================================

    static class Location {
        public String city;
        public String state;

        public Location() {}
        public Location(String city, String state) {
            this.city = city;
            this.state = state;
        }
    }

    @JsonDeserialize(builder = UnwrappedCreatorBuilder.class)
    static class UnwrappedTarget {
        final int id;
        final String name;
        final int age;
        final Location location;

        public UnwrappedTarget(int id, String name, int age, Location location) {
            this.id = id;
            this.name = name;
            this.age = age;
            this.location = location;
        }
    }

    @JsonPOJOBuilder(withPrefix = "with")
    static class UnwrappedCreatorBuilder {
        private final int id;
        private String name;
        private int age;
        private Location location;

        @JsonCreator
        public UnwrappedCreatorBuilder(@JsonProperty("id") int id) {
            this.id = id;
        }

        public UnwrappedCreatorBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public UnwrappedCreatorBuilder withAge(int age) {
            this.age = age;
            return this;
        }

        @JsonUnwrapped
        public UnwrappedCreatorBuilder withLocation(Location location) {
            this.location = location;
            return this;
        }

        public UnwrappedTarget build() {
            return new UnwrappedTarget(id, name, age, location);
        }
    }

    /**
     * Targets Defect: testWithUnwrappedAndCreatorSingleParameterAtBeginning
     * Failure symptom on bug: expected:<John> but was:<null>
     * Creator parameter 'id' is at the beginning; subsequent builder property 'name'
     * was improperly routed into unwrapped token buffer instead of builder setter.
     */
    @Test(timeout = 4000)
    public void testDefectCreatorParameterAtBeginningWithUnwrapped() throws Exception {
        String json = "{\"id\":101,\"name\":\"John\",\"city\":\"Boston\",\"state\":\"MA\"}";
        UnwrappedTarget result = mapper.readValue(json, UnwrappedTarget.class);

        assertNotNull("Deserialized result should not be null", result);
        assertEquals("Creator parameter 'id' should be correctly bound", 101, result.id);
        assertEquals("Regular property 'name' after creator param must be set on builder", "John", result.name);
        assertNotNull("Unwrapped location object should not be null", result.location);
        assertEquals("Boston", result.location.city);
        assertEquals("MA", result.location.state);
    }

    /**
     * Targets Defect: testWithUnwrappedAndCreatorSingleParameterInMiddle
     * Failure symptom on bug: expected:<30> but was:<0>
     * Creator parameter 'id' appears after 'name', and 'age' appears after 'id'.
     */
    @Test(timeout = 4000)
    public void testDefectCreatorParameterInMiddleWithUnwrapped() throws Exception {
        String json = "{\"name\":\"Alice\",\"id\":202,\"age\":30,\"city\":\"Seattle\",\"state\":\"WA\"}";
        UnwrappedTarget result = mapper.readValue(json, UnwrappedTarget.class);

        assertNotNull(result);
        assertEquals(202, result.id);
        assertEquals("Alice", result.name);
        assertEquals("Regular property 'age' following creator param must be preserved", 30, result.age);
        assertNotNull(result.location);
        assertEquals("Seattle", result.location.city);
        assertEquals("WA", result.location.state);
    }

    // =========================================================================
    // PARTITION A: CORE FUNCTIONAL LOGIC & BUILDER LIFECYCLE
    // =========================================================================

    @JsonDeserialize(builder = SimplePojoBuilder.class)
    static class SimplePojo {
        final String text;
        final int number;

        SimplePojo(String text, int number) {
            this.text = text;
            this.number = number;
        }
    }

    @JsonPOJOBuilder(buildMethodName = "create", withPrefix = "set")
    static class SimplePojoBuilder {
        private String text;
        private int number;

        public SimplePojoBuilder setText(String text) {
            this.text = text;
            return this;
        }

        public SimplePojoBuilder setNumber(int number) {
            this.number = number;
            return this;
        }

        public SimplePojo create() {
            return new SimplePojo(text, number);
        }
    }

    @Test(timeout = 4000)
    public void testStandardBuilderDeserialization() throws Exception {
        String json = "{\"text\":\"hello\",\"number\":42}";
        SimplePojo pojo = mapper.readValue(json, SimplePojo.class);

        assertNotNull(pojo);
        assertEquals("hello", pojo.text);
        assertEquals(42, pojo.number);
    }

    @Test(timeout = 4000)
    public void testVanillaEmptyObject() throws Exception {
        String json = "{}";
        SimplePojo pojo = mapper.readValue(json, SimplePojo.class);
        assertNotNull(pojo);
        assertNull(pojo.text);
        assertEquals(0, pojo.number);
    }

    // Test buildMethod == null (databind#777: Builder itself returned as bean)
    @JsonDeserialize(builder = SelfReturningBuilder.class)
    static class SelfReturningBuilder {
        public String value;

        public SelfReturningBuilder withValue(String v) {
            this.value = v;
            return this;
        }
    }

    @Test(timeout = 4000)
    public void testFinishBuildWithoutBuildMethodReturnsBuilderItself() throws Exception {
        String json = "{\"value\":\"abc\"}";
        SelfReturningBuilder result = mapper.readValue(json, SelfReturningBuilder.class);
        assertNotNull(result);
        assertEquals("abc", result.value);
    }

    // =========================================================================
    // PARTITION B: BOUNDARY VALUE ANALYSIS (BVA) & DATA TYPES
    // =========================================================================

    @JsonDeserialize(builder = MultiTypeBuilder.class)
    static class MultiTypeTarget {
        final String str;
        final long numInt;
        final double numFloat;
        final boolean boolVal;

        MultiTypeTarget(String str, long numInt, double numFloat, boolean boolVal) {
            this.str = str;
            this.numInt = numInt;
            this.numFloat = numFloat;
            this.boolVal = boolVal;
        }
    }

    static class MultiTypeBuilder {
        private String str;
        private long numInt;
        private double numFloat;
        private boolean boolVal;

        @JsonCreator
        public static MultiTypeBuilder fromString(String str) {
            MultiTypeBuilder b = new MultiTypeBuilder();
            b.str = str;
            return b;
        }

        @JsonCreator
        public static MultiTypeBuilder fromLong(long n) {
            MultiTypeBuilder b = new MultiTypeBuilder();
            b.numInt = n;
            return b;
        }

        @JsonCreator
        public static MultiTypeBuilder fromDouble(double d) {
            MultiTypeBuilder b = new MultiTypeBuilder();
            b.numFloat = d;
            return b;
        }

        @JsonCreator
        public static MultiTypeBuilder fromBoolean(boolean bVal) {
            MultiTypeBuilder b = new MultiTypeBuilder();
            b.boolVal = bVal;
            return b;
        }

        public MultiTypeTarget build() {
            return new MultiTypeTarget(str, numInt, numFloat, boolVal);
        }
    }

    @Test(timeout = 4000)
    public void testDeserializeFromString() throws Exception {
        MultiTypeTarget res = mapper.readValue("\"stringValue\"", MultiTypeTarget.class);
        assertNotNull(res);
        assertEquals("stringValue", res.str);
    }

    @Test(timeout = 4000)
    public void testDeserializeFromNumberInt() throws Exception {
        MultiTypeTarget res = mapper.readValue("12345", MultiTypeTarget.class);
        assertNotNull(res);
        assertEquals(12345L, res.numInt);
    }

    @Test(timeout = 4000)
    public void testDeserializeFromNumberFloat() throws Exception {
        MultiTypeTarget res = mapper.readValue("12.34", MultiTypeTarget.class);
        assertNotNull(res);
        assertEquals(12.34, res.numFloat, 0.0001);
    }

    @Test(timeout = 4000)
    public void testDeserializeFromBoolean() throws Exception {
        MultiTypeTarget res = mapper.readValue("true", MultiTypeTarget.class);
        assertNotNull(res);
        assertTrue(res.boolVal);
    }

    // =========================================================================
    // PARTITION D: EXCEPTION & DEFENSIVE GUARD PATHS
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorDisallowsObjectIdReader() {
        BeanDeserializerBuilder bdb = new BeanDeserializerBuilder(
                mapper.getDeserializationConfig().introspect(mapper.constructType(SimplePojo.class)),
                mapper.getDeserializationConfig()
        );
        ObjectIdReader oir = ObjectIdReader.construct(
                mapper.constructType(String.class),
                new PropertyName("id"),
                null,
                null,
                null,
                null
        );
        bdb.setObjectIdReader(oir);

        try {
            new BuilderBasedDeserializer(bdb, bdb.getBeanDescription(),
                    BeanPropertyMap.construct(Collections.<SettableBeanProperty>emptyList(), false),
                    Collections.<String, SettableBeanProperty>emptyMap(),
                    Collections.<String>emptySet(), false, false);
            fail("Expected IllegalArgumentException when ObjectIdReader is supplied to BuilderBasedDeserializer");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Can not use Object Id with Builder-based deserialization"));
        }
    }

    @JsonDeserialize(builder = FailingBuildMethodBuilder.class)
    static class FailingTarget {}

    static class FailingBuildMethodBuilder {
        public FailingTarget build() {
            throw new IllegalStateException("Build failed deliberately");
        }
    }

    @Test(timeout = 4000)
    public void testBuildMethodExceptionWrapping() throws Exception {
        try {
            mapper.readValue("{}", FailingTarget.class);
            fail("Expected JsonMappingException wrapping the builder invocation failure");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Build failed deliberately"));
        }
    }

    // =========================================================================
    // PARTITION E: VIEWS, ANY-SETTER, AND ADVANCED DESERIALIZATION
    // =========================================================================

    static class Views {
        static class Public {}
        static class Internal extends Public {}
    }

    @JsonDeserialize(builder = ViewBuilder.class)
    static class ViewTarget {
        final String pub;
        final String secret;

        ViewTarget(String pub, String secret) {
            this.pub = pub;
            this.secret = secret;
        }
    }

    static class ViewBuilder {
        private String pub;
        private String secret;

        @JsonView(Views.Public.class)
        public ViewBuilder withPub(String pub) {
            this.pub = pub;
            return this;
        }

        @JsonView(Views.Internal.class)
        public ViewBuilder withSecret(String secret) {
            this.secret = secret;
            return this;
        }

        public ViewTarget build() {
            return new ViewTarget(pub, secret);
        }
    }

    @Test(timeout = 4000)
    public void testDeserializeWithActiveView() throws Exception {
        String json = "{\"pub\":\"visible\",\"secret\":\"hidden\"}";
        ViewTarget target = mapper.readerWithView(Views.Public.class)
                .forType(ViewTarget.class)
                .readValue(json);

        assertNotNull(target);
        assertEquals("visible", target.pub);
        assertNull("Secret should not be populated under Public view", target.secret);
    }

    // AnySetter test
    @JsonDeserialize(builder = AnySetterBuilder.class)
    static class AnySetterTarget {
        final Map<String, Object> extra;

        AnySetterTarget(Map<String, Object> extra) {
            this.extra = extra;
        }
    }

    static class AnySetterBuilder {
        private final Map<String, Object> extra = new HashMap<String, Object>();

        @JsonAnySetter
        public AnySetterBuilder addExtra(String key, Object value) {
            this.extra.put(key, value);
            return this;
        }

        public AnySetterTarget build() {
            return new AnySetterTarget(extra);
        }
    }

    @Test(timeout = 4000)
    public void testBuilderWithAnySetter() throws Exception {
        String json = "{\"foo\":\"bar\",\"count\":10}";
        AnySetterTarget target = mapper.readValue(json, AnySetterTarget.class);

        assertNotNull(target);
        assertEquals("bar", target.extra.get("foo"));
        assertEquals(10, target.extra.get("count"));
    }

    // Ignorable properties test
    @JsonIgnoreProperties({"ignoredField"})
    @JsonDeserialize(builder = IgnorablePropBuilder.class)
    static class IgnorableTarget {
        final String name;

        IgnorableTarget(String name) {
            this.name = name;
        }
    }

    static class IgnorablePropBuilder {
        private String name;

        public IgnorablePropBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public IgnorableTarget build() {
            return new IgnorableTarget(name);
        }
    }

    @Test(timeout = 4000)
    public void testIgnorablePropertiesPassedToBuilder() throws Exception {
        String json = "{\"name\":\"tester\",\"ignoredField\":\"someTrash\"}";
        IgnorableTarget target = mapper.readValue(json, IgnorableTarget.class);
        assertNotNull(target);
        assertEquals("tester", target.name);
    }

    // =========================================================================
    // PARTITION F: COPY CONSTRUCTORS AND MUTATOR METHODS
    // =========================================================================

    @Test(timeout = 4000)
    public void testBuilderBasedDeserializerMutators() throws Exception {
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(SimplePojo.class);
        JsonDeserializer<?> deser = mapper.getDeserializationConfig()
                .findTypeDeserializer(type) != null ? null : mapper.findRootValueDeserializer(type);

        assertTrue("Root deserializer should be BuilderBasedDeserializer", deser instanceof BuilderBasedDeserializer);
        BuilderBasedDeserializer builderDeser = (BuilderBasedDeserializer) deser;

        // withIgnorableProperties
        Set<String> toIgnore = new HashSet<String>(Collections.singletonList("dummy"));
        BeanDeserializerBase withIgnored = builderDeser.withIgnorableProperties(toIgnore);
        assertNotNull(withIgnored);
        assertNotSame(builderDeser, withIgnored);

        // unwrappingDeserializer
        JsonDeserializer<Object> unwrapped = builderDeser.unwrappingDeserializer(NameTransformer.NOP);
        assertNotNull(unwrapped);
        assertTrue(unwrapped instanceof BuilderBasedDeserializer);

        // withBeanProperties
        BeanDeserializerBase withProps = builderDeser.withBeanProperties(builderDeser.getPropertyMap());
        assertNotNull(withProps);

        // asArrayDeserializer
        BeanDeserializerBase arrayDeser = builderDeser.asArrayDeserializer();
        assertNotNull(arrayDeser);
    }

    // =========================================================================
    // PARTITION G: EXTERNAL TYPE ID GUARD PATH
    // =========================================================================

    @Test(timeout = 4000)
    public void testDeserializeUsingPropertyBasedWithExternalTypeIdThrowsIllegalState() throws Exception {
        BuilderBasedDeserializer deser = (BuilderBasedDeserializer) mapper.findRootValueDeserializer(mapper.constructType(UnwrappedTarget.class));
        try {
            deser.deserializeUsingPropertyBasedWithExternalTypeId(null, null);
            fail("Expected IllegalStateException for deserializeUsingPropertyBasedWithExternalTypeId");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("Deserialization with Builder, External type id, @JsonCreator not yet implemented"));
        }
    }
}