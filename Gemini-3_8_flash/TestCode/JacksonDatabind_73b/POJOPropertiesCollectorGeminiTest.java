/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.fasterxml.jackson.databind.introspect.POJOPropertiesCollector
 *
 * 1. Defect Analysis (Defects4J / Jackson-databind #935 / ReadOrWriteOnlyTest):
 *    - Problem: Deserialization of classes with read-only properties (via @JsonProperty(access = Access.READ_ONLY)
 *      or getter-only accessors when mutators are absent) fails with UnrecognizedPropertyException because the
 *      property name is not registered into `_ignoredPropertyNames` unless explicit ignorals (@JsonIgnore) are present.
 *    - In `_removeUnwantedProperties`: `_collectIgnorals` was only called if `prop.anyIgnorals()` was true.
 *    - In `_removeUnwantedAccessor`: `prop.removeNonVisible(inferMutators)` trims accessors without tracking non-deserializable
 *      properties into `_ignoredPropertyNames`.
 *    - Target Tests:
 *      * `testReadOnlyPropertyIgnoredDuringDeserialization_Defect935`: tests deserialization of a POJO with Access.READ_ONLY
 *         property ("fullName") and verifies it is ignored during deserialization rather than throwing UnrecognizedPropertyException.
 *      * `testReadXWriteY_DefectTarget`: tests deserialization where property "x" is read-only and "y" is write-only.
 *
 * 2. Decision & Branch Coverage Target Matrix:
 *    - Constructor:
 *      * `_mutatorPrefix == null` vs non-null (default to "set")
 *      * `_annotationIntrospector == null` (disabled annotation processing) vs enabled
 *      * `findAutoDetectVisibility` configuration
 *    - Public APIs:
 *      * `getConfig()`, `getType()`, `getClassDef()`, `getAnnotationIntrospector()`
 *      * `getProperties()`: returns copy of properties list
 *      * `getInjectables()`: empty vs populated vs duplicate ID throwing IllegalArgumentException
 *      * `getJsonValueMethod()`: none vs single vs multiple throwing IllegalArgumentException
 *      * `getAnyGetter()`: none vs single vs multiple throwing IllegalArgumentException
 *      * `getAnySetterMethod()`: none vs single vs multiple throwing IllegalArgumentException
 *      * `getAnySetterField()`: none vs single vs multiple throwing IllegalArgumentException
 *      * `getObjectIdInfo()`: null introspector vs introspector with null vs non-null ObjectIdInfo
 *      * `findPOJOBuilderClass()`: returns builder class or null
 *      * `getIgnoredPropertyNames()`: populated vs null
 *    - Internal Member Collection (_addFields, _addMethods, _addCreators, _addInjectables):
 *      * Fields: pruneFinalFields (for deser, final fields pruned unless allowed/ignored), transient field handling
 *        (MapperFeature.PROPAGATE_TRANSIENT_MARKER), @JsonAnySetter field
 *      * Methods: 0-arg (regular getter, is-getter, @JsonAnyGetter, @JsonValue), 1-arg (regular setter, prefix matches,
 *        @JsonSetter), 2-arg (setter marked with @JsonAnySetter)
 *      * Creators: constructors vs static factory methods, implicit vs explicit parameter names, non-static inner class exclusion
 *      * Renaming & Strategies: PropertyNamingStrategy (subclass, Class, PropertyNamingStrategy.class default),
 *        Wrapper names (MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME), Explicit renaming override feature
 *      * Sorting: explicit order (@JsonPropertyOrder), creator-first ordering, alphabetical ordering
 */

package com.fasterxml.jackson.databind.introspect;

import java.io.IOException;
import java.util.*;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.MapperConfig;

public class POJOPropertiesCollectorGeminiTest {

    // =========================================================================
    // Helper POJOs for Structural & Branch Testing
    // =========================================================================

    public static class SimpleBean {
        public int x;
        protected String y;

        public SimpleBean() { }

        public SimpleBean(int x, String y) {
            this.x = x;
            this.y = y;
        }

        public String getY() { return y; }
        public void setY(String y) { this.y = y; }
    }

    public static class BooleanBean {
        private boolean flag;
        public boolean isFlag() { return flag; }
        public void setFlag(boolean f) { this.flag = f; }
    }

    public static class AnyGetterBean {
        private Map<String, Object> map = new HashMap<String, Object>();

        @JsonAnyGetter
        public Map<String, Object> any() {
            return map;
        }
    }

    public static class MultipleAnyGetterBean {
        @JsonAnyGetter
        public Map<String, Object> any1() { return Collections.emptyMap(); }

        @JsonAnyGetter
        public Map<String, Object> any2() { return Collections.emptyMap(); }
    }

    public static class AnySetterMethodBean {
        private Map<String, Object> map = new HashMap<String, Object>();

        @JsonAnySetter
        public void setAny(String k, Object v) {
            map.put(k, v);
        }
    }

    public static class MultipleAnySetterMethodBean {
        @JsonAnySetter
        public void setAny1(String k, Object v) {}

        @JsonAnySetter
        public void setAny2(String k, Object v) {}
    }

    public static class AnySetterFieldBean {
        @JsonAnySetter
        public Map<String, Object> anyField = new HashMap<String, Object>();
    }

    public static class MultipleAnySetterFieldBean {
        @JsonAnySetter
        public Map<String, Object> anyField1 = new HashMap<String, Object>();

        @JsonAnySetter
        public Map<String, Object> anyField2 = new HashMap<String, Object>();
    }

    public static class JsonValueBean {
        private final String val;
        public JsonValueBean(String v) { this.val = v; }

        @JsonValue
        public String value() { return val; }
    }

    public static class MultipleJsonValueBean {
        @JsonValue
        public String val1() { return "1"; }

        @JsonValue
        public String val2() { return "2"; }
    }

    public static class CreatorBean {
        protected final int a;
        protected final String b;

        @JsonCreator
        public CreatorBean(@JsonProperty("a") int a, @JsonProperty("b") String b) {
            this.a = a;
            this.b = b;
        }

        public int getA() { return a; }
        public String getB() { return b; }
    }

    public static class StaticCreatorBean {
        protected final int val;

        private StaticCreatorBean(int v) { this.val = v; }

        @JsonCreator
        public static StaticCreatorBean create(@JsonProperty("val") int v) {
            return new StaticCreatorBean(v);
        }

        public int getVal() { return val; }
    }

    public static class InjectableBean {
        @JacksonInject("id1")
        public String value;

        public String methodVal;

        @JacksonInject("id2")
        public void setMethodVal(String v) {
            this.methodVal = v;
        }
    }

    public static class DuplicateInjectableBean {
        @JacksonInject("sameId")
        public String field1;

        @JacksonInject("sameId")
        public String field2;
    }

    @JsonPropertyOrder({"c", "b", "a"})
    public static class OrderedBean {
        public int a = 1;
        public int b = 2;
        public int c = 3;
    }

    public static class FinalFieldsBean {
        public final int finalValue = 42;
        public int regularValue = 10;
    }

    public static class TransientBean {
        public transient int transValue = 99;
        public int normalValue = 1;
    }

    @JsonRootName("customWrapper")
    public static class WrapperBean {
        public String prop = "value";
    }

    // =========================================================================
    // Defect Ground Truth POJOs (Jackson-databind #935 & ReadOrWriteOnlyTest)
    // =========================================================================

    public static class Pojo935 {
        private String firstName;
        private String lastName;

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        public String getFullName() {
            return firstName + " " + lastName;
        }

        public String getFirstName() { return firstName; }
        public void setFirstName(String n) { firstName = n; }
        public String getLastName() { return lastName; }
        public void setLastName(String n) { lastName = n; }
    }

    public static class ReadXWriteY {
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        public int x = 1;

        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        public int y = 2;
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================

    private POJOPropertiesCollector createCollector(Class<?> cls, boolean forSerialization) {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(cls);
        MapperConfig<?> config = forSerialization ? mapper.getSerializationConfig() : mapper.getDeserializationConfig();
        AnnotatedClass ac = AnnotatedClass.construct(cls, config);
        return new POJOPropertiesCollector(config, forSerialization, type, ac, "set");
    }

    private POJOPropertiesCollector createCollectorWithPrefix(Class<?> cls, boolean forSerialization, String prefix) {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(cls);
        MapperConfig<?> config = forSerialization ? mapper.getSerializationConfig() : mapper.getDeserializationConfig();
        AnnotatedClass ac = AnnotatedClass.construct(cls, config);
        return new POJOPropertiesCollector(config, forSerialization, type, ac, prefix);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicPropertiesCollectionForSerialization() {
        POJOPropertiesCollector collector = createCollector(SimpleBean.class, true);
        assertNotNull(collector.getConfig());
        assertNotNull(collector.getType());
        assertNotNull(collector.getClassDef());
        assertNotNull(collector.getAnnotationIntrospector());

        List<BeanPropertyDefinition> props = collector.getProperties();
        assertNotNull(props);
        assertEquals(2, props.size());

        Set<String> propNames = new HashSet<String>();
        for (BeanPropertyDefinition prop : props) {
            propNames.add(prop.getName());
        }
        assertTrue(propNames.contains("x"));
        assertTrue(propNames.contains("y"));
    }

    @Test(timeout = 4000)
    public void testBasicPropertiesCollectionForDeserialization() {
        POJOPropertiesCollector collector = createCollector(SimpleBean.class, false);
        List<BeanPropertyDefinition> props = collector.getProperties();
        assertEquals(2, props.size());

        POJOPropertiesCollector collectorCopy = collector.collect();
        assertSame(collector, collectorCopy);
    }

    @Test(timeout = 4000)
    public void testBooleanIsGetterSupport() {
        POJOPropertiesCollector collector = createCollector(BooleanBean.class, true);
        List<BeanPropertyDefinition> props = collector.getProperties();
        assertEquals(1, props.size());
        assertEquals("flag", props.get(0).getName());
    }

    @Test(timeout = 4000)
    public void testAnyGetterDetection() {
        POJOPropertiesCollector collector = createCollector(AnyGetterBean.class, true);
        AnnotatedMember anyGetter = collector.getAnyGetter();
        assertNotNull(anyGetter);
        assertEquals("any", anyGetter.getName());
    }

    @Test(timeout = 4000)
    public void testAnySetterMethodDetection() {
        POJOPropertiesCollector collector = createCollector(AnySetterMethodBean.class, false);
        AnnotatedMethod anySetter = collector.getAnySetterMethod();
        assertNotNull(anySetter);
        assertEquals("setAny", anySetter.getName());
    }

    @Test(timeout = 4000)
    public void testAnySetterFieldDetection() {
        POJOPropertiesCollector collector = createCollector(AnySetterFieldBean.class, false);
        AnnotatedMember anySetterField = collector.getAnySetterField();
        assertNotNull(anySetterField);
        assertEquals("anyField", anySetterField.getName());
    }

    @Test(timeout = 4000)
    public void testJsonValueMethodDetection() {
        POJOPropertiesCollector collector = createCollector(JsonValueBean.class, true);
        AnnotatedMethod jv = collector.getJsonValueMethod();
        assertNotNull(jv);
        assertEquals("value", jv.getName());
    }

    @Test(timeout = 4000)
    public void testConstructorCreatorProperties() {
        POJOPropertiesCollector collector = createCollector(CreatorBean.class, false);
        List<BeanPropertyDefinition> props = collector.getProperties();
        assertEquals(2, props.size());

        Set<String> names = new HashSet<String>();
        for (BeanPropertyDefinition bpd : props) {
            names.add(bpd.getName());
            assertTrue(bpd.hasConstructorParameter());
        }
        assertTrue(names.contains("a"));
        assertTrue(names.contains("b"));
    }

    @Test(timeout = 4000)
    public void testStaticMethodCreatorProperties() {
        POJOPropertiesCollector collector = createCollector(StaticCreatorBean.class, false);
        List<BeanPropertyDefinition> props = collector.getProperties();
        assertEquals(1, props.size());
        assertEquals("val", props.get(0).getName());
    }

    @Test(timeout = 4000)
    public void testInjectablesCollection() {
        POJOPropertiesCollector collector = createCollector(InjectableBean.class, false);
        Map<Object, AnnotatedMember> injectables = collector.getInjectables();
        assertNotNull(injectables);
        assertEquals(2, injectables.size());
        assertTrue(injectables.containsKey("id1"));
        assertTrue(injectables.containsKey("id2"));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Configuration Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefaultMutatorPrefixWhenNull() {
        POJOPropertiesCollector collector = createCollectorWithPrefix(SimpleBean.class, false, null);
        List<BeanPropertyDefinition> props = collector.getProperties();
        boolean foundY = false;
        for (BeanPropertyDefinition p : props) {
            if ("y".equals(p.getName())) {
                foundY = true;
                assertTrue(p.hasSetter());
            }
        }
        assertTrue(foundY);
    }

    @Test(timeout = 4000)
    public void testCustomMutatorPrefix() {
        class BuilderStyle {
            public int x;
            public void withX(int v) { this.x = v; }
        }
        POJOPropertiesCollector collector = createCollectorWithPrefix(BuilderStyle.class, false, "with");
        List<BeanPropertyDefinition> props = collector.getProperties();
        assertEquals(1, props.size());
        assertEquals("x", props.get(0).getName());
        assertTrue(props.get(0).hasSetter());
    }

    @Test(timeout = 4000)
    public void testPruningFinalFieldsForDeserialization() {
        POJOPropertiesCollector collectorSer = createCollector(FinalFieldsBean.class, true);
        assertEquals(2, collectorSer.getProperties().size());

        POJOPropertiesCollector collectorDeser = createCollector(FinalFieldsBean.class, false);
        List<BeanPropertyDefinition> deserProps = collectorDeser.getProperties();
        assertEquals(1, deserProps.size());
        assertEquals("regularValue", deserProps.get(0).getName());
    }

    @Test(timeout = 4000)
    public void testTransientFieldPropagatedAsIgnoral() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(MapperFeature.PROPAGATE_TRANSIENT_MARKER);
        JavaType type = mapper.constructType(TransientBean.class);
        MapperConfig<?> config = mapper.getDeserializationConfig();
        AnnotatedClass ac = AnnotatedClass.construct(TransientBean.class, config);
        POJOPropertiesCollector collector = new POJOPropertiesCollector(config, false, type, ac, "set");

        List<BeanPropertyDefinition> props = collector.getProperties();
        assertEquals(1, props.size());
        assertEquals("normalValue", props.get(0).getName());

        Set<String> ignored = collector.getIgnoredPropertyNames();
        assertNotNull(ignored);
        assertTrue(ignored.contains("transValue"));
    }

    @Test(timeout = 4000)
    public void testExplicitOrderingOfProperties() {
        POJOPropertiesCollector collector = createCollector(OrderedBean.class, true);
        List<BeanPropertyDefinition> props = collector.getProperties();
        assertEquals(3, props.size());
        assertEquals("c", props.get(0).getName());
        assertEquals("b", props.get(1).getName());
        assertEquals("a", props.get(2).getName());
    }

    @Test(timeout = 4000)
    public void testAlphabeticalSortingFeature() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
        JavaType type = mapper.constructType(SimpleBean.class);
        MapperConfig<?> config = mapper.getSerializationConfig();
        AnnotatedClass ac = AnnotatedClass.construct(SimpleBean.class, config);
        POJOPropertiesCollector collector = new POJOPropertiesCollector(config, true, type, ac, "set");

        List<BeanPropertyDefinition> props = collector.getProperties();
        assertEquals(2, props.size());
        assertEquals("x", props.get(0).getName());
        assertEquals("y", props.get(1).getName());
    }

    @Test(timeout = 4000)
    public void testRenameWithNamingStrategy() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        class NamingBean {
            public int firstName = 1;
        }
        JavaType type = mapper.constructType(NamingBean.class);
        MapperConfig<?> config = mapper.getSerializationConfig();
        AnnotatedClass ac = AnnotatedClass.construct(NamingBean.class, config);
        POJOPropertiesCollector collector = new POJOPropertiesCollector(config, true, type, ac, "set");

        List<BeanPropertyDefinition> props = collector.getProperties();
        assertEquals(1, props.size());
        assertEquals("first_name", props.get(0).getName());
    }

    @Test(timeout = 4000)
    public void testWrapperNameAsPropertyName() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME);
        JavaType type = mapper.constructType(WrapperBean.class);
        MapperConfig<?> config = mapper.getSerializationConfig();
        AnnotatedClass ac = AnnotatedClass.construct(WrapperBean.class, config);
        POJOPropertiesCollector collector = new POJOPropertiesCollector(config, true, type, ac, "set");

        List<BeanPropertyDefinition> props = collector.getProperties();
        assertEquals(1, props.size());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Zone (Jackson-databind #935 & ReadOrWriteOnlyTest)
    // =========================================================================

    @Test(timeout = 4000)
    public void testReadOnlyPropertyIgnoredDuringDeserialization_Defect935() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"firstName\":\"Bob\",\"lastName\":\"Burger\",\"fullName\":\"Bob Burger\"}";

        // When deserializing, fullName is READ_ONLY and has no setter.
        // On unpatched Jackson, this throws UnrecognizedPropertyException: Unrecognized field "fullName".
        // It must deserialize successfully by ignoring the read-only "fullName".
        Pojo935 result = mapper.readValue(json, Pojo935.class);
        assertNotNull(result);
        assertEquals("Bob", result.getFirstName());
        assertEquals("Burger", result.getLastName());
        assertEquals("Bob Burger", result.getFullName());
    }

    @Test(timeout = 4000)
    public void testReadXWriteY_DefectTarget() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"x\":10,\"y\":20}";

        // 'x' is READ_ONLY, 'y' is WRITE_ONLY.
        // Deserializing must succeed: 'x' should be ignored (not throw UnrecognizedPropertyException)
        // and 'y' should be populated.
        ReadXWriteY result = mapper.readValue(json, ReadXWriteY.class);
        assertNotNull(result);
        assertEquals(1, result.x); // x remains unchanged default because it is read-only
        assertEquals(20, result.y); // y is deserialized
    }

    @Test(timeout = 4000)
    public void testIgnoredPropertyCollectorDirectly_Defect935() {
        POJOPropertiesCollector collector = createCollector(Pojo935.class, false);
        // Calling getProperties triggers collection
        collector.getProperties();
        Set<String> ignored = collector.getIgnoredPropertyNames();
        assertNotNull("Ignored property names must not be null when read-only properties exist", ignored);
        assertTrue("Expected 'fullName' to be registered as an ignored property for deserialization",
                ignored.contains("fullName"));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testMultipleAnyGettersThrowsException() {
        POJOPropertiesCollector collector = createCollector(MultipleAnyGetterBean.class, true);
        collector.getAnyGetter();
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testMultipleAnySettersMethodThrowsException() {
        POJOPropertiesCollector collector = createCollector(MultipleAnySetterMethodBean.class, false);
        collector.getAnySetterMethod();
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testMultipleAnySetterFieldThrowsException() {
        POJOPropertiesCollector collector = createCollector(MultipleAnySetterFieldBean.class, false);
        collector.getAnySetterField();
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testMultipleJsonValueThrowsException() {
        POJOPropertiesCollector collector = createCollector(MultipleJsonValueBean.class, true);
        collector.getJsonValueMethod();
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testDuplicateInjectablesThrowsException() {
        POJOPropertiesCollector collector = createCollector(DuplicateInjectableBean.class, false);
        collector.getInjectables();
    }

    // =========================================================================
    // Partition E: Object Lifecycle, Introspection Null Checks & Mutators
    // =========================================================================

    @Test(timeout = 4000)
    public void testObjectIdInfoAndBuilderClassWhenNonePresent() {
        POJOPropertiesCollector collector = createCollector(SimpleBean.class, true);
        assertNull(collector.getObjectIdInfo());
        assertNull(collector.findPOJOBuilderClass());
        assertNull(collector.getAnyGetter());
        assertNull(collector.getAnySetterMethod());
        assertNull(collector.getAnySetterField());
        assertNull(collector.getJsonValueMethod());
    }

    @Test(timeout = 4000)
    public void testPropertyMapIsCopySafe() {
        POJOPropertiesCollector collector = createCollector(SimpleBean.class, true);
        List<BeanPropertyDefinition> props1 = collector.getProperties();
        List<BeanPropertyDefinition> props2 = collector.getProperties();
        assertNotSame(props1, props2);
        assertEquals(props1.size(), props2.size());
    }

    @Test(timeout = 4000)
    public void testNonStaticInnerClassCreatorsIgnored() {
        class Outer {
            class Inner {
                public int a;
                @JsonCreator
                public Inner(@JsonProperty("a") int a) {
                    this.a = a;
                }
            }
        }
        POJOPropertiesCollector collector = createCollector(Outer.Inner.class, false);
        List<BeanPropertyDefinition> props = collector.getProperties();
        assertNotNull(props);
    }
}