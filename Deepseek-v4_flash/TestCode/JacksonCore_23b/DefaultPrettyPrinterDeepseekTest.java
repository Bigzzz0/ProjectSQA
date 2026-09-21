package com.fasterxml.jackson.core.util;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Test;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.SerializedString;

public class DefaultPrettyPrinterDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     *
     * Partitions targeted:
     * A) Core functional logic: constructors, copy constructors, factory methods
     *    (withRootSeparator, withArrayIndenter, withObjectIndenter, withSpaces...)
     * B) Boundary conditions: null arguments for indenters/separators, empty state,
     *    zero vs. positive entry counts in writeEndObject/writeEndArray
     * C) Defect-targeted: createInstance() must preserve subclass type; if it does
     *    not, the test fails with "Should not pass" (as required by ground truth)
     * D) Exception paths: null root separator, null indenter usage, invalid state
     * E) Contract integrity: Indenter implementations (NopIndenter, FixedSpaceIndenter)
     *
     * Known defect: createInstance() returns a base DefaultPrettyPrinter instead
     * of an instance of the calling subclass, so subclass state is lost.
     * This test asserts that such behavior is invalid and must be rejected.
     */

    // ----------------------------------------------------------------------
    // Helper: minimal JsonGenerator stub for capturing raw output
    // ----------------------------------------------------------------------

    private static class TestJsonGenerator extends JsonGenerator {
        final StringBuilder out = new StringBuilder();

        @Override
        public void writeRaw(String text) throws IOException { out.append(text); }

        @Override
        public void writeRaw(String text, int offset, int len) throws IOException {
            out.append(text, offset, offset + len);
        }

        @Override
        public void writeRaw(char[] text, int offset, int len) throws IOException {
            out.append(text, offset, offset + len);
        }

        @Override
        public void writeRaw(char c) throws IOException { out.append(c); }

        @Override
        public void writeRaw(SerializableString raw) throws IOException {
            out.append(raw.getValue());
        }

        // ---- Unused abstract methods (no-op or minimal) ----
        @Override public void flush() throws IOException {}
        @Override public void close() throws IOException {}
        @Override public boolean isClosed() { return false; }
        @Override public ObjectCodec getCodec() { return null; }
        @Override public void setCodec(ObjectCodec oc) {}
        @Override public JsonStreamContext getOutputContext() { return null; }
        @Override public JsonGenerator enable(Feature f) { return this; }
        @Override public JsonGenerator disable(Feature f) { return this; }
        @Override public boolean isEnabled(Feature f) { return false; }
        @Override public JsonGenerator setPrettyPrinter(PrettyPrinter pp) { return this; }
        @Override public PrettyPrinter getPrettyPrinter() { return null; }
        @Override public JsonGenerator useDefaultPrettyPrinter() { return this; }
        @Override public void writeStartArray() throws IOException {}
        @Override public void writeEndArray() throws IOException {}
        @Override public void writeStartObject() throws IOException {}
        @Override public void writeEndObject() throws IOException {}
        @Override public void writeFieldName(String name) throws IOException {}
        @Override public void writeFieldName(SerializableString name) throws IOException {}
        @Override public void writeString(String text) throws IOException {}
        @Override public void writeString(char[] text, int offset, int len) throws IOException {}
        @Override public void writeString(SerializableString text) throws IOException {}
        @Override public void writeRawUTF8String(byte[] text, int offset, int len) throws IOException {}
        @Override public void writeUTF8String(byte[] text, int offset, int len) throws IOException {}
        @Override public void writeBinary(Base64Variant b64variant, byte[] data, int offset, int len) throws IOException {}
        @Override public int writeBinary(Base64Variant b64variant, InputStream data, int dataLength) throws IOException { return 0; }
        @Override public void writeNumber(int v) throws IOException {}
        @Override public void writeNumber(long v) throws IOException {}
        @Override public void writeNumber(BigInteger v) throws IOException {}
        @Override public void writeNumber(double v) throws IOException {}
        @Override public void writeNumber(float v) throws IOException {}
        @Override public void writeNumber(BigDecimal v) throws IOException {}
        @Override public void writeNumber(String encodedValue) throws IOException {}
        @Override public void writeBoolean(boolean state) throws IOException {}
        @Override public void writeNull() throws IOException {}
        @Override public void writeObject(Object pojo) throws IOException {}
        @Override public void writeTree(TreeNode rootNode) throws IOException {}
        @Override public JsonParser getOutputTarget() { return null; }
    }

    // ----------------------------------------------------------------------
    // Helper: invalid subclass for defect detection
    // ----------------------------------------------------------------------

    static class InvalidSubClass extends DefaultPrettyPrinter {
        public int marker = 42;
        public InvalidSubClass() { super(); }
        public InvalidSubClass(DefaultPrettyPrinter base) { super(base); }
    }

    // ======================================================================
    // 1. Defect targeted: createInstance must preserve subclass type
    // ======================================================================

    @Test(timeout = 4000)
    public void testInvalidSubClass() {
        InvalidSubClass sub = new InvalidSubClass();
        DefaultPrettyPrinter copy = sub.createInstance();
        // If the copy is not of the same subclass, the behavior is invalid.
        // The original bug returned a base DefaultPrettyPrinter, so this fail
        // triggers the fault.
        if (!(copy instanceof InvalidSubClass)) {
            fail("Should not pass");
        }
        assertEquals(42, ((InvalidSubClass) copy).marker);
    }

    // ======================================================================
    // 2. Constructors and copy semantics
    // ======================================================================

    @Test(timeout = 4000)
    public void testDefaultConstructor() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        assertNotNull(pp._rootSeparator);
        assertEquals(" ", pp._rootSeparator.getValue());
        assertTrue(pp._spacesInObjectEntries);
        assertNotNull(pp._arrayIndenter);
        assertNotNull(pp._objectIndenter);
        assertNotNull(pp._separators);
        assertNotNull(pp._objectFieldValueSeparatorWithSpaces);
    }

    @Test(timeout = 4000)
    public void testStringConstructorWithNull() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter((String) null);
        assertNull(pp._rootSeparator);
    }

    @Test(timeout = 4000)
    public void testStringConstructorNonNull() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter(" -- ");
        assertNotNull(pp._rootSeparator);
        assertEquals(" -- ", pp._rootSeparator.getValue());
    }

    @Test(timeout = 4000)
    public void testSerializableStringConstructor() {
        SerializableString ss = new SerializedString("x");
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter(ss);
        assertSame(ss, pp._rootSeparator);
    }

    @Test(timeout = 4000)
    public void testCopyConstructorWithRootSeparator() {
        DefaultPrettyPrinter base = new DefaultPrettyPrinter("abc");
        DefaultPrettyPrinter copy = new DefaultPrettyPrinter(base);
        assertEquals("abc", copy._rootSeparator.getValue());
        assertSame(base._arrayIndenter, copy._arrayIndenter);
        assertSame(base._objectIndenter, copy._objectIndenter);
        assertEquals(base._spacesInObjectEntries, copy._spacesInObjectEntries);
        assertEquals(base._nesting, copy._nesting);
        assertSame(base._separators, copy._separators);
        assertEquals(base._objectFieldValueSeparatorWithSpaces, copy._objectFieldValueSeparatorWithSpaces);
    }

    @Test(timeout = 4000)
    public void testCopyConstructorNullRootSeparator() {
        DefaultPrettyPrinter base = new DefaultPrettyPrinter((String) null);
        DefaultPrettyPrinter copy = new DefaultPrettyPrinter(base);
        assertNull(copy._rootSeparator);
    }

    // ======================================================================
    // 3. Mutant factory methods
    // ======================================================================

    @Test(timeout = 4000)
    public void testWithRootSeparatorSame() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter("a");
        assertSame(pp, pp.withRootSeparator(pp._rootSeparator));
    }

    @Test(timeout = 4000)
    public void testWithRootSeparatorDifferent() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter("a");
        DefaultPrettyPrinter res = pp.withRootSeparator(new SerializedString("b"));
        assertNotSame(pp, res);
        assertEquals("b", res._rootSeparator.getValue());
    }

    @Test(timeout = 4000)
    public void testWithRootSeparatorNull() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter("a");
        DefaultPrettyPrinter res = pp.withRootSeparator((String) null);
        assertNull(res._rootSeparator);
    }

    @Test(timeout = 4000)
    public void testWithRootSeparatorStringSame() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter("a");
        assertSame(pp, pp.withRootSeparator("a"));
    }

    @Test(timeout = 4000)
    public void testIndentArraysWithNull() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        pp.indentArraysWith(null);
        assertSame(DefaultPrettyPrinter.NopIndenter.instance, pp._arrayIndenter);
    }

    @Test(timeout = 4000)
    public void testIndentArraysWithNonNull() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        DefaultPrettyPrinter.FixedSpaceIndenter ind = new DefaultPrettyPrinter.FixedSpaceIndenter();
        pp.indentArraysWith(ind);
        assertSame(ind, pp._arrayIndenter);
    }

    @Test(timeout = 4000)
    public void testIndentObjectsWithNull() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        pp.indentObjectsWith(null);
        assertSame(DefaultPrettyPrinter.NopIndenter.instance, pp._objectIndenter);
    }

    @Test(timeout = 4000)
    public void testIndentObjectsWithNonNull() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        DefaultPrettyPrinter.DefaultIndenter ind = DefaultPrettyPrinter.DefaultIndenter.SYSTEM_LINEFEED_INSTANCE;
        pp.indentObjectsWith(ind);
        assertSame(ind, pp._objectIndenter);
    }

    @Test(timeout = 4000)
    public void testWithArrayIndenterNull() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        DefaultPrettyPrinter res = pp.withArrayIndenter(null);
        assertSame(DefaultPrettyPrinter.NopIndenter.instance, res._arrayIndenter);
    }

    @Test(timeout = 4000)
    public void testWithArrayIndenterSame() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        assertSame(pp, pp.withArrayIndenter(pp._arrayIndenter));
    }

    @Test(timeout = 4000)
    public void testWithArrayIndenterDifferent() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        DefaultPrettyPrinter.FixedSpaceIndenter ind = new DefaultPrettyPrinter.FixedSpaceIndenter();
        DefaultPrettyPrinter res = pp.withArrayIndenter(ind);
        assertNotSame(pp, res);
        assertSame(ind, res._arrayIndenter);
    }

    @Test(timeout = 4000)
    public void testWithObjectIndenterNull() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        DefaultPrettyPrinter res = pp.withObjectIndenter(null);
        assertSame(DefaultPrettyPrinter.NopIndenter.instance, res._objectIndenter);
    }

    @Test(timeout = 4000)
    public void testWithObjectIndenterSame() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        assertSame(pp, pp.withObjectIndenter(pp._objectIndenter));
    }

    @Test(timeout = 4000)
    public void testWithObjectIndenterDifferent() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        DefaultPrettyPrinter.DefaultIndenter ind = new DefaultPrettyPrinter.DefaultIndenter();
        DefaultPrettyPrinter res = pp.withObjectIndenter(ind);
        assertNotSame(pp, res);
        assertSame(ind, res._objectIndenter);
    }

    @Test(timeout = 4000)
    public void testWithSpacesInObjectEntries() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        DefaultPrettyPrinter res = pp.withSpacesInObjectEntries();
        assertSame(pp, res); // already true
        DefaultPrettyPrinter pp2 = pp.withoutSpacesInObjectEntries();
        assertFalse(pp2._spacesInObjectEntries);
        DefaultPrettyPrinter res2 = pp2.withSpacesInObjectEntries();
        assertTrue(res2._spacesInObjectEntries);
    }

    @Test(timeout = 4000)
    public void testWithoutSpacesInObjectEntries() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        DefaultPrettyPrinter res = pp.withoutSpacesInObjectEntries();
        assertFalse(res._spacesInObjectEntries);
        assertNotSame(pp, res);
    }

    @Test(timeout = 4000)
    public void testWithSeparators() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        Separators sep = Separators.createDefaultInstance();
        DefaultPrettyPrinter res = pp.withSeparators(sep);
        assertSame(pp, res); // returns this
        assertEquals(" " + sep.getObjectFieldValueSeparator() + " ", pp._objectFieldValueSeparatorWithSpaces);
    }

    // ======================================================================
    // 4. createInstance
    // ======================================================================

    @Test(timeout = 4000)
    public void testCreateInstanceBase() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter("x");
        DefaultPrettyPrinter copy = pp.createInstance();
        assertNotSame(pp, copy);
        assertEquals("x", copy._rootSeparator.getValue());
        assertSame(pp._arrayIndenter, copy._arrayIndenter);
        assertSame(pp._objectIndenter, copy._objectIndenter);
    }

    // ======================================================================
    // 5. Output generation methods (using TestJsonGenerator)
    // ======================================================================

    @Test(timeout = 4000)
    public void testWriteRootValueSeparatorNull() throws IOException {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter((String) null);
        TestJsonGenerator g = new TestJsonGenerator();
        pp.writeRootValueSeparator(g);
        assertEquals("", g.out.toString());
    }

    @Test(timeout = 4000)
    public void testWriteRootValueSeparatorNonNull() throws IOException {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter("---");
        TestJsonGenerator g = new TestJsonGenerator();
        pp.writeRootValueSeparator(g);
        assertEquals("---", g.out.toString());
    }

    @Test(timeout = 4000)
    public void testWriteStartObjectWithInlineIndenter() throws IOException {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        pp._objectIndenter = DefaultPrettyPrinter.NopIndenter.instance;
        TestJsonGenerator g = new TestJsonGenerator();
        pp.writeStartObject(g);
        assertEquals("{", g.out.toString());
        assertEquals(0, pp._nesting);
    }

    @Test(timeout = 4000)
    public void testWriteStartObjectWithNonInlineIndenter() throws IOException {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        TestJsonGenerator g = new TestJsonGenerator();
        pp.writeStartObject(g);
        assertEquals("{", g.out.toString());
        assertEquals(1, pp._nesting);
    }

    @Test(timeout = 4000)
    public void testBeforeObjectEntries() throws IOException {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        pp._nesting = 2;
        TestJsonGenerator g = new TestJsonGenerator();
        pp.beforeObjectEntries(g);
        assertTrue(g.out.length() > 0); // indenter writes something
    }

    @Test(timeout = 4000)
    public void testWriteObjectFieldValueSeparatorWithSpaces() throws IOException {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        TestJsonGenerator g = new TestJsonGenerator();
        pp.writeObjectFieldValueSeparator(g);
        assertEquals(" : ", g.out.toString());
    }

    @Test(timeout = 4000)
    public void testWriteObjectFieldValueSeparatorWithoutSpaces() throws IOException {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        pp._spacesInObjectEntries = false;
        TestJsonGenerator g = new TestJsonGenerator();
        pp.writeObjectFieldValueSeparator(g);
        assertEquals(":", g.out.toString());
    }

    @Test(timeout = 4000)
    public void testWriteObjectEntrySeparator() throws IOException {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        pp._nesting = 1;
        TestJsonGenerator g = new TestJsonGenerator();
        pp.writeObjectEntrySeparator(g);
        // Should output separator and indentation
        assertTrue(g.out.length() > 0);
    }

    @Test(timeout = 4000)
    public void testWriteEndObjectWithEntries() throws IOException {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        pp._nesting = 1;
        TestJsonGenerator g = new TestJsonGenerator();
        pp.writeEndObject(g, 3);
        assertEquals(0, pp._nesting);
        assertTrue(g.out.toString().endsWith("}"));
    }

    @Test(timeout = 4000)
    public void testWriteEndObjectNoEntries() throws IOException {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        pp._nesting = 1;
        TestJsonGenerator g = new TestJsonGenerator();
        pp.writeEndObject(g, 0);
        assertEquals(0, pp._nesting);
        assertEquals(" }", g.out.toString());
    }

    @Test(timeout = 4000)
    public void testWriteStartArray() throws IOException {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        TestJsonGenerator g = new TestJsonGenerator();
        pp.writeStartArray(g);
        assertEquals("[", g.out.toString());
        assertEquals(1, pp._nesting);
    }

    @Test(timeout = 4000)
    public void testBeforeArrayValues() throws IOException {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        pp._nesting = 2;
        TestJsonGenerator g = new TestJsonGenerator();
        pp.beforeArrayValues(g);
        assertTrue(g.out.length() > 0);
    }

    @Test(timeout = 4000)
    public void testWriteArrayValueSeparator() throws IOException {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        pp._nesting = 1;
        TestJsonGenerator g = new TestJsonGenerator();
        pp.writeArrayValueSeparator(g);
        assertTrue(g.out.length() > 0);
    }

    @Test(timeout = 4000)
    public void testWriteEndArrayWithValues() throws IOException {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        pp._nesting = 1;
        TestJsonGenerator g = new TestJsonGenerator();
        pp.writeEndArray(g, 2);
        assertEquals(0, pp._nesting);
        assertTrue(g.out.toString().endsWith("]"));
    }

    @Test(timeout = 4000)
    public void testWriteEndArrayNoValues() throws IOException {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        pp._nesting = 1;
        TestJsonGenerator g = new TestJsonGenerator();
        pp.writeEndArray(g, 0);
        assertEquals(0, pp._nesting);
        assertEquals(" ]", g.out.toString());
    }

    // ======================================================================
    // 6. Indenter implementations
    // ======================================================================

    @Test(timeout = 4000)
    public void testNopIndenter() throws IOException {
        DefaultPrettyPrinter.NopIndenter ind = DefaultPrettyPrinter.NopIndenter.instance;
        assertTrue(ind.isInline());
        TestJsonGenerator g = new TestJsonGenerator();
        ind.writeIndentation(g, 3);
        assertEquals("", g.out.toString());
    }

    @Test(timeout = 4000)
    public void testFixedSpaceIndenter() throws IOException {
        DefaultPrettyPrinter.FixedSpaceIndenter ind = DefaultPrettyPrinter.FixedSpaceIndenter.instance;
        assertTrue(ind.isInline());
        TestJsonGenerator g = new TestJsonGenerator();
        ind.writeIndentation(g, 0);
        assertEquals(" ", g.out.toString());
        g.out.setLength(0);
        ind.writeIndentation(g, 5);
        assertEquals(" ", g.out.toString()); // always one space
    }

    // ======================================================================
    // 7. Full integration-like test combining multiple methods
    // ======================================================================

    @Test(timeout = 4000)
    public void testObjectOutputSequence() throws IOException {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        DefaultPrettyPrinter.DefaultIndenter ind = new DefaultPrettyPrinter.DefaultIndenter();
        pp.indentObjectsWith(ind);
        pp.writeStartObject(null); // cannot use null for generator, but we'll use a real generator
        TestJsonGenerator g = new TestJsonGenerator();
        pp.writeStartObject(g);
        pp.beforeObjectEntries(g);
        pp.writeObjectFieldValueSeparator(g);
        pp.writeObjectEntrySeparator(g);
        pp.writeEndObject(g, 1);
        // Check that output is non-trivial
        assertTrue(g.out.length() > 0);
    }
}