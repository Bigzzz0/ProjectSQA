package org.jsoup.nodes;

import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.helper.HttpConnection;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

/**
 * Branch & Defect Analysis Matrix:
 * - Partition A: Core functional logic – formData() with various control types (text, checkbox, radio, select)
 * - Partition B: Boundary values – empty name, empty value, disabled, no options, multiple selected options
 * - Partition C: Defect-targeted – submit button inclusion (expected 6 vs 7 bug)
 * - Partition D: Exception paths – submit() with missing action, invalid base URI
 * - Partition E: Object lifecycle – addElement, elements(), removeChild
 *
 * Decision branches covered:
 *   - isFormSubmittable() true/false
 *   - hasAttr("disabled") true/false
 *   - name.length() == 0
 *   - "select".equals(normalName())
 *   - options with selected / no selected but first option exists / no options
 *   - checkbox/radio with checked / unchecked
 *   - else branch for other submittable elements (including submit buttons)
 *   - submit() action resolution and method detection
 */
public class FormElementDeepseekTest {

    // Helper to parse a form from HTML
    private FormElement parseForm(String html) {
        Document doc = Jsoup.parse(html);
        return (FormElement) doc.select("form").first();
    }

    // ========== Partition A: Core Functional Logic ==========

    @Test(timeout = 4000)
    public void testFormDataWithTextInput() {
        FormElement form = parseForm("<form><input name='q' value='search'></form>");
        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("q", data.get(0).key());
        assertEquals("search", data.get(0).value());
    }

    @Test(timeout = 4000)
    public void testFormDataWithMultipleTextInputs() {
        FormElement form = parseForm("<form>" +
                "<input name='a' value='1'>" +
                "<input name='b' value='2'>" +
                "</form>");
        List<Connection.KeyVal> data = form.formData();
        assertEquals(2, data.size());
        assertEquals("a", data.get(0).key());
        assertEquals("1", data.get(0).value());
        assertEquals("b", data.get(1).key());
        assertEquals("2", data.get(1).value());
    }

    @Test(timeout = 4000)
    public void testFormDataWithTextarea() {
        FormElement form = parseForm("<form><textarea name='msg'>hello</textarea></form>");
        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("msg", data.get(0).key());
        assertEquals("hello", data.get(0).value());
    }

    // ========== Partition B: Boundary Value Analysis ==========

    @Test(timeout = 4000)
    public void testFormDataSkipsDisabled() {
        FormElement form = parseForm("<form>" +
                "<input name='a' value='1'>" +
                "<input name='b' value='2' disabled>" +
                "</form>");
        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("a", data.get(0).key());
    }

    @Test(timeout = 4000)
    public void testFormDataSkipsNoName() {
        FormElement form = parseForm("<form>" +
                "<input value='1'>" +
                "<input name='a' value='2'>" +
                "</form>");
        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("a", data.get(0).key());
    }

    @Test(timeout = 4000)
    public void testFormDataSkipsNonSubmittable() {
        FormElement form = parseForm("<form>" +
                "<div name='x'>text</div>" +
                "<input name='a' value='1'>" +
                "</form>");
        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("a", data.get(0).key());
    }

    @Test(timeout = 4000)
    public void testFormDataSelectWithSelectedOptions() {
        FormElement form = parseForm("<form>" +
                "<select name='color'>" +
                "<option value='red' selected>Red</option>" +
                "<option value='blue' selected>Blue</option>" +
                "<option value='green'>Green</option>" +
                "</select>" +
                "</form>");
        List<Connection.KeyVal> data = form.formData();
        assertEquals(2, data.size());
        assertEquals("color", data.get(0).key());
        assertEquals("red", data.get(0).value());
        assertEquals("color", data.get(1).key());
        assertEquals("blue", data.get(1).value());
    }

    @Test(timeout = 4000)
    public void testFormDataSelectNoSelectedOption() {
        FormElement form = parseForm("<form>" +
                "<select name='color'>" +
                "<option value='red'>Red</option>" +
                "<option value='blue'>Blue</option>" +
                "</select>" +
                "</form>");
        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("color", data.get(0).key());
        assertEquals("red", data.get(0).value()); // first option
    }

    @Test(timeout = 4000)
    public void testFormDataSelectNoOptions() {
        FormElement form = parseForm("<form>" +
                "<select name='empty'></select>" +
                "</form>");
        List<Connection.KeyVal> data = form.formData();
        assertEquals(0, data.size());
    }

    @Test(timeout = 4000)
    public void testFormDataCheckboxChecked() {
        FormElement form = parseForm("<form>" +
                "<input type='checkbox' name='agree' checked value='yes'>" +
                "</form>");
        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("agree", data.get(0).key());
        assertEquals("yes", data.get(0).value());
    }

    @Test(timeout = 4000)
    public void testFormDataCheckboxUnchecked() {
        FormElement form = parseForm("<form>" +
                "<input type='checkbox' name='agree' value='yes'>" +
                "</form>");
        List<Connection.KeyVal> data = form.formData();
        assertEquals(0, data.size());
    }

    @Test(timeout = 4000)
    public void testFormDataCheckboxCheckedNoValue() {
        FormElement form = parseForm("<form>" +
                "<input type='checkbox' name='agree' checked>" +
                "</form>");
        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("agree", data.get(0).key());
        assertEquals("on", data.get(0).value()); // default value
    }

    @Test(timeout = 4000)
    public void testFormDataRadioChecked() {
        FormElement form = parseForm("<form>" +
                "<input type='radio' name='gender' value='male' checked>" +
                "<input type='radio' name='gender' value='female'>" +
                "</form>");
        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("gender", data.get(0).key());
        assertEquals("male", data.get(0).value());
    }

    @Test(timeout = 4000)
    public void testFormDataRadioUnchecked() {
        FormElement form = parseForm("<form>" +
                "<input type='radio' name='gender' value='male'>" +
                "</form>");
        List<Connection.KeyVal> data = form.formData();
        assertEquals(0, data.size());
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========
    // Known defect: submit button is included in form data, causing extra entry.
    // Expected correct behavior: submit buttons should NOT be included.
    @Test(timeout = 4000)
    public void testFormDataExcludesSubmitButton() {
        // Form with text input and a submit button. Expected data count = 1 (only text input).
        FormElement form = parseForm("<form>" +
                "<input name='user' value='alice'>" +
                "<input type='submit' name='submit' value='Send'>" +
                "</form>");
        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size()); // Bug: defective version returns 2
        assertEquals("user", data.get(0).key());
        assertEquals("alice", data.get(0).value());
    }

    @Test(timeout = 4000)
    public void testFormDataExcludesButtonElement() {
        // Also test <button type='submit'> which is form submittable
        FormElement form = parseForm("<form>" +
                "<input name='q' value='search'>" +
                "<button type='submit' name='btn' value='go'>Go</button>" +
                "</form>");
        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("q", data.get(0).key());
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSubmitThrowsWhenNoActionAndNoBaseUri() {
        // Form without action and document without base URI
        FormElement form = parseForm("<form><input name='a' value='1'></form>");
        form.submit(); // should throw
    }

    @Test(timeout = 4000)
    public void testSubmitWithAction() {
        FormElement form = parseForm("<form action='/submit'><input name='a' value='1'></form>");
        Connection conn = form.submit();
        assertNotNull(conn);
        // We cannot execute the connection, but we can verify it's set up
        // The method should be GET by default
        assertEquals(Connection.Method.GET, conn.request().method());
    }

    @Test(timeout = 4000)
    public void testSubmitWithPostMethod() {
        FormElement form = parseForm("<form action='/submit' method='POST'><input name='a' value='1'></form>");
        Connection conn = form.submit();
        assertEquals(Connection.Method.POST, conn.request().method());
    }

    @Test(timeout = 4000)
    public void testSubmitWithAbsoluteAction() {
        // Use base URI to resolve relative action
        Document doc = Jsoup.parse("<form action='/submit'><input name='a' value='1'></form>", "http://example.com");
        FormElement form = (FormElement) doc.select("form").first();
        Connection conn = form.submit();
        // The action should be absolute
        assertTrue(conn.request().url().toString().startsWith("http://example.com/submit"));
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testAddElementAndElements() {
        FormElement form = new FormElement(Tag.valueOf("form"), "", new Attributes());
        Element input = new Element(Tag.valueOf("input"), "").attr("name", "test").val("value");
        form.addElement(input);
        assertEquals(1, form.elements().size());
        assertTrue(form.elements().contains(input));
    }

    @Test(timeout = 4000)
    public void testRemoveChildUpdatesElements() {
        FormElement form = parseForm("<form><input name='a' value='1'><input name='b' value='2'></form>");
        Element inputA = form.select("input[name=a]").first();
        Element inputB = form.select("input[name=b]").first();
        assertEquals(2, form.elements().size());
        inputA.remove(); // triggers removeChild
        assertEquals(1, form.elements().size());
        assertFalse(form.elements().contains(inputA));
        assertTrue(form.elements().contains(inputB));
    }

    @Test(timeout = 4000)
    public void testFormDataReturnsCopy() {
        FormElement form = parseForm("<form><input name='a' value='1'></form>");
        List<Connection.KeyVal> data = form.formData();
        data.clear(); // modify the returned list
        // The original form data should be unaffected
        assertEquals(1, form.formData().size());
    }

    // Additional edge: empty form
    @Test(timeout = 4000)
    public void testEmptyFormData() {
        FormElement form = parseForm("<form></form>");
        List<Connection.KeyVal> data = form.formData();
        assertEquals(0, data.size());
    }

    // Test that checkbox/radio with type case-insensitive works
    @Test(timeout = 4000)
    public void testCheckboxCaseInsensitive() {
        FormElement form = parseForm("<form>" +
                "<input type='CHECKBOX' name='agree' checked value='yes'>" +
                "</form>");
        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("agree", data.get(0).key());
    }
}