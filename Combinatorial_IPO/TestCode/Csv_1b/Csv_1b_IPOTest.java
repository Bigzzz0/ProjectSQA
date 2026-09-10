package org.apache.commons.csv;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.StringReader;

/** Generated from approved defect-focused scenarios using native IPO. */
public class Csv_1b_IPOTest {

    @Test(timeout = 4000)
    public void test_line_counter_001() throws Exception {
        // Native IPO combination: line_ending=cr, read_mode=character, line_count=two
        String ending = "\r";
        int count = 2;
        boolean characterMode = true;
        String text = count == 2 ? "a" + ending + "b" : "a" + ending + "b" + ending + "c";
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader(text));
        if (characterMode) {
            while (reader.read() != -1) { }
        } else {
            while (reader.readLine() != null) { }
        }
        assertEquals(characterMode ? count - 1 : count, reader.getLineNumber());
        reader.close();
    }

    @Test(timeout = 4000)
    public void test_line_counter_002() throws Exception {
        // Native IPO combination: line_ending=cr, read_mode=line, line_count=three
        String ending = "\r";
        int count = 3;
        boolean characterMode = false;
        String text = count == 2 ? "a" + ending + "b" : "a" + ending + "b" + ending + "c";
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader(text));
        if (characterMode) {
            while (reader.read() != -1) { }
        } else {
            while (reader.readLine() != null) { }
        }
        assertEquals(characterMode ? count - 1 : count, reader.getLineNumber());
        reader.close();
    }

    @Test(timeout = 4000)
    public void test_line_counter_003() throws Exception {
        // Native IPO combination: line_ending=lf, read_mode=character, line_count=three
        String ending = "\n";
        int count = 3;
        boolean characterMode = true;
        String text = count == 2 ? "a" + ending + "b" : "a" + ending + "b" + ending + "c";
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader(text));
        if (characterMode) {
            while (reader.read() != -1) { }
        } else {
            while (reader.readLine() != null) { }
        }
        assertEquals(characterMode ? count - 1 : count, reader.getLineNumber());
        reader.close();
    }

    @Test(timeout = 4000)
    public void test_line_counter_004() throws Exception {
        // Native IPO combination: line_ending=lf, read_mode=line, line_count=two
        String ending = "\n";
        int count = 2;
        boolean characterMode = false;
        String text = count == 2 ? "a" + ending + "b" : "a" + ending + "b" + ending + "c";
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader(text));
        if (characterMode) {
            while (reader.read() != -1) { }
        } else {
            while (reader.readLine() != null) { }
        }
        assertEquals(characterMode ? count - 1 : count, reader.getLineNumber());
        reader.close();
    }

    @Test(timeout = 4000)
    public void test_line_counter_005() throws Exception {
        // Native IPO combination: line_ending=crlf, read_mode=character, line_count=two
        String ending = "\r\n";
        int count = 2;
        boolean characterMode = true;
        String text = count == 2 ? "a" + ending + "b" : "a" + ending + "b" + ending + "c";
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader(text));
        if (characterMode) {
            while (reader.read() != -1) { }
        } else {
            while (reader.readLine() != null) { }
        }
        assertEquals(characterMode ? count - 1 : count, reader.getLineNumber());
        reader.close();
    }

    @Test(timeout = 4000)
    public void test_line_counter_006() throws Exception {
        // Native IPO combination: line_ending=crlf, read_mode=line, line_count=three
        String ending = "\r\n";
        int count = 3;
        boolean characterMode = false;
        String text = count == 2 ? "a" + ending + "b" : "a" + ending + "b" + ending + "c";
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader(text));
        if (characterMode) {
            while (reader.read() != -1) { }
        } else {
            while (reader.readLine() != null) { }
        }
        assertEquals(characterMode ? count - 1 : count, reader.getLineNumber());
        reader.close();
    }

    @Test(timeout = 4000)
    public void test_line_counter_007() throws Exception {
        // Native IPO combination: line_ending=cr, read_mode=character, line_count=three
        String ending = "\r";
        int count = 3;
        boolean characterMode = true;
        String text = count == 2 ? "a" + ending + "b" : "a" + ending + "b" + ending + "c";
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader(text));
        if (characterMode) {
            while (reader.read() != -1) { }
        } else {
            while (reader.readLine() != null) { }
        }
        assertEquals(characterMode ? count - 1 : count, reader.getLineNumber());
        reader.close();
    }

}
