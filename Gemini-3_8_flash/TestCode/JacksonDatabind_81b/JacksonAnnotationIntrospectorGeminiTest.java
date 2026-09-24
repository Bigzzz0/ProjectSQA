package com.fasterxml.jackson.databind.introspect;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.*;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.VirtualBeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.std.RawSerializer;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import com.fasterxml.jackson.databind.util.NameTransformer;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: JacksonAnnotationIntrospector
 *
 * Targeted Decision Branches & Boundary Conditions:
 * 1. Defect-Targeted [databind#1592]:
 *    - refineSerializationType() with primitive-to-wrapper and wrapper-to-primitive conversions
 *      (e.g., int.class -> Integer.class, Integer.class -> int.class).
 *      Branch under test: serClass.isAssignableFrom(currRaw) vs currRaw.isAssignableFrom(serClass).
 *      Prior to fix, primitive<->wrapper threw JsonMappingException ("types not related").
 * 2. Annotation Bundling & Caching (isAnnotationBundle):
 *    - @JacksonAnnotationsInside meta-annotation present vs absent.
 *    - LRUMap cache hit vs cache miss.
 * 3. Enum Introspection:
 *    - findEnumValue(): normal enum, @JsonProperty with value, empty value, missing field fallback.
 *    - findEnumValues(): non-enum fields ignored, empty name ignored, mapped enum names.
 *    - findDefaultEnumValue(): @JsonEnumDefaultValue present vs absent.
 * 4. General Class Annotations:
 *    - findRootName(): with namespace, empty namespace (converted to null), no annotation.
 *    - findPropertyIgnorals(): empty vs populated.
 *    - isIgnorableType(): true/false/null.
 *    - findFilterId(): non-empty id vs empty string.
 *    - findNamingStrategy(), findClassDescription(), findAutoDetectVisibility().
 * 5. Member Annotations & Property Detection:
 *    - findPropertyAliases(), hasRequiredMarker(), findPropertyAccess(), findPropertyDescription(),
 *      findPropertyIndex(), findPropertyDefaultValue(), findFormat(), findReferenceType().
 *    - findUnwrappingNameTransformer(): disabled vs enabled with prefix/suffix.
 *    - findInjectableValue(): setter vs getter vs field, explicit id vs implicit type name.
 *    - findViews(): present vs absent.
 *    - resolveSetterConflict(): primitive vs non-primitive, String vs non-String.
 * 6. Polymorphic Type Handling:
 *    - findTypeResolver(), findPropertyTypeResolver() on container/reference types (returns null),
 *      findPropertyContentTypeResolver() validation (exception when not container).
 *    - findSubtypes(), findTypeName(), isTypeId().
 * 7. Object Id Handling:
 *    - findObjectIdInfo(): generator None vs active generator.
 *    - findObjectReferenceInfo(): with null vs non-null base info.
 * 8. Serialization & Deserialization Refinements:
 *    - findSerializer(): explicit serializer vs @JsonRawValue.
 *    - findPropertyInclusion(): @JsonInclude and deprecated @JsonSerialize(include=...).
 *    - refineSerializationType() and refineDeserializationType(): base type, key type (MapLike),
 *      and content type refinements; type specialization vs generalization.
 * 9. Virtual Properties & Custom Builders:
 *    - findAndAddVirtualProperties(): JsonAppend attrs & props, prepend flag.
 *    - findPOJOBuilder(), findPOJOBuilderConfig(), findValueInstantiator().
 * 10. Lifecycle & Serialization:
 *     - version() check, readResolve() cache regeneration after Java serialization.
 */
public class JacksonAnnotationIntrospectorGeminiTest {

    private final JacksonAnnotationIntrospector _ai = new JacksonAnnotationIntrospector();
    private final ObjectMapper _mapper = new ObjectMapper();

    // =========================================================================
    // Test Dummy Fixtures & Annotations
    // =========================================================================

    @JacksonAnnotationsInside
    @Retention(RetentionPolicy.RUNTIME)
    @interface DummyBundle { }

    @Retention(RetentionPolicy.RUNTIME)
    @interface NotABundle { }

    enum DummyEnum {
        @JsonProperty("first_value")
        FIRST,
        @JsonProperty("")
        EMPTY_PROP,
        @JsonEnumDefaultValue
        DEFAULT_VAL,
        PLAIN;

        public static final String NOT_AN_ENUM_CONSTANT = "ignore_me";
    }

    @JsonRootName(value = "root", namespace = "http://example.com")
    static class RootWithNamespace { }

    @JsonRootName(value = "rootEmptyNs", namespace = "")
    static class RootWithEmptyNamespace { }

    static class NoRoot { }

    @JsonIgnoreProperties(value = {"p1", "p2"}, allowGetters = true)
    static class IgnoralBean { }

    @JsonIgnoreType(true)
    static class IgnoredType { }

    @JsonFilter("testFilter")
    static class FilteredBean { }

    @JsonFilter("")
    static class EmptyFilteredBean { }

    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    @JsonClassDescription("A test class description")
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    static class ClassLevelMetaBean { }

    static class MemberBean {
        @JsonAlias({"a1", "a2"})
        @JsonProperty(value = "prop", index = 3, defaultValue = "defVal", access = JsonProperty.Access.READ_ONLY, required = true)
        @JsonPropertyDescription("A description")
        public String field;

        @JsonAlias({})
        public String emptyAlias;

        @JsonManagedReference("ref")
        public MemberBean managed;

        @JsonBackReference("ref")
        public MemberBean back;

        @JsonUnwrapped(prefix = "pre_", suffix = "_post")
        public MemberBean unwrapped;

        @JsonUnwrapped(enabled = false)
        public MemberBean unwrappedDisabled;

        @JacksonInject(value = "injectedField")
        public String injected;

        @JacksonInject
        public Long implicitIdField;

        @JsonView({String.class, Integer.class})
        public int viewedField;

        @JacksonInject
        public Double getInjectedGetter() { return 1.0; }

        @JacksonInject
        public void setInjectedSetter(Boolean value) { }

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        public Date formattedDate;

        @JsonTypeId
        public String typeIdField;
    }

    static class SetterConflictBean {
        public void setA(int v) { }
        public void setA(Integer v) { }

        public void setB(String v) { }
        public void setB(Object v) { }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@type", defaultImpl = DummySubclass.class, visible = true)
    @JsonSubTypes({
        @JsonSubTypes.Type(value = DummySubclass.class, name = "sub")
    })
    @JsonTypeName("base")
    static class DummyPolyBase { }

    static class DummySubclass extends DummyPolyBase { }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
    static class NoTypeInfoBean { }

    static class CustomTypeIdRes implements TypeIdResolver {
        public void init(JavaType baseType) { }
        public String idFromValue(Object value) { return "id"; }
        public String idFromValueAndType(Object value, Class<?> suggestedType) { return "id"; }
        public String idFromBaseType() { return "id"; }
        public JavaType typeFromId(DatabindContext context, String id) { return null; }
        public String getDescForKnownTypeIds() { return null; }
        public JsonTypeInfo.Id getMechanism() { return JsonTypeInfo.Id.CUSTOM; }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.EXTERNAL_PROPERTY)
    @JsonTypeIdResolver(CustomTypeIdRes.class)
    static class CustomTypeInfoBean { }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = MemberBean.class)
    @JsonIdentityReference(alwaysAsId = true)
    static class IdentityBean {
        public int id;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.None.class)
    static class NoneIdentityBean { }

    static class CustomSerializer extends StdScalarSerializer<String> {
        public CustomSerializer() { super(String.class); }
        @Override
        public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, SerializerProvider provider) { }
    }

    static class CustomDeserializer extends JsonDeserializer<String> {
        @Override
        public String deserialize(com.fasterxml.jackson.core.JsonParser p, DeserializationContext ctxt) { return null; }
    }

    static class CustomKeyDeserializer extends KeyDeserializer {
        @Override
        public Object deserializeKey(String key, DeserializationContext ctxt) { return key; }
    }

    static class SerializerBean {
        @JsonSerialize(using = CustomSerializer.class, keyUsing = CustomSerializer.class,
                contentUsing = CustomSerializer.class, nullsUsing = CustomSerializer.class,
                typing = JsonSerialize.Typing.STATIC)
        public Map<String, String> map;

        @JsonRawValue
        public String raw;

        @JsonRawValue(false)
        public String notRaw;

        @JsonGetter("customGetter")
        public String getCustom() { return "x"; }

        @JsonValue
        public int asValue() { return 1; }

        @JsonAnyGetter
        public Map<String, Object> any() { return Collections.emptyMap(); }
    }

    static class DeserializerBean {
        @JsonDeserialize(using = CustomDeserializer.class, keyUsing = CustomKeyDeserializer.class,
                contentUsing = CustomDeserializer.class)
        public Map<String, String> map;

        @JsonSetter("customSetter")
        public void setCustom(String s) { }

        @JsonAnySetter
        public void setAny(String k, Object v) { }

        @JsonMerge
        public List<String> list;
    }

    static class CreatorBean {
        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public CreatorBean(String val) { }

        @JsonCreator(mode = JsonCreator.Mode.DISABLED)
        public CreatorBean(int val) { }
    }

    @JsonPropertyOrder(value = {"b", "a"}, alphabetic = true)
    static class OrderBean {
        public int b;
        public int a;
    }

    @JsonAppend(
        attrs = {
            @JsonAppend.Attr(value = "attr1", propName = "prop1", required = true)
        },
        props = {
            @JsonAppend.Prop(value = DummyVirtualPropWriter.class, name = "vProp")
        },
        prepend = true
    )
    static class VirtualPropsBean { }

    public static class DummyVirtualPropWriter extends VirtualBeanPropertyWriter {
        public DummyVirtualPropWriter() { super(); }
        public DummyVirtualPropWriter(BeanPropertyDefinition propDef, com.fasterxml.jackson.databind.util.Annotations contextAnnotations, JavaType declaredType) {
            super(propDef, contextAnnotations, declaredType);
        }
        @Override
        protected Object value(Object bean, com.fasterxml.jackson.core.JsonGenerator gen, SerializerProvider prov) {
            return "virt";
        }
        @Override
        public VirtualBeanPropertyWriter withConfig(MapperConfig<?> config, AnnotatedClass declaringClass, BeanPropertyDefinition propDef, JavaType type) {
            return new DummyVirtualPropWriter(propDef, declaringClass.getAnnotations(), type);
        }
    }

    @JsonValueInstantiator(Object.class)
    @JsonDeserialize(builder = BuilderBean.class)
    @JsonPOJOBuilder(buildMethodName = "construct", withPrefix = "with")
    static class TargetWithBuilder { }

    static class BuilderBean {
        public TargetWithBuilder construct() { return new TargetWithBuilder(); }
    }

    @SuppressWarnings("deprecation")
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    static class DeprecatedInclusionBean { }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    static class ModernInclusionBean { }

    static class Defect1592PrimitiveToWrapperBean {
        @JsonSerialize(as = Integer.class)
        public int primitiveInt;
    }

    static class Defect1592WrapperToPrimitiveBean {
        @JsonSerialize(as = int.class)
        public Integer wrapperInt;
    }

    static class TypeHierarchyBase { }
    static class TypeHierarchySub extends TypeHierarchyBase { }

    static class RefineBean {
        @JsonSerialize(as = TypeHierarchyBase.class, keyAs = String.class, contentAs = TypeHierarchyBase.class)
        public Map<CharSequence, TypeHierarchySub> generalizedMap;

        @JsonSerialize(as = TypeHierarchySub.class)
        public TypeHierarchyBase specialized;

        @JsonDeserialize(as = TypeHierarchySub.class, keyAs = String.class, contentAs = TypeHierarchySub.class)
        public Map<CharSequence, TypeHierarchyBase> deserSpecializedMap;
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (databind-1592)
    // =========================================================================

    /**
     * Targets Jackson Databind defect #1592:
     * Coercion / refinement between primitive and wrapper types (e.g., int.class <-> Integer.class)
     * must not fail with JsonMappingException: "types not related".
     */
    @Test(timeout = 4000)
    public void testDefect1592PrimitiveToWrapperSerializationRefinement() throws Exception {
        SerializationConfig config = _mapper.getSerializationConfig();
        JavaType beanType = _mapper.constructType(Defect1592PrimitiveToWrapperBean.class);
        BeanDescription desc = config.introspect(beanType);
        AnnotatedMember member = null;
        for (BeanPropertyDefinition prop : desc.findProperties()) {
            if ("primitiveInt".equals(prop.getName())) {
                member = prop.getPrimaryMember();
                break;
            }
        }
        assertNotNull("Should find property member for primitiveInt", member);

        JavaType primitiveType = _mapper.getTypeFactory().constructType(int.class);
        // On defective versions, this throws JsonMappingException:
        // "Can not refine serialization type [simple type, class int] into java.lang.Integer; types not related"
        JavaType refined = _ai.refineSerializationType(config, member, primitiveType);
        assertNotNull("Refined type must not be null", refined);
        assertTrue("Refined type should be compatible with int or Integer",
                refined.hasRawClass(int.class) || refined.hasRawClass(Integer.class));
    }

    @Test(timeout = 4000)
    public void testDefect1592WrapperToPrimitiveSerializationRefinement() throws Exception {
        SerializationConfig config = _mapper.getSerializationConfig();
        JavaType beanType = _mapper.constructType(Defect1592WrapperToPrimitiveBean.class);
        BeanDescription desc = config.introspect(beanType);
        AnnotatedMember member = null;
        for (BeanPropertyDefinition prop : desc.findProperties()) {
            if ("wrapperInt".equals(prop.getName())) {
                member = prop.getPrimaryMember();
                break;
            }
        }
        assertNotNull("Should find property member for wrapperInt", member);

        JavaType wrapperType = _mapper.getTypeFactory().constructType(Integer.class);
        JavaType refined = _ai.refineSerializationType(config, member, wrapperType);
        assertNotNull("Refined type must not be null", refined);
        assertTrue("Refined type should be compatible with int or Integer",
                refined.hasRawClass(int.class) || refined.hasRawClass(Integer.class));
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testVersionAndConfiguration() {
        Version v = _ai.version();
        assertNotNull(v);
        assertFalse(v.isUnknownVersion());

        JacksonAnnotationIntrospector fluent = _ai.setConstructorPropertiesImpliesCreator(false);
        assertSame("Should return self for chaining", _ai, fluent);
        _ai.setConstructorPropertiesImpliesCreator(true);
    }

    @Test(timeout = 4000)
    public void testIsAnnotationBundle() {
        Annotation bundleAnn = DummyBundle.class.getAnnotations()[0];
        Annotation notBundleAnn = MemberBean.class.getAnnotations().length > 0
                ? MemberBean.class.getAnnotations()[0]
                : DummyBundle.class.getAnnotation(JacksonAnnotationsInside.class);

        // Bundle annotation
        assertTrue(_ai.isAnnotationBundle(new DummyBundle() {
            public Class<? extends Annotation> annotationType() { return DummyBundle.class; }
        }));
        // Cache hit branch
        assertTrue(_ai.isAnnotationBundle(new DummyBundle() {
            public Class<? extends Annotation> annotationType() { return DummyBundle.class; }
        }));

        // Not a bundle
        assertFalse(_ai.isAnnotationBundle(new NotABundle() {
            public Class<? extends Annotation> annotationType() { return NotABundle.class; }
        }));
    }

    @Test(timeout = 4000)
    public void testEnumIntrospection() {
        @SuppressWarnings("deprecation")
        String nameFirst = _ai.findEnumValue(DummyEnum.FIRST);
        assertEquals("first_value", nameFirst);

        @SuppressWarnings("deprecation")
        String nameEmpty = _ai.findEnumValue(DummyEnum.EMPTY_PROP);
        assertEquals("EMPTY_PROP", nameEmpty);

        @SuppressWarnings("deprecation")
        String namePlain = _ai.findEnumValue(DummyEnum.PLAIN);
        assertEquals("PLAIN", namePlain);

        DummyEnum[] values = DummyEnum.values();
        String[] names = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            names[i] = values[i].name();
        }
        String[] resolved = _ai.findEnumValues(DummyEnum.class, values, names);
        assertEquals("first_value", resolved[0]);
        assertEquals("EMPTY_PROP", resolved[1]);

        Enum<?> defaultEnum = _ai.findDefaultEnumValue(DummyEnum.class);
        assertEquals(DummyEnum.DEFAULT_VAL, defaultEnum);
    }

    @Test(timeout = 4000)
    public void testClassAnnotationsRootAndIgnorals() {
        AnnotatedClass acRoot = _constructAnnotatedClass(RootWithNamespace.class);
        PropertyName rootName = _ai.findRootName(acRoot);
        assertNotNull(rootName);
        assertEquals("root", rootName.getSimpleName());
        assertEquals("http://example.com", rootName.getNamespace());

        AnnotatedClass acEmptyNs = _constructAnnotatedClass(RootWithEmptyNamespace.class);
        PropertyName emptyNsName = _ai.findRootName(acEmptyNs);
        assertNotNull(emptyNsName);
        assertNull("Empty namespace should normalize to null", emptyNsName.getNamespace());

        AnnotatedClass acNoRoot = _constructAnnotatedClass(NoRoot.class);
        assertNull(_ai.findRootName(acNoRoot));

        AnnotatedClass acIgnoral = _constructAnnotatedClass(IgnoralBean.class);
        JsonIgnoreProperties.Value ignorals = _ai.findPropertyIgnorals(acIgnoral);
        assertTrue(ignorals.getIgnored().contains("p1"));
        assertTrue(ignorals.getIgnored().contains("p2"));
        assertTrue(ignorals.getAllowGetters());

        AnnotatedClass acIgnoredType = _constructAnnotatedClass(IgnoredType.class);
        assertEquals(Boolean.TRUE, _ai.isIgnorableType(acIgnoredType));
        assertNull(_ai.isIgnorableType(acNoRoot));
    }

    @Test(timeout = 4000)
    public void testClassAnnotationsFiltersAndNaming() {
        AnnotatedClass acFilter = _constructAnnotatedClass(FilteredBean.class);
        assertEquals("testFilter", _ai.findFilterId(acFilter));

        AnnotatedClass acEmptyFilter = _constructAnnotatedClass(EmptyFilteredBean.class);
        assertNull("Empty filter id should be treated as null", _ai.findFilterId(acEmptyFilter));

        AnnotatedClass acMeta = _constructAnnotatedClass(ClassLevelMetaBean.class);
        assertEquals(PropertyNamingStrategy.SnakeCaseStrategy.class, _ai.findNamingStrategy(acMeta));
        assertEquals("A test class description", _ai.findClassDescription(acMeta));

        VisibilityChecker<?> checker = new VisibilityChecker.Std(JsonAutoDetect.Visibility.DEFAULT);
        VisibilityChecker<?> updated = _ai.findAutoDetectVisibility(acMeta, checker);
        assertNotSame(checker, updated);
        assertNull(_ai.findAutoDetectVisibility(acNoRoot, null));
    }

    @Test(timeout = 4000)
    public void testMemberPropertyDetails() throws Exception {
        AnnotatedClass ac = _constructAnnotatedClass(MemberBean.class);
        AnnotatedField field = _getField(ac, "field");

        assertEquals(Boolean.TRUE, _ai.hasRequiredMarker(field));
        assertEquals(JsonProperty.Access.READ_ONLY, _ai.findPropertyAccess(field));
        assertEquals("A description", _ai.findPropertyDescription(field));
        assertEquals(Integer.valueOf(3), _ai.findPropertyIndex(field));
        assertEquals("defVal", _ai.findPropertyDefaultValue(field));

        List<PropertyName> aliases = _ai.findPropertyAliases(field);
        assertNotNull(aliases);
        assertEquals(2, aliases.size());
        assertEquals("a1", aliases.get(0).getSimpleName());

        AnnotatedField emptyAliasField = _getField(ac, "emptyAlias");
        List<PropertyName> emptyAliases = _ai.findPropertyAliases(emptyAliasField);
        assertNotNull(emptyAliases);
        assertTrue(emptyAliases.isEmpty());

        AnnotatedField formattedDateField = _getField(ac, "formattedDate");
        JsonFormat.Value format = _ai.findFormat(formattedDateField);
        assertNotNull(format);
        assertEquals("yyyy-MM-dd", format.getPattern());
        assertEquals(JsonFormat.Shape.STRING, format.getShape());

        AnnotatedField typeIdField = _getField(ac, "typeIdField");
        assertEquals(Boolean.TRUE, _ai.isTypeId(typeIdField));
    }

    @Test(timeout = 4000)
    public void testReferencesAndUnwrapping() throws Exception {
        AnnotatedClass ac = _constructAnnotatedClass(MemberBean.class);
        AnnotatedField managedField = _getField(ac, "managed");
        AnnotationIntrospector.ReferenceProperty managedRef = _ai.findReferenceType(managedField);
        assertNotNull(managedRef);
        assertTrue(managedRef.isManagedReference());
        assertEquals("ref", managedRef.getName());

        AnnotatedField backField = _getField(ac, "back");
        AnnotationIntrospector.ReferenceProperty backRef = _ai.findReferenceType(backField);
        assertNotNull(backRef);
        assertTrue(backRef.isBackReference());
        assertEquals("ref", backRef.getName());

        AnnotatedField unwrappedField = _getField(ac, "unwrapped");
        NameTransformer transformer = _ai.findUnwrappingNameTransformer(unwrappedField);
        assertNotNull(transformer);
        assertEquals("pre_name_post", transformer.transform("name"));

        AnnotatedField disabledUnwrapped = _getField(ac, "unwrappedDisabled");
        assertNull(_ai.findUnwrappingNameTransformer(disabledUnwrapped));
    }

    @Test(timeout = 4000)
    public void testInjectableValue() throws Exception {
        AnnotatedClass ac = _constructAnnotatedClass(MemberBean.class);

        AnnotatedField field = _getField(ac, "injected");
        JacksonInject.Value val = _ai.findInjectableValue(field);
        assertNotNull(val);
        assertEquals("injectedField", val.getId());
        @SuppressWarnings("deprecation")
        Object id = _ai.findInjectableValueId(field);
        assertEquals("injectedField", id);

        AnnotatedField implicitField = _getField(ac, "implicitIdField");
        JacksonInject.Value implicitVal = _ai.findInjectableValue(implicitField);
        assertNotNull(implicitVal);
        assertEquals(Long.class.getName(), implicitVal.getId());

        AnnotatedMethod getter = _getMethod(ac, "getInjectedGetter");
        JacksonInject.Value getterVal = _ai.findInjectableValue(getter);
        assertNotNull(getterVal);
        assertEquals(Double.class.getName(), getterVal.getId());

        AnnotatedMethod setter = _getMethod(ac, "setInjectedSetter", Boolean.class);
        JacksonInject.Value setterVal = _ai.findInjectableValue(setter);
        assertNotNull(setterVal);
        assertEquals(Boolean.class.getName(), setterVal.getId());
    }

    @Test(timeout = 4000)
    public void testViews() throws Exception {
        AnnotatedClass ac = _constructAnnotatedClass(MemberBean.class);
        AnnotatedField field = _getField(ac, "viewedField");
        Class<?>[] views = _ai.findViews(field);
        assertNotNull(views);
        assertEquals(2, views.length);
        assertEquals(String.class, views[0]);
    }

    @Test(timeout = 4000)
    public void testResolveSetterConflict() throws Exception {
        AnnotatedClass ac = _constructAnnotatedClass(SetterConflictBean.class);
        AnnotatedMethod setterPrimitive = _getMethod(ac, "setA", int.class);
        AnnotatedMethod setterWrapper = _getMethod(ac, "setA", Integer.class);

        // Primitive preferred over non-primitive
        assertSame(setterPrimitive, _ai.resolveSetterConflict(null, setterPrimitive, setterWrapper));
        assertSame(setterPrimitive, _ai.resolveSetterConflict(null, setterWrapper, setterPrimitive));

        AnnotatedMethod setterStr = _getMethod(ac, "setB", String.class);
        AnnotatedMethod setterObj = _getMethod(ac, "setB", Object.class);

        // String preferred over non-String
        assertSame(setterStr, _ai.resolveSetterConflict(null, setterStr, setterObj));
        assertSame(setterStr, _ai.resolveSetterConflict(null, setterObj, setterStr));

        // When both are same priority
        assertNull(_ai.resolveSetterConflict(null, setterObj, setterObj));
    }

    @Test(timeout = 4000)
    public void testPolymorphicTypeHandling() {
        AnnotatedClass acPoly = _constructAnnotatedClass(DummyPolyBase.class);
        JavaType baseType = _mapper.constructType(DummyPolyBase.class);
        TypeResolverBuilder<?> b = _ai.findTypeResolver(_mapper.getSerializationConfig(), acPoly, baseType);
        assertNotNull(b);
        assertEquals(DummySubclass.class, b.getDefaultImpl());

        AnnotatedClass acNoType = _constructAnnotatedClass(NoTypeInfoBean.class);
        TypeResolverBuilder<?> bNo = _ai.findTypeResolver(_mapper.getSerializationConfig(), acNoType, _mapper.constructType(NoTypeInfoBean.class));
        assertNotNull(bNo);
        assertNull(bNo.getDefaultImpl());

        List<NamedType> subtypes = _ai.findSubtypes(acPoly);
        assertNotNull(subtypes);
        assertEquals(1, subtypes.size());
        assertEquals("sub", subtypes.get(0).getName());
        assertEquals(DummySubclass.class, subtypes.get(0).getType());

        assertEquals("base", _ai.findTypeName(acPoly));
    }

    @Test(timeout = 4000)
    public void testPropertyTypeResolverGuards() throws Exception {
        AnnotatedClass ac = _constructAnnotatedClass(MemberBean.class);
        AnnotatedField field = _getField(ac, "field");
        JavaType listType = _mapper.getTypeFactory().constructCollectionType(List.class, String.class);

        // Should return null for container types
        assertNull(_ai.findPropertyTypeResolver(_mapper.getSerializationConfig(), field, listType));

        // findPropertyContentTypeResolver requires a container
        TypeResolverBuilder<?> b = _ai.findPropertyContentTypeResolver(_mapper.getSerializationConfig(), field, listType);
        assertNull(b); // field does not have @JsonTypeInfo
    }

    @Test(timeout = 4000)
    public void testObjectIdHandling() {
        AnnotatedClass acId = _constructAnnotatedClass(IdentityBean.class);
        ObjectIdInfo info = _ai.findObjectIdInfo(acId);
        assertNotNull(info);
        assertEquals("id", info.getPropertyName().getSimpleName());
        assertEquals(ObjectIdGenerators.PropertyGenerator.class, info.getGeneratorType());

        ObjectIdInfo refInfo = _ai.findObjectReferenceInfo(acId, info);
        assertNotNull(refInfo);
        assertTrue(refInfo.getAlwaysAsId());

        AnnotatedClass acNone = _constructAnnotatedClass(NoneIdentityBean.class);
        assertNull(_ai.findObjectIdInfo(acNone));
    }

    @Test(timeout = 4000)
    public void testSerializationAnnotations() throws Exception {
        AnnotatedClass ac = _constructAnnotatedClass(SerializerBean.class);
        AnnotatedField mapField = _getField(ac, "map");

        assertEquals(CustomSerializer.class, _ai.findSerializer(mapField));
        assertEquals(CustomSerializer.class, _ai.findKeySerializer(mapField));
        assertEquals(CustomSerializer.class, _ai.findContentSerializer(mapField));
        assertEquals(CustomSerializer.class, _ai.findNullSerializer(mapField));
        assertEquals(JsonSerialize.Typing.STATIC, _ai.findSerializationTyping(mapField));

        AnnotatedField rawField = _getField(ac, "raw");
        Object rawSer = _ai.findSerializer(rawField);
        assertNotNull(rawSer);
        assertTrue(rawSer instanceof RawSerializer);

        AnnotatedField notRawField = _getField(ac, "notRaw");
        assertNull(_ai.findSerializer(notRawField));

        AnnotatedMethod getter = _getMethod(ac, "getCustom");
        assertEquals("customGetter", _ai.findNameForSerialization(getter).getSimpleName());

        AnnotatedMethod asValMethod = _getMethod(ac, "asValue");
        assertEquals(Boolean.TRUE, _ai.hasAsValue(asValMethod));
        @SuppressWarnings("deprecation")
        boolean hasValLegacy = _ai.hasAsValueAnnotation(asValMethod);
        assertTrue(hasValLegacy);

        AnnotatedMethod anyMethod = _getMethod(ac, "any");
        assertEquals(Boolean.TRUE, _ai.hasAnyGetter(anyMethod));
        @SuppressWarnings("deprecation")
        boolean hasAnyLegacy = _ai.hasAnyGetterAnnotation(anyMethod);
        assertTrue(hasAnyLegacy);
    }

    @Test(timeout = 4000)
    public void testDeserializationAnnotations() throws Exception {
        AnnotatedClass ac = _constructAnnotatedClass(DeserializerBean.class);
        AnnotatedField mapField = _getField(ac, "map");

        assertEquals(CustomDeserializer.class, _ai.findDeserializer(mapField));
        assertEquals(CustomKeyDeserializer.class, _ai.findKeyDeserializer(mapField));
        assertEquals(CustomDeserializer.class, _ai.findContentDeserializer(mapField));

        AnnotatedMethod setter = _getMethod(ac, "setCustom", String.class);
        assertEquals("customSetter", _ai.findNameForDeserialization(setter).getSimpleName());

        AnnotatedMethod anySetter = _getMethod(ac, "setAny", String.class, Object.class);
        assertEquals(Boolean.TRUE, _ai.hasAnySetter(anySetter));
        @SuppressWarnings("deprecation")
        boolean hasAnySetLegacy = _ai.hasAnySetterAnnotation(anySetter);
        assertTrue(hasAnySetLegacy);

        AnnotatedField listField = _getField(ac, "list");
        assertEquals(Boolean.TRUE, _ai.findMergeInfo(listField));
    }

    @Test(timeout = 4000)
    public void testCreatorAnnotations() throws Exception {
        AnnotatedClass ac = _constructAnnotatedClass(CreatorBean.class);
        AnnotatedConstructor ctorDelegating = null;
        AnnotatedConstructor ctorDisabled = null;

        for (AnnotatedConstructor ctor : ac.getConstructors()) {
            if (ctor.getParameterCount() == 1) {
                if (ctor.getRawParameterType(0) == String.class) {
                    ctorDelegating = ctor;
                } else if (ctor.getRawParameterType(0) == int.class) {
                    ctorDisabled = ctor;
                }
            }
        }
        assertNotNull(ctorDelegating);
        assertNotNull(ctorDisabled);

        assertEquals(JsonCreator.Mode.DELEGATING, _ai.findCreatorAnnotation(_mapper.getDeserializationConfig(), ctorDelegating));
        @SuppressWarnings("deprecation")
        boolean hasCreator = _ai.hasCreatorAnnotation(ctorDelegating);
        assertTrue(hasCreator);

        assertEquals(JsonCreator.Mode.DISABLED, _ai.findCreatorAnnotation(_mapper.getDeserializationConfig(), ctorDisabled));
        @SuppressWarnings("deprecation")
        boolean hasDisabledCreator = _ai.hasCreatorAnnotation(ctorDisabled);
        assertFalse(hasDisabledCreator);
    }

    @Test(timeout = 4000)
    public void testPropertyInclusion() {
        AnnotatedClass acModern = _constructAnnotatedClass(ModernInclusionBean.class);
        JsonInclude.Value incModern = _ai.findPropertyInclusion(acModern);
        assertEquals(JsonInclude.Include.NON_EMPTY, incModern.getValueInclusion());

        AnnotatedClass acDeprecated = _constructAnnotatedClass(DeprecatedInclusionBean.class);
        JsonInclude.Value incDeprecated = _ai.findPropertyInclusion(acDeprecated);
        assertEquals(JsonInclude.Include.NON_NULL, incDeprecated.getValueInclusion());
    }

    @Test(timeout = 4000)
    public void testSerializationOrderingAndVirtualProperties() {
        AnnotatedClass acOrder = _constructAnnotatedClass(OrderBean.class);
        String[] propOrder = _ai.findSerializationPropertyOrder(acOrder);
        assertNotNull(propOrder);
        assertArrayEquals(new String[]{"b", "a"}, propOrder);
        assertEquals(Boolean.TRUE, _ai.findSerializationSortAlphabetically(acOrder));

        AnnotatedClass acVirtual = _constructAnnotatedClass(VirtualPropsBean.class);
        List<BeanPropertyWriter> props = new ArrayList<>();
        _ai.findAndAddVirtualProperties(_mapper.getSerializationConfig(), acVirtual, props);
        assertEquals(2, props.size());
        assertEquals("prop1", props.get(0).getName());
        assertEquals("vProp", props.get(1).getName());
    }

    @Test(timeout = 4000)
    public void testValueInstantiatorAndPOJOBuilder() {
        AnnotatedClass ac = _constructAnnotatedClass(TargetWithBuilder.class);
        assertEquals(Object.class, _ai.findValueInstantiator(ac));
        assertEquals(BuilderBean.class, _ai.findPOJOBuilder(ac));
        JsonPOJOBuilder.Value builderConfig = _ai.findPOJOBuilderConfig(ac);
        assertNotNull(builderConfig);
        assertEquals("construct", builderConfig.buildMethodName);
        assertEquals("with", builderConfig.withPrefix);
    }

    @Test(timeout = 4000)
    public void testTypeRefinementSpecializationAndGeneralization() throws Exception {
        AnnotatedClass ac = _constructAnnotatedClass(RefineBean.class);
        SerializationConfig serConfig = _mapper.getSerializationConfig();
        DeserializationConfig deserConfig = _mapper.getDeserializationConfig();

        // 1. Serialization Generalization on base, key, and content
        AnnotatedField genMapField = _getField(ac, "generalizedMap");
        JavaType concreteMapType = _mapper.getTypeFactory().constructMapType(HashMap.class, String.class, TypeHierarchySub.class);
        JavaType serRefined = _ai.refineSerializationType(serConfig, genMapField, concreteMapType);
        assertEquals(Map.class, serRefined.getRawClass());
        assertEquals(String.class, serRefined.getKeyType().getRawClass());
        assertEquals(TypeHierarchyBase.class, serRefined.getContentType().getRawClass());

        // 2. Serialization Specialization
        AnnotatedField specField = _getField(ac, "specialized");
        JavaType baseType = _mapper.constructType(TypeHierarchyBase.class);
        JavaType specRefined = _ai.refineSerializationType(serConfig, specField, baseType);
        assertEquals(TypeHierarchySub.class, specRefined.getRawClass());

        // 3. Deserialization Specialization on base, key, and content
        AnnotatedField deserMapField = _getField(ac, "deserSpecializedMap");
        JavaType deserMapType = _mapper.getTypeFactory().constructMapType(Map.class, CharSequence.class, TypeHierarchyBase.class);
        JavaType deserRefined = _ai.refineDeserializationType(deserConfig, deserMapField, deserMapType);
        assertEquals(String.class, deserRefined.getKeyType().getRawClass());
        assertEquals(TypeHierarchySub.class, deserRefined.getContentType().getRawClass());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testNullAndEmptyHandling() {
        AnnotatedClass acNoRoot = _constructAnnotatedClass(NoRoot.class);
        assertNull(_ai.findPropertyIgnorals(null).getIgnored());
        assertNull(_ai.findNamingStrategy(acNoRoot));
        assertNull(_ai.findClassDescription(acNoRoot));
        assertNull(_ai.findFilterId(acNoRoot));
        assertNull(_ai.findPropertyAliases(acNoRoot));
        assertNull(_ai.findPropertyAccess(acNoRoot));
        assertNull(_ai.findPropertyDescription(acNoRoot));
        assertNull(_ai.findPropertyIndex(acNoRoot));
        assertNull(_ai.findPropertyDefaultValue(acNoRoot));
        assertNull(_ai.findFormat(acNoRoot));
        assertNull(_ai.findReferenceType(acNoRoot));
        assertNull(_ai.findInjectableValue(acNoRoot));
        assertNull(_ai.findViews(acNoRoot));
        assertNull(_ai.findSubtypes(acNoRoot));
        assertNull(_ai.findTypeName(acNoRoot));
        assertFalse(_ai.isTypeId(acNoRoot));
        assertNull(_ai.findObjectIdInfo(acNoRoot));
        assertNull(_ai.findSerializer(acNoRoot));
        assertNull(_ai.findKeySerializer(acNoRoot));
        assertNull(_ai.findContentSerializer(acNoRoot));
        assertNull(_ai.findNullSerializer(acNoRoot));
        assertNull(_ai.findSerializationTyping(acNoRoot));
        assertNull(_ai.findSerializationConverter(acNoRoot));
        assertNull(_ai.findSerializationPropertyOrder(acNoRoot));
        assertNull(_ai.findSerializationSortAlphabetically(acNoRoot));
        assertNull(_ai.findDeserializer(acNoRoot));
        assertNull(_ai.findKeyDeserializer(acNoRoot));
        assertNull(_ai.findContentDeserializer(acNoRoot));
        assertNull(_ai.findDeserializationConverter(acNoRoot));
        assertNull(_ai.findValueInstantiator(acNoRoot));
        assertNull(_ai.findPOJOBuilder(acNoRoot));
        assertNull(_ai.findPOJOBuilderConfig(acNoRoot));
        assertNull(_ai.hasAnySetter(acNoRoot));
        assertNull(_ai.findMergeInfo(acNoRoot));
    }

    @Test(timeout = 4000)
    public void testDeprecatedMethodsReturnDefaults() {
        AnnotatedClass ac = _constructAnnotatedClass(NoRoot.class);
        JavaType type = _mapper.constructType(String.class);

        @SuppressWarnings("deprecation")
        Class<?> serType = _ai.findSerializationType(ac);
        assertNull(serType);

        @SuppressWarnings("deprecation")
        Class<?> serKeyType = _ai.findSerializationKeyType(ac, type);
        assertNull(serKeyType);

        @SuppressWarnings("deprecation")
        Class<?> serContentType = _ai.findSerializationContentType(ac, type);
        assertNull(serContentType);

        @SuppressWarnings("deprecation")
        Class<?> deserType = _ai.findDeserializationType(ac, type);
        assertNull(deserType);

        @SuppressWarnings("deprecation")
        Class<?> deserKeyType = _ai.findDeserializationKeyType(ac, type);
        assertNull(deserKeyType);

        @SuppressWarnings("deprecation")
        Class<?> deserContentType = _ai.findDeserializationContentType(ac, type);
        assertNull(deserContentType);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testFindPropertyContentTypeResolverWithNonContainerThrowsException() throws Exception {
        AnnotatedClass ac = _constructAnnotatedClass(MemberBean.class);
        AnnotatedField field = _getField(ac, "field");
        JavaType scalarType = _mapper.constructType(String.class);
        // String has no content type; must throw IllegalArgumentException
        _ai.findPropertyContentTypeResolver(_mapper.getSerializationConfig(), field, scalarType);
    }

    static class IncompatibleRefineBean {
        @JsonSerialize(as = String.class)
        public Integer num;
    }

    @Test(timeout = 4000, expected = JsonMappingException.class)
    public void testRefineSerializationTypeUnrelatedThrowsException() throws Exception {
        AnnotatedClass ac = _constructAnnotatedClass(IncompatibleRefineBean.class);
        AnnotatedField field = _getField(ac, "num");
        JavaType baseType = _mapper.constructType(Integer.class);
        _ai.refineSerializationType(_mapper.getSerializationConfig(), field, baseType);
    }

    static class IncompatibleDeserRefineBean {
        @JsonDeserialize(as = String.class)
        public Integer num;
    }

    @Test(timeout = 4000, expected = JsonMappingException.class)
    public void testRefineDeserializationTypeUnrelatedThrowsException() throws Exception {
        AnnotatedClass ac = _constructAnnotatedClass(IncompatibleDeserRefineBean.class);
        AnnotatedField field = _getField(ac, "num");
        JavaType baseType = _mapper.constructType(Integer.class);
        _ai.refineDeserializationType(_mapper.getDeserializationConfig(), field, baseType);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testSerializationAndReadResolve() throws Exception {
        // Exercise cache populating
        _ai.isAnnotationBundle(new DummyBundle() {
            public Class<? extends Annotation> annotationType() { return DummyBundle.class; }
        });

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(_ai);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object deserialized = ois.readObject();

        assertNotNull(deserialized);
        assertTrue(deserialized instanceof JacksonAnnotationIntrospector);

        JacksonAnnotationIntrospector restored = (JacksonAnnotationIntrospector) deserialized;
        // Verify restored instance works and re-creates internal transient cache
        assertTrue(restored.isAnnotationBundle(new DummyBundle() {
            public Class<? extends Annotation> annotationType() { return DummyBundle.class; }
        }));
    }

    // =========================================================================
    // Helper Inspection Utilities
    // =========================================================================

    private AnnotatedClass _constructAnnotatedClass(Class<?> cls) {
        return AnnotatedClassResolver.resolve(_mapper.getSerializationConfig(),
                _mapper.constructType(cls), _mapper.getSerializationConfig());
    }

    private AnnotatedField _getField(AnnotatedClass ac, String fieldName) {
        for (AnnotatedField f : ac.fields()) {
            if (f.getName().equals(fieldName)) {
                return f;
            }
        }
        fail("Field not found: " + fieldName);
        return null;
    }

    private AnnotatedMethod _getMethod(AnnotatedClass ac, String methodName, Class<?>... paramTypes) {
        for (AnnotatedMethod m : ac.memberMethods()) {
            if (m.getName().equals(methodName) && m.getParameterCount() == paramTypes.length) {
                boolean match = true;
                for (int i = 0; i < paramTypes.length; i++) {
                    if (!m.getRawParameterType(i).equals(paramTypes[i])) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    return m;
                }
            }
        }
        fail("Method not found: " + methodName);
        return null;
    }
}