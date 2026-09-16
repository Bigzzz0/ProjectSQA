/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.google.gson.internal.bind.TypeAdapters
 *
 * Branches & Edge Conditions Targeted:
 * 1. TypeAdapters private constructor invocation via reflection (asserting UnsupportedOperationException).
 * 2. CLASS: serialize non-null Class (UnsupportedOperationException), serialize null, deserialize null, deserialize non-null (UnsupportedOperationException).
 * 3. BIT_SET: null handling, empty array, mixed types (NUMBER, BOOLEAN, STRING "0"/"1", invalid STRING format -> JsonSyntaxException, invalid token -> JsonSyntaxException).
 * 4. BOOLEAN & BOOLEAN_AS_STRING: boolean primitives, string coercion ("true", "false", invalid string), null tokens, write null vs value.
 * 5. BYTE, SHORT, INTEGER, LONG, FLOAT, DOUBLE: null tokens, valid numeric strings/numbers, overflow/parse errors -> JsonSyntaxException.
 * 6. NUMBER [CRITICAL DEFECT TARGET]:
 *    - Defects4J Ground Truth: PrimitiveTest.testNumberAsStringDeserialization -> Expecting number, got: STRING.
 *    - Number type adapter deserialization of String token (e.g., "\"123.45\"") asserting correct parsed Number instance without crashing.
 * 7. ATOMIC_INTEGER, ATOMIC_BOOLEAN, ATOMIC_INTEGER_ARRAY: null safety, empty array, valid elements, parse error handling.
 * 8. CHARACTER: single char, empty or multi-char strings -> JsonSyntaxException, null.
 * 9. STRING, BIG_DECIMAL, BIG_INTEGER: boolean coercion in STRING, null handling, valid and invalid numeric strings.
 * 10. STRING_BUILDER, STRING_BUFFER: null tokens and valid strings.
 * 11. URL, URI: null tokens, literal "null", invalid syntax for URI (JsonIOException).
 * 12. INET_ADDRESS: null tokens, valid IP/hostnames, serialization checks.
 * 13. UUID, CURRENCY: null tokens, valid UUID strings, ISO-4217 currencies.
 * 14. TIMESTAMP_FACTORY: null handling, date conversion delegation, non-matching type tokens.
 * 15. CALENDAR: fields ordering, missing fields, null tokens, serialization verification.
 * 16. LOCALE: tokens (language, language_country, language_country_variant), null tokens.
 * 17. JSON_ELEMENT: recursive deserialization (Primitive string/number/boolean, Null, Array, Object) and invalid tokens; write coverage.
 * 18. ENUM_FACTORY: Enum with and without @SerializedName, alternate names, anonymous enum subclasses, null tokens.
 * 19. Factories: newFactory, newFactoryForMultipleTypes, newTypeHierarchyFactory (matching type, sub-type, mismatch -> JsonSyntaxException).
 */

package com.google.gson.internal.bind;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.Json