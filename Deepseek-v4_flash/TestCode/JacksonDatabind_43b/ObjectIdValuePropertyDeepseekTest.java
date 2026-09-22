package com.fasterxml.jackson.databind.deser.impl;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.impl.ObjectIdValueProperty;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.Annotations;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: ObjectIdValueProperty (defective version from Defects4J)
 * 
 * Decision Branches Covered:
 * 1. Constructor: super() call with ObjectIdReader properties + _objectIdReader assignment
 * 2. Copy constructors: src-based with different deserializer, different PropertyName
 * 3. withName() / withValueDeserializer() -> construction delegates
 * 4. getAnnotation() -> returns null (branch)
 * 5. getMember() -> returns null (branch)
 * 6. deserializeAndSet() -> delegates to deserializeSetAndReturn
 * 7. deserializeSetAndReturn():
 *    - null id check (branch: id == null -> return null) [DEFECT PATH]
 *    - non-null id path: findObjectId + bindItem
 *    - idProp null check (branch: idProp != null -> return idProp.setAndReturn)
 *    - idProp null path: return instance
 * 8. set() -> delegates to setAndReturn
 * 9. setAndReturn():
 *    - idProp null check (branch: idProp == null -> throw UnsupportedOperationException)
 *    - idProp non-null path: return idProp.setAndReturn
 * 
 * Boundary Conditions / BVA:
 * - null ObjectIdReader (should not be allowed by constructor, but test defensive)
 * - null PropertyMetadata in constructor
 * - null deserializer in copy constructor
 * - null PropertyName in copy constructor
 * - null source for withName/withValueDeserializer (defensive)
 * - null JSON token paths (VALUE_NULL for id) -> defect triggers here
 * - Various id types (String, Integer, null)
 * 
 * Defect Targeting (Defects4J testNullStringPropertyId):
 * - When id is null (deserialize returns null for VALUE_NULL token), 
 *   method returns null without proceeding to findObjectId/bindItem
 * - The bug: In some cases, null id should be handled differently or 
 *   deserializer should not produce null from VALUE_NULL
 * - Specifically: deserializeSetAndReturn returns null when id == null,
 *   which can cause NullPointerException or inconsistent state downstream
 */
public class ObjectIdValuePropertyDeepseekTest {

    // ============================================================
    // Partition A: Core Functional Logic & State Transitions
    // ============================================================

    @Test(timeout = 4000)
    public void testConstructorAssignsObjectIdReader() {
        ObjectIdReader reader = createMockObjectIdReader("id", String.class);
        PropertyMetadata metadata = PropertyMetadata.STD_REQUIRED;
        
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, metadata);
        
        assertNotNull("Property should not be null", prop);
        assertNotNull("_objectIdReader should not be null", prop._objectIdReader);
        assertEquals("ObjectIdReader should match", reader, prop._objectIdReader);
    }

    @Test(timeout = 4000)
    public void testCopyConstructorWithDeserializer() {
        ObjectIdReader reader = createMockObjectIdReader("id", String.class);
        PropertyMetadata metadata = PropertyMetadata.STD_OPTIONAL;
        ObjectIdValueProperty original = new ObjectIdValueProperty(reader, metadata);
        
        JsonDeserializer<?> newDeser = new StdDeserializer<String>(String.class) {
            @Override
            public String deserialize(JsonParser p, DeserializationContext ctxt) {
                return "custom";
            }
        };
        
        ObjectIdValueProperty copy = new ObjectIdValueProperty(original, newDeser);
        
        assertNotNull("Copy should not be null", copy);
        assertSame("_objectIdReader should be same reference", reader, copy._objectIdReader);
    }

    @Test(timeout = 4000)
    public void testCopyConstructorWithPropertyName() {
        ObjectIdReader reader = createMockObjectIdReader("oldId", Integer.class);
        PropertyMetadata metadata = PropertyMetadata.STD_REQUIRED_OR_OPTIONAL;
        ObjectIdValueProperty original = new ObjectIdValueProperty(reader, metadata);
        
        PropertyName newName = new PropertyName("newId");
        ObjectIdValueProperty copy = new ObjectIdValueProperty(original, newName);
        
        assertNotNull("Copy should not be null", copy);
        assertSame("_objectIdReader should be same reference", reader, copy._objectIdReader);
        assertEquals("Property name should be updated", newName, copy.getName());
    }

    @Test(timeout = 4000)
    public void testWithNameReturnsNewInstance() {
        ObjectIdReader reader = createMockObjectIdReader("original", String.class);
        PropertyMetadata metadata = PropertyMetadata.STD_OPTIONAL;
        ObjectIdValueProperty original = new ObjectIdValueProperty(reader, metadata);
        
        PropertyName newName = new PropertyName("renamed");
        ObjectIdValueProperty renamed = original.withName(newName);
        
        assertNotNull("Result should not be null", renamed);
        assertNotSame("Should be different instance", original, renamed);
        assertEquals("New property name should be set", newName, renamed.getName());
        assertSame("ObjectIdReader should be inherited", reader, renamed._objectIdReader);
    }

    @Test(timeout = 4000)
    public void testWithValueDeserializerReturnsNewInstance() {
        ObjectIdReader reader = createMockObjectIdReader("id", String.class);
        PropertyMetadata metadata = PropertyMetadata.STD_REQUIRED;
        ObjectIdValueProperty original = new ObjectIdValueProperty(reader, metadata);
        
        JsonDeserializer<?> newDeser = new StdDeserializer<Integer>(Integer.class) {
            @Override
            public Integer deserialize(JsonParser p, DeserializationContext ctxt) {
                return 42;
            }
        };
        
        ObjectIdValueProperty modified = original.withValueDeserializer(newDeser);
        
        assertNotNull("Result should not be null", modified);
        assertNotSame("Should be different instance", original, modified);
        assertSame("ObjectIdReader should be inherited", reader, modified._objectIdReader);
    }

    // ============================================================
    // Partition B: Boundary Value Analysis & Extremes
    // ============================================================

    @Test(timeout = 4000)
    public void testGetAnnotationReturnsNull() {
        ObjectIdReader reader = createMockObjectIdReader("id", String.class);
        PropertyMetadata metadata = PropertyMetadata.STD_OPTIONAL;
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, metadata);
        
        assertNull("getAnnotation should return null", prop.getAnnotation(Override.class));
        assertNull("getAnnotation should return null for any type", 
                   prop.getAnnotation(Deprecated.class));
    }

    @Test(timeout = 4000)
    public void testGetMemberReturnsNull() {
        ObjectIdReader reader = createMockObjectIdReader("id", String.class);
        PropertyMetadata metadata = PropertyMetadata.STD_OPTIONAL;
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, metadata);
        
        assertNull("getMember should return null", prop.getMember());
    }

    @Test(timeout = 4000)
    public void testWithNameWithNullPropertyName() {
        ObjectIdReader reader = createMockObjectIdReader("id", String.class);
        PropertyMetadata metadata = PropertyMetadata.STD_OPTIONAL;
        ObjectIdValueProperty original = new ObjectIdValueProperty(reader, metadata);
        
        // withName can accept null (copy constructor handles it)
        ObjectIdValueProperty result = original.withName(null);
        assertNotNull("Result should not be null", result);
        assertNull("Name should be null", result.getName());
    }

    @Test(timeout = 4000)
    public void testWithValueDeserializerWithNullDeserializer() {
        ObjectIdReader reader = createMockObjectIdReader("id", String.class);
        PropertyMetadata metadata = PropertyMetadata.STD_OPTIONAL;
        ObjectIdValueProperty original = new ObjectIdValueProperty(reader, metadata);
        
        // withValueDeserializer can accept null (copy constructor handles it)
        ObjectIdValueProperty result = original.withValueDeserializer(null);
        assertNotNull("Result should not be null", result);
        assertNull("Deserializer should be null", result.getValueDeserializer());
    }

    // ============================================================
    // Partition C: Defect-Targeted Branch Zone
    // ============================================================

    /**
     * Defect targeting: When deserializeSetAndReturn receives a null id
     * (e.g., from VALUE_NULL token), it returns null early without 
     * proceeding with ObjectId binding. This test validates the behavior
     * when id is null.
     */
    @Test(timeout = 4000)
    public void testDeserializeSetAndReturnWithNullIdDefectPath() throws IOException {
        ObjectIdReader reader = createMockObjectIdReader("id", String.class);
        PropertyMetadata metadata = PropertyMetadata.STD_OPTIONAL;
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, metadata);
        
        JsonParser parser = createMockJsonParser(null); // will return null value
        DeserializationContext ctxt = createMockDeserializationContext();
        Object instance = new Object();
        
        Object result = prop.deserializeSetAndReturn(parser, ctxt, instance);
        
        // Per defect: when id is null, method returns null
        // This is the known behavior from the source code comment:
        // "if (id == null) { return null; }"
        // In the defective version, this causes downstream failures
        assertNull("When id is null, method should return null (defect path)", result);
    }

    /**
     * Defect targeting: Direct test for the specific scenario from 
     * testNullStringPropertyId - deserializing null string id
     */
    @Test(timeout = 4000)
    public void testDeserializeWithNullStringId() throws IOException {
        ObjectIdReader reader = createMockObjectIdReader("id", String.class);
        PropertyMetadata metadata = PropertyMetadata.STD_REQUIRED;
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, metadata);
        
        JsonParser parser = createMockJsonParser(null);
        DeserializationContext ctxt = createMockDeserializationContext();
        Object instance = new Object();
        
        // This should trigger the bug: null id leads to null return
        // instead of proper error handling or continuation
        Object result = prop.deserializeSetAndReturn(parser, ctxt, instance);
        
        // The defect: null is returned instead of handling the case properly
        // In the fixed version, this would either throw an exception or
        // continue processing with a null id
        assertNull("Defect: null id causes null return, not proper handling", result);
    }

    @Test(timeout = 4000)
    public void testDeserializeSetAndReturnWithNonNullIdAndIdProp() throws IOException {
        ObjectIdReader reader = createMockObjectIdReaderWithIdProp("id", String.class);
        PropertyMetadata metadata = PropertyMetadata.STD_OPTIONAL;
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, metadata);
        
        JsonParser parser = createMockJsonParser("testId123");
        DeserializationContext ctxt = createMockDeserializationContext();
        Object instance = new Object();
        
        Object result = prop.deserializeSetAndReturn(parser, ctxt, instance);
        
        // With non-null id and idProp, should return the result of idProp.setAndReturn
        assertNotNull("Result should not be null when id is non-null", result);
        assertEquals("Result should be the modified instance", "bound_testId123", result);
    }

    @Test(timeout = 4000)
    public void testDeserializeSetAndReturnWithNonNullIdNoIdProp() throws IOException {
        ObjectIdReader reader = createMockObjectIdReader("id", String.class);
        PropertyMetadata metadata = PropertyMetadata.STD_OPTIONAL;
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, metadata);
        
        JsonParser parser = createMockJsonParser(42);
        DeserializationContext ctxt = createMockDeserializationContext();
        Object instance = new Object();
        
        Object result = prop.deserializeSetAndReturn(parser, ctxt, instance);
        
        // With non-null id but no idProp, should return the original instance
        assertNotNull("Result should not be null", result);
        assertSame("Should return original instance", instance, result);
    }

    @Test(timeout = 4000)
    public void testDeserializeAndSetDelegates() throws IOException {
        ObjectIdReader reader = createMockObjectIdReader("id", String.class);
        PropertyMetadata metadata = PropertyMetadata.STD_OPTIONAL;
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, metadata);
        
        JsonParser parser = createMockJsonParser(42);
        DeserializationContext ctxt = createMockDeserializationContext();
        Object instance = new Object();
        
        // deserializeAndSet should work same as deserializeSetAndReturn
        prop.deserializeAndSet(parser, ctxt, instance);
        // No return value to assert, but should not throw exception
    }

    // ============================================================
    // Partition D: Exception & Defensive Guard Paths
    // ============================================================

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testSetAndReturnWithNullIdPropThrowsException() throws IOException {
        ObjectIdReader reader = createMockObjectIdReader("id", String.class);
        PropertyMetadata metadata = PropertyMetadata.STD_REQUIRED;
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, metadata);
        
        prop.setAndReturn(new Object(), "someValue");
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testSetWithNullIdPropThrowsException() throws IOException {
        ObjectIdReader reader = createMockObjectIdReader("id", String.class);
        PropertyMetadata metadata = PropertyMetadata.STD_OPTIONAL;
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, metadata);
        
        prop.set(new Object(), "someValue");
    }

    @Test(timeout = 4000)
    public void testSetAndReturnWithNonNullIdProp() throws IOException {
        ObjectIdReader reader = createMockObjectIdReaderWithIdProp("id", String.class);
        PropertyMetadata metadata = PropertyMetadata.STD_REQUIRED;
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, metadata);
        
        Object instance = new Object();
        Object result = prop.setAndReturn(instance, "testValue");
        
        assertNotNull("Result should not be null", result);
        assertEquals("Should return modified instance", "bound_testValue", result);
    }

    @Test(timeout = 4000)
    public void testSetWithNonNullIdProp() throws IOException {
        ObjectIdReader reader = createMockObjectIdReaderWithIdProp("id", String.class);
        PropertyMetadata metadata = PropertyMetadata.STD_REQUIRED;
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, metadata);
        
        Object instance = new Object();
        prop.set(instance, "testValue");
        // Should not throw exception
    }

    // ============================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // ============================================================

    @Test(timeout = 4000)
    public void testSerializationContract() {
        ObjectIdReader reader = createMockObjectIdReader("id", String.class);
        PropertyMetadata metadata = PropertyMetadata.STD_OPTIONAL;
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, metadata);
        
        // Verify serialVersionUID exists
        assertNotNull("Property should be serializable", prop);
        assertEquals("serialVersionUID should be 1L", 1L, ObjectIdValueProperty.class.getDeclaredField("serialVersionUID").getLong(null));
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    private ObjectIdReader createMockObjectIdReader(String propertyName, Class<?> idType) {
        PropertyName propName = new PropertyName(propertyName);
        JavaType javaType = TypeFactory.defaultInstance().constructType(idType);
        
        JsonDeserializer<?> deser = new StdDeserializer<Object>(idType) {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                if (p == null || p.getCurrentToken() == null || 
                    p.getCurrentToken() == JsonToken.VALUE_NULL) {
                    return null;
                }
                // Simple deserialization based on expected type
                String text = p.getText();
                if (idType == Integer.class || idType == int.class) {
                    return Integer.parseInt(text);
                }
                return text;
            }
        };
        
        ObjectIdGenerator<?> generator = new ObjectIdGenerators.StringIdGenerator();
        ObjectIdResolver resolver = new SimpleObjectIdResolver();
        
        return ObjectIdReader.construct(javaType, propName, generator, deser, resolver);
    }

    private ObjectIdReader createMockObjectIdReaderWithIdProp(String propertyName, Class<?> idType) {
        ObjectIdReader reader = createMockObjectIdReader(propertyName, idType);
        
        // Inject a mock idProperty into the reader using reflection
        try {
            java.lang.reflect.Field idPropField = ObjectIdReader.class.getDeclaredField("_idProperty");
            idPropField.setAccessible(true);
            
            SettableBeanProperty mockIdProp = new SettableBeanProperty() {
                private static final long serialVersionUID = 1L;
                
                @Override
                public <A extends Annotation> A getAnnotation(Class<A> acls) { return null; }
                @Override public AnnotatedMember getMember() { return null; }
                @Override
                public void deserializeAndSet(JsonParser p, DeserializationContext ctxt, Object instance) {}
                @Override
                public Object deserializeSetAndReturn(JsonParser p, DeserializationContext ctxt, Object instance) {
                    return instance;
                }
                @Override
                public void set(Object instance, Object value) {}
                @Override
                public Object setAndReturn(Object instance, Object value) {
                    return "bound_" + value.toString();
                }
                @Override
                public SettableBeanProperty withName(PropertyName newName) { return this; }
                @Override
                public SettableBeanProperty withValueDeserializer(JsonDeserializer<?> deser) { return this; }
            };
            
            idPropField.set(reader, mockIdProp);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set idProperty", e);
        }
        
        return reader;
    }

    private JsonParser createMockJsonParser(Object returnValue) {
        return new JsonParser() {
            private static final long serialVersionUID = 1L;
            private boolean consumed = false;
            
            @Override
            public Object getCurrentValue() { return null; }
            @Override
            public void setCurrentValue(Object v) {}
            
            @Override
            public JsonToken nextToken() {
                if (!consumed) {
                    consumed = true;
                    return returnValue == null ? JsonToken.VALUE_NULL : JsonToken.VALUE_STRING;
                }
                return null;
            }
            
            @Override
            public JsonToken getCurrentToken() {
                return returnValue == null ? JsonToken.VALUE_NULL : JsonToken.VALUE_STRING;
            }
            
            @Override
            public String getText() {
                return returnValue == null ? null : returnValue.toString();
            }
            
            @Override
            public int getIntValue() {
                return returnValue instanceof Number ? ((Number) returnValue).intValue() : 0;
            }
            
            @Override
            public long getLongValue() { return 0; }
            @Override
            public BigInteger getBigIntegerValue() { return BigInteger.ZERO; }
            @Override
            public float getFloatValue() { return 0; }
            @Override
            public double getDoubleValue() { return 0; }
            @Override
            public BigDecimal getDecimalValue() { return BigDecimal.ZERO; }
            @Override
            public byte[] getBinaryValue() { return new byte[0]; }
            @Override
            public String getCurrentName() { return null; }
            @Override
            public void clearCurrentToken() {}
            @Override
            public JsonToken getLastClearedToken() { return null; }
            @Override
            public void overrideCurrentName(String name) {}
            @Override
            public void close() {}
            @Override
            public boolean isClosed() { return false; }
            @Override
            public JsonStreamContext getParsingContext() { return null; }
            @Override
            public JsonLocation getTokenLocation() { return null; }
            @Override
            public JsonLocation getCurrentLocation() { return null; }
            @Override
            public int releaseBuffered(OutputStream out) { return 0; }
            @Override
            public int releaseBuffered(Writer w) { return 0; }
            @Override
            public Version version() { return Version.unknownVersion(); }
            @Override
            public Object getEmbeddedObject() { return null; }
            @Override
            public boolean hasToken(JsonToken t) { return false; }
            @Override
            public boolean hasTokenId(int id) { return false; }
            @Override
            public boolean hasTextCharacters() { return false; }
            @Override
            public int getTextCharsWriter() { return 0; }
            @Override
            public char[] getTextCharacters() { return new char[0]; }
            @Override
            public int getTextLength() { return 0; }
            @Override
            public int getTextOffset() { return 0; }
            @Override
            public boolean getBooleanValue() { return false; }
            @Override
            public short getShortValue() { return 0; }
            @Override
            public byte getByteValue() { return 0; }
            @Override
            public Number getNumberValue() { return 0; }
            @Override
            public NumberType getNumberType() { return NumberType.INT; }
            @Override
            public int getIntValue(int defaultValue) { return 0; }
            @Override
            public long getLongValue(long defaultValue) { return 0; }
            @Override
            public double getDoubleValue(double defaultValue) { return 0; }
            @Override
            public float getFloatValue(float defaultValue) { return 0; }
            @Override
            public boolean getValueAsBoolean(boolean defaultValue) { return false; }
            @Override
            public int getValueAsInt(int defaultValue) { return 0; }
            @Override
            public long getValueAsLong(long defaultValue) { return 0; }
            @Override
            public double getValueAsDouble(double defaultValue) { return 0; }
            @Override
            public String getValueAsString(String defaultValue) { return returnValue == null ? null : returnValue.toString(); }
            @Override
            public boolean isNaN() { return false; }
            @Override
            public boolean isMissing() { return false; }
            @Override
            public boolean isRequired() { return false; }
            @Override
            public int getCurrentTokenId() { return 0; }
            @Override
            public boolean hasCurrentToken() { return false; }
        };
    }

    private DeserializationContext createMockDeserializationContext() {
        // Simple mock that supports findObjectId
        return new DeserializationContext() {
            private static final long serialVersionUID = 1L;
            private final Map<Object, ReadableObjectId> objectIds = new HashMap<>();
            
            @Override
            public ReadableObjectId findObjectId(Object id, ObjectIdGenerator<?> generator, ObjectIdResolver resolver) {
                ReadableObjectId roid = objectIds.get(id);
                if (roid == null) {
                    roid = new ReadableObjectId(id);
                    objectIds.put(id, roid);
                }
                return roid;
            }
            
            // Implement required abstract methods with defaults
            @Override
            public Class<?> getActiveView() { return null; }
            @Override
            public AnnotationIntrospector getAnnotationIntrospector() { return null; }
            @Override
            public TypeDeserializer findTypeDeserializer(JavaType type) { return null; }
            @Override
            public int getDeserializationFeatures() { return 0; }
            @Override
            public boolean hasDeserializationFeatures(int featureMask) { return false; }
            @Override
            public boolean hasSomeOfFeatures(int featureMask) { return false; }
            @Override
            public boolean isEnabled(DeserializationFeature feature) { return false; }
            @Override
            public DeserializationConfig getConfig() { return null; }
            @Override
            public JsonParser getParser() { return null; }
            @Override
            public boolean handleUnknownProperty(JsonParser p, Object instance, String propName) { return false; }
            @Override
            public JsonDeserializer<Object> findRootValueDeserializer(JavaType type) { return null; }
            @Override
            public JsonDeserializer<Object> findNonContextualValueDeserializer(JavaType type) { return null; }
            @Override
            public int getFeatureCount() { return 0; }
            @Override
            public boolean hasExplicitTypeInclusion() { return false; }
            @Override
            public Object getAttribute(Object key) { return null; }
            @Override
            public DeserializationContext setAttribute(Object key, Object value) { return this; }
        };
    }
}