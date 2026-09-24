package com.fasterxml.jackson.databind.deser.impl;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyMetadata;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.introspect.AnnotatedConstructor;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;

/*
 * [Branch & Defect Analysis Matrix]
 * Class Under Test: com.fasterxml.jackson.databind.deser.impl.InnerClassProperty
 *
 * Targeted Decision Branches:
 * 1. Constructor (InnerClassProperty, AnnotatedConstructor):
 *    - Branch: _annotated == null -> _creator = null -> throws IllegalArgumentException
 *    - Branch: _annotated != null, _annotated.getAnnotated() == null -> throws IllegalArgumentException
 *    - Branch: _annotated != null, _annotated.getAnnotated() != null -> success
 * 2. deserializeAndSet(JsonParser, DeserializationContext, Object):
 *    - Branch: t == JsonToken.VALUE_NULL -> _valueDeserializer.getNullValue(ctxt)
 *    - Branch: _valueTypeDeserializer != null -> _valueDeserializer.deserializeWithType(...)
 *    - Branch: usual case -> _creator.newInstance(bean) followed by deserialize & set
 *    - Branch: exception inside _creator.newInstance(bean) -> caught and unwrapped via ClassUtil.unwrapAndThrowAsIAE
 * 3. writeReplace():
 *    - Branch: _annotated != null -> returns this
 *    - Branch: _annotated == null -> returns new InnerClassProperty with wrapped AnnotatedConstructor
 * 4. readResolve():
 *    - Invokes constructor (InnerClassProperty, _annotated)
 * 5. SettableBeanProperty delegation:
 *    - withName(), withValueDeserializer(), assignIndex(), getPropertyIndex(),
 *      getAnnotation(), getMember(), set(), setAndReturn(), deserializeSetAndReturn()
 * 6. Defect-Targeted Zone (Defects4J Issue 1501):
 *    - InnerClassProperty wrapping CreatorProperty: calls getCreatorIndex()
 *    - Buggy behavior: SettableBeanProperty.getCreatorIndex() throws IllegalStateException
 *    - Expected behavior: delegates to _delegate.getCreatorIndex()
 */
public class InnerClassPropertyGeminiTest {

    @Retention(RetentionPolicy.RUNTIME)
    private @interface CustomAnnotation {
        String value() default "";
    }

    public class OuterClass {
        public InnerClass inner;

        public class InnerClass {
            public String text;
            public InnerClass() {}
        }

        public class ThrowingInnerClass {
            public ThrowingInnerClass() {
                throw new IllegalStateException("Simulated construction failure in InnerClass");
            }
        }
    }

    static class DummyTypeDeserializer extends TypeDeserializer {
        @Override public TypeDeserializer forProperty(BeanProperty prop) { return this; }
        @Override public As getTypeInclusion() { return As.PROPERTY; }
        @Override public String getPropertyName() { return "@type"; }
        @Override public TypeIdResolver getTypeIdResolver() { return null; }
        @Override public Class<?> getDefaultImpl() { return null; }
        @Override public Object deserializeTypedFromObject(JsonParser p, DeserializationContext ctxt) { return null; }
        @Override public Object deserializeTypedFromArray(JsonParser p, DeserializationContext ctxt) { return null; }
        @Override public Object deserializeTypedFromScalar(JsonParser p, DeserializationContext ctxt) { return null; }
        @Override public Object deserializeTypedFromAny(JsonParser p, DeserializationContext ctxt) { return null; }
    }

    static class TestSettableProperty extends SettableBeanProperty {
        private static final long serialVersionUID = 1L;
        private final int creatorIndex;
        private Object assignedValue;
        private int propertyIndex = -1;

        public TestSettableProperty(String name, int creatorIndex) {
            super(new PropertyName(name),
                  TypeFactory.defaultInstance().constructType(Object.class),
                  PropertyMetadata.STD_OPTIONAL,
                  null);
            this.creatorIndex = creatorIndex;
        }

        public TestSettableProperty(String name, TypeDeserializer typeDeser) {
            super(new PropertyName(name),
                  TypeFactory.defaultInstance().constructType(Object.class),
                  new PropertyName(name),
                  typeDeser,
                  null,
                  PropertyMetadata.STD_OPTIONAL);
            this.creatorIndex = -1;
        }

        protected TestSettableProperty(TestSettableProperty src, JsonDeserializer<?> deser) {
            super(src, deser);
            this.creatorIndex = src.creatorIndex;
            this.assignedValue = src.assignedValue;
            this.propertyIndex = src.propertyIndex;
        }

        protected TestSettableProperty(TestSettableProperty src, PropertyName name) {
            super(src, name);
            this.creatorIndex = src.creatorIndex;
            this.assignedValue = src.assignedValue;
            this.propertyIndex = src.propertyIndex;
        }

        @Override
        public int getCreatorIndex() {
            return creatorIndex;
        }

        @Override
        public SettableBeanProperty withValueDeserializer(JsonDeserializer<?> deser) {
            return new TestSettableProperty(this, deser);
        }

        @Override
        public SettableBeanProperty withName(PropertyName newName) {
            return new TestSettableProperty(this, newName);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <A extends Annotation> A getAnnotation(Class<A> acls) {
            if (acls == CustomAnnotation.class) {
                return (A) OuterClass.class.getAnnotation(CustomAnnotation.class);
            }
            return null;
        }

        @Override
        public AnnotatedMember getMember() {
            return null;
        }

        @Override
        public void deserializeAndSet(JsonParser p, DeserializationContext ctxt, Object instance) throws IOException {
            set(instance, deserialize(p, ctxt));
        }

        @Override
        public Object deserializeSetAndReturn(JsonParser p, DeserializationContext ctxt, Object instance) throws IOException {
            return setAndReturn(instance, deserialize(p, ctxt));
        }

        @Override
        public void set(Object instance, Object value) throws IOException {
            this.assignedValue = value;
        }

        @Override
        public Object setAndReturn(Object instance, Object value) throws IOException {
            set(instance, value);
            return "setAndReturn:" + value;
        }

        @Override
        public void assignIndex(int index) {
            this.propertyIndex = index;
        }

        @Override
        public int getPropertyIndex() {
            return this.propertyIndex;
        }

        public Object getAssignedValue() {
            return assignedValue;
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorAndBasicDelegates() throws Exception {
        TestSettableProperty delegate = new TestSettableProperty("testProp", 0);
        Constructor<?> ctor = OuterClass.InnerClass.class.getDeclaredConstructor(OuterClass.class);

        InnerClassProperty prop = new InnerClassProperty(delegate, ctor);

        assertEquals("testProp", prop.getName());
        assertNull(prop.getMember());
        assertNull(prop.getAnnotation(CustomAnnotation.class));

        prop.assignIndex(7);
        assertEquals(7, prop.getPropertyIndex());
        assertEquals(7, delegate.getPropertyIndex());
    }

    @Test(timeout = 4000)
    public void testWithNameTransformation() throws Exception {
        TestSettableProperty delegate = new TestSettableProperty("initialName", 1);
        Constructor<?> ctor = OuterClass.InnerClass.class.getDeclaredConstructor(OuterClass.class);

        InnerClassProperty prop = new InnerClassProperty(delegate, ctor);
        InnerClassProperty renamed = prop.withName(new PropertyName("renamedProp"));

        assertNotSame(prop, renamed);
        assertEquals("renamedProp", renamed.getName());
        assertEquals("initialName", prop.getName());
    }

    @Test(timeout = 4000)
    public void testWithValueDeserializerTransformation() throws Exception {
        TestSettableProperty delegate = new TestSettableProperty("prop", 1);
        Constructor<?> ctor = OuterClass.InnerClass.class.getDeclaredConstructor(OuterClass.class);

        InnerClassProperty prop = new InnerClassProperty(delegate, ctor);

        JsonDeserializer<Object> deser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return "deserVal";
            }
        };

        InnerClassProperty transformed = prop.withValueDeserializer(deser);
        assertNotSame(prop, transformed);
        assertSame(deser, transformed.getValueDeserializer());
    }

    @Test(timeout = 4000)
    public void testSetAndSetAndReturnDelegation() throws Exception {
        TestSettableProperty delegate = new TestSettableProperty("prop", 1);
        Constructor<?> ctor = OuterClass.InnerClass.class.getDeclaredConstructor(OuterClass.class);

        InnerClassProperty prop = new InnerClassProperty(delegate, ctor);
        OuterClass outer = new OuterClass();

        prop.set(outer, "myValue");
        assertEquals("myValue", delegate.getAssignedValue());

        Object result = prop.setAndReturn(outer, "myValue2");
        assertEquals("setAndReturn:myValue2", result);
        assertEquals("myValue2", delegate.getAssignedValue());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Deserialization Branches
    // =========================================================================

    @Test(timeout = 4000)
    public void testDeserializeAndSetNullTokenBranch() throws Exception {
        TestSettableProperty delegate = new TestSettableProperty("prop", -1);
        Constructor<?> ctor = OuterClass.InnerClass.class.getDeclaredConstructor(OuterClass.class);
        InnerClassProperty prop = new InnerClassProperty(delegate, ctor);

        JsonDeserializer<Object> nullHandlingDeser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return null;
            }

            @Override
            public Object getNullValue(DeserializationContext ctxt) {
                return "customNullRepresentation";
            }
        };

        InnerClassProperty activeProp = prop.withValueDeserializer(nullHandlingDeser);

        ObjectMapper mapper = new ObjectMapper();
        JsonParser parser = mapper.getFactory().createParser("null");
        parser.nextToken(); // position to VALUE_NULL

        OuterClass outer = new OuterClass();
        activeProp.deserializeAndSet(parser, mapper.getDeserializationContext(), outer);

        assertEquals("customNullRepresentation", delegate.getAssignedValue());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testDeserializeAndSetWithTypeDeserializerBranch() throws Exception {
        DummyTypeDeserializer typeDeser = new DummyTypeDeserializer();
        TestSettableProperty delegate = new TestSettableProperty("typedProp", typeDeser);
        Constructor<?> ctor = OuterClass.InnerClass.class.getDeclaredConstructor(OuterClass.class);

        InnerClassProperty prop = new InnerClassProperty(delegate, ctor);

        JsonDeserializer<Object> deser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return null;
            }

            @Override
            public Object deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer td) {
                return "typedDeserializedValue";
            }
        };

        InnerClassProperty activeProp = prop.withValueDeserializer(deser);

        ObjectMapper mapper = new ObjectMapper();
        JsonParser parser = mapper.getFactory().createParser("{\"@type\":\"dummy\"}");
        parser.nextToken(); // START_OBJECT

        OuterClass outer = new OuterClass();
        activeProp.deserializeAndSet(parser, mapper.getDeserializationContext(), outer);

        assertEquals("typedDeserializedValue", delegate.getAssignedValue());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testDeserializeAndSetUsualCaseBranch() throws Exception {
        TestSettableProperty delegate = new TestSettableProperty("innerProp", -1);
        Constructor<?> ctor = OuterClass.InnerClass.class.getDeclaredConstructor(OuterClass.class);
        InnerClassProperty prop = new InnerClassProperty(delegate, ctor);

        JsonDeserializer<Object> deser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return null;
            }

            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt, Object intoValue) {
                ((OuterClass.InnerClass) intoValue).text = "populatedInner";
                return intoValue;
            }
        };

        InnerClassProperty activeProp = prop.withValueDeserializer(deser);

        ObjectMapper mapper = new ObjectMapper();
        JsonParser parser = mapper.getFactory().createParser("{\"text\":\"populatedInner\"}");
        parser.nextToken(); // START_OBJECT

        OuterClass outer = new OuterClass();
        activeProp.deserializeAndSet(parser, mapper.getDeserializationContext(), outer);

        assertNotNull(delegate.getAssignedValue());
        assertTrue(delegate.getAssignedValue() instanceof OuterClass.InnerClass);
        assertEquals("populatedInner", ((OuterClass.InnerClass) delegate.getAssignedValue()).text);
        parser.close();
    }

    @Test(timeout = 4000)
    public void testDeserializeSetAndReturn() throws Exception {
        TestSettableProperty delegate = new TestSettableProperty("prop", -1);
        Constructor<?> ctor = OuterClass.InnerClass.class.getDeclaredConstructor(OuterClass.class);
        InnerClassProperty prop = new InnerClassProperty(delegate, ctor);

        JsonDeserializer<Object> deser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return "returnedDeserVal";
            }
        };

        InnerClassProperty activeProp = prop.withValueDeserializer(deser);
        ObjectMapper mapper = new ObjectMapper();
        JsonParser parser = mapper.getFactory().createParser("\"someString\"");
        parser.nextToken();

        OuterClass outer = new OuterClass();
        Object returnVal = activeProp.deserializeSetAndReturn(parser, mapper.getDeserializationContext(), outer);

        assertEquals("setAndReturn:returnedDeserVal", returnVal);
        assertEquals("returnedDeserVal", delegate.getAssignedValue());
        parser.close();
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Issue 1501)
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefectIssue1501CreatorIndexDelegation() throws Exception {
        TestSettableProperty delegate = new TestSettableProperty("a", 3);
        Constructor<?> ctor = OuterClass.InnerClass.class.getDeclaredConstructor(OuterClass.class);
        InnerClassProperty prop = new InnerClassProperty(delegate, ctor);

        // Defects4J Issue 1501: InnerClassProperty must delegate getCreatorIndex() to _delegate
        // In the defective version, SettableBeanProperty.getCreatorIndex() is executed and throws IllegalStateException
        assertEquals(3, prop.getCreatorIndex());
    }

    @Test(timeout = 4000)
    public void testDefectIssue1501CreatorIndexBoundaryZero() throws Exception {
        TestSettableProperty delegate = new TestSettableProperty("firstParam", 0);
        Constructor<?> ctor = OuterClass.InnerClass.class.getDeclaredConstructor(OuterClass.class);
        InnerClassProperty prop = new InnerClassProperty(delegate, ctor);

        assertEquals(0, prop.getCreatorIndex());
    }

    static class Outer1501 {
        public Inner1501 inner;

        @JsonCreator
        public Outer1501(@JsonProperty("a") Inner1501 a) {
            this.inner = a;
        }

        public class Inner1501 {
            public int a;
            public Inner1501() {}
        }
    }

    @Test(timeout = 4000)
    public void testDefectIssue1501ObjectMapperIntegration() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Outer1501 outer = mapper.readValue("{\"a\":{\"a\":42}}", Outer1501.class);
        assertNotNull(outer);
        assertNotNull(outer.inner);
        assertEquals(42, outer.inner.a);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testInstantiationExceptionUnwrappedAsIAE() throws Exception {
        TestSettableProperty delegate = new TestSettableProperty("throwingProp", -1);
        Constructor<?> ctor = OuterClass.ThrowingInnerClass.class.getDeclaredConstructor(OuterClass.class);
        InnerClassProperty prop = new InnerClassProperty(delegate, ctor);

        JsonDeserializer<Object> deser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return null;
            }
        };

        InnerClassProperty activeProp = prop.withValueDeserializer(deser);
        ObjectMapper mapper = new ObjectMapper();
        JsonParser parser = mapper.getFactory().createParser("{}");
        parser.nextToken();

        OuterClass outer = new OuterClass();
        try {
            activeProp.deserializeAndSet(parser, mapper.getDeserializationContext(), outer);
            fail("Expected IllegalArgumentException wrapping construction failure");
        } catch (IllegalArgumentException iae) {
            assertTrue("Expected message containing instantiating problem",
                    iae.getMessage().contains("Failed to instantiate class"));
            assertTrue("Expected root cause message",
                    iae.getMessage().contains("Simulated construction failure in InnerClass"));
        } finally {
            parser.close();
        }
    }

    @Test(timeout = 4000)
    public void testInstantiationWithWrongEnclosingBeanThrowsIAE() throws Exception {
        TestSettableProperty delegate = new TestSettableProperty("wrongEnclosing", -1);
        Constructor<?> ctor = OuterClass.InnerClass.class.getDeclaredConstructor(OuterClass.class);
        InnerClassProperty prop = new InnerClassProperty(delegate, ctor);

        JsonDeserializer<Object> deser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return null;
            }
        };

        InnerClassProperty activeProp = prop.withValueDeserializer(deser);
        ObjectMapper mapper = new ObjectMapper();
        JsonParser parser = mapper.getFactory().createParser("{}");
        parser.nextToken();

        try {
            activeProp.deserializeAndSet(parser, mapper.getDeserializationContext(), "InvalidBeanType");
            fail("Expected IllegalArgumentException when passing incompatible bean type");
        } catch (IllegalArgumentException iae) {
            assertTrue(iae.getMessage().contains("Failed to instantiate class"));
        } finally {
            parser.close();
        }
    }

    @Test(timeout = 4000)
    public void testConstructorWithNullAnnotatedConstructorThrows() throws Exception {
        TestSettableProperty delegate = new TestSettableProperty("prop", 0);
        Constructor<?> ctor = OuterClass.InnerClass.class.getDeclaredConstructor(OuterClass.class);
        InnerClassProperty baseProp = new InnerClassProperty(delegate, ctor);

        try {
            new InnerClassProperty(baseProp, (AnnotatedConstructor) null);
            fail("Expected IllegalArgumentException when AnnotatedConstructor is null");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("Missing constructor (broken JDK (de)serialization?)"));
        }
    }

    @Test(timeout = 4000)
    public void testConstructorWithAnnotatedConstructorWrappingNullThrows() throws Exception {
        TestSettableProperty delegate = new TestSettableProperty("prop", 0);
        Constructor<?> ctor = OuterClass.InnerClass.class.getDeclaredConstructor(OuterClass.class);
        InnerClassProperty baseProp = new InnerClassProperty(delegate, ctor);

        AnnotatedConstructor emptyAnn = new AnnotatedConstructor(null, null, null, null);
        try {
            new InnerClassProperty(baseProp, emptyAnn);
            fail("Expected IllegalArgumentException when annotated.getAnnotated() is null");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("Missing constructor (broken JDK (de)serialization?)"));
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Serialization Handling
    // =========================================================================

    @Test(timeout = 4000)
    public void testWriteReplaceAndReadResolveLifecycle() throws Exception {
        TestSettableProperty delegate = new TestSettableProperty("serialProp", 1);
        Constructor<?> ctor = OuterClass.InnerClass.class.getDeclaredConstructor(OuterClass.class);
        InnerClassProperty prop = new InnerClassProperty(delegate, ctor);

        Object replaced = prop.writeReplace();
        assertNotNull(replaced);
        assertTrue(replaced instanceof InnerClassProperty);
        InnerClassProperty replacedProp = (InnerClassProperty) replaced;
        assertNotNull(replacedProp._annotated);
        assertEquals(ctor, replacedProp._creator);

        // Calling writeReplace again when _annotated != null should return `this`
        Object idempotent = replacedProp.writeReplace();
        assertSame(replacedProp, idempotent);

        // Resolving should reconstitute InnerClassProperty
        Object resolved = replacedProp.readResolve();
        assertNotNull(resolved);
        assertTrue(resolved instanceof InnerClassProperty);
        InnerClassProperty resolvedProp = (InnerClassProperty) resolved;
        assertEquals(ctor, resolvedProp._creator);
    }

    @Test(timeout = 4000)
    public void testReadResolveWithNullAnnotatedThrows() throws Exception {
        TestSettableProperty delegate = new TestSettableProperty("prop", 0);
        Constructor<?> ctor = OuterClass.InnerClass.class.getDeclaredConstructor(OuterClass.class);
        InnerClassProperty prop = new InnerClassProperty(delegate, ctor);
        assertNull(prop._annotated);

        try {
            prop.readResolve();
            fail("Expected readResolve to fail with IllegalArgumentException when _annotated is null");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("Missing constructor (broken JDK (de)serialization?)"));
        }
    }
}