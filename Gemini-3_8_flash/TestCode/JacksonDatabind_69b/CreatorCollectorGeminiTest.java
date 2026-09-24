package com.fasterxml.jackson.databind.deser.impl;

import java.io.IOException;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.deser.CreatorProperty;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.deser.std.StdValueInstantiator;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * /* [Branch & Defect Analysis Matrix]
 * **************************************************************************************************
 * Branch / Condition                                         Targeted Test Method
 * --------------------------------------------------------------------------------------------------
 * Non-default creators absent: Collection/List/ArrayList     testVanillaCollectionTypes
 * Non-default creators absent: Map/LinkedHashMap             testVanillaMapTypes
 * Non-default creators absent: HashMap                       testVanillaHashMap
 * Non-default creators absent: fallback (Object.class)       testVanillaFallbackToStdValueInstantiator
 * Vanilla inner class contract & branches                    testVanillaDirectInstantiationAndErrors
 * Setting default creator & access fix                       testSetDefaultCreatorAndFixAccess
 * Scalar creators (String, Int, Long, Double, Boolean)       testScalarCreators
 * Delegating creators: Array delegate vs Standard delegate   testDelegatingCreatorsCollectionVsStandard
 * Delegating creator: delegateArgs null marker indexing      testDelegateTypeComputationWithNullMarker
 * Property-based creators: duplicate name detection          testAddPropertyCreatorDuplicateNamesThrow
 * Property-based creators: injectable empty name skip        testAddPropertyCreatorInjectableEmptyNameAllowed
 * Incomplete parameter configuration (idempotency branch)    testAddIncompleteParameter
 * Deprecated creator delegations                             testDeprecatedCreatorMethods
 * verifyNonDup: explicit overrides non-explicit              testVerifyNonDupExplicitOverridesImplicit
 * verifyNonDup: more specific type overrides generic         testVerifyNonDupMoreSpecificTypeOverrides
 * verifyNonDup: generic type does not override specific      testVerifyNonDupGenericTypeIgnored
 * verifyNonDup: conflicting identical types throw            testVerifyNonDupConflictingIdenticalTypesThrow
 * verifyNonDup: different creator classes (ctor vs method)   testVerifyNonDupDifferentCreatorClasses
 * DEFECT 1476: Constructor choice desync properties          testDefect1476ConstructorChoiceViaMapper
 * DEFECT 1476: Non-explicit property args must not overwrite testDefectPropertyArgsNotOverwrittenByImplicit
 * **************************************************************************************************
 */
public class CreatorCollectorGeminiTest {

    // =========================================================================
    // Test Dummy Fixtures
    // =========================================================================

    static class TargetBean {
        public TargetBean() {}
        public TargetBean(String s) {}
        public TargetBean(int i) {}
        public TargetBean(long l) {}
        public TargetBean(double d) {}
        public TargetBean(boolean b) {}
        public TargetBean(List<?> list) {}
        public TargetBean(CharSequence cs) {}
        public TargetBean(String s, int i) {}

        public static TargetBean factory(String s) {
            return new TargetBean(s);
        }
    }

    // Fixture specifically targeting JacksonDatabind Issue #1476
    static class SimplePojo1476 {
        int intField;
        String anotherField;

        public SimplePojo1476(int intField) {
            this.intField = intField;
        }

        @JsonCreator
        public SimplePojo1476(@JsonProperty("intField") int intField,
                              @JsonProperty("anotherField") String anotherField) {
            this.intField = intField;
            this.anotherField = anotherField;
        }
    }

    // =========================================================================
    // Test Helper Methods
    // =========================================================================

    private AnnotatedConstructor findConstructor(BeanDescription desc, int paramCount, Class<?> firstParamType) {
        for (AnnotatedConstructor ctor : desc.getConstructors()) {
            if (ctor.getParameterCount() == paramCount) {
                if (paramCount == 0 || ctor.getRawParameterType(0) == firstParamType) {
                    return ctor;
                }
            }
        }
        throw new NoSuchElementException("Constructor not found for paramCount=" + paramCount + ", type=" + firstParamType);
    }

    private CreatorProperty makeProp(String name) {
        return new CreatorProperty(new PropertyName(name), TypeFactory.unknownType(),
                null, null, null, null, 0, null, PropertyMetadata.STD_REQUIRED);
    }

    private CreatorProperty makeInjectableProp(String name, Object injectId) {
        return new CreatorProperty(new PropertyName(name), TypeFactory.unknownType(),
                null, null, null, null, 0, injectId, PropertyMetadata.STD_REQUIRED);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testVanillaCollectionTypes() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();

        Class<?>[] collectionClasses = new Class<?>[] { Collection.class, List.class, ArrayList.class };
        for (Class<?> cls : collectionClasses) {
            BeanDescription desc = config.introspect(mapper.constructType(cls));
            CreatorCollector coll = new CreatorCollector(desc, config);
            ValueInstantiator inst = coll.constructValueInstantiator(config);

            assertTrue("Expected Vanilla instantiator for " + cls.getName(), inst instanceof CreatorCollector.Vanilla);
            assertEquals(ArrayList.class.getName(), inst.getValueTypeDesc());
            assertTrue(inst.canInstantiate());
            assertTrue(inst.canCreateUsingDefault());
            try {
                Object obj = inst.createUsingDefault(null);
                assertNotNull(obj);
                assertTrue(obj instanceof ArrayList);
            } catch (IOException e) {
                fail("Failed to instantiate Vanilla collection: " + e.getMessage());
            }
        }
    }

    @Test(timeout = 4000)
    public void testVanillaMapTypes() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();

        Class<?>[] mapClasses = new Class<?>[] { Map.class, LinkedHashMap.class };
        for (Class<?> cls : mapClasses) {
            BeanDescription desc = config.introspect(mapper.constructType(cls));
            CreatorCollector coll = new CreatorCollector(desc, config);
            ValueInstantiator inst = coll.constructValueInstantiator(config);

            assertTrue("Expected Vanilla instantiator for " + cls.getName(), inst instanceof CreatorCollector.Vanilla);
            assertEquals(LinkedHashMap.class.getName(), inst.getValueTypeDesc());
            try {
                Object obj = inst.createUsingDefault(null);
                assertNotNull(obj);
                assertTrue(obj instanceof LinkedHashMap);
            } catch (IOException e) {
                fail("Failed to instantiate Vanilla map: " + e.getMessage());
            }
        }
    }

    @Test(timeout = 4000)
    public void testVanillaHashMap() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();

        BeanDescription desc = config.introspect(mapper.constructType(HashMap.class));
        CreatorCollector coll = new CreatorCollector(desc, config);
        ValueInstantiator inst = coll.constructValueInstantiator(config);

        assertTrue(inst instanceof CreatorCollector.Vanilla);
        assertEquals(HashMap.class.getName(), inst.getValueTypeDesc());
        try {
            Object obj = inst.createUsingDefault(null);
            assertNotNull(obj);
            assertTrue(obj instanceof HashMap);
        } catch (IOException e) {
            fail("Failed to instantiate Vanilla HashMap: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testVanillaFallbackToStdValueInstantiator() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();

        BeanDescription desc = config.introspect(mapper.constructType(Object.class));
        CreatorCollector coll = new CreatorCollector(desc, config);
        ValueInstantiator inst = coll.constructValueInstantiator(config);

        assertTrue("Expected StdValueInstantiator for Object.class", inst instanceof StdValueInstantiator);
        assertFalse(inst instanceof CreatorCollector.Vanilla);
    }

    @Test(timeout = 4000)
    public void testVanillaDirectInstantiationAndErrors() throws IOException {
        CreatorCollector.Vanilla unknownVanilla = new CreatorCollector.Vanilla(999);
        assertEquals(Object.class.getName(), unknownVanilla.getValueTypeDesc());

        try {
            unknownVanilla.createUsingDefault(null);
            fail("Expected IllegalStateException for unknown Vanilla type");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("Unknown type 999"));
        }
    }

    @Test(timeout = 4000)
    public void testSetDefaultCreatorAndFixAccess() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        BeanDescription desc = config.introspect(mapper.constructType(TargetBean.class));

        CreatorCollector coll = new CreatorCollector(desc, config);
        assertFalse(coll.hasDefaultCreator());

        AnnotatedConstructor ctor0 = findConstructor(desc, 0, null);
        coll.setDefaultCreator(ctor0);

        assertTrue(coll.hasDefaultCreator());
        assertSame(ctor0, coll._creators[CreatorCollector.C_DEFAULT]);
    }

    @Test(timeout = 4000)
    public void testScalarCreators() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        BeanDescription desc = config.introspect(mapper.constructType(TargetBean.class));

        CreatorCollector coll = new CreatorCollector(desc, config);

        AnnotatedConstructor ctorStr = findConstructor(desc, 1, String.class);
        AnnotatedConstructor ctorInt = findConstructor(desc, 1, int.class);
        AnnotatedConstructor ctorLong = findConstructor(desc, 1, long.class);
        AnnotatedConstructor ctorDouble = findConstructor(desc, 1, double.class);
        AnnotatedConstructor ctorBool = findConstructor(desc, 1, boolean.class);

        coll.addStringCreator(ctorStr, true);
        coll.addIntCreator(ctorInt, true);
        coll.addLongCreator(ctorLong, true);
        coll.addDoubleCreator(ctorDouble, true);
        coll.addBooleanCreator(ctorBool, true);

        assertSame(ctorStr, coll._creators[CreatorCollector.C_STRING]);
        assertSame(ctorInt, coll._creators[CreatorCollector.C_INT]);
        assertSame(ctorLong, coll._creators[CreatorCollector.C_LONG]);
        assertSame(ctorDouble, coll._creators[CreatorCollector.C_DOUBLE]);
        assertSame(ctorBool, coll._creators[CreatorCollector.C_BOOLEAN]);

        // When non-default creator is added to a collection type, Vanilla MUST NOT be used
        BeanDescription collDesc = config.introspect(mapper.constructType(ArrayList.class));
        CreatorCollector collWithCreator = new CreatorCollector(collDesc, config);
        collWithCreator.addStringCreator(ctorStr, true);
        ValueInstantiator inst = collWithCreator.constructValueInstantiator(config);
        assertFalse("Adding a non-default creator must disable Vanilla optimization", inst instanceof CreatorCollector.Vanilla);
    }

    @Test(timeout = 4000)
    public void testDelegatingCreatorsCollectionVsStandard() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        BeanDescription desc = config.introspect(mapper.constructType(TargetBean.class));

        CreatorCollector coll = new CreatorCollector(desc, config);
        assertFalse(coll.hasDelegatingCreator());

        AnnotatedConstructor ctorList = findConstructor(desc, 1, List.class);
        SettableBeanProperty[] arrProps = new SettableBeanProperty[] { makeProp("p1") };
        coll.addDelegatingCreator(ctorList, true, arrProps);

        // List is collection-like -> C_ARRAY_DELEGATE
        assertSame(ctorList, coll._creators[CreatorCollector.C_ARRAY_DELEGATE]);
        assertSame(arrProps, coll._arrayDelegateArgs);
        assertFalse("Array delegate is not standard delegate", coll.hasDelegatingCreator());

        AnnotatedConstructor ctorStr = findConstructor(desc, 1, String.class);
        SettableBeanProperty[] delegateProps = new SettableBeanProperty[] { makeProp("p2") };
        coll.addDelegatingCreator(ctorStr, true, delegateProps);

        assertTrue("hasDelegatingCreator must be true after C_DELEGATE", coll.hasDelegatingCreator());
        assertSame(ctorStr, coll._creators[CreatorCollector.C_DELEGATE]);
        assertSame(delegateProps, coll._delegateArgs);
    }

    @Test(timeout = 4000)
    public void testDelegateTypeComputationWithNullMarker() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        BeanDescription desc = config.introspect(mapper.constructType(TargetBean.class));

        CreatorCollector coll = new CreatorCollector(desc, config);
        AnnotatedConstructor ctor2 = findConstructor(desc, 2, String.class);

        // Injectable props where index 1 is null (the marker for the delegate argument itself)
        SettableBeanProperty[] delegateProps = new SettableBeanProperty[] {
            makeInjectableProp("injectable", "id"),
            null // null marker: ix = 1
        };

        coll.addDelegatingCreator(ctor2, true, delegateProps);
        ValueInstantiator vi = coll.constructValueInstantiator(config);

        assertTrue(vi instanceof StdValueInstantiator);
        StdValueInstantiator stdVi = (StdValueInstantiator) vi;
        JavaType delType = stdVi.getDelegateType(config);
        assertNotNull(delType);
        assertEquals(int.class, delType.getRawClass());
    }

    @Test(timeout = 4000)
    public void testAddIncompleteParameter() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        BeanDescription desc = config.introspect(mapper.constructType(TargetBean.class));

        CreatorCollector coll = new CreatorCollector(desc, config);
        AnnotatedConstructor ctor1 = findConstructor(desc, 1, String.class);
        AnnotatedParameter p0 = ctor1.getParameter(0);
        AnnotatedConstructor ctor2 = findConstructor(desc, 1, int.class);
        AnnotatedParameter p1 = ctor2.getParameter(0);

        coll.addIncompeteParameter(p0);
        assertSame(p0, coll._incompleteParameter);

        // Secondary call should not overwrite existing incomplete parameter
        coll.addIncompeteParameter(p1);
        assertSame(p0, coll._incompleteParameter);

        StdValueInstantiator vi = (StdValueInstantiator) coll.constructValueInstantiator(config);
        assertSame(p0, vi.getIncompleteParameter());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testAddPropertyCreatorEmptyAndSingleProperties() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        BeanDescription desc = config.introspect(mapper.constructType(TargetBean.class));

        CreatorCollector coll = new CreatorCollector(desc, config);
        AnnotatedConstructor ctor0 = findConstructor(desc, 0, null);

        // Boundary: 0 properties (properties.length == 0)
        coll.addPropertyCreator(ctor0, true, new SettableBeanProperty[0]);
        assertTrue(coll.hasPropertyBasedCreator());
        assertEquals(0, coll._propertyBasedArgs.length);

        // Boundary: 1 property (properties.length == 1: bypasses duplicate detection loop)
        AnnotatedConstructor ctor1 = findConstructor(desc, 1, String.class);
        SettableBeanProperty[] singleProp = new SettableBeanProperty[] { makeProp("one") };
        coll.addPropertyCreator(ctor1, true, singleProp);
        assertSame(singleProp, coll._propertyBasedArgs);
    }

    @Test(timeout = 4000)
    public void testAddPropertyCreatorInjectableEmptyNameAllowed() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        BeanDescription desc = config.introspect(mapper.constructType(TargetBean.class));

        CreatorCollector coll = new CreatorCollector(desc, config);
        AnnotatedConstructor ctor2 = findConstructor(desc, 2, String.class);

        // Both have empty name "", but both have an injectable ID -> should NOT trigger duplicate conflict
        SettableBeanProperty[] props = new SettableBeanProperty[] {
            makeInjectableProp("", "id1"),
            makeInjectableProp("", "id2")
        };

        coll.addPropertyCreator(ctor2, false, props);
        assertSame(props, coll._propertyBasedArgs);
    }

    @Test(timeout = 4000)
    public void testComputeDelegateTypeEdgeCases() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        BeanDescription desc = config.introspect(mapper.constructType(TargetBean.class));

        // When _hasNonDefaultCreator is false, delegateType must be null
        CreatorCollector coll = new CreatorCollector(desc, config);
        ValueInstantiator vi = coll.constructValueInstantiator(config);
        assertNull(vi.getDelegateType(config));
        assertNull(vi.getArrayDelegateType(config));

        // When delegating creator is provided with null args array, default index is 0
        AnnotatedConstructor ctorStr = findConstructor(desc, 1, String.class);
        coll.addDelegatingCreator(ctorStr, true, null);
        ValueInstantiator vi2 = coll.constructValueInstantiator(config);
        assertEquals(String.class, vi2.getDelegateType(config).getRawClass());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (JacksonDatabind #1476)
    // =========================================================================

    /**
     * Targets Defects4J ground truth failure:
     * com.fasterxml.jackson.databind.creators.Creator1476Test::testConstructorChoice
     * Deserializing SimplePojo1476 must not fail with "Could not find creator property with name 'intField'".
     */
    @Test(timeout = 4000)
    public void testDefect1476ConstructorChoiceViaMapper() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"intField\":10,\"anotherField\":\"defVal\"}";
        SimplePojo1476 result = mapper.readValue(json, SimplePojo1476.class);

        assertNotNull(result);
        assertEquals(10, result.intField);
        assertEquals("defVal", result.anotherField);
    }

    /**
     * Root Cause Verification on CreatorCollector:
     * When an explicit property creator is registered, a subsequent non-explicit
     * property creator must be rejected by verifyNonDup AND its properties
     * MUST NOT overwrite _propertyBasedArgs.
     */
    @Test(timeout = 4000)
    public void testDefectPropertyArgsNotOverwrittenByImplicit() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        BeanDescription desc = config.introspect(mapper.constructType(TargetBean.class));

        CreatorCollector coll = new CreatorCollector(desc, config);

        AnnotatedConstructor explicitCtor = findConstructor(desc, 2, String.class);
        SettableBeanProperty[] explicitProps = new SettableBeanProperty[] {
            makeProp("explicit1"),
            makeProp("explicit2")
        };
        coll.addPropertyCreator(explicitCtor, true, explicitProps);

        AnnotatedConstructor implicitCtor = findConstructor(desc, 1, String.class);
        SettableBeanProperty[] implicitProps = new SettableBeanProperty[] {
            makeProp("implicitOnly")
        };

        // Calling with explicit = false: verifyNonDup returns early without updating _creators[C_PROPS].
        coll.addPropertyCreator(implicitCtor, false, implicitProps);

        StdValueInstantiator vi = (StdValueInstantiator) coll.constructValueInstantiator(config);
        SettableBeanProperty[] finalArgs = vi.getFromObjectArguments(config);

        // In the presence of bug #1476, finalArgs becomes implicitProps (length 1)
        // despite explicitCtor being kept in _creators[C_PROPS].
        assertNotNull(finalArgs);
        assertEquals("Arguments must match the surviving explicit creator (2 arguments)", 2, finalArgs.length);
        assertEquals("explicit1", finalArgs[0].getName());
        assertEquals("explicit2", finalArgs[1].getName());
    }

    /**
     * When two explicit property creators are added and the second has a more generic parameter,
     * verifyNonDup retains the first creator. The second creator's arguments MUST NOT overwrite the first's.
     */
    @Test(timeout = 4000)
    public void testDefectPropertyArgsNotOverwrittenWhenGenericIgnored() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        BeanDescription desc = config.introspect(mapper.constructType(TargetBean.class));

        CreatorCollector coll = new CreatorCollector(desc, config);

        AnnotatedConstructor specificCtor = findConstructor(desc, 1, String.class);
        SettableBeanProperty[] specificProps = new SettableBeanProperty[] { makeProp("specificParam") };
        coll.addPropertyCreator(specificCtor, true, specificProps);

        AnnotatedConstructor genericCtor = findConstructor(desc, 1, CharSequence.class);
        SettableBeanProperty[] genericProps = new SettableBeanProperty[] { makeProp("genericParam") };
        coll.addPropertyCreator(genericCtor, true, genericProps);

        StdValueInstantiator vi = (StdValueInstantiator) coll.constructValueInstantiator(config);
        SettableBeanProperty[] finalArgs = vi.getFromObjectArguments(config);

        assertNotNull(finalArgs);
        assertEquals("Properties must not be overwritten by generic creator", "specificParam", finalArgs[0].getName());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testAddPropertyCreatorDuplicateNamesThrow() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        BeanDescription desc = config.introspect(mapper.constructType(TargetBean.class));

        CreatorCollector coll = new CreatorCollector(desc, config);
        AnnotatedConstructor ctor2 = findConstructor(desc, 2, String.class);

        SettableBeanProperty[] duplicateProps = new SettableBeanProperty[] {
            makeProp("sharedName"),
            makeProp("sharedName")
        };

        try {
            coll.addPropertyCreator(ctor2, false, duplicateProps);
            fail("Expected IllegalArgumentException for duplicate creator property names");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Duplicate creator property \"sharedName\""));
        }
    }

    @Test(timeout = 4000)
    public void testAddPropertyCreatorDuplicateEmptyNameWithoutInjectableThrow() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        BeanDescription desc = config.introspect(mapper.constructType(TargetBean.class));

        CreatorCollector coll = new CreatorCollector(desc, config);
        AnnotatedConstructor ctor2 = findConstructor(desc, 2, String.class);

        SettableBeanProperty[] duplicateEmpty = new SettableBeanProperty[] {
            makeProp(""),
            makeProp("")
        };

        try {
            coll.addPropertyCreator(ctor2, false, duplicateEmpty);
            fail("Expected IllegalArgumentException for duplicate empty creator property names without injectable id");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Duplicate creator property \"\""));
        }
    }

    @Test(timeout = 4000)
    public void testVerifyNonDupConflictingIdenticalTypesThrow() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        BeanDescription desc = config.introspect(mapper.constructType(TargetBean.class));

        CreatorCollector coll = new CreatorCollector(desc, config);
        AnnotatedConstructor ctorStr = findConstructor(desc, 1, String.class);

        coll.addStringCreator(ctorStr, true);

        try {
            // Attempt to add the exact same type with explicit = true
            coll.addStringCreator(ctorStr, true);
            fail("Expected IllegalArgumentException for conflicting String creators");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Conflicting String creators"));
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle, Subtyping & Deprecated API Coverage
    // =========================================================================

    @Test(timeout = 4000)
    public void testVerifyNonDupExplicitOverridesImplicit() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        BeanDescription desc = config.introspect(mapper.constructType(TargetBean.class));

        CreatorCollector coll = new CreatorCollector(desc, config);
        AnnotatedConstructor ctor1 = findConstructor(desc, 1, String.class);

        // Initially non-explicit
        coll.addStringCreator(ctor1, false);
        assertEquals(0, coll._explicitCreators & (1 << CreatorCollector.C_STRING));

        // Explicit overrides non-explicit
        coll.addStringCreator(ctor1, true);
        assertEquals(1 << CreatorCollector.C_STRING, coll._explicitCreators & (1 << CreatorCollector.C_STRING));
    }

    @Test(timeout = 4000)
    public void testVerifyNonDupMoreSpecificTypeOverrides() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        BeanDescription desc = config.introspect(mapper.constructType(TargetBean.class));

        CreatorCollector coll = new CreatorCollector(desc, config);
        AnnotatedConstructor ctorCS = findConstructor(desc, 1, CharSequence.class);
        AnnotatedConstructor ctorStr = findConstructor(desc, 1, String.class);

        // Register generic first (CharSequence)
        coll.addStringCreator(ctorCS, false);
        assertSame(ctorCS, coll._creators[CreatorCollector.C_STRING]);

        // Register more specific (String is assignable to CharSequence, but CharSequence is not assignable to String)
        coll.addStringCreator(ctorStr, false);
        assertSame("More specific type (String) must replace generic type (CharSequence)",
                ctorStr, coll._creators[CreatorCollector.C_STRING]);
    }

    @Test(timeout = 4000)
    public void testVerifyNonDupGenericTypeIgnored() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        BeanDescription desc = config.introspect(mapper.constructType(TargetBean.class));

        CreatorCollector coll = new CreatorCollector(desc, config);
        AnnotatedConstructor ctorCS = findConstructor(desc, 1, CharSequence.class);
        AnnotatedConstructor ctorStr = findConstructor(desc, 1, String.class);

        // Register specific first (String)
        coll.addStringCreator(ctorStr, false);
        assertSame(ctorStr, coll._creators[CreatorCollector.C_STRING]);

        // Register generic next (CharSequence) -> Should be ignored
        coll.addStringCreator(ctorCS, false);
        assertSame("Generic type must not replace existing specific creator",
                ctorStr, coll._creators[CreatorCollector.C_STRING]);
    }

    @Test(timeout = 4000)
    public void testVerifyNonDupDifferentCreatorClasses() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        BeanDescription desc = config.introspect(mapper.constructType(TargetBean.class));

        CreatorCollector coll = new CreatorCollector(desc, config);
        AnnotatedConstructor ctorStr = findConstructor(desc, 1, String.class);
        List<AnnotatedMethod> factories = desc.getFactoryMethods();
        assertFalse(factories.isEmpty());
        AnnotatedMethod factoryMethod = factories.get(0);

        coll.addStringCreator(ctorStr, false);
        assertSame(ctorStr, coll._creators[CreatorCollector.C_STRING]);

        // Method vs Constructor have different classes (AnnotatedMethod.class != AnnotatedConstructor.class)
        // Should replace without throwing conflict exception
        coll.addStringCreator(factoryMethod, false);
        assertSame("Different AnnotatedMember class should override without conflict error",
                factoryMethod, coll._creators[CreatorCollector.C_STRING]);
    }

    @SuppressWarnings("deprecation")
    @Test(timeout = 4000)
    public void testDeprecatedCreatorMethods() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        BeanDescription desc = config.introspect(mapper.constructType(TargetBean.class));

        CreatorCollector coll = new CreatorCollector(desc, config);
        AnnotatedConstructor ctorStr = findConstructor(desc, 1, String.class);
        AnnotatedConstructor ctorBool = findConstructor(desc, 1, boolean.class);

        coll.addStringCreator(ctorStr);
        assertSame(ctorStr, coll._creators[CreatorCollector.C_STRING]);

        // Jackson 2.5 deprecated methods delegate to addBooleanCreator
        coll.addIntCreator(ctorBool);
        assertSame(ctorBool, coll._creators[CreatorCollector.C_BOOLEAN]);

        coll.addLongCreator(ctorBool);
        assertSame(ctorBool, coll._creators[CreatorCollector.C_BOOLEAN]);

        coll.addDoubleCreator(ctorBool);
        assertSame(ctorBool, coll._creators[CreatorCollector.C_BOOLEAN]);

        coll.addBooleanCreator(ctorBool);
        assertSame(ctorBool, coll._creators[CreatorCollector.C_BOOLEAN]);

        CreatorProperty[] dummyProps = new CreatorProperty[] { makeProp("depProp") };
        coll.addPropertyCreator(ctorStr, dummyProps);
        assertSame(dummyProps, coll._propertyBasedArgs);

        coll.addDelegatingCreator(ctorStr, dummyProps);
        assertSame(dummyProps, coll._delegateArgs);
    }
}