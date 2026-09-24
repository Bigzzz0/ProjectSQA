package com.fasterxml.jackson.databind.deser;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyMetadata;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.impl.NullsConstantProvider;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.ObjectIdInfo;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Annotations;

/**
 * [Branch & Defect Analysis Matrix]
 * Targets: SettableBeanProperty and SettableBeanProperty.Delegating
 *
 * 1. Lifecycle & Constructors:
 *    - Full ctor with propDef / PropertyName, wrapperName, typeDeser, annotations, metadata.
 *    - Null PropertyName fallback to PropertyName.NO_NAME and internSimpleName.
 *    - TypeDeserializer contextualization with forProperty(this).
 *    - Special ObjectIdValueProperty ctor (4 params).
 *    - Copy ctor (shallow copy of all fields including _propertyIndex, _nullProvider, etc.).
 *    - Copy-with-deserializer-change: deser == null -> MISSING_VALUE_DESERIALIZER;
 *      nuller == MISSING_VALUE_DESERIALIZER -> assigns _valueDeserializer to nuller.
 *    - Copy-with-name ctor: updates _propName.
 *
 * 2. Fluent Mutators & Accessors:
 *    - withSimpleName: same name vs different name branching.
 *    - assignIndex: success on -1; throw IllegalStateException on reassignment.
 *    - getCreatorIndex: unconditionally throws IllegalStateException.
 *    - fixAccess, markAsIgnorable, isIgnorable (defaults).
 *    - getDeclaringClass via member.
 *    - depositSchemaProperty: isRequired true vs false branches.
 *    - hasViews, visibleInView: null view matcher vs configured view matching.
 *
 * 3. Deserialization Pathways:
 *    - deserialize():
 *      * token == VALUE_NULL -> _nullProvider.getNullValue()
 *      * _valueTypeDeserializer != null -> deserializeWithType
 *      * value == null coercion -> _nullProvider.getNullValue()
 *    - deserializeWith():
 *      * token == VALUE_NULL with skipper -> returns toUpdate
 *      * token == VALUE_NULL without skipper -> returns _nullProvider.getNullValue()
 *      * _valueTypeDeserializer != null -> calls ctxt.reportBadDefinition
 *      * value == null with skipper -> returns toUpdate
 *      * value == null without skipper -> returns _nullProvider.getNullValue()
 *
 * 4. Exception Handling (_throwAsIOE):
 *    - IllegalArgumentException with non-null message -> JsonMappingException formatted.
 *    - IllegalArgumentException with null message -> "(no error message provided)".
 *    - IOException rethrown directly without rewrapping.
 *    - RuntimeException rethrown directly without rewrapping.
 *    - Checked exception wrapped into JsonMappingException.
 *    - Double-location defect regression test (asserting no duplicate "at [" location markers).
 *
 * 5. Delegating Subclass:
 *    - All delegated method passes, identity checks in _with().
 */
public class SettableBeanPropertyGeminiTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final JavaType STRING_TYPE = TypeFactory.defaultInstance().constructType(String.class);

    @Retention(RetentionPolicy.RUNTIME)
    private @interface TestAnnotation {
        String value() default "test";
    }

    private static class SimpleAnnotations implements Annotations {
        @Override
        @SuppressWarnings("unchecked")
        public <A extends Annotation> A get(Class<A> cls) {
            if (cls == TestAnnotation.class) {
                return (A) ConcreteProperty.class.getAnnotation(TestAnnotation.class);
            }
            return null;
        }

        @Override
        public int size() {
            return 1;
        }
    }

    @TestAnnotation("class")
    private static class ConcreteProperty extends SettableBeanProperty {
        private Object assignedValue;

        public ConcreteProperty(PropertyName name, JavaType type, PropertyMetadata metadata,
                                JsonDeserializer<Object> deser) {
            super(name, type, metadata, deser);
        }

        public ConcreteProperty(PropertyName propName, JavaType type, PropertyName wrapper,
                                TypeDeserializer typeDeser, Annotations contextAnnotations,
                                PropertyMetadata metadata) {
            super(propName, type, wrapper, typeDeser, contextAnnotations, metadata);
        }

        protected ConcreteProperty(SettableBeanProperty src) {
            super(src);
        }

        protected ConcreteProperty(SettableBeanProperty src, JsonDeserializer<?> deser, NullValueProvider nuller) {
            super(src, deser, nuller);
        }

        protected ConcreteProperty(SettableBeanProperty src, PropertyName newName) {
            super(src, newName);
        }

        @Override
        public SettableBeanProperty withValueDeserializer(JsonDeserializer<?> deser) {
            return new ConcreteProperty(this, deser, this._nullProvider);
        }

        @Override
        public SettableBeanProperty withName(PropertyName newName) {
            return new ConcreteProperty(this, newName);
        }

        @Override
        public SettableBeanProperty withNullProvider(NullValueProvider nva) {
            return new ConcreteProperty(this, this._valueDeserializer, nva);
        }

        @Override
        public AnnotatedMember getMember() {
            return null;
        }

        @Override
        public <A extends Annotation> A getAnnotation(Class<A> acls) {
            return null;
        }

        @Override
        public void deserializeAndSet(JsonParser p, DeserializationContext ctxt, Object instance) throws IOException {
            set(instance, deserialize(p, ctxt));
        }

        @Override
        public Object deserializeSetAndReturn(JsonParser p, DeserializationContext ctxt, Object instance) throws IOException {
            Object v = deserialize(p, ctxt);
            set(instance, v);
            return instance;
        }

        @Override
        public void set(Object instance, Object value) throws IOException {
            this.assignedValue = value;
        }

        @Override
        public Object setAndReturn(Object instance, Object value) throws IOException {
            this.assignedValue = value;
            return instance;
        }

        public Object getAssignedValue() {
            return assignedValue;
        }
    }

    private static class ConcreteDelegating extends SettableBeanProperty.Delegating {
        public ConcreteDelegating(SettableBeanProperty delegate) {
            super(delegate);
        }

        @Override
        protected SettableBeanProperty withDelegate(SettableBeanProperty d) {
            return new ConcreteDelegating(d);
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorsAndAccessors() {
        PropertyName propName = new PropertyName("prop1");
        PropertyName wrapperName = new PropertyName("wrapper");
        Annotations annotations = new SimpleAnnotations();

        ConcreteProperty prop = new ConcreteProperty(propName, STRING_TYPE, wrapperName,
                null, annotations, PropertyMetadata.STD_REQUIRED);

        assertEquals("prop1", prop.getName());
        assertEquals(propName, prop.getFullName());
        assertEquals(STRING_TYPE, prop.getType());
        assertEquals(wrapperName, prop.getWrapperName());
        assertNotNull(prop.getContextAnnotation(TestAnnotation.class));
        assertNull(prop.getValueDeserializer());
        assertFalse(prop.hasValueDeserializer());
        assertFalse(prop.hasValueTypeDeserializer());
        assertNull(prop.getValueTypeDeserializer());
        assertEquals(-1, prop.getPropertyIndex());
        assertNull(prop.getInjectableValueId());
        assertFalse(prop.isIgnorable());
        assertEquals("[property 'prop1']", prop.toString());

        prop.markAsIgnorable();
        assertFalse(prop.isIgnorable());

        prop.fixAccess(null); // No-op verification
    }

    @Test(timeout = 4000)
    public void testAssignIndexSuccessAndFailure() {
        ConcreteProperty prop = new ConcreteProperty(new PropertyName("idxProp"), STRING_TYPE,
                null, null, null, PropertyMetadata.STD_OPTIONAL);
        assertEquals(-1, prop.getPropertyIndex());

        prop.assignIndex(3);
        assertEquals(3, prop.getPropertyIndex());

        try {
            prop.assignIndex(4);
            fail("Expected IllegalStateException on reassignment of propertyIndex");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("already had index (3)"));
        }
    }

    @Test(timeout = 4000)
    public void testGetCreatorIndexThrowsIllegalStateException() {
        ConcreteProperty prop = new ConcreteProperty(new PropertyName("creatorProp"), STRING_TYPE,
                null, null, null, PropertyMetadata.STD_OPTIONAL);
        try {
            prop.getCreatorIndex();
            fail("Expected IllegalStateException from getCreatorIndex");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("no creator index for property 'creatorProp'"));
        }
    }

    @Test(timeout = 4000)
    public void testManagedReferenceAndObjectId() {
        ConcreteProperty prop = new ConcreteProperty(new PropertyName("refProp"), STRING_TYPE,
                null, null, null, PropertyMetadata.STD_OPTIONAL);

        assertNull(prop.getManagedReferenceName());
        prop.setManagedReferenceName("managedRef");
        assertEquals("managedRef", prop.getManagedReferenceName());

        assertNull(prop.getObjectIdInfo());
        ObjectIdInfo info = new ObjectIdInfo(new PropertyName("id"), Object.class, null, null);
        prop.setObjectIdInfo(info);
        assertSame(info, prop.getObjectIdInfo());
    }

    @Test(timeout = 4000)
    public void testViewsConfiguration() {
        ConcreteProperty prop = new ConcreteProperty(new PropertyName("viewProp"), STRING_TYPE,
                null, null, null, PropertyMetadata.STD_OPTIONAL);
        assertFalse(prop.hasViews());
        assertTrue(prop.visibleInView(String.class));

        prop.setViews(new Class<?>[]{String.class});
        assertTrue(prop.hasViews());
        assertTrue(prop.visibleInView(String.class));
        assertFalse(prop.visibleInView(Integer.class));

        prop.setViews(null);
        assertFalse(prop.hasViews());
        assertTrue(prop.visibleInView(Integer.class));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testNullPropertyNameDefaultsToNoName() {
        ConcreteProperty prop = new ConcreteProperty(null, STRING_TYPE,
                PropertyMetadata.STD_OPTIONAL, null);
        assertEquals(PropertyName.NO_NAME, prop.getFullName());
        assertEquals("", prop.getName());
    }

    @Test(timeout = 4000)
    public void testWithSimpleNameBranches() {
        ConcreteProperty prop = new ConcreteProperty(new PropertyName("nameA"), STRING_TYPE,
                null, null, null, PropertyMetadata.STD_OPTIONAL);

        SettableBeanProperty sameProp = prop.withSimpleName("nameA");
        assertSame(prop, sameProp);

        SettableBeanProperty changedProp = prop.withSimpleName("nameB");
        assertNotSame(prop, changedProp);
        assertEquals("nameB", changedProp.getName());
    }

    @Test(timeout = 4000)
    public void testCopyWithNullDeserializerResetsToMissing() {
        ConcreteProperty prop = new ConcreteProperty(new PropertyName("prop"), STRING_TYPE,
                PropertyMetadata.STD_OPTIONAL, SettableBeanProperty.MISSING_VALUE_DESERIALIZER);

        SettableBeanProperty copy = prop.withValueDeserializer(null);
        assertFalse(copy.hasValueDeserializer());
        assertNull(copy.getValueDeserializer());
    }

    @Test(timeout = 4000)
    public void testCopyWithMissingValueDeserializerNullerLinksToDeserializer() {
        JsonDeserializer<Object> deser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return "custom";
            }
        };

        ConcreteProperty prop = new ConcreteProperty(new PropertyName("prop"), STRING_TYPE,
                PropertyMetadata.STD_OPTIONAL, SettableBeanProperty.MISSING_VALUE_DESERIALIZER);

        ConcreteProperty copied = new ConcreteProperty(prop, deser, SettableBeanProperty.MISSING_VALUE_DESERIALIZER);
        assertTrue(copied.hasValueDeserializer());
        assertSame(deser, copied.getValueDeserializer());
        assertSame(deser, copied.getNullValueProvider());
    }

    @Test(timeout = 4000)
    public void testDepositSchemaPropertyBranches() throws Exception {
        ConcreteProperty reqProp = new ConcreteProperty(new PropertyName("req"), STRING_TYPE,
                null, null, null, PropertyMetadata.STD_REQUIRED);
        ConcreteProperty optProp = new ConcreteProperty(new PropertyName("opt"), STRING_TYPE,
                null, null, null, PropertyMetadata.STD_OPTIONAL);

        final boolean[] called = new boolean[2];
        JsonObjectFormatVisitor visitor = new JsonObjectFormatVisitor.Base() {
            @Override
            public void property(BeanProperty writer) {
                called[0] = true;
            }

            @Override
            public void optionalProperty(BeanProperty writer) {
                called[1] = true;
            }
        };

        reqProp.depositSchemaProperty(visitor, null);
        assertTrue(called[0]);
        assertFalse(called[1]);

        optProp.depositSchemaProperty(visitor, null);
        assertTrue(called[1]);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Location Duplication / IOE Wrapping)
    // =========================================================================

    @Test(timeout = 4000)
    public void testThrowAsIOEWithIllegalArgumentException() throws Exception {
        ConcreteProperty prop = new ConcreteProperty(new PropertyName("age"), STRING_TYPE,
                null, null, null, PropertyMetadata.STD_OPTIONAL);

        JsonParser parser = MAPPER.getFactory().createParser("123");
        IllegalArgumentException cause = new IllegalArgumentException("Invalid number");

        try {
            prop._throwAsIOE(parser, cause, "stringVal");
            fail("Expected JsonMappingException");
        } catch (JsonMappingException jme) {
            assertTrue(jme.getMessage().contains("Problem deserializing property 'age'"));
            assertTrue(jme.getMessage().contains("expected type: " + STRING_TYPE));
            assertTrue(jme.getMessage().contains("actual type: java.lang.String"));
            assertTrue(jme.getMessage().contains("problem: Invalid number"));
        } finally {
            parser.close();
        }
    }

    @Test(timeout = 4000)
    public void testThrowAsIOEWithIllegalArgumentExceptionNullMessage() throws Exception {
        ConcreteProperty prop = new ConcreteProperty(new PropertyName("age"), STRING_TYPE,
                null, null, null, PropertyMetadata.STD_OPTIONAL);

        IllegalArgumentException cause = new IllegalArgumentException((String) null);

        try {
            prop._throwAsIOE(cause, 123);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException jme) {
            assertTrue(jme.getMessage().contains("(no error message provided)"));
        }
    }

    @Test(timeout = 4000)
    public void testThrowAsIOERethrowsIOExceptionDirectly() {
        ConcreteProperty prop = new ConcreteProperty(new PropertyName("test"), STRING_TYPE,
                null, null, null, PropertyMetadata.STD_OPTIONAL);
        IOException originalIOE = new IOException("Disk failure");

        try {
            prop._throwAsIOE(originalIOE);
            fail("Expected IOException");
        } catch (IOException e) {
            assertSame(originalIOE, e);
        }
    }

    @Test(timeout = 4000)
    public void testThrowAsIOERethrowsRuntimeExceptionDirectly() {
        ConcreteProperty prop = new ConcreteProperty(new PropertyName("test"), STRING_TYPE,
                null, null, null, PropertyMetadata.STD_OPTIONAL);
        IllegalStateException originalRTE = new IllegalStateException("State error");

        try {
            prop._throwAsIOE(originalRTE);
            fail("Expected RuntimeException");
        } catch (Exception e) {
            assertSame(originalRTE, e);
        }
    }

    @Test(timeout = 4000)
    public void testThrowAsIOECheckedExceptionWrappedWithoutDuplicateLocation() throws Exception {
        ConcreteProperty prop = new ConcreteProperty(new PropertyName("test"), STRING_TYPE,
                null, null, null, PropertyMetadata.STD_OPTIONAL);

        JsonParser parser = MAPPER.getFactory().createParser("true");
        parser.nextToken(); // position parser
        Exception checkedEx = new Exception("Root checked error");

        try {
            prop._throwAsIOE(parser, checkedEx);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            String msg = e.getMessage();
            int firstAt = msg.indexOf("at [");
            if (firstAt != -1) {
                int secondAt = msg.indexOf("at [", firstAt + 4);
                assertEquals("Should only get one 'at [' marker in exception message, got multiple: " + msg,
                        -1, secondAt);
            }
        } finally {
            parser.close();
        }
    }

    // =========================================================================
    // Partition D: Deserialization Logic & Null Providers
    // =========================================================================

    @Test(timeout = 4000)
    public void testDeserializeTokenNullUsesNullProvider() throws Exception {
        NullValueProvider nullProvider = new NullValueProvider() {
            @Override
            public Object getNullValue(DeserializationContext ctxt) {
                return "CUSTOM_NULL";
            }
        };

        ConcreteProperty prop = new ConcreteProperty(new PropertyName("nullProp"), STRING_TYPE,
                PropertyMetadata.STD_OPTIONAL, null);
        SettableBeanProperty withNuller = prop.withNullProvider(nullProvider);

        JsonParser parser = MAPPER.getFactory().createParser("null");
        parser.nextToken();
        DeserializationContext ctxt = MAPPER.getDeserializationContext();

        Object res = withNuller.deserialize(parser, ctxt);
        assertEquals("CUSTOM_NULL", res);
        parser.close();
    }

    @Test(timeout = 4000)
    public void testDeserializeNormalValueAndCoercedNull() throws Exception {
        JsonDeserializer<Object> nullReturningDeser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return null;
            }
        };

        NullValueProvider fallbackProvider = new NullValueProvider() {
            @Override
            public Object getNullValue(DeserializationContext ctxt) {
                return "FALLBACK";
            }
        };

        ConcreteProperty prop = new ConcreteProperty(new PropertyName("coerced"), STRING_TYPE,
                null, null, null, PropertyMetadata.STD_OPTIONAL);
        SettableBeanProperty configured = prop.withValueDeserializer(nullReturningDeser).withNullProvider(fallbackProvider);

        JsonParser parser = MAPPER.getFactory().createParser("\"\"");
        parser.nextToken();
        DeserializationContext ctxt = MAPPER.getDeserializationContext();

        Object res = configured.deserialize(parser, ctxt);
        assertEquals("FALLBACK", res);
        parser.close();
    }

    @Test(timeout = 4000)
    public void testDeserializeWithNullValueSkipper() throws Exception {
        ConcreteProperty prop = new ConcreteProperty(new PropertyName("skipProp"), STRING_TYPE,
                null, null, null, PropertyMetadata.STD_OPTIONAL);
        SettableBeanProperty configured = prop.withNullProvider(NullsConstantProvider.skipper());

        JsonParser parser = MAPPER.getFactory().createParser("null");
        parser.nextToken();
        DeserializationContext ctxt = MAPPER.getDeserializationContext();

        Object initial = new Object();
        Object result = configured.deserializeWith(parser, ctxt, initial);
        assertSame(initial, result);
        parser.close();
    }

    @Test(timeout = 4000)
    public void testDeserializeWithNullValueNonSkipper() throws Exception {
        NullValueProvider nullProvider = new NullValueProvider() {
            @Override
            public Object getNullValue(DeserializationContext ctxt) {
                return "REPLACED_NULL";
            }
        };

        ConcreteProperty prop = new ConcreteProperty(new PropertyName("nonSkipProp"), STRING_TYPE,
                null, null, null, PropertyMetadata.STD_OPTIONAL);
        SettableBeanProperty configured = prop.withNullProvider(nullProvider);

        JsonParser parser = MAPPER.getFactory().createParser("null");
        parser.nextToken();
        DeserializationContext ctxt = MAPPER.getDeserializationContext();

        Object initial = new Object();
        Object result = configured.deserializeWith(parser, ctxt, initial);
        assertEquals("REPLACED_NULL", result);
        parser.close();
    }

    @Test(timeout = 4000)
    public void testDeserializeAndSetAndReturn() throws Exception {
        JsonDeserializer<Object> deser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return "hello";
            }
        };

        ConcreteProperty prop = new ConcreteProperty(new PropertyName("testProp"), STRING_TYPE,
                PropertyMetadata.STD_OPTIONAL, deser);

        JsonParser parser = MAPPER.getFactory().createParser("\"hello\"");
        parser.nextToken();
        DeserializationContext ctxt = MAPPER.getDeserializationContext();

        Object target = new Object();
        prop.deserializeAndSet(parser, ctxt, target);
        assertEquals("hello", prop.getAssignedValue());

        Object ret = prop.deserializeSetAndReturn(parser, ctxt, target);
        assertSame(target, ret);
        assertEquals("hello", prop.getAssignedValue());
        parser.close();
    }

    // =========================================================================
    // Partition E: Delegating Subclass Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testDelegatingClassDelegation() throws Exception {
        JsonDeserializer<Object> deser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return "delegatedValue";
            }
        };

        ConcreteProperty baseProp = new ConcreteProperty(new PropertyName("base"), STRING_TYPE,
                PropertyMetadata.STD_OPTIONAL, deser);
        ConcreteDelegating delegating = new ConcreteDelegating(baseProp);

        assertSame(baseProp, delegating.getDelegate());
        assertEquals("base", delegating.getName());
        assertEquals(baseProp.getFullName(), delegating.getFullName());
        assertEquals(STRING_TYPE, delegating.getType());
        assertTrue(delegating.hasValueDeserializer());
        assertSame(deser, delegating.getValueDeserializer());
        assertFalse(delegating.hasValueTypeDeserializer());
        assertNull(delegating.getValueTypeDeserializer());
        assertNull(delegating.getManagedReferenceName());
        assertNull(delegating.getObjectIdInfo());
        assertFalse(delegating.hasViews());
        assertNull(delegating.getInjectableValueId());
        assertNull(delegating.getMember());
        assertNull(delegating.getAnnotation(TestAnnotation.class));

        delegating.assignIndex(10);
        assertEquals(10, baseProp.getPropertyIndex());
        assertEquals(10, delegating.getPropertyIndex());

        delegating.fixAccess(null);

        SettableBeanProperty same = delegating._with(baseProp);
        assertSame(delegating, same);

        SettableBeanProperty newNamed = delegating.withName(new PropertyName("newName"));
        assertEquals("newName", newNamed.getName());

        SettableBeanProperty newDeser = delegating.withValueDeserializer(null);
        assertFalse(newDeser.hasValueDeserializer());

        SettableBeanProperty newNuller = delegating.withNullProvider(NullsConstantProvider.nuller());
        assertNotNull(newNuller.getNullValueProvider());

        Object dummy = new Object();
        delegating.set(dummy, "val1");
        assertEquals("val1", baseProp.getAssignedValue());

        Object ret = delegating.setAndReturn(dummy, "val2");
        assertSame(dummy, ret);
        assertEquals("val2", baseProp.getAssignedValue());

        JsonParser parser = MAPPER.getFactory().createParser("\"foo\"");
        parser.nextToken();
        DeserializationContext ctxt = MAPPER.getDeserializationContext();

        delegating.deserializeAndSet(parser, ctxt, dummy);
        assertEquals("delegatedValue", baseProp.getAssignedValue());

        Object ret2 = delegating.deserializeSetAndReturn(parser, ctxt, dummy);
        assertSame(dummy, ret2);
        assertEquals("delegatedValue", baseProp.getAssignedValue());
        parser.close();
    }
}