package org.jsoup.nodes;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 *
 * Target: FormElement.java (Defects4J)
 * Known Defects:
 *   1. formData() returns 7 key-vals instead of 6 (extra element counted)
 *   2. Checkbox without value attribute yields empty string instead of "on"
 *
 * Branch Coverage Targets:
 *   - submit(): action resolution (hasAttr("action") vs baseUri), method detection (POST/GET), Validate.notEmpty
 *   - formData(): loop over elements, isFormSubmittable(), name length check, select handling (selected options, fallback first option), checkbox/radio checked attribute, default else branch
 *   - addElement() / elements() getter
 *   - equals() delegation
 *
 * Partitions:
 *   A: Core functional logic – addElement, elements, formData with various controls
 *   B: Boundary – empty elements, null/empty attributes, missing base URI
 *   C: Defect-targeted – checkbox without value, formData count with specific set
 *   D: Exception paths – submit() with missing action and no base URI
 *   E: Object contract – equals
 */
public class FormElementDeepseekTest {

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void addElementAndElements() {
        FormElement form = new FormElement(Tag.valueOf("form"), "http://example.com", new Attributes());
        Element input = new Element(Tag.valueOf("input"), "").attr("name", "user").attr("type", "text");
        form.addElement(input);
        assertEquals(1, form.elements().size());
        assertSame(input, form.elements().get(0));
    }

    @Test(timeout = 4000)
    public void formDataWithTextInput() {
        FormElement form = new FormElement(Tag.valueOf("form"), "http://example.com", new Attributes());
        Element input = new Element(Tag.valueOf("input"), "").attr("name", "username").attr("type", "text").val("john");
        form.addElement(input);
        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("username", data.get(0).key());
        assertEquals("john", data.get(0).value());
    }

    @Test(timeout = 4000)
    public void formDataSkipsNonSubmittable() {
        FormElement form = new FormElement(Tag.valueOf("form"), "http://example.com", new Attributes());
        Element fieldset = new Element(Tag.valueOf("fieldset"), "").attr("name", "group");
        form.addElement(fieldset);
        List<Connection.KeyVal> data = form.formData();
        assertTrue(data.isEmpty());
    }

    @Test(timeout = 4000)
    public void formDataSkipsEmptyName() {
        FormElement form = new FormElement(Tag.valueOf("form"), "http://example.com", new Attributes());
        Element input = new Element(Tag.valueOf("input"), "").attr("type", "text").val("no name");
        form.addElement(input);
        List<Connection.KeyVal> data = form.formData();
        assertTrue(data.isEmpty());
    }

    @Test(timeout = 4000)
    public void formDataSelectWithSelectedOptions() {
        FormElement form = new FormElement(Tag.valueOf("form"), "http://example.com", new Attributes());
        Element select = new Element(Tag.valueOf("select"), "").attr("name", "color");
        Element option1 = new Element(Tag.valueOf("option"), "").val("red").attr("selected", "");
        Element option2 = new Element(Tag.valueOf("option"), "").val("blue");
        select.appendChild(option1);
        select.appendChild(option2);
        form.addElement(select);
        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("color", data.get(0).key());
        assertEquals("red", data.get(0).value());
    }

    @Test(timeout = 4000)
    public void formDataSelectFallbackFirstOption() {
        FormElement form = new FormElement(Tag.valueOf("form"), "http://example.com", new Attributes());
        Element select = new Element(Tag.valueOf("select"), "").attr("name", "size");
        Element option = new Element(Tag.valueOf("option"), "").val("M");
        select.appendChild(option);
        form.addElement(select);
        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("size", data.get(0).key());
        assertEquals("M", data.get(0).value());
    }

    @Test(timeout = 4000)
    public void formDataCheckboxChecked() {
        FormElement form = new FormElement(Tag.valueOf("form"), "http://example.com", new Attributes());
        Element checkbox = new Element(Tag.valueOf("input"), "").attr("name", "agree").attr("type", "checkbox").attr("checked", "").val("yes");
        form.addElement(checkbox);
        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("agree", data.get(0).key());
        assertEquals("yes", data.get(0).value());
    }

    @Test(timeout = 4000)
    public void formDataCheckboxNotChecked() {
        FormElement form = new FormElement(Tag.valueOf("form"), "http://example.com", new Attributes());
        Element checkbox = new Element(Tag.valueOf("input"), "").attr("name", "agree").attr("type", "checkbox").val("yes");
        form.addElement(checkbox);
        List<Connection.KeyVal> data = form.formData();
        assertTrue(data.isEmpty());
    }

    @Test(timeout = 4000)
    public void formDataRadioChecked() {
        FormElement form = new FormElement(Tag.valueOf("form"), "http://example.com", new Attributes());
        Element radio = new Element(Tag.valueOf("input"), "").attr("name", "gender").attr("type", "radio").attr("checked", "").val("male");
        form.addElement(radio);
        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("gender", data.get(0).key());
        assertEquals("male", data.get(0).value());
    }

    @Test(timeout = 4000)
    public void formDataRadioNotChecked() {
        FormElement form = new FormElement(Tag.valueOf("form"), "http://example.com", new Attributes());
        Element radio = new Element(Tag.valueOf("input"), "").attr("name", "gender").attr("type", "radio").val("female");
        form.addElement(radio);
        List<Connection.KeyVal> data = form.formData();
        assertTrue(data.isEmpty());
    }

    @Test(timeout = 4000)
    public void formDataReturnsCopy() {
        FormElement form = new FormElement(Tag.valueOf("form"), "http://example.com", new Attributes());
        Element input = new Element(Tag.valueOf("input"), "").attr("name", "test").attr("type", "text").val("a");
        form.addElement(input);
        List<Connection.KeyVal> data = form.formData();
        data.clear();
        assertEquals(1, form.formData().size()); // original unchanged
    }

    // ==================== Partition B: Boundary & Extremes ====================

    @Test(timeout = 4000)
    public void formDataEmptyElements() {
        FormElement form = new FormElement(Tag.valueOf("form"), "http://example.com", new Attributes());
        List<Connection.KeyVal> data = form.formData();
        assertTrue(data.isEmpty());
    }

    @Test(timeout = 4000)
    public void formDataSelectNoOptions() {
        FormElement form = new FormElement(Tag.valueOf("form"), "http://example.com", new Attributes());
        Element select = new Element(Tag.valueOf("select"), "").attr("name", "empty");
        form.addElement(select);
        List<Connection.KeyVal> data = form.formData();
        assertTrue(data.isEmpty());
    }

    @Test(timeout = 4000)
    public void submitWithAction() {
        FormElement form = new FormElement(Tag.valueOf("form"), "http://example.com", new Attributes());
        form.attr("action", "/submit");
        Connection con = form.submit();
        assertNotNull(con);
        assertEquals("http://example.com/submit", con.request().url().toString());
    }

    @Test(timeout = 4000)
    public void submitWithoutActionUsesBaseUri() {
        FormElement form = new FormElement(Tag.valueOf("form"), "http://example.com", new Attributes());
        Connection con = form.submit();
        assertNotNull(con);
        assertEquals("http://example.com", con.request().url().toString());
    }

    @Test(timeout = 4000)
    public void submitMethodPost() {
        FormElement form = new FormElement(Tag.valueOf("form"), "http://example.com", new Attributes());
        form.attr("method", "POST");
        Connection con = form.submit();
        assertEquals(Connection.Method.POST, con.request().method());
    }

    @Test(timeout = 4000)
    public void submitMethodGetDefault() {
        FormElement form = new FormElement(Tag.valueOf("form"), "http://example.com", new Attributes());
        Connection con = form.submit();
        assertEquals(Connection.Method.GET, con.request().method());
    }

    // ==================== Partition C: Defect-Targeted Tests ====================

    /**
     * Targets defect: Checkbox without value attribute should submit "on".
     * Expected: key-val value is "on", not empty string.
     */
    @Test(timeout = 4000)
    public void checkboxWithoutValueDefaultsToOn() {
        FormElement form = new FormElement(Tag.valueOf("form"), "http://example.com", new Attributes());
        Element checkbox = new Element(Tag.valueOf("input"), "").attr("name", "agree").attr("type", "checkbox").attr("checked", "");
        // No value attribute set
        form.addElement(checkbox);
        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("agree", data.get(0).key());
        assertEquals("on", data.get(0).value()); // This will fail on buggy version (returns "")
    }

    /**
     * Targets defect: formData() returns 7 key-vals instead of 6.
     * Construct a form with exactly 6 submittable elements (with names) and assert size 6.
     * The bug may cause an extra element to be included (e.g., a submit button or duplicate).
     */
    @Test(timeout = 4000)
    public void formDataCountIsCorrect() {
        FormElement form = new FormElement(Tag.valueOf("form"), "http://example.com", new Attributes());

        // 1. text input
        Element text = new Element(Tag.valueOf("input"), "").attr("name", "a").attr("type", "text").val("1");
        form.addElement(text);
        // 2. hidden input
        Element hidden = new Element(Tag.valueOf("input"), "").attr("name", "b").attr("type", "hidden").val("2");
        form.addElement(hidden);
        // 3. checkbox (checked, with value)
        Element cb = new Element(Tag.valueOf("input"), "").attr("name", "c").attr("type", "checkbox").attr("checked", "").val("3");
        form.addElement(cb);
        // 4. radio (checked)
        Element radio = new Element(Tag.valueOf("input"), "").attr("name", "d").attr("type", "radio").attr("checked", "").val("4");
        form.addElement(radio);
        // 5. select with selected option
        Element select = new Element(Tag.valueOf("select"), "").attr("name", "e");
        Element opt = new Element(Tag.valueOf("option"), "").val("5").attr("selected", "");
        select.appendChild(opt);
        form.addElement(select);
        // 6. textarea
        Element textarea = new Element(Tag.valueOf("textarea"), "").attr("name", "f").val("6");
        form.addElement(textarea);

        List<Connection.KeyVal> data = form.formData();
        assertEquals("Expected 6 key-vals, but got " + data.size(), 6, data.size());
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void submitThrowsWhenNoActionAndNoBaseUri() {
        FormElement form = new FormElement(Tag.valueOf("form"), "", new Attributes());
        form.submit(); // baseUri is empty, no action attribute
    }

    @Test(timeout = 4000)
    public void submitThrowsWithMessage() {
        FormElement form = new FormElement(Tag.valueOf("form"), "", new Attributes());
        try {
            form.submit();
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Could not determine a form action URL"));
        }
    }

    // ==================== Partition E: Object Contract ====================

    @Test(timeout = 4000)
    public void equalsSameAttributes() {
        Attributes attrs = new Attributes();
        attrs.put("id", "form1");
        FormElement f1 = new FormElement(Tag.valueOf("form"), "http://example.com", attrs);
        FormElement f2 = new FormElement(Tag.valueOf("form"), "http://example.com", attrs);
        assertEquals(f1, f2);
    }

    @Test(timeout = 4000)
    public void equalsDifferentAttributes() {
        Attributes attrs1 = new Attributes();
        attrs1.put("id", "form1");
        Attributes attrs2 = new Attributes();
        attrs2.put("id", "form2");
        FormElement f1 = new FormElement(Tag.valueOf("form"), "http://example.com", attrs1);
        FormElement f2 = new FormElement(Tag.valueOf("form"), "http://example.com", attrs2);
        assertNotEquals(f1, f2);
    }

    @Test(timeout = 4000)
    public void equalsWithNull() {
        FormElement f = new FormElement(Tag.valueOf("form"), "http://example.com", new Attributes());
        assertNotNull(f);
        assertNotEquals(f, null);
    }

    @Test(timeout = 4000)
    public void equalsWithDifferentType() {
        FormElement f = new FormElement(Tag.valueOf("form"), "http://example.com", new Attributes());
        assertNotEquals(f, "string");
    }
}