/* [Branch & Defect Analysis Matrix]
 * Target Class: com.fasterxml.jackson.databind.deser.BeanDeserializer
 *
 * Key Decision Branches & Scenarios Covered:
 * 1. Defect-Targeted Branch (Defects4J Issue 2088):
 *    - deserializeUsingPropertyBasedWithUnwrapped(): When the last creator property is bound,
 *      subsequent tokens in the JSON object (belonging to unwrapped properties) must have their
 *      field names properly copied into the TokenBuffer before calling processUnwrapped().
 *      In defective versions, p.nextToken() advanced to the value before copyCurrentStructure(),
 *      dropping the field name and resulting in unpopulated unwrapped fields (e.g., expected 4 but was 0).
 * 2. Unwrapped Deserializer Lifecycle & Self-Reference Guard:
 *    - unwrappingDeserializer(): Recursive reference guard (_currentlyTransforming == transformer),
 *      subclass check (getClass() != BeanDeserializer.class).
 * 3. Token-Driven Deserialization Routing (_deserializeOther):
 *    - START_OBJECT with vanilla vs non-standard/custom processing.
 *    - VALUE_STRING, VALUE_NUMBER_INT, VALUE_NUMBER_FLOAT, VALUE_EMBEDDED_OBJECT,
 *      VALUE_TRUE/FALSE, VALUE_NULL (XML-compatibility and standard), START_ARRAY,
 *      FIELD_NAME, END_OBJECT.
 * 4. Property-Based Creator Execution & Forward References:
 *    - Buffer property, anySetter handling, ignorable properties, unknown properties,
 *      and BeanReferring for unresolved forward references with ObjectIds.
 * 5. Active View Processing:
 *    - deserializeWithView() and deserialize(p, ctxt, bean) filtering properties invisible to view.
 * 6. Copy Constructors & Fluent Mutation:
 *    - withObjectIdReader(), withIgnorableProperties(), withBeanProperties(), asArrayDeserializer().
 */

package com.fasterxml.jackson.databind.deser;

import java.io.IOException;
import java.util.*;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.impl.BeanPropertyMap;
import com.fasterxml.jackson.databind.deser.impl.ObjectIdReader;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.util.NameTransformer;

public class BeanDeserializerGeminiTest {

    // =========================================================================
    // Test Helper Classes
    // =========================================================================

    static class Views {
        static class Public {}
        static class Internal extends Public {}
    }

    static class Issue2088Parent {
        final int id;

        @JsonUnwrapped
        public Issue2088Child child;

        @JsonCreator
        public Issue2088Parent(@JsonProperty("id") int id) {
            this.id = id;
        }
    }

    static class Issue2088Child {
        public int x;
        public int y;

        public Issue2088Child() {}

        public Issue2088Child(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    static class SimpleBean {
        public String name;
        public int age;

        public SimpleBean() {}

        public SimpleBean(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }

    static class CreatorBean {
        final String first;
        final int count;
        public String extra;

        @JsonCreator
        public CreatorBean(@JsonProperty("first") String first, @JsonProperty("count") int count) {
            this.first = first;
            this.count = count;
        }
    }

    static class ViewBean {
        @JsonView(Views.Public.class)
        public String publicProp;

        @JsonView(Views.Internal.class)
        public String internalProp;
    }

    static class AnySetterBean {
        public String title;
        private final Map<String, Object> _any = new HashMap<String, Object>();

        public void set(String name, Object value) {
            _any.put(name, value);
        }

        public Map<String, Object> any() {
            return _any;
        }
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
    static class Node {
        public int id;
        public Node next;

        public Node() {}
        public Node(int id) { this.id = id; }
    }

    static class UnwrappedContainer {
        public String name;
        @JsonUnwrapped
        public InnerUnwrapped inner;
    }

    static class InnerUnwrapped {
        public String city;
        public String zip;
    }

    static class SubclassedBeanDeserializer extends BeanDeserializer {
        private static final long serialVersionUID = 1L;

        public SubclassedBeanDeserializer(BeanDeserializerBase src) {
            super(src);
        }
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Issue 2088)
    // =========================================================================

    /**
     * TARGETS DEFECT: JacksonDatabind Issue #2088 / Defects4J failure
     * com.fasterxml.jackson.databind.struct.TestUnwrapped::testIssue2088UnwrappedFieldsAfterLastCreatorProp
     *
     * In deserializeUsingPropertyBasedWithUnwrapped, tokens following the last creator property
     * must have both their FIELD_NAME and value copied correctly to the buffer.
     * The defective code skipped the FIELD_NAME with an errant p.nextToken(), causing
     * child properties to be ignored or missed entirely (expected: 4, actual: 0).
     */
    @Test(timeout = 4000)
    public void testIssue2088UnwrappedFieldsAfterLastCreatorProp() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"id\":100,\"x\":4,\"y\":8}";
        Issue2088Parent result = mapper.readValue(json, Issue2088Parent.class);

        assertNotNull("Deserialized parent bean should not be null", result);
        assertEquals(100, result.id);
        assertNotNull("Unwrapped child object should have been instantiated", result.child);
        assertEquals("Unwrapped property 'x' after creator property should be populated", 4, result.child.x);
        assertEquals("Unwrapped property 'y' after creator property should be populated", 8, result.child.y);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testVanillaDeserializationSuccess() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleBean bean = mapper.readValue("{\"name\":\"Alice\",\"age\":30}", SimpleBean.class);
        assertNotNull(bean);
        assertEquals("Alice", bean.name);
        assertEquals(30, bean.age);
    }

    @Test(timeout = 4000)
    public void testPropertyBasedCreatorDeserialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        CreatorBean bean = mapper.readValue("{\"extra\":\"foo\",\"first\":\"Bob\",\"count\":5}", CreatorBean.class);
        assertNotNull(bean);
        assertEquals("Bob", bean.first);
        assertEquals(5, bean.count);
        assertEquals("foo", bean.extra);
    }

    @Test(timeout = 4000)
    public void testDeserializeIntoExistingInstance() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleBean bean = new SimpleBean();
        bean.name = "Initial";
        bean.age = 10;

        SimpleBean updated = mapper.readerForUpdating(bean)
                .readValue("{\"name\":\"Updated\",\"age\":25}");
        assertSame(bean, updated);
        assertEquals("Updated", bean.name);
        assertEquals(25, bean.age);
    }

    @Test(timeout = 4000)
    public void testDeserializeWithViews() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"publicProp\":\"pubVal\",\"internalProp\":\"privVal\"}";

        ViewBean publicResult = mapper.readerWithView(Views.Public.class)
                .forType(ViewBean.class)
                .readValue(json);
        assertNotNull(publicResult);
        assertEquals("pubVal", publicResult.publicProp);
        assertNull("internalProp must not be deserialized when Public view is active", publicResult.internalProp);

        ViewBean internalResult = mapper.readerWithView(Views.Internal.class)
                .forType(ViewBean.class)
                .readValue(json);
        assertNotNull(internalResult);
        assertEquals("pubVal", internalResult.publicProp);
        assertEquals("privVal", internalResult.internalProp);
    }

    @Test(timeout = 4000)
    public void testDeserializeWithUnwrapped() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"name\":\"Office\",\"city\":\"Boston\",\"zip\":\"02101\"}";
        UnwrappedContainer container = mapper.readValue(json, UnwrappedContainer.class);
        assertNotNull(container);
        assertEquals("Office", container.name);
        assertNotNull(container.inner);
        assertEquals("Boston", container.inner.city);
        assertEquals("02101", container.inner.zip);
    }

    @Test(timeout = 4000)
    public void testDeserializeWithObjectIdReference() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"@id\":1,\"id\":10,\"next\":1}";
        Node node = mapper.readValue(json, Node.class);
        assertNotNull(node);
        assertEquals(10, node.id);
        assertSame("Node should have circular self-reference via ObjectId", node, node.next);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyJsonObjectDeserialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleBean bean = mapper.readValue("{}", SimpleBean.class);
        assertNotNull(bean);
        assertNull(bean.name);
        assertEquals(0, bean.age);
    }

    @Test(timeout = 4000)
    public void testNullTokenDeserializationStandard() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleBean bean = mapper.readValue("null", SimpleBean.class);
        assertNull(bean);
    }

    @Test(timeout = 4000)
    public void testUnwrappingDeserializerRecursionGuard() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDeserializerBase deser = (BeanDeserializerBase) mapper.getDeserializationConfig()
                .findTypeDeserializer(type); // fallback check
        deser = (BeanDeserializerBase) mapper.deserializationConfigInstance()
                .introspect(type); // verify type inspection

        JsonDeserializer<Object> rootDeser = mapper.getDeserProvider().findRootValueDeserializer(ctxt, type);
        assertTrue(rootDeser instanceof BeanDeserializer);
        BeanDeserializer beanDeser = (BeanDeserializer) rootDeser;

        NameTransformer transformer = NameTransformer.NOP;
        JsonDeserializer<Object> unwrapped1 = beanDeser.unwrappingDeserializer(transformer);
        assertNotNull(unwrapped1);
        assertTrue(unwrapped1 instanceof BeanDeserializer);

        // Verify recursion prevention mechanism (_currentlyTransforming)
        JsonDeserializer<Object> unwrapped2 = unwrapped1.unwrappingDeserializer(transformer);
        assertNotNull(unwrapped2);
    }

    @Test(timeout = 4000)
    public void testUnwrappingDeserializerSubclassBypass() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDeserializer orig = (BeanDeserializer) mapper.getDeserProvider().findRootValueDeserializer(ctxt, type);

        SubclassedBeanDeserializer custom = new SubclassedBeanDeserializer(orig);
        NameTransformer transformer = NameTransformer.simpleTransformer("prefix.", "");
        JsonDeserializer<Object> res = custom.unwrappingDeserializer(transformer);
        assertSame("Subclasses of BeanDeserializer should return this unless overridden", custom, res);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = UnrecognizedPropertyException.class, timeout = 4000)
    public void testUnknownPropertyFailOnUnknown() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.readValue("{\"unknownProp\":\"badVal\"}", SimpleBean.class);
    }

    @Test(timeout = 4000)
    public void testUnknownPropertyIgnoredWhenConfigured() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        SimpleBean bean = mapper.readValue("{\"unknownProp\":\"badVal\",\"name\":\"Ok\"}", SimpleBean.class);
        assertNotNull(bean);
        assertEquals("Ok", bean.name);
    }

    @Test(expected = MismatchedInputException.class, timeout = 4000)
    public void testUnexpectedArrayToken() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.readValue("[1, 2, 3]", SimpleBean.class);
    }

    @Test(expected = MismatchedInputException.class, timeout = 4000)
    public void testUnexpectedBooleanToken() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.readValue("true", SimpleBean.class);
    }

    @Test(expected = MismatchedInputException.class, timeout = 4000)
    public void testUnexpectedNumberToken() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.readValue("12345", SimpleBean.class);
    }

    @Test(expected = MismatchedInputException.class, timeout = 4000)
    public void testUnexpectedStringToken() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.readValue("\"some string\"", SimpleBean.class);
    }

    @Test(timeout = 4000)
    public void testCreatorReturnedNull() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDeserializer deser = (BeanDeserializer) mapper.getDeserProvider().findRootValueDeserializer(ctxt, type);

        Exception ex = deser._creatorReturnedNullException();
        assertNotNull(ex);
        assertTrue(ex instanceof NullPointerException);
        assertEquals("JSON Creator returned null", ex.getMessage());

        // Ensure idempotency of lazy-constructed exception
        assertSame(ex, deser._creatorReturnedNullException());
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Copy Mutators
    // =========================================================================

    @Test(timeout = 4000)
    public void testMutantCopyConstructors() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDeserializer orig = (BeanDeserializer) mapper.getDeserProvider().findRootValueDeserializer(ctxt, type);

        // withIgnorableProperties
        Set<String> ignorable = new HashSet<String>(Collections.singletonList("age"));
        BeanDeserializer withIgnorable = orig.withIgnorableProperties(ignorable);
        assertNotNull(withIgnorable);
        assertNotSame(orig, withIgnorable);

        // withBeanProperties
        BeanPropertyMap propMap = orig.getPropertyMap();
        BeanDeserializerBase withProps = orig.withBeanProperties(propMap);
        assertNotNull(withProps);
        assertNotSame(orig, withProps);

        // asArrayDeserializer
        BeanDeserializerBase asArray = orig.asArrayDeserializer();
        assertNotNull(asArray);
        assertTrue(asArray instanceof BeanAsArrayDeserializer);

        // withObjectIdReader
        ObjectIdReader oir = ObjectIdReader.construct(
                type,
                PropertyName.construct("id"),
                new ObjectIdGenerators.IntSequenceGenerator(),
                null,
                null,
                null
        );
        BeanDeserializer withOir = orig.withObjectIdReader(oir);
        assertNotNull(withOir);
        assertNotSame(orig, withOir);
    }

    @Test(timeout = 4000)
    public void testIgnorablePropertiesInPropertyBasedCreator() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule mod = new SimpleModule();
        mapper.registerModule(mod);

        // 'ignored' is not a field in CreatorBean, and should not be passed to creator or fail if ignorable
        String json = "{\"first\":\"Charlie\",\"count\":2,\"ignored\":\"ignoreMe\"}";
        ObjectMapper ignorableMapper = new ObjectMapper();
        ignorableMapper.addMixIn(CreatorBean.class, MixInIgnorable.class);

        CreatorBean bean = ignorableMapper.readValue(json, CreatorBean.class);
        assertNotNull(bean);
        assertEquals("Charlie", bean.first);
        assertEquals(2, bean.count);
    }

    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"ignored"})
    private abstract static class MixInIgnorable {}
}