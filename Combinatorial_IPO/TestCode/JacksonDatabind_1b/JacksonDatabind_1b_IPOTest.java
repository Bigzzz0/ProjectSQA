package com.fasterxml.jackson.databind.ser;

import org.junit.Test;
import static org.junit.Assert.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;

/** Generated from approved defect-focused scenarios using native IPO. */
public class JacksonDatabind_1b_IPOTest {

@JsonPropertyOrder(alphabetic=true)
    @JsonFormat(shape=JsonFormat.Shape.ARRAY)
    static class PairBean {
        public String bar;
        public String foo;
        PairBean(String bar, String foo) { this.bar = bar; this.foo = foo; }
    }

    @Test(timeout = 4000)
    public void test_null_array_column_001() throws Exception {
        // Native IPO combination: null_column=first, value=bar, mapper_reuse=fresh
        ObjectMapper mapper = new ObjectMapper();
        boolean firstNull = true;
        String value = "bar";
        PairBean bean = firstNull ? new PairBean(null, value) : new PairBean(value, null);
        ObjectMapper selected = new ObjectMapper();
        String json = selected.writeValueAsString(bean);
        assertEquals(firstNull ? "[null,\"" + value + "\"]" : "[\"" + value + "\",null]", json);
    }

    @Test(timeout = 4000)
    public void test_null_array_column_002() throws Exception {
        // Native IPO combination: null_column=first, value=text, mapper_reuse=shared_style
        ObjectMapper mapper = new ObjectMapper();
        boolean firstNull = true;
        String value = "text";
        PairBean bean = firstNull ? new PairBean(null, value) : new PairBean(value, null);
        ObjectMapper selected = mapper;
        String json = selected.writeValueAsString(bean);
        assertEquals(firstNull ? "[null,\"" + value + "\"]" : "[\"" + value + "\",null]", json);
    }

    @Test(timeout = 4000)
    public void test_null_array_column_003() throws Exception {
        // Native IPO combination: null_column=second, value=bar, mapper_reuse=shared_style
        ObjectMapper mapper = new ObjectMapper();
        boolean firstNull = false;
        String value = "bar";
        PairBean bean = firstNull ? new PairBean(null, value) : new PairBean(value, null);
        ObjectMapper selected = mapper;
        String json = selected.writeValueAsString(bean);
        assertEquals(firstNull ? "[null,\"" + value + "\"]" : "[\"" + value + "\",null]", json);
    }

    @Test(timeout = 4000)
    public void test_null_array_column_004() throws Exception {
        // Native IPO combination: null_column=second, value=text, mapper_reuse=fresh
        ObjectMapper mapper = new ObjectMapper();
        boolean firstNull = false;
        String value = "text";
        PairBean bean = firstNull ? new PairBean(null, value) : new PairBean(value, null);
        ObjectMapper selected = new ObjectMapper();
        String json = selected.writeValueAsString(bean);
        assertEquals(firstNull ? "[null,\"" + value + "\"]" : "[\"" + value + "\",null]", json);
    }

}
