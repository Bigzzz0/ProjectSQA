package com.fasterxml.jackson.databind;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringWriter;
import java.util.List;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.deser.BeanDeserializerFactory;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;

/* [Branch & Defect Analysis Matrix]
 * -------------------------------------------------------------------------------------------------
 * Target Class: com.fasterxml.jackson.databind.JsonMappingException
 * Target Inner Class: com.fasterxml.jackson.databind.JsonMappingException.Reference
 *
 * 1. DEFECT-TARGETED BRANCH: [databind#1403] / Defects4J Reference Chain for Inner/Nested Classes
 *    - Branch: Reference.getDescription() -> class name formatting logic
 *    - Defect: Using `cls.getSimpleName()` strips enclosing class names for inner/nested classes
 *      (e.g., `OuterClass$InnerClass` becomes `InnerClass` in description output).
 *    - Trigger Tests: `testReferenceChainForInnerClass`, `testReferenceChainForDeepInnerClass`,
 *      `testReferenceChainForInnerClassInstance`.
 *
 * 2. PARTITION A: Core Functional Logic & State Transitions
 *    - Lifecycle constructors: deprecated variants, Closeable processor variants, Location handling.
 *    - Factory methods: `from(JsonParser)`, `from(JsonGenerator)`, `from(DeserializationContext)`,
 *      `from(SerializerProvider)`, `fromUnexpectedIOE(IOException)`.
 *    - Path management: `prependPath(Reference)`, `prependPath(Object, String)`,
 *      `prependPath(Object, int)`.
 *    - Formatting: `getPathReference()`, `getMessage()`, `getLocalizedMessage()`, `toString()`.
 *
 * 3. PARTITION B: Boundary Value Analysis (BVA) & Extremes
 *    - Max reference limit (`MAX_REFS_TO_LIST` = 1000): tests boundary at 999, 1000, 1005 items.
 *    - Reference with `_from == null`, `_fieldName == null`, `_index < 0`, `_index == 0`, `_index > 0`.
 *    - Exception with null message, empty message, single reference, chained references.
 *
 * 4. PARTITION C: Exception & Defensive Guard Paths
 *    - `new Reference(from, null)` must throw `NullPointerException`.
 *    - `wrapWithPath` when source has null/empty message -> generates "(was <ClassName>)".
 *    - `wrapWithPath` when source is `JsonProcessingException` with Closeable vs non-Closeable processor.
 *    - Path list immutability (`getPath()`).
 *
 * 5. PARTITION D: Object Lifecycle & Contract Integrity
 *    - JDK Serialization of `JsonMappingException` and `Reference`.
 *    - Verification of transient `_from` handling via `writeReplace()` and `_desc` persistence.
 * -------------------------------------------------------------------------------------------------
 */
public class JsonMappingExceptionGeminiTest {

    // Helper static nested classes for defect targeting
    static class OuterClass {
        static class InnerClass {
        }
    }

    /*
     * ---------------------------------------------------------------------------------------------
     * Partition C: Defect-Targeted Branch Zone (Defects4J Known Bug: Databind #1403)
     * ---------------------------------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testReferenceChainForInnerClass() {
        JsonMappingException.Reference ref = new JsonMappingException.Reference(OuterClass.class, "inner");
        String desc = ref.getDescription();
        // Buggy version returns "com.fasterxml.jackson.databind.OuterClass[\"inner\"]" dropping the enclosing class
        assertEquals("com.fasterxml.jackson.databind.JsonMappingExceptionGeminiTest$OuterClass[\"inner\"]", desc);
    }

    @Test(timeout = 4000)
    public void testReferenceChainForDeepInnerClass() {
        JsonMappingException.Reference ref = new JsonMappingException.Reference(OuterClass.InnerClass.class, "field");
        String desc = ref.getDescription();
        assertEquals("com.fasterxml.jackson.databind.JsonMappingExceptionGeminiTest$OuterClass$InnerClass[\"field\"]", desc);
    }

    @Test(timeout = 4000)
    public void testReferenceChainForInnerClassInstance() {
        OuterClass outerInstance = new OuterClass();
        JsonMappingException.Reference ref = new JsonMappingException.Reference(outerInstance, "prop");
        String desc = ref.getDescription();
        assertEquals("com.fasterxml.jackson.databind.JsonMappingExceptionGeminiTest$OuterClass[\"prop\"]", desc);
    }

    /*
     * ---------------------------------------------------------------------------------------------
     * Partition A: Core Functional Logic & State Transitions
     * ---------------------------------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    @SuppressWarnings("deprecation")
    public void testDeprecatedConstructors() {
        JsonLocation loc = new JsonLocation("src", 100L, 1, 10);
        Throwable cause = new RuntimeException("cause");

        JsonMappingException ex1 = new JsonMappingException("msg1");
        assertEquals("msg1", ex1.getMessage());
        assertNull(ex1.getCause());

        JsonMappingException ex2 = new JsonMappingException("msg2", cause);
        assertEquals("msg2", ex2.getMessage());
        assertSame(cause, ex2.getCause());

        JsonMappingException ex3 = new JsonMappingException("msg3", loc);
        assertEquals("msg3", ex3.getMessage());
        assertEquals(loc, ex3.getLocation());

        JsonMappingException ex4 = new JsonMappingException("msg4", loc, cause);
        assertEquals("msg4", ex4.getMessage());
        assertEquals(loc, ex4.getLocation());
        assertSame(cause, ex4.getCause());
    }

    @Test(timeout = 4000)
    public void testCloseableProcessorConstructorsAndFactories() throws IOException {
        JsonFactory jf = new JsonFactory();
        JsonParser parser = jf.createParser("{\"k\":\"v\"}");
        parser.nextToken();

        JsonMappingException ex1 = new JsonMappingException(parser, "parse error");
        assertSame(parser, ex1.getProcessor());
        assertNotNull(ex1.getLocation());
        assertEquals(parser.getTokenLocation(), ex1.getLocation());

        Throwable cause = new IOException("io problem");
        JsonMappingException ex2 = new JsonMappingException(parser, "parse error 2", cause);
        assertSame(parser, ex2.getProcessor());
        assertSame(cause, ex2.getCause());
        assertEquals(parser.getTokenLocation(), ex2.getLocation());

        JsonLocation loc = new JsonLocation("customSrc", 10L, 2, 5);
        JsonMappingException ex3 = new JsonMappingException(parser, "parse error 3", loc);
        assertSame(parser, ex3.getProcessor());
        assertEquals(loc, ex3.getLocation());

        // Factories for JsonParser
        JsonMappingException exFromP1 = JsonMappingException.from(parser, "fromP1");
        assertSame(parser, exFromP1.getProcessor());
        assertEquals("fromP1", exFromP1.getMessage());

        JsonMappingException exFromP2 = JsonMappingException.from(parser, "fromP2", cause);
        assertSame(parser, exFromP2.getProcessor());
        assertSame(cause, exFromP2.getCause());

        // Factories for JsonGenerator
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jf.createGenerator(sw);
        JsonMappingException exFromG1 = JsonMappingException.from(gen, "fromG1");
        assertSame(gen, exFromG1.getProcessor());
        assertEquals("fromG1", exFromG1.getMessage());

        JsonMappingException exFromG2 = JsonMappingException.from(gen, "fromG2", cause);
        assertSame(gen, exFromG2.getProcessor());
        assertSame(cause, exFromG2.getCause());

        parser.close();
        gen.close();
    }

    @Test(timeout = 4000)
    public void testContextFactoryMethods() {
        DefaultSerializerProvider.Impl sp = new DefaultSerializerProvider.Impl();
        Throwable cause = new RuntimeException("ctx problem");

        JsonMappingException exSp1 = JsonMappingException.from(sp, "spMsg");
        assertEquals("spMsg", exSp1.getMessage());
        assertNull(exSp1.getProcessor());

        JsonMappingException exSp2 = JsonMappingException.from(sp, "spMsg2", cause);
        assertEquals("spMsg2", exSp2.getMessage());
        assertSame(cause, exSp2.getCause());

        DefaultDeserializationContext.Impl dc = new DefaultDeserializationContext.Impl(
                new BeanDeserializerFactory(new DeserializerFactoryConfig()));

        JsonMappingException exDc1 = JsonMappingException.from(dc, "dcMsg");
        assertEquals("dcMsg", exDc1.getMessage());
        assertNull(exDc1.getProcessor());

        JsonMappingException exDc2 = JsonMappingException.from(dc, "dcMsg2", cause);
        assertEquals("dcMsg2", exDc2.getMessage());
        assertSame(cause, exDc2.getCause());
    }

    @Test(timeout = 4000)
    public void testFromUnexpectedIOE() {
        IOException src = new IOException("Disk failure");
        JsonMappingException ex = JsonMappingException.fromUnexpectedIOE(src);
        assertNotNull(ex);
        assertTrue(ex.getMessage().contains("Unexpected IOException (of type java.io.IOException): Disk failure"));
    }

    @Test(timeout = 4000)
    public void testPathAccessorsAndFormatting() {
        JsonMappingException ex = new JsonMappingException("Base message");
        assertTrue(ex.getPath().isEmpty());
        assertEquals("", ex.getPathReference());
        assertEquals("Base message", ex.getMessage());
        assertEquals("Base message", ex.getLocalizedMessage());
        assertEquals(JsonMappingException.class.getName() + ": Base message", ex.toString());

        ex.prependPath("EntityC", "propC");
        ex.prependPath("EntityB", 2);
        ex.prependPath(String.class, "propA");

        List<JsonMappingException.Reference> path = ex.getPath();
        assertEquals(3, path.size());
        assertEquals("propA", path.get(0).getFieldName());
        assertEquals(2, path.get(1).getIndex());
        assertEquals("propC", path.get(2).getFieldName());

        String pathRef = ex.getPathReference();
        assertEquals("java.lang.String[\"propA\"]->java.lang.String[2]->java.lang.String[\"propC\"]", pathRef);

        StringBuilder sb = new StringBuilder("Prefix: ");
        ex.getPathReference(sb);
        assertTrue(sb.toString().startsWith("Prefix: java.lang.String[\"propA\"]"));

        String fullMsg = ex.getMessage();
        assertTrue(fullMsg.startsWith("Base message (through reference chain: "));
        assertTrue(fullMsg.endsWith(")"));
        assertEquals(fullMsg, ex.getLocalizedMessage());
        assertEquals(JsonMappingException.class.getName() + ": " + fullMsg, ex.toString());
    }

    @Test(timeout = 4000)
    public void testReferenceGettersAndSetters() {
        JsonMappingException.Reference ref = new JsonMappingException.Reference();
        assertNull(ref.getFrom());
        assertNull(ref.getFieldName());
        assertEquals(-1, ref.getIndex());
        assertEquals("UNKNOWN[?]", ref.getDescription());
        assertEquals("UNKNOWN[?]", ref.toString());

        ref.setFieldName("fieldX");
        assertEquals("fieldX", ref.getFieldName());

        ref.setIndex(7);
        assertEquals(7, ref.getIndex());

        ref.setDescription("OverriddenDescription");
        assertEquals("OverriddenDescription", ref.getDescription());
        assertEquals("OverriddenDescription", ref.toString());
    }

    /*
     * ---------------------------------------------------------------------------------------------
     * Partition B: Boundary Value Analysis (BVA) & Extremes
     * ---------------------------------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testMaxRefsToListBoundary() {
        JsonMappingException ex = new JsonMappingException("overflow test");
        for (int i = 0; i < 1005; i++) {
            ex.prependPath("Container", i);
        }
        assertEquals(JsonMappingException.MAX_REFS_TO_LIST, ex.getPath().size());
        // Verify head contains index 999 (the last allowed prepended reference)
        assertEquals(999, ex.getPath().get(0).getIndex());
    }

    @Test(timeout = 4000)
    public void testReferenceWithNullFrom() {
        JsonMappingException.Reference refField = new JsonMappingException.Reference(null, "f");
        assertEquals("UNKNOWN[\"f\"]", refField.getDescription());

        JsonMappingException.Reference refIndex = new JsonMappingException.Reference(null, 0);
        assertEquals("UNKNOWN[0]", refIndex.getDescription());

        JsonMappingException.Reference refUnknown = new JsonMappingException.Reference(null);
        assertEquals("UNKNOWN[?]", refUnknown.getDescription());
    }

    @Test(timeout = 4000)
    public void testReferenceIndexBoundaries() {
        JsonMappingException.Reference refNeg = new JsonMappingException.Reference("Source", -1);
        assertEquals("java.lang.String[?]", refNeg.getDescription());

        JsonMappingException.Reference refZero = new JsonMappingException.Reference("Source", 0);
        assertEquals("java.lang.String[0]", refZero.getDescription());

        JsonMappingException.Reference refPos = new JsonMappingException.Reference("Source", Integer.MAX_VALUE);
        assertEquals("java.lang.String[" + Integer.MAX_VALUE + "]", refPos.getDescription());
    }

    @Test(timeout = 4000)
    public void testNullOrEmptyMessageBuild() {
        JsonMappingException exNull = new JsonMappingException((String) null);
        assertNull(exNull.getMessage());
        exNull.prependPath("Root", "field");
        assertEquals(" (through reference chain: java.lang.String[\"field\"])", exNull.getMessage());

        JsonMappingException exEmpty = new JsonMappingException("");
        assertEquals("", exEmpty.getMessage());
        exEmpty.prependPath("Root", 1);
        assertEquals(" (through reference chain: java.lang.String[1])", exEmpty.getMessage());
    }

    /*
     * ---------------------------------------------------------------------------------------------
     * Partition D: Exception & Defensive Guard Paths
     * ---------------------------------------------------------------------------------------------
     */

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testReferenceNullFieldNameThrowsNPE() {
        new JsonMappingException.Reference("Source", (String) null);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testGetPathListIsUnmodifiable() {
        JsonMappingException ex = new JsonMappingException("msg");
        ex.prependPath("item", 0);
        List<JsonMappingException.Reference> path = ex.getPath();
        path.add(new JsonMappingException.Reference("illegal", 1));
    }

    @Test(timeout = 4000)
    public void testWrapWithPathWithExistingJsonMappingException() {
        JsonMappingException original = new JsonMappingException("original error");
        JsonMappingException wrapped1 = JsonMappingException.wrapWithPath(original, "entity", "field1");
        assertSame(original, wrapped1);

        JsonMappingException.Reference ref2 = new JsonMappingException.Reference("entity", 3);
        JsonMappingException wrapped2 = JsonMappingException.wrapWithPath(original, ref2);
        assertSame(original, wrapped2);

        assertEquals(2, original.getPath().size());
        assertEquals(3, original.getPath().get(0).getIndex());
        assertEquals("field1", original.getPath().get(1).getFieldName());
    }

    @Test(timeout = 4000)
    public void testWrapWithPathWithNonJsonMappingException() {
        RuntimeException exWithMessage = new RuntimeException("runtime fault");
        JsonMappingException wrapped1 = JsonMappingException.wrapWithPath(exWithMessage, "source", "attr");
        assertSame(exWithMessage, wrapped1.getCause());
        assertTrue(wrapped1.getMessage().contains("runtime fault"));
        assertEquals(1, wrapped1.getPath().size());

        RuntimeException exWithNullMsg = new RuntimeException((String) null);
        JsonMappingException wrapped2 = JsonMappingException.wrapWithPath(exWithNullMsg, "source", 0);
        assertTrue(wrapped2.getMessage().contains("(was java.lang.RuntimeException)"));

        RuntimeException exWithEmptyMsg = new RuntimeException("");
        JsonMappingException wrapped3 = JsonMappingException.wrapWithPath(exWithEmptyMsg, "source", 0);
        assertTrue(wrapped3.getMessage().contains("(was java.lang.RuntimeException)"));
    }

    @Test(timeout = 4000)
    public void testWrapWithPathWithJsonProcessingExceptionProcessor() throws IOException {
        JsonFactory jf = new JsonFactory();
        final JsonParser parser = jf.createParser("{}");

        JsonProcessingException jpeCloseable = new JsonProcessingException("proc error", null, null) {
            private static final long serialVersionUID = 1L;
            @Override
            public Object getProcessor() {
                return parser;
            }
        };

        JsonMappingException wrappedCloseable = JsonMappingException.wrapWithPath(jpeCloseable, "item", "val");
        assertSame(parser, wrappedCloseable.getProcessor());

        JsonProcessingException jpeNonCloseable = new JsonProcessingException("proc error 2", null, null) {
            private static final long serialVersionUID = 1L;
            @Override
            public Object getProcessor() {
                return "NotACloseable";
            }
        };

        JsonMappingException wrappedNonCloseable = JsonMappingException.wrapWithPath(jpeNonCloseable, "item", "val");
        assertNull(wrappedNonCloseable.getProcessor());

        parser.close();
    }

    /*
     * ---------------------------------------------------------------------------------------------
     * Partition E: Object Lifecycle & Contract Integrity (Serialization)
     * ---------------------------------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testReferenceSerializationAndWriteReplace() throws Exception {
        JsonMappingException.Reference ref = new JsonMappingException.Reference("sourceObj", "property");
        assertSame(ref, ref.writeReplace());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(ref);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        JsonMappingException.Reference deserializedRef = (JsonMappingException.Reference) ois.readObject();
        ois.close();

        assertNull(deserializedRef.getFrom()); // Transient field dropped
        assertEquals("property", deserializedRef.getFieldName());
        assertEquals(-1, deserializedRef.getIndex());
        assertEquals("java.lang.String[\"property\"]", deserializedRef.getDescription());
    }

    @Test(timeout = 4000)
    public void testJsonMappingExceptionSerialization() throws Exception {
        JsonMappingException ex = new JsonMappingException("serialization test");
        ex.prependPath("Host", "leaf");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(ex);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        JsonMappingException deserializedEx = (JsonMappingException) ois.readObject();
        ois.close();

        assertNull(deserializedEx.getProcessor());
        assertEquals(1, deserializedEx.getPath().size());
        assertTrue(deserializedEx.getMessage().contains("serialization test"));
        assertTrue(deserializedEx.getMessage().contains("Host[\"leaf\"]"));
    }
}