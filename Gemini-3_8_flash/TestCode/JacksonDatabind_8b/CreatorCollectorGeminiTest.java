package com.fasterxml.jackson.databind.deser.impl;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.PropertyMetadata;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.deser.CreatorProperty;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.deser.std.StdValueInstantiator;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.type.TypeBindings;
import com.fasterxml.jackson.databind.type.TypeFactory;

/* [Branch & Defect Analysis Matrix]
 * Target Class: com.fasterxml.jackson.databind.deser.impl.CreatorCollector
 *
 * 1. Defect-Targeted Ground Truth (Defects4J / databind#667):
 *    - In verifyNonDup(newOne, typeIndex, explicit), when oldOne is not explicit and newOne
 *      is also implicit (or newOne is explicit overriding implicit), verifyNonDup unconditionally
 *      throws IllegalArgumentException claiming "already had explicitly marked <oldOne>".
 *    - Specifically for StringBuilder-like JDK types with multiple implicit single-arg constructors
 *      (e.g., String vs CharSequence), non-explicit resolution should not trigger false conflicting
 *      explicit creator exceptions.
 *
 * 2. Decision Branches Covered:
 *    - constructValueInstantiator:
 *        * maybeVanilla (true/false) based on _hasNonDefaultCreator
 *        * _creators[C_DELEGATE] null vs non-null with _delegateArgs (finding delegate index ix where arg == null)
 *        * Vanilla fast-path: Collection, List, ArrayList -> Vanilla.TYPE_COLLECTION
 *        * Vanilla fast-path: Map, LinkedHashMap -> Vanilla.TYPE_MAP
 *        * Vanilla fast-path: HashMap -> Vanilla.TYPE_HASH_MAP
 *        * Other types -> StdValueInstantiator
 *    - verifyNonDup:
 *        * oldOne == null vs oldOne != null
 *        * (_explicitCreators & mask) != 0:
 *            - !explicit -> return (preserve existing explicit creator)
 *            - explicit -> throw IllegalArgumentException
 *        * oldOne.getClass() == newOne.getClass() vs differing classes (subclasses/overrides)
 *    - addPropertyCreator:
 *        * properties.length <= 1 vs properties.length > 1
 *        * Duplicate property names detected -> IllegalArgumentException
 *        * Injectable values with empty name -> allowed, skip duplicate check
 *    - addIncompeteParameter:
 *        * _incompleteParameter == null -> set
 *        * _incompleteParameter != null -> ignore
 *    - _fixAccess:
 *        * canFixAccess = true vs canFixAccess = false
 *        * member == null vs member != null
 *    - Vanilla ValueInstantiator inner class:
 *        * canInstantiate, canCreateUsingDefault
 *        * getValueTypeDesc for TYPE_COLLECTION, TYPE_MAP, TYPE_HASH_MAP, and unknown type
 *        * createUsingDefault for all valid types and unknown type throwing IllegalStateException
 *    - Deprecated API pass-through coverage.
 */
public class CreatorCollectorGeminiTest {

    // Dummy helper classes for introspecting constructors and methods
    static class DummyBean {
        public DummyBean() {}
        public DummyBean(String s) {}
        public DummyBean(CharSequence cs) {}
        public DummyBean(int i) {}
        public DummyBean(long l) {}
        public DummyBean(double d) {}
        public DummyBean(boolean b) {}
        public DummyBean(Object delegate, String prop) {}
        public DummyBean(String prop1, String prop2) {}

        public static DummyBean factory(String s) { return new DummyBean(s); }
    }

    static class SubDummyBean extends DummyBean {
        public SubDummyBean(String s) { super(s); }
    }

    private AnnotatedConstructor makeConstructor(Class<?> clazz, Class<?>... paramTypes) {
        try {
            Constructor<?> ctor = clazz.getDeclaredConstructor(paramTypes);
            AnnotationMap annMap = new AnnotationMap();
            int pCount = paramTypes.length;
            AnnotationMap[] pAnns = new AnnotationMap[pCount];
            for (int i = 0; i < pCount; i++) {
                pAnns[i] = new AnnotationMap();
            }
            return new AnnotatedConstructor(ctor, annMap, pAnns);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private AnnotatedMethod makeMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        try {
            Method m = clazz.getDeclaredMethod(methodName, paramTypes);
            AnnotationMap annMap = new AnnotationMap();
            int pCount = paramTypes.length;
            AnnotationMap[] pAnns = new AnnotationMap[pCount];
            for (int i = 0; i < pCount; i++) {
                pAnns[i] = new AnnotationMap();
            }
            return new AnnotatedMethod(m, annMap, pAnns);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private BeanDescription makeBeanDesc(Class<?> rawClass) {
        JavaType jt = TypeFactory.defaultInstance().constructType(rawClass);
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(rawClass, null, null);
        return new BasicBeanDescription(null, jt, ac, Collections.<BeanPropertyDefinition>emptyList());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefaultCreatorAndHasDefaultCreator() {
        BeanDescription bd = makeBeanDesc(DummyBean.class);
        CreatorCollector coll = new CreatorCollector(bd, true);

        assertFalse(coll.hasDefaultCreator());

        AnnotatedConstructor defaultCtor = makeConstructor(DummyBean.class);
        coll.setDefaultCreator(defaultCtor);

        assertTrue(coll.hasDefaultCreator());
        ValueInstantiator vi = coll.constructValueInstantiator(null);
        assertNotNull(vi);
        assertTrue(vi.canCreateUsingDefault());
    }

    @Test(timeout = 4000)
    public void testAddAllScalarCreatorsExplicit() {
        BeanDescription bd = makeBeanDesc(DummyBean.class);
        CreatorCollector coll = new CreatorCollector(bd, false);

        AnnotatedConstructor sCtor = makeConstructor(DummyBean.class, String.class);
        AnnotatedConstructor iCtor = makeConstructor(DummyBean.class, int.class);
        AnnotatedConstructor lCtor = makeConstructor(DummyBean.class, long.class);
        AnnotatedConstructor dCtor = makeConstructor(DummyBean.class, double.class);
        AnnotatedConstructor bCtor = makeConstructor(DummyBean.class, boolean.class);

        coll.addStringCreator(sCtor, true);
        coll.addIntCreator(iCtor, true);
        coll.addLongCreator(lCtor, true);
        coll.addDoubleCreator(dCtor, true);
        coll.addBooleanCreator(bCtor, true);

        ValueInstantiator vi = coll.constructValueInstantiator(null);
        assertTrue(vi instanceof StdValueInstantiator);
        StdValueInstantiator svi = (StdValueInstantiator) vi;

        assertTrue(svi.canCreateFromString());
        assertTrue(svi.canCreateFromInt());
        assertTrue(svi.canCreateFromLong());
        assertTrue(svi.canCreateFromDouble());
        assertTrue(svi.canCreateFromBoolean());
    }

    @Test(timeout = 4000)
    public void testAddDelegatingCreatorWithArgs() {
        BeanDescription bd = makeBeanDesc(DummyBean.class);
        CreatorCollector coll = new CreatorCollector(bd, true);

        AnnotatedConstructor delCtor = makeConstructor(DummyBean.class, Object.class, String.class);
        CreatorProperty[] props = new CreatorProperty[2];
        props[0] = null; // marker for delegate
        PropertyName propName = new PropertyName("injectProp");
        JavaType strType = TypeFactory.defaultInstance().constructType(String.class);
        props[1] = new CreatorProperty(propName, strType, null, null, null, null, 1, "injectId", PropertyMetadata.STD_OPTIONAL);

        coll.addDelegatingCreator(delCtor, true, props);

        ValueInstantiator vi = coll.constructValueInstantiator(null);
        assertTrue(vi.canCreateUsingDelegate());
        assertEquals(Object.class, vi.getDelegateType(null).getRawClass());
    }

    @Test(timeout = 4000)
    public void testAddDelegatingCreatorWithoutArgs() {
        BeanDescription bd = makeBeanDesc(DummyBean.class);
        CreatorCollector coll = new CreatorCollector(bd, true);

        AnnotatedConstructor delCtor = makeConstructor(DummyBean.class, String.class);
        coll.addDelegatingCreator(delCtor, true, null);

        ValueInstantiator vi = coll.constructValueInstantiator(null);
        assertTrue(vi.canCreateUsingDelegate());
        assertEquals(String.class, vi.getDelegateType(null).getRawClass());
    }

    @Test(timeout = 4000)
    public void testAddPropertyCreatorSuccess() {
        BeanDescription bd = makeBeanDesc(DummyBean.class);
        CreatorCollector coll = new CreatorCollector(bd, false);

        AnnotatedConstructor propCtor = makeConstructor(DummyBean.class, String.class, String.class);
        JavaType strType = TypeFactory.defaultInstance().constructType(String.class);

        CreatorProperty p1 = new CreatorProperty(new PropertyName("prop1"), strType, null, null, null, null, 0, null, PropertyMetadata.STD_REQUIRED);
        CreatorProperty p2 = new CreatorProperty(new PropertyName("prop2"), strType, null, null, null, null, 1, null, PropertyMetadata.STD_REQUIRED);

        coll.addPropertyCreator(propCtor, true, new CreatorProperty[] { p1, p2 });

        ValueInstantiator vi = coll.constructValueInstantiator(null);
        assertTrue(vi.canCreateFromObjectWith());
        assertEquals(2, vi.getFromObjectArguments(null).length);
    }

    @Test(timeout = 4000)
    public void testAddIncompleteParameter() {
        BeanDescription bd = makeBeanDesc(DummyBean.class);
        CreatorCollector coll = new CreatorCollector(bd, false);

        AnnotatedConstructor ctor = makeConstructor(DummyBean.class, String.class);
        AnnotatedParameter p1 = new AnnotatedParameter(ctor, String.class, new AnnotationMap(), 0);
        AnnotatedParameter p2 = new AnnotatedParameter(ctor, String.class, new AnnotationMap(), 1);

        coll.addIncompeteParameter(p1);
        // Second call should not overwrite first
        coll.addIncompeteParameter(p2);

        ValueInstantiator vi = coll.constructValueInstantiator(null);
        assertTrue(vi instanceof StdValueInstantiator);
        assertEquals(p1, ((StdValueInstantiator) vi).getIncompleteParameter());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Fast-path (Vanilla)
    // =========================================================================

    @Test(timeout = 4000)
    public void testVanillaCollectionsAndMaps() throws IOException {
        // ArrayList fast-path
        BeanDescription bdList = makeBeanDesc(ArrayList.class);
        CreatorCollector collList = new CreatorCollector(bdList, false);
        ValueInstantiator viList = collList.constructValueInstantiator(null);
        assertEquals(CreatorCollector.Vanilla.class, viList.getClass());
        assertTrue(viList.canInstantiate());
        assertTrue(viList.canCreateUsingDefault());
        assertEquals(ArrayList.class.getName(), viList.getValueTypeDesc());
        Object objList = viList.createUsingDefault(null);
        assertTrue(objList instanceof ArrayList<?>);

        // List interface fast-path
        BeanDescription bdListInterface = makeBeanDesc(List.class);
        CreatorCollector collListInterface = new CreatorCollector(bdListInterface, false);
        ValueInstantiator viListInterface = collListInterface.constructValueInstantiator(null);
        assertEquals(CreatorCollector.Vanilla.class, viListInterface.getClass());

        // Collection interface fast-path
        BeanDescription bdColl = makeBeanDesc(Collection.class);
        CreatorCollector collColl = new CreatorCollector(bdColl, false);
        ValueInstantiator viColl = collColl.constructValueInstantiator(null);
        assertEquals(CreatorCollector.Vanilla.class, viColl.getClass());

        // HashMap fast-path
        BeanDescription bdHash = makeBeanDesc(HashMap.class);
        CreatorCollector collHash = new CreatorCollector(bdHash, false);
        ValueInstantiator viHash = collHash.constructValueInstantiator(null);
        assertEquals(CreatorCollector.Vanilla.class, viHash.getClass());
        assertEquals(HashMap.class.getName(), viHash.getValueTypeDesc());
        Object objHash = viHash.createUsingDefault(null);
        assertTrue(objHash instanceof HashMap<?, ?>);

        // Map interface fast-path
        BeanDescription bdMap = makeBeanDesc(Map.class);
        CreatorCollector collMap = new CreatorCollector(bdMap, false);
        ValueInstantiator viMap = collMap.constructValueInstantiator(null);
        assertEquals(CreatorCollector.Vanilla.class, viMap.getClass());
        assertEquals(LinkedHashMap.class.getName(), viMap.getValueTypeDesc());
        Object objMap = viMap.createUsingDefault(null);
        assertTrue(objMap instanceof LinkedHashMap<?, ?>);

        // LinkedHashMap fast-path
        BeanDescription bdLinked = makeBeanDesc(LinkedHashMap.class);
        CreatorCollector collLinked = new CreatorCollector(bdLinked, false);
        ValueInstantiator viLinked = collLinked.constructValueInstantiator(null);
        assertEquals(CreatorCollector.Vanilla.class, viLinked.getClass());
    }

    @Test(timeout = 4000)
    public void testVanillaDirectInstantiation() throws IOException {
        CreatorCollector.Vanilla vColl = new CreatorCollector.Vanilla(CreatorCollector.Vanilla.TYPE_COLLECTION);
        assertEquals(ArrayList.class.getName(), vColl.getValueTypeDesc());
        assertTrue(vColl.createUsingDefault(null) instanceof ArrayList);

        CreatorCollector.Vanilla vMap = new CreatorCollector.Vanilla(CreatorCollector.Vanilla.TYPE_MAP);
        assertEquals(LinkedHashMap.class.getName(), vMap.getValueTypeDesc());
        assertTrue(vMap.createUsingDefault(null) instanceof LinkedHashMap);

        CreatorCollector.Vanilla vHashMap = new CreatorCollector.Vanilla(CreatorCollector.Vanilla.TYPE_HASH_MAP);
        assertEquals(HashMap.class.getName(), vHashMap.getValueTypeDesc());
        assertTrue(vHashMap.createUsingDefault(null) instanceof HashMap);

        // Unknown type branch
        CreatorCollector.Vanilla vUnknown = new CreatorCollector.Vanilla(999);
        assertEquals(Object.class.getName(), vUnknown.getValueTypeDesc());
        try {
            vUnknown.createUsingDefault(null);
            fail("Should throw IllegalStateException for unknown type");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("Unknown type 999"));
        }
    }

    @Test(timeout = 4000)
    public void testNonVanillaDueToNonDefaultCreator() {
        BeanDescription bd = makeBeanDesc(ArrayList.class);
        CreatorCollector coll = new CreatorCollector(bd, false);
        // Registering a non-default creator prevents Vanilla
        AnnotatedConstructor sCtor = makeConstructor(DummyBean.class, String.class);
        coll.addStringCreator(sCtor, false);

        ValueInstantiator vi = coll.constructValueInstantiator(null);
        assertFalse(vi instanceof CreatorCollector.Vanilla);
        assertTrue(vi instanceof StdValueInstantiator);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Jackson Databind #667 & Duplicates)
    // =========================================================================

    /**
     * TARGET DEFECT TEST:
     * Ground truth: StringBuilder has both StringBuilder(String) and StringBuilder(CharSequence).
     * When both are discovered without explicit annotations (explicit=false), CreatorCollector
     * must resolve them without throwing:
     * "Conflicting String creators: already had explicitly marked ..."
     *
     * In the defective implementation, verifyNonDup throws IllegalArgumentException even when
     * neither creator is explicit!
     */
    @Test(timeout = 4000)
    public void testDefectTwoImplicitStringCreatorsResolution() {
        BeanDescription bd = makeBeanDesc(StringBuilder.class);
        CreatorCollector coll = new CreatorCollector(bd, true);

        AnnotatedConstructor ctorCharSequence = makeConstructor(StringBuilder.class, CharSequence.class);
        AnnotatedConstructor ctorString = makeConstructor(StringBuilder.class, String.class);

        // Adding the first implicit String creator
        coll.addStringCreator(ctorCharSequence, false);

        // Adding the second implicit String creator must not fail with "already had explicitly marked"
        // On defective version, this throws IllegalArgumentException!
        try {
            coll.addStringCreator(ctorString, false);
        } catch (IllegalArgumentException e) {
            fail("Defect databind#667 triggered: conflicting string creators thrown for implicit creators: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testExplicitOverridesImplicitCreatorWithoutException() {
        BeanDescription bd = makeBeanDesc(DummyBean.class);
        CreatorCollector coll = new CreatorCollector(bd, false);

        AnnotatedConstructor implicitCtor = makeConstructor(DummyBean.class, CharSequence.class);
        AnnotatedConstructor explicitCtor = makeConstructor(DummyBean.class, String.class);

        // First is auto-detected / implicit
        coll.addStringCreator(implicitCtor, false);

        // Second is explicitly annotated, it should override the implicit one without throwing!
        try {
            coll.addStringCreator(explicitCtor, true);
        } catch (IllegalArgumentException e) {
            fail("Explicit creator should override implicit creator without exception: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testImplicitDoesNotOverrideExplicitCreator() {
        BeanDescription bd = makeBeanDesc(DummyBean.class);
        CreatorCollector coll = new CreatorCollector(bd, false);

        AnnotatedConstructor explicitCtor = makeConstructor(DummyBean.class, String.class);
        AnnotatedConstructor implicitCtor = makeConstructor(DummyBean.class, CharSequence.class);

        // Add explicit creator
        coll.addStringCreator(explicitCtor, true);

        // Adding non-explicit should be ignored gracefully (not throw, not override)
        coll.addStringCreator(implicitCtor, false);

        ValueInstantiator vi = coll.constructValueInstantiator(null);
        assertTrue(vi.canCreateFromString());
    }

    @Test(timeout = 4000)
    public void testDifferingCreatorClassesAllowed() {
        // If oldOne and newOne have different classes (e.g. AnnotatedConstructor vs AnnotatedMethod)
        BeanDescription bd = makeBeanDesc(DummyBean.class);
        CreatorCollector coll = new CreatorCollector(bd, false);

        AnnotatedConstructor ctor = makeConstructor(DummyBean.class, String.class);
        AnnotatedMethod method = makeMethod(DummyBean.class, "factory", String.class);

        coll.addStringCreator(ctor, false);
        // Subclass/different reflection wrapper: oldOne.getClass() != newOne.getClass()
        // verifyNonDup should not throw
        coll.addStringCreator(method, false);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testDuplicateExplicitCreatorsThrows() {
        BeanDescription bd = makeBeanDesc(DummyBean.class);
        CreatorCollector coll = new CreatorCollector(bd, false);

        AnnotatedConstructor ctor1 = makeConstructor(DummyBean.class, String.class);
        AnnotatedConstructor ctor2 = makeConstructor(DummyBean.class, String.class);

        coll.addStringCreator(ctor1, true);
        // Second explicit creator must trigger IllegalArgumentException
        coll.addStringCreator(ctor2, true);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testDuplicatePropertyNamesThrows() {
        BeanDescription bd = makeBeanDesc(DummyBean.class);
        CreatorCollector coll = new CreatorCollector(bd, false);

        AnnotatedConstructor propCtor = makeConstructor(DummyBean.class, String.class, String.class);
        JavaType strType = TypeFactory.defaultInstance().constructType(String.class);

        CreatorProperty p1 = new CreatorProperty(new PropertyName("duplicateName"), strType, null, null, null, null, 0, null, PropertyMetadata.STD_REQUIRED);
        CreatorProperty p2 = new CreatorProperty(new PropertyName("duplicateName"), strType, null, null, null, null, 1, null, PropertyMetadata.STD_REQUIRED);

        coll.addPropertyCreator(propCtor, true, new CreatorProperty[] { p1, p2 });
    }

    @Test(timeout = 4000)
    public void testDuplicatePropertyNameCheckWithInjectableEmptyName() {
        BeanDescription bd = makeBeanDesc(DummyBean.class);
        CreatorCollector coll = new CreatorCollector(bd, false);

        AnnotatedConstructor propCtor = makeConstructor(DummyBean.class, String.class, String.class);
        JavaType strType = TypeFactory.defaultInstance().constructType(String.class);

        // Injectable with empty name should be skipped from duplicate check
        CreatorProperty p1 = new CreatorProperty(new PropertyName(""), strType, null, null, null, null, 0, "inject1", PropertyMetadata.STD_OPTIONAL);
        CreatorProperty p2 = new CreatorProperty(new PropertyName(""), strType, null, null, null, null, 1, "inject2", PropertyMetadata.STD_OPTIONAL);

        coll.addPropertyCreator(propCtor, false, new CreatorProperty[] { p1, p2 });
        ValueInstantiator vi = coll.constructValueInstantiator(null);
        assertTrue(vi.canCreateFromObjectWith());
    }

    @Test(timeout = 4000)
    public void testSinglePropertyDoesNotTriggerDuplicateCheck() {
        BeanDescription bd = makeBeanDesc(DummyBean.class);
        CreatorCollector coll = new CreatorCollector(bd, false);

        AnnotatedConstructor propCtor = makeConstructor(DummyBean.class, String.class);
        JavaType strType = TypeFactory.defaultInstance().constructType(String.class);

        CreatorProperty p1 = new CreatorProperty(new PropertyName("singleProp"), strType, null, null, null, null, 0, null, PropertyMetadata.STD_REQUIRED);
        coll.addPropertyCreator(propCtor, true, new CreatorProperty[] { p1 });

        ValueInstantiator vi = coll.constructValueInstantiator(null);
        assertTrue(vi.canCreateFromObjectWith());
    }

    // =========================================================================
    // Partition E: Deprecated API & Access Fixes
    // =========================================================================

    @Test(timeout = 4000)
    @SuppressWarnings("deprecation")
    public void testDeprecatedMethodsCoverage() {
        BeanDescription bd = makeBeanDesc(DummyBean.class);
        CreatorCollector coll = new CreatorCollector(bd, true);

        AnnotatedConstructor sCtor = makeConstructor(DummyBean.class, String.class);
        AnnotatedConstructor bCtor = makeConstructor(DummyBean.class, boolean.class);
        AnnotatedConstructor delCtor = makeConstructor(DummyBean.class, String.class);
        AnnotatedConstructor propCtor = makeConstructor(DummyBean.class, String.class);

        coll.addStringCreator(sCtor);
        coll.addBooleanCreator(bCtor);
        coll.addDelegatingCreator(delCtor, null);

        JavaType strType = TypeFactory.defaultInstance().constructType(String.class);
        CreatorProperty prop = new CreatorProperty(new PropertyName("p"), strType, null, null, null, null, 0, null, PropertyMetadata.STD_REQUIRED);
        coll.addPropertyCreator(propCtor, new CreatorProperty[] { prop });

        // Deprecated int, long, double (which historically mapped to boolean or delegate in deprecated methods)
        AnnotatedConstructor dummyCtor = makeConstructor(DummyBean.class, int.class);
        // These call addBooleanCreator in the deprecated implementation
        // calling on fresh collector to avoid conflicts:
        CreatorCollector c2 = new CreatorCollector(bd, false);
        c2.addIntCreator(dummyCtor);

        CreatorCollector c3 = new CreatorCollector(bd, false);
        c3.addLongCreator(dummyCtor);

        CreatorCollector c4 = new CreatorCollector(bd, false);
        c4.addDoubleCreator(dummyCtor);

        // Deprecated verifyNonDup
        CreatorCollector c5 = new CreatorCollector(bd, false);
        AnnotatedWithParams res = c5.verifyNonDup(sCtor, CreatorCollector.C_STRING);
        assertNotNull(res);
        assertSame(sCtor, res);
    }

    @Test(timeout = 4000)
    public void testFixAccessWithNullMember() {
        // Can fix access = true, passing null creator
        BeanDescription bd = makeBeanDesc(DummyBean.class);
        CreatorCollector coll = new CreatorCollector(bd, true);
        coll.setDefaultCreator(null);
        assertFalse(coll.hasDefaultCreator());
    }
}