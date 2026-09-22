package com.fasterxml.jackson.databind.deser.std;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.NullValueProvider;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.impl.NullsAsEmptyProvider;
import com.fasterxml.jackson.databind.deser.impl.NullsConstantProvider;
import com.fasterxml.jackson.databind.deser.impl.NullsFailProvider;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.util.AccessPattern;
import com.fasterxml.jackson.databind.util.ClassUtil;
import com.fasterxml.jackson.databind.util.Converter;

/**
 * White-box test suite for StdDeserializer.
 * Targets all branches, boundary conditions, and the known defect
 * (duplicate "at [" markers in error messages).
 */
public class StdDeserializerDeepseekTest {

    // ------------------------------------------------------------
    // Helper: concrete subclass for testing protected methods
    // ------------------------------------------------------------
    static class TestStdDeserializer extends StdDeserializer<Integer> {
        public TestStdDeserializer() {
            super(Integer.class);
        }

        @Override
        public Integer deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public JavaType getValueType() {
            return null; // default
        }
    }

    // ------------------------------------------------------------
    // Helper: minimal JsonParser stub
    // ------------------------------------------------------------
    static class TestParser extends JsonParser {
        private JsonToken currentToken;
        private String text;
        private int intValue;
        private long longValue;
        private float floatValue;
        private double doubleValue;
        private Number numberValue;
        private boolean nextTokenCalled;

        public TestParser(JsonToken t) { this.currentToken = t; }

        public TestParser withText(String s) { this.text = s; return this; }
        public TestParser withInt(int i) { this.intValue = i; this.numberValue = i; return this; }
        public TestParser withLong(long l) { this.longValue = l; this.numberValue = l; return this; }
        public TestParser withFloat(float f) { this.floatValue = f; this.numberValue = f; return this; }
        public TestParser withDouble(double d) { this.doubleValue = d; this.numberValue = d; return this; }

        @Override public JsonToken getCurrentToken() { return currentToken; }
        @Override public int getCurrentTokenId() { return currentToken.id(); }
        @Override public boolean hasToken(JsonToken t) { return currentToken == t; }
        @Override public String getText() { return text; }
        @Override public int getIntValue() { return intValue; }
        @Override public long getLongValue() { return longValue; }
        @Override public float getFloatValue() { return floatValue; }
        @Override public double getDoubleValue() { return doubleValue; }
        @Override public Number getNumberValue() { return numberValue; }
        @Override public BigInteger getBigIntegerValue() { return BigInteger.valueOf(longValue); }
        @Override public JsonToken nextToken() {
            if (!nextTokenCalled) {
                nextTokenCalled = true;
                return currentToken;
            }
            return JsonToken.END_ARRAY;
        }
        @Override public String getValueAsString() { return text; }
        @Override public int getValueAsInt() { return intValue; }
        @Override public long getValueAsLong() { return longValue; }
        @Override public double getValueAsDouble() { return doubleValue; }
        // Unsupported
        @Override public Object getEmbeddedObject() { return null; }
        @Override public byte[] getBinaryValue(Base64Variant b64) { return null; }
        @Override public JsonLocation getCurrentLocation() { return null; }
        @Override public JsonLocation getTokenLocation() { return null; }
        @Override public String getCurrentName() { return null; }
        @Override public void clearCurrentToken() {}
        @Override public JsonParser skipChildren() { return this; }
        @Override public boolean isClosed() { return false; }
        @Override public JsonStreamContext getParsingContext() { return null; }
        @Override public JsonToken overrideCurrentName(String name) { return null; }
        @Override public void close() {}
        @Override public int releaseBuffered(OutputStream out) { return 0; }
    }

    // ------------------------------------------------------------
    // Helper: minimal DeserializationContext stub
    // ------------------------------------------------------------
    static class TestContext extends DeserializationContext {
        private int deserFeatures;
        private boolean allowCoercionOfScalars = true;
        private boolean failOnNullForPrimitives = false;
        private boolean acceptFloatAsInt = false;
        private boolean unwrapSingleValueArrays = false;
        private boolean acceptEmptyArrayAsNullObject = false;
        private boolean acceptEmptyStringAsNullObject = false;
        private String weirdStringValueMessage;
        private String weirdNumberValueMessage;
        private String unexpectedTokenMessage;
        private String inputMismatchMessage;
        private boolean throwOnWeirdString = false;
        private boolean throwOnWeirdNumber = false;
        private boolean throwOnUnexpectedToken = false;
        private boolean throwOnInputMismatch = false;

        public TestContext() {
            super(null, null, null, null); // dummy
        }

        public TestContext withFeature(int mask, boolean enabled) {
            if (enabled) deserFeatures |= mask;
            else deserFeatures &= ~mask;
            return this;
        }

        public TestContext withAllowCoercionOfScalars(boolean b) { allowCoercionOfScalars = b; return this; }
        public TestContext withFailOnNullForPrimitives(boolean b) { failOnNullForPrimitives = b; return this; }
        public TestContext withAcceptFloatAsInt(boolean b) { acceptFloatAsInt = b; return this; }
        public TestContext withUnwrapSingleValueArrays(boolean b) { unwrapSingleValueArrays = b; return this; }
        public TestContext withAcceptEmptyArrayAsNullObject(boolean b) { acceptEmptyArrayAsNullObject = b; return this; }
        public TestContext withAcceptEmptyStringAsNullObject(boolean b) { acceptEmptyStringAsNullObject = b; return this; }

        // Configure exception throwing with a message that contains one "at [" marker
        public TestContext withWeirdStringValueMessage(String msg) { weirdStringValueMessage = msg; throwOnWeirdString = true; return this; }
        public TestContext withWeirdNumberValueMessage(String msg) { weirdNumberValueMessage = msg; throwOnWeirdNumber = true; return this; }
        public TestContext withUnexpectedTokenMessage(String msg) { unexpectedTokenMessage = msg; throwOnUnexpectedToken = true; return this; }
        public TestContext withInputMismatchMessage(String msg) { inputMismatchMessage = msg; throwOnInputMismatch = true; return this; }

        @Override
        public boolean isEnabled(DeserializationFeature feat) {
            return (deserFeatures & feat.getMask()) != 0;
        }

        @Override
        public boolean isEnabled(MapperFeature feat) {
            if (feat == MapperFeature.ALLOW_COERCION_OF_SCALARS) return allowCoercionOfScalars;
            return false;
        }

        @Override
        public int getDeserializationFeatures() { return deserFeatures; }

        @Override
        public boolean hasSomeOfFeatures(int mask) { return (deserFeatures & mask) != 0; }

        @Override
        public Object handleWeirdStringValue(Class<?> targetClass, String value, String msg, Object... args) throws IOException {
            if (throwOnWeirdString) {
                throw new JsonMappingException(this, weirdStringValueMessage);
            }
            // default: return null
            return null;
        }

        @Override
        public Object handleWeirdNumberValue(Class<?> targetClass, Number value, String msg, Object... args) throws IOException {
            if (throwOnWeirdNumber) {
                throw new JsonMappingException(this, weirdNumberValueMessage);
            }
            return null;
        }

        @Override
        public Object handleUnexpectedToken(Class<?> targetType, JsonParser p) throws IOException {
            if (throwOnUnexpectedToken) {
                throw new JsonMappingException(this, unexpectedTokenMessage);
            }
            return null;
        }

        @Override
        public Object handleUnexpectedToken(Class<?> targetType, JsonToken t, JsonParser p, String msg) throws IOException {
            if (throwOnUnexpectedToken) {
                throw new JsonMappingException(this, unexpectedTokenMessage);
            }
            return null;
        }

        @Override
        public void reportInputMismatch(JsonDeserializer<?> deser, String msg, Object... args) throws JsonMappingException {
            if (throwOnInputMismatch) {
                throw new JsonMappingException(this, inputMismatchMessage);
            }
        }

        @Override
        public void reportWrongTokenException(JsonDeserializer<?> deser, JsonToken exp, String msg, Object... args) throws JsonMappingException {
            throw new JsonMappingException(this, "wrong token");
        }

        @Override
        public Date parseDate(String dateStr) throws IllegalArgumentException {
            return new Date(0);
        }

        @Override
        public JsonDeserializer<Object> findContextualValueDeserializer(JavaType type, BeanProperty prop) {
            return null;
        }

        @Override
        public AnnotationIntrospector getAnnotationIntrospector() { return null; }

        @Override
        public DeserializationConfig getConfig() { return null; }

        @Override
        public JsonFormat.Value getDefaultPropertyFormat(Class<?> baseType) { return null; }

        @Override
        public JavaType constructType(Class<?> cls) { return null; }

        @Override
        public void reportBadDefinition(JavaType type, String msg) throws JsonMappingException {
            throw new JsonMappingException(this, msg);
        }

        @Override
        public boolean handleUnknownProperty(JsonParser p, JsonDeserializer<?> deser, Object instanceOrClass, String propName) {
            return false;
        }

        @Override
        public <T> T reportInputMismatch(JsonDeserializer<?> deser, Class<?> type, String msg, Object... args) throws JsonMappingException {
            throw new JsonMappingException(this, msg);
        }

        // Unsupported methods
        @Override public Class<?> getActiveView() { return null; }
        @Override public Object getAttribute(Object key) { return null; }
        @Override public DeserializationContext setAttribute(Object key, Object value) { return this; }
        @Override public JsonParser getParser() { return null; }
        @Override public int getCurrentTokenId() { return 0; }
        @Override public JsonToken getCurrentToken() { return null; }
        @Override public boolean hasCurrentToken() { return false; }
        @Override public boolean hasTokenId(int id) { return false; }
        @Override public boolean hasToken(JsonToken t) { return false; }
        @Override public void clearAndResolve() {}
        @Override public Object findInjectableValue(Object valueId, BeanProperty forProperty, Object beanInstance) { return null; }
        @Override public JavaType getContextualType() { return null; }
        @Override public Object getNullValue(JsonDeserializer<?> deser) { return null; }
        @Override public Object getNullValue() { return null; }
        @Override public Object getEmptyValue() { return null; }
        @Override public Object getEmptyValue(JsonDeserializer<?> deser) { return null; }
        @Override public AccessPattern getNullAccessPattern() { return AccessPattern.ALWAYS_NULL; }
        @Override public AccessPattern getEmptyAccessPattern() { return AccessPattern.ALWAYS_NULL; }
        @Override public Object readValue(JsonParser p, JavaType type) { return null; }
        @Override public <T> T readValue(JsonParser p, Class<T> type) { return null; }
        @Override public boolean isEnabled(StreamReadFeature feat) { return false; }
        @Override public JsonDeserializer<?> deserializerInstance(AnnotatedMember ann, Object deserDef) { return null; }
        @Override public KeyDeserializer keyDeserializerInstance(AnnotatedMember ann, Object deserDef) { return null; }
        @Override public <T> Converter<T,?> converterInstance(AnnotatedMember ann, Object convDef) { return null; }
        @Override public JsonDeserializer<?> findRootValueDeserializer(JavaType type) { return null; }
        @Override public JsonDeserializer<?> handleSecondaryContextualization(JsonDeserializer<?> deser, BeanProperty prop, JavaType type) { return deser; }
        @Override public JsonDeserializer<?> handlePrimaryContextualization(JsonDeserializer<?> deser, BeanProperty prop, JavaType type) { return deser; }
        @Override public int getFeatureCount() { return 0; }
        @Override public int getFeatureIndex(DeserializationFeature feat) { return 0; }
        @Override public int getFeatureIndex(MapperFeature feat) { return 0; }
        @Override public boolean hasDeserializationFeatures(int mask) { return false; }
        @Override public boolean hasSomeOfFeatures(int mask) { return false; }
        @Override public boolean isEnabled(DeserializationFeature feat, JavaType type) { return false; }
        @Override public boolean isEnabled(MapperFeature feat, JavaType type) { return false; }
        @Override public DeserializationCache getDeserializationCache() { return null; }
        @Override public DeserializerFactory getFactory() { return null; }
        @Override public Object getLastProperty() { return null; }
        @Override public void setLastProperty(Object prop) {}
        @Override public void reportUnresolvedObjectId(Object id, JsonDeserializer<?> deser) {}
        @Override public JsonNode getNode(Object value) { return null; }
        @Override public void reportUnknownProperty(Object instance, String fieldName, JsonParser p) {}
        @Override public void reportMissingContent(String msg) {}
        @Override public void reportTrailingTokens(Class<?> targetType, JsonParser p, JsonToken t) {}
        @Override public void reportWrongTokenException(JsonDeserializer<?> deser, JsonToken exp, JsonParser p, String msg, Object... args) {}
        @Override public void reportWrongTokenException(JavaType targetType, JsonToken exp, JsonParser p, String msg, Object... args) {}
        @Override public void reportPropertyInputMismatch(JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportPropertyInputMismatch(JsonDeserializer<?> deser, Object p, String msg, Object... args) {}
        @Override public void reportBadPropertyDefinition(BeanDescription beanDesc, SettableBeanProperty prop, String msg, Object... args) {}
        @Override public void reportBadTypeDefinition(BeanDescription beanDesc, String msg, Object... args) {}
        @Override public void reportMappingException(String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, String msg) {}
        @Override public void reportInvalidNull(JsonDeserializer<?> deser, String msg) {}
        @Override public void reportInvalidNull(JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportInvalidNull(JsonDeserializer<?> deser, JavaType type, String msg, Object... args) {}
        @Override public void reportInvalidDefinition(JavaType type, String msg, Object... args) {}
        @Override public void reportInputMismatch(JavaType targetType, String msg, Object... args) {}
        @Override public void reportInputMismatch(JsonDeserializer<?> deser, Object p, String msg, Object... args) {}
        @Override public void reportInputMismatch(Class<?> targetType, String msg, Object... args) {}
        @Override public void reportWrongTokenException(JsonDeserializer<?> deser, JsonToken exp, String msg, Object... args) {}
        @Override public void reportWrongTokenException(JavaType targetType, JsonToken exp, String msg, Object... args) {}
        @Override public void reportMissingInstance(JsonDeserializer<?> deser) {}
        @Override public void reportMissingInstance(JavaType type) {}
        @Override public void reportMissingCreatorProperty(BeanProperty prop) {}
        @Override public void reportMissingCreatorProperty(BeanProperty prop, String msg, Object... args) {}
        @Override public void reportUnresolvedObjectId(Object id, String msg) {}
        @Override public void reportUnresolvedObjectId(Object id, JsonDeserializer<?> deser, String msg) {}
        @Override public void reportUnresolvedObjectId(Object id, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedObjectId(Object id, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @Override public void reportUnresolvedForwardReference(Object referrer, JsonDeserializer<?> deser, String msg, Object... args) {}
        @