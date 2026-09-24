package com.fasterxml.jackson.databind.introspect;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

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
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import com.fasterxml.jackson.databind.util.NameTransformer;

/*
 * [Branch & Defect Analysis Matrix]
 * Targets: JacksonAnnotationIntrospector
 *
 * 1. Defect-Targeted Zone (com.fasterxml.jackson.databind.struct.TestUnwrapped / #442):
 *    - In `findNameForSerialization(Annotated)`, `@JsonUnwrapped` must be recognized as an
 *      indicator that the member is a serialization property (returning empty PropertyName),
 *      matching the behavior of `findNameForDeserialization(Annotated)`.
 *    - In defective code, `findNameForSerialization` returns null for properties marked only with
 *      `@JsonUnwrapped`, causing empty bean serialization errors.
 *
 * 2. General Class & Property Annotations:
 *    - Enum value naming: @JsonProperty on enum constant vs plain enum.
 *    - Root name: @JsonRootName with and without explicit namespace, empty string namespace handling.
 *    - Ignored properties: @JsonIgnoreProperties with allowGetters, allowSetters, ignoreUnknown, null checks.
 *    - Ignorable type: @JsonIgnoreType (true / false).
 *    - Filter id: @JsonFilter with non-empty vs empty string ID.
 *    - Naming strategy: @JsonNaming.
 *    - Auto-detect visibility: @JsonAutoDetect.
 *
 * 3. Member Annotations:
 *    - Markers: @JsonIgnore, @JsonProperty(required, access, index, defaultValue).
 *    - Reference types: @JsonManagedReference, @JsonBackReference.
 *    - Unwrapping: @JsonUnwrapped (enabled vs disabled).
 *    - Injections: @JacksonInject with explicit id vs default fallback to field/method param type name.
 *    - Views: @JsonView.
 *
 * 4. Polymorphic Type Handling & Object IDs:
 *    - @JsonTypeInfo, @JsonTypeResolver, @JsonTypeIdResolver.
 *    - findPropertyTypeResolver on container vs non-container types.
 *    - findPropertyContentTypeResolver throwing IllegalArgumentException on non-containers.
 *    - Subtypes & Type name: @JsonSubTypes, @JsonTypeName, @JsonTypeId.
 *    - Object ID: @JsonIdentityInfo (valid generator vs None), @JsonIdentityReference.
 *
 * 5. Serialization & Deserialization Configurations:
 *    - Custom serializers/deserializers (using, keyUsing, contentUsing, nullsUsing).
 *    - @JsonRawValue serialization.
 *    - Inclusion rules: @JsonInclude vs legacy @JsonSerialize.Inclusion.
 *    - Property ordering & alphabetic sorting: @JsonPropertyOrder.
 *    - Virtual properties: @JsonAppend (attrs and props).
 *    - Creators: @JsonCreator (mode selection and detection).
 */
public class JacksonAnnotationIntrospectorGeminiTest {

    private final JacksonAnnotationIntrospector introspector = new JacksonAnnotationIntrospector();
    private final ObjectMapper mapper = new ObjectMapper();

    private AnnotatedClass getAnnotatedClass(Class<?> cls) {
        return AnnotatedClass.constructWithoutSuperTypes(cls, mapper.getSerializationConfig());
    }

    private AnnotatedMember getField(Class<?> cls, String name) {
        AnnotatedClass ac = getAnnotatedClass(cls);
        for (AnnotatedField f : ac.fields()) {
            if (f.getName().equals(name)) {
                return f;
            }
        }
        fail("Field '" + name + "' not found on " + cls.getName());
        return null;
    }

    private AnnotatedMethod getMethod(Class<?> cls, String name) {
        AnnotatedClass ac = getAnnotatedClass(cls);
        for (AnnotatedMethod m : ac.memberMethods()) {
            if (m.getName().equals(name)) {
                return m;
            }
        }
        fail("Method '" + name + "' not found on " + cls.getName());
        return null;
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (TestUnwrapped as property indicator)
    // =========================================================================

    static class InnerBean {
        public int x;
    }

    static class OuterWithUnwrappedField {
        @JsonUnwrapped
        public InnerBean inner;
    }

    static class OuterWithUnwrappedMethod {
        private InnerBean inner;

        @JsonUnwrapped
        public InnerBean getInner() {
            return inner;
        }
    }

    static class OuterWithBackReference {
        @JsonBackReference
        public InnerBean inner;
    }

    static class OuterWithManagedReference {
        @JsonManagedReference
        public InnerBean inner;
    }

    /**
     * Targets the defect where findNameForSerialization does not recognize @JsonUnwrapped
     * as an implicit property marker (unlike findNameForDeserialization).
     */
    @Test(timeout = 4000)
    public void testFindNameForSerializationWithJsonUnwrappedFieldDefect() {
        AnnotatedMember member = getField(OuterWithUnwrappedField.class, "inner");
        PropertyName name = introspector.findNameForSerialization(member);
        assertNotNull("findNameForSerialization must recognize @JsonUnwrapped and return empty PropertyName", name);
        assertEquals("", name.getSimpleName());
    }

    @Test(timeout = 4000)
    public void testFindNameForSerializationWithJsonUnwrappedMethodDefect() {
        AnnotatedMember member = getMethod(OuterWithUnwrappedMethod.class, "getInner");
        PropertyName name = introspector.findNameForSerialization(member);
        assertNotNull("findNameForSerialization must recognize @JsonUnwrapped on getters", name);
        assertEquals("", name.getSimpleName());
    }

    @Test(timeout = 4000)
    public void testFindNameForSerializationWithReferencesDefect() {
        AnnotatedMember backMember = getField(OuterWithBackReference.class, "inner");
        PropertyName backName = introspector.findNameForSerialization(backMember);
        assertNotNull("findNameForSerialization should recognize @JsonBackReference", backName);
        assertEquals("", backName.getSimpleName());

        AnnotatedMember managedMember = getField(OuterWithManagedReference.class, "inner");
        PropertyName managedName = introspector.findNameForSerialization(managedMember);
        assertNotNull("findNameForSerialization should recognize @JsonManagedReference", managedName);
        assertEquals("", managedName.getSimpleName());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testVersion() {
        Version v = introspector.version();
        assertFalse(v.isUnknownVersion());
        assertEquals("jackson-databind", v.getArtifactId());
    }

    @JacksonAnnotationsInside
    @Retention(RetentionPolicy.RUNTIME)
    @interface CustomBundle { }

    @Retention(RetentionPolicy.RUNTIME)
    @interface NotABundle { }

    @CustomBundle
    static class BundledClass { }

    @NotABundle
    static class PlainClass { }

    @Test(timeout = 4000)
    public void testIsAnnotationBundle() {
        CustomBundle b = BundledClass.class.getAnnotation(CustomBundle.class);
        assertTrue(introspector.isAnnotationBundle(b));

        NotABundle nb = PlainClass.class.getAnnotation(NotABundle.class);
        assertFalse(introspector.isAnnotationBundle(nb));
    }

    enum TestEnum {
        @JsonProperty("first_value")
        FIRST,
        @JsonProperty("")
        EMPTY_NAME,
        PLAIN
    }

    @Test(timeout = 4000)
    public void testFindEnumValue() {
        assertEquals("first_value", introspector.findEnumValue(TestEnum.FIRST));
        assertEquals("EMPTY_NAME", introspector.findEnumValue(TestEnum.EMPTY_NAME));
        assertEquals("PLAIN", introspector.findEnumValue(TestEnum.PLAIN));
    }

    @JsonRootName(value = "customRoot", namespace = "http://example.com")
    static class RootWithNamespace { }

    @JsonRootName(value = "customRoot", namespace = "")
    static class RootWithEmptyNamespace { }

    static class PlainRoot { }

    @Test(timeout = 4000)
    public void testFindRootName() {
        PropertyName pn1 = introspector.findRootName(getAnnotatedClass(RootWithNamespace.class));
        assertNotNull(pn1);
        assertEquals("customRoot", pn1.getSimpleName());
        assertEquals("http://example.com", pn1.getNamespace());

        PropertyName pn2 = introspector.findRootName(getAnnotatedClass(RootWithEmptyNamespace.class));
        assertNotNull(pn2);
        assertEquals("customRoot", pn2.getSimpleName());
        assertNull(pn2.getNamespace());

        assertNull(introspector.findRootName(getAnnotatedClass(PlainRoot.class)));
    }

    @JsonIgnoreProperties(value = {"p1", "p2"}, allowGetters = true, allowSetters = false, ignoreUnknown = true)
    static class IgnorePropertiesTarget { }

    @JsonIgnoreProperties(value = {"p3"}, allowGetters = false, allowSetters = true)
    static class IgnorePropertiesTarget2 { }

    @Test(timeout = 4000)
    @SuppressWarnings("deprecation")
    public void testFindPropertiesToIgnore() {
        AnnotatedClass ac1 = getAnnotatedClass(IgnorePropertiesTarget.class);
        AnnotatedClass ac2 = getAnnotatedClass(IgnorePropertiesTarget2.class);
        AnnotatedClass plain = getAnnotatedClass(PlainRoot.class);

        assertArrayEquals(new String[]{"p1", "p2"}, introspector.findPropertiesToIgnore(ac1));
        assertNull(introspector.findPropertiesToIgnore(plain));

        assertNull(introspector.findPropertiesToIgnore(ac1, true));
        assertArrayEquals(new String[]{"p1", "p2"}, introspector.findPropertiesToIgnore(ac1, false));

        assertArrayEquals(new String[]{"p3"}, introspector.findPropertiesToIgnore(ac2, true));
        assertNull(introspector.findPropertiesToIgnore(ac2, false));

        assertTrue(introspector.findIgnoreUnknownProperties(ac1));
        assertNull(introspector.findIgnoreUnknownProperties(plain));
    }

    @JsonIgnoreType(true)
    static class IgnorableClass { }

    @JsonIgnoreType(false)
    static class NonIgnorableClass { }

    @Test(timeout = 4000)
    public void testIsIgnorableType() {
        assertTrue(introspector.isIgnorableType(getAnnotatedClass(IgnorableClass.class)));
        assertFalse(introspector.isIgnorableType(getAnnotatedClass(NonIgnorableClass.class)));
        assertNull(introspector.isIgnorableType(getAnnotatedClass(PlainRoot.class)));
    }

    @JsonFilter("filter-123")
    static class FilteredClass { }

    @JsonFilter("")
    static class EmptyFilterClass { }

    @Test(timeout = 4000)
    @SuppressWarnings("deprecation")
    public void testFindFilterId() {
        AnnotatedClass ac = getAnnotatedClass(FilteredClass.class);
        assertEquals("filter-123", introspector.findFilterId(ac));
        assertEquals("filter-123", introspector.findFilterId((Annotated) ac));

        AnnotatedClass acEmpty = getAnnotatedClass(EmptyFilterClass.class);
        assertNull(introspector.findFilterId(acEmpty));
        assertNull(introspector.findFilterId(getAnnotatedClass(PlainRoot.class)));
    }

    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    static class NamingClass { }

    @Test(timeout = 4000)
    public void testFindNamingStrategy() {
        assertEquals(PropertyNamingStrategy.SnakeCaseStrategy.class,
                introspector.findNamingStrategy(getAnnotatedClass(NamingClass.class)));
        assertNull(introspector.findNamingStrategy(getAnnotatedClass(PlainRoot.class)));
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    static class AutoDetectClass { }

    @Test(timeout = 4000)
    public void testFindAutoDetectVisibility() {
        VisibilityChecker<?> defaultChecker = VisibilityChecker.Std.defaultInstance();
        VisibilityChecker<?> custom = introspector.findAutoDetectVisibility(getAnnotatedClass(AutoDetectClass.class), defaultChecker);
        assertNotEquals(defaultChecker, custom);

        VisibilityChecker<?> same = introspector.findAutoDetectVisibility(getAnnotatedClass(PlainRoot.class), defaultChecker);
        assertSame(defaultChecker, same);
    }

    // =========================================================================
    // Partition B: Member Properties, Formatting, Markers & Injections
    // =========================================================================

    static class MemberBean {
        @JsonProperty(value = "fieldProp", required = true, access = JsonProperty.Access.READ_ONLY,
                index = 3, defaultValue = "default_val")
        public String annotatedField;

        @JsonProperty(index = JsonProperty.INDEX_UNKNOWN, defaultValue = "")
        public String indexUnknown;

        @JsonIgnore(true)
        public String ignoredField;

        @JsonIgnore(false)
        public String notIgnoredField;

        @JsonPropertyDescription("A nice description")
        public String descField;

        @JsonFormat(pattern = "yyyy-MM-dd")
        public String formatField;

        @JsonManagedReference("ref-name")
        public MemberBean managedRef;

        @JsonBackReference("ref-name")
        public MemberBean backRef;

        @JsonUnwrapped(prefix = "pre_", suffix = "_post")
        public InnerBean unwrappedOn;

        @JsonUnwrapped(enabled = false)
        public InnerBean unwrappedOff;

        @JacksonInject("injectId")
        public String injectedExplicit;

        @JacksonInject("")
        public String injectedDefaultField;

        @JacksonInject("")
        public void setInjectedDefault(String val) { }

        @JacksonInject("")
        public String getInjectedDefaultNoArgs() { return ""; }

        @JsonView({MemberBean.class})
        public String viewField;

        @JsonGetter("customGetterName")
        public String getCustomGetter() { return ""; }

        @JsonSetter("customSetterName")
        public void setCustomSetter(String s) { }

        @JsonValue
        public String asValueMethod() { return ""; }

        @JsonValue(false)
        public String notAsValueMethod() { return ""; }

        @JsonAnySetter
        public void anySetter(String k, Object v) { }

        @JsonAnyGetter
        public java.util.Map<String, Object> anyGetter() { return null; }

        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        public MemberBean(@JsonProperty("annotatedField") String f) {
            this.annotatedField = f;
        }

        @JsonCreator(mode = JsonCreator.Mode.DISABLED)
        public MemberBean() { }
    }

    @Test(timeout = 4000)
    public void testMemberPropertyMarkers() {
        AnnotatedMember f = getField(MemberBean.class, "annotatedField");
        assertNull(introspector.findImplicitPropertyName(f));
        assertEquals(Boolean.TRUE, introspector.hasRequiredMarker(f));
        assertEquals(JsonProperty.Access.READ_ONLY, introspector.findPropertyAccess(f));
        assertEquals(Integer.valueOf(3), introspector.findPropertyIndex(f));
        assertEquals("default_val", introspector.findPropertyDefaultValue(f));

        AnnotatedMember fUnknown = getField(MemberBean.class, "indexUnknown");
        assertNull(introspector.findPropertyIndex(fUnknown));
        assertNull(introspector.findPropertyDefaultValue(fUnknown));

        assertTrue(introspector.hasIgnoreMarker(getField(MemberBean.class, "ignoredField")));
        assertFalse(introspector.hasIgnoreMarker(getField(MemberBean.class, "notIgnoredField")));

        assertEquals("A nice description", introspector.findPropertyDescription(getField(MemberBean.class, "descField")));
        assertNull(introspector.findPropertyDescription(fUnknown));

        JsonFormat.Value fmt = introspector.findFormat(getField(MemberBean.class, "formatField"));
        assertNotNull(fmt);
        assertEquals("yyyy-MM-dd", fmt.getPattern());
        assertNull(introspector.findFormat(fUnknown));
    }

    @Test(timeout = 4000)
    public void testReferenceProperties() {
        AnnotatedMember m1 = getField(MemberBean.class, "managedRef");
        AnnotationIntrospector.ReferenceProperty ref1 = introspector.findReferenceType(m1);
        assertNotNull(ref1);
        assertTrue(ref1.isManagedReference());
        assertEquals("ref-name", ref1.getName());

        AnnotatedMember m2 = getField(MemberBean.class, "backRef");
        AnnotationIntrospector.ReferenceProperty ref2 = introspector.findReferenceType(m2);
        assertNotNull(ref2);
        assertTrue(ref2.isBackReference());
        assertEquals("ref-name", ref2.getName());

        assertNull(introspector.findReferenceType(getField(MemberBean.class, "annotatedField")));
    }

    @Test(timeout = 4000)
    public void testUnwrappingNameTransformer() {
        NameTransformer nt = introspector.findUnwrappingNameTransformer(getField(MemberBean.class, "unwrappedOn"));
        assertNotNull(nt);
        assertEquals("pre_val_post", nt.transform("val"));

        assertNull(introspector.findUnwrappingNameTransformer(getField(MemberBean.class, "unwrappedOff")));
        assertNull(introspector.findUnwrappingNameTransformer(getField(MemberBean.class, "annotatedField")));
    }

    @Test(timeout = 4000)
    public void testFindInjectableValueId() {
        assertEquals("injectId", introspector.findInjectableValueId(getField(MemberBean.class, "injectedExplicit")));
        assertEquals(String.class.getName(), introspector.findInjectableValueId(getField(MemberBean.class, "injectedDefaultField")));
        assertEquals(String.class.getName(), introspector.findInjectableValueId(getMethod(MemberBean.class, "setInjectedDefault")));
        assertEquals(String.class.getName(), introspector.findInjectableValueId(getMethod(MemberBean.class, "getInjectedDefaultNoArgs")));
        assertNull(introspector.findInjectableValueId(getField(MemberBean.class, "annotatedField")));
    }

    @Test(timeout = 4000)
    public void testFindViews() {
        Class<?>[] views = introspector.findViews(getField(MemberBean.class, "viewField"));
        assertNotNull(views);
        assertEquals(1, views.length);
        assertEquals(MemberBean.class, views[0]);
        assertNull(introspector.findViews(getField(MemberBean.class, "annotatedField")));
    }

    @Test(timeout = 4000)
    public void testPropertyNamesSerializationAndDeserialization() {
        AnnotatedMethod getM = getMethod(MemberBean.class, "getCustomGetter");
        assertEquals("customGetterName", introspector.findNameForSerialization(getM).getSimpleName());

        AnnotatedMethod setM = getMethod(MemberBean.class, "setCustomSetter");
        assertEquals("customSetterName", introspector.findNameForDeserialization(setM).getSimpleName());

        AnnotatedMember f = getField(MemberBean.class, "annotatedField");
        assertEquals("fieldProp", introspector.findNameForSerialization(f).getSimpleName());
        assertEquals("fieldProp", introspector.findNameForDeserialization(f).getSimpleName());

        AnnotatedMember plainField = getField(MemberBean.class, "indexUnknown");
        assertEquals("", introspector.findNameForSerialization(plainField).getSimpleName());
        assertEquals("", introspector.findNameForDeserialization(plainField).getSimpleName());

        AnnotatedMember descField = getField(MemberBean.class, "descField");
        assertNull(introspector.findNameForSerialization(descField));
        assertNull(introspector.findNameForDeserialization(descField));
    }

    @Test(timeout = 4000)
    public void testValueAndCreatorMarkers() {
        assertTrue(introspector.hasAsValueAnnotation(getMethod(MemberBean.class, "asValueMethod")));
        assertFalse(introspector.hasAsValueAnnotation(getMethod(MemberBean.class, "notAsValueMethod")));

        assertTrue(introspector.hasAnySetterAnnotation(getMethod(MemberBean.class, "anySetter")));
        assertTrue(introspector.hasAnyGetterAnnotation(getMethod(MemberBean.class, "anyGetter")));

        AnnotatedClass ac = getAnnotatedClass(MemberBean.class);
        AnnotatedConstructor activeCreator = null;
        AnnotatedConstructor disabledCreator = null;
        for (AnnotatedConstructor ctor : ac.getConstructors()) {
            if (ctor.getParameterCount() == 1) {
                activeCreator = ctor;
            } else if (ctor.getParameterCount() == 0) {
                disabledCreator = ctor;
            }
        }
        assertNotNull(activeCreator);
        assertNotNull(disabledCreator);

        assertTrue(introspector.hasCreatorAnnotation(activeCreator));
        assertEquals(JsonCreator.Mode.PROPERTIES, introspector.findCreatorBinding(activeCreator));

        assertFalse(introspector.hasCreatorAnnotation(disabledCreator));
        assertEquals(JsonCreator.Mode.DISABLED, introspector.findCreatorBinding(disabledCreator));
    }

    // =========================================================================
    // Partition D: Polymorphism, Typing & Subtypes
    // =========================================================================

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@type", defaultImpl = PolySubA.class)
    @JsonSubTypes({
            @JsonSubTypes.Type(value = PolySubA.class, name = "subA"),
            @JsonSubTypes.Type(value = PolySubB.class, name = "subB")
    })
    static class PolyBase { }

    @JsonTypeName("subA")
    static class PolySubA extends PolyBase { }

    @JsonTypeName("subB")
    static class PolySubB extends PolyBase { }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
    static class NoTypeInfoClass { }

    static class ContainerHolder {
        @JsonTypeId
        public String typeIdField;

        public List<String> listField;
    }

    @Test(timeout = 4000)
    public void testPolymorphicTypeResolvers() {
        AnnotatedClass acBase = getAnnotatedClass(PolyBase.class);
        JavaType baseType = TypeFactory.defaultInstance().constructType(PolyBase.class);
        TypeResolverBuilder<?> b = introspector.findTypeResolver(mapper.getSerializationConfig(), acBase, baseType);
        assertNotNull(b);

        AnnotatedClass acNoType = getAnnotatedClass(NoTypeInfoClass.class);
        TypeResolverBuilder<?> bNone = introspector.findTypeResolver(mapper.getSerializationConfig(), acNoType,
                TypeFactory.defaultInstance().constructType(NoTypeInfoClass.class));
        assertNotNull(bNone);
        assertEquals(StdTypeResolverBuilder.class, bNone.getClass());

        AnnotatedMember fList = getField(ContainerHolder.class, "listField");
        JavaType listType = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
        assertNull(introspector.findPropertyTypeResolver(mapper.getSerializationConfig(), fList, listType));

        TypeResolverBuilder<?> bCont = introspector.findPropertyContentTypeResolver(
                mapper.getSerializationConfig(), fList, listType);
        assertNull(bCont);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testPropertyContentTypeResolverNonContainerThrows() {
        AnnotatedMember f = getField(ContainerHolder.class, "typeIdField");
        JavaType scalarType = TypeFactory.defaultInstance().constructType(String.class);
        introspector.findPropertyContentTypeResolver(mapper.getSerializationConfig(), f, scalarType);
    }

    @Test(timeout = 4000)
    public void testSubtypesAndTypeName() {
        AnnotatedClass ac = getAnnotatedClass(PolyBase.class);
        List<NamedType> subtypes = introspector.findSubtypes(ac);
        assertNotNull(subtypes);
        assertEquals(2, subtypes.size());
        assertEquals("subA", subtypes.get(0).getName());
        assertEquals(PolySubA.class, subtypes.get(0).getType());

        assertEquals("subA", introspector.findTypeName(getAnnotatedClass(PolySubA.class)));
        assertNull(introspector.findTypeName(getAnnotatedClass(PlainRoot.class)));

        assertTrue(introspector.isTypeId(getField(ContainerHolder.class, "typeIdField")));
        assertFalse(introspector.isTypeId(getField(ContainerHolder.class, "listField")));
    }

    // =========================================================================
    // Partition E: Object ID, Custom Serializers/Deserializers & Inclusions
    // =========================================================================

    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
    @JsonIdentityReference(alwaysAsId = true)
    static class IdClass { }

    @JsonIdentityInfo(generator = ObjectIdGenerators.None.class)
    static class NoneIdClass { }

    @Test(timeout = 4000)
    public void testObjectIdHandling() {
        AnnotatedClass ac = getAnnotatedClass(IdClass.class);
        ObjectIdInfo info = introspector.findObjectIdInfo(ac);
        assertNotNull(info);
        assertEquals("@id", info.getPropertyName().getSimpleName());
        assertEquals(ObjectIdGenerators.IntSequenceGenerator.class, info.getGeneratorType());

        ObjectIdInfo refInfo = introspector.findObjectReferenceInfo(ac, info);
        assertTrue(refInfo.getAlwaysAsId());

        assertNull(introspector.findObjectIdInfo(getAnnotatedClass(NoneIdClass.class)));
        assertNull(introspector.findObjectIdInfo(getAnnotatedClass(PlainRoot.class)));
    }

    static class DummySerializer extends JsonSerializer<String> {
        @Override
        public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen,
                              SerializerProvider serializers) { }
    }

    static class DummyDeserializer extends JsonDeserializer<String> {
        @Override
        public String deserialize(com.fasterxml.jackson.core.JsonParser p,
                                  DeserializationContext ctxt) { return ""; }
    }

    static class DummyKeyDeserializer extends KeyDeserializer {
        @Override
        public Object deserializeKey(String key, DeserializationContext ctxt) { return key; }
    }

    static class SerDeserBean {
        @JsonSerialize(using = DummySerializer.class, keyUsing = DummySerializer.class,
                contentUsing = DummySerializer.class, nullsUsing = DummySerializer.class,
                as = String.class, keyAs = String.class, contentAs = String.class,
                typing = JsonSerialize.Typing.STATIC)
        public Object fullyConfiguredSer;

        @JsonDeserialize(using = DummyDeserializer.class, keyUsing = DummyKeyDeserializer.class,
                contentUsing = DummyDeserializer.class, as = String.class,
                keyAs = String.class, contentAs = String.class)
        public Object fullyConfiguredDeser;

        @JsonRawValue(true)
        public String rawVal;

        @JsonInclude(value = JsonInclude.Include.NON_EMPTY, content = JsonInclude.Include.NON_NULL)
        public List<String> inclusionProp;

        @SuppressWarnings("deprecation")
        @JsonSerialize(include = JsonSerialize.Inclusion.NON_DEFAULT)
        public String legacyInclusionProp;
    }

    @Test(timeout = 4000)
    @SuppressWarnings("deprecation")
    public void testSerializersAndDeserializers() {
        AnnotatedMember serM = getField(SerDeserBean.class, "fullyConfiguredSer");
        assertEquals(DummySerializer.class, introspector.findSerializer(serM));
        assertEquals(DummySerializer.class, introspector.findKeySerializer(serM));
        assertEquals(DummySerializer.class, introspector.findContentSerializer(serM));
        assertEquals(DummySerializer.class, introspector.findNullSerializer(serM));
        assertEquals(String.class, introspector.findSerializationType(serM));
        assertEquals(String.class, introspector.findSerializationKeyType(serM, null));
        assertEquals(String.class, introspector.findSerializationContentType(serM, null));
        assertEquals(JsonSerialize.Typing.STATIC, introspector.findSerializationTyping(serM));

        AnnotatedMember rawM = getField(SerDeserBean.class, "rawVal");
        Object rawSer = introspector.findSerializer(rawM);
        assertNotNull(rawSer);
        assertEquals(RawSerializer.class, rawSer.getClass());

        AnnotatedMember deserM = getField(SerDeserBean.class, "fullyConfiguredDeser");
        assertEquals(DummyDeserializer.class, introspector.findDeserializer(deserM));
        assertEquals(DummyKeyDeserializer.class, introspector.findKeyDeserializer(deserM));
        assertEquals(DummyDeserializer.class, introspector.findContentDeserializer(deserM));
        assertEquals(String.class, introspector.findDeserializationType(deserM, null));
        assertEquals(String.class, introspector.findDeserializationKeyType(deserM, null));
        assertEquals(String.class, introspector.findDeserializationContentType(deserM, null));
    }

    @Test(timeout = 4000)
    @SuppressWarnings("deprecation")
    public void testInclusions() {
        AnnotatedMember incM = getField(SerDeserBean.class, "inclusionProp");
        assertEquals(JsonInclude.Include.NON_EMPTY,
                introspector.findSerializationInclusion(incM, JsonInclude.Include.ALWAYS));
        assertEquals(JsonInclude.Include.NON_NULL,
                introspector.findSerializationInclusionForContent(incM, JsonInclude.Include.ALWAYS));

        JsonInclude.Value incVal = introspector.findPropertyInclusion(incM);
        assertEquals(JsonInclude.Include.NON_EMPTY, incVal.getValueInclusion());
        assertEquals(JsonInclude.Include.NON_NULL, incVal.getContentInclusion());

        AnnotatedMember legacyM = getField(SerDeserBean.class, "legacyInclusionProp");
        assertEquals(JsonInclude.Include.NON_DEFAULT,
                introspector.findSerializationInclusion(legacyM, JsonInclude.Include.ALWAYS));
        JsonInclude.Value legacyIncVal = introspector.findPropertyInclusion(legacyM);
        assertEquals(JsonInclude.Include.NON_DEFAULT, legacyIncVal.getValueInclusion());
    }

    @JsonPropertyOrder(value = {"propB", "propA"}, alphabetic = true)
    static class OrderedBean { }

    @JsonPropertyOrder(alphabetic = false)
    static class UnorderedBean { }

    @Test(timeout = 4000)
    public void testSerializationOrdering() {
        AnnotatedClass ac = getAnnotatedClass(OrderedBean.class);
        assertArrayEquals(new String[]{"propB", "propA"}, introspector.findSerializationPropertyOrder(ac));
        assertEquals(Boolean.TRUE, introspector.findSerializationSortAlphabetically(ac));

        AnnotatedClass acUnordered = getAnnotatedClass(UnorderedBean.class);
        assertNull(introspector.findSerializationSortAlphabetically(acUnordered));
    }

    @JsonAppend(attrs = {@JsonAppend.Attr(value = "attr1", propName = "propAttr", propNamespace = "ns")},
                props = {@JsonAppend.Prop(value = DummyPropWriter.class, name = "virtualProp", type = String.class)})
    static class AppendBean { }

    public static class DummyPropWriter extends VirtualBeanPropertyWriter {
        public DummyPropWriter() { super(); }
        public DummyPropWriter(BeanPropertyDefinition propDef, com.fasterxml.jackson.databind.util.Annotations contextAnnotations, JavaType declaredType) {
            super(propDef, contextAnnotations, declaredType);
        }
        @Override
        protected Object value(Object bean, com.fasterxml.jackson.core.JsonGenerator gen, SerializerProvider prov) {
            return "virtualValue";
        }
        @Override
        public VirtualBeanPropertyWriter withConfig(MapperConfig<?> config, AnnotatedClass declaringClass, BeanPropertyDefinition propDef, JavaType type) {
            return new DummyPropWriter(propDef, declaringClass.getAnnotations(), type);
        }
    }

    @Test(timeout = 4000)
    public void testFindAndAddVirtualProperties() {
        AnnotatedClass ac = getAnnotatedClass(AppendBean.class);
        List<BeanPropertyWriter> props = new ArrayList<BeanPropertyWriter>();
        introspector.findAndAddVirtualProperties(mapper.getSerializationConfig(), ac, props);

        assertEquals(2, props.size());
        assertEquals("propAttr", props.get(0).getName());
        assertEquals("virtualProp", props.get(1).getName());
    }

    @JsonValueInstantiator(SerDeserBean.class)
    @JsonPOJOBuilder(buildMethodName = "create", withPrefix = "set")
    static class InstantiatorAndBuilderBean { }

    @JsonDeserialize(builder = InstantiatorAndBuilderBean.class)
    static class BuilderHolder { }

    @Test(timeout = 4000)
    public void testInstantiatorAndPOJOBuilder() {
        AnnotatedClass ac = getAnnotatedClass(InstantiatorAndBuilderBean.class);
        assertEquals(SerDeserBean.class, introspector.findValueInstantiator(ac));

        JsonPOJOBuilder.Value bValue = introspector.findPOJOBuilderConfig(ac);
        assertNotNull(bValue);
        assertEquals("create", bValue.buildMethodName);
        assertEquals("set", bValue.withPrefix);

        AnnotatedClass acHolder = getAnnotatedClass(BuilderHolder.class);
        assertEquals(InstantiatorAndBuilderBean.class, introspector.findPOJOBuilder(acHolder));
    }

    static class ConverterTestBean {
        @JsonSerialize(converter = Converter.None.class, contentConverter = Converter.None.class)
        @JsonDeserialize(converter = Converter.None.class, contentConverter = Converter.None.class)
        public String defaultConverter;
    }

    @Test(timeout = 4000)
    public void testDefaultConvertersReturnNull() {
        AnnotatedMember m = getField(ConverterTestBean.class, "defaultConverter");
        assertNull(introspector.findSerializationConverter(m));
        assertNull(introspector.findSerializationContentConverter(m));
        assertNull(introspector.findDeserializationConverter(m));
        assertNull(introspector.findDeserializationContentConverter(m));
    }
}