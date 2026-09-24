/* [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------------
 * Target Class: com.fasterxml.jackson.core.util.DefaultPrettyPrinter
 *
 * 1. Defect-Targeted Branch Zone (Defects4J ground truth: TestDefaultPrettyPrinter::testInvalidSubClass):
 *    - Method: createInstance()
 *    - Branch: Subclass does not override createInstance() -> must throw IllegalStateException("Sub-class ... does not override createInstance()").
 *    - Defect: Base DefaultPrettyPrinter unconditionally instantiates new DefaultPrettyPrinter(this),
 *              silently slicing/dropping the custom subclass type instead of enforcing the contract.
 *
 * 2. Equivalence Partitioning & Decision/Condition Coverage:
 *    - Constructors:
 *        * DefaultPrettyPrinter() -> default root separator (' ') and DEFAULT_SEPARATORS.
 *        * DefaultPrettyPrinter(String) -> null string branch vs non-null SerializedString branch.
 *        * DefaultPrettyPrinter(SerializableString) -> direct reference, null and non-null.
 *        * DefaultPrettyPrinter(DefaultPrettyPrinter) -> copy constructor.
 *        * DefaultPrettyPrinter(DefaultPrettyPrinter, SerializableString) -> complete field propagation.
 *    - Mutant Factories & Idempotence:
 *        * withRootSeparator(SerializableString): same reference (this), equal content (this), different content (new instance).
 *        * withRootSeparator(String): null string vs populated string.
 *        * indentArraysWith(Indenter): null branch (NopIndenter.instance) vs non-null.
 *        * indentObjectsWith(Indenter): null branch (NopIndenter.instance) vs non-null.
 *        * withArrayIndenter(Indenter): null -> Nop; same instance -> this; changed instance -> new instance.
 *        * withObjectIndenter(Indenter): null -> Nop; same instance -> this; changed instance -> new instance.
 *        * withSpacesInObjectEntries() / withoutSpacesInObjectEntries():
 *          same state -> this; state toggle -> new instance.
 *        * withSeparators(Separators): custom separators and field value separator formatting (" " + sep + " ").
 *    - PrettyPrinter JSON Generation Lifecycle:
 *        * writeRootValueSeparator: _rootSeparator != null vs _rootSeparator == null.
 *        * writeStartObject / writeEndObject:
 *            - _objectIndenter.isInline() == false -> increments/decrements _nesting.
 *            - _objectIndenter.isInline() == true  -> leaves _nesting unchanged.
 *            - nrOfEntries > 0 -> writes indenter indentation.
 *            - nrOfEntries <= 0 -> writes single space ' '.
 *        * writeObjectFieldValueSeparator:
 *            - _spacesInObjectEntries == true  -> writes decorated separator (" : ").
 *            - _spacesInObjectEntries == false -> writes raw separator (":").
 *        * writeObjectEntrySeparator: writes entry separator and indentation.
 *        * writeStartArray / writeEndArray:
 *            - _arrayIndenter.isInline() == false -> increments/decrements _nesting.
 *            - _arrayIndenter.isInline() == true  -> leaves _nesting unchanged.
 *            - nrOfValues > 0 -> writes indenter indentation.
 *            - nrOfValues <= 0 -> writes single space ' '.
 *        * writeArrayValueSeparator: writes array value separator and indenter indentation.
 *        * beforeObjectEntries / beforeArrayValues: delegates to respective indenters with _nesting.
 *    - Indenter Implementations:
 *        * NopIndenter: isInline() is true, writeIndentation is a no-op.
 *        * FixedSpaceIndenter: isInline() is true, writeIndentation outputs ' '.
 *    - Serialization & Transient State:
 *        * Java Serialization roundtrip: preserves configuration; transient _nesting reset.
 * ---------------------------------------------------------------------------------------------------------
 */
package com.fasterxml.jackson.core.util;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.SerializedString;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StringWriter;

import static org.junit.Assert.*;

public class DefaultPrettyPrinterGeminiTest {

    private final JsonFactory JSON_F = new JsonFactory();

    // =========================================================================
    // Helper Test Classes & Doubles
    // =========================================================================

    /**
     * Non-overriding subclass specifically targeting the Defects4J regression defect.
     */
    static class InvalidSubPrettyPrinter extends DefaultPrettyPrinter {
        private static final long serialVersionUID = 1L;

        public InvalidSubPrettyPrinter() {
            super();
        }

        public InvalidSubPrettyPrinter(InvalidSubPrettyPrinter base) {
            super(base);
        }
    }

    /**
     * Subclass that correctly overrides createInstance().
     */
    static class ValidSubPrettyPrinter extends DefaultPrettyPrinter {
        private static final long serialVersionUID = 1L;

        public ValidSubPrettyPrinter() {
            super();
        }

        public ValidSubPrettyPrinter(ValidSubPrettyPrinter base) {
            super(base);
        }

        @Override
        public ValidSubPrettyPrinter createInstance() {
            return new ValidSubPrettyPrinter(this);
        }
    }

    /**
     * Custom indenter capable of simulating both inline and multiline behavior.
     */
    static class MockIndenter implements DefaultPrettyPrinter.Indenter, Serializable {
        private static final long serialVersionUID = 1L;

        private final boolean inline;
        private int calls = 0;
        private int lastLevel = -1;

        public MockIndenter(boolean inline) {
            this.inline = inline;
        }

        @Override
        public void writeIndentation(JsonGenerator g, int level) throws IOException {
            calls++;
            lastLevel = level;
            g.writeRaw("/*indent:" + level + "*/");
        }

        @Override
        public boolean isInline() {
            return inline;
        }

        public int getCalls() {
            return calls;
        }

        public int getLastLevel() {
            return lastLevel;
        }
    }

    private JsonGenerator createGenerator(StringWriter sw) throws IOException {
        return JSON_F.createGenerator(sw);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    @Test(timeout = 4000)
    public void testInvalidSubClass() {
        InvalidSubPrettyPrinter subPrinter = new InvalidSubPrettyPrinter();
        try {
            subPrinter.createInstance();
            fail("Should not pass");
        } catch (IllegalStateException e) {
            String msg = e.getMessage();
            assertNotNull(msg);
            assertTrue("Expected exception message to state that subclass does not override createInstance(), got: " + msg,
                    msg.contains("does not override") || msg.contains("createInstance"));
        }
    }

    @Test(timeout = 4000)
    public void testValidSubClassOverridesCreateInstance() {
        ValidSubPrettyPrinter subPrinter = new ValidSubPrettyPrinter();
        ValidSubPrettyPrinter copy = subPrinter.createInstance();
        assertNotNull(copy);
        assertNotSame(subPrinter, copy);
        assertEquals(ValidSubPrettyPrinter.class, copy.getClass());
    }

    @Test(timeout = 4000)
    public void testCreateInstanceOnBaseClass() {
        DefaultPrettyPrinter base = new DefaultPrettyPrinter();
        DefaultPrettyPrinter copy = base.createInstance();
        assertNotNull(copy);
        assertNotSame(base, copy);
        assertEquals(DefaultPrettyPrinter.class, copy.getClass());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions (Mutators & Fluent API)
    // =========================================================================

    @Test(timeout = 4000)
    public void testWithRootSeparatorSerializableString() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        SerializableString defaultSep = DefaultPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR;

        // Same reference returns same instance
        assertSame(pp, pp.withRootSeparator(defaultSep));

        // Equal content returns same instance
        SerializedString equalSep = new SerializedString(" ");
        assertSame(pp, pp.withRootSeparator(equalSep));

        // Different separator returns new instance
        SerializedString differentSep = new SerializedString("\n");
        DefaultPrettyPrinter pp2 = pp.withRootSeparator(differentSep);
        assertNotSame(pp, pp2);

        // Setting to null returns new instance
        DefaultPrettyPrinter ppNull = pp.withRootSeparator((SerializableString) null);
        assertNotSame(pp, ppNull);

        // Setting null when already null returns same instance
        assertSame(ppNull, ppNull.withRootSeparator((SerializableString) null));

        // Setting from null back to value returns new instance
        DefaultPrettyPrinter ppRestored = ppNull.withRootSeparator(defaultSep);
        assertNotSame(ppNull, ppRestored);
    }

    @Test(timeout = 4000)
    public void testWithRootSeparatorString() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();

        // Same string content as default (" ") returns same instance
        assertSame(pp, pp.withRootSeparator(" "));

        // Changing string returns new instance
        DefaultPrettyPrinter ppCustom = pp.withRootSeparator("\t");
        assertNotSame(pp, ppCustom);

        // Null string returns new instance with null separator
        DefaultPrettyPrinter ppNull = pp.withRootSeparator((String) null);
        assertNotSame(pp, ppNull);
        assertSame(ppNull, ppNull.withRootSeparator((String) null));
    }

    @Test(timeout = 4000)
    public void testWithArrayIndenter() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        DefaultPrettyPrinter.Indenter initial = pp._arrayIndenter;

        // Passing same instance returns this
        assertSame(pp, pp.withArrayIndenter(initial));

        // Passing custom indenter returns new instance
        MockIndenter custom = new MockIndenter(true);
        DefaultPrettyPrinter pp2 = pp.withArrayIndenter(custom);
        assertNotSame(pp, pp2);
        assertSame(custom, pp2._arrayIndenter);

        // Passing null defaults to NopIndenter
        DefaultPrettyPrinter ppNop = pp2.withArrayIndenter(null);
        assertNotSame(pp2, ppNop);
        assertSame(DefaultPrettyPrinter.NopIndenter.instance, ppNop._arrayIndenter);

        // Passing null when already NopIndenter returns this
        assertSame(ppNop, ppNop.withArrayIndenter(null));
        assertSame(ppNop, ppNop.withArrayIndenter(DefaultPrettyPrinter.NopIndenter.instance));
    }

    @Test(timeout = 4000)
    public void testWithObjectIndenter() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        DefaultPrettyPrinter.Indenter initial = pp._objectIndenter;

        // Passing same instance returns this
        assertSame(pp, pp.withObjectIndenter(initial));

        // Passing custom indenter returns new instance
        MockIndenter custom = new MockIndenter(false);
        DefaultPrettyPrinter pp2 = pp.withObjectIndenter(custom);
        assertNotSame(pp, pp2);
        assertSame(custom, pp2._objectIndenter);

        // Passing null defaults to NopIndenter
        DefaultPrettyPrinter ppNop = pp2.withObjectIndenter(null);
        assertNotSame(pp2, ppNop);
        assertSame(DefaultPrettyPrinter.NopIndenter.instance, ppNop._objectIndenter);

        // Passing null when already NopIndenter returns this
        assertSame(ppNop, ppNop.withObjectIndenter(null));
        assertSame(ppNop, ppNop.withObjectIndenter(DefaultPrettyPrinter.NopIndenter.instance));
    }

    @Test(timeout = 4000)
    public void testIndentArraysWithAndIndentObjectsWithDirectMutators() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();

        // indentArraysWith with non-null and null
        MockIndenter arrayIndenter = new MockIndenter(true);
        pp.indentArraysWith(arrayIndenter);
        assertSame(arrayIndenter, pp._arrayIndenter);

        pp.indentArraysWith(null);
        assertSame(DefaultPrettyPrinter.NopIndenter.instance, pp._arrayIndenter);

        // indentObjectsWith with non-null and null
        MockIndenter objectIndenter = new MockIndenter(false);
        pp.indentObjectsWith(objectIndenter);
        assertSame(objectIndenter, pp._objectIndenter);

        pp.indentObjectsWith(null);
        assertSame(DefaultPrettyPrinter.NopIndenter.instance, pp._objectIndenter);
    }

    @Test(timeout = 4000)
    public void testWithSpacesAndWithoutSpacesInObjectEntries() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        assertTrue(pp._spacesInObjectEntries);

        // Already true -> returns this
        assertSame(pp, pp.withSpacesInObjectEntries());

        // Toggle to false -> returns new instance
        DefaultPrettyPrinter ppNoSpaces = pp.withoutSpacesInObjectEntries();
        assertNotSame(pp, ppNoSpaces);
        assertFalse(ppNoSpaces._spacesInObjectEntries);

        // Already false -> returns this
        assertSame(ppNoSpaces, ppNoSpaces.withoutSpacesInObjectEntries());

        // Toggle back to true -> returns new instance
        DefaultPrettyPrinter ppSpacesAgain = ppNoSpaces.withSpacesInObjectEntries();
        assertNotSame(ppNoSpaces, ppSpacesAgain);
        assertTrue(ppSpacesAgain._spacesInObjectEntries);
    }

    @Test(timeout = 4000)
    public void testWithSeparators() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        Separators sep = Separators.createDefaultInstance().withObjectFieldValueSeparator(':');
        DefaultPrettyPrinter result = pp.withSeparators(sep);
        assertSame(pp, result);
        assertEquals(" : ", pp._objectFieldValueSeparatorWithSpaces);
        assertSame(sep, pp._separators);

        Separators customSep = Separators.createDefaultInstance().withObjectFieldValueSeparator('=');
        pp.withSeparators(customSep);
        assertEquals(" = ", pp._objectFieldValueSeparatorWithSpaces);
        assertSame(customSep, pp._separators);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Extremes (Constructors & Nulls)
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorsBoundaryAndCopyIntegrity() {
        // Default constructor
        DefaultPrettyPrinter pp1 = new DefaultPrettyPrinter();
        assertNotNull(pp1._rootSeparator);
        assertEquals(" ", pp1._rootSeparator.getValue());
        assertNotNull(pp1._separators);
        assertEquals(" : ", pp1._objectFieldValueSeparatorWithSpaces);

        // String constructor with null
        DefaultPrettyPrinter ppNullStr = new DefaultPrettyPrinter((String) null);
        assertNull(ppNullStr._rootSeparator);

        // String constructor with empty string
        DefaultPrettyPrinter ppEmptyStr = new DefaultPrettyPrinter("");
        assertNotNull(ppEmptyStr._rootSeparator);
        assertEquals("", ppEmptyStr._rootSeparator.getValue());

        // String constructor with non-empty string
        DefaultPrettyPrinter ppCustomStr = new DefaultPrettyPrinter(";;");
        assertNotNull(ppCustomStr._rootSeparator);
        assertEquals(";;", ppCustomStr._rootSeparator.getValue());

        // SerializableString constructor with null
        DefaultPrettyPrinter ppNullSerializable = new DefaultPrettyPrinter((SerializableString) null);
        assertNull(ppNullSerializable._rootSeparator);

        // Copy constructor from base
        pp1._nesting = 3;
        DefaultPrettyPrinter copy1 = new DefaultPrettyPrinter(pp1);
        assertEquals(3, copy1._nesting);
        assertSame(pp1._arrayIndenter, copy1._arrayIndenter);
        assertSame(pp1._objectIndenter, copy1._objectIndenter);
        assertEquals(pp1._spacesInObjectEntries, copy1._spacesInObjectEntries);
        assertSame(pp1._rootSeparator, copy1._rootSeparator);
        assertSame(pp1._separators, copy1._separators);

        // Copy constructor with base and explicit rootSeparator
        SerializedString customRoot = new SerializedString("|");
        DefaultPrettyPrinter copy2 = new DefaultPrettyPrinter(pp1, customRoot);
        assertEquals(3, copy2._nesting);
        assertSame(customRoot, copy2._rootSeparator);
    }

    // =========================================================================
    // Partition D: Execution Flow & Output Coverage (PrettyPrinter Methods)
    // =========================================================================

    @Test(timeout = 4000)
    public void testWriteRootValueSeparator() throws IOException {
        StringWriter sw = new StringWriter();
        JsonGenerator g = createGenerator(sw);

        // Non-null separator branch
        DefaultPrettyPrinter ppWithSep = new DefaultPrettyPrinter(" # ");
        ppWithSep.writeRootValueSeparator(g);
        g.flush();
        assertEquals(" # ", sw.toString());

        // Null separator branch
        DefaultPrettyPrinter ppNull = new DefaultPrettyPrinter((String) null);
        StringWriter swNull = new StringWriter();
        JsonGenerator gNull = createGenerator(swNull);
        ppNull.writeRootValueSeparator(gNull);
        gNull.flush();
        assertEquals("", swNull.toString());
    }

    @Test(timeout = 4000)
    public void testObjectFormattingLifecycleNonInlineIndenter() throws IOException {
        StringWriter sw = new StringWriter();
        JsonGenerator g = createGenerator(sw);
        MockIndenter objectIndenter = new MockIndenter(false); // non-inline

        DefaultPrettyPrinter pp = new DefaultPrettyPrinter()
                .withObjectIndenter(objectIndenter);

        // Start Object
        assertEquals(0, pp._nesting);
        pp.writeStartObject(g);
        assertEquals(1, pp._nesting);

        // Before Object Entries
        pp.beforeObjectEntries(g);
        assertEquals(1, objectIndenter.getLastLevel());

        // Field Value Separator (with spaces: default " : ")
        pp.writeObjectFieldValueSeparator(g);

        // Field Value Separator (without spaces: raw ":")
        pp.withoutSpacesInObjectEntries().writeObjectFieldValueSeparator(g);

        // Object Entry Separator
        pp.writeObjectEntrySeparator(g);

        // End Object with nrOfEntries > 0
        pp.writeEndObject(g, 1);
        assertEquals(0, pp._nesting);

        // End Object with nrOfEntries == 0 (empty object emits space)
        pp.writeStartObject(g);
        assertEquals(1, pp._nesting);
        pp.writeEndObject(g, 0);
        assertEquals(0, pp._nesting);

        g.flush();
        String out = sw.toString();
        assertTrue(out.startsWith("{"));
        assertTrue(out.contains(" : "));
        assertTrue(out.contains(":"));
        assertTrue(out.endsWith(" }"));
    }

    @Test(timeout = 4000)
    public void testObjectFormattingLifecycleInlineIndenter() throws IOException {
        StringWriter sw = new StringWriter();
        JsonGenerator g = createGenerator(sw);
        MockIndenter inlineIndenter = new MockIndenter(true); // inline

        DefaultPrettyPrinter pp = new DefaultPrettyPrinter()
                .withObjectIndenter(inlineIndenter);

        assertEquals(0, pp._nesting);
        pp.writeStartObject(g);
        // Inline indenter does NOT increment nesting
        assertEquals(0, pp._nesting);

        pp.writeEndObject(g, 1);
        // Inline indenter does NOT decrement nesting
        assertEquals(0, pp._nesting);

        g.flush();
        assertEquals("{/*indent:0*//*indent:0*/}", sw.toString());
    }

    @Test(timeout = 4000)
    public void testArrayFormattingLifecycleNonInlineIndenter() throws IOException {
        StringWriter sw = new StringWriter();
        JsonGenerator g = createGenerator(sw);
        MockIndenter arrayIndenter = new MockIndenter(false); // non-inline

        DefaultPrettyPrinter pp = new DefaultPrettyPrinter()
                .withArrayIndenter(arrayIndenter);

        assertEquals(0, pp._nesting);
        pp.writeStartArray(g);
        assertEquals(1, pp._nesting);

        pp.beforeArrayValues(g);
        assertEquals(1, arrayIndenter.getLastLevel());

        pp.writeArrayValueSeparator(g);

        // End Array with nrOfValues > 0
        pp.writeEndArray(g, 2);
        assertEquals(0, pp._nesting);

        // End Array with nrOfValues == 0
        pp.writeStartArray(g);
        assertEquals(1, pp._nesting);
        pp.writeEndArray(g, 0);
        assertEquals(0, pp._nesting);

        g.flush();
        String out = sw.toString();
        assertTrue(out.startsWith("["));
        assertTrue(out.contains(","));
        assertTrue(out.endsWith(" ]"));
    }

    @Test(timeout = 4000)
    public void testArrayFormattingLifecycleInlineIndenter() throws IOException {
        StringWriter sw = new StringWriter();
        JsonGenerator g = createGenerator(sw);
        MockIndenter inlineIndenter = new MockIndenter(true); // inline

        DefaultPrettyPrinter pp = new DefaultPrettyPrinter()
                .withArrayIndenter(inlineIndenter);

        assertEquals(0, pp._nesting);
        pp.writeStartArray(g);
        // Inline indenter does NOT increment nesting
        assertEquals(0, pp._nesting);

        pp.writeEndArray(g, 1);
        // Inline indenter does NOT decrement nesting
        assertEquals(0, pp._nesting);

        g.flush();
        assertEquals("[/*indent:0*/]", sw.toString());
    }

    @Test(timeout = 4000)
    public void testNopIndenterContract() throws IOException {
        DefaultPrettyPrinter.NopIndenter nop = DefaultPrettyPrinter.NopIndenter.instance;
        assertTrue(nop.isInline());

        StringWriter sw = new StringWriter();
        JsonGenerator g = createGenerator(sw);
        nop.writeIndentation(g, 0);
        nop.writeIndentation(g, 5);
        nop.writeIndentation(g, -1);
        g.flush();
        assertEquals("", sw.toString());
    }

    @Test(timeout = 4000)
    public void testFixedSpaceIndenterContract() throws IOException {
        DefaultPrettyPrinter.FixedSpaceIndenter fixed = DefaultPrettyPrinter.FixedSpaceIndenter.instance;
        assertTrue(fixed.isInline());

        StringWriter sw = new StringWriter();
        JsonGenerator g = createGenerator(sw);
        fixed.writeIndentation(g, 0);
        fixed.writeIndentation(g, 3);
        g.flush();
        assertEquals("  ", sw.toString());
    }

    @Test(timeout = 4000)
    public void testFullDocumentPrettyPrintingIntegration() throws IOException {
        StringWriter sw = new StringWriter();
        JsonGenerator g = createGenerator(sw);
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        g.setPrettyPrinter(pp);

        g.writeStartObject();
        g.writeStringField("key1", "val1");
        g.writeFieldName("arr");
        g.writeStartArray();
        g.writeNumber(100);
        g.writeNumber(200);
        g.writeEndArray();
        g.writeFieldName("emptyObj");
        g.writeStartObject();
        g.writeEndObject();
        g.writeFieldName("emptyArr");
        g.writeStartArray();
        g.writeEndArray();
        g.writeEndObject();
        g.close();

        String json = sw.toString();
        assertTrue(json.contains("\"key1\" : \"val1\""));
        assertTrue(json.contains("[ 100, 200 ]"));
        assertTrue(json.contains("\"emptyObj\" : { }"));
        assertTrue(json.contains("\"emptyArr\" : [ ]"));
    }

    // =========================================================================
    // Partition E: Object Lifecycle, State & Serialization Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testSerializationRoundtrip() throws Exception {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter("---");
        pp._nesting = 4;
        pp.indentArraysWith(DefaultPrettyPrinter.FixedSpaceIndenter.instance);
        pp.indentObjectsWith(DefaultPrettyPrinter.NopIndenter.instance);
        pp = pp.withoutSpacesInObjectEntries();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(pp);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        DefaultPrettyPrinter deserialized = (DefaultPrettyPrinter) ois.readObject();
        ois.close();

        assertNotNull(deserialized);
        assertNotSame(pp, deserialized);
        // _nesting is transient, must be reset to 0
        assertEquals(0, deserialized._nesting);
        assertFalse(deserialized._spacesInObjectEntries);
        assertEquals("---", deserialized._rootSeparator.getValue());
        assertNotNull(deserialized._arrayIndenter);
        assertNotNull(deserialized._objectIndenter);
        assertNotNull(deserialized._separators);

        // Verify deserialized instance can format normally
        StringWriter sw = new StringWriter();
        JsonGenerator g = createGenerator(sw);
        deserialized.writeRootValueSeparator(g);
        deserialized.writeObjectFieldValueSeparator(g);
        g.flush();
        assertEquals("---:", sw.toString());
    }
}