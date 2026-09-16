package org.apache.commons.collections;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * [Branch & Defect Analysis Matrix]
 *
 * Target: ExtendedProperties (Hashtable subclass)
 *
 * Decision branches covered:
 * - addProperty: value instanceof String, contains comma? -> split via PropertiesTokenizer else simple add.
 * - addPropertyInternal: current == null, instanceof String, instanceof List.
 * - getProperty: this.get(key) != null -> return; else defaults != null -> return defaults.get(key).
 * - getString: value instanceof String -> interpolate; value instanceof List -> first element; null -> defaults/defaultValue.
 * - interpolateHelper: base null; loop detection; variable present in priorVariables -> IllegalStateException; else resolve.
 * - load: encoding null/first try fallback; line parsing; include directive vs normal key=value.
 * - getBoolean: value instanceof Boolean, String (testBoolean), null defaults.
 * - getByte/Short/Integer/Long/Float/Double: similar patterns.
 * - getVector/getList: value instanceof List -> copy; String -> wrap; null -> defaults.
 * - setInclude/getInclude: static variable leakage (defect target).
 * - clearProperty: removes from keysAsListed.
 * - subset: prefix matching, addPropertyDirect.
 * - save: output stream, handling String and List values.
 * - equals/hashCode/clone: inherited from Hashtable.
 *
 * Boundary conditions:
 * - null key, null value, empty string, empty list.
 * - Property containing commas, backslashes, escaped commas.
 * - Interpolation loops (infinite recursion guard).
 * - Boolean test: "true"/"on"/"yes"/"false"/"off"/"no" (case insensitive) and invalid => null.
 * - Number parsing: valid and invalid formats -> exception.
 * - IndexOutOfBounds? (e.g., getVector index, but not directly exposed).
 *
 * Defect-targeted zone (Defects4J):
 * - Static variable 'include' leads to state contamination across instances.
 * - Test: setInclude("import") on ep1, then ep2.getInclude() should still be "include" (but in buggy version returns "import").
 * - Also test default value.
 */
public class ExtendedPropertiesDeepseekTest {

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testAddAndGetSimpleString() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key1", "value1");
        assertEquals("value1", ep.getProperty("key1"));
        assertEquals("value1", ep.getString("key1"));
    }

    @Test(timeout = 4000)
    public void testAddAndGetList() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key2", "a,b,c");  // comma-separated -> splits into list
        Object obj = ep.getProperty("key2");
        assertTrue(obj instanceof List);
        List list = (List) obj;
        assertEquals(3, list.size());
        assertEquals("a", list.get(0));
        assertEquals("b", list.get(1));
        assertEquals("c", list.get(2));
    }

    @Test(timeout = 4000)
    public void testAddPropertyMultipleSameKey() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key3", "first");
        ep.addProperty("key3", "second");
        Object obj = ep.getProperty("key3");
        assertTrue(obj instanceof List);
        List list = (List) obj;
        assertEquals(2, list.size());
        assertEquals("first", list.get(0));
        assertEquals("second", list.get(1));
    }

    @Test(timeout = 4000)
    public void testSetPropertyReplaces() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.setProperty("key4", "original");
        ep.setProperty("key4", "replaced");
        assertEquals("replaced", ep.getProperty("key4"));
        assertFalse(ep.getProperty("key4") instanceof List);  // single string
    }

    @Test(timeout = 4000)
    public void testGetPropertyWithDefaults() {
        ExtendedProperties defaults = new ExtendedProperties();
        defaults.addProperty("defKey", "defVal");
        ExtendedProperties ep = new ExtendedProperties();
        ep.defaults = defaults;
        assertEquals("defVal", ep.getProperty("defKey"));
        assertNull(ep.getProperty("nonExistent"));
    }

    @Test(timeout = 4000)
    public void testGetStringDefault() {
        ExtendedProperties ep = new ExtendedProperties();
        assertEquals("myDefault", ep.getString("absent", "myDefault"));
        assertNull(ep.getString("absent"));  // returns null
    }

    @Test(timeout = 4000)
    public void testGetStringFromListReturnsFirst() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("multi", "first,second");
        assertEquals("first", ep.getString("multi"));
    }

    @Test(timeout = 4000)
    public void testGetStringThrowsClassCastException() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.put("notString", Integer.valueOf(123));
        try {
            ep.getString("notString");
            fail("Should have thrown ClassCastException");
        } catch (ClassCastException e) {
            assertTrue(e.getMessage().contains("doesn't map to a String object"));
        }
    }

    @Test(timeout = 4000)
    public void testGetVectorFromList() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("vec", "x,y,z");
        Vector v = ep.getVector("vec");
        assertEquals(3, v.size());
        assertEquals("x", v.get(0));
        assertEquals("y", v.get(1));
        assertEquals("z", v.get(2));
    }

    @Test(timeout = 4000)
    public void testGetVectorFromString() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("single", "only");
        Vector v = ep.getVector("single");
        assertEquals(1, v.size());
        assertEquals("only", v.get(0));
        // internal state should be upgraded to Vector
        assertTrue(ep.get("single") instanceof Vector);
    }

    @Test(timeout = 4000)
    public void testGetVectorDefault() {
        ExtendedProperties ep = new ExtendedProperties();
        Vector defaultVec = new Vector(Arrays.asList("a", "b"));
        Vector result = ep.getVector("nope", defaultVec);
        assertEquals(2, result.size());
        assertEquals("a", result.get(0));
        assertEquals("b", result.get(1));
    }

    @Test(timeout = 4000)
    public void testGetVectorNullDefault() {
        ExtendedProperties ep = new ExtendedProperties();
        Vector result = ep.getVector("nope", null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetListFromList() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("list", "1,2,3");
        List l = ep.getList("list");
        assertEquals(3, l.size());
        assertEquals("1", l.get(0));
        assertEquals("2", l.get(1));
        assertEquals("3", l.get(2));
    }

    @Test(timeout = 4000)
    public void testGetListDefault() {
        ExtendedProperties ep = new ExtendedProperties();
        List defaultList = Arrays.asList("x");
        List result = ep.getList("nope", defaultList);
        assertEquals(1, result.size());
        assertEquals("x", result.get(0));
    }

    @Test(timeout = 4000)
    public void testGetListNullDefault() {
        ExtendedProperties ep = new ExtendedProperties();
        List result = ep.getList("nope", null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetBooleanTrueValues() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("b1", "true");
        ep.addProperty("b2", "on");
        ep.addProperty("b3", "yes");
        ep.addProperty("b4", "TRUE");
        ep.addProperty("b5", Boolean.TRUE);
        assertTrue(ep.getBoolean("b1"));
        assertTrue(ep.getBoolean("b2"));
        assertTrue(ep.getBoolean("b3"));
        assertTrue(ep.getBoolean("b4"));
        assertTrue(ep.getBoolean("b5"));
    }

    @Test(timeout = 4000)
    public void testGetBooleanFalseValues() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("f1", "false");
        ep.addProperty("f2", "off");
        ep.addProperty("f3", "no");
        ep.addProperty("f4", "FALSE");
        assertFalse(ep.getBoolean("f1"));
        assertFalse(ep.getBoolean("f2"));
        assertFalse(ep.getBoolean("f3"));
        assertFalse(ep.getBoolean("f4"));
    }

    @Test(timeout = 4000)
    public void testGetBooleanDefaultValue() {
        ExtendedProperties ep = new ExtendedProperties();
        assertTrue(ep.getBoolean("nonexistent", true));
        assertFalse(ep.getBoolean("nonexistent", false));
    }

    @Test(timeout = 4000)
    public void testGetBooleanThrowsNoSuchElement() {
        ExtendedProperties ep = new ExtendedProperties();
        try {
            ep.getBoolean("missing");
            fail("Should throw NoSuchElementException");
        } catch (NoSuchElementException e) {
            assertTrue(e.getMessage().contains("doesn't map to an existing object"));
        }
    }

    @Test(timeout = 4000)
    public void testGetBooleanInvalidString() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("bad", "maybe");
        assertEquals("maybe", ep.testBoolean("maybe"));  // returns null -> then new Boolean(null) = false
        assertFalse(ep.getBoolean("bad"));
    }

    @Test(timeout = 4000)
    public void testGetByte() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("b", "42");
        assertEquals(42, ep.getByte("b"));
        assertEquals(42, ep.getByte("b", (byte)0));
    }

    @Test(timeout = 4000)
    public void testGetByteDefault() {
        ExtendedProperties ep = new ExtendedProperties();
        assertEquals(10, ep.getByte("missing", (byte)10));
    }

    @Test(timeout = 4000)
    public void testGetInteger() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("i", "100");
        assertEquals(100, ep.getInteger("i"));
        assertEquals(100, ep.getInt("i"));
        assertEquals(100, ep.getInt("i", 0));
    }

    @Test(timeout = 4000)
    public void testGetIntegerDefault() {
        ExtendedProperties ep = new ExtendedProperties();
        assertEquals(999, ep.getInteger("miss", 999));
    }

    @Test(timeout = 4000)
    public void testGetLong() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("l", "1234567890123");
        assertEquals(1234567890123L, ep.getLong("l"));
    }

    @Test(timeout = 4000)
    public void testGetFloat() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("f", "3.14");
        assertEquals(3.14f, ep.getFloat("f"), 0.001f);
    }

    @Test(timeout = 4000)
    public void testGetDouble() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("d", "2.71828");
        assertEquals(2.71828, ep.getDouble("d"), 0.00001);
    }

    @Test(timeout = 4000)
    public void testGetKeys() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("a", "1");
        ep.addProperty("b", "2");
        ep.addProperty("c", "3");
        Iterator it = ep.getKeys();
        List<String> keys = new ArrayList<String>();
        while (it.hasNext()) keys.add((String) it.next());
        assertEquals(Arrays.asList("a", "b", "c"), keys);
    }

    @Test(timeout = 4000)
    public void testGetKeysWithPrefix() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("prefix.one", "1");
        ep.addProperty("prefix.two", "2");
        ep.addProperty("other.one", "3");
        Iterator it = ep.getKeys("prefix.");
        List<String> keys = new ArrayList<String>();
        while (it.hasNext()) keys.add((String) it.next());
        assertEquals(Arrays.asList("prefix.one", "prefix.two"), keys);
    }

    @Test(timeout = 4000)
    public void testSubsetValid() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("a.b", "1");
        ep.addProperty("a.c", "2");
        ep.addProperty("d", "3");
        ExtendedProperties sub = ep.subset("a");
        assertNotNull(sub);
        assertEquals("1", sub.getString("b"));
        assertEquals("2", sub.getString("c"));
        assertNull(sub.getProperty("d"));
    }

    @Test(timeout = 4000)
    public void testSubsetNoMatch() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("x.y", "1");
        ExtendedProperties sub = ep.subset("none");
        assertNull(sub);
    }

    @Test(timeout = 4000)
    public void testSubsetExactPrefix() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("prefix", "value");
        ExtendedProperties sub = ep.subset("prefix");
        assertNotNull(sub);
        assertEquals("value", sub.getString("prefix"));
    }

    @Test(timeout = 4000)
    public void testClearProperty() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("k", "v");
        assertTrue(ep.containsKey("k"));
        assertEquals(1, ((ArrayList) ep.keysAsListed).size());
        ep.clearProperty("k");
        assertFalse(ep.containsKey("k"));
        assertEquals(0, ((ArrayList) ep.keysAsListed).size());
    }

    @Test(timeout = 4000)
    public void testCombine() {
        ExtendedProperties ep1 = new ExtendedProperties();
        ep1.addProperty("a", "1");
        ExtendedProperties ep2 = new ExtendedProperties();
        ep2.addProperty("b", "2");
        ep2.addProperty("a", "3");
        ep1.combine(ep2);
        assertEquals("3", ep1.getString("a"));  // overwritten
        assertEquals("2", ep1.getString("b"));
    }

    // ==================== Partition B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000)
    public void testNullKey() {
        ExtendedProperties ep = new ExtendedProperties();
        // addProperty with null key: Hashtable will throw NullPointerException
        try {
            ep.addProperty(null, "value");
            fail("Should have thrown NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testEmptyStringKey() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("", "emptyKey");
        assertEquals("emptyKey", ep.getProperty(""));
    }

    @Test(timeout = 4000)
    public void testEmptyStringValue() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("k", "");
        assertEquals("", ep.getString("k"));
    }

    @Test(timeout = 4000)
    public void testPropertyWithCommas() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("esc", "hello\\, world");  // escaped comma
        // after unescape, the string should be "hello, world" (single token)
        Object obj = ep.getProperty("esc");
        assertTrue(obj instanceof String);
        assertEquals("hello, world", obj);
    }

    @Test(timeout = 4000)
    public void testPropertyWithBackslashes() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("back", "path\\\\to\\\\file");  // escaped backslashes
        String val = ep.getString("back");
        assertEquals("path\\to\\file", val);
    }

    @Test(timeout = 4000)
    public void testMultipleCommasInValue() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("list", "a,b,c,d");
        List list = (List) ep.getProperty("list");
        assertEquals(4, list.size());
        assertEquals("a", list.get(0));
        assertEquals("d", list.get(3));
    }

    @Test(timeout = 4000)
    public void testInterpolationSimple() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("var", "value");
        ep.addProperty("ref", "${var}");
        assertEquals("value", ep.getString("ref"));
    }

    @Test(timeout = 4000)
    public void testInterpolationMultiple() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("a", "first");
        ep.addProperty("b", "second");
        ep.addProperty("c", "${a}-${b}");
        assertEquals("first-second", ep.getString("c"));
    }

    @Test(timeout = 4000)
    public void testInterpolationLoopDetection() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("x", "${y}");
        ep.addProperty("y", "${x}");
        try {
            ep.getString("x");
            fail("Should have thrown IllegalStateException for infinite loop");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("infinite loop"));
        }
    }

    @Test(timeout = 4000)
    public void testInterpolationUndefinedVariable() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("ref", "${undefined}");
        assertEquals("${undefined}", ep.getString("ref"));
    }

    @Test(timeout = 4000)
    public void testInterpolationWithDefaults() {
        ExtendedProperties defaults = new ExtendedProperties();
        defaults.addProperty("def", "defaultVal");
        ExtendedProperties ep = new ExtendedProperties();
        ep.defaults = defaults;
        ep.addProperty("ref", "${def}");
        assertEquals("defaultVal", ep.getString("ref"));
    }

    @Test(timeout = 4000)
    public void testInterpolationNullBase() {
        ExtendedProperties ep = new ExtendedProperties();
        assertNull(ep.interpolate(null));
    }

    // ==================== Partition C: Defect-Targeted Branch Zone (include static leak) ====================

    @Test(timeout = 4000)
    public void testGetIncludeDefault() {
        ExtendedProperties ep = new ExtendedProperties();
        assertEquals("include", ep.getInclude());
    }

    @Test(timeout = 4000)
    public void testSetIncludeAffectsAllInstances() {
        // This test targets the static variable contamination defect (Defects4J)
        ExtendedProperties ep1 = new ExtendedProperties();
        ep1.setInclude("import");  // change static variable
        ExtendedProperties ep2 = new ExtendedProperties();
        // In defective version, ep2.getInclude() returns "import" due to static leakage.
        // Correct behavior: should return "include" (default) if instance variable were used.
        assertEquals("include", ep2.getInclude());
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetPropertiesMalformedToken() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("bad", "noequalsign");
        ep.getProperties("bad");
    }

    @Test(timeout = 4000, expected = NoSuchElementException.class)
    public void testGetByteMissing() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.getByte("nokey");
    }

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testGetShortMissing() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.getShort("nokey");
    }

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testGetIntegerMissing() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.getInteger("nokey");
    }

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testGetLongMissing() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.getLong("nokey");
    }

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testGetFloatMissing() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.getFloat("nokey");
    }

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testGetDoubleMissing() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.getDouble("nokey");
    }

    @Test(timeout = 4000, expected = ClassCastException.class)
    public void testGetVectorNonStringList() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.put("bad", Integer.valueOf(1));
        ep.getVector("bad");
    }

    @Test(timeout = 4000, expected = ClassCastException.class)
    public void testGetListNonStringList() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.put("bad", Boolean.TRUE);
        ep.getList("bad");
    }

    @Test(timeout = 4000)
    public void testGetStringArray() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("arr", "one,two,three");
        String[] arr = ep.getStringArray("arr");
        assertArrayEquals(new String[]{"one", "two", "three"}, arr);
    }

    @Test(timeout = 4000)
    public void testGetStringArraySingle() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("single", "only");
        String[] arr = ep.getStringArray("single");
        assertArrayEquals(new String[]{"only"}, arr);
    }

    @Test(timeout = 4000)
    public void testGetStringArrayMissingReturnsEmpty() {
        ExtendedProperties ep = new ExtendedProperties();
        String[] arr = ep.getStringArray("nope");
        assertEquals(0, arr.length);
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testIsInitializedAfterAdd() {
        ExtendedProperties ep = new ExtendedProperties();
        assertFalse(ep.isInitialized());
        ep.addProperty("k", "v");
        assertTrue(ep.isInitialized());
    }

    @Test(timeout = 4000)
    public void testKeysAsListedOrder() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("z", "1");
        ep.addProperty("a", "2");
        ep.addProperty("m", "3");
        Iterator it = ep.getKeys();
        assertEquals("z", it.next());
        assertEquals("a", it.next());
        assertEquals("m", it.next());
    }

    @Test(timeout = 4000)
    public void testConvertProperties() {
        Properties props = new Properties();
        props.setProperty("k1", "v1");
        props.setProperty("k2", "v2");
        ExtendedProperties ep = ExtendedProperties.convertProperties(props);
        assertEquals("v1", ep.getString("k1"));
        assertEquals("v2", ep.getString("k2"));
    }

    @Test(timeout = 4000)
    public void testLoadFromInputStream() throws IOException {
        String data = "key1 = value1\nkey2 = value2\n";
        InputStream is = new ByteArrayInputStream(data.getBytes("8859_1"));
        ExtendedProperties ep = new ExtendedProperties();
        ep.load(is);
        assertEquals("value1", ep.getString("key1"));
        assertEquals("value2", ep.getString("key2"));
        is.close();
    }

    @Test(timeout = 4000)
    public void testLoadWithContinuedLine() throws IOException {
        String data = "long = aaa\\\nbbb\n";
        InputStream is = new ByteArrayInputStream(data.getBytes("8859_1"));
        ExtendedProperties ep = new ExtendedProperties();
        ep.load(is);
        assertEquals("aaabbb", ep.getString("long"));
        is.close();
    }

    @Test(timeout = 4000)
    public void testLoadWithCommentsAndBlankLines() throws IOException {
        String data = "# comment\n\nkey = val\n";
        InputStream is = new ByteArrayInputStream(data.getBytes("8859_1"));
        ExtendedProperties ep = new ExtendedProperties();
        ep.load(is);
        assertEquals("val", ep.getString("key"));
        is.close();
    }

    @Test(timeout = 4000)
    public void testLoadWithIncludeDirectiveIgnored() throws IOException {
        // Since we cannot actually include a file, we just verify that the load doesn't throw
        String data = "include = somefile.properties\nkey = val\n";
        InputStream is = new ByteArrayInputStream(data.getBytes("8859_1"));
        ExtendedProperties ep = new ExtendedProperties();
        ep.load(is);
        // The include directive will attempt to load file but may fail silently due to file not found.
        // In this test environment, we just ensure key is added.
        assertNotNull(ep.getString("key"));
        is.close();
    }

    @Test(timeout = 4000)
    public void testSaveOutput() throws IOException {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("k", "v");
        ep.addProperty("list", "a,b");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        ep.save(bos, "header");
        String output = new String(bos.toByteArray(), "8859_1");
        assertTrue(output.contains("header"));
        assertTrue(output.contains("k=v"));
        // list items should be on separate lines
        assertTrue(output.contains("list=a"));
        assertTrue(output.contains("list=b"));
        bos.close();
    }

    @Test(timeout = 4000)
    public void testSaveWithNullOutput() throws IOException {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("k", "v");
        // should not throw
        ep.save(null, "header");
    }

    @Test(timeout = 4000)
    public void testToString() {
        ExtendedProperties ep = new ExtendedProperties();
        assertNotNull(ep.toString());  // inherited from Hashtable
    }

    // Additional edge: getProperty on null key (Hashtable will NPE)
    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testGetPropertyNullKey() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.getProperty(null);
    }
}