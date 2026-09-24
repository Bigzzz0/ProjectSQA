package com.fasterxml.jackson.databind.deser;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.impl.BeanPropertyMap;
import com.fasterxml.jackson.databind.deser.impl.ObjectIdReader;
import com.fasterxml.jackson.databind.exc.IgnoredPropertyException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.util.NameTransformer;
import com.fasterxml.jackson.databind.util.StdConverter;
import com.fasterxml.jackson.databind.util.TokenBuffer;

/* [Branch & Defect Analysis Matrix]
 * Target Class: com.fasterxml.jackson.databind.deser.BeanDeserializerBase
 *
 * 1. Targeted Known Defect (Defects4J / databind #999):
 *    - Branch: External type ID handling in resolve() when external type property is also
 *      retained as a field/property of the enclosing bean.
 *    - Condition: When JSON has external type ID before or after the payload, the external type
 *      property must be preserved and correctly populated on the target bean instance rather than left null.
 *    - Test Methods: testExternalTypeId999DefectTypeBeforePayload, testExternalTypeId999DefectPayloadBeforeType
 *
 * 2. Partition A: Core Functional Logic & State Transitions
 *    - Accessors: hasProperty, getPropertyCount, getKnownPropertyNames, handledType, getBeanClass,
 *      getValueType, properties(), creatorProperties(), findProperty(String/PropertyName/int).
 *    - Mutant factory method: withBeanProperties() throws UnsupportedOperationException by default.
 *    - Array shape: JsonFormat.Shape.ARRAY deserialization.
 *    - Unwrapped properties: JsonUnwrapped delegation and renaming.
 *    - Managed / Back reference resolution and linking.
 *    - View processing (JsonView), Value Injection (JacksonInject), Deserialization Converter.
 *
 * 3. Partition B: Boundary Value Analysis (BVA) & Extremes
 *    - Null / non-existent property names or indices out-of-bounds in findProperty.
 *    - Empty arrays with ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT vs disabled.
 *    - UNWRAP_SINGLE_VALUE_ARRAYS on scalar/object payloads.
 *    - Null PropertyName throws NPE in findProperty(PropertyName).
 *
 * 4. Partition C: Defect-Targeted Branch Zone
 *    - External type ID resolution with polymorphic subclasses.
 *    - Token ordering (type property first vs payload property first).
 *
 * 5. Partition D: Exception & Defensive Guard Paths
 *    - wrapAndThrow() handling of InvocationTargetException unwrapping, Error propagation,
 *      JsonProcessingException wrapping, and unchecked exception pass-through.
 *    - wrapInstantiationProblem() handling of Errors, plain IOExceptions, and unchecked exceptions.
 *    - handleUnknownVanilla() & handleIgnoredProperty() with FAIL_ON_IGNORED_PROPERTIES and FAIL_ON_UNKNOWN_PROPERTIES.
 *    - Deserialization missing instantiator for abstract types, numbers, strings, booleans, and floats.
 *
 * 6. Partition E: Object Lifecycle & Contract Integrity
 *    - Polymorphic subclass deserializer cache lookup and creation (_findSubclassDeserializer).
 *    - Native and typed Object ID handling (_handleTypedObjectId, _convertObjectId).
 *    - Embedded object deserialization (deserializeFromEmbedded).
 */
public class BeanDeserializerBaseGeminiTest {

    // -------------------------------------------------------------------------
    // Test POJOs and Helper Classes
    // -------------------------------------------------------------------------

    public static class SimpleBean {
        public int x;
        public String y;

        public SimpleBean() {}
        public SimpleBean(int x, String y) {
            this.x = x;
            this.y = y;
        }
    }

    public static class SubBean extends SimpleBean {
        public int z;
    }

    public static class Holder999 {
        public String type;

        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
        @JsonSubTypes({
            @JsonSubTypes.Type(value = Foo999.class, name = "foo")
        })
        public Payload999 payload;

        public Holder999() {}
    }

    public interface Payload999 {}

    public static class Foo999 implements Payload999 {
        public int val;

        public Foo999() {}
        public Foo999(int val) { this.val = val; }
    }

    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    public static class ArrayFormatBean {
        public int a;
        public String b;

        public ArrayFormatBean() {}
        public ArrayFormatBean(int a, String b) {
            this.a = a;
            this.b = b;
        }
    }

    public static class ParentBean {
        public int id;
        @JsonUnwrapped(prefix = "child_")
        public ChildBean child;
    }

    public static class ChildBean {
        public String name;
        public int age;
    }

    public static class ParentRef {
        public int id;
        @JsonManagedReference
        public ChildRef child;
    }

    public static class ChildRef {
        public String name;
        @JsonBackReference
        public ParentRef parent;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    public static class IdBean {
        public int id;
        public String name;
        public IdBean next;
    }

    public static class Views {
        public static class Public {}
        public static class Internal extends Public {}
    }

    public static class ViewBean {
        @JsonView(Views.Public.class)
        public int pub;
        @JsonView(Views.Internal.class)
        public int priv;
    }

    public static class InjectedBean {
        public int x;
        @JacksonInject("injectedVal")
        public String y;
    }

    public static class ConvertedBean {
        @JsonDeserialize(converter = UpperCaseConverter.class)
        public String text;
    }

    public static class UpperCaseConverter extends StdConverter<String, String> {
        @Override
        public String convert(String value) {
            return value == null ? null : value.toUpperCase();
        }
    }

    @JsonIgnoreProperties({"secret"})
    public static class IgnoredBean {
        public int id;
        public String secret;
    }

    public static class AnySetterBean {
        public int id;
        public Map<String, Object> others = new HashMap<String, Object>();

        @JsonAnySetter
        public void setOther(String name, Object value) {
            others.put(name, value);
        }
    }

    public static class CreatorBean {
        public final int x;
        public final String y;

        @JsonCreator
        public CreatorBean(@JsonProperty("x") int x, @JsonProperty("y") String y) {
            this.x = x;
            this.y = y;
        }
    }

    public static class DelegateStringBean {
        public String value;
        @JsonCreator
        public DelegateStringBean(String v) {
            this.value = "delegated:" + v;
        }
    }

    public static class DelegateIntBean {
        public int num;
        @JsonCreator
        public DelegateIntBean(int n) {
            this.num = n * 2;
        }
    }

    public static class DelegateBoolBean {
        public boolean flag;
        @JsonCreator
        public DelegateBoolBean(boolean b) {
            this.flag = !b;
        }
    }

    public static class OuterBean {
        public InnerBean inner;
        public class InnerBean {
            public int val;
        }
    }

    public abstract static class AbstractBean {
        public int id;
    }

    // Concrete test subclass to expose protected constructors and lifecycle methods
    public static class TestBeanDeserializer extends BeanDeserializerBase {
        private static final long serialVersionUID = 1L;

        public TestBeanDeserializer(BeanDeserializerBase src) {
            super(src);
        }

        public TestBeanDeserializer(BeanDeserializerBase src, boolean ignoreAllUnknown) {
            super(src, ignoreAllUnknown);
        }

        public TestBeanDeserializer(BeanDeserializerBase src, NameTransformer unwrapper) {
            super(src, unwrapper);
        }

        public TestBeanDeserializer(BeanDeserializerBase src, ObjectIdReader oir) {
            super(src, oir);
        }

        public TestBeanDeserializer(BeanDeserializerBase src, Set<String> ignorableProps) {
            super(src, ignorableProps);
        }

        public TestBeanDeserializer(BeanDeserializerBase src, BeanPropertyMap beanProps) {
            super(src, beanProps);
        }

        @Override
        public JsonDeserializer<Object> unwrappingDeserializer(NameTransformer unwrapper) {
            return this;
        }

        @Override
        public BeanDeserializerBase withObjectIdReader(ObjectIdReader oir) {
            return new TestBeanDeserializer(this, oir);
        }

        @Override
        public BeanDeserializerBase withIgnorableProperties(Set<String> ignorableProps) {
            return new TestBeanDeserializer(this, ignorableProps);
        }

        @Override
        protected BeanDeserializerBase asArrayDeserializer() {
            return this;
        }

        @Override
        public Object deserializeFromObject(JsonParser p, DeserializationContext ctxt) throws IOException {
            return null;
        }

        @Override
        protected Object _deserializeUsingPropertyBased(JsonParser p, DeserializationContext ctxt) throws IOException {
            return null;
        }
    }

    private BeanDeserializerBase getBaseDeserializer(ObjectMapper mapper, Class<?> cls) throws Exception {
        JavaType type = mapper.constructType(cls);
        DeserializationContext ctxt = mapper.getDeserializationContext();
        if (ctxt instanceof DefaultDeserializationContext) {
            ctxt = ((DefaultDeserializationContext) ctxt).createInstance(mapper.getDeserializationConfig(), null, null);
        }
        JsonDeserializer<Object> deser = ctxt.findRootValueDeserializer(type);
        assertTrue("Deserializer must extend BeanDeserializerBase", deser instanceof BeanDeserializerBase);
        return (BeanDeserializerBase) deser;
    }

    // -------------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (databind #999)
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testExternalTypeId999DefectTypeBeforePayload() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"type\":\"foo\",\"payload\":{\"val\":42}}";
        Holder999 result = mapper.readValue(json, Holder999.class);
        assertNotNull(result);
        assertEquals("foo", result.type);
        assertNotNull(result.payload);
        assertTrue(result.payload instanceof Foo999);
        assertEquals(42, ((Foo999) result.payload).val);
    }

    @Test(timeout = 4000)
    public void testExternalTypeId999DefectPayloadBeforeType() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"payload\":{\"val\":84},\"type\":\"foo\"}";
        Holder999 result = mapper.readValue(json, Holder999.class);
        assertNotNull(result);
        assertEquals("foo", result.type);
        assertNotNull(result.payload);
        assertTrue(result.payload instanceof Foo999);
        assertEquals(84, ((Foo999) result.payload).val);
    }

    // -------------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testAccessorsAndMetadata() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        BeanDeserializerBase deser = getBaseDeserializer(mapper, SimpleBean.class);

        assertEquals(SimpleBean.class, deser.handledType());
        assertEquals(SimpleBean.class, deser.getBeanClass());
        assertEquals(SimpleBean.class, deser.getValueType().getRawClass());
        assertTrue(deser.isCachable());
        assertEquals(2, deser.getPropertyCount());
        assertTrue(deser.hasProperty("x"));
        assertTrue(deser.hasProperty("y"));
        assertFalse(deser.hasProperty("nonExistent"));
        assertFalse(deser.hasViews());
        assertNull(deser.getObjectIdReader());
        assertNotNull(deser.getValueInstantiator());

        Collection<Object> names = deser.getKnownPropertyNames();
        assertEquals(2, names.size());
        assertTrue(names.contains("x"));
        assertTrue(names.contains("y"));

        assertNotNull(deser.findProperty("x"));
        assertNotNull(deser.findProperty(new PropertyName("y")));
        assertNotNull(deser.findProperty(0));
        assertNull(deser.findProperty(999));
        assertNull(deser.findProperty("unknown"));
        assertNull(deser.findBackReference("missingRef"));

        Iterator<SettableBeanProperty> it = deser.properties();
        int count = 0;
        while (it.hasNext()) {
            assertNotNull(it.next());
            count++;
        }
        assertEquals(2, count);
    }

    @Test(timeout = 4000)
    public void testReplaceProperty() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        BeanDeserializerBase deser = getBaseDeserializer(mapper, SimpleBean.class);
        SettableBeanProperty propX = deser.findProperty("x");
        assertNotNull(propX);

        deser.replaceProperty(propX, propX);
        assertSame(propX, deser.findProperty("x"));
    }

    @Test(timeout = 4000)
    public void testWithBeanPropertiesDefaultThrows() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        BeanDeserializerBase deser = getBaseDeserializer(mapper, SimpleBean.class);
        TestBeanDeserializer testDeser = new TestBeanDeserializer(deser);
        try {
            testDeser.withBeanProperties(deser._beanProperties);
            fail("Expected UnsupportedOperationException for un-overridden withBeanProperties()");
        } catch (UnsupportedOperationException e) {
            assertTrue(e.getMessage().contains("does not override `withBeanProperties()`"));
        }
    }

    @Test(timeout = 4000)
    public void testShapeArrayTransformation() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ArrayFormatBean bean = mapper.readValue("[100, \"hello\"]", ArrayFormatBean.class);
        assertNotNull(bean);
        assertEquals(100, bean.a);
        assertEquals("hello", bean.b);
    }

    @Test(timeout = 4000)
    public void testUnwrappedProperty() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"id\":1,\"child_name\":\"Alice\",\"child_age\":30}";
        ParentBean parent = mapper.readValue(json, ParentBean.class);
        assertNotNull(parent);
        assertEquals(1, parent.id);
        assertNotNull(parent.child);
        assertEquals("Alice", parent.child.name);
        assertEquals(30, parent.child.age);
    }

    @Test(timeout = 4000)
    public void testManagedAndBackReference() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"id\":10,\"child\":{\"name\":\"kid\"}}";
        ParentRef parent = mapper.readValue(json, ParentRef.class);
        assertNotNull(parent);
        assertNotNull(parent.child);
        assertEquals("kid", parent.child.name);
        assertSame(parent, parent.child.parent);
    }

    @Test(timeout = 4000)
    public void testViewProcessing() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"pub\":1,\"priv\":2}";
        ViewBean bean = mapper.readerWithView(Views.Public.class).forType(ViewBean.class).readValue(json);
        assertNotNull(bean);
        assertEquals(1, bean.pub);
        assertEquals(0, bean.priv);

        BeanDeserializerBase deser = getBaseDeserializer(mapper, ViewBean.class);
        assertTrue(deser.hasViews());
    }

    @Test(timeout = 4000)
    public void testInjectValues() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        InjectableValues.Std inject = new InjectableValues.Std();
        inject.addValue("injectedVal", "injected_string");
        InjectedBean bean = mapper.reader(inject).forType(InjectedBean.class).readValue("{\"x\":5}");
        assertNotNull(bean);
        assertEquals(5, bean.x);
        assertEquals("injected_string", bean.y);
    }

    @Test(timeout = 4000)
    public void testConvertingDeserializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ConvertedBean bean = mapper.readValue("{\"text\":\"lower\"}", ConvertedBean.class);
        assertNotNull(bean);
        assertEquals("LOWER", bean.text);
    }

    @Test(timeout = 4000)
    public void testCreatorProperties() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        BeanDeserializerBase deser = getBaseDeserializer(mapper, CreatorBean.class);
        Iterator<SettableBeanProperty> it = deser.creatorProperties();
        assertTrue(it.hasNext());
        SettableBeanProperty p = it.next();
        assertNotNull(p);

        CreatorBean bean = mapper.readValue("{\"x\":44,\"y\":\"creator\"}", CreatorBean.class);
        assertNotNull(bean);
        assertEquals(44, bean.x);
        assertEquals("creator", bean.y);
    }

    @Test(timeout = 4000)
    public void testDelegatingCreators() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        DelegateStringBean strBean = mapper.readValue("\"val\"", DelegateStringBean.class);
        assertEquals("delegated:val", strBean.value);

        DelegateIntBean intBean = mapper.readValue("21", DelegateIntBean.class);
        assertEquals(42, intBean.num);

        DelegateBoolBean boolBean = mapper.readValue("true", DelegateBoolBean.class);
        assertFalse(boolBean.flag);
    }

    @Test(timeout = 4000)
    public void testInnerClassProperty() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        OuterBean outer = mapper.readValue("{\"inner\":{\"val\":99}}", OuterBean.class);
        assertNotNull(outer);
        assertNotNull(outer.inner);
        assertEquals(99, outer.inner.val);
    }

    @Test(timeout = 4000)
    public void testAnySetter() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AnySetterBean bean = mapper.readValue("{\"id\":7,\"k1\":\"v1\",\"k2\":123}", AnySetterBean.class);
        assertNotNull(bean);
        assertEquals(7, bean.id);
        assertEquals("v1", bean.others.get("k1"));
        assertEquals(123, bean.others.get("k2"));
    }

    // -------------------------------------------------------------------------
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testFindPropertyBoundaries() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        BeanDeserializerBase deser = getBaseDeserializer(mapper, SimpleBean.class);

        assertNull(deser.findProperty((String) null));
        assertNull(deser.findProperty(""));
        assertNull(deser.findProperty(-1));
        assertNull(deser.findProperty(Integer.MAX_VALUE));
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testFindPropertyNullPropertyNameThrows() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        BeanDeserializerBase deser = getBaseDeserializer(mapper, SimpleBean.class);
        deser.findProperty((PropertyName) null);
    }

    @Test(timeout = 4000)
    public void testArrayUnwrappingAndEmptyHandling() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);
        SimpleBean emptyBean = mapper.readValue("[]", SimpleBean.class);
        assertNull(emptyBean);

        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        SimpleBean singleBean = mapper.readValue("[{\"x\":12,\"y\":\"arr\"}]", SimpleBean.class);
        assertNotNull(singleBean);
        assertEquals(12, singleBean.x);
        assertEquals("arr", singleBean.y);
    }

    @Test(expected = JsonMappingException.class, timeout = 4000)
    public void testArrayWithoutFeatureThrows() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.readValue("[]", SimpleBean.class);
    }

    @Test(timeout = 4000)
    public void testCopyConstructorsAndMutants() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        BeanDeserializerBase deser = getBaseDeserializer(mapper, SimpleBean.class);

        TestBeanDeserializer copy1 = new TestBeanDeserializer(deser);
        assertEquals(deser.handledType(), copy1.handledType());

        TestBeanDeserializer copy2 = new TestBeanDeserializer(deser, true);
        assertEquals(deser.handledType(), copy2.handledType());

        TestBeanDeserializer copy3 = new TestBeanDeserializer(deser, NameTransformer.NOP);
        assertEquals(deser.handledType(), copy3.handledType());

        TestBeanDeserializer copy4 = new TestBeanDeserializer(deser, (ObjectIdReader) null);
        assertEquals(deser.handledType(), copy4.handledType());

        TestBeanDeserializer copy5 = new TestBeanDeserializer(deser, Collections.singleton("x"));
        assertNull(copy5.findProperty("x"));
        assertNotNull(copy5.findProperty("y"));

        TestBeanDeserializer copy6 = new TestBeanDeserializer(deser, deser._beanProperties);
        assertNotNull(copy6.findProperty("x"));
    }

    // -------------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testWrapAndThrowHandling() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        BeanDeserializerBase deser = getBaseDeserializer(mapper, SimpleBean.class);
        DeserializationContext ctxt = mapper.getDeserializationContext();

        // 1. InvocationTargetException wrapping Error -> throws Error unwrapped
        OutOfMemoryError oom = new OutOfMemoryError("oom");
        try {
            deser.wrapAndThrow(new InvocationTargetException(oom), new SimpleBean(), "x", ctxt);
            fail("Expected Error to be thrown");
        } catch (OutOfMemoryError e) {
            assertSame(oom, e);
        }

        // 2. InvocationTargetException wrapping IOException -> throws IOException
        IOException io = new IOException("disk error");
        try {
            deser.wrapAndThrow(new InvocationTargetException(io), new SimpleBean(), "x", ctxt);
            fail("Expected IOException to be thrown");
        } catch (IOException e) {
            assertSame(io, e);
        }

        // 3. RuntimeException wrapping into JsonMappingException
        IllegalArgumentException iae = new IllegalArgumentException("invalid value");
        try {
            deser.wrapAndThrow(iae, new SimpleBean(), "x", ctxt);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertSame(iae, e.getCause());
            assertEquals(1, e.getPath().size());
            assertEquals("x", e.getPath().get(0).getFieldName());
        }

        // 4. Deprecated wrapAndThrow with index
        try {
            deser.wrapAndThrow(iae, new SimpleBean(), 5, ctxt);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertSame(iae, e.getCause());
            assertEquals(5, e.getPath().get(0).getIndex());
        }
    }

    @Test(timeout = 4000)
    public void testWrapInstantiationProblem() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        BeanDeserializerBase deser = getBaseDeserializer(mapper, SimpleBean.class);
        DeserializationContext ctxt = mapper.getDeserializationContext();

        try {
            deser.wrapInstantiationProblem(new OutOfMemoryError("oom"), ctxt);
            fail("Expected OutOfMemoryError");
        } catch (OutOfMemoryError expected) {
            // expected
        }

        try {
            deser.wrapInstantiationProblem(new IOException("io_error"), ctxt);
            fail("Expected IOException");
        } catch (IOException expected) {
            // expected
        }
    }

    @Test(expected = IgnoredPropertyException.class, timeout = 4000)
    public void testFailOnIgnoredProperties() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);
        mapper.readValue("{\"id\":10,\"secret\":\"shh\"}", IgnoredBean.class);
    }

    @Test(expected = UnrecognizedPropertyException.class, timeout = 4000)
    public void testFailOnUnknownProperties() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.readValue("{\"x\":10,\"extra\":999}", SimpleBean.class);
    }

    @Test(timeout = 4000)
    public void testIgnoreUnknownWhenDisabled() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        SimpleBean bean = mapper.readValue("{\"x\":10,\"extra\":999}", SimpleBean.class);
        assertNotNull(bean);
        assertEquals(10, bean.x);
    }

    @Test(expected = JsonMappingException.class, timeout = 4000)
    public void testAbstractTypeWithoutInstantiatorThrows() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.readValue("{\"id\":1}", AbstractBean.class);
    }

    @Test(expected = JsonMappingException.class, timeout = 4000)
    public void testDeserializeFromNumberWithoutCreatorThrows() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        BeanDeserializerBase deser = getBaseDeserializer(mapper, SimpleBean.class);
        TokenBuffer tb = new TokenBuffer(null, false);
        tb.writeNumber(123);
        JsonParser p = tb.asParser();
        p.nextToken();
        deser.deserializeFromNumber(p, mapper.getDeserializationContext());
    }

    @Test(expected = JsonMappingException.class, timeout = 4000)
    public void testDeserializeFromStringWithoutCreatorThrows() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        BeanDeserializerBase deser = getBaseDeserializer(mapper, SimpleBean.class);
        TokenBuffer tb = new TokenBuffer(null, false);
        tb.writeString("abc");
        JsonParser p = tb.asParser();
        p.nextToken();
        deser.deserializeFromString(p, mapper.getDeserializationContext());
    }

    @Test(expected = JsonMappingException.class, timeout = 4000)
    public void testDeserializeFromDoubleWithoutCreatorThrows() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        BeanDeserializerBase deser = getBaseDeserializer(mapper, SimpleBean.class);
        TokenBuffer tb = new TokenBuffer(null, false);
        tb.writeNumber(12.34);
        JsonParser p = tb.asParser();
        p.nextToken();
        deser.deserializeFromDouble(p, mapper.getDeserializationContext());
    }

    @Test(expected = JsonMappingException.class, timeout = 4000)
    public void testDeserializeFromBooleanWithoutCreatorThrows() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        BeanDeserializerBase deser = getBaseDeserializer(mapper, SimpleBean.class);
        TokenBuffer tb = new TokenBuffer(null, false);
        tb.writeBoolean(true);
        JsonParser p = tb.asParser();
        p.nextToken();
        deser.deserializeFromBoolean(p, mapper.getDeserializationContext());
    }

    // -------------------------------------------------------------------------
    // Partition E: Object Lifecycle & Contract Integrity
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testObjectIdPropertyGeneratorLifecycle() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"id\":1,\"name\":\"first\",\"next\":1}";
        IdBean bean = mapper.readValue(json, IdBean.class);
        assertNotNull(bean);
        assertEquals(1, bean.id);
        assertEquals("first", bean.name);
        assertSame(bean, bean.next);

        BeanDeserializerBase idDeser = getBaseDeserializer(mapper, IdBean.class);
        assertNotNull(idDeser.getObjectIdReader());
        assertEquals("id", idDeser.getObjectIdReader().propertyName.getSimpleName());

        // Test _handleTypedObjectId with direct ID conversion
        DeserializationContext ctxt = mapper.getDeserializationContext();
        if (ctxt instanceof DefaultDeserializationContext) {
            ctxt = ((DefaultDeserializationContext) ctxt).createInstance(mapper.getDeserializationConfig(), null, null);
        }
        TokenBuffer tb = new TokenBuffer(null, false);
        tb.writeNumber(2);
        JsonParser p = tb.asParser();
        p.nextToken();

        IdBean target = new IdBean();
        Object handled = idDeser._handleTypedObjectId(p, ctxt, target, Long.valueOf(2L));
        assertSame(target, handled);
        assertEquals(2, target.id);
    }

    @Test(timeout = 4000)
    public void testDeserializeFromEmbedded() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        BeanDeserializerBase deser = getBaseDeserializer(mapper, SimpleBean.class);

        TokenBuffer tb = new TokenBuffer(null, false);
        tb.writeEmbeddedObject("custom_embedded_obj");
        JsonParser p = tb.asParser();
        p.nextToken();

        Object embedded = deser.deserializeFromEmbedded(p, mapper.getDeserializationContext());
        assertEquals("custom_embedded_obj", embedded);
    }

    @Test(timeout = 4000)
    public void testHandlePolymorphicAndSubclassCache() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        BeanDeserializerBase deser = getBaseDeserializer(mapper, SimpleBean.class);
        DeserializationContext ctxt = mapper.getDeserializationContext();
        if (ctxt instanceof DefaultDeserializationContext) {
            ctxt = ((DefaultDeserializationContext) ctxt).createInstance(mapper.getDeserializationConfig(), null, null);
        }

        SubBean sub = new SubBean();
        sub.x = 10;
        sub.y = "poly";

        // First invocation populates cache
        Object result1 = deser.handlePolymorphic(null, ctxt, sub, null);
        assertNotNull(result1);
        assertTrue(result1 instanceof SubBean);

        // Second invocation uses cached subclass deserializer
        Object result2 = deser.handlePolymorphic(null, ctxt, sub, null);
        assertNotNull(result2);
        assertTrue(result2 instanceof SubBean);
    }

    @Test(timeout = 4000)
    public void testHandleUnknownVanillaAndPropertiesBuffer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        BeanDeserializerBase deser = getBaseDeserializer(mapper, SimpleBean.class);
        DeserializationContext ctxt = mapper.getDeserializationContext();
        if (ctxt instanceof DefaultDeserializationContext) {
            ctxt = ((DefaultDeserializationContext) ctxt).createInstance(mapper.getDeserializationConfig(), null, null);
        }

        TestBeanDeserializer ignoringDeser = new TestBeanDeserializer(deser, Collections.singleton("skipProp"));

        TokenBuffer tb = new TokenBuffer(null, false);
        tb.writeString("someValue");
        JsonParser p = tb.asParser();
        p.nextToken();

        SimpleBean bean = new SimpleBean();
        // Should skip without error since skipProp is in ignorable props
        ignoringDeser.handleUnknownVanilla(p, ctxt, bean, "skipProp");

        // Test handleUnknownProperties with TokenBuffer
        TokenBuffer unknownTokens = new TokenBuffer(null, false);
        unknownTokens.writeStartObject();
        unknownTokens.writeFieldName("skipProp");
        unknownTokens.writeString("ignored");
        ignoringDeser.handleUnknownProperties(ctxt, bean, unknownTokens);
    }
}