/* [Branch & Defect Analysis Matrix]
 *
 * Target Class: com.fasterxml.jackson.databind.introspect.POJOPropertyBuilder
 * Defects4J Defect: Properties marked with @JsonProperty(access = Access.READ_ONLY)
 *   are not recognized as ignorable during deserialization, causing UnrecognizedPropertyException
 *   when present in input JSON (e.g. ReadOrWriteOnlyTest#testReadOnlyAndWriteOnly and testReadOnly935).
 *
 * Key Decision Branches & Boundary Conditions Targeted:
 * 1. Constructor variants & fluent factories (withName, withSimpleName - same vs new name).
 * 2. compareTo: this._ctorParameters vs other._ctorParameters (null vs non-null, both null/non-null), name comparison.
 * 3. Accessor existence & capability checks: hasGetter, hasSetter, hasField, hasConstructorParameter, couldDeserialize, couldSerialize.
 * 4. Getter resolution (getGetter):
 *    - null / single getter / multiple getters
 *    - class hierarchy precedence (super vs subclass masking)
 *    - priority resolution: regular ("get"), is-getter ("is"), implicit ("x")
 *    - conflicting getters exception trigger
 * 5. Setter resolution (getSetter):
 *    - null / single setter / multiple setters
 *    - class hierarchy precedence masking
 *    - priority: "set" vs implicit
 *    - AnnotationIntrospector.resolveSetterConflict (pref == curr, pref == next, or unresolved -> exception)
 * 6. Field resolution (getField):
 *    - null / single / multiple fields with class hierarchy masking or conflict exception
 * 7. Constructor parameter resolution (getConstructorParameter, getConstructorParameters):
 *    - null vs non-null, AnnotatedConstructor vs factory method owner, empty iterator.
 * 8. Accessor / Mutator / PrimaryMember resolution:
 *    - _forSerialization true (accessor = getter -> field) vs false (mutator = ctor -> setter -> field)
 * 9. Metadata & Refinements:
 *    - findViews, findReferenceType, isTypeId
 *    - getMetadata: combination of required, description, index, defaultValue (STD_REQUIRED_OR_OPTIONAL branches)
 *    - findObjectIdInfo (null vs non-null with reference info)
 *    - findInclusion, findAccess
 * 10. Data aggregation & modifications:
 *     - addField, addCtor, addGetter, addSetter, addAll, merge
 *     - removeIgnored (chain withoutIgnored)
 *     - removeNonVisible (READ_ONLY, READ_WRITE, WRITE_ONLY, AUTO / inferMutators)
 *     - removeConstructors, trimByVisibility, mergeAnnotations (serialization vs deserialization chains)
 * 11. Explode & Name operations:
 *     - anyVisible, anyIgnorals, findExplicitNames, explode (conflicts, mismatched accessors, successful explode)
 * 12. Defect Targets:
 *     - READ_ONLY properties during deserialization must be ignored without throwing UnrecognizedPropertyException.
 */

package com.fasterxml.jackson.databind.introspect;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyMetadata;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class POJOPropertyBuilderGeminiTest {

    // =========================================================================
    // Test Dummy Hierarchy for Inheritance & Reflection
    // =========================================================================
    static class BaseClass {
        public int fieldA;
        public int getProp() { return 1; }
        public void setProp(int val) { }
    }

    static class SubClass extends BaseClass {
        public int fieldA; // masks base field
        public int fieldB;
        @Override
        public int getProp() { return 2; } // masks base getter
        @Override
        public void setProp(int val) { } // masks base setter

        public boolean isProp() { return true; }
        public int prop() { return 3; }
        public void prop(int val) { }

        public SubClass() { }
        public SubClass(int param) { }

        public static SubClass create(int param) {
            return new SubClass(param);
        }
    }

    static class SiblingClass {
        public int getProp() { return 4; }
        public void setProp(int val) { }
        public int fieldA;
    }

    // Helpers to construct Jackson AST nodes
    private static AnnotatedClass getAnnotatedClass(Class<?> cls) {
        return AnnotatedClass.constructWithoutSuperTypes(cls, null, null);
    }

    private static AnnotatedField makeField(Class<?> cls, String fieldName) {
        try {
            Field f = cls.getDeclaredField(fieldName);
            return new AnnotatedField(null, f, new AnnotationMap());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static AnnotatedMethod makeMethod(Class<?> cls, String methodName, Class<?>... params) {
        try {
            Method m = cls.getDeclaredMethod(methodName, params);
            return new AnnotatedMethod(null, m, new AnnotationMap(), null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static AnnotatedParameter makeCtorParam(Class<?> cls, int index, Class<?>... params) {
        try {
            Constructor<?> ctor = cls.getDeclaredConstructor(params);
            AnnotatedConstructor annCtor = new AnnotatedConstructor(null, ctor, new AnnotationMap(), null);
            return new AnnotatedParameter(annCtor, TypeFactory.unknownType(), new AnnotationMap(), index);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static AnnotatedParameter makeFactoryParam(Class<?> cls, String methodName, int index, Class<?>... params) {
        try {
            Method m = cls.getDeclaredMethod(methodName, params);
            AnnotatedMethod annMethod = new AnnotatedMethod(null, m, new AnnotationMap(), null);
            return new AnnotatedParameter(annMethod, TypeFactory.unknownType(), new AnnotationMap(), index);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // =========================================================================
    // PARTITION A: Core Functional Logic, State Transitions & Getters/Setters
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicCreationAndNameAccess() {
        PropertyName propName = new PropertyName("testProp");
        POJOPropertyBuilder builder = new POJOPropertyBuilder(null, null, true, propName);

        assertEquals("testProp", builder.getName());
        assertEquals("testProp", builder.getInternalName());
        assertEquals(propName, builder.getFullName());
        assertTrue(builder.hasName(propName));
        assertFalse(builder.hasName(new PropertyName("other")));

        POJOPropertyBuilder withNewName = builder.withName(new PropertyName("newName"));
        assertEquals("newName", withNewName.getName());
        assertEquals("testProp", withNewName.getInternalName());

        POJOPropertyBuilder withSameSimpleName = builder.withSimpleName("testProp");
        assertSame(builder, withSameSimpleName);

        POJOPropertyBuilder withDiffSimpleName = builder.withSimpleName("modifiedProp");
        assertNotSame(builder, withDiffSimpleName);
        assertEquals("modifiedProp", withDiffSimpleName.getName());
    }

    @Test(timeout = 4000)
    public void testCompareToOrdering() {
        PropertyName nameA = new PropertyName("alpha");
        PropertyName nameB = new PropertyName("beta");

        POJOPropertyBuilder p1 = new POJOPropertyBuilder(null, null, true, nameA);
        POJOPropertyBuilder p2 = new POJOPropertyBuilder(null, null, true, nameB);

        // Alpha precedes beta alphabetically
        assertTrue(p1.compareTo(p2) < 0);
        assertTrue(p2.compareTo(p1) > 0);

        // Properties with constructor parameters must come before those without
        AnnotatedParameter ctorParam = makeCtorParam(SubClass.class, 0, int.class);
        p2.addCtor(ctorParam, nameB, true, true, false);

        assertTrue(p2.compareTo(p1) < 0); // p2 has ctor param, so it comes first
        assertTrue(p1.compareTo(p2) > 0);

        // Both have constructor parameters -> compare by name
        p1.addCtor(ctorParam, nameA, true, true, false);
        assertTrue(p1.compareTo(p2) < 0);
    }

    @Test(timeout = 4000)
    public void testGetterPriorityAndResolution() {
        POJOPropertyBuilder builder = new POJOPropertyBuilder(null, null, true, new PropertyName("prop"));

        AnnotatedMethod getMethod = makeMethod(SubClass.class, "getProp");
        AnnotatedMethod isMethod = makeMethod(SubClass.class, "isProp");
        AnnotatedMethod implicitMethod = makeMethod(SubClass.class, "prop");

        // getX (priority 1) > isX (priority 2) > x (priority 3)
        builder.addGetter(implicitMethod, new PropertyName("prop"), false, true, false);
        builder.addGetter(isMethod, new PropertyName("prop"), false, true, false);
        builder.addGetter(getMethod, new PropertyName("prop"), false, true, false);

        AnnotatedMethod resolved = builder.getGetter();
        assertNotNull(resolved);
        assertEquals("getProp", resolved.getName());
    }

    @Test(timeout = 4000)
    public void testGetterSubclassMasking() {
        POJOPropertyBuilder builder = new POJOPropertyBuilder(null, null, true, new PropertyName("prop"));

        AnnotatedMethod baseGetter = makeMethod(BaseClass.class, "getProp");
        AnnotatedMethod subGetter = makeMethod(SubClass.class, "getProp");

        // Base getter masked by subclass getter
        builder.addGetter(baseGetter, new PropertyName("prop"), false, true, false);
        builder.addGetter(subGetter, new PropertyName("prop"), false, true, false);

        AnnotatedMethod resolved = builder.getGetter();
        assertEquals(SubClass.class, resolved.getDeclaringClass());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConflictingGettersThrowsException() {
        POJOPropertyBuilder builder = new POJOPropertyBuilder(null, null, true, new PropertyName("prop"));

        AnnotatedMethod subGetter = makeMethod(SubClass.class, "getProp");
        AnnotatedMethod siblingGetter = makeMethod(SiblingClass.class, "getProp");

        builder.addGetter(subGetter, new PropertyName("prop"), false, true, false);
        builder.addGetter(siblingGetter, new PropertyName("prop"), false, true, false);

        builder.getGetter(); // Conflicting classes not assignable to each other
    }

    @Test(timeout = 4000)
    public void testSetterPriorityAndResolution() {
        POJOPropertyBuilder builder = new POJOPropertyBuilder(null, null, false, new PropertyName("prop"));

        AnnotatedMethod implicitSetter = makeMethod(SubClass.class, "prop", int.class);
        AnnotatedMethod regularSetter = makeMethod(SubClass.class, "setProp", int.class);

        builder.addSetter(implicitSetter, new PropertyName("prop"), false, true, false);
        builder.addSetter(regularSetter, new PropertyName("prop"), false, true, false);

        AnnotatedMethod resolved = builder.getSetter();
        assertNotNull(resolved);
        assertEquals("setProp", resolved.getName());
    }

    @Test(timeout = 4000)
    public void testSetterSubclassMasking() {
        POJOPropertyBuilder builder = new POJOPropertyBuilder(null, null, false, new PropertyName("prop"));

        AnnotatedMethod baseSetter = makeMethod(BaseClass.class, "setProp", int.class);
        AnnotatedMethod subSetter = makeMethod(SubClass.class, "setProp", int.class);

        builder.addSetter(baseSetter, new PropertyName("prop"), false, true, false);
        builder.addSetter(subSetter, new PropertyName("prop"), false, true, false);

        AnnotatedMethod resolved = builder.getSetter();
        assertEquals(SubClass.class, resolved.getDeclaringClass());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConflictingSettersThrowsException() {
        POJOPropertyBuilder builder = new POJOPropertyBuilder(null, null, false, new PropertyName("prop"));

        AnnotatedMethod subSetter = makeMethod(SubClass.class, "setProp", int.class);
        AnnotatedMethod siblingSetter = makeMethod(SiblingClass.class, "setProp", int.class);

        builder.addSetter(subSetter, new PropertyName("prop"), false, true, false);
        builder.addSetter(siblingSetter, new PropertyName("prop"), false, true, false);

        builder.getSetter();
    }

    @Test(timeout = 4000)
    public void testFieldMaskingAndConflict() {
        POJOPropertyBuilder builder = new POJOPropertyBuilder(null, null, true, new PropertyName("fieldA"));

        AnnotatedField baseField = makeField(BaseClass.class, "fieldA");
        AnnotatedField subField = makeField(SubClass.class, "fieldA");

        builder.addField(baseField, new PropertyName("fieldA"), false, true, false);
        builder.addField(subField, new PropertyName("fieldA"), false, true, false);

        AnnotatedField resolved = builder.getField();
        assertEquals(SubClass.class, resolved.getDeclaringClass());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConflictingFieldsThrowsException() {
        POJOPropertyBuilder builder = new POJOPropertyBuilder(null, null, true, new PropertyName("fieldA"));

        AnnotatedField subField = makeField(SubClass.class, "fieldA");
        AnnotatedField siblingField = makeField(SiblingClass.class, "fieldA");

        builder.addField(subField, new PropertyName("fieldA"), false, true, false);
        builder.addField(siblingField, new PropertyName("fieldA"), false, true, false);

        builder.getField();
    }

    // =========================================================================
    // PARTITION B: Boundary Value Analysis (BVA), Extremes & Structural Chains
    // =========================================================================

    @Test(timeout = 4000)
    public void testNullAccessorsAndEmptyIterators() {
        POJOPropertyBuilder builder = new POJOPropertyBuilder(null, null, true, new PropertyName("empty"));

        assertNull(builder.getGetter());
        assertNull(builder.getSetter());
        assertNull(builder.getField());
        assertNull(builder.getConstructorParameter());
        assertFalse(builder.getConstructorParameters().hasNext());
        assertNull(builder.getAccessor());
        assertNull(builder.getMutator());
        assertNull(builder.getNonConstructorMutator());
        assertNull(builder.getPrimaryMember());
        assertFalse(builder.couldSerialize());
        assertFalse(builder.couldDeserialize());
        assertFalse(builder.hasGetter());
        assertFalse(builder.hasSetter());
        assertFalse(builder.hasField());
        assertFalse(builder.hasConstructorParameter());
        assertFalse(builder.anyVisible());
        assertFalse(builder.anyIgnorals());
        assertTrue(builder.findExplicitNames().isEmpty());
    }

    @Test(timeout = 4000)
    public void testConstructorParamResolutionPrecedence() {
        POJOPropertyBuilder builder = new POJOPropertyBuilder(null, null, false, new PropertyName("ctorParam"));

        AnnotatedParameter factoryParam = makeFactoryParam(SubClass.class, "create", 0, int.class);
        AnnotatedParameter ctorParam = makeCtorParam(SubClass.class, 0, int.class);

        // Factory param added first, constructor param added second
        builder.addCtor(factoryParam, new PropertyName("ctorParam"), false, true, false);
        builder.addCtor(ctorParam, new PropertyName("ctorParam"), false, true, false);

        AnnotatedParameter resolved = builder.getConstructorParameter();
        assertNotNull(resolved);
        // Constructor must have precedence over factory method
        assertTrue(resolved.getOwner() instanceof AnnotatedConstructor);

        Iterator<AnnotatedParameter> it = builder.getConstructorParameters();
        assertTrue(it.hasNext());
        assertNotNull(it.next());
        assertTrue(it.hasNext());
        assertNotNull(it.next());
        assertFalse(it.hasNext());
    }

    @Test(timeout = 4000)
    public void testPrimaryMemberSerializationVsDeserialization() {
        AnnotatedMethod getter = makeMethod(SubClass.class, "getProp");
        AnnotatedMethod setter = makeMethod(SubClass.class, "setProp",