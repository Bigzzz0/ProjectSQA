package com.google.gson.internal.bind;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.internal.Excluder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.Collections;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * 1. Target Defect:
 *    - In ReflectiveTypeAdapterFactory#createBoundField, 'jsonAdapterPresent' was computed
 *      but ignored when constructing TypeAdapterRuntimeTypeWrapper. For primitive fields
 *      annotated with @JsonAdapter, the boxed runtime type (e.g. Long) did not equal the
 *      primitive type (e.g. long), causing TypeAdapterRuntimeTypeWrapper to override the custom
 *      @JsonAdapter with Gson's default primitive adapter.
 *    - Targeted by: testPrimitiveFieldAnnotationTakesPrecedenceOverDefault
 *
 * 2. Equivalence Partitions & Decision Branches:
 *    - create():
 *      - Non-Object / Primitive raw type (!Object.class.isAssignableFrom) -> returns null
 *      - Interface raw type -> returns adapter with empty boundFields
 *      - Class with inheritance hierarchy -> traverses until raw == Object.class
 *    - excludeField():
 *      - serialize/deserialize exclusion flags via Excluder
 *      - !serialize && !deserialize -> field completely skipped
 *    - getFieldNames():
 *      - Field without @SerializedName -> translated name from FieldNamingStrategy
 *      - Field with @SerializedName without alternate names -> singleton list
 *      - Field with @SerializedName with alternate names -> list with primary + alternates
 *    - Duplicate Field Collision:
 *      - Multiple fields resolving to the same serialized name -> throws IllegalArgumentException
 *    - BoundField#writeField():
 *      - serialized == false -> returns false
 *      - fieldValue == value (circular reference, e.g., Throwable.cause) -> returns false
 *      - normal field -> returns true
 *    - BoundField#read():
 *      - Read null on primitive field -> field.set not invoked (leaves default)
 *      - Read null or object on non-primitive field -> field.set invoked
 *    - Adapter#read():
 *      - JsonToken.NULL -> returns null
 *      - Unknown field or field with !deserialized -> reader.skipValue()
 *      - Syntax error / malformed token -> throws JsonSyntaxException
 *    - Adapter#write():
 *      - null value -> writes nullValue()
 *      - non-null value -> writes beginObject, field names and values, endObject
 */
public class ReflectiveTypeAdapterFactoryGeminiTest {

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth Defect)
  // =========================================================================

  private static final class LongToStringAdapter extends TypeAdapter<Long> {
    @Override
    public void write(JsonWriter out, Long value) throws IOException {
      out.beginArray();
      out.value(String.valueOf(value));
      out.endArray();
    }

    @Override
    public Long read(JsonReader in) throws IOException {
      in.beginArray();
      String value = in.nextString();
      in.endArray();
      return Long.parseLong(value);
    }
  }

  private static final class GadgetWithAnnotatedPart {
    @JsonAdapter(LongToStringAdapter.class)
    final long part;

    GadgetWithAnnotatedPart(long part) {
      this.part = part;
    }
  }

  @Test(timeout = 4000)
  public void testPrimitiveFieldAnnotationTakesPrecedenceOverDefault() {
    Gson gson = new Gson();
    GadgetWithAnnotatedPart gadget = new GadgetWithAnnotatedPart(42L);
    String json = gson.toJson(gadget);
    // Fault condition: Without fix, returns {"part":42} instead of {"part":["42"]}
    assertEquals("{\"part\":[\"42\"]}", json);

    GadgetWithAnnotatedPart deserialized =
        gson.fromJson("{\"part\":[\"42\"]}", GadgetWithAnnotatedPart.class);
    assertEquals(42L, deserialized.part);
  }

  // =========================================================================
  // Partition A: Core Functional Logic & Serialization / Deserialization
  // =========================================================================

  private static class SimplePojo {
    String text;
    int number;

    SimplePojo() {}

    SimplePojo(String text, int number) {
      this.text = text;
      this.number = number;
    }
  }

  @Test(timeout = 4000)
  public void testSimplePojoSerializationAndDeserialization() {
    Gson gson = new Gson();
    SimplePojo pojo = new SimplePojo("hello", 123);
    String json = gson.toJson(pojo);
    assertTrue(json.contains("\"text\":\"hello\""));
    assertTrue(json.contains("\"number\":123"));

    SimplePojo read = gson.fromJson(json, SimplePojo.class);
    assertEquals("hello", read.text);
    assertEquals(123, read.number);
  }

  private static class ParentClass {
    String parentField = "parent";
  }

  private static class ChildClass extends ParentClass {
    String childField = "child";
  }

  @Test(timeout = 4000)
  public void testInheritanceFieldResolution() {
    Gson gson = new Gson();
    ChildClass child = new ChildClass();
    String json = gson.toJson(child);
    assertTrue(json.contains("\"parentField\":\"parent\""));
    assertTrue(json.contains("\"childField\":\"child\""));

    ChildClass read = gson.fromJson(json, ChildClass.class);
    assertEquals("parent", read.parentField);
    assertEquals("child", read.childField);
  }

  private interface DummyInterface {
    void execute();
  }

  @Test(timeout = 4000)
  public void testInterfaceRawTypeReturnsAdapterWithNoFields() {
    ConstructorConstructor constructorConstructor =
        new ConstructorConstructor(Collections.emptyMap());
    ReflectiveTypeAdapterFactory factory =
        new ReflectiveTypeAdapterFactory(
            constructorConstructor, FieldNamingPolicy.IDENTITY, Excluder.DEFAULT);

    Gson gson = new Gson();
    TypeAdapter<DummyInterface> adapter =
        factory.create(gson, TypeToken.get(DummyInterface.class));
    assertNotNull(adapter);

    StringWriter writer = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(writer);
    try {
      adapter.write(jsonWriter, null);
      assertEquals("null", writer.toString());
    } catch (IOException e) {
      fail("Writing null failed: " + e.getMessage());
    }
  }

  @Test(timeout = 4000)
  public void testPrimitiveTypeTokenReturnsNull() {
    ConstructorConstructor constructorConstructor =
        new ConstructorConstructor(Collections.emptyMap());
    ReflectiveTypeAdapterFactory factory =
        new ReflectiveTypeAdapterFactory(
            constructorConstructor, FieldNamingPolicy.IDENTITY, Excluder.DEFAULT);

    Gson gson = new Gson();
    TypeAdapter<Integer> adapter = factory.create(gson, TypeToken.get(int.class));
    assertNull(adapter);
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis & SerializedName Alternates
  // =========================================================================

  private static class AlternateNamesPojo {
    @SerializedName(value = "primaryName", alternate = {"alt1", "alt2"})
    String value;
  }

  @Test(timeout = 4000)
  public void testSerializedNameWithAlternatesSerialization() {
    Gson gson = new Gson();
    AlternateNamesPojo pojo = new AlternateNamesPojo();
    pojo.value = "testVal";
    String json = gson.toJson(pojo);
    // Serialization must ONLY use the primary default name, not alternates
    assertEquals("{\"primaryName\":\"testVal\"}", json);
  }

  @Test(timeout = 4000)
  public void testSerializedNameWithAlternatesDeserialization() {
    Gson gson = new Gson();
    // Read using first alternate
    AlternateNamesPojo readAlt1 = gson.fromJson("{\"alt1\":\"v1\"}", AlternateNamesPojo.class);
    assertEquals("v1", readAlt1.value);

    // Read using second alternate
    AlternateNamesPojo readAlt2 = gson.fromJson("{\"alt2\":\"v2\"}", AlternateNamesPojo.class);
    assertEquals("v2", readAlt2.value);

    // Read using primary name
    AlternateNamesPojo readPrimary =
        gson.fromJson("{\"primaryName\":\"v3\"}", AlternateNamesPojo.class);
    assertEquals("v3", readPrimary.value);
  }

  private static class SelfReferencingClass {
    SelfReferencingClass self;
    String name;

    SelfReferencingClass(String name) {
      this.name = name;
    }
  }

  @Test(timeout = 4000)
  public void testSelfReferencingFieldAvoidsInfiniteRecursion() {
    Gson gson = new Gson();
    SelfReferencingClass obj = new SelfReferencingClass("root");
    obj.self = obj; // Direct self reference

    String json = gson.toJson(obj);
    // obj.self should be omitted by BoundField.writeField check (fieldValue != value)
    assertEquals("{\"name\":\"root\"}", json);
  }

  private static class PrimitiveHolder {
    int count = 10;
  }

  @Test(timeout = 4000)
  public void testNullDeserializationIntoPrimitivePreservesDefault() {
    Gson gson = new Gson();
    // JSON contains explicit null for a primitive integer
    PrimitiveHolder holder = gson.fromJson("{\"count\":null}", PrimitiveHolder.class);
    assertNotNull(holder);
    assertEquals(10, holder.count);
  }

  @Test(timeout = 4000)
  public void testAdapterNullReadAndWrite() throws IOException {
    Gson gson = new Gson();
    TypeAdapter<SimplePojo> adapter = gson.getAdapter(SimplePojo.class);

    // Adapter.write(null)
    StringWriter out = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(out);
    adapter.write(jsonWriter, null);
    assertEquals("null", out.toString());

    // Adapter.read(null)
    JsonReader jsonReader = new JsonReader(new StringReader("null"));
    SimplePojo result = adapter.read(jsonReader);
    assertNull(result);
  }

  @Test(timeout = 4000)
  public void testSkipUnknownFieldsDuringDeserialization() {
    Gson gson = new Gson();
    String json = "{\"unknownField\":12345,\"unknownObj\":{\"a\":1},\"number\":99}";
    SimplePojo pojo = gson.fromJson(json, SimplePojo.class);
    assertEquals(99, pojo.number);
    assertNull(pojo.text);
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  private static class DuplicateSubclass1 {
    @SerializedName("sameKey")
    int a = 1;
  }

  private static class DuplicateSubclass2 extends DuplicateSubclass1 {
    @SerializedName("sameKey")
    int b = 2;
  }

  @Test(timeout = 4000)
  public void testDuplicateFieldNamesThrowsIllegalArgumentException() {
    Gson gson = new Gson();
    try {
      gson.getAdapter(DuplicateSubclass2.class);
      fail("Expected IllegalArgumentException due to multiple JSON fields named 'sameKey'");
    } catch (IllegalArgumentException expected) {
      assertTrue(expected.getMessage().contains("declares multiple JSON fields named sameKey"));
    }
  }

  private static class DuplicateWithAlternate {
    @SerializedName("foo")
    int a;

    @SerializedName(value = "bar", alternate = {"foo"})
    int b;
  }

  @Test(timeout = 4000)
  public void testDuplicateFieldWithAlternateThrowsIllegalArgumentException() {
    Gson gson = new Gson();
    try {
      gson.getAdapter(DuplicateWithAlternate.class);
      fail("Expected IllegalArgumentException due to alternate colliding with primary name");
    } catch (IllegalArgumentException expected) {
      assertTrue(expected.getMessage().contains("declares multiple JSON fields named foo"));
    }
  }

  @Test(timeout = 4000)
  public void testMalformedJsonSyntaxThrowsJsonSyntaxException() {
    Gson gson = new Gson();
    TypeAdapter<SimplePojo> adapter = gson.getAdapter(SimplePojo.class);
    try {
      adapter.read(new JsonReader(new StringReader("[\"not an object\"]")));
      fail("Expected JsonSyntaxException for malformed object");
    } catch (JsonSyntaxException expected) {
      // Expected
    } catch (IOException e) {
      fail("Unexpected IOException: " + e.getMessage());
    }
  }

  // =========================================================================
  // Partition E: Excluder & Strategy Field Exclusion Rules
  // =========================================================================

  private static class ExcludedFieldsClass {
    @Expose(serialize = false, deserialize = true)
    String onlyDeserialize = "deserialized";

    @Expose(serialize = true, deserialize = false)
    String onlySerialize = "serialized";

    @Expose(serialize = false, deserialize = false)
    String completelyIgnored = "ignored";

    transient String transientField = "transient";
  }

  @Test(timeout = 4000)
  public void testExcluderIntegration() {
    Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    ExcludedFieldsClass obj = new ExcludedFieldsClass();
    obj.onlyDeserialize = "initDes";
    obj.onlySerialize = "initSer";

    String json = gson.toJson(obj);
    assertTrue(json.contains("\"onlySerialize\":\"initSer\""));
    assertFalse(json.contains("onlyDeserialize"));
    assertFalse(json.contains("completelyIgnored"));
    assertFalse(json.contains("transientField"));

    String inputJson =
        "{\"onlyDeserialize\":\"updatedDes\",\"onlySerialize\":\"updatedSer\","
            + "\"completelyIgnored\":\"updatedIgnored\"}";
    ExcludedFieldsClass read = gson.fromJson(inputJson, ExcludedFieldsClass.class);
    assertEquals("updatedDes", read.onlyDeserialize);
    assertEquals("serialized", read.onlySerialize); // was ignored on deserialize
    assertEquals("ignored", read.completelyIgnored); // was completely ignored
  }

  @Test(timeout = 4000)
  public void testStaticExcludeFieldMethod() throws NoSuchFieldException {
    Excluder excluder = Excluder.DEFAULT;
    Field normalField = SimplePojo.class.getDeclaredField("text");
    Field transientField = ExcludedFieldsClass.class.getDeclaredField("transientField");

    assertTrue(ReflectiveTypeAdapterFactory.excludeField(normalField, true, excluder));
    assertTrue(ReflectiveTypeAdapterFactory.excludeField(normalField, false, excluder));

    assertFalse(ReflectiveTypeAdapterFactory.excludeField(transientField, true, excluder));
    assertFalse(ReflectiveTypeAdapterFactory.excludeField(transientField, false, excluder));
  }

  @Test(timeout = 4000)
  public void testInstanceExcludeFieldMethod() throws NoSuchFieldException {
    ConstructorConstructor constructorConstructor =
        new ConstructorConstructor(Collections.emptyMap());
    Excluder excluder = Excluder.DEFAULT;
    ReflectiveTypeAdapterFactory factory =
        new ReflectiveTypeAdapterFactory(
            constructorConstructor, FieldNamingPolicy.IDENTITY, excluder);

    Field normalField = SimplePojo.class.getDeclaredField("number");
    assertTrue(factory.excludeField(normalField, true));
    assertTrue(factory.excludeField(normalField, false));
  }

  private static class CustomNamingClass {
    String originalField = "val";
  }

  @Test(timeout = 4000)
  public void testCustomFieldNamingStrategy() {
    FieldNamingStrategy uppercaseStrategy = field -> field.getName().toUpperCase();
    Gson gson = new GsonBuilder().setFieldNamingStrategy(uppercaseStrategy).create();

    CustomNamingClass obj = new CustomNamingClass();
    String json = gson.toJson(obj);
    assertEquals("{\"ORIGINALFIELD\":\"val\"}", json);

    CustomNamingClass read = gson.fromJson("{\"ORIGINALFIELD\":\"newVal\"}", CustomNamingClass.class);
    assertEquals("newVal", read.originalField);
  }
}