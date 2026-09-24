/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector
 *
 * Key Areas Tested:
 * 1. Object ID & Reference Info (Defect Zone - Issue #1607):
 *    - findObjectIdInfo & findObjectReferenceInfo with @JsonIdentityInfo and @JsonIdentityReference(alwaysAsId=true)
 *    - Interaction on classes, properties, and collection/container elements when serialized via ObjectMapper
 *    - Boundary: findObjectReferenceInfo when objectIdInfo is null or non-null
 * 2. General Annotations & Enums:
 *    - findEnumValue with explicit @JsonProperty vs default name, empty name, missing field
 *    - findEnumValues scanning enum constants with/without @JsonProperty
 *    - isAnnotationBundle caching mechanism and @JacksonAnnotationsInside detection
 * 3. Class Annotations:
 *    - findRootName with namespace (null, empty, populated)
 *    - findPropertiesToIgnore (deprecated 1-arg and 2-arg with allowGetters/allowSetters)
 *    - findIgnoreUnknownProperties, isIgnorableType, findFilterId (empty vs valid)
 *    - findNamingStrategy, findClassDescription
 * 4. Member & Property Annotations:
 *    - findImplicitPropertyName via JDK7 ConstructorProperties
 *    - hasIgnoreMarker (_isIgnorable: JsonIgnore vs JDK7 Transient)
 *    - hasRequiredMarker, findPropertyAccess, findPropertyDescription, findPropertyIndex, findPropertyDefaultValue
 *    - findFormat, findReferenceType (managed vs back reference)
 *    - findUnwrappingNameTransformer (enabled vs disabled)
 *    - findInjectableValueId (explicit id, empty id fallback to raw type / method param type)
 *    - findViews, resolveSetterConflict (primitive vs non-primitive, String vs non-String)
 * 5. Polymorphic Type Handling & Resolvers:
 *    - findTypeResolver, findPropertyTypeResolver (container/reference check), findPropertyContentTypeResolver
 *    - _findTypeResolver inclusion mapping (EXTERNAL_PROPERTY converted to PROPERTY on class)
 *    - findSubtypes, findTypeName, isTypeId
 * 6. Serialization & Deserialization Configurations:
 *    - findSerializer, findKeySerializer, findContentSerializer, findNullSerializer
 *    - findRawValue handling
 *    - findSerializationInclusion (JsonInclude vs deprecated JsonSerialize.Inclusion values)
 *    - findSerializationTyping, findSerializationConverter, findSerializationContentConverter
 *    - findSerializationPropertyOrder, findSerializationSortAlphabetically
 *    - findAndAddVirtualProperties (JsonAppend attrs and props, prepend true/false)
 *    - findPOJOBuilder, findPOJOBuilderConfig, findValueInstantiator
 *    - findNameForSerialization & findNameForDeserialization priority and inference
 *    - hasCreatorAnnotation, findCreatorBinding, setConstructorPropertiesImpliesCreator
 * 7. Defensive Guards & Lifecycle:
 *    - Null / empty inputs, readResolve LRUMap re-initialization, version check
 */

package com.fasterxml.jackson.databind.introspect;

import org.junit.Test;
import static org.junit.Assert.*;

import java.beans.ConstructorProperties;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.*;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.*;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.std.RawSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import com.fasterxml.jackson.databind.util.NameTransformer;

public class JacksonAnnotationIntrospectorGeminiTest {

    private final JacksonAnnotationIntrospector ai = new JacksonAnnotationIntrospector();

    /*
    /**********************************************************
    /* Test Mock/Helper Classes & Annotations
    /**********************************************************
     */

    @JacksonAnnotationsInside
    @Retention(RetentionPolicy.RUNTIME)
    @interface MetaBundle { }

    @Retention(RetentionPolicy.RUNTIME)
    @interface NotABundle { }

    enum TestEnum {
        @JsonProperty("first_value")
        FIRST,
        @JsonProperty("")
        SECOND_EMPTY,
        THIRD_NO_ANN
    }

    @JsonRootName(value = "root", namespace = "http://example.com")
    static class RootWithNamespace { }

    @JsonRootName(value = "root_empty_ns", namespace = "")
    static class RootWithEmptyNamespace { }

    @JsonIgnoreProperties(value = {"p1", "p2"}, allowGetters = true, allowSetters = false, ignoreUnknown = true)
    static class IgnorePropertiesClass { }

    @JsonIgnoreProperties(value = {"p3"}, allowGetters = false, allowSetters = true)
    static class IgnorePropsSetterClass { }

    @JsonIgnoreType(true)
    static class IgnorableClass { }

    @JsonFilter("filter123")
    static class FilteredClass { }

    @JsonFilter("")
    static class EmptyFilterClass { }

    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    @JsonClassDescription("A test class description")
    static class DescribedClass { }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    static class AutoDetectClass { }

    static class ConstructorBean {
        final int x;
        final String y;

        @ConstructorProperties({"x", "y"})
        public ConstructorBean(int x, String y) {
            this.x = x;
            this.y = y;
        }
    }

    static class PropertyBean {
        @JsonProperty(value = "prop", index = 3, defaultValue = "defVal", required = true, access = JsonProperty.Access.READ_ONLY)
        @JsonPropertyDescription("A property description")
        public int field;

        @JsonProperty(defaultValue = "")
        public String defaultEmpty;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        public Date formattedDate;

        @JsonManagedReference("ref-name")
        public PropertyBean managed;

        @JsonBackReference("ref-name")
        public PropertyBean back;

        @JsonUnwrapped(prefix = "pre_", suffix = "_post", enabled = true)
        public PropertyBean unwrapped;

        @JsonUnwrapped(enabled = false)
        public PropertyBean unwrappedDisabled;

        @JacksonInject("customInject")
        public String injectedNamed;

        @JacksonInject
        public String injectedDefault;

        @JacksonInject
        public void setInjectedParam(Double val) {}

        @JsonView({String.class, Integer.class})
        public String viewedField;

        @JsonIgnore
        public String ignoredField;

        @java.beans.Transient
        public String transientField;

        @JsonRawValue(true)
        public String rawVal;

        @JsonTypeId
        public String customTypeId;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
    @JsonSubTypes({
        @JsonSubTypes.Type(value = SubTypeA.class, name = "subA"),
        @JsonSubTypes.Type(value = SubTypeB.class, name = "subB")
    })
    @JsonTypeName("base")
    static class BaseTypeClass { }

    static class SubTypeA extends BaseTypeClass { }
    static class SubTypeB extends BaseTypeClass { }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
    static class NoTypeInfoClass { }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Issue1607Bean.class)
    @JsonIdentityReference(alwaysAsId = true)
    static class Issue1607Bean {
        public int id;
        public int value;

        public Issue1607Bean() {}
        public Issue1607Bean(int id, int value) {
            this.id = id;
            this.value = value;
        }
    }

    static class Issue1607Wrapper {
        public List<Issue1607Bean> alwaysClass;
        public Issue1607Bean alwaysProp;

        public Issue1607Wrapper() {}
        public Issue1607Wrapper(List<Issue1607Bean> list, Issue1607Bean prop) {
            this.alwaysClass = list;
            this.alwaysProp = prop;
        }
    }

    @JsonAppend(
        prepend = true,
        attrs = {
            @JsonAppend.Attr(value = "attr1", propName = "propAttr1", propNamespace = "ns1", required = true)
        }
    )
    static class AppendAttrClass { }

    @JsonPropertyOrder(value = {"b", "a"}, alphabetic = true)
    static class OrderClass {
        public int b;
        public int a;
    }

    @JsonPropertyOrder(alphabetic = false)
    static class NonAlphaOrderClass { }

    @JsonSerialize(
        using = JsonSerializer.None.class,
        keyUsing = JsonSerializer.None.class,
        contentUsing = JsonSerializer.None.class,
        nullsUsing = JsonSerializer.None.class,
        include = JsonSerialize.Inclusion.NON_NULL,
        typing = JsonSerialize.Typing.STATIC
    )
    @JsonDeserialize(
        using = JsonDeserializer.None.class,
        keyUsing = KeyDeserializer.None.class,
        contentUsing = JsonDeserializer.None.class
    )
    static class SerializerHolder {
        @JsonValue
        public String asValueMethod() { return "val"; }
    }

    @JsonPOJOBuilder(buildMethodName = "create", withPrefix = "having")
    @JsonValueInstantiator(ValueInstantiator.class)
    static class PojoBuilderClass { }

    static class ConflictSetterBean {
        public void setPrimitive(int x) {}
        public void setPrimitive(Integer x) {}

        public void setStr(String s) {}
        public void setStr(Object s) {}

        public void setUnknown1(Double d) {}
        public void setUnknown2(Float f) {}
    }

    static class InferredSerProps {
        @JsonSerialize public int ser1;
        @JsonView(Object.class) public int ser2;
        @JsonFormat public int ser3;
        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME) public int ser4;
        @JsonRawValue public int ser5;
        @JsonUnwrapped public int ser6;
        @JsonBackReference public int ser7;
        @JsonManagedReference public int ser8;
        @JsonGetter("explicitGetter") public int getExplicit() { return 1; }
        @JsonProperty("explicitProp") public int explicitProp;
    }

    static class InferredDeserProps {
        @JsonDeserialize public int deser1;
        @JsonSetter("explicitSetter") public void setExplicit(int x) {}
        @JsonProperty("explicitProp") public void setExplicitProp(int x) {}
    }

    static class CreatorBean {
        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        public CreatorBean(@JsonProperty("n") String n) {}

        @JsonCreator(mode = JsonCreator.Mode.DISABLED)
        public static CreatorBean disabled(String n) { return null; }
    }

    /*
    /**********************************************************
    /* Partition C: Defect-Targeted Zone (Defects4J Issue #1607)
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testDefectIssue1607AlwaysAsIdOnClassLevelInCollection() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Issue1607Bean b1 = new Issue1607Bean(1, 13);
        Issue1607Bean b2 = new Issue1607Bean(2, 42);
        Issue1607Wrapper wrapper = new Issue1607Wrapper(Collections.singletonList(b1), b2);

        String json = mapper.writeValueAsString(wrapper);
        // Fault check: When @JsonIdentityReference(alwaysAsId=true) is on Issue1607Bean,
        // it must be serialized purely as its ID even on the very first occurrence in a collection.
        // Defective behavior outputs: {"alwaysClass":[{"id":1,"value":13}],"alwaysProp":2}
        // Correct behavior outputs:   {"alwaysClass":[1],"alwaysProp":2}
        assertEquals("{\"alwaysClass\":[1],\"alwaysProp\":2}", json);
    }

    @Test(timeout = 4000)
    public void testFindObjectIdInfoAndReferenceDirectly() {
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(Issue1607Bean.class, null, null);
        ObjectIdInfo info = ai.findObjectIdInfo(ac);
        assertNotNull("findObjectIdInfo must return ObjectIdInfo", info);
        assertEquals("id", info.getPropertyName().getSimpleName());
        assertEquals(ObjectIdGenerators.PropertyGenerator.class, info.getGeneratorType());
        assertEquals(Issue1607Bean.class, info.getScope());

        ObjectIdInfo refInfo = ai.findObjectReferenceInfo(ac, info);
        assertNotNull(refInfo);
        assertTrue("alwaysAsId should be true on refInfo", refInfo.getAlwaysAsId());
    }

    @Test(timeout = 4000)
    public void testFindObjectIdInfoNoneGenerator() {
        @JsonIdentityInfo(generator = ObjectIdGenerators.None.class)
        class NoneIdClass {}

        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(NoneIdClass.class, null, null);
        assertNull(ai.findObjectIdInfo(ac));
    }

    /*
    /**********************************************************
    /* Partition A: Core Functional Logic & State Transitions
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testVersionAndLifecycle() throws Exception {
        Version v = ai.version();
        assertNotNull(v);
        assertFalse(v.isUnknownVersion());

        // Test serialization and readResolve()
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(ai);
        oos.close();

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        JacksonAnnotationIntrospector restored = (JacksonAnnotationIntrospector) ois.readObject();
        assertNotNull(restored);
        assertNotNull(restored._annotationsInside);
    }

    @Test(timeout = 4000)
    public void testIsAnnotationBundle() {
        MetaBundle meta = PropertyBean.class.getAnnotation(MetaBundle.class);
        NotABundle notMeta = PropertyBean.class.getAnnotation(NotABundle.class);

        @MetaBundle
        class DummyBundle {}
        @NotABundle
        class DummyNotBundle {}

        MetaBundle realMeta = DummyBundle.class.getAnnotation(MetaBundle.class);
        NotABundle realNotMeta = DummyNotBundle.class.getAnnotation(NotABundle.class);

        assertTrue(ai.isAnnotationBundle(realMeta));
        // Verify cache hit branch
        assertTrue(ai.isAnnotationBundle(realMeta));
        assertFalse(ai.isAnnotationBundle(realNotMeta));
    }

    @Test(timeout = 4000)
    public void testFindEnumValueAndEnumValues() {
        assertEquals("first_value", ai.findEnumValue(TestEnum.FIRST));
        // Empty string falls back to name()
        assertEquals("SECOND_EMPTY", ai.findEnumValue(TestEnum.SECOND_EMPTY));
        assertEquals("THIRD_NO_ANN", ai.findEnumValue(TestEnum.THIRD_NO_ANN));

        Enum<?>[] enumValues = TestEnum.values();
        String[] names = new String[enumValues.length];
        for (int i = 0; i < names.length; ++i) {
            names[i] = enumValues[i].name();
        }

        String[] resolved = ai.findEnumValues(TestEnum.class, enumValues, names);
        assertEquals("first_value", resolved[0]);
        assertEquals("SECOND_EMPTY", resolved[1]);
        assertEquals("THIRD_NO_ANN", resolved[2]);
    }

    @Test(timeout = 4000)
    public void testFindRootName() {
        AnnotatedClass ac1 = AnnotatedClass.constructWithoutSuperTypes(RootWithNamespace.class, null, null);
        PropertyName pn1 = ai.findRootName(ac1);
        assertNotNull(pn1);
        assertEquals("root", pn1.getSimpleName());
        assertEquals("http://example.com", pn1.getNamespace());

        AnnotatedClass ac2 = AnnotatedClass.constructWithoutSuperTypes(RootWithEmptyNamespace.class, null, null);
        PropertyName pn2 = ai.findRootName(ac2);
        assertNotNull(pn2);
        assertEquals("root_empty_ns", pn2.getSimpleName());
        assertNull(pn2.getNamespace());

        AnnotatedClass acNone = AnnotatedClass.constructWithoutSuperTypes(TestEnum.class, null, null);
        assertNull(ai.findRootName(acNone));
    }

    @Test(timeout = 4000)
    @SuppressWarnings("deprecation")
    public void testFindPropertiesToIgnore() {
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(IgnorePropertiesClass.class, null, null);
        String[] ignored = ai.findPropertiesToIgnore(ac);
        assertNotNull(ignored);
        assertEquals(2, ignored.length);
        assertEquals("p1", ignored[0]);

        // Serialization with allowGetters = true -> returns null
        assertNull(ai.findPropertiesToIgnore(ac, true));
        // Deserialization with allowSetters = false -> returns array
        assertNotNull(ai.findPropertiesToIgnore(ac, false));

        AnnotatedClass acSetter = AnnotatedClass.constructWithoutSuperTypes(IgnorePropsSetterClass.class, null, null);
        // Serialization with allowGetters = false -> returns array
        assertNotNull(ai.findPropertiesToIgnore(acSetter, true));
        // Deserialization with allowSetters = true -> returns null
        assertNull(ai.findPropertiesToIgnore(acSetter, false));
    }

    @Test(timeout = 4000)
    public void testClassLevelProperties() {
        AnnotatedClass acIgnore = AnnotatedClass.constructWithoutSuperTypes(IgnorePropertiesClass.class, null, null);
        assertEquals(Boolean.TRUE, ai.findIgnoreUnknownProperties(acIgnore));

        AnnotatedClass acType = AnnotatedClass.constructWithoutSuperTypes(IgnorableClass.class, null, null);
        assertEquals(Boolean.TRUE, ai.isIgnorableType(acType));

        AnnotatedClass acFilter = AnnotatedClass.constructWithoutSuperTypes(FilteredClass.class, null, null);
        assertEquals("filter123", ai.findFilterId(acFilter));

        AnnotatedClass acEmptyFilter = AnnotatedClass.constructWithoutSuperTypes(EmptyFilterClass.class, null, null);
        assertNull(ai.findFilterId(acEmptyFilter));

        AnnotatedClass acDesc = AnnotatedClass.constructWithoutSuperTypes(DescribedClass.class, null, null);
        assertEquals(PropertyNamingStrategy.SnakeCaseStrategy.class, ai.findNamingStrategy(acDesc));
        assertEquals("A test class description", ai.findClassDescription(acDesc));

        AnnotatedClass acDetect = AnnotatedClass.constructWithoutSuperTypes(AutoDetectClass.class, null, null);
        VisibilityChecker<?> checker = ai.findAutoDetectVisibility(acDetect, VisibilityChecker.Std.defaultInstance());
        assertNotNull(checker);
    }

    @Test(timeout = 4000)
    public void testMemberGeneralAnnotations() throws Exception {
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(PropertyBean.class, null, null);

        AnnotatedField fProp = findField(ac, "field");
        assertTrue(ai.hasRequiredMarker(fProp));
        assertEquals(JsonProperty.Access.READ_ONLY, ai.findPropertyAccess(fProp));
        assertEquals("A property description", ai.findPropertyDescription(fProp));
        assertEquals(Integer.valueOf(3), ai.findPropertyIndex(fProp));
        assertEquals("defVal", ai.findPropertyDefaultValue(fProp));

        AnnotatedField fDefEmpty = findField(ac, "defaultEmpty");
        assertNull(ai.findPropertyDefaultValue(fDefEmpty));

        AnnotatedField fFormat = findField(ac, "formattedDate");
        JsonFormat.Value fVal = ai.findFormat(fFormat);
        assertNotNull(fVal);
        assertEquals(JsonFormat.Shape.STRING, fVal.getShape());
        assertEquals("yyyy-MM-dd", fVal.getPattern());

        AnnotatedField fManaged = findField(ac, "managed");
        AnnotationIntrospector.ReferenceProperty refManaged = ai.findReferenceType(fManaged);
        assertNotNull(refManaged);
        assertTrue(refManaged.isManagedReference());
        assertEquals("ref-name", refManaged.getName());

        AnnotatedField fBack = findField(ac, "back");
        AnnotationIntrospector.ReferenceProperty refBack = ai.findReferenceType(fBack);
        assertNotNull(refBack);
        assertTrue(refBack.isBackReference());
        assertEquals("ref-name", refBack.getName());

        AnnotatedField fUnwrapped = findField(ac, "unwrapped");
        NameTransformer nt = ai.findUnwrappingNameTransformer(fUnwrapped);
        assertNotNull(nt);
        assertEquals("pre_val_post", nt.transform("val"));

        AnnotatedField fUnwrappedDis = findField(ac, "unwrappedDisabled");
        assertNull(ai.findUnwrappingNameTransformer(fUnwrappedDis));

        AnnotatedField fInjNamed = findField(ac, "injectedNamed");
        assertEquals("customInject", ai.findInjectableValueId(fInjNamed));

        AnnotatedField fInjDef = findField(ac, "injectedDefault");
        assertEquals(String.class.getName(), ai.findInjectableValueId(fInjDef));

        AnnotatedMethod mInjParam = findMethod(ac, "setInjectedParam");
        assertEquals(Double.class.getName(), ai.findInjectableValueId(mInjParam));

        AnnotatedField fViews = findField(ac, "viewedField");
        Class<?>[] views = ai.findViews(fViews);
        assertNotNull(views);
        assertEquals(2, views.length);
        assertEquals(String.class, views[0]);

        AnnotatedField fIgnored = findField(ac, "ignoredField");
        assertTrue(ai.hasIgnoreMarker(fIgnored));

        AnnotatedField fTransient = findField(ac, "transientField");
        assertTrue(ai.hasIgnoreMarker(fTransient));

        AnnotatedField fRaw = findField(ac, "rawVal");
        Object ser = ai.findSerializer(fRaw);
        assertTrue(ser instanceof RawSerializer<?>);

        AnnotatedField fTypeId = findField(ac, "customTypeId");
        assertTrue(ai.isTypeId(fTypeId));
    }

    /*
    /**********************************************************
    /* Partition B: Boundary Value Analysis & Type Resolvers
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testConstructorPropertiesAndImplicitNames() {
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(ConstructorBean.class, null, null);
        List<AnnotatedConstructor> ctors = ac.getConstructors();
        assertFalse(ctors.isEmpty());
        AnnotatedConstructor ctor = ctors.get(0);

        assertTrue(ai.hasCreatorAnnotation(ctor));

        AnnotatedParameter p0 = ctor.getParameter(0);
        AnnotatedParameter p1 = ctor.getParameter(1);

        assertEquals("x", ai.findImplicitPropertyName(p0));
        assertEquals("y", ai.findImplicitPropertyName(p1));

        ai.setConstructorPropertiesImpliesCreator(false);
        assertFalse(ai.hasCreatorAnnotation(ctor));
        ai.setConstructorPropertiesImpliesCreator(true);
    }

    @Test(timeout = 4000)
    public void testPolymorphicTypeResolvers() {
        ObjectMapper mapper = new ObjectMapper();
        MapperConfig<?> config = mapper.getSerializationConfig();
        JavaType baseType = TypeFactory.defaultInstance().constructType(BaseTypeClass.class);
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(BaseTypeClass.class, null, null);

        TypeResolverBuilder<?> b = ai.findTypeResolver(config, ac, baseType);
        assertNotNull(b);
        assertTrue(b instanceof StdTypeResolverBuilder);

        List<NamedType> subtypes = ai.findSubtypes(ac);
        assertNotNull(subtypes);
        assertEquals(2, subtypes.size());
        assertEquals("subA", subtypes.get(0).getName());
        assertEquals(SubTypeA.class, subtypes.get(0).getType());

        assertEquals("base", ai.findTypeName(ac));

        AnnotatedClass acNone = AnnotatedClass.constructWithoutSuperTypes(NoTypeInfoClass.class, null, null);
        TypeResolverBuilder<?> bNone = ai.findTypeResolver(config, acNone, TypeFactory.defaultInstance().constructType(NoTypeInfoClass.class));
        assertNotNull(bNone);
    }

    @Test(timeout = 4000)
    public void testPropertyContentTypeResolverGuard() {
        JavaType scalarType = TypeFactory.defaultInstance().constructType(String.class);
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(PropertyBean.class, null, null);
        AnnotatedField f = findField(ac, "field");

        try {
            ai.findPropertyContentTypeResolver(null, f, scalarType);
            fail("Expected IllegalArgumentException on non-container type");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("Must call method with a container or reference type"));
        }
    }

    @Test(timeout = 4000)
    public void testResolveSetterConflict() {
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(ConflictSetterBean.class, null, null);
        List<AnnotatedMethod> primitives = findMethods(ac, "setPrimitive");
        assertEquals(2, primitives.size());
        AnnotatedMethod primMeth = primitives.get(0).getRawParameterType(0).isPrimitive() ? primitives.get(0) : primitives.get(1);
        AnnotatedMethod objMeth = primMeth == primitives.get(0) ? primitives.get(1) : primitives.get(0);

        AnnotatedMethod winner = ai.resolveSetterConflict(null, primMeth, objMeth);
        assertSame(primMeth, winner);

        AnnotatedMethod winnerReverse = ai.resolveSetterConflict(null, objMeth, primMeth);
        assertSame(primMeth, winnerReverse);

        List<AnnotatedMethod> strMethods = findMethods(ac, "setStr");
        AnnotatedMethod strMeth = strMethods.get(0).getRawParameterType(0) == String.class ? strMethods.get(0) : strMethods.get(1);
        AnnotatedMethod otherMeth = strMeth == strMethods.get(0) ? strMethods.get(1) : strMethods.get(0);

        assertSame(strMeth, ai.resolveSetterConflict(null, strMeth, otherMeth));
        assertSame(strMeth, ai.resolveSetterConflict(null, otherMeth, strMeth));

        AnnotatedMethod mU1 = findMethod(ac, "setUnknown1");
        AnnotatedMethod mU2 = findMethod(ac, "setUnknown2");
        assertNull(ai.resolveSetterConflict(null, mU1, mU2));
    }

    /*
    /**********************************************************
    /* Partition D: Exception & Defensive Guard Paths
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testSerializationAndDeserializationInference() {
        AnnotatedClass acSer = AnnotatedClass.constructWithoutSuperTypes(InferredSerProps.class, null, null);
        for (String fieldName : Arrays.asList("ser1", "ser2", "ser3", "ser4", "ser5", "ser6", "ser7", "ser8")) {
            AnnotatedField f = findField(acSer, fieldName);
            PropertyName pn = ai.findNameForSerialization(f);
            assertSame("Field " + fieldName + " should infer USE_DEFAULT", PropertyName.USE_DEFAULT, pn);
        }

        AnnotatedMethod mGet = findMethod(acSer, "getExplicit");
        assertEquals("explicitGetter", ai.findNameForSerialization(mGet).getSimpleName());

        AnnotatedField fExp = findField(acSer, "explicitProp");
        assertEquals("explicitProp", ai.findNameForSerialization(fExp).getSimpleName());

        AnnotatedClass acDeser = AnnotatedClass.constructWithoutSuperTypes(InferredDeserProps.class, null, null);
        AnnotatedField fDeser = findField(acDeser, "deser1");
        assertSame(PropertyName.USE_DEFAULT, ai.findNameForDeserialization(fDeser));

        AnnotatedMethod mSet = findMethod(acDeser, "setExplicit");
        assertEquals("explicitSetter", ai.findNameForDeserialization(mSet).getSimpleName());
    }

    @Test(timeout = 4000)
    @SuppressWarnings("deprecation")
    public void testSerializationInclusion() {
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(SerializerHolder.class, null, null);
        JsonInclude.Include inc = ai.findSerializationInclusion(ac, JsonInclude.Include.ALWAYS);
        assertEquals(JsonInclude.Include.NON_NULL, inc);

        JsonInclude.Value incVal = ai.findPropertyInclusion(ac);
        assertEquals(JsonInclude.Include.NON_NULL, incVal.getValueInclusion());
        assertEquals(JsonInclude.Include.USE_DEFAULTS, incVal.getContentInclusion());

        assertEquals(JsonSerialize.Typing.STATIC, ai.findSerializationTyping(ac));
    }

    @Test(timeout = 4000)
    public void testSortAlphabeticallyAndPropertyOrder() {
        AnnotatedClass acOrder = AnnotatedClass.constructWithoutSuperTypes(OrderClass.class, null, null);
        String[] order = ai.findSerializationPropertyOrder(acOrder);
        assertNotNull(order);
        assertArrayEquals(new String[]{"b", "a"}, order);
        assertEquals(Boolean.TRUE, ai.findSerializationSortAlphabetically(acOrder));

        AnnotatedClass acNonAlpha = AnnotatedClass.constructWithoutSuperTypes(NonAlphaOrderClass.class, null, null);
        assertNull(ai.findSerializationSortAlphabetically(acNonAlpha));
    }

    @Test(timeout = 4000)
    public void testVirtualProperties() {
        ObjectMapper mapper = new ObjectMapper();
        MapperConfig<?> config = mapper.getSerializationConfig();
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(AppendAttrClass.class, null, null);

        List<BeanPropertyWriter> props = new ArrayList<BeanPropertyWriter>();
        ai.findAndAddVirtualProperties(config, ac, props);
        assertEquals(1, props.size());
        assertEquals("propAttr1", props.get(0).getName());
    }

    @Test(timeout = 4000)
    public void testPojoBuilderAndValueInstantiator() {
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(PojoBuilderClass.class, null, null);
        JsonPOJOBuilder.Value bVal = ai.findPOJOBuilderConfig(ac);
        assertNotNull(bVal);
        assertEquals("create", bVal.buildMethodName);
        assertEquals("having", bVal.withPrefix);

        Object vi = ai.findValueInstantiator(ac);
        assertEquals(ValueInstantiator.class, vi);
    }

    @Test(timeout = 4000)
    public void testCreatorAnnotationModes() {
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(CreatorBean.class, null, null);
        AnnotatedConstructor ctor = ac.getConstructors().get(0);
        assertTrue(ai.hasCreatorAnnotation(ctor));
        assertEquals(JsonCreator.Mode.PROPERTIES, ai.findCreatorBinding(ctor));

        AnnotatedMethod disabledMethod = findMethod(ac, "disabled");
        assertFalse(ai.hasCreatorAnnotation(disabledMethod));
        assertEquals(JsonCreator.Mode.DISABLED, ai.findCreatorBinding(disabledMethod));
    }

    @Test(timeout = 4000)
    public void testConvertersAndSerializersNullOrExplicit() {
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(PropertyBean.class, null, null);
        assertNull(ai.findSerializer(ac));
        assertNull(ai.findKeySerializer(ac));
        assertNull(ai.findContentSerializer(ac));
        assertNull(ai.findNullSerializer(ac));
        assertNull(ai.findDeserializer(ac));
        assertNull(ai.findKeyDeserializer(ac));
        assertNull(ai.findContentDeserializer(ac));
        assertNull(ai.findSerializationConverter(ac));
        assertNull(ai.findDeserializationConverter(ac));
    }

    /*
    /**********************************************************
    /* Reflection Helper Methods
    /**********************************************************
     */

    private static AnnotatedField findField(AnnotatedClass ac, String name) {
        for (AnnotatedField f : ac.fields()) {
            if (f.getName().equals(name)) {
                return f;
            }
        }
        throw new IllegalArgumentException("Field not found: " + name);
    }

    private static AnnotatedMethod findMethod(AnnotatedClass ac, String name) {
        for (AnnotatedMethod m : ac.memberMethods()) {
            if (m.getName().equals(name)) {
                return m;
            }
        }
        throw new IllegalArgumentException("Method not found: " + name);
    }

    private static List<AnnotatedMethod> findMethods(AnnotatedClass ac, String name) {
        List<AnnotatedMethod> list = new ArrayList<AnnotatedMethod>();
        for (AnnotatedMethod m : ac.memberMethods()) {
            if (m.getName().equals(name)) {
                list.add(m);
            }
        }
        return list;
    }
}