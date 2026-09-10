package com.fasterxml.jackson.dataformat.xml.deser;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

/** Generated from approved defect-focused scenarios using native IPO. */
public class JacksonXml_1b_IPOTest {

static class Records {
        @JacksonXmlElementWrapper(useWrapping=false) public List<Record> records = new ArrayList<Record>();
    }
    static class Record {
        @JacksonXmlElementWrapper(useWrapping=false) public List<Field> fields = new ArrayList<Field>();
    }
    static class Field {
        @JacksonXmlProperty(isAttribute=true) public String name;
        public Field() { }
    }

    @Test(timeout = 4000)
    public void test_nested_unwrapped_empty_list_001() throws Exception {
        // Native IPO combination: empty_syntax=paired, empty_position=first, field_name=b
        String empty = "<records></records>";
        String populated = "<records><fields name='" + "b" + "'/></records>";
        String xml = "<Records>" + (true ? empty + populated : populated + empty) + "</Records>";
        Records result = new XmlMapper().readValue(xml, Records.class);
        assertNotNull(result.records);
        assertEquals(2, result.records.size());
        int populatedIndex = true ? 1 : 0;
        assertNotNull(result.records.get(1 - populatedIndex));
        assertEquals("b", result.records.get(populatedIndex).fields.get(0).name);
    }

    @Test(timeout = 4000)
    public void test_nested_unwrapped_empty_list_002() throws Exception {
        // Native IPO combination: empty_syntax=paired, empty_position=last, field_name=name
        String empty = "<records></records>";
        String populated = "<records><fields name='" + "name" + "'/></records>";
        String xml = "<Records>" + (false ? empty + populated : populated + empty) + "</Records>";
        Records result = new XmlMapper().readValue(xml, Records.class);
        assertNotNull(result.records);
        assertEquals(2, result.records.size());
        int populatedIndex = false ? 1 : 0;
        assertNotNull(result.records.get(1 - populatedIndex));
        assertEquals("name", result.records.get(populatedIndex).fields.get(0).name);
    }

    @Test(timeout = 4000)
    public void test_nested_unwrapped_empty_list_003() throws Exception {
        // Native IPO combination: empty_syntax=self_closing, empty_position=first, field_name=name
        String empty = "<records/>";
        String populated = "<records><fields name='" + "name" + "'/></records>";
        String xml = "<Records>" + (true ? empty + populated : populated + empty) + "</Records>";
        Records result = new XmlMapper().readValue(xml, Records.class);
        assertNotNull(result.records);
        assertEquals(2, result.records.size());
        int populatedIndex = true ? 1 : 0;
        assertNotNull(result.records.get(1 - populatedIndex));
        assertEquals("name", result.records.get(populatedIndex).fields.get(0).name);
    }

    @Test(timeout = 4000)
    public void test_nested_unwrapped_empty_list_004() throws Exception {
        // Native IPO combination: empty_syntax=self_closing, empty_position=last, field_name=b
        String empty = "<records/>";
        String populated = "<records><fields name='" + "b" + "'/></records>";
        String xml = "<Records>" + (false ? empty + populated : populated + empty) + "</Records>";
        Records result = new XmlMapper().readValue(xml, Records.class);
        assertNotNull(result.records);
        assertEquals(2, result.records.size());
        int populatedIndex = false ? 1 : 0;
        assertNotNull(result.records.get(1 - populatedIndex));
        assertEquals("b", result.records.get(populatedIndex).fields.get(0).name);
    }

}
