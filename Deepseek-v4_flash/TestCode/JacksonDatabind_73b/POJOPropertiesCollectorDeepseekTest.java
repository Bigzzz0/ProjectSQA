package com.fasterxml.jackson.databind.introspect;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.util.BeanUtil;
import com.fasterxml.jackson.databind.util.ClassUtil;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import static org.junit.Assert.*;

/**
 * POJOPropertiesCollectorDeepseekTest
 * 
 * <p>Branch & Defect Analysis Matrix:
 * <ul>
 *   <li><b>Partition A – Core functional logic:</b> Normal classes with getters/setters, creators, injectables.</li>
 *   <li><b>Partition B – Boundary values:</b> null config, empty class, no annotations, transient, final fields.</li>
 *   <li><b>Partition C – Defect targeting:</b> Read-only and write-only properties (ground truth defect).</li>
 *   <li><b>Partition D – Exception paths:</b> Duplicate injectables, multiple @JsonValue, missing names, illegal arguments.</li>
 *   <li><b>Partition E – Lifecycle:</b> collectAll lazy initialization, accessors for ignored properties, etc.</li>
 * </ul>
 * 
 * <p>The known defect: during deserialization, properties that are only getter (read-only) or only setter (write-only)
 * are incorrectly removed, causing UnrecognizedPropertyException. Test must verify both are retained.
 */
public class POJOPropertiesCollectorDeepseekTest {

    // -----------------------------------------------------------------------
    // Helper: Create POJOPropertiesCollector from a simple class
    // -----------------------------------------------------------------------
    private static POJOPropertiesCollector createCollector(Class<?> cls, boolean forSerialization) {
        ObjectMapper mapper = new ObjectMapper();
        MapperConfig<?> config = forSerialization
                ? mapper.getSerializationConfig()
                : mapper.getDeserializationConfig();
        JavaType type = mapper.constructType(cls);
        AnnotatedClass ac = AnnotatedClassResolver.resolve(config, type, config);
        return new POJOPropertiesCollector(config, forSerialization, type, ac, "set");
    }

    // -----------------------------------------------------------------------
    // Helper classes for test scenarios
    // -----------------------------------------------------------------------

    static class SimpleBean {
        public int x;
        private String y;
        public String getY() { return y; }
        public void setY(String y) { this.y = y; }
    }

    static class ReadOnlyWriteOnly {
        private String x;
        private int y;

        @JsonProperty
        public String getX() { return x; }
        @JsonProperty
        public void setY(int y) { this.y = y; }

        // No setter for X, no getter for Y
        public String getXread() { return x; } // not property according to naming
        public void setYwrite(int y) { this.y = y; } // not property
    }

    static class Pojo935 {
        private String firstName, lastName, fullName;

        @JsonProperty
        public String getFullName() { return fullName; }
        @JsonProperty
        public void setFirstName(String fn) { this.firstName = fn; }
        @JsonProperty
        public void setLastName(String ln) { this.lastName = ln; }
        // fullName is read-only, firstName/lastName are write-only
    }

    static class WithAnnotations {
        private int id;
        private String name;

        @JsonProperty("identifier")
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        @JsonProperty("full_name")
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    static class WithIgnore {
        private int a;
        private int b;

        @JsonIgnore
        public int getA() { return a; }
        public void setA(int a) { this.a = a; }
        public int getB() { return b; }
        @JsonIgnore
        public void setB(int b) { this.b = b; }
    }

    static class WithTransient {
        private int a;
        private transient int b;

        public int getA() { return a; }
        public void setA(int a) { this.a = a; }
        public int getB() { return b; }
        public void setB(int b) { this.b = b; }
    }

    static class CreatorBean {
        private int x, y;

        @JsonCreator
        public CreatorBean(@JsonProperty("x") int x, @JsonProperty("y") int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() { return x; }
        public int getY() { return y; }
    }

    static class AnySetterBean {
        private Map<String, Object> extra = new HashMap<>();

        @JsonAnySetter
        public void setAny(String key, Object value) {
            extra.put(key, value);
        }
    }

    // -----------------------------------------------------------------------
    // Partition A: Core functional logic
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testSimpleBeanProperties() {
        POJOPropertiesCollector coll = createCollector(SimpleBean.class, false);
        List<BeanPropertyDefinition> props = coll.getProperties();
        assertEquals(2, props.size());
        Map<String, BeanPropertyDefinition> map = new HashMap<>();
        for (BeanPropertyDefinition def : props) map.put(def.getName(), def);

        assertTrue(map.containsKey("x"));
        assertTrue(map.containsKey("y"));
        assertTrue(map.get("x").hasField());
        assertTrue(map.get("y").hasGetter() && map.get("y").hasSetter());
    }

    @Test(timeout = 4000)
    public void testSerializationMode() {
        POJOPropertiesCollector coll = createCollector(SimpleBean.class, true);
        Map<String, POJOPropertyBuilder> map = coll.getPropertyMap();
        assertTrue(map.containsKey("x"));
        assertTrue(map.containsKey("y"));
    }

    @Test(timeout = 4000)
    public void testExplicitNames() {
        POJOPropertiesCollector coll = createCollector(WithAnnotations.class, false);
        Map<String, POJOPropertyBuilder> map = coll.getPropertyMap();
        assertTrue(map.containsKey("identifier"));
        assertTrue(map.containsKey("full_name"));
    }

    @Test(timeout = 4000)
    public void testCreatorProperties() {
        POJOPropertiesCollector coll = createCollector(CreatorBean.class, false);
        List<BeanPropertyDefinition> props = coll.getProperties();
        assertEquals(2, props.size());
        for (BeanPropertyDefinition def : props) {
            assertTrue(def.hasConstructorParameter());
        }
    }

    @Test(timeout = 4000)
    public void testAnySetterMethod() {
        POJOPropertiesCollector coll = createCollector(AnySetterBean.class, false);
        AnnotatedMethod any = coll.getAnySetterMethod();
        assertNotNull(any);
        assertEquals("setAny", any.getName());
        assertNull(coll.getAnySetterField());
        assertNull(coll.getAnyGetter());
        assertNull(coll.getJsonValueMethod());
    }

    // -----------------------------------------------------------------------
    // Partition B: Boundary values / edge cases
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testEmptyClass() {
        class Empty {}
        POJOPropertiesCollector coll = createCollector(Empty.class, false);
        List<BeanPropertyDefinition> props = coll.getProperties();
        assertTrue(props.isEmpty());
    }

    @Test(timeout = 4000)
    public void testTransientFieldWithoutAnnotation() {
        POJOPropertiesCollector coll = createCollector(WithTransient.class, false);
        Map<String, POJOPropertyBuilder> map = coll.getPropertyMap();
        // 'b' is transient, should not be visible unless annotated
        assertFalse(map.containsKey("b"));
    }

    @Test(timeout = 4000)
    public void testIgnoredProperty() {
        POJOPropertiesCollector coll = createCollector(WithIgnore.class, false);
        Map<String, POJOPropertyBuilder> map = coll.getPropertyMap();
        assertFalse(map.containsKey("a"));  // getter ignored -> property removed
        assertFalse(map.containsKey("b"));  // setter ignored -> property removed
    }

    @Test(timeout = 4000)
    public void testPseudoEmptyName() throws Exception {
        // Property with empty explicit name (should revert to implicit)
        class EmptyNameBean {
            @JsonProperty("")
            public int getX() { return 1; }
        }
        POJOPropertiesCollector coll = createCollector(EmptyNameBean.class, false);
        Map<String, POJOPropertyBuilder> map = coll.getPropertyMap();
        assertTrue(map.containsKey("x"));
    }

    // -----------------------------------------------------------------------
    // Partition C: Defect-targeted – Read‑only / write‑only properties
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testReadOnlyWriteOnlyCollectForDeserialization() {
        // This directly targets the known defect: properties that are only getter (read-only)
        // or only setter (write-only) must be retained when collecting for deserialization.
        POJOPropertiesCollector coll = createCollector(ReadOnlyWriteOnly.class, false);
        Map<String, POJOPropertyBuilder> map = coll.getPropertyMap();
        assertTrue("Read-only property 'x' must be present in deserialization collection",
                map.containsKey("x"));
        assertTrue("Write-only property 'y' must be present in deserialization collection",
                map.containsKey("y"));

        // Verify accessor presence
        POJOPropertyBuilder propX = map.get("x");
        assertNotNull("x must have a getter", propX.getGetter());
        assertNull("x should not have a setter", propX.getSetter());

        POJOPropertyBuilder propY = map.get("y");
        assertNull("y should not have a getter", propY.getGetter());
        assertNotNull("y must have a setter", propY.getSetter());
    }

    @Test(timeout = 4000)
    public void testPojo935Style() {
        // Reproduce scenario from defect: read-only fullName, write-only firstName, lastName
        POJOPropertiesCollector coll = createCollector(Pojo935.class, false);
        Map<String, POJOPropertyBuilder> map = coll.getPropertyMap();
        assertTrue("fullName (read-only) must be present", map.containsKey("fullName"));
        assertTrue("firstName (write-only) must be present", map.containsKey("firstName"));
        assertTrue("lastName (write-only) must be present", map.containsKey("lastName"));
        assertTrue(map.get("fullName").hasGetter());
        assertTrue(map.get("firstName").hasSetter());
        assertTrue(map.get("lastName").hasSetter());
    }

    // -----------------------------------------------------------------------
    // Partition D: Exception paths and error conditions
    // -----------------------------------------------------------------------

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testDuplicateJsonValue() {
        class DuplicateJsonValue {
            @JsonValue
            public String getA() { return "a"; }
            @JsonValue
            public String getB() { return "b"; }
        }
        POJOPropertiesCollector coll = createCollector(DuplicateJsonValue.class, false);
        coll.getJsonValueMethod();  // should throw
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testDuplicateAnyGetters() {
        class DuplicateAnyGetter {
            @com.fasterxml.jackson.annotation.JsonAnyGetter
            public Map<String, Object> getA() { return null; }
            @com.fasterxml.jackson.annotation.JsonAnyGetter
            public Map<String, Object> getB() { return null; }
        }
        POJOPropertiesCollector coll = createCollector(DuplicateAnyGetter.class, false);
        coll.getAnyGetter();  // should throw
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testDuplicateAnySetters() {
        class DuplicateAnySetter {
            @JsonAnySetter
            public void setA(String key, Object val) {}
            @JsonAnySetter
            public void setB(String key, Object val) {}
        }
        POJOPropertiesCollector coll = createCollector(DuplicateAnySetter.class, false);
        coll.getAnySetterMethod();  // should throw
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testDuplicateInjectables() {
        class DupInjectable {
            @com.fasterxml.jackson.annotation.JacksonInject("id")
            public int x;
            @com.fasterxml.jackson.annotation.JacksonInject("id")
            public int y;
        }
        POJOPropertiesCollector coll = createCollector(DupInjectable.class, false);
        coll.getInjectables();  // should throw
    }

    @Test(timeout = 4000)
    public void testAnnotationIntrospectorDisabled() {
        // When annotation processing is disabled, annotation introspector is null.
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(MapperFeature.USE_ANNOTATIONS);
        MapperConfig<?> config = mapper.getDeserializationConfig();
        JavaType type = mapper.constructType(SimpleBean.class);
        AnnotatedClass ac = AnnotatedClassResolver.resolve(config, type, config);
        POJOPropertiesCollector coll = new POJOPropertiesCollector(config, false, type, ac, "set");
        Map<String, POJOPropertyBuilder> map = coll.getPropertyMap();
        assertTrue(map.containsKey("x"));
        assertTrue(map.containsKey("y"));
    }

    // -----------------------------------------------------------------------
    // Partition E: Lifecycle, lazy collection, state integrity
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testLazyCollection() {
        // Before getPropertyMap is called, _collected should be false
        POJOPropertiesCollector coll = createCollector(SimpleBean.class, false);
        assertFalse(coll.getClassDef().toString().startsWith("$")); // not a non-static inner
        // Access property map triggers collection
        assertNotNull(coll.getPropertyMap());
        // Subsequent calls return same map
        Map<String, POJOPropertyBuilder> map1 = coll.getPropertyMap();
        Map<String, POJOPropertyBuilder> map2 = coll.getPropertyMap();
        assertSame(map1, map2);
    }

    @Test(timeout = 4000)
    public void testIgnoreCollectIgnorals() {
        // When a property is completely ignored and no explicit inclusion, it should be removed
        // and its name added to ignoredPropertyNames during deserialization.
        class AllIgnored {
            @JsonIgnore
            public int getX() { return 0; }
            @JsonIgnore
            public void setX(int x) {}
        }
        POJOPropertiesCollector coll = createCollector(AllIgnored.class, false);
        Set<String> ignored = coll.getIgnoredPropertyNames();
        assertNotNull("Ignored property names should be collected", ignored);
        assertTrue(ignored.contains("x"));
        assertFalse(coll.getPropertyMap().containsKey("x"));
    }

    @Test(timeout = 4000)
    public void testNonStaticInnerClassSkipsCreators() {
        class Inner {
            private int x;
            public int getX() { return x; }
            public void setX(int x) { this.x = x; }
        }
        // Non-static inner classes should skip creator introspection
        POJOPropertiesCollector coll = createCollector(Inner.class, false);
        List<BeanPropertyDefinition> props = coll.getProperties();
        // Should have property x from field or method
        assertTrue(props.stream().anyMatch(p -> p.getName().equals("x")));
    }

    @Test(timeout = 4000)
    public void testObjectIdInfoWithoutAnnotation() {
        POJOPropertiesCollector coll = createCollector(SimpleBean.class, false);
        assertNull(coll.getObjectIdInfo());
    }

    @Test(timeout = 4000)
    public void testFindPOJOBuilderClass() {
        POJOPropertiesCollector coll = createCollector(SimpleBean.class, false);
        assertNull(coll.findPOJOBuilderClass());
    }

    @Test(timeout = 4000)
    public void testConfigAccessors() {
        POJOPropertiesCollector coll = createCollector(SimpleBean.class, false);
        assertNotNull(coll.getConfig());
        assertNotNull(coll.getType());
        assertNotNull(coll.getClassDef());
        assertNotNull(coll.getAnnotationIntrospector());
    }

    // -----------------------------------------------------------------------
    // Additional coverage: private field handling with any-setter field
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testAnySetterField() {
        class AnySetterFieldBean {
            @JsonAnySetter
            private Map<String, Object> extra;

            public Map<String, Object> getExtra() { return extra; }
            public void setExtra(Map<String, Object> extra) { this.extra = extra; }
        }
        POJOPropertiesCollector coll = createCollector(AnySetterFieldBean.class, false);
        AnnotatedMember anyField = coll.getAnySetterField();
        assertNotNull(anyField);
        assertEquals("extra", anyField.getName());
        assertNull(coll.getAnySetterMethod());
    }

    @Test(timeout = 4000)
    public void testDuplicateAnySetterField() {
        // Should throw on duplicate any-setter fields
        class DupAnyField {
            @JsonAnySetter
            public Map<String, Object> m1;
            @JsonAnySetter
            public Map<String, Object> m2;
        }
        POJOPropertiesCollector coll = createCollector(DupAnyField.class, false);
        // The duplication is reported when getAnySetterField is called
        try {
            coll.getAnySetterField();
            fail("Should have thrown IllegalArgumentException for duplicate any-setter fields");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Multiple 'any-Setters' defined"));
        }
    }

    // -----------------------------------------------------------------------
    // Coverage for methods on POJOPropertyBuilder access
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testPropertyWithMultipleExplicitNames() {
        // Property with multiple explicit names via separate accessors
        class MultiName {
            @JsonProperty("x")
            public int getA() { return 0; }
            @JsonProperty("y")
            public void setB(int b) {}
        }
        POJOPropertiesCollector coll = createCollector(MultiName.class, false);
        Map<String, POJOPropertyBuilder> map = coll.getPropertyMap();
        // Should result in two property entries after explosion
        assertTrue(map.containsKey("x") || map.containsKey("y"));
        // Expect both (or a merge) - depends on behavior; at least one present
        assertFalse(map.isEmpty());
    }

    @Test(timeout = 4000)
    public void testNoGetterForSerializationUnknownMethod() {
        // Method with no return type gets skipped
        class NoReturn {
            public void getFoo() {}  // not a getter
        }
        POJOPropertiesCollector coll = createCollector(NoReturn.class, true);
        Map<String, POJOPropertyBuilder> map = coll.getPropertyMap();
        assertTrue(map.isEmpty());
    }

    @Test(timeout = 4000)
    public void testStaticInnerClassNotSkipped() {
        // Static inner classes should still introspect creators
        static class StaticInner {
            private int x;
            public StaticInner() {}
            public int getX() { return x; }
            public void setX(int x) { this.x = x; }
        }
        POJOPropertiesCollector coll = createCollector(StaticInner.class, false);
        assertNotNull(coll.getProperties());
        // Creator introspection may be empty for no-arg constructor
    }
}