package com.google.gson.internal.bind;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: JsonAdapterAnnotationTypeAdapterFactory
 * 
 * Decision branches in create():
 *   B1: annotation == null -> return null (true branch)
 *   B2: annotation != null -> proceed to getTypeAdapter (false branch)
 * 
 * Decision branches in getTypeAdapter():
 *   B3: TypeAdapter.class.isAssignableFrom(value) -> true branch
 *   B4: TypeAdapterFactory.class.isAssignableFrom(value) -> true branch (only if B3 false)
 *   B5: else -> throw IllegalArgumentException (only if B3 false and B4 false)
 *   B6: typeAdapter.nullSafe() call (always executed after B3/B4/B5)
 * 
 * Boundary conditions:
 *   - null annotation (B1 true)
 *   - TypeAdapter subclass (B3 true)
 *   - TypeAdapterFactory implementation (B4 true)
 *   - Invalid class (B5)
 *   - nullSafe() behavior on custom adapters (defect target)
 * 
 * Known defect: The nullSafe() call at the end of getTypeAdapter() can cause
 * NullPointerException when the constructed adapter returns null for null input
 * during serialization/deserialization. The bug manifests in:
 *   - testNullSafeBugDeserialize: NPE when deserializing null JSON
 *   - testNullSafeBugSerialize: NPE when serializing null object
 * 
 * The defect is that nullSafe() is called on the adapter, but if the adapter's
 * nullSafe() implementation or the adapter itself has issues with null handling,
 * it throws NPE instead of gracefully handling null.
 */
public class JsonAdapterAnnotationTypeAdapterFactoryDeepseekTest {

    // Test adapter that does NOT handle null properly (defect trigger)
    public static class NonNullSafeAdapter extends TypeAdapter<String> {
        @Override
        public void write(JsonWriter out, String value) throws IOException {
            if (value == null) {
                throw new NullPointerException("Cannot serialize null");
            }
            out.value(value);
        }

        @Override
        public String read(JsonReader in) throws IOException {
            String value = in.nextString();
            if (value == null) {
                throw new NullPointerException("Cannot deserialize null");
            }
            return value;
        }
    }

    // Properly null-safe adapter for comparison
    public static class SafeAdapter extends TypeAdapter<String> {
        @Override
        public void write(JsonWriter out, String value) throws IOException {
            if (value == null) {
                out.nullValue();
                return;
            }
            out.value(value);
        }

        @Override
        public String read(JsonReader in) throws IOException {
            if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            return in.nextString();
        }
    }

    @JsonAdapter(NonNullSafeAdapter.class)
    public static class NonNullSafeContainer {
        public String value;
    }

    @JsonAdapter(SafeAdapter.class)
    public static class SafeContainer {
        public String value;
    }

    @JsonAdapter(InvalidAdapter.class)
    public static class InvalidContainer {
    }

    public static class InvalidAdapter {
        // Not a TypeAdapter or TypeAdapterFactory
    }

    @JsonAdapter(TestTypeAdapterFactory.class)
    public static class FactoryContainer {
    }

    public static class TestTypeAdapterFactory implements TypeAdapterFactory {
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            return (TypeAdapter<T>) new SafeAdapter();
        }
    }

    private final Gson gson = new GsonBuilder().create();
    private final ConstructorConstructor constructorConstructor = new ConstructorConstructor();

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testCreateWithNullAnnotation() {
        // Class without @JsonAdapter annotation
        TypeToken<String> typeToken = TypeToken.get(String.class);
        JsonAdapterAnnotationTypeAdapterFactory factory =
                new JsonAdapterAnnotationTypeAdapterFactory(constructorConstructor);
        assertNull("Should return null when no annotation present",
                factory.create(gson, typeToken));
    }

    @Test(timeout = 4000)
    public void testCreateWithTypeAdapterAnnotation() {
        JsonAdapterAnnotationTypeAdapterFactory factory =
                new JsonAdapterAnnotationTypeAdapterFactory(constructorConstructor);
        TypeAdapter<?> adapter = factory.create(gson, TypeToken.get(NonNullSafeContainer.class));
        assertNotNull("Adapter should be created for annotated class", adapter);
        assertTrue("Adapter should be instance of NonNullSafeAdapter",
                adapter instanceof NonNullSafeAdapter);
    }

    @Test(timeout = 4000)
    public void testCreateWithTypeAdapterFactoryAnnotation() {
        JsonAdapterAnnotationTypeAdapterFactory factory =
                new JsonAdapterAnnotationTypeAdapterFactory(constructorConstructor);
        TypeAdapter<?> adapter = factory.create(gson, TypeToken.get(FactoryContainer.class));
        assertNotNull("Adapter should be created for factory-annotated class", adapter);
        assertTrue("Adapter should be instance of SafeAdapter",
                adapter instanceof SafeAdapter);
    }

    @Test(timeout = 4000)
    public void testGetTypeAdapterWithTypeAdapterClass() {
        JsonAdapter annotation = NonNullSafeContainer.class.getAnnotation(JsonAdapter.class);
        TypeAdapter<?> adapter = JsonAdapterAnnotationTypeAdapterFactory.getTypeAdapter(
                constructorConstructor, gson, TypeToken.get(NonNullSafeContainer.class), annotation);
        assertNotNull("Adapter should not be null", adapter);
        assertTrue("Adapter should be NonNullSafeAdapter", adapter instanceof NonNullSafeAdapter);
    }

    @Test(timeout = 4000)
    public void testGetTypeAdapterWithFactoryClass() {
        JsonAdapter annotation = FactoryContainer.class.getAnnotation(JsonAdapter.class);
        TypeAdapter<?> adapter = JsonAdapterAnnotationTypeAdapterFactory.getTypeAdapter(
                constructorConstructor, gson, TypeToken.get(FactoryContainer.class), annotation);
        assertNotNull("Adapter should not be null", adapter);
        assertTrue("Adapter should be SafeAdapter", adapter instanceof SafeAdapter);
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000)
    public void testGetTypeAdapterWithInvalidClass() {
        JsonAdapter annotation = InvalidContainer.class.getAnnotation(JsonAdapter.class);
        try {
            JsonAdapterAnnotationTypeAdapterFactory.getTypeAdapter(
                    constructorConstructor, gson, TypeToken.get(InvalidContainer.class), annotation);
            fail("Should throw IllegalArgumentException for invalid adapter class");
        } catch (IllegalArgumentException e) {
            assertEquals("Error message mismatch",
                    "@JsonAdapter value must be TypeAdapter or TypeAdapterFactory reference.",
                    e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testNullAnnotationHandling() {
        JsonAdapterAnnotationTypeAdapterFactory factory =
                new JsonAdapterAnnotationTypeAdapterFactory(constructorConstructor);
        // Test with null annotation via reflection
        TypeToken<Object> typeToken = new TypeToken<Object>() {};
        assertNull("Should handle null annotation gracefully",
                factory.create(gson, typeToken));
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    /**
     * Defect test: This test targets the known NPE bug where nullSafe() causes
     * NullPointerException during deserialization of null values.
     * Expected behavior: Should handle null gracefully and return null.
     * Actual defective behavior: Throws NullPointerException.
     */
    @Test(timeout = 4000)
    public void testNullSafeBugDeserialize() {
        Gson gson = new GsonBuilder().create();
        String json = "null";
        
        try {
            NonNullSafeContainer result = gson.fromJson(json, NonNullSafeContainer.class);
            // If we reach here, the bug is not triggered (defect not present)
            // But with the defect, this line won't be reached
            assertNull("Should return null for null JSON input", result);
        } catch (NullPointerException e) {
            fail("NullPointerException thrown during deserialization of null: " + e.getMessage());
        }
    }

    /**
     * Defect test: This test targets the known NPE bug where nullSafe() causes
     * NullPointerException during serialization of null values.
     * Expected behavior: Should handle null gracefully and write null.
     * Actual defective behavior: Throws NullPointerException.
     */
    @Test(timeout = 4000)
    public void testNullSafeBugSerialize() {
        Gson gson = new GsonBuilder().create();
        NonNullSafeContainer container = null;
        
        try {
            String json = gson.toJson(container, NonNullSafeContainer.class);
            // If we reach here, the bug is not triggered (defect not present)
            assertEquals("Should serialize null to JSON null", "null", json);
        } catch (NullPointerException e) {
            fail("NullPointerException thrown during serialization of null: " + e.getMessage());
        }
    }

    /**
     * Additional defect verification: Test that safe adapter works correctly
     * with null values (control test)
     */
    @Test(timeout = 4000)
    public void testSafeAdapterWithNull() {
        Gson gson = new GsonBuilder().create();
        
        // Test deserialization
        SafeContainer result = gson.fromJson("null", SafeContainer.class);
        assertNull("Safe adapter should handle null deserialization", result);
        
        // Test serialization
        SafeContainer container = null;
        String json = gson.toJson(container, SafeContainer.class);
        assertEquals("Safe adapter should serialize null correctly", "null", json);
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000)
    public void testCreateWithNullGson() {
        JsonAdapterAnnotationTypeAdapterFactory factory =
                new JsonAdapterAnnotationTypeAdapterFactory(constructorConstructor);
        try {
            factory.create(null, TypeToken.get(NonNullSafeContainer.class));
            fail("Should throw NullPointerException for null Gson");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateWithNullTypeToken() {
        JsonAdapterAnnotationTypeAdapterFactory factory =
                new JsonAdapterAnnotationTypeAdapterFactory(constructorConstructor);
        try {
            factory.create(gson, null);
            fail("Should throw NullPointerException for null TypeToken");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testConstructorAndState() throws Exception {
        ConstructorConstructor cc = new ConstructorConstructor();
        JsonAdapterAnnotationTypeAdapterFactory factory =
                new JsonAdapterAnnotationTypeAdapterFactory(cc);
        
        // Verify constructor stores reference
        Field field = JsonAdapterAnnotationTypeAdapterFactory.class
                .getDeclaredField("constructorConstructor");
        field.setAccessible(true);
        assertSame("Constructor should store the ConstructorConstructor instance",
                cc, field.get(factory));
    }

    @Test(timeout = 4000)
    public void testMultipleInstances() {
        JsonAdapterAnnotationTypeAdapterFactory factory1 =
                new JsonAdapterAnnotationTypeAdapterFactory(constructorConstructor);
        JsonAdapterAnnotationTypeAdapterFactory factory2 =
                new JsonAdapterAnnotationTypeAdapterFactory(constructorConstructor);
        
        assertNotSame("Different factory instances should not be same",
                factory1, factory2);
        
        TypeAdapter<?> adapter1 = factory1.create(gson, TypeToken.get(NonNullSafeContainer.class));
        TypeAdapter<?> adapter2 = factory2.create(gson, TypeToken.get(NonNullSafeContainer.class));
        
        assertNotNull("First adapter should not be null", adapter1);
        assertNotNull("Second adapter should not be null", adapter2);
        assertNotSame("Adapters from different factories should be different instances",
                adapter1, adapter2);
    }

    @Test(timeout = 4000)
    public void testTypeAdapterFactoryWithGenericType() {
        // Test with parameterized type
        TypeToken<List<String>> listType = new TypeToken<List<String>>() {};
        JsonAdapterAnnotationTypeAdapterFactory factory =
                new JsonAdapterAnnotationTypeAdapterFactory(constructorConstructor);
        
        // List doesn't have @JsonAdapter annotation
        assertNull("Should return null for non-annotated generic type",
                factory.create(gson, listType));
    }

    @Test(timeout = 4000)
    public void testTypeAdapterFactoryWithNestedAnnotation() {
        // Test with a class that has @JsonAdapter on a field
        class AnnotatedField {
            @JsonAdapter(SafeAdapter.class)
            String value;
        }
        
        JsonAdapterAnnotationTypeAdapterFactory factory =
                new JsonAdapterAnnotationTypeAdapterFactory(constructorConstructor);
        
        // The annotation is on the field, not the class, so create should return null
        assertNull("Should return null when annotation is on field, not class",
                factory.create(gson, TypeToken.get(AnnotatedField.class)));
    }

    @Test(timeout = 4000)
    public void testGetTypeAdapterWithNullSafeAdapter() {
        // Test that nullSafe() is applied to the adapter
        JsonAdapter annotation = SafeContainer.class.getAnnotation(JsonAdapter.class);
        TypeAdapter<?> adapter = JsonAdapterAnnotationTypeAdapterFactory.getTypeAdapter(
                constructorConstructor, gson, TypeToken.get(SafeContainer.class), annotation);
        
        assertNotNull("Adapter should not be null", adapter);
        assertTrue("Adapter should be SafeAdapter", adapter instanceof SafeAdapter);
        
        // Test that the adapter is null-safe
        try {
            String json = gson.toJson(null, SafeContainer.class);
            assertEquals("Should serialize null to JSON null", "null", json);
        } catch (Exception e) {
            fail("SafeAdapter should handle null serialization, but got: " + e.getMessage());
        }
    }
}