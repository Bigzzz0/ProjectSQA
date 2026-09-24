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
 * Decision / Condition Coverage Targets:
 * 1. elements(): Verification of internal Elements collection access.
 * 2. addElement(Element): Verification of element chaining and list inclusion.
 * 3. submit():
 *    - Branch: hasAttr("action") == true -> absUrl("action")
 *    - Branch: hasAttr("action") == false -> baseUri()
 *    - Branch: Validate.notEmpty(action) -> throws IllegalArgumentException on empty action/baseUri
 *    - Branch: attr("method").toUpperCase().equals("POST") -> Connection.Method.POST
 *    - Branch: attr("method").toUpperCase() != "POST" (e.g. GET, empty) -> Connection.Method.GET
 * 4. formData():
 *    - Condition: !el.tag().isFormSubmittable() -> continue
 *    - Condition: name.length() == 0 -> continue
 *    - Branch: el.hasAttr("disabled") -> defect zone (disabled fields must NOT be submitted)
 *    - Branch: "select".equals(el.tagName())
 *      - Sub-branch: has option[selected] -> adds all selected options
 *      - Sub-branch: no option[selected] -> selects first option if available
 *      - Sub-branch: select with no options -> ignored
 *    - Branch: "checkbox".equalsIgnoreCase(type) || "radio".equalsIgnoreCase(type)
 *      - Sub-branch: el.hasAttr("checked") == true
 *        - Value fallback: el.val() length == 0 / missing value attr -> defaults to "on" [Defects4J defect]
 *      - Sub-branch: el.hasAttr("checked") == false -> skipped
 *    - Branch: default (text, password, hidden, textarea, etc.) -> adds key-value pair
 * 5. equals(Object): Equality verification delegating to super.equals.
 */
public class FormElementGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testElementsAndAddElementChaining() {
        Tag formTag = Tag.valueOf("form");
        FormElement form = new FormElement(formTag, "http://example.com/", new Attributes());

        assertEquals(0, form.elements().size());

        Element input1 = new Element(Tag.valueOf("input"), "http://example.com/");
        input1.attr("name", "username");
        input1.attr("value", "alice");

        FormElement chained = form.addElement(input1);
        assertSame(form, chained);
        assertEquals(1, form.elements().size());
        assertSame(input1, form.elements().get(0));

        Element input2 = new Element(Tag.valueOf("input"), "http://example.com/");
        form.addElement(input2);
        assertEquals(2, form.elements().size());
        assertSame(input2, form.elements().get(1));
    }

    @Test(timeout = 4000)
    public void testSubmitWithPostMethodAndAction() {
        String html = "<form action='/submit.php' method='POST'><input name='field' value='val'/></form>";
        Document doc = Jsoup.parse(html, "http://example.com/dir/");
        FormElement form = (FormElement) doc.select("form").first();
        assertNotNull(form);

        Connection con = form.submit();
        assertEquals("http://example.com/submit.php", con.request().url().toExternalForm());
        assertEquals(Connection.Method.POST, con.request().method());
        List<Connection.KeyVal> data = con.request().data();
        assertEquals(1, data.size());
        assertEquals("field", data.get(0).key());
        assertEquals("val", data.get(0).value());
    }

    @Test(timeout = 4000)
    public void testSubmitWithGetMethodDefaultAndCaseInsensitive() {
        String html = "<form action='search' method='get'><input name='q' value='jsoup'/></form>";
        Document doc = Jsoup.parse(html, "http://example.com/sub/");
        FormElement form = (FormElement) doc.select("form").first();
        assertNotNull(form);

        Connection con = form.submit();
        assertEquals("http://example.com/sub/search", con.request().url().toExternalForm());
        assertEquals(Connection.Method.GET, con.request().method());
    }

    @Test(timeout = 4000)
    public void testSubmitFallbackToBaseUriWhenNoAction() {
        String html = "<form method='post'><input name='test' value='1'/></form>";
        Document doc = Jsoup.parse(html, "http://example.com/index.html");
        FormElement form = (FormElement) doc.select("form").first();
        assertNotNull(form);

        Connection con = form.submit();
        assertEquals("http://example.com/index.html", con.request().url().toExternalForm());
        assertEquals(Connection.Method.POST, con.request().method());
    }

    @Test(timeout = 4000)
    public void testFormDataWithSelectMultipleOptionsSelected() {
        String html = "<form>" +
                "<select name='colors' multiple>" +
                "<option value='red' selected>Red</option>" +
                "<option value='green'>Green</option>" +
                "<option value='blue' selected>Blue</option>" +
                "</select>" +
                "</form>";
        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.select("form").first();
        assertNotNull(form);

        List<Connection.KeyVal> data = form.formData();
        assertEquals(2, data.size());
        assertEquals("colors", data.get(0).key());
        assertEquals("red", data.get(0).value());
        assertEquals("colors", data.get(1).key());
        assertEquals("blue", data.get(1).value());
    }

    @Test(timeout = 4000)
    public void testFormDataWithSelectNoExplicitSelectedFallsBackToFirstOption() {
        String html = "<form>" +
                "<select name='flavor'>" +
                "<option value='vanilla'>Vanilla</option>" +
                "<option value='chocolate'>Chocolate</option>" +
                "</select>" +
                "</form>";
        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.select("form").first();
        assertNotNull(form);

        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("flavor", data.get(0).key());
        assertEquals("vanilla", data.get(0).value());
    }

    @Test(timeout = 4000)
    public void testFormDataWithSelectEmptyOptions() {
        String html = "<form><select name='empty'></select></form>";
        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.select("form").first();
        assertNotNull(form);

        List<Connection.KeyVal> data = form.formData();
        assertTrue(data.isEmpty());
    }

    @Test(timeout = 4000)
    public void testFormDataWithTextareaAndStandardInputs() {
        String html = "<form>" +
                "<input type='text' name='user' value='john'>" +
                "<input type='password' name='pass' value='secret'>" +
                "<input type='hidden' name='token' value='xyz123'>" +
                "<textarea name='bio'>Hello World</textarea>" +
                "</form>";
        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.select("form").first();
        assertNotNull(form);

        List<Connection.KeyVal> data = form.formData();
        assertEquals(4, data.size());
        assertEquals("user", data.get(0).key());
        assertEquals("john", data.get(0).value());
        assertEquals("pass", data.get(1).key());
        assertEquals("secret", data.get(1).value());
        assertEquals("token", data.get(2).key());
        assertEquals("xyz123", data.get(2).value());
        assertEquals("bio", data.get(3).key());
        assertEquals("Hello World", data.get(3).value());
    }

    @Test(timeout = 4000)
    public void testFormDataSkipsUncheckedCheckboxesAndRadios() {
        String html = "<form>" +
                "<input type='checkbox' name='agree' value='yes'>" +
                "<input type='radio' name='gender' value='male'>" +
                "<input type='radio' name='gender' value='female' checked='checked'>" +
                "</form>";
        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.select("form").first();
        assertNotNull(form);

        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("gender", data.get(0).key());
        assertEquals("female", data.get(0).value());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testFormDataOnEmptyFormProducesEmptyList() {
        Tag formTag = Tag.valueOf("form");
        FormElement form = new FormElement(formTag, "http://example.com/", new Attributes());
        List<Connection.KeyVal> data = form.formData();
        assertNotNull(data);
        assertEquals(0, data.size());
    }

    @Test(timeout = 4000)
    public void testFormDataIgnoresControlsWithoutNameOrWithEmptyName() {
        String html = "<form>" +
                "<input value='noname'>" +
                "<input name='' value='emptyname'>" +
                "<select><option value='1' selected>1</option></select>" +
                "<select name=''><option value='2' selected>2</option></select>" +
                "<input type='text' name='valid' value='ok'>" +
                "</form>";
        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.select("form").first();
        assertNotNull(form);

        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("valid", data.get(0).key());
        assertEquals("ok", data.get(0).value());
    }

    @Test(timeout = 4000)
    public void testFormDataIgnoresNonSubmittableTags() {
        Tag formTag = Tag.valueOf("form");
        FormElement form = new FormElement(formTag, "http://example.com/", new Attributes());

        Element div = new Element(Tag.valueOf("div"), "http://example.com/");
        div.attr("name", "divName");
        div.attr("value", "divVal");

        Element span = new Element(Tag.valueOf("span"), "http://example.com/");
        span.attr("name", "spanName");

        form.addElement(div);
        form.addElement(span);

        List<Connection.KeyVal> data = form.formData();
        assertEquals(0, data.size());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Target Defect 1:
     * Checkbox and radio buttons without a value attribute must default to value="on"
     * when checked, per HTML specifications.
     * In defective versions, this returned "" instead of "on".
     */
    @Test(timeout = 4000)
    public void testDefectUsesOnForCheckboxValueIfNoValueSet() {
        String html = "<form><input type='checkbox' checked name='foo'></form>";
        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.select("form").first();
        assertNotNull(form);

        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("foo", data.get(0).key());
        assertEquals("on", data.get(0).value());
    }

    @Test(timeout = 4000)
    public void testDefectUsesOnForRadioValueIfNoValueSet() {
        String html = "<form><input type='radio' checked name='bar'></form>";
        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.select("form").first();
        assertNotNull(form);

        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("bar", data.get(0).key());
        assertEquals("on", data.get(0).value());
    }

    /**
     * Target Defect 2:
     * Disabled elements must NOT be included in form submission data.
     * In defective versions, disabled elements were improperly accumulated.
     */
    @Test(timeout = 4000)
    public void testDefectDisabledInputsExcludedFromFormData() {
        String html = "<form>" +
                "<input name='one' value='two'>" +
                "<input name='three' value='four' disabled>" +
                "<input name='five' value='six' />" +
                "<input name='seven' />" +
                "<input name='eight' value='nine' type='submit' />" +
                "</form>";
        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.select("form").first();
        assertNotNull(form);

        List<Connection.KeyVal> data = form.formData();
        for (Connection.KeyVal kv : data) {
            assertNotEquals("Disabled form element must not be submitted", "three", kv.key());
        }
        assertEquals(4, data.size());
        assertEquals("one", data.get(0).key());
        assertEquals("two", data.get(0).value());
        assertEquals("five", data.get(1).key());
        assertEquals("six", data.get(1).value());
        assertEquals("seven", data.get(2).key());
        assertEquals("", data.get(2).value());
        assertEquals("eight", data.get(3).key());
        assertEquals("nine", data.get(3).value());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSubmitThrowsWhenNoActionAndEmptyBaseUri() {
        Tag formTag = Tag.valueOf("form");
        FormElement form = new FormElement(formTag, "", new Attributes());
        form.submit();
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSubmitThrowsWhenActionEmptyAndNoBaseUri() {
        Attributes attrs = new Attributes();
        attrs.put("action", "");
        Tag formTag = Tag.valueOf("form");
        FormElement form = new FormElement(formTag, "", attrs);
        form.submit();
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        Tag formTag = Tag.valueOf("form");
        FormElement form1 = new FormElement(formTag, "http://example.com/", new Attributes());
        FormElement form2 = new FormElement(formTag, "http://example.com/", new Attributes());

        assertEquals(form1, form1);
        assertNotEquals(form1, form2);
        assertNotEquals(form1, null);
        assertNotEquals(form1, "non-element-object");
    }

    @Test(timeout = 4000)
    public void testElementsReturnsLiveCollection() {
        Tag formTag = Tag.valueOf("form");
        FormElement form = new FormElement(formTag, "http://example.com/", new Attributes());
        Elements elements = form.elements();
        assertNotNull(elements);

        Element input = new Element(Tag.valueOf("input"), "http://example.com/");
        form.addElement(input);
        assertEquals(1, elements.size());
        assertSame(input, elements.get(0));
    }
}