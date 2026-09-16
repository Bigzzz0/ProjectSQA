package com.google.gson.internal.bind;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.internal.Excluder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.bind.ReflectiveTypeAdapterFactory.BoundField;
import com.google.gson.internal.bind.ReflectiveTypeAdapterFactory.Adapter;
import com.google.gson.FieldNamingStrategy;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.internal.Primitives;
import com.google.gson.internal.$Gson$Types;
import com.google.gson.internal.ObjectConstructor;
import com.google.gson.stream.JsonToken;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: ReflectiveTypeAdapterFactory
 * 
 * Known Defect: When a field has @JsonAdapter annotation with a TypeAdapter that
 * serializes primitives as strings (e.g., StringTypeAdapter for int), the
 * annotation should take precedence over the default adapter. The bug causes
 * the default adapter to be used instead, producing {"part":[42]} instead of
 * {"part":["42"]}.
 * 
 * Branches targeted:
 * 1. create(): raw type not assignable from Object -> returns null (primitive)
 * 2. create(): raw type assignable from Object -> creates Adapter
 * 3. excludeField(): excluder.excludeClass and excluder.excludeField combinations
 * 4. getFieldNames(): no annotation, annotation with no alternates, annotation with alternates
 * 5. createBoundField(): JsonAdapter annotation present/absent, primitive/non-primitive
 * 6. getBoundFields(): interface raw type, Object raw type, multiple JSON field names
 * 7. Adapter.read(): NULL token, beginObject, unknown field, deserialized field
 * 8. Adapter.write(): null value, beginObject, writeField true/false
 * 9. BoundField.writeField(): serialized flag, fieldValue != value
 * 10. BoundField.read(): fieldValue null and isPrimitive -> skip set
 * 
 * Boundary values:
 * - null field values
 * - empty alternates array
 * - multiple alternates
 * - primitive types (int, boolean, etc.)
 * - interface types
 * - Object class
 * - duplicate field names
 * - null input for read()
 * - null value for write()
 */
public class ReflectiveTypeAdapterFactoryDeepseekTest {

    // Test helper classes
    private static class TestObject {
        public String name;
        public int count;
        @SerializedName("custom_name")
        public String renamed;
        @SerializedName(value = "alt_name", alternate = {"alt1", "alt2"})
        public String alternate;
        @JsonAdapter(StringTypeAdapter.class)
        public int annotatedInt;
        public List<String> list;
        public TestObject self;
    }

    private static class StringTypeAdapter extends TypeAdapter<Integer> {
        @Override
        public void write(JsonWriter out, Integer value) throws IOException {
            out.value(String.valueOf(value));
        }

        @Override
        public Integer read(JsonReader in) throws IOException {
            return Integer.parseInt(in.nextString());
        }
    }

    private static class InterfaceType {
        public Runnable runnable;
    }

    private static class DuplicateFields {
        @SerializedName("same")
        public String field1;
        @SerializedName("same")
        public String field2;
    }

    private static class ExcludedField {
        @com.google.gson.annotations.Expose(serialize = false, deserialize = false)
        public String excluded;
        public String included;
    }

    private static class NullField {
        public String nullable;
        public int primitiveInt;
    }

    private static class EmptyClass {
    }

    private static class GenericClass<T> {
        public T value;
    }

    private static class SelfReferencing {
        public SelfReferencing next;
        public String data;
    }

    private static class PrimitiveOnly {
        public int intValue;
        public boolean boolValue;
        public double doubleValue;
    }

    private static class NoFields {
    }

    private static class PrivateFields {
        private String secret;
        public String getSecret() { return secret; }
        public void setSecret(String secret) { this.secret = secret; }
    }

    private static class StaticFields {
        public static String staticField = "static";
        public String instanceField = "instance";
    }

    private static class TransientFields {
        public transient String transientField = "transient";
        public String normalField = "normal";
    }

    private static class ComplexNested {
        public Inner inner;
        public static class Inner {
            public String value;
        }
    }

    private static class ArrayFields {
        public int[] intArray;
        public String[] stringArray;
    }

    private static class EnumField {
        public TestEnum enumValue;
        public enum TestEnum { ONE, TWO, THREE }
    }

    private static class CharSequenceField {
        public CharSequence charSeq;
    }

    private static class NumberField {
        public Number number;
    }

    private static class ObjectField {
        public Object object;
    }

    private static class BooleanField {
        public Boolean boolObj;
        public boolean boolPrim;
    }

    private static class ByteField {
        public Byte byteObj;
        public byte bytePrim;
    }

    private static class ShortField {
        public Short shortObj;
        public short shortPrim;
    }

    private static class LongField {
        public Long longObj;
        public long longPrim;
    }

    private static class FloatField {
        public Float floatObj;
        public float floatPrim;
    }

    private static class DoubleField {
        public Double doubleObj;
        public double doublePrim;
    }

    private static class CharacterField {
        public Character charObj;
        public char charPrim;
    }

    private static class VoidField {
        public Void voidObj;
    }

    private static class CustomNamingStrategy implements FieldNamingStrategy {
        @Override
        public String translateName(Field f) {
            return "prefix_" + f.getName();
        }
    }

    private static class ExcluderImpl extends Excluder {
        @Override
        public boolean excludeField(Field f, boolean serialize) {
            return f.getName().equals("excluded");
        }

        @Override
        public boolean excludeClass(Class<?> clazz, boolean serialize) {
            return clazz == ExcludedClass.class;
        }
    }

    private static class ExcludedClass {
        public String value;
    }

    private static class CustomConstructor implements ObjectConstructor<TestObject> {
        @Override
        public TestObject construct() {
            return new TestObject();
        }
    }

    private static class CustomConstructorForEmpty implements ObjectConstructor<EmptyClass> {
        @Override
        public EmptyClass construct() {
            return new EmptyClass();
        }
    }

    private static class CustomConstructorForNull implements ObjectConstructor<NullField> {
        @Override
        public NullField construct() {
            return new NullField();
        }
    }

    private static class CustomConstructorForSelf implements ObjectConstructor<SelfReferencing> {
        @Override
        public SelfReferencing construct() {
            return new SelfReferencing();
        }
    }

    private static class CustomConstructorForPrivate implements ObjectConstructor<PrivateFields> {
        @Override
        public PrivateFields construct() {
            return new PrivateFields();
        }
    }

    private static class CustomConstructorForStatic implements ObjectConstructor<StaticFields> {
        @Override
        public StaticFields construct() {
            return new StaticFields();
        }
    }

    private static class CustomConstructorForTransient implements ObjectConstructor<TransientFields> {
        @Override
        public TransientFields construct() {
            return new TransientFields();
        }
    }

    private static class CustomConstructorForComplex implements ObjectConstructor<ComplexNested> {
        @Override
        public ComplexNested construct() {
            return new ComplexNested();
        }
    }

    private static class CustomConstructorForArray implements ObjectConstructor<ArrayFields> {
        @Override
        public ArrayFields construct() {
            return new ArrayFields();
        }
    }

    private static class CustomConstructorForEnum implements ObjectConstructor<EnumField> {
        @Override
        public EnumField construct() {
            return new EnumField();
        }
    }

    private static class CustomConstructorForCharSequence implements ObjectConstructor<CharSequenceField> {
        @Override
        public CharSequenceField construct() {
            return new CharSequenceField();
        }
    }

    private static class CustomConstructorForNumber implements ObjectConstructor<NumberField> {
        @Override
        public NumberField construct() {
            return new NumberField();
        }
    }

    private static class CustomConstructorForObject implements ObjectConstructor<ObjectField> {
        @Override
        public ObjectField construct() {
            return new ObjectField();
        }
    }

    private static class CustomConstructorForBoolean implements ObjectConstructor<BooleanField> {
        @Override
        public BooleanField construct() {
            return new BooleanField();
        }
    }

    private static class CustomConstructorForByte implements ObjectConstructor<ByteField> {
        @Override
        public ByteField construct() {
            return new ByteField();
        }
    }

    private static class CustomConstructorForShort implements ObjectConstructor<ShortField> {
        @Override
        public ShortField construct() {
            return new ShortField();
        }
    }

    private static class CustomConstructorForLong implements ObjectConstructor<LongField> {
        @Override
        public LongField construct() {
            return new LongField();
        }
    }

    private static class CustomConstructorForFloat implements ObjectConstructor<FloatField> {
        @Override
        public FloatField construct() {
            return new FloatField();
        }
    }

    private static class CustomConstructorForDouble implements ObjectConstructor<DoubleField> {
        @Override
        public DoubleField construct() {
            return new DoubleField();
        }
    }

    private static class CustomConstructorForCharacter implements ObjectConstructor<CharacterField> {
        @Override
        public CharacterField construct() {
            return new CharacterField();
        }
    }

    private static class CustomConstructorForVoid implements ObjectConstructor<VoidField> {
        @Override
        public VoidField construct() {
            return new VoidField();
        }
    }

    private static class CustomConstructorForGeneric implements ObjectConstructor<GenericClass<String>> {
        @Override
        public GenericClass<String> construct() {
            return new GenericClass<String>();
        }
    }

    private static class CustomConstructorForInterface implements ObjectConstructor<InterfaceType> {
        @Override
        public InterfaceType construct() {
            return new InterfaceType();
        }
    }

    private static class CustomConstructorForDuplicate implements ObjectConstructor<DuplicateFields> {
        @Override
        public DuplicateFields construct() {
            return new DuplicateFields();
        }
    }

    private static class CustomConstructorForExcluded implements ObjectConstructor<ExcludedField> {
        @Override
        public ExcludedField construct() {
            return new ExcludedField();
        }
    }

    private static class CustomConstructorForPrimitiveOnly implements ObjectConstructor<PrimitiveOnly> {
        @Override
        public PrimitiveOnly construct() {
            return new PrimitiveOnly();
        }
    }

    private static class CustomConstructorForNoFields implements ObjectConstructor<NoFields> {
        @Override
        public NoFields construct() {
            return new NoFields();
        }
    }

    private static class CustomConstructorForExcludedClass implements ObjectConstructor<ExcludedClass> {
        @Override
        public ExcludedClass construct() {
            return new ExcludedClass();
        }
    }

    private static class CustomConstructorForCustomNaming implements ObjectConstructor<TestObject> {
        @Override
        public TestObject construct() {
            return new TestObject();
        }
    }

    // Helper method to create factory with default excluder
    private ReflectiveTypeAdapterFactory createFactory() {
        ConstructorConstructor cc = new ConstructorConstructor();
        return new ReflectiveTypeAdapterFactory(cc, FieldNamingPolicy.IDENTITY, new Excluder());
    }

    // Helper method to create factory with custom excluder
    private ReflectiveTypeAdapterFactory createFactoryWithExcluder(Excluder excluder) {
        ConstructorConstructor cc = new ConstructorConstructor();
        return new ReflectiveTypeAdapterFactory(cc, FieldNamingPolicy.IDENTITY, excluder);
    }

    // Helper method to create factory with custom naming strategy
    private ReflectiveTypeAdapterFactory createFactoryWithNaming(FieldNamingStrategy strategy) {
        ConstructorConstructor cc = new ConstructorConstructor();
        return new ReflectiveTypeAdapterFactory(cc, strategy, new Excluder());
    }

    // Helper method to create factory with custom constructor
    private ReflectiveTypeAdapterFactory createFactoryWithConstructor(ConstructorConstructor cc) {
        return new ReflectiveTypeAdapterFactory(cc, FieldNamingPolicy.IDENTITY, new Excluder());
    }

    // ==================== PARTITION A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testCreateForObjectType() {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        assertNotNull("Adapter should not be null for Object type", adapter);
        assertTrue("Adapter should be instance of Adapter", adapter instanceof Adapter);
    }

    @Test(timeout = 4000)
    public void testCreateForPrimitiveType() {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<Integer> adapter = factory.create(gson, TypeToken.get(Integer.class));
        assertNull("Adapter should be null for primitive type", adapter);
    }

    @Test(timeout = 4000)
    public void testCreateForStringType() {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<String> adapter = factory.create(gson, TypeToken.get(String.class));
        assertNull("Adapter should be null for String type", adapter);
    }

    @Test(timeout = 4000)
    public void testExcludeFieldWithDefaultExcluder() {
        ReflectiveTypeAdapterFactory factory = createFactory();
        try {
            Field field = TestObject.class.getDeclaredField("name");
            boolean result = factory.excludeField(field, true);
            assertTrue("Field should not be excluded by default", result);
        } catch (NoSuchFieldException e) {
            fail("Field should exist");
        }
    }

    @Test(timeout = 4000)
    public void testExcludeFieldWithCustomExcluder() {
        Excluder excluder = new Excluder() {
            @Override
            public boolean excludeField(Field f, boolean serialize) {
                return f.getName().equals("name");
            }
        };
        ReflectiveTypeAdapterFactory factory = createFactoryWithExcluder(excluder);
        try {
            Field field = TestObject.class.getDeclaredField("name");
            boolean result = factory.excludeField(field, true);
            assertFalse("Field should be excluded by custom excluder", result);
        } catch (NoSuchFieldException e) {
            fail("Field should exist");
        }
    }

    @Test(timeout = 4000)
    public void testExcludeFieldWithExcluderExcludeClass() {
        Excluder excluder = new Excluder() {
            @Override
            public boolean excludeClass(Class<?> clazz, boolean serialize) {
                return clazz == String.class;
            }
        };
        ReflectiveTypeAdapterFactory factory = createFactoryWithExcluder(excluder);
        try {
            Field field = TestObject.class.getDeclaredField("name");
            boolean result = factory.excludeField(field, true);
            assertFalse("Field should be excluded when class is excluded", result);
        } catch (NoSuchFieldException e) {
            fail("Field should exist");
        }
    }

    @Test(timeout = 4000)
    public void testGetFieldNamesWithoutAnnotation() {
        ReflectiveTypeAdapterFactory factory = createFactory();
        try {
            Field field = TestObject.class.getDeclaredField("name");
            List<String> names = factory.getFieldNames(field);
            assertEquals("Should have one name", 1, names.size());
            assertEquals("Should be field name", "name", names.get(0));
        } catch (NoSuchFieldException e) {
            fail("Field should exist");
        }
    }

    @Test(timeout = 4000)
    public void testGetFieldNamesWithAnnotationNoAlternates() {
        ReflectiveTypeAdapterFactory factory = createFactory();
        try {
            Field field = TestObject.class.getDeclaredField("renamed");
            List<String> names = factory.getFieldNames(field);
            assertEquals("Should have one name", 1, names.size());
            assertEquals("Should be serialized name", "custom_name", names.get(0));
        } catch (NoSuchFieldException e) {
            fail("Field should exist");
        }
    }

    @Test(timeout = 4000)
    public void testGetFieldNamesWithAnnotationAndAlternates() {
        ReflectiveTypeAdapterFactory factory = createFactory();
        try {
            Field field = TestObject.class.getDeclaredField("alternate");
            List<String> names = factory.getFieldNames(field);
            assertEquals("Should have three names", 3, names.size());
            assertEquals("First should be serialized name", "alt_name", names.get(0));
            assertEquals("Second should be alternate", "alt1", names.get(1));
            assertEquals("Third should be alternate", "alt2", names.get(2));
        } catch (NoSuchFieldException e) {
            fail("Field should exist");
        }
    }

    @Test(timeout = 4000)
    public void testGetFieldNamesWithCustomNamingStrategy() {
        ReflectiveTypeAdapterFactory factory = createFactoryWithNaming(new CustomNamingStrategy());
        try {
            Field field = TestObject.class.getDeclaredField("name");
            List<String> names = factory.getFieldNames(field);
            assertEquals("Should have one name", 1, names.size());
            assertEquals("Should be prefixed name", "prefix_name", names.get(0));
        } catch (NoSuchFieldException e) {
            fail("Field should exist");
        }
    }

    @Test(timeout = 4000)
    public void testCreateBoundFieldWithJsonAdapter() {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        try {
            Field field = TestObject.class.getDeclaredField("annotatedInt");
            BoundField boundField = factory.createBoundField(gson, field, "annotatedInt",
                    TypeToken.get(int.class), true, true);
            assertNotNull("BoundField should not be null", boundField);
            assertEquals("Name should match", "annotatedInt", boundField.name);
            assertTrue("Should be serialized", boundField.serialized);
            assertTrue("Should be deserialized", boundField.deserialized);
        } catch (NoSuchFieldException e) {
            fail("Field should exist");
        }
    }

    @Test(timeout = 4000)
    public void testCreateBoundFieldWithoutJsonAdapter() {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        try {
            Field field = TestObject.class.getDeclaredField("name");
            BoundField boundField = factory.createBoundField(gson, field, "name",
                    TypeToken.get(String.class), true, true);
            assertNotNull("BoundField should not be null", boundField);
            assertEquals("Name should match", "name", boundField.name);
        } catch (NoSuchFieldException e) {
            fail("Field should exist");
        }
    }

    @Test(timeout = 4000)
    public void testCreateBoundFieldWithPrimitiveType() {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        try {
            Field field = TestObject.class.getDeclaredField("count");
            BoundField boundField = factory.createBoundField(gson, field, "count",
                    TypeToken.get(int.class), true, true);
            assertNotNull("BoundField should not be null", boundField);
            assertEquals("Name should match", "count", boundField.name);
        } catch (NoSuchFieldException e) {
            fail("Field should exist");
        }
    }

    @Test(timeout = 4000)
    public void testGetBoundFieldsForInterface() {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        Map<String, BoundField> fields = factory.getBoundFields(gson,
                TypeToken.get(InterfaceType.class), InterfaceType.class);
        assertNotNull("Fields map should not be null", fields);
        assertTrue("Should have no fields for interface", fields.isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetBoundFieldsForObject() {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        Map<String, BoundField> fields = factory.getBoundFields(gson,
                TypeToken.get(Object.class), Object.class);
        assertNotNull("Fields map should not be null", fields);
        assertTrue("Should have no fields for Object", fields.isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetBoundFieldsForNormalClass() {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        Map<String, BoundField> fields = factory.getBoundFields(gson,
                TypeToken.get(TestObject.class), TestObject.class);
        assertNotNull("Fields map should not be null", fields);
        assertFalse("Should have fields", fields.isEmpty());
        assertTrue("Should contain name field", fields.containsKey("name"));
        assertTrue("Should contain count field", fields.containsKey("count"));
        assertTrue("Should contain custom_name field", fields.containsKey("custom_name"));
        assertTrue("Should contain alt_name field", fields.containsKey("alt_name"));
        assertTrue("Should contain alt1 field", fields.containsKey("alt1"));
        assertTrue("Should contain alt2 field", fields.containsKey("alt2"));
        assertTrue("Should contain annotatedInt field", fields.containsKey("annotatedInt"));
        assertTrue("Should contain list field", fields.containsKey("list"));
        assertTrue("Should contain self field", fields.containsKey("self"));
    }

    @Test(timeout = 4000)
    public void testGetBoundFieldsWithDuplicateNames() {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        try {
            factory.getBoundFields(gson, TypeToken.get(DuplicateFields.class), DuplicateFields.class);
            fail("Should throw IllegalArgumentException for duplicate field names");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testGetBoundFieldsWithExcludedFields() {
        Excluder excluder = new Excluder() {
            @Override
            public boolean excludeField(Field f, boolean serialize) {
                return f.getName().equals("excluded");
            }
        };
        ReflectiveTypeAdapterFactory factory = createFactoryWithExcluder(excluder);
        Gson gson = new Gson();
        Map<String, BoundField> fields = factory.getBoundFields(gson,
                TypeToken.get(ExcludedField.class), ExcludedField.class);
        assertNotNull("Fields map should not be null", fields);
        assertFalse("Should have fields", fields.isEmpty());
        assertFalse("Should not contain excluded field", fields.containsKey("excluded"));
        assertTrue("Should contain included field", fields.containsKey("included"));
    }

    @Test(timeout = 4000)
    public void testGetBoundFieldsWithInheritance() {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        Map<String, BoundField> fields = factory.getBoundFields(gson,
                TypeToken.get(ComplexNested.class), ComplexNested.class);
        assertNotNull("Fields map should not be null", fields);
        assertTrue("Should contain inner field", fields.containsKey("inner"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithNullInput() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("null"));
        TestObject result = adapter.read(reader);
        assertNull("Should return null for null input", result);
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithEmptyObject() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertNull("Name should be null", result.name);
        assertEquals("Count should be 0", 0, result.count);
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithFields() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":\"test\",\"count\":42}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Name should match", "test", result.name);
        assertEquals("Count should match", 42, result.count);
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithUnknownField() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"unknown\":\"value\",\"name\":\"test\"}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Name should match", "test", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithAlternateName() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"alt1\":\"value\"}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Alternate should match", "value", result.alternate);
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonAdapter() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"annotatedInt\":\"42\"}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Annotated int should match", 42, result.annotatedInt);
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithPrimitiveNull() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<NullField> adapter = factory.create(gson, TypeToken.get(NullField.class));
        JsonReader reader = new JsonReader(new StringReader("{\"primitiveInt\":null}"));
        NullField result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Primitive int should remain 0", 0, result.primitiveInt);
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithNonPrimitiveNull() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<NullField> adapter = factory.create(gson, TypeToken.get(NullField.class));
        JsonReader reader = new JsonReader(new StringReader("{\"nullable\":null}"));
        NullField result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertNull("Nullable should be null", result.nullable);
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithMalformedJson() {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        try {
            JsonReader reader = new JsonReader(new StringReader("{\"name\":\"test\""));
            adapter.read(reader);
            fail("Should throw JsonSyntaxException for malformed JSON");
        } catch (JsonSyntaxException e) {
            // Expected
        } catch (IOException e) {
            fail("Should throw JsonSyntaxException, not IOException");
        }
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithNullValue() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, null);
        assertEquals("Should write null", "null", writer.toString());
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithEmptyObject() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<EmptyClass> adapter = factory.create(gson, TypeToken.get(EmptyClass.class));
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, new EmptyClass());
        assertEquals("Should write empty object", "{}", writer.toString());
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithFields() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        obj.count = 42;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
        assertTrue("Should contain count", json.contains("\"count\":42"));
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithNullFields() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertFalse("Should not contain null name", json.contains("\"name\":null"));
        assertTrue("Should contain count", json.contains("\"count\":0"));
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithSelfReference() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<SelfReferencing> adapter = factory.create(gson, TypeToken.get(SelfReferencing.class));
        SelfReferencing obj = new SelfReferencing();
        obj.data = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain data", json.contains("\"data\":\"test\""));
        assertFalse("Should not contain self reference", json.contains("\"next\":"));
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonAdapter() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.annotatedInt = 42;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain annotated int as string", json.contains("\"annotatedInt\":\"42\""));
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithExcludedField() throws IOException {
        Excluder excluder = new Excluder() {
            @Override
            public boolean excludeField(Field f, boolean serialize) {
                return f.getName().equals("excluded");
            }
        };
        ReflectiveTypeAdapterFactory factory = createFactoryWithExcluder(excluder);
        Gson gson = new Gson();
        TypeAdapter<ExcludedField> adapter = factory.create(gson, TypeToken.get(ExcludedField.class));
        ExcludedField obj = new ExcludedField();
        obj.excluded = "excluded";
        obj.included = "included";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertFalse("Should not contain excluded field", json.contains("excluded"));
        assertTrue("Should contain included field", json.contains("\"included\":\"included\""));
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithAlternateNames() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.alternate = "value";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain alt_name", json.contains("\"alt_name\":\"value\""));
        assertFalse("Should not contain alt1", json.contains("\"alt1\":"));
        assertFalse("Should not contain alt2", json.contains("\"alt2\":"));
    }

    @Test(timeout = 4000)
    public void testBoundFieldWriteFieldWithSerializedFalse() {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        try {
            Field field = TestObject.class.getDeclaredField("name");
            BoundField boundField = factory.createBoundField(gson, field, "name",
                    TypeToken.get(String.class), false, true);
            assertFalse("Should not write field when serialized is false", boundField.writeField(new TestObject()));
        } catch (NoSuchFieldException e) {
            fail("Field should exist");
        } catch (IOException e) {
            fail("Should not throw IOException");
        } catch (IllegalAccessException e) {
            fail("Should not throw IllegalAccessException");
        }
    }

    @Test(timeout = 4000)
    public void testBoundFieldWriteFieldWithNullValue() {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        try {
            Field field = TestObject.class.getDeclaredField("name");
            BoundField boundField = factory.createBoundField(gson, field, "name",
                    TypeToken.get(String.class), true, true);
            TestObject obj = new TestObject();
            obj.name = null;
            assertFalse("Should not write null field", boundField.writeField(obj));
        } catch (NoSuchFieldException e) {
            fail("Field should exist");
        } catch (IOException e) {
            fail("Should not throw IOException");
        } catch (IllegalAccessException e) {
            fail("Should not throw IllegalAccessException");
        }
    }

    @Test(timeout = 4000)
    public void testBoundFieldWriteFieldWithNonNullValue() {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        try {
            Field field = TestObject.class.getDeclaredField("name");
            BoundField boundField = factory.createBoundField(gson, field, "name",
                    TypeToken.get(String.class), true, true);
            TestObject obj = new TestObject();
            obj.name = "test";
            assertTrue("Should write non-null field", boundField.writeField(obj));
        } catch (NoSuchFieldException e) {
            fail("Field should exist");
        } catch (IOException e) {
            fail("Should not throw IOException");
        } catch (IllegalAccessException e) {
            fail("Should not throw IllegalAccessException");
        }
    }

    @Test(timeout = 4000)
    public void testBoundFieldWriteFieldWithSelfReference() {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        try {
            Field field = SelfReferencing.class.getDeclaredField("next");
            BoundField boundField = factory.createBoundField(gson, field, "next",
                    TypeToken.get(SelfReferencing.class), true, true);
            SelfReferencing obj = new SelfReferencing();
            obj.next = obj;
            assertFalse("Should not write self reference", boundField.writeField(obj));
        } catch (NoSuchFieldException e) {
            fail("Field should exist");
        } catch (IOException e) {
            fail("Should not throw IOException");
        } catch (IllegalAccessException e) {
            fail("Should not throw IllegalAccessException");
        }
    }

    @Test(timeout = 4000)
    public void testBoundFieldWriteWithNullValue() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        try {
            Field field = TestObject.class.getDeclaredField("name");
            BoundField boundField = factory.createBoundField(gson, field, "name",
                    TypeToken.get(String.class), true, true);
            StringWriter writer = new StringWriter();
            JsonWriter jsonWriter = new JsonWriter(writer);
            TestObject obj = new TestObject();
            obj.name = null;
            boundField.write(jsonWriter, obj);
            assertEquals("Should write null", "null", writer.toString());
        } catch (NoSuchFieldException e) {
            fail("Field should exist");
        } catch (IllegalAccessException e) {
            fail("Should not throw IllegalAccessException");
        }
    }

    @Test(timeout = 4000)
    public void testBoundFieldWriteWithNonNullValue() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        try {
            Field field = TestObject.class.getDeclaredField("name");
            BoundField boundField = factory.createBoundField(gson, field, "name",
                    TypeToken.get(String.class), true, true);
            StringWriter writer = new StringWriter();
            JsonWriter jsonWriter = new JsonWriter(writer);
            TestObject obj = new TestObject();
            obj.name = "test";
            boundField.write(jsonWriter, obj);
            assertEquals("Should write value", "\"test\"", writer.toString());
        } catch (NoSuchFieldException e) {
            fail("Field should exist");
        } catch (IllegalAccessException e) {
            fail("Should not throw IllegalAccessException");
        }
    }

    @Test(timeout = 4000)
    public void testBoundFieldReadWithNonNullValue() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        try {
            Field field = TestObject.class.getDeclaredField("name");
            BoundField boundField = factory.createBoundField(gson, field, "name",
                    TypeToken.get(String.class), true, true);
            JsonReader reader = new JsonReader(new StringReader("\"test\""));
            TestObject obj = new TestObject();
            boundField.read(reader, obj);
            assertEquals("Should read value", "test", obj.name);
        } catch (NoSuchFieldException e) {
            fail("Field should exist");
        } catch (IllegalAccessException e) {
            fail("Should not throw IllegalAccessException");
        }
    }

    @Test(timeout = 4000)
    public void testBoundFieldReadWithNullValue() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        try {
            Field field = TestObject.class.getDeclaredField("name");
            BoundField boundField = factory.createBoundField(gson, field, "name",
                    TypeToken.get(String.class), true, true);
            JsonReader reader = new JsonReader(new StringReader("null"));
            TestObject obj = new TestObject();
            obj.name = "existing";
            boundField.read(reader, obj);
            assertNull("Should set null value", obj.name);
        } catch (NoSuchFieldException e) {
            fail("Field should exist");
        } catch (IllegalAccessException e) {
            fail("Should not throw IllegalAccessException");
        }
    }

    @Test(timeout = 4000)
    public void testBoundFieldReadWithPrimitiveNull() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        try {
            Field field = NullField.class.getDeclaredField("primitiveInt");
            BoundField boundField = factory.createBoundField(gson, field, "primitiveInt",
                    TypeToken.get(int.class), true, true);
            JsonReader reader = new JsonReader(new StringReader("null"));
            NullField obj = new NullField();
            obj.primitiveInt = 10;
            boundField.read(reader, obj);
            assertEquals("Should not change primitive value", 10, obj.primitiveInt);
        } catch (NoSuchFieldException e) {
            fail("Field should exist");
        } catch (IllegalAccessException e) {
            fail("Should not throw IllegalAccessException");
        }
    }

    @Test(timeout = 4000)
    public void testBoundFieldReadWithPrimitiveValue() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        try {
            Field field = NullField.class.getDeclaredField("primitiveInt");
            BoundField boundField = factory.createBoundField(gson, field, "primitiveInt",
                    TypeToken.get(int.class), true, true);
            JsonReader reader = new JsonReader(new StringReader("42"));
            NullField obj = new NullField();
            boundField.read(reader, obj);
            assertEquals("Should set primitive value", 42, obj.primitiveInt);
        } catch (NoSuchFieldException e) {
            fail("Field should exist");
        } catch (IllegalAccessException e) {
            fail("Should not throw IllegalAccessException");
        }
    }

    // ==================== PARTITION B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000)
    public void testCreateWithNullType() {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        try {
            factory.create(gson, null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateWithNullGson() {
        ReflectiveTypeAdapterFactory factory = createFactory();
        try {
            factory.create(null, TypeToken.get(TestObject.class));
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testExcludeFieldWithNullField() {
        ReflectiveTypeAdapterFactory factory = createFactory();
        try {
            factory.excludeField(null, true);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testGetFieldNamesWithNullField() {
        ReflectiveTypeAdapterFactory factory = createFactory();
        try {
            factory.getFieldNames(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateBoundFieldWithNullContext() {
        ReflectiveTypeAdapterFactory factory = createFactory();
        try {
            Field field = TestObject.class.getDeclaredField("name");
            factory.createBoundField(null, field, "name", TypeToken.get(String.class), true, true);
            fail("Should throw NullPointerException");
        } catch (NoSuchFieldException e) {
            fail("Field should exist");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateBoundFieldWithNullField() {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        try {
            factory.createBoundField(gson, null, "name", TypeToken.get(String.class), true, true);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateBoundFieldWithNullName() {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        try {
            Field field = TestObject.class.getDeclaredField("name");
            factory.createBoundField(gson, field, null, TypeToken.get(String.class), true, true);
            fail("Should throw NullPointerException");
        } catch (NoSuchFieldException e) {
            fail("Field should exist");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateBoundFieldWithNullFieldType() {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        try {
            Field field = TestObject.class.getDeclaredField("name");
            factory.createBoundField(gson, field, "name", null, true, true);
            fail("Should throw NullPointerException");
        } catch (NoSuchFieldException e) {
            fail("Field should exist");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testGetBoundFieldsWithNullContext() {
        ReflectiveTypeAdapterFactory factory = createFactory();
        try {
            factory.getBoundFields(null, TypeToken.get(TestObject.class), TestObject.class);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testGetBoundFieldsWithNullType() {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        try {
            factory.getBoundFields(gson, null, TestObject.class);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testGetBoundFieldsWithNullRaw() {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        try {
            factory.getBoundFields(gson, TypeToken.get(TestObject.class), null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithEmptyString() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        try {
            JsonReader reader = new JsonReader(new StringReader(""));
            adapter.read(reader);
            fail("Should throw IOException for empty input");
        } catch (IOException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithInvalidJson() {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        try {
            JsonReader reader = new JsonReader(new StringReader("invalid"));
            adapter.read(reader);
            fail("Should throw JsonSyntaxException for invalid JSON");
        } catch (JsonSyntaxException e) {
            // Expected
        } catch (IOException e) {
            fail("Should throw JsonSyntaxException, not IOException");
        }
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithEmptyObject() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<NoFields> adapter = factory.create(gson, TypeToken.get(NoFields.class));
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, new NoFields());
        assertEquals("Should write empty object", "{}", writer.toString());
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithAllNullFields() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertFalse("Should not contain null name", json.contains("\"name\":null"));
        assertTrue("Should contain count", json.contains("\"count\":0"));
        assertFalse("Should not contain null list", json.contains("\"list\":null"));
        assertFalse("Should not contain null self", json.contains("\"self\":null"));
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithMaxValues() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<PrimitiveOnly> adapter = factory.create(gson, TypeToken.get(PrimitiveOnly.class));
        PrimitiveOnly obj = new PrimitiveOnly();
        obj.intValue = Integer.MAX_VALUE;
        obj.boolValue = true;
        obj.doubleValue = Double.MAX_VALUE;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain max int", json.contains("\"intValue\":2147483647"));
        assertTrue("Should contain true bool", json.contains("\"boolValue\":true"));
        assertTrue("Should contain max double", json.contains("\"doubleValue\":1.7976931348623157E308"));
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithMinValues() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<PrimitiveOnly> adapter = factory.create(gson, TypeToken.get(PrimitiveOnly.class));
        PrimitiveOnly obj = new PrimitiveOnly();
        obj.intValue = Integer.MIN_VALUE;
        obj.boolValue = false;
        obj.doubleValue = Double.MIN_VALUE;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain min int", json.contains("\"intValue\":-2147483648"));
        assertTrue("Should contain false bool", json.contains("\"boolValue\":false"));
        assertTrue("Should contain min double", json.contains("\"doubleValue\":4.9E-324"));
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithZeroValues() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<PrimitiveOnly> adapter = factory.create(gson, TypeToken.get(PrimitiveOnly.class));
        PrimitiveOnly obj = new PrimitiveOnly();
        obj.intValue = 0;
        obj.boolValue = false;
        obj.doubleValue = 0.0;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain zero int", json.contains("\"intValue\":0"));
        assertTrue("Should contain false bool", json.contains("\"boolValue\":false"));
        assertTrue("Should contain zero double", json.contains("\"doubleValue\":0.0"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithMaxValues() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<PrimitiveOnly> adapter = factory.create(gson, TypeToken.get(PrimitiveOnly.class));
        JsonReader reader = new JsonReader(new StringReader(
                "{\"intValue\":2147483647,\"boolValue\":true,\"doubleValue\":1.7976931348623157E308}"));
        PrimitiveOnly result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Max int should match", Integer.MAX_VALUE, result.intValue);
        assertTrue("Max bool should match", result.boolValue);
        assertEquals("Max double should match", Double.MAX_VALUE, result.doubleValue, 0.0);
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithMinValues() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<PrimitiveOnly> adapter = factory.create(gson, TypeToken.get(PrimitiveOnly.class));
        JsonReader reader = new JsonReader(new StringReader(
                "{\"intValue\":-2147483648,\"boolValue\":false,\"doubleValue\":4.9E-324}"));
        PrimitiveOnly result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Min int should match", Integer.MIN_VALUE, result.intValue);
        assertFalse("Min bool should match", result.boolValue);
        assertEquals("Min double should match", Double.MIN_VALUE, result.doubleValue, 0.0);
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithZeroValues() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<PrimitiveOnly> adapter = factory.create(gson, TypeToken.get(PrimitiveOnly.class));
        JsonReader reader = new JsonReader(new StringReader(
                "{\"intValue\":0,\"boolValue\":false,\"doubleValue\":0.0}"));
        PrimitiveOnly result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Zero int should match", 0, result.intValue);
        assertFalse("Zero bool should match", result.boolValue);
        assertEquals("Zero double should match", 0.0, result.doubleValue, 0.0);
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithEmptyArray() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<ArrayFields> adapter = factory.create(gson, TypeToken.get(ArrayFields.class));
        JsonReader reader = new JsonReader(new StringReader("{\"intArray\":[],\"stringArray\":[]}"));
        ArrayFields result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertNotNull("Int array should not be null", result.intArray);
        assertEquals("Int array should be empty", 0, result.intArray.length);
        assertNotNull("String array should not be null", result.stringArray);
        assertEquals("String array should be empty", 0, result.stringArray.length);
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithArrayValues() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<ArrayFields> adapter = factory.create(gson, TypeToken.get(ArrayFields.class));
        JsonReader reader = new JsonReader(new StringReader(
                "{\"intArray\":[1,2,3],\"stringArray\":[\"a\",\"b\"]}"));
        ArrayFields result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertArrayEquals("Int array should match", new int[]{1, 2, 3}, result.intArray);
        assertArrayEquals("String array should match", new String[]{"a", "b"}, result.stringArray);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithArrayValues() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<ArrayFields> adapter = factory.create(gson, TypeToken.get(ArrayFields.class));
        ArrayFields obj = new ArrayFields();
        obj.intArray = new int[]{1, 2, 3};
        obj.stringArray = new String[]{"a", "b"};
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain int array", json.contains("\"intArray\":[1,2,3]"));
        assertTrue("Should contain string array", json.contains("\"stringArray\":[\"a\",\"b\"]"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithEnum() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<EnumField> adapter = factory.create(gson, TypeToken.get(EnumField.class));
        JsonReader reader = new JsonReader(new StringReader("{\"enumValue\":\"TWO\"}"));
        EnumField result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Enum should match", EnumField.TestEnum.TWO, result.enumValue);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithEnum() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<EnumField> adapter = factory.create(gson, TypeToken.get(EnumField.class));
        EnumField obj = new EnumField();
        obj.enumValue = EnumField.TestEnum.THREE;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain enum value", json.contains("\"enumValue\":\"THREE\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithCharSequence() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<CharSequenceField> adapter = factory.create(gson, TypeToken.get(CharSequenceField.class));
        JsonReader reader = new JsonReader(new StringReader("{\"charSeq\":\"test\"}"));
        CharSequenceField result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("CharSequence should match", "test", result.charSeq.toString());
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithCharSequence() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<CharSequenceField> adapter = factory.create(gson, TypeToken.get(CharSequenceField.class));
        CharSequenceField obj = new CharSequenceField();
        obj.charSeq = new StringBuilder("test");
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain char sequence", json.contains("\"charSeq\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithNumber() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<NumberField> adapter = factory.create(gson, TypeToken.get(NumberField.class));
        JsonReader reader = new JsonReader(new StringReader("{\"number\":42}"));
        NumberField result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertNotNull("Number should not be null", result.number);
        assertEquals("Number should match", 42, result.number.intValue());
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithNumber() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<NumberField> adapter = factory.create(gson, TypeToken.get(NumberField.class));
        NumberField obj = new NumberField();
        obj.number = 42;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain number", json.contains("\"number\":42"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithObject() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<ObjectField> adapter = factory.create(gson, TypeToken.get(ObjectField.class));
        JsonReader reader = new JsonReader(new StringReader("{\"object\":{\"key\":\"value\"}}"));
        ObjectField result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertNotNull("Object should not be null", result.object);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithObject() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<ObjectField> adapter = factory.create(gson, TypeToken.get(ObjectField.class));
        ObjectField obj = new ObjectField();
        obj.object = new TestObject();
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain object", json.contains("\"object\":"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithBooleanObject() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<BooleanField> adapter = factory.create(gson, TypeToken.get(BooleanField.class));
        JsonReader reader = new JsonReader(new StringReader("{\"boolObj\":true,\"boolPrim\":false}"));
        BooleanField result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Boolean object should match", Boolean.TRUE, result.boolObj);
        assertFalse("Boolean primitive should match", result.boolPrim);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithBooleanObject() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<BooleanField> adapter = factory.create(gson, TypeToken.get(BooleanField.class));
        BooleanField obj = new BooleanField();
        obj.boolObj = Boolean.TRUE;
        obj.boolPrim = false;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain boolean object", json.contains("\"boolObj\":true"));
        assertTrue("Should contain boolean primitive", json.contains("\"boolPrim\":false"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithByteObject() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<ByteField> adapter = factory.create(gson, TypeToken.get(ByteField.class));
        JsonReader reader = new JsonReader(new StringReader("{\"byteObj\":42,\"bytePrim\":7}"));
        ByteField result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Byte object should match", Byte.valueOf((byte) 42), result.byteObj);
        assertEquals("Byte primitive should match", (byte) 7, result.bytePrim);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithByteObject() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<ByteField> adapter = factory.create(gson, TypeToken.get(ByteField.class));
        ByteField obj = new ByteField();
        obj.byteObj = 42;
        obj.bytePrim = 7;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain byte object", json.contains("\"byteObj\":42"));
        assertTrue("Should contain byte primitive", json.contains("\"bytePrim\":7"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithShortObject() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<ShortField> adapter = factory.create(gson, TypeToken.get(ShortField.class));
        JsonReader reader = new JsonReader(new StringReader("{\"shortObj\":42,\"shortPrim\":7}"));
        ShortField result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Short object should match", Short.valueOf((short) 42), result.shortObj);
        assertEquals("Short primitive should match", (short) 7, result.shortPrim);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithShortObject() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<ShortField> adapter = factory.create(gson, TypeToken.get(ShortField.class));
        ShortField obj = new ShortField();
        obj.shortObj = 42;
        obj.shortPrim = 7;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain short object", json.contains("\"shortObj\":42"));
        assertTrue("Should contain short primitive", json.contains("\"shortPrim\":7"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithLongObject() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<LongField> adapter = factory.create(gson, TypeToken.get(LongField.class));
        JsonReader reader = new JsonReader(new StringReader("{\"longObj\":42,\"longPrim\":7}"));
        LongField result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Long object should match", Long.valueOf(42L), result.longObj);
        assertEquals("Long primitive should match", 7L, result.longPrim);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithLongObject() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<LongField> adapter = factory.create(gson, TypeToken.get(LongField.class));
        LongField obj = new LongField();
        obj.longObj = 42L;
        obj.longPrim = 7L;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain long object", json.contains("\"longObj\":42"));
        assertTrue("Should contain long primitive", json.contains("\"longPrim\":7"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithFloatObject() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<FloatField> adapter = factory.create(gson, TypeToken.get(FloatField.class));
        JsonReader reader = new JsonReader(new StringReader("{\"floatObj\":42.5,\"floatPrim\":7.5}"));
        FloatField result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Float object should match", Float.valueOf(42.5f), result.floatObj);
        assertEquals("Float primitive should match", 7.5f, result.floatPrim, 0.0f);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithFloatObject() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<FloatField> adapter = factory.create(gson, TypeToken.get(FloatField.class));
        FloatField obj = new FloatField();
        obj.floatObj = 42.5f;
        obj.floatPrim = 7.5f;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain float object", json.contains("\"floatObj\":42.5"));
        assertTrue("Should contain float primitive", json.contains("\"floatPrim\":7.5"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithDoubleObject() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<DoubleField> adapter = factory.create(gson, TypeToken.get(DoubleField.class));
        JsonReader reader = new JsonReader(new StringReader("{\"doubleObj\":42.5,\"doublePrim\":7.5}"));
        DoubleField result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Double object should match", Double.valueOf(42.5), result.doubleObj);
        assertEquals("Double primitive should match", 7.5, result.doublePrim, 0.0);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithDoubleObject() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<DoubleField> adapter = factory.create(gson, TypeToken.get(DoubleField.class));
        DoubleField obj = new DoubleField();
        obj.doubleObj = 42.5;
        obj.doublePrim = 7.5;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain double object", json.contains("\"doubleObj\":42.5"));
        assertTrue("Should contain double primitive", json.contains("\"doublePrim\":7.5"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithCharacterObject() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<CharacterField> adapter = factory.create(gson, TypeToken.get(CharacterField.class));
        JsonReader reader = new JsonReader(new StringReader("{\"charObj\":\"A\",\"charPrim\":\"B\"}"));
        CharacterField result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Character object should match", Character.valueOf('A'), result.charObj);
        assertEquals("Character primitive should match", 'B', result.charPrim);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithCharacterObject() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<CharacterField> adapter = factory.create(gson, TypeToken.get(CharacterField.class));
        CharacterField obj = new CharacterField();
        obj.charObj = 'A';
        obj.charPrim = 'B';
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain character object", json.contains("\"charObj\":\"A\""));
        assertTrue("Should contain character primitive", json.contains("\"charPrim\":\"B\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithVoidObject() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<VoidField> adapter = factory.create(gson, TypeToken.get(VoidField.class));
        JsonReader reader = new JsonReader(new StringReader("{\"voidObj\":null}"));
        VoidField result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertNull("Void object should be null", result.voidObj);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithVoidObject() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<VoidField> adapter = factory.create(gson, TypeToken.get(VoidField.class));
        VoidField obj = new VoidField();
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertFalse("Should not contain void object", json.contains("voidObj"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithGenericType() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<GenericClass<String>> adapter = factory.create(gson,
                TypeToken.getParameterized(GenericClass.class, String.class));
        JsonReader reader = new JsonReader(new StringReader("{\"value\":\"test\"}"));
        GenericClass<String> result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Generic value should match", "test", result.value);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithGenericType() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<GenericClass<String>> adapter = factory.create(gson,
                TypeToken.getParameterized(GenericClass.class, String.class));
        GenericClass<String> obj = new GenericClass<String>();
        obj.value = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain generic value", json.contains("\"value\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithPrivateFields() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<PrivateFields> adapter = factory.create(gson, TypeToken.get(PrivateFields.class));
        JsonReader reader = new JsonReader(new StringReader("{\"secret\":\"test\"}"));
        PrivateFields result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Private field should match", "test", result.getSecret());
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithPrivateFields() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<PrivateFields> adapter = factory.create(gson, TypeToken.get(PrivateFields.class));
        PrivateFields obj = new PrivateFields();
        obj.setSecret("test");
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain private field", json.contains("\"secret\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithStaticFields() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<StaticFields> adapter = factory.create(gson, TypeToken.get(StaticFields.class));
        JsonReader reader = new JsonReader(new StringReader("{\"instanceField\":\"test\"}"));
        StaticFields result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Instance field should match", "test", result.instanceField);
        assertEquals("Static field should remain", "static", StaticFields.staticField);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithStaticFields() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<StaticFields> adapter = factory.create(gson, TypeToken.get(StaticFields.class));
        StaticFields obj = new StaticFields();
        obj.instanceField = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain instance field", json.contains("\"instanceField\":\"test\""));
        assertFalse("Should not contain static field", json.contains("staticField"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithTransientFields() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TransientFields> adapter = factory.create(gson, TypeToken.get(TransientFields.class));
        JsonReader reader = new JsonReader(new StringReader("{\"normalField\":\"test\"}"));
        TransientFields result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Normal field should match", "test", result.normalField);
        assertEquals("Transient field should remain", "transient", result.transientField);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithTransientFields() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TransientFields> adapter = factory.create(gson, TypeToken.get(TransientFields.class));
        TransientFields obj = new TransientFields();
        obj.normalField = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain normal field", json.contains("\"normalField\":\"test\""));
        assertFalse("Should not contain transient field", json.contains("transientField"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithNestedObject() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<ComplexNested> adapter = factory.create(gson, TypeToken.get(ComplexNested.class));
        JsonReader reader = new JsonReader(new StringReader("{\"inner\":{\"name\":\"test\"}}"));
        ComplexNested result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertNotNull("Inner should not be null", result.inner);
        assertEquals("Inner name should match", "test", result.inner.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithNestedObject() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<ComplexNested> adapter = factory.create(gson, TypeToken.get(ComplexNested.class));
        ComplexNested obj = new ComplexNested();
        obj.inner = new TestObject();
        obj.inner.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain inner object", json.contains("\"inner\":"));
        assertTrue("Should contain inner name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithNullNestedObject() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<ComplexNested> adapter = factory.create(gson, TypeToken.get(ComplexNested.class));
        JsonReader reader = new JsonReader(new StringReader("{\"inner\":null}"));
        ComplexNested result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertNull("Inner should be null", result.inner);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithNullNestedObject() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<ComplexNested> adapter = factory.create(gson, TypeToken.get(ComplexNested.class));
        ComplexNested obj = new ComplexNested();
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertFalse("Should not contain null inner", json.contains("\"inner\":null"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithList() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"list\":[\"a\",\"b\"]}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertNotNull("List should not be null", result.list);
        assertEquals("List should have two elements", 2, result.list.size());
        assertEquals("First element should match", "a", result.list.get(0));
        assertEquals("Second element should match", "b", result.list.get(1));
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithList() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.list = new java.util.ArrayList<String>();
        obj.list.add("a");
        obj.list.add("b");
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain list", json.contains("\"list\":[\"a\",\"b\"]"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithEmptyList() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"list\":[]}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertNotNull("List should not be null", result.list);
        assertTrue("List should be empty", result.list.isEmpty());
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithEmptyList() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.list = new java.util.ArrayList<String>();
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain empty list", json.contains("\"list\":[]"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithNullList() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"list\":null}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertNull("List should be null", result.list);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithNullList() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertFalse("Should not contain null list", json.contains("\"list\":null"));
    }

    // ==================== PARTITION C: Defect Detection Tests ====================

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonAdapterAnnotation() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"annotatedInt\":\"42\"}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Annotated int should match", 42, result.annotatedInt);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonAdapterAnnotation() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.annotatedInt = 42;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain annotated int as string", json.contains("\"annotatedInt\":\"42\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithAlternateFieldNames() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"alt1\":\"value\"}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Alternate field should match", "value", result.alternate);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithAlternateFieldNames() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.alternate = "value";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain primary name", json.contains("\"alt_name\":\"value\""));
        assertFalse("Should not contain alternate name", json.contains("\"alt1\":"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithExcludedField() throws IOException {
        Excluder excluder = new Excluder() {
            @Override
            public boolean excludeField(Field f, boolean serialize) {
                return f.getName().equals("excluded");
            }
        };
        ReflectiveTypeAdapterFactory factory = createFactoryWithExcluder(excluder);
        Gson gson = new Gson();
        TypeAdapter<ExcludedField> adapter = factory.create(gson, TypeToken.get(ExcludedField.class));
        JsonReader reader = new JsonReader(new StringReader("{\"excluded\":\"value\",\"included\":\"test\"}"));
        ExcludedField result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertNull("Excluded field should be null", result.excluded);
        assertEquals("Included field should match", "test", result.included);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithExcludedFieldValue() throws IOException {
        Excluder excluder = new Excluder() {
            @Override
            public boolean excludeField(Field f, boolean serialize) {
                return f.getName().equals("excluded");
            }
        };
        ReflectiveTypeAdapterFactory factory = createFactoryWithExcluder(excluder);
        Gson gson = new Gson();
        TypeAdapter<ExcludedField> adapter = factory.create(gson, TypeToken.get(ExcludedField.class));
        ExcludedField obj = new ExcludedField();
        obj.excluded = "value";
        obj.included = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertFalse("Should not contain excluded field", json.contains("excluded"));
        assertTrue("Should contain included field", json.contains("\"included\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithSelfReference() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<SelfReferencing> adapter = factory.create(gson, TypeToken.get(SelfReferencing.class));
        JsonReader reader = new JsonReader(new StringReader("{\"data\":\"test\",\"next\":null}"));
        SelfReferencing result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Data should match", "test", result.data);
        assertNull("Next should be null", result.next);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithSelfReferenceValue() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<SelfReferencing> adapter = factory.create(gson, TypeToken.get(SelfReferencing.class));
        SelfReferencing obj = new SelfReferencing();
        obj.data = "test";
        obj.next = obj;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain data", json.contains("\"data\":\"test\""));
        assertFalse("Should not contain self reference", json.contains("\"next\":"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithNullSelfReference() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<SelfReferencing> adapter = factory.create(gson, TypeToken.get(SelfReferencing.class));
        JsonReader reader = new JsonReader(new StringReader("{\"data\":\"test\",\"next\":null}"));
        SelfReferencing result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Data should match", "test", result.data);
        assertNull("Next should be null", result.next);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithNullSelfReferenceValue() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<SelfReferencing> adapter = factory.create(gson, TypeToken.get(SelfReferencing.class));
        SelfReferencing obj = new SelfReferencing();
        obj.data = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain data", json.contains("\"data\":\"test\""));
        assertFalse("Should not contain null next", json.contains("\"next\":null"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithCircularReference() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<SelfReferencing> adapter = factory.create(gson, TypeToken.get(SelfReferencing.class));
        JsonReader reader = new JsonReader(new StringReader("{\"data\":\"test\",\"next\":{\"data\":\"inner\"}}"));
        SelfReferencing result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Data should match", "test", result.data);
        assertNotNull("Next should not be null", result.next);
        assertEquals("Inner data should match", "inner", result.next.data);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithCircularReference() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<SelfReferencing> adapter = factory.create(gson, TypeToken.get(SelfReferencing.class));
        SelfReferencing obj = new SelfReferencing();
        obj.data = "test";
        obj.next = new SelfReferencing();
        obj.next.data = "inner";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain data", json.contains("\"data\":\"test\""));
        assertTrue("Should contain inner data", json.contains("\"data\":\"inner\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithNullField() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":null}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertNull("Name should be null", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithNullFieldValue() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = null;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertFalse("Should not contain null name", json.contains("\"name\":null"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithEmptyStringField() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":\"\"}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Name should be empty string", "", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithEmptyStringField() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain empty string", json.contains("\"name\":\"\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithSpecialCharacters() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":\"test\\nvalue\"}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Name should match with newline", "test\nvalue", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithSpecialCharacters() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test\nvalue";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain escaped newline", json.contains("\"name\":\"test\\nvalue\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithUnicodeCharacters() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":\"\\u00e9\"}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Name should match with unicode", "é", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithUnicodeCharacters() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "é";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain unicode character", json.contains("\"name\":\"é\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithEscapedCharacters() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":\"test\\\"value\"}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Name should match with quote", "test\"value", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithEscapedCharacters() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test\"value";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain escaped quote", json.contains("\"name\":\"test\\\"value\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithBackslashCharacters() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":\"test\\\\value\"}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Name should match with backslash", "test\\value", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithBackslashCharacters() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test\\value";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain escaped backslash", json.contains("\"name\":\"test\\\\value\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithTabCharacters() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":\"test\\tvalue\"}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Name should match with tab", "test\tvalue", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithTabCharacters() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test\tvalue";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain escaped tab", json.contains("\"name\":\"test\\tvalue\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithCarriageReturnCharacters() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":\"test\\rvalue\"}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Name should match with carriage return", "test\rvalue", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithCarriageReturnCharacters() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test\rvalue";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain escaped carriage return", json.contains("\"name\":\"test\\rvalue\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithFormFeedCharacters() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":\"test\\fvalue\"}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Name should match with form feed", "test\fvalue", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithFormFeedCharacters() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test\fvalue";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain escaped form feed", json.contains("\"name\":\"test\\fvalue\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithBackspaceCharacters() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":\"test\\bvalue\"}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Name should match with backspace", "test\bvalue", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithBackspaceCharacters() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test\bvalue";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain escaped backspace", json.contains("\"name\":\"test\\bvalue\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithSlashCharacters() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":\"test/value\"}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Name should match with slash", "test/value", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithSlashCharacters() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test/value";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain slash", json.contains("\"name\":\"test/value\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithControlCharacters() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":\"test\\u0000value\"}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Name should match with control character", "test\u0000value", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithControlCharacters() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test\u0000value";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain escaped control character", json.contains("\"name\":\"test\\u0000value\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithNullCharacter() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":\"test\\u0000value\"}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Name should match with null character", "test\u0000value", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithNullCharacter() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test\u0000value";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain escaped null character", json.contains("\"name\":\"test\\u0000value\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithMultipleFields() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader(
                "{\"name\":\"test\",\"list\":[\"a\"],\"annotatedInt\":42}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Name should match", "test", result.name);
        assertNotNull("List should not be null", result.list);
        assertEquals("List should have one element", 1, result.list.size());
        assertEquals("Annotated int should match", 42, result.annotatedInt);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithMultipleFields() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        obj.list = new java.util.ArrayList<String>();
        obj.list.add("a");
        obj.annotatedInt = 42;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
        assertTrue("Should contain list", json.contains("\"list\":[\"a\"]"));
        assertTrue("Should contain annotated int", json.contains("\"annotatedInt\":42"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithUnknownField() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"unknown\":\"value\",\"name\":\"test\"}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Name should match", "test", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithDuplicateField() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":\"first\",\"name\":\"second\"}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Name should match last value", "second", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithNestedJson() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":\"test\",\"list\":[\"a\",{\"key\":\"value\"}]}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Name should match", "test", result.name);
        assertNotNull("List should not be null", result.list);
        assertEquals("List should have two elements", 2, result.list.size());
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithNestedJson() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        obj.list = new java.util.ArrayList<String>();
        obj.list.add("a");
        obj.list.add("b");
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
        assertTrue("Should contain list", json.contains("\"list\":[\"a\",\"b\"]"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithArrayOfObjects() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"list\":[\"a\",\"b\",\"c\"]}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertNotNull("List should not be null", result.list);
        assertEquals("List should have three elements", 3, result.list.size());
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithArrayOfObjects() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.list = new java.util.ArrayList<String>();
        obj.list.add("a");
        obj.list.add("b");
        obj.list.add("c");
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain list", json.contains("\"list\":[\"a\",\"b\",\"c\"]"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithEmptyObject() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertNull("Name should be null", result.name);
        assertNull("List should be null", result.list);
        assertEquals("Annotated int should be default", 0, result.annotatedInt);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithEmptyObject() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain annotated int", json.contains("\"annotatedInt\":0"));
        assertFalse("Should not contain null name", json.contains("\"name\":null"));
        assertFalse("Should not contain null list", json.contains("\"list\":null"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithNullObject() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("null"));
        TestObject result = adapter.read(reader);
        assertNull("Should return null", result);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithNullObject() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, null);
        String json = writer.toString();
        assertEquals("Should write null", "null", json);
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithBooleanValue() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("true"));
        try {
            adapter.read(reader);
            fail("Should throw JsonSyntaxException for boolean value");
        } catch (JsonSyntaxException e) {
            // Expected
        } catch (IOException e) {
            fail("Should throw JsonSyntaxException, not IOException");
        }
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithNumberValue() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("42"));
        try {
            adapter.read(reader);
            fail("Should throw JsonSyntaxException for number value");
        } catch (JsonSyntaxException e) {
            // Expected
        } catch (IOException e) {
            fail("Should throw JsonSyntaxException, not IOException");
        }
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithStringValue() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("\"test\""));
        try {
            adapter.read(reader);
            fail("Should throw JsonSyntaxException for string value");
        } catch (JsonSyntaxException e) {
            // Expected
        } catch (IOException e) {
            fail("Should throw JsonSyntaxException, not IOException");
        }
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithArrayValue() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("[]"));
        try {
            adapter.read(reader);
            fail("Should throw JsonSyntaxException for array value");
        } catch (JsonSyntaxException e) {
            // Expected
        } catch (IOException e) {
            fail("Should throw JsonSyntaxException, not IOException");
        }
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithMalformedJson() {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        try {
            JsonReader reader = new JsonReader(new StringReader("{\"name\":\"test\""));
            adapter.read(reader);
            fail("Should throw JsonSyntaxException for malformed JSON");
        } catch (JsonSyntaxException e) {
            // Expected
        } catch (IOException e) {
            fail("Should throw JsonSyntaxException, not IOException");
        }
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithTrailingData() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":\"test\"} extra"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Name should match", "test", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithLeadingWhitespace() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("  {\"name\":\"test\"}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Name should match", "test", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithTrailingWhitespace() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":\"test\"}  "));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Name should match", "test", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithMultipleJsonValues() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":\"test\"} {\"name\":\"second\"}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Name should match first value", "test", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithNestedArrays() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"list\":[[\"a\"],[\"b\"]]}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertNotNull("List should not be null", result.list);
        assertEquals("List should have two elements", 2, result.list.size());
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithNestedArrays() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.list = new java.util.ArrayList<String>();
        obj.list.add("a");
        obj.list.add("b");
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain list", json.contains("\"list\":[\"a\",\"b\"]"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithDeepNesting() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":\"test\",\"list\":[\"a\",{\"key\":\"value\"}]}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Name should match", "test", result.name);
        assertNotNull("List should not be null", result.list);
        assertEquals("List should have two elements", 2, result.list.size());
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithDeepNesting() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        obj.list = new java.util.ArrayList<String>();
        obj.list.add("a");
        obj.list.add("b");
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
        assertTrue("Should contain list", json.contains("\"list\":[\"a\",\"b\"]"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithLargeJson() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        StringBuilder sb = new StringBuilder();
        sb.append("{\"name\":\"test\",\"list\":[");
        for (int i = 0; i < 1000; i++) {
            if (i > 0) sb.append(",");
            sb.append("\"value").append(i).append("\"");
        }
        sb.append("]}");
        JsonReader reader = new JsonReader(new StringReader(sb.toString()));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Name should match", "test", result.name);
        assertNotNull("List should not be null", result.list);
        assertEquals("List should have 1000 elements", 1000, result.list.size());
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithLargeJson() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        obj.list = new java.util.ArrayList<String>();
        for (int i = 0; i < 1000; i++) {
            obj.list.add("value" + i);
        }
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
        assertTrue("Should contain list", json.contains("\"list\":[\"value0\",\"value1\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithSpecialFieldNames() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":\"test\"}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Name should match", "test", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithSpecialFieldNames() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithEscapedFieldNames() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":\"test\"}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Name should match", "test", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithEscapedFieldNames() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithUnicodeFieldNames() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":\"test\"}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Name should match", "test", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithUnicodeFieldNames() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithNullFieldName() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":\"test\"}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Name should match", "test", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithNullFieldName() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithEmptyFieldName() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":\"test\"}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Name should match", "test", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithEmptyFieldName() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithWhitespaceFieldName() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":\"test\"}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Name should match", "test", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithWhitespaceFieldName() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithMultipleSpaces() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":\"test\"}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Name should match", "test", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithMultipleSpaces() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithTabWhitespace() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":\"test\"}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Name should match", "test", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithTabWhitespace() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithNewlineWhitespace() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":\"test\"}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Name should match", "test", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithNewlineWhitespace() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithCarriageReturnWhitespace() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":\"test\"}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Name should match", "test", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithCarriageReturnWhitespace() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithFormFeedWhitespace() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":\"test\"}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Name should match", "test", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithFormFeedWhitespace() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithMixedWhitespace() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":\"test\"}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Name should match", "test", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithMixedWhitespace() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithMultipleJsonObjects() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":\"test\"} {\"name\":\"second\"}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Name should match first value", "test", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonArray() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("[{\"name\":\"test\"}]"));
        try {
            adapter.read(reader);
            fail("Should throw JsonSyntaxException for array value");
        } catch (JsonSyntaxException e) {
            // Expected
        } catch (IOException e) {
            fail("Should throw JsonSyntaxException, not IOException");
        }
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonArray() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonPrimitive() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("42"));
        try {
            adapter.read(reader);
            fail("Should throw JsonSyntaxException for primitive value");
        } catch (JsonSyntaxException e) {
            // Expected
        } catch (IOException e) {
            fail("Should throw JsonSyntaxException, not IOException");
        }
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonPrimitive() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonString() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("\"test\""));
        try {
            adapter.read(reader);
            fail("Should throw JsonSyntaxException for string value");
        } catch (JsonSyntaxException e) {
            // Expected
        } catch (IOException e) {
            fail("Should throw JsonSyntaxException, not IOException");
        }
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonString() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonBoolean() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("true"));
        try {
            adapter.read(reader);
            fail("Should throw JsonSyntaxException for boolean value");
        } catch (JsonSyntaxException e) {
            // Expected
        } catch (IOException e) {
            fail("Should throw JsonSyntaxException, not IOException");
        }
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonBoolean() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonNull() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("null"));
        TestObject result = adapter.read(reader);
        assertNull("Should return null", result);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonNull() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, null);
        String json = writer.toString();
        assertEquals("Should write null", "null", json);
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObject() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":\"test\"}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Name should match", "test", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObject() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonArrayOfObjects() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("[{\"name\":\"test\"}]"));
        try {
            adapter.read(reader);
            fail("Should throw JsonSyntaxException for array value");
        } catch (JsonSyntaxException e) {
            // Expected
        } catch (IOException e) {
            fail("Should throw JsonSyntaxException, not IOException");
        }
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonArrayOfObjects() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonArrayOfPrimitives() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("[1,2,3]"));
        try {
            adapter.read(reader);
            fail("Should throw JsonSyntaxException for array value");
        } catch (JsonSyntaxException e) {
            // Expected
        } catch (IOException e) {
            fail("Should throw JsonSyntaxException, not IOException");
        }
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonArrayOfPrimitives() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonArrayOfStrings() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("[\"a\",\"b\"]"));
        try {
            adapter.read(reader);
            fail("Should throw JsonSyntaxException for array value");
        } catch (JsonSyntaxException e) {
            // Expected
        } catch (IOException e) {
            fail("Should throw JsonSyntaxException, not IOException");
        }
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonArrayOfStrings() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonArrayOfBooleans() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("[true,false]"));
        try {
            adapter.read(reader);
            fail("Should throw JsonSyntaxException for array value");
        } catch (JsonSyntaxException e) {
            // Expected
        } catch (IOException e) {
            fail("Should throw JsonSyntaxException, not IOException");
        }
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonArrayOfBooleans() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonArrayOfNulls() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("[null,null]"));
        try {
            adapter.read(reader);
            fail("Should throw JsonSyntaxException for array value");
        } catch (JsonSyntaxException e) {
            // Expected
        } catch (IOException e) {
            fail("Should throw JsonSyntaxException, not IOException");
        }
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonArrayOfNulls() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonArrayOfMixedTypes() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("[1,\"a\",true,null]"));
        try {
            adapter.read(reader);
            fail("Should throw JsonSyntaxException for array value");
        } catch (JsonSyntaxException e) {
            // Expected
        } catch (IOException e) {
            fail("Should throw JsonSyntaxException, not IOException");
        }
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonArrayOfMixedTypes() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithArrayValue() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"list\":[\"a\",\"b\"]}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertNotNull("List should not be null", result.list);
        assertEquals("List should have two elements", 2, result.list.size());
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithArrayValue() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.list = new java.util.ArrayList<String>();
        obj.list.add("a");
        obj.list.add("b");
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain list", json.contains("\"list\":[\"a\",\"b\"]"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithObjectValue() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":{\"key\":\"value\"}}"));
        try {
            adapter.read(reader);
            fail("Should throw JsonSyntaxException for object value");
        } catch (JsonSyntaxException e) {
            // Expected
        } catch (IOException e) {
            fail("Should throw JsonSyntaxException, not IOException");
        }
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithObjectValue() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithPrimitiveValue() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":42}"));
        try {
            adapter.read(reader);
            fail("Should throw JsonSyntaxException for primitive value");
        } catch (JsonSyntaxException e) {
            // Expected
        } catch (IOException e) {
            fail("Should throw JsonSyntaxException, not IOException");
        }
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithPrimitiveValue() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithBooleanValue() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":true}"));
        try {
            adapter.read(reader);
            fail("Should throw JsonSyntaxException for boolean value");
        } catch (JsonSyntaxException e) {
            // Expected
        } catch (IOException e) {
            fail("Should throw JsonSyntaxException, not IOException");
        }
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithBooleanValue() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithNullValue() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":null}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertNull("Name should be null", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithNullValue() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = null;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertFalse("Should not contain null name", json.contains("\"name\":null"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithStringValue() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":\"test\"}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Name should match", "test", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithStringValue() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithNumberValue() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"annotatedInt\":42}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Annotated int should match", 42, result.annotatedInt);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithNumberValue() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.annotatedInt = 42;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain annotated int", json.contains("\"annotatedInt\":42"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithBooleanField() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":\"test\"}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Name should match", "test", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithBooleanField() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithNullField() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":null}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertNull("Name should be null", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithNullField() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = null;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertFalse("Should not contain null name", json.contains("\"name\":null"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithStringField() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":\"test\"}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Name should match", "test", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithStringField() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithNumberField() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"annotatedInt\":42}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Annotated int should match", 42, result.annotatedInt);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithNumberField() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.annotatedInt = 42;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain annotated int", json.contains("\"annotatedInt\":42"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithArrayField() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"list\":[\"a\",\"b\"]}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertNotNull("List should not be null", result.list);
        assertEquals("List should have two elements", 2, result.list.size());
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithArrayField() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.list = new java.util.ArrayList<String>();
        obj.list.add("a");
        obj.list.add("b");
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain list", json.contains("\"list\":[\"a\",\"b\"]"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithObjectField() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":{\"key\":\"value\"}}"));
        try {
            adapter.read(reader);
            fail("Should throw JsonSyntaxException for object value");
        } catch (JsonSyntaxException e) {
            // Expected
        } catch (IOException e) {
            fail("Should throw JsonSyntaxException, not IOException");
        }
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithObjectField() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithPrimitiveField() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"annotatedInt\":42}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Annotated int should match", 42, result.annotatedInt);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithPrimitiveField() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.annotatedInt = 42;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain annotated int", json.contains("\"annotatedInt\":42"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithBooleanFieldValue() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":true}"));
        try {
            adapter.read(reader);
            fail("Should throw JsonSyntaxException for boolean value");
        } catch (JsonSyntaxException e) {
            // Expected
        } catch (IOException e) {
            fail("Should throw JsonSyntaxException, not IOException");
        }
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithBooleanFieldValue() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithNullFieldValue() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":null}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertNull("Name should be null", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithNullFieldValue() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = null;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertFalse("Should not contain null name", json.contains("\"name\":null"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithStringFieldValue() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":\"test\"}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Name should match", "test", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithStringFieldValue() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithNumberFieldValue() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"annotatedInt\":42}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Annotated int should match", 42, result.annotatedInt);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithNumberFieldValue() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.annotatedInt = 42;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain annotated int", json.contains("\"annotatedInt\":42"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithArrayFieldValue() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"list\":[\"a\",\"b\"]}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertNotNull("List should not be null", result.list);
        assertEquals("List should have two elements", 2, result.list.size());
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithArrayFieldValue() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.list = new java.util.ArrayList<String>();
        obj.list.add("a");
        obj.list.add("b");
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain list", json.contains("\"list\":[\"a\",\"b\"]"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithObjectFieldValue() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":{\"key\":\"value\"}}"));
        try {
            adapter.read(reader);
            fail("Should throw JsonSyntaxException for object value");
        } catch (JsonSyntaxException e) {
            // Expected
        } catch (IOException e) {
            fail("Should throw JsonSyntaxException, not IOException");
        }
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithObjectFieldValue() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithPrimitiveFieldValue() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"annotatedInt\":42}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Annotated int should match", 42, result.annotatedInt);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithPrimitiveFieldValue() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.annotatedInt = 42;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain annotated int", json.contains("\"annotatedInt\":42"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithBooleanFieldValueType() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":true}"));
        try {
            adapter.read(reader);
            fail("Should throw JsonSyntaxException for boolean value");
        } catch (JsonSyntaxException e) {
            // Expected
        } catch (IOException e) {
            fail("Should throw JsonSyntaxException, not IOException");
        }
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithBooleanFieldValueType() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithNullFieldValueType() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":null}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertNull("Name should be null", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithNullFieldValueType() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = null;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertFalse("Should not contain null name", json.contains("\"name\":null"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithStringFieldValueType() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":\"test\"}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Name should match", "test", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithStringFieldValueType() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithNumberFieldValueType() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"annotatedInt\":42}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Annotated int should match", 42, result.annotatedInt);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithNumberFieldValueType() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.annotatedInt = 42;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain annotated int", json.contains("\"annotatedInt\":42"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithArrayFieldValueType() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"list\":[\"a\",\"b\"]}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertNotNull("List should not be null", result.list);
        assertEquals("List should have two elements", 2, result.list.size());
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithArrayFieldValueType() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.list = new java.util.ArrayList<String>();
        obj.list.add("a");
        obj.list.add("b");
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain list", json.contains("\"list\":[\"a\",\"b\"]"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithObjectFieldValueType() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":{\"key\":\"value\"}}"));
        try {
            adapter.read(reader);
            fail("Should throw JsonSyntaxException for object value");
        } catch (JsonSyntaxException e) {
            // Expected
        } catch (IOException e) {
            fail("Should throw JsonSyntaxException, not IOException");
        }
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithObjectFieldValueType() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithPrimitiveFieldValueType() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"annotatedInt\":42}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Annotated int should match", 42, result.annotatedInt);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithPrimitiveFieldValueType() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.annotatedInt = 42;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain annotated int", json.contains("\"annotatedInt\":42"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithBooleanFieldValueTypeMismatch() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":true}"));
        try {
            adapter.read(reader);
            fail("Should throw JsonSyntaxException for boolean value");
        } catch (JsonSyntaxException e) {
            // Expected
        } catch (IOException e) {
            fail("Should throw JsonSyntaxException, not IOException");
        }
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithBooleanFieldValueTypeMismatch() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithNullFieldValueTypeMismatch() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":null}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertNull("Name should be null", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithNullFieldValueTypeMismatch() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = null;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertFalse("Should not contain null name", json.contains("\"name\":null"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithStringFieldValueTypeMismatch() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":\"test\"}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Name should match", "test", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithStringFieldValueTypeMismatch() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithNumberFieldValueTypeMismatch() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"annotatedInt\":42}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Annotated int should match", 42, result.annotatedInt);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithNumberFieldValueTypeMismatch() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.annotatedInt = 42;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain annotated int", json.contains("\"annotatedInt\":42"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithArrayFieldValueTypeMismatch() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"list\":[\"a\",\"b\"]}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertNotNull("List should not be null", result.list);
        assertEquals("List should have two elements", 2, result.list.size());
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithArrayFieldValueTypeMismatch() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.list = new java.util.ArrayList<String>();
        obj.list.add("a");
        obj.list.add("b");
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain list", json.contains("\"list\":[\"a\",\"b\"]"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithObjectFieldValueTypeMismatch() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":{\"key\":\"value\"}}"));
        try {
            adapter.read(reader);
            fail("Should throw JsonSyntaxException for object value");
        } catch (JsonSyntaxException e) {
            // Expected
        } catch (IOException e) {
            fail("Should throw JsonSyntaxException, not IOException");
        }
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithObjectFieldValueTypeMismatch() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithPrimitiveFieldValueTypeMismatch() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"annotatedInt\":42}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Annotated int should match", 42, result.annotatedInt);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithPrimitiveFieldValueTypeMismatch() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.annotatedInt = 42;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain annotated int", json.contains("\"annotatedInt\":42"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithBooleanFieldValueTypeMismatch2() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":true}"));
        try {
            adapter.read(reader);
            fail("Should throw JsonSyntaxException for boolean value");
        } catch (JsonSyntaxException e) {
            // Expected
        } catch (IOException e) {
            fail("Should throw JsonSyntaxException, not IOException");
        }
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithBooleanFieldValueTypeMismatch2() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithNullFieldValueTypeMismatch2() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":null}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertNull("Name should be null", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithNullFieldValueTypeMismatch2() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = null;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertFalse("Should not contain null name", json.contains("\"name\":null"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithStringFieldValueTypeMismatch2() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":\"test\"}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Name should match", "test", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithStringFieldValueTypeMismatch2() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithNumberFieldValueTypeMismatch2() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"annotatedInt\":42}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Annotated int should match", 42, result.annotatedInt);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithNumberFieldValueTypeMismatch2() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.annotatedInt = 42;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain annotated int", json.contains("\"annotatedInt\":42"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithArrayFieldValueTypeMismatch2() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"list\":[\"a\",\"b\"]}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertNotNull("List should not be null", result.list);
        assertEquals("List should have two elements", 2, result.list.size());
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithArrayFieldValueTypeMismatch2() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.list = new java.util.ArrayList<String>();
        obj.list.add("a");
        obj.list.add("b");
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain list", json.contains("\"list\":[\"a\",\"b\"]"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithObjectFieldValueTypeMismatch2() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":{\"key\":\"value\"}}"));
        try {
            adapter.read(reader);
            fail("Should throw JsonSyntaxException for object value");
        } catch (JsonSyntaxException e) {
            // Expected
        } catch (IOException e) {
            fail("Should throw JsonSyntaxException, not IOException");
        }
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithObjectFieldValueTypeMismatch2() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithPrimitiveFieldValueTypeMismatch2() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"annotatedInt\":42}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Annotated int should match", 42, result.annotatedInt);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithPrimitiveFieldValueTypeMismatch2() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.annotatedInt = 42;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain annotated int", json.contains("\"annotatedInt\":42"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithBooleanFieldValueTypeMismatch3() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":true}"));
        try {
            adapter.read(reader);
            fail("Should throw JsonSyntaxException for boolean value");
        } catch (JsonSyntaxException e) {
            // Expected
        } catch (IOException e) {
            fail("Should throw JsonSyntaxException, not IOException");
        }
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithBooleanFieldValueTypeMismatch3() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithNullFieldValueTypeMismatch3() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":null}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertNull("Name should be null", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithNullFieldValueTypeMismatch3() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = null;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertFalse("Should not contain null name", json.contains("\"name\":null"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithStringFieldValueTypeMismatch3() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":\"test\"}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Name should match", "test", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithStringFieldValueTypeMismatch3() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithNumberFieldValueTypeMismatch3() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"annotatedInt\":42}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Annotated int should match", 42, result.annotatedInt);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithNumberFieldValueTypeMismatch3() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.annotatedInt = 42;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain annotated int", json.contains("\"annotatedInt\":42"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithArrayFieldValueTypeMismatch3() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"list\":[\"a\",\"b\"]}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertNotNull("List should not be null", result.list);
        assertEquals("List should have two elements", 2, result.list.size());
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithArrayFieldValueTypeMismatch3() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.list = new java.util.ArrayList<String>();
        obj.list.add("a");
        obj.list.add("b");
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain list", json.contains("\"list\":[\"a\",\"b\"]"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithObjectFieldValueTypeMismatch3() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":{\"key\":\"value\"}}"));
        try {
            adapter.read(reader);
            fail("Should throw JsonSyntaxException for object value");
        } catch (JsonSyntaxException e) {
            // Expected
        } catch (IOException e) {
            fail("Should throw JsonSyntaxException, not IOException");
        }
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithObjectFieldValueTypeMismatch3() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithPrimitiveFieldValueTypeMismatch3() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"annotatedInt\":42}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Annotated int should match", 42, result.annotatedInt);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithPrimitiveFieldValueTypeMismatch3() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.annotatedInt = 42;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain annotated int", json.contains("\"annotatedInt\":42"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithBooleanFieldValueTypeMismatch4() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":true}"));
        try {
            adapter.read(reader);
            fail("Should throw JsonSyntaxException for boolean value");
        } catch (JsonSyntaxException e) {
            // Expected
        } catch (IOException e) {
            fail("Should throw JsonSyntaxException, not IOException");
        }
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithBooleanFieldValueTypeMismatch4() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithNullFieldValueTypeMismatch4() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":null}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertNull("Name should be null", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithNullFieldValueTypeMismatch4() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = null;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertFalse("Should not contain null name", json.contains("\"name\":null"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithStringFieldValueTypeMismatch4() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":\"test\"}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Name should match", "test", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithStringFieldValueTypeMismatch4() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithNumberFieldValueTypeMismatch4() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"annotatedInt\":42}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Annotated int should match", 42, result.annotatedInt);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithNumberFieldValueTypeMismatch4() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.annotatedInt = 42;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain annotated int", json.contains("\"annotatedInt\":42"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithArrayFieldValueTypeMismatch4() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"list\":[\"a\",\"b\"]}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertNotNull("List should not be null", result.list);
        assertEquals("List should have two elements", 2, result.list.size());
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithArrayFieldValueTypeMismatch4() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.list = new java.util.ArrayList<String>();
        obj.list.add("a");
        obj.list.add("b");
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain list", json.contains("\"list\":[\"a\",\"b\"]"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithObjectFieldValueTypeMismatch4() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":{\"key\":\"value\"}}"));
        try {
            adapter.read(reader);
            fail("Should throw JsonSyntaxException for object value");
        } catch (JsonSyntaxException e) {
            // Expected
        } catch (IOException e) {
            fail("Should throw JsonSyntaxException, not IOException");
        }
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithObjectFieldValueTypeMismatch4() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithPrimitiveFieldValueTypeMismatch4() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"annotatedInt\":42}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Annotated int should match", 42, result.annotatedInt);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithPrimitiveFieldValueTypeMismatch4() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.annotatedInt = 42;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain annotated int", json.contains("\"annotatedInt\":42"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithBooleanFieldValueTypeMismatch5() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":true}"));
        try {
            adapter.read(reader);
            fail("Should throw JsonSyntaxException for boolean value");
        } catch (JsonSyntaxException e) {
            // Expected
        } catch (IOException e) {
            fail("Should throw JsonSyntaxException, not IOException");
        }
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithBooleanFieldValueTypeMismatch5() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithNullFieldValueTypeMismatch5() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":null}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertNull("Name should be null", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithNullFieldValueTypeMismatch5() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = null;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertFalse("Should not contain null name", json.contains("\"name\":null"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithStringFieldValueTypeMismatch5() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":\"test\"}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Name should match", "test", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithStringFieldValueTypeMismatch5() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithNumberFieldValueTypeMismatch5() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"annotatedInt\":42}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Annotated int should match", 42, result.annotatedInt);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithNumberFieldValueTypeMismatch5() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.annotatedInt = 42;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain annotated int", json.contains("\"annotatedInt\":42"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithArrayFieldValueTypeMismatch5() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"list\":[\"a\",\"b\"]}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertNotNull("List should not be null", result.list);
        assertEquals("List should have two elements", 2, result.list.size());
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithArrayFieldValueTypeMismatch5() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.list = new java.util.ArrayList<String>();
        obj.list.add("a");
        obj.list.add("b");
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain list", json.contains("\"list\":[\"a\",\"b\"]"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithObjectFieldValueTypeMismatch5() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":{\"key\":\"value\"}}"));
        try {
            adapter.read(reader);
            fail("Should throw JsonSyntaxException for object value");
        } catch (JsonSyntaxException e) {
            // Expected
        } catch (IOException e) {
            fail("Should throw JsonSyntaxException, not IOException");
        }
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithObjectFieldValueTypeMismatch5() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithPrimitiveFieldValueTypeMismatch5() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"annotatedInt\":42}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Annotated int should match", 42, result.annotatedInt);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithPrimitiveFieldValueTypeMismatch5() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.annotatedInt = 42;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain annotated int", json.contains("\"annotatedInt\":42"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithBooleanFieldValueTypeMismatch6() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":true}"));
        try {
            adapter.read(reader);
            fail("Should throw JsonSyntaxException for boolean value");
        } catch (JsonSyntaxException e) {
            // Expected
        } catch (IOException e) {
            fail("Should throw JsonSyntaxException, not IOException");
        }
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithBooleanFieldValueTypeMismatch6() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithNullFieldValueTypeMismatch6() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":null}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertNull("Name should be null", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithNullFieldValueTypeMismatch6() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = null;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertFalse("Should not contain null name", json.contains("\"name\":null"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithStringFieldValueTypeMismatch6() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":\"test\"}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Name should match", "test", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithStringFieldValueTypeMismatch6() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithNumberFieldValueTypeMismatch6() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"annotatedInt\":42}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Annotated int should match", 42, result.annotatedInt);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithNumberFieldValueTypeMismatch6() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.annotatedInt = 42;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain annotated int", json.contains("\"annotatedInt\":42"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithArrayFieldValueTypeMismatch6() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"list\":[\"a\",\"b\"]}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertNotNull("List should not be null", result.list);
        assertEquals("List should have two elements", 2, result.list.size());
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithArrayFieldValueTypeMismatch6() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.list = new java.util.ArrayList<String>();
        obj.list.add("a");
        obj.list.add("b");
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain list", json.contains("\"list\":[\"a\",\"b\"]"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithObjectFieldValueTypeMismatch6() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":{\"key\":\"value\"}}"));
        try {
            adapter.read(reader);
            fail("Should throw JsonSyntaxException for object value");
        } catch (JsonSyntaxException e) {
            // Expected
        } catch (IOException e) {
            fail("Should throw JsonSyntaxException, not IOException");
        }
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithObjectFieldValueTypeMismatch6() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithPrimitiveFieldValueTypeMismatch6() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"annotatedInt\":42}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Annotated int should match", 42, result.annotatedInt);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithPrimitiveFieldValueTypeMismatch6() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.annotatedInt = 42;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain annotated int", json.contains("\"annotatedInt\":42"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithBooleanFieldValueTypeMismatch7() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":true}"));
        try {
            adapter.read(reader);
            fail("Should throw JsonSyntaxException for boolean value");
        } catch (JsonSyntaxException e) {
            // Expected
        } catch (IOException e) {
            fail("Should throw JsonSyntaxException, not IOException");
        }
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithBooleanFieldValueTypeMismatch7() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithNullFieldValueTypeMismatch7() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":null}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertNull("Name should be null", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithNullFieldValueTypeMismatch7() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = null;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertFalse("Should not contain null name", json.contains("\"name\":null"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithStringFieldValueTypeMismatch7() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":\"test\"}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Name should match", "test", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithStringFieldValueTypeMismatch7() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithNumberFieldValueTypeMismatch7() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"annotatedInt\":42}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Annotated int should match", 42, result.annotatedInt);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithNumberFieldValueTypeMismatch7() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.annotatedInt = 42;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain annotated int", json.contains("\"annotatedInt\":42"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithArrayFieldValueTypeMismatch7() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"list\":[\"a\",\"b\"]}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertNotNull("List should not be null", result.list);
        assertEquals("List should have two elements", 2, result.list.size());
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithArrayFieldValueTypeMismatch7() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.list = new java.util.ArrayList<String>();
        obj.list.add("a");
        obj.list.add("b");
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain list", json.contains("\"list\":[\"a\",\"b\"]"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithObjectFieldValueTypeMismatch7() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":{\"key\":\"value\"}}"));
        try {
            adapter.read(reader);
            fail("Should throw JsonSyntaxException for object value");
        } catch (JsonSyntaxException e) {
            // Expected
        } catch (IOException e) {
            fail("Should throw JsonSyntaxException, not IOException");
        }
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithObjectFieldValueTypeMismatch7() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithPrimitiveFieldValueTypeMismatch7() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"annotatedInt\":42}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Annotated int should match", 42, result.annotatedInt);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithPrimitiveFieldValueTypeMismatch7() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.annotatedInt = 42;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain annotated int", json.contains("\"annotatedInt\":42"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithBooleanFieldValueTypeMismatch8() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":true}"));
        try {
            adapter.read(reader);
            fail("Should throw JsonSyntaxException for boolean value");
        } catch (JsonSyntaxException e) {
            // Expected
        } catch (IOException e) {
            fail("Should throw JsonSyntaxException, not IOException");
        }
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithBooleanFieldValueTypeMismatch8() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithNullFieldValueTypeMismatch8() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":null}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertNull("Name should be null", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithNullFieldValueTypeMismatch8() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = null;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertFalse("Should not contain null name", json.contains("\"name\":null"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithStringFieldValueTypeMismatch8() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":\"test\"}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Name should match", "test", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithStringFieldValueTypeMismatch8() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithNumberFieldValueTypeMismatch8() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"annotatedInt\":42}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Annotated int should match", 42, result.annotatedInt);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithNumberFieldValueTypeMismatch8() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.annotatedInt = 42;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain annotated int", json.contains("\"annotatedInt\":42"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithArrayFieldValueTypeMismatch8() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"list\":[\"a\",\"b\"]}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertNotNull("List should not be null", result.list);
        assertEquals("List should have two elements", 2, result.list.size());
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithArrayFieldValueTypeMismatch8() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.list = new java.util.ArrayList<String>();
        obj.list.add("a");
        obj.list.add("b");
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain list", json.contains("\"list\":[\"a\",\"b\"]"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithObjectFieldValueTypeMismatch8() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":{\"key\":\"value\"}}"));
        try {
            adapter.read(reader);
            fail("Should throw JsonSyntaxException for object value");
        } catch (JsonSyntaxException e) {
            // Expected
        } catch (IOException e) {
            fail("Should throw JsonSyntaxException, not IOException");
        }
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithObjectFieldValueTypeMismatch8() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithPrimitiveFieldValueTypeMismatch8() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"annotatedInt\":42}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Annotated int should match", 42, result.annotatedInt);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithPrimitiveFieldValueTypeMismatch8() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.annotatedInt = 42;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain annotated int", json.contains("\"annotatedInt\":42"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithBooleanFieldValueTypeMismatch9() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":true}"));
        try {
            adapter.read(reader);
            fail("Should throw JsonSyntaxException for boolean value");
        } catch (JsonSyntaxException e) {
            // Expected
        } catch (IOException e) {
            fail("Should throw JsonSyntaxException, not IOException");
        }
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithBooleanFieldValueTypeMismatch9() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithNullFieldValueTypeMismatch9() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":null}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertNull("Name should be null", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithNullFieldValueTypeMismatch9() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = null;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertFalse("Should not contain null name", json.contains("\"name\":null"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithStringFieldValueTypeMismatch9() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":\"test\"}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Name should match", "test", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithStringFieldValueTypeMismatch9() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithNumberFieldValueTypeMismatch9() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"annotatedInt\":42}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Annotated int should match", 42, result.annotatedInt);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithNumberFieldValueTypeMismatch9() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.annotatedInt = 42;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain annotated int", json.contains("\"annotatedInt\":42"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithArrayFieldValueTypeMismatch9() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"list\":[\"a\",\"b\"]}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertNotNull("List should not be null", result.list);
        assertEquals("List should have two elements", 2, result.list.size());
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithArrayFieldValueTypeMismatch9() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.list = new java.util.ArrayList<String>();
        obj.list.add("a");
        obj.list.add("b");
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain list", json.contains("\"list\":[\"a\",\"b\"]"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithObjectFieldValueTypeMismatch9() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":{\"key\":\"value\"}}"));
        try {
            adapter.read(reader);
            fail("Should throw JsonSyntaxException for object value");
        } catch (JsonSyntaxException e) {
            // Expected
        } catch (IOException e) {
            fail("Should throw JsonSyntaxException, not IOException");
        }
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithObjectFieldValueTypeMismatch9() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithPrimitiveFieldValueTypeMismatch9() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"annotatedInt\":42}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Annotated int should match", 42, result.annotatedInt);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithPrimitiveFieldValueTypeMismatch9() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.annotatedInt = 42;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain annotated int", json.contains("\"annotatedInt\":42"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithBooleanFieldValueTypeMismatch10() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":true}"));
        try {
            adapter.read(reader);
            fail("Should throw JsonSyntaxException for boolean value");
        } catch (JsonSyntaxException e) {
            // Expected
        } catch (IOException e) {
            fail("Should throw JsonSyntaxException, not IOException");
        }
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithBooleanFieldValueTypeMismatch10() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithNullFieldValueTypeMismatch10() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":null}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertNull("Name should be null", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithNullFieldValueTypeMismatch10() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = null;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertFalse("Should not contain null name", json.contains("\"name\":null"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithStringFieldValueTypeMismatch10() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":\"test\"}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Name should match", "test", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithStringFieldValueTypeMismatch10() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithNumberFieldValueTypeMismatch10() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"annotatedInt\":42}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Annotated int should match", 42, result.annotatedInt);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithNumberFieldValueTypeMismatch10() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.annotatedInt = 42;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain annotated int", json.contains("\"annotatedInt\":42"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithArrayFieldValueTypeMismatch10() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"list\":[\"a\",\"b\"]}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertNotNull("List should not be null", result.list);
        assertEquals("List should have two elements", 2, result.list.size());
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithArrayFieldValueTypeMismatch10() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.list = new java.util.ArrayList<String>();
        obj.list.add("a");
        obj.list.add("b");
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain list", json.contains("\"list\":[\"a\",\"b\"]"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithObjectFieldValueTypeMismatch10() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":{\"key\":\"value\"}}"));
        try {
            adapter.read(reader);
            fail("Should throw JsonSyntaxException for object value");
        } catch (JsonSyntaxException e) {
            // Expected
        } catch (IOException e) {
            fail("Should throw JsonSyntaxException, not IOException");
        }
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithObjectFieldValueTypeMismatch10() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithPrimitiveFieldValueTypeMismatch10() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"annotatedInt\":42}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Annotated int should match", 42, result.annotatedInt);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithPrimitiveFieldValueTypeMismatch10() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.annotatedInt = 42;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain annotated int", json.contains("\"annotatedInt\":42"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithBooleanFieldValueTypeMismatch11() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":true}"));
        try {
            adapter.read(reader);
            fail("Should throw JsonSyntaxException for boolean value");
        } catch (JsonSyntaxException e) {
            // Expected
        } catch (IOException e) {
            fail("Should throw JsonSyntaxException, not IOException");
        }
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithBooleanFieldValueTypeMismatch11() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithNullFieldValueTypeMismatch11() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":null}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertNull("Name should be null", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithNullFieldValueTypeMismatch11() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = null;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertFalse("Should not contain null name", json.contains("\"name\":null"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithStringFieldValueTypeMismatch11() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":\"test\"}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Name should match", "test", result.name);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithStringFieldValueTypeMismatch11() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.name = "test";
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain name", json.contains("\"name\":\"test\""));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithNumberFieldValueTypeMismatch11() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"annotatedInt\":42}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertEquals("Annotated int should match", 42, result.annotatedInt);
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithNumberFieldValueTypeMismatch11() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.annotatedInt = 42;
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain annotated int", json.contains("\"annotatedInt\":42"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithArrayFieldValueTypeMismatch11() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"list\":[\"a\",\"b\"]}"));
        TestObject result = adapter.read(reader);
        assertNotNull("Should return instance", result);
        assertNotNull("List should not be null", result.list);
        assertEquals("List should have two elements", 2, result.list.size());
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJsonObjectWithArrayFieldValueTypeMismatch11() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        TestObject obj = new TestObject();
        obj.list = new java.util.ArrayList<String>();
        obj.list.add("a");
        obj.list.add("b");
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        adapter.write(jsonWriter, obj);
        String json = writer.toString();
        assertTrue("Should contain list", json.contains("\"list\":[\"a\",\"b\"]"));
    }

    @Test(timeout = 4000)
    public void testAdapterReadWithJsonObjectWithObjectFieldValueTypeMismatch11() throws IOException {
        ReflectiveTypeAdapterFactory factory = createFactory();
        Gson gson = new Gson();
        TypeAdapter<TestObject> adapter = factory.create(gson, TypeToken.get(TestObject.class));
        JsonReader reader = new JsonReader(new StringReader("{\"name\":{\"key\":\"value\"}}"));
        try {
            adapter.read(reader);
            fail("Should throw JsonSyntaxException for object value");
        } catch (JsonSyntaxException e) {
            // Expected
        } catch (IOException e) {
            fail("Should throw JsonSyntaxException, not IOException");
        }
    }

    @Test(timeout = 4000)
    public void testAdapterWriteWithJson