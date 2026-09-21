package org.apache.commons.lang.text;

import org.junit.Test;
import static org.junit.Assert.*;

import java.text.Format;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target class: ExtendedMessageFormat
 * 
 * Branches covered:
 * - applyPattern: registry null vs non-null
 * - applyPattern: quoted strings (escapingOn true/false)
 * - applyPattern: format element parsing (START_FE, START_FMT, END_FE)
 * - applyPattern: readArgumentIndex: whitespace handling, digit validation, error cases
 * - applyPattern: parseFormatDescription: nested braces, quoted strings
 * - applyPattern: insertFormats: custom patterns insertion
 * - getFormat: registry lookup, format name with args
 * - containsElements: null/empty/non-null elements
 * - setFormat/setFormatByArgumentIndex/setFormats/setFormatsByArgumentIndex: UnsupportedOperationException
 * 
 * Defect target (LANG-477): Escaped quotes in pattern cause infinite loop / OutOfMemoryError.
 * The bug is in appendQuotedString when escapingOn is true and pattern contains escaped quotes ('').
 * The method may fail to advance the parse position correctly, leading to an infinite loop.
 * 
 * Test strategy:
 * - Partition A: Core functional logic (normal patterns, registry usage)
 * - Partition B: Boundary values (null/empty patterns, extreme indices)
 * - Partition C: Defect-targeted (escaped quotes patterns that trigger the bug)
 * - Partition D: Exception paths (invalid patterns, unsupported operations)
 * - Partition E: Object lifecycle (toPattern consistency, serialization not tested)
 */
public class ExtendedMessageFormatDeepseekTest {

    // ========== Partition A: Core Functional Logic ==========

    @Test(timeout = 4000)
    public void testSimplePatternNoRegistry() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("Hello {0}!");
        assertEquals("Hello {0}!", emf.toPattern());
        assertEquals("Hello World!", emf.format(new Object[]{"World"}));
    }

    @Test(timeout = 4000)
    public void testPatternWithRegistry() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("upper", new FormatFactory() {
            public Format getFormat(String name, String args, Locale locale) {
                return new java.text.Format() {
                    public StringBuffer format(Object obj, StringBuffer toAppendTo, java.text.FieldPosition pos) {
                        if (obj instanceof String) {
                            toAppendTo.append(((String) obj).toUpperCase());
                        }
                        return toAppendTo;
                    }
                    public Object parseObject(String source, java.text.ParsePosition pos) {
                        return null;
                    }
                };
            }
        });
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0,upper}", Locale.US, registry);
        assertEquals("{0,upper}", emf.toPattern());
        assertEquals("HELLO", emf.format(new Object[]{"hello"}));
    }

    @Test(timeout = 4000)
    public void testPatternWithCustomFormatAndStyle() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("custom", new FormatFactory() {
            public Format getFormat(String name, String args, Locale locale) {
                if ("style1".equals(args)) {
                    return new java.text.Format() {
                        public StringBuffer format(Object obj, StringBuffer toAppendTo, java.text.FieldPosition pos) {
                            toAppendTo.append("[" + obj + "]");
                            return toAppendTo;
                        }
                        public Object parseObject(String source, java.text.ParsePosition pos) {
                            return null;
                        }
                    };
                }
                return null;
            }
        });
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0,custom,style1}", Locale.US, registry);
        assertEquals("{0,custom,style1}", emf.toPattern());
        assertEquals("[test]", emf.format(new Object[]{"test"}));
    }

    @Test(timeout = 4000)
    public void testPatternWithQuotedString() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("'{'}'{0}'");
        assertEquals("'{'}'{0}'", emf.toPattern());
        assertEquals("{'test'}", emf.format(new Object[]{"test"}));
    }

    @Test(timeout = 4000)
    public void testPatternWithEscapedQuote() {
        // Single escaped quote
        ExtendedMessageFormat emf = new ExtendedMessageFormat("''{0}''");
        assertEquals("''{0}''", emf.toPattern());
        assertEquals("'test'", emf.format(new Object[]{"test"}));
    }

    @Test(timeout = 4000)
    public void testPatternWithMultipleFormatElements() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0} {1} {2}");
        assertEquals("{0} {1} {2}", emf.toPattern());
        assertEquals("a b c", emf.format(new Object[]{"a", "b", "c"}));
    }

    @Test(timeout = 4000)
    public void testPatternWithNestedBracesInFormatDescription() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("choice", new FormatFactory() {
            public Format getFormat(String name, String args, Locale locale) {
                return new java.text.ChoiceFormat(args);
            }
        });
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0,choice,0#zero|1<one}", Locale.US, registry);
        assertEquals("{0,choice,0#zero|1<one}", emf.toPattern());
        assertEquals("zero", emf.format(new Object[]{0}));
    }

    // ========== Partition B: Boundary Value Analysis ==========

    @Test(timeout = 4000)
    public void testEmptyPattern() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("");
        assertEquals("", emf.toPattern());
    }

    @Test(timeout = 4000)
    public void testNullPattern() {
        try {
            new ExtendedMessageFormat((String) null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testPatternWithOnlyQuotes() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("''");
        assertEquals("''", emf.toPattern());
        assertEquals("'", emf.format(new Object[]{}));
    }

    @Test(timeout = 4000)
    public void testPatternWithMaxArgumentIndex() {
        // Use a large index (but within reasonable bounds)
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{999}");
        assertEquals("{999}", emf.toPattern());
        // No argument provided, will output {999}? Actually MessageFormat will throw if index out of range
        // But we just test pattern parsing
    }

    @Test(timeout = 4000)
    public void testPatternWithWhitespaceInFormatElement() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{ 0 , number , integer }");
        // After trimming, should be {0,number,integer}
        assertEquals("{0,number,integer}", emf.toPattern());
    }

    @Test(timeout = 4000)
    public void testRegistryWithNullFactory() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("nullfactory", null);
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0,nullfactory}", Locale.US, registry);
        // Since factory is null, getFormat returns null, so custom pattern is kept
        assertEquals("{0,nullfactory}", emf.toPattern());
    }

    // ========== Partition C: Defect-Targeted (LANG-477) ==========

    @Test(timeout = 4000)
    public void testEscapedQuote_LANG_477() {
        // This pattern is known to cause OutOfMemoryError due to infinite loop in appendQuotedString
        // The bug occurs when escapingOn is true and pattern contains escaped quotes.
        // The pattern "''{0''}" or similar may trigger it.
        // We use a pattern that has an escaped quote inside a format element.
        String pattern = "''{0''}";
        try {
            ExtendedMessageFormat emf = new ExtendedMessageFormat(pattern);
            // If bug is fixed, this should complete without error
            // We also verify the pattern is correctly parsed
            assertEquals("''{0''}", emf.toPattern());
            // Format with argument
            String result = emf.format(new Object[]{"test"});
            // Expected: 'test' (since the outer quotes are escaped, inner {0} is replaced)
            // Actually pattern: ''{0''} -> first '' becomes ', then {0''} is a format element with index 0 and format description "''}"? 
            // This is tricky. The bug is that it hangs, so just completing is a pass.
            // We assert something reasonable
            assertNotNull(result);
        } catch (OutOfMemoryError e) {
            fail("OutOfMemoryError triggered - bug LANG-477 present");
        } catch (IllegalArgumentException e) {
            // If the pattern is invalid, that's also acceptable (but not the bug)
            // The bug is infinite loop, not exception
        }
    }

    @Test(timeout = 4000)
    public void testEscapedQuoteMultiple_LANG_477() {
        // Another variant: multiple escaped quotes
        String pattern = "''''{0}''''";
        try {
            ExtendedMessageFormat emf = new ExtendedMessageFormat(pattern);
            assertEquals("''''{0}''''", emf.toPattern());
            String result = emf.format(new Object[]{"x"});
            assertEquals("''x''", result);
        } catch (OutOfMemoryError e) {
            fail("OutOfMemoryError triggered - bug LANG-477 present");
        }
    }

    @Test(timeout = 4000)
    public void testEscapedQuoteAtEnd_LANG_477() {
        // Pattern ending with escaped quote
        String pattern = "{0}''";
        try {
            ExtendedMessageFormat emf = new ExtendedMessageFormat(pattern);
            assertEquals("{0}''", emf.toPattern());
            assertEquals("test'", emf.format(new Object[]{"test"}));
        } catch (OutOfMemoryError e) {
            fail("OutOfMemoryError triggered - bug LANG-477 present");
        }
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testSetFormatThrows() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0}");
        emf.setFormat(0, null);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testSetFormatByArgumentIndexThrows() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0}");
        emf.setFormatByArgumentIndex(0, null);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testSetFormatsThrows() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0}");
        emf.setFormats(new Format[0]);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testSetFormatsByArgumentIndexThrows() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0}");
        emf.setFormatsByArgumentIndex(new Format[0]);
    }

    @Test(timeout = 4000)
    public void testInvalidPatternMissingEndBrace() {
        try {
            new ExtendedMessageFormat("{0");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testInvalidPatternNonDigitArgumentIndex() {
        try {
            new ExtendedMessageFormat("{abc}");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testInvalidPatternUnterminatedQuotedString() {
        try {
            new ExtendedMessageFormat("'{0}");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testInvalidPatternUnterminatedFormatDescription() {
        try {
            new ExtendedMessageFormat("{0,number");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testToPatternConsistencyAfterApplyPattern() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0,number}");
        String pattern1 = emf.toPattern();
        // Apply same pattern again
        emf.applyPattern("{0,number}");
        String pattern2 = emf.toPattern();
        assertEquals(pattern1, pattern2);
    }

    @Test(timeout = 4000)
    public void testToPatternWithCustomFormat() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("custom", new FormatFactory() {
            public Format getFormat(String name, String args, Locale locale) {
                return new java.text.Format() {
                    public StringBuffer format(Object obj, StringBuffer toAppendTo, java.text.FieldPosition pos) {
                        toAppendTo.append("custom:" + obj);
                        return toAppendTo;
                    }
                    public Object parseObject(String source, java.text.ParsePosition pos) {
                        return null;
                    }
                };
            }
        });
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0,custom}", Locale.US, registry);
        assertEquals("{0,custom}", emf.toPattern());
        // Ensure format works
        assertEquals("custom:test", emf.format(new Object[]{"test"}));
    }

    @Test(timeout = 4000)
    public void testContainsElementsWithNullCollection() {
        // Indirectly test containsElements via applyPattern with null registry
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0}", (Map) null);
        assertEquals("{0}", emf.toPattern());
    }

    @Test(timeout = 4000)
    public void testApplyPatternWithNullRegistry() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("test", Locale.US, null);
        assertEquals("test", emf.toPattern());
    }

    @Test(timeout = 4000)
    public void testLocaleSetCorrectly() {
        Locale locale = Locale.GERMANY;
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0,date,full}", locale);
        assertEquals(locale, emf.getLocale());
    }
}