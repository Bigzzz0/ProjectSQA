package org.apache.commons.collections;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Vector;

/*
 * [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------
 * Defect Target (COLLECTIONS-271):
 * - Target: ExtendedProperties.combine(ExtendedProperties)
 * - Flaw: props.combine(otherProps) copies key/value pairs using super.put(key, val) directly,
 *         completely failing to record the newly combined keys in `keysAsListed`. Consequently,
 *         getKeys(), getKeys(prefix), subset(prefix), and display() fail to reflect combined keys.
 * - Test Target: testCollections271() asserts that combined keys exist in getKeys() iterator.
 *
 * Partition Matrix:
 * - Partition A (Core Functional & Lifecycle):
 *   - Empty constructor, file constructor, file + defaultFile constructor.
 *   - isInitialized tracking on load and addProperty.
 *   - getInclude / setInclude instance vs backwards compatibility static variable and null handling.
 * - Partition B (Parser & Tokenizer & Interpolation):
 *   - Line continuation handling (odd vs even trailing slashes).
 *   - Escaped delimiters vs unescaped delimiters in PropertiesTokenizer.
 *   - Property interpolation with recursive substitution, variable stacks, defaults fallback,
 *     and cycle detection throwing IllegalStateException.
 * - Partition C (Subset, Combine & Conversions):
 *   - subset(prefix) on exact prefix match vs dotted hierarchy keys.
 *   - combine(ExtendedProperties) state merging.
 *   - convertProperties(Properties) retaining parent/defaults relationships.
 * - Partition D (Type-Safe Typed Accessors):
 *   - Boolean, Byte, Short, Integer, Long, Float, Double with scalar and default overloads.
 *   - Vector / List morphing behavior when querying string values.
 * - Partition E (Defensive Guards & Exception Paths):
 *   - Malformed getProperties tokens throwing IllegalArgumentException.
 *   - Absent keys throwing NoSuchElementException.
 *   - Invalid type mappings throwing ClassCastException and NumberFormatException.
 */
public class ExtendedPropertiesGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (COLLECTIONS-271)
    // =========================================================================

    /**
     * Target Defect: COLLECTIONS-271.
     * ExtendedProperties.combine(ExtendedProperties) must properly register keys
     * into keysAsListed so that subsequent calls to getKeys() include all merged keys.
     */
    @Test(timeout = 4000)
    public void testCollections271() {
        ExtendedProperties ep1 = new ExtendedProperties();
        ep1.setProperty("key1", "val1");

        ExtendedProperties ep2 = new ExtendedProperties();
        ep2.setProperty("key2", "val2");

        ep1.combine(ep2);

        List keys = new ArrayList();
        for (Iterator it = ep1.getKeys(); it.hasNext(); ) {
            keys.add(it.next());
        }

        assertTrue("Original key must be present in getKeys()", keys.contains("key1"));
        assertTrue("COLLECTIONS-271: Combined keys must be listed in getKeys()", keys.contains("key2"));
        assertEquals("Both keys must be counted", 2, keys.size());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testInitializationState() {
        ExtendedProperties ep = new ExtendedProperties();
        assertFalse("New instance must not be initialized", ep.isInitialized());

        ep.addProperty("initKey", "initVal");
        assertTrue("Instance must be initialized after adding a property", ep.isInitialized());
    }

    @Test(timeout = 4000)
    public void testIncludePropertyConfiguration() {
        ExtendedProperties ep = new ExtendedProperties();
        assertEquals("Default include property name is 'include'", "include", ep.getInclude());

        ep.setInclude("import");
        assertEquals("Include property should be updated", "import", ep.getInclude());

        ep.setInclude("");
        assertNull("Empty string include property should map to null", ep.getInclude());

        ep.setInclude(null);
        assertNull("Null string include property should map to null", ep.getInclude());
    }

    @Test(timeout = 4000)
    public void testFileConstructorsAndDefaults() throws IOException {
        File parentFile = File.createTempFile("test_parent", ".properties");
        File childFile = File.createTempFile("test_child", ".properties");
        parentFile.deleteOnExit();
        childFile.deleteOnExit();

        try {
            FileOutputStream fosParent = new FileOutputStream(parentFile);
            fosParent.write("parent.prop = parentValue\nshared.prop = parentShared\n".getBytes("ISO-8859-1"));
            fosParent.close();

            FileOutputStream fosChild = new FileOutputStream(childFile);
            fosChild.write("child.prop = childValue\nshared.prop = childShared\n".getBytes("ISO-8859-1"));
            fosChild.close();

            ExtendedProperties childEp = new ExtendedProperties(childFile.getAbsolutePath(), parentFile.getAbsolutePath());
            assertTrue(childEp.isInitialized());
            assertEquals("childValue", childEp.getString("child.prop"));
            assertEquals("childShared", childEp.getString("shared.prop"));
            assertEquals("parentValue", childEp.getString("parent.prop"));

            ExtendedProperties singleEp = new ExtendedProperties(parentFile.getAbsolutePath());
            assertEquals("parentValue", singleEp.getString("parent.prop"));
        } finally {
            parentFile.delete();
            childFile.delete();
        }
    }

    // =========================================================================
    // Partition B: Loading, Syntax Parsing, Escaping & Interpolation
    // =========================================================================

    @Test(timeout = 4000)
    public void testPropertiesLoadingWithCommentsAndContinuations() throws IOException {
        String config = "# Header Comment\n"
                + "  # Indented Comment\n"
                + "\n"
                + "simple.key = simpleValue\n"
                + "continued.key = first line \\\n"
                + "                second line\n"
                + "escaped.slash = endingWithSlash\\\\\n"
                + "list.key = one, two, three\\,stillThree, four\\\\four\n";

        ExtendedProperties ep = new ExtendedProperties();
        ep.load(new ByteArrayInputStream(config.getBytes("ISO-8859-1")));

        assertEquals("simpleValue", ep.getString("simple.key"));
        assertEquals("first linesecond line", ep.getString("continued.key"));
        assertEquals("endingWithSlash\\\\", ep.getString("escaped.slash"));

        String[] listTokens = ep.getStringArray("list.key");
        assertEquals(4, listTokens.length);
        assertEquals("one", listTokens[0]);
        assertEquals("two", listTokens[1]);
        assertEquals("three,stillThree", listTokens[2]);
        assertEquals("four\\four", listTokens[3]);
    }

    @Test(timeout = 4000)
    public void testLoadWithEncodingFallback() throws IOException {
        String config = "key = value\n";
        ExtendedProperties ep = new ExtendedProperties();
        ep.load(new ByteArrayInputStream(config.getBytes("UTF-8")), "UTF-8");
        assertEquals("value", ep.getString("key"));

        ExtendedProperties epInvalidEnc = new ExtendedProperties();
        epInvalidEnc.load(new ByteArrayInputStream(config.getBytes("ISO-8859-1")), "INVALID_ENCODING_NAME");
        assertEquals("value", epInvalidEnc.getString("key"));
    }

    @Test(timeout = 4000)
    public void testIncludeDirective() throws IOException {
        File includedFile = File.createTempFile("inc_file", ".properties");
        includedFile.deleteOnExit();

        try {
            FileOutputStream fos = new FileOutputStream(includedFile);
            fos.write("included.key = includedValue\n".getBytes("ISO-8859-1"));
            fos.close();

            String config = "base.key = baseValue\n"
                    + "include = " + includedFile.getAbsolutePath() + "\n";

            ExtendedProperties ep = new ExtendedProperties();
            ep.load(new ByteArrayInputStream(config.getBytes("ISO-8859-1")));

            assertEquals("baseValue", ep.getString("base.key"));
            assertEquals("includedValue", ep.getString("included.key"));
        } finally {
            includedFile.delete();
        }
    }

    @Test(timeout = 4000)
    public void testInterpolationNormalAndNested() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("base.url", "http://localhost");
        ep.addProperty("port", "8080");
        ep.addProperty("full.url", "${base.url}:${port}/api");
        ep.addProperty("unresolved", "${unknown.variable}/test");

        assertEquals("http://localhost:8080/api", ep.getString("full.url"));
        assertEquals("${unknown.variable}/test", ep.getString("unresolved"));
        assertNull(ep.interpolate(null));
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testInterpolationDirectLoopThrowsException() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("loopA", "${loopB}");
        ep.addProperty("loopB", "${loopA}");
        ep.getString("loopA");
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testInterpolationIndirectLoopThrowsException() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("a", "${b}");
        ep.addProperty("b", "${c}");
        ep.addProperty("c", "${a}");
        ep.getString("a");
    }

    @Test(timeout = 4000)
    public void testInterpolationWithDefaultsFallback() {
        ExtendedProperties defaults = new ExtendedProperties();
        defaults.addProperty("default.host", "example.com");

        ExtendedProperties ep = new ExtendedProperties();
        ep.defaults = defaults;
        ep.addProperty("url", "http://${default.host}/index");

        assertEquals("http://example.com/index", ep.getString("url"));
    }

    // =========================================================================
    // Partition C: Collections Operations (Subset, Clear, Put, Remove, Save)
    // =========================================================================

    @Test(timeout = 4000)
    public void testSubsetOperation() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("database.host", "localhost");
        ep.addProperty("database.port", "5432");
        ep.addProperty("database", "exactMatch");
        ep.addProperty("service.timeout", "3000");

        ExtendedProperties subDb = ep.subset("database");
        assertNotNull(subDb);
        assertEquals("localhost", subDb.getString("host"));
        assertEquals("5432", subDb.getString("port"));
        assertEquals("exactMatch", subDb.getString("database"));
        assertNull(subDb.getString("service.timeout"));

        ExtendedProperties nonExistent = ep.subset("nonexistent");
        assertNull(nonExistent);
    }

    @Test(timeout = 4000)
    public void testGetKeysWithPrefix() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("app.name", "TestApp");
        ep.addProperty("app.version", "1.0");
        ep.addProperty("db.url", "jdbc:h2:mem:");

        Iterator it = ep.getKeys("app");
        List appKeys = new ArrayList();
        while (it.hasNext()) {
            appKeys.add(it.next());
        }

        assertEquals(2, appKeys.size());
        assertTrue(appKeys.contains("app.name"));
        assertTrue(appKeys.contains("app.version"));
        assertFalse(appKeys.contains("db.url"));
    }

    @Test(timeout = 4000)
    public void testClearProperty() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key1", "val1");
        ep.addProperty("key2", "val2");

        ep.clearProperty("key1");
        assertNull(ep.getProperty("key1"));

        Iterator it = ep.getKeys();
        List remaining = new ArrayList();
        while (it.hasNext()) {
            remaining.add(it.next());
        }
        assertEquals(1, remaining.size());
        assertEquals("key2", remaining.get(0));

        // clearing non-existent property is safe
        ep.clearProperty("nonexistent");
    }

    @Test(timeout = 4000)
    public void testSetPropertyReplacesOldValue() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("key", "val1");
        ep.addProperty("key", "val2");
        assertEquals(2, ep.getStringArray("key").length);

        ep.setProperty("key", "singleVal");
        assertEquals("singleVal", ep.getString("key"));
        assertEquals(1, ep.getStringArray("key").length);
    }

    @Test(timeout = 4000)
    public void testMapContractsPutPutAllRemove() {
        ExtendedProperties ep = new ExtendedProperties();
        Object old = ep.put("map.key", "initialValue");
        assertNull(old);
        assertEquals("initialValue", ep.getProperty("map.key"));

        // put appends if key exists
        ep.put("map.key", "secondValue");
        assertEquals(2, ep.getStringArray("map.key").length);

        Map standardMap = new HashMap();
        standardMap.put("fromMap1", "v1");
        standardMap.put("fromMap2", "v2");
        ep.putAll(standardMap);
        assertEquals("v1", ep.getString("fromMap1"));
        assertEquals("v2", ep.getString("fromMap2"));

        ExtendedProperties otherEp = new ExtendedProperties();
        otherEp.addProperty("fromEp", "epVal");
        ep.putAll(otherEp);
        assertEquals("epVal", ep.getString("fromEp"));

        Object removed = ep.remove("fromEp");
        assertEquals("epVal", removed);
        assertNull(ep.getProperty("fromEp"));
    }

    @Test(timeout = 4000)
    public void testConvertPropertiesWithParentDefaults() {
        Properties parentProps = new Properties();
        parentProps.setProperty("parentKey", "parentVal");

        Properties childProps = new Properties(parentProps);
        childProps.setProperty("childKey", "childVal");

        ExtendedProperties ep = ExtendedProperties.convertProperties(childProps);
        assertEquals("childVal", ep.getString("childKey"));
        assertEquals("parentVal", ep.getString("parentKey"));
    }

    @Test(timeout = 4000)
    public void testSaveAndDisplay() throws IOException {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("scalar.key", "value,with,commas\\and\\slashes");
        ep.addProperty("list.key", "item1");
        ep.addProperty("list.key", "item2");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ep.save(baos, "=== Test Header ===");
        String savedContent = new String(baos.toByteArray());

        assertTrue(savedContent.contains("=== Test Header ==="));
        assertTrue(savedContent.contains("scalar.key="));
        assertTrue(savedContent.contains("list.key=item1"));
        assertTrue(savedContent.contains("list.key=item2"));

        // Null output stream must be ignored cleanly
        ep.save((OutputStream) null, "Ignore Header");

        // Smoke test display() stdout output
        ep.display();
    }

    // =========================================================================
    // Partition D: Type Conversions & Accessors
    // =========================================================================

    @Test(timeout = 4000)
    public void testStringAndStringArrayAccessors() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("single", "val");
        ep.addProperty("multi", "val1, val2");

        assertEquals("val", ep.getString("single"));
        assertEquals("default", ep.getString("missing", "default"));
        assertEquals("val1", ep.getString("multi")); // First token

        String[] singleArr = ep.getStringArray("single");
        assertEquals(1, singleArr.length);
        assertEquals("val", singleArr[0]);

        String[] multiArr = ep.getStringArray("multi");
        assertEquals(2, multiArr.length);
        assertEquals("val1", multiArr[0]);
        assertEquals("val2", multiArr[1]);

        String[] missingArr = ep.getStringArray("missing");
        assertEquals(0, missingArr.length);
    }

    @Test(timeout = 4000)
    public void testGetPropertiesAccessor() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("subprops", "k1=v1, k2=v2");

        Properties props = ep.getProperties("subprops");
        assertEquals("v1", props.getProperty("k1"));
        assertEquals("v2", props.getProperty("k2"));

        Properties defaults = new Properties();
        defaults.setProperty("kDef", "vDef");
        Properties propsWithDef = ep.getProperties("subprops", defaults);
        assertEquals("vDef", propsWithDef.getProperty("kDef"));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetPropertiesMalformedTokenThrowsException() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("badProps", "noEqualSignToken");
        ep.getProperties("badProps");
    }

    @Test(timeout = 4000)
    public void testVectorAndListAccessors() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("strKey", "single");
        ep.addProperty("listKey", "e1, e2");

        // getVector
        Vector vFromStr = ep.getVector("strKey");
        assertEquals(1, vFromStr.size());
        assertEquals("single", vFromStr.get(0));

        Vector vFromList = ep.getVector("listKey");
        assertEquals(2, vFromList.size());

        Vector defVector = new Vector();
        defVector.add("def");
        assertSame(defVector, ep.getVector("absent", defVector));
        assertNotNull(ep.getVector("absent")); // null default creates empty Vector

        // getList
        ExtendedProperties ep2 = new ExtendedProperties();
        ep2.addProperty("strKey", "single");
        List lFromStr = ep2.getList("strKey");
        assertEquals(1, lFromStr.size());
        assertEquals("single", lFromStr.get(0));

        List defList = new ArrayList();
        assertSame(defList, ep2.getList("absent", defList));
        assertNotNull(ep2.getList("absent"));
    }

    @Test(timeout = 4000)
    public void testBooleanAccessors() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("b.true", "true");
        ep.addProperty("b.on", "on");
        ep.addProperty("b.yes", "yes");
        ep.addProperty("b.false", "false");
        ep.addProperty("b.off", "off");
        ep.addProperty("b.no", "no");
        ep.addProperty("b.obj", Boolean.TRUE);

        assertTrue(ep.getBoolean("b.true"));
        assertTrue(ep.getBoolean("b.on"));
        assertTrue(ep.getBoolean("b.yes"));
        assertFalse(ep.getBoolean("b.false"));
        assertFalse(ep.getBoolean("b.off"));
        assertFalse(ep.getBoolean("b.no"));
        assertTrue(ep.getBoolean("b.obj"));

        assertTrue(ep.getBoolean("b.missing", true));
        assertEquals(Boolean.FALSE, ep.getBoolean("b.missing", Boolean.FALSE));
        assertNull(ep.testBoolean("not_a_boolean"));
    }

    @Test(timeout = 4000, expected = NoSuchElementException.class)
    public void testGetBooleanMissingKeyThrowsException() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.getBoolean("nonexistent");
    }

    @Test(timeout = 4000)
    public void testNumericAccessors() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("num.byte", "10");
        ep.addProperty("num.short", "20");
        ep.addProperty("num.int", "30");
        ep.addProperty("num.long", "40");
        ep.addProperty("num.float", "50.5");
        ep.addProperty("num.double", "60.6");

        // Byte
        assertEquals((byte) 10, ep.getByte("num.byte"));
        assertEquals((byte) 10, ep.getByte("num.byte", (byte) 1));
        assertEquals((byte) 99, ep.getByte("missing", (byte) 99));

        // Short
        assertEquals((short) 20, ep.getShort("num.short"));
        assertEquals((short) 20, ep.getShort("num.short", (short) 1));
        assertEquals((short) 99, ep.getShort("missing", (short) 99));

        // Integer
        assertEquals(30, ep.getInt("num.int"));
        assertEquals(30, ep.getInt("num.int", 1));
        assertEquals(30, ep.getInteger("num.int"));
        assertEquals(30, ep.getInteger("num.int", 1));
        assertEquals(99, ep.getInt("missing", 99));

        // Long
        assertEquals(40L, ep.getLong("num.long"));
        assertEquals(40L, ep.getLong("num.long", 1L));
        assertEquals(99L, ep.getLong("missing", 99L));

        // Float
        assertEquals(50.5f, ep.getFloat("num.float"), 0.001f);
        assertEquals(50.5f, ep.getFloat("num.float", 1.0f), 0.001f);
        assertEquals(99.0f, ep.getFloat("missing", 99.0f), 0.001f);

        // Double
        assertEquals(60.6, ep.getDouble("num.double"), 0.001);
        assertEquals(60.6, ep.getDouble("num.double", 1.0), 0.001);
        assertEquals(99.0, ep.getDouble("missing", 99.0), 0.001);
    }

    @Test(timeout = 4000)
    public void testNumericAccessorsWithPreParsedObjects() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addPropertyDirect("b", new Byte((byte) 1));
        ep.addPropertyDirect("s", new Short((short) 2));
        ep.addPropertyDirect("i", new Integer(3));
        ep.addPropertyDirect("l", new Long(4L));
        ep.addPropertyDirect("f", new Float(5.0f));
        ep.addPropertyDirect("d", new Double(6.0));

        assertEquals((byte) 1, ep.getByte("b"));
        assertEquals((short) 2, ep.getShort("s"));
        assertEquals(3, ep.getInt("i"));
        assertEquals(4L, ep.getLong("l"));
        assertEquals(5.0f, ep.getFloat("f"), 0.001f);
        assertEquals(6.0, ep.getDouble("d"), 0.001);
    }

    // =========================================================================
    // Partition E: Defensive Guards & Exception Paths
    // =========================================================================

    @Test(timeout = 4000, expected = NoSuchElementException.class)
    public void testGetByteMissingThrowsException() {
        new ExtendedProperties().getByte("missing");
    }

    @Test(timeout = 4000, expected = NoSuchElementException.class)
    public void testGetShortMissingThrowsException() {
        new ExtendedProperties().getShort("missing");
    }

    @Test(timeout = 4000, expected = NoSuchElementException.class)
    public void testGetIntMissingThrowsException() {
        new ExtendedProperties().getInt("missing");
    }

    @Test(timeout = 4000, expected = NoSuchElementException.class)
    public void testGetLongMissingThrowsException() {
        new ExtendedProperties().getLong("missing");
    }

    @Test(timeout = 4000, expected = NoSuchElementException.class)
    public void testGetFloatMissingThrowsException() {
        new ExtendedProperties().getFloat("missing");
    }

    @Test(timeout = 4000, expected = NoSuchElementException.class)
    public void testGetDoubleMissingThrowsException() {
        new ExtendedProperties().getDouble("missing");
    }

    @Test(timeout = 4000, expected = ClassCastException.class)
    public void testGetStringClassCastException() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addPropertyDirect("objKey", new Object());
        ep.getString("objKey");
    }

    @Test(timeout = 4000, expected = ClassCastException.class)
    public void testGetStringArrayClassCastException() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addPropertyDirect("objKey", new Object());
        ep.getStringArray("objKey");
    }

    @Test(timeout = 4000, expected = ClassCastException.class)
    public void testGetVectorClassCastException() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addPropertyDirect("objKey", new Object());
        ep.getVector("objKey");
    }

    @Test(timeout = 4000, expected = ClassCastException.class)
    public void testGetListClassCastException() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addPropertyDirect("objKey", new Object());
        ep.getList("objKey");
    }

    @Test(timeout = 4000, expected = ClassCastException.class)
    public void testGetBooleanClassCastException() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addPropertyDirect("objKey", new Object());
        ep.getBoolean("objKey");
    }

    @Test(timeout = 4000, expected = ClassCastException.class)
    public void testGetByteClassCastException() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addPropertyDirect("objKey", new Object());
        ep.getByte("objKey");
    }

    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testGetByteInvalidFormatThrowsException() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("invalid", "notANumber");
        ep.getByte("invalid");
    }

    @Test(timeout = 4000, expected = ClassCastException.class)
    public void testGetShortClassCastException() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addPropertyDirect("objKey", new Object());
        ep.getShort("objKey");
    }

    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testGetShortInvalidFormatThrowsException() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("invalid", "notANumber");
        ep.getShort("invalid");
    }

    @Test(timeout = 4000, expected = ClassCastException.class)
    public void testGetIntegerClassCastException() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addPropertyDirect("objKey", new Object());
        ep.getInteger("objKey");
    }

    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testGetIntegerInvalidFormatThrowsException() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("invalid", "notANumber");
        ep.getInteger("invalid");
    }

    @Test(timeout = 4000, expected = ClassCastException.class)
    public void testGetLongClassCastException() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addPropertyDirect("objKey", new Object());
        ep.getLong("objKey");
    }

    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testGetLongInvalidFormatThrowsException() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("invalid", "notANumber");
        ep.getLong("invalid");
    }

    @Test(timeout = 4000, expected = ClassCastException.class)
    public void testGetFloatClassCastException() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addPropertyDirect("objKey", new Object());
        ep.getFloat("objKey");
    }

    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testGetFloatInvalidFormatThrowsException() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("invalid", "notANumber");
        ep.getFloat("invalid");
    }

    @Test(timeout = 4000, expected = ClassCastException.class)
    public void testGetDoubleClassCastException() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addPropertyDirect("objKey", new Object());
        ep.getDouble("objKey");
    }

    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testGetDoubleInvalidFormatThrowsException() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("invalid", "notANumber");
        ep.getDouble("invalid");
    }
}