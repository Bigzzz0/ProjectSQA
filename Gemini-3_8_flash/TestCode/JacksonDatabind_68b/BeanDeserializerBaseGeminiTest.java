package com.fasterxml.jackson.databind.deser;

import java.io.IOException;
import java.util.*;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.impl.ObjectIdReader;
import com.fasterxml.jackson.databind.exc.IgnoredPropertyException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.util.NameTransformer;

/* [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------
 * Branch / Target                  | Description & Execution Path
 * ---------------------------------------------------------------------------------------------------
 * Defect Zone 1 (Array Delegate)   | deserializeFromArray: fallback to _delegateDeserializer when
 *                                  | POJO uses a Collection-based @JsonCreator delegate. Bug triggers
 *                                  | createUsingArrayDelegate instead of createUsingDelegate.
 * Defect Zone 2 (Unwrap Single Arr)| deserializeFromArray: UNWRAP_SINGLE_VALUE_ARRAYS interaction with
 *                                  | delegating creators (String / Scalar delegates).
 * Partition A: Core Functional     | Instantiation via default, property-based, and delegate creators.
 *                                  | Accessors: getKnownPropertyNames, findProperty, getPropertyCount.
 * Partition B: Boundary Values     | Empty arrays as null objects (ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT),
 *                                  | null ObjectIdReader, unknown properties skipping vs failing.
 * Partition C: Mutator & LifeCycle | withObjectIdReader, withIgnorableProperties, withBeanProperties,
 *                                  | replaceProperty, isCachable, handledType, getValueType.
 * Partition D: Error Handling      | wrapAndThrow, wrapInstantiationProblem, IgnoredPropertyException,
 *                                  | unhandled missing instantiator for numbers/strings/booleans.
 * ---------------------------------------------------------------------------------------------------
 */
public class BeanDeserializerBaseGeminiTest {

    // --- Helper POJOs for testing BeanDeserializerBase branches ---

    static class SimpleBean {
        public String name;
        public int age;

        public SimpleBean() {}
        public SimpleBean(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }

    static class MessagesListHolder {
        public final List<String> messages;

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public MessagesListHolder(List<String> messages) {
            this.messages = messages;
        }
    }

    static class DelegatingStringBean {
        protected final String value;

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public DelegatingStringBean(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    static class DelegatingIntBean {
        protected final int number;

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public DelegatingIntBean(int number) {
            this.number = number;
        }
    }

    static class DelegatingBooleanBean {
        protected final boolean flag;

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public DelegatingBooleanBean(boolean flag) {
            this.flag = flag;
        }
    }

    static class DelegatingDoubleBean {
        protected final double value;

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public DelegatingDoubleBean(double value) {
            this.value = value;
        }
    }

    static class PropertyBasedBean {
        public final String id;
        public final int code;

        @JsonCreator
        public PropertyBasedBean(@JsonProperty("id") String id, @JsonProperty("code") int code) {
            this.id = id;
            this.code = code;
        }
    }

    @JsonIgnoreProperties(value = {"ignoredProp"}, ignoreUnknown = false)
    static class IgnorableBean {
        public String validProp;
        public String ignoredProp;
    }

    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    static class ShapeAsArrayBean {
        public String first;
        public String second;

        public ShapeAsArrayBean() {}
        public ShapeAsArrayBean(String f, String s) {
            this.first = f;
            this.second = s;
        }
    }

    // ===============================================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // ===============================================================================================

    /**
     * Targets Defect: Deserializing an object with a Collection delegate creator from a JSON Array.
     * In defective Jackson versions, BeanDeserializerBase#deserializeFromArray erroneously calls
     * `_valueInstantiator.createUsingArrayDelegate` instead of `createUsingDelegate` for regular
     * delegates, causing a JsonMappingException.
     */
    @Test(timeout = 4000)
    public void testDefectDeserializationOfObjectWithChainedCollectionDelegate() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "[\"item1\", \"item2\", \"item3\"]";

        MessagesListHolder holder = mapper.readValue(json, MessagesListHolder.class);
        assertNotNull("Deserialized object should not be null", holder);
        assertNotNull("Messages list should not be null", holder.messages);
        assertEquals("List should contain 3 items", 3, holder.messages.size());
        assertEquals("item1", holder.messages.get(0));
        assertEquals("item2", holder.messages.get(1));
        assertEquals("item3", holder.messages.get(2));
    }

    /**
     * Targets Defect: Deserializing a single scalar delegating bean from an unwrapped single array.
     * Under UNWRAP_SINGLE_VALUE_ARRAYS, a single-element array ["test2"] should unwrap and bind
     * to a delegating String constructor.
     */
    @Test(timeout = 4000)
    public void testDefectWithSingleStringUnwrapSingleValueArray() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);

        String json = "[\"test2\"]";
        DelegatingStringBean bean = mapper.readValue(json, DelegatingStringBean.class);
        assertNotNull("Bean should be deserialized from unwrapped array", bean);
        assertEquals("test2", bean.getValue());
    }

    // ===============================================================================================
    // Partition A: Core Functional Logic & State Transitions
    // ===============================================================================================

    @Test(timeout = 4000)
    public void testVanillaDefaultInstantiationAndPropertyBinding() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"name\":\"Alice\",\"age\":30}";

        SimpleBean bean = mapper.readValue(json, SimpleBean.class);
        assertNotNull(bean);
        assertEquals("Alice", bean.name);
        assertEquals(30, bean.age);
    }

    @Test(timeout = 4000)
    public void testPropertyBasedCreatorInstantiation() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"id\":\"X100\",\"code\":42}";

        PropertyBasedBean bean = mapper.readValue(json, PropertyBasedBean.class);
        assertNotNull(bean);
        assertEquals("X100", bean.id);
        assertEquals(42, bean.code);
    }

    @Test(timeout = 4000)
    public void testScalarDelegatingCreators() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // String delegate
        DelegatingStringBean strBean = mapper.readValue("\"direct_scalar\"", DelegatingStringBean.class);
        assertNotNull(strBean);
        assertEquals("direct_scalar", strBean.getValue());

        // Int delegate
        DelegatingIntBean intBean = mapper.readValue("123", DelegatingIntBean.class);
        assertNotNull(intBean);
        assertEquals(123, intBean.number);

        // Boolean delegate
        DelegatingBooleanBean boolBean = mapper.readValue("true", DelegatingBooleanBean.class);
        assertNotNull(boolBean);
        assertTrue(boolBean.flag);

        // Double delegate
        DelegatingDoubleBean dblBean = mapper.readValue("3.1415", DelegatingDoubleBean.class);
        assertNotNull(dblBean);
        assertEquals(3.1415, dblBean.value, 0.00001);
    }

    @Test(timeout = 4000)
    public void testBeanDeserializerBaseMetadataAndAccessors() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(SimpleBean.class);
        JsonDeserializer<?> deser = ctxt.findRootValueDeserializer(type);

        assertTrue("Deserializer should be instance of BeanDeserializerBase",
                deser instanceof BeanDeserializerBase);
        BeanDeserializerBase beanDeser = (BeanDeserializerBase) deser;

        assertTrue(beanDeser.isCachable());
        assertEquals(SimpleBean.class, beanDeser.handledType());
        assertEquals(SimpleBean.class, beanDeser.getBeanClass());
        assertEquals(type, beanDeser.getValueType());
        assertEquals(2, beanDeser.getPropertyCount());
        assertTrue(beanDeser.hasProperty("name"));
        assertTrue(beanDeser.hasProperty("age"));
        assertFalse(beanDeser.hasProperty("nonExistent"));

        SettableBeanProperty propName = beanDeser.findProperty("name");
        assertNotNull(propName);
        assertEquals("name", propName.getName());

        SettableBeanProperty propByPropName = beanDeser.findProperty(new PropertyName("age"));
        assertNotNull(propByPropName);
        assertEquals("age", propByPropName.getName());

        Collection<Object> knownNames = beanDeser.getKnownPropertyNames();
        assertTrue(knownNames.contains("name"));
        assertTrue(knownNames.contains("age"));
        assertEquals(2, knownNames.size());

        assertNotNull(beanDeser.getValueInstantiator());
        assertFalse(beanDeser.hasViews());
        assertNull(beanDeser.getObjectIdReader());
        assertNull(beanDeser.findBackReference("anyBackRef"));
    }

    @Test(timeout = 4000)
    public void testSerializationShapeArray() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "[\"firstVal\",\"secondVal\"]";

        ShapeAsArrayBean bean = mapper.readValue(json, ShapeAsArrayBean.class);
        assertNotNull(bean);
        assertEquals("firstVal", bean.first);
        assertEquals("secondVal", bean.second);
    }

    // ===============================================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // ===============================================================================================

    @Test(timeout = 4000)
    public void testAcceptEmptyArrayAsNullObject() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);

        SimpleBean bean = mapper.readValue("[]", SimpleBean.class);
        assertNull("Empty array should deserialize as null object when feature enabled", bean);
    }

    @Test(timeout = 4000)
    public void testUnwrapSingleValueArrayMultipleElementsShouldFail() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);

        try {
            mapper.readValue("[{\"name\":\"Alice\"}, {\"name\":\"Bob\"}]", SimpleBean.class);
            fail("Expected JsonMappingException for trailing elements in unwrapped single array");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("expected closing END_ARRAY"));
        }
    }

    @Test(timeout = 4000)
    public void testIgnoredPropertiesHandling() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // Default: ignore ignored properties without failing
        String json = "{\"validProp\":\"ok\",\"ignoredProp\":\"skipMe\"}";
        IgnorableBean bean = mapper.readValue(json, IgnorableBean.class);
        assertNotNull(bean);
        assertEquals("ok", bean.validProp);
        assertNull(bean.ignoredProp);

        // FAIL_ON_IGNORED_PROPERTIES enabled
        ObjectMapper strictMapper = new ObjectMapper();
        strictMapper.enable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);
        try {
            strictMapper.readValue(json, IgnorableBean.class);
            fail("Should fail when FAIL_ON_IGNORED_PROPERTIES is enabled");
        } catch (IgnoredPropertyException expected) {
            assertEquals("ignoredProp", expected.getPropertyName());
        }
    }

    @Test(timeout = 4000)
    public void testUnknownPropertiesFailByDefault() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"name\":\"Bob\",\"unexpected\":123}";
        try {
            mapper.readValue(json, SimpleBean.class);
            fail("Expected UnrecognizedPropertyException for unexpected field");
        } catch (UnrecognizedPropertyException expected) {
            assertEquals("unexpected", expected.getPropertyName());
        }
    }

    @Test(timeout = 4000)
    public void testUnknownPropertiesIgnoredWhenConfigured() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        String json = "{\"name\":\"Bob\",\"unexpected\":123}";
        SimpleBean bean = mapper.readValue(json, SimpleBean.class);
        assertNotNull(bean);
        assertEquals("Bob", bean.name);
    }

    // ===============================================================================================
    // Partition D: Exception & Defensive Guard Paths
    // ===============================================================================================

    @Test(timeout = 4000)
    public void testWrapAndThrowWithRuntimeAndCheckedExceptions() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDeserializerBase deser = (BeanDeserializerBase) ctxt.findRootValueDeserializer(type);

        SimpleBean beanInstance = new SimpleBean();

        // 1. Error should be re-thrown directly
        try {
            deser.wrapAndThrow(new OutOfMemoryError("Simulated Error"), beanInstance, "name", ctxt);
            fail("Should throw OutOfMemoryError directly");
        } catch (OutOfMemoryError oom) {
            assertEquals("Simulated Error", oom.getMessage());
        }

        // 2. IOException with WRAP_EXCEPTIONS should be wrapped in JsonMappingException
        try {
            deser.wrapAndThrow(new IOException("Simulated IO"), beanInstance, "name", ctxt);
            fail("Should throw wrapped IOException");
        } catch (IOException ioe) {
            assertTrue(ioe instanceof JsonMappingException || ioe.getMessage().contains("Simulated IO"));
        }

        // 3. RuntimeException wrapping
        try {
            deser.wrapAndThrow(new IllegalArgumentException("Bad input"), beanInstance, "age", ctxt);
            fail("Should throw JsonMappingException wrapping IllegalArgumentException");
        } catch (JsonMappingException jme) {
            assertTrue(jme.getCause() instanceof IllegalArgumentException);
            assertEquals("Bad input", jme.getCause().getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testMissingInstantiatorExceptionPaths() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // Attempting to deserialize a boolean into PropertyBasedBean which only has (@JsonProperty String, int)
        try {
            mapper.readValue("true", PropertyBasedBean.class);
            fail("Should fail to deserialize boolean into PropertyBasedBean");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("can not deserialize from Boolean")
                    || e.getMessage().contains("no suitable creator"));
        }

        // Attempting to deserialize a Double into PropertyBasedBean
        try {
            mapper.readValue("99.99", PropertyBasedBean.class);
            fail("Should fail to deserialize double into PropertyBasedBean");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("can not deserialize from")
                    || e.getMessage().contains("no suitable creator"));
        }
    }

    // ===============================================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // ===============================================================================================

    @Test(timeout = 4000)
    public void testWithObjectIdReaderAndPropertiesCloning() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDeserializerBase deser = (BeanDeserializerBase) ctxt.findRootValueDeserializer(type);

        // Cloned with null ObjectIdReader should maintain property count
        BeanDeserializerBase cloneNoOir = deser.withObjectIdReader(null);
        assertNotNull(cloneNoOir);
        assertEquals(deser.getPropertyCount(), cloneNoOir.getPropertyCount());

        // Cloned with ignorable properties
        Set<String> ignorables = new HashSet<String>();
        ignorables.add("name");
        BeanDeserializerBase cloneWithIgnored = deser.withIgnorableProperties(ignorables);
        assertNotNull(cloneWithIgnored);
        // "name" property should be excluded from active bean properties
        assertEquals(1, cloneWithIgnored.getPropertyCount());
        assertFalse(cloneWithIgnored.hasProperty("name"));
        assertTrue(cloneWithIgnored.hasProperty("age"));
    }

    @Test(timeout = 4000)
    public void testPropertiesIterationAndLookupByIndex() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDeserializerBase deser = (BeanDeserializerBase) ctxt.findRootValueDeserializer(type);

        Iterator<SettableBeanProperty> it = deser.properties();
        assertNotNull(it);
        int count = 0;
        while (it.hasNext()) {
            SettableBeanProperty prop = it.next();
            assertNotNull(prop);
            count++;
        }
        assertEquals(2, count);

        // Creator properties should be empty for default constructor POJO
        Iterator<SettableBeanProperty> creatorProps = deser.creatorProperties();
        assertNotNull(creatorProps);
        assertFalse(creatorProps.hasNext());
    }

    @Test(timeout = 4000)
    public void testWithBeanPropertiesThrowsUnsupportedByDefaultIfNotOverridden() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDeserializerBase deser = (BeanDeserializerBase) ctxt.findRootValueDeserializer(type);

        // If withBeanProperties is not overridden, base implementation throws UnsupportedOperationException
        // (Standard BeanDeserializer overrides it, but we test base default behaviour contract on custom subclass)
        BeanDeserializerBase minimalSubclass = new BeanDeserializerBase(deser) {
            private static final long serialVersionUID = 1L;

            @Override
            public JsonDeserializer<Object> unwrappingDeserializer(NameTransformer unwrapper) {
                return this;
            }

            @Override
            public BeanDeserializerBase withObjectIdReader(ObjectIdReader oir) {
                return this;
            }

            @Override
            public BeanDeserializerBase withIgnorableProperties(Set<String> ignorableProps) {
                return this;
            }

            @Override
            protected BeanDeserializerBase asArrayDeserializer() {
                return this;
            }

            @Override
            public Object deserializeFromObject(JsonParser p, DeserializationContext ctxt) {
                return null;
            }

            @Override
            protected Object _deserializeUsingPropertyBased(JsonParser p, DeserializationContext ctxt) {
                return null;
            }
        };

        try {
            minimalSubclass.withBeanProperties(deser._beanProperties);
            fail("Expected UnsupportedOperationException when withBeanProperties() is not overridden");
        } catch (UnsupportedOperationException uoe) {
            assertTrue(uoe.getMessage().contains("does not override `withBeanProperties()`"));
        }
    }
}