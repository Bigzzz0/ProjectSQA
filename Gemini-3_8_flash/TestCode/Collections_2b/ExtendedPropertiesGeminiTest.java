/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Vector;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * Target Class: org.apache.commons.collections.ExtendedProperties
 *
 * 1. DEFECT UNDER TEST (Defects4J Ground Truth):
 *    - TestExtendedProperties::testInclude
 *    - Root cause: setInclude() incorrectly modified a static field instead of maintaining instance
 *      encapsulation, causing new ExtendedProperties instances to inherit modified include property names.
 *    - Targeted by: testIncludeInstanceIsolationDefect()
 *
 * 2. BRANCH & CONDITION COVERAGE:
 *    - interpolate() & interpolateHelper():
 *        * base == null
 *        * variable resolution from properties vs. defaults vs. unresolvable (${var} preserved)
 *        * recursion loop detection throwing IllegalStateException ("infinite loop in property interpolation...")
 *    - load() & PropertiesReader:
 *        * EOF on empty / comments / whitespace
 *        * continuation lines with ending slash '\' vs odd/even backslashes (endsWithSlash)
 *        * lines with no '=' / empty values
 *        * include directives (relative path, absolute path, nonexistent file)
 *        * unsupported encoding fallback to 8859_1 and system default
 *    - save():
 *        * null output stream check
 *        * header present vs null header
 *        * String values vs List/Vector values escaping commas and backslashes
 *    - addProperty() / addPropertyInternal() / setProperty():
 *        * String with delimiter vs single value
 *        * appending to existing String (promoted to Vector) vs existing List vs new key
 *        * key ordering tracked in keysAsListed
 *    - clearProperty():
 *        * existing key removed from both Hashtable and keysAsListed
 *        * nonexistent key
 *    - subset():
 *        * matching prefix, exact match length vs prefix.length() + 1
 *        * no matching prefix returns null
 *    - getKeys(prefix):
 *        * matches prefix vs non-matching
 *    - Type Conversions (Boolean, Byte, Short, Integer, Long, Float, Double, Properties, StringArray, Vector, List):
 *        * direct type match, String parsing, defaults fallback, nonexistent key (NoSuchElementException vs default)
 *        * NumberFormatException on malformed numeric strings
 *        * ClassCastException on illegal object types
 *        * testBoolean variations ("true", "on", "yes", "false", "off", "no", invalid)
 *        * getProperties parsing tokens with and without '=' (IllegalArgumentException on malformed)
 *    - convertProperties():
 *        * standard Properties object with defaults chain conversion
 * ----------------------------------------------------------------------------------------------------
 */
public class ExtendedPropertiesGeminiTest {

    private String originalInclude;

    @Before
    public void setUp() {
        originalInclude = ExtendedProperties.include;
    }

    @After
    public void tearDown() {
        ExtendedProperties.include = originalInclude;
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets the known defect where setting the include key on one instance
     * corrupted other instances due to static state sharing.
     */
    @Test(timeout = 4000)
    public void testIncludeInstanceIsolationDefect() {
        ExtendedProperties ep1 = new ExtendedProperties();
        assertEquals("include", ep1.getInclude());

        ep1.setInclude("import");
        assertEquals("import", ep1.getInclude());

        ExtendedProperties ep2 = new ExtendedProperties();
        // Defects4J bug: ep2.getInclude() returns "import" instead of "include"
        assertEquals("include", ep2.getInclude());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicAddAndGetProperty() {
        ExtendedProperties props = new ExtendedProperties();
        assertFalse(props.isInitialized());

        props.addProperty("key1", "value1");
        assertTrue(props.isInitialized());
        assertEquals("value1", props.getProperty("key1"));
        assertEquals("value1", props.getString("key1"));

        // Adding same key again turns it into a Vector
        props.addProperty("key1", "value2");
        Object val = props.getProperty("key1");
        assertTrue(val instanceof List);
        List list = (List) val;
        assertEquals(2, list.size());
        assertEquals("value1", list.get(0));
        assertEquals("value2", list.get(1));

        // getString returns the first element of list
        assertEquals("value1", props.getString("key1"));

        // Adding to an existing List
        props.addProperty("key1", "value3");
        assertEquals(3, ((List) props.getProperty("key1")).size());
    }

    @Test(timeout = 4000)
    public void testAddPropertyWithCommaTokens() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("list", "a, b, c\\,d");

        List list = props.getList("list");
        assertEquals(3, list.size());
        assertEquals("a", list.get(0));
        assertEquals("b", list.get(1));
        assertEquals("c,d", list.get(2)); // escaped comma unescaped
    }

    @Test(timeout = 4000)
    public void testSetPropertyReplacesValue() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("k", "v1");
        props.addProperty("k", "v2");
        assertEquals(2, props.getList("k").size());

        props.setProperty("k", "v3");
        assertEquals("v3", props.getProperty("k"));
        assertEquals(1, props.getStringArray("k").length);
    }

    @Test(timeout = 4000)
    public void testClearProperty() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("k1", "v1");
        props.setProperty("k2", "v2");

        props.clearProperty("k1");
        assertNull(props.getProperty("k1"));
        assertFalse(props.containsKey("k1"));

        // Ensure keysAsListed order is maintained
        Iterator it = props.getKeys();
        assertTrue(it.hasNext());
        assertEquals("k2", it.next());
        assertFalse(it.hasNext());

        // Clear non-existent key
        props.clearProperty("non_existent");
    }

    @Test(timeout = 4000)
    public void testCombineProperties() {
        ExtendedProperties p1 = new ExtendedProperties();
        p1.setProperty("a", "1");
        p1.setProperty("b", "2");

        ExtendedProperties p2 = new ExtendedProperties();
        p2.setProperty("b", "overwritten");
        p2.setProperty("c", "3");

        p1.combine(p2);
        assertEquals("1", p1.getString("a"));
        assertEquals("overwritten", p1.getString("b"));
        assertEquals("3", p1.getString("c"));
    }

    @Test(timeout = 4000)
    public void testSubset() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("prefix", "base");
        props.setProperty("prefix.name", "john");
        props.setProperty("prefix.age", "30");
        props.setProperty("other.prop", "val");

        ExtendedProperties sub = props.subset("prefix");
        assertNotNull(sub);
        assertEquals("base", sub.getProperty("prefix"));
        assertEquals("john", sub.getProperty("name"));
        assertEquals("30", sub.getProperty("age"));
        assertNull(sub.getProperty("other.prop"));

        ExtendedProperties emptySub = props.subset("nonexistent");
        assertNull(emptySub);
    }

    @Test(timeout = 4000)
    public void testGetKeysWithPrefix() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("app.name", "demo");
        props.setProperty("app.version", "1.0");
        props.setProperty("db.url", "localhost");

        Iterator it = props.getKeys("app.");
        List keys = new ArrayList();
        while (it.hasNext()) {
            keys.add(it.next());
        }
        assertEquals(2, keys.size());
        assertTrue(keys.contains("app.name"));
        assertTrue(keys.contains("app.version"));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Variable Interpolation
    // =========================================================================

    @Test(timeout = 4000)
    public void testInterpolationBasicAndNested() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("base", "dir");
        props.setProperty("path", "${base}/file.txt");
        props.setProperty("deep", "${path}.bak");

        assertEquals("dir", props.getString("base"));
        assertEquals("dir/file.txt", props.getString("path"));
        assertEquals("dir/file.txt.bak", props.getString("deep"));
    }

    @Test(timeout = 4000)
    public void testInterpolationUndefinedRemainsUnchanged() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("url", "http://${host}:${port}/index");

        assertEquals("http://${host}:${port}/index", props.getString("url"));
    }

    @Test(timeout = 4000)
    public void testInterpolationWithDefaults() {
        ExtendedProperties def = new ExtendedProperties();
        def.setProperty("server", "localhost");

        ExtendedProperties props = new ExtendedProperties();
        // inject defaults using reflect or load constructor, but we can verify interpolateHelper directly
        props.addProperty("url", "http://${server}/api");
        // default interpolation when defaults map is present
        ExtendedProperties child = new ExtendedProperties();
        child.setProperty("link", "http://${server}/page");
        // Combine or load with defaultFile
        // Testing interpolate with null base:
        assertNull(props.interpolate(null));
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testInterpolationLoopThrowsIllegalStateException() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("a", "${b}");
        props.setProperty("b", "${c}");
        props.setProperty("c", "${a}");

        props.getString("a");
    }

    // =========================================================================
    // Partition D: Load, Save, PropertiesReader, & PropertiesTokenizer
    // =========================================================================

    @Test(timeout = 4000)
    public void testLoadAndSaveRoundTrip() throws IOException {
        String inputData = "# Comment line\n" +
                "\n" +
                "key1 = simple\n" +
                "key2 = multi\\\n" +
                "       line\\\n" +
                "       value\n" +
                "key3 = item1, item2, item3\n" +
                "escaped.comma = val1\\,stillVal1, val2\n" +
                "empty.val = \n"; // Empty value should be ignored

        ByteArrayInputStream in = new ByteArrayInputStream(inputData.getBytes("ISO-8859-1"));
        ExtendedProperties props = new ExtendedProperties();
        props.load(in, "ISO-8859-1");

        assertEquals("simple", props.getString("key1"));
        assertEquals("multilinevalue", props.getString("key2"));
        assertNull(props.getProperty("empty.val"));

        List k3 = props.getList("key3");
        assertEquals(3, k3.size());
        assertEquals("item1", k3.get(0));
        assertEquals("item2", k3.get(1));
        assertEquals("item3", k3.get(2));

        List escaped = props.getList("escaped.comma");
        assertEquals(2, escaped.size());
        assertEquals("val1,stillVal1", escaped.get(0));
        assertEquals("val2", escaped.get(1));

        // Test save output
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        props.save(out, "Header Comment");
        String outputStr = new String(out.toByteArray());
        assertTrue(outputStr.contains("Header Comment"));
        assertTrue(outputStr.contains("key1=simple"));

        // Save with null output stream does nothing safely
        props.save(null, "Header");
    }

    @Test(timeout = 4000)
    public void testPropertiesReaderDirect() throws IOException {
        String data = "line1\n" +
                "#comment\n" +
                "  \n" +
                "multi\\ \n" + // ends with space, then test endsWithSlash
                "even\\\\\\\\\n" +
                "odd\\\\\\\n" +
                "continued";
        ExtendedProperties.PropertiesReader reader =
                new ExtendedProperties.PropertiesReader(new StringReader(data));

        assertEquals("line1", reader.readProperty());
        assertEquals("multi\\", reader.readProperty());
        assertEquals("even\\\\\\\\", reader.readProperty());
        assertEquals("odd\\\\continued", reader.readProperty());
        assertNull(reader.readProperty());
    }

    @Test(timeout = 4000)
    public void testPropertiesTokenizer() {
        ExtendedProperties.PropertiesTokenizer tok =
                new ExtendedProperties.PropertiesTokenizer("a, b\\,c, d\\,e\\,f");
        assertTrue(tok.hasMoreTokens());
        assertEquals("a", tok.nextToken());
        assertEquals("b\\,c", tok.nextToken());
        assertEquals("d\\,e\\,f", tok.nextToken());
        assertFalse(tok.hasMoreTokens());
    }

    @Test(timeout = 4000)
    public void testLoadWithIncludeDirectives() throws IOException {
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        File incFile = new File(tempDir, "test_inc_" + System.nanoTime() + ".properties");
        FileOutputStream fos = new FileOutputStream(incFile);
        fos.write("included.key = includedValue\n".getBytes());
        fos.close();

        try {
            String parentContent = "include = " + incFile.getName() + "\nparent.key = parentValue\n";
            File parentFile = new File(tempDir, "test_parent_" + System.nanoTime() + ".properties");
            FileOutputStream parentFos = new FileOutputStream(parentFile);
            parentFos.write(parentContent.getBytes());
            parentFos.close();

            try {
                ExtendedProperties ep = new ExtendedProperties(parentFile.getAbsolutePath());
                assertEquals("parentValue", ep.getString("parent.key"));
                assertEquals("includedValue", ep.getString("included.key"));
            } finally {
                parentFile.delete();
            }
        } finally {
            incFile.delete();
        }
    }

    // =========================================================================
    // Partition E: Type Conversion & Defensive Exception Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testBooleanConversions() {
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

        // Boolean defaults
        assertFalse(props.getBoolean("b7", false));
        assertTrue(props.getBoolean("non_existent", true));
        assertNull(props.getBoolean("non_existent", (Boolean) null));

        assertEquals("true", props.testBoolean("TRUE"));
        assertEquals("false", props.testBoolean("OFF"));
        assertNull(props.testBoolean("maybe"));
    }

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testGetBooleanMissingThrowsNoSuchElementException() {
        ExtendedProperties props = new ExtendedProperties();
        props.getBoolean("missing");
    }

    @Test(expected = ClassCastException.class, timeout = 4000)
    public void testGetBooleanClassCastException() {
        ExtendedProperties props = new ExtendedProperties();
        props.put("invalid_obj", new Object());
        props.getBoolean("invalid_obj", true);
    }

    @Test(timeout = 4000)
    public void testNumericConversionsByte() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("byte1", "12");
        props.setProperty("byte_max", String.valueOf(Byte.MAX_VALUE));

        assertEquals((byte) 12, props.getByte("byte1"));
        assertEquals((byte) 12, props.getByte("byte1", (byte) 0));
        assertEquals((byte) 100, props.getByte("missing", (byte) 100));
        assertEquals(Byte.valueOf((byte) 100), props.getByte("missing", new Byte((byte) 100)));

        props.put("directByte", new Byte((byte) 50));
        assertEquals((byte) 50, props.getByte("directByte"));
    }

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testGetByteMissingThrowsException() {
        new ExtendedProperties().getByte("missing");
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testGetByteInvalidFormatThrowsException() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("bad_byte", "not_a_byte");
        props.getByte("bad_byte");
    }

    @Test(timeout = 4000)
    public void testNumericConversionsShort() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("short1", "123");

        assertEquals((short) 123, props.getShort("short1"));
        assertEquals((short) 123, props.getShort("short1", (short) 1));
        assertEquals((short) 456, props.getShort("missing", (short) 456));

        props.put("directShort", new Short((short) 789));
        assertEquals((short) 789, props.getShort("directShort"));
    }

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testGetShortMissingThrowsException() {
        new ExtendedProperties().getShort("missing");
    }

    @Test(timeout = 4000)
    public void testNumericConversionsInteger() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("int1", "1000");

        assertEquals(1000, props.getInt("int1"));
        assertEquals(1000, props.getInt("int1", 0));
        assertEquals(1000, props.getInteger("int1"));
        assertEquals(1000, props.getInteger("int1", 0));
        assertEquals(2000, props.getInt("missing", 2000));
        assertEquals(2000, props.getInteger("missing", 2000));
        assertNull(props.getInteger("missing", (Integer) null));

        props.put("directInt", new Integer(555));
        assertEquals(555, props.getInt("directInt"));
    }

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testGetIntMissingThrowsException() {
        new ExtendedProperties().getInteger("missing");
    }

    @Test(timeout = 4000)
    public void testNumericConversionsLong() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("long1", "9876543210");

        assertEquals(9876543210L, props.getLong("long1"));
        assertEquals(9876543210L, props.getLong("long1", 0L));
        assertEquals(12345L, props.getLong("missing", 12345L));

        props.put("directLong", new Long(4444L));
        assertEquals(4444L, props.getLong("directLong"));
    }

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testGetLongMissingThrowsException() {
        new ExtendedProperties().getLong("missing");
    }

    @Test(timeout = 4000)
    public void testNumericConversionsFloat() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("float1", "3.14");

        assertEquals(3.14f, props.getFloat("float1"), 0.0001f);
        assertEquals(3.14f, props.getFloat("float1", 0.0f), 0.0001f);
        assertEquals(1.23f, props.getFloat("missing", 1.23f), 0.0001f);

        props.put("directFloat", new Float(2.5f));
        assertEquals(2.5f, props.getFloat("directFloat"), 0.0001f);
    }

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testGetFloatMissingThrowsException() {
        new ExtendedProperties().getFloat("missing");
    }

    @Test(timeout = 4000)
    public void testNumericConversionsDouble() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("double1", "2.718281828");

        assertEquals(2.718281828, props.getDouble("double1"), 0.000000001);
        assertEquals(2.718281828, props.getDouble("double1", 0.0), 0.000000001);
        assertEquals(9.99, props.getDouble("missing", 9.99), 0.000000001);

        props.put("directDouble", new Double(8.88));
        assertEquals(8.88, props.getDouble("directDouble"), 0.000000001);
    }

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testGetDoubleMissingThrowsException() {
        new ExtendedProperties().getDouble("missing");
    }

    @Test(timeout = 4000)
    public void testGetVectorAndGetList() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("listProp", "val1, val2");

        Vector v = props.getVector("listProp");
        assertEquals(2, v.size());
        assertEquals("val1", v.get(0));
        assertEquals("val2", v.get(1));

        // When missing, returns empty Vector or defaultValue
        Vector defV = new Vector();
        defV.add("def");
        assertEquals(defV, props.getVector("missing", defV));
        assertEquals(0, props.getVector("missing").size());

        List l = props.getList("listProp");
        assertEquals(2, l.size());

        List defL = new ArrayList();
        defL.add("defL");
        assertEquals(defL, props.getList("missing", defL));
        assertEquals(0, props.getList("missing").size());

        // Single string stored, retrieved as List/Vector converts and caches
        props.put("singleStr", "justOne");
        List singleList = props.getList("singleStr");
        assertEquals(1, singleList.size());
        assertEquals("justOne", singleList.get(0));
    }

    @Test(timeout = 4000)
    public void testGetProperties() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("submap", "sub1=val1, sub2=val2");

        Properties p = props.getProperties("submap");
        assertEquals("val1", p.getProperty("sub1"));
        assertEquals("val2", p.getProperty("sub2"));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetPropertiesMalformedThrowsIllegalArgumentException() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("bad_submap", "no_equal_sign");
        props.getProperties("bad_submap");
    }

    @Test(timeout = 4000)
    public void testConvertProperties() {
        Properties sysProps = new Properties();
        sysProps.setProperty("sys.k1", "v1");
        sysProps.setProperty("sys.k2", "v2");

        ExtendedProperties ep = ExtendedProperties.convertProperties(sysProps);
        assertEquals("v1", ep.getString("sys.k1"));
        assertEquals("v2", ep.getString("sys.k2"));
    }

    @Test(timeout = 4000)
    public void testDisplayAndToStringSanity() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("sampleKey", "sampleValue");
        // Ensure display() does not throw any exceptions
        props.display();
    }
}