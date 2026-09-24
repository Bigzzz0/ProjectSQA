package com.fasterxml.jackson.databind;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException.Reference;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;

/* [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * TARGET CLASS: com.fasterxml.jackson.databind.JsonMappingException
 * INNER CLASS:  com.fasterxml.jackson.databind.JsonMappingException.Reference
 *
 * BRANCH COVERAGE TARGETS:
 * 1. Reference.getDescription():
 *    - Cached vs uncached: _desc != null vs _desc == null
 *    - Target resolution: _from == null ("UNKNOWN") vs Class<?> vs Object instance
 *    - Array type unwrapping: 0-dim, 1-dim, multi-dim arrays (unwind component types and append [])
 *    - Target member: _fieldName != null ("\"field\"") vs _index >= 0 ([ix]) vs _index < 0 ([?])
 * 2. Reference.writeReplace():
 *    - Ensures _desc is initialized before transient _from is dropped during JDK serialization.
 * 3. JsonMappingException.wrapWithPath(Throwable, Reference):
 *    - Branch: src instanceof JsonMappingException (in-place prepend)
 *    - Branch: !(src instanceof JsonMappingException) -> extract message:
 *              msg == null, msg.length() == 0 -> fallback to "(was " + src.getClass().getName() + ")"
 *    - Branch: src instanceof JsonProcessingException -> processor extraction (proc instanceof Closeable)
 * 4. JsonMappingException.prependPath():
 *    - _path == null (lazy init LinkedList)
 *    - _path.size() < MAX_REFS_TO_LIST (1000) vs _path.size() >= MAX_REFS_TO_LIST (bounded capacity)
 * 5. JsonMappingException._buildMessage() / getMessage() / getLocalizedMessage():
 *    - _path == null -> return super.getMessage()
 *    - _path != null, msg == null vs msg != null -> append " (through reference chain: ...)"
 * 6. JsonMappingException._appendPathDesc(StringBuilder):
 *    - _path == null -> no-op
 *    - single ref vs multiple refs (joining with "->")
 *
 * DEFECT TARGET (Ground Truth: BasicExceptionTest::testLocationAddition):
 * - An issue where wrapping exceptions or calculating messages when locations/nested causes are
 *   present duplicates the "at [" location marker (two 'at [' occurrences instead of at most one).
 * ----------------------------------------------------------------------------------------------------
 */
public class JsonMappingExceptionGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testReferenceGettersAndSetters() {
        Reference ref = new Reference();
        assertNull(ref.getFrom());
        assertNull(ref.getFieldName());
        assertEquals(-1, ref.getIndex());
        assertNull(ref.getDescription());

        ref.setFieldName("myField");
        ref.setIndex(42);
        ref.setDescription("customDesc");

        assertEquals("myField", ref.getFieldName());
        assertEquals(42, ref.getIndex());
        assertEquals("customDesc", ref.getDescription());
        assertEquals("customDesc", ref.toString());
    }

    @Test(timeout = 4000)
    public void testReferenceDescriptionClassAndField() {
        Reference ref = new Reference(String.class, "value");
        assertEquals("java.lang.String[\"value\"]", ref.getDescription());
        // Verify cached description returned on subsequent call
        assertEquals("java.lang.String[\"value\"]", ref.getDescription());
    }

    @Test(timeout = 4000)
    public void testReferenceDescriptionInstanceAndIndex() {
        Reference ref = new Reference("sampleInstance", 7);
        assertEquals("java.lang.String[7]", ref.getDescription());
    }

    @Test(timeout = 4000)
    public void testReferenceDescriptionUnknownFallback() {
        Reference ref = new Reference(null, -1);
        assertEquals("UNKNOWN[?]", ref.getDescription());
    }

    @Test(timeout = 4000)
    public void testReferenceDescriptionArraysMultiDimensional() {
        Reference ref1D = new Reference(int[].class, 0);
        assertEquals("int[][0]", ref1D.getDescription());

        Reference ref2DClass = new Reference(String[][].class, "row");
        assertEquals("java.lang.String[][][\"row\"]", ref2DClass.getDescription());

        Object[][][] instance3D = new Object[1][1][1];
        Reference ref3DInstance = new Reference(instance3D, -1);
        assertEquals("java.lang.Object[][][?]", ref3DInstance.getDescription());
    }

    @Test(timeout = 4000)
    public void testPrependPathMultipleChained() {
        JsonMappingException exc = new JsonMappingException("Base mapping failure");
        assertTrue(exc.getPath().isEmpty());
        assertEquals("", exc.getPathReference());

        exc.prependPath("RootBean", "subField");
        exc.prependPath(String[].class, 3);

        List<Reference> path = exc.getPath();
        assertEquals(2, path.size());
        assertEquals("java.lang.String[][3]", path.get(0).toString());
        assertEquals("java.lang.String[\"subField\"]", path.get(1).toString());

        String pathRef = exc.getPathReference();
        assertEquals("java.lang.String[][3]->java.lang.String[\"subField\"]", pathRef);

        String msg = exc.getMessage();
        assertTrue(msg.startsWith("Base mapping failure (through reference chain: "));
        assertTrue(msg.endsWith("java.lang.String[][3]->java.lang.String[\"subField\"])"));
        assertEquals(msg, exc.getLocalizedMessage());
    }

    @Test(timeout = 4000)
    public void testGetPathReturnsUnmodifiableList() {
        JsonMappingException exc = new JsonMappingException("Test");
        exc.prependPath("Object", 1);
        List<Reference> path = exc.getPath();
        try {
            path.add(new Reference("Other", 2));
            fail("Expected UnsupportedOperationException when modifying getPath()");
        } catch (UnsupportedOperationException expected) {
            // Success
        }
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testPrependPathBindsToMaxRefsLimit() {
        JsonMappingException exc = new JsonMappingException("Overflow Test");
        for (int i = 0; i < JsonMappingException.MAX_REFS_TO_LIST + 50; i++) {
            exc.prependPath("Item", i);
        }
        assertEquals(JsonMappingException.MAX_REFS_TO_LIST, exc.getPath().size());
        // Verify head element is index 999 (the last one accepted before limit was reached)
        assertEquals(999, exc.getPath().get(0).getIndex());
    }

    @Test(timeout = 4000)
    public void testNullMessageHandlingInBuildMessage() {
        JsonMappingException exc = new JsonMappingException((String) null);
        assertNull(exc.getMessage());

        exc.prependPath("Bean", "field");
        String msg = exc.getMessage();
        assertNotNull(msg);
        assertEquals(" (through reference chain: java.lang.String[\"field\"])", msg);
    }

    @Test(timeout = 4000)
    public void testEmptyReferenceIndexBoundaries() {
        Reference refZero = new Reference("Obj", 0);
        assertEquals("java.lang.String[0]", refZero.getDescription());

        Reference refMaxInt = new Reference("Obj", Integer.MAX_VALUE);
        assertEquals("java.lang.String[" + Integer.MAX_VALUE + "]", refMaxInt.getDescription());

        Reference refNegative = new Reference("Obj", -99);
        assertEquals("java.lang.String[?]", refNegative.getDescription());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (JacksonDatabind / Location Markers)
    // =========================================================================

    private static class DummyProcessorException extends JsonProcessingException {
        private static final long serialVersionUID = 1L;
        private final Closeable _proc;

        public DummyProcessorException(String msg, JsonLocation loc, Closeable proc) {
            super(msg, loc);
            this._proc = proc;
        }

        @Override
        public Object getProcessor() {
            return _proc;
        }
    }

    private static class DummyCloseable implements Closeable {
        @Override
        public void close() throws IOException {}
    }

    /**
     * Targets defect specification:
     * BasicExceptionTest::testLocationAddition -> assertion failed: Should only get one 'at [' marker, got 2.
     * When wrapping a JsonProcessingException with existing location/formatting, ensure message does not
     * duplicate the location marker 'at ['.
     */
    @Test(timeout = 4000)
    public void testWrapWithPathDoesNotDuplicateLocationMarker() {
        JsonLocation loc = new JsonLocation("unit-test-src", 100L, 1, 1);
        DummyCloseable proc = new DummyCloseable();
        DummyProcessorException src = new DummyProcessorException("Original problem", loc, proc);

        JsonMappingException wrapped = JsonMappingException.wrapWithPath(src, "targetBean", "property");

        String msg = wrapped.getMessage();
        assertNotNull(msg);

        int firstIdx = msg.indexOf("at [");
        if (firstIdx >= 0) {
            int secondIdx = msg.indexOf("at [", firstIdx + 4);
            assertEquals("Should only get one 'at [' marker, got multiple in: " + msg, -1, secondIdx);
        }

        assertSame(proc, wrapped.getProcessor());
        assertSame(src, wrapped.getCause());
    }

    @Test(timeout = 4000)
    public void testWrapWithPathWithExistingJsonMappingException() {
        JsonMappingException jme = new JsonMappingException("Already mapping exception");
        jme.prependPath("First", 0);

        JsonMappingException wrapped = JsonMappingException.wrapWithPath(jme, "Second", "field");
        assertSame(jme, wrapped);
        assertEquals(2, wrapped.getPath().size());
        assertEquals("Second", wrapped.getPath().get(0).getFrom());
        assertEquals("First", wrapped.getPath().get(1).getFrom());
    }

    @Test(timeout = 4000)
    public void testWrapWithPathNullOrEmptyMessageFallback() {
        Exception nullMsgEx = new Exception((String) null);
        JsonMappingException jmeNull = JsonMappingException.wrapWithPath(nullMsgEx, "From", "f1");
        assertTrue(jmeNull.getMessage().contains("(was java.lang.Exception)"));

        Exception emptyMsgEx = new Exception("");
        JsonMappingException jmeEmpty = JsonMappingException.wrapWithPath(emptyMsgEx, "From", "f2");
        assertTrue(jmeEmpty.getMessage().contains("(was java.lang.Exception)"));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testReferenceConstructorNullFieldNameThrowsNPE() {
        try {
            new Reference("FromObject", null);
            fail("Expected NullPointerException for null fieldName in Reference constructor");
        } catch (NullPointerException e) {
            assertEquals("Cannot pass null fieldName", e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testFromUnexpectedIOE() {
        IOException ioe = new IOException("Disk full");
        JsonMappingException jme = JsonMappingException.fromUnexpectedIOE(ioe);

        assertNotNull(jme);
        assertTrue(jme.getMessage().contains("Unexpected IOException (of type java.io.IOException): Disk full"));
        assertNull(jme.getProcessor());
    }

    @Test(timeout = 4000)
    public void testDeprecatedConstructorsAndFactories() {
        JsonLocation loc = new JsonLocation("src", 10L, 2, 5);
        Throwable cause = new RuntimeException("root");

        // Deprecated constructors
        JsonMappingException ex1 = new JsonMappingException("msg1");
        assertEquals("msg1", ex1.getMessage());

        JsonMappingException ex2 = new JsonMappingException("msg2", cause);
        assertEquals("msg2", ex2.getMessage());
        assertSame(cause, ex2.getCause());

        JsonMappingException ex3 = new JsonMappingException("msg3", loc);
        assertTrue(ex3.getMessage().contains("msg3"));
        assertEquals(loc, ex3.getLocation());

        JsonMappingException ex4 = new JsonMappingException("msg4", loc, cause);
        assertTrue(ex4.getMessage().contains("msg4"));
        assertEquals(loc, ex4.getLocation());
        assertSame(cause, ex4.getCause());

        // Factories for JsonParser & JsonGenerator (testing with null parameters)
        JsonMappingException fromParser1 = JsonMappingException.from((JsonParser) null, "parser err");
        assertNotNull(fromParser1);
        assertEquals("parser err", fromParser1.getMessage());

        JsonMappingException fromParser2 = JsonMappingException.from((JsonParser) null, "parser err 2", cause);
        assertNotNull(fromParser2);
        assertSame(cause, fromParser2.getCause());

        JsonMappingException fromGen1 = JsonMappingException.from((JsonGenerator) null, "gen err");
        assertNotNull(fromGen1);
        assertEquals("gen err", fromGen1.getMessage());

        JsonMappingException fromGen2 = JsonMappingException.from((JsonGenerator) null, "gen err 2", cause);
        assertNotNull(fromGen2);
        assertSame(cause, fromGen2.getCause());
    }

    @Test(timeout = 4000)
    public void testConstructorWithCloseableProcessor() {
        DummyCloseable closeable = new DummyCloseable();
        JsonMappingException exMsg = new JsonMappingException(closeable, "proc msg");
        assertSame(closeable, exMsg.getProcessor());
        assertEquals("proc msg", exMsg.getMessage());

        Throwable t = new IllegalArgumentException("bad arg");
        JsonMappingException exMsgCause = new JsonMappingException(closeable, "proc msg 2", t);
        assertSame(closeable, exMsgCause.getProcessor());
        assertSame(t, exMsgCause.getCause());

        JsonLocation loc = new JsonLocation("src", 50L, 1, 1);
        JsonMappingException exMsgLoc = new JsonMappingException(closeable, "proc msg 3", loc);
        assertSame(closeable, exMsgLoc.getProcessor());
        assertEquals(loc, exMsgLoc.getLocation());
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testReferenceSerializationPreservesDescription() throws Exception {
        // NonSerializableObject cannot be serialized by Java serialization
        class NonSerializableObject {}
        NonSerializableObject rawObj = new NonSerializableObject();

        Reference ref = new Reference(rawObj, "nestedProp");
        assertEquals(rawObj, ref.getFrom());
        assertTrue(ref.getDescription().contains("NonSerializableObject[\"nestedProp\"]"));

        Reference deserializedRef = serializeAndDeserialize(ref);

        // _from is transient, so it should be dropped (null)
        assertNull(deserializedRef.getFrom());
        assertEquals("nestedProp", deserializedRef.getFieldName());
        // Description must be retained from writeReplace serialization guard
        assertTrue(deserializedRef.getDescription().contains("NonSerializableObject[\"nestedProp\"]"));
    }

    @Test(timeout = 4000)
    public void testJsonMappingExceptionSerializationRoundTrip() throws Exception {
        JsonMappingException original = new JsonMappingException("Serialization Test Failure");
        original.prependPath("MyBean", "myProp");
        original.prependPath(new Reference("ContainerList", 0));

        JsonMappingException deserialized = serializeAndDeserialize(original);

        assertNotNull(deserialized);
        assertEquals(2, deserialized.getPath().size());
        assertEquals(original.getMessage(), deserialized.getMessage());
        assertEquals(original.getPathReference(), deserialized.getPathReference());
        assertTrue(deserialized.toString().startsWith(JsonMappingException.class.getName()));
    }

    @Test(timeout = 4000)
    public void testToStringContract() {
        JsonMappingException jme = new JsonMappingException("Simple failure message");
        String stringRep = jme.toString();
        assertEquals("com.fasterxml.jackson.databind.JsonMappingException: Simple failure message", stringRep);
    }

    @Test(timeout = 4000)
    public void testGetPathReferenceWithCustomStringBuilder() {
        JsonMappingException jme = new JsonMappingException("Error");
        jme.prependPath("ObjA", "fieldA");
        StringBuilder sb = new StringBuilder("Prefix: ");
        StringBuilder returnedSb = jme.getPathReference(sb);
        assertSame(sb, returnedSb);
        assertEquals("Prefix: java.lang.String[\"fieldA\"]", returnedSb.toString());
    }

    // =========================================================================
    // Test Helpers
    // =========================================================================

    @SuppressWarnings("unchecked")
    private <T extends Serializable> T serializeAndDeserialize(T object) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(object);
        }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            return (T) ois.readObject();
        }
    }
}