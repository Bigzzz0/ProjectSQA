package com.fasterxml.jackson.core.util;

import static org.junit.Assert.*;
import org.junit.Test;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.json.JsonFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * [Branch & Defect Analysis Matrix]
 *
 * Target class: JsonParserSequence
 * Known defect: testInitialized expects 2 but gets 3 (missing flag for already-positioned parser)
 *
 * Branches covered:
 * 1. Constructor: _parsers[0] assigned as delegate, _nextParser=1
 * 2. createFlattened: both not sequences -> simple sequence
 * 3. createFlattened: first is sequence -> flatten
 * 4. createFlattened: second is sequence -> flatten
 * 5. createFlattened: both sequences -> flatten both
 * 6. addFlattenedActiveParsers: loop from _nextParser-1 to end, recursive flattening
 * 7. nextToken: delegate.nextToken() returns non-null -> return it
 * 8. nextToken: delegate.nextToken() returns null -> switchToNext and retry
 * 9. nextToken: switchToNext fails -> return null
 * 10. switchToNext: _nextParser >= _parsers.length -> false
 * 11. switchToNext: else -> set delegate, increment, return true
 * 12. close: loop calling delegate.close() and switchToNext()
 * 13. containedParsersCount: returns _parsers.length
 *
 * Boundary conditions:
 * - Empty parsers array? Not possible due to constructor requiring at least one.
 * - Single parser sequence (via createFlattened? Not directly, but possible via manual construction? Not exposed.)
 * - Parser already positioned (defect trigger)
 * - Null parsers? Not allowed by contract.
 * - Flattening with nested sequences of varying depths.
 * - Edge: _nextParser-1 may be 0 initially, but addFlattenedActiveParsers starts from _nextParser-1.
 *
 * Defect-targeted test: testInitializedState – verifies that a sequence created from a parser
 * that already has a current token returns that token first, not skipping it.
 */
public class JsonParserSequenceDeepseekTest {

    // Helper to create a simple parser from a JSON string
    private JsonParser parserFromString(String json) throws IOException, JsonParseException {
        JsonFactory factory = new JsonFactory();
        return factory.createParser(json);
    }

    // ===================== Partition A: Core Functional Logic & State Transitions =====================

    @Test(timeout = 4000)
    public void testBasicSequenceTwoParsers() throws IOException {
        JsonParser p1 = parserFromString("{\"a\":1}");
        JsonParser p2 = parserFromString("{\"b\":2}");
        JsonParserSequence seq = JsonParserSequence.createFlattened(p1, p2);
        assertNotNull(seq);
        assertEquals(2, seq.containedParsersCount());

        // First token from p1
        assertSame(JsonToken.START_OBJECT, seq.nextToken());
        // Second token from p1
        assertSame(JsonToken.FIELD_NAME, seq.nextToken());
        assertEquals("a", seq.getCurrentName());
        // Third token from p1
        assertSame(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertEquals(1, seq.getIntValue());
        // Fourth token from p1
        assertSame(JsonToken.END_OBJECT, seq.nextToken());
        // Now switch to p2
        assertSame(JsonToken.START_OBJECT, seq.nextToken());
        assertSame(JsonToken.FIELD_NAME, seq.nextToken());
        assertEquals("b", seq.getCurrentName());
        assertSame(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertEquals(2, seq.getIntValue());
        assertSame(JsonToken.END_OBJECT, seq.nextToken());
        assertNull(seq.nextToken()); // end of sequence
    }

    @Test(timeout = 4000)
    public void testNextTokenReturnsNullWhenEmpty() throws IOException {
        // Create a sequence with a single parser that has no tokens (empty document)
        JsonParser p1 = parserFromString(" ");
        JsonParserSequence seq = JsonParserSequence.createFlattened(p1, parserFromString(" "));
        // Both parsers will return null immediately
        assertNull(seq.nextToken());
    }

    @Test(timeout = 4000)
    public void testSwitchToNextReturnsFalseWhenNoMoreParsers() throws IOException {
        JsonParser p1 = parserFromString("1");
        JsonParserSequence seq = JsonParserSequence.createFlattened(p1, parserFromString("2"));
        // Consume all tokens
        assertSame(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertEquals(1, seq.getIntValue());
        assertSame(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertEquals(2, seq.getIntValue());
        assertNull(seq.nextToken());
        // Now switchToNext should return false
        assertFalse(seq.switchToNext());
    }

    @Test(timeout = 4000)
    public void testCloseClosesAllParsers() throws IOException {
        JsonParser p1 = parserFromString("1");
        JsonParser p2 = parserFromString("2");
        JsonParserSequence seq = JsonParserSequence.createFlattened(p1, p2);
        seq.close();
        // After close, parsers should be closed; further nextToken should throw
        try {
            seq.nextToken();
            fail("Expected IOException");
        } catch (IOException e) {
            // expected
        }
    }

    // ===================== Partition B: Boundary Value Analysis & Extremes =====================

    @Test(timeout = 4000)
    public void testSingleParserSequence() throws IOException {
        // createFlattened with only one parser? Not directly, but we can use the protected constructor via subclass? Not needed.
        // Instead, test that createFlattened with two parsers where one is empty works.
        JsonParser p1 = parserFromString("true");
        JsonParser p2 = parserFromString(""); // empty, will return null immediately
        JsonParserSequence seq = JsonParserSequence.createFlattened(p1, p2);
        assertSame(JsonToken.VALUE_TRUE, seq.nextToken());
        assertNull(seq.nextToken());
    }

    @Test(timeout = 4000)
    public void testFlattenedWithNestedSequences() throws IOException {
        // Create two sequences and flatten them
        JsonParser p1 = parserFromString("1");
        JsonParser p2 = parserFromString("2");
        JsonParserSequence inner1 = JsonParserSequence.createFlattened(p1, p2);
        JsonParser p3 = parserFromString("3");
        JsonParser p4 = parserFromString("4");
        JsonParserSequence inner2 = JsonParserSequence.createFlattened(p3, p4);
        JsonParserSequence outer = JsonParserSequence.createFlattened(inner1, inner2);
        // Should have 4 parsers flattened
        assertEquals(4, outer.containedParsersCount());
        // Consume all tokens
        for (int i = 1; i <= 4; i++) {
            assertSame(JsonToken.VALUE_NUMBER_INT, outer.nextToken());
            assertEquals(i, outer.getIntValue());
        }
        assertNull(outer.nextToken());
    }

    @Test(timeout = 4000)
    public void testContainedParsersCountAfterFlattening() throws IOException {
        JsonParser p1 = parserFromString("1");
        JsonParser p2 = parserFromString("2");
        JsonParserSequence inner = JsonParserSequence.createFlattened(p1, p2);
        JsonParser p3 = parserFromString("3");
        JsonParserSequence outer = JsonParserSequence.createFlattened(inner, p3);
        assertEquals(3, outer.containedParsersCount());
    }

    // ===================== Partition C: Defect-Targeted Branch Zone =====================

    /**
     * This test directly targets the known defect: when a parser is already positioned
     * on a token (e.g., after calling nextToken() once), the sequence should return that
     * token first, not skip it. The bug is that the missing flag causes the sequence to
     * call delegate.nextToken() again, advancing past the current token.
     */
    @Test(timeout = 4000)
    public void testInitializedState() throws IOException {
        // Create two parsers, each already positioned on a token
        JsonParser p1 = parserFromString("{\"x\":1}");
        p1.nextToken(); // now at START_OBJECT
        JsonParser p2 = parserFromString("{\"y\":2}");
        p2.nextToken(); // now at START_OBJECT

        // Create sequence with these already-positioned parsers
        JsonParserSequence seq = JsonParserSequence.createFlattened(p1, p2);

        // The first token should be the current token of p1 (START_OBJECT)
        // Bug would cause it to return the next token (FIELD_NAME "x") or skip to p2
        assertSame("First token should be START_OBJECT from p1's current token",
                JsonToken.START_OBJECT, seq.nextToken());

        // Now continue normally: should get FIELD_NAME "x", VALUE_NUMBER_INT 1, END_OBJECT
        assertSame(JsonToken.FIELD_NAME, seq.nextToken());
        assertEquals("x", seq.getCurrentName());
        assertSame(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertEquals(1, seq.getIntValue());
        assertSame(JsonToken.END_OBJECT, seq.nextToken());

        // Then switch to p2: should get its current token (START_OBJECT)
        assertSame("After p1, should get START_OBJECT from p2's current token",
                JsonToken.START_OBJECT, seq.nextToken());

        // Then rest of p2
        assertSame(JsonToken.FIELD_NAME, seq.nextToken());
        assertEquals("y", seq.getCurrentName());
        assertSame(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertEquals(2, seq.getIntValue());
        assertSame(JsonToken.END_OBJECT, seq.nextToken());

        // End of sequence
        assertNull(seq.nextToken());
    }

    // ===================== Partition D: Exception & Defensive Guard Paths =====================

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testCreateFlattenedWithNullFirst() {
        JsonParserSequence.createFlattened(null, parserFromString("1"));
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testCreateFlattenedWithNullSecond() {
        JsonParserSequence.createFlattened(parserFromString("1"), null);
    }

    @Test(timeout = 4000)
    public void testSwitchToNextAfterExhaustion() throws IOException {
        JsonParser p1 = parserFromString("1");
        JsonParserSequence seq = JsonParserSequence.createFlattened(p1, parserFromString("2"));
        seq.nextToken(); // consume 1
        seq.nextToken(); // consume 2
        seq.nextToken(); // null
        // Now switchToNext should return false
        assertFalse(seq.switchToNext());
    }

    // ===================== Partition E: Object Lifecycle & Contract Integrity =====================

    @Test(timeout = 4000)
    public void testContainedParsersCountConsistency() throws IOException {
        JsonParser p1 = parserFromString("1");
        JsonParser p2 = parserFromString("2");
        JsonParserSequence seq = JsonParserSequence.createFlattened(p1, p2);
        assertEquals(2, seq.containedParsersCount());
        // After consuming all tokens, count should remain same
        seq.nextToken();
        seq.nextToken();
        seq.nextToken();
        assertEquals(2, seq.containedParsersCount());
    }

    @Test(timeout = 4000)
    public void testCloseIdempotent() throws IOException {
        JsonParser p1 = parserFromString("1");
        JsonParserSequence seq = JsonParserSequence.createFlattened(p1, parserFromString("2"));
        seq.close();
        // Closing again should not throw
        seq.close();
    }

    @Test(timeout = 4000)
    public void testAddFlattenedActiveParsersInternal() throws IOException {
        // Test the protected method indirectly via createFlattened with nested sequences
        JsonParser p1 = parserFromString("1");
        JsonParser p2 = parserFromString("2");
        JsonParserSequence inner = JsonParserSequence.createFlattened(p1, p2);
        JsonParser p3 = parserFromString("3");
        JsonParserSequence outer = JsonParserSequence.createFlattened(inner, p3);
        // The inner sequence's addFlattenedActiveParsers should have been called
        // and should have added p1 and p2 directly to outer's list.
        assertEquals(3, outer.containedParsersCount());
    }
}