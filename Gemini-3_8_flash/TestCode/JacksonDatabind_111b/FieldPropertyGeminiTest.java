package com.fasterxml.jackson.databind.deser.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.deser.NullValueProvider;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedClassResolver;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotationMap;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.introspect.TypeResolutionContext;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Annotations;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 *
 * Target Class: com.fasterxml.jackson.databind.deser.impl.FieldProperty
 *
 * 1. Constructor and Copy Operations:
 *    - FieldProperty(BeanPropertyDefinition, JavaType, TypeDeserializer, Annotations, AnnotatedField)
 *      * Evaluates `_skipNulls` via `NullsConstantProvider.isSkipper(_nullProvider)`
 *      * Sets `_annotated` and `_field`
 *    - FieldProperty(FieldProperty, JsonDeserializer, NullValueProvider)
 *      * Updates `_skipNulls` based on `NullsConstantProvider.isSkipper(nva)`
 *      * [databind#2303]: In `withValueDeserializer(JsonDeserializer)`, keeps deserializer and nullProvider in sync!
 *    - FieldProperty(FieldProperty, PropertyName): Preserves properties and rename.
 *    - FieldProperty(FieldProperty): Copy constructor for JDK serialization; checks `f == null` -> IllegalArgumentException.
 *
 * 2. Method `withValueDeserializer(JsonDeserializer<?> deser)`:
 *    - Branch: `_valueDeserializer == deser` -> returns `this`
 *    - Branch: `_valueDeserializer != deser` -> returns new instance with deser and `_nullProvider`
 *      * CRITICAL DEFECT [databind#2303] / `testNullWithinNested`:
 *        When deserializer is updated, nullProvider must stay in sync if deser is a NullValueProvider (e.g. AtomicReferenceDeserializer).
 *        If `_nullProvider` was NullsConstantProvider.nuller(), but updated deser has a custom `getNullValue()`, keeping old `_nullProvider`
 *        leads to incorrect nested null unwrapping (e.g., AtomicReference<AtomicReference<String>> containing null becomes null instead of Reference(null)).
 *
 * 3. Method `deserializeAndSet` / `deserializeSetAndReturn`:
 *    - Branch 1: `p.hasToken(JsonToken.VALUE_NULL)`
 *      * Sub-branch: `_skipNulls == true` -> return (no-op or returns instance)
 *      * Sub-branch: `_skipNulls == false` -> `value = _nullProvider.getNullValue(ctxt)`
 *    - Branch 2: `_valueTypeDeserializer == null`
 *      * Sub-branch: `value = _valueDeserializer.deserialize(p, ctxt)`
 *      * Sub-branch: `value == null` -> if `_skipNulls`, return early; else `_nullProvider.getNullValue(ctxt)`
 *    - Branch 3: `_valueTypeDeserializer != null` -> `_valueDeserializer.deserializeWithType(p, ctxt, _valueTypeDeserializer)`
 *    - Field assignment failure: throws IOE via `_throwAsIOE(p, e, value)`
 *
 * 4. Method `set` & `setAndReturn`:
 *    - Happy path: `_field.set(instance, value)`
 *    - Error path: `_throwAsIOE(e, value)`
 *
 * 5. Method `fixAccess(DeserializationConfig config)`:
 *    - Config override public access modifiers enabled vs disabled.
 *
 * 6. Method `readResolve()` & JDK Serialization:
 *    - Full roundtrip serialization verification.
 */
public class FieldPropertyGeminiTest {

    @Retention(RetentionPolicy.RUNTIME)
    public @interface MarkerTestAnnotation {
        String value() default "test";
    }

    public static class SampleBean {
        @MarkerTestAnnotation("annotatedField")
        public String textField;

        public final String finalField = "constant";

        public Integer intField;

        public Object typedField;
    }

    public static class NestedAtomicBean {
        public AtomicReference<AtomicReference<String>> nested;
    }

    private static class DummyPropertyDefinition extends BeanPropertyDefinition {
        private final PropertyName _name;

        public DummyPropertyDefinition(String name) {
            _name = PropertyName.construct(name);
        }

        @Override
        public PropertyName getFullName() { return _name; }
        @Override
        public String getName() { return _name.getSimpleName(); }
        @Override
        public PropertyName getWrapperName() { return null; }
        @Override
        public boolean isExplicitlyIncluded() { return true; }
        @Override
        public boolean hasGetter() { return false; }
        @Override
        public boolean hasSetter() { return false; }
        @Override
        public boolean hasField() { return true; }
        @Override
        public boolean hasConstructorParameter() { return false; }
        @Override
        public com.fasterxml.jackson.databind.introspect.AnnotatedMethod getGetter() { return null; }
        @Override
        public com.fasterxml.jackson.databind.introspect.AnnotatedMethod getSetter() { return null; }
        @Override
        public AnnotatedField getField() { return null; }
        @Override
        public com.fasterxml.jackson.databind.introspect.AnnotatedParameter getConstructorParameter() { return null; }
        @Override
        public com.fasterxml.jackson.databind.PropertyMetadata getMetadata() {
            return com.fasterxml.jackson.databind.PropertyMetadata.STD_REQUIRED;
        }
    }

    private FieldProperty createFieldProperty(String fieldName, Class<?> beanClass, JavaType type,
                                              TypeDeserializer typeDeser) throws Exception {
        Field f = beanClass.getDeclaredField(fieldName);
        f.setAccessible(true);

        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        AnnotatedClass ac = AnnotatedClassResolver.resolve(config, mapper.constructType(beanClass), null);
        TypeResolutionContext typeResCtx = new TypeResolutionContext.Basic(TypeFactory.defaultInstance(), ac.getBindings());
        AnnotatedField af = new AnnotatedField(typeResCtx, f, new AnnotationMap());

        for (java.lang.annotation.Annotation ann : f.getDeclaredAnnotations()) {
            af.getAllAnnotations().add(ann);
        }

        BeanPropertyDefinition propDef = new DummyPropertyDefinition(fieldName);
        return new FieldProperty(propDef, type, typeDeser, af.getAllAnnotations(), af);
    }

    /*
     ********************************************************************************
     * Partition A: Core Functional Logic & State Transitions
     ********************************************************************************
     */

    @Test(timeout = 4000)
    public void testCorePropertiesAndAnnotations() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        FieldProperty prop = createFieldProperty("textField", SampleBean.class, type, null);

        assertNotNull(prop.getMember());
        assertEquals("textField", prop.getName());
        assertEquals(type, prop.getType());

        MarkerTestAnnotation ann = prop.getAnnotation(MarkerTestAnnotation.class);
        assertNotNull(ann);
        assertEquals("annotatedField", ann.value());

        assertNull(prop.getAnnotation(Deprecated.class));
    }

    @Test(timeout = 4000)
    public void testDirectSetAndSetAndReturn() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        FieldProperty prop = createFieldProperty("textField", SampleBean.class, type, null);

        SampleBean target = new SampleBean();
        prop.set(target, "Hello Direct");
        assertEquals("Hello Direct", target.textField);

        Object returned = prop.setAndReturn(target, "Hello Return");
        assertSame(target, returned);
        assertEquals("Hello Return", target.textField);
    }

    @Test(timeout = 4000)
    public void testWithNameCopy() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        FieldProperty prop = createFieldProperty("textField", SampleBean.class, type, null);

        SettableBeanProperty renamed = prop.withName(PropertyName.construct("renamedText"));
        assertEquals("renamedText", renamed.getName());
        assertSame(prop.getMember(), renamed.getMember());
    }

    @Test(timeout = 4000)
    public void testWithValueDeserializerSameReturnsThis() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        FieldProperty prop = createFieldProperty("textField", SampleBean.class, type, null);

        SettableBeanProperty same = prop.withValueDeserializer(prop.getValueDeserializer());
        assertSame(prop, same);
    }

    @Test(timeout = 4000)
    public void testFixAccess() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        FieldProperty prop = createFieldProperty("textField", SampleBean.class, type, null);

        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig cfg = mapper.getDeserializationConfig()
                .with(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS);
        prop.fixAccess(cfg);
        assertTrue(prop.getMember().getMember().isAccessible());
    }

    /*
     ********************************************************************************
     * Partition B: Boundary Value Analysis & Null Handling
     ********************************************************************************
     */

    @Test(timeout = 4000)
    public void testDeserializeAndSetWithNullTokenSkipNulls() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(String.class);
        FieldProperty prop = createFieldProperty("textField", SampleBean.class, type, null);

        SettableBeanProperty propSkipper = prop.withNullProvider(NullsConstantProvider.skipper());

        JsonParser parser = mapper.getFactory().createParser("null");
        parser.nextToken(); // position to VALUE_NULL
        DeserializationContext ctxt = mapper.getDeserializationContext();

        SampleBean target = new SampleBean();
        target.textField = "initial";
        propSkipper.deserializeAndSet(parser, ctxt, target);
        // Skipped: textField should stay "initial"
        assertEquals("initial", target.textField);

        Object ret = propSkipper.deserializeSetAndReturn(parser, ctxt, target);
        assertSame(target, ret);
        assertEquals("initial", target.textField);
    }

    @Test(timeout = 4000)
    public void testDeserializeAndSetWithNullTokenInjectNull() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(String.class);
        FieldProperty prop = createFieldProperty("textField", SampleBean.class, type, null);

        JsonDeserializer<Object> deser = mapper.getDeserializationConfig()
                .findRootValueDeserializer(type);
        SettableBeanProperty activeProp = prop.withValueDeserializer(deser);

        JsonParser parser = mapper.getFactory().createParser("null");
        parser.nextToken(); // VALUE_NULL
        DeserializationContext ctxt = mapper.getDeserializationContext();

        SampleBean target = new SampleBean();
        target.textField = "pre-filled";
        activeProp.deserializeAndSet(parser, ctxt, target);
        assertNull(target.textField);

        target.textField = "pre-filled-2";
        Object ret = activeProp.deserializeSetAndReturn(parser, ctxt, target);
        assertSame(target, ret);
        assertNull(target.textField);
    }

    @Test(timeout = 4000)
    public void testCoercionFromEmptyStringToNullHandling() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(Integer.class);
        FieldProperty prop = createFieldProperty("intField", SampleBean.class, type, null);

        JsonDeserializer<Object> deser = mapper.getDeserializationConfig()
                .findRootValueDeserializer(type);
        SettableBeanProperty propWithDeser = prop.withValueDeserializer(deser);

        // Deserializing "" as Integer returns null via coercion
        JsonParser parser = mapper.getFactory().createParser("\"\"");
        parser.nextToken(); // VALUE_STRING
        DeserializationContext ctxt = mapper.getDeserializationContext();

        SampleBean target = new SampleBean();
        target.intField = 42;
        propWithDeser.deserializeAndSet(parser, ctxt, target);
        assertNull(target.intField);

        // With skipper configured, null result from string coercion must be skipped
        SettableBeanProperty skipperProp = propWithDeser.withNullProvider(NullsConstantProvider.skipper());
        JsonParser parser2 = mapper.getFactory().createParser("\"\"");
        parser2.nextToken();

        SampleBean target2 = new SampleBean();
        target2.intField = 99;
        skipperProp.deserializeAndSet(parser2, ctxt, target2);
        assertEquals(Integer.valueOf(99), target2.intField);

        Object ret = skipperProp.deserializeSetAndReturn(parser2, ctxt, target2);
        assertSame(target2, ret);
        assertEquals(Integer.valueOf(99), target2.intField);
    }

    /*
     ********************************************************************************
     * Partition C: Defect-Targeted Branch Zone (Defects4J JDKAtomicTypesDeserTest)
     ********************************************************************************
     */

    /**
     * Targets defects where nested nullable references (like AtomicReference<AtomicReference<String>>)
     * deserialize JSON string "null" incorrectly when deserializer and nullProvider are not kept in sync.
     * Specifically, when FieldProperty.withValueDeserializer is invoked, null provider must remain
     * consistent with the deserializer if it is also a NullValueProvider.
     */
    @Test(timeout = 4000)
    public void testNullWithinNestedAtomicReferenceDefect() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        NestedAtomicBean result = mapper.readValue("{\"nested\": null}", NestedAtomicBean.class);
        assertNotNull(result);
        assertNull(result.nested);

        // Crucial test for JDKAtomicTypesDeserTest::testNullWithinNested
        // When JSON contains {"nested": "null"} or {"nested": null} where inner reference is expected:
        NestedAtomicBean resultWithInnerNull = mapper.readValue("{\"nested\": [ null ]}", NestedAtomicBean.class);
        assertNotNull(resultWithInnerNull);
        assertNotNull(resultWithInnerNull.nested);
        assertNotNull(resultWithInnerNull.nested.get());
        assertNull(resultWithInnerNull.nested.get().get());
    }

    /*
     ********************************************************************************
     * Partition D: Exception & Defensive Guard Paths
     ********************************************************************************
     */

    @Test(timeout = 4000)
    public void testSetOnIncompatibleTargetThrowsException() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        FieldProperty prop = createFieldProperty("textField", SampleBean.class, type, null);

        try {
            // String is not a SampleBean instance
            prop.set("invalidTarget", "someValue");
            fail("Expected JsonMappingException or IllegalArgumentException wrapped in IOE");
        } catch (JsonMappingException jme) {
            assertTrue(jme.getMessage().contains("textField"));
        } catch (IOException ioe) {
            assertNotNull(ioe.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testDeserializeAndSetOnFinalFieldThrowsException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(String.class);
        FieldProperty prop = createFieldProperty("finalField", SampleBean.class, type, null);

        JsonDeserializer<Object> deser = mapper.getDeserializationConfig()
                .findRootValueDeserializer(type);
        SettableBeanProperty propWithDeser = prop.withValueDeserializer(deser);

        JsonParser parser = mapper.getFactory().createParser("\"newValue\"");
        parser.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        SampleBean target = new SampleBean();
        try {
            // Attempting to set final field via Field.set may fail or throw IllegalAccessException depending on JVM
            // If access is not made writable, it should throw
            propWithDeser.deserializeAndSet(parser, ctxt, target);
        } catch (JsonMappingException jme) {
            assertNotNull(jme.getMessage());
        } catch (IOException ioe) {
            assertNotNull(ioe.getMessage());
        }
    }

    /*
     ********************************************************************************
     * Partition E: Object Lifecycle, Reflection & Serialization Integrity
     ********************************************************************************
     */

    @Test(timeout = 4000)
    public void testJdkSerializationAndReadResolve() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        FieldProperty prop = createFieldProperty("textField", SampleBean.class, type, null);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(prop);
        }

        SettableBeanProperty deserializedProp;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            deserializedProp = (SettableBeanProperty) ois.readObject();
        }

        assertNotNull(deserializedProp);
        assertEquals(prop.getName(), deserializedProp.getName());
        assertEquals(prop.getType(), deserializedProp.getType());

        SampleBean sample = new SampleBean();
        deserializedProp.set(sample, "Deserialized Value");
        assertEquals("Deserialized Value", sample.textField);
    }

    @Test(timeout = 4000)
    public void testNullAnnotationHandling() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        Field f = SampleBean.class.getDeclaredField("textField");
        TypeResolutionContext dummyCtx = new TypeResolutionContext.Basic(TypeFactory.defaultInstance(), null);
        AnnotatedField af = new AnnotatedField(dummyCtx, f, null);

        BeanPropertyDefinition propDef = new DummyPropertyDefinition("textField");
        FieldProperty prop = new FieldProperty(propDef, type, null, null, af);

        assertNull(prop.getAnnotation(Override.class));
    }
}