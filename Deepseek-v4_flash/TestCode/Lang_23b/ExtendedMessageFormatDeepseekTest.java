package org.apache.commons.lang3.text;

import org.junit.Test;
import static org.junit.Assert.*;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * White-box JUnit 4 test suite for ExtendedMessageFormat.
 * Targets the known defect: registry not considered in equals/hashCode (Defects4J).
 * Achieves high line and branch coverage with systematic partitioning.
 */
public class ExtendedMessageFormatDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Partitions:
     * A: Core functional logic (applyPattern, toPattern, getFormat, insertFormats)
     * B: Boundary/edge cases (null, empty, extreme indices, escaped quotes)
     * C: Defect-targeted: equals/hashCode and registry interaction
     * D: Exception paths (invalid patterns, unsupported setFormat)
     * E: Object lifecycle (constructor variations)
     *
     * Key decision branches in source:
     * - registry == null → super.applyPattern
     * - c[pos] == QUOTE → appendQuotedString (escapingOn=true/false)
     * - c[pos] == START_FE → readArgumentIndex, parseFormatDescription
     * - depth increments in parseFormatDescription
     * - containsElements checks null/size/elements
     * - seekNonWs uses StrMatcher.splitMatcher()
     * - insertFormats loops with depth tracking
     * - readArgumentIndex: whitespace, digit check, NumberFormatException
     * - getFormat: indexOf(START_FMT), factory lookup
     * - Defect: hashCode/equals not overridden for registry
     */

    //------------------ Helper FormatFactory for tests ------------------
    private static final FormatFactory UPPER_CASE_FACTORY = new FormatFactory() {
        @Override
        public Format getFormat(String name, String arguments, Locale locale) {
            return new Format() {
                private static final long serialVersionUID = 1L;
                @Override
                public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                    if (obj instanceof String) {
                        toAppendTo.append(((String) obj).toUpperCase(locale));
                    } else {
                        toAppendTo.append(String.valueOf(obj));
                    }
                    return toAppendTo;
                }
                @Override
                public Object parseObject(String source, ParsePosition pos) {
                    return source;
                }
            };
        }
    };

    private static final FormatFactory DATE_FACTORY = new FormatFactory() {
        @Override
        public Format getFormat(String name, String arguments, Locale locale) {
            if (arguments == null) {
                return new SimpleDateFormat("yyyy-MM-dd", locale);
            }
            return new SimpleDateFormat(arguments, locale);
        }
    };

    //------------------ Partition A: Core Functional Logic ------------------

    @Test(timeout = 4000)
    public void testSimplePatternNoRegistry() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("Hello {0}!");
        assertEquals("Hello {0}!", emf.toPattern());
        assertEquals("Hello World!", emf.format(new Object[]{"World"}));
    }

    @Test(timeout = 4000)
    public void testPatternWithCustomFormat() throws Exception {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("upper", UPPER_CASE_FACTORY);
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0, upper}", registry);
        assertEquals("{0, upper}", emf.toPattern());
        assertEquals("HELLO", emf.format(new Object[]{"hello"}));
    }

    @Test(timeout = 4000)
    public void testPatternWithCustomFormatAndStyle() throws Exception {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("date", DATE_FACTORY);
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0, date, yyyy-MM-dd}", registry);
        assertEquals("{0, date, yyyy-MM-dd}", emf.toPattern());
        Date date = new SimpleDateFormat("yyyy-MM-dd").parse("2023-01-15");
        assertEquals("2023-01-15", emf.format(new Object[]{date}));
    }

    @Test(timeout = 4000)
    public void testApplyPatternWithoutRegistryDoesNotOverrideToPattern() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0}");
        assertEquals("{0}", emf.toPattern());
    }

    //------------------ Partition B: Boundary Value Analysis ------------------

    @Test(timeout = 4000)
    public void testEmptyPattern() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("");
        assertEquals("", emf.toPattern());
        assertEquals("", emf.format(new Object[]{}));
    }

    @Test(timeout = 4000)
    public void testNullRegistryDoesNotImpactPattern() {
        ExtendedMessageFormat emf1 = new ExtendedMessageFormat("{0}", Locale.US, null);
        ExtendedMessageFormat emf2 = new ExtendedMessageFormat("{0}", Locale.US, null);
        assertEquals(emf1.toPattern(), emf2.toPattern());
    }

    @Test(timeout = 4000)
    public void testEscapedQuotes() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("upper", UPPER_CASE_FACTORY);
        ExtendedMessageFormat emf = new ExtendedMessageFormat("'{'0, upper}'", registry);
        assertEquals("'{'0, upper}'", emf.toPattern());
        assertEquals("{0, upper}", emf.format(new Object[]{}));
    }

    @Test(timeout = 4000)
    public void testDoubleQuoteEscape() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("''{0}''");
        assertEquals("''{0}''", emf.toPattern());
        assertEquals("'value'", emf.format(new Object[]{"value"}));
    }

    @Test(timeout = 4000)
    public void testWhitespaceInPattern() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0 , number , integer}");
        assertEquals("{0, number, integer}", emf.toPattern());
    }

    @Test(timeout = 4000)
    public void testMultipleFormatElements() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("upper", UPPER_CASE_FACTORY);
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0, upper} {1}", registry);
        assertEquals("{0, upper} {1}", emf.toPattern());
        assertEquals("HELLO WORLD", emf.format(new Object[]{"hello", "WORLD"}));
    }

    //------------------ Partition C: Defect-Targeted Branch Zone ------------------

    /**
     * Directly targets the known defect: registry not considered in equals/hashCode.
     * Two ExtendedMessageFormat objects with same pattern but different registries
     * should NOT be equal (and thus may have different hashCodes).
     * The bug caused assertion failure in testEqualsHashcode.
     */
    @Test(timeout = 4000)
    public void testEqualsAndHashCodeWithDifferentRegistries() {
        Map<String, FormatFactory> reg1 = new HashMap<String, FormatFactory>();
        reg1.put("upper", UPPER_CASE_FACTORY);
        Map<String, FormatFactory> reg2 = new HashMap<String, FormatFactory>();
        reg2.put("upper", UPPER_CASE_FACTORY); // same content but different map object

        ExtendedMessageFormat emf1 = new ExtendedMessageFormat("{0, upper}", reg1);
        ExtendedMessageFormat emf2 = new ExtendedMessageFormat("{0, upper}", reg2);

        // They should be equal if registry content is the same? Actually registry is part of state.
        // MessageFormat's equals() does not consider registry -> bug: they are not equal but equals might return true.
        // We assert that they are NOT equal because registry objects differ (different maps).
        // The defect is that equals returns true incorrectly (or hashCode inconsistent).
        // We assume the intended behavior is that equality depends on registry.
        // So we assert that two objects with different registry (different map) are NOT equal.
        assertFalse("ExtendedMessageFormat with different registry maps should not be equal",
                emf1.equals(emf2));
        // Also assert that hashCode differs (must be consistent with equals)
        assertNotEquals("hashCode must be consistent with equals",
                emf1.hashCode(), emf2.hashCode());
    }

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeWithNullRegistry() {
        ExtendedMessageFormat emf1 = new ExtendedMessageFormat("{0}");
        ExtendedMessageFormat emf2 = new ExtendedMessageFormat("{0}");
        // Same pattern, no registry -> should be equal (MessageFormat equality)
        assertTrue("Identical patterns with null registry should be equal", emf1.equals(emf2));
        assertEquals("hashCode must match for equal objects", emf1.hashCode(), emf2.hashCode());
    }

    //------------------ Partition D: Exception & Defensive Guard Paths ------------------

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testSetFormatThrows() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0}");
        emf.setFormat(0, new SimpleDateFormat());
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testSetFormatByArgumentIndexThrows() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0}");
        emf.setFormatByArgumentIndex(0, new SimpleDateFormat());
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testSetFormatsThrows() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0}");
        emf.setFormats(new Format[]{new SimpleDateFormat()});
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testSetFormatsByArgumentIndexThrows() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0}");
        emf.setFormatsByArgumentIndex(new Format[]{new SimpleDateFormat()});
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInvalidPatternMissingClosingBrace() {
        new ExtendedMessageFormat("{0");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInvalidArgumentIndexNonNumeric() {
        new ExtendedMessageFormat("{abc}");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testUnterminatedQuotedString() {
        new ExtendedMessageFormat("'{0}");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testUnterminatedFormatElement() {
        new ExtendedMessageFormat("{0, number");
    }

    //------------------ Partition E: Object Lifecycle & Contract ------------------

    @Test(timeout = 4000)
    public void testConstructorWithLocale() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0, date}", Locale.US);
        // Should work without registry
        assertEquals("{0, date}", emf.toPattern());
    }

    @Test(timeout = 4000)
    public void testConstructorWithDefaultLocale() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0, date}", Locale.getDefault());
        assertEquals("{0, date}", emf.toPattern());
    }

    @Test(timeout = 4000)
    public void testContainsElementsEdgeCases() {
        // Indirectly test via getFormat and insertFormats
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("upper", UPPER_CASE_FACTORY);
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0, upper} {1, upper}", registry);
        assertEquals("UPPER UPPER", emf.format(new Object[]{"upper", "upper"}));
    }

    @Test(timeout = 4000)
    public void testFormatNotFoundFallsThrough() {
        // when factory returns null, formatDescription is not inserted into stripCustom
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("unknown", new FormatFactory() {
            @Override
            public Format getFormat(String name, String arguments, Locale locale) {
                return null; // simulate unknown
            }
        });
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0, unknown, style}", registry);
        // pattern should retain the custom part because format is null
        assertEquals("{0, unknown, style}", emf.toPattern());
    }

    @Test(timeout = 4000)
    public void testAppendQuotedStringEscapingOn() {
        // This indirectly tests the appendQuotedString path
        ExtendedMessageFormat emf = new ExtendedMessageFormat("''{0}''");
        assertEquals("'value'", emf.format(new Object[]{"value"}));
    }

    @Test(timeout = 4000)
    public void testGetQuotedStringWithoutEscaping() {
        // Directly test private method via public behavior? Not possible.
        // But we can test that quoted strings are consumed correctly.
        ExtendedMessageFormat emf = new ExtendedMessageFormat("'{0}'");
        assertEquals("{0}", emf.toPattern());
        assertEquals("{0}", emf.format(new Object[]{"ignored"}));
    }

    // Additional branch: readArgumentIndex with whitespace and correct termination
    @Test(timeout = 4000)
    public void testReadArgumentIndexWithWhitespace() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{ 0 }");
        assertEquals("{0}", emf.toPattern());
    }

    // Branch: containsElements returns false when all null
    @Test(timeout = 4000)
    public void testContainsElementsAllNullReturnsFalse() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        // Use factory that returns null for all patterns -> descriptions are all null
        registry.put("nullFactory", new FormatFactory() {
            @Override
            public Format getFormat(String name, String arguments, Locale locale) {
                return null;
            }
        });
        // Pattern with two elements, both will get null format -> foundFormats contains nulls
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0, nullFactory} {1, nullFactory}", registry);
        // No custom formats inserted, pattern unchanged
        assertEquals("{0, nullFactory} {1, nullFactory}", emf.toPattern());
    }
}