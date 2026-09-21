package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

public class EntitiesTest {

    // Existing test method for quoteReplacements
    @Test
    public void quoteReplacements() {
        // Test that the escape method correctly escapes special characters
        String input = "\"hello\" & 'world'";
        String expected = "&quot;hello&quot; &amp; &apos;world&apos;";
        assertEquals(expected, Entities.escape(input, Charset.forName("UTF-8"), Entities.EscapeMode.base, false));
    }

    // New test method targeting the defect
    @Test
    public void testIllegalGroupReference() {
        // This test targets the defect where an IllegalArgumentException is thrown
        // when the input contains a backslash followed by a digit, which is interpreted
        // as a group reference in the replacement string.
        String input = "\\1";
        try {
            String result = Entities.escape(input, Charset.forName("UTF-8"), Entities.EscapeMode.base, false);
            // If no exception is thrown, the test fails because the defect is not triggered
            fail("Expected IllegalArgumentException to be thrown for input with group reference");
        } catch (IllegalArgumentException e) {
            // Expected exception, test passes
        }
    }
}