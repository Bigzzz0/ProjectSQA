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
package org.apache.commons.lang.text;

import org.junit.Test;
import static org.junit.Assert.*;

import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/*
 * [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------------
 * Target Class: org.apache.commons.lang.text.ExtendedMessageFormat
 * Target Defect: LANG-477 (OutOfMemoryError due to unadvanced ParsePosition when encountering escaped quotes)
 *
 * Decision / Condition Matrix:
 * 1. Constructor Overloads:
 *    - ExtendedMessageFormat(pattern)
 *    - ExtendedMessageFormat(pattern, locale)
 *    - ExtendedMessageFormat(pattern, registry)
 *    - ExtendedMessageFormat(pattern, locale, registry)
 *
 * 2. applyPattern(pattern) & insertFormats(pattern, customPatterns):
 *    - registry == null vs registry != null
 *    - Quote branches: single quote, escaped quotes ('')
 *    - START_FE ('{'): argument index parsing, non-whitespace traversal
 *    - Custom format found in registry vs not found in registry (fallback to stripCustom)
 *    - Format description with sub-arguments (indexOf(',') > 0) vs without arguments
 *    - Nested format elements (depth > 1, e.g. ChoiceFormat patterns)
 *    - Quoted sections inside format element descriptions (e.g., choice formats with strings)
 *
 * 3. readArgumentIndex:
 *    - Digits only (normal path)
 *    - Whitespace trimming before and after digits
 *    - Invalid argument index: non-digit characters, whitespace followed by illegal characters
 *    - Premature end of pattern: unterminated format element
 *
 * 4. parseFormatDescription:
 *    - Depth counting for nested braces
 *    - Quoted strings inside description (getQuotedString)
 *    - Unterminated format descriptions
 *
 * 5. Disabled Mutator Guard Paths:
 *    - setFormat(int, Format) -> UnsupportedOperationException
 *    - setFormatByArgumentIndex(int, Format) -> UnsupportedOperationException
 *    - setFormats(Format[]) -> UnsupportedOperationException
 *    - setFormatsByArgumentIndex(Format[]) -> UnsupportedOperationException
 *
 * 6. Defect LANG-477:
 *    - Pattern containing escaped quote with non-null registry triggers infinite loop/OOM in defective version.
 * ---------------------------------------------------------------------------------------------------------
 */
public class ExtendedMessageFormatGeminiTest {

    // Simple test factory for custom formats
    private static class LowerCaseFormatFactory implements FormatFactory {
        public Format getFormat(String name, String arguments, Locale locale) {
            return new Format() {
                private static final long serialVersionUID = 1L;

                @Override
                public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                    return toAppendTo.append(obj.toString().toLowerCase(locale));
                }

                @Override
                public Object parseObject(String source, ParsePosition pos) {
                    return null;
                }
            };
        }
    }

    private static class UpperCaseFormatFactory implements FormatFactory {
        public Format getFormat(String name, String arguments, Locale locale) {
            return new Format() {
                private static final long serialVersionUID = 1L;

                @Override
                public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                    return toAppendTo.append(obj.toString().toUpperCase(locale));
                }

                @Override
                public Object parseObject(String source, ParsePosition pos) {
                    return null;
                }
            };
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicMessageFormatWithoutRegistry() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("Hello {0}!");
        assertEquals("Hello {0}!", emf.toPattern());
        assertEquals("Hello World!", emf.format(new Object[]{"World"}));
    }

    @Test(timeout = 4000)
    public void testBasicMessageFormatWithLocale() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("Amount: {0,number,currency}", Locale.US);
        assertEquals(Locale.US, emf.getLocale());
        String formatted = emf.format(new Object[]{12.34});
        assertTrue(formatted.contains("$12.34"));
    }

    @Test(timeout = 4000)
    public void testCustomFormatWithoutArgs() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("lower", new LowerCaseFormatFactory());

        ExtendedMessageFormat emf = new ExtendedMessageFormat("Result: {0,lower}", registry);
        assertEquals("Result: {0,lower}", emf.toPattern());
        assertEquals("Result: test", emf.format(new Object[]{"TEST"}));
    }

    @Test(timeout = 4000)
    public void testCustomFormatWithArgs() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("date", new FormatFactory() {
            public Format getFormat(String name, String arguments, Locale locale) {
                return new SimpleDateFormat(arguments, locale);
            }
        });

        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0,date,yyyy-MM-dd}", Locale.US, registry);
        assertEquals("{0,date,yyyy-MM-dd}", emf.toPattern());
    }

    @Test(timeout = 4000)
    public void testMultipleCustomAndStandardFormats() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("lower", new LowerCaseFormatFactory());
        registry.put("upper", new UpperCaseFormatFactory());

        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0,lower} and {1,upper} and {2,number}", registry);
        String formatted = emf.format(new Object[]{"FOO", "bar", 100});
        assertEquals("foo and BAR and 100", formatted);
    }

    @Test(timeout = 4000)
    public void testCustomFormatWithWhitespace() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("lower", new LowerCaseFormatFactory());

        ExtendedMessageFormat emf = new ExtendedMessageFormat("Value: {  0  ,  lower  }", registry);
        assertEquals("Value: test", emf.format(new Object[]{"TEST"}));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyPatternWithoutRegistry() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("");
        assertEquals("", emf.toPattern());
        assertEquals("", emf.format(new Object[0]));
    }

    @Test(timeout = 4000)
    public void testEmptyPatternWithRegistry() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("lower", new LowerCaseFormatFactory());

        ExtendedMessageFormat emf = new ExtendedMessageFormat("", registry);
        assertEquals("", emf.toPattern());
        assertEquals("", emf.format(new Object[0]));
    }

    @Test(timeout = 4000)
    public void testEmptyRegistry() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        ExtendedMessageFormat emf = new ExtendedMessageFormat("Plain {0}", registry);
        assertEquals("Plain {0}", emf.toPattern());
        assertEquals("Plain text", emf.format(new Object[]{"text"}));
    }

    @Test(timeout = 4000)
    public void testRegistryWithNullFormatReturned() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("unknown", new FormatFactory() {
            public Format getFormat(String name, String arguments, Locale locale) {
                return null;
            }
        });

        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0,unknown}", registry);
        assertNotNull(emf);
    }

    @Test(timeout = 4000)
    public void testFormatDescriptionWithNestedBraces() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("dummy", new LowerCaseFormatFactory());

        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0,choice,0#zero|1#{1,dummy}}", registry);
        assertNotNull(emf);
    }

    @Test(timeout = 4000)
    public void testFormatDescriptionWithQuotedSection() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("dummy", new LowerCaseFormatFactory());

        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0,choice,0#'{none}'|1#'{single}'}", registry);
        assertNotNull(emf);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (LANG-477)
    // =========================================================================

    /**
     * LANG-477: ExtendedMessageFormat enters infinite loop and throws OutOfMemoryError
     * when pattern contains an escaped quote ('') and a custom registry is present.
     */
    @Test(timeout = 4000)
    public void testEscapedQuote_LANG_477() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("dummy", new LowerCaseFormatFactory());

        String pattern = "# ''";
        ExtendedMessageFormat emf = new ExtendedMessageFormat(pattern, registry);
        assertEquals("# ''", emf.format(new Object[0]));
    }

    @Test(timeout = 4000)
    public void testEscapedQuoteWithFormatElement() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("lower", new LowerCaseFormatFactory());

        String pattern = "''{0,lower}''";
        ExtendedMessageFormat emf = new ExtendedMessageFormat(pattern, registry);
        assertEquals("'hello'", emf.format(new Object[]{"HELLO"}));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testSetFormatThrowsUnsupportedOperationException() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("test");
        emf.setFormat(0, NumberFormat.getInstance());
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testSetFormatByArgumentIndexThrowsUnsupportedOperationException() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("test");
        emf.setFormatByArgumentIndex(0, NumberFormat.getInstance());
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testSetFormatsThrowsUnsupportedOperationException() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("test");
        emf.setFormats(new Format[]{NumberFormat.getInstance()});
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testSetFormatsByArgumentIndexThrowsUnsupportedOperationException() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("test");
        emf.setFormatsByArgumentIndex(new Format[]{NumberFormat.getInstance()});
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInvalidFormatArgumentIndexNonDigit() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("lower", new LowerCaseFormatFactory());
        new ExtendedMessageFormat("{invalid}", registry);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInvalidFormatArgumentIndexWithExtraNonWs() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("lower", new LowerCaseFormatFactory());
        new ExtendedMessageFormat("{0 bad}", registry);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testUnterminatedFormatElementAtStart() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("lower", new LowerCaseFormatFactory());
        new ExtendedMessageFormat("{", registry);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testUnterminatedFormatElementIndex() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("lower", new LowerCaseFormatFactory());
        new ExtendedMessageFormat("{0", registry);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testUnterminatedFormatDescription() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("lower", new LowerCaseFormatFactory());
        new ExtendedMessageFormat("{0,lower", registry);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testUnterminatedQuotedString() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("lower", new LowerCaseFormatFactory());
        new ExtendedMessageFormat("'{0,lower}", registry);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("lower", new LowerCaseFormatFactory());

        ExtendedMessageFormat emf1 = new ExtendedMessageFormat("{0,lower}", registry);
        ExtendedMessageFormat emf2 = new ExtendedMessageFormat("{0,lower}", registry);
        ExtendedMessageFormat emf3 = new ExtendedMessageFormat("{0}", registry);

        assertEquals(emf1, emf1);
        assertEquals(emf1, emf2);
        assertEquals(emf1.hashCode(), emf2.hashCode());

        assertFalse(emf1.equals(emf3));
        assertFalse(emf1.equals(null));
        assertFalse(emf1.equals("Some String"));
    }

    @Test(timeout = 4000)
    public void testApplyPatternOverwritesPreviousState() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("lower", new LowerCaseFormatFactory());
        registry.put("upper", new UpperCaseFormatFactory());

        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0,lower}", registry);
        assertEquals("hello", emf.format(new Object[]{"HELLO"}));

        emf.applyPattern("{0,upper}");
        assertEquals("HELLO", emf.format(new Object[]{"hello"}));
    }
}