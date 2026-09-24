package org.jsoup.nodes;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------------
 * Target Class: org.jsoup.nodes.FormElement
 *
 * Branch / Condition Coverage:
 * 1. elements() & addElement(Element):
 *    - elements collection storage, retrieval, and method chaining.
 * 2. submit():
 *    - hasAttr("action") == true  -> resolves absUrl("action").
 *    - hasAttr("action") == false -> falls back to baseUri().
 *    - action is empty            -> throws IllegalArgumentException via Validate.notEmpty.
 *    - attr("method") == "POST" (case-insensitive) -> Method.POST.
 *    - attr("method") != "POST" (e.g., "GET", empty, lowercase "get") -> Method.GET.
 * 3. formData():
 *    - !el.tag().isFormSubmittable() -> skip non-submittable elements (div, span, etc.).
 *    - el.hasAttr("disabled")        -> skip disabled elements.
 *    - el.attr("name").length() == 0 -> skip unnamed elements.
 *    - "select".equals(el.tagName()):
 *        - option[selected] present  -> add each selected option.
 *        - multiple option[selected] -> adds all selected values under the same name.
 *        - no option[selected]       -> fallback to first option element if present.
 *        - select with zero options  -> no data added.
 *    - "checkbox" / "radio":
 *        - el.hasAttr("checked") == true:
 *            - el.val().length() > 0 -> uses el.val().
 *            - el.val().length() == 0 -> defaults to "on".
 *        - el.hasAttr("checked") == false -> skipped entirely.
 *    - other inputs (text, hidden, password, textarea):
 *        - adds (name, value).
 *
 * Defect-Targeted Branch Zone (Defects4J):
 * - Removing a form control element from the DOM (e.g. element.remove()) must synchronize with
 *   FormElement's internal elements list. If not removed, formData() and elements() return stale
 *   references, resulting in incorrect submission payload count.
 * ---------------------------------------------------------------------------------------------------------
 */
public class FormElementGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testElementsAndAddElementChaining() {
        FormElement form = new FormElement(Tag.valueOf("form"), "http://example.com", new Attributes());
        assertEquals(0, form.elements().size());

        Element input = new Element(Tag.valueOf("input"), "http://example.com");
        FormElement returned = form.addElement(input);

        assertSame("addElement must support fluent chaining", form, returned);
        assertEquals(1, form.elements().size());
        assertTrue(form.elements().contains(input));
    }

    @Test(timeout = 4000)
    public void testSubmitWithPostMethodAndAction() {
        String html = "<form action='http://example.com/submit' method='POST'>"
                + "<input type='text' name='username' value='gemini'/>"
                + "</form>";
        Document doc = Jsoup.parse(html, "http://example.com");
        FormElement form = (FormElement) doc.select("form").first();

        Connection conn = form.submit();
        assertEquals(Connection.Method.POST, conn.request().method());
        assertEquals("http://example.com/submit", conn.request().url().toExternalForm());

        List<Connection.KeyVal> data = (List<Connection.KeyVal>) conn.request().data();
        assertEquals(1, data.size());
        assertEquals("username", data.get(0).key());
        assertEquals("gemini", data.get(0).value());
    }

    @Test(timeout = 4000)
    public void testSubmitWithGetMethodDefault() {
        String html = "<form action='http://example.com/search' method='get'>"
                + "<input type='text' name='q' value='test'/>"
                + "</form>";
        Document doc = Jsoup.parse(html, "http://example.com");
        FormElement form = (FormElement) doc.select("form").first();

        Connection conn = form.submit();
        assertEquals(Connection.Method.GET, conn.request().method());
        assertEquals("http://example.com/search", conn.request().url().toExternalForm());
    }

    @Test(timeout = 4000)
    public void testSubmitFallbackToBaseUriWhenActionMissing() {
        String html = "<form method='post'><input type='text' name='v' value='1'/></form>";
        Document doc = Jsoup.parse(html, "http://example.com/fallback");
        FormElement form = (FormElement) doc.select("form").first();

        Connection conn = form.submit();
        assertEquals("http://example.com/fallback", conn.request().url().toExternalForm());
        assertEquals(Connection.Method.POST, conn.request().method());
    }

    @Test(timeout = 4000)
    public void testFormDataWithTextareaAndVariousInputs() {
        String html = "<form action='/post'>"
                + "<input type='text' name='user' value='john'>"
                + "<input type='password' name='pass' value='secret'>"
                + "<input type='hidden' name='csrf' value='token123'>"
                + "<textarea name='bio'>Hello World</textarea>"
                + "</form>";
        Document doc = Jsoup.parse(html, "http://example.com");
        FormElement form = (FormElement) doc.select("form").first();

        List<Connection.KeyVal> data = form.formData();
        assertEquals(4, data.size());
        assertEquals("user", data.get(0).key());
        assertEquals("john", data.get(0).value());
        assertEquals("pass", data.get(1).key());
        assertEquals("secret", data.get(1).value());
        assertEquals("csrf", data.get(2).key());
        assertEquals("token123", data.get(2).value());
        assertEquals("bio", data.get(3).key());
        assertEquals("Hello World", data.get(3).value());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testFormDataFilteringNonSubmittableDisabledAndUnnamed() {
        String html = "<form action='/'>"
                + "<div>Just text</div>"
                + "<p>Paragraph</p>"
                + "<input type='text' value='noName'/>"
                + "<input type='text' name='' value='emptyName'/>"
                + "<input type='text' name='disabledInput' value='skipMe' disabled/>"
                + "<input type='text' name='validInput' value='keepMe'/>"
                + "</form>";
        Document doc = Jsoup.parse(html, "http://example.com");
        FormElement form = (FormElement) doc.select("form").first();

        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("validInput", data.get(0).key());
        assertEquals("keepMe", data.get(0).value());
    }

    @Test(timeout = 4000)
    public void testFormDataSelectMultipleAndExplicitSelected() {
        String html = "<form action='/'>"
                + "<select name='cities' multiple>"
                + "  <option value='nyc' selected>New York</option>"
                + "  <option value='lon'>London</option>"
                + "  <option value='par' selected>Paris</option>"
                + "</select>"
                + "</form>";
        Document doc = Jsoup.parse(html, "http://example.com");
        FormElement form = (FormElement) doc.select("form").first();

        List<Connection.KeyVal> data = form.formData();
        assertEquals(2, data.size());
        assertEquals("cities", data.get(0).key());
        assertEquals("nyc", data.get(0).value());
        assertEquals("cities", data.get(1).key());
        assertEquals("par", data.get(1).value());
    }

    @Test(timeout = 4000)
    public void testFormDataSelectNoSelectedDefaultsToFirstOption() {
        String html = "<form action='/'>"
                + "<select name='country'>"
                + "  <option value='ca'>Canada</option>"
                + "  <option value='us'>USA</option>"
                + "</select>"
                + "</form>";
        Document doc = Jsoup.parse(html, "http://example.com");
        FormElement form = (FormElement) doc.select("form").first();

        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("country", data.get(0).key());
        assertEquals("ca", data.get(0).value());
    }

    @Test(timeout = 4000)
    public void testFormDataSelectEmptyNoOptions() {
        String html = "<form action='/'>"
                + "<select name='emptySelect'></select>"
                + "</form>";
        Document doc = Jsoup.parse(html, "http://example.com");
        FormElement form = (FormElement) doc.select("form").first();

        List<Connection.KeyVal> data = form.formData();
        assertTrue(data.isEmpty());
    }

    @Test(timeout = 4000)
    public void testFormDataCheckboxAndRadioVariations() {
        String html = "<form action='/'>"
                + "<input type='checkbox' name='c_checked_with_val' value='foo' checked/>"
                + "<input type='checkbox' name='c_checked_no_val' checked/>"
                + "<input type='checkbox' name='c_unchecked' value='bar'/>"
                + "<input type='radio' name='r_checked_with_val' value='rad1' checked/>"
                + "<input type='radio' name='r_checked_no_val' checked/>"
                + "<input type='radio' name='r_unchecked' value='rad2'/>"
                + "</form>";
        Document doc = Jsoup.parse(html, "http://example.com");
        FormElement form = (FormElement) doc.select("form").first();

        List<Connection.KeyVal> data = form.formData();
        assertEquals(4, data.size());

        assertEquals("c_checked_with_val", data.get(0).key());
        assertEquals("foo", data.get(0).value());

        // When no value specified, checked box/radio defaults to "on"
        assertEquals("c_checked_no_val", data.get(1).key());
        assertEquals("on", data.get(1).value());

        assertEquals("r_checked_with_val", data.get(2).key());
        assertEquals("rad1", data.get(2).value());

        assertEquals("r_checked_no_val", data.get(3).key());
        assertEquals("on", data.get(3).value());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    @Test(timeout = 4000)
    public void testRemoveFormElementReflectedInFormDataAndElements() {
        // Targets defect: when a form control child element is removed,
        // it must be evicted from FormElement's internal elements list.
        String html = "<html>"
                + "  <body>"
                + "      <form action='/action'>"
                + "          <input type='hidden' name='one' value='foo'>"
                + "          <input type='text' name='two' value='bar'>"
                + "          <input type='text' name='three' value='baz'>"
                + "      </form>"
                + "  </body>"
                + "</html>";
        Document doc = Jsoup.parse(html, "http://example.com");
        FormElement form = (FormElement) doc.select("form").first();

        Element two = form.select("input[name=two]").first();
        assertNotNull("Input element 'two' must exist prior to removal", two);

        two.remove();

        List<Connection.KeyVal> data = form.formData();
        assertEquals("Form data must not contain removed element", 2, data.size());
        assertEquals("one", data.get(0).key());
        assertEquals("three", data.get(1).key());

        Elements formElements = form.elements();
        assertEquals("Form elements collection must reflect removal", 2, formElements.size());
        assertFalse("Form elements must not retain removed element", formElements.contains(two));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSubmitWithoutActionAndWithoutBaseUriThrowsException() {
        // No action attribute and no base URI provided -> action string is empty
        String html = "<form><input type='text' name='q' value='test'/></form>";
        Document doc = Jsoup.parse(html); // baseUri is ""
        FormElement form = (FormElement) doc.select("form").first();

        form.submit();
    }

    @Test(timeout = 4000)
    public void testSubmitRelativeActionWithoutBaseUriThrowsException() {
        String html = "<form action='relative/path'><input type='text' name='q' value='test'/></form>";
        Document doc = Jsoup.parse(html); // baseUri is "" -> absUrl("action") is ""
        FormElement form = (FormElement) doc.select("form").first();

        try {
            form.submit();
            fail("Expected IllegalArgumentException when submitting with relative action and no base URI");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Could not determine a form action URL"));
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & DOM Interaction Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testFormDataIsDisconnectedCopy() {
        String html = "<form action='/test'><input type='text' name='field' value='initial'/></form>";
        Document doc = Jsoup.parse(html, "http://example.com");
        FormElement form = (FormElement) doc.select("form").first();

        List<Connection.KeyVal> data1 = form.formData();
        assertEquals(1, data1.size());

        // Modifying the returned list should not affect the form or subsequent calls
        data1.clear();
        assertEquals(0, data1.size());

        List<Connection.KeyVal> data2 = form.formData();
        assertEquals(1, data2.size());
        assertEquals("field", data2.get(0).key());
        assertEquals("initial", data2.get(0).value());
    }

    @Test(timeout = 4000)
    public void testDirectConstructorInitialization() {
        Tag tag = Tag.valueOf("form");
        Attributes attrs = new Attributes();
        attrs.put("action", "http://example.org/api");
        attrs.put("method", "POST");

        FormElement form = new FormElement(tag, "http://example.org", attrs);
        assertEquals("form", form.tagName());
        assertEquals("http://example.org", form.baseUri());
        assertEquals("http://example.org/api", form.attr("action"));
        assertEquals("POST", form.attr("method"));
        assertNotNull(form.elements());
        assertTrue(form.elements().isEmpty());
    }
}