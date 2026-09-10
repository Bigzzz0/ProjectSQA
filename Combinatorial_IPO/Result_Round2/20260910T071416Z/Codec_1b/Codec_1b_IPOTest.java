package org.apache.commons.codec.language;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Locale;
import org.apache.commons.codec.StringEncoder;

/** Generated from approved defect-focused scenarios using native IPO. */
public class Codec_1b_IPOTest {

    @Test(timeout = 4000)
    public void test_locale_independent_encoding_001() throws Exception {
        // Native IPO combination: encoder=caverphone, locale=english, input=upper_i
        Locale original = Locale.getDefault();
        try {
            StringEncoder encoder = new Caverphone();
            String input = "I";
            Locale.setDefault(Locale.ENGLISH);
            String expected = encoder.encode(input);
            Locale.setDefault(Locale.ENGLISH);
            assertEquals(expected, encoder.encode(input));
        } finally {
            Locale.setDefault(original);
        }
    }

    @Test(timeout = 4000)
    public void test_locale_independent_encoding_002() throws Exception {
        // Native IPO combination: encoder=caverphone, locale=turkish, input=lower_i
        Locale original = Locale.getDefault();
        try {
            StringEncoder encoder = new Caverphone();
            String input = "i";
            Locale.setDefault(Locale.ENGLISH);
            String expected = encoder.encode(input);
            Locale.setDefault(new Locale("tr"));
            assertEquals(expected, encoder.encode(input));
        } finally {
            Locale.setDefault(original);
        }
    }

    @Test(timeout = 4000)
    public void test_locale_independent_encoding_003() throws Exception {
        // Native IPO combination: encoder=metaphone, locale=english, input=lower_i
        Locale original = Locale.getDefault();
        try {
            StringEncoder encoder = new Metaphone();
            String input = "i";
            Locale.setDefault(Locale.ENGLISH);
            String expected = encoder.encode(input);
            Locale.setDefault(Locale.ENGLISH);
            assertEquals(expected, encoder.encode(input));
        } finally {
            Locale.setDefault(original);
        }
    }

    @Test(timeout = 4000)
    public void test_locale_independent_encoding_004() throws Exception {
        // Native IPO combination: encoder=metaphone, locale=turkish, input=upper_i
        Locale original = Locale.getDefault();
        try {
            StringEncoder encoder = new Metaphone();
            String input = "I";
            Locale.setDefault(Locale.ENGLISH);
            String expected = encoder.encode(input);
            Locale.setDefault(new Locale("tr"));
            assertEquals(expected, encoder.encode(input));
        } finally {
            Locale.setDefault(original);
        }
    }

    @Test(timeout = 4000)
    public void test_locale_independent_encoding_005() throws Exception {
        // Native IPO combination: encoder=soundex, locale=english, input=upper_i
        Locale original = Locale.getDefault();
        try {
            StringEncoder encoder = new Soundex();
            String input = "I";
            Locale.setDefault(Locale.ENGLISH);
            String expected = encoder.encode(input);
            Locale.setDefault(Locale.ENGLISH);
            assertEquals(expected, encoder.encode(input));
        } finally {
            Locale.setDefault(original);
        }
    }

    @Test(timeout = 4000)
    public void test_locale_independent_encoding_006() throws Exception {
        // Native IPO combination: encoder=soundex, locale=turkish, input=lower_i
        Locale original = Locale.getDefault();
        try {
            StringEncoder encoder = new Soundex();
            String input = "i";
            Locale.setDefault(Locale.ENGLISH);
            String expected = encoder.encode(input);
            Locale.setDefault(new Locale("tr"));
            assertEquals(expected, encoder.encode(input));
        } finally {
            Locale.setDefault(original);
        }
    }

    @Test(timeout = 4000)
    public void test_locale_independent_encoding_007() throws Exception {
        // Native IPO combination: encoder=caverphone, locale=turkish, input=upper_i
        Locale original = Locale.getDefault();
        try {
            StringEncoder encoder = new Caverphone();
            String input = "I";
            Locale.setDefault(Locale.ENGLISH);
            String expected = encoder.encode(input);
            Locale.setDefault(new Locale("tr"));
            assertEquals(expected, encoder.encode(input));
        } finally {
            Locale.setDefault(original);
        }
    }

}
