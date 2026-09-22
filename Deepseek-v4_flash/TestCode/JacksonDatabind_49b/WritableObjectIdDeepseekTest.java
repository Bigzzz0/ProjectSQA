package com.fasterxml.jackson.databind.ser.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class WritableObjectIdDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * Target class: WritableObjectId
     * 
     * Branches in writeAsId():
     *   B1: (id != null) - true/false
     *   B2: (idWritten || w.alwaysAsId) - true/false (short-circuit)
     *   B3: gen.canWriteObjectId() - true/false
     *   B4: inner branch when canWriteObjectId true: writeObjectRef path
     *   B5: inner branch when canWriteObjectId false: serializer.serialize path
     * 
     * Branches in generateId():
     *   B6: always assigns id = generator.generateId(forPojo); no branch
     * 
     * Branches in writeAsField():
     *   B7: gen.canWriteObjectId() - true/false
     *   B8: name != null - true/false
     *   B9: when name != null, writeFieldName + serialize
     * 
     * Defect-targeted scenario (Issue #1255):
     *   - When id is generated for "alwaysAsId" but not written as POJO,
     *     the id must be reused. The bug occurs when generateId() is called
     *     multiple times for the same POJO, producing different ids.
     *   - Test: generateId() called twice with same POJO should return same id.
     *   - Also: writeAsField with alwaysAsId=true should write the id even if
     *     idWritten=false, and subsequent writeAsId should use the same id.
     * 
     * Boundary conditions:
     *   - id == null vs non-null
     *   - idWritten true/false
     *   - alwaysAsId true/false
     *   - canWriteObjectId true/false
     *   - propertyName null/non-null
     *   - id as String, Integer, custom object
     */

    // ===== Test Doubles =====

    private static class TestObjectIdGenerator extends ObjectIdGenerator<Object> {
        private final Object idToGenerate;
        private final Class<?> scope;
        private int callCount = 0;

        TestObjectIdGenerator(Object idToGenerate) {
            this(idToGenerate, Object.class);
        }

        TestObjectIdGenerator(Object idToGenerate, Class<?> scope) {
            this.idToGenerate = idToGenerate;
            this.scope = scope;
        }

        @Override
        public Class<?> getScope() {
            return scope;
        }

        @Override
        public boolean canUseFor(ObjectIdGenerator<?> gen) {
            return gen.getClass() == getClass() && gen.getScope().isAssignableFrom(scope);
        }

        @Override
        public ObjectIdGenerator<Object> forScope(Class<?> scope) {
            return new TestObjectIdGenerator(idToGenerate, scope);
        }

        @Override
        public ObjectIdGenerator<Object> newForSerialization(Object context) {
            return new TestObjectIdGenerator(idToGenerate, scope);
        }

        @Override
        public Object generateId(Object forPojo) {
            callCount++;
            // Simulate bug: return different id on second call for same POJO
            if (callCount > 1) {
                return "changed-" + idToGenerate;
            }
            return idToGenerate;
        }

        int getCallCount() {
            return callCount;
        }
    }

    private static class TestJsonGenerator extends JsonGenerator {
        private boolean canWriteObjectId;
        private Object writtenObjectId;
        private String writtenFieldName;
        private Object writtenObjectRef;
        private boolean writeObjectIdCalled;
        private boolean writeFieldNameCalled;

        TestJsonGenerator(boolean canWriteObjectId) {
            this.canWriteObjectId = canWriteObjectId;
        }

        @Override
        public boolean canWriteObjectId() {
            return canWriteObjectId;
        }

        @Override
        public void writeObjectId(Object id) throws IOException {
            this.writeObjectIdCalled = true;
            this.writtenObjectId = id;
        }

        @Override
        public void writeObjectRef(Object id) throws IOException {
            this.writtenObjectRef = id;
        }

        @Override
        public void writeFieldName(SerializableString name) throws IOException {
            this.writeFieldNameCalled = true;
            this.writtenFieldName = name.getValue();
        }

        @Override
        public void writeFieldName(String name) throws IOException {
            this.writeFieldNameCalled = true;
            this.writtenFieldName = name;
        }

        // Abstract methods - not used in tests
        @Override public JsonGenerator enable(Feature f) { return this; }
        @Override public JsonGenerator disable(Feature f) { return this; }
        @Override public boolean isEnabled(Feature f) { return false; }
        @Override public JsonGenerator useDefaultPrettyPrinter() { return this; }
        @Override public void writeStartArray() throws IOException {}
        @Override public void writeEndArray() throws IOException {}
        @Override public void writeStartObject() throws IOException {}
        @Override public void writeEndObject() throws IOException {}
        @Override public void writeFieldName(com.fasterxml.jackson.core.SerializableString name) throws IOException {}
        @Override public void writeString(String text) throws IOException {}
        @Override public void writeString(char[] buffer, int offset, int len) throws IOException {}
        @Override public void writeString(SerializableString text) throws IOException {}
        @Override public void writeRawUTF8String(byte[] text, int offset, int len) throws IOException {}
        @Override public void writeUTF8String(byte[] text, int offset, int len) throws IOException {}
        @Override public void writeRaw(String text) throws IOException {}
        @Override public void writeRaw(String text, int offset, int len) throws IOException {}
        @Override public void writeRaw(char[] text, int offset, int len) throws IOException {}
        @Override public void writeRaw(char c) throws IOException {}
        @Override public void writeBinary(byte[] data, int offset, int len) throws IOException { return; }
        @Override public void writeBinary(byte[] data, int offset, int len, int remaining) throws IOException {}
        @Override public void writeNumber(int v) throws IOException {}
        @Override public void writeNumber(long v) throws IOException {}
        @Override public void writeNumber(java.math.BigInteger v) throws IOException {}
        @Override public void writeNumber(double v) throws IOException {}
        @Override public void writeNumber(float v) throws IOException {}
        @Override public void writeNumber(java.math.BigDecimal v) throws IOException {}
        @Override public void writeNumber(String encodedValue) throws IOException {}
        @Override public void writeBoolean(boolean state) throws IOException {}
        @Override public void writeNull() throws IOException {}
        @Override public void writeObject(Object value) throws IOException {}
        @Override public void writeTree(com.fasterxml.jackson.core.TreeNode node) throws IOException {}
        @Override public com.fasterxml.jackson.core.JsonStreamContext getOutputContext() { return null; }
        @Override public void flush() throws IOException {}
        @Override public boolean isClosed() { return false; }
        @Override public void close() throws IOException {}
        @Override public com.fasterxml.jackson.core.JsonParser getCodec() { return null; }
        @Override public void setCodec(com.fasterxml.jackson.core.ObjectCodec oc) {}
        @Override public com.fasterxml.jackson.core.JsonGenerator copy() { return null; }
        @Override public int getOutputBuffered() { return 0; }
        @Override public boolean canWriteTypeId() { return false; }
        @Override public void writeTypeId(Object id) throws IOException {}
        @Override public boolean canWriteBinaryNatively() { return false; }
        @Override public void writeEmbeddedObject(Object object) throws IOException {}
        @Override public void writeString(char[] text, int offset, int len) throws IOException {}
        @Override public void writeRaw(char[] text, int offset, int len) throws IOException {}
        @Override public void writeRaw(String text, int offset, int len) throws IOException {}
        @Override public void writeRaw(char c) throws IOException {}
        @Override public void writeBinary(byte[] data, int offset, int len) throws IOException {}
        @Override public void writeBinary(byte[] data, int offset, int len, int remaining) throws IOException {}
        @Override public void writeNumber(int v) throws IOException {}
        @Override public void writeNumber(long v) throws IOException {}
        @Override public void writeNumber(java.math.BigInteger v) throws IOException {}
        @Override public void writeNumber(double v) throws IOException {}
        @Override public void writeNumber(float v) throws IOException {}
        @Override public void writeNumber(java.math.BigDecimal v) throws IOException {}
        @Override public void writeNumber(String encodedValue) throws IOException {}
        @Override public void writeBoolean(boolean state) throws IOException {}
        @Override public void writeNull() throws IOException {}
        @Override public void writeObject(Object value) throws IOException {}
        @Override public void writeTree(com.fasterxml.jackson.core.TreeNode node) throws IOException {}
        @Override public com.fasterxml.jackson.core.JsonStreamContext getOutputContext() { return null; }
        @Override public void flush() throws IOException {}
        @Override public boolean isClosed() { return false; }
        @Override public void close() throws IOException {}
        @Override public com.fasterxml.jackson.core.JsonParser getCodec() { return null; }
        @Override public void setCodec(com.fasterxml.jackson.core.ObjectCodec oc) {}
        @Override public com.fasterxml.jackson.core.JsonGenerator copy() { return null; }
        @Override public int getOutputBuffered() { return 0; }
        @Override public boolean canWriteTypeId() { return false; }
        @Override public void writeTypeId(Object id) throws IOException {}
        @Override public boolean canWriteBinaryNatively() { return false; }
        @Override public void writeEmbeddedObject(Object object) throws IOException {}
    }

    private static class TestSerializerProvider extends SerializerProvider {
        TestSerializerProvider() {
            super(null, null, null);
        }

        @Override
        public JsonSerializer<Object> findValueSerializer(Class<?> valueType) {
            return null;
        }

        @Override
        public JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType) {
            return null;
        }

        @Override
        public JsonSerializer<Object> findTypedValueSerializer(Class<?> valueType, boolean cache) {
            return null;
        }

        @Override
        public JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache) {
            return null;
        }

        @Override
        public JsonSerializer<Object> findKeySerializer(Class<?> keyType) {
            return null;
        }

        @Override
        public JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType) {
            return null;
        }

        @Override
        public JsonSerializer<Object> findNullValueSerializer() {
            return null;
        }

        @Override
        public JsonSerializer<Object> findNullKeySerializer() {
            return null;
        }

        @Override
        public JsonSerializer<Object> findValueSerializer(Class<?> valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public JsonSerializer<Object> findTypedValueSerializer(Class<?> valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public JsonSerializer<Object> findKeySerializer(Class<?> keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(Class<?> valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(Class<?> valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(Class<?> keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findKeySerializer(com.fasterxml.jackson.databind.JavaType keyType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullValueSerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findNullKeySerializer(com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, com.fasterxml.jackson.databind.BeanProperty property) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<Object> findTypedValueSerializer(com.fasterxml.jackson.databind.JavaType valueType, boolean cache, com.fasterxml.jackson.databind.BeanProperty property) {