package org.jsoup.helper;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.parser.Parser;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.IllegalCharsetNameException;
import java.util.*;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Targets: org.jsoup.helper.HttpConnection, Base, Request, Response, KeyVal
 *
 * 1. Defect Targeting:
 *    - Response.processResponseHeaders: According to RFC 2616 Sec 4.2 (referenced in source comments),
 *      duplicate response headers must be combined using a comma separator. The defective code only
 *      takes values.get(0) (`header(name, values.get(0));`), ignoring subsequent occurrences of the header.
 *      Test: targets sameHeadersCombineWithComma / processResponseHeaders with multiple values.
 *
 * 2. KeyVal:
 *    - Creation via string values, inputStreams.
 *    - Null/empty validations for key, value, inputStream. Note the subtle check `Validate.notNull(value, ...)`.
 *    - toString representation.
 *
 * 3. Base (Headers & Cookies):
 *    - Case-insensitive header matching, replacement, removal, and hasHeaderWithValue.
 *    - Cookie storage, extraction, removal, and validation.
 *
 * 4. Request Configuration:
 *    - Timeout, maxBodySize, followRedirects, ignoreHttpErrors, ignoreContentType,
 *      validateTLSCertificates, parser, postDataCharset (supported vs unsupported Charset).
 *
 * 5. URL & Encoding Logic:
 *    - encodeUrl (space to %20, null check).
 *    - encodeMimeName (quote replacement).
 *    - serialiseRequestUrl (appending query parameters to URL for non-body GET requests).
 *    - setOutputContentType (multipart/form-data with boundary vs form-url-encoded).
 *    - writePost (application/x-www-form-urlencoded and multipart streams).
 *
 * 6. Response State & Lifecycle:
 *    - parse(), body(), bodyAsBytes() prior to execution throwing IllegalArgumentException.
 *    - Parsing response headers (Set-Cookie token parsing, header combination).
 */
public class HttpConnectionGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone
    // =========================================================================

    /**
     * Targets Defects4J bug: duplicate response headers must be joined by ", ".
     * In the buggy version, HttpConnection.Response.processResponseHeaders does:
     *   header(name, values.get(0));
     * instead of combining them:
     *   String.join(", ", values) or manual string concatenation.
     */
    @Test(timeout = 4000)
    public void testSameHeadersCombineWithCommaDefect() {
        HttpConnection.Response response = new HttpConnection.Response();
        Map<String, List<String>> headers = new HashMap<String, List<String>>();

        List<String> cacheControlValues = new ArrayList<String>();
        cacheControlValues.add("no-cache");
        cacheControlValues.add("no-store");
        headers.put("Cache-Control", cacheControlValues);

        response.processResponseHeaders(headers);

        assertEquals("no-cache, no-store", response.header("Cache-Control"));
    }

    @Test(timeout = 4000)
    public void testMultipleDuplicateHeadersCombineWithComma() {
        HttpConnection.Response response = new HttpConnection.Response();
        Map<String, List<String>> headers = new HashMap<String, List<String>>();

        List<String> varyValues = Arrays.asList("Accept-Encoding", "User-Agent", "Accept");
        headers.put("Vary", varyValues);

        response.processResponseHeaders(headers);

        assertEquals("Accept-Encoding, User-Agent, Accept", response.header("Vary"));
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConnectStringUrl() {
        Connection con = HttpConnection.connect("http://example.com/test");
        assertNotNull(con);
        assertEquals("http://example.com/test", con.request().url().toExternalForm());
    }

    @Test(timeout = 4000)
    public void testConnectURL() throws MalformedURLException {
        URL url = new URL("http://example.com/path");
        Connection con = HttpConnection.connect(url);
        assertNotNull(con);
        assertEquals(url, con.request().url());
    }

    @Test(timeout = 4000)
    public void testFluentConfigurationChaining() throws MalformedURLException {
        Connection con = HttpConnection.connect("http://example.com");
        Parser parser = Parser.xmlParser();
        Connection returned = con.url("http://example.com/updated")
                .userAgent("Mozilla/5.0")
                .timeout(5000)
                .maxBodySize(2048)
                .followRedirects(false)
                .referrer("http://google.com")
                .method(Connection.Method.POST)
                .ignoreHttpErrors(true)
                .ignoreContentType(true)
                .validateTLSCertificates(false)
                .data("param1", "value1")
                .header("X-Test", "123")
                .cookie("sessionId", "abc")
                .parser(parser)
                .postDataCharset("UTF-8");

        assertSame(con, returned);
        Connection.Request req = con.request();
        assertEquals("http://example.com/updated", req.url().toExternalForm());
        assertEquals("Mozilla/5.0", req.header("User-Agent"));
        assertEquals(5000, req.timeout());
        assertEquals(2048, req.maxBodySize());
        assertFalse(req.followRedirects());
        assertEquals("http://google.com", req.header("Referer"));
        assertEquals(Connection.Method.POST, req.method());
        assertTrue(req.ignoreHttpErrors());
        assertTrue(req.ignoreContentType());
        assertFalse(req.validateTLSCertificates());
        assertEquals("123", req.header("X-Test"));
        assertEquals("abc", req.cookie("sessionId"));
        assertSame(parser, req.parser());
        assertEquals("UTF-8", req.postDataCharset());
    }

    @Test(timeout = 4000)
    public void testDataAddingVariants() {
        Connection con = HttpConnection.connect("http://example.com");

        // Key-Value varargs
        con.data("k1", "v1", "k2", "v2");
        assertEquals(2, con.request().data().size());

        // Map
        Map<String, String> mapData = new HashMap<String, String>();
        mapData.put("k3", "v3");
        con.data(mapData);
        assertEquals(3, con.request().data().size());

        // KeyVal collection
        List<Connection.KeyVal> listData = new ArrayList<Connection.KeyVal>();
        listData.add(HttpConnection.KeyVal.create("k4", "v4"));
        con.data(listData);
        assertEquals(4, con.request().data().size());

        // Stream data
        InputStream stream = new ByteArrayInputStream("file content".getBytes());
        con.data("upload", "filename.txt", stream);
        assertEquals(5, con.request().data().size());
    }

    @Test(timeout = 4000)
    public void testCookiesMap() {
        Connection con = HttpConnection.connect("http://example.com");
        Map<String, String> cookies = new HashMap<String, String>();
        cookies.put("uid", "1001");
        cookies.put("auth", "token");
        con.cookies(cookies);

        assertEquals("1001", con.request().cookie("uid"));
        assertEquals("token", con.request().cookie("auth"));
    }

    @Test(timeout = 4000)
    public void testHeadersCaseInsensitiveAndRemoval() {
        Connection con = HttpConnection.connect("http://example.com");
        Connection.Request req = con.request();

        req.header("Content-Type", "application/json");
        assertTrue(req.hasHeader("content-type"));
        assertTrue(req.hasHeader("CONTENT-TYPE"));
        assertTrue(req.hasHeaderWithValue("content-type", "application/json"));
        assertTrue(req.hasHeaderWithValue("Content-Type", "APPLICATION/JSON"));
        assertFalse(req.hasHeaderWithValue("content-type", "text/html"));

        // Overwrite preserves single entry
        req.header("content-type", "application/xml");
        assertEquals("application/xml", req.header("Content-Type"));
        assertEquals(1, countHeaderOccurrences(req.headers(), "content-type"));

        // Remove header
        req.removeHeader("Content-Type");
        assertFalse(req.hasHeader("Content-Type"));
        assertNull(req.header("Content-Type"));
    }

    private int countHeaderOccurrences(Map<String, String> headers, String headerName) {
        int count = 0;
        for (String k : headers.keySet()) {
            if (k.equalsIgnoreCase(headerName)) count++;
        }
        return count;
    }

    @Test(timeout = 4000)
    public void testCookieOperations() {
        Connection con = HttpConnection.connect("http://example.com");
        Connection.Request req = con.request();

        req.cookie("session", "abc");
        assertTrue(req.hasCookie("session"));
        assertEquals("abc", req.cookie("session"));

        req.removeCookie("session");
        assertFalse(req.hasCookie("session"));
        assertNull(req.cookie("session"));
    }

    @Test(timeout = 4000)
    public void testKeyValCreationAndProperties() {
        HttpConnection.KeyVal kv = HttpConnection.KeyVal.create("key1", "val1");
        assertEquals("key1", kv.key());
        assertEquals("val1", kv.value());
        assertFalse(kv.hasInputStream());
        assertNull(kv.inputStream());
        assertEquals("key1=val1", kv.toString());

        InputStream is = new ByteArrayInputStream(new byte[]{1, 2, 3});
        HttpConnection.KeyVal fileKv = HttpConnection.KeyVal.create("fileKey", "test.bin", is);
        assertEquals("fileKey", fileKv.key());
        assertEquals("test.bin", fileKv.value());
        assertTrue(fileKv.hasInputStream());
        assertSame(is, fileKv.inputStream());
        assertEquals("fileKey=test.bin", fileKv.toString());
    }

    @Test(timeout = 4000)
    public void testProcessResponseHeadersCookies() {
        HttpConnection.Response response = new HttpConnection.Response();
        Map<String, List<String>> headers = new HashMap<String, List<String>>();

        List<String> cookieHeaders = new ArrayList<String>();
        cookieHeaders.add("id=1234; Path=/; Secure");
        cookieHeaders.add("theme=dark; Path=/; HttpOnly");
        cookieHeaders.add(null); // test null value resilience
        cookieHeaders.add("=invalid; Path=/"); // empty name
        headers.put("Set-Cookie", cookieHeaders);
        headers.put(null, Collections.singletonList("HTTP/1.1 200 OK")); // status line

        response.processResponseHeaders(headers);

        assertEquals("1234", response.cookie("id"));
        assertEquals("dark", response.cookie("theme"));
        assertFalse(response.hasCookie(""));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testUrlWithSpacesEncoding() {
        Connection con = HttpConnection.connect("http://example.com/test path/with space.html");
        assertEquals("http://example.com/test%20path/with%20space.html", con.request().url().toExternalForm());
    }

    @Test(timeout = 4000)
    public void testTimeoutAndBodySizeBoundaries() {
        Connection con = HttpConnection.connect("http://example.com");
        con.timeout(0); // 0 is infinite
        assertEquals(0, con.request().timeout());

        con.maxBodySize(0); // 0 is unlimited
        assertEquals(0, con.request().maxBodySize());
    }

    @Test(timeout = 4000)
    public void testEmptyCollectionDataAndCookieOperations() {
        Connection con = HttpConnection.connect("http://example.com");
        con.data(Collections.<String, String>emptyMap());
        con.data(Collections.<Connection.KeyVal>emptyList());
        con.cookies(Collections.<String, String>emptyMap());

        assertEquals(0, con.request().data().size());
        assertEquals(0, con.request().cookies().size());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConnectNullUrlStringThrows() {
        HttpConnection.connect((String) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConnectEmptyUrlStringThrows() {
        HttpConnection.connect("   ");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConnectMalformedUrlThrows() {
        HttpConnection.connect("htp://invalid-url");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testRequestNullUrlThrows() {
        new HttpConnection.Request().url(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testRequestNullMethodThrows() {
        new HttpConnection.Request().method(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNegativeTimeoutThrows() {
        new HttpConnection.Request().timeout(-1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNegativeMaxBodySizeThrows() {
        new HttpConnection.Request().maxBodySize(-1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNullUserAgentThrows() {
        HttpConnection.connect("http://example.com").userAgent(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNullReferrerThrows() {
        HttpConnection.connect("http://example.com").referrer(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEmptyHeaderNameThrows() {
        HttpConnection.connect("http://example.com").header("", "value");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNullHeaderValueThrows() {
        HttpConnection.connect("http://example.com").header("X-Key", null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNullHeaderLookupThrows() {
        HttpConnection.connect("http://example.com").request().header(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEmptyCookieNameThrows() {
        HttpConnection.connect("http://example.com").cookie("", "value");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNullCookieValueThrows() {
        HttpConnection.connect("http://example.com").cookie("name", null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testOddNumberOfKeyValVarargsThrows() {
        HttpConnection.connect("http://example.com").data("key1", "val1", "orphanKey");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNullKeyValVarargThrows() {
        HttpConnection.connect("http://example.com").data((String[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNullKeyValInVarargsThrows() {
        HttpConnection.connect("http://example.com").data("key1", null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEmptyKeyValInVarargsThrows() {
        HttpConnection.connect("http://example.com").data("", "val1");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNullDataMapThrows() {
        HttpConnection.connect("http://example.com").data((Map<String, String>) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNullDataCollectionThrows() {
        HttpConnection.connect("http://example.com").data((Collection<Connection.KeyVal>) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNullKeyValDataThrows() {
        HttpConnection.connect("http://example.com").request().data((Connection.KeyVal) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNullPostDataCharsetThrows() {
        HttpConnection.connect("http://example.com").postDataCharset(null);
    }

    @Test(expected = IllegalCharsetNameException.class, timeout = 4000)
    public void testUnsupportedPostDataCharsetThrows() {
        HttpConnection.connect("http://example.com").postDataCharset("INVALID-CHARSET-NAME-123");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testUnexecutedResponseParseThrows() throws IOException {
        HttpConnection.Response response = new HttpConnection.Response();
        response.parse();
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testUnexecutedResponseBodyThrows() {
        HttpConnection.Response response = new HttpConnection.Response();
        response.body();
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testUnexecutedResponseBodyAsBytesThrows() {
        HttpConnection.Response response = new HttpConnection.Response();
        response.bodyAsBytes();
    }

    @Test(expected = MalformedURLException.class, timeout = 4000)
    public void testExecuteUnsupportedProtocolThrows() throws IOException {
        Connection.Request req = new HttpConnection.Request();
        req.url(new URL("ftp://example.com/file.txt"));
        HttpConnection.Response.execute(req);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testKeyValEmptyKeyThrows() {
        HttpConnection.KeyVal.create("", "value");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testKeyValNullValueThrows() {
        HttpConnection.KeyVal.create("key", null);
    }

    // =========================================================================
    // Partition E: Internal Logic, URL Serialization & Post Formats
    // =========================================================================

    @Test(timeout = 4000)
    public void testSerialiseRequestUrlWithGetParams() throws Exception {
        Connection.Request req = new HttpConnection.Request();
        req.url(new URL("http://example.com/search?q=jsoup"));
        req.data(HttpConnection.KeyVal.create("page", "1"));
        req.data(HttpConnection.KeyVal.create("sort", "desc"));

        Method serialiseMethod = HttpConnection.Response.class.getDeclaredMethod("serialiseRequestUrl", Connection.Request.class);
        serialiseMethod.setAccessible(true);
        serialiseMethod.invoke(null, req);

        assertEquals("http://example.com/search?q=jsoup&page=1&sort=desc", req.url().toExternalForm());
        assertTrue(req.data().isEmpty());
    }

    @Test(timeout = 4000)
    public void testSerialiseRequestUrlWithoutExistingQuery() throws Exception {
        Connection.Request req = new HttpConnection.Request();
        req.url(new URL("http://example.com/search"));
        req.data(HttpConnection.KeyVal.create("q", "test value"));

        Method serialiseMethod = HttpConnection.Response.class.getDeclaredMethod("serialiseRequestUrl", Connection.Request.class);
        serialiseMethod.setAccessible(true);
        serialiseMethod.invoke(null, req);

        assertEquals("http://example.com/search?q=test+value", req.url().toExternalForm());
        assertTrue(req.data().isEmpty());
    }

    @Test(timeout = 4000)
    public void testSetOutputContentTypeStandard() throws Exception {
        Connection.Request req = new HttpConnection.Request();
        req.data(HttpConnection.KeyVal.create("name", "val"));

        Method setOutputContentTypeMethod = HttpConnection.Response.class.getDeclaredMethod("setOutputContentType", Connection.Request.class);
        setOutputContentTypeMethod.setAccessible(true);
        String boundary = (String) setOutputContentTypeMethod.invoke(null, req);

        assertNull(boundary);
        assertEquals("application/x-www-form-urlencoded; charset=UTF-8", req.header("Content-Type"));
    }

    @Test(timeout = 4000)
    public void testSetOutputContentTypeMultipart() throws Exception {
        Connection.Request req = new HttpConnection.Request();
        InputStream stream = new ByteArrayInputStream("data".getBytes());
        req.data(HttpConnection.KeyVal.create("upload", "file.txt", stream));

        Method setOutputContentTypeMethod = HttpConnection.Response.class.getDeclaredMethod("setOutputContentType", Connection.Request.class);
        setOutputContentTypeMethod.setAccessible(true);
        String boundary = (String) setOutputContentTypeMethod.invoke(null, req);

        assertNotNull(boundary);
        assertTrue(req.header("Content-Type").startsWith("multipart/form-data; boundary="));
    }

    @Test(timeout = 4000)
    public void testEncodeUrlAndEncodeMimeName() throws Exception {
        Method encodeUrl = HttpConnection.class.getDeclaredMethod("encodeUrl", String.class);
        encodeUrl.setAccessible(true);
        assertNull(encodeUrl.invoke(null, (Object) null));
        assertEquals("foo%20bar%20baz", encodeUrl.invoke(null, "foo bar baz"));

        Method encodeMimeName = HttpConnection.class.getDeclaredMethod("encodeMimeName", String.class);
        encodeMimeName.setAccessible(true);
        assertNull(encodeMimeName.invoke(null, (Object) null));
        assertEquals("test%22quote%22", encodeMimeName.invoke(null, "test\"quote\""));
    }

    @Test(timeout = 4000)
    public void testWritePostUrlEncoded() throws Exception {
        Connection.Request req = new HttpConnection.Request();
        req.data(HttpConnection.KeyVal.create("key 1", "val 1"));
        req.data(HttpConnection.KeyVal.create("key&2", "val=2"));

        Method writePost = HttpConnection.Response.class.getDeclaredMethod("writePost", Connection.Request.class, java.io.OutputStream.class, String.class);
        writePost.setAccessible(true);

        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        writePost.invoke(null, req, out, null);

        String postBody = out.toString("UTF-8");
        assertEquals("key+1=val+1&key%262=val%3D2", postBody);
    }

    @Test(timeout = 4000)
    public void testWritePostMultipart() throws Exception {
        Connection.Request req = new HttpConnection.Request();
        req.data(HttpConnection.KeyVal.create("regularField", "fieldValue"));
        InputStream is = new ByteArrayInputStream("sample file content".getBytes("UTF-8"));
        req.data(HttpConnection.KeyVal.create("fileField", "hello.txt", is));

        Method writePost = HttpConnection.Response.class.getDeclaredMethod("writePost", Connection.Request.class, java.io.OutputStream.class, String.class);
        writePost.setAccessible(true);

        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        String boundary = "testBoundary123";
        writePost.invoke(null, req, out, boundary);

        String postBody = out.toString("UTF-8");
        assertTrue(postBody.contains("--testBoundary123"));
        assertTrue(postBody.contains("Content-Disposition: form-data; name=\"regularField\""));
        assertTrue(postBody.contains("fieldValue"));
        assertTrue(postBody.contains("Content-Disposition: form-data; name=\"fileField\"; filename=\"hello.txt\""));
        assertTrue(postBody.contains("Content-Type: application/octet-stream"));
        assertTrue(postBody.contains("sample file content"));
        assertTrue(postBody.endsWith("--testBoundary123--"));
    }

    @Test(timeout = 4000)
    public void testGetRequestCookieString() throws Exception {
        Connection.Request req = new HttpConnection.Request();
        req.cookie("c1", "v1");
        req.cookie("c2", "v2");

        Method getCookieStr = HttpConnection.Response.class.getDeclaredMethod("getRequestCookieString", Connection.Request.class);
        getCookieStr.setAccessible(true);
        String cookieStr = (String) getCookieStr.invoke(null, req);

        assertEquals("c1=v1; c2=v2", cookieStr);
    }

    @Test(timeout = 4000)
    public void testRequestAndResponseExchangeViaConnection() {
        Connection con = HttpConnection.connect("http://example.com");
        Connection.Request req = new HttpConnection.Request();
        Connection.Response res = new HttpConnection.Response();

        con.request(req);
        con.response(res);

        assertSame(req, con.request());
        assertSame(res, con.response());
    }
}