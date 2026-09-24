package com.fasterxml.jackson.databind.deser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Proxy;
import java.util.*;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.deser.impl.DefaultDeserializationContext;
import com.fasterxml.jackson.databind.deser.std.ThrowableDeserializer;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;

/*
 * [Branch & Defect Analysis Matrix]
 * -------------------------------------------------------------------------------------------------
 * Method                           | Target Condition / Branch                   | Coverage Focus
 * -------------------------------------------------------------------------------------------------
 * withConfig                       | _factoryConfig == config                    | Identity return
 * withConfig                       | getClass() != BeanDeserializerFactory.class | Subclass error guard
 * withConfig                       | Proper subclass override                    | Delegated factory
 * createBeanDeserializer           | Custom deserializer match                   | _findCustomBeanDeserializer
 * createBeanDeserializer           | type.isThrowable()                          | buildThrowableDeserializer
 * createBeanDeserializer           | type.isAbstract() && !isPrimitive()         | materializeAbstractType
 * createBeanDeserializer           | findStdDeserializer != null                 | Standard deser path
 * createBeanDeserializer           | !isPotentialBeanType()                      | Bail out / Exception guard
 * createBeanDeserializer           | Defect #1599: Unsafe/Illegal Bean Types     | Security check / prevention
 * isPotentialBeanType              | ClassUtil.canBeABeanType (primitive, array) | IllegalArgumentException
 * isPotentialBeanType              | ClassUtil.isProxyType (dynamic proxy)       | IllegalArgumentException
 * isPotentialBeanType              | ClassUtil.isLocalType (local method class)  | IllegalArgumentException
 * buildBeanDeserializer            | Ignorable types / properties                | filterBeanProps, isIgnorable
 * buildBeanDeserializer            | AnySetter presence                          | constructAnySetter
 * buildBeanDeserializer            | Setterless Map/Collection                   | constructSetterlessProperty
 * buildBeanDeserializer            | Creator properties & Fallback setters       | Constructor param linking
 * buildBeanDeserializer            | View processing (DEFAULT_VIEW_INCLUSION)   | NO_VIEWS fallback
 * addObjectIdReader                | PropertyGenerator (valid property)          | ObjectIdReader setup
 * addObjectIdReader                | PropertyGenerator (missing property)        | IllegalArgumentException
 * addObjectIdReader                | Non-Property generator                      | Class-level Id resolution
 * buildBuilderBasedDeserializer    | Builder with JsonPOJOBuilder annotation     | POJOBuilder custom naming
 * buildBuilderBasedDeserializer    | Builder without annotation (default build)  | "build" fallback
 * buildThrowableDeserializer       | initCause method introspection              | "cause" property injection
 * (Serialization Contract)         | Serializable implementation                 | Deep copy preservation
 * -------------------------------------------------------------------------------------------------
 */
public class BeanDeserializerFactoryGeminiTest {

    // =========================================================================
    // Test POJOs & Dummy Fixtures
    // =========================================================================

    public static class SimpleBean {
        private String name;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    public static class CustomThrowable extends Throwable {
        private static final long serialVersionUID = 1L;
        public CustomThrowable() { super(); }
        public CustomThrowable(String msg) { super(msg); }
    }

    public static abstract class AbstractSample {
        public String text;
    }

    public static class ConcreteSample extends AbstractSample {
        public ConcreteSample() {}
    }

    public static abstract class NonInstantiableAbstract {
        public abstract String getValue();
    }

    @JsonIgnoreProperties({"blockedField"})
    public static class IgnoredPropsBean {
        public String blockedField;
        public String allowedField;
        @JsonIgnore public String explicitIgnored;
    }

    @JsonIgnoreType
    public static class ForbiddenIgnoredType {
        public int id;
    }

    public static class ContainerForIgnoredType {
        public ForbiddenIgnoredType dummy;
        public String normal;
    }

    public static class AnySetterBean {
        private final Map<String, Object> values = new HashMap<String, Object>();

        @JsonAnySetter
        public void set(String key, Object val) {
            values.put(key, val);
        }

        public Map<String, Object> getValues() { return values; }
    }

    public static class SetterlessBean {
        private final List<String> items = new ArrayList<String>();
        public List<String> getItems() { return items; }
    }

    public static class CreatorBean {
        private final String code;
        private final int count;

        @JsonCreator
        public CreatorBean(@JsonProperty("code") String code, @JsonProperty("count") int count) {
            this.code = code;
            this.count = count;
        }

        public String getCode() { return code; }
        public int getCount() { return count; }
    }

    public static class InjectableBean {
        @JacksonInject
        public String injected;
        public String plain;
    }

    public static class ManagedParent {
        @JsonManagedReference
        public ManagedChild child;
    }

    public static class ManagedChild {
        @JsonBackReference
        public ManagedParent parent;
        public String note;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    public static class ValidIdBean {
        public int id;
        public String label;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "invalidId")
    public static class MissingPropertyIdBean {
        public int id;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@seq")
    public static class SequenceIdBean {
        public String title;
    }

    public static class ViewBean {
        @JsonView(Object.class)
        public String visible;
    }

    @JsonDeserialize(builder = AnnotatedBuilder.class)
    public static class PojoWithAnnotatedBuilder {
        final String data;
        PojoWithAnnotatedBuilder(String data) { this.data = data; }
    }

    @JsonPOJOBuilder(buildMethodName = "produce", withPrefix = "assign")
    public static class AnnotatedBuilder {
        private String data;
        public AnnotatedBuilder assignData(String data) { this.data = data; return this; }
        public PojoWithAnnotatedBuilder produce() { return new PojoWithAnnotatedBuilder(data); }
    }

    public static class PlainBuilder {
        private String data;
        public PlainBuilder setData(String data) { this.data = data; return this; }
        public SimpleBean build() {
            SimpleBean b = new SimpleBean();
            b.setName(data);
            return b;
        }
    }

    public static class BadSubtypeFactory extends BeanDeserializerFactory {
        private static final long serialVersionUID = 1L;
        public BadSubtypeFactory(DeserializerFactoryConfig config) {
            super(config);
        }
    }

    public static class GoodSubtypeFactory extends BeanDeserializerFactory {
        private static final long serialVersionUID = 1L;
        public GoodSubtypeFactory(DeserializerFactoryConfig config) {
            super(config);
        }
        @Override
        public DeserializerFactory withConfig(DeserializerFactoryConfig config) {
            return new GoodSubtypeFactory(config);
        }
    }

    private DefaultDeserializationContext createTestContext(ObjectMapper mapper) throws Exception {
        return ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(mapper.getDeserializationConfig(), mapper.getFactory().createParser("{}"), mapper.getInjectableValues());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateBeanDeserializerStandard() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = createTestContext(mapper);
        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);

        JsonDeserializer<Object> deser = BeanDeserializerFactory.instance.createBeanDeserializer(ctxt, type, desc);
        assertNotNull("Deserializer must be created for simple POJO", deser);
        assertTrue("Deserializer should be BeanDeserializer", deser instanceof BeanDeserializer);

        SimpleBean bean = (SimpleBean) deser.deserialize(mapper.getFactory().createParser("{\"name\":\"jackson\"}"), ctxt);
        assertEquals("jackson", bean.getName());
    }

    @Test(timeout = 4000)
    public void testBuildThrowableDeserializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = createTestContext(mapper);
        JavaType type = mapper.constructType(CustomThrowable.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);

        JsonDeserializer<Object> deser = BeanDeserializerFactory.instance.createBeanDeserializer(ctxt, type, desc);
        assertNotNull("Deserializer must be created for Throwable", deser);
        assertTrue("Deserializer should be ThrowableDeserializer", deser instanceof ThrowableDeserializer);
    }

    @Test(timeout = 4000)
    public void testMaterializeAbstractType() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(AbstractSample.class, ConcreteSample.class);

        DeserializerFactoryConfig config = new DeserializerFactoryConfig().withAbstractTypeResolver(resolver);
        BeanDeserializerFactory factory = new BeanDeserializerFactory(config);

        DefaultDeserializationContext ctxt = ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(mapper.getDeserializationConfig(), mapper.getFactory().createParser("{}"), null);
        JavaType type = mapper.constructType(AbstractSample.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);

        JsonDeserializer<Object> deser = factory.createBeanDeserializer(ctxt, type, desc);
        assertNotNull("Materialized deserializer must not be null", deser);
        Object result = deser.deserialize(mapper.getFactory().createParser("{}"), ctxt);
        assertTrue("Materialized instance must be of ConcreteSample type", result instanceof ConcreteSample);
    }

    @Test(timeout = 4000)
    public void testCustomBeanDeserializerOverride() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleDeserializers customDesers = new SimpleDeserializers();
        final SimpleBean placeholder = new SimpleBean();
        placeholder.setName("override");

        JsonDeserializer<SimpleBean> customDeser = new JsonDeserializer<SimpleBean>() {
            @Override
            public SimpleBean deserialize(com.fasterxml.jackson.core.JsonParser p, DeserializationContext ctxt) {
                return placeholder;
            }
        };
        customDesers.addDeserializer(SimpleBean.class, customDeser);

        BeanDeserializerFactory factory = (BeanDeserializerFactory) BeanDeserializerFactory.instance.withAdditionalDeserializers(customDesers);
        DefaultDeserializationContext ctxt = ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(mapper.getDeserializationConfig(), mapper.getFactory().createParser("{}"), null);
        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);

        JsonDeserializer<Object> deser = factory.createBeanDeserializer(ctxt, type, desc);
        assertSame("Should return the registered custom deserializer", customDeser, deser);
    }

    @Test(timeout = 4000)
    public void testCreateBuilderBasedDeserializerWithAnnotation() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        PojoWithAnnotatedBuilder pojo = mapper.readValue("{\"data\":\"constructed\"}", PojoWithAnnotatedBuilder.class);
        assertNotNull(pojo);
        assertEquals("constructed", pojo.data);
    }

    @Test(timeout = 4000)
    public void testBuildBuilderBasedDeserializerWithoutAnnotation() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = createTestContext(mapper);
        JavaType valueType = mapper.constructType(SimpleBean.class);
        BeanDescription beanDesc = mapper.getDeserializationConfig().introspect(valueType);

        JsonDeserializer<Object> deser = BeanDeserializerFactory.instance.createBuilderBasedDeserializer(
                ctxt, valueType, beanDesc, PlainBuilder.class);
        assertNotNull(deser);

        SimpleBean bean = (SimpleBean) deser.deserialize(mapper.getFactory().createParser("{\"data\":\"builderVal\"}"), ctxt);
        assertEquals("builderVal", bean.getName());
    }

    @Test(timeout = 4000)
    public void testSetterlessCollectionProperty() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SetterlessBean bean = mapper.readValue("{\"items\":[\"x\",\"y\"]}", SetterlessBean.class);
        assertNotNull(bean.getItems());
        assertEquals(2, bean.getItems().size());
        assertEquals("x", bean.getItems().get(0));
        assertEquals("y", bean.getItems().get(1));
    }

    @Test(timeout = 4000)
    public void testCreatorPropertyAndBinding() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        CreatorBean bean = mapper.readValue("{\"code\":\"alpha\",\"count\":10}", CreatorBean.class);
        assertEquals("alpha", bean.getCode());
        assertEquals(10, bean.getCount());
    }

    @Test(timeout = 4000)
    public void testInjectableValuesHandling() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        InjectableValues.Std inject = new InjectableValues.Std();
        inject.addValue(String.class.getName(), "injectedSecret");

        InjectableBean bean = mapper.reader(inject).forType(InjectableBean.class).readValue("{\"plain\":\"public\"}");
        assertEquals("injectedSecret", bean.injected);
        assertEquals("public", bean.plain);
    }

    @Test(timeout = 4000)
    public void testManagedAndBackReferenceProperties() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ManagedParent parent = mapper.readValue("{\"child\":{\"note\":\"childNote\"}}", ManagedParent.class);
        assertNotNull(parent.child);
        assertEquals("childNote", parent.child.note);
        assertSame("Back reference must point to parent", parent, parent.child.parent);
    }

    @Test(timeout = 4000)
    public void testAnySetterHandling() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AnySetterBean bean = mapper.readValue("{\"key1\":\"val1\",\"key2\":\"val2\"}", AnySetterBean.class);
        assertEquals("val1", bean.getValues().get("key1"));
        assertEquals("val2", bean.getValues().get("key2"));
    }

    @Test(timeout = 4000)
    public void testObjectIdPropertyGeneratorValid() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ValidIdBean bean = mapper.readValue("{\"id\":99,\"label\":\"item99\"}", ValidIdBean.class);
        assertEquals(99, bean.id);
        assertEquals("item99", bean.label);
    }

    @Test(timeout = 4000)
    public void testObjectIdSequenceGenerator() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SequenceIdBean bean = mapper.readValue("{\"title\":\"sample\"}", SequenceIdBean.class);
        assertEquals("sample", bean.title);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testIsPotentialBeanTypeRejectsPrimitive() {
        BeanDeserializerFactory.instance.isPotentialBeanType(int.class);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testIsPotentialBeanTypeRejectsArray() {
        BeanDeserializerFactory.instance.isPotentialBeanType(String[].class);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testIsPotentialBeanTypeRejectsProxy() {
        Class<?> proxyClass = Proxy.getProxyClass(
                BeanDeserializerFactoryGeminiTest.class.getClassLoader(),
                Runnable.class
        );
        BeanDeserializerFactory.instance.isPotentialBeanType(proxyClass);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testIsPotentialBeanTypeRejectsLocalClass() {
        class LocalTypeInMethod {
            @SuppressWarnings("unused")
            public int x;
        }
        BeanDeserializerFactory.instance.isPotentialBeanType(LocalTypeInMethod.class);
    }

    @Test(timeout = 4000)
    public void testIgnorablePropertiesAndClassFilters() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        IgnoredPropsBean bean = mapper.readValue(
                "{\"allowedField\":\"ok\",\"blockedField\":\"ignoreMe\",\"explicitIgnored\":\"skipMe\"}",
                IgnoredPropsBean.class);
        assertEquals("ok", bean.allowedField);
        assertNull(bean.blockedField);
        assertNull(bean.explicitIgnored);
    }

    @Test(timeout = 4000)
    public void testIgnorableTypeAnnotationOnClass() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ContainerForIgnoredType container = mapper.readValue(
                "{\"dummy\":{\"id\":5},\"normal\":\"keep\"}",
                ContainerForIgnoredType.class);
        assertEquals("keep", container.normal);
        assertNull(container.dummy);
    }

    @Test(timeout = 4000)
    public void testDefaultViewInclusionDisabled() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
        DeserializationContext ctxt = createTestContext(mapper);
        JavaType type = mapper.constructType(ViewBean.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);

        JsonDeserializer<Object> deser = BeanDeserializerFactory.instance.createBeanDeserializer(ctxt, type, desc);
        assertNotNull(deser);
    }

    @Test(timeout = 4000)
    public void testBuildAbstractDeserializerForNonInstantiableType() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = createTestContext(mapper);
        JavaType type = mapper.constructType(NonInstantiableAbstract.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);

        JsonDeserializer<Object> deser = BeanDeserializerFactory.instance.buildBeanDeserializer(ctxt, type, desc);
        assertNotNull("Should build an abstract deserializer instance", deser);
        assertTrue("Must be an abstract deserializer", deser instanceof AbstractDeserializer);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (databind#1599)
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefectIssue1599IllegalTypeDeserializationPrevention() throws Exception {
        Class<?> illegalClass = null;
        for (String className : new String[] {
                "org.springframework.context.support.FileSystemXmlApplicationContext",
                "com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl",
                "org.apache.commons.collections.functors.InvokerTransformer"
        }) {
            try {
                illegalClass = Class.forName(className);
                break;
            } catch (Throwable ignored) {
            }
        }
        assertNotNull("Execution environment must provide at least one standard illegal type candidate", illegalClass);

        ObjectMapper mapper = new ObjectMapper();
        DefaultDeserializationContext ctxt = createTestContext(mapper);
        JavaType type = mapper.constructType(illegalClass);
        BeanDescription beanDesc = mapper.getDeserializationConfig().introspect(type);

        try {
            BeanDeserializerFactory.instance.createBeanDeserializer(ctxt, type, beanDesc);
            fail("Expected JsonMappingException indicating Illegal type prevented for security reasons [databind#1599]");
        } catch (JsonMappingException e) {
            String msg = e.getMessage();
            assertTrue("Expected exception message to contain 'Illegal type', got: " + msg,
                    msg != null && msg.contains("Illegal type"));
        }
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testWithConfigSubtypeImproperOverrideThrowsException() {
        BadSubtypeFactory badFactory = new BadSubtypeFactory(new DeserializerFactoryConfig());
        badFactory.withConfig(new DeserializerFactoryConfig());
    }

    @Test(timeout = 4000)
    public void testAddObjectIdReaderMissingPropertyThrowsException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readValue("{\"id\":1}", MissingPropertyIdBean.class);
            fail("Expected IllegalArgumentException due to nonexistent ObjectId property");
        } catch (JsonMappingException e) {
            assertTrue(e.getCause() instanceof IllegalArgumentException);
            assertTrue(e.getCause().getMessage().contains("can not find property with name 'invalidId'"));
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testWithConfigIdentityAndNewInstance() {
        BeanDeserializerFactory base = BeanDeserializerFactory.instance;
        DeserializerFactoryConfig config = base.getFactoryConfig();

        DeserializerFactory same = base.withConfig(config);
        assertSame("Passing the same configuration should return the identical factory instance", base, same);

        DeserializerFactoryConfig newConfig = new DeserializerFactoryConfig();
        DeserializerFactory distinct = base.withConfig(newConfig);
        assertNotSame("Passing a different configuration instance must return a new factory", base, distinct);
        assertSame(newConfig, distinct.getFactoryConfig());
    }

    @Test(timeout = 4000)
    public void testWithConfigProperSubclassOverride() {
        GoodSubtypeFactory goodFactory = new GoodSubtypeFactory(new DeserializerFactoryConfig());
        DeserializerFactoryConfig newConfig = new DeserializerFactoryConfig();
        DeserializerFactory result = goodFactory.withConfig(newConfig);

        assertNotNull(result);
        assertTrue(result instanceof GoodSubtypeFactory);
        assertSame(newConfig, result.getFactoryConfig());
    }

    @Test(timeout = 4000)
    public void testJavaSerializationRoundtrip() throws Exception {
        BeanDeserializerFactory original = BeanDeserializerFactory.instance;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object deserialized = ois.readObject();
        ois.close();

        assertNotNull(deserialized);
        assertTrue(deserialized instanceof BeanDeserializerFactory);
        assertNotNull(((BeanDeserializerFactory) deserialized).getFactoryConfig());
    }
}