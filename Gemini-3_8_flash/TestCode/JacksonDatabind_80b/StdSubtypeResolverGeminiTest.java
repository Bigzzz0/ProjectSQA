package com.fasterxml.jackson.databind.jsontype.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedClassResolver;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.jsontype.NamedType;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * 1. Defect-Targeted Branch Zone (databind#1616 / Defects4J TestTypeNames::testBaseTypeId1616):
 *    - In `_combineNamedAndUnnamed`: Check exclusion of abstract root base types without explicit names
 *      when `(cls == rawBase) && Modifier.isAbstract(cls.getModifiers())`.
 *    - In the defective version, an abstract base type was erroneously added as an unnamed NamedType(Base.class),
 *      causing a downstream NullPointerException during type ID resolution and deserialization.
 *
 * 2. Resolution by Class (Serialization) - collectAndResolveSubtypesByClass:
 *    - `baseType == null` (for backwards compatibility fallback to `property.getRawType()`) vs `baseType != null`.
 *    - `_registeredSubtypes == null` vs `_registeredSubtypes != null`.
 *    - `rawBase.isAssignableFrom(subtype.getType())`: true (is subtype) vs false (disjoint type).
 *    - Annotated property subtypes (`st != null` vs `st == null`).
 *    - Recursive subtypes from annotations on base type and subtypes.
 *    - Conflict resolution in `_collectAndResolve`: subtype already present with/without name.
 *
 * 3. Resolution by Type ID (Deserialization) - collectAndResolveSubtypesByTypeId:
 *    - Overload with `(MapperConfig, AnnotatedMember, JavaType)` and `(MapperConfig, AnnotatedClass)`.
 *    - Explicitly named base type (`@JsonTypeName`) vs unnamed base type.
 *    - Abstract base type vs concrete base type retention in `_combineNamedAndUnnamed`.
 *    - Concrete base class without name must NOT be skipped.
 *    - Precedence: base hierarchy < property annotations < explicit registered subtypes.
 *
 * 4. Boundary Value Analysis (BVA) & Defensive Guard Paths:
 *    - Empty arrays to `registerSubtypes(NamedType...)` and `registerSubtypes(Class<?>...)`.
 *    - Multiple calls to `registerSubtypes` accumulating into `_registeredSubtypes`.
 *    - `_collectAndResolve` and `_collectAndResolveByTypeId` handling already visited types (cycle prevention).
 *
 * 5. Object Lifecycle & Contract Integrity:
 *    - Java Serialization/Deserialization round-trip preserving `_registeredSubtypes` state.
 */
public class StdSubtypeResolverGeminiTest {

    // =========================================================================
    // Test Fixtures & Model Hierarchy
    // =========================================================================

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    @JsonSubTypes({
        @JsonSubTypes.Type(value = Sub1616.class, name = "sub1616")
    })
    static abstract class Base1616 {
        public int id;
    }

    static class Sub1616 extends Base1616 {
        public Sub1616() { }
        public Sub1616(int id) { this.id = id; }
    }

    static class Wrapper1616 {
        public Base1616 item;
    }

    static class ConcreteBaseWithoutName {
        public String name;
    }

    static class ConcreteSubWithoutName extends ConcreteBaseWithoutName { }

    @JsonTypeName("namedRoot")
    static abstract class AbstractBaseWithTypeName {
        public double val;
    }

    static class SubOfNamedRoot extends AbstractBaseWithTypeName { }

    static class PropertySubtypeContainer {
        @JsonSubTypes({
            @JsonSubTypes.Type(value = PropSubA.class, name = "a"),
            @JsonSubTypes.Type(value = PropSubB.class, name = "b")
        })
        public PlainBase field;
    }

    static class PlainBase { }
    static class PropSubA extends PlainBase { }
    static class PropSubB extends PlainBase { }

    @JsonSubTypes({
        @JsonSubTypes.Type(value = RecursiveMid.class, name = "mid")
    })
    static class RecursiveRoot { }

    @JsonSubTypes({
        @JsonSubTypes.Type(value = RecursiveLeaf.class, name = "leaf")
    })
    static class RecursiveMid extends RecursiveRoot { }

    static class RecursiveLeaf extends RecursiveMid { }

    static class DisjointType { }

    // Helper to extract AnnotatedMember field
    private AnnotatedMember getFieldMember(MapperConfig<?> config, Class<?> wrapperClass, String fieldName) {
        AnnotatedClass ac = AnnotatedClassResolver.resolveWithoutSuperTypes(config, wrapperClass);
        for (AnnotatedField f : ac.fields()) {
            if (fieldName.equals(f.getName())) {
                return f;
            }
        }
        fail("Field '" + fieldName + "' not found on " + wrapperClass);
        return null;
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testRegisterSubtypesViaClassArrayAndNamedTypeArray() {
        StdSubtypeResolver resolver = new StdSubtypeResolver();

        resolver.registerSubtypes(Sub1616.class, ConcreteSubWithoutName.class);
        resolver.registerSubtypes(new NamedType(PlainBase.class, "customPlain"));

        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        AnnotatedClass ac = AnnotatedClassResolver.resolveWithoutSuperTypes(config, Object.class);

        Collection<NamedType> resolved = resolver.collectAndResolveSubtypesByClass(config, ac);
        assertNotNull(resolved);

        boolean foundCustomPlain = false;
        boolean foundSub1616 = false;
        for (NamedType nt : resolved) {
            if (nt.getType() == PlainBase.class && "customPlain".equals(nt.getName())) {
                foundCustomPlain = true;
            }
            if (nt.getType() == Sub1616.class) {
                foundSub1616 = true;
            }
        }
        assertTrue("Explicitly registered subtype customPlain must be resolved", foundCustomPlain);
        assertTrue("Sub1616 registered via Class must be resolved", foundSub1616);
    }

    @Test(timeout = 4000)
    public void testCollectAndResolveSubtypesByClassWithAnnotatedClass() {
        StdSubtypeResolver resolver = new StdSubtypeResolver();
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();

        AnnotatedClass ac = AnnotatedClassResolver.resolveWithoutSuperTypes(config, RecursiveRoot.class);
        Collection<NamedType> resolved = resolver.collectAndResolveSubtypesByClass(config, ac);

        assertNotNull(resolved);
        Set<Class<?>> classes = new HashSet<Class<?>>();
        for (NamedType nt : resolved) {
            classes.add(nt.getType());
        }
        assertTrue(classes.contains(RecursiveRoot.class));
        assertTrue(classes.contains(RecursiveMid.class));
        assertTrue(classes.contains(RecursiveLeaf.class));
    }

    @Test(timeout = 4000)
    public void testCollectAndResolveSubtypesByTypeIdWithAnnotatedClass() {
        StdSubtypeResolver resolver = new StdSubtypeResolver();
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();

        AnnotatedClass ac = AnnotatedClassResolver.resolveWithoutSuperTypes(config, RecursiveRoot.class);
        Collection<NamedType> resolved = resolver.collectAndResolveSubtypesByTypeId(config, ac);

        assertNotNull(resolved);
        Map<String, Class<?>> nameToClass = new HashMap<String, Class<?>>();
        for (NamedType nt : resolved) {
            if (nt.hasName()) {
                nameToClass.put(nt.getName(), nt.getType());
            }
        }
        assertEquals(RecursiveMid.class, nameToClass.get("mid"));
        assertEquals(RecursiveLeaf.class, nameToClass.get("leaf"));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testRegisterEmptySubtypes() {
        StdSubtypeResolver resolver = new StdSubtypeResolver();
        resolver.registerSubtypes(new NamedType[0]);
        resolver.registerSubtypes(new Class<?>[0]);

        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        AnnotatedClass ac = AnnotatedClassResolver.resolveWithoutSuperTypes(config, PlainBase.class);

        Collection<NamedType> resolved = resolver.collectAndResolveSubtypesByClass(config, ac);
        assertNotNull(resolved);
        assertEquals(1, resolved.size());
        assertEquals(PlainBase.class, resolved.iterator().next().getType());
    }

    @Test(timeout = 4000)
    public void testNullBaseTypeFallbackToPropertyRawType() {
        StdSubtypeResolver resolver = new StdSubtypeResolver();
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();

        AnnotatedMember member = getFieldMember(config, PropertySubtypeContainer.class, "field");
        assertNotNull(member);

        // Explicitly pass baseType == null to verify backwards compatibility branch
        Collection<NamedType> resolved = resolver.collectAndResolveSubtypesByClass(config, member, null);
        assertNotNull(resolved);

        Set<Class<?>> types = new HashSet<Class<?>>();
        for (NamedType nt : resolved) {
            types.add(nt.getType());
        }
        assertTrue("Must resolve PropSubA from property annotations", types.contains(PropSubA.class));
        assertTrue("Must resolve PropSubB from property annotations", types.contains(PropSubB.class));
        assertTrue("Must resolve PlainBase from property raw type", types.contains(PlainBase.class));
    }

    @Test(timeout = 4000)
    public void testRegisteredSubtypesFiltersOutNonAssignable() {
        StdSubtypeResolver resolver = new StdSubtypeResolver();
        resolver.registerSubtypes(new NamedType(DisjointType.class, "disjoint"));
        resolver.registerSubtypes(new NamedType(PropSubA.class, "propSubA"));

        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        AnnotatedClass ac = AnnotatedClassResolver.resolveWithoutSuperTypes(config, PlainBase.class);

        Collection<NamedType> resolved = resolver.collectAndResolveSubtypesByClass(config, ac);
        for (NamedType nt : resolved) {
            assertNotEquals("DisjointType should not be resolved for PlainBase hierarchy",
                    DisjointType.class, nt.getType());
        }
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (TestTypeNames::testBaseTypeId1616)
    // =========================================================================

    @Test(timeout = 4000)
    public void testBaseTypeId1616DefectResolutionByTypeIdAnnotatedClass() {
        StdSubtypeResolver resolver = new StdSubtypeResolver();
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();

        AnnotatedClass ac = AnnotatedClassResolver.resolveWithoutSuperTypes(config, Base1616.class);
        Collection<NamedType> subtypes = resolver.collectAndResolveSubtypesByTypeId(config, ac);

        assertNotNull(subtypes);
        for (NamedType nt : subtypes) {
            assertFalse("Abstract base type without an explicit name must NOT be included as unnamed subtype (defect #1616)",
                    nt.getType() == Base1616.class && !nt.hasName());
        }
        assertEquals(1, subtypes.size());
        NamedType subtype = subtypes.iterator().next();
        assertEquals(Sub1616.class, subtype.getType());
        assertEquals("sub1616", subtype.getName());
    }

    @Test(timeout = 4000)
    public void testBaseTypeId1616DefectResolutionByTypeIdMember() {
        StdSubtypeResolver resolver = new StdSubtypeResolver();
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();

        AnnotatedMember member = getFieldMember(config, Wrapper1616.class, "item");
        JavaType baseType = mapper.constructType(Base1616.class);

        Collection<NamedType> subtypes = resolver.collectAndResolveSubtypesByTypeId(config, member, baseType);
        assertNotNull(subtypes);
        for (NamedType nt : subtypes) {
            assertFalse("Abstract base type without name must be excluded from result (defect #1616)",
                    nt.getType() == Base1616.class && !nt.hasName());
        }
        assertEquals(1, subtypes.size());
        assertEquals(Sub1616.class, subtypes.iterator().next().getType());
    }

    @Test(timeout = 4000)
    public void testBaseTypeId1616FullDeserializationPipeline() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"type\":\"sub1616\",\"id\":99}";
        Base1616 result = mapper.readValue(json, Base1616.class);

        assertNotNull("Deserialized object should not be null", result);
        assertTrue("Expected instance of Sub1616", result instanceof Sub1616);
        assertEquals(99, result.id);
    }

    // =========================================================================
    // Partition D: Combine Named & Unnamed Branches & Hierarchy Edge Cases
    // =========================================================================

    @Test(timeout = 4000)
    public void testCombineNamedAndUnnamedRetainsConcreteBaseWithoutName() {
        StdSubtypeResolver resolver = new StdSubtypeResolver();
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();

        AnnotatedClass ac = AnnotatedClassResolver.resolveWithoutSuperTypes(config, ConcreteBaseWithoutName.class);
        Collection<NamedType> subtypes = resolver.collectAndResolveSubtypesByTypeId(config, ac);

        assertNotNull(subtypes);
        boolean foundConcreteBase = false;
        for (NamedType nt : subtypes) {
            if (nt.getType() == ConcreteBaseWithoutName.class) {
                foundConcreteBase = true;
            }
        }
        assertTrue("Concrete base type without explicit name MUST be retained in subtypes", foundConcreteBase);
    }

    @Test(timeout = 4000)
    public void testAbstractBaseWithExplicitTypeNameIsRetained() {
        StdSubtypeResolver resolver = new StdSubtypeResolver();
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();

        AnnotatedClass ac = AnnotatedClassResolver.resolveWithoutSuperTypes(config, AbstractBaseWithTypeName.class);
        Collection<NamedType> subtypes = resolver.collectAndResolveSubtypesByTypeId(config, ac);

        assertNotNull(subtypes);
        boolean foundNamedRoot = false;
        for (NamedType nt : subtypes) {
            if (nt.getType() == AbstractBaseWithTypeName.class && "namedRoot".equals(nt.getName())) {
                foundNamedRoot = true;
            }
        }
        assertTrue("Abstract base type WITH explicit @JsonTypeName must be retained", foundNamedRoot);
    }

    @Test(timeout = 4000)
    public void testNameOverrideWhenSubtypeResolvedWithNameAfterBeingAddedWithoutName() {
        StdSubtypeResolver resolver = new StdSubtypeResolver();
        // Register without name first
        resolver.registerSubtypes(new NamedType(PlainBase.class, null));
        // Register again with an explicit name
        resolver.registerSubtypes(new NamedType(PlainBase.class, "assignedName"));

        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        AnnotatedClass ac = AnnotatedClassResolver.resolveWithoutSuperTypes(config, PlainBase.class);

        Collection<NamedType> subtypes = resolver.collectAndResolveSubtypesByClass(config, ac);
        assertEquals(1, subtypes.size());
        NamedType result = subtypes.iterator().next();
        assertEquals(PlainBase.class, result.getType());
        assertEquals("assignedName", result.getName());
    }

    @Test(timeout = 4000)
    public void testPropertyAnnotationsAndRegisteredSubtypesCombined() {
        StdSubtypeResolver resolver = new StdSubtypeResolver();
        // Explicitly register a subtype for PlainBase with a custom name
        resolver.registerSubtypes(new NamedType(PropSubA.class, "registeredOverrideA"));

        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        AnnotatedMember member = getFieldMember(config, PropertySubtypeContainer.class, "field");
        JavaType baseType = mapper.constructType(PlainBase.class);

        Collection<NamedType> subtypes = resolver.collectAndResolveSubtypesByTypeId(config, member, baseType);
        assertNotNull(subtypes);

        Map<Class<?>, String> typeToName = new HashMap<Class<?>, String>();
        for (NamedType nt : subtypes) {
            typeToName.put(nt.getType(), nt.getName());
        }

        // Explicit type registration takes highest precedence over property annotation
        assertEquals("registeredOverrideA", typeToName.get(PropSubA.class));
        assertEquals("b", typeToName.get(PropSubB.class));
    }

    @Test(timeout = 4000)
    public void testDirectCombineNamedAndUnnamedMethod() {
        StdSubtypeResolver resolver = new StdSubtypeResolver();
        Set<Class<?>> typesHandled = new HashSet<Class<?>>();
        Map<String, NamedType> byName = new LinkedHashMap<String, NamedType>();

        // Scenario: Abstract base type not in byName -> must be skipped
        typesHandled.add(Base1616.class);
        // Concrete sub in byName -> must be in result, removed from typesHandled
        NamedType sub = new NamedType(Sub1616.class, "sub");
        byName.put("sub", sub);
        typesHandled.add(Sub1616.class);
        // Concrete un-named sibling -> must be in result
        typesHandled.add(ConcreteSubWithoutName.class);

        Collection<NamedType> result = resolver._combineNamedAndUnnamed(Base1616.class, typesHandled, byName);
        assertNotNull(result);
        assertEquals(2, result.size());

        Set<Class<?>> resultClasses = new HashSet<Class<?>>();
        for (NamedType nt : result) {
            resultClasses.add(nt.getType());
        }
        assertTrue(resultClasses.contains(Sub1616.class));
        assertTrue(resultClasses.contains(ConcreteSubWithoutName.class));
        assertFalse("Abstract base class must be skipped", resultClasses.contains(Base1616.class));
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testJavaSerializationAndDeserializationRoundTrip() throws Exception {
        StdSubtypeResolver resolver = new StdSubtypeResolver();
        resolver.registerSubtypes(new NamedType(Sub1616.class, "sub1616"));
        resolver.registerSubtypes(RecursiveMid.class);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(resolver);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        StdSubtypeResolver deserialized = (StdSubtypeResolver) ois.readObject();
        ois.close();

        assertNotNull("Deserialized resolver must not be null", deserialized);

        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        AnnotatedClass ac = AnnotatedClassResolver.resolveWithoutSuperTypes(config, Base1616.class);

        Collection<NamedType> resolved = deserialized.collectAndResolveSubtypesByTypeId(config, ac);
        assertEquals(1, resolved.size());
        NamedType nt = resolved.iterator().next();
        assertEquals(Sub1616.class, nt.getType());
        assertEquals("sub1616", nt.getName());
    }
}