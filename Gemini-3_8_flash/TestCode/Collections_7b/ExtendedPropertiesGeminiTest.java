package org.apache.commons.collections;

/*
 * [Branch & Defect Analysis Matrix]
 * --------------------------------------------------------------------------------------------------
 * Target Class: org.apache.commons.collections.ExtendedProperties
 * Tested Defect Scenarios (Defects4J):
 *  - testKeySet2 / testKeySet3 (NoSuchElementException): Discrepancies between keysAsListed and Map
 *    views (keySet()), key additions, putAll omitting keysAsListed, and missing property NoSuchElementException.
 *  - testKeySet4 (ComparisonFailure: expected:<[c]> but was:<[b]>): Key traversal ordering, subset prefix
 *    handling, and duplicate/removal order synchronization.
 * Branch & Condition Coverage Targets:
 *  - interpolate() & interpolateHelper():
 *      * base == null vs base without tokens vs base with ${...}
 *      * recursion with existing property vs default property vs undefined variable
 *      * infinite loop detection throwing IllegalStateException (cycle in priorVariables)
 *      * multiple tokens in single string (${var1}/${var2})
 *  - PropertiesTokenizer & Reader:
 *      * endsWithSlash with odd vs even preceding backslashes
 *      * escaped commas (\,) vs unescaped delimiters
 *      * multiline properties with trailing backslash continuation
 *      * comments (#) and blank lines ignored
 *      * empty values (key =) ignored
 *  - load() & include directives:
 *      * encoding fallback (given enc -> 8859_1 -> system default)
 *      * includePropertyName instance vs deprecated static include
 *      * relative includes with and without "./" vs absolute paths
 *      * non-existent or unreadable include files
 *  - addProperty() / addPropertyInternal() / setProperty() / clearProperty():
 *      * String with commas -> tokenized; String without commas -> direct
 *      * non-String values
 *      * key collisions: single String -> Vector; List -> List.add()
 *      * clearProperty() rebuilding keysAsListed and removing entry
 *  - Type Getters (Boolean, Byte, Short, Integer, Long, Float, Double, Vector, List, StringArray, Properties):
 *      * exact type instance return
 *      * String conversion and caching back into map
 *      * missing key with default value vs missing key throwing NoSuchElementException
 *      * ClassCastException on incompatible types
 *      * NumberFormatException on malformed strings
 *      * testBoolean cases: "true", "on", "yes", "false", "off", "no" (case-insensitive), invalid -> null
 *  - subset(), combine(), save(), convertProperties(), putAll():
 *      * prefix matching: exact length vs longer length (substring offset)
 *      * empty/non-matching subset returning null
 *      * combine overwriting existing keys
 *      * save() formatting Strings and Lists, escaping delimiters
 *      * putAll() with ExtendedProperties instance vs standard Map
 */

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

public class ExtendedPropertiesGeminiTest {

    // =========================================================================
    // PARTITION A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicAddAndGetProperty() {
        ExtendedProperties props = new ExtendedProperties();
        assertFalse(props.isInitialized());

        props.addProperty("host", "localhost");
        assertTrue(props.isInitialized());
        assertEquals("localhost", props.getProperty("host"));
        assertEquals("localhost", props.getString("host"));
    }

    @Test(timeout = 4000)
    public void testAddPropertyMultiValuesFormVector() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("fruits", "apple");
        props.addProperty("fruits", "banana");
        props.addProperty("fruits", "orange");

        Object prop = props.getProperty("fruits");
        assertTrue(prop instanceof List);
        List list = (List) prop;
        assertEquals(3, list.size());
        assertEquals("apple", list.get(0));
        assertEquals("banana", list.get(1));
        assertEquals("orange", list.get(2));

        assertEquals("apple", props.getString("fruits")); // returns first element
    }

    @Test(timeout = 4000)
    public void testAddPropertyCommaSeparatedString() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("items", "item1, item2, item3");

        Object val = props.getProperty("items");
        assertTrue(val instanceof List);
        List list = (List) val;
        assertEquals(3, list.size());
        assertEquals("item1", list.get(0));
        assertEquals("item2", list.get(1));
        assertEquals("item3", list.get(2));
    }

    @Test(timeout = 4000)
    public void testAddPropertyEscapedComma() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("greeting", "Hello\\, World, How are you?");

        Object val = props.getProperty("greeting");
        assertTrue(val instanceof List);
        List list = (List) val;
        assertEquals(2, list.size());
        assertEquals("Hello, World", list.get(0));
        assertEquals("How are you?", list.get(1));
    }

    @Test(timeout = 4000)
    public void testSetPropertyReplacesOldValue() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("name", "initial");
        props.addProperty("name", "second");
        assertEquals(2, ((List) props.getProperty("name")).size());

        props.setProperty("name", "finalValue");
        assertEquals("finalValue", props.getProperty("name"));
        assertEquals("finalValue", props.getString("name"));
    }

    @Test(timeout = 4000)
    public void testClearPropertyRemovesEntryAndKeyAccounting() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("k1", "v1");
        props.addProperty("k2", "v2");

        assertTrue(props.containsKey("k1"));
        props.clearProperty("k1");
        assertFalse(props.containsKey("k1"));
        assertNull(props.getProperty("k1"));

        // Clear non-existent key (safe no-op)
        props.clearProperty("k1");

        Iterator it = props.getKeys();
        assertTrue(it.hasNext());
        assertEquals("k2", it.next());
        assertFalse(it.hasNext());
    }

    @Test(timeout = 4000)
    public void testCombineProperties() {
        ExtendedProperties p1 = new ExtendedProperties();
        p1.addProperty("k1", "v1");
        p1.addProperty("k2", "v2");

        ExtendedProperties p2 = new ExtendedProperties();
        p2.addProperty("k2", "v2_override");
        p2.addProperty("k3", "v3");

        p1.combine(p2);
        assertEquals("v1", p1.getString("k1"));
        assertEquals("v2_override", p1.getString("k2"));
        assertEquals("v3", p1.getString("k3"));
    }

    @Test(timeout = 4000)
    public void testSubsetExtraction() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("app.db.host", "127.0.0.1");
        props.addProperty("app.db.port", "5432");
        props.addProperty("app.name", "TestApp");
        props.addProperty("app", "rootApp"); // test key length == prefix.length()

        ExtendedProperties dbSub = props.subset("app.db");
        assertNotNull(dbSub);
        assertEquals("127.0.0.1", dbSub.getString("host"));
        assertEquals("5432", dbSub.getString("port"));
        assertNull(dbSub.getString("name"));

        ExtendedProperties appExact = props.subset("app");
        assertNotNull(appExact);
        assertEquals("rootApp", appExact.getString("app"));
        assertEquals("TestApp", appExact.getString("name"));

        // Non-matching prefix
        ExtendedProperties nonMatch = props.subset("nomatch");
        assertNull(nonMatch);
    }

    @Test(timeout = 4000)
    public void testGetKeysWithPrefix() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("service.a", "1");
        props.addProperty("service.b", "2");
        props.addProperty("client.c", "3");

        Iterator it = props.getKeys("service");
        assertTrue(it.hasNext());
        assertEquals("service.a", it.next());
        assertTrue(it.hasNext());
        assertEquals("service.b", it.next());
        assertFalse(it.hasNext());

        Iterator emptyIt = props.getKeys("nonexistent");
        assertFalse(emptyIt.hasNext());
    }

    @Test(timeout = 4000)
    public void testDisplayDoesNotThrow() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("debug.key", "debug.val");
        props.display(); // ensure no unexpected exceptions
    }

    // =========================================================================
    // PARTITION B: Boundary Value Analysis (BVA) & Interpolation
    // =========================================================================

    @Test(timeout = 4000)
    public void testInterpolationBasic() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("baseDir", "/home/app");
        props.addProperty("logDir", "${baseDir}/logs");

        assertEquals("/home/app/logs", props.getString("logDir"));
    }

    @Test(timeout = 4000)
    public void testInterpolationMultipleVariables() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("protocol", "https");
        props.addProperty("host", "example.com");
        props.addProperty("port", "8443");
        props.addProperty("url", "${protocol}://${host}:${port}/api");

        assertEquals("https://example.com:8443/api", props.getString("url"));
    }

    @Test(timeout = 4000)
    public void testInterpolationUndefinedLeavesTokenIntact() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("msg", "Welcome ${user}!");

        assertEquals("Welcome ${user}!", props.getString("msg"));
    }

    @Test(timeout = 4000)
    public void testInterpolationWithDefaultsFallback() {
        ExtendedProperties defaults = new ExtendedProperties();
        defaults.addProperty("env", "production");

        ExtendedProperties props = new ExtendedProperties();
        props.defaults = defaults;
        props.addProperty("target", "run-${env}");

        assertEquals("run-production", props.getString("target"));
    }

    @Test(timeout = 4000)
    public void testInterpolationNullBase() {
        ExtendedProperties props = new ExtendedProperties();
        assertNull(props.interpolate(null));
        assertNull(props.interpolateHelper(null, null));
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testInterpolationInfiniteLoopDirect() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("loop", "${loop}");
        props.getString("loop");
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testInterpolationInfiniteLoopIndirect() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("a", "${b}");
        props.addProperty("b", "${c}");
        props.addProperty("c", "${a}");
        props.getString("a");
    }

    // =========================================================================
    // PARTITION C: Defect-Targeted Branch Zone (Defects4J ground truth focus)
    // =========================================================================

    @Test(timeout = 4000)
    public void testKeySetOrderingAndIntegrityDefectTarget() {
        // Targets testKeySet2, testKeySet3, testKeySet4 issues:
        // Verification of key order preservation, multiple keys, and removal
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("a", "alpha");
        ep.addProperty("b", "beta");
        ep.addProperty("c", "gamma");

        Iterator it = ep.getKeys();
        assertTrue(it.hasNext());
        assertEquals("a", it.next());
        assertTrue(it.hasNext());
        assertEquals("b", it.next());
        assertTrue(it.hasNext());
        assertEquals("c", it.next());
        assertFalse(it.hasNext());

        Set keySet = ep.keySet();
        assertEquals(3, keySet.size());
        assertTrue(keySet.contains("a"));
        assertTrue(keySet.contains("b"));
        assertTrue(keySet.contains("c"));

        // Removing key 'b' should update keysAsListed and Map properly
        ep.clearProperty("b");
        assertEquals(2, ep.size());
        Iterator itAfter = ep.getKeys();
        assertTrue(itAfter.hasNext());
        assertEquals("a", itAfter.next());
        assertTrue(itAfter.hasNext());
        assertEquals("c", itAfter.next()); // Ensures 'c' is next, not 'b'
        assertFalse(itAfter.hasNext());
    }

    @Test(timeout = 4000)
    public void testPutAllWithExtendedPropertiesMaintainsOrder() {
        ExtendedProperties source = new ExtendedProperties();
        source.addProperty("k1", "v1");
        source.addProperty("k2", "v2");
        source.addProperty("k3", "v3");

        ExtendedProperties target = new ExtendedProperties();
        target.putAll(source);

        assertEquals("v1", target.get("k1"));
        assertEquals("v2", target.get("k2"));
        assertEquals("v3", target.get("k3"));
    }

    @Test(timeout = 4000)
    public void testPutAllWithStandardMap() {
        Map map = new HashMap();
        map.put("m1", "v1");
        map.put("m2", "v2");

        ExtendedProperties target = new ExtendedProperties();
        target.putAll(map);
        assertEquals("v1", target.get("m1"));
        assertEquals("v2", target.get("m2"));
    }

    @Test(timeout = 4000)
    public void testMissingKeyNoSuchElementExceptions() {
        ExtendedProperties props = new ExtendedProperties();

        try {
            props.getBoolean("missing.bool");
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException expected) {
            assertTrue(expected.getMessage().contains("missing.bool"));
        }

        try {
            props.getByte("missing.byte");
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException expected) {
            assertTrue(expected.getMessage().contains("missing.byte"));
        }

        try {
            props.getShort("missing.short");
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException expected) {
            assertTrue(expected.getMessage().contains("missing.short"));
        }

        try {
            props.getInteger("missing.int");
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException expected) {
            assertTrue(expected.getMessage().contains("missing.int"));
        }

        try {
            props.getInt("missing.int2");
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException expected) {
            assertTrue(expected.getMessage().contains("missing.int2"));
        }

        try {
            props.getLong("missing.long");
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException expected) {
            assertTrue(expected.getMessage().contains("missing.long"));
        }

        try {
            props.getFloat("missing.float");
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException expected) {
            assertTrue(expected.getMessage().contains("missing.float"));
        }

        try {
            props.getDouble("missing.double");
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException expected) {
            assertTrue(expected.getMessage().contains("missing.double"));
        }
    }

    // =========================================================================
    // PARTITION D: Type Conversions, Defaults, and Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testBooleanConversions() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("b1", "true");
        props.addProperty("b2", "on");
        props.addProperty("b3", "yes");
        props.addProperty("b4", "false");
        props.addProperty("b5", "off");
        props.addProperty("b6", "no");
        props.addProperty("b7", Boolean.TRUE);

        assertTrue(props.getBoolean("b1"));
        assertTrue(props.getBoolean("b2"));
        assertTrue(props.getBoolean("b3"));
        assertFalse(props.getBoolean("b4"));
        assertFalse(props.getBoolean("b5"));
        assertFalse(props.getBoolean("b6"));
        assertTrue(props.getBoolean("b7"));

        assertTrue(props.getBoolean("nonexistent", true));
        assertFalse(props.getBoolean("nonexistent", false));
        assertEquals(Boolean.TRUE, props.getBoolean("nonexistent", Boolean.TRUE));
    }

    @Test(timeout = 4000)
    public void testTestBooleanHelperDirectly() {
        ExtendedProperties props = new ExtendedProperties();
        assertEquals("true", props.testBoolean("TRUE"));
        assertEquals("true", props.testBoolean("On"));
        assertEquals("true", props.testBoolean("YeS"));
        assertEquals("false", props.testBoolean("FALSE"));
        assertEquals("false", props.testBoolean("Off"));
        assertEquals("false", props.testBoolean("No"));
        assertNull(props.testBoolean("invalid"));
    }

    @Test(timeout = 4000)
    public void testNumericConversionsAndDefaults() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("num.byte", "10");
        props.addProperty("num.short", "20");
        props.addProperty("num.int", "30");
        props.addProperty("num.long", "40");
        props.addProperty("num.float", "50.5");
        props.addProperty("num.double", "60.6");

        assertEquals((byte) 10, props.getByte("num.byte"));
        assertEquals((short) 20, props.getShort("num.short"));
        assertEquals(30, props.getInt("num.int"));
        assertEquals(30, props.getInt("num.int", 999));
        assertEquals(30, props.getInteger("num.int", 999));
        assertEquals(Integer.valueOf(30), props.getInteger("num.int", (Integer) null));
        assertEquals(40L, props.getLong("num.long"));
        assertEquals(50.5f, props.getFloat("num.float"), 0.0001f);
        assertEquals(60.6, props.getDouble("num.double"), 0.0001);

        // Primitive defaults when key is missing
        assertEquals((byte) 1, props.getByte("missing", (byte) 1));
        assertEquals((short) 2, props.getShort("missing", (short) 2));
        assertEquals(3, props.getInt("missing", 3));
        assertEquals(4L, props.getLong("missing", 4L));
        assertEquals(5.5f, props.getFloat("missing", 5.5f), 0.0001f);
        assertEquals(6.6, props.getDouble("missing", 6.6), 0.0001);

        // Object defaults when key is missing
        assertEquals(Byte.valueOf((byte) 1), props.getByte("missing", Byte.valueOf((byte) 1)));
        assertEquals(Short.valueOf((short) 2), props.getShort("missing", Short.valueOf((short) 2)));
        assertEquals(Integer.valueOf(3), props.getInteger("missing", Integer.valueOf(3)));
        assertEquals(Long.valueOf(4L), props.getLong("missing", Long.valueOf(4L)));
        assertEquals(Float.valueOf(5.5f), props.getFloat("missing", Float.valueOf(5.5f)));
        assertEquals(Double.valueOf(6.6), props.getDouble("missing", Double.valueOf(6.6)));
    }

    @Test(timeout = 4000)
    public void testDirectNumericObjectsReturnedDirectly() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("byte.obj", Byte.valueOf((byte) 7));
        props.addProperty("short.obj", Short.valueOf((short) 8));
        props.addProperty("int.obj", Integer.valueOf(9));
        props.addProperty("long.obj", Long.valueOf(10L));
        props.addProperty("float.obj", Float.valueOf(11.1f));
        props.addProperty("double.obj", Double.valueOf(12.2));

        assertEquals((byte) 7, props.getByte("byte.obj"));
        assertEquals((short) 8, props.getShort("short.obj"));
        assertEquals(9, props.getInt("int.obj"));
        assertEquals(10L, props.getLong("long.obj"));
        assertEquals(11.1f, props.getFloat("float.obj"), 0.0001f);
        assertEquals(12.2, props.getDouble("double.obj"), 0.0001);
    }

    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testInvalidNumberFormatThrowsException() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("bad.number", "not_a_number");
        props.getInt("bad.number");
    }

    @Test(timeout = 4000, expected = ClassCastException.class)
    public void testClassCastExceptionForIncompatibleType() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("obj.key", new Object());
        props.getString("obj.key");
    }

    @Test(timeout = 4000, expected = ClassCastException.class)
    public void testClassCastExceptionForBoolean() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("obj.key", new Object());
        props.getBoolean("obj.key");
    }

    @Test(timeout = 4000, expected = ClassCastException.class)
    public void testClassCastExceptionForByte() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("obj.key", new Object());
        props.getByte("obj.key");
    }

    @Test(timeout = 4000, expected = ClassCastException.class)
    public void testClassCastExceptionForShort() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("obj.key", new Object());
        props.getShort("obj.key");
    }

    @Test(timeout = 4000, expected = ClassCastException.class)
    public void testClassCastExceptionForInteger() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("obj.key", new Object());
        props.getInteger("obj.key");
    }

    @Test(timeout = 4000, expected = ClassCastException.class)
    public void testClassCastExceptionForLong() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("obj.key", new Object());
        props.getLong("obj.key");
    }

    @Test(timeout = 4000, expected = ClassCastException.class)
    public void testClassCastExceptionForFloat() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("obj.key", new Object());
        props.getFloat("obj.key");
    }

    @Test(timeout = 4000, expected = ClassCastException.class)
    public void testClassCastExceptionForDouble() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("obj.key", new Object());
        props.getDouble("obj.key");
    }

    @Test(timeout = 4000, expected = ClassCastException.class)
    public void testClassCastExceptionForStringArray() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("obj.key", new Object());
        props.getStringArray("obj.key");
    }

    @Test(timeout = 4000, expected = ClassCastException.class)
    public void testClassCastExceptionForVector() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("obj.key", new Object());
        props.getVector("obj.key");
    }

    @Test(timeout = 4000, expected = ClassCastException.class)
    public void testClassCastExceptionForList() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("obj.key", new Object());
        props.getList("obj.key");
    }

    // =========================================================================
    // PARTITION E: Collections, Array Getters & Defaults Hierarchy
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetStringArray() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("single", "value1");
        props.addProperty("multi", "val1, val2, val3");

        String[] singleArr = props.getStringArray("single");
        assertEquals(1, singleArr.length);
        assertEquals("value1", singleArr[0]);

        String[] multiArr = props.getStringArray("multi");
        assertEquals(3, multiArr.length);
        assertEquals("val1", multiArr[0]);
        assertEquals("val2", multiArr[1]);
        assertEquals("val3", multiArr[2]);

        String[] emptyArr = props.getStringArray("missing");
        assertNotNull(emptyArr);
        assertEquals(0, emptyArr.length);
    }

    @Test(timeout = 4000)
    public void testGetVectorAndGetListFromSingleString() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("single", "value");

        Vector vec = props.getVector("single");
        assertEquals(1, vec.size());
        assertEquals("value", vec.get(0));

        // It also morphs internal storage to list/vector
        List list = props.getList("single");
        assertEquals(1, list.size());
        assertEquals("value", list.get(0));

        // Null key with defaults
        Vector defVec = new Vector();
        defVec.add("def");
        Vector resVec = props.getVector("missing", defVec);
        assertEquals("def", resVec.get(0));

        List defLst = new ArrayList();
        defLst.add("defLst");
        List resLst = props.getList("missing", defLst);
        assertEquals("defLst", resLst.get(0));

        // Null key without defaults
        assertNotNull(props.getVector("missing", (Vector) null));
        assertNotNull(props.getList("missing", (List) null));
    }

    @Test(timeout = 4000)
    public void testGetPropertiesSubKeyExtraction() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("database", "user=scott, password=tiger, timeout=30");

        Properties subProps = props.getProperties("database");
        assertEquals("scott", subProps.getProperty("user"));
        assertEquals("tiger", subProps.getProperty("password"));
        assertEquals("30", subProps.getProperty("timeout"));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetPropertiesMalformedTokenThrowsException() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("badProps", "user=scott, missingEqualSign");
        props.getProperties("badProps");
    }

    @Test(timeout = 4000)
    public void testDefaultsFallbackForGetters() {
        ExtendedProperties defaults = new ExtendedProperties();
        defaults.addProperty("d.str", "defaultString");
        defaults.addProperty("d.bool", "true");
        defaults.addProperty("d.byte", "5");
        defaults.addProperty("d.short", "15");
        defaults.addProperty("d.int", "25");
        defaults.addProperty("d.long", "35");
        defaults.addProperty("d.float", "45.5");
        defaults.addProperty("d.double", "55.5");
        defaults.addProperty("d.list", "a,b");

        ExtendedProperties props = new ExtendedProperties();
        props.defaults = defaults;

        assertEquals("defaultString", props.getString("d.str"));
        assertEquals("defaultString", props.getString("d.str", "ignored"));
        assertTrue(props.getBoolean("d.bool", false));
        assertEquals((byte) 5, props.getByte("d.byte", (byte) 0));
        assertEquals((short) 15, props.getShort("d.short", (short) 0));
        assertEquals(25, props.getInt("d.int", 0));
        assertEquals(35L, props.getLong("d.long", 0L));
        assertEquals(45.5f, props.getFloat("d.float", 0f), 0.0001f);
        assertEquals(55.5, props.getDouble("d.double", 0.0), 0.0001);
        assertEquals(2, props.getStringArray("d.list").length);
        assertEquals(2, props.getVector("d.list").size());
        assertEquals(2, props.getList("d.list").size());
    }

    // =========================================================================
    // PARTITION F: I/O, Load, Save, Escape & Line Parsing
    // =========================================================================

    @Test(timeout = 4000)
    public void testLoadFromStreamWithContinuationAndComments() throws IOException {
        String content =
                "# Leading comment\n" +
                "\n" +
                "simpleKey = simpleValue\n" +
                "emptyValue = \n" + // should be ignored
                "multiline = first line \\\n" +
                "            second line \\\n" +
                "            third line\n" +
                "commas = val1, val2\\,withComma, val3\n";

        ExtendedProperties props = new ExtendedProperties();
        props.load(new ByteArrayInputStream(content.getBytes("ISO-8859-1")));

        assertEquals("simpleValue", props.getString("simpleKey"));
        assertNull(props.getProperty("emptyValue"));
        assertEquals("first line second line third line", props.getString("multiline"));

        List list = (List) props.getProperty("commas");
        assertEquals(3, list.size());
        assertEquals("val1", list.get(0));
        assertEquals("val2,withComma", list.get(1));
        assertEquals("val3", list.get(2));
    }

    @Test(timeout = 4000)
    public void testIncludeDirectiveRelativeAndAbsolute() throws IOException {
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        File subFile = File.createTempFile("subProps", ".properties", tempDir);
        subFile.deleteOnExit();

        FileOutputStream fos = new FileOutputStream(subFile);
        fos.write("sub.key = sub.val\n".getBytes("ISO-8859-1"));
        fos.close();

        // Main configuration including subFile using relative "./" syntax
        File mainFile = File.createTempFile("mainProps", ".properties", tempDir);
        mainFile.deleteOnExit();

        FileOutputStream mainFos = new FileOutputStream(mainFile);
        String includeLine = "include = ./" + subFile.getName() + "\nmain.key = main.val\n";
        mainFos.write(includeLine.getBytes("ISO-8859-1"));
        mainFos.close();

        ExtendedProperties props = new ExtendedProperties(mainFile.getAbsolutePath());
        assertEquals("main.val", props.getString("main.key"));
        assertEquals("sub.val", props.getString("sub.key"));
    }

    @Test(timeout = 4000)
    public void testIncludeDirectiveAbsolute() throws IOException {
        File subFile = File.createTempFile("absSubProps", ".properties");
        subFile.deleteOnExit();

        FileOutputStream fos = new FileOutputStream(subFile);
        fos.write("abs.key = abs.val\n".getBytes("ISO-8859-1"));
        fos.close();

        ExtendedProperties props = new ExtendedProperties();
        // Load with absolute path in include property
        String config = "include = " + subFile.getAbsolutePath() + "\n";
        props.load(new ByteArrayInputStream(config.getBytes("ISO-8859-1")));

        assertEquals("abs.val", props.getString("abs.key"));
    }

    @Test(timeout = 4000)
    public void testIncludePropertyNameGetSet() {
        ExtendedProperties props = new ExtendedProperties();
        assertEquals("include", props.getInclude());

        props.setInclude("custom.include");
        assertEquals("custom.include", props.getInclude());

        props.setInclude(null);
        assertNull(props.getInclude());
    }

    @Test(timeout = 4000)
    public void testSaveOutputFormatting() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("single", "val,with,commas\\and\\slashes");
        props.addProperty("list", "item1");
        props.addProperty("list", "item2");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        props.save(baos, "Header Comment");

        String savedContent = baos.toString();
        assertTrue(savedContent.startsWith("Header Comment"));
        assertTrue(savedContent.contains("single="));
        assertTrue(savedContent.contains("list="));

        // Save with null output stream does nothing
        props.save(null, "Ignored");
    }

    @Test(timeout = 4000)
    public void testConvertStandardProperties() {
        Properties sysProps = new Properties();
        sysProps.setProperty("p1", "v1");
        sysProps.setProperty("p2", "v2");

        ExtendedProperties ep = ExtendedProperties.convertProperties(sysProps);
        assertEquals("v1", ep.getString("p1"));
        assertEquals("v2", ep.getString("p2"));
    }

    @Test(timeout = 4000)
    public void testConstructorWithDefaultFile() throws IOException {
        File defFile = File.createTempFile("defaults", ".properties");
        defFile.deleteOnExit();
        FileOutputStream fos1 = new FileOutputStream(defFile);
        fos1.write("default.prop = fromDefault\n".getBytes("ISO-8859-1"));
        fos1.close();

        File mainFile = File.createTempFile("main", ".properties");
        mainFile.deleteOnExit();
        FileOutputStream fos2 = new FileOutputStream(mainFile);
        fos2.write("main.prop = fromMain\n".getBytes("ISO-8859-1"));
        fos2.close();

        ExtendedProperties ep = new ExtendedProperties(mainFile.getAbsolutePath(), defFile.getAbsolutePath());
        assertEquals("fromMain", ep.getString("main.prop"));
        assertEquals("fromDefault", ep.getString("default.prop"));
    }
}