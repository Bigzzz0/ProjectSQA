package com.fasterxml.jackson.databind.introspect;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.type.TypeBindings;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import com.fasterxml.jackson.databind.util.StdConverter;

/*
 * [Branch & Defect Analysis Matrix]
 * Class Under Test: com.fasterxml.jackson.databind.introspect.BasicBeanDescription
 *
 * Target Branches & Paths Covered:
 * 1. Construction paths:
 *    - forDeserialization(POJOPropertiesCollector), forSerialization(POJOPropertiesCollector)
 *    - forOtherUse(MapperConfig, JavaType, AnnotatedClass)
 *    - Constructors with null config vs non-null config (_annotationIntrospector initialized or null).
 * 2. Property modification and lookup:
 *    - removeProperty(): existing name (returns true, removed from list), non-existing (returns false)
 *    - addProperty(): duplicate name (returns false), unique name (returns true)
 *    - hasProperty(PropertyName) & findProperty(PropertyName): found vs not found
 *    - findProperties() / _properties(): lazy initialization via _propCollector vs pre-supplied list
 * 3. Introspection methods & Accessors:
 *    - findJsonValueMethod() and findJsonValueAccessor(): null collector vs with @JsonValue method/field
 *    - getIgnoredPropertyNames(): null collector, no ignored properties, with ignored properties
 *    - hasKnownClassAnnotations() & getClassAnnotations()
 *    - bindingsForBeanType() & resolveType(Type): null type vs valid type
 *    - findDefaultConstructor(), getConstructors()
 *    - instantiateBean(boolean fixAccess):
 *        * no default constructor -> returns null
 *        * valid default constructor with fixAccess true and false
 *        * constructor throws exception -> unwraps cause, throws IllegalArgumentException
 *        * abstract class instantiation failure -> unwrapped & rethrown
 * 4. AnySetter & AnyGetter introspection & Validation Guards:
 *    - findAnySetterAccessor():
 *        * null collector -> returns null
 *        * method with String/Object 1st param -> returns method
 *        * method with invalid 1st param (e.g. int) -> throws IllegalArgumentException
 *        * field of java.util.Map type -> returns field
 *        * field not implementing Map -> throws IllegalArgumentException
 *    - findAnyGetter():
 *        * null collector / no getter -> null
 *        * valid Map getter -> returns member
 *        * invalid non-Map getter -> throws IllegalArgumentException
 * 5. Format, Views, Inclusion, Converters:
 *    - findExpectedFormat(JsonFormat.Value): with/without per-class @JsonFormat, with/without defValue
 *    - findDefaultViews(): caching branch (_defaultViewsResolved), DEFAULT_VIEW_INCLUSION disabled -> NO_VIEWS
 *    - findPropertyInclusion(JsonInclude.Value): null defValue vs overrides
 *    - _createConverter(Object): null -> null, Converter instance, None.class -> null, invalid Class/Object -> IllegalStateException
 *    - findSerializationConverter() and findDeserializationConverter()
 * 6. Back references & duplicate handling:
 *    - findBackReferences(): none, single, multiple with same name -> throws IllegalArgumentException
 *    - findBackReferenceProperties(): deprecated map mapping
 * 7. Factory methods & single-arg constructors:
 *    - isFactoryMethod(): incompatible return type (false), @JsonCreator (true), valueOf(1 arg vs 2 args),
 *      fromString(1 arg String/CharSequence vs non-string)
 *    - findSingleArgConstructor(), findFactoryMethod()
 * 8. POJOBuilder and descriptions:
 *    - findPOJOBuilder(), findPOJOBuilderConfig(), findClassDescription()
 * 9. Defect Ground Truth targeting:
 *    - Defects4J defect: com.fasterxml.jackson.databind.exc.BasicExceptionTest::testLocationAddition
 *      Verifies deserialization failure on Enum Map key does not append duplicate 'at [' location markers.
 */
public class BasicBeanDescriptionGeminiTest {

    // --------------------------------------------------------------------
    // Fixture Classes for Introspection
    // --------------------------------------------------------------------

    static enum TestEnumABC {
        A, B, C
    }

    static class SimplePojo {
        public String name;
        public int age;

        public SimplePojo() {}
        public SimplePojo(String name) { this.name = name; }
        public SimplePojo(int age) { this.age = age; }

        public static SimplePojo valueOf(String text) {
            SimplePojo p = new SimplePojo();
            p.name = text;
            return p;
        }

        public static SimplePojo valueOf(String text, int extra) {
            return new SimplePojo(text);
        }

        public static SimplePojo fromString(CharSequence cs) {
            SimplePojo p = new SimplePojo();
            p.name = cs.toString();
            return p;
        }

        public static SimplePojo fromString(int val) {
            return new SimplePojo(val);
        }

        public static String notAFactory(String text) {
            return text;
        }
    }

    static class NoDefaultConstructorPojo {
        public String val;
        public NoDefaultConstructorPojo(String val) { this.val = val; }
    }

    static abstract class AbstractPojo {
        public AbstractPojo() {}
    }

    static class FailingConstructorPojo {
        public FailingConstructorPojo() {
            throw new IllegalStateException("Intentional constructor failure");
        }
    }

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    static class FormattedPojo {
        @JsonProperty("id")
        private String id;

        public String getId() { return id; }
    }

    @JsonView(String.class)
    static class ViewedPojo {
        public String text;
    }

    static class JsonValueMethodPojo {
        private String val = "test";
        @JsonValue
        public String getVal() { return val; }
    }

    static class JsonValueFieldPojo {
        @JsonValue
        public String val = "testField";
    }

    static class IgnoredPojo {
        public String kept;
        @JsonIgnore
        public String ignored;
    }

    static class ValidAnySetterMethodPojo {
        private Map<String, Object> map = new HashMap<>();
        @JsonAnySetter
        public void set(String key, Object value) { map.put(key, value); }
    }

    static class ValidAnySetterObjectMethodPojo {
        private Map<Object, Object> map = new HashMap<>();
        @JsonAnySetter
        public void set(Object key, Object value) { map.put(key, value); }
    }

    static class InvalidAnySetterMethodPojo {
        @JsonAnySetter
        public void set(int key, Object value) {}
    }

    static class ValidAnySetterFieldPojo {
        @JsonAnySetter
        public Map<String, Object> extra = new HashMap<>();
    }

    static class InvalidAnySetterFieldPojo {
        @JsonAnySetter
        public String extra = "not-a-map";
    }

    static class ValidAnyGetterPojo {
        private Map<String, Object> map = new HashMap<>();
        @JsonAnyGetter
        public Map<String, Object> getMap() { return map; }
    }

    static class InvalidAnyGetterPojo {
        @JsonAnyGetter
        public String getMap() { return "invalid-map-type"; }
    }

    static class ParentPojo {
        public String id;
    }

    static class ChildWithBackRefPojo {
        @JsonBackReference("parentRef")
        public ParentPojo parent;
    }

    static class DuplicateBackRefPojo {
        @JsonBackReference("dupRef")
        public ParentPojo parent1;

        @JsonBackReference("dupRef")
        public ParentPojo parent2;
    }

    @JsonPOJOBuilder(buildMethodName = "construct", withPrefix = "have")
    static class CustomBuilder {
        public String prop;
        public CustomBuilder haveProp(String p) { this.prop = p; return this; }
        public String construct() { return prop; }
    }

    @JsonDeserialize(builder = CustomBuilder.class)
    static class PojoWithBuilder {
    }

    static class DummyConverter extends StdConverter<String, Integer> {
        @Override
        public Integer convert(String value) {
            return Integer.parseInt(value);
        }
    }

    // --------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // --------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testBasicIntrospectionPropertiesAndAccessors() {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(SimplePojo.class);
        BasicBeanDescription desc = (BasicBeanDescription) mapper.getSerializationConfig().introspect(type);

        assertNotNull(desc.getClassInfo());
        assertEquals(SimplePojo.class, desc.getBeanClass());
        assertNotNull(desc.findProperties());
        assertTrue(desc.findProperties().size() >= 2);
        assertTrue(desc.hasKnownClassAnnotations());
        assertNotNull(desc.getClassAnnotations());
        assertNotNull(desc.getConstructors());
        assertNotNull(desc.findDefaultConstructor());
    }

    @Test(timeout = 4000)
    public void testPropertyAddAndRemove() {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(SimplePojo.class);
        BasicBeanDescription desc = (BasicBeanDescription) mapper.getSerializationConfig().introspect(type);

        assertTrue(desc.hasProperty(new PropertyName("name")));
        assertNotNull(desc.findProperty(new PropertyName("name")));

        // Remove property
        boolean removed = desc.removeProperty("name");
        assertTrue(removed);
        assertFalse(desc.hasProperty(new PropertyName("name")));
        assertNull(desc.findProperty(new PropertyName("name")));

        // Removing non-existent property
        assertFalse(desc.removeProperty("name"));
        assertFalse(desc.removeProperty("completelyNonExistent"));

        // Add back removed property using one from the other properties
        BeanPropertyDefinition ageProp = desc.findProperty(new PropertyName("age"));
        assertNotNull(ageProp);

        // Trying to add existing property should return false
        assertFalse(desc.addProperty(ageProp));
    }

    @Test(timeout = 4000)
    public void testJsonValueAccessors() {
        ObjectMapper mapper = new ObjectMapper();

        // 1. Method annotated with @JsonValue
        JavaType type1 = mapper.constructType(JsonValueMethodPojo.class);
        BasicBeanDescription desc1 = (BasicBeanDescription) mapper.getSerializationConfig().introspect(type1);
        assertNotNull(desc1.findJsonValueMethod());
        assertEquals("getVal", desc1.findJsonValueMethod().getName());
        assertNotNull(desc1.findJsonValueAccessor());

        // 2. Field annotated with @JsonValue
        JavaType type2 = mapper.constructType(JsonValueFieldPojo.class);
        BasicBeanDescription desc2 = (BasicBeanDescription) mapper.getSerializationConfig().introspect(type2);
        assertNull(desc2.findJsonValueMethod());
        assertNotNull(desc2.findJsonValueAccessor());
        assertEquals("val", desc2.findJsonValueAccessor().getName());
    }

    @Test(timeout = 4000)
    public void testIgnoredProperties() {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(IgnoredPojo.class);
        BasicBeanDescription desc = (BasicBeanDescription) mapper.getDeserializationConfig().introspect(type);

        Set<String> ignored = desc.getIgnoredPropertyNames();
        assertNotNull(ignored);
        assertTrue(ignored.contains("ignored"));
        assertFalse(ignored.contains("kept"));
    }

    @Test(timeout = 4000)
    public void testFactoryMethodsDetection() {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(SimplePojo.class);
        BasicBeanDescription desc = (BasicBeanDescription) mapper.getDeserializationConfig().introspect(type);

        List<AnnotatedMethod> factories = desc.getFactoryMethods();
        assertNotNull(factories);
        assertFalse(factories.isEmpty());

        Method mValueOf = desc.findFactoryMethod(String.class);
        assertNotNull(mValueOf);
        assertEquals("valueOf", mValueOf.getName());

        Method mFromString = desc.findFactoryMethod(CharSequence.class);
        assertNotNull(mFromString);
        assertEquals("fromString", mFromString.getName());

        // Multi-argument or non-matching factory method shouldn't be matched by findFactoryMethod(Class)
        Method mNonFactory = desc.findFactoryMethod(Boolean.class);
        assertNull(mNonFactory);
    }

    @Test(timeout = 4000)
    public void testSingleArgConstructorDetection() {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(SimplePojo.class);
        BasicBeanDescription desc = (BasicBeanDescription) mapper.getDeserializationConfig().introspect(type);

        Constructor<?> ctorString = desc.findSingleArgConstructor(String.class);
        assertNotNull(ctorString);
        assertEquals(String.class, ctorString.getParameterTypes()[0]);

        Constructor<?> ctorInt = desc.findSingleArgConstructor(int.class);
        assertNotNull(ctorInt);
        assertEquals(int.class, ctorInt.getParameterTypes()[0]);

        Constructor<?> ctorBoolean = desc.findSingleArgConstructor(Boolean.class);
        assertNull(ctorBoolean);
    }

    @Test(timeout = 4000)
    public void testInstantiateBeanSuccessAndNoDefaultCtor() {
        ObjectMapper mapper = new ObjectMapper();

        // 1. Successful default instantiator
        JavaType type1 = mapper.constructType(SimplePojo.class);
        BasicBeanDescription desc1 = (BasicBeanDescription) mapper.getDeserializationConfig().introspect(type1);
        Object instance = desc1.instantiateBean(true);
        assertNotNull(instance);
        assertTrue(instance instanceof SimplePojo);

        Object instance2 = desc1.instantiateBean(false);
        assertNotNull(instance2);

        // 2. Class without default constructor returns null
        JavaType type2 = mapper.constructType(NoDefaultConstructorPojo.class);
        BasicBeanDescription desc2 = (BasicBeanDescription) mapper.getDeserializationConfig().introspect(type2);
        assertNull(desc2.instantiateBean(false));
    }

    @Test(timeout = 4000)
    public void testFormatAndInclusion() {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(FormattedPojo.class);
        BasicBeanDescription desc = (BasicBeanDescription) mapper.getSerializationConfig().introspect(type);

        JsonFormat.Value format = desc.findExpectedFormat(null);
        assertNotNull(format);
        assertEquals(JsonFormat.Shape.OBJECT, format.getShape());

        JsonFormat.Value mergedFormat = desc.findExpectedFormat(JsonFormat.Value.empty());
        assertEquals(JsonFormat.Shape.OBJECT, mergedFormat.getShape());

        JsonInclude.Value incl = desc.findPropertyInclusion(null);
        assertNotNull(incl);
        assertEquals(JsonInclude.Include.NON_EMPTY, incl.getValueInclusion());

        JsonInclude.Value defaultIncl = JsonInclude.Value.empty().withValueInclusion(JsonInclude.Include.ALWAYS);
        JsonInclude.Value overridden = desc.findPropertyInclusion(defaultIncl);
        assertEquals(JsonInclude.Include.NON_EMPTY, overridden.getValueInclusion());
    }

    @Test(timeout = 4000)
    public void testFindDefaultViewsAndResolution() {
        ObjectMapper mapper = new ObjectMapper();

        // With viewed class
        JavaType type = mapper.constructType(ViewedPojo.class);
        BasicBeanDescription desc = (BasicBeanDescription) mapper.getSerializationConfig().introspect(type);
        Class<?>[] views = desc.findDefaultViews();
        assertNotNull(views);
        assertEquals(1, views.length);
        assertEquals(String.class, views[0]);

        // Repeated call to hit cache (_defaultViewsResolved == true)
        assertSame(views, desc.findDefaultViews());