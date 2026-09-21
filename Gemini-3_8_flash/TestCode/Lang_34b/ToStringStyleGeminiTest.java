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
package org.apache.commons.lang3.builder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.SystemUtils;
import org.junit.Test;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------------------------
 * Class under Test: ToStringStyle (Defects4J Commons-Lang)
 *
 * Partition A: Core Functional Logic & State Transitions
 * - Field setters/getters (arrayStart, arrayEnd, arraySeparator, contentStart, contentEnd,
 *   fieldNameValueSeparator, fieldSeparator, nullText, sizeStartText, sizeEndText,
 *   summaryObjectStartText, summaryObjectEndText, useClassName, useShortClassName,
 *   useIdentityHashCode, useFieldNames, defaultFullDetail, arrayContentDetail,
 *   fieldSeparatorAtStart, fieldSeparatorAtEnd).
 * - appendStart: with/without class name, short class name, identity hash code, fieldSeparatorAtStart.
 * - appendEnd: with fieldSeparatorAtEnd true/false, unregister behavior.
 * - removeLastFieldSeparator: len=0, sepLen=0, len < sepLen, exact match, partial match, mismatch.
 * - appendSuper & appendToString: null string, empty/unmatched delimiters, fieldSeparatorAtStart true/false.
 * - append(buffer, fieldName, Object, Boolean): fullDetail=null/true/false, value=null/non-null.
 * - Primitives & Primitive Arrays: long, int, short, byte, char, double, float, boolean (detail & summary).
 * - Object arrays & reflectionAppendArrayDetail: empty, single element, multi-element, null items.
 *
 * Partition B: Boundary Value Analysis (BVA) & Extremes
 * - Null inputs for all setters: defaults converted to empty strings ("").
 * - Empty arrays (length 0), single item arrays (length 1), boundary primitive values (MIN_VALUE, MAX_VALUE).
 * - Empty collections, empty maps, size summary prefixes and suffixes.
 *
 * Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
 * - Defect: ToStringStyle.getRegistry() returning Collections.emptyMap() instead of null when registry is empty.
 * - Ground truth failures in ToStringBuilderTest::testObjectCycle, testReflectionHierarchy, etc. expecting <null>.
 * - Circular reference handling in appendInternal: isRegistered() check bypassing Number, Boolean, Character,
 *   and triggering appendCyclicObject on self-referencing / cyclical instances.
 *
 * Partition D: Defensive & Unregistered Paths
 * - register(null) and unregister(null) defensive no-ops.
 * - unregistering objects not in registry, unregistering last object triggering thread local cleanup.
 *
 * Partition E: Object Lifecycle & Contract Integrity
 * - Singleton preservation through serialization / readResolve for:
 *   DEFAULT_STYLE, MULTI_LINE_STYLE, NO_FIELD_NAMES_STYLE, SHORT_PREFIX_STYLE, SIMPLE_STYLE.
 * ---------------------------------------------------------------------------------------------------------------------
 */
public class ToStringStyleGeminiTest {

    private static class ConcreteToStringStyle extends ToStringStyle {
        private static final long serialVersionUID = 1L;
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testGettersAndSettersStandardValues() {
        ConcreteToStringStyle style = new ConcreteToStringStyle();

        style.setArrayStart("<arr>");
        assertEquals("<arr>", style.getArrayStart());

        style.setArrayEnd("</arr>");
        assertEquals("</arr>", style.getArrayEnd());

        style.setArraySeparator(";");
        assertEquals(";", style.getArraySeparator());

        style.setContentStart("<content>");
        assertEquals("<content>", style.getContentStart());

        style.setContentEnd("</content>");
        assertEquals("</content>", style.getContentEnd());

        style.setFieldNameValueSeparator("->");
        assertEquals("->", style.getFieldNameValueSeparator());

        style.setFieldSeparator("|");
        assertEquals("|", style.getFieldSeparator());

        style.setNullText("<none>");
        assertEquals("<none>", style.getNullText());

        style.setSizeStartText("(count=");
        assertEquals("(count=", style.getSizeStartText());

        style.setSizeEndText(")");
        assertEquals(")", style.getSizeEndText());

        style.setSummaryObjectStartText("[obj:");
        assertEquals("[obj:", style.getSummaryObjectStartText());

        style.setSummaryObjectEndText("]");
        assertEquals("]", style.getSummaryObjectEndText());

        style.setUseClassName(false);
        assertFalse(style.isUseClassName());

        style.setUseShortClassName(true);
        assertTrue(style.isUseShortClassName());

        style.setUseIdentityHashCode(false);
        assertFalse(style.isUseIdentityHashCode());

        style.setUseFieldNames(false);
        assertFalse(style.isUseFieldNames());

        style.setDefaultFullDetail(false);
        assertFalse(style.isDefaultFullDetail());

        style.setArrayContentDetail(false);
        assertFalse(style.isArrayContentDetail());

        style.setFieldSeparatorAtStart(true);
        assertTrue(style.isFieldSeparatorAtStart());

        style.setFieldSeparatorAtEnd(true);
        assertTrue(style.isFieldSeparatorAtEnd());
    }

    @Test(timeout = 4000)
    public void testAppendStartVariations() {
        ConcreteToStringStyle style = new ConcreteToStringStyle();
        StringBuffer sb = new StringBuffer();
        Object target = new Integer(100);

        // 1. Full class name with identity hash code
        style.setUseClassName(true);
        style.setUseShortClassName(false);
        style.setUseIdentityHashCode(true);
        style.setContentStart("[");
        style.appendStart(sb, target);
        String expectedPrefix = "java.lang.Integer@" + Integer.toHexString(System.identityHashCode(target)) + "[";
        assertEquals(expectedPrefix, sb.toString());

        // 2. Short class name, no identity hash code, field separator at start
        style.setUseShortClassName(true);
        style.setUseIdentityHashCode(false);
        style.setFieldSeparatorAtStart(true);
        style.setFieldSeparator(",");
        sb.setLength(0);
        style.appendStart(sb, target);
        assertEquals("Integer[,", sb.toString());

        // 3. No class name, no identity hash code, content start empty
        style.setUseClassName(false);
        style.setFieldSeparatorAtStart(false);
        style.setContentStart("");
        sb.setLength(0);
        style.appendStart(sb, target);
        assertEquals("", sb.toString());

        // 4. Null object passed to appendStart
        sb.setLength(0);
        style.appendStart(sb, null);
        assertEquals("", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendEndVariations() {
        ConcreteToStringStyle style = new ConcreteToStringStyle();
        StringBuffer sb = new StringBuffer();
        Object target = new String("sample");

        // When fieldSeparatorAtEnd is false, trailing separator should be removed
        style.setFieldSeparator(",");
        style.setFieldSeparatorAtEnd(false);
        style.setContentEnd("]");
        sb.append("name=foo,");
        style.appendEnd(sb, target);
        assertEquals("name=foo]", sb.toString());

        // When fieldSeparatorAtEnd is true, trailing separator is retained
        style.setFieldSeparatorAtEnd(true);
        sb.setLength(0);
        sb.append("name=foo,");
        style.appendEnd(sb, target);
        assertEquals("name=foo,]", sb.toString());
    }

    @Test(timeout = 4000)
    public void testRemoveLastFieldSeparatorBranches() {
        ConcreteToStringStyle style = new ConcreteToStringStyle();
        StringBuffer sb = new StringBuffer();

        // Branch: len == 0
        style.setFieldSeparator(",");
        style.removeLastFieldSeparator(sb);
        assertEquals(0, sb.length());

        // Branch: sepLen == 0
        style.setFieldSeparator("");
        sb.append("hello");
        style.removeLastFieldSeparator(sb);
        assertEquals("hello", sb.toString());

        // Branch: len < sepLen
        style.setFieldSeparator("---");
        sb.setLength(0);
        sb.append("-");
        style.removeLastFieldSeparator(sb);
        assertEquals("-", sb.toString());

        // Branch: match == false (mismatch on second character)
        style.setFieldSeparator(",,");
        sb.setLength(0);
        sb.append("datax,");
        style.removeLastFieldSeparator(sb);
        assertEquals("datax,", sb.toString());

        // Branch: exact match
        style.setFieldSeparator(",,");
        sb.setLength(0);
        sb.append("data,,");
        style.removeLastFieldSeparator(sb);
        assertEquals("data", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendSuperAndAppendToString() {
        ConcreteToStringStyle style = new ConcreteToStringStyle();
        StringBuffer sb = new StringBuffer();

        // Null superToString / toString
        style.appendSuper(sb, null);
        style.appendToString(sb, null);
        assertEquals(0, sb.length());

        // Unmatched delimiters
        style.setContentStart("[");
        style.setContentEnd("]");
        style.appendToString(sb, "NoDelimitersHere");
        assertEquals(0, sb.length());

        // Equal positions (empty content)
        style.appendToString(sb, "Sample[]");
        assertEquals(0, sb.length());

        // Normal valid toString content
        sb.append("initial,");
        style.setFieldSeparator(",");
        style.setFieldSeparatorAtStart(false);
        style.appendSuper(sb, "MyClass[field1=1,field2=2]");
        assertEquals("initial,field1=1,field2=2,", sb.toString());

        // With fieldSeparatorAtStart == true
        style.setFieldSeparatorAtStart(true);
        sb.setLength(0);
        sb.append("initial,");
        style.appendToString(sb, "MyClass[field3=3]");
        assertEquals("initialfield3=3,", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendObjectAndCollections() {
        ConcreteToStringStyle style = new ConcreteToStringStyle();
        StringBuffer sb = new StringBuffer();

        // Object with full detail = null (fall back to defaultFullDetail true)
        style.append(sb, "str", "val", null);
        assertEquals("str=val,", sb.toString());

        // Object with null value
        sb.setLength(0);
        style.append(sb, "nullObj", (Object) null, Boolean.TRUE);
        assertEquals("nullObj=<null>,", sb.toString());

        // Collection in detail mode
        sb.setLength(0);
        List<String> list = Arrays.asList("alpha", "beta");
        style.append(sb, "items", list, Boolean.TRUE);
        assertEquals("items=[alpha, beta],", sb.toString());

        // Collection in summary mode
        sb.setLength(0);
        style.append(sb, "items", list, Boolean.FALSE);
        assertEquals("items=<size=2>,", sb.toString());

        // Map in detail mode
        sb.setLength(0);
        Map<String, String> map = new HashMap<String, String>();
        map.put("k1", "v1");
        style.append(sb, "map", map, Boolean.TRUE);
        assertEquals("map={k1=v1},", sb.toString());

        // Map in summary mode
        sb.setLength(0);
        style.append(sb, "map", map, Boolean.FALSE);
        assertEquals("map=<size=1>,", sb.toString());

        // Arbitrary Object in summary mode
        sb.setLength(0);
        Object custom = new Object() {
            @Override
            public String toString() {
                return "CustomObject";
            }
        };
        style.append(sb, "custom", custom, Boolean.FALSE);
        assertTrue(sb.toString().startsWith("custom=<"));
        assertTrue(sb.toString().endsWith(">,"));
    }

    @Test(timeout = 4000)
    public void testAppendPrimitives() {
        ConcreteToStringStyle style = new ConcreteToStringStyle();
        StringBuffer sb = new StringBuffer();

        style.append(sb, "long", 1234567890123L);
        style.append(sb, "int", 42);
        style.append(sb, "short", (short) 7);
        style.append(sb, "byte", (byte) 1);
        style.append(sb, "char", 'x');
        style.append(sb, "double", 3.14159);
        style.append(sb, "float", 2.718f);
        style.append(sb, "bool", true);

        String expected = "long=1234567890123,int=42,short=7,byte=1,char=x,double=3.14159,float=2.718,bool=true,";
        assertEquals(expected, sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendPrimitiveArraysDetailAndSummary() {
        ConcreteToStringStyle style = new ConcreteToStringStyle();
        StringBuffer sb = new StringBuffer();

        // long[]
        style.append(sb, "longs", new long[]{1L, 2L}, Boolean.TRUE);
        style.append(sb, "longsNull", (long[]) null, Boolean.TRUE);
        style.append(sb, "longsSum", new long[]{1L, 2L}, Boolean.FALSE);

        // int[]
        style.append(sb, "ints", new int[]{10, 20}, Boolean.TRUE);
        style.append(sb, "intsNull", (int[]) null, Boolean.TRUE);
        style.append(sb, "intsSum", new int[]{10, 20}, Boolean.FALSE);

        // short[]
        style.append(sb, "shorts", new short[]{(short) 3, (short) 4}, Boolean.TRUE);
        style.append(sb, "shortsNull", (short[]) null, Boolean.TRUE);
        style.append(sb, "shortsSum", new short[]{(short) 3, (short) 4}, Boolean.FALSE);

        // byte[]
        style.append(sb, "bytes", new byte[]{(byte) 5, (byte) 6}, Boolean.TRUE);
        style.append(sb, "bytesNull", (byte[]) null, Boolean.TRUE);
        style.append(sb, "bytesSum", new byte[]{(byte) 5, (byte) 6}, Boolean.FALSE);

        // char[]
        style.append(sb, "chars", new char[]{'a', 'b'}, Boolean.TRUE);
        style.append(sb, "charsNull", (char[]) null, Boolean.TRUE);
        style.append(sb, "charsSum", new char[]{'a', 'b'}, Boolean.FALSE);

        // double[]
        style.append(sb, "doubles", new double[]{1.1, 2.2}, Boolean.TRUE);
        style.append(sb, "doublesNull", (double[]) null, Boolean.TRUE);
        style.append(sb, "doublesSum", new double[]{1.1, 2.2}, Boolean.FALSE);

        // float[]
        style.append(sb, "floats", new float[]{3.3f, 4.4f}, Boolean.TRUE);
        style.append(sb, "floatsNull", (float[]) null, Boolean.TRUE);
        style.append(sb, "floatsSum", new float[]{3.3f, 4.4f}, Boolean.FALSE);

        // boolean[]
        style.append(sb, "bools", new boolean[]{true, false}, Boolean.TRUE);
        style.append(sb, "boolsNull", (boolean[]) null, Boolean.TRUE);
        style.append(sb, "boolsSum", new boolean[]{true, false}, Boolean.FALSE);

        String res = sb.toString();
        assertTrue(res.contains("longs={1,2},longsNull=<null>,longsSum=<size=2>,"));
        assertTrue(res.contains("ints={10,20},intsNull=<null>,intsSum=<size=2>,"));
        assertTrue(res.contains("shorts={3,4},shortsNull=<null>,shortsSum=<size=2>,"));
        assertTrue(res.contains("bytes={5,6},bytesNull=<null>,bytesSum=<size=2>,"));
        assertTrue(res.contains("chars={a,b},charsNull=<null>,charsSum=<size=2>,"));
        assertTrue(res.contains("doubles={1.1,2.2},doublesNull=<null>,doublesSum=<size=2>,"));
        assertTrue(res.contains("floats={3.3,4.4},floatsNull=<null>,floatsSum=<size=2>,"));
        assertTrue(res.contains("bools={true,false},boolsNull=<null>,boolsSum=<size=2>,"));
    }

    @Test(timeout = 4000)
    public void testAppendObjectArraysAndReflectionDetail() {
        ConcreteToStringStyle style = new ConcreteToStringStyle();
        StringBuffer sb = new StringBuffer();

        // Object array with null element
        Object[] array = new Object[]{"test", null, 123};
        style.append(sb, "arr", array, Boolean.TRUE);
        assertEquals("arr={test,<null>,123},", sb.toString());

        // Object array in summary mode
        sb.setLength(0);
        style.append(sb, "arr", array, Boolean.FALSE);
        assertEquals("arr=<size=3>,", sb.toString());

        // Object array null
        sb.setLength(0);
        style.append(sb, "arr", (Object[]) null, Boolean.TRUE);
        assertEquals("arr=<null>,", sb.toString());

        // reflectionAppendArrayDetail with primitive and object arrays
        sb.setLength(0);
        style.reflectionAppendArrayDetail(sb, "refl", new int[]{9, 8});
        assertEquals("{9,8}", sb.toString());

        sb.setLength(0);
        style.reflectionAppendArrayDetail(sb, "reflNull", new Object[]{"val", null});
        assertEquals("{val,<null>}", sb.toString());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testSettersWithNullArguments() {
        ConcreteToStringStyle style = new ConcreteToStringStyle();

        style.setArrayStart(null);
        assertEquals("", style.getArrayStart());

        style.setArrayEnd(null);
        assertEquals("", style.getArrayEnd());

        style.setArraySeparator(null);
        assertEquals("", style.getArraySeparator());

        style.setContentStart(null);
        assertEquals("", style.getContentStart());

        style.setContentEnd(null);
        assertEquals("", style.getContentEnd());

        style.setFieldNameValueSeparator(null);
        assertEquals("", style.getFieldNameValueSeparator());

        style.setFieldSeparator(null);
        assertEquals("", style.getFieldSeparator());

        style.setNullText(null);
        assertEquals("", style.getNullText());

        style.setSizeStartText(null);
        assertEquals("", style.getSizeStartText());

        style.setSizeEndText(null);
        assertEquals("", style.getSizeEndText());

        style.setSummaryObjectStartText(null);
        assertEquals("", style.getSummaryObjectStartText());

        style.setSummaryObjectEndText(null);
        assertEquals("", style.getSummaryObjectEndText());
    }

    @Test(timeout = 4000)
    public void testEmptyArraysAndBoundaryValues() {
        ConcreteToStringStyle style = new ConcreteToStringStyle();
        StringBuffer sb = new StringBuffer();

        // Empty primitive arrays
        style.appendDetail(sb, "emptyLong", new long[0]);
        style.appendDetail(sb, "emptyInt", new int[0]);
        style.appendDetail(sb, "emptyShort", new short[0]);
        style.appendDetail(sb, "emptyByte", new byte[0]);
        style.appendDetail(sb, "emptyChar", new char[0]);
        style.appendDetail(sb, "emptyDouble", new double[0]);
        style.appendDetail(sb, "emptyFloat", new float[0]);
        style.appendDetail(sb, "emptyBoolean", new boolean[0]);
        style.appendDetail(sb, "emptyObject", new Object[0]);
        assertEquals("{}{}{}{}{}{}{}{}{}", sb.toString());

        // Boundary numeric values
        sb.setLength(0);
        style.append(sb, "maxLong", Long.MAX_VALUE);
        style.append(sb, "minLong", Long.MIN_VALUE);
        style.append(sb, "maxInt", Integer.MAX_VALUE);
        style.append(sb, "minInt", Integer.MIN_VALUE);
        style.append(sb, "maxDouble", Double.MAX_VALUE);
        style.append(sb, "minDouble", Double.MIN_VALUE);
        assertTrue(sb.toString().contains("maxLong=9223372036854775807,"));
        assertTrue(sb.toString().contains("minLong=-9223372036854775808,"));
        assertTrue(sb.toString().contains("maxInt=2147483647,"));
        assertTrue(sb.toString().contains("minInt=-2147483648,"));
    }

    @Test(timeout = 4000)
    public void testNullFieldNamesAndFlags() {
        ConcreteToStringStyle style = new ConcreteToStringStyle();
        StringBuffer sb = new StringBuffer();

        // Field name is null with useFieldNames true
        style.setUseFieldNames(true);
        style.append(sb, null, "value", Boolean.TRUE);
        assertEquals("value,", sb.toString());

        // Field name is provided with useFieldNames false
        sb.setLength(0);
        style.setUseFieldNames(false);
        style.append(sb, "fieldName", "value", Boolean.TRUE);
        assertEquals("value,", sb.toString());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Ground Truth: Defects4J)
    // =========================================================================

    /**
     * Direct target for Defects4J ground truth defect in ToStringStyle:
     * When all objects are unregistered, getRegistry() returned Collections.emptyMap()
     * instead of null, triggering:
     * "junit.framework.AssertionFailedError: Expected: <null> but was: {}"
     * across 27 ToStringBuilderTest methods.
     */
    @Test(timeout = 4000)
    public void testRegistryDefectTargetingEmptyVsNull() {
        Object item = new Object();
        ToStringStyle.register(item);
        assertTrue("Item must be registered", ToStringStyle.isRegistered(item));

        ToStringStyle.unregister(item);
        assertFalse("Item must be unregistered", ToStringStyle.isRegistered(item));

        // The defect causes this assertion to fail with: Expected: <null> but was: {}
        assertNull("Registry should be null after unregistering all items", ToStringStyle.getRegistry());
    }

    @Test(timeout = 4000)
    public void testCyclicReferenceDetection() {
        ConcreteToStringStyle style = new ConcreteToStringStyle();
        StringBuffer sb = new StringBuffer();

        Object cycleObject = new Object() {
            @Override
            public String toString() {
                return "CycleObj";
            }
        };

        // Manually simulate circular registration
        ToStringStyle.register(cycleObject);
        try {
            assertTrue(ToStringStyle.isRegistered(cycleObject));
            style.appendInternal(sb, "cycle", cycleObject, true);

            // Cyclic object should be formatted as identityToString
            String expectedIdentity = cycleObject.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(cycleObject));
            assertEquals(expectedIdentity, sb.toString());
        } finally {
            ToStringStyle.unregister(cycleObject);
        }
    }

    @Test(timeout = 4000)
    public void testCyclicCheckExemptions() {
        ConcreteToStringStyle style = new ConcreteToStringStyle();

        // 1. Number instance registered should bypass cyclic replacement
        Integer number = new Integer(999);
        ToStringStyle.register(number);
        try {
            StringBuffer sb = new StringBuffer();
            style.appendInternal(sb, "num", number, true);
            assertEquals("999", sb.toString());
        } finally {
            ToStringStyle.unregister(number);
        }

        // 2. Boolean instance registered should bypass cyclic replacement
        Boolean bool = Boolean.TRUE;
        ToStringStyle.register(bool);
        try {
            StringBuffer sb = new StringBuffer();
            style.appendInternal(sb, "bool", bool, true);
            assertEquals("true", sb.toString());
        } finally {
            ToStringStyle.unregister(bool);
        }

        // 3. Character instance registered should bypass cyclic replacement
        Character ch = Character.valueOf('Q');
        ToStringStyle.register(ch);
        try {
            StringBuffer sb = new StringBuffer();
            style.appendInternal(sb, "ch", ch, true);
            assertEquals("Q", sb.toString());
        } finally {
            ToStringStyle.unregister(ch);
        }
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testRegisterAndUnregisterNull() {
        // Must safely handle null arguments without throwing exceptions
        ToStringStyle.register(null);
        assertFalse(ToStringStyle.isRegistered(null));
        ToStringStyle.unregister(null);

        // Unregister an object that was never registered
        Object unreg = new Object();
        ToStringStyle.unregister(unreg);
        assertFalse(ToStringStyle.isRegistered(unreg));
    }

    @Test(timeout = 4000)
    public void testIsFullDetailLogic() {
        ConcreteToStringStyle style = new ConcreteToStringStyle();

        style.setDefaultFullDetail(true);
        assertTrue(style.isFullDetail(null));
        assertTrue(style.isFullDetail(Boolean.TRUE));
        assertFalse(style.isFullDetail(Boolean.FALSE));

        style.setDefaultFullDetail(false);
        assertFalse(style.isFullDetail(null));
        assertTrue(style.isFullDetail(Boolean.TRUE));
        assertFalse(style.isFullDetail(Boolean.FALSE));
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity (Singletons & Styles)
    // =========================================================================

    private Object serializeAndDeserialize(Object obj) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object result = ois.readObject();
        ois.close();
        return result;
    }

    @Test(timeout = 4000)
    public void testDefaultToStringStyle() throws Exception {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        assertSame(style, serializeAndDeserialize(style));

        StringBuffer sb = new StringBuffer();
        Object obj = new Integer(10);
        style.appendStart(sb, obj);
        style.append(sb, "val", 10);
        style.appendEnd(sb, obj);

        String out = sb.toString();
        assertTrue(out.startsWith("java.lang.Integer@"));
        assertTrue(out.endsWith("[val=10]"));
    }

    @Test(timeout = 4000)
    public void testMultiLineToStringStyle() throws Exception {
        ToStringStyle style = ToStringStyle.MULTI_LINE_STYLE;
        assertSame(style, serializeAndDeserialize(style));

        StringBuffer sb = new StringBuffer();
        Object obj = new Integer(20);
        style.appendStart(sb, obj);
        style.append(sb, "f1", "a");
        style.append(sb, "f2", "b");
        style.appendEnd(sb, obj);

        String out = sb.toString();
        assertTrue(out.contains(SystemUtils.LINE_SEPARATOR + "  f1=a"));
        assertTrue(out.contains(SystemUtils.LINE_SEPARATOR + "  f2=b"));
        assertTrue(out.endsWith(SystemUtils.LINE_SEPARATOR + "]"));
    }

    @Test(timeout = 4000)
    public void testNoFieldNameToStringStyle() throws Exception {
        ToStringStyle style = ToStringStyle.NO_FIELD_NAMES_STYLE;
        assertSame(style, serializeAndDeserialize(style));

        StringBuffer sb = new StringBuffer();
        Object obj = new Integer(30);
        style.appendStart(sb, obj);
        style.append(sb, "ignoredField", "targetValue");
        style.appendEnd(sb, obj);

        String out = sb.toString();
        assertFalse(out.contains("ignoredField"));
        assertTrue(out.contains("[targetValue]"));
    }

    @Test(timeout = 4000)
    public void testShortPrefixToStringStyle() throws Exception {
        ToStringStyle style = ToStringStyle.SHORT_PREFIX_STYLE;
        assertSame(style, serializeAndDeserialize(style));

        StringBuffer sb = new StringBuffer();
        Object obj = new Integer(40);
        style.appendStart(sb, obj);
        style.append(sb, "x", 1);
        style.appendEnd(sb, obj);

        String out = sb.toString();
        assertTrue(out.startsWith("Integer[x=1]"));
        assertFalse(out.contains("@"));
    }

    @Test(timeout = 4000)
    public void testSimpleToStringStyle() throws Exception {
        ToStringStyle style = ToStringStyle.SIMPLE_STYLE;
        assertSame(style, serializeAndDeserialize(style));

        StringBuffer sb = new StringBuffer();
        Object obj = new Integer(50);
        style.appendStart(sb, obj);
        style.append(sb, "field", "onlyValue");
        style.appendEnd(sb, obj);

        String out = sb.toString();
        assertEquals("onlyValue", out);
    }
}