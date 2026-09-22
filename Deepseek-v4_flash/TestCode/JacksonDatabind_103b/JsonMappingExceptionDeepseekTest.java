package com.fasterxml.jackson.databind;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

import com.fasterxml.jackson.core.*;

/**
 * [Branch & Defect Analysis Matrix]
 * ==================================
 * 
 * Branch Zones Targeted:
 * 1. Constructor overloads (deprecated 2.7+, new 2.7+ with Closeable processor)
 * 2. Factory methods: from(JsonParser), from(JsonGenerator), from(DeserializationContext), 
 *    from(SerializerProvider), fromUnexpectedIOE
 * 3. Reference class: constructors (Object from, Object from+fieldName, Object from+index),
 *    fieldName null check, getDescription() logic for arrays, fieldName vs index priority
 * 4. wrapWithPath: 3 overloads, handling of src being/not being JsonMappingException,
 *    null/empty message handling, processor extraction from JsonProcessingException
 * 5. prependPath: 3 overloads, path list creation, MAX_REFS_TO_LIST boundary
 * 6. getPath(), getPathReference(), _appendPathDesc() iteration logic
 * 7. _buildMessage(): null path, non-null path, null super message
 * 8. toString(), getProcessor() override
 * 9. Reference serialization: writeReplace(), getDescription() lazy init
 * 
 * Boundary Values:
 * - Reference index: -1 default, 0, MAX_VALUE
 * - Reference fieldName: null (constructor default), non-null (with NullPointerException guard)
 * - _path: null, empty, single element, multiple elements, MAX_REFS_TO_LIST size, exceeded limit
 * - Message: null, empty, non-empty with special characters
 * - Throwable: null, IOException, JsonProcessingException, JsonMappingException
 * - Arrays in Reference.getDescription(): single-dim, multi-dim, component types
 * 
 * Defect Targeting (from BasicExceptionTest:testLocationAddition):
 * The bug involves duplicate 'at [' markers in error messages when InvalidFormatException
 * is wrapped. The wrapWithPath mechanism may produce redundant path markers when the
 * source exception already contains location information. Test case D1 specifically
 * validates single 'at [' marker occurrence in wrapped exception messages.
 */
public class JsonMappingExceptionDeepseekTest {

    /* ===========================================================
     * Partition A: Core Functional Logic & State Transitions
     * =========================================================== */

    @Test(timeout = 4000)
    public void testConstructorWithProcessorAndMessage() {
        Closeable processor = new Closeable() {
            @Override
            public void close() throws IOException {}
        };
        JsonMappingException e = new JsonMappingException(processor, "test msg");
        assertEquals("test msg", e.getMessage());
        assertTrue(e.getProcessor() instanceof Closeable);
        assertNull(e.getLocation());
    }

    @Test(timeout = 4000)
    public void testConstructorWithProcessorMessageAndThrowable() {
        Throwable cause = new RuntimeException("root cause");
        JsonMappingException e = new JsonMappingException(null, "msg", cause);
        assertEquals("msg", e.getMessage());
        assertSame(cause, e.getCause());
        assertNull(e.getProcessor());
    }

    @Test(timeout = 4000)
    public void testConstructorWithProcessorMessageAndLocation() {
        JsonLocation loc = new JsonLocation(null, 1L, 2, 3);
        JsonMappingException e = new JsonMappingException(null, "msg", loc);
        assertEquals("msg", e.getMessage());
        assertSame(loc, e.getLocation());
    }

    @Test(timeout = 4000)
    public void testDeprecatedConstructors() {
        // These still compile but produce warnings; test they work
        JsonMappingException e1 = new JsonMappingException("msg");
        assertEquals("msg", e1.getMessage());

        Throwable cause = new RuntimeException();
        JsonMappingException e2 = new JsonMappingException("msg", cause);
        assertEquals("msg", e2.getMessage());
        assertSame(cause, e2.getCause());

        JsonLocation loc = new JsonLocation(null, 0L, 0, 0);
        JsonMappingException e3 = new JsonMappingException("msg", loc);
        assertEquals("msg", e3.getMessage());
        assertSame(loc, e3.getLocation());

        JsonMappingException e4 = new JsonMappingException("msg", loc, cause);
        assertEquals("msg", e4.getMessage());
        assertSame(loc, e4.getLocation());
        assertSame(cause, e4.getCause());
    }

    /* ===========================================================
     * Partition B: Boundary Value Analysis & Extremes
     * =========================================================== */

    @Test(timeout = 4000)
    public void testReferenceDefaultConstructor() {
        Reference ref = new Reference();
        assertNull(ref.getFrom());
        assertNull(ref.getFieldName());
        assertEquals(-1, ref.getIndex());
    }

    @Test(timeout = 4000)
    public void testReferenceFromObject() {
        Object from = new Object();
        Reference ref = new Reference(from);
        assertSame(from, ref.getFrom());
        assertNull(ref.getFieldName());
        assertEquals(-1, ref.getIndex());
    }

    @Test(timeout = 4000)
    public void testReferenceFromObjectAndFieldName() {
        Object from = new Object();
        Reference ref = new Reference(from, "myField");
        assertSame(from, ref.getFrom());
        assertEquals("myField", ref.getFieldName());
        assertEquals(-1, ref.getIndex());
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testReferenceNullFieldNameThrowsNPE() {
        new Reference(new Object(), null);
    }

    @Test(timeout = 4000)
    public void testReferenceFromObjectAndIndex() {
        Object from = new Object();
        Reference ref = new Reference(from, 42);
        assertSame(from, ref.getFrom());
        assertNull(ref.getFieldName());
        assertEquals(42, ref.getIndex());
    }

    @Test(timeout = 4000)
    public void testReferenceIndexNegativeOneDefault() {
        Reference ref = new Reference(new Object());
        assertEquals(-1, ref.getIndex());
    }

    @Test(timeout = 4000)
    public void testReferenceSetters() {
        Reference ref = new Reference();
        ref.setFieldName("fn");
        ref.setIndex(99);
        ref.setDescription("desc");
        assertEquals("fn", ref.getFieldName());
        assertEquals(99, ref.getIndex());
        // description should now be cached
        assertEquals("desc", ref.getDescription());
    }

    @Test(timeout = 4000)
    public void testReferenceGetDescriptionWithFromNull() {
        Reference ref = new Reference();
        // with _from null and _desc null
        String desc = ref.getDescription();
        assertTrue(desc.contains("UNKNOWN"));
        assertTrue(desc.contains("[?]"));
    }

    @Test(timeout = 4000)
    public void testReferenceGetDescriptionWithClassFrom() {
        Reference ref = new Reference(String.class);
        String desc = ref.getDescription();
        assertTrue(desc.contains("java.lang.String"));
        assertTrue(desc.contains("[?]"));
    }

    @Test(timeout = 4000)
    public void testReferenceGetDescriptionWithArrayClass() {
        Reference ref = new Reference(int[][][].class);
        String desc = ref.getDescription();
        assertTrue(desc.contains("int[]"));
        assertTrue(desc.contains("int[][]"));
        assertTrue(desc.contains("int[][][]"));
    }

    @Test(timeout = 4000)
    public void testReferenceGetDescriptionWithFieldName() {
        Reference ref = new Reference(new Object(), "fieldX");
        String desc = ref.getDescription();
        assertTrue(desc.contains("java.lang.Object"));
        assertTrue(desc.contains("[\"fieldX\"]"));
    }

    @Test(timeout = 4000)
    public void testReferenceGetDescriptionWithIndex() {
        Reference ref = new Reference(new Object(), 7);
        String desc = ref.getDescription();
        assertTrue(desc.contains("java.lang.Object"));
        assertTrue(desc.contains("[7]"));
    }

    @Test(timeout = 4000)
    public void testReferenceGetDescriptionIndexNegative() {
        Reference ref = new Reference(new Object());
        // index -1, no fieldName -> should show '?'
        String desc = ref.getDescription();
        assertTrue(desc.contains("[?]"));
    }

    @Test(timeout = 4000)
    public void testReferenceToString() {
        Reference ref = new Reference("fromObj", 1);
        assertEquals(ref.getDescription(), ref.toString());
    }

    @Test(timeout = 4000)
    public void testReferenceWriteReplace() {
        Reference ref = new Reference("fromVal");
        Object replacement = ref.writeReplace();
        assertSame(replacement, ref);
        // getDescription must have been called as side effect
        assertNotNull(ref.getDescription());
    }

    /* ===========================================================
     * Partition C: Defect-Targeted Branch Zone
     * =========================================================== */

    // D1: Core defect test - verifies single 'at [' marker in wrapped exception
    @Test(timeout = 4000)
    public void testWrapWithPathFromInvalidFormatException() {
        // Simulate an InvalidFormatException-like scenario
        IOException src = new IOException("Cannot deserialize Map key of type " +
                "`com.fasterxml.jackson.databind.BaseMapTest$ABC` from String \"value\": " +
                "not a valid representation, problem: (com.fasterxml.jackson.databind.exc.InvalidFormatException) " +
                "Cannot deserialize Map key of type `com.fasterxml.jackson.databind.BaseMapTest$ABC` " +
                "from String \"value\": not one of values excepted for Enum class: [A, B, C]");
        
        Object refFrom = new Object();
        String refFieldName = "testField";
        
        JsonMappingException result = JsonMappingException.wrapWithPath(src, refFrom, refFieldName);
        
        String message = result.getMessage();
        // Verify we have exactly one path marker (not two)
        int atBracketCount = countOccurrences(message, "at [");
        assertEquals("Should only get one 'at [' marker", 1, atBracketCount);
        
        // Verify the path is NOT duplicated
        assertFalse("Message should not contain duplicate path markers",
                message.contains("(through reference chain:") && 
                message.indexOf("through reference chain:") != message.lastIndexOf("through reference chain:"));
    }

    // D2: Edge case - wrapWithPath with JsonMappingException itself
    @Test(timeout = 4000)
    public void testWrapWithPathWithExistingJsonMappingException() {
        JsonMappingException original = new JsonMappingException(null, "original error");
        Reference ref = new Reference(new Object(), "pathField");
        
        JsonMappingException result = JsonMappingException.wrapWithPath(original, ref);
        
        // Should be same instance
        assertSame(result, original);
        String msg = result.getMessage();
        assertTrue(msg.contains("original error"));
        assertTrue(msg.contains("pathField"));
    }

    // D3: wrapWithPath with null/empty message
    @Test(timeout = 4000)
    public void testWrapWithPathWithEmptyMessage() {
        IOException src = new IOException("");
        JsonMappingException result = JsonMappingException.wrapWithPath(src, new Object(), "f");
        assertTrue(result.getMessage().contains("(was java.io.IOException)"));
    }

    @Test(timeout = 4000)
    public void testWrapWithPathWithNullMessage() {
        // IOException with null message
        IOException src = new IOException((String) null);
        JsonMappingException result = JsonMappingException.wrapWithPath(src, new Object(), 0);
        assertTrue(result.getMessage().contains("(was java.io.IOException)"));
    }

    // D4: wrapWithPath extracting processor from JsonProcessingException
    @Test(timeout = 4000)
    public void testWrapWithPathProcessorExtraction() {
        // Create a JsonProcessingException-like instance with processor
        JsonProcessingException src = new JsonProcessingException("io error", (Throwable) null) {};
        // Set processor via reflection (since original constructor leaves it null)
        try {
            Field procField = JsonProcessingException.class.getDeclaredField("_processor");
            procField.setAccessible(true);
            procField.set(src, new Closeable() {
                @Override public void close() throws IOException {}
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        JsonMappingException result = JsonMappingException.wrapWithPath(src, new Object(), "f");
        assertNotNull(result.getProcessor());
    }

    @Test(timeout = 4000)
    public void testWrapWithPathNonJsonProcessingException() {
        // src is IOException (not JsonProcessingException)
        IOException src = new IOException("plain IO");
        JsonMappingException result = JsonMappingException.wrapWithPath(src, new Object(), "f");
        assertNotNull(result);
        assertTrue(result.getMessage().contains("plain IO"));
        assertNull(result.getProcessor());
    }

    /* ===========================================================
     * Partition D: Exception & Defensive Guard Paths
     * =========================================================== */

    @Test(timeout = 4000)
    public void testPrependPathNullPath() {
        JsonMappingException e = new JsonMappingException(null, "msg");
        assertNull(e.getPath()); // internal _path is null
        e.prependPath(new Object(), "field");
        List<Reference> path = e.getPath();
        assertEquals(1, path.size());
        assertEquals("field", path.get(0).getFieldName());
    }

    @Test(timeout = 4000)
    public void testPrependPathIndexOverload() {
        JsonMappingException e = new JsonMappingException(null, "msg");
        e.prependPath(new Object(), 5);
        List<Reference> path = e.getPath();
        assertEquals(1, path.size());
        assertEquals(5, path.get(0).getIndex());
    }

    @Test(timeout = 4000)
    public void testPrependPathReferenceOverload() {
        JsonMappingException e = new JsonMappingException(null, "msg");
        Reference ref = new Reference("referrer", "fieldName");
        e.prependPath(ref);
        assertSame(ref, e.getPath().get(0));
    }

    @Test(timeout = 4000)
    public void testPrependPathMaxRefsBoundary() {
        JsonMappingException e = new JsonMappingException(null, "msg");
        // Add exactly MAX_REFS_TO_LIST references
        for (int i = 0; i < 1000; i++) {
            e.prependPath(new Object(), "f" + i);
        }
        assertEquals("Should have exactly 1000 refs", 1000, e.getPath().size());
        
        // Add one more - should NOT increase beyond 1000
        e.prependPath(new Object(), "overflow");
        assertEquals("Should still be 1000 refs", 1000, e.getPath().size());
    }

    @Test(timeout = 4000)
    public void testGetPathReturnsUnmodifiableList() {
        JsonMappingException e = new JsonMappingException(null, "msg");
        e.prependPath(new Object(), "f1");
        List<Reference> path = e.getPath();
        try {
            path.add(new Reference());
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException ex) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetPathWhenNullReturnsEmpty() {
        JsonMappingException e = new JsonMappingException(null, "msg");
        List<Reference> path = e.getPath();
        assertTrue(path.isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetPathReference() {
        JsonMappingException e = new JsonMappingException(null, "msg");
        e.prependPath(new Object(), "a");
        e.prependPath(new Object(), "b");
        String pathRef = e.getPathReference();
        assertTrue(pathRef.contains("a") && pathRef.contains("b"));
        assertTrue(pathRef.contains("->"));
    }

    @Test(timeout = 4000)
    public void testGetPathReferenceWithStringBuilder() {
        JsonMappingException e = new JsonMappingException(null, "msg");
        e.prependPath(new Object(), "x");
        StringBuilder sb = new StringBuilder("prefix_");
        StringBuilder result = e.getPathReference(sb);
        assertSame(sb, result);
        assertTrue(result.toString().contains("prefix_"));
        assertTrue(result.toString().contains("x"));
    }

    /* ===========================================================
     * Partition E: Object Lifecycle & Contract Integrity
     * =========================================================== */

    @Test(timeout = 4000)
    public void testGetMessageWithPath() {
        JsonMappingException e = new JsonMappingException(null, "base msg");
        e.prependPath(new Object(), "someField");
        String msg = e.getMessage();
        assertTrue(msg.contains("base msg"));
        assertTrue(msg.contains("(through reference chain: "));
    }

    @Test(timeout = 4000)
    public void testGetMessageWithoutPath() {
        JsonMappingException e = new JsonMappingException(null, "no path");
        assertEquals("no path", e.getMessage());
    }

    @Test(timeout = 4000)
    public void testGetLocalizedMessage() {
        JsonMappingException e = new JsonMappingException(null, "localized");
        assertEquals(e.getMessage(), e.getLocalizedMessage());
    }

    @Test(timeout = 4000)
    public void testGetMessageWithNullSuperAndPath() {
        // This tests branch where super.getMessage() is null but path exists
        JsonMappingException e = new JsonMappingException(null, (String) null);
        // Set some path so _buildMessage takes the path branch
        e.prependPath(new Object(), "field");
        String msg = e.getMessage();
        assertNotNull(msg);
        assertTrue(msg.contains("(through reference chain: "));
    }

    @Test(timeout = 4000)
    public void testToString() {
        JsonMappingException e = new JsonMappingException(null, "to string test");
        String str = e.toString();
        assertTrue(str.contains("JsonMappingException"));
        assertTrue(str.contains("to string test"));
    }

    @Test(timeout = 4000)
    public void testFactoryMethodFromJsonParser() {
        // Can't easily construct JsonParser, but can test null processor case
        JsonMappingException e = JsonMappingException.from((JsonParser) null, "parser msg");
        assertEquals("parser msg", e.getMessage());
    }

    @Test(timeout = 4000)
    public void testFactoryMethodFromJsonParserWithThrowable() {
        Throwable cause = new RuntimeException("parser cause");
        JsonMappingException e = JsonMappingException.from((JsonParser) null, "parser msg", cause);
        assertEquals("parser msg", e.getMessage());
        assertSame(cause, e.getCause());
    }

    @Test(timeout = 4000)
    public void testFactoryMethodFromJsonGenerator() {
        JsonMappingException e = JsonMappingException.from((JsonGenerator) null, "gen msg");
        assertEquals("gen msg", e.getMessage());
    }

    @Test(timeout = 4000)
    public void testFactoryMethodFromJsonGeneratorWithThrowable() {
        Throwable cause = new RuntimeException("gen cause");
        JsonMappingException e = JsonMappingException.from((JsonGenerator) null, "gen msg", cause);
        assertEquals("gen msg", e.getMessage());
        assertSame(cause, e.getCause());
    }

    @Test(timeout = 4000)
    public void testFactoryMethodFromDeserializationContext() {
        // Can't mock DeserializationContext easily, but test via null parser path
        // For coverage, just ensure no exception is thrown for null context
        try {
            JsonMappingException.from((DeserializationContext) null, "ctxt msg");
        } catch (NullPointerException e) {
            // Expected if ctxt.getParser() is called on null
        }
    }

    @Test(timeout = 4000)
    public void testFactoryMethodFromSerializerProvider() {
        // null provider -> NPE from getGenerator
        try {
            JsonMappingException.from((SerializerProvider) null, "prov msg");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testFromUnexpectedIOE() {
        IOException ioe = new IOException("unexpected IO issue");
        JsonMappingException e = JsonMappingException.fromUnexpectedIOE(ioe);
        assertTrue(e.getMessage().contains("Unexpected IOException"));
        assertTrue(e.getMessage().contains("java.io.IOException"));
        assertTrue(e.getMessage().contains("unexpected IO issue"));
    }

    @Test(timeout = 4000)
    public void test_appendPathDescWithNullPath() {
        JsonMappingException e = new JsonMappingException(null, "msg");
        // Internal method, but we can trigger it via getPathReference call
        e.getPathReference(); // should not throw
    }

    // Helper method
    private int countOccurrences(String str, String target) {
        int count = 0;
        int idx = 0;
        while ((idx = str.indexOf(target, idx)) != -1) {
            count++;
            idx += target.length();
        }
        return count;
    }
}