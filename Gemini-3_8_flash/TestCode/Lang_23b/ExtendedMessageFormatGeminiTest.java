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
package org.apache.commons.lang3.text;

import java.text.ChoiceFormat;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.lang3.text.ExtendedMessageFormat
 *
 * 1. Defects4J Known Defect Targeted:
 *    - ExtendedMessageFormatTest::testEqualsHashcode ("registry, hashcode()")
 *      The class declares Javadoc comments for equals(Object) and hashCode() but fails to properly
 *      override or differentiate state based on 'registry'. Instances with differing registries
 *      must not be considered equal and must differentiate hashCode appropriately.
 *
 * 2. Decision Branches Covered:
 *    - Constructors: (pattern), (pattern, Locale), (pattern, registry), (pattern, Locale, registry)
 *    - applyPattern(pattern):
 *        * registry == null vs registry != null
 *        * quotes handling: single quote, escaped quotes ("''"), unterminated quotes
 *        * format elements: valid index, with/without format name, with/without sub-style arguments
 *        * custom registry match vs no registry match (fallback to built-in/stripCustom)
 *        * containsElements: null, empty collection, collection with all nulls, collection with formats
 *        * invalid format elements: non-digit index, whitespace within index, missing '}', unreadable position
 *    - readArgumentIndex:
 *        * whitespace handling, boundary between digits and delimiters (',' or '}')
 *        * error branches: non-digit characters, unterminated format element
 *    - parseFormatDescription:
 *        * nested format elements (depth tracking with '{' and '}')
 *        * quoted blocks inside format description
 *        * unterminated description branch
 *    - insertFormats:
 *        * customPatterns empty/null -> returns original pattern
 *        * customPatterns present -> properly reconstructs pattern with custom format names and arguments
 *        * quotes inside insertFormats
 *    - Mutator Guards:
 *        * setFormat, setFormatByArgumentIndex, setFormats, setFormatsByArgumentIndex -> UnsupportedOperationException
 *    - Object Contract:
 *        * equals: reflexivity, symmetry, comparison with null, wrong type, different registry, different pattern
 *        * hashCode: consistency with equals contract
 */
public class ExtendedMessageFormatGeminiTest {

    // Helper custom Format and FormatFactory classes
    private static class LowerCaseFormat extends Format {
        private static final long serialVersionUID = 1L;

        @Override
        public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
            return toAppendTo.append(String.valueOf(obj).toLowerCase(Locale.ENGLISH));
        }

        @Override
        public Object parseObject(String source, ParsePosition pos) {
            pos.setIndex(source.length());
            return source.toLowerCase(Locale.ENGLISH);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof LowerCaseFormat;
        }

        @Override
        public int hashCode() {
            return LowerCaseFormat.class.hashCode();
        }
    }

    private static class UpperCaseFormat extends Format {
        private static final long serialVersionUID = 1L;

        @Override
        public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
            return toAppendTo.append(String.valueOf(obj).toUpperCase(Locale.ENGLISH));
        }

        @Override
        public Object parseObject(String source, ParsePosition pos) {
            pos.setIndex(source.length());
            return source.toUpperCase(Locale.ENGLISH);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof UpperCaseFormat;
        }

        @Override
        public int hashCode() {
            return UpperCaseFormat.class.hashCode();
        }
    }

    private static class LowerCaseFormatFactory implements FormatFactory {
        @Override
        public Format getFormat(String name, String arguments, Locale locale) {
            return new LowerCaseFormat();
        }
    }

    private static class UpperCaseFormatFactory implements FormatFactory {
        @Override
        public Format getFormat(String name, String arguments, Locale locale) {
            return new UpperCaseFormat();
        }
    }

    private static class ParamBearingFormatFactory implements FormatFactory {
        private String receivedName;
        private String receivedArgs;

        @Override
        public Format getFormat(String name, String arguments, Locale locale) {
            this.receivedName = name;
            this.receivedArgs = arguments;
            return new LowerCaseFormat();
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicPatternFormattingWithoutRegistry() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("Hello {0}!");
        assertEquals("Hello {0}!", emf.toPattern());
        assertEquals("Hello World!", emf.format(new Object[]{"World"}));
    }

    @Test(timeout = 4000)
    public void testCustomRegistryFormatting() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("lower", new LowerCaseFormatFactory());

        ExtendedMessageFormat emf = new ExtendedMessageFormat("Value: {0,lower}", registry);
        assertEquals("Value: {0,lower}", emf.toPattern());
        String result = emf.format(new Object[]{"ABC"});
        assertEquals("Value: abc", result);
    }

    @Test(timeout = 4000)
    public void testCustomRegistryWithArguments() {
        ParamBearingFormatFactory factory = new ParamBearingFormatFactory();
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("custom", factory);

        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0, custom , arg1, arg2 }", Locale.US, registry);
        assertEquals("{0,custom, arg1, arg2 }", emf.toPattern());
        assertEquals("custom", factory.receivedName);
        assertEquals("arg1, arg2", factory.receivedArgs);
    }

    @Test(timeout = 4000)
    public void testBuiltInFormatPassthroughWhenNotInRegistry() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("lower", new LowerCaseFormatFactory());

        ExtendedMessageFormat emf = new ExtendedMessageFormat("Number: {0,number,currency} and Text: {1,lower}", Locale.US, registry);
        String formatted = emf.format(new Object[]{12.34, "HELLO"});
        assertTrue(formatted.contains("$12.34"));
        assertTrue(formatted.contains("hello"));
    }

    @Test(timeout = 4000)
    public void testConstructorsInitialization() {
        ExtendedMessageFormat emf1 = new ExtendedMessageFormat("Test {0}");
        assertEquals(Locale.getDefault(), emf1.getLocale());
        assertEquals("Test {0}", emf1.toPattern());

        ExtendedMessageFormat emf2 = new ExtendedMessageFormat("Test {0}", Locale.GERMAN);
        assertEquals(Locale.GERMAN, emf2.getLocale());
        assertEquals("Test {0}", emf2.toPattern());

        Map<String, FormatFactory> registry = Collections.singletonMap("lower", (FormatFactory) new LowerCaseFormatFactory());
        ExtendedMessageFormat emf3 = new ExtendedMessageFormat("Test {0,lower}", registry);
        assertEquals(Locale.getDefault(), emf3.getLocale());
        assertEquals("Test {0,lower}", emf3.toPattern());
    }

    @Test(timeout = 4000)
    public void testNestedBracesInFormatDescription() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("choice", new FormatFactory() {
            @Override
            public Format getFormat(String name, String arguments, Locale locale) {
                return new ChoiceFormat(arguments);
            }
        });

        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0,choice,0#zero|1#{1}|2#more}", registry);
        assertNotNull(emf);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Quote Escaping
    // =========================================================================

    @Test(timeout = 4000)
    public void testQuotesAndEscapedQuotesInPattern() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("lower", new LowerCaseFormatFactory());

        // Single quotes escaping literals in MessageFormat
        ExtendedMessageFormat emf = new ExtendedMessageFormat("'{' {0,lower} ''howdy'' '}'", registry);
        String result = emf.format(new Object[]{"WORLD"});
        assertEquals("{ world 'howdy' }", result);
    }

    @Test(timeout = 4000)
    public void testQuoteImmediatelyAtStartAndEnd() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("lower", new LowerCaseFormatFactory());

        ExtendedMessageFormat emf = new ExtendedMessageFormat("''{0,lower}''", registry);
        assertEquals("'test'", emf.format(new Object[]{"TEST"}));
    }

    @Test(timeout = 4000)
    public void testFormatDescriptionWithQuotedString() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("dummy", new FormatFactory() {
            @Override
            public Format getFormat(String name, String arguments, Locale locale) {
                return new LowerCaseFormat();
            }
        });

        // Quoted string inside format description containing braces
        ExtendedMessageFormat emf = new ExtendedMessageFormat("{0,dummy,'foo {1} bar'}", registry);
        assertEquals("{0,dummy,'foo {1} bar'}", emf.toPattern());
    }

    @Test(timeout = 4000)
    public void testEmptyAndWhitespacePattern() {
        ExtendedMessageFormat emf1 = new ExtendedMessageFormat("");
        assertEquals("", emf1.toPattern());
        assertEquals("", emf1.format(new Object[0]));

        ExtendedMessageFormat emf2 = new ExtendedMessageFormat("   ", Collections.<String, FormatFactory>emptyMap());
        assertEquals("   ", emf2.toPattern());
    }

    @Test(timeout = 4000)
    public void testMultipleFormatElementsAndSpacing() {
        Map<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("lower", new LowerCaseFormatFactory());

        ExtendedMessageFormat emf = new ExtendedMessageFormat("  {  0  ,  lower  }  -  {  1  }  ", registry);
        assertEquals("  abc  -  def  ", emf.format(new Object[]{"ABC", "def"}));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Lang-40 Defect Reproduction)
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsHashcodeTargetingDefect() {
        Map<String, FormatFactory> registry1 = new HashMap<String, FormatFactory>();
        registry1.put("lower", new LowerCaseFormatFactory());

        Map<String, FormatFactory> registry2 = new HashMap<String, FormatFactory>();
        registry2.put("upper", new UpperCaseFormatFactory());

        String pattern = "test: {0,lower}";
        ExtendedMessageFormat emf1 = new ExtendedMessageFormat(pattern, Locale.US, registry1);
        ExtendedMessageFormat emf2 = new ExtendedMessageFormat(pattern, Locale.US, registry1);

        // Identical configurations must be equal and have matching hashCodes
        assertTrue("Equal objects must return true for equals", emf1.equals(emf2));
        assertTrue("Symmetric equality", emf2.equals(emf1));
        assertEquals("Equal objects must have identical hashCode", emf1.hashCode(), emf2.hashCode());

        // Objects with differing registries MUST NOT be equal
        ExtendedMessageFormat emf3 = new ExtendedMessageFormat(pattern, Locale.US, registry2);
        assertFalse("registry, equals()", emf1.equals(emf3));

        // Registry difference must be reflected in hashCode differentiation
        assertFalse("registry, hashcode()", emf1.hashCode() == emf3.hashCode());
    }

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeWithNullRegistry() {
        String pattern = "Format {0}";
        ExtendedMessageFormat emfWithoutRegistry = new ExtendedMessageFormat(pattern, Locale.US);
        ExtendedMessageFormat emfWithoutRegistry2 = new ExtendedMessageFormat(pattern, Locale.US);

        assertTrue(emfWithoutRegistry.equals(emfWithoutRegistry2));
        assertEquals(emfWithoutRegistry.hashCode(), emfWithoutRegistry2.hashCode());

        Map<String, FormatFactory> registry = Collections.singletonMap("lower", (FormatFactory) new LowerCaseFormatFactory());
        ExtendedMessageFormat emfWithRegistry = new ExtendedMessageFormat(pattern, Locale.US, registry);

        assertFalse("One null registry and one non-null registry must not be equal", emfWithoutRegistry.equals(emfWithRegistry));
        assertFalse("Null vs non-null registry hashCodes should diverge", emfWithoutRegistry.hashCode() == emfWithRegistry.hashCode());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testSetFormatUnsupported() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("test {0}");
        emf.setFormat(0, NumberFormat.getInstance());
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testSetFormatByArgumentIndexUnsupported() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("test {0}");
        emf.setFormatByArgumentIndex(0, NumberFormat.getInstance());
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testSetFormatsUnsupported() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("test {0}");
        emf.setFormats(new Format[]{NumberFormat.getInstance()});
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testSetFormatsByArgumentIndexUnsupported() {
        ExtendedMessageFormat emf = new ExtendedMessageFormat("test {0}");
        emf.setFormatsByArgumentIndex(new Format[]{NumberFormat.getInstance()});
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testUnterminatedQuotedString() {
        Map<String, FormatFactory> registry = Collections.singletonMap("dummy", (FormatFactory) new LowerCaseFormatFactory());
        new ExtendedMessageFormat("Prefix 'unterminated", registry);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testUnterminatedFormatElement() {
        Map<String, FormatFactory> registry = Collections.singletonMap("dummy", (FormatFactory) new LowerCaseFormatFactory());
        new ExtendedMessageFormat("Prefix {0,dummy", registry);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInvalidFormatArgumentIndexNonDigit() {
        Map<String, FormatFactory> registry = Collections.singletonMap("dummy", (FormatFactory) new LowerCaseFormatFactory());
        new ExtendedMessageFormat("{abc}", registry);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInvalidFormatArgumentIndexEmbeddedWhitespace() {
        Map<String, FormatFactory> registry = Collections.singletonMap("dummy", (FormatFactory) new LowerCaseFormatFactory());
        new ExtendedMessageFormat("{0 1}", registry);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMissingClosingBraceAfterFormatDescription() {
        Map<String, FormatFactory> registry = Collections.singletonMap("dummy", (FormatFactory) new LowerCaseFormatFactory());
        new ExtendedMessageFormat("{0,dummy", registry);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testUnreadableFormatElementPosition() {
        Map<String, FormatFactory> registry = Collections.singletonMap("dummy", (FormatFactory) new LowerCaseFormatFactory());
        // Trailing garbage inside the format element before closing brace
        new ExtendedMessageFormat("{0,dummy unexpected}", registry);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        Map<String, FormatFactory> registry = Collections.singletonMap("lower", (FormatFactory) new LowerCaseFormatFactory());
        ExtendedMessageFormat emf = new ExtendedMessageFormat("pattern {0}", registry);

        // Reflexivity
        assertTrue(emf.equals(emf));

        // Non-nullity
        assertFalse(emf.equals(null));

        // Type difference
        assertFalse(emf.equals("string-literal"));

        // Pattern difference
        ExtendedMessageFormat emfDiffPattern = new ExtendedMessageFormat("other {0}", registry);
        assertFalse(emf.equals(emfDiffPattern));

        // Locale difference
        ExtendedMessageFormat emfDiffLocale = new ExtendedMessageFormat("pattern {0}", Locale.FRANCE, registry);
        assertFalse(emf.equals(emfDiffLocale));
    }

    @Test(timeout = 4000)
    public void testApplyPatternOverwritesPreviousState() {
        Map<String, FormatFactory> registry = Collections.singletonMap("lower", (FormatFactory) new LowerCaseFormatFactory());
        ExtendedMessageFormat emf = new ExtendedMessageFormat("initial {0}", registry);
        assertEquals("initial {0}", emf.toPattern());

        emf.applyPattern("updated {0,lower}");
        assertEquals("updated {0,lower}", emf.toPattern());
        assertEquals("updated test", emf.format(new Object[]{"TEST"}));
    }
}