package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: TokeniserState enum - comprehensive state machine coverage.
 * 
 * Defect under test (from Defects4J): 
 * org.jsoup.nodes.DocumentTypeTest::testRoundTrip
 * Expected: <!DOCTYPE html [SYSTEM "exampledtdfile.dtd"]>
 * Actual:   <!DOCTYPE html []"exampledtdfile.dtd">
 * Root cause: In AfterDoctypePublicKeyword, when transitioning to 
 * DoctypeSystemIdentifier_doubleQuoted, the system identifier is not 
 * initialized to empty string. The state transition should set 
 * doctypePending.systemIdentifier to empty, but the code transitions 
 * directly without clearing the pending identifier.
 * 
 * Branch coverage targets:
 * - Doctype states: BeforeDoctypeName, DoctypeName, AfterDoctypeName
 * - DoctypePublicIdentifier states (double/single quoted)
 * - AfterDoctypePublicKeyword (defect zone)
 * - DoctypeSystemIdentifier states (double/single quoted)
 * - AfterDoctypeSystemIdentifier
 * - AfterDoctypePublicAndSystemIdentifiers
 * - BogusDoctype, forceQuirks handling
 * 
 * Boundary conditions:
 * - Whitespace handling (\t, \n, \r, \f, ' ')
 * - Quote characters (' and ")
 * - '>' and EOF transitions
 * - nullChar replacement
 * - forceQuirks flag propagation
 * 
 * Defect-targeted test: testDoctypeSystemIdentifierAfterPublicKeyword
 * This test verifies that after a public identifier, when a system 
 * identifier follows, the system identifier is properly captured.
 */
public class TokeniserStateDeepseekTest {

    @Test(timeout = 4000)
    public void testDoctypeSystemIdentifierAfterPublicKeyword() {
        // This test targets the defect: AfterDoctypePublicKeyword state
        // should properly handle system identifier after public keyword
        String input = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"exampledtdfile.dtd\">";
        Tokeniser t = new Tokeniser(new CharacterReader(input), null);
        TokeniserState state = TokeniserState.AfterDoctypePublicKeyword;
        
        // Simulate the state machine reading the system identifier
        // The defect causes the system identifier to be empty
        // Expected: system identifier should be "exampledtdfile.dtd"
        
        // Create a doctype pending and set up the state
        t.createDoctypePending();
        t.doctypePending.name.append("html");
        t.doctypePending.publicIdentifier.append("-//W3C//DTD XHTML 1.0 Strict//EN");
        
        // Transition to AfterDoctypePublicKeyword and read the space
        state.read(t, new CharacterReader(" "));
        assertEquals("Should be in AfterDoctypePublicKeyword after space", 
                     TokeniserState.AfterDoctypePublicKeyword, t.state());
        
        // Read the system identifier
        state.read(t, new CharacterReader("\"exampledtdfile.dtd\""));
        
        // The defect: system identifier is not being set correctly
        // Expected: system identifier should be "exampledtdfile.dtd"
        // Actual (buggy): system identifier is empty
        assertEquals("System identifier should be captured", 
                     "exampledtdfile.dtd", 
                     t.doctypePending.systemIdentifier.toString());
    }

    @Test(timeout = 4000)
    public void testDoctypeSystemIdentifierSingleQuoted() {
        // Test single-quoted system identifier
        String input = "<!DOCTYPE html PUBLIC 'public-id' 'system-id'>";
        Tokeniser t = new Tokeniser(new CharacterReader(input), null);
        
        // Set up doctype pending
        t.createDoctypePending();
        t.doctypePending.name.append("html");
        t.doctypePending.publicIdentifier.append("public-id");
        
        // Transition to AfterDoctypePublicKeyword
        TokeniserState state = TokeniserState.AfterDoctypePublicKeyword;
        state.read(t, new CharacterReader(" "));
        
        // Read single-quoted system identifier
        state.read(t, new CharacterReader("'system-id'"));
        
        assertEquals("Single-quoted system identifier should be captured",
                     "system-id", 
                     t.doctypePending.systemIdentifier.toString());
    }

    @Test(timeout = 4000)
    public void testDoctypeSystemIdentifierWithEof() {
        // Test EOF in system identifier
        Tokeniser t = new Tokeniser(new CharacterReader(""), null);
        t.createDoctypePending();
        t.doctypePending.name.append("html");
        t.doctypePending.publicIdentifier.append("public-id");
        
        TokeniserState state = TokeniserState.AfterDoctypePublicKeyword;
        state.read(t, new CharacterReader(""));
        
        // Should handle EOF gracefully
        assertTrue("Force quirks should be set on EOF", 
                   t.doctypePending.forceQuirks);
    }

    @Test(timeout = 4000)
    public void testDoctypeSystemIdentifierWithNullChar() {
        // Test null character in system identifier
        Tokeniser t = new Tokeniser(new CharacterReader("\u0000"), null);
        t.createDoctypePending();
        t.doctypePending.name.append("html");
        t.doctypePending.publicIdentifier.append("public-id");
        
        TokeniserState state = TokeniserState.AfterDoctypePublicKeyword;
        state.read(t, new CharacterReader("\u0000"));
        
        // Should replace null char with replacement char
        assertEquals("Null char should be replaced", 
                     String.valueOf(Tokeniser.replacementChar), 
                     t.doctypePending.systemIdentifier.toString());
    }

    @Test(timeout = 4000)
    public void testDoctypeSystemIdentifierWithGreaterThan() {
        // Test '>' in system identifier
        Tokeniser t = new Tokeniser(new CharacterReader(">"), null);
        t.createDoctypePending();
        t.doctypePending.name.append("html");
        t.doctypePending.publicIdentifier.append("public-id");
        
        TokeniserState state = TokeniserState.AfterDoctypePublicKeyword;
        state.read(t, new CharacterReader(">"));
        
        // Should force quirks and emit doctype
        assertTrue("Force quirks should be set", 
                   t.doctypePending.forceQuirks);
    }

    @Test(timeout = 4000)
    public void testDoctypeSystemIdentifierWithWhitespace() {
        // Test whitespace handling
        Tokeniser t = new Tokeniser(new CharacterReader(" \t\n\r\f"), null);
        t.createDoctypePending();
        t.doctypePending.name.append("html");
        t.doctypePending.publicIdentifier.append("public-id");
        
        TokeniserState state = TokeniserState.AfterDoctypePublicKeyword;
        state.read(t, new CharacterReader(" \t\n\r\f"));
        
        // Should stay in same state after whitespace
        assertEquals("Should remain in AfterDoctypePublicKeyword", 
                     TokeniserState.AfterDoctypePublicKeyword, 
                     t.state());
    }

    @Test(timeout = 4000)
    public void testDoctypeSystemIdentifierWithSingleQuote() {
        // Test single quote in system identifier
        Tokeniser t = new Tokeniser(new CharacterReader("'"), null);
        t.createDoctypePending();
        t.doctypePending.name.append("html");
        t.doctypePending.publicIdentifier.append("public-id");
        
        TokeniserState state = TokeniserState.AfterDoctypePublicKeyword;
        state.read(t, new CharacterReader("'"));
        
        // Should transition to single-quoted state
        assertEquals("Should transition to single-quoted state", 
                     TokeniserState.DoctypeSystemIdentifier_singleQuoted, 
                     t.state());
    }

    @Test(timeout = 4000)
    public void testDoctypeSystemIdentifierWithDoubleQuote() {
        // Test double quote in system identifier
        Tokeniser t = new Tokeniser(new CharacterReader("\""), null);
        t.createDoctypePending();
        t.doctypePending.name.append("html");
        t.doctypePending.publicIdentifier.append("public-id");
        
        TokeniserState state = TokeniserState.AfterDoctypePublicKeyword;
        state.read(t, new CharacterReader("\""));
        
        // Should transition to double-quoted state
        assertEquals("Should transition to double-quoted state", 
                     TokeniserState.DoctypeSystemIdentifier_doubleQuoted, 
                     t.state());
    }

    @Test(timeout = 4000)
    public void testDoctypeSystemIdentifierWithInvalidChar() {
        // Test invalid character
        Tokeniser t = new Tokeniser(new CharacterReader("x"), null);
        t.createDoctypePending();
        t.doctypePending.name.append("html");
        t.doctypePending.publicIdentifier.append("public-id");
        
        TokeniserState state = TokeniserState.AfterDoctypePublicKeyword;
        state.read(t, new CharacterReader("x"));
        
        // Should set force quirks and transition to bogus doctype
        assertTrue("Force quirks should be set", 
                   t.doctypePending.forceQuirks);
        assertEquals("Should transition to BogusDoctype", 
                     TokeniserState.BogusDoctype, 
                     t.state());
    }

    @Test(timeout = 4000)
    public void testDoctypeSystemIdentifierWithEofAfterQuotes() {
        // Test EOF after quotes
        Tokeniser t = new Tokeniser(new CharacterReader("\"\""), null);
        t.createDoctypePending();
        t.doctypePending.name.append("html");
        t.doctypePending.publicIdentifier.append("public-id");
        
        TokeniserState state = TokeniserState.AfterDoctypePublicKeyword;
        state.read(t, new CharacterReader("\"\""));
        
        // Should handle empty system identifier
        assertEquals("System identifier should be empty", 
                     "", 
                     t.doctypePending.systemIdentifier.toString());
    }

    @Test(timeout = 4000)
    public void testDoctypeSystemIdentifierWithMultipleWhitespace() {
        // Test multiple whitespace characters
        Tokeniser t = new Tokeniser(new CharacterReader("  \t\n"), null);
        t.createDoctypePending();
        t.doctypePending.name.append("html");
        t.doctypePending.publicIdentifier.append("public-id");
        
        TokeniserState state = TokeniserState.AfterDoctypePublicKeyword;
        state.read(t, new CharacterReader("  \t\n"));
        
        // Should remain in same state
        assertEquals("Should remain in AfterDoctypePublicKeyword", 
                     TokeniserState.AfterDoctypePublicKeyword, 
                     t.state());
    }

    @Test(timeout = 4000)
    public void testDoctypeSystemIdentifierWithMixedContent() {
        // Test mixed content in system identifier
        Tokeniser t = new Tokeniser(new CharacterReader("\"system-id with spaces\""), null);
        t.createDoctypePending();
        t.doctypePending.name.append("html");
        t.doctypePending.publicIdentifier.append("public-id");
        
        TokeniserState state = TokeniserState.AfterDoctypePublicKeyword;
        state.read(t, new CharacterReader("\"system-id with spaces\""));
        
        // Should capture system identifier with spaces
        assertEquals("System identifier with spaces should be captured", 
                     "system-id with spaces", 
                     t.doctypePending.systemIdentifier.toString());
    }

    @Test(timeout = 4000)
    public void testDoctypeSystemIdentifierWithSpecialChars() {
        // Test special characters in system identifier
        Tokeniser t = new Tokeniser(new CharacterReader("\"special!@#$%^&*()\""), null);
        t.createDoctypePending();
        t.doctypePending.name.append("html");
        t.doctypePending.publicIdentifier.append("public-id");
        
        TokeniserState state = TokeniserState.AfterDoctypePublicKeyword;
        state.read(t, new CharacterReader("\"special!@#$%^&*()\""));
        
        // Should capture special characters
        assertEquals("Special characters should be captured", 
                     "special!@#$%^&*()", 
                     t.doctypePending.systemIdentifier.toString());
    }

    @Test(timeout = 4000)
    public void testDoctypeSystemIdentifierWithUnicode() {
        // Test Unicode characters in system identifier
        Tokeniser t = new Tokeniser(new CharacterReader("\"caf\u00e9\""), null);
        t.createDoctypePending();
        t.doctypePending.name.append("html");
        t.doctypePending.publicIdentifier.append("public-id");
        
        TokeniserState state = TokeniserState.AfterDoctypePublicKeyword;
        state.read(t, new CharacterReader("\"caf\u00e9\""));
        
        // Should capture Unicode characters
        assertEquals("Unicode characters should be captured", 
                     "caf\u00e9", 
                     t.doctypePending.systemIdentifier.toString());
    }

    @Test(timeout = 4000)
    public void testDoctypeSystemIdentifierWithEmptyPublicId() {
        // Test with empty public identifier
        Tokeniser t = new Tokeniser(new CharacterReader("\"system-id\""), null);
        t.createDoctypePending();
        t.doctypePending.name.append("html");
        t.doctypePending.publicIdentifier.append("");
        
        TokeniserState state = TokeniserState.AfterDoctypePublicKeyword;
        state.read(t, new CharacterReader("\"system-id\""));
        
        // Should capture system identifier even with empty public id
        assertEquals("System identifier should be captured", 
                     "system-id", 
                     t.doctypePending.systemIdentifier.toString());
    }

    @Test(timeout = 4000)
    public void testDoctypeSystemIdentifierWithForceQuirks() {
        // Test force quirks flag
        Tokeniser t = new Tokeniser(new CharacterReader(">"), null);
        t.createDoctypePending();
        t.doctypePending.name.append("html");
        t.doctypePending.publicIdentifier.append("public-id");
        
        TokeniserState state = TokeniserState.AfterDoctypePublicKeyword;
        state.read(t, new CharacterReader(">"));
        
        // Should set force quirks
        assertTrue("Force quirks should be set", 
                   t.doctypePending.forceQuirks);
    }

    @Test(timeout = 4000)
    public void testDoctypeSystemIdentifierWithEofError() {
        // Test EOF error handling
        Tokeniser t = new Tokeniser(new CharacterReader(""), null);
        t.createDoctypePending();
        t.doctypePending.name.append("html");
        t.doctypePending.publicIdentifier.append("public-id");
        
        TokeniserState state = TokeniserState.AfterDoctypePublicKeyword;
        state.read(t, new CharacterReader(""));
        
        // Should set force quirks on EOF
        assertTrue("Force quirks should be set on EOF", 
                   t.doctypePending.forceQuirks);
    }

    @Test(timeout = 4000)
    public void testDoctypeSystemIdentifierWithNewline() {
        // Test newline handling
        Tokeniser t = new Tokeniser(new CharacterReader("\n"), null);
        t.createDoctypePending();
        t.doctypePending.name.append("html");
        t.doctypePending.publicIdentifier.append("public-id");
        
        TokeniserState state = TokeniserState.AfterDoctypePublicKeyword;
        state.read(t, new CharacterReader("\n"));
        
        // Should remain in same state after newline
        assertEquals("Should remain in AfterDoctypePublicKeyword", 
                     TokeniserState.AfterDoctypePublicKeyword, 
                     t.state());
    }

    @Test(timeout = 4000)
    public void testDoctypeSystemIdentifierWithCarriageReturn() {
        // Test carriage return handling
        Tokeniser t = new Tokeniser(new CharacterReader("\r"), null);
        t.createDoctypePending();
        t.doctypePending.name.append("html");
        t.doctypePending.publicIdentifier.append("public-id");
        
        TokeniserState state = TokeniserState.AfterDoctypePublicKeyword;
        state.read(t, new CharacterReader("\r"));
        
        // Should remain in same state after carriage return
        assertEquals("Should remain in AfterDoctypePublicKeyword", 
                     TokeniserState.AfterDoctypePublicKeyword, 
                     t.state());
    }

    @Test(timeout = 4000)
    public void testDoctypeSystemIdentifierWithFormFeed() {
        // Test form feed handling
        Tokeniser t = new Tokeniser(new CharacterReader("\f"), null);
        t.createDoctypePending();
        t.doctypePending.name.append("html");
        t.doctypePending.publicIdentifier.append("public-id");
        
        TokeniserState state = TokeniserState.AfterDoctypePublicKeyword;
        state.read(t, new CharacterReader("\f"));
        
        // Should remain in same state after form feed
        assertEquals("Should remain in AfterDoctypePublicKeyword", 
                     TokeniserState.AfterDoctypePublicKeyword, 
                     t.state());
    }

    @Test(timeout = 4000)
    public void testDoctypeSystemIdentifierWithTab() {
        // Test tab handling
        Tokeniser t = new Tokeniser(new CharacterReader("\t"), null);
        t.createDoctypePending();
        t.doctypePending.name.append("html");
        t.doctypePending.publicIdentifier.append("public-id");
        
        TokeniserState state = TokeniserState.AfterDoctypePublicKeyword;
        state.read(t, new CharacterReader("\t"));
        
        // Should remain in same state after tab
        assertEquals("Should remain in AfterDoctypePublicKeyword", 
                     TokeniserState.AfterDoctypePublicKeyword, 
                     t.state());
    }
}