package org.jsoup.nodes;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 *
 * Target: FormElement.java
 * Branches covered:
 *   - submit(): action from attr vs baseUri; method POST vs GET; Validate.notEmpty
 *   - formData(): loop over elements; isFormSubmittable; disabled; name length; type switch:
 *       select (selected options, fallback first), checkbox/radio (checked, val vs "on"), else (default)
 *   - addElement / elements(): chaining, list mutation
 * Defect: removeFormElement – when an element is removed from the DOM (or from the form's children),
 *         the internal `elements` list is not updated, causing size to remain unchanged.
 *         Test: add 3 elements, remove one, assert size == 2 (fails on buggy version: size == 3).
 */
public class FormElementDeepseekTest {

    // Helper: create a FormElement with a base URI and optional attributes
    private FormElement createForm(String baseUri, String action, String method) {
        Tag tag = Tag.valueOf("form");
        Attributes attrs = new Attributes();
        if (action != null) attrs.put("action", action);
        if (method != null) attrs.put("method", method);
        FormElement form = new FormElement(tag, baseUri, attrs);
        return form;
    }

    // Helper: create a simple text input element
    private Element createInput(String name, String value, String type) {
        Tag inputTag = Tag.valueOf("input");
        Attributes attrs = new Attributes();
        attrs.put("name", name);
        if (value != null) attrs.put("value", value);
        if (type != null) attrs.put("type", type);
        return new Element(inputTag, "", attrs);
    }

    // Helper: create a select element with options
    private Element createSelect(String name, String... optionValues) {
        Tag selectTag = Tag.valueOf("select");
        Attributes attrs = new Attributes();
        attrs.put("name", name);
        Element select = new Element(selectTag, "", attrs);
        for (String val : optionValues) {
            Element option = new Element(Tag.valueOf("option"), "", new Attributes());
            option.attr("value", val);
            select.appendChild(option);
        }
        return select;
    }

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testAddElementAndElements() {
        FormElement form = createForm("http://example.com", null, null);
        assertEquals(0, form.elements().size());
        Element input = createInput("name", "value", "text");
        FormElement returned = form.addElement(input);
        assertSame(form, returned); // chaining
        assertEquals(1, form.elements().size());
        assertTrue(form.elements().contains(input));
    }

    @Test(timeout = 4000)
    public void testFormDataWithTextInput() {
        FormElement form = createForm("http://example.com", null, null);
        form.addElement(createInput("username", "john", "text"));
        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("username", data.get(0).key());
        assertEquals("john", data.get(0).value());
    }

    @Test(timeout = 4000)
    public void testFormDataWithCheckboxChecked() {
        FormElement form = createForm("http://example.com", null, null);
        Element cb = createInput("agree", "", "checkbox");
        cb.attr("checked", "checked");
        form.addElement(cb);
        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("agree", data.get(0).key());
        assertEquals("on", data.get(0).value()); // default value when empty
    }

    @Test(timeout = 4000)
    public void testFormDataWithCheckboxUnchecked() {
        FormElement form = createForm("http://example.com", null, null);
        Element cb = createInput("agree", "yes", "checkbox");
        // no checked attribute
        form.addElement(cb);
        List<Connection.KeyVal> data = form.formData();
        assertEquals(0, data.size());
    }

    @Test(timeout = 4000)
    public void testFormDataWithRadioChecked() {
        FormElement form = createForm("http://example.com", null, null);
        Element radio = createInput("gender", "male", "radio");
        radio.attr("checked", "checked");
        form.addElement(radio);
        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("gender", data.get(0).key());
        assertEquals("male", data.get(0).value());
    }

    @Test(timeout = 4000)
    public void testFormDataWithSelectSingleSelected() {
        FormElement form = createForm("http://example.com", null, null);
        Element select = createSelect("country", "US", "UK", "DE");
        // select first option as selected
        select.child(0).attr("selected", "selected");
        form.addElement(select);
        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("country", data.get(0).key());
        assertEquals("US", data.get(0).value());
    }

    @Test(timeout = 4000)
    public void testFormDataWithSelectNoSelected() {
        FormElement form = createForm("http://example.com", null, null);
        Element select = createSelect("country", "US", "UK", "DE");
        // no selected attribute
        form.addElement(select);
        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("country", data.get(0).key());
        assertEquals("US", data.get(0).value()); // first option
    }

    @Test(timeout = 4000)
    public void testFormDataWithDisabledInput() {
        FormElement form = createForm("http://example.com", null, null);
        Element input = createInput("disabledField", "value", "text");
        input.attr("disabled", "disabled");
        form.addElement(input);
        List<Connection.KeyVal> data = form.formData();
        assertEquals(0, data.size());
    }

    @Test(timeout = 4000)
    public void testFormDataWithEmptyName() {
        FormElement form = createForm("http://example.com", null, null);
        Element input = createInput("", "value", "text");
        form.addElement(input);
        List<Connection.KeyVal> data = form.formData();
        assertEquals(0, data.size());
    }

    @Test(timeout = 4000)
    public void testFormDataWithNonFormSubmittable() {
        FormElement form = createForm("http://example.com", null, null);
        Element div = new Element(Tag.valueOf("div"), "", new Attributes());
        div.attr("name", "divField");
        form.addElement(div);
        List<Connection.KeyVal> data = form.formData();
        assertEquals(0, data.size());
    }

    @Test(timeout = 4000)
    public void testFormDataMultipleSameName() {
        FormElement form = createForm("http://example.com", null, null);
        form.addElement(createInput("hobby", "reading", "text"));
        form.addElement(createInput("hobby", "gaming", "text"));
        List<Connection.KeyVal> data = form.formData();
        assertEquals(2, data.size());
        assertEquals("hobby", data.get(0).key());
        assertEquals("reading", data.get(0).value());
        assertEquals("hobby", data.get(1).key());
        assertEquals("gaming", data.get(1).value());
    }

    // ==================== Partition B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000)
    public void testFormDataWithCheckboxCheckedNonEmptyValue() {
        FormElement form = createForm("http://example.com", null, null);
        Element cb = createInput("agree", "yes", "checkbox");
        cb.attr("checked", "checked");
        form.addElement(cb);
        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("yes", data.get(0).value());
    }

    @Test(timeout = 4000)
    public void testFormDataWithSelectMultipleSelected() {
        FormElement form = createForm("http://example.com", null, null);
        Element select = createSelect("colors", "red", "green", "blue");
        select.child(0).attr("selected", "selected");
        select.child(2).attr("selected", "selected");
        form.addElement(select);
        List<Connection.KeyVal> data = form.formData();
        assertEquals(2, data.size());
        assertEquals("colors", data.get(0).key());
        assertEquals("red", data.get(0).value());
        assertEquals("colors", data.get(1).key());
        assertEquals("blue", data.get(1).value());
    }

    @Test(timeout = 4000)
    public void testFormDataWithSelectEmptyOptions() {
        FormElement form = createForm("http://example.com", null, null);
        Element select = createSelect("empty"); // no options
        form.addElement(select);
        List<Connection.KeyVal> data = form.formData();
        assertEquals(0, data.size());
    }

    @Test(timeout = 4000)
    public void testFormDataWithNullElement() {
        FormElement form = createForm("http://example.com", null, null);
        try {
            form.addElement(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    @Test(timeout = 4000)
    public void testRemoveFormElement() {
        // This test directly targets the known defect: removing a form element from the DOM
        // does not update the internal elements list.
        FormElement form = createForm("http://example.com", null, null);
        Element input1 = createInput("field1", "val1", "text");
        Element input2 = createInput("field2", "val2", "text");
        Element input3 = createInput("field3", "val3", "text");
        form.addElement(input1);
        form.addElement(input2);
        form.addElement(input3);
        assertEquals(3, form.elements().size());

        // Remove one element from the DOM (simulating what a user would do)
        input2.remove(); // this removes from parent, but elements list is not updated

        // The bug: elements list still contains input2, so size remains 3.
        // Correct behavior: size should be 2.
        assertEquals("After removing an element, the form's elements list should be updated", 2, form.elements().size());
    }

    @Test(timeout = 4000)
    public void testRemoveFormElementViaRemoveChild() {
        // Alternative: remove via parent's removeChild
        FormElement form = createForm("http://example.com", null, null);
        Element input1 = createInput("a", "1", "text");
        Element input2 = createInput("b", "2", "text");
        form.addElement(input1);
        form.addElement(input2);
        assertEquals(2, form.elements().size());

        // Remove input1 from the DOM (form is the parent)
        form.removeChild(input1);
        // Bug: elements list unchanged -> size still 2
        assertEquals("After removeChild, elements list should be updated", 1, form.elements().size());
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSubmitNoActionNoBaseUri() {
        // No base URI and no action attribute -> should throw
        FormElement form = createForm(null, null, null);
        form.submit();
    }

    @Test(timeout = 4000)
    public void testSubmitWithAction() {
        FormElement form = createForm("http://example.com", "/submit", "GET");
        Connection conn = form.submit();
        assertNotNull(conn);
        // We cannot easily inspect the connection internals, but at least it doesn't throw.
    }

    @Test(timeout = 4000)
    public void testSubmitWithPostMethod() {
        FormElement form = createForm("http://example.com", "/submit", "POST");
        Connection conn = form.submit();
        assertNotNull(conn);
    }

    @Test(timeout = 4000)
    public void testSubmitWithBaseUriFallback() {
        // No action attribute, but base URI is set
        FormElement form = createForm("http://example.com", null, "GET");
        Connection conn = form.submit();
        assertNotNull(conn);
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testElementsReturnsSameList() {
        FormElement form = createForm("http://example.com", null, null);
        Elements elems1 = form.elements();
        Elements elems2 = form.elements();
        assertSame(elems1, elems2); // same reference
    }

    @Test(timeout = 4000)
    public void testFormDataReturnsCopy() {
        FormElement form = createForm("http://example.com", null, null);
        form.addElement(createInput("x", "y", "text"));
        List<Connection.KeyVal> data1 = form.formData();
        List<Connection.KeyVal> data2 = form.formData();
        assertNotSame(data1, data2); // different list objects
        assertEquals(data1, data2); // but equal content
    }

    @Test(timeout = 4000)
    public void testMultipleAddElementChaining() {
        FormElement form = createForm("http://example.com", null, null);
        form.addElement(createInput("a", "1", "text"))
            .addElement(createInput("b", "2", "text"))
            .addElement(createInput("c", "3", "text"));
        assertEquals(3, form.elements().size());
    }
}