/* [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.collections.ExtendedProperties
 *
 * 1. Defect-Targeted Zone:
 *    - COLLECTIONS-299: ExtendedProperties.convertProperties(Properties) throws NullPointerException
 *      when encountering non-String property values in java.util.Properties because Properties.getProperty(key)
 *      returns null for non-String values, and ExtendedProperties.put(key, null) delegates to Hashtable.put(key, null).
 *
 * 2. Decision Branches & Boundary Conditions:
 *    - Constructors: empty, single file, file + defaultFile, basePath calculation.
 *    - Interpolation: null base, base without tokens, base with tokens, recursive interpolation,
 *      infinite loop detection (IllegalStateException with cycle path), default repository lookup,
 *      unresolved token retention.
 *    - Reader / Parsing:
 *      * comments (#), blank lines, continuation lines with trailing backslashes (endsWithSlash / countPreceding).
 *      * escaped characters: backslashes (\\), commas (\,).
 *      * include handling: getInclude / setInclude (null, empty, custom), absolute path, relative path ("./" stripped).
 *    - Type Conversions & Accessors:
 *      * getString, getStringArray, getVector, getList, getProperties.
 *      * getBoolean / testBoolean ("true", "on", "yes", "false", "off", "no", case insensitivity, defaults).
 *      * numeric: getByte, getShort, getInt, getInteger, getLong, getFloat, getDouble (direct type, string parsed, defaults).
 *      * error paths: NoSuchElementException on missing key, NumberFormatException on bad format, ClassCastException on mismatched types.
 *    - Mutations & Hierarchy:
 *      * addProperty (String with commas, escaped commas, non-String, single to Vector migration, List appending).
 *      * setProperty, clearProperty, combine, subset (exact prefix match vs prefix + suffix, null on no matches).
 *      * put, putAll (ExtendedProperties vs generic Map), remove.
 *      * save (null stream, headers, List vs String value escaping).
 */

package org.apache.commons.collections;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Vector;

import static org.junit.Assert.*;

public class ExtendedPropertiesGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (COLLECTIONS-299)
    // =========================================================================

    /**
     * Targets COLLECTIONS-299: ExtendedProperties.convertProperties(Properties) throws
     * NullPointerException when java.util.Properties contains a non-String value.
     */
    @Test(timeout = 4000)
    public void testCollections299NonStringPropertyConversion() {
        Properties props = new Properties();
        props.put("validKey", "validValue");
        // Non-String values cause props.getProperty("nonStringKey") to return null
        props.put("nonStringKey", new Object());
        props.put("intKey", 12345);

        ExtendedProperties ep = ExtendedProperties.convertProperties(props);
        assertNotNull("Converted properties must not be null", ep);
        assertEquals("validValue", ep.getString("validKey"));
        assertFalse("Non-String properties should not be stored as null or crash conversion", ep.containsKey("nonStringKey"));
        assertFalse("Non-String properties should not be stored as null or crash conversion", ep.containsKey("intKey"));
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicAddAndRetrieveProperties() {
        ExtendedProperties ep = new ExtendedProperties();
        assertFalse(ep.isInitialized());

        ep.addProperty("key1", "value1");
        assertTrue(ep.isInitialized());
        assertEquals("value1", ep.getProperty("key1"));
        assertEquals("value1", ep.getString("key1"));

        // Comma-separated strings should be vectorized
        ep.addProperty("listKey", "one, two, three");
        List list = ep.getList("listKey");
        assertEquals(3, list.size());
        assertEquals("one", list.get(0));
        assertEquals("two", list.get(1));
        assertEquals("three", list.get(2));

        // Multiple adds with the same key
        ep.addProperty("multiKey", "val1");
        ep.addProperty("multiKey", "val2");
        Object multiVal = ep.getProperty("multiKey");
        assertTrue(multiVal instanceof List);
        List multiList = (List) multiVal;
        assertEquals(2, multiList.size());
        assertEquals("val1", multiList.get(0));
        assertEquals("val2", multiList.get(1));
    }

    @Test(timeout = 4000)
    public void testLoadFromStreamWithEscapedCommasAndContinuations() throws IOException {
        String configData =
                "# Leading comment\n" +
                "simple = hello\n" +
                "continued = line 1 \\\n" +
                "line 2\n" +
                "escaped.comma = Hello\\, World, second\n" +
                "double.slash = C:\\\\Path\\\\To\\\\File\n";

        ExtendedProperties ep = new ExtendedProperties();
        ep.load(new ByteArrayInputStream(configData.getBytes(StandardCharsets.ISO_8859_1)));

        assertEquals("hello", ep.getString("simple"));
        assertEquals("line 1 line 2", ep.getString("continued"));

        List escapedList = ep.getList("escaped.comma");
        assertEquals(2, escapedList.size());
        assertEquals("Hello, World", escapedList.get(0));
        assertEquals("second", escapedList.get(1));

        assertEquals("C:\\Path\\To\\File", ep.getString("double.slash"));
    }

    @Test(timeout = 4000)
    public void testIncludeDirective() throws IOException {
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        File subFile = new File(tempDir, "ext_prop_sub_" + System.nanoTime() + ".properties");
        File mainFile = new File(tempDir, "ext_prop_main_" + System.nanoTime() + ".properties");

        try {
            PrintWriter subWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(subFile), StandardCharsets.ISO_8859_1));
            subWriter.println("subKey = subValue");
            subWriter.close();

            PrintWriter mainWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(mainFile), StandardCharsets.ISO_8859_1));
            mainWriter.println("mainKey = mainValue");
            // Test relative include using dot slash
            mainWriter.println("include = ./" + subFile.getName());
            mainWriter.close();

            ExtendedProperties ep = new ExtendedProperties(mainFile.getAbsolutePath());
            assertEquals("mainValue", ep.getString("mainKey"));
            assertEquals("subValue", ep.getString("subKey"));
        } finally {
            if (subFile.exists()) subFile.delete();
            if (mainFile.exists()) mainFile.delete();
        }
    }

    @Test(timeout = 4000)
    public void testInterpolationRecursiveAndDefaults() {
        ExtendedProperties defaults = new ExtendedProperties();
        defaults.addProperty("defVar", "fromDefault");
        defaults.addProperty("baseDef", "def_${defVar}");

        ExtendedProperties ep = new ExtendedProperties();
        ep.combine(defaults);
        ep.addProperty("greeting", "Hello");
        ep.addProperty("target", "World");
        ep.addProperty("message", "${greeting}, ${target}!");
        ep.addProperty("nested", "Message is: ${message}");
        ep.addProperty("unresolved", "Value: ${unknown}");

        assertEquals("Hello, World!", ep.getString("message"));
        assertEquals("Message is: Hello, World!", ep.getString("nested"));
        assertEquals("Value: ${unknown}", ep.getString("unresolved"));
        assertEquals("def_fromDefault", ep.getString("baseDef"));
    }

    @Test(timeout = 4000)
    public void testGetKeysAndPrefixFiltering() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("service.name", "billing");
        ep.addProperty("service.port", "8080");
        ep.addProperty("client.timeout", "5000");

        Iterator allKeys = ep.getKeys();
        List keyList = new ArrayList();
        while (allKeys.hasNext()) {
            keyList.add(allKeys.next());
        }
        assertEquals(3, keyList.size());
        assertEquals("service.name", keyList.get(0));
        assertEquals("service.port", keyList.get(1));
        assertEquals("client.timeout", keyList.get(2));

        Iterator serviceKeys = ep.getKeys("service");
        List serviceList = new ArrayList();
        while (serviceKeys.hasNext()) {
            serviceList.add(serviceKeys.next());
        }
        assertEquals(2, serviceList.size());
        assertTrue(serviceList.contains("service.name"));
        assertTrue(serviceList.contains("service.port"));
    }

    @Test(timeout = 4000)
    public void testSubsetOperation() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("database", "postgres");
        ep.addProperty("database.host", "localhost");
        ep.addProperty("database.port", "5432");
        ep.addProperty("other.prop", "val");

        ExtendedProperties sub = ep.subset("database");
        assertNotNull(sub);
        assertEquals("postgres", sub.getProperty("database"));
        assertEquals("localhost", sub.getProperty("host"));
        assertEquals("5432", sub.getProperty("port"));
        assertNull(sub.getProperty("other.prop"));

        ExtendedProperties emptySub = ep.subset("nonexistent");
        assertNull(emptySub);
    }

    @Test(timeout = 4000)
    public void testSaveAndReload() throws IOException {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key1", "val,with,commas");
        ep.addProperty("key2", "simple");
        ep.addProperty("multi", "a");
        ep.addProperty("multi", "b");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ep.save(out, "Header Comment");

        ExtendedProperties loaded = new ExtendedProperties();
        loaded.load(new ByteArrayInputStream(out.toByteArray()));

        assertEquals("val,with,commas", loaded.getString("key1"));
        assertEquals("simple", loaded.getString("key2"));
        List multiList = loaded.getList("multi");
        assertEquals(2, multiList.size());
        assertEquals("a", multiList.get(0));
        assertEquals("b", multiList.get(1));

        // Test save with null output stream
        ep.save(null, "Header");
    }

    @Test(timeout = 4000)
    public void testGetPropertiesKeyAndSubProperties() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("subProps", "prop1=val1, prop2=val2, prop3=val3");

        Properties props = ep.getProperties("subProps");
        assertEquals("val1", props.getProperty("prop1"));
        assertEquals("val2", props.getProperty("prop2"));
        assertEquals("val3", props.getProperty("prop3"));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Type Conversions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBooleanConversions() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("bool.true1", "true");
        ep.addProperty("bool.true2", "on");
        ep.addProperty("bool.true3", "yes");
        ep.addProperty("bool.false1", "false");
        ep.addProperty("bool.false2", "off");
        ep.addProperty("bool.false3", "no");
        ep.addProperty("bool.literal", Boolean.TRUE);

        assertTrue(ep.getBoolean("bool.true1"));
        assertTrue(ep.getBoolean("bool.true2"));
        assertTrue(ep.getBoolean("bool.true3"));
        assertFalse(ep.getBoolean("bool.false1"));
        assertFalse(ep.getBoolean("bool.false2"));
        assertFalse(ep.getBoolean("bool.false3"));
        assertTrue(ep.getBoolean("bool.literal"));

        // Defaults
        assertTrue(ep.getBoolean("missing", true));
        assertFalse(ep.getBoolean("missing", false));
        assertEquals(Boolean.TRUE, ep.getBoolean("missing", Boolean.TRUE));
        assertNull(ep.getBoolean("missing", (Boolean) null));

        // testBoolean direct helper
        assertEquals("true", ep.testBoolean("YES"));
        assertEquals("true", ep.testBoolean("On"));
        assertEquals("false", ep.testBoolean("Off"));
        assertEquals("false", ep.testBoolean("NO"));
        assertNull(ep.testBoolean("not_a_boolean"));
    }

    @Test(timeout = 4000)
    public void testNumericConversionsAndBoundaries() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("b.min", String.valueOf(Byte.MIN_VALUE));
        ep.addProperty("b.max", String.valueOf(Byte.MAX_VALUE));
        ep.addProperty("s.min", String.valueOf(Short.MIN_VALUE));
        ep.addProperty("s.max", String.valueOf(Short.MAX_VALUE));
        ep.addProperty("i.min", String.valueOf(Integer.MIN_VALUE));
        ep.addProperty("i.max", String.valueOf(Integer.MAX_VALUE));
        ep.addProperty("l.min", String.valueOf(Long.MIN_VALUE));
        ep.addProperty("l.max", String.valueOf(Long.MAX_VALUE));
        ep.addProperty("f.val", "3.14159");
        ep.addProperty("d.val", "2.718281828459045");

        assertEquals(Byte.MIN_VALUE, ep.getByte("b.min"));
        assertEquals(Byte.MAX_VALUE, ep.getByte("b.max"));
        assertEquals((byte) 5, ep.getByte("b.missing", (byte) 5));
        assertEquals(new Byte((byte) 7), ep.getByte("b.missing", new Byte((byte) 7)));

        assertEquals(Short.MIN_VALUE, ep.getShort("s.min"));
        assertEquals(Short.MAX_VALUE, ep.getShort("s.max"));
        assertEquals((short) 10, ep.getShort("s.missing", (short) 10));
        assertEquals(new Short((short) 12), ep.getShort("s.missing", new Short((short) 12)));

        assertEquals(Integer.MIN_VALUE, ep.getInt("i.min"));
        assertEquals(Integer.MAX_VALUE, ep.getInteger("i.max").intValue());
        assertEquals(20, ep.getInt("i.missing", 20));
        assertEquals(new Integer(25), ep.getInteger("i.missing", new Integer(25)));

        assertEquals(Long.MIN_VALUE, ep.getLong("l.min"));
        assertEquals(Long.MAX_VALUE, ep.getLong("l.max"));
        assertEquals(30L, ep.getLong("l.missing", 30L));
        assertEquals(new Long(35L), ep.getLong("l.missing", new Long(35L)));

        assertEquals(3.14159f, ep.getFloat("f.val"), 0.00001f);
        assertEquals(1.23f, ep.getFloat("f.missing", 1.23f), 0.001f);
        assertEquals(new Float(4.56f), ep.getFloat("f.missing", new Float(4.56f)));

        assertEquals(2.718281828459045, ep.getDouble("d.val"), 0.0000000001);
        assertEquals(9.87, ep.getDouble("d.missing", 9.87), 0.001);
        assertEquals(new Double(6.54), ep.getDouble("d.missing", new Double(6.54)));
    }

    @Test(timeout = 4000)
    public void testCollectionsGettersVectorAndList() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("single", "value");
        ep.addProperty("items", "a, b, c");

        // getStringArray
        String[] arr1 = ep.getStringArray("single");
        assertArrayEquals(new String[]{"value"}, arr1);
        String[] arr2 = ep.getStringArray("items");
        assertArrayEquals(new String[]{"a", "b", "c"}, arr2);
        String[] emptyArr = ep.getStringArray("nonexistent");
        assertEquals(0, emptyArr.length);

        // getVector
        Vector v1 = ep.getVector("single");
        assertEquals(1, v1.size());
        assertEquals("value", v1.get(0));
        Vector v2 = ep.getVector("items");
        assertEquals(3, v2.size());
        Vector defV = new Vector();
        defV.add("default");
        assertEquals(defV, ep.getVector("nonexistent", defV));
        assertNotNull(ep.getVector("nonexistent", null));

        // getList
        List l1 = ep.getList("single");
        assertEquals(1, l1.size());
        assertEquals("value", l1.get(0));
        List defL = new ArrayList();
        defL.add("default");
        assertEquals(defL, ep.getList("nonexistent", defL));
        assertNotNull(ep.getList("nonexistent", null));
    }

    @Test(timeout = 4000)
    public void testIncludePropertyNameMutations() {
        ExtendedProperties ep = new ExtendedProperties();
        assertEquals("include", ep.getInclude());

        ep.setInclude("custom_include");
        assertEquals("custom_include", ep.getInclude());

        ep.setInclude("");
        assertNull(ep.getInclude());

        ep.setInclude(null);
        assertNull(ep.getInclude());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testInterpolationInfiniteLoopThrowsIllegalStateException() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("a", "${b}");
        ep.addProperty("b", "${c}");
        ep.addProperty("c", "${a}");
        ep.getString("a");
    }

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testGetBooleanMissingKeyThrowsNoSuchElementException() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.getBoolean("nonexistent");
    }

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testGetByteMissingKeyThrowsNoSuchElementException() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.getByte("nonexistent");
    }

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testGetShortMissingKeyThrowsNoSuchElementException() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.getShort("nonexistent");
    }

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testGetIntegerMissingKeyThrowsNoSuchElementException() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.getInteger("nonexistent");
    }

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testGetLongMissingKeyThrowsNoSuchElementException() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.getLong("nonexistent");
    }

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testGetFloatMissingKeyThrowsNoSuchElementException() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.getFloat("nonexistent");
    }

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testGetDoubleMissingKeyThrowsNoSuchElementException() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.getDouble("nonexistent");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testNumberFormatExceptionOnInvalidNumber() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("badNum", "not_a_number");
        ep.getInt("badNum");
    }

    @Test(expected = ClassCastException.class, timeout = 4000)
    public void testClassCastExceptionOnIncompatibleType() {
        ExtendedProperties ep = new ExtendedProperties();
        // Store an arbitrary Object that is not String/Number/Boolean/List
        ep.put("customObj", new Object());
        ep.getString("customObj");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetPropertiesMalformedTokenThrowsIllegalArgumentException() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("malformed", "keyWithoutEqualsSign");
        ep.getProperties("malformed");
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Map Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testPutPutAllAndRemove() {
        ExtendedProperties ep1 = new ExtendedProperties();
        ep1.put("k1", "v1");
        assertEquals("v1", ep1.getString("k1"));

        Map map = new HashMap();
        map.put("k2", "v2");
        map.put("k3", "v3");
        ep1.putAll(map);
        assertEquals("v2", ep1.getString("k2"));
        assertEquals("v3", ep1.getString("k3"));

        ExtendedProperties ep2 = new ExtendedProperties();
        ep2.put("k4", "v4");
        ep1.putAll(ep2);
        assertEquals("v4", ep1.getString("k4"));

        Object removed = ep1.remove("k1");
        assertEquals("v1", removed);
        assertNull(ep1.getProperty("k1"));

        // Clear and rebuild keysAsListed
        ep1.clearProperty("k2");
        assertFalse(ep1.containsKey("k2"));
    }

    @Test(timeout = 4000)
    public void testSetPropertyOverwritesExisting() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", "val1");
        ep.addProperty("key", "val2");
        assertEquals(2, ep.getList("key").size());

        ep.setProperty("key", "val3");
        assertEquals("val3", ep.getString("key"));
        assertEquals(1, ep.getList("key").size());
    }

    @Test(timeout = 4000)
    public void testDisplayDoesNotThrow() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("prop1", "val1");
        ep.addProperty("prop2", "val2");
        ep.display(); // Writes to System.out, ensure no crash occurs
    }

    @Test(timeout = 4000)
    public void testFileConstructorsWithDefaults() throws IOException {
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        File defFile = new File(tempDir, "ext_prop_defaults_" + System.nanoTime() + ".properties");
        File primaryFile = new File(tempDir, "ext_prop_primary_" + System.nanoTime() + ".properties");

        try {
            PrintWriter defWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(defFile), StandardCharsets.ISO_8859_1));
            defWriter.println("defaultKey = defaultValue");
            defWriter.println("sharedKey = fromDefault");
            defWriter.close();

            PrintWriter priWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(primaryFile), StandardCharsets.ISO_8859_1));
            priWriter.println("primaryKey = primaryValue");
            priWriter.println("sharedKey = fromPrimary");
            priWriter.close();

            ExtendedProperties ep = new ExtendedProperties(primaryFile.getAbsolutePath(), defFile.getAbsolutePath());
            assertEquals("primaryValue", ep.getString("primaryKey"));
            assertEquals("fromPrimary", ep.getString("sharedKey"));
            assertEquals("defaultValue", ep.getString("defaultKey"));
        } finally {
            if (defFile.exists()) defFile.delete();
            if (primaryFile.exists()) primaryFile.delete();
        }
    }
}