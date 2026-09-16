package com.google.gson.stream;

import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * Target Class: com.google.gson.stream.JsonWriter
 *
 * 1. Defect-Targeted Branch:
 *    - testBoxedBooleans: Targets missing overload/handling for boxed Boolean instances (causing NPE
 *      during unboxing of null values in value(Boolean) / value(boolean)).
 *
 * 2. Decision Branches & Conditions Covered:
 *    - Constructor: out == null guard (NPE).
 *    - setIndent: indent.length() == 0 vs indent.length() > 0 (compact vs pretty formatting, separator).
 *    - setLenient / isLenient: Lenient top-level multiple values, lenient NaNs/Infinities via Number.
 *    - setHtmlSafe / isHtmlSafe: HTML-safe replacement chars (<, >, &, =, ') vs default REPLACEMENT_CHARS.
 *    - setSerializeNulls / getSerializeNulls: serializeNulls true vs false when deferredName is present.
 *    - beginArray / endArray: EMPTY_ARRAY to NONEMPTY_ARRAY transitions, comma separators, newlines.
 *    - beginObject / endObject: EMPTY_OBJECT to NONEMPTY_OBJECT transitions, colon separators, newlines.
 *    - close (scope): mismatched context (ISE), dangling deferredName (ISE), stack decrement, nonempty newline.
 *    - push: stack resizing branch when stackSize == stack.length (stack capacity doubling from 32 to 64).
 *    - peek: stackSize == 0 guard (ISE "JsonWriter is closed.").
 *    - name: name == null guard (NPE), deferredName != null duplicate guard (ISE), closed writer guard (ISE).
 *    - value(String): null routing to nullValue(), character escaping loops (\u0000-\u001f, quotes, slashes,
 *      control chars \b, \t, \n, \f, \r, and Unicode line separators \u2028, \u2029).
 *    - jsonValue: null routing to nullValue(), raw appending without quotes.
 *    - nullValue: serializeNulls handling with and without deferredName.
 *    - value(boolean): true vs false literals.
 *    - value(double): NaN and Infinite boundary checks throwing IAE.
 *    - value(long): arbitrary and extreme long values.
 *    - value(Number): null routing, lenient vs non-lenient NaN/Infinities strings.
 *    - flush: stackSize == 0 guard, flushing underlying Writer.
 *    - close (lifecycle): incomplete document IOExceptions (stackSize > 1, stackSize == 1 with EMPTY_DOCUMENT),
 *      successful close, repeated close idempotency.
 *    - beforeName: EMPTY_OBJECT vs NONEMPTY_OBJECT (comma insertion), non-object error branch (ISE).
 *    - beforeValue: NONEMPTY_DOCUMENT (lenient vs strict), EMPTY_DOCUMENT, EMPTY_ARRAY, NONEMPTY_ARRAY,
 *      DANGLING_NAME, and illegal default context (ISE).
 */
public class JsonWriterGeminiTest {

    // =========================================================================
    // PARTITION A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyArray() throws IOException {
        StringWriter out = new StringWriter();
        JsonWriter writer = new JsonWriter(out);
        writer.beginArray();
        writer.endArray();
        writer.close();
        assertEquals("[]", out.toString());
    }

    @Test(timeout = 4000)
    public void testEmptyObject() throws IOException {
        StringWriter out = new StringWriter();
        JsonWriter writer = new JsonWriter(out);
        writer.beginObject();
        writer.endObject();
        writer.close();
        assertEquals("{}", out.toString());
    }

    @Test(timeout = 4000)
    public void testArrayWithValues() throws IOException {
        StringWriter out = new StringWriter();
        JsonWriter writer = new JsonWriter(out);
        writer.beginArray();
        writer.value("first");
        writer.value(true);
        writer.value(false);
        writer.value(123L);
        writer.value(45.5);
        writer.nullValue();
        writer.endArray();
        writer.close();
        assertEquals("[\"first\",true,false,123,45.5,null]", out.toString());
    }

    @Test(timeout = 4000)
    public void testObjectWithValues() throws IOException {
        StringWriter out = new StringWriter();
        JsonWriter writer = new JsonWriter(out);
        writer.beginObject();
        writer.name("name").value("Google");
        writer.name("count").value(100L);
        writer.name("active").value(true);
        writer.name("extra").nullValue();
        writer.endObject();
        writer.close();
        assertEquals("{\"name\":\"Google\",\"count\":100,\"active\":true,\"extra\":null}", out.toString());
    }

    @Test(timeout = 4000)
    public void testNestedStructures() throws IOException {
        StringWriter out = new StringWriter();
        JsonWriter writer = new JsonWriter(out);
        writer.beginObject();
        writer.name("users").beginArray();
        writer.beginObject().name("id").value(1L).endObject();
        writer.beginObject().name("id").value(2L).endObject();
        writer.endArray();
        writer.end