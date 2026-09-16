/* [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Target Class: com.google.gson.internal.bind.JsonAdapterAnnotationTypeAdapterFactory
 * Target Environment: Java 8 / JUnit 4 / Defects4J Benchmark
 *
 * Decision / Condition Branch Analysis:
 * 1. create(Gson, TypeToken<T>)
 *    - Branch 1.1: annotation == null -> returns null (target class not annotated with @JsonAdapter).
 *    - Branch 1.2: annotation != null -> delegates to getTypeAdapter.
 * 2. getTypeAdapter(ConstructorConstructor, Gson, TypeToken<?>, JsonAdapter)
 *    - Branch 2.1: TypeAdapter.class.isAssignableFrom(value) is TRUE
 *                  -> constructs TypeAdapter via ConstructorConstructor, wraps with nullSafe().
 *    - Branch 2.2: TypeAdapterFactory.class.isAssignableFrom(value) is TRUE
 *                  -> constructs TypeAdapterFactory via ConstructorConstructor, invokes create(gson, fieldType).
 *                  - Branch 2.2.1: Factory returns non-null TypeAdapter -> wraps with nullSafe().
 *                  - Branch 2.2.2 [DEFECT ZONE]: Factory returns null
 *                                  -> Defective Code: calls null.nullSafe() -> NullPointerException.
 *                                  -> Correct Code: null check prevents NPE, returns null TypeAdapter.
 *    - Branch 2.3: Neither TypeAdapter nor TypeAdapterFactory
 *                  -> throws IllegalArgumentException("@JsonAdapter value must be TypeAdapter or TypeAdapterFactory reference.").
 * 3. Null-Safe Wrapper Logic:
 *    - Validates nullSafe() wrapping ensures null serialization outputs 'null' and reading 'null' skips adapter.
 * ====================================================================================================
 */

package com.google.gson.internal.bind;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.google.gson.Gson;
import com.google.gson.InstanceCreator;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class JsonAdapterAnnotationTypeAdapterFactoryGeminiTest {

  private ConstructorConstructor constructorConstructor;
  private JsonAdapterAnnotationTypeAdapterFactory factory;
  private Gson gson;

  @Before
  public void setUp() {
    this.constructorConstructor = new ConstructorConstructor(Collections.<Type, InstanceCreator<?>>emptyMap());
    this.factory = new JsonAdapterAnnotationTypeAdapterFactory(constructorConstructor);
    this.gson = new Gson();
  }

  // -------------------------------------------------------------------------
  // Helper Test Classes & Adapters
  // -------------------------------------------------------------------------

  private static class UnannotatedModel {
    String name = "unannotated";
  }

  @JsonAdapter(DirectModelAdapter.class)
  private static class DirectAnnotatedModel {
    final String value;
    DirectAnnotatedModel(String value) {
      this.value = value;
    }
  }

  private static class DirectModelAdapter extends TypeAdapter<DirectAnnotatedModel> {
    @Override
    public void write(JsonWriter out, DirectAnnotatedModel value) throws IOException {
      out.value("prefix:" + value.value);
    }

    @Override
    public DirectAnnotatedModel read(JsonReader in) throws IOException {
      return new DirectAnnotatedModel(in.nextString().replace("prefix:", ""));
    }
  }

  @JsonAdapter(NullSensitiveAdapter.class)
  private static class NullSensitiveModel {
    final String content;
    NullSensitiveModel(String content) {
      this.content = content;
    }
  }

  private static class NullSensitiveAdapter extends TypeAdapter<NullSensitiveModel> {
    @Override
    public void write(JsonWriter out, NullSensitiveModel value) throws IOException {
      // Intentionally dereferences 'value' without null check to verify nullSafe() wrapper
      out.value(value.content.toUpperCase());
    }

    @Override
    public NullSensitiveModel read(JsonReader in) throws IOException {
      // Intentionally does not handle JsonToken.NULL to verify nullSafe() wrapper
      return new NullSensitiveModel(in.nextString().toLowerCase());
    }
  }

  @JsonAdapter(ModelFactory.class)
  private static class FactoryAnnotatedModel {
    final int number;
    FactoryAnnotatedModel(int number) {
      this.number = number;
    }
  }

  private static class ModelFactory implements TypeAdapterFactory {
    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
      if (type.getRawType() == FactoryAnnotatedModel.class) {
        return (TypeAdapter<T>) new TypeAdapter<FactoryAnnotatedModel>() {
          @Override
          public void write(JsonWriter out, FactoryAnnotatedModel value) throws IOException {
            out.value(value.number * 10);
          }

          @Override
          public FactoryAnnotatedModel read(JsonReader in) throws IOException {
            return new FactoryAnnotatedModel(in.nextInt() / 10);
          }
        };
      }
      return null;
    }
  }

  @JsonAdapter(NullReturningFactory.class)
  private static class NullReturningFactoryModel {
    String text = "defaultText";

    NullReturningFactoryModel() {}

    NullReturningFactoryModel(String text) {
      this.text = text;
    }
  }

  private static class NullReturningFactory implements TypeAdapterFactory {
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
      return null;
    }
  }

  @JsonAdapter(InvalidClassAdapter.class)
  private static class InvalidClassAnnotatedModel {}

  private static class InvalidClassAdapter {}

  @JsonAdapter(ConstructedWithDependencyAdapter.class)
  private static class ModelWithCustomConstructor {
    final String message;
    ModelWithCustomConstructor(String message) {
      this.message = message;
    }
  }

  private static class ConstructedWithDependencyAdapter extends TypeAdapter<ModelWithCustomConstructor> {
    private final String suffix;

    ConstructedWithDependencyAdapter(String suffix) {
      this.suffix = suffix;
    }

    @Override
    public void write(JsonWriter out, ModelWithCustomConstructor value) throws IOException {
      out.value(value.message + ":" + suffix);
    }

    @Override
    public ModelWithCustomConstructor read(JsonReader in) throws IOException {
      String str = in.nextString();
      return new ModelWithCustomConstructor(str.substring(0, str.indexOf(':')));
    }
  }

  // -------------------------------------------------------------------------
  // Partition A: Core Functional Logic & State Transitions
  // -------------------------------------------------------------------------

  @Test(timeout = 4000)
  public void testDirectTypeAdapterAnnotationSerializationAndDeserialization() {
    DirectAnnotatedModel model = new DirectAnnotatedModel("testPayload");
    String json = gson.toJson(model);
    assertEquals("\"prefix:testPayload\"", json);

    DirectAnnotatedModel deserialized = gson.fromJson("\"prefix:deserializedPayload\"", DirectAnnotatedModel.class);
    assertNotNull(deserialized);
    assertEquals("deserializedPayload", deserialized.value);
  }

  @Test(timeout = 4000)
  public void testTypeAdapterFactoryAnnotationSerializationAndDeserialization() {
    FactoryAnnotatedModel model = new FactoryAnnotatedModel(5);
    String json = gson.toJson(model);
    assertEquals("50", json);

    FactoryAnnotatedModel deserialized = gson.fromJson("80", FactoryAnnotatedModel.class);
    assertNotNull(deserialized);
    assertEquals(8, deserialized.number);
  }

  @Test(timeout = 4000)
  public void testNullSafeWrapperHandlesNullSerialization() {
    NullSensitiveModel nullModel = null;
    String json = gson.toJson(nullModel, NullSensitiveModel.class);
    assertEquals("null", json);
  }

  @Test(timeout = 4000)
  public void testNullSafeWrapperHandlesNullDeserialization() {
    NullSensitiveModel deserialized = gson.fromJson("null", NullSensitiveModel.class);
    assertNull(deserialized);
  }

  @Test(timeout = 4000)
  public void testNullSafeWrapperProcessesNonNullValues() {
    NullSensitiveModel model = new NullSensitiveModel("hello");
    String json = gson.toJson(model);
    assertEquals("\"HELLO\"", json);

    NullSensitiveModel deserialized = gson.fromJson("\"WORLD\"", NullSensitiveModel.class);
    assertNotNull(deserialized);
    assertEquals("world", deserialized.content);
  }

  // -------------------------------------------------------------------------
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // -------------------------------------------------------------------------

  @Test(timeout = 4000)
  public void testCreateReturnsNullForUnannotatedClass() {
    TypeToken<UnannotatedModel> token = TypeToken.get(UnannotatedModel.class);
    TypeAdapter<UnannotatedModel> adapter = factory.create(gson, token);
    assertNull("Unannotated class must produce null adapter from factory", adapter);
  }

  @Test(timeout = 4000)
  public void testCreateReturnsNullForStandardJavaTypes() {
    assertNull(factory.create(gson, TypeToken.get(String.class)));
    assertNull(factory.create(gson, TypeToken.get(Integer.class)));
    assertNull(factory.create(gson, TypeToken.get(Object.class)));
  }

  @Test(timeout = 4000)
  public void testCustomConstructorInstanceCreatorIntegration() {
    Map<Type, InstanceCreator<?>> creators = new HashMap<Type, InstanceCreator<?>>();
    creators.put(ConstructedWithDependencyAdapter.class, new InstanceCreator<ConstructedWithDependencyAdapter>() {
      @Override
      public ConstructedWithDependencyAdapter createInstance(Type type) {
        return new ConstructedWithDependencyAdapter("customInjectedSuffix");
      }
    });

    ConstructorConstructor customConstructors = new ConstructorConstructor(creators);
    JsonAdapterAnnotationTypeAdapterFactory customFactory = new JsonAdapterAnnotationTypeAdapterFactory(customConstructors);

    TypeAdapter<ModelWithCustomConstructor> adapter =
        customFactory.create(gson, TypeToken.get(ModelWithCustomConstructor.class));
    assertNotNull(adapter);

    ModelWithCustomConstructor model = new ModelWithCustomConstructor("data");
    String json = adapter.toJson(model);
    assertEquals("\"data:customInjectedSuffix\"", json);
  }

  // -------------------------------------------------------------------------
  // Partition C: Defect-Targeted Branch Zone (Defects4J Regression)
  // -------------------------------------------------------------------------

  @Test(timeout = 4000)
  public void testNullSafeBugSerialize() {
    // Ground truth regression: when a TypeAdapterFactory specified in @JsonAdapter returns null,
    // serialization should fall back to next adapter (ReflectiveTypeAdapterFactory) rather than throwing NPE.
    NullReturningFactoryModel instance = new NullReturningFactoryModel("expectedValue");
    String json = gson.toJson(instance);
    assertEquals("{\"text\":\"expectedValue\"}", json);
  }

  @Test(timeout = 4000)
  public void testNullSafeBugDeserialize() {
    // Ground truth regression: when a TypeAdapterFactory specified in @JsonAdapter returns null,
    // deserialization should fall back to next adapter rather than throwing NPE.
    NullReturningFactoryModel result = gson.fromJson("{\"text\":\"deserializedValue\"}", NullReturningFactoryModel.class);
    assertNotNull(result);
    assertEquals("deserializedValue", result.text);
  }

  @Test(timeout = 4000)
  public void testCreateReturnsNullWhenFactoryReturnsNullDirectly() {
    // Direct white-box invocation of create:
    // If the referenced TypeAdapterFactory returns null, create() must return null without NPE.
    TypeAdapter<NullReturningFactoryModel> adapter =
        factory.create(gson, TypeToken.get(NullReturningFactoryModel.class));
    assertNull("When annotated TypeAdapterFactory returns null, create() must return null", adapter);
  }

  @Test(timeout = 4000)
  public void testGetTypeAdapterReturnsNullWhenFactoryReturnsNullDirectly() {
    // Direct white-box invocation of package-private getTypeAdapter():
    JsonAdapter annotation = NullReturningFactoryModel.class.getAnnotation(JsonAdapter.class);
    TypeAdapter<?> adapter = JsonAdapterAnnotationTypeAdapterFactory.getTypeAdapter(
        constructorConstructor, gson, TypeToken.get(NullReturningFactoryModel.class), annotation);
    assertNull("getTypeAdapter must return null without throwing NPE when factory creates null adapter", adapter);
  }

  // -------------------------------------------------------------------------
  // Partition D: Exception & Defensive Guard Paths
  // -------------------------------------------------------------------------

  @Test(timeout = 4000)
  public void testInvalidAdapterClassThrowsIllegalArgumentException() {
    try {
      factory.create(gson, TypeToken.get(InvalidClassAnnotatedModel.class));
      fail("Expected IllegalArgumentException when @JsonAdapter specifies non-TypeAdapter/non-Factory class");
    } catch (IllegalArgumentException expected) {
      assertEquals("@JsonAdapter value must be TypeAdapter or TypeAdapterFactory reference.", expected.getMessage());
    }
  }

  @Test(timeout = 4000)
  public void testGetTypeAdapterInvalidClassThrowsIllegalArgumentException() {
    JsonAdapter annotation = InvalidClassAnnotatedModel.class.getAnnotation(JsonAdapter.class);
    try {
      JsonAdapterAnnotationTypeAdapterFactory.getTypeAdapter(
          constructorConstructor, gson, TypeToken.get(InvalidClassAnnotatedModel.class), annotation);
      fail("Expected IllegalArgumentException for invalid adapter class reference");
    } catch (IllegalArgumentException expected) {
      assertEquals("@JsonAdapter value must be TypeAdapter or TypeAdapterFactory reference.", expected.getMessage());
    }
  }

  @Test(timeout = 4000)
  public void testCreateWithNullTargetTypeThrowsNullPointerException() {
    try {
      factory.create(gson, null);
      fail("Expected NullPointerException when targetType is null");
    } catch (NullPointerException expected) {
      // Expected exception on accessing null targetType
    }
  }

  // -------------------------------------------------------------------------
  // Partition E: Object Lifecycle & Contract Integrity
  // -------------------------------------------------------------------------

  @Test(timeout = 4000)
  public void testFactoryCreationAndIdempotence() {
    TypeToken<DirectAnnotatedModel> token = TypeToken.get(DirectAnnotatedModel.class);
    TypeAdapter<DirectAnnotatedModel> adapter1 = factory.create(gson, token);
    TypeAdapter<DirectAnnotatedModel> adapter2 = factory.create(gson, token);

    assertNotNull(adapter1);
    assertNotNull(adapter2);
    // Each invocation constructs a distinct adapter instance
    assertNotSame(adapter1, adapter2);
  }

  @Test(timeout = 4000)
  public void testConstructorConstructorPreserved() {
    ConstructorConstructor cc = new ConstructorConstructor(Collections.<Type, InstanceCreator<?>>emptyMap());
    JsonAdapterAnnotationTypeAdapterFactory localFactory = new JsonAdapterAnnotationTypeAdapterFactory(cc);
    assertNotNull(localFactory);
  }
}