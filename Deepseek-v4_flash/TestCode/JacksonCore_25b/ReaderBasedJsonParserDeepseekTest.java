import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.base.ParserBase;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.json.JsonReadContext;
import com.fasterxml.jackson.core.json.JsonWriteContext;
import com.fasterxml.jackson.core.sym.ByteQuadsCanonicalizer;
import com.fasterxml.jackson.core.sym.CharsToNameCanonicalizer;
import com.fasterxml.jackson.core.util.BufferRecycler;
import com.fasterxml.jackson.core.util.JsonParserDelegate;
import com.fasterxml.jackson.core.util.TextBuffer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Test class targeting the known defect in ReaderBasedJsonParser.
 * The defect causes ArrayIndexOutOfBoundsException when parsing
 * unquoted field names with certain characters (issue #510).
 */
public class ReaderBasedJsonParserTest {

    private JsonFactory factory;
    private ReaderBasedJsonParser parser;
    private String input;
    private StringReader reader;

    @BeforeEach
    public void setUp() {
        factory = new JsonFactory();
        // Enable the feature that allows unquoted field names
        factory.enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
    }

    @AfterEach
    public void tearDown() throws IOException {
        if (parser != null) {
            parser.close();
        }
        if (reader != null) {
            reader.close();
        }
    }

    /**
     * Test that specifically targets the ArrayIndexOutOfBoundsException
     * when parsing unquoted field names with non-ASCII characters.
     * This reproduces the issue from testUnquotedIssue510.
     */
    @Test
    public void testUnquotedIssue510() throws IOException {
        // This input contains an unquoted field name with a non-ASCII character
        // that caused ArrayIndexOutOfBoundsException in the defective version
        input = "{\u00e9:1}";  // é character in field name
        
        reader = new StringReader(input);
        parser = (ReaderBasedJsonParser) factory.createParser(reader);
        
        // This should not throw ArrayIndexOutOfBoundsException
        // The bug was in _parseName2 method when handling non-ASCII characters
        assertDoesNotThrow(() -> {
            JsonToken token = parser.nextToken();
            assertEquals(JsonToken.START_OBJECT, token);
            
            token = parser.nextToken();
            assertEquals(JsonToken.FIELD_NAME, token);
            assertEquals("\u00e9", parser.getCurrentName());
            
            token = parser.nextToken();
            assertEquals(JsonToken.VALUE_NUMBER_INT, token);
            assertEquals(1, parser.getIntValue());
            
            token = parser.nextToken();
            assertEquals(JsonToken.END_OBJECT, token);
        });
    }

    /**
     * Test with various unquoted field names that could trigger the bug
     */
    @Test
    public void testUnquotedFieldNamesWithSpecialChars() throws IOException {
        String[] fieldNames = {
            "abc", "a\u00e9b", "\u00e9", "a\u00e9", "\u00e9b",
            "a\u00e9\u00e9b", "test\u00e9", "\u00e9test"
        };
        
        for (String fieldName : fieldNames) {
            input = "{" + fieldName + ":1}";
            reader = new StringReader(input);
            parser = (ReaderBasedJsonParser) factory.createParser(reader);
            
            assertDoesNotThrow(() -> {
                JsonToken token = parser.nextToken();
                assertEquals(JsonToken.START_OBJECT, token);
                
                token = parser.nextToken();
                assertEquals(JsonToken.FIELD_NAME, token);
                assertEquals(fieldName, parser.getCurrentName());
                
                token = parser.nextToken();
                assertEquals(JsonToken.VALUE_NUMBER_INT, token);
                
                token = parser.nextToken();
                assertEquals(JsonToken.END_OBJECT, token);
            });
            
            parser.close();
            reader.close();
        }
    }

    /**
     * Test with unquoted field names at buffer boundaries
     */
    @Test
    public void testUnquotedFieldNamesAtBufferBoundaries() throws IOException {
        // Create a field name that spans multiple buffer segments
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int i = 0; i < 100; i++) {
            sb.append("a");
        }
        sb.append(":1}");
        
        input = sb.toString();
        reader = new StringReader(input);
        parser = (ReaderBasedJsonParser) factory.createParser(reader);
        
        assertDoesNotThrow(() -> {
            JsonToken token = parser.nextToken();
            assertEquals(JsonToken.START_OBJECT, token);
            
            token = parser.nextToken();
            assertEquals(JsonToken.FIELD_NAME, token);
            assertEquals(100, parser.getCurrentName().length());
            
            token = parser.nextToken();
            assertEquals(JsonToken.VALUE_NUMBER_INT, token);
            
            token = parser.nextToken();
            assertEquals(JsonToken.END_OBJECT, token);
        });
    }

    /**
     * Test with unquoted field names containing various characters
     */
    @Test
    public void testUnquotedFieldNamesWithMixedChars() throws IOException {
        String[] inputs = {
            "{abc123:1}",
            "{a_b-c:1}",
            "{a.b:1}",
            "{a$b:1}",
            "{a\u00e9b:1}",
            "{\u00e9\u00e9:1}",
            "{a\u00e9\u00e9b:1}"
        };
        
        for (String testInput : inputs) {
            input = testInput;
            reader = new StringReader(input);
            parser = (ReaderBasedJsonParser) factory.createParser(reader);
            
            assertDoesNotThrow(() -> {
                JsonToken token = parser.nextToken();
                assertEquals(JsonToken.START_OBJECT, token);
                
                token = parser.nextToken();
                assertEquals(JsonToken.FIELD_NAME, token);
                
                token = parser.nextToken();
                assertEquals(JsonToken.VALUE_NUMBER_INT, token);
                
                token = parser.nextToken();
                assertEquals(JsonToken.END_OBJECT, token);
            });
            
            parser.close();
            reader.close();
        }
    }

    /**
     * Test with unquoted field names that have leading/trailing spaces
     */
    @Test
    public void testUnquotedFieldNamesWithSpaces() throws IOException {
        String[] inputs = {
            "{ abc:1}",
            "{abc :1}",
            "{ abc :1}",
            "{\tabc\t:1}",
            "{\nabc\n:1}"
        };
        
        for (String testInput : inputs) {
            input = testInput;
            reader = new StringReader(input);
            parser = (ReaderBasedJsonParser) factory.createParser(reader);
            
            assertDoesNotThrow(() -> {
                JsonToken token = parser.nextToken();
                assertEquals(JsonToken.START_OBJECT, token);
                
                token = parser.nextToken();
                assertEquals(JsonToken.FIELD_NAME, token);
                
                token = parser.nextToken();
                assertEquals(JsonToken.VALUE_NUMBER_INT, token);
                
                token = parser.nextToken();
                assertEquals(JsonToken.END_OBJECT, token);
            });
            
            parser.close();
            reader.close();
        }
    }

    /**
     * Test with unquoted field names that are very long
     */
    @Test
    public void testUnquotedFieldNamesVeryLong() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int i = 0; i < 1000; i++) {
            sb.append("a");
        }
        sb.append(":1}");
        
        input = sb.toString();
        reader = new StringReader(input);
        parser = (ReaderBasedJsonParser) factory.createParser(reader);
        
        assertDoesNotThrow(() -> {
            JsonToken token = parser.nextToken();
            assertEquals(JsonToken.START_OBJECT, token);
            
            token = parser.nextToken();
            assertEquals(JsonToken.FIELD_NAME, token);
            assertEquals(1000, parser.getCurrentName().length());
            
            token = parser.nextToken();
            assertEquals(JsonToken.VALUE_NUMBER_INT, token);
            
            token = parser.nextToken();
            assertEquals(JsonToken.END_OBJECT, token);
        });
    }

    /**
     * Test with unquoted field names that contain escape sequences
     */
    @Test
    public void testUnquotedFieldNamesWithEscapes() throws IOException {
        String[] inputs = {
            "{a\\u00e9b:1}",
            "{a\\nb:1}",
            "{a\\tb:1}",
            "{a\\/b:1}",
            "{a\\\\b:1}"
        };
        
        for (String testInput : inputs) {
            input = testInput;
            reader = new StringReader(input);
            parser = (ReaderBasedJsonParser) factory.createParser(reader);
            
            assertDoesNotThrow(() -> {
                JsonToken token = parser.nextToken();
                assertEquals(JsonToken.START_OBJECT, token);
                
                token = parser.nextToken();
                assertEquals(JsonToken.FIELD_NAME, token);
                
                token = parser.nextToken();
                assertEquals(JsonToken.VALUE_NUMBER_INT, token);
                
                token = parser.nextToken();
                assertEquals(JsonToken.END_OBJECT, token);
            });
            
            parser.close();
            reader.close();
        }
    }

    /**
     * Test with unquoted field names that are numbers
     */
    @Test
    public void testUnquotedFieldNamesAsNumbers() throws IOException {
        String[] inputs = {
            "{123:1}",
            "{123.45:1}",
            "{123e10:1}",
            "{-123:1}",
            "{+123:1}"
        };
        
        for (String testInput : inputs) {
            input = testInput;
            reader = new StringReader(input);
            parser = (ReaderBasedJsonParser) factory.createParser(reader);
            
            assertDoesNotThrow(() -> {
                JsonToken token = parser.nextToken();
                assertEquals(JsonToken.START_OBJECT, token);
                
                token = parser.nextToken();
                assertEquals(JsonToken.FIELD_NAME, token);
                
                token = parser.nextToken();
                assertEquals(JsonToken.VALUE_NUMBER_INT, token);
                
                token = parser.nextToken();
                assertEquals(JsonToken.END_OBJECT, token);
            });
            
            parser.close();
            reader.close();
        }
    }

    /**
     * Test with unquoted field names that are keywords
     */
    @Test
    public void testUnquotedFieldNamesAsKeywords() throws IOException {
        String[] inputs = {
            "{true:1}",
            "{false:1}",
            "{null:1}",
            "{NaN:1}",
            "{Infinity:1}"
        };
        
        for (String testInput : inputs) {
            input = testInput;
            reader = new StringReader(input);
            parser = (ReaderBasedJsonParser) factory.createParser(reader);
            
            assertDoesNotThrow(() -> {
                JsonToken token = parser.nextToken();
                assertEquals(JsonToken.START_OBJECT, token);
                
                token = parser.nextToken();
                assertEquals(JsonToken.FIELD_NAME, token);
                
                token = parser.nextToken();
                assertEquals(JsonToken.VALUE_NUMBER_INT, token);
                
                token = parser.nextToken();
                assertEquals(JsonToken.END_OBJECT, token);
            });
            
            parser.close();
            reader.close();
        }
    }

    /**
     * Test with unquoted field names that have comments
     */
    @Test
    public void testUnquotedFieldNamesWithComments() throws IOException {
        factory.enable(JsonParser.Feature.ALLOW_COMMENTS);
        
        String[] inputs = {
            "{/*comment*/abc:1}",
            "{abc/*comment*/:1}",
            "{abc:/*comment*/1}",
            "{//comment\nabc:1}",
            "{abc//comment\n:1}"
        };
        
        for (String testInput : inputs) {
            input = testInput;
            reader = new StringReader(input);
            parser = (ReaderBasedJsonParser) factory.createParser(reader);
            
            assertDoesNotThrow(() -> {
                JsonToken token = parser.nextToken();
                assertEquals(JsonToken.START_OBJECT, token);
                
                token = parser.nextToken();
                assertEquals(JsonToken.FIELD_NAME, token);
                
                token = parser.nextToken();
                assertEquals(JsonToken.VALUE_NUMBER_INT, token);
                
                token = parser.nextToken();
                assertEquals(JsonToken.END_OBJECT, token);
            });
            
            parser.close();
            reader.close();
        }
    }

    /**
     * Test with unquoted field names that have YAML comments
     */
    @Test
    public void testUnquotedFieldNamesWithYAMLComments() throws IOException {
        factory.enable(JsonParser.Feature.ALLOW_YAML_COMMENTS);
        
        String[] inputs = {
            "{#comment\nabc:1}",
            "{abc#comment\n:1}",
            "{abc:#comment\n1}"
        };
        
        for (String testInput : inputs) {
            input = testInput;
            reader = new StringReader(input);
            parser = (ReaderBasedJsonParser) factory.createParser(reader);
            
            assertDoesNotThrow(() -> {
                JsonToken token = parser.nextToken();
                assertEquals(JsonToken.START_OBJECT, token);
                
                token = parser.nextToken();
                assertEquals(JsonToken.FIELD_NAME, token);
                
                token = parser.nextToken();
                assertEquals(JsonToken.VALUE_NUMBER_INT, token);
                
                token = parser.nextToken();
                assertEquals(JsonToken.END_OBJECT, token);
            });
            
            parser.close();
            reader.close();
        }
    }
}