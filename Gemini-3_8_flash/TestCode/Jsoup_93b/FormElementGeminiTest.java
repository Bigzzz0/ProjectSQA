package org.jsoup.nodes;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: org.jsoup.nodes.FormElement
 *
 * Branch / Condition Coverage:
 * 1. Constructor: FormElement(Tag, String, Attributes) -> initializes elements collection.
 * 2. elements(): Returns the internal Elements list containing form controls.
 * 3. addElement(Element): Appends form control element, returns this for method chaining.
 * 4. removeChild(Node):
 *    - Calls super.removeChild(out).
 *    - Evicts the node from the internal elements collection.
 * 5. submit():
 *    - Branch: hasAttr("action") -> absUrl("action") vs baseUri().
 *    - Boundary: Validate.notEmpty(action) -> throws IllegalArgumentException if empty/unset.
 *    - Branch: attr("method").toUpperCase().equals("POST") -> Connection.Method.POST vs GET (defaults to GET).
 *    - Jsoup.connect configuration with url, data (formData()), and method.
 * 6. formData():
 *    - Loop over elements.
 *    - Guard 1: !el.tag().isFormSubmittable() -> continue.
 *    - Guard 2: el.hasAttr("disabled") -> continue (Defect-Targeted: ensures disabled inputs are skipped).
 *    - Guard 3: name.length() == 0 -> continue (inputs without name or empty name skipped).
 *    - Branch A: "select".equals(el.normalName()):
 *        - option[selected] found -> adds each selected option key/val.
 *        - no option selected -> first option added if present; nothing added if select is empty.
 *    - Branch B: "checkbox" or "radio" (case-insensitive):
 *        - el.hasAttr("checked") is true:
 *            - val.length() > 0 ? val : "on" (default value fallback to "on").
 *        - el.hasAttr("checked") is false -> omitted.
 *    - Branch C: other controls (text, password, hidden, textarea, etc.) -> adds key and el.val().
 *
 * Ground Truth Defect Addressed:
 * - org.jsoup.nodes.FormElementTest::createsFormData
 *   Defect: Disabled form elements were erroneously included in formData(), causing expected:<6> but was:<7>.
 */
public class FormElementGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testFormElementConstructorAndState() {
        Attributes attrs = new Attributes();
        attrs.put("action", "http://example.com/api");
        attrs.put("method", "post");
        FormElement form = new FormElement(Tag.valueOf("form"), "http://example.com", attrs);

        assertNotNull(form.elements());
        assertEquals(0, form.elements().size());
        assertEquals("http://example.com/api", form.attr("action"));
        assertEquals("post", form.attr("method"));
    }

    @Test(timeout = 4000)
    public void testAddElementChainingAndRetrieval() {
        FormElement form = new FormElement(Tag.valueOf("form"), "http://example.com", new Attributes());
        Element input1 = new Element(Tag.valueOf("input"), "").attr("name", "user").attr("value", "alice");
        Element input2 = new Element(Tag.valueOf("input"), "").attr("name", "pass").attr("value", "secret");

        FormElement returnedForm = form.addElement(input1).addElement(input2);

        assertSame(form, returnedForm);
        assertEquals(2, form.elements().size());
        assertSame(input1, form.elements().get(0));
        assertSame(input2, form.elements().get(1));
    }

    @Test(timeout = 4000)
    public void testFormDataWithTextInputsAndTextArea() {
        String html = "<form action='/submit'>" +
                "<input type='text' name='username' value='admin'>" +
                "<input type='password' name='password' value='12345'>" +
                "<textarea name='bio'>Hello world</textarea>" +
                "</form>";
        Document doc = Jsoup.parse(html, "http://example.com");
        FormElement form = (FormElement) doc.select("form").first();

        List<Connection.KeyVal> data = form.formData();
        assertEquals(3, data.size());
        assertEquals("username", data.get(0).key());
        assertEquals("admin", data.get(0).value());
        assertEquals("password", data.get(1).key());
        assertEquals("12345", data.get(1).value());
        assertEquals("bio", data.get(2).key());
        assertEquals("Hello world", data.get(2).value());
    }

    @Test(timeout = 4000)
    public void testFormDataSelectMultipleSelectedOptions() {
        String html = "<form>" +
                "<select name='skills' multiple>" +
                "<option value='java' selected>Java</option>" +
                "<option value='cpp'>C++</option>" +
                "<option value='python' selected>Python</option>" +
                "</select>" +
                "</form>";
        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.select("form").first();

        List<Connection.KeyVal> data = form.formData();
        assertEquals(2, data.size());
        assertEquals("skills", data.get(0).key());
        assertEquals("java", data.get(0).value());
        assertEquals("skills", data.get(1).key());
        assertEquals("python", data.get(1).value());
    }

    @Test(timeout = 4000)
    public void testFormDataSelectNoSelectedDefaultsToFirstOption() {
        String html = "<form>" +
                "<select name='status'>" +
                "<option value='active'>Active</option>" +
                "<option value='pending'>Pending</option>" +
                "</select>" +
                "</form>";
        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.select("form").first();

        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("status", data.get(0).key());
        assertEquals("active", data.get(0).value());
    }

    @Test(timeout = 4000)
    public void testFormDataSelectWithNoOptionsGeneratesNothing() {
        String html = "<form><select name='empty'></select></form>";
        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.select("form").first();

        List<Connection.KeyVal> data = form.formData();
        assertTrue(data.isEmpty());
    }

    @Test(timeout = 4000)
    public void testFormDataCheckboxAndRadioCheckedWithAndWithoutExplicitValues() {
        String html = "<form>" +
                "<input type='checkbox' name='agree' checked>" + // val is empty -> defaults to "on"
                "<input type='checkbox' name='subscribe' value='weekly' checked>" +
                "<input type='radio' name='gender' value='male' checked>" +
                "<input type='radio' name='unvaluedRadio' checked>" + // val is empty -> defaults to "on"
                "</form>";
        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.select("form").first();

        List<Connection.KeyVal> data = form.formData();
        assertEquals(4, data.size());
        assertEquals("agree", data.get(0).key());
        assertEquals("on", data.get(0).value());
        assertEquals("subscribe", data.get(1).key());
        assertEquals("weekly", data.get(1).value());
        assertEquals("gender", data.get(2).key());
        assertEquals("male", data.get(2).value());
        assertEquals("unvaluedRadio", data.get(3).key());
        assertEquals("on", data.get(3).value());
    }

    @Test(timeout = 4000)
    public void testFormDataUncheckedCheckboxAndRadioIgnored() {
        String html = "<form>" +
                "<input type='checkbox' name='optIn' value='yes'>" +
                "<input type='radio' name='rate' value='1'>" +
                "<input type='radio' name='rate' value='2'>" +
                "</form>";
        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.select("form").first();

        List<Connection.KeyVal> data = form.formData();
        assertTrue(data.isEmpty());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testFormDataIgnoresControlsWithoutNameOrWithEmptyName() {
        String html = "<form>" +
                "<input value='no-name-attr'>" +
                "<input name='' value='empty-name-attr'>" +
                "<input name='valid' value='ok'>" +
                "</form>";
        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.select("form").first();

        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("valid", data.get(0).key());
        assertEquals("ok", data.get(0).value());
    }

    @Test(timeout = 4000)
    public void testFormDataIgnoresNonSubmittableTags() {
        FormElement form = new FormElement(Tag.valueOf("form"), "http://example.com", new Attributes());
        // Non-submittable element added directly to form elements
        Element div = new Element(Tag.valueOf("div"), "").attr("name", "divName").attr("value", "divVal");
        Element p = new Element(Tag.valueOf("p"), "").attr("name", "pName");
        Element input = new Element(Tag.valueOf("input"), "").attr("name", "validInput").attr("value", "val");

        form.addElement(div);
        form.addElement(p);
        form.addElement(input);

        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("validInput", data.get(0).key());
        assertEquals("val", data.get(0).value());
    }

    @Test(timeout = 4000)
    public void testSubmitMethodHandlingPostAndGet() {
        String htmlPost = "<form action='/process' method='post'><input name='k' value='v'></form>";
        Document docPost = Jsoup.parse(htmlPost, "http://example.com");
        FormElement formPost = (FormElement) docPost.select("form").first();
        Connection connPost = formPost.submit();
        assertEquals(Connection.Method.POST, connPost.request().method());
        assertEquals("http://example.com/process", connPost.request().url().toString());

        String htmlMixedPost = "<form action='/process' method='PoSt'></form>";
        Document docMixedPost = Jsoup.parse(htmlMixedPost, "http://example.com");
        FormElement formMixedPost = (FormElement) docMixedPost.select("form").first();
        assertEquals(Connection.Method.POST, formMixedPost.submit().request().method());

        String htmlGet = "<form action='/process' method='get'></form>";
        Document docGet = Jsoup.parse(htmlGet, "http://example.com");
        FormElement formGet = (FormElement) docGet.select("form").first();
        assertEquals(Connection.Method.GET, formGet.submit().request().method());

        String htmlDefault = "<form action='/process'></form>";
        Document docDefault = Jsoup.parse(htmlDefault, "http://example.com");
        FormElement formDefault = (FormElement) docDefault.select("form").first();
        assertEquals(Connection.Method.GET, formDefault.submit().request().method());
    }

    @Test(timeout = 4000)
    public void testSubmitActionFallbackToBaseUriWhenActionAttrAbsent() {
        String html = "<form method='post'><input name='field' value='value'></form>";
        Document doc = Jsoup.parse(html, "http://example.com/page.html");
        FormElement form = (FormElement) doc.select("form").first();

        Connection conn = form.submit();
        assertEquals("http://example.com/page.html", conn.request().url().toString());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets Defects4J known regression:
     * org.jsoup.nodes.FormElementTest::createsFormData
     * AssertionFailedError: expected:<6> but was:<7>
     * Disabled inputs MUST be ignored by formData().
     */
    @Test(timeout = 4000)
    public void testCreatesFormDataDefect_DisabledInputsMustBeExcluded() {
        String html = "<form>" +
                "<input name='one' value='two'>" +
                "<select name='three'>" +
                "<option value='not' selected>Three</option>" +
                "<option value='four' selected>Four</option>" +
                "</select>" +
                "<input name='four' value='five' disabled>" + // Must be skipped (disabled)
                "<input name='seven' type='radio' value='on' checked>" +
                "<input name='seven' type='radio' value='off'>" + // Must be skipped (not checked)
                "<input name='eight' type='checkbox' checked>" +
                "<input name='nine' type='checkbox' value='unset'>" + // Must be skipped (not checked)
                "<input name='ten' value='text'>" +
                "</form>";

        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.select("form").first();
        List<Connection.KeyVal> data = form.formData();

        // If disabled input is incorrectly included, size will be 7 instead of 6
        assertEquals(6, data.size());
        assertEquals("one=two", data.get(0).toString());
        assertEquals("three=not", data.get(1).toString());
        assertEquals("three=four", data.get(2).toString());
        assertEquals("seven=on", data.get(3).toString());
        assertEquals("eight=on", data.get(4).toString());
        assertEquals("ten=text", data.get(5).toString());
    }

    @Test(timeout = 4000)
    public void testRemoveChildRemovesFromInternalElementsCollection() {
        String html = "<form><input name='foo' value='1'><input name='bar' value='2'></form>";
        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.select("form").first();
        assertEquals(2, form.elements().size());

        Element fooInput = form.select("input[name=foo]").first();
        fooInput.remove(); // triggers parent.removeChild(this) on FormElement

        assertEquals(1, form.elements().size());
        assertEquals("bar", form.elements().get(0).attr("name"));

        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("bar", data.get(0).key());
        assertEquals("2", data.get(0).value());
    }

    @Test(timeout = 4000)
    public void testRemoveChildDirectInvocation() {
        FormElement form = new FormElement(Tag.valueOf("form"), "http://example.com", new Attributes());
        Element input = new Element(Tag.valueOf("input"), "").attr("name", "test").attr("value", "val");
        form.appendChild(input);
        form.addElement(input);

        assertEquals(1, form.elements().size());
        assertEquals(1, form.children().size());

        form.removeChild(input);

        assertEquals(0, form.elements().size());
        assertEquals(0, form.children().size());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSubmitWithoutActionAndWithoutBaseUriThrowsException() {
        String html = "<form><input name='q' value='search'></form>";
        Document doc = Jsoup.parse(html); // baseUri is ""
        FormElement form = (FormElement) doc.select("form").first();

        // Must throw IllegalArgumentException because action URL cannot be determined
        form.submit();
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSubmitWithEmptyActionAttrAndEmptyBaseUriThrowsException() {
        String html = "<form action=''><input name='q' value='search'></form>";
        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.select("form").first();

        form.submit();
    }

    // =========================================================================
    // Partition E: Object Lifecycle & DOM Structure Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testFormDataCopiesListAndDoesNotMutateDom() {
        String html = "<form><input name='user' value='john'></form>";
        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.select("form").first();

        List<Connection.KeyVal> data1 = form.formData();
        assertEquals(1, data1.size());
        data1.clear();

        List<Connection.KeyVal> data2 = form.formData();
        assertEquals(1, data2.size());
        assertEquals("user", data2.get(0).key());
    }

    @Test(timeout = 4000)
    public void testNestedFormControlsInsideContainers() {
        String html = "<form action='/submit' method='post'>" +
                "<div>" +
                "  <p><input name='nestedField' value='nestedValue'></p>" +
                "  <fieldset>" +
                "    <input type='text' name='fieldInFieldset' value='fieldsetVal'>" +
                "  </fieldset>" +
                "</div>" +
                "</form>";
        Document doc = Jsoup.parse(html, "http://example.com");
        FormElement form = (FormElement) doc.select("form").first();

        List<Connection.KeyVal> data = form.formData();
        assertEquals(2, data.size());
        assertEquals("nestedField", data.get(0).key());
        assertEquals("nestedValue", data.get(0).value());
        assertEquals("fieldInFieldset", data.get(1).key());
        assertEquals("fieldsetVal", data.get(1).value());
    }

    @Test(timeout = 4000)
    public void testEmptyFormSubmitsCorrectlyWithBaseUri() {
        String html = "<form></form>";
        Document doc = Jsoup.parse(html, "http://example.com/test");
        FormElement form = (FormElement) doc.select("form").first();

        Connection conn = form.submit();
        assertEquals("http://example.com/test", conn.request().url().toString());
        assertTrue(conn.request().data().isEmpty());
    }
}