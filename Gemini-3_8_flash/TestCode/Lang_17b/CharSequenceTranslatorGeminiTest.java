/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.lang3.text.translate;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import org.junit.Test;
import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Target Class: CharSequenceTranslator
 *
 * Method: translate(CharSequence input)
 * - Branch 1: input == null -> return null
 * - Branch 2: input != null -> allocate StringWriter, call translate(input, writer), return toString()
 *
 * Method: translate(CharSequence input, Writer out)
 * - Branch 1: out == null -> throw IllegalArgumentException("The Writer must not be null")
 * - Branch 2: input == null -> return immediately (no-op)
 * - Branch 3: pos < len loop:
 *     - Sub-branch 3a: consumed == 0 -> write codePointAt(input, pos) as char[]
 *     - Sub-branch 3b: consumed != 0 -> advance pos by consumed codepoints:
 *         - Sub-branch 3b.1: pos < len - 2 -> pos += Character.charCount(...)
 *         - Sub-branch 3b.2: else -> pos++
 *
 * DEFECT TARGET (LANG-720 / testLang720):
 * - Defect: 'len' is calculated using Character.codePointCount(input, 0, input.length()), but 'pos'
 *   is passed to Character.codePointAt(input, pos) which expects a UTF-16 code unit (char) index,
 *   causing index misalignment and early loop termination for supplementary characters (e.g. "\uD842\uDFB7A" / "𠮷A").
 * - Manifestation: High/low surrogate pairs cause the second char unit to be treated as an independent codepoint,
 *   producing corrupted surrogate output (e.g., expected "𠮷A", defective yields "𠮷?").
 *
 * Method: with(CharSequenceTranslator... translators)
 * - Merges this translator at index 0 with subsequent translators into an AggregateTranslator.
 *
 * Method: hex(int codepoint)
 * - Returns uppercase hex string representation of the given codepoint.
 * ====================================================================================================
 */
public class CharSequenceTranslatorGeminiTest {

    /**
     * Minimal concrete CharSequenceTranslator for testing base functionality.
     * Passes through characters without modification (returns 0 consumed).
     */
    private static final class IdentityTranslator extends CharSequenceTranslator {
        @Override
        public int translate(CharSequence input, int index, Writer out) throws IOException {
            return 0;
        }
    }

    /**
     * Translator that replaces any character 'a' with "X", consuming 1 code unit.
     */
    private static final class ReplaceCharTranslator extends CharSequenceTranslator {
        private final char target;
        private final String replacement;

        ReplaceCharTranslator(char target, String replacement) {
            this.target = target;
            this.replacement = replacement;
        }

        @Override
        public int translate(CharSequence input, int index, Writer out) throws IOException {
            if (input.charAt(index) == target) {
                out.write(replacement);
                return 1;
            }
            return 0;
        }
    }

    /**
     * Mock Writer that predictably throws an IOException on any write operation.
     */
    private static final class FaultyWriter extends Writer {
        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            throw new IOException("Simulated write failure");
        }

        @Override
        public void flush() throws IOException {
            throw new IOException("Simulated flush failure");
        }

        @Override
        public void close() throws IOException {
            throw new IOException("Simulated close failure");
        }
    }

    // ================================================================================================
    // Partition A: Core Functional Logic & State Transitions
    // ================================================================================================

    @Test(timeout = 4000)
    public void testTranslateStringPassthrough() {
        CharSequenceTranslator translator = new IdentityTranslator();
        String input = "Hello, World!";
        String result = translator.translate(input);
        assertEquals("Unmodified text should be returned identically", "Hello, World!", result);
    }

    @Test(timeout = 4000)
    public void testTranslateToWriterPassthrough() throws IOException {
        CharSequenceTranslator translator = new IdentityTranslator();
        StringWriter writer = new StringWriter();
        translator.translate("Testing 123", writer);
        assertEquals("Output in writer should match input string", "Testing 123", writer.toString());
    }

    @Test(timeout = 4000)
    public void testTranslateWithReplacement() {
        CharSequenceTranslator translator = new ReplaceCharTranslator('a', "XXX");
        String result = translator.translate("banana");
        assertEquals("Should replace all occurrences of 'a' with 'XXX'", "bXXXnXXXnXXX", result);
    }

    @Test(timeout = 4000)
    public void testWithMergesTranslators() {
        CharSequenceTranslator t1 = new ReplaceCharTranslator('a', "A");
        CharSequenceTranslator t2 = new ReplaceCharTranslator('b', "B");
        CharSequenceTranslator combined = t1.with(t2);

        assertNotNull("Merged translator should not be null", combined);
        assertTrue("Merged translator must be an AggregateTranslator instance",
                combined instanceof AggregateTranslator);

        String result = combined.translate("abracadabra");
        assertEquals("Both translator rules should be executed sequentially", "ABrAcAdABrA", result);
    }

    @Test(timeout = 4000)
    public void testHexConversions() {
        assertEquals