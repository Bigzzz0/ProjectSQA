package com.fasterxml.jackson.core.json;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;

/* [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Class Under Test: com.fasterxml.jackson.core.json.JsonWriteContext
 * Defect Reference: Defects4J / JacksonCore GeneratorFailTest (testFailOnWritingStringNotFieldNameBytes)
 *
 * 1. Root Defect Analysis (Partition C):
 *    - In an Object context (TYPE_OBJECT), writing a value via writeValue() must strictly require that
 *      a field name has already been provided (_gotName == true).
 *    - Buggy Condition: writeValue() unconditionally resets _gotName and increments _index, returning
 *      STATUS_OK_AFTER_COLON even when _gotName is false, allowing values without preceding field names (e.g. {:"a"}).
 *    - Expected Behavior: writeValue() must return STATUS_EXPECT_NAME (value 5) when _gotName == false in TYPE_OBJECT.
 *
 * 2. Decision Branches & State Space Covered:
 *    - Context Types: Root (TYPE_ROOT), Array (TYPE_ARRAY), Object (TYPE_OBJECT).
 *    - Root writeValue(): first item (STATUS_OK_AS_IS, _index == 0) vs subsequent items (STATUS_OK_AFTER_SPACE).
 *    - Array writeValue(): first item (STATUS_OK_AS_IS, _index < 0 -> 0) vs subsequent items (STATUS_OK_AFTER_COMMA).
 *    - Object writeFieldName():
 *      * If _gotName == true -> returns STATUS_EXPECT_VALUE.
 *      * If _gotName == false, _index < 0 -> returns STATUS_OK_AS_IS.
 *      * If _gotName == false, _index >= 0 -> returns STATUS_OK_AFTER_COMMA.
 *    - Object writeValue():
 *      * If _gotName == false -> returns STATUS_EXPECT_NAME (Defect Target).
 *      * If _gotName == true -> returns STATUS_OK_AFTER_COLON, resets _gotName to false.
 *    - Duplicate Detection (DupDetector):
 *      * Null vs non-null DupDetector on root and child contexts.
 *      * Duplicate field name triggers JsonGenerationException.
 *      * Unique field names pass successfully.
 *    - Child Reusability & Reset:
 *      * First child creation allocates new JsonWriteContext with child DupDetector.
 *      * Subsequent child creation reuses cached _child and invokes reset(int type).
 *    - Path & String Formatting (appendDesc / toString):
 *      * Root returns "/".
 *      * Array returns "[index]".
 *      * Object returns "{\"fieldName\"}" when currentName != null, and "{?}" when currentName is null.
 * ====================================================================================================
 */
public class JsonWriteContextGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testRootContextInitialState() {
        JsonWriteContext root = JsonWriteContext.createRootContext();
        assertNull("Root context parent must be null", root.getParent());
        assertTrue("Root context should report inRoot()", root.inRoot());
        assertFalse("Root context is not array", root.inArray());
        assertFalse("Root context is not object", root.inObject());
        assertEquals("Initial index should be 0", 0, root.getCurrentIndex());
        assertEquals("Initial entry count should be 0", 0, root.getEntryCount());
        assertNull("Initial currentName must be null", root.getCurrentName());
        assertNull("Initial currentValue must be null", root.getCurrentValue());
        assertNull("Default DupDetector should be null", root.getDupDetector());
    }

    @Test(timeout = 4000)
    public void testRootContextValueTransitions() {
        JsonWriteContext root = JsonWriteContext.createRootContext();

        int first = root.writeValue();
        assertEquals("First root value should be OK_AS_IS", JsonWriteContext.STATUS_OK_AS_IS, first);
        assertEquals(0, root.getCurrentIndex());

        int second = root.writeValue();
        assertEquals("Subsequent root value should be OK_AFTER_SPACE", JsonWriteContext.STATUS_OK_AFTER_SPACE, second);
        assertEquals(1, root.getCurrentIndex());

        int third = root.writeValue();
        assertEquals("Subsequent root value should be OK_AFTER_SPACE", JsonWriteContext.STATUS_OK_AFTER_SPACE, third);
        assertEquals(2, root.getCurrentIndex());
    }

    @Test(timeout = 4000)
    public void testArrayContextValueTransitions() {
        JsonWriteContext root = JsonWriteContext.createRootContext();
        JsonWriteContext array = root.createChildArrayContext();

        assertSame("Child array must point to root parent", root, array.getParent());
        assertTrue("Should be inArray()", array.inArray());
        assertEquals(0, array.getCurrentIndex());

        int first = array.writeValue();
        assertEquals("First array element should be OK_AS_IS", JsonWriteContext.STATUS_OK_AS_IS, first);
        assertEquals(0, array.getCurrentIndex());

        int second = array.writeValue();
        assertEquals("Second array element should be OK_AFTER_COMMA", JsonWriteContext.STATUS_OK_AFTER_COMMA, second);
        assertEquals(1, array.getCurrentIndex());

        int third = array.writeValue();
        assertEquals("Third array element should be OK_AFTER_COMMA", JsonWriteContext.STATUS_OK_AFTER_COMMA, third);
        assertEquals(2, array.getCurrentIndex());
    }

    @Test(timeout = 4000)
    public void testObjectContextNormalTransitions() throws Exception {
        JsonWriteContext root = JsonWriteContext.createRootContext();
        JsonWriteContext obj = root.createChildObjectContext();

        assertSame("Child object must point to root parent", root, obj.getParent());
        assertTrue("Should be inObject()", obj.inObject());
        assertEquals(0, obj.getCurrentIndex());

        // First field name
        int nameStatus1 = obj.writeFieldName("first");
        assertEquals("First field name should be OK_AS_IS", JsonWriteContext.STATUS_OK_AS_IS, nameStatus1);
        assertEquals("first", obj.getCurrentName());

        // First value
        int valStatus1 = obj.writeValue();
        assertEquals("Value after field name should be OK_AFTER_COLON", JsonWriteContext.STATUS_OK_AFTER_COLON, valStatus1);
        assertEquals(0, obj.getCurrentIndex());

        // Second field name
        int nameStatus2 = obj.writeFieldName("second");
        assertEquals("Second field name after value should be OK_AFTER_COMMA", JsonWriteContext.STATUS_OK_AFTER_COMMA, nameStatus2);
        assertEquals("second", obj.getCurrentName());

        // Second value
        int valStatus2 = obj.writeValue();
        assertEquals("Value after field name should be OK_AFTER_COLON", JsonWriteContext.STATUS_OK_AFTER_COLON, valStatus2);
        assertEquals(1, obj.getCurrentIndex());
    }

    @Test(timeout = 4000)
    public void testObjectContextWriteFieldNameWhenExpectingValue() throws Exception {
        JsonWriteContext root = JsonWriteContext.createRootContext();
        JsonWriteContext obj = root.createChildObjectContext();

        int firstStatus = obj.writeFieldName("field1");
        assertEquals(JsonWriteContext.STATUS_OK_AS_IS, firstStatus);

        // Attempting to write another field name without having written a value
        int secondStatus = obj.writeFieldName("field2");
        assertEquals("Writing name when expecting value must return STATUS_EXPECT_VALUE",
                JsonWriteContext.STATUS_EXPECT_VALUE, secondStatus);
    }

    @Test(timeout = 4000)
    public void testCurrentValueGetterSetter() {
        JsonWriteContext root = JsonWriteContext.createRootContext();
        assertNull(root.getCurrentValue());

        Object testObj = new Object();
        root.setCurrentValue(testObj);
        assertSame(testObj, root.getCurrentValue());

        root.setCurrentValue(null);
        assertNull(root.getCurrentValue());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyAndSpecialFieldNames() throws Exception {
        JsonWriteContext root = JsonWriteContext.createRootContext();
        JsonWriteContext obj = root.createChildObjectContext();

        int statusEmpty = obj.writeFieldName("");
        assertEquals(JsonWriteContext.STATUS_OK_AS_IS, statusEmpty);
        assertEquals("", obj.getCurrentName());
        assertEquals("{\"" + "\"}", obj.toString());

        obj.writeValue();

        int statusWithWhitespace = obj.writeFieldName("   ");
        assertEquals(JsonWriteContext.STATUS_OK_AFTER_COMMA, statusWithWhitespace);
        assertEquals("   ", obj.getCurrentName());
        assertEquals("{\"   \"}", obj.toString());
    }

    @Test(timeout = 4000)
    public void testNullFieldNameHandling() throws Exception {
        JsonWriteContext root = JsonWriteContext.createRootContext();
        JsonWriteContext obj = root.createChildObjectContext();

        int status = obj.writeFieldName(null);
        assertEquals(JsonWriteContext.STATUS_OK_AS_IS, status);
        assertNull(obj.getCurrentName());
        assertEquals("{?}", obj.toString());
    }

    @Test(timeout = 4000)
    public void testChildInstanceReuseAndReset() {
        JsonWriteContext root = JsonWriteContext.createRootContext();

        // 1. Create first array child
        JsonWriteContext childArray = root.createChildArrayContext();
        childArray.writeValue();
        childArray.setCurrentValue("arrayVal");
        assertEquals(0, childArray.getCurrentIndex());
        assertEquals("arrayVal", childArray.getCurrentValue());

        // 2. Reuse cached child slot by creating an object context from root
        JsonWriteContext childObj = root.createChildObjectContext();
        assertSame("Child instance should be reused to avoid allocations", childArray, childObj);
        assertTrue("Reused instance must now be OBJECT", childObj.inObject());
        assertFalse("Reused instance must no longer be ARRAY", childObj.inArray());
        assertEquals("Reset must reset index to -1 (getCurrentIndex() returns 0)", 0, childObj.getCurrentIndex());
        assertEquals("Entry count should be reset to 0", 0, childObj.getEntryCount());
        assertNull("Current name must be reset to null", childObj.getCurrentName());
        assertNull("Current value must be reset to null", childObj.getCurrentValue());

        // 3. Reuse child slot again by switching back to array
        JsonWriteContext childArray2 = root.createChildArrayContext();
        assertSame(childArray, childArray2);
        assertTrue(childArray2.inArray());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * TARGET DEFECT:
     * Defects4J GeneratorFailTest: testFailOnWritingStringNotFieldNameBytes / testFailOnWritingStringNotFieldNameChars
     *
     * In an Object context, attempting to write a value before any field name has been written
     * MUST return STATUS_EXPECT_NAME (value 5).
     * In defective JacksonCore, writeValue() did not check !_gotName and instead returned STATUS_OK_AFTER_COLON (value 2),
     * allowing invalid JSON output like `{:"value"`.
     */
    @Test(timeout = 4000)
    public void testFailOnWritingValueInObjectContextWhenExpectingFieldName() {
        JsonWriteContext root = JsonWriteContext.createRootContext();
        JsonWriteContext obj = root.createChildObjectContext();

        // Object has just been created; no field name written yet
        int status = obj.writeValue();
        assertEquals("Writing value in object context without field name must return STATUS_EXPECT_NAME",
                JsonWriteContext.STATUS_EXPECT_NAME, status);
    }

    /**
     * TARGET DEFECT (Subsequent Value Variation):
     * After writing a valid property and value, attempting to write another value immediately
     * without a new field name MUST also return STATUS_EXPECT_NAME.
     */
    @Test(timeout = 4000)
    public void testFailOnWritingConsecutiveValuesInObjectContext() throws Exception {
        JsonWriteContext root = JsonWriteContext.createRootContext();
        JsonWriteContext obj = root.createChildObjectContext();

        obj.writeFieldName("first");
        int val1Status = obj.writeValue();
        assertEquals(JsonWriteContext.STATUS_OK_AFTER_COLON, val1Status);

        // Immediately write a second value without calling writeFieldName
        int val2Status = obj.writeValue();
        assertEquals("Second consecutive value in object context must return STATUS_EXPECT_NAME",
                JsonWriteContext.STATUS_EXPECT_NAME, val2Status);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testDuplicateDetectionInObjectContext() throws Exception {
        DupDetector dd = DupDetector.rootDetector((JsonGenerator) null);
        JsonWriteContext root = JsonWriteContext.createRootContext(dd);
        JsonWriteContext obj = root.createChildObjectContext();

        int first = obj.writeFieldName("dupKey");
        assertEquals(JsonWriteContext.STATUS_OK_AS_IS, first);
        obj.writeValue();

        try {
            obj.writeFieldName("dupKey");
            fail("Expected JsonGenerationException due to duplicate field name");
        } catch (JsonGenerationException e) {
            assertTrue("Exception message should identify the duplicate field",
                    e.getMessage().contains("Duplicate field 'dupKey'"));
        }
    }

    @Test(timeout = 4000)
    public void testDuplicateDetectionResetOnContextReuse() throws Exception {
        DupDetector dd = DupDetector.rootDetector((JsonGenerator) null);
        JsonWriteContext root = JsonWriteContext.createRootContext(dd);

        // First object life cycle
        JsonWriteContext obj1 = root.createChildObjectContext();
        obj1.writeFieldName("fieldX");
        obj1.writeValue();

        // Reuse child context for a new object; DupDetector should be reset
        JsonWriteContext obj2 = root.createChildObjectContext();
        int status = obj2.writeFieldName("fieldX");
        assertEquals("After reset on reused context, fieldX should not be considered duplicate",
                JsonWriteContext.STATUS_OK_AS_IS, status);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    @SuppressWarnings("deprecation")
    public void testDeprecatedCreateRootContext() {
        JsonWriteContext root = JsonWriteContext.createRootContext();
        assertNotNull(root);
        assertTrue(root.inRoot());
        assertNull(root.getDupDetector());
    }

    @Test(timeout = 4000)
    public void testWithDupDetector() {
        JsonWriteContext root = JsonWriteContext.createRootContext();
        assertNull(root.getDupDetector());

        DupDetector dd = DupDetector.rootDetector((JsonGenerator) null);
        JsonWriteContext updated = root.withDupDetector(dd);

        assertSame("withDupDetector should return the same context instance", root, updated);
        assertSame("DupDetector should be set on the context", dd, root.getDupDetector());

        JsonWriteContext childObj = updated.createChildObjectContext();
        assertNotNull("Child object context should inherit a child DupDetector", childObj.getDupDetector());
    }

    @Test(timeout = 4000)
    public void testToStringRepresentations() throws Exception {
        JsonWriteContext root = JsonWriteContext.createRootContext();
        assertEquals("/", root.toString());

        JsonWriteContext arr = root.createChildArrayContext();
        assertEquals("[0]", arr.toString());
        arr.writeValue();
        assertEquals("[0]", arr.toString());
        arr.writeValue();
        assertEquals("[1]", arr.toString());

        JsonWriteContext obj = root.createChildObjectContext();
        assertEquals("{?}", obj.toString());

        obj.writeFieldName("id");
        assertEquals("{\"id\"}", obj.toString());
    }

    @Test(timeout = 4000)
    public void testToStringNestedContextHierarchy() {
        JsonWriteContext root = JsonWriteContext.createRootContext();
        JsonWriteContext childArr = root.createChildArrayContext();
        JsonWriteContext grandChildObj = childArr.createChildObjectContext();

        assertEquals("{?}", grandChildObj.toString());
        assertSame(childArr, grandChildObj.getParent());
        assertSame(root, childArr.getParent());
        assertNull(root.getParent());
    }
}