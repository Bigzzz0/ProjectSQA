package com.fasterxml.jackson.databind;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

/**
 * White-box test suite for JsonMappingException, targeting known defect
 * where Reference.getDescription() omits enclosing class name for inner classes.
 */
public class JsonMappingExceptionDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Target class: JsonMappingException (and inner class Reference)
     * 
     * Key branches in Reference.getDescription():
     *   - _from == null -> "UNKNOWN"
     *   - _from instanceof Class -> use Class<?> directly
     *   - else -> use _from.getClass()
     *   - pkgName != null -> prepend package
     *   - cls.getSimpleName() -> used for class name (defect: inner classes lose enclosing name)
     *   - _fieldName != null -> append '"fieldName"'
     *   - _index >= 0 -> append index
     *   - else -> append '?'
     * 
     * Key branches in JsonMappingException:
     *   - constructors with/without processor, location
     *   - _path null vs non-null in getPath(), getMessage(), _buildMessage()
     *   - prependPath with size check against MAX_REFS_TO_LIST (1000)
     *   - wrapWithPath: src instanceof JsonMappingException vs not
     * 
     * Defect: For inner class instances, cls.getSimpleName() returns only the inner class name,
     *         e.g., "Inner" instead of "Outer$Inner". Expected: full qualified name including outer.
     *         This test suite includes a dedicated test that asserts the correct behavior,
     *         which will fail on the defective version.
     */

    // Helper inner classes to simulate the defect scenario
    static class Outer {
        public String value;
        static class Inner {
            public String innerValue;
        }
    }

    // --- Partition A: Core Functional Logic & State Transitions ---

    @Test(timeout = 4000)
    public void testConstructorWithProcessorAndMessage() {
        // Use a dummy processor (not a real parser/generator)
        Closeable dummy = new Closeable() {
            @Override
            public void close() throws IOException {}
        };
        JsonMappingException e = new JsonMappingException(dummy, "test msg");
        assertEquals("test msg", e.getMessage());
        assertSame(dummy, e.getProcessor());
    }

    @Test(timeout = 4000)
    public void testConstructorWithProcessorMessageAndThrowable() {
        Throwable cause = new RuntimeException("cause");
        JsonMappingException e = new JsonMappingException(null, "msg", cause);
        assertEquals("msg", e.getMessage());
        assertSame(cause, e.getCause());
    }

    @Test(timeout = 4000)
    public void testFactoryFromParser() {
        // We cannot easily create a real JsonParser, but we can test the static method signature
        // by using a mock-like approach? Actually we can use a simple null processor.
        // The factory methods just call constructors; we can test with null.
        JsonMappingException e = JsonMappingException.from((JsonParser) null, "from parser");
        assertNotNull(e);
        assertTrue(e.getMessage().contains("from parser"));
    }

    @Test(timeout = 4000)
    public void testFactoryFromGenerator() {
        JsonMappingException e = JsonMappingException.from((JsonGenerator) null, "from gen");
        assertNotNull(e);
        assertTrue(e.getMessage().contains("from gen"));
    }

    @Test(timeout = 4000)
    public void testFactoryFromDeserializationContext() {
        // DeserializationContext is abstract; we can't instantiate. But we can test the method exists.
        // For coverage, we can call with null (will throw NPE, but that's fine for coverage of the method call)
        try {
            JsonMappingException.from((DeserializationContext) null, "test");
            fail("Should have thrown NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testFactoryFromSerializerProvider() {
        try {
            JsonMappingException.from((SerializerProvider) null, "test");
            fail("Should have thrown NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testFromUnexpectedIOE() {
        IOException ioe = new IOException("IO error");
        JsonMappingException e = JsonMappingException.fromUnexpectedIOE(ioe);
        assertTrue(e.getMessage().contains("Unexpected IOException"));
        assertTrue(e.getMessage().contains("IOException"));
    }

    @Test(timeout = 4000)
    public void testGetPathWhenNull() {
        JsonMappingException e = new JsonMappingException((String) null);
        List<JsonMappingException.Reference> path = e.getPath();
        assertNotNull(path);
        assertTrue(path.isEmpty());
    }

    @Test(timeout = 4000)
    public void testPrependPathAndGetPath() {
        JsonMappingException e = new JsonMappingException((String) null);
        Object refFrom = new Object();
        e.prependPath(refFrom, "field1");
        List<JsonMappingException.Reference> path = e.getPath();
        assertEquals(1, path.size());
        assertEquals("field1", path.get(0).getFieldName());
        assertSame(refFrom, path.get(0).getFrom());
    }

    @Test(timeout = 4000)
    public void testPrependPathWithIndex() {
        JsonMappingException e = new JsonMappingException((String) null);
        e.prependPath(new Object(), 42);
        List<JsonMappingException.Reference> path = e.getPath();
        assertEquals(1, path.size());
        assertEquals(42, path.get(0).getIndex());
    }

    @Test(timeout = 4000)
    public void testPrependPathReferenceObject() {
        JsonMappingException e = new JsonMappingException((String) null);
        JsonMappingException.Reference ref = new JsonMappingException.Reference(new Object(), "test");
        e.prependPath(ref);
        assertEquals(1, e.getPath().size());
    }

    @Test(timeout = 4000)
    public void testPrependPathMaxRefs() {
        JsonMappingException e = new JsonMappingException((String) null);
        // Add MAX_REFS_TO_LIST + 1 references
        for (int i = 0; i < 1001; i++) {
            e.prependPath(new Object(), "field" + i);
        }
        // Should only have MAX_REFS_TO_LIST entries
        assertEquals(1000, e.getPath().size());
    }

    @Test(timeout = 4000)
    public void testGetPathReference() {
        JsonMappingException e = new JsonMappingException((String) null);
        e.prependPath("fromObj", "field");
        String pathRef = e.getPathReference();
        assertTrue(pathRef.contains("field"));
    }

    @Test(timeout = 4000)
    public void testGetPathReferenceWithBuilder() {
        JsonMappingException e = new JsonMappingException((String) null);
        e.prependPath("fromObj", "field");
        StringBuilder sb = new StringBuilder("prefix");
        e.getPathReference(sb);
        assertTrue(sb.toString().startsWith("prefix"));
        assertTrue(sb.toString().contains("field"));
    }

    // --- Partition B: Boundary Value Analysis & Extremes ---

    @Test(timeout = 4000)
    public void testReferenceConstructorWithNullFieldName() {
        try {
            new JsonMappingException.Reference(new Object(), (String) null);
            fail("Should have thrown NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testReferenceDefaultConstructor() {
        JsonMappingException.Reference ref = new JsonMappingException.Reference();
        assertNull(ref.getFrom());
        assertNull(ref.getFieldName());
        assertEquals(-1, ref.getIndex());
    }

    @Test(timeout = 4000)
    public void testReferenceFromObjectOnly() {
        Object from = new Object();
        JsonMappingException.Reference ref = new JsonMappingException.Reference(from);
        assertSame(from, ref.getFrom());
        assertNull(ref.getFieldName());
        assertEquals(-1, ref.getIndex());
    }

    @Test(timeout = 4000)
    public void testReferenceFromObjectAndFieldName() {
        Object from = new Object();
        JsonMappingException.Reference ref = new JsonMappingException.Reference(from, "myField");
        assertSame(from, ref.getFrom());
        assertEquals("myField", ref.getFieldName());
        assertEquals(-1, ref.getIndex());
    }

    @Test(timeout = 4000)
    public void testReferenceFromObjectAndIndex() {
        Object from = new Object();
        JsonMappingException.Reference ref = new JsonMappingException.Reference(from, 5);
        assertSame(from, ref.getFrom());
        assertNull(ref.getFieldName());
        assertEquals(5, ref.getIndex());
    }

    @Test(timeout = 4000)
    public void testReferenceSetters() {
        JsonMappingException.Reference ref = new JsonMappingException.Reference();
        ref.setFieldName("setField");
        ref.setIndex(99);
        ref.setDescription("desc");
        assertEquals("setField", ref.getFieldName());
        assertEquals(99, ref.getIndex());
        // getDescription should return the set description if not null
        assertEquals("desc", ref.getDescription());
    }

    @Test(timeout = 4000)
    public void testReferenceGetDescriptionWithNullFrom() {
        JsonMappingException.Reference ref = new JsonMappingException.Reference();
        // _from is null, _desc is null
        String desc = ref.getDescription();
        assertTrue(desc.startsWith("UNKNOWN"));
    }

    @Test(timeout = 4000)
    public void testReferenceGetDescriptionWithClassFrom() {
        JsonMappingException.Reference ref = new JsonMappingException.Reference(String.class, "value");
        String desc = ref.getDescription();
        // Should contain package java.lang and class name String
        assertTrue(desc.contains("java.lang.String"));
        assertTrue(desc.contains("\"value\""));
    }

    @Test(timeout = 4000)
    public void testReferenceGetDescriptionWithIndexOnly() {
        Object from = new Object();
        JsonMappingException.Reference ref = new JsonMappingException.Reference(from, 123);
        String desc = ref.getDescription();
        // Should contain the index
        assertTrue(desc.contains("123"));
        // Should not contain field name
        assertFalse(desc.contains("\""));
    }

    @Test(timeout = 4000)
    public void testReferenceGetDescriptionWithNoFieldNoIndex() {
        Object from = new Object();
        JsonMappingException.Reference ref = new JsonMappingException.Reference(from);
        String desc = ref.getDescription();
        // Should contain '?'
        assertTrue(desc.contains("?"));
    }

    @Test(timeout = 4000)
    public void testReferenceToString() {
        Object from = new Object();
        JsonMappingException.Reference ref = new JsonMappingException.Reference(from, "field");
        assertEquals(ref.getDescription(), ref.toString());
    }

    // --- Partition C: Defect-Targeted Branch Zone ---

    @Test(timeout = 4000)
    public void testReferenceGetDescriptionForInnerClass() {
        // This test targets the known defect: inner class name missing enclosing class.
        // Create an instance of an inner class (Outer.Inner)
        Outer.Inner innerObj = new Outer.Inner();
        JsonMappingException.Reference ref = new JsonMappingException.Reference(innerObj, "innerField");
        String desc = ref.getDescription();
        // Expected: should contain "Outer$Inner" (the full qualified inner class name)
        // The defective version will produce just "Inner" (simple name)
        assertTrue("Description should include outer class name: " + desc,
                desc.contains("Outer$Inner"));
        // Also ensure field name is present
        assertTrue(desc.contains("\"innerField\""));
    }

    @Test(timeout = 4000)
    public void testWrapWithPathFromNonJsonMappingException() {
        IOException src = new IOException("original");
        Object refFrom = new Object();
        JsonMappingException result = JsonMappingException.wrapWithPath(src, refFrom, "field");
        assertNotNull(result);
        assertTrue(result.getMessage().contains("original"));
        assertEquals(1, result.getPath().size());
        assertEquals("field", result.getPath().get(0).getFieldName());
    }

    @Test(timeout = 4000)
    public void testWrapWithPathFromJsonMappingException() {
        JsonMappingException original = new JsonMappingException((String) null, "base");
        original.prependPath("obj1", "f1");
        JsonMappingException result = JsonMappingException.wrapWithPath(original, "obj2", "f2");
        // Should be same instance and path prepended
        assertSame(original, result);
        assertEquals(2, result.getPath().size());
        assertEquals("f2", result.getPath().get(0).getFieldName());
        assertEquals("f1", result.getPath().get(1).getFieldName());
    }

    @Test(timeout = 4000)
    public void testWrapWithPathWithIndex() {
        IOException src = new IOException("err");
        JsonMappingException result = JsonMappingException.wrapWithPath(src, new Object(), 7);
        assertEquals(7, result.getPath().get(0).getIndex());
    }

    @Test(timeout = 4000)
    public void testWrapWithPathWithNullMessage() {
        // When src message is null, wrapWithPath should use a placeholder
        IOException src = new IOException();
        JsonMappingException result = JsonMappingException.wrapWithPath(src, new Object(), "f");
        assertTrue(result.getMessage().contains("was java.io.IOException"));
    }

    // --- Partition D: Exception & Defensive Guard Paths ---

    @Test(timeout = 4000)
    public void testGetMessageWithPath() {
        JsonMappingException e = new JsonMappingException((String) null, "root cause");
        e.prependPath("obj", "field");
        String msg = e.getMessage();
        assertTrue(msg.contains("root cause"));
        assertTrue(msg.contains("through reference chain"));
    }

    @Test(timeout = 4000)
    public void testGetMessageWithoutPath() {
        JsonMappingException e = new JsonMappingException((String) null, "simple");
        assertEquals("simple", e.getMessage());
    }

    @Test(timeout = 4000)
    public void testGetLocalizedMessage() {
        JsonMappingException e = new JsonMappingException((String) null, "localized");
        assertEquals(e.getMessage(), e.getLocalizedMessage());
    }

    @Test(timeout = 4000)
    public void testToString() {
        JsonMappingException e = new JsonMappingException((String) null, "to string");
        String str = e.toString();
        assertTrue(str.startsWith("com.fasterxml.jackson.databind.JsonMappingException"));
        assertTrue(str.contains("to string"));
    }

    @Test(timeout = 4000)
    public void testDeprecatedConstructors() {
        // These are deprecated but still present; test for coverage
        JsonMappingException e1 = new JsonMappingException("deprecated msg");
        assertEquals("deprecated msg", e1.getMessage());

        JsonMappingException e2 = new JsonMappingException("msg", new RuntimeException());
        assertNotNull(e2.getCause());

        JsonMappingException e3 = new JsonMappingException("msg", (JsonLocation) null);
        assertEquals("msg", e3.getMessage());

        JsonMappingException e4 = new JsonMappingException("msg", null, new RuntimeException());
        assertEquals("msg", e4.getMessage());
    }

    // --- Partition E: Object Lifecycle & Contract Integrity ---

    @Test(timeout = 4000)
    public void testReferenceWriteReplace() {
        // writeReplace should return this after ensuring description is set
        JsonMappingException.Reference ref = new JsonMappingException.Reference(new Object(), "f");
        Object replacement = ref.writeReplace();
        assertSame(ref, replacement);
        // Description should be non-null after writeReplace
        assertNotNull(ref.getDescription());
    }

    @Test(timeout = 4000)
    public void testReferenceSerialization() {
        // Basic check that Reference is Serializable (no exception thrown)
        JsonMappingException.Reference ref = new JsonMappingException.Reference("from", "field");
        // Just ensure no exception during writeReplace (which is called by serialization)
        ref.writeReplace();
    }

    @Test(timeout = 4000)
    public void testPathReferenceWithMultipleReferences() {
        JsonMappingException e = new JsonMappingException((String) null);
        e.prependPath("obj1", "field1");
        e.prependPath("obj2", "field2");
        String pathRef = e.getPathReference();
        assertTrue(pathRef.contains("field1"));
        assertTrue(pathRef.contains("field2"));
        assertTrue(pathRef.contains("->"));
    }

    @Test(timeout = 4000)
    public void testAppendPathDescWithNullPath() {
        // _appendPathDesc is protected; we can test indirectly via getPathReference when _path is null
        JsonMappingException e = new JsonMappingException((String) null);
        assertEquals("", e.getPathReference());
    }
}