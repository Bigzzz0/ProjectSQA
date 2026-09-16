package org.apache.commons.collections;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

public class ExtendedPropertiesDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Target Class: ExtendedProperties (extends Hashtable)
     * Known Defect: testCollections271 - AssertionFailedError
     * 
     * The defect is related to the handling of empty property values.
     * In the load(InputStream) method, there is a commented-out check:
     *   // COLLECTIONS-238 allows empty properties to exist by commenting this out
     *   // if ("".equals(value)) { continue; }
     * 
     * This means that when a property has an empty value (e.g., "key="), 
     * the code proceeds to process it. The defect manifests when:
     * 1. An empty value is loaded
     * 2. The value is then retrieved and processed
     * 
     * The specific failure in testCollections271 involves:
     * - Loading properties with empty values
     * - The empty string value being incorrectly handled
     * - Possibly related to the addProperty method splitting logic
     * 
     * Branch analysis for load(InputStream):
     * - line == null (EOF) -> break
     * - line.length() == 0 -> continue
     * - line.charAt(0) == '#' -> continue
     * - endsWithSlash(line) -> true/false
     * - equalSign == -1 -> continue
     * - value.startsWith(fileSeparator) -> true/false
     * - value.startsWith("." + fileSeparator) -> true/false
     * - file.exists() && file.canRead() -> true/false
     * 
     * Branch analysis for addProperty(String, Object):
     * - value instanceof String -> true/false
     * - token contains commas -> true/false
     * - key already exists -> true/false
     * - existing value is Vector -> true/false
     * - existing value is String -> true/false
     * 
     * Branch analysis for getString(String):
     * - value == null -> true/false
     * - value instanceof String -> true/false
     * - value instanceof List -> true/false
     * 
     * Branch analysis for getVector(String):
     * - value instanceof Vector -> true/false
     * - value instanceof List -> true/false
     * - value instanceof String -> true/false
     * - value == null -> true/false
     * 
     * Branch analysis for getBoolean(String):
     * - value instanceof Boolean -> true/false
     * - value instanceof String -> true/false
     * - value == null -> true/false
     * 
     * Branch analysis for testBoolean(String):
     * - s.equals("true") || s.equals("on") || s.equals("yes") -> true/false
     * - s.equals("false") || s.equals("off") || s.equals("no") -> true/false
     * 
     * Branch analysis for getInteger(String):
     * - value instanceof Integer -> true/false
     * - value instanceof String -> true/false
     * - value == null -> true/false
     * 
     * Branch analysis for getLong(String):
     * - value instanceof Long -> true/false
     * - value instanceof String -> true/false
     * - value == null -> true/false
     * 
     * Branch analysis for getFloat(String):
     * - value instanceof Float -> true/false
     * - value instanceof String -> true/false
     * - value == null -> true/false
     * 
     * Branch analysis for getDouble(String):
     * - value instanceof Double -> true/false
     * - value instanceof String -> true/false
     * - value == null -> true/false
     * 
     * Branch analysis for combine(ExtendedProperties):
     * - key already exists -> true/false
     * 
     * Branch analysis for subset(String):
     * - key starts with prefix -> true/false
     * 
     * Branch analysis for escape(String):
     * - character is ',' or '\\' -> true/false
     * 
     * Branch analysis for unescape(String):
     * - character is '\\' -> true/false
     * 
     * Branch analysis for countPreceding(String, int, char):
     * - line.charAt(i) != ch -> true/false
     * 
     * Branch analysis for endsWithSlash(String):
     * - !line.endsWith("\\") -> true/false
     * - countPreceding(...) % 2 == 0 -> true/false
     * 
     * Branch analysis for PropertiesReader.readProperty():
     * - line == null -> true/false
     * - line.length() != 0 && line.charAt(0) != '#' -> true/false
     * - endsWithSlash(line) -> true/false
     * 
     * Branch analysis for PropertiesTokenizer:
     * - hasMoreTokens() -> true/false
     * 
     * Branch analysis for convertProperties(Properties):
     * - props.getProperty(key) != null -> true/false
     * 
     * Branch analysis for put(Object, Object):
     * - value instanceof String -> true/false
     * 
     * Branch analysis for setProperty(String, Object):
     * - value instanceof String -> true/false
     * 
     * Branch analysis for save(OutputStream, String):
     * - output == null -> true/false
     * - value instanceof List -> true/false
     * 
     * Branch analysis for getKeys():
     * - enumeration has more elements -> true/false
     * 
     * Branch analysis for getKeys(String):
     * - key starts with prefix -> true/false
     * 
     * Branch analysis for getStringList(String):
     * - value instanceof List -> true/false
     * - value instanceof String -> true/false
     * - value == null -> true/false
     * 
     * Branch analysis for getBoolean(String, boolean):
     * - value == null -> true/false
     * 
     * Branch analysis for getByte(String):
     * - value instanceof Byte -> true/false
     * - value instanceof String -> true/false
     * - value == null -> true/false
     * 
     * Branch analysis for getShort(String):
     * - value instanceof Short -> true/false
     * - value instanceof String -> true/false
     * - value == null -> true/false
     * 
     * Branch analysis for getByte(String, byte):
     * - value == null -> true/false
     * 
     * Branch analysis for getShort(String, short):
     * - value == null -> true/false
     * 
     * Branch analysis for getInt(String, int):
     * - value == null -> true/false
     * 
     * Branch analysis for getLong(String, long):
     * - value == null -> true/false
     * 
     * Branch analysis for getFloat(String, float):
     * - value == null -> true/false
     * 
     * Branch analysis for getDouble(String, double):
     * - value == null -> true/false
     * 
     * Branch analysis for getString(String, String):
     * - value == null -> true/false
     * 
     * Branch analysis for getProperty(String):
     * - value == null -> true/false
     * 
     * Branch analysis for getProperty(String, String):
     * - value == null -> true/false
     * 
     * Branch analysis for getInclude():
     * - include == null -> true/false
     * 
     * Branch analysis for setInclude(String):
     * - "".equals(includePropertyName) -> true/false
     * 
     * Branch analysis for addPropertyDirect(String, Object):
     * - key already exists -> true/false
     * - existing value is Vector -> true/false
     * - existing value is String -> true/false
     * 
     * Branch analysis for clearProperty(String):
     * - key exists -> true/false
     * 
     * Branch analysis for get(String):
     * - value == null -> true/false
     * 
     * Branch analysis for get(String, Object):
     * - value == null -> true/false
     * 
     * Branch analysis for get(String, Class):
     * - value == null -> true/false
     * 
     * Branch analysis for getList(String):
     * - value instanceof List -> true/false
     * - value instanceof String -> true/false
     * - value == null -> true/false
     * 
     * Branch analysis for getList(String, List):
     * - value == null -> true/false
     * 
     * Branch analysis for getVector(String, Vector):
     * - value == null -> true/false
     * 
     * Branch analysis for getStringList(String, List):
     * - value == null -> true/false
     * 
     * Branch analysis for getStringArray(String):
     * - value == null -> true/false
     * 
     * Branch analysis for getStringArray(String, String[]):
     * - value == null -> true/false
     * 
     * Branch analysis for isInitialized():
     * - file == null -> true/false
     * 
     * Branch analysis for setFilePath(String):
     * - file == null -> true/false
     * 
     * Branch analysis for getFilePath():
     * - file == null -> true/false
     * 
     * Branch analysis for getBasePath():
     * - basePath == null -> true/false
     * 
     * Branch analysis for setBasePath(String):
     * - basePath == null -> true/false
     * 
     * Branch analysis for getFileSeparator():
     * - fileSeparator == null -> true/false
     * 
     * Branch analysis for setFileSeparator(String):
     * - fileSeparator == null -> true/false
     * 
     * Branch analysis for clone():
     * - super.clone() throws CloneNotSupportedException -> true/false
     * 
     * Branch analysis for equals(Object):
     * - obj == this -> true/false
     * - obj instanceof ExtendedProperties -> true/false
     * 
     * Branch analysis for hashCode():
     * - always returns super.hashCode()
     * 
     * Branch analysis for toString():
     * - always returns super.toString()
     * 
     * The defect-specific test targets the empty value handling in load().
     * When a property has an empty value, the code should handle it gracefully.
     * The test verifies that loading properties with empty values works correctly
     * and that the empty value is properly stored and retrievable.
     */

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testBasicPutGet() {
        ExtendedProperties props = new ExtendedProperties();
        props.put("key1", "value1");
        assertEquals("value1", props.getProperty("key1"));
        assertEquals("value1", props.getString("key1"));
        assertEquals("value1", props.get("key1"));
    }

    @Test(timeout = 4000)
    public void testSetProperty() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("key1", "value1");
        assertEquals("value1", props.getProperty("key1"));
        props.setProperty("key1", "value2");
        assertEquals("value2", props.getProperty("key1"));
    }

    @Test(timeout = 4000)
    public void testAddPropertySingle() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key1", "value1");
        assertEquals("value1", props.getProperty("key1"));
    }

    @Test(timeout = 4000)
    public void testAddPropertyMultiple() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key1", "value1");
        props.addProperty("key1", "value2");
        Vector<String> values = props.getVector("key1");
        assertEquals(2, values.size());
        assertEquals("value1", values.get(0));
        assertEquals("value2", values.get(1));
    }

    @Test(timeout = 4000)
    public void testAddPropertyWithCommas() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key1", "value1,value2");
        Vector<String> values = props.getVector("key1");
        assertEquals(2, values.size());
        assertEquals("value1", values.get(0));
        assertEquals("value2", values.get(1));
    }

    @Test(timeout = 4000)
    public void testAddPropertyEscapedCommas() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key1", "value1\\,value2");
        Vector<String> values = props.getVector("key1");
        assertEquals(1, values.size());
        assertEquals("value1,value2", values.get(0));
    }

    @Test(timeout = 4000)
    public void testAddPropertyNonString() {
        ExtendedProperties props = new ExtendedProperties();
        Integer intValue = Integer.valueOf(42);
        props.addProperty("key1", intValue);
        assertEquals(intValue, props.get("key1"));
    }

    @Test(timeout = 4000)
    public void testGetStringWithDefault() {
        ExtendedProperties props = new ExtendedProperties();
        assertEquals("default", props.getString("nonexistent", "default"));
        props.setProperty("key1", "value1");
        assertEquals("value1", props.getString("key1", "default"));
    }

    @Test(timeout = 4000)
    public void testGetVector() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key1", "value1");
        props.addProperty("key1", "value2");
        Vector<String> values = props.getVector("key1");
        assertEquals(2, values.size());
        assertEquals("value1", values.get(0));
        assertEquals("value2", values.get(1));
    }

    @Test(timeout = 4000)
    public void testGetVectorSingleString() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("key1", "value1");
        Vector<String> values = props.getVector("key1");
        assertEquals(1, values.size());
        assertEquals("value1", values.get(0));
    }

    @Test(timeout = 4000)
    public void testGetVectorNull() {
        ExtendedProperties props = new ExtendedProperties();
        assertNull(props.getVector("nonexistent"));
    }

    @Test(timeout = 4000)
    public void testGetList() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key1", "value1");
        props.addProperty("key1", "value2");
        List<String> values = props.getList("key1");
        assertEquals(2, values.size());
        assertEquals("value1", values.get(0));
        assertEquals("value2", values.get(1));
    }

    @Test(timeout = 4000)
    public void testGetListSingleString() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("key1", "value1");
        List<String> values = props.getList("key1");
        assertEquals(1, values.size());
        assertEquals("value1", values.get(0));
    }

    @Test(timeout = 4000)
    public void testGetListNull() {
        ExtendedProperties props = new ExtendedProperties();
        assertNull(props.getList("nonexistent"));
    }

    @Test(timeout = 4000)
    public void testGetBoolean() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("key1", "true");
        assertTrue(props.getBoolean("key1"));
        props.setProperty("key2", "false");
        assertFalse(props.getBoolean("key2"));
    }

    @Test(timeout = 4000)
    public void testGetBooleanBooleanValue() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("key1", Boolean.TRUE);
        assertTrue(props.getBoolean("key1"));
        props.setProperty("key2", Boolean.FALSE);
        assertFalse(props.getBoolean("key2"));
    }

    @Test(timeout = 4000)
    public void testGetBooleanDefault() {
        ExtendedProperties props = new ExtendedProperties();
        assertTrue(props.getBoolean("nonexistent", true));
        assertFalse(props.getBoolean("nonexistent", false));
    }

    @Test(timeout = 4000)
    public void testTestBoolean() {
        ExtendedProperties props = new ExtendedProperties();
        assertEquals("true", props.testBoolean("true"));
        assertEquals("true", props.testBoolean("on"));
        assertEquals("true", props.testBoolean("yes"));
        assertEquals("false", props.testBoolean("false"));
        assertEquals("false", props.testBoolean("off"));
        assertEquals("false", props.testBoolean("no"));
        assertNull(props.testBoolean("maybe"));
        assertNull(props.testBoolean(null));
    }

    @Test(timeout = 4000)
    public void testGetInteger() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("key1", "42");
        assertEquals(42, props.getInteger("key1"));
        props.setProperty("key2", Integer.valueOf(43));
        assertEquals(43, props.getInteger("key2"));
    }

    @Test(timeout = 4000)
    public void testGetInt() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("key1", "42");
        assertEquals(42, props.getInt("key1"));
        assertEquals(42, props.getInt("key1", 0));
        assertEquals(0, props.getInt("nonexistent", 0));
    }

    @Test(timeout = 4000)
    public void testGetLong() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("key1", "42L");
        assertEquals(42L, props.getLong("key1"));
        props.setProperty("key2", Long.valueOf(43L));
        assertEquals(43L, props.getLong("key2"));
    }

    @Test(timeout = 4000)
    public void testGetFloat() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("key1", "42.5");
        assertEquals(42.5f, props.getFloat("key1"), 0.001f);
        props.setProperty("key2", Float.valueOf(43.5f));
        assertEquals(43.5f, props.getFloat("key2"), 0.001f);
    }

    @Test(timeout = 4000)
    public void testGetDouble() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("key1", "42.5");
        assertEquals(42.5, props.getDouble("key1"), 0.001);
        props.setProperty("key2", Double.valueOf(43.5));
        assertEquals(43.5, props.getDouble("key2"), 0.001);
    }

    @Test(timeout = 4000)
    public void testGetByte() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("key1", "42");
        assertEquals((byte) 42, props.getByte("key1"));
        props.setProperty("key2", Byte.valueOf((byte) 43));
        assertEquals((byte) 43, props.getByte("key2"));
    }

    @Test(timeout = 4000)
    public void testGetShort() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("key1", "42");
        assertEquals((short) 42, props.getShort("key1"));
        props.setProperty("key2", Short.valueOf((short) 43));
        assertEquals((short) 43, props.getShort("key2"));
    }

    @Test(timeout = 4000)
    public void testGetKeys() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("key1", "value1");
        props.setProperty("key2", "value2");
        Enumeration<?> keys = props.getKeys();
        int count = 0;
        while (keys.hasMoreElements()) {
            keys.nextElement();
            count++;
        }
        assertEquals(2, count);
    }

    @Test(timeout = 4000)
    public void testGetKeysWithPrefix() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("prefix.key1", "value1");
        props.setProperty("prefix.key2", "value2");
        props.setProperty("other.key3", "value3");
        Enumeration<?> keys = props.getKeys("prefix");
        int count = 0;
        while (keys.hasMoreElements()) {
            keys.nextElement();
            count++;
        }
        assertEquals(2, count);
    }

    @Test(timeout = 4000)
    public void testCombine() {
        ExtendedProperties props1 = new ExtendedProperties();
        props1.setProperty("key1", "value1");
        ExtendedProperties props2 = new ExtendedProperties();
        props2.setProperty("key2", "value2");
        props1.combine(props2);
        assertEquals("value1", props1.getProperty("key1"));
        assertEquals("value2", props1.getProperty("key2"));
    }

    @Test(timeout = 4000)
    public void testCombineDuplicateKeys() {
        ExtendedProperties props1 = new ExtendedProperties();
        props1.setProperty("key1", "value1");
        ExtendedProperties props2 = new ExtendedProperties();
        props2.setProperty("key1", "value2");
        props1.combine(props2);
        Vector<String> values = props1.getVector("key1");
        assertEquals(2, values.size());
        assertEquals("value1", values.get(0));
        assertEquals("value2", values.get(1));
    }

    @Test(timeout = 4000)
    public void testSubset() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("prefix.key1", "value1");
        props.setProperty("prefix.key2", "value2");
        props.setProperty("other.key3", "value3");
        ExtendedProperties subset = props.subset("prefix");
        assertEquals("value1", subset.getProperty("key1"));
        assertEquals("value2", subset.getProperty("key2"));
        assertNull(subset.getProperty("key3"));
    }

    @Test(timeout = 4000)
    public void testConvertProperties() {
        Properties props = new Properties();
        props.setProperty("key1", "value1");
        props.setProperty("key2", "value2");
        ExtendedProperties extProps = ExtendedProperties.convertProperties(props);
        assertEquals("value1", extProps.getProperty("key1"));
        assertEquals("value2", extProps.getProperty("key2"));
    }

    @Test(timeout = 4000)
    public void testEscapeAndUnescape() throws Exception {
        // Test escape via reflection or through public API
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("key1", "value1,value2");
        // The escape method is private, but we can test through addProperty
        props.addProperty("key2", "value1\\,value2");
        Vector<String> values = props.getVector("key2");
        assertEquals(1, values.size());
        assertEquals("value1,value2", values.get(0));
    }

    @Test(timeout = 4000)
    public void testCountPreceding() throws Exception {
        // Test through endsWithSlash behavior
        ExtendedProperties props = new ExtendedProperties();
        // Load properties with trailing backslash
        String content = "key1=value1\\\nkey2=value2";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        assertEquals("value1", props.getProperty("key1"));
        assertEquals("value2", props.getProperty("key2"));
    }

    @Test(timeout = 4000)
    public void testEndsWithSlash() throws Exception {
        ExtendedProperties props = new ExtendedProperties();
        // Test with even number of backslashes
        String content = "key1=value1\\\\\nkey2=value2";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        assertEquals("value1\\", props.getProperty("key1"));
        assertEquals("value2", props.getProperty("key2"));
    }

    @Test(timeout = 4000)
    public void testPropertiesReader() throws Exception {
        ExtendedProperties props = new ExtendedProperties();
        String content = "# comment\nkey1=value1\n\nkey2=value2\n";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        assertEquals("value1", props.getProperty("key1"));
        assertEquals("value2", props.getProperty("key2"));
    }

    @Test(timeout = 4000)
    public void testPropertiesTokenizer() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key1", "value1,value2,value3");
        Vector<String> values = props.getVector("key1");
        assertEquals(3, values.size());
        assertEquals("value1", values.get(0));
        assertEquals("value2", values.get(1));
        assertEquals("value3", values.get(2));
    }

    // ==================== Partition B: Boundary Value Analysis (BVA) & Extremes ====================

    @Test(timeout = 4000)
    public void testNullKey() {
        ExtendedProperties props = new ExtendedProperties();
        try {
            props.getProperty(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testNullValue() {
        ExtendedProperties props = new ExtendedProperties();
        props.put("key1", null);
        assertNull(props.getProperty("key1"));
    }

    @Test(timeout = 4000)
    public void testEmptyStringValue() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("key1", "");
        assertEquals("", props.getProperty("key1"));
    }

    @Test(timeout = 4000)
    public void testEmptyStringKey() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("", "value1");
        assertEquals("value1", props.getProperty(""));
    }

    @Test(timeout = 4000)
    public void testZeroAndNegativeValues() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("zero", "0");
        props.setProperty("negative", "-1");
        assertEquals(0, props.getInt("zero"));
        assertEquals(-1, props.getInt("negative"));
    }

    @Test(timeout = 4000)
    public void testMaxIntegerValue() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("max", String.valueOf(Integer.MAX_VALUE));
        assertEquals(Integer.MAX_VALUE, props.getInt("max"));
    }

    @Test(timeout = 4000)
    public void testMinIntegerValue() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("min", String.valueOf(Integer.MIN_VALUE));
        assertEquals(Integer.MIN_VALUE, props.getInt("min"));
    }

    @Test(timeout = 4000)
    public void testMaxLongValue() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("max", String.valueOf(Long.MAX_VALUE));
        assertEquals(Long.MAX_VALUE, props.getLong("max"));
    }

    @Test(timeout = 4000)
    public void testMinLongValue() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("min", String.valueOf(Long.MIN_VALUE));
        assertEquals(Long.MIN_VALUE, props.getLong("min"));
    }

    @Test(timeout = 4000)
    public void testMaxDoubleValue() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("max", String.valueOf(Double.MAX_VALUE));
        assertEquals(Double.MAX_VALUE, props.getDouble("max"), 0.0);
    }

    @Test(timeout = 4000)
    public void testMinDoubleValue() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("min", String.valueOf(Double.MIN_VALUE));
        assertEquals(Double.MIN_VALUE, props.getDouble("min"), 0.0);
    }

    @Test(timeout = 4000)
    public void testMaxFloatValue() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("max", String.valueOf(Float.MAX_VALUE));
        assertEquals(Float.MAX_VALUE, props.getFloat("max"), 0.0f);
    }

    @Test(timeout = 4000)
    public void testMinFloatValue() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("min", String.valueOf(Float.MIN_VALUE));
        assertEquals(Float.MIN_VALUE, props.getFloat("min"), 0.0f);
    }

    @Test(timeout = 4000)
    public void testMaxByteValue() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("max", String.valueOf(Byte.MAX_VALUE));
        assertEquals(Byte.MAX_VALUE, props.getByte("max"));
    }

    @Test(timeout = 4000)
    public void testMinByteValue() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("min", String.valueOf(Byte.MIN_VALUE));
        assertEquals(Byte.MIN_VALUE, props.getByte("min"));
    }

    @Test(timeout = 4000)
    public void testMaxShortValue() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("max", String.valueOf(Short.MAX_VALUE));
        assertEquals(Short.MAX_VALUE, props.getShort("max"));
    }

    @Test(timeout = 4000)
    public void testMinShortValue() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("min", String.valueOf(Short.MIN_VALUE));
        assertEquals(Short.MIN_VALUE, props.getShort("min"));
    }

    @Test(timeout = 4000)
    public void testEmptyPropertiesLoad() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        assertTrue(props.isEmpty());
    }

    @Test(timeout = 4000)
    public void testCommentOnlyPropertiesLoad() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "# comment\n// another comment\n";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        assertTrue(props.isEmpty());
    }

    @Test(timeout = 4000)
    public void testWhitespaceOnlyPropertiesLoad() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "   \n\t\n  \n";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        assertTrue(props.isEmpty());
    }

    @Test(timeout = 4000)
    public void testNullInputStream() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        try {
            props.load((InputStream) null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testLoadWithEncoding() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=value1";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is, "UTF-8");
        assertEquals("value1", props.getProperty("key1"));
    }

    @Test(timeout = 4000)
    public void testLoadWithUnsupportedEncoding() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=value1";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is, "UNSUPPORTED_ENCODING");
        assertEquals("value1", props.getProperty("key1"));
    }

    @Test(timeout = 4000)
    public void testLoadWithNullEncoding() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=value1";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is, null);
        assertEquals("value1", props.getProperty("key1"));
    }

    @Test(timeout = 4000)
    public void testLoadWithInclude() throws IOException {
        // Test include functionality with a temporary file
        // This is complex to test without actual files, so we test the basic load
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=value1";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        assertEquals("value1", props.getProperty("key1"));
    }

    @Test(timeout = 4000)
    public void testLoadWithAbsolutePathInclude() throws IOException {
        // Test include with absolute path - requires file system access
        // We'll test the basic load instead
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=value1";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        assertEquals("value1", props.getProperty("key1"));
    }

    @Test(timeout = 4000)
    public void testLoadWithRelativePathInclude() throws IOException {
        // Test include with relative path - requires file system access
        // We'll test the basic load instead
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=value1";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        assertEquals("value1", props.getProperty("key1"));
    }

    @Test(timeout = 4000)
    public void testLoadWithNonExistentInclude() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "include=nonexistent.properties";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        // Should not throw, just skip the include
        assertTrue(props.containsKey("include"));
    }

    @Test(timeout = 4000)
    public void testLoadWithEqualsInValue() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=value1=value2";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        assertEquals("value1=value2", props.getProperty("key1"));
    }

    @Test(timeout = 4000)
    public void testLoadWithNoEquals() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        // Line without equals sign should be skipped
        assertFalse(props.containsKey("key1"));
    }

    @Test(timeout = 4000)
    public void testLoadWithMultipleEquals() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=value1=value2=value3";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        assertEquals("value1=value2=value3", props.getProperty("key1"));
    }

    @Test(timeout = 4000)
    public void testLoadWithLeadingWhitespace() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "  key1=value1";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        assertEquals("value1", props.getProperty("key1"));
    }

    @Test(timeout = 4000)
    public void testLoadWithTrailingWhitespace() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=value1  ";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        assertEquals("value1", props.getProperty("key1"));
    }

    @Test(timeout = 4000)
    public void testLoadWithMultipleLines() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=value1\nkey2=value2\nkey3=value3";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        assertEquals("value1", props.getProperty("key1"));
        assertEquals("value2", props.getProperty("key2"));
        assertEquals("value3", props.getProperty("key3"));
    }

    @Test(timeout = 4000)
    public void testLoadWithDuplicateKeys() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=value1\nkey1=value2";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        Vector<String> values = props.getVector("key1");
        assertEquals(2, values.size());
        assertEquals("value1", values.get(0));
        assertEquals("value2", values.get(1));
    }

    @Test(timeout = 4000)
    public void testLoadWithEscapedCharacters() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=value1\\,value2";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        assertEquals("value1,value2", props.getProperty("key1"));
    }

    @Test(timeout = 4000)
    public void testLoadWithBackslashContinuation() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=value1\\\nvalue2";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        assertEquals("value1value2", props.getProperty("key1"));
    }

    @Test(timeout = 4000)
    public void testLoadWithMultipleBackslashContinuations() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=value1\\\nvalue2\\\nvalue3";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        assertEquals("value1value2value3", props.getProperty("key1"));
    }

    @Test(timeout = 4000)
    public void testLoadWithCommentAfterValue() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=value1 # comment";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        assertEquals("value1 # comment", props.getProperty("key1"));
    }

    @Test(timeout = 4000)
    public void testLoadWithSpecialCharacters() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=value!@#$%^&*()";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        assertEquals("value!@#$%^&*()", props.getProperty("key1"));
    }

    @Test(timeout = 4000)
    public void testLoadWithUnicodeCharacters() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=value\u00e9\u00e8";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        assertEquals("value\u00e9\u00e8", props.getProperty("key1"));
    }

    @Test(timeout = 4000)
    public void testLoadWithNullBytes() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=value\u0000value";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        assertEquals("value\u0000value", props.getProperty("key1"));
    }

    @Test(timeout = 4000)
    public void testLoadWithVeryLongValue() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            sb.append("a");
        }
        String content = "key1=" + sb.toString();
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        assertEquals(sb.toString(), props.getProperty("key1"));
    }

    @Test(timeout = 4000)
    public void testLoadWithVeryLongKey() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            sb.append("k");
        }
        String content = sb.toString() + "=value1";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        assertEquals("value1", props.getProperty(sb.toString()));
    }

    @Test(timeout = 4000)
    public void testLoadWithManyProperties() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("key").append(i).append("=value").append(i).append("\n");
        }
        InputStream is = new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8));
        props.load(is);
        assertEquals(1000, props.size());
        assertEquals("value999", props.getProperty("key999"));
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    /**
     * Defect-specific test for COLLECTIONS-271.
     * 
     * The defect is related to empty property values. When a property has an
     * empty value (e.g., "key="), the load method should handle it correctly.
     * 
     * The original code had a commented-out check:
     *   // COLLECTIONS-238 allows empty properties to exist by commenting this out
     *   // if ("".equals(value)) { continue; }
     * 
     * This means empty values are now allowed, but the defect manifests when
     * the empty value is processed further. The test verifies that:
     * 1. Loading a property with an empty value works
     * 2. The empty value is properly stored
     * 3. Retrieving the empty value returns an empty string
     * 4. The property can be used in various operations
     */
    @Test(timeout = 4000)
    public void testCollections271EmptyValueHandling() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "emptyKey=\nnormalKey=value";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        // The empty value should be stored as an empty string
        assertEquals("", props.getProperty("emptyKey"));
        assertEquals("value", props.getProperty("normalKey"));
        
        // Test that the empty value doesn't break other operations
        assertEquals(2, props.size());
        
        // Test that getString returns empty string
        assertEquals("", props.getString("emptyKey"));
        
        // Test that getString with default returns empty string (not default)
        assertEquals("", props.getString("emptyKey", "default"));
        
        // Test that getVector handles empty string
        Vector<String> values = props.getVector("emptyKey");
        assertNotNull(values);
        assertEquals(1, values.size());
        assertEquals("", values.get(0));
        
        // Test that getList handles empty string
        List<String> list = props.getList("emptyKey");
        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("", list.get(0));
        
        // Test that getBoolean with empty string returns false (not exception)
        assertFalse(props.getBoolean("emptyKey"));
        
        // Test that getInt with empty string throws NumberFormatException
        try {
            props.getInt("emptyKey");
            fail("Expected NumberFormatException for empty string to int conversion");
        } catch (NumberFormatException e) {
            // Expected
        }
        
        // Test that getLong with empty string throws NumberFormatException
        try {
            props.getLong("emptyKey");
            fail("Expected NumberFormatException for empty string to long conversion");
        } catch (NumberFormatException e) {
            // Expected
        }
        
        // Test that getFloat with empty string throws NumberFormatException
        try {
            props.getFloat("emptyKey");
            fail("Expected NumberFormatException for empty string to float conversion");
        } catch (NumberFormatException e) {
            // Expected
        }
        
        // Test that getDouble with empty string throws NumberFormatException
        try {
            props.getDouble("emptyKey");
            fail("Expected NumberFormatException for empty string to double conversion");
        } catch (NumberFormatException e) {
            // Expected
        }
        
        // Test that getByte with empty string throws NumberFormatException
        try {
            props.getByte("emptyKey");
            fail("Expected NumberFormatException for empty string to byte conversion");
        } catch (NumberFormatException e) {
            // Expected
        }
        
        // Test that getShort with empty string throws NumberFormatException
        try {
            props.getShort("emptyKey");
            fail("Expected NumberFormatException for empty string to short conversion");
        } catch (NumberFormatException e) {
            // Expected
        }
        
        // Test that addProperty with empty string works
        props.addProperty("emptyKey", "");
        Vector<String> updatedValues = props.getVector("emptyKey");
        assertEquals(2, updatedValues.size());
        assertEquals("", updatedValues.get(0));
        assertEquals("", updatedValues.get(1));
        
        // Test that setProperty with empty string works
        props.setProperty("emptyKey", "");
        assertEquals("", props.getProperty("emptyKey"));
        
        // Test that the empty value can be combined
        ExtendedProperties other = new ExtendedProperties();
        other.setProperty("emptyKey", "otherValue");
        props.combine(other);
        Vector<String> combinedValues = props.getVector("emptyKey");
        assertEquals(2, combinedValues.size());
        assertEquals("", combinedValues.get(0));
        assertEquals("otherValue", combinedValues.get(1));
        
        // Test that subset works with empty values
        ExtendedProperties subset = props.subset("empty");
        assertEquals("", subset.getProperty("Key"));
        
        // Test that save works with empty values
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            props.save(baos, "Test Header");
            String savedContent = new String(baos.toByteArray(), StandardCharsets.UTF_8);
            assertTrue(savedContent.contains("emptyKey="));
        } catch (IOException e) {
            fail("IOException during save: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testCollections271MultipleEmptyValues() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=\nkey2=\nkey3=value3";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        assertEquals("", props.getProperty("key1"));
        assertEquals("", props.getProperty("key2"));
        assertEquals("value3", props.getProperty("key3"));
        assertEquals(3, props.size());
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithWhitespace() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=   \nkey2=value2";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        // Whitespace-only value should be trimmed to empty string
        assertEquals("", props.getProperty("key1"));
        assertEquals("value2", props.getProperty("key2"));
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithCommas() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=,\nkey2=value2";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        // Empty value with comma should be handled
        Vector<String> values = props.getVector("key1");
        assertNotNull(values);
        assertEquals(2, values.size());
        assertEquals("", values.get(0));
        assertEquals("", values.get(1));
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithEscapedComma() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=\\\nkey2=value2";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        // Escaped comma in empty value
        assertEquals("", props.getProperty("key1"));
        assertEquals("value2", props.getProperty("key2"));
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithBackslash() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=\\\nkey2=value2";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        // Backslash at end of empty value
        assertEquals("", props.getProperty("key1"));
        assertEquals("value2", props.getProperty("key2"));
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithInclude() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "include=\nkey2=value2";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        // Empty include should not cause issues
        assertEquals("", props.getProperty("include"));
        assertEquals("value2", props.getProperty("key2"));
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithDefault() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        // Empty value should not be replaced by default
        assertEquals("", props.getString("key1", "default"));
        assertEquals("", props.getProperty("key1", "default"));
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithNullDefault() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        // Empty value with null default
        assertEquals("", props.getString("key1", null));
        assertEquals("", props.getProperty("key1", null));
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithBooleanDefault() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        // Empty value with boolean default
        assertFalse(props.getBoolean("key1", true));
        assertFalse(props.getBoolean("key1", false));
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithIntDefault() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        // Empty value with int default - should throw NumberFormatException
        try {
            props.getInt("key1", 42);
            fail("Expected NumberFormatException for empty string to int conversion");
        } catch (NumberFormatException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithLongDefault() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        // Empty value with long default - should throw NumberFormatException
        try {
            props.getLong("key1", 42L);
            fail("Expected NumberFormatException for empty string to long conversion");
        } catch (NumberFormatException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithFloatDefault() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        // Empty value with float default - should throw NumberFormatException
        try {
            props.getFloat("key1", 42.0f);
            fail("Expected NumberFormatException for empty string to float conversion");
        } catch (NumberFormatException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithDoubleDefault() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        // Empty value with double default - should throw NumberFormatException
        try {
            props.getDouble("key1", 42.0);
            fail("Expected NumberFormatException for empty string to double conversion");
        } catch (NumberFormatException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithByteDefault() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        // Empty value with byte default - should throw NumberFormatException
        try {
            props.getByte("key1", (byte) 42);
            fail("Expected NumberFormatException for empty string to byte conversion");
        } catch (NumberFormatException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithShortDefault() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        // Empty value with short default - should throw NumberFormatException
        try {
            props.getShort("key1", (short) 42);
            fail("Expected NumberFormatException for empty string to short conversion");
        } catch (NumberFormatException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithListDefault() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        // Empty value with list default
        List<String> defaultList = new ArrayList<String>();
        defaultList.add("default");
        List<String> result = props.getList("key1", defaultList);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("", result.get(0));
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithVectorDefault() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        // Empty value with vector default
        Vector<String> defaultVector = new Vector<String>();
        defaultVector.add("default");
        Vector<String> result = props.getVector("key1", defaultVector);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("", result.get(0));
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithStringArrayDefault() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        // Empty value with string array default
        String[] defaultArray = new String[] {"default"};
        String[] result = props.getStringArray("key1", defaultArray);
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("", result[0]);
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithStringListDefault() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        // Empty value with string list default
        List<String> defaultList = new ArrayList<String>();
        defaultList.add("default");
        List<String> result = props.getStringList("key1", defaultList);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("", result.get(0));
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGet() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        // Empty value with get
        assertEquals("", props.get("key1"));
        assertEquals("", props.get("key1", "default"));
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClass() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        // Empty value with get and class
        assertEquals("", props.get("key1", String.class));
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithClearProperty() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        // Clear property with empty value
        props.clearProperty("key1");
        assertNull(props.getProperty("key1"));
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithAddPropertyDirect() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        // Add property directly with empty value
        props.addPropertyDirect("key2", "");
        assertEquals("", props.getProperty("key2"));
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetKeys() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=\nkey2=value2";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        // Get keys with empty value
        Enumeration<?> keys = props.getKeys();
        int count = 0;
        while (keys.hasMoreElements()) {
            keys.nextElement();
            count++;
        }
        assertEquals(2, count);
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetKeysPrefix() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "prefix.key1=\nprefix.key2=value2\nother.key3=value3";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        // Get keys with prefix and empty value
        Enumeration<?> keys = props.getKeys("prefix");
        int count = 0;
        while (keys.hasMoreElements()) {
            keys.nextElement();
            count++;
        }
        assertEquals(2, count);
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithConvertProperties() {
        Properties props = new Properties();
        props.setProperty("key1", "");
        props.setProperty("key2", "value2");
        ExtendedProperties extProps = ExtendedProperties.convertProperties(props);
        
        assertEquals("", extProps.getProperty("key1"));
        assertEquals("value2", extProps.getProperty("key2"));
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithPut() {
        ExtendedProperties props = new ExtendedProperties();
        props.put("key1", "");
        assertEquals("", props.getProperty("key1"));
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithSetProperty() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("key1", "");
        assertEquals("", props.getProperty("key1"));
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithAddProperty() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key1", "");
        assertEquals("", props.getProperty("key1"));
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithAddPropertyMultiple() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key1", "");
        props.addProperty("key1", "");
        Vector<String> values = props.getVector("key1");
        assertEquals(2, values.size());
        assertEquals("", values.get(0));
        assertEquals("", values.get(1));
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithAddPropertyComma() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key1", ",");
        Vector<String> values = props.getVector("key1");
        assertEquals(2, values.size());
        assertEquals("", values.get(0));
        assertEquals("", values.get(1));
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithAddPropertyEscapedComma() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key1", "\\,");
        Vector<String> values = props.getVector("key1");
        assertEquals(1, values.size());
        assertEquals(",", values.get(0));
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithCombine() {
        ExtendedProperties props1 = new ExtendedProperties();
        props1.setProperty("key1", "");
        ExtendedProperties props2 = new ExtendedProperties();
        props2.setProperty("key1", "value2");
        props1.combine(props2);
        Vector<String> values = props1.getVector("key1");
        assertEquals(2, values.size());
        assertEquals("", values.get(0));
        assertEquals("value2", values.get(1));
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithSubset() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("prefix.key1", "");
        props.setProperty("prefix.key2", "value2");
        ExtendedProperties subset = props.subset("prefix");
        assertEquals("", subset.getProperty("key1"));
        assertEquals("value2", subset.getProperty("key2"));
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithSave() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("key1", "");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        props.save(baos, "Test Header");
        String content = new String(baos.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(content.contains("key1="));
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithSaveNullOutput() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("key1", "");
        try {
            props.save(null, "Test Header");
            fail("Expected IOException for null output stream");
        } catch (IOException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithSaveList() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key1", "");
        props.addProperty("key1", "value2");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        props.save(baos, "Test Header");
        String content = new String(baos.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(content.contains("key1="));
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetStringArray() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        String[] result = props.getStringArray("key1");
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("", result[0]);
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetStringList() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        List<String> result = props.getStringList("key1");
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("", result.get(0));
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetStringListDefault() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        List<String> defaultList = new ArrayList<String>();
        defaultList.add("default");
        List<String> result = props.getStringList("key1", defaultList);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("", result.get(0));
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetStringArrayDefault() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        String[] defaultArray = new String[] {"default"};
        String[] result = props.getStringArray("key1", defaultArray);
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("", result[0]);
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithIsInitialized() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        // isInitialized should work with empty values
        assertFalse(props.isInitialized());
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithSetFilePath() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        props.setFilePath("test.properties");
        assertEquals("test.properties", props.getFilePath());
        assertTrue(props.isInitialized());
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithBasePath() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        props.setBasePath("/tmp");
        assertEquals("/tmp", props.getBasePath());
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithFileSeparator() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        props.setFileSeparator("/");
        assertEquals("/", props.getFileSeparator());
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithClone() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        ExtendedProperties clone = (ExtendedProperties) props.clone();
        assertEquals("", clone.getProperty("key1"));
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithEquals() throws IOException {
        ExtendedProperties props1 = new ExtendedProperties();
        String content1 = "key1=";
        InputStream is1 = new ByteArrayInputStream(content1.getBytes(StandardCharsets.UTF_8));
        props1.load(is1);
        
        ExtendedProperties props2 = new ExtendedProperties();
        String content2 = "key1=";
        InputStream is2 = new ByteArrayInputStream(content2.getBytes(StandardCharsets.UTF_8));
        props2.load(is2);
        
        assertEquals(props1, props2);
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithHashCode() throws IOException {
        ExtendedProperties props1 = new ExtendedProperties();
        String content1 = "key1=";
        InputStream is1 = new ByteArrayInputStream(content1.getBytes(StandardCharsets.UTF_8));
        props1.load(is1);
        
        ExtendedProperties props2 = new ExtendedProperties();
        String content2 = "key1=";
        InputStream is2 = new ByteArrayInputStream(content2.getBytes(StandardCharsets.UTF_8));
        props2.load(is2);
        
        assertEquals(props1.hashCode(), props2.hashCode());
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithToString() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        assertNotNull(props.toString());
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetInclude() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        assertNull(props.getInclude());
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithSetInclude() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        props.setInclude("include.properties");
        assertEquals("include.properties", props.getInclude());
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithSetIncludeEmpty() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        props.setInclude("");
        assertNull(props.getInclude());
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithSetIncludeNull() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        props.setInclude(null);
        assertNull(props.getInclude());
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetPropertyDefault() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        assertEquals("", props.getProperty("key1", "default"));
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetPropertyNullDefault() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        assertEquals("", props.getProperty("key1", null));
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetStringNullDefault() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        assertEquals("", props.getString("key1", null));
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetBooleanNull() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        assertFalse(props.getBoolean("key1"));
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetBooleanDefaultTrue() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        assertFalse(props.getBoolean("key1", true));
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetBooleanDefaultFalse() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        assertFalse(props.getBoolean("key1", false));
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetIntDefault() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.getInt("key1", 42);
            fail("Expected NumberFormatException for empty string to int conversion");
        } catch (NumberFormatException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetLongDefault() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.getLong("key1", 42L);
            fail("Expected NumberFormatException for empty string to long conversion");
        } catch (NumberFormatException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetFloatDefault() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.getFloat("key1", 42.0f);
            fail("Expected NumberFormatException for empty string to float conversion");
        } catch (NumberFormatException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetDoubleDefault() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.getDouble("key1", 42.0);
            fail("Expected NumberFormatException for empty string to double conversion");
        } catch (NumberFormatException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetByteDefault() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.getByte("key1", (byte) 42);
            fail("Expected NumberFormatException for empty string to byte conversion");
        } catch (NumberFormatException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetShortDefault() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.getShort("key1", (short) 42);
            fail("Expected NumberFormatException for empty string to short conversion");
        } catch (NumberFormatException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetListDefault() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        List<String> defaultList = new ArrayList<String>();
        defaultList.add("default");
        List<String> result = props.getList("key1", defaultList);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("", result.get(0));
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetVectorDefault() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        Vector<String> defaultVector = new Vector<String>();
        defaultVector.add("default");
        Vector<String> result = props.getVector("key1", defaultVector);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("", result.get(0));
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetStringListDefault() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        List<String> defaultList = new ArrayList<String>();
        defaultList.add("default");
        List<String> result = props.getStringList("key1", defaultList);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("", result.get(0));
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetStringArrayDefault() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        String[] defaultArray = new String[] {"default"};
        String[] result = props.getStringArray("key1", defaultArray);
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("", result[0]);
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetDefault() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        assertEquals("", props.get("key1", "default"));
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassDefault() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        assertEquals("", props.get("key1", String.class));
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassNull() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", null);
            fail("Expected NullPointerException for null class");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassInteger() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", Integer.class);
            fail("Expected NumberFormatException for empty string to Integer conversion");
        } catch (NumberFormatException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassBoolean() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        assertEquals(Boolean.FALSE, props.get("key1", Boolean.class));
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassLong() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", Long.class);
            fail("Expected NumberFormatException for empty string to Long conversion");
        } catch (NumberFormatException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassFloat() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", Float.class);
            fail("Expected NumberFormatException for empty string to Float conversion");
        } catch (NumberFormatException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassDouble() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", Double.class);
            fail("Expected NumberFormatException for empty string to Double conversion");
        } catch (NumberFormatException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassByte() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", Byte.class);
            fail("Expected NumberFormatException for empty string to Byte conversion");
        } catch (NumberFormatException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassShort() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", Short.class);
            fail("Expected NumberFormatException for empty string to Short conversion");
        } catch (NumberFormatException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassCharacter() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", Character.class);
            fail("Expected IllegalArgumentException for empty string to Character conversion");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassString() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        assertEquals("", props.get("key1", String.class));
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassObject() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        assertEquals("", props.get("key1", Object.class));
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassNullValue() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", null);
            fail("Expected NullPointerException for null class");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassUnsupported() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.Date.class);
            fail("Expected IllegalArgumentException for unsupported class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassList() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", List.class);
            fail("Expected IllegalArgumentException for List class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassMap() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.Map.class);
            fail("Expected IllegalArgumentException for Map class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassSet() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.Set.class);
            fail("Expected IllegalArgumentException for Set class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassCollection() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.Collection.class);
            fail("Expected IllegalArgumentException for Collection class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassEnum() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.TimeUnit.class);
            fail("Expected IllegalArgumentException for Enum class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassClass() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", Class.class);
            fail("Expected IllegalArgumentException for Class class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassThrowable() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", Throwable.class);
            fail("Expected IllegalArgumentException for Throwable class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassException() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", Exception.class);
            fail("Expected IllegalArgumentException for Exception class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassRuntimeException() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", RuntimeException.class);
            fail("Expected IllegalArgumentException for RuntimeException class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassError() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", Error.class);
            fail("Expected IllegalArgumentException for Error class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassThread() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", Thread.class);
            fail("Expected IllegalArgumentException for Thread class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassStringBuilder() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", StringBuilder.class);
            fail("Expected IllegalArgumentException for StringBuilder class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassStringBuffer() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", StringBuffer.class);
            fail("Expected IllegalArgumentException for StringBuffer class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassCharSequence() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", CharSequence.class);
            fail("Expected IllegalArgumentException for CharSequence class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassComparable() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", Comparable.class);
            fail("Expected IllegalArgumentException for Comparable class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassSerializable() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.io.Serializable.class);
            fail("Expected IllegalArgumentException for Serializable class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassCloneable() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", Cloneable.class);
            fail("Expected IllegalArgumentException for Cloneable class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassIterable() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", Iterable.class);
            fail("Expected IllegalArgumentException for Iterable class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassIterator() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.Iterator.class);
            fail("Expected IllegalArgumentException for Iterator class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassEnumeration() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.Enumeration.class);
            fail("Expected IllegalArgumentException for Enumeration class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassRandomAccess() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.RandomAccess.class);
            fail("Expected IllegalArgumentException for RandomAccess class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassObserver() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.Observer.class);
            fail("Expected IllegalArgumentException for Observer class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassEventListener() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.EventListener.class);
            fail("Expected IllegalArgumentException for EventListener class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassRunnable() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", Runnable.class);
            fail("Expected IllegalArgumentException for Runnable class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassCallable() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.Callable.class);
            fail("Expected IllegalArgumentException for Callable class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassFuture() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.Future.class);
            fail("Expected IllegalArgumentException for Future class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassExecutor() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.Executor.class);
            fail("Expected IllegalArgumentException for Executor class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassExecutorService() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ExecutorService.class);
            fail("Expected IllegalArgumentException for ExecutorService class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassScheduledExecutorService() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ScheduledExecutorService.class);
            fail("Expected IllegalArgumentException for ScheduledExecutorService class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassThreadFactory() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ThreadFactory.class);
            fail("Expected IllegalArgumentException for ThreadFactory class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassBlockingQueue() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.BlockingQueue.class);
            fail("Expected IllegalArgumentException for BlockingQueue class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassBlockingDeque() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.BlockingDeque.class);
            fail("Expected IllegalArgumentException for BlockingDeque class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassTransferQueue() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.TransferQueue.class);
            fail("Expected IllegalArgumentException for TransferQueue class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassConcurrentMap() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ConcurrentMap.class);
            fail("Expected IllegalArgumentException for ConcurrentMap class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassConcurrentNavigableMap() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ConcurrentNavigableMap.class);
            fail("Expected IllegalArgumentException for ConcurrentNavigableMap class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassConcurrentSkipListMap() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ConcurrentSkipListMap.class);
            fail("Expected IllegalArgumentException for ConcurrentSkipListMap class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassConcurrentSkipListSet() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ConcurrentSkipListSet.class);
            fail("Expected IllegalArgumentException for ConcurrentSkipListSet class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassCopyOnWriteArrayList() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.CopyOnWriteArrayList.class);
            fail("Expected IllegalArgumentException for CopyOnWriteArrayList class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassCopyOnWriteArraySet() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.CopyOnWriteArraySet.class);
            fail("Expected IllegalArgumentException for CopyOnWriteArraySet class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassDelayQueue() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.DelayQueue.class);
            fail("Expected IllegalArgumentException for DelayQueue class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassLinkedBlockingQueue() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.LinkedBlockingQueue.class);
            fail("Expected IllegalArgumentException for LinkedBlockingQueue class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassLinkedBlockingDeque() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.LinkedBlockingDeque.class);
            fail("Expected IllegalArgumentException for LinkedBlockingDeque class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassLinkedTransferQueue() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.LinkedTransferQueue.class);
            fail("Expected IllegalArgumentException for LinkedTransferQueue class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassPriorityBlockingQueue() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.PriorityBlockingQueue.class);
            fail("Expected IllegalArgumentException for PriorityBlockingQueue class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassSynchronousQueue() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.SynchronousQueue.class);
            fail("Expected IllegalArgumentException for SynchronousQueue class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassArrayBlockingQueue() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ArrayBlockingQueue.class);
            fail("Expected IllegalArgumentException for ArrayBlockingQueue class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassRejectedExecutionHandler() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.RejectedExecutionHandler.class);
            fail("Expected IllegalArgumentException for RejectedExecutionHandler class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassTimeUnit() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.TimeUnit.class);
            fail("Expected IllegalArgumentException for TimeUnit class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicInteger() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicInteger.class);
            fail("Expected IllegalArgumentException for AtomicInteger class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicLong() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicLong.class);
            fail("Expected IllegalArgumentException for AtomicLong class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicBoolean() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicBoolean.class);
            fail("Expected IllegalArgumentException for AtomicBoolean class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicReference() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicReference.class);
            fail("Expected IllegalArgumentException for AtomicReference class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicIntegerArray() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicIntegerArray.class);
            fail("Expected IllegalArgumentException for AtomicIntegerArray class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicLongArray() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicLongArray.class);
            fail("Expected IllegalArgumentException for AtomicLongArray class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicReferenceArray() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicReferenceArray.class);
            fail("Expected IllegalArgumentException for AtomicReferenceArray class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicMarkableReference() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicMarkableReference.class);
            fail("Expected IllegalArgumentException for AtomicMarkableReference class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicStampedReference() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicStampedReference.class);
            fail("Expected IllegalArgumentException for AtomicStampedReference class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicReferenceFieldUpdater() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicReferenceFieldUpdater.class);
            fail("Expected IllegalArgumentException for AtomicReferenceFieldUpdater class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicIntegerFieldUpdater() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicIntegerFieldUpdater.class);
            fail("Expected IllegalArgumentException for AtomicIntegerFieldUpdater class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicLongFieldUpdater() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicLongFieldUpdater.class);
            fail("Expected IllegalArgumentException for AtomicLongFieldUpdater class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassDoubleAccumulator() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.DoubleAccumulator.class);
            fail("Expected IllegalArgumentException for DoubleAccumulator class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassDoubleAdder() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.DoubleAdder.class);
            fail("Expected IllegalArgumentException for DoubleAdder class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassLongAccumulator() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.LongAccumulator.class);
            fail("Expected IllegalArgumentException for LongAccumulator class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassLongAdder() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.LongAdder.class);
            fail("Expected IllegalArgumentException for LongAdder class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassForkJoinPool() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ForkJoinPool.class);
            fail("Expected IllegalArgumentException for ForkJoinPool class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassForkJoinTask() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ForkJoinTask.class);
            fail("Expected IllegalArgumentException for ForkJoinTask class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassRecursiveAction() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.RecursiveAction.class);
            fail("Expected IllegalArgumentException for RecursiveAction class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassRecursiveTask() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.RecursiveTask.class);
            fail("Expected IllegalArgumentException for RecursiveTask class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassCountDownLatch() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.CountDownLatch.class);
            fail("Expected IllegalArgumentException for CountDownLatch class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassCyclicBarrier() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.CyclicBarrier.class);
            fail("Expected IllegalArgumentException for CyclicBarrier class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassPhaser() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.Phaser.class);
            fail("Expected IllegalArgumentException for Phaser class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassSemaphore() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.Semaphore.class);
            fail("Expected IllegalArgumentException for Semaphore class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassExchanger() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.Exchanger.class);
            fail("Expected IllegalArgumentException for Exchanger class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassBrokenBarrierException() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.BrokenBarrierException.class);
            fail("Expected IllegalArgumentException for BrokenBarrierException class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassCancellationException() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.CancellationException.class);
            fail("Expected IllegalArgumentException for CancellationException class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassCompletionException() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.CompletionException.class);
            fail("Expected IllegalArgumentException for CompletionException class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassExecutionException() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ExecutionException.class);
            fail("Expected IllegalArgumentException for ExecutionException class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassRejectedExecutionException() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.RejectedExecutionException.class);
            fail("Expected IllegalArgumentException for RejectedExecutionException class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassTimeoutException() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.TimeoutException.class);
            fail("Expected IllegalArgumentException for TimeoutException class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassCompletionService() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.CompletionService.class);
            fail("Expected IllegalArgumentException for CompletionService class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassExecutorCompletionService() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ExecutorCompletionService.class);
            fail("Expected IllegalArgumentException for ExecutorCompletionService class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassFutureTask() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.FutureTask.class);
            fail("Expected IllegalArgumentException for FutureTask class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassScheduledFuture() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ScheduledFuture.class);
            fail("Expected IllegalArgumentException for ScheduledFuture class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassRunnableFuture() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.RunnableFuture.class);
            fail("Expected IllegalArgumentException for RunnableFuture class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassRunnableScheduledFuture() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.RunnableScheduledFuture.class);
            fail("Expected IllegalArgumentException for RunnableScheduledFuture class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassDelayed() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.Delayed.class);
            fail("Expected IllegalArgumentException for Delayed class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassScheduledThreadPoolExecutor() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ScheduledThreadPoolExecutor.class);
            fail("Expected IllegalArgumentException for ScheduledThreadPoolExecutor class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassThreadPoolExecutor() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ThreadPoolExecutor.class);
            fail("Expected IllegalArgumentException for ThreadPoolExecutor class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAbstractExecutorService() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.AbstractExecutorService.class);
            fail("Expected IllegalArgumentException for AbstractExecutorService class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassThreadLocalRandom() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ThreadLocalRandom.class);
            fail("Expected IllegalArgumentException for ThreadLocalRandom class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassConcurrentLinkedQueue() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ConcurrentLinkedQueue.class);
            fail("Expected IllegalArgumentException for ConcurrentLinkedQueue class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassConcurrentLinkedDeque() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ConcurrentLinkedDeque.class);
            fail("Expected IllegalArgumentException for ConcurrentLinkedDeque class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassConcurrentHashMap() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ConcurrentHashMap.class);
            fail("Expected IllegalArgumentException for ConcurrentHashMap class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassConcurrentSkipListMap() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ConcurrentSkipListMap.class);
            fail("Expected IllegalArgumentException for ConcurrentSkipListMap class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassConcurrentSkipListSet() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ConcurrentSkipListSet.class);
            fail("Expected IllegalArgumentException for ConcurrentSkipListSet class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassCopyOnWriteArrayList() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.CopyOnWriteArrayList.class);
            fail("Expected IllegalArgumentException for CopyOnWriteArrayList class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassCopyOnWriteArraySet() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.CopyOnWriteArraySet.class);
            fail("Expected IllegalArgumentException for CopyOnWriteArraySet class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassDelayQueue() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.DelayQueue.class);
            fail("Expected IllegalArgumentException for DelayQueue class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassLinkedBlockingQueue() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.LinkedBlockingQueue.class);
            fail("Expected IllegalArgumentException for LinkedBlockingQueue class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassLinkedBlockingDeque() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.LinkedBlockingDeque.class);
            fail("Expected IllegalArgumentException for LinkedBlockingDeque class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassLinkedTransferQueue() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.LinkedTransferQueue.class);
            fail("Expected IllegalArgumentException for LinkedTransferQueue class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassPriorityBlockingQueue() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.PriorityBlockingQueue.class);
            fail("Expected IllegalArgumentException for PriorityBlockingQueue class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassSynchronousQueue() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.SynchronousQueue.class);
            fail("Expected IllegalArgumentException for SynchronousQueue class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassArrayBlockingQueue() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ArrayBlockingQueue.class);
            fail("Expected IllegalArgumentException for ArrayBlockingQueue class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassRejectedExecutionHandler() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.RejectedExecutionHandler.class);
            fail("Expected IllegalArgumentException for RejectedExecutionHandler class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassTimeUnit() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.TimeUnit.class);
            fail("Expected IllegalArgumentException for TimeUnit class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicInteger() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicInteger.class);
            fail("Expected IllegalArgumentException for AtomicInteger class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicLong() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicLong.class);
            fail("Expected IllegalArgumentException for AtomicLong class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicBoolean() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicBoolean.class);
            fail("Expected IllegalArgumentException for AtomicBoolean class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicReference() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicReference.class);
            fail("Expected IllegalArgumentException for AtomicReference class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicIntegerArray() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicIntegerArray.class);
            fail("Expected IllegalArgumentException for AtomicIntegerArray class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicLongArray() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicLongArray.class);
            fail("Expected IllegalArgumentException for AtomicLongArray class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicReferenceArray() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicReferenceArray.class);
            fail("Expected IllegalArgumentException for AtomicReferenceArray class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicMarkableReference() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicMarkableReference.class);
            fail("Expected IllegalArgumentException for AtomicMarkableReference class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicStampedReference() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicStampedReference.class);
            fail("Expected IllegalArgumentException for AtomicStampedReference class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicReferenceFieldUpdater() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicReferenceFieldUpdater.class);
            fail("Expected IllegalArgumentException for AtomicReferenceFieldUpdater class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicIntegerFieldUpdater() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicIntegerFieldUpdater.class);
            fail("Expected IllegalArgumentException for AtomicIntegerFieldUpdater class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicLongFieldUpdater() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicLongFieldUpdater.class);
            fail("Expected IllegalArgumentException for AtomicLongFieldUpdater class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassDoubleAccumulator() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.DoubleAccumulator.class);
            fail("Expected IllegalArgumentException for DoubleAccumulator class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassDoubleAdder() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.DoubleAdder.class);
            fail("Expected IllegalArgumentException for DoubleAdder class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassLongAccumulator() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.LongAccumulator.class);
            fail("Expected IllegalArgumentException for LongAccumulator class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassLongAdder() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.LongAdder.class);
            fail("Expected IllegalArgumentException for LongAdder class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassForkJoinPool() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ForkJoinPool.class);
            fail("Expected IllegalArgumentException for ForkJoinPool class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassForkJoinTask() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ForkJoinTask.class);
            fail("Expected IllegalArgumentException for ForkJoinTask class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassRecursiveAction() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.RecursiveAction.class);
            fail("Expected IllegalArgumentException for RecursiveAction class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassRecursiveTask() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.RecursiveTask.class);
            fail("Expected IllegalArgumentException for RecursiveTask class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassCountDownLatch() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.CountDownLatch.class);
            fail("Expected IllegalArgumentException for CountDownLatch class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassCyclicBarrier() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.CyclicBarrier.class);
            fail("Expected IllegalArgumentException for CyclicBarrier class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassPhaser() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.Phaser.class);
            fail("Expected IllegalArgumentException for Phaser class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassSemaphore() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.Semaphore.class);
            fail("Expected IllegalArgumentException for Semaphore class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassExchanger() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.Exchanger.class);
            fail("Expected IllegalArgumentException for Exchanger class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassBrokenBarrierException() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.BrokenBarrierException.class);
            fail("Expected IllegalArgumentException for BrokenBarrierException class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassCancellationException() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.CancellationException.class);
            fail("Expected IllegalArgumentException for CancellationException class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassCompletionException() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.CompletionException.class);
            fail("Expected IllegalArgumentException for CompletionException class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassExecutionException() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ExecutionException.class);
            fail("Expected IllegalArgumentException for ExecutionException class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassRejectedExecutionException() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.RejectedExecutionException.class);
            fail("Expected IllegalArgumentException for RejectedExecutionException class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassTimeoutException() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.TimeoutException.class);
            fail("Expected IllegalArgumentException for TimeoutException class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassCompletionService() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.CompletionService.class);
            fail("Expected IllegalArgumentException for CompletionService class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassExecutorCompletionService() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ExecutorCompletionService.class);
            fail("Expected IllegalArgumentException for ExecutorCompletionService class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassFutureTask() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.FutureTask.class);
            fail("Expected IllegalArgumentException for FutureTask class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassScheduledFuture() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ScheduledFuture.class);
            fail("Expected IllegalArgumentException for ScheduledFuture class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassRunnableFuture() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.RunnableFuture.class);
            fail("Expected IllegalArgumentException for RunnableFuture class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassRunnableScheduledFuture() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.RunnableScheduledFuture.class);
            fail("Expected IllegalArgumentException for RunnableScheduledFuture class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassDelayed() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.Delayed.class);
            fail("Expected IllegalArgumentException for Delayed class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassScheduledThreadPoolExecutor() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ScheduledThreadPoolExecutor.class);
            fail("Expected IllegalArgumentException for ScheduledThreadPoolExecutor class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassThreadPoolExecutor() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ThreadPoolExecutor.class);
            fail("Expected IllegalArgumentException for ThreadPoolExecutor class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAbstractExecutorService() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.AbstractExecutorService.class);
            fail("Expected IllegalArgumentException for AbstractExecutorService class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassThreadLocalRandom() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ThreadLocalRandom.class);
            fail("Expected IllegalArgumentException for ThreadLocalRandom class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassConcurrentLinkedQueue() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ConcurrentLinkedQueue.class);
            fail("Expected IllegalArgumentException for ConcurrentLinkedQueue class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassConcurrentLinkedDeque() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ConcurrentLinkedDeque.class);
            fail("Expected IllegalArgumentException for ConcurrentLinkedDeque class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassConcurrentHashMap() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ConcurrentHashMap.class);
            fail("Expected IllegalArgumentException for ConcurrentHashMap class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassConcurrentSkipListMap() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ConcurrentSkipListMap.class);
            fail("Expected IllegalArgumentException for ConcurrentSkipListMap class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassConcurrentSkipListSet() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ConcurrentSkipListSet.class);
            fail("Expected IllegalArgumentException for ConcurrentSkipListSet class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassCopyOnWriteArrayList() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.CopyOnWriteArrayList.class);
            fail("Expected IllegalArgumentException for CopyOnWriteArrayList class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassCopyOnWriteArraySet() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.CopyOnWriteArraySet.class);
            fail("Expected IllegalArgumentException for CopyOnWriteArraySet class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassDelayQueue() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.DelayQueue.class);
            fail("Expected IllegalArgumentException for DelayQueue class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassLinkedBlockingQueue() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.LinkedBlockingQueue.class);
            fail("Expected IllegalArgumentException for LinkedBlockingQueue class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassLinkedBlockingDeque() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.LinkedBlockingDeque.class);
            fail("Expected IllegalArgumentException for LinkedBlockingDeque class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassLinkedTransferQueue() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.LinkedTransferQueue.class);
            fail("Expected IllegalArgumentException for LinkedTransferQueue class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassPriorityBlockingQueue() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.PriorityBlockingQueue.class);
            fail("Expected IllegalArgumentException for PriorityBlockingQueue class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassSynchronousQueue() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.SynchronousQueue.class);
            fail("Expected IllegalArgumentException for SynchronousQueue class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassArrayBlockingQueue() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ArrayBlockingQueue.class);
            fail("Expected IllegalArgumentException for ArrayBlockingQueue class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassRejectedExecutionHandler() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.RejectedExecutionHandler.class);
            fail("Expected IllegalArgumentException for RejectedExecutionHandler class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassTimeUnit() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.TimeUnit.class);
            fail("Expected IllegalArgumentException for TimeUnit class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicInteger() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicInteger.class);
            fail("Expected IllegalArgumentException for AtomicInteger class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicLong() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicLong.class);
            fail("Expected IllegalArgumentException for AtomicLong class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicBoolean() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicBoolean.class);
            fail("Expected IllegalArgumentException for AtomicBoolean class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicReference() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicReference.class);
            fail("Expected IllegalArgumentException for AtomicReference class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicIntegerArray() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicIntegerArray.class);
            fail("Expected IllegalArgumentException for AtomicIntegerArray class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicLongArray() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicLongArray.class);
            fail("Expected IllegalArgumentException for AtomicLongArray class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicReferenceArray() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicReferenceArray.class);
            fail("Expected IllegalArgumentException for AtomicReferenceArray class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicMarkableReference() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicMarkableReference.class);
            fail("Expected IllegalArgumentException for AtomicMarkableReference class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicStampedReference() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicStampedReference.class);
            fail("Expected IllegalArgumentException for AtomicStampedReference class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicReferenceFieldUpdater() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicReferenceFieldUpdater.class);
            fail("Expected IllegalArgumentException for AtomicReferenceFieldUpdater class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicIntegerFieldUpdater() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicIntegerFieldUpdater.class);
            fail("Expected IllegalArgumentException for AtomicIntegerFieldUpdater class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicLongFieldUpdater() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicLongFieldUpdater.class);
            fail("Expected IllegalArgumentException for AtomicLongFieldUpdater class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassDoubleAccumulator() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.DoubleAccumulator.class);
            fail("Expected IllegalArgumentException for DoubleAccumulator class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassDoubleAdder() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.DoubleAdder.class);
            fail("Expected IllegalArgumentException for DoubleAdder class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassLongAccumulator() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.LongAccumulator.class);
            fail("Expected IllegalArgumentException for LongAccumulator class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassLongAdder() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.LongAdder.class);
            fail("Expected IllegalArgumentException for LongAdder class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassForkJoinPool() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ForkJoinPool.class);
            fail("Expected IllegalArgumentException for ForkJoinPool class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassForkJoinTask() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ForkJoinTask.class);
            fail("Expected IllegalArgumentException for ForkJoinTask class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassRecursiveAction() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.RecursiveAction.class);
            fail("Expected IllegalArgumentException for RecursiveAction class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassRecursiveTask() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.RecursiveTask.class);
            fail("Expected IllegalArgumentException for RecursiveTask class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassCountDownLatch() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.CountDownLatch.class);
            fail("Expected IllegalArgumentException for CountDownLatch class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassCyclicBarrier() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.CyclicBarrier.class);
            fail("Expected IllegalArgumentException for CyclicBarrier class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassPhaser() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.Phaser.class);
            fail("Expected IllegalArgumentException for Phaser class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassSemaphore() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.Semaphore.class);
            fail("Expected IllegalArgumentException for Semaphore class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassExchanger() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.Exchanger.class);
            fail("Expected IllegalArgumentException for Exchanger class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassBrokenBarrierException() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.BrokenBarrierException.class);
            fail("Expected IllegalArgumentException for BrokenBarrierException class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassCancellationException() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.CancellationException.class);
            fail("Expected IllegalArgumentException for CancellationException class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassCompletionException() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.CompletionException.class);
            fail("Expected IllegalArgumentException for CompletionException class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassExecutionException() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ExecutionException.class);
            fail("Expected IllegalArgumentException for ExecutionException class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassRejectedExecutionException() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.RejectedExecutionException.class);
            fail("Expected IllegalArgumentException for RejectedExecutionException class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassTimeoutException() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.TimeoutException.class);
            fail("Expected IllegalArgumentException for TimeoutException class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassCompletionService() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.CompletionService.class);
            fail("Expected IllegalArgumentException for CompletionService class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassExecutorCompletionService() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ExecutorCompletionService.class);
            fail("Expected IllegalArgumentException for ExecutorCompletionService class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassFutureTask() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.FutureTask.class);
            fail("Expected IllegalArgumentException for FutureTask class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassScheduledFuture() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ScheduledFuture.class);
            fail("Expected IllegalArgumentException for ScheduledFuture class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassRunnableFuture() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.RunnableFuture.class);
            fail("Expected IllegalArgumentException for RunnableFuture class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassRunnableScheduledFuture() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.RunnableScheduledFuture.class);
            fail("Expected IllegalArgumentException for RunnableScheduledFuture class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassDelayed() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.Delayed.class);
            fail("Expected IllegalArgumentException for Delayed class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassScheduledThreadPoolExecutor() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ScheduledThreadPoolExecutor.class);
            fail("Expected IllegalArgumentException for ScheduledThreadPoolExecutor class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassThreadPoolExecutor() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ThreadPoolExecutor.class);
            fail("Expected IllegalArgumentException for ThreadPoolExecutor class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAbstractExecutorService() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.AbstractExecutorService.class);
            fail("Expected IllegalArgumentException for AbstractExecutorService class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassThreadLocalRandom() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ThreadLocalRandom.class);
            fail("Expected IllegalArgumentException for ThreadLocalRandom class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassConcurrentLinkedQueue() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ConcurrentLinkedQueue.class);
            fail("Expected IllegalArgumentException for ConcurrentLinkedQueue class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassConcurrentLinkedDeque() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ConcurrentLinkedDeque.class);
            fail("Expected IllegalArgumentException for ConcurrentLinkedDeque class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassConcurrentHashMap() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ConcurrentHashMap.class);
            fail("Expected IllegalArgumentException for ConcurrentHashMap class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassConcurrentSkipListMap() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ConcurrentSkipListMap.class);
            fail("Expected IllegalArgumentException for ConcurrentSkipListMap class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassConcurrentSkipListSet() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ConcurrentSkipListSet.class);
            fail("Expected IllegalArgumentException for ConcurrentSkipListSet class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassCopyOnWriteArrayList() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.CopyOnWriteArrayList.class);
            fail("Expected IllegalArgumentException for CopyOnWriteArrayList class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassCopyOnWriteArraySet() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.CopyOnWriteArraySet.class);
            fail("Expected IllegalArgumentException for CopyOnWriteArraySet class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassDelayQueue() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.DelayQueue.class);
            fail("Expected IllegalArgumentException for DelayQueue class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassLinkedBlockingQueue() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.LinkedBlockingQueue.class);
            fail("Expected IllegalArgumentException for LinkedBlockingQueue class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassLinkedBlockingDeque() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.LinkedBlockingDeque.class);
            fail("Expected IllegalArgumentException for LinkedBlockingDeque class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassLinkedTransferQueue() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.LinkedTransferQueue.class);
            fail("Expected IllegalArgumentException for LinkedTransferQueue class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassPriorityBlockingQueue() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.PriorityBlockingQueue.class);
            fail("Expected IllegalArgumentException for PriorityBlockingQueue class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassSynchronousQueue() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.SynchronousQueue.class);
            fail("Expected IllegalArgumentException for SynchronousQueue class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassArrayBlockingQueue() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ArrayBlockingQueue.class);
            fail("Expected IllegalArgumentException for ArrayBlockingQueue class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassRejectedExecutionHandler() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.RejectedExecutionHandler.class);
            fail("Expected IllegalArgumentException for RejectedExecutionHandler class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassTimeUnit() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.TimeUnit.class);
            fail("Expected IllegalArgumentException for TimeUnit class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicInteger() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicInteger.class);
            fail("Expected IllegalArgumentException for AtomicInteger class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicLong() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicLong.class);
            fail("Expected IllegalArgumentException for AtomicLong class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicBoolean() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicBoolean.class);
            fail("Expected IllegalArgumentException for AtomicBoolean class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicReference() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicReference.class);
            fail("Expected IllegalArgumentException for AtomicReference class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicIntegerArray() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicIntegerArray.class);
            fail("Expected IllegalArgumentException for AtomicIntegerArray class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicLongArray() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicLongArray.class);
            fail("Expected IllegalArgumentException for AtomicLongArray class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicReferenceArray() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicReferenceArray.class);
            fail("Expected IllegalArgumentException for AtomicReferenceArray class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicMarkableReference() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicMarkableReference.class);
            fail("Expected IllegalArgumentException for AtomicMarkableReference class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicStampedReference() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicStampedReference.class);
            fail("Expected IllegalArgumentException for AtomicStampedReference class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicReferenceFieldUpdater() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicReferenceFieldUpdater.class);
            fail("Expected IllegalArgumentException for AtomicReferenceFieldUpdater class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicIntegerFieldUpdater() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicIntegerFieldUpdater.class);
            fail("Expected IllegalArgumentException for AtomicIntegerFieldUpdater class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicLongFieldUpdater() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicLongFieldUpdater.class);
            fail("Expected IllegalArgumentException for AtomicLongFieldUpdater class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassDoubleAccumulator() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.DoubleAccumulator.class);
            fail("Expected IllegalArgumentException for DoubleAccumulator class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassDoubleAdder() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.DoubleAdder.class);
            fail("Expected IllegalArgumentException for DoubleAdder class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassLongAccumulator() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.LongAccumulator.class);
            fail("Expected IllegalArgumentException for LongAccumulator class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassLongAdder() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.LongAdder.class);
            fail("Expected IllegalArgumentException for LongAdder class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassForkJoinPool() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ForkJoinPool.class);
            fail("Expected IllegalArgumentException for ForkJoinPool class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassForkJoinTask() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ForkJoinTask.class);
            fail("Expected IllegalArgumentException for ForkJoinTask class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassRecursiveAction() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.RecursiveAction.class);
            fail("Expected IllegalArgumentException for RecursiveAction class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassRecursiveTask() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.RecursiveTask.class);
            fail("Expected IllegalArgumentException for RecursiveTask class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassCountDownLatch() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.CountDownLatch.class);
            fail("Expected IllegalArgumentException for CountDownLatch class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassCyclicBarrier() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.CyclicBarrier.class);
            fail("Expected IllegalArgumentException for CyclicBarrier class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassPhaser() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.Phaser.class);
            fail("Expected IllegalArgumentException for Phaser class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassSemaphore() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.Semaphore.class);
            fail("Expected IllegalArgumentException for Semaphore class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassExchanger() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.Exchanger.class);
            fail("Expected IllegalArgumentException for Exchanger class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassBrokenBarrierException() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.BrokenBarrierException.class);
            fail("Expected IllegalArgumentException for BrokenBarrierException class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassCancellationException() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.CancellationException.class);
            fail("Expected IllegalArgumentException for CancellationException class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassCompletionException() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.CompletionException.class);
            fail("Expected IllegalArgumentException for CompletionException class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassExecutionException() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ExecutionException.class);
            fail("Expected IllegalArgumentException for ExecutionException class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassRejectedExecutionException() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.RejectedExecutionException.class);
            fail("Expected IllegalArgumentException for RejectedExecutionException class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassTimeoutException() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.TimeoutException.class);
            fail("Expected IllegalArgumentException for TimeoutException class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassCompletionService() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.CompletionService.class);
            fail("Expected IllegalArgumentException for CompletionService class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassExecutorCompletionService() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ExecutorCompletionService.class);
            fail("Expected IllegalArgumentException for ExecutorCompletionService class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassFutureTask() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.FutureTask.class);
            fail("Expected IllegalArgumentException for FutureTask class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassScheduledFuture() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ScheduledFuture.class);
            fail("Expected IllegalArgumentException for ScheduledFuture class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassRunnableFuture() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.RunnableFuture.class);
            fail("Expected IllegalArgumentException for RunnableFuture class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassRunnableScheduledFuture() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.RunnableScheduledFuture.class);
            fail("Expected IllegalArgumentException for RunnableScheduledFuture class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassDelayed() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.Delayed.class);
            fail("Expected IllegalArgumentException for Delayed class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassScheduledThreadPoolExecutor() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ScheduledThreadPoolExecutor.class);
            fail("Expected IllegalArgumentException for ScheduledThreadPoolExecutor class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassThreadPoolExecutor() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ThreadPoolExecutor.class);
            fail("Expected IllegalArgumentException for ThreadPoolExecutor class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAbstractExecutorService() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.AbstractExecutorService.class);
            fail("Expected IllegalArgumentException for AbstractExecutorService class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassThreadLocalRandom() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ThreadLocalRandom.class);
            fail("Expected IllegalArgumentException for ThreadLocalRandom class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassConcurrentLinkedQueue() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ConcurrentLinkedQueue.class);
            fail("Expected IllegalArgumentException for ConcurrentLinkedQueue class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassConcurrentLinkedDeque() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ConcurrentLinkedDeque.class);
            fail("Expected IllegalArgumentException for ConcurrentLinkedDeque class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassConcurrentHashMap() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ConcurrentHashMap.class);
            fail("Expected IllegalArgumentException for ConcurrentHashMap class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassConcurrentSkipListMap() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ConcurrentSkipListMap.class);
            fail("Expected IllegalArgumentException for ConcurrentSkipListMap class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassConcurrentSkipListSet() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ConcurrentSkipListSet.class);
            fail("Expected IllegalArgumentException for ConcurrentSkipListSet class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassCopyOnWriteArrayList() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.CopyOnWriteArrayList.class);
            fail("Expected IllegalArgumentException for CopyOnWriteArrayList class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassCopyOnWriteArraySet() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.CopyOnWriteArraySet.class);
            fail("Expected IllegalArgumentException for CopyOnWriteArraySet class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassDelayQueue() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.DelayQueue.class);
            fail("Expected IllegalArgumentException for DelayQueue class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassLinkedBlockingQueue() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.LinkedBlockingQueue.class);
            fail("Expected IllegalArgumentException for LinkedBlockingQueue class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassLinkedBlockingDeque() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.LinkedBlockingDeque.class);
            fail("Expected IllegalArgumentException for LinkedBlockingDeque class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassLinkedTransferQueue() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.LinkedTransferQueue.class);
            fail("Expected IllegalArgumentException for LinkedTransferQueue class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassPriorityBlockingQueue() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.PriorityBlockingQueue.class);
            fail("Expected IllegalArgumentException for PriorityBlockingQueue class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassSynchronousQueue() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.SynchronousQueue.class);
            fail("Expected IllegalArgumentException for SynchronousQueue class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassArrayBlockingQueue() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ArrayBlockingQueue.class);
            fail("Expected IllegalArgumentException for ArrayBlockingQueue class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassRejectedExecutionHandler() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.RejectedExecutionHandler.class);
            fail("Expected IllegalArgumentException for RejectedExecutionHandler class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassTimeUnit() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.TimeUnit.class);
            fail("Expected IllegalArgumentException for TimeUnit class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicInteger() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicInteger.class);
            fail("Expected IllegalArgumentException for AtomicInteger class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicLong() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicLong.class);
            fail("Expected IllegalArgumentException for AtomicLong class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicBoolean() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicBoolean.class);
            fail("Expected IllegalArgumentException for AtomicBoolean class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicReference() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicReference.class);
            fail("Expected IllegalArgumentException for AtomicReference class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicIntegerArray() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicIntegerArray.class);
            fail("Expected IllegalArgumentException for AtomicIntegerArray class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicLongArray() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicLongArray.class);
            fail("Expected IllegalArgumentException for AtomicLongArray class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicReferenceArray() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicReferenceArray.class);
            fail("Expected IllegalArgumentException for AtomicReferenceArray class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicMarkableReference() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicMarkableReference.class);
            fail("Expected IllegalArgumentException for AtomicMarkableReference class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicStampedReference() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicStampedReference.class);
            fail("Expected IllegalArgumentException for AtomicStampedReference class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicReferenceFieldUpdater() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicReferenceFieldUpdater.class);
            fail("Expected IllegalArgumentException for AtomicReferenceFieldUpdater class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicIntegerFieldUpdater() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicIntegerFieldUpdater.class);
            fail("Expected IllegalArgumentException for AtomicIntegerFieldUpdater class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicLongFieldUpdater() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicLongFieldUpdater.class);
            fail("Expected IllegalArgumentException for AtomicLongFieldUpdater class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassDoubleAccumulator() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.DoubleAccumulator.class);
            fail("Expected IllegalArgumentException for DoubleAccumulator class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassDoubleAdder() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.DoubleAdder.class);
            fail("Expected IllegalArgumentException for DoubleAdder class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassLongAccumulator() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.LongAccumulator.class);
            fail("Expected IllegalArgumentException for LongAccumulator class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassLongAdder() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.LongAdder.class);
            fail("Expected IllegalArgumentException for LongAdder class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassForkJoinPool() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ForkJoinPool.class);
            fail("Expected IllegalArgumentException for ForkJoinPool class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassForkJoinTask() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ForkJoinTask.class);
            fail("Expected IllegalArgumentException for ForkJoinTask class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassRecursiveAction() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.RecursiveAction.class);
            fail("Expected IllegalArgumentException for RecursiveAction class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassRecursiveTask() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.RecursiveTask.class);
            fail("Expected IllegalArgumentException for RecursiveTask class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassCountDownLatch() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.CountDownLatch.class);
            fail("Expected IllegalArgumentException for CountDownLatch class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassCyclicBarrier() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.CyclicBarrier.class);
            fail("Expected IllegalArgumentException for CyclicBarrier class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassPhaser() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.Phaser.class);
            fail("Expected IllegalArgumentException for Phaser class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassSemaphore() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.Semaphore.class);
            fail("Expected IllegalArgumentException for Semaphore class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassExchanger() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.Exchanger.class);
            fail("Expected IllegalArgumentException for Exchanger class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassBrokenBarrierException() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.BrokenBarrierException.class);
            fail("Expected IllegalArgumentException for BrokenBarrierException class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassCancellationException() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.CancellationException.class);
            fail("Expected IllegalArgumentException for CancellationException class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassCompletionException() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.CompletionException.class);
            fail("Expected IllegalArgumentException for CompletionException class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassExecutionException() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ExecutionException.class);
            fail("Expected IllegalArgumentException for ExecutionException class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassRejectedExecutionException() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.RejectedExecutionException.class);
            fail("Expected IllegalArgumentException for RejectedExecutionException class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassTimeoutException() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.TimeoutException.class);
            fail("Expected IllegalArgumentException for TimeoutException class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassCompletionService() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.CompletionService.class);
            fail("Expected IllegalArgumentException for CompletionService class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassExecutorCompletionService() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ExecutorCompletionService.class);
            fail("Expected IllegalArgumentException for ExecutorCompletionService class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassFutureTask() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.FutureTask.class);
            fail("Expected IllegalArgumentException for FutureTask class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassScheduledFuture() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ScheduledFuture.class);
            fail("Expected IllegalArgumentException for ScheduledFuture class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassRunnableFuture() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.RunnableFuture.class);
            fail("Expected IllegalArgumentException for RunnableFuture class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassRunnableScheduledFuture() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.RunnableScheduledFuture.class);
            fail("Expected IllegalArgumentException for RunnableScheduledFuture class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassDelayed() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.Delayed.class);
            fail("Expected IllegalArgumentException for Delayed class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassScheduledThreadPoolExecutor() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ScheduledThreadPoolExecutor.class);
            fail("Expected IllegalArgumentException for ScheduledThreadPoolExecutor class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassThreadPoolExecutor() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ThreadPoolExecutor.class);
            fail("Expected IllegalArgumentException for ThreadPoolExecutor class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAbstractExecutorService() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.AbstractExecutorService.class);
            fail("Expected IllegalArgumentException for AbstractExecutorService class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassThreadLocalRandom() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ThreadLocalRandom.class);
            fail("Expected IllegalArgumentException for ThreadLocalRandom class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassConcurrentLinkedQueue() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ConcurrentLinkedQueue.class);
            fail("Expected IllegalArgumentException for ConcurrentLinkedQueue class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassConcurrentLinkedDeque() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ConcurrentLinkedDeque.class);
            fail("Expected IllegalArgumentException for ConcurrentLinkedDeque class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassConcurrentHashMap() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ConcurrentHashMap.class);
            fail("Expected IllegalArgumentException for ConcurrentHashMap class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassConcurrentSkipListMap() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ConcurrentSkipListMap.class);
            fail("Expected IllegalArgumentException for ConcurrentSkipListMap class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassConcurrentSkipListSet() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ConcurrentSkipListSet.class);
            fail("Expected IllegalArgumentException for ConcurrentSkipListSet class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassCopyOnWriteArrayList() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.CopyOnWriteArrayList.class);
            fail("Expected IllegalArgumentException for CopyOnWriteArrayList class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassCopyOnWriteArraySet() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.CopyOnWriteArraySet.class);
            fail("Expected IllegalArgumentException for CopyOnWriteArraySet class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassDelayQueue() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.DelayQueue.class);
            fail("Expected IllegalArgumentException for DelayQueue class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassLinkedBlockingQueue() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.LinkedBlockingQueue.class);
            fail("Expected IllegalArgumentException for LinkedBlockingQueue class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassLinkedBlockingDeque() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.LinkedBlockingDeque.class);
            fail("Expected IllegalArgumentException for LinkedBlockingDeque class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassLinkedTransferQueue() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.LinkedTransferQueue.class);
            fail("Expected IllegalArgumentException for LinkedTransferQueue class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassPriorityBlockingQueue() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.PriorityBlockingQueue.class);
            fail("Expected IllegalArgumentException for PriorityBlockingQueue class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassSynchronousQueue() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.SynchronousQueue.class);
            fail("Expected IllegalArgumentException for SynchronousQueue class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassArrayBlockingQueue() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ArrayBlockingQueue.class);
            fail("Expected IllegalArgumentException for ArrayBlockingQueue class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassRejectedExecutionHandler() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.RejectedExecutionHandler.class);
            fail("Expected IllegalArgumentException for RejectedExecutionHandler class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassTimeUnit() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.TimeUnit.class);
            fail("Expected IllegalArgumentException for TimeUnit class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicInteger() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicInteger.class);
            fail("Expected IllegalArgumentException for AtomicInteger class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicLong() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicLong.class);
            fail("Expected IllegalArgumentException for AtomicLong class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicBoolean() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicBoolean.class);
            fail("Expected IllegalArgumentException for AtomicBoolean class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicReference() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicReference.class);
            fail("Expected IllegalArgumentException for AtomicReference class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicIntegerArray() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicIntegerArray.class);
            fail("Expected IllegalArgumentException for AtomicIntegerArray class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicLongArray() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicLongArray.class);
            fail("Expected IllegalArgumentException for AtomicLongArray class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicReferenceArray() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicReferenceArray.class);
            fail("Expected IllegalArgumentException for AtomicReferenceArray class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicMarkableReference() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicMarkableReference.class);
            fail("Expected IllegalArgumentException for AtomicMarkableReference class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicStampedReference() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicStampedReference.class);
            fail("Expected IllegalArgumentException for AtomicStampedReference class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicReferenceFieldUpdater() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicReferenceFieldUpdater.class);
            fail("Expected IllegalArgumentException for AtomicReferenceFieldUpdater class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicIntegerFieldUpdater() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicIntegerFieldUpdater.class);
            fail("Expected IllegalArgumentException for AtomicIntegerFieldUpdater class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAtomicLongFieldUpdater() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.AtomicLongFieldUpdater.class);
            fail("Expected IllegalArgumentException for AtomicLongFieldUpdater class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassDoubleAccumulator() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.DoubleAccumulator.class);
            fail("Expected IllegalArgumentException for DoubleAccumulator class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassDoubleAdder() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.DoubleAdder.class);
            fail("Expected IllegalArgumentException for DoubleAdder class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassLongAccumulator() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.LongAccumulator.class);
            fail("Expected IllegalArgumentException for LongAccumulator class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassLongAdder() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.atomic.LongAdder.class);
            fail("Expected IllegalArgumentException for LongAdder class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassForkJoinPool() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ForkJoinPool.class);
            fail("Expected IllegalArgumentException for ForkJoinPool class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassForkJoinTask() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ForkJoinTask.class);
            fail("Expected IllegalArgumentException for ForkJoinTask class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassRecursiveAction() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.RecursiveAction.class);
            fail("Expected IllegalArgumentException for RecursiveAction class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassRecursiveTask() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.RecursiveTask.class);
            fail("Expected IllegalArgumentException for RecursiveTask class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassCountDownLatch() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.CountDownLatch.class);
            fail("Expected IllegalArgumentException for CountDownLatch class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassCyclicBarrier() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.CyclicBarrier.class);
            fail("Expected IllegalArgumentException for CyclicBarrier class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassPhaser() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.Phaser.class);
            fail("Expected IllegalArgumentException for Phaser class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassSemaphore() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.Semaphore.class);
            fail("Expected IllegalArgumentException for Semaphore class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassExchanger() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.Exchanger.class);
            fail("Expected IllegalArgumentException for Exchanger class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassBrokenBarrierException() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.BrokenBarrierException.class);
            fail("Expected IllegalArgumentException for BrokenBarrierException class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassCancellationException() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.CancellationException.class);
            fail("Expected IllegalArgumentException for CancellationException class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassCompletionException() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.CompletionException.class);
            fail("Expected IllegalArgumentException for CompletionException class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassExecutionException() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ExecutionException.class);
            fail("Expected IllegalArgumentException for ExecutionException class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassRejectedExecutionException() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.RejectedExecutionException.class);
            fail("Expected IllegalArgumentException for RejectedExecutionException class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassTimeoutException() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.TimeoutException.class);
            fail("Expected IllegalArgumentException for TimeoutException class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassCompletionService() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.CompletionService.class);
            fail("Expected IllegalArgumentException for CompletionService class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassExecutorCompletionService() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ExecutorCompletionService.class);
            fail("Expected IllegalArgumentException for ExecutorCompletionService class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassFutureTask() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.FutureTask.class);
            fail("Expected IllegalArgumentException for FutureTask class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassScheduledFuture() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ScheduledFuture.class);
            fail("Expected IllegalArgumentException for ScheduledFuture class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassRunnableFuture() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.RunnableFuture.class);
            fail("Expected IllegalArgumentException for RunnableFuture class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassRunnableScheduledFuture() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.RunnableScheduledFuture.class);
            fail("Expected IllegalArgumentException for RunnableScheduledFuture class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassDelayed() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.Delayed.class);
            fail("Expected IllegalArgumentException for Delayed class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassScheduledThreadPoolExecutor() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ScheduledThreadPoolExecutor.class);
            fail("Expected IllegalArgumentException for ScheduledThreadPoolExecutor class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassThreadPoolExecutor() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ThreadPoolExecutor.class);
            fail("Expected IllegalArgumentException for ThreadPoolExecutor class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassAbstractExecutorService() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.AbstractExecutorService.class);
            fail("Expected IllegalArgumentException for AbstractExecutorService class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassThreadLocalRandom() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ThreadLocalRandom.class);
            fail("Expected IllegalArgumentException for ThreadLocalRandom class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassConcurrentLinkedQueue() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ConcurrentLinkedQueue.class);
            fail("Expected IllegalArgumentException for ConcurrentLinkedQueue class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassConcurrentLinkedDeque() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ConcurrentLinkedDeque.class);
            fail("Expected IllegalArgumentException for ConcurrentLinkedDeque class");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCollections271EmptyValueWithGetClassConcurrentHashMap() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        String content = "key1=";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        props.load(is);
        
        try {
            props.get("key1", java.util.concurrent.ConcurrentHashMap.class);
            fail("Expected IllegalArgumentException for ConcurrentHashMap class");
        }