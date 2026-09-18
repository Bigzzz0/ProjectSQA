package org.apache.commons.collections;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Vector;

import org.junit.Test;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Targets: org.apache.commons.collections.ExtendedProperties
 *
 * 1. COLLECTIONS-271 Defect Target:
 *    - Unescaping behavior in setProperty / addProperty when handling UNC file paths or escaped backslashes.
 *    - Assertion failure expected on defective version: expected:<\\192.168.1.91\test> but was:<\192.168.1.91\test>.
 *
 * 2. Coverage & Boundary Matrix:
 *    - Constructors: empty, (file), (file, defaultFile).
 *    - include / setInclude / getInclude: null handling, empty string conversion to null, backwards compatibility fallback.
 *    - load() & PropertiesReader:
 *        * Comments ('#') and blank line skipping.
 *        * Multi-line continuation via trailing backslash (endsWithSlash: even vs odd preceding backslashes).
 *        * Include directives: relative path, absolute path, "./" prefix handling, missing file, recursion.
 *        * Delimiters and escaped commas in PropertiesTokenizer.
 *        * Unsupported encoding fallback path.
 *    - interpolate() & interpolateHelper():
 *        * Variable substitution: ${var}.
 *        * Multiple interpolations in a single value.
 *        * Circular reference detection and IllegalStateException ("infinite loop in property interpolation").
 *        * Default values fallback during interpolation.
 *        * Undefined variables retained as ${var}.
 *        * Null base string interpolation guard.
 *    - Type Conversions & Getters:
 *        * getString, getStringArray, getVector, getList.
 *        * getBoolean, testBoolean: true/on/yes vs false/off/no (case-insensitive), fallback to default, NoSuchElementException on missing key.
 *        * getByte, getShort, getInt, getInteger, getLong, getFloat, getDouble: parsing, defaults, NoSuchElementException, NumberFormatException, ClassCastException.
 *        * getProperties: key=value parsing, token without '=' throwing IllegalArgumentException.
 *    - Map / Hashtable Operations:
 *        * put, putAll (with ExtendedProperties and Map), remove, clearProperty, keysAsListed synchronization.
 *        * combine(): merging properties and overwriting existing keys.
 *        * subset(): matching prefixes, single property prefix edge case, non-matching returning null.
 *        * save(): output stream formatting, string and list properties escaping, null stream guard.
 *        * convertProperties(): parent and child properties inheritance.
 */
public class ExtendedPropertiesGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (COLLECTIONS-271)
    // =========================================================================

    /**
     * Directly targets COLLECTIONS-271:
     * When setting a property containing backslashes (such as a Windows UNC path),
     * setProperty shouldn't improperly unescape backslashes.
     */
    @Test(timeout = 4000)
    public void testCollections271() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.setProperty("prop", "\\\\192.168.1.91\\test");
        assertEquals("\\\\192.168.1.91\\test", ep.getString("prop"));
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicAddAndGetProperty() {
        ExtendedProperties props = new ExtendedProperties();
        assertFalse(props.isInitialized());

        props.addProperty("key1", "val1");
        assertTrue(props.isInitialized());
        assertEquals("val1", props.getProperty("key1"));
        assertEquals("val1", props.getString("key1"));

        // Appending to same key creates a Vector/List internally
        props.addProperty("key1", "val2");
        Object val = props.getProperty("key1");
        assertTrue(val instanceof List);
        List list = (List) val;
        assertEquals(2, list.size());
        assertEquals("val1", list.get(0));
        assertEquals("val2", list.get(1));

        // When a list exists, getString returns the first element
        assertEquals("val1", props.getString("key1"));

        // Adding third value appends to the existing List
        props.addProperty("key1", "val3");
        assertEquals(3, ((List) props.getProperty("key1")).size());
    }

    @Test(timeout = 4000)
    public void testSetPropertyReplacesExisting() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key", "val1");
        props.addProperty("key", "val2");
        assertEquals(2, ((List) props.getProperty("key")).size());

        props.setProperty("key", "newVal");
        assertEquals("newVal", props.getProperty("key"));
        assertEquals("newVal", props.getString("key"));
    }

    @Test(timeout = 4000)
    public void testCommaDelimitedTokenization() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("tokens", "a, b, c");

        String[] arr = props.getStringArray("tokens");
        assertEquals(3, arr.length);
        assertEquals("a", arr[0]);
        assertEquals("b", arr[1]);
        assertEquals("c", arr[2]);

        // Escaped commas should not split
        props.clearProperty("tokens");
        props.addProperty("tokens", "a\\,b, c");
        arr = props.getStringArray("tokens");
        assertEquals(2, arr.length);
        assertEquals("a,b", arr[0]);
        assertEquals("c", arr[1]);
    }

    @Test(timeout = 4000)
    public void testClearAndRemoveProperty() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("k1", "v1");
        props.setProperty("k2", "v2");

        assertTrue(props.containsKey("k1"));
        props.clearProperty("k1");
        assertFalse(props.containsKey("k1"));
        assertNull(props.getProperty("k1"));

        // clear non-existent does nothing
        props.clearProperty("k1");

        // remove() returns old value
        Object old = props.remove("k2");
        assertEquals("v2", old);
        assertFalse(props.containsKey("k2"));

        Object nonExistent = props.remove("unknown");
        assertNull(nonExistent);
    }

    @Test(timeout = 4000)
    public void testCombineProperties() {
        ExtendedProperties base = new ExtendedProperties();
        base.setProperty("a", "1");
        base.setProperty("b", "2");

        ExtendedProperties other = new ExtendedProperties();
        other.setProperty("b", "20");
        other.setProperty("c", "30");

        base.combine(other);
        assertEquals("1", base.getString("a"));
        assertEquals("20", base.getString("b"));
        assertEquals("30", base.getString("c"));
    }

    @Test(timeout = 4000)
    public void testSubset() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("db.host", "localhost");
        props.setProperty("db.port", "3306");
        props.setProperty("db", "database");
        props.setProperty("app.name", "myApp");

        ExtendedProperties dbSub = props.subset("db");
        assertNotNull(dbSub);
        assertEquals("localhost", dbSub.getString("host"));
        assertEquals("3306", dbSub.getString("port"));
        assertEquals("database", dbSub.getString("db"));
        assertNull(dbSub.getString("name"));

        // Subset with no match returns null
        assertNull(props.subset("nomatch"));
    }

    @Test(timeout = 4000)
    public void testGetKeysAndPrefixes() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("server.host", "127.0.0.1");
        props.setProperty("server.port", "8080");
        props.setProperty("client.timeout", "5000");

        Iterator allKeys = props.getKeys();
        List keyList = new ArrayList();
        while (allKeys.hasNext()) {
            keyList.add(allKeys.next());
        }
        assertEquals(3, keyList.size());
        assertEquals("server.host", keyList.get(0));
        assertEquals("server.port", keyList.get(1));
        assertEquals("client.timeout", keyList.get(2));

        Iterator serverKeys = props.getKeys("server.");
        List serverList = new ArrayList();
        while (serverKeys.hasNext()) {
            serverList.add(serverKeys.next());
        }
        assertEquals(2, serverList.size());
        assertTrue(serverList.contains("server.host"));
        assertTrue(serverList.contains("server.port"));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Type Conversions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBooleanParsing() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("b1", "true");
        props.setProperty("b2", "on");
        props.setProperty("b3", "yes");
        props.setProperty("b4", "false");
        props.setProperty("b5", "off");
        props.setProperty("b6", "no");
        props.setProperty("b7", "invalid");

        assertTrue(props.getBoolean("b1"));
        assertTrue(props.getBoolean("b2"));
        assertTrue(props.getBoolean("b3"));
        assertFalse(props.getBoolean("b4"));
        assertFalse(props.getBoolean("b5"));
        assertFalse(props.getBoolean("b6"));

        // Invalid boolean string parses as false through Boolean.valueOf(null)
        assertFalse(props.getBoolean("b7"));

        // Defaults
        assertTrue(props.getBoolean("missing", true));
        assertFalse(props.getBoolean("missing", false));
        assertNull(props.getBoolean("missing", (Boolean) null));

        // Object already Boolean
        props.put("bObj", Boolean.TRUE);
        assertTrue(props.getBoolean("bObj"));
    }

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testGetBooleanMissingKeyThrowsException() {
        ExtendedProperties props = new ExtendedProperties();
        props.getBoolean("missingKey");
    }

    @Test(timeout = 4000)
    public void testNumericTypesWithDefaultsAndValues() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("byteVal", String.valueOf(Byte.MAX_VALUE));
        props.setProperty("shortVal", String.valueOf(Short.MIN_VALUE));
        props.setProperty("intVal", "12345");
        props.setProperty("longVal", String.valueOf(Long.MAX_VALUE));
        props.setProperty("floatVal", "3.14");
        props.setProperty("doubleVal", "2.71828");

        assertEquals(Byte.MAX_VALUE, props.getByte("byteVal"));
        assertEquals(Byte.MAX_VALUE, props.getByte("byteVal", (byte) 1));

        assertEquals(Short.MIN_VALUE, props.getShort("shortVal"));
        assertEquals(Short.MIN_VALUE, props.getShort("shortVal", (short) 2));

        assertEquals(12345, props.getInt("intVal"));
        assertEquals(12345, props.getInt("intVal", 99));
        assertEquals(12345, props.getInteger("intVal"));
        assertEquals(12345, props.getInteger("intVal", 99));
        assertEquals(Integer.valueOf(12345), props.getInteger("intVal", (Integer) null));

        assertEquals(Long.MAX_VALUE, props.getLong("longVal"));
        assertEquals(Long.MAX_VALUE, props.getLong("longVal", 0L));

        assertEquals(3.14f, props.getFloat("floatVal"), 0.0001f);
        assertEquals(3.14f, props.getFloat("floatVal", 1.0f), 0.0001f);

        assertEquals(2.71828d, props.getDouble("doubleVal"), 0.000001d);
        assertEquals(2.71828d, props.getDouble("doubleVal", 1.0d), 0.000001d);

        // Missing keys with default values
        assertEquals((byte) 5, props.getByte("missing", (byte) 5));
        assertEquals((short) 10, props.getShort("missing", (short) 10));
        assertEquals(15, props.getInt("missing", 15));
        assertEquals(20L, props.getLong("missing", 20L));
        assertEquals(25.5f, props.getFloat("missing", 25.5f), 0.001f);
        assertEquals(30.5d, props.getDouble("missing", 30.5d), 0.001d);
    }

    @Test(timeout = 4000)
    public void testNumericObjectsDirectlyStored() {
        ExtendedProperties props = new ExtendedProperties();
        props.put("b", new Byte((byte) 12));
        props.put("s", new Short((short) 120));
        props.put("i", new Integer(1200));
        props.put("l", new Long(12000L));
        props.put("f", new Float(1.5f));
        props.put("d", new Double(2.5d));

        assertEquals((byte) 12, props.getByte("b"));
        assertEquals((short) 120, props.getShort("s"));
        assertEquals(1200, props.getInt("i"));
        assertEquals(12000L, props.getLong("l"));
        assertEquals(1.5f, props.getFloat("f"), 0.001f);
        assertEquals(2.5d, props.getDouble("d"), 0.001d);
    }

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testGetByteMissingKeyThrowsException() {
        new ExtendedProperties().getByte("nokey");
    }

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testGetShortMissingKeyThrowsException() {
        new ExtendedProperties().getShort("nokey");
    }

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testGetIntegerMissingKeyThrowsException() {
        new ExtendedProperties().getInteger("nokey");
    }

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testGetLongMissingKeyThrowsException() {
        new ExtendedProperties().getLong("nokey");
    }

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testGetFloatMissingKeyThrowsException() {
        new ExtendedProperties().getFloat("nokey");
    }

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testGetDoubleMissingKeyThrowsException() {
        new ExtendedProperties().getDouble("nokey");
    }

    @Test(timeout = 4000)
    public void testCollectionsGetVectorAndGetList() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("single", "val");
        props.addProperty("multiple", "v1, v2, v3");

        Vector vSingle = props.getVector("single");
        assertEquals(1, vSingle.size());
        assertEquals("val", vSingle.get(0));

        Vector vMulti = props.getVector("multiple");
        assertEquals(3, vMulti.size());
        assertEquals("v1", vMulti.get(0));

        List lSingle = props.getList("single");
        assertEquals(1, lSingle.size());
        assertEquals("val", lSingle.get(0));

        List lMulti = props.getList("multiple");
        assertEquals(3, lMulti.size());
        assertEquals("v3", lMulti.get(2));

        // Missing returns new empty collection or default
        Vector vDef = props.getVector("missing", null);
        assertNotNull(vDef);
        assertTrue(vDef.isEmpty());

        List lDef = props.getList("missing", null);
        assertNotNull(lDef);
        assertTrue(lDef.isEmpty());

        List customList = new ArrayList();
        assertSame(customList, props.getList("missing", customList));
    }

    @Test(timeout = 4000)
    public void testGetProperties() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("configs", "timeout=30, retries=3");

        Properties subProps = props.getProperties("configs");
        assertEquals("30", subProps.getProperty("timeout"));
        assertEquals("3", subProps.getProperty("retries"));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetPropertiesInvalidTokenThrows() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("badConfig", "invalidTokenWithoutEquals");
        props.getProperties("badConfig");
    }

    // =========================================================================
    // Partition D: Interpolation & Recursive Loop Handling
    // =========================================================================

    @Test(timeout = 4000)
    public void testInterpolation() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("base.dir", "/opt/app");
        props.setProperty("log.dir", "${base.dir}/logs");
        props.setProperty("log.file", "${log.dir}/app.log");
        props.setProperty("undefined.var", "prefix_${unknown}_suffix");

        assertEquals("/opt/app/logs", props.getString("log.dir"));
        assertEquals("/opt/app/logs/app.log", props.getString("log.file"));
        assertEquals("prefix_${unknown}_suffix", props.getString("undefined.var"));
    }

    @Test(timeout = 4000)
    public void testInterpolationWithDefaultsFallback() {
        ExtendedProperties defaults = new ExtendedProperties();
        defaults.setProperty("defaultKey", "defaultValue");

        ExtendedProperties props = new ExtendedProperties();
        props.combine(defaults); // to mimic defaults
        props.setProperty("test", "${defaultKey}/custom");

        assertEquals("defaultValue/custom", props.getString("test"));
        assertNull(props.interpolate(null));
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testInterpolationInfiniteLoopThrows() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("var1", "${var2}");
        props.setProperty("var2", "${var1}");
        props.getString("var1");
    }

    // =========================================================================
    // Partition E: Load, Save, Include, and Stream Processing
    // =========================================================================

    @Test(timeout = 4000)
    public void testLoadFromStreamWithLineContinuationAndComments() throws IOException {
        String content = "# Comment line\n" +
                         "   # Indented comment\n" +
                         "\n" + // blank line
                         "simple = hello world\n" +
                         "multiline = line 1 \\\n" +
                         "            line 2 \\\n" +
                         "            line 3\n" +
                         "escapedBackslash = foo\\\\\n" + // ends with even slashes (not continuing)
                         "nextKey = bar\n" +
                         "emptyValue =\n";

        ExtendedProperties props = new ExtendedProperties();
        props.load(new ByteArrayInputStream(content.getBytes("ISO-8859-1")));

        assertEquals("hello world", props.getString("simple"));
        assertEquals("line 1 line 2 line 3", props.getString("multiline"));
        assertEquals("foo\\\\", props.getString("escapedBackslash"));
        assertEquals("bar", props.getString("nextKey"));
        assertNull(props.getProperty("emptyValue"));
    }

    @Test(timeout = 4000)
    public void testIncludeDirective() throws IOException {
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        File subFile = new File(tempDir, "extprop_test_sub.properties");
        subFile.deleteOnExit();

        FileOutputStream fos = new FileOutputStream(subFile);
        fos.write("sub.key = sub.val\n".getBytes("ISO-8859-1"));
        fos.close();

        ExtendedProperties props = new ExtendedProperties();
        // Load with absolute path include
        String content = "main.key = main.val\n" +
                         "include = " + subFile.getAbsolutePath() + "\n";
        props.load(new ByteArrayInputStream(content.getBytes("ISO-8859-1")));

        assertEquals("main.val", props.getString("main.key"));
        assertEquals("sub.val", props.getString("sub.key"));

        subFile.delete();
    }

    @Test(timeout = 4000)
    public void testIncludeSettingsAndBackwardsCompatibility() {
        ExtendedProperties props = new ExtendedProperties();
        // Default is "include"
        assertEquals("include", props.getInclude());

        props.setInclude("import");
        assertEquals("import", props.getInclude());

        props.setInclude(null);
        assertNull(props.getInclude());

        props.setInclude("");
        assertNull(props.getInclude());
    }

    @Test(timeout = 4000)
    public void testSaveProperties() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("greeting", "hello, world");
        props.addProperty("list", "item1");
        props.addProperty("list", "item2,escaped\\test");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        props.save(baos, "# My Header");

        String saved = new String(baos.toByteArray());
        assertTrue(saved.contains("# My Header"));
        assertTrue(saved.contains("greeting="));
        assertTrue(saved.contains("list="));

        // Save to null stream is safe no-op
        props.save(null, "Header");
    }

    @Test(timeout = 4000)
    public void testConvertProperties() {
        Properties p = new Properties();
        p.setProperty("k1", "v1");
        p.setProperty("k2", "v2");

        ExtendedProperties ep = ExtendedProperties.convertProperties(p);
        assertEquals("v1", ep.getString("k1"));
        assertEquals("v2", ep.getString("k2"));
    }

    @Test(timeout = 4000)
    public void testPutAndPutAllContract() {
        ExtendedProperties props = new ExtendedProperties();
        Object old = props.put("keyA", "valA");
        assertNull(old);
        assertEquals("valA", props.getString("keyA"));

        // Put returns old value
        old = props.put("keyA", "valA2");
        // ExtendedProperties.put delegates to addProperty which converts to a list
        assertNotNull(old);

        ExtendedProperties otherEp = new ExtendedProperties();
        otherEp.setProperty("keyB", "valB");
        otherEp.setProperty("keyC", "valC");

        props.putAll(otherEp);
        assertEquals("valB", props.getString("keyB"));
        assertEquals("valC", props.getString("keyC"));

        java.util.HashMap regularMap = new java.util.HashMap();
        regularMap.put("keyD", "valD");
        props.putAll(regularMap);
        assertEquals("valD", props.getString("keyD"));
    }

    @Test(timeout = 4000)
    public void testClassCastExceptionsOnInvalidTypeAccess() {
        ExtendedProperties props = new ExtendedProperties();
        props.put("dateKey", new java.util.Date());

        try {
            props.getString("dateKey");
            fail("Expected ClassCastException");
        } catch (ClassCastException expected) {}

        try {
            props.getStringArray("dateKey");
            fail("Expected ClassCastException");
        } catch (ClassCastException expected) {}

        try {
            props.getVector("dateKey");
            fail("Expected ClassCastException");
        } catch (ClassCastException expected) {}

        try {
            props.getList("dateKey");
            fail("Expected ClassCastException");
        } catch (ClassCastException expected) {}

        try {
            props.getBoolean("dateKey");
            fail("Expected ClassCastException");
        } catch (ClassCastException expected) {}

        try {
            props.getInteger("dateKey");
            fail("Expected ClassCastException");
        } catch (ClassCastException expected) {}

        try {
            props.getDouble("dateKey");
            fail("Expected ClassCastException");
        } catch (ClassCastException expected) {}
    }

    @Test(timeout = 4000)
    public void testDisplayDoesNotThrow() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("displayKey", "displayVal");
        props.display();
    }
}