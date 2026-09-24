package com.fasterxml.jackson.core.util;

/* [Branch & Defect Analysis Matrix]
 * Target Class: com.fasterxml.jackson.core.util.JsonParserSequence
 *
 * 1. Constructor: JsonParserSequence(JsonParser[])
 *    - Assigns delegate = parsers[0], _parsers = parsers, _nextParser = 1
 *    - Boundary: null array (NPE), empty array (AIOOBE), single parser array
 *
 * 2. Factory: createFlattened(JsonParser first, JsonParser second)
 *    - Decision Branch 1: Neither is JsonParserSequence -> direct array creation [first, second]
 *    - Decision Branch 2: first is JsonParserSequence, second is NOT -> first flattened, second appended
 *    - Decision Branch 3: first is NOT, second is JsonParserSequence -> first appended, second flattened
 *    - Decision Branch 4: Both are JsonParserSequence -> both flattened into combined list
 *    - Recursive Branch: Nested sequences within sequences flattened into single level
 *
 * 3. Flattening Helper: addFlattenedActiveParsers(List<JsonParser> result)
 *    - Loop bounds: i = (_nextParser - 1) to (_parsers.length - 1)
 *    - State sensitivity: Excludes parsers already consumed prior to current active delegate
 *    - Branch: Element is JsonParserSequence -> recursive flattening
 *    - Branch: Element is leaf JsonParser -> directly appended to result
 *
 * 4. Token Iteration: nextToken()
 *    - Branch A: delegate.nextToken() != null -> returns active parser's token immediately
 *    - Branch B: delegate.nextToken() == null -> enters while (switchToNext()) loop
 *      - Sub-branch B1: switchToNext() succeeds (returns true):
 *        - delegate.nextToken() != null -> returns next parser's token
 *        - delegate.nextToken() == null -> loops again (seamlessly skips empty subparsers)
 *      - Sub-branch B2: switchToNext() exhausts all parsers (returns false) -> returns null
 *
 * 5. State Transition: switchToNext()
 *    - Branch A: _nextParser >= _parsers.length -> returns false (sequence exhausted)
 *    - Branch B: _nextParser < _parsers.length -> advances delegate to _parsers[_nextParser++], returns true
 *
 * 6. Resource Cleanup: close()
 *    - Loop: do { delegate.close(); } while (switchToNext())
 *    - Ensures the active parser and all subsequent unconsumed parsers are closed
 *
 * 7. Query: containedParsersCount()
 *    - Returns fixed length of _parsers array regardless of consumption state
 *
 * 8. Defect-Targeted Zone (Defects4J Ground Truth):
 *    - Failure: com.fasterxml.jackson.core.json.ParserSequenceTest::testInitialized
 *      --> junit.framework.AssertionFailedError: expected:<2> but was:<3>
 *    - Root cause: An initialized parser pointing to a token is sequenced. Calling nextToken()
 *      unconditionally delegates to delegate.nextToken(), skipping the current token.
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public class JsonParserSequenceGeminiTest {

    private final JsonFactory JSON_F = new JsonFactory();

    // =========================================================================
    // PARTITION A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicTwoParserSequence() throws IOException {
        JsonParser p1 = JSON_F.createParser("{\"a\":1}");
        JsonParser p2 = JSON_F.createParser("{\"b\":2}");

        JsonParserSequence seq = JsonParserSequence.createFlattened(p1, p2);
        assertEquals(2, seq.containedParsersCount());

        // First parser tokens
        assertEquals(JsonToken.START_OBJECT, seq.nextToken());
        assertEquals(JsonToken.FIELD_NAME, seq.nextToken());
        assertEquals("a", seq.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertEquals(1, seq.getIntValue());
        assertEquals(JsonToken.END_OBJECT, seq.nextToken());

        // Seamless transition to second parser tokens
        assertEquals(JsonToken.START_OBJECT, seq.nextToken());
        assertEquals(JsonToken.FIELD_NAME, seq.nextToken());
        assertEquals("b", seq.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertEquals(2, seq.getIntValue());
        assertEquals(JsonToken.END_OBJECT, seq.nextToken());

        // Sequence exhausted
        assertNull(seq.nextToken());
        seq.close();
    }

    @Test(timeout = 4000)
    public void testFlatteningFirstOnlyIsSequence() throws IOException {
        JsonParser p1 = JSON_F.createParser("\"t1\"");
        JsonParser p2 = JSON_F.createParser("\"t2\"");
        JsonParser p3 = JSON_F.createParser("\"t3\"");

        JsonParserSequence seq1 = JsonParserSequence.createFlattened(p1, p2);
        JsonParserSequence seqCombined = JsonParserSequence.createFlattened(seq1, p3);

        assertEquals(3, seqCombined.containedParsersCount());
        assertEquals(JsonToken.VALUE_STRING, seqCombined.nextToken());
        assertEquals("t1", seqCombined.getText());
        assertEquals(JsonToken.VALUE_STRING, seqCombined.nextToken());
        assertEquals("t2", seqCombined.getText());
        assertEquals(JsonToken.VALUE_STRING, seqCombined.nextToken());
        assertEquals("t3", seqCombined.getText());
        assertNull(seqCombined.nextToken());

        seqCombined.close();
    }

    @Test(timeout = 4000)
    public void testFlatteningSecondOnlyIsSequence() throws IOException {
        JsonParser p1 = JSON_F.createParser("\"t1\"");
        JsonParser p2 = JSON_F.createParser("\"t2\"");
        JsonParser p3 = JSON_F.createParser("\"t3\"");

        JsonParserSequence seq2 = JsonParserSequence.createFlattened(p2, p3);
        JsonParserSequence seqCombined = JsonParserSequence.createFlattened(p1, seq2);

        assertEquals(3, seqCombined.containedParsersCount());
        assertEquals(JsonToken.VALUE_STRING, seqCombined.nextToken());
        assertEquals("t1", seqCombined.getText());
        assertEquals(JsonToken.VALUE_STRING, seqCombined.nextToken());
        assertEquals("t2", seqCombined.getText());
        assertEquals(JsonToken.VALUE_STRING, seqCombined.nextToken());
        assertEquals("t3", seqCombined.getText());
        assertNull(seqCombined.nextToken());

        seqCombined.close();
    }

    @Test(timeout = 4000)
    public void testFlatteningBothAreSequences() throws IOException {
        JsonParser p1 = JSON_F.createParser("10");
        JsonParser p2 = JSON_F.createParser("20");
        JsonParser p3 = JSON_F.createParser("30");
        JsonParser p4 = JSON_F.createParser("40");

        JsonParserSequence seq1 = JsonParserSequence.createFlattened(p1, p2);
        JsonParserSequence seq2 = JsonParserSequence.createFlattened(p3, p4);

        JsonParserSequence combined = JsonParserSequence.createFlattened(seq1, seq2);
        assertEquals(4, combined.containedParsersCount());

        assertEquals(JsonToken.VALUE_NUMBER_INT, combined.nextToken());
        assertEquals(10, combined.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, combined.nextToken());
        assertEquals(20, combined.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, combined.nextToken());
        assertEquals(30, combined.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, combined.nextToken());
        assertEquals(40, combined.getIntValue());
        assertNull(combined.nextToken());

        combined.close();
    }

    @Test(timeout = 4000)
    public void testDeeplyNestedFlatteningRecursion() throws IOException {
        JsonParser p1 = JSON_F.createParser("1");
        JsonParser p2 = JSON_F.createParser("2");
        JsonParser p3 = JSON_F.createParser("3");

        // Manually assemble nested sequences to exercise recursive branch in addFlattenedActiveParsers
        JsonParserSequence inner = JsonParserSequence.createFlattened(p1, p2);
        JsonParserSequence outer = new JsonParserSequence(new JsonParser[] { inner, p3 });

        List<JsonParser> result = new ArrayList<JsonParser>();
        outer.addFlattenedActiveParsers(result);

        assertEquals(3, result.size());
        assertSame(p1, result.get(0));
        assertSame(p2, result.get(1));
        assertSame(p3, result.get(2));

        outer.close();
    }

    @Test(timeout = 4000)
    public void testSwitchToNextDirectHelperCalls() throws IOException {
        JsonParser p1 = JSON_F.createParser("1");
        JsonParser p2 = JSON_F.createParser("2");
        JsonParserSequence seq = new JsonParserSequence(new JsonParser[] { p1, p2 });

        assertSame(p1, seq.delegate());
        assertTrue(seq.switchToNext());
        assertSame(p2, seq.delegate());
        assertFalse(seq.switchToNext());
        // Verify state is stable when further exhausted
        assertSame(p2, seq.delegate());
        assertFalse(seq.switchToNext());

        seq.close();
    }

    // =========================================================================
    // PARTITION B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testSingleParserInSequence() throws IOException {
        JsonParser p1 = JSON_F.createParser("42");
        JsonParserSequence seq = new JsonParserSequence(new JsonParser[] { p1 });

        assertEquals(1, seq.containedParsersCount());
        assertSame(p1, seq.delegate());
        assertFalse(seq.switchToNext());

        assertEquals(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertEquals(42, seq.getIntValue());
        assertNull(seq.nextToken());

        seq.close();
    }

    @Test(timeout = 4000)
    public void testEmptySubparsersSkippedCorrectly() throws IOException {
        // Sequence with empty parsers at start, middle, and end
        JsonParser empty1 = JSON_F.createParser("");
        JsonParser empty2 = JSON_F.createParser("");
        JsonParser valid1 = JSON_F.createParser("100");
        JsonParser empty3 = JSON_F.createParser("");
        JsonParser valid2 = JSON_F.createParser("200");
        JsonParser empty4 = JSON_F.createParser("");

        JsonParserSequence seq = new JsonParserSequence(
                new JsonParser[] { empty1, empty2, valid1, empty3, valid2, empty4 }
        );

        assertEquals(6, seq.containedParsersCount());
        assertEquals(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertEquals(100, seq.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertEquals(200, seq.getIntValue());
        assertNull(seq.nextToken());

        seq.close();
    }

    @Test(timeout = 4000)
    public void testAllSubparsersEmptyReturnsNull() throws IOException {
        JsonParser empty1 = JSON_F.createParser("");
        JsonParser empty2 = JSON_F.createParser("");
        JsonParser empty3 = JSON_F.createParser("");

        JsonParserSequence seq = new JsonParserSequence(new JsonParser[] { empty1, empty2, empty3 });
        assertNull(seq.nextToken());
        assertNull(seq.nextToken());

        seq.close();
    }

    @Test(timeout = 4000)
    public void testAddFlattenedActiveParsersRespectsCurrentIndex() throws IOException {
        JsonParser p1 = JSON_F.createParser("1");
        JsonParser p2 = JSON_F.createParser("2");
        JsonParser p3 = JSON_F.createParser("3");

        JsonParserSequence seq = new JsonParserSequence(new JsonParser[] { p1, p2, p3 });

        // Consume first parser completely
        assertEquals(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        // Switch to p2
        assertEquals(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertEquals(2, seq.getIntValue());

        // Now _nextParser should have advanced past p1
        List<JsonParser> activeList = new ArrayList<JsonParser>();
        seq.addFlattenedActiveParsers(activeList);

        // p1 must not be added since it is no longer active
        assertEquals(2, activeList.size());
        assertSame(p2, activeList.get(0));
        assertSame(p3, activeList.get(1));

        seq.close();
    }

    // =========================================================================
    // PARTITION C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets known failure: ParserSequenceTest::testInitialized
     * Ground Truth Error: junit.framework.AssertionFailedError: expected:<2> but was:<3>
     *
     * When a JsonParser is already initialized to point to a token (e.g. token '2'),
     * creating a sequence and calling nextToken() must yield that initialized token (2)
     * rather than skipping it and returning the next token (3).
     */
    @Test(timeout = 4000)
    public void testInitializedParserTargetingDefectExpected2Was3() throws IOException {
        JsonParser p1 = JSON_F.createParser("1 2");
        JsonParser p2 = JSON_F.createParser("3 false");

        // Advance p1 to token 1
        assertEquals(JsonToken.VALUE_NUMBER_INT, p1.nextToken());
        assertEquals(1, p1.getIntValue());

        // Advance p1 to token 2 so p1 is initialized with current token = 2
        assertEquals(JsonToken.VALUE_NUMBER_INT, p1.nextToken());
        assertEquals(2, p1.getIntValue());

        // Sequence p1 (currently pointing to 2) with p2
        JsonParserSequence seq = JsonParserSequence.createFlattened(p1, p2);

        // Expectation: The sequence must yield the active token of p1 (2)
        // On the defective implementation, nextToken() advances p1 immediately,
        // exhausting p1, switching to p2, and returning 3, failing with expected:<2> but was:<3>.
        assertEquals(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertEquals(2, seq.getIntValue());

        assertEquals(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertEquals(3, seq.getIntValue());

        assertEquals(JsonToken.VALUE_FALSE, seq.nextToken());
        assertNull(seq.nextToken());

        seq.close();
    }

    /**
     * Additional boundary test for an initialized single-token parser.
     */
    @Test(timeout = 4000)
    public void testInitializedSingleTokenParserRetention() throws IOException {
        JsonParser p1 = JSON_F.createParser("2");
        assertEquals(JsonToken.VALUE_NUMBER_INT, p1.nextToken());
        assertEquals(2, p1.getIntValue());

        JsonParser p2 = JSON_F.createParser("3");

        JsonParserSequence seq = JsonParserSequence.createFlattened(p1, p2);

        // Expected to return token 2 before transitioning to token 3
        assertEquals(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertEquals(2, seq.getIntValue());

        assertEquals(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertEquals(3, seq.getIntValue());
        assertNull(seq.nextToken());

        seq.close();
    }

    // =========================================================================
    // PARTITION D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testNullArrayInConstructorThrowsNPE() {
        new JsonParserSequence(null);
    }

    @Test(timeout = 4000, expected = ArrayIndexOutOfBoundsException.class)
    public void testEmptyArrayInConstructorThrowsAIOOBE() {
        new JsonParserSequence(new JsonParser[0]);
    }

    @Test(timeout = 4000)
    public void testCloseClosesAllRemainingParsers() throws IOException {
        JsonParser p1 = JSON_F.createParser("1");
        JsonParser p2 = JSON_F.createParser("2");
        JsonParser p3 = JSON_F.createParser("3");

        JsonParserSequence seq = new JsonParserSequence(new JsonParser[] { p1, p2, p3 });

        assertFalse(p1.isClosed());
        assertFalse(p2.isClosed());
        assertFalse(p3.isClosed());

        // Close when at initial position
        seq.close();

        assertTrue(p1.isClosed());
        assertTrue(p2.isClosed());
        assertTrue(p3.isClosed());
    }

    @Test(timeout = 4000)
    public void testClosePartiallyConsumedSequence() throws IOException {
        JsonParser p1 = JSON_F.createParser("1");
        JsonParser p2 = JSON_F.createParser("2");
        JsonParser p3 = JSON_F.createParser("3");

        JsonParserSequence seq = new JsonParserSequence(new JsonParser[] { p1, p2, p3 });

        // Advance into p2
        assertEquals(JsonToken.VALUE_NUMBER_INT, seq.nextToken()); // consumes 1 from p1
        assertEquals(JsonToken.VALUE_NUMBER_INT, seq.nextToken()); // consumes 2 from p2

        seq.close();

        // All parsers from p2 to end must be closed
        assertTrue(p2.isClosed());
        assertTrue(p3.isClosed());
    }

    @Test(timeout = 4000)
    public void testCloseFullyConsumedSequence() throws IOException {
        JsonParser p1 = JSON_F.createParser("1");
        JsonParser p2 = JSON_F.createParser("2");

        JsonParserSequence seq = new JsonParserSequence(new JsonParser[] { p1, p2 });
        while (seq.nextToken() != null) {
            // consume all
        }

        seq.close();
        assertTrue(p1.isClosed());
        assertTrue(p2.isClosed());
    }

    // =========================================================================
    // PARTITION E: Object Lifecycle & Delegation Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testDelegationMethodsReflectCurrentParserState() throws IOException {
        JsonParser p1 = JSON_F.createParser("{\"k\":\"v\"}");
        JsonParser p2 = JSON_F.createParser("[true]");

        JsonParserSequence seq = JsonParserSequence.createFlattened(p1, p2);

        assertNull(seq.currentToken());
        assertFalse(seq.hasCurrentToken());

        assertEquals(JsonToken.START_OBJECT, seq.nextToken());
        assertEquals(JsonToken.START_OBJECT, seq.currentToken());
        assertTrue(seq.hasCurrentToken());

        assertEquals(JsonToken.FIELD_NAME, seq.nextToken());
        assertEquals("k", seq.getCurrentName());
        assertEquals("k", seq.getText());

        assertEquals(JsonToken.VALUE_STRING, seq.nextToken());
        assertEquals("v", seq.getText());

        assertEquals(JsonToken.END_OBJECT, seq.nextToken());

        // Switches to p2
        assertEquals(JsonToken.START_ARRAY, seq.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, seq.nextToken());
        assertTrue(seq.getBooleanValue());
        assertEquals(JsonToken.END_ARRAY, seq.nextToken());

        assertNull(seq.nextToken());
        assertFalse(seq.hasCurrentToken());

        seq.close();
    }
}