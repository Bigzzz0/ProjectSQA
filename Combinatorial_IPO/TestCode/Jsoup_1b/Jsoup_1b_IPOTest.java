package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;
import org.jsoup.Jsoup;

/** Generated from approved defect-focused scenarios using native IPO. */
public class Jsoup_1b_IPOTest {

    @Test(timeout = 4000)
    public void test_body_snippet_order_001() throws Exception {
        // Native IPO combination: leading_text=foo, tag=bold, trailing_text=baz
        String leading = "foo";
        String tag = "b";
        String trailing = "baz";
        String html = leading + " <" + tag + ">bar</" + tag + "> " + trailing;
        Document document = Jsoup.parse(html);
        assertEquals(leading + " bar " + trailing, document.text());
    }

    @Test(timeout = 4000)
    public void test_body_snippet_order_002() throws Exception {
        // Native IPO combination: leading_text=foo, tag=italic, trailing_text=world
        String leading = "foo";
        String tag = "i";
        String trailing = "world";
        String html = leading + " <" + tag + ">bar</" + tag + "> " + trailing;
        Document document = Jsoup.parse(html);
        assertEquals(leading + " bar " + trailing, document.text());
    }

    @Test(timeout = 4000)
    public void test_body_snippet_order_003() throws Exception {
        // Native IPO combination: leading_text=hello, tag=bold, trailing_text=world
        String leading = "hello";
        String tag = "b";
        String trailing = "world";
        String html = leading + " <" + tag + ">bar</" + tag + "> " + trailing;
        Document document = Jsoup.parse(html);
        assertEquals(leading + " bar " + trailing, document.text());
    }

    @Test(timeout = 4000)
    public void test_body_snippet_order_004() throws Exception {
        // Native IPO combination: leading_text=hello, tag=italic, trailing_text=baz
        String leading = "hello";
        String tag = "i";
        String trailing = "baz";
        String html = leading + " <" + tag + ">bar</" + tag + "> " + trailing;
        Document document = Jsoup.parse(html);
        assertEquals(leading + " bar " + trailing, document.text());
    }

}
