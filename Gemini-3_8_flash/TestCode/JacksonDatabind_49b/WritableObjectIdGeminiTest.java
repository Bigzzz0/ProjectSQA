package com.fasterxml.jackson.databind.ser.impl;

import java.io.IOException;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.core.util.JsonGeneratorDelegate;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.util.TokenBuffer;

/* [Branch & Defect Analysis Matrix]
 *
 * Target Class: com.fasterxml.jackson.databind.ser.impl.WritableObjectId
 *
 * Branch & Condition Analysis:
 * 1. Constructor:
 *    - WritableObjectId(ObjectIdGenerator<?> generator): initializes generator, id = null, idWritten = false.
 *
 * 2. writeAsId(JsonGenerator gen, SerializerProvider provider, ObjectIdWriter w):
 *    - Condition 1: ((id != null) && (idWritten || w.alwaysAsId))
 *      - False Path A: id == null (with idWritten=F, w.alwaysAsId=F/T; short-circuits, avoids NPE even if w=null) -> returns false.
 *      - False Path B: id != null, idWritten == false, w.alwaysAsId == false -> returns false.
 *      - True Path A: id != null, idWritten == true, w.alwaysAsId == false.
 *      - True Path B: id != null, idWritten == false, w.alwaysAsId == true.
 *      - True Path C: id != null, idWritten == true, w.alwaysAsId == true.
 *    - Condition 2: gen.canWriteObjectId()
 *      - True Path: calls gen.writeObjectRef(String.valueOf(id)), returns true.
 *      - False Path: calls w.serializer.serialize(id, gen, provider), returns true.
 *
 * 3. generateId(Object forPojo):
 *    - Defect Target (databind#1255 / AlwaysAsReferenceFirstTest#testIssue1255):
 *      When id is already generated (or pre-set), subsequent calls to generateId() must NOT
 *      re-invoke generator.generateId(forPojo). It must preserve and return the existing id.
 *      In the defective version, id = generator.generateId(forPojo) is unconditionally called,
 *      overwriting the existing ID and advancing stateful/sequence generators incorrectly.
 *
 * 4. writeAsField(JsonGenerator gen, SerializerProvider provider, ObjectIdWriter w):
 *    - Mutates state: idWritten = true.
 *    - Condition 1: gen.canWriteObjectId()
 *      - True Path: calls gen.writeObjectId(String.valueOf(id)), returns immediately.
 *      - False Path:
 *        - Sub-condition 2: w.propertyName != null
 *          - True Path: gen.writeFieldName(w.propertyName), w.serializer.serialize(id, gen, provider).
 *          - False Path: w.propertyName == null -> neither field name nor serializer is invoked.
 */
public class WritableObjectIdGeminiTest {

    // --- Helper Test Doubles ---

    private static class CountingGenerator extends ObjectIdGenerator<Integer> {
        private static final long serialVersionUID = 1L;
        private int counter;

        public CountingGenerator(int start) {
            this.counter = start;
        }

        @Override
        public Class<?> getScope() {
            return Object.class;
        }

        @Override
        public boolean canUseFor(ObjectIdGenerator<?> gen) {
            return gen.getClass() == getClass();
        }

        @Override
        public ObjectIdGenerator<Integer> forScope(Class<?> scope) {
            return this;
        }

        @Override
        public ObjectIdGenerator<Integer> newForSerialization(Object context) {
            return new CountingGenerator(this.counter);
        }

        @Override
        public IdKey key(Object key) {
            return new IdKey(getClass(), getScope(), key);
        }

        @Override
        public Integer generateId(Object forPojo) {
            return counter++;
        }
    }

    private static class TrackingSerializer extends JsonSerializer<Object> {
        boolean serialized = false;
        Object lastValue = null;

        @Override
        public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            this.serialized = true;
            this.lastValue = value;
            gen.writeNumber(((Number) value).intValue());
        }
    }

    private static class CustomJsonGeneratorDelegate extends JsonGeneratorDelegate {
        final boolean nativeIds;
        boolean writeObjectIdCalled = false;
        boolean writeObjectRefCalled = false;
        String writtenNativeId = null;
        String writtenRefId = null;

        public CustomJsonGeneratorDelegate(JsonGenerator d, boolean nativeIds) {
            super(d, false);
            this.nativeIds = nativeIds;
        }

        @Override
        public boolean canWriteObjectId() {
            return nativeIds;
        }

        @Override
        public void writeObjectId(Object id) throws IOException {
            this.writeObjectIdCalled = true;
            this.writtenNativeId = String.valueOf(id);
        }

        @Override
        public void writeObjectRef(Object id) throws IOException {
            this.writeObjectRefCalled = true;
            this.writtenRefId = String.valueOf(id);
        }
    }

    private ObjectIdWriter createWriter(String propName, JsonSerializer<?> ser, boolean alwaysAsId) {
        SerializableString sName = (propName == null) ? null : new SerializedString(propName);
        return new ObjectIdWriter((JavaType) null, sName, new ObjectIdGenerators.IntSequenceGenerator(), ser, alwaysAsId);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (databind#1255)
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefectIssue1255ExistingIdNotOverwrittenOnSubsequentGenerateId() {
        CountingGenerator countingGen = new CountingGenerator(1);
        WritableObjectId woid = new WritableObjectId(countingGen);

        Object firstGenerated = woid.generateId("pojoInstance");
        assertEquals(1, firstGenerated);
        assertEquals(1, woid.id);

        // Crucial defect trigger: when id has already been generated, calling generateId again
        // MUST return existing id without advancing or calling generator.generateId(forPojo).
        Object secondGenerated = woid.generateId("pojoInstance");
        assertEquals("generateId must return existing id if already generated (databind#1255)", 1, secondGenerated);
        assertEquals("woid.id state must remain unchanged", 1, woid.id);
    }

    @Test(timeout = 4000)
    public void testDefectIssue1255PreSetIdPreservedByGenerateId() {
        CountingGenerator countingGen = new CountingGenerator(500);
        WritableObjectId woid = new WritableObjectId(countingGen);
        woid.id = 999;

        Object id = woid.generateId("pojoInstance");
        assertEquals("generateId must not overwrite pre-existing id (databind#1255)", 999, id);
        assertEquals(999, woid.id);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testInitialState() {
        ObjectIdGenerator<?> gen = new ObjectIdGenerators.IntSequenceGenerator();
        WritableObjectId woid = new WritableObjectId(gen);

        assertSame(gen, woid.generator);
        assertNull(woid.id);
        assertFalse(woid.idWritten);
    }

    @Test(timeout = 4000)
    public void testWriteAsIdWhenIdNotWrittenAndNotAlwaysAsIdReturnsFalse() throws IOException {
        CountingGenerator gen = new CountingGenerator(1);
        WritableObjectId woid = new WritableObjectId(gen);
        woid.id = 123;
        woid.idWritten = false;

        TokenBuffer tb = new TokenBuffer(null, false);
        TrackingSerializer ser = new TrackingSerializer();
        ObjectIdWriter writer = createWriter("id", ser, false);

        boolean result = woid.writeAsId(tb, null, writer);
        assertFalse(result);
        assertFalse(ser.serialized);
    }

    @Test(timeout = 4000)
    public void testWriteAsIdWhenIdWrittenTrueAndNonNativeIdsSerializesValue() throws IOException {
        CountingGenerator gen = new CountingGenerator(1);
        WritableObjectId woid = new WritableObjectId(gen);
        woid.id = 42;
        woid.idWritten = true;

        TokenBuffer tb = new TokenBuffer(null, false);
        CustomJsonGeneratorDelegate customGen = new CustomJsonGeneratorDelegate(tb, false);
        TrackingSerializer ser = new TrackingSerializer();
        ObjectIdWriter writer = createWriter("id", ser, false);

        boolean result = woid.writeAsId(customGen, null, writer);
        assertTrue(result);
        assertTrue(ser.serialized);
        assertEquals(42, ser.lastValue);
        assertFalse(customGen.writeObjectRefCalled);
    }

    @Test(timeout = 4000)
    public void testWriteAsIdWhenAlwaysAsIdTrueAndNonNativeIdsSerializesValue() throws IOException {
        CountingGenerator gen = new CountingGenerator(1);
        WritableObjectId woid = new WritableObjectId(gen);
        woid.id = 77;
        woid.idWritten = false;

        TokenBuffer tb = new TokenBuffer(null, false);
        CustomJsonGeneratorDelegate customGen = new CustomJsonGeneratorDelegate(tb, false);
        TrackingSerializer ser = new TrackingSerializer();
        ObjectIdWriter writer = createWriter("id", ser, true);

        boolean result = woid.writeAsId(customGen, null, writer);
        assertTrue(result);
        assertTrue(ser.serialized);
        assertEquals(77, ser.lastValue);
        assertFalse(customGen.writeObjectRefCalled);
    }

    @Test(timeout = 4000)
    public void testWriteAsIdWhenNativeIdsSupportedCallsWriteObjectRef() throws IOException {
        CountingGenerator gen = new CountingGenerator(1);
        WritableObjectId woid = new WritableObjectId(gen);
        woid.id = 88;
        woid.idWritten = true;

        TokenBuffer tb = new TokenBuffer(null, false);
        CustomJsonGeneratorDelegate customGen = new CustomJsonGeneratorDelegate(tb, true);
        TrackingSerializer ser = new TrackingSerializer();
        ObjectIdWriter writer = createWriter("id", ser, false);

        boolean result = woid.writeAsId(customGen, null, writer);
        assertTrue(result);
        assertTrue(customGen.writeObjectRefCalled);
        assertEquals("88", customGen.writtenRefId);
        assertFalse(ser.serialized);
    }

    @Test(timeout = 4000)
    public void testWriteAsFieldWithNonNativeIdsWritesFieldAndValue() throws IOException {
        CountingGenerator gen = new CountingGenerator(1);
        WritableObjectId woid = new WritableObjectId(gen);
        woid.id = 101;
        assertFalse(woid.idWritten);

        TokenBuffer tb = new TokenBuffer(null, false);
        CustomJsonGeneratorDelegate customGen = new CustomJsonGeneratorDelegate(tb, false);
        TrackingSerializer ser = new TrackingSerializer();
        ObjectIdWriter writer = createWriter("@id", ser, false);

        woid.writeAsField(customGen, null, writer);

        assertTrue(woid.idWritten);
        assertTrue(ser.serialized);
        assertEquals(101, ser.lastValue);
        assertFalse(customGen.writeObjectIdCalled);
    }

    @Test(timeout = 4000)
    public void testWriteAsFieldWithNativeIdsCallsWriteObjectIdDirectly() throws IOException {
        CountingGenerator gen = new CountingGenerator(1);
        WritableObjectId woid = new WritableObjectId(gen);
        woid.id = 202;
        assertFalse(woid.idWritten);

        TokenBuffer tb = new TokenBuffer(null, false);
        CustomJsonGeneratorDelegate customGen = new CustomJsonGeneratorDelegate(tb, true);
        TrackingSerializer ser = new TrackingSerializer();
        ObjectIdWriter writer = createWriter("@id", ser, false);

        woid.writeAsField(customGen, null, writer);

        assertTrue(woid.idWritten);
        assertTrue(customGen.writeObjectIdCalled);
        assertEquals("202", customGen.writtenNativeId);
        assertFalse(ser.serialized);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Edge Cases
    // =========================================================================

    @Test(timeout = 4000)
    public void testWriteAsIdWhenIdIsNullShortCircuitsAndReturnsFalse() throws IOException {
        CountingGenerator gen = new CountingGenerator(1);
        WritableObjectId woid = new WritableObjectId(gen);
        assertNull(woid.id);

        TokenBuffer tb = new TokenBuffer(null, false);
        TrackingSerializer ser = new TrackingSerializer();

        // Even with alwaysAsId = true, if id is null, it must return false
        ObjectIdWriter writerAlways = createWriter("id", ser, true);
        assertFalse(woid.writeAsId(tb, null, writerAlways));

        // Even with idWritten = true, if id is null, it must return false
        woid.idWritten = true;
        ObjectIdWriter writerNotAlways = createWriter("id", ser, false);
        assertFalse(woid.writeAsId(tb, null, writerNotAlways));

        // Short-circuiting verification: null writer reference must not throw NPE when id is null
        assertFalse(woid.writeAsId(tb, null, null));
    }

    @Test(timeout = 4000)
    public void testWriteAsFieldWhenPropertyNameIsNullDoesNotWrite() throws IOException {
        CountingGenerator gen = new CountingGenerator(1);
        WritableObjectId woid = new WritableObjectId(gen);
        woid.id = 303;

        TokenBuffer tb = new TokenBuffer(null, false);
        CustomJsonGeneratorDelegate customGen = new CustomJsonGeneratorDelegate(tb, false);
        TrackingSerializer ser = new TrackingSerializer();
        ObjectIdWriter writerNullName = createWriter(null, ser, false);

        woid.writeAsField(customGen, null, writerNullName);

        assertTrue(woid.idWritten);
        assertFalse(ser.serialized);
    }

    @Test(timeout = 4000)
    public void testWriteAsIdStringIdObject() throws IOException {
        CountingGenerator gen = new CountingGenerator(1);
        WritableObjectId woid = new WritableObjectId(gen);
        woid.id = "custom-uuid-12345";
        woid.idWritten = true;

        TokenBuffer tb = new TokenBuffer(null, false);
        CustomJsonGeneratorDelegate customGen = new CustomJsonGeneratorDelegate(tb, true);
        ObjectIdWriter writer = createWriter("id", new TrackingSerializer(), false);

        boolean written = woid.writeAsId(customGen, null, writer);
        assertTrue(written);
        assertEquals("custom-uuid-12345", customGen.writtenRefId);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorAllowsNullGenerator() {
        WritableObjectId woid = new WritableObjectId(null);
        assertNull(woid.generator);
        assertNull(woid.id);
        assertFalse(woid.idWritten);
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testGenerateIdThrowsNpeWhenGeneratorIsNull() {
        WritableObjectId woid = new WritableObjectId(null);
        woid.generateId("pojo");
    }
}