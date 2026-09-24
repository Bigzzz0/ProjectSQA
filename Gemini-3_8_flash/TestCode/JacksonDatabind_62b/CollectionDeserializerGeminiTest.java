package com.fasterxml.jackson.databind.deser.std;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.UnresolvedForwardReference;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.deser.std.StdValueInstantiator;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.TypeFactory;

/*
 * [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------
 * Defect Target (Defects4J):
 *  - ArrayDelegatorCreatorForCollectionTest::testUnmodifiable
 *    Failure: java.lang.IllegalStateException: No default constructor for [collection type; class
 *    java.util.Collections$UnmodifiableSet, contains [simple type, class java.lang.Object]]
 *    Triggered when CollectionDeserializer encounters an ArrayDelegator creator (e.g. taking a
 *    Collection/List/Array) but ignores canCreateUsingArrayDelegate(), causing fall-through to default
 *    instantiation which fails on unmodifiable collections lacking default constructors.
 *
 * Branches & Decision Points Analyzed:
 *  1. CollectionDeserializer lifecycle & caching:
 *     - isCachable() when _valueDeserializer, _valueTypeDeserializer, and _delegateDeserializer are null
 *       vs when any of them is non-null.
 *     - withResolved() identity return vs new instance return.
 *     - Deprecated withResolved() overload.
 *     - Copy-constructor attribute retention.
 *  2. Delegate Creator Resolution & Execution:
 *     - createContextual: _valueInstantiator != null && canCreateUsingDelegate() == true, but
 *       getDelegateType() == null -> throws IllegalArgumentException.
 *     - deserialize: _delegateDeserializer != null -> instantiator.createUsingDelegate().
 *     - Array delegator creators for unmodifiable collections (Defects4J target).
 *  3. Contextual Configuration & Unwrapping:
 *     - Per-property @JsonFormat(Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY).
 *     - Global DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY enabled vs disabled.
 *     - _unwrapSingle == Boolean.TRUE / Boolean.FALSE.
 *  4. Token Handling & Special Branches:
 *     - START_ARRAY vs non-array (handleNonArray).
 *     - Empty string (VALUE_STRING of length 0) -> instantiator.createFromString().
 *     - VALUE_NULL inside array and inside single value unwrapping.
 *     - Polymorphic values with TypeDeserializer (deserializeWithType).
 *  5. Forward Reference Resolution & Ordering:
 *     - CollectionReferringAccumulator: empty vs populated accumulator.
 *     - resolveForwardReference with known vs unknown reference ID.
 *     - Forward reference encountered when referringAccumulator == null -> JsonMappingException.
 *  6. Exception Handling & Wrapping:
 *     - DeserializationFeature.WRAP_EXCEPTIONS disabled + RuntimeException -> rethrown raw.
 *     - DeserializationFeature.WRAP_EXCEPTIONS enabled -> wrapped in JsonMappingException with path.
 */
public class CollectionDeserializerGeminiTest {

    // =========================================================================
    // Target Defect Mixins & Helper POJOs
    // =========================================================================

    static abstract class UnmodifiableSetMixin {
        @JsonCreator
        public static <E> Set<E> create(Collection<E> elements) {
            return Collections.unmodifiableSet(new HashSet<E>(elements));
        }
    }

    static abstract class UnmodifiableListMixin {
        @JsonCreator
        public static <E> List<E> create(Collection<E> elements) {
            return Collections.unmodifiableList(new ArrayList<E>(elements));
        }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = Dog.class, name = "dog"),
            @JsonSubTypes.Type(value = Cat.class, name = "cat")
    })
    static abstract class Animal {
        public String name;
    }

    static class Dog extends Animal {
        public int barkVolume;
    }

    static class Cat extends Animal {
        public boolean likesMilk;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = IdentifiableItem.class)
    static class IdentifiableItem {
        public int id;
        public String label;

        public IdentifiableItem() {}
        public IdentifiableItem(int id, String label) {
            this.id = id;
            this.label = label;
        }
    }

    static class UnwrappedSingleContainer {
        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        public List<String> entries;
    }

    static class BoomDeserializer extends JsonDeserializer<Object> {
        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) {
            throw new IllegalStateException("Simulated element deserialization failure");
        }
    }

    static class ThrowingForwardRefDeserializer extends JsonDeserializer<Object> {
        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            throw new UnresolvedForwardReference(p, "Simulated unresolved forward ref without reader");
        }
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    @Test(timeout = 4000)
    public void testUnmodifiableSetWithArrayDelegatorCreatorDefect() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Class<?> unmodSetType = Collections.unmodifiableSet(Collections.emptySet()).getClass();
        mapper.addMixIn(unmodSetType, UnmodifiableSetMixin.class);

        JavaType targetType = mapper.getTypeFactory().constructCollectionType(unmodSetType, String.class);
        Set<String> result = mapper.readValue("[\"alpha\", \"beta\", \"gamma\"]", targetType);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains("alpha"));
        assertTrue(result.contains("beta"));
        assertTrue(result.contains("gamma"));
        assertEquals(unmodSetType, result.getClass());
    }

    @Test(timeout = 4000)
    public void testUnmodifiableListWithArrayDelegatorCreatorDefect() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Class<?> unmodListType = Collections.unmodifiableList(Collections.emptyList()).getClass();
        mapper.addMixIn(unmodListType, UnmodifiableListMixin.class);

        JavaType targetType = mapper.getTypeFactory().constructCollectionType(unmodListType, Integer.class);
        List<Integer> result = mapper.readValue("[10, 20, 30]", targetType);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(Integer.valueOf(10), result.get(0));
        assertEquals(Integer.valueOf(20), result.get(1));
        assertEquals(Integer.valueOf(30), result.get(2));
        assertEquals(unmodListType, result.getClass());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testStandardCollectionDeserialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType listType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, String.class);
        List<String> list = mapper.readValue("[\"one\", \"two\", \"three\"]", listType);

        assertNotNull(list);
        assertEquals(3, list.size());
        assertEquals("one", list.get(0));
        assertEquals("two", list.get(1));
        assertEquals("three", list.get(2));
    }

    @Test(timeout = 4000)
    public void testEmptyCollectionDeserialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType setType = mapper.getTypeFactory().constructCollectionType(HashSet.class, Integer.class);
        Set<Integer> set = mapper.readValue("[]", setType);

        assertNotNull(set);
        assertTrue(set.isEmpty());
    }

    @Test(timeout = 4000)
    public void testArrayWithNullElements() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType listType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, String.class);
        List<String> list = mapper.readValue("[\"first\", null, \"third\"]", listType);

        assertNotNull(list);
        assertEquals(3, list.size());
        assertEquals("first", list.get(0));
        assertNull(list.get(1));
        assertEquals("third", list.get(2));
    }

    @Test(timeout = 4000)
    public void testPolymorphicElementDeserialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "[ {\"type\":\"dog\",\"name\":\"Rex\",\"barkVolume\":10}," +
                      "  {\"type\":\"cat\",\"name\":\"Whiskers\",\"likesMilk\":true} ]";
        JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, Animal.class);
        List<Animal> animals = mapper.readValue(json, type);

        assertNotNull(animals);
        assertEquals(2, animals.size());
        assertTrue(animals.get(0) instanceof Dog);
        assertEquals("Rex", animals.get(0).name);
        assertEquals(10, ((Dog) animals.get(0)).barkVolume);
        assertTrue(animals.get(1) instanceof Cat);
        assertEquals("Whiskers", animals.get(1).name);
        assertTrue(((Cat) animals.get(1)).likesMilk);
    }

    @Test(timeout = 4000)
    public void testDeserializeUsingDelegateDeserializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType colType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, String.class);
        ValueInstantiator vi = new StdValueInstantiator(mapper.getDeserializationConfig(), colType) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean canCreateUsingDelegate() { return true; }
            @Override
            public Object createUsingDelegate(DeserializationContext ctxt, Object delegate) {
                ArrayList<Object> res = new ArrayList<Object>();
                res.add("delegate:" + delegate);
                return res;
            }
        };

        JsonDeserializer<Object> delegateDeser = mapper.getDeserializationContext()
                .findRootValueDeserializer(mapper.constructType(String.class));

        CollectionDeserializer deser = new CollectionDeserializer(colType, null, null, vi, delegateDeser, null);
        JsonParser parser = mapper.getFactory().createParser("\"delegatedContent\"");
        parser.nextToken();

        Collection<Object> result = deser.deserialize(parser, mapper.getDeserializationContext());
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("delegate:delegatedContent", result.iterator().next());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testDeserializeEmptyStringUsingCustomInstantiator() throws Exception {
        JavaType colType = TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, String.class);
        ValueInstantiator vi = new StdValueInstantiator(null, colType) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean canCreateFromString() { return true; }
            @Override
            public Object createFromString(DeserializationContext ctxt, String value) {
                ArrayList<Object> list = new ArrayList<Object>();
                list.add("from-empty-string:" + value);
                return list;
            }
        };

        CollectionDeserializer deser = new CollectionDeserializer(colType, null, null, vi);
        ObjectMapper mapper = new ObjectMapper();
        JsonParser parser = mapper.getFactory().createParser("\"\"");
        parser.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        Collection<Object> res = deser.deserialize(parser, ctxt);
        assertNotNull(res);
        assertEquals(1, res.size());
        assertEquals("from-empty-string:", res.iterator().next());
    }

    @Test(timeout = 4000)
    public void testAcceptSingleValueAsArrayGlobally() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, String.class);

        List<String> list = mapper.readValue("\"standaloneValue\"", type);
        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("standaloneValue", list.get(0));
    }

    @Test(timeout = 4000)
    public void testAcceptSingleValueAsArrayDisabledFails() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, String.class);

        try {
            mapper.readValue("\"standaloneValue\"", type);
            fail("Expected JsonMappingException because ACCEPT_SINGLE_VALUE_AS_ARRAY is disabled");
        } catch (JsonMappingException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testPerPropertyAcceptSingleValueAsArray() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

        UnwrappedSingleContainer container = mapper.readValue("{\"entries\": \"singleEntry\"}", UnwrappedSingleContainer.class);
        assertNotNull(container);
        assertNotNull(container.entries);
        assertEquals(1, container.entries.size());
        assertEquals("singleEntry", container.entries.get(0));
    }

    @Test(timeout = 4000)
    public void testHandleNonArrayWithNullTokenExplicitUnwrap() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType colType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, String.class);
        CollectionDeserializer deser = new CollectionDeserializer(
                colType,
                mapper.getDeserializationContext().findRootValueDeserializer(mapper.constructType(String.class)),
                null,
                new StdValueInstantiator(mapper.getDeserializationConfig(), colType),
                null,
                Boolean.TRUE);

        JsonParser parser = mapper.getFactory().createParser("null");
        parser.nextToken();
        List<Object> target = new ArrayList<Object>();
        deser.handleNonArray(parser, mapper.getDeserializationContext(), target);

        assertEquals(1, target.size());
        assertNull(target.get(0));
    }

    @Test(timeout = 4000)
    public void testPolymorphicElementUnwrappedSingle() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        String json = "{\"type\":\"dog\",\"name\":\"Rex\",\"barkVolume\":7}";
        JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, Animal.class);

        List<Animal> animals = mapper.readValue(json, type);
        assertNotNull(animals);
        assertEquals(1, animals.size());
        assertTrue(animals.get(0) instanceof Dog);
        assertEquals("Rex", animals.get(0).name);
        assertEquals(7, ((Dog) animals.get(0)).barkVolume);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateContextualThrowsWhenDelegateTypeIsNull() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType colType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, String.class);
        ValueInstantiator vi = new StdValueInstantiator(mapper.getDeserializationConfig(), colType) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean canCreateUsingDelegate() { return true; }
            @Override
            public JavaType getDelegateType(DeserializationConfig config) { return null; }
        };

        CollectionDeserializer deser = new CollectionDeserializer(colType, null, null, vi);
        try {
            deser.createContextual(mapper.getDeserializationContext(), null);
            fail("Expected IllegalArgumentException when delegate creator type is null");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Invalid delegate-creator definition"));
        }
    }

    @Test(timeout = 4000)
    public void testWrapExceptionsDisabledThrowsRawRuntimeException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.WRAP_EXCEPTIONS);
        SimpleModule module = new SimpleModule();
        module.addDeserializer(String.class, new BoomDeserializer());
        mapper.registerModule(module);

        JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, String.class);
        try {
            mapper.readValue("[\"trigger\"]", type);
            fail("Expected raw IllegalStateException when WRAP_EXCEPTIONS is disabled");
        } catch (IllegalStateException e) {
            assertEquals("Simulated element deserialization failure", e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testWrapExceptionsEnabledWrapsInJsonMappingException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.WRAP_EXCEPTIONS);
        SimpleModule module = new SimpleModule();
        module.addDeserializer(String.class, new BoomDeserializer());
        mapper.registerModule(module);

        JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, String.class);
        try {
            mapper.readValue("[\"trigger\"]", type);
            fail("Expected JsonMappingException wrapping IllegalStateException");
        } catch (JsonMappingException e) {
            assertTrue(e.getCause() instanceof IllegalStateException);
            assertEquals("Simulated element deserialization failure", e.getCause().getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testUnresolvedForwardReferenceWithoutObjectIdReaderThrowsMappingException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(String.class, new ThrowingForwardRefDeserializer());
        mapper.registerModule(module);

        JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, String.class);
        try {
            mapper.readValue("[\"elem\"]", type);
            fail("Expected JsonMappingException due to unresolved forward reference without identity info");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Unresolved forward reference but no identity info"));
        }
    }

    @Test(timeout = 4000)
    public void testForwardReferenceResolutionWithObjectIds() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "[ 10, {\"id\": 10, \"label\": \"resolvedItem\"}, 10 ]";
        JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, IdentifiableItem.class);

        List<IdentifiableItem> items = mapper.readValue(json, type);
        assertNotNull(items);
        assertEquals(3, items.size());
        assertEquals(10, items.get(0).id);
        assertEquals("resolvedItem", items.get(0).label);
        assertSame(items.get(0), items.get(1));
        assertSame(items.get(1), items.get(2));
    }

    @Test(timeout = 4000)
    public void testCollectionReferringAccumulatorUnknownIdThrows() throws Exception {
        List<Object> target = new ArrayList<Object>();
        CollectionDeserializer.CollectionReferringAccumulator accumulator =
                new CollectionDeserializer.CollectionReferringAccumulator(String.class, target);

        accumulator.add("initialElement");
        assertEquals(1, target.size());
        assertEquals("initialElement", target.get(0));

        try {
            accumulator.resolveForwardReference("unregistered-id", "resolvedValue");
            fail("Expected IllegalArgumentException for unresolved forward reference id");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("wasn't previously seen as unresolved"));
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testIsCachable() {
        JavaType colType = TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, String.class);
        ValueInstantiator vi = new StdValueInstantiator(null, colType);

        CollectionDeserializer cachableDeser = new CollectionDeserializer(colType, null, null, vi);
        assertTrue(cachableDeser.isCachable());

        JsonDeserializer<Object> dummyDeser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) { return null; }
        };

        CollectionDeserializer withValDeser = new CollectionDeserializer(colType, dummyDeser, null, vi);
        assertFalse(withValDeser.isCachable());

        CollectionDeserializer withDelDeser = new CollectionDeserializer(colType, null, null, vi, dummyDeser, null);
        assertFalse(withDelDeser.isCachable());
    }

    @Test(timeout = 4000)
    public void testLifecycleGettersAndWithResolved() {
        JavaType colType = TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, String.class);
        ValueInstantiator vi = new StdValueInstantiator(null, colType);
        JsonDeserializer<Object> valDeser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) { return null; }
        };
        JsonDeserializer<Object> delDeser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) { return null; }
        };

        CollectionDeserializer deser = new CollectionDeserializer(colType, valDeser, null, vi, delDeser, Boolean.TRUE);

        assertEquals(colType.getContentType(), deser.getContentType());
        assertSame(valDeser, deser.getContentDeserializer());

        CollectionDeserializer same = deser.withResolved(delDeser, valDeser, null, Boolean.TRUE);
        assertSame(deser, same);

        CollectionDeserializer changed = deser.withResolved(null, valDeser, null, Boolean.FALSE);
        assertNotSame(deser, changed);

        @SuppressWarnings("deprecation")
        CollectionDeserializer deprecatedChanged = deser.withResolved(null, valDeser, null);
        assertNotSame(deser, deprecatedChanged);

        CollectionDeserializer copy = new CollectionDeserializer(deser);
        assertEquals(deser.getContentType(), copy.getContentType());
        assertSame(deser.getContentDeserializer(), copy.getContentDeserializer());
        assertFalse(copy.isCachable());
    }

    @Test(timeout = 4000)
    public void testDeserializeWithTypeDelegatesCorrectly() throws Exception {
        JavaType colType = TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, String.class);
        CollectionDeserializer deser = new CollectionDeserializer(colType, null, null, null);

        final boolean[] delegateInvoked = new boolean[1];
        TypeDeserializer stubTypeDeser = new TypeDeserializer() {
            @Override
            public TypeDeserializer forProperty(BeanProperty prop) { return this; }
            @Override
            public JsonTypeInfo.As getTypeInclusion() { return null; }
            @Override
            public String getPropertyName() { return null; }
            @Override
            public TypeIdResolver getTypeIdResolver() { return null; }
            @Override
            public Class<?> getDefaultImpl() { return null; }
            @Override
            public Object deserializeTypedFromObject(JsonParser p, DeserializationContext ctxt) { return null; }
            @Override
            public Object deserializeTypedFromArray(JsonParser p, DeserializationContext ctxt) {
                delegateInvoked[0] = true;
                return new ArrayList<Object>();
            }
            @Override
            public Object deserializeTypedFromScalar(JsonParser p, DeserializationContext ctxt) { return null; }
            @Override
            public Object deserializeTypedFromAny(JsonParser p, DeserializationContext ctxt) { return null; }
        };

        Object output = deser.deserializeWithType(null, null, stubTypeDeser);
        assertTrue(delegateInvoked[0]);
        assertTrue(output instanceof List);
    }
}