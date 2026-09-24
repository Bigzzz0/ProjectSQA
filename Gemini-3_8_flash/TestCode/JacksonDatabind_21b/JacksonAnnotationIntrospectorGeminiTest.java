package com.fasterxml.jackson.databind.introspect;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.*;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.VirtualBeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.std.RawSerializer;
import com.fasterxml.jackson.databind.util.Annotations;
import com.fasterxml.jackson.databind.util.Converter;
import com.fasterxml.jackson.databind.util.NameTransformer;
import com.fasterxml.jackson.databind.util.StdConverter;

/* [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------------
 * Target Component: JacksonAnnotationIntrospector
 *
 * 1. DEFECT-TARGETED BRANCH ZONE (Defects4J ground truth: TestEnumDeserialization::testEnumWithJsonPropertyRename)
 *    - Problem: Enum constants annotated with @JsonProperty("renamed") are not resolved to their custom names
 *      when findEnumValues(Class, Enum[], String[]) is invoked; JacksonAnnotationIntrospector failed to override
 *      findEnumValues, defaulting to Enum.name() values (e.g. ["B", "A"] instead of ["b", "a"]).
 *    - Targeted Branches: findEnumValues / Enum deserialization handling @JsonProperty on enum constant fields.
 *
 * 2. CORE FUNCTIONAL LOGIC & STATE TRANSITIONS
 *    - findRootName: null annotation, value with default namespace, value with explicit/empty namespace.
 *    - findPropertiesToIgnore: (1-arg vs 2-arg), forSerialization true/false, allowGetters/allowSetters.
 *    - findFilterId: null annotation, non-empty filter id, empty string filter id (overrides).
 *    - findNamingStrategy: null vs custom PropertyNamingStrategy class.
 *    - findAutoDetectVisibility: unchanged checker vs modified checker.
 *    - hasIgnoreMarker, hasRequiredMarker, findPropertyAccess, findPropertyDescription, findPropertyIndex,
 *      findPropertyDefaultValue, findFormat, findReferenceType (managed vs back), findUnwrappingNameTransformer.
 *    - findInjectableValueId: null, explicit string id, fallback to field type name, fallback to setter param type,
 *      fallback to getter return type.
 *    - findViews: null vs explicit Class<?> array.
 *    - Type Resolvers (_findTypeResolver):
 *      * Custom resolver (@JsonTypeResolver) with and without @JsonTypeInfo.
 *      * Standard resolver with Id.NONE -> noTypeInfoBuilder.
 *      * Standard resolver with Id.NAME, Id.CLASS.
 *      * External property on AnnotatedClass -> mapped to PROPERTY.
 *      * defaultImpl checks: None.class, annotation class (JsonTypeInfo.class), explicit class.
 *      * Custom @JsonTypeIdResolver.
 *    - Subtypes & TypeName: @JsonSubTypes parsing to NamedType, @JsonTypeName, isTypeId.
 *    - ObjectId: @JsonIdentityInfo (generator None.class vs active generator), @JsonIdentityReference.
 *    - Serialization & Deserialization types/serializers/converters/inclusions:
 *      * findSerializer, findKeySerializer, findContentSerializer, findNullSerializer (None.class vs custom).
 *      * @JsonRawValue handling -> RawSerializer instance.
 *      * Inclusion: @JsonInclude vs deprecated @JsonSerialize.Inclusion values (ALWAYS, NON_NULL, etc.).
 *      * Builder & ValueInstantiator introspection.
 *      * Creator annotations: @JsonCreator modes (PROPERTIES, DELEGATING, DISABLED).
 *    - Virtual Properties: @JsonAppend (attrs, props, prepend true/false).
 *
 * 3. BOUNDARY VALUE ANALYSIS (BVA) & DEFENSIVE PATHS
 *    - findPropertyContentTypeResolver with non-container JavaType -> IllegalArgumentException.
 *    - PropertyName construction helpers (_propertyName): empty localName -> USE_DEFAULT; null/empty namespace.
 *    - _classIfExplicit helper: null class, bogus class (Void.class), explicit class, implicit matching class.
 *
 * 4. OBJECT LIFECYCLE & INTEGRITY
 *    - Version verification, Java serialization round-trip preservation.
 * ---------------------------------------------------------------------------------------------------------
 */
public class JacksonAnnotationIntrospectorGeminiTest {

    // =========================================================================
    // PARTITION C: DEFECT-TARGETED BRANCH ZONE
    // =========================================================================

    public enum EnumWithJsonProperty {
        @JsonProperty("b")
        B,
        @JsonProperty("a")
        A
    }

    public enum PlainEnum {
        ONE,
        TWO
    }

    @Test(timeout = 4000)
    public void testEnumWithJsonPropertyRenameDirectIntrospector() {
        JacksonAnnotationIntrospector ai = new JacksonAnnotationIntrospector();
        String[] names = new String[2];
        String[] resolved = ai.findEnumValues(EnumWithJsonProperty.class, EnumWithJsonProperty.values(), names);
        assertNotNull("Resolved names array must not be null", resolved);
        assertEquals("Enum constant B should be renamed to 'b'", "b", resolved[0]);
        assertEquals("Enum constant A should be renamed to 'a'", "a", resolved[1]);
    }

    @Test(timeout = 4000)
    public void testEnumWithJsonPropertyDeserialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        EnumWithJsonProperty valB = mapper.readValue("\"b\"", EnumWithJsonProperty.class);
        assertEquals(EnumWithJsonProperty.B, valB);
        EnumWithJsonProperty valA = mapper.readValue("\"a\"", EnumWithJsonProperty.class);
        assertEquals(EnumWithJsonProperty.A, valA);
    }

    @Test(timeout = 4000)
    public void testPlainEnumValuesFallback() {
        JacksonAnnotationIntrospector ai = new JacksonAnnotationIntrospector();
        String[] names = new String[2];
        String[] resolved = ai.findEnumValues(PlainEnum.class, PlainEnum.values(), names);
        assertNotNull(resolved);
        assertEquals("ONE", resolved[0]);
        assertEquals("TWO", resolved[1]);
    }

    // =========================================================================
    // PARTITION A: CORE FUNCTIONAL LOGIC & STATE TRANSITIONS
    // =========================================================================

    @Target(ElementType.ANNOTATION_TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @JacksonAnnotationsInside
    public @interface CustomAnnotationBundle {
    }

    @CustomAnnotationBundle
    public static class BundleTarget { }

    @Test(timeout = 4000)
    public void testAnnotationBundleDetection() {
        JacksonAnnotationIntrospector ai = new JacksonAnnotationIntrospector();
        CustomAnnotationBundle ann = BundleTarget.class.getAnnotation(CustomAnnotationBundle.class);
        assertTrue("Meta-annotated with @JacksonAnnotationsInside must be detected as bundle",
                ai.isAnnotationBundle(ann));

        JsonProperty nonBundle = DummyPropertyHolder.class.getAnnotation(JsonProperty.class);
        assertFalse("Standard annotation without meta-annotation is not a bundle",
                ai.isAnnotationBundle(nonBundle));
    }

    @JsonRootName(value = "rootBean", namespace = "http://example.com/ns")
    public static class RootBeanWithNs { }

    @JsonRootName(value = "simpleRoot", namespace = "")
    public static class RootBeanEmptyNs { }

    public static class NoRootBean { }

    @Test(timeout = 4000)
    public void testFindRootName() {
        JacksonAnnotationIntrospector ai = new JacksonAnnotationIntrospector();
        AnnotatedClass acNs = getAnnotatedClass(RootBeanWithNs.class);
        PropertyName pnNs = ai.findRootName(acNs);
        assertNotNull(pnNs);
        assertEquals("rootBean", pnNs.getSimpleName());
        assertEquals("http://example.com/ns", pnNs.getNamespace());

        AnnotatedClass acEmptyNs = getAnnotatedClass(RootBeanEmptyNs.class);
        PropertyName pnEmptyNs = ai.findRootName(acEmptyNs);
        assertNotNull(pnEmptyNs);
        assertEquals("simpleRoot", pnEmptyNs.getSimpleName());
        assertNull("Empty namespace should be normalized to null", pnEmptyNs.getNamespace());

        AnnotatedClass acNone = getAnnotatedClass(NoRootBean.class);
        assertNull(ai.findRootName(acNone));
    }

    @JsonIgnoreProperties(value = {"propA", "propB"}, ignoreUnknown = true, allowGetters = true, allowSetters = false)
    public static class IgnorePropertiesBean1 { }

    @JsonIgnoreProperties(value = {"propC"}, allowGetters = false, allowSetters = true)
    public static class IgnorePropertiesBean2 { }

    @Test(timeout = 4000)
    @SuppressWarnings("deprecation")
    public void testFindPropertiesToIgnore() {
        JacksonAnnotationIntrospector ai = new JacksonAnnotationIntrospector();
        AnnotatedClass ac1 = getAnnotatedClass(IgnorePropertiesBean1.class);
        AnnotatedClass ac2 = getAnnotatedClass(IgnorePropertiesBean2.class);
        AnnotatedClass acNone = getAnnotatedClass(NoRootBean.class);

        // Deprecated 1-arg
        String[] ignores = ai.findPropertiesToIgnore(ac1);
        assertNotNull(ignores);
        assertEquals(2, ignores.length);
        assertNull(ai.findPropertiesToIgnore(acNone));

        // 2-arg: forSerialization true and allowGetters true -> returns null
        assertNull(ai.findPropertiesToIgnore(ac1, true));
        // 2-arg: forSerialization false and allowSetters false -> returns value
        String[] deserIgnores1 = ai.findPropertiesToIgnore(ac1, false);
        assertNotNull(deserIgnores1);
        assertEquals(2, deserIgnores1.length);

        // 2-arg: forSerialization true and allowGetters false -> returns value
        String[] serIgnores2 = ai.findPropertiesToIgnore(ac2, true);
        assertNotNull(serIgnores2);
        assertEquals(1, serIgnores2.length);
        assertEquals("propC", serIgnores2[0]);
        // 2-arg: forSerialization false and allowSetters true -> returns null
        assertNull(ai.findPropertiesToIgnore(ac2, false));

        // 2-arg: null annotation returns null
        assertNull(ai.findPropertiesToIgnore(acNone, true));
        assertNull(ai.findPropertiesToIgnore(acNone, false));
    }

    @Test(timeout = 4000)
    public void testFindIgnoreUnknownProperties() {
        JacksonAnnotationIntrospector ai = new JacksonAnnotationIntrospector();
        AnnotatedClass ac1 = getAnnotatedClass(IgnorePropertiesBean1.class);
        assertEquals(Boolean.TRUE, ai.findIgnoreUnknownProperties(ac1));

        AnnotatedClass acNone = getAnnotatedClass(NoRootBean.class);
        assertNull(ai.findIgnoreUnknownProperties(acNone));
    }

    @JsonIgnoreType(true)
    public static class IgnorableClass { }

    @JsonIgnoreType(false)
    public static class NonIgnorableClass { }

    @Test(timeout = 4000)
    public void testIsIgnorableType() {
        JacksonAnnotationIntrospector ai = new JacksonAnnotationIntrospector();
        assertEquals(Boolean.TRUE, ai.isIgnorableType(getAnnotatedClass(IgnorableClass.class)));
        assertEquals(Boolean.FALSE, ai.isIgnorableType(getAnnotatedClass(NonIgnorableClass.class)));
        assertNull(ai.isIgnorableType(getAnnotatedClass(NoRootBean.class)));
    }

    @JsonFilter("filter123")
    public static class FilteredClass { }

    @JsonFilter("")
    public static class EmptyFilterClass { }

    @Test(timeout = 4000)
    @SuppressWarnings("deprecation")
    public void testFindFilterId() {
        JacksonAnnotationIntrospector ai = new JacksonAnnotationIntrospector();
        assertEquals("filter123", ai.findFilterId(getAnnotatedClass(FilteredClass.class)));
        assertEquals("filter123", ai.findFilterId((Annotated) getAnnotatedClass(FilteredClass.class)));
        assertNull(ai.findFilterId(getAnnotatedClass(EmptyFilterClass.class)));
        assertNull(ai.findFilterId(getAnnotatedClass(NoRootBean.class)));
    }

    @JsonNaming(PropertyNamingStrategy.LowerCaseStrategy.class)
    public static class NamingClass { }

    @Test(timeout = 4000)
    public void testFindNamingStrategy() {
        JacksonAnnotationIntrospector ai = new JacksonAnnotationIntrospector();
        assertEquals(PropertyNamingStrategy.LowerCaseStrategy.class,
                ai.findNamingStrategy(getAnnotatedClass(NamingClass.class)));
        assertNull(ai.findNamingStrategy(getAnnotatedClass(NoRootBean.class)));
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NON_PRIVATE)
    public static class AutoDetectClass { }

    @Test(timeout = 4000)
    public void testFindAutoDetectVisibility() {
        JacksonAnnotationIntrospector ai = new JacksonAnnotationIntrospector();
        VisibilityChecker<?> checker = VisibilityChecker.Std.defaultInstance();
        VisibilityChecker<?> updated = ai.findAutoDetectVisibility(getAnnotatedClass(AutoDetectClass.class), checker);
        assertNotNull(updated);
        assertNotSame(checker, updated);

        VisibilityChecker<?> unchanged = ai.findAutoDetectVisibility(getAnnotatedClass(NoRootBean.class), checker);
        assertSame(checker, unchanged);
    }

    @JsonProperty("dummy")
    public static class DummyPropertyHolder {
        @JsonIgnore(true)
        public String ignoredField;

        @JsonIgnore(false)
        public String unignoredField;

        @JsonProperty(value = "namedProp", required = true, index = 4, defaultValue = "defVal",
                access = JsonProperty.Access.READ_ONLY)
        @JsonPropertyDescription("A property description")
        public String detailedField;

        @JsonProperty(value = "unknownIndexProp", index = JsonProperty.INDEX_UNKNOWN, defaultValue = "")
        public String indexUnknownField;

        @JsonFormat(pattern = "yyyy/MM/dd", shape = JsonFormat.Shape.STRING)
        public String formattedField;

        @JsonManagedReference("parent-child")
        public DummyPropertyHolder managedRef;

        @JsonBackReference("parent-child")
        public DummyPropertyHolder backRef;

        @JsonUnwrapped(enabled = true, prefix = "un_", suffix = "_wrap")
        public DummyPropertyHolder unwrappedProp;

        @JsonUnwrapped(enabled = false)
        public DummyPropertyHolder disabledUnwrapped;

        @JsonView({String.class, Integer.class})
        public String viewedField;

        @JacksonInject("customInjectId")
        public String explicitInjectField;

        @JacksonInject("")
        public Long defaultInjectField;

        @JacksonInject("")
        public void injectSetter(Integer val) { }

        @JacksonInject("")
        public Double injectGetterZeroArgs() { return 1.0; }
    }

    @Test(timeout = 4000)
    public void testMemberPropertyAnnotations() {
        JacksonAnnotationIntrospector ai = new JacksonAnnotationIntrospector();
        AnnotatedField fIgnored = getField(DummyPropertyHolder.class, "ignoredField");
        AnnotatedField fUnignored = getField(DummyPropertyHolder.class, "unignoredField");
        AnnotatedField fDetailed = getField(DummyPropertyHolder.class, "detailedField");
        AnnotatedField fUnknown = getField(DummyPropertyHolder.class, "indexUnknownField");

        assertTrue(ai.hasIgnoreMarker(fIgnored));
        assertFalse(ai.hasIgnoreMarker(fUnignored));
        assertNull(ai.findImplicitPropertyName(fDetailed));

        assertEquals(Boolean.TRUE, ai.hasRequiredMarker(fDetailed));
        assertNull(ai.hasRequiredMarker(fIgnored));

        assertEquals(JsonProperty.Access.READ_ONLY, ai.findPropertyAccess(fDetailed));
        assertNull(ai.findPropertyAccess(fIgnored));

        assertEquals("A property description", ai.findPropertyDescription(fDetailed));
        assertNull(ai.findPropertyDescription(fIgnored));

        assertEquals(Integer.valueOf(4), ai.findPropertyIndex(fDetailed));
        assertNull(ai.findPropertyIndex(fUnknown));
        assertNull(ai.findPropertyIndex(fIgnored));

        assertEquals("defVal", ai.findPropertyDefaultValue(fDetailed));
        assertNull(ai.findPropertyDefaultValue(fUnknown));
        assertNull(ai.findPropertyDefaultValue(fIgnored));
    }

    @Test(timeout = 4000)
    public void testFindFormat() {
        JacksonAnnotationIntrospector ai = new JacksonAnnotationIntrospector();
        AnnotatedField fFormatted = getField(DummyPropertyHolder.class, "formattedField");
        JsonFormat.Value fv = ai.findFormat(fFormatted);
        assertNotNull(fv);
        assertEquals("yyyy/MM/dd", fv.getPattern());
        assertEquals(JsonFormat.Shape.STRING, fv.getShape());

        AnnotatedField fIgnored = getField(DummyPropertyHolder.class, "ignoredField");
        assertNull(ai.findFormat(fIgnored));
    }

    @Test(timeout = 4000)
    public void testFindReferenceType() {
        JacksonAnnotationIntrospector ai = new JacksonAnnotationIntrospector();
        AnnotatedField fManaged = getField(DummyPropertyHolder.class, "managedRef");
        AnnotatedField fBack = getField(DummyPropertyHolder.class, "backRef");
        AnnotatedField fNone = getField(DummyPropertyHolder.class, "ignoredField");

        AnnotationIntrospector.ReferenceProperty refManaged = ai.findReferenceType(fManaged);
        assertNotNull(refManaged);
        assertTrue(refManaged.isManagedReference());
        assertEquals("parent-child", refManaged.getName());

        AnnotationIntrospector.ReferenceProperty refBack = ai.findReferenceType(fBack);
        assertNotNull(refBack);
        assertTrue(refBack.isBackReference());
        assertEquals("parent-child", refBack.getName());

        assertNull(ai.findReferenceType(fNone));
    }

    @Test(timeout = 4000)
    public void testFindUnwrappingNameTransformer() {
        JacksonAnnotationIntrospector ai = new JacksonAnnotationIntrospector();
        AnnotatedField fEnabled = getField(DummyPropertyHolder.class, "unwrappedProp");
        AnnotatedField fDisabled = getField(DummyPropertyHolder.class, "disabledUnwrapped");
        AnnotatedField fNone = getField(DummyPropertyHolder.class, "ignoredField");

        NameTransformer nt = ai.findUnwrappingNameTransformer(fEnabled);
        assertNotNull(nt);
        assertEquals("un_name_wrap", nt.transform("name"));

        assertNull(ai.findUnwrappingNameTransformer(fDisabled));
        assertNull(ai.findUnwrappingNameTransformer(fNone));
    }

    @Test(timeout = 4000)
    public void testFindInjectableValueId() {
        JacksonAnnotationIntrospector ai = new JacksonAnnotationIntrospector();
        AnnotatedField fExplicit = getField(DummyPropertyHolder.class, "explicitInjectField");
        assertEquals("customInjectId", ai.findInjectableValueId(fExplicit));

        AnnotatedField fDefault = getField(DummyPropertyHolder.class, "defaultInjectField");
        assertEquals(Long.class.getName(), ai.findInjectableValueId(fDefault));

        AnnotatedMethod mSetter = getMethod(DummyPropertyHolder.class, "injectSetter");
        assertEquals(Integer.class.getName(), ai.findInjectableValueId(mSetter));

        AnnotatedMethod mGetter = getMethod(DummyPropertyHolder.class, "injectGetterZeroArgs");
        assertEquals(Double.class.getName(), ai.findInjectableValueId(mGetter));

        AnnotatedField fNone = getField(DummyPropertyHolder.class, "ignoredField");
        assertNull(ai.findInjectableValueId(fNone));
    }

    @Test(timeout = 4000)
    public void testFindViews() {
        JacksonAnnotationIntrospector ai = new JacksonAnnotationIntrospector();
        AnnotatedField fViewed = getField(DummyPropertyHolder.class, "viewedField");
        Class<?>[] views = ai.findViews(fViewed);
        assertNotNull(views);
        assertEquals(2, views.length);
        assertEquals(String.class, views[0]);
        assertEquals(Integer.class, views[1]);

        AnnotatedField fNone = getField(DummyPropertyHolder.class, "ignoredField");
        assertNull(ai.findViews(fNone));
    }

    // =========================================================================
    // PARTITION A (Continued): POLYMORPHIC TYPE & OBJECT ID HANDLING
    // =========================================================================

    public static class CustomTypeResolverBuilder extends StdTypeResolverBuilder { }

    public static class CustomTypeIdResolver extends TypeIdResolverBase {
        public CustomTypeIdResolver() { }
        @Override
        public String idFromValue(Object value) { return "id"; }
        @Override
        public String idFromValueAndType(Object value, Class<?> suggestedType) { return "id"; }
        @Override
        public JsonTypeInfo.Id getMechanism() { return JsonTypeInfo.Id.CUSTOM; }
    }

    @JsonTypeResolver(CustomTypeResolverBuilder.class)
    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM)
    public static class CustomTypeResolverAnnotated { }

    @JsonTypeResolver(CustomTypeResolverBuilder.class)
    public static class MissingTypeInfoClass { }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
    public static class NoTypeInfoClass { }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
            defaultImpl = Void.class, property = "@typeExt")
    public static class ExternalPropertyClass { }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, defaultImpl = JsonTypeInfo.None.class)
    public static class NoneDefaultImplClass { }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, defaultImpl = JsonTypeInfo.class)
    public static class AnnotationDefaultImplClass { }

    @JsonTypeIdResolver(CustomTypeIdResolver.class)
    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM)
    public static class CustomIdResolverClass { }

    @JsonTypeName("baseNamedType")
    @JsonSubTypes({
        @JsonSubTypes.Type(value = DummySubA.class, name = "subA"),
        @JsonSubTypes.Type(value = DummySubB.class, name = "subB")
    })
    public static class BasePolyClass {
        @JsonTypeId
        public String typeIdField;
        public String normalField;
    }

    public static class DummySubA extends BasePolyClass { }
    public static class DummySubB extends BasePolyClass { }

    @Test(timeout = 4000)
    public void testFindTypeResolverBranches() {
        JacksonAnnotationIntrospector ai = new JacksonAnnotationIntrospector();
        ObjectMapper mapper = new ObjectMapper();
        MapperConfig<?> config = mapper.getSerializationConfig();
        JavaType baseType = mapper.constructType(Object.class);

        // Custom builder with type info
        AnnotatedClass acCustom = getAnnotatedClass(CustomTypeResolverAnnotated.class);
        TypeResolverBuilder<?> bCustom = ai.findTypeResolver(config, acCustom, baseType);
        assertNotNull(bCustom);

        // Custom builder missing type info -> returns null
        AnnotatedClass acMissing = getAnnotatedClass(MissingTypeInfoClass.class);
        assertNull(ai.findTypeResolver(config, acMissing, baseType));

        // Type info Id.NONE -> noTypeInfoBuilder
        AnnotatedClass acNone = getAnnotatedClass(NoTypeInfoClass.class);
        TypeResolverBuilder<?> bNone = ai.findTypeResolver(config, acNone, baseType);
        assertNotNull(bNone);

        // External property on AnnotatedClass -> mapped to PROPERTY
        AnnotatedClass acExt = getAnnotatedClass(ExternalPropertyClass.class);
        TypeResolverBuilder<?> bExt = ai.findTypeResolver(config, acExt, baseType);
        assertNotNull(bExt);
        assertEquals(Void.class, bExt.getDefaultImpl());

        // defaultImpl is None.class
        AnnotatedClass acNoneImpl = getAnnotatedClass(NoneDefaultImplClass.class);
        TypeResolverBuilder<?> bNoneImpl = ai.findTypeResolver(config, acNoneImpl, baseType);
        assertNotNull(bNoneImpl);
        assertNull(bNoneImpl.getDefaultImpl());

        // defaultImpl is an annotation class -> not set
        AnnotatedClass acAnnImpl = getAnnotatedClass(AnnotationDefaultImplClass.class);
        TypeResolverBuilder<?> bAnnImpl = ai.findTypeResolver(config, acAnnImpl, baseType);
        assertNotNull(bAnnImpl);
        assertNull(bAnnImpl.getDefaultImpl());

        // Custom TypeIdResolver
        AnnotatedClass acIdRes = getAnnotatedClass(CustomIdResolverClass.class);
        TypeResolverBuilder<?> bIdRes = ai.findTypeResolver(config, acIdRes, baseType);
        assertNotNull(bIdRes);
    }

    @Test(timeout = 4000)
    public void testSubTypesAndTypeName() {
        JacksonAnnotationIntrospector ai = new JacksonAnnotationIntrospector();
        AnnotatedClass ac = getAnnotatedClass(BasePolyClass.class);

        assertEquals("baseNamedType", ai.findTypeName(ac));
        assertNull(ai.findTypeName(getAnnotatedClass(NoRootBean.class)));

        List<NamedType> subtypes = ai.findSubtypes(ac);
        assertNotNull(subtypes);
        assertEquals(2, subtypes.size());
        assertEquals(DummySubA.class, subtypes.get(0).getType());
        assertEquals("subA", subtypes.get(0).getName());
        assertEquals(DummySubB.class, subtypes.get(1).getType());
        assertEquals("subB", subtypes.get(1).getName());

        assertNull(ai.findSubtypes(getAnnotatedClass(NoRootBean.class)));

        AnnotatedField fTypeId = getField(BasePolyClass.class, "typeIdField");
        AnnotatedField fNormal = getField(BasePolyClass.class, "normalField");
        assertEquals(Boolean.TRUE, ai.isTypeId(fTypeId));
        assertEquals(Boolean.FALSE, ai.isTypeId(fNormal));
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
    @JsonIdentityReference(alwaysAsId = true)
    public static class IdentityTarget { }

    @JsonIdentityInfo(generator = ObjectIdGenerators.None.class)
    public static class IdentityNoneTarget { }

    @Test(timeout = 4000)
    public void testObjectIdHandling() {
        JacksonAnnotationIntrospector ai = new JacksonAnnotationIntrospector();
        AnnotatedClass ac = getAnnotatedClass(IdentityTarget.class);
        ObjectIdInfo info = ai.findObjectIdInfo(ac);
        assertNotNull(info);
        assertEquals("@id", info.getPropertyName().getSimpleName());
        assertEquals(ObjectIdGenerators.IntSequenceGenerator.class, info.getGeneratorType());

        ObjectIdInfo refInfo = ai.findObjectReferenceInfo(ac, info);
        assertTrue(refInfo.getAlwaysAsId());

        AnnotatedClass acNone = getAnnotatedClass(IdentityNoneTarget.class);
        assertNull(ai.findObjectIdInfo(acNone));
        assertNull(ai.findObjectIdInfo(getAnnotatedClass(NoRootBean.class)));
    }

    // =========================================================================
    // PARTITION A (Continued): SERIALIZATION & DESERIALIZATION ANNOTATIONS
    // =========================================================================

    public static class DummySer extends JsonSerializer<Object> {
        @Override
        public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) { }
    }

    public static class DummyDeser extends JsonDeserializer<Object> {
        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) { return null; }
    }

    public static class DummyKeyDeser extends KeyDeserializer {
        @Override
        public Object deserializeKey(String key, DeserializationContext ctxt) { return key; }
    }

    public static class DummyConverter extends StdConverter<String, Integer> {
        @Override
        public Integer convert(String value) { return Integer.valueOf(value); }
    }

    public static class SerHolder {
        @JsonSerialize(using = DummySer.class, keyUsing = DummySer.class,
                contentUsing = DummySer.class, nullsUsing = DummySer.class,
                as = String.class, keyAs = Integer.class, contentAs = Long.class,
                typing = JsonSerialize.Typing.STATIC,
                converter = DummyConverter.class, contentConverter = DummyConverter.class)
        public Object fullSerField;

        @JsonSerialize(using = JsonSerializer.None.class, keyUsing = JsonSerializer.None.class,
                contentUsing = JsonSerializer.None.class, nullsUsing = JsonSerializer.None.class,
                as = Void.class, converter = Converter.None.class, contentConverter = Converter.None.class)
        public Object noneSerField;

        @JsonRawValue(true)
        public String rawField;

        @JsonRawValue(false)
        public String nonRawField;

        @JsonGetter("customGetterName")
        public String getCustomName() { return "name"; }

        @JsonProperty("regularPropName")
        public String getRegularProp() { return "reg"; }

        @JsonSerialize
        public String serializeNameOnlyField;

        @JsonView(String.class)
        public String viewNameOnlyField;

        @JsonValue(true)
        public String asValueMethod() { return "val"; }

        @JsonValue(false)
        public String notAsValueMethod() { return "notVal"; }
    }

    public static class DeserHolder {
        @JsonDeserialize(using = DummyDeser.class, keyUsing = DummyKeyDeser.class,
                contentUsing = DummyDeser.class, as = String.class, keyAs = Integer.class,
                contentAs = Long.class, converter = DummyConverter.class, contentConverter = DummyConverter.class)
        public Object fullDeserField;

        @JsonDeserialize(using = JsonDeserializer.None.class, keyUsing = KeyDeserializer.None.class,
                contentUsing = JsonDeserializer.None.class, as = Void.class,
                converter = Converter.None.class, contentConverter = Converter.None.class)
        public Object noneDeserField;

        @JsonSetter("customSetterName")
        public void setCustomName(String val) { }

        @JsonProperty("regularDeserProp")
        public void setRegularProp(String val) { }

        @JsonDeserialize
        public void deserOnlyProp(String val) { }

        @JsonAnySetter
        public void anySetter(String k, Object v) { }

        @JsonAnyGetter
        public java.util.Map<String, Object> anyGetter() { return null; }
    }

    @Test(timeout = 4000)
    public void testSerializationIntrospection() {
        JacksonAnnotationIntrospector ai = new JacksonAnnotationIntrospector();
        AnnotatedField fFull = getField(SerHolder.class, "fullSerField");
        AnnotatedField fNone = getField(SerHolder.class, "noneSerField");
        AnnotatedField fRaw = getField(SerHolder.class, "rawField");
        AnnotatedField fNonRaw = getField(SerHolder.class, "nonRawField");

        assertEquals(DummySer.class, ai.findSerializer(fFull));
        assertEquals(DummySer.class, ai.findKeySerializer(fFull));
        assertEquals(DummySer.class, ai.findContentSerializer(fFull));
        assertEquals(DummySer.class, ai.findNullSerializer(fFull));
        assertEquals(String.class, ai.findSerializationType(fFull));
        assertEquals(Integer.class, ai.findSerializationKeyType(fFull, null));
        assertEquals(Long.class, ai.findSerializationContentType(fFull, null));
        assertEquals(JsonSerialize.Typing.STATIC, ai.findSerializationTyping(fFull));
        assertEquals(DummyConverter.class, ai.findSerializationConverter(fFull));
        assertEquals(DummyConverter.class, ai.findSerializationContentConverter(fFull));

        assertNull(ai.findSerializer(fNone));
        assertNull(ai.findKeySerializer(fNone));
        assertNull(ai.findContentSerializer(fNone));
        assertNull(ai.findNullSerializer(fNone));
        assertNull(ai.findSerializationType(fNone));
        assertNull(ai.findSerializationConverter(fNone));
        assertNull(ai.findSerializationContentConverter(fNone));

        assertTrue(ai.findSerializer(fRaw) instanceof RawSerializer);
        assertNull(ai.findSerializer(fNonRaw));
    }

    @Test(timeout = 4000)
    public void testFindNameForSerialization() {
        JacksonAnnotationIntrospector ai = new JacksonAnnotationIntrospector();
        AnnotatedMethod mGetter = getMethod(SerHolder.class, "getCustomName");
        assertEquals("customGetterName", ai.findNameForSerialization(mGetter).getSimpleName());

        AnnotatedMethod mProp = getMethod(SerHolder.class, "getRegularProp");
        assertEquals("regularPropName", ai.findNameForSerialization(mProp).getSimpleName());

        AnnotatedField fSer = getField(SerHolder.class, "serializeNameOnlyField");
        assertEquals("", ai.findNameForSerialization(fSer).getSimpleName());

        AnnotatedField fView = getField(SerHolder.class, "viewNameOnlyField");
        assertEquals("", ai.findNameForSerialization(fView).getSimpleName());

        AnnotatedField fRaw = getField(SerHolder.class, "rawField");
        assertEquals("", ai.findNameForSerialization(fRaw).getSimpleName());

        AnnotatedField fNone = getField(DummyPropertyHolder.class, "ignoredField");
        assertNull(ai.findNameForSerialization(fNone));
    }

    @Test(timeout = 4000)
    public void testHasAsValueAnnotation() {
        JacksonAnnotationIntrospector ai = new JacksonAnnotationIntrospector();
        AnnotatedMethod mVal = getMethod(SerHolder.class, "asValueMethod");
        AnnotatedMethod mNotVal = getMethod(SerHolder.class, "notAsValueMethod");
        AnnotatedMethod mNone = getMethod(SerHolder.class, "getCustomName");

        assertTrue(ai.hasAsValueAnnotation(mVal));
        assertFalse(ai.hasAsValueAnnotation(mNotVal));
        assertFalse(ai.hasAsValueAnnotation(mNone));
    }

    @Test(timeout = 4000)
    public void testDeserializationIntrospection() {
        JacksonAnnotationIntrospector ai = new JacksonAnnotationIntrospector();
        AnnotatedField fFull = getField(DeserHolder.class, "fullDeserField");
        AnnotatedField fNone = getField(DeserHolder.class, "noneDeserField");

        assertEquals(DummyDeser.class, ai.findDeserializer(fFull));
        assertEquals(DummyKeyDeser.class, ai.findKeyDeserializer(fFull));
        assertEquals(DummyDeser.class, ai.findContentDeserializer(fFull));
        assertEquals(String.class, ai.findDeserializationType(fFull, null));
        assertEquals(Integer.class, ai.findDeserializationKeyType(fFull, null));
        assertEquals(Long.class, ai.findDeserializationContentType(fFull, null));
        assertEquals(DummyConverter.class, ai.findDeserializationConverter(fFull));
        assertEquals(DummyConverter.class, ai.findDeserializationContentConverter(fFull));

        assertNull(ai.findDeserializer(fNone));
        assertNull(ai.findKeyDeserializer(fNone));
        assertNull(ai.findContentDeserializer(fNone));
        assertNull(ai.findDeserializationType(fNone, null));
        assertNull(ai.findDeserializationConverter(fNone));
        assertNull(ai.findDeserializationContentConverter(fNone));
    }

    @Test(timeout = 4000)
    public void testFindNameForDeserialization() {
        JacksonAnnotationIntrospector ai = new JacksonAnnotationIntrospector();
        AnnotatedMethod mSetter = getMethod(DeserHolder.class, "setCustomName");
        assertEquals("customSetterName", ai.findNameForDeserialization(mSetter).getSimpleName());

        AnnotatedMethod mProp = getMethod(DeserHolder.class, "setRegularProp");
        assertEquals("regularDeserProp", ai.findNameForDeserialization(mProp).getSimpleName());

        AnnotatedMethod mDeser = getMethod(DeserHolder.class, "deserOnlyProp");
        assertEquals("", ai.findNameForDeserialization(mDeser).getSimpleName());

        AnnotatedField fView = getField(DummyPropertyHolder.class, "viewedField");
        assertEquals("", ai.findNameForDeserialization(fView).getSimpleName());

        AnnotatedField fUnwrapped = getField(DummyPropertyHolder.class, "unwrappedProp");
        assertEquals("", ai.findNameForDeserialization(fUnwrapped).getSimpleName());

        AnnotatedField fBack = getField(DummyPropertyHolder.class, "backRef");
        assertEquals("", ai.findNameForDeserialization(fBack).getSimpleName());

        AnnotatedField fManaged = getField(DummyPropertyHolder.class, "managedRef");
        assertEquals("", ai.findNameForDeserialization(fManaged).getSimpleName());

        AnnotatedField fNone = getField(DummyPropertyHolder.class, "ignoredField");
        assertNull(ai.findNameForDeserialization(fNone));
    }

    @Test(timeout = 4000)
    public void testAnyGetterAndSetter() {
        JacksonAnnotationIntrospector ai = new JacksonAnnotationIntrospector();
        AnnotatedMethod mAnySetter = getMethod(DeserHolder.class, "anySetter");
        AnnotatedMethod mAnyGetter = getMethod(DeserHolder.class, "anyGetter");
        AnnotatedMethod mNormal = getMethod(DeserHolder.class, "setCustomName");

        assertTrue(ai.hasAnySetterAnnotation(mAnySetter));
        assertFalse(ai.hasAnySetterAnnotation(mNormal));
        assertTrue(ai.hasAnyGetterAnnotation(mAnyGetter));
        assertFalse(ai.hasAnyGetterAnnotation(mNormal));
    }

    // =========================================================================
    // PARTITION A (Continued): INCLUSION, CREATORS, BUILDERS & VIRTUAL PROPS
    // =========================================================================

    public static class InclusionHolder {
        @JsonInclude(value = JsonInclude.Include.NON_EMPTY, content = JsonInclude.Include.NON_NULL)
        public String includeField;

        @SuppressWarnings("deprecation")
        @JsonSerialize(include = JsonSerialize.Inclusion.ALWAYS)
        public String alwaysField;

        @SuppressWarnings("deprecation")
        @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
        public String nonNullField;

        @SuppressWarnings("deprecation")
        @JsonSerialize(include = JsonSerialize.Inclusion.NON_DEFAULT)
        public String nonDefaultField;

        @SuppressWarnings("deprecation")
        @JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY)
        public String nonEmptyField;

        @SuppressWarnings("deprecation")
        @JsonSerialize(include = JsonSerialize.Inclusion.DEFAULT_INCLUSION)
        public String defaultInclusionField;
    }

    @Test(timeout = 4000)
    @SuppressWarnings("deprecation")
    public void testFindSerializationInclusion() {
        JacksonAnnotationIntrospector ai = new JacksonAnnotationIntrospector();
        AnnotatedField fInc = getField(InclusionHolder.class, "includeField");
        assertEquals(JsonInclude.Include.NON_EMPTY,
                ai.findSerializationInclusion(fInc, JsonInclude.Include.USE_DEFAULTS));
        assertEquals(JsonInclude.Include.NON_NULL,
                ai.findSerializationInclusionForContent(fInc, JsonInclude.Include.USE_DEFAULTS));

        AnnotatedField fAlways = getField(InclusionHolder.class, "alwaysField");
        assertEquals(JsonInclude.Include.ALWAYS,
                ai.findSerializationInclusion(fAlways, JsonInclude.Include.USE_DEFAULTS));

        AnnotatedField fNonNull = getField(InclusionHolder.class, "nonNullField");
        assertEquals(JsonInclude.Include.NON_NULL,
                ai.findSerializationInclusion(fNonNull, JsonInclude.Include.USE_DEFAULTS));

        AnnotatedField fNonDef = getField(InclusionHolder.class, "nonDefaultField");
        assertEquals(JsonInclude.Include.NON_DEFAULT,
                ai.findSerializationInclusion(fNonDef, JsonInclude.Include.USE_DEFAULTS));

        AnnotatedField fNonEmpty = getField(InclusionHolder.class, "nonEmptyField");
        assertEquals(JsonInclude.Include.NON_EMPTY,
                ai.findSerializationInclusion(fNonEmpty, JsonInclude.Include.USE_DEFAULTS));

        AnnotatedField fDefault = getField(InclusionHolder.class, "defaultInclusionField");
        assertEquals(JsonInclude.Include.USE_DEFAULTS,
                ai.findSerializationInclusion(fDefault, JsonInclude.Include.USE_DEFAULTS));

        AnnotatedField fNone = getField(DummyPropertyHolder.class, "ignoredField");
        assertEquals(JsonInclude.Include.USE_DEFAULTS,
                ai.findSerializationInclusion(fNone, JsonInclude.Include.USE_DEFAULTS));
        assertEquals(JsonInclude.Include.USE_DEFAULTS,
                ai.findSerializationInclusionForContent(fNone, JsonInclude.Include.USE_DEFAULTS));
    }

    public static class CreatorHolder {
        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        public CreatorHolder(String a) { }

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public static CreatorHolder createDelegating(int b) { return null; }

        @JsonCreator(mode = JsonCreator.Mode.DISABLED)
        public static CreatorHolder createDisabled(double c) { return null; }

        public static CreatorHolder createUnannotated(boolean d) { return null; }
    }

    @Test(timeout = 4000)
    public void testCreatorAnnotations() {
        JacksonAnnotationIntrospector ai = new JacksonAnnotationIntrospector();
        AnnotatedConstructor cProps = getConstructor(CreatorHolder.class);
        AnnotatedMethod mDelegating = getMethod(CreatorHolder.class, "createDelegating");
        AnnotatedMethod mDisabled = getMethod(CreatorHolder.class, "createDisabled");
        AnnotatedMethod mNone = getMethod(CreatorHolder.class, "createUnannotated");

        assertTrue(ai.hasCreatorAnnotation(cProps));
        assertEquals(JsonCreator.Mode.PROPERTIES, ai.findCreatorBinding(cProps));

        assertTrue(ai.hasCreatorAnnotation(mDelegating));
        assertEquals(JsonCreator.Mode.DELEGATING, ai.findCreatorBinding(mDelegating));

        assertFalse("Disabled mode creator must return false for hasCreatorAnnotation",
                ai.hasCreatorAnnotation(mDisabled));
        assertEquals(JsonCreator.Mode.DISABLED, ai.findCreatorBinding(mDisabled));

        assertFalse(ai.hasCreatorAnnotation(mNone));
        assertNull(ai.findCreatorBinding(mNone));
    }

    @JsonValueInstantiator(ValueInstantiator.class)
    @JsonDeserialize(builder = BuilderClass.class)
    public static class InstantiatorAndBuilderClass { }

    @JsonPOJOBuilder(buildMethodName = "construct", withPrefix = "using")
    public static class BuilderClass { }

    @Test(timeout = 4000)
    public void testInstantiatorAndBuilderIntrospection() {
        JacksonAnnotationIntrospector ai = new JacksonAnnotationIntrospector();
        AnnotatedClass ac = getAnnotatedClass(InstantiatorAndBuilderClass.class);
        assertEquals(ValueInstantiator.class, ai.findValueInstantiator(ac));
        assertEquals(BuilderClass.class, ai.findPO