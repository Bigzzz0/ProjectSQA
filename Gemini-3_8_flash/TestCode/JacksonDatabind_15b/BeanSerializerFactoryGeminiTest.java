/* [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------------
 * Target Class: com.fasterxml.jackson.databind.ser.BeanSerializerFactory
 * Target Defects: [databind#731] Converter to java.lang.Object fails with empty bean exception
 *
 * Major Decision Points & Branches Covered:
 * 1. withConfig(SerializerFactoryConfig):
 *    - Same config instance -> returns this
 *    - Subclass of BeanSerializerFactory not overriding withConfig -> throws IllegalStateException
 *    - Standard instance -> returns new BeanSerializerFactory(config)
 * 2. createSerializer(SerializerProvider, JavaType):
 *    - Explicit class-level serializer via @JsonSerialize(using=...) -> fast return
 *    - Type modification via annotation (modified vs original type, raw class match vs mismatch)
 *    - Converter handling:
 *      * null converter -> simple serializer construction
 *      * converter outputting typed value -> new BeanDesc introspection, annotation check
 *      * [databind#731]: converter nominally outputting java.lang.Object -> skip static Object serializer
 * 3. _createSerializer2(SerializerProvider, JavaType, BeanDescription, boolean):
 *    - Annotated serializer (@JsonValue, JsonSerializable)
 *    - Container types (List, Map, Array) vs Non-container POJO types
 *    - Custom module-provided serializers (customSerializers())
 *    - Primary types, Lookup types, Add-on types, Unknown type fallback
 *    - SerializerModifier post-processing
 * 4. constructBeanSerializer & findBeanProperties:
 *    - Plain Object.class check -> UnknownTypeSerializer
 *    - Empty properties: with known class annotations -> dummy serializer; without -> null
 *    - Property filtering via @JsonIgnoreProperties
 *    - Ignorable type handling via @JsonIgnoreType
 *    - Setterless getters suppression (REQUIRE_SETTERS_FOR_GETTERS)
 *    - TypeId property handling (@JsonTypeId)
 *    - BackReference property suppression (@JsonBackReference)
 *    - AnyGetter handling (@JsonAnyGetter) with default vs custom serializer
 *    - View processing (DEFAULT_VIEW_INCLUSION on/off, with/without @JsonView)
 *    - ObjectId handling:
 *      * null ObjectIdInfo
 *      * PropertyGenerator (property at idx 0, reordering from idx > 0, missing property failure)
 *      * Non-property ObjectIdGenerator (IntSequenceGenerator)
 *    - Polymorphic type serializers (findPropertyTypeSerializer, findPropertyContentTypeSerializer)
 */

package com.fasterxml.jackson.databind.ser;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.*;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonTypeId;
import com.fasterxml.jackson.databind.cfg.SerializerFactoryConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.StdConverter;

public class BeanSerializerFactoryGeminiTest {

    // =========================================================================
    // Test Helpers and Mocking Classes
    // =========================================================================

    public static class CustomSubclassFactory extends BeanSerializerFactory {
        public CustomSubclassFactory(SerializerFactoryConfig config) {
            super(config);
        }
    }

    public static class CustomClassSerializer extends StdSerializer<AnnotatedCustomClass> {
        public CustomClassSerializer() {
            super(AnnotatedCustomClass.class);
        }
        @Override
        public void serialize(AnnotatedCustomClass value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeString("custom_class_serialized");
        }
    }

    @JsonSerialize(using = CustomClassSerializer.class)
    public static class AnnotatedCustomClass {
        public int id = 100;
    }

    public static class SimpleValueBean {
        public String value;
        public SimpleValueBean(String value) { this.value = value; }
    }

    public static class CustomValueSerializer extends StdSerializer<SimpleValueBean> {
        public CustomValueSerializer() { super(SimpleValueBean.class); }
        @Override
        public void serialize(SimpleValueBean val, JsonGenerator gen, SerializerProvider prov) throws IOException {
            gen.writeString("MOD:" + val.value);
        }
    }

    public static class EmptyBeanWithoutAnnotations {}

    @JsonRootName("annotated_dummy")
    public static class EmptyBeanWithAnnotations {}

    @JsonIgnoreType
    public static class IgnoredType {
        public String secret = "hidden";
    }

    public static class BeanWithIgnoredType {
        public String name = "visible";
        public IgnoredType ignored = new IgnoredType();
    }

    public static class SetterlessBean {
        private String _name = "test";
        private String _explicit = "explicit";

        public String getName() { return _name; }
        @JsonProperty("explicit")
        public String getExplicit() { return _explicit; }
    }

    public static class ParentBackRef {
        public String title = "parent";
        public ChildBackRef child;
    }

    public static class ChildBackRef {
        public String childName = "child";
        @JsonBackReference
        public ParentBackRef parent;
    }

    public static class TypeIdBean {
        @JsonTypeId
        public String customType = "specialType";
        public int data = 42;
    }

    @JsonIgnoreProperties({"hidden1", "hidden2"})
    public static class FilteredPropBean {
        public String visible = "ok";
        public String hidden1 = "bad1";
        public String hidden2 = "bad2";
    }

    public static class AnyGetterBean {
        public String normal = "val";
        private Map<String, Object> extra = new LinkedHashMap<String, Object>();

        public AnyGetterBean() {
            extra.put("extraKey", "extraValue");
        }

        @JsonAnyGetter
        public Map<String, Object> any() {
            return extra;
        }
    }

    public static class CustomAnySerializer extends StdSerializer<Map<String, Object>> {
        @SuppressWarnings("unchecked")
        public CustomAnySerializer() { super((Class<Map<String, Object>>)(Class<?>)Map.class); }
        @Override
        public void serialize(Map<String, Object> val, JsonGenerator gen, SerializerProvider prov) throws IOException {
            gen.writeStartObject();
            gen.writeStringField("customAny", "anyHandled");
            gen.writeEndObject();
        }
    }

    public static class AnyGetterCustomSerializerBean {
        @JsonAnyGetter
        @JsonSerialize(using = CustomAnySerializer.class)
        public Map<String, Object> any() {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("a", "b");
            return map;
        }
    }

    public static class Views {
        public static class Public {}
        public static class Internal extends Public {}
    }

    public static class ViewBean {
        @JsonView(Views.Public.class)
        public String pub = "publicValue";

        @JsonView(Views.Internal.class)
        public String priv = "internalValue";

        public String undoc = "undocumented";
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    public static class IdReorderBean {
        public String first = "firstProp";
        public int id = 999;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "missingId")
    public static class InvalidIdBean {
        public int id = 123;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@seq")
    public static class SequenceIdBean {
        public String name = "seqName";
    }

    public static class PolymorphicContainerBean {
        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
        public Object polyItem = new SimpleValueBean("contained");

        public List<@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY) Object> polyList =
                Collections.singletonList((Object) new SimpleValueBean("listContained"));
    }

    // =========================================================================
    // PARTITION A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testSingletonAndConfigurationTransitions() {
        BeanSerializerFactory factory = BeanSerializerFactory.instance;
        assertNotNull(factory);

        SerializerFactoryConfig config = new SerializerFactoryConfig();
        SerializerFactory withSameConfig = factory.withConfig(factory.getFactoryConfig());
        assertSame("Same config should return same factory instance", factory, withSameConfig);

        SerializerFactory withNewConfig = factory.withConfig(config);
        assertNotSame("New config should return new factory instance", factory, withNewConfig);
        assertTrue(withNewConfig instanceof BeanSerializerFactory);
    }

    @Test(timeout = 4000)
    public void testExplicitSerializerAnnotationOnClass() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(new AnnotatedCustomClass());
        assertEquals("\"custom_class_serialized\"", json);
    }

    @Test(timeout = 4000)
    public void testCustomSerializersRegistrationViaModule() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(SimpleValueBean.class, new CustomValueSerializer());
        mapper.registerModule(module);

        String json = mapper.writeValueAsString(new SimpleValueBean("testData"));
        assertEquals("\"MOD:testData\"", json);
    }

    @Test(timeout = 4000)
    public void testPropertyFilterAnnotation() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(new FilteredPropBean());
        assertTrue(json.contains("\"visible\":\"ok\""));
        assertFalse(json.contains("hidden1"));
        assertFalse(json.contains("hidden2"));
    }

    @Test(timeout = 4000)
    public void testIgnorableTypeAnnotation() throws Exception {
        ObjectMapper mapper = new ObjectMapper