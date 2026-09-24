package com.fasterxml.jackson.databind.deser;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.*;
import java.util.*;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;

/*
 * [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * Method Under Test                | Branch / Condition Targeted                | Test Method
 * ----------------------------------------------------------------------------------------------------
 * checkIllegalTypes                | databind#1737 (FileHandler illegal type)   | testJDKTypes1737_FileHandler
 * checkIllegalTypes                | databind#1737 (UnicastRemoteObject illegal)| testJDKTypes1737_UnicastRemoteObject
 * checkIllegalTypes                | Known illegal class (JdbcRowSetImpl)       | testCheckIllegalTypes_JdbcRowSetImpl
 * withConfig                       | Same config (identity check -> this)       | testWithConfig_SameInstance
 * withConfig                       | Different config -> new Factory instance   | testWithConfig_NewInstance
 * withConfig                       | Subtype not overriding withConfig -> Error | testWithConfig_SubtypeThrows
 * isPotentialBeanType              | Primitive type -> IllegalArgumentException | testIsPotentialBeanType_Primitive
 * isPotentialBeanType              | Array type -> IllegalArgumentException     | testIsPotentialBeanType_Array
 * isPotentialBeanType              | Enum type -> IllegalArgumentException      | testIsPotentialBeanType_Enum
 * isPotentialBeanType              | Proxy type -> IllegalArgumentException     | testIsPotentialBeanType_Proxy
 * isPotentialBeanType              | In-method local class -> IllegalArgumentEx | testIsPotentialBeanType_LocalClass
 * isPotentialBeanType              | Valid POJO class -> true                   | testIsPotentialBeanType_ValidBean
 * createBeanDeserializer           | Custom deserializer override               | testCreateBeanDeserializer_CustomOverride
 * createBeanDeserializer           | isPotentialBeanType returns false -> null  | testCreateBeanDeserializer_NotPotentialBean
 * materializeAbstractType          | AbstractTypeResolver resolution            | testMaterializeAbstractType_Resolved
 * buildBeanDeserializer            | Abstract type without resolution (buildAbs)| testBuildBeanDeserializer_AbstractUninstantiable
 * buildThrowableDeserializer       | Exception deserialization & initCause      | testBuildThrowableDeserializer_InitCause
 * buildThrowableDeserializer       | DeserializerModifier decoration on error   | testBuildThrowableDeserializer_WithModifier
 * createBuilderBasedDeserializer   | Standard builder with withPrefix           | testBuilderBasedDeserializer_Standard
 * createBuilderBasedDeserializer   | POJOBuilder with custom build name/prefix  | testBuilderBasedDeserializer_CustomPOJOBuilder
 * createBuilderBasedDeserializer   | Builder modified by BeanDeserializerMod    | testBuilderBasedDeserializer_WithModifier
 * addObjectIdReader                | Property-based ObjectIdGenerator           | testAddObjectIdReader_PropertyGenerator
 * addObjectIdReader                | Property generator with missing prop name  | testAddObjectIdReader_InvalidPropertyThrows
 * addObjectIdReader                | IntSequenceGenerator (non-property based)  | testAddObjectIdReader_IntSequenceGenerator
 * addBeanProps                     | Setterless collections / map handling      | testAddBeanProps_SetterlessProperties
 * addBeanProps                     | @JsonIgnoreProperties annotation           | testAddBeanProps_IgnoredProperties
 * addBeanProps                     | @JsonIgnoreType class annotation           | testAddBeanProps_IgnoredType
 * addBeanProps                     | MapperFeature.DEFAULT_VIEW_INCLUSION off   | testAddBeanProps_ViewInclusionDisabled
 * addReferenceProperties           | Managed / Back reference wiring            | testAddReferenceProperties_ManagedAndBack
 * addInjectables                   | @JacksonInject parameter injection         | testAddInjectables_JacksonInject
 * constructAnySetter               | @JsonAnySetter invocation                  | testConstructAnySetter_JsonAnySetter
 * isIgnorableType                  | Direct cache verification                  | testIsIgnorableType_Caching
 * findStdDeserializer              | DeserializerModifier applied to std type   | testFindStdDeserializer_WithModifier
 * java.io.Serializable             | Java serialization / deserialization round | testSerializationIntegrity
 * ----------------------------------------------------------------------------------------------------
 */
public class BeanDeserializerFactoryGeminiTest {

    // =========================================================================
    // Test Dummy Classes & Mock Models
    // =========================================================================

    static class SimpleBean {
        public String name;
        public int age;
    }

    static class CreatorBean {
        public final String first;
        public final int second;

        @JsonCreator
        public CreatorBean(@JsonProperty("first") String first,
                           @JsonProperty("second") int second) {
            this.first = first;
            this.second = second;
        }
    }

    @JsonIgnoreProperties({"secret"})
    static class IgnoredPropBean {
        public String secret;
        public String visible;
    }

    @JsonIgnoreType
    static class IgnoredType {
        public String data;
    }

    static class BeanWithIgnoredTypeProp {
        public IgnoredType ignored;
        public String name;
    }

    @JsonDeserialize(builder = SimpleBuilder.class)
    static class ValueClass {
        final int x;
        ValueClass(int x) { this.x = x; }
    }

    static class SimpleBuilder {
        private int x;
        public SimpleBuilder withX(int x) { this.x = x; return this; }
        public ValueClass build() { return new ValueClass(x); }
    }

    @JsonDeserialize(builder = CustomBuilder.class)
    static class CustomValue {
        final String val;
        CustomValue(String val) { this.val = val; }
    }

    @JsonPOJOBuilder(buildMethodName = "create", withPrefix = "set")
    static class CustomBuilder {
        private String val;
        public CustomBuilder setVal(String v) { this.val = v; return this; }
        public CustomValue create() { return new CustomValue(val); }
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    static class IdBean {
        public int id;
        public String value;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
    static class IntIdBean {
        public String name;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "nonExistent")
    static class InvalidIdBean {
        public int id;
    }

    static class AnySetterBean {
        private final Map<String, Object> extra = new HashMap<String, Object>();

        @JsonAnySetter
        public void setAny(String key, Object value) {
            extra.put(key, value);
        }

        public Map<String, Object> getExtra() {
            return extra;
        }
    }

    static class InjectBean {
        @JacksonInject("injectedVal")
        public String injected;
        public String regular;
    }

    static class Parent {
        @JsonManagedReference
        public List<Child> children = new ArrayList<Child>();
    }

    static class Child {
        @JsonBackReference
        public Parent parent;
        public String name;
    }

    static class SetterlessBean {
        private final List<String> items = new ArrayList<String>();
        private final Map<String, String> map = new HashMap<String, String>();

        public List<String> getItems() { return items; }
        public Map<String, String> getMap() { return map; }
    }

    static class CustomException extends Exception {
        private static final long serialVersionUID = 1L;
        public CustomException() { super(); }
        public CustomException(String msg) { super(msg); }
    }

    interface MyInterface {
        String getName();
    }

    static class MyInterfaceImpl implements MyInterface {
        private String name;
        public MyInterfaceImpl() {}
        public MyInterfaceImpl(String name) { this.name = name; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    abstract static class AbstractUninstantiable {
        public String val;
    }

    static class ViewViews {
        static class Public {}
        static class Private {}
    }

    static class ViewBean {
        @JsonView(ViewViews.Public.class)
        public String pub;
        @JsonView(ViewViews.Private.class)
        public String priv;
    }

    enum TestEnum { FOO, BAR }

    static class SubFactoryWithoutOverride extends BeanDeserializerFactory {
        public SubFactoryWithoutOverride(DeserializerFactoryConfig config) {
            super(config);
        }
    }

    static class RejectingBeanDeserializerFactory extends BeanDeserializerFactory {
        public RejectingBeanDeserializerFactory(DeserializerFactoryConfig config) {
            super(config);
        }

        @Override
        protected boolean isPotentialBeanType(Class<?> type) {
            return false;
        }

        @Override
        public DeserializerFactory withConfig(DeserializerFactoryConfig config) {
            return new RejectingBeanDeserializerFactory(config);
        }
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (databind#1737 / Illegal Types)
    // =========================================================================

    /**
     * Targets Defects4J ground truth: IllegalTypesCheckTest::testJDKTypes1737
     * Tests that java.util.logging.FileHandler is recognized as an illegal type
     * and blocked from deserialization for security reasons.
     */
    @Test(timeout = 4000)
    public void testJDKTypes1737_FileHandler() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readValue("{}", java.util.logging.FileHandler.class);
            fail("Should not pass: java.util.logging.FileHandler must be blocked as an illegal type");
        } catch (JsonMappingException e) {
            String msg = e.getMessage();
            assertTrue("Exception message should contain 'Illegal type', got: " + msg,
                    msg.contains("Illegal type"));
            assertTrue("Exception message should contain 'prevented for security reasons', got: " + msg,
                    msg.contains("prevented for security reasons"));
        }
    }

    /**
     * Targets databind#1737 JDK remote object blocking:
     * Tests that java.rmi.server.UnicastRemoteObject is blocked from deserialization.
     */
    @Test(timeout = 4000)
    public void testJDKTypes1737_UnicastRemoteObject() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readValue("{}", java.rmi.server.UnicastRemoteObject.class);
            fail("Should not pass: java.rmi.server.UnicastRemoteObject must be blocked as an illegal type");
        } catch (JsonMappingException e) {
            String msg = e.getMessage();
            assertTrue("Exception message should contain 'Illegal type', got: " + msg,
                    msg.contains("Illegal type"));
            assertTrue("Exception message should contain 'prevented for security reasons', got: " + msg,
                    msg.contains("prevented for security reasons"));
        }
    }

    /**
     * Verifies checkIllegalTypes against default illegal type: com.sun.rowset.JdbcRowSetImpl.
     */
    @Test(timeout = 4000)
    public void testCheckIllegalTypes_JdbcRowSetImpl() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readValue("{}", com.sun.rowset.JdbcRowSetImpl.class);
            fail("Expected security JsonMappingException for JdbcRowSetImpl");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Illegal type"));
            assertTrue(e.getMessage().contains("prevented for security reasons"));
        }
    }

    /**
     * Directly invokes checkIllegalTypes on BeanDeserializerFactory instance.
     */
    @Test(timeout = 4000)
    public void testCheckIllegalTypes_DirectInvocation() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(com.sun.rowset.JdbcRowSetImpl.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);

        try {
            BeanDeserializerFactory.instance.checkIllegalTypes(ctxt, type, desc);
            fail("Should have thrown JsonMappingException directly from checkIllegalTypes");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Illegal type"));
            assertTrue(e.getMessage().contains("com.sun.rowset.JdbcRowSetImpl"));
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testWithConfig_SameInstance() {
        BeanDeserializerFactory factory = BeanDeserializerFactory.instance;
        DeserializerFactory result = factory.withConfig(factory.getFactoryConfig());
        assertSame("withConfig should return 'this' when config instance is identical", factory, result);
    }

    @Test(timeout = 4000)
    public void testWithConfig_NewInstance() {
        BeanDeserializerFactory factory = BeanDeserializerFactory.instance;
        DeserializerFactoryConfig newConfig = new DeserializerFactoryConfig();
        DeserializerFactory result = factory.withConfig(newConfig);
        assertNotNull(result);
        assertNotSame(factory, result);
        assertEquals(BeanDeserializerFactory.class, result.getClass());
    }

    @Test(timeout = 4000)
    public void testCreateBeanDeserializer_CustomOverride() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule mod = new SimpleModule();
        mod.addDeserializer(SimpleBean.class, new JsonDeserializer<SimpleBean>() {
            @Override
            public SimpleBean deserialize(com.fasterxml.jackson.core.JsonParser p, DeserializationContext ctxt) {
                SimpleBean bean = new SimpleBean();
                bean.name = "CUSTOM_OVERRIDE";
                bean.age = 999;
                return bean;
            }
        });
        mapper.registerModule(mod);

        SimpleBean bean = mapper.readValue("{\"name\":\"Alice\",\"age\":30}", SimpleBean.class);
        assertEquals("CUSTOM_OVERRIDE", bean.name);
        assertEquals(999, bean.age);
    }

    @Test(timeout = 4000)
    public void testMaterializeAbstractType_Resolved() throws Exception {
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(MyInterface.class, MyInterfaceImpl.class);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule().setAbstractTypes(resolver));

        MyInterface result = mapper.readValue("{\"name\":\"materialized\"}", MyInterface.class);
        assertNotNull(result);
        assertTrue(result instanceof MyInterfaceImpl);
        assertEquals("materialized", result.getName());
    }

    @Test(timeout = 4000)
    public void testBuildThrowableDeserializer_InitCause() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"message\":\"outer-error\",\"cause\":{\"message\":\"root-cause\"}}";
        CustomException ex = mapper.readValue(json, CustomException.class);

        assertNotNull(ex);
        assertEquals("outer-error", ex.getMessage());
        assertNotNull(ex.getCause());
        assertEquals("root-cause", ex.getCause().getMessage());
    }

    @Test(timeout = 4000)
    public void testBuildThrowableDeserializer_WithModifier() throws Exception {
        final boolean[] modifierCalled = new boolean[1];
        BeanDeserializerModifier mod = new BeanDeserializerModifier() {
            @Override
            public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config,
                    BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
                if (Throwable.class.isAssignableFrom(beanDesc.getBeanClass())) {
                    modifierCalled[0] = true;
                }
                return deserializer;
            }
        };

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule().setDeserializerModifier(mod));

        CustomException ex = mapper.readValue("{\"message\":\"error\"}", CustomException.class);
        assertEquals("error", ex.getMessage());
        assertTrue("DeserializerModifier should have been called for Throwable", modifierCalled[0]);
    }

    @Test(timeout = 4000)
    public void testBuilderBasedDeserializer_Standard() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ValueClass value = mapper.readValue("{\"x\":42}", ValueClass.class);
        assertNotNull(value);
        assertEquals(42, value.x);
    }

    @Test(timeout = 4000)
    public void testBuilderBasedDeserializer_CustomPOJOBuilder() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        CustomValue value = mapper.readValue("{\"val\":\"hello\"}", CustomValue.class);
        assertNotNull(value);
        assertEquals("hello", value.val);
    }

    @Test(timeout = 4000)
    public void testBuilderBasedDeserializer_WithModifier() throws Exception {
        final boolean[] updateBuilderCalled = new boolean[1];
        final boolean[] modifyDeserCalled = new boolean[1];
        BeanDeserializerModifier mod = new BeanDeserializerModifier() {
            @Override
            public BeanDeserializerBuilder updateBuilder(DeserializationConfig config,
                    BeanDescription beanDesc, BeanDeserializerBuilder builder) {
                if (beanDesc.getBeanClass() == SimpleBuilder.class) {
                    updateBuilderCalled[0] = true;
                }
                return builder;
            }

            @Override
            public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config,
                    BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
                if (beanDesc.getBeanClass() == SimpleBuilder.class) {
                    modifyDeserCalled[0] = true;
                }
                return deserializer;
            }
        };

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule().setDeserializerModifier(mod));

        ValueClass vc = mapper.readValue("{\"x\":88}", ValueClass.class);
        assertEquals(88, vc.x);
        assertTrue(updateBuilderCalled[0]);
        assertTrue(modifyDeserCalled[0]);
    }

    @Test(timeout = 4000)
    public void testAddObjectIdReader_PropertyGenerator() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        IdBean bean = mapper.readValue("{\"id\":101,\"value\":\"first\"}", IdBean.class);
        assertNotNull(bean);
        assertEquals(101, bean.id);
        assertEquals("first", bean.value);
    }

    @Test(timeout = 4000)
    public void testAddObjectIdReader_IntSequenceGenerator() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        IntIdBean bean = mapper.readValue("{\"@id\":1,\"name\":\"seq\"}", IntIdBean.class);
        assertNotNull(bean);
        assertEquals("seq", bean.name);
    }

    @Test(timeout = 4000)
    public void testAddBeanProps_SetterlessProperties() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SetterlessBean bean = mapper.readValue(
                "{\"items\":[\"a\",\"b\"],\"map\":{\"key\":\"value\"}}",
                SetterlessBean.class
        );
        assertNotNull(bean);
        assertEquals(2, bean.getItems().size());
        assertEquals("a", bean.getItems().get(0));
        assertEquals("b", bean.getItems().get(1));
        assertEquals("value", bean.getMap().get("key"));
    }

    @Test(timeout = 4000)
    public void testAddBeanProps_IgnoredProperties() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        IgnoredPropBean bean = mapper.readValue(
                "{\"secret\":\"classified\",\"visible\":\"clear\"}",
                IgnoredPropBean.class
        );
        assertNull(bean.secret);
        assertEquals("clear", bean.visible);
    }

    @Test(timeout = 4000)
    public void testAddBeanProps_IgnoredType() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        BeanWithIgnoredTypeProp bean = mapper.readValue(
                "{\"ignored\":{\"data\":\"something\"},\"name\":\"valid\"}",
                BeanWithIgnoredTypeProp.class
        );
        assertNull(bean.ignored);
        assertEquals("valid", bean.name);
    }

    @Test(timeout = 4000)
    public void testAddBeanProps_ViewInclusionDisabled() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);

        ViewBean bean = mapper.readerWithView(ViewViews.Public.class)
                .forType(ViewBean.class)
                .readValue("{\"pub\":\"publicVal\",\"priv\":\"privateVal\"}");

        assertEquals("publicVal", bean.pub);
        assertNull(bean.priv);
    }

    @Test(timeout = 4000)
    public void testAddReferenceProperties_ManagedAndBack() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Parent parent = mapper.readValue(
                "{\"children\":[{\"name\":\"child1\"},{\"name\":\"child2\"}]}",
                Parent.class
        );
        assertNotNull(parent.children);
        assertEquals(2, parent.children.size());
        assertSame(parent, parent.children.get(0).parent);
        assertSame(parent, parent.children.get(1).parent);
    }

    @Test(timeout = 4000)
    public void testAddInjectables_JacksonInject() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        InjectableValues.Std injectables = new InjectableValues.Std();
        injectables.addValue("injectedVal", "INJECTED_STRING");
        mapper.setInjectableValues(injectables);

        InjectBean bean = mapper.readValue("{\"regular\":\"regularVal\"}", InjectBean.class);
        assertEquals("INJECTED_STRING", bean.injected);
        assertEquals("regularVal", bean.regular);
    }

    @Test(timeout = 4000)
    public void testConstructAnySetter_JsonAnySetter() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AnySetterBean bean = mapper.readValue(
                "{\"prop1\":\"val1\",\"prop2\":123}",
                AnySetterBean.class
        );
        assertEquals("val1", bean.getExtra().get("prop1"));
        assertEquals(123, bean.getExtra().get("prop2"));
    }

    @Test(timeout = 4000)
    public void testCreatorProperties_Binding() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        CreatorBean bean = mapper.readValue("{\"first\":\"hello\",\"second\":50}", CreatorBean.class);
        assertNotNull(bean);
        assertEquals("hello", bean.first);
        assertEquals(50, bean.second);
    }

    @Test(timeout = 4000)
    public void testFindStdDeserializer_WithModifier() throws Exception {
        final boolean[] modifierHit = new boolean[1];
        BeanDeserializerModifier mod = new BeanDeserializerModifier() {
            @Override
            public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config,
                    BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
                if (beanDesc.getBeanClass() == UUID.class) {
                    modifierHit[0] = true;
                }
                return deserializer;
            }
        };

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule().setDeserializerModifier(mod));

        UUID uuid = mapper.readValue("\"00000000-0000-0000-0000-000000000000\"", UUID.class);
        assertNotNull(uuid);
        assertTrue("Modifier should intercept standard deserializer construction", modifierHit[0]);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testIsPotentialBeanType_ValidBean() {
        assertTrue(BeanDeserializerFactory.instance.isPotentialBeanType(SimpleBean.class));
    }

    @Test(timeout = 4000)
    public void testIsIgnorableType_Caching() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDescription desc = config.introspect(type);
        Map<Class<?>, Boolean> cache = new HashMap<Class<?>, Boolean>();

        boolean isIgnored = BeanDeserializerFactory.instance.isIgnorableType(config, desc, IgnoredType.class, cache);
        assertTrue(isIgnored);
        assertTrue("Result must be cached in map", cache.containsKey(IgnoredType.class));
        assertTrue("Cached result should remain true", BeanDeserializerFactory.instance.isIgnorableType(config, desc, IgnoredType.class, cache));

        assertFalse(BeanDeserializerFactory.instance.isIgnorableType(config, desc, SimpleBean.class, cache));
        assertFalse(cache.get(SimpleBean.class));
    }

    @Test(timeout = 4000)
    public void testDefaultIllegalClassNames_SetIntegrity() {
        Set<String> illegal = BeanDeserializerFactory.instance._cfgIllegalClassNames;
        assertNotNull(illegal);
        assertTrue(illegal.contains("com.sun.rowset.JdbcRowSetImpl"));
        assertTrue(illegal.contains("org.apache.commons.collections.functors.InvokerTransformer"));
        assertTrue(illegal.contains("org.apache.commons.collections.functors.InstantiateTransformer"));
        assertTrue(illegal.contains("org.apache.commons.collections4.functors.InvokerTransformer"));
        assertTrue(illegal.contains("org.apache.commons.collections4.functors.InstantiateTransformer"));
        assertTrue(illegal.contains("org.codehaus.groovy.runtime.ConvertedClosure"));
        assertTrue(illegal.contains("org.codehaus.groovy.runtime.MethodClosure"));
        assertTrue(illegal.contains("org.springframework.beans.factory.ObjectFactory"));
        assertTrue(illegal.contains("com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl"));
        assertTrue(illegal.contains("org.apache.xalan.xsltc.trax.TemplatesImpl"));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testWithConfig_SubtypeThrows() {
        SubFactoryWithoutOverride subFactory = new SubFactoryWithoutOverride(new DeserializerFactoryConfig());
        subFactory.withConfig(new DeserializerFactoryConfig());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testIsPotentialBeanType_Primitive() {
        BeanDeserializerFactory.instance.isPotentialBeanType(int.class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testIsPotentialBeanType_Array() {
        BeanDeserializerFactory.instance.isPotentialBeanType(String[].class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testIsPotentialBeanType_Enum() {
        BeanDeserializerFactory.instance.isPotentialBeanType(TestEnum.class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testIsPotentialBeanType_Proxy() {
        Class<?> proxyClass = java.lang.reflect.Proxy.getProxyClass(
                getClass().getClassLoader(),
                Serializable.class
        );
        BeanDeserializerFactory.instance.isPotentialBeanType(proxyClass);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testIsPotentialBeanType_LocalClass() {
        class InMethodLocalClass {}
        BeanDeserializerFactory.instance.isPotentialBeanType(InMethodLocalClass.class);
    }

    @Test(timeout = 4000)
    public void testAddObjectIdReader_InvalidPropertyThrows() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readValue("{\"id\":1}", InvalidIdBean.class);
            fail("Expected failure when Object Id references nonexistent property");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("can not find property with name 'nonExistent'"));
        } catch (JsonMappingException e) {
            assertTrue(e.getCause() instanceof IllegalArgumentException);
            assertTrue(e.getCause().getMessage().contains("can not find property with name 'nonExistent'"));
        }
    }

    @Test(timeout = 4000)
    public void testBuildBeanDeserializer_AbstractUninstantiable() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readValue("{\"val\":\"foo\"}", AbstractUninstantiable.class);
            fail("Abstract uninstantiable type without concrete mapping should fail");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Can not instantiate abstract type")
                    || e.getMessage().contains("abstract"));
        }
    }

    @Test(timeout = 4000)
    public void testCreateBeanDeserializer_NotPotentialBean() throws Exception {
        RejectingBeanDeserializerFactory rejectingFactory = new RejectingBeanDeserializerFactory(
                new DeserializerFactoryConfig()
        );
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);

        JsonDeserializer<Object> deser = rejectingFactory.createBeanDeserializer(ctxt, type, desc);
        assertNull("If isPotentialBeanType is false, createBeanDeserializer must return null", deser);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testSerializationIntegrity() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(BeanDeserializerFactory.instance);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object deserialized = ois.readObject();
        ois.close();

        assertNotNull(deserialized);
        assertEquals(BeanDeserializerFactory.class, deserialized.getClass());
    }
}