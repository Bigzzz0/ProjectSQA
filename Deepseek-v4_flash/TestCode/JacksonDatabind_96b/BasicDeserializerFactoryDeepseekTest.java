package com.fasterxml.jackson.databind.deser;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.deser.BasicDeserializerFactory;
import com.fasterxml.jackson.databind.deser.creators.CreatorWithNamingStrategyTest;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.*;
import com.fasterxml.jackson.databind.util.*;

public class BasicDeserializerFactoryDeepseekTest {
    
    /* [Branch & Defect Analysis Matrix]
     * 
     * Target Defect: Creator property name resolution with naming strategies
     * 
     * Key branches to test:
     * 1. Property name resolution when @JsonCreator is used with @JsonProperty annotations
     * 2. Naming strategy application (SNAKE_CASE) to creator properties
     * 3. Matching between external property names and internal creator parameter names
     * 4. Error handling when property name cannot be resolved
     * 
     * Boundary conditions:
     * - Single argument constructor with naming strategy
     * - Multiple arguments with naming strategy
     * - Properties with and without explicit @JsonProperty annotations
     * - Naming strategy that transforms property names
     * 
     * The defect occurs when a property named with snake_case (e.g., "param_name0") 
     * cannot be matched to the actual creator parameter name (e.g., "paramName0") 
     * when using naming strategies.
     */

    // Test POJO classes
    static class OneProperty {
        private final String paramName0;
        
        @JsonCreator
        public OneProperty(@JsonProperty("paramName0") String paramName0) {
            this.paramName0 = paramName0;
        }
        
        public String getParamName0() { return paramName0; }
    }
    
    static class TwoProperties {
        private final String paramName0;
        private final int paramName1;
        
        @JsonCreator
        public TwoProperties(@JsonProperty("paramName0") String paramName0, 
                            @JsonProperty("paramName1") int paramName1) {
            this.paramName0 = paramName0;
            this.paramName1 = paramName1;
        }
        
        public String getParamName0() { return paramName0; }
        public int getParamName1() { return paramName1; }
    }
    
    static class NoExplicitName {
        private final String value;
        
        @JsonCreator
        public NoExplicitName(String value) {
            this.value = value;
        }
        
        public String getValue() { return value; }
    }
    
    static class MixedProperties {
        private final String explicitName;
        private final String implicitName;
        
        @JsonCreator
        public MixedProperties(@JsonProperty("explicit_name") String explicitName, 
                              String implicitName) {
            this.explicitName = explicitName;
            this.implicitName = implicitName;
        }
        
        public String getExplicitName() { return explicitName; }
        public String getImplicitName() { return implicitName; }
    }
    
    static class InjectableProperty {
        private final String value;
        private final String injected;
        
        @JsonCreator
        public InjectableProperty(@JsonProperty("value") String value, 
                                 @JacksonInject("injected") String injected) {
            this.value = value;
            this.injected = injected;
        }
        
        public String getValue() { return value; }
        public String getInjected() { return injected; }
    }
    
    static class EnumProperty {
        private final TestEnum enumValue;
        
        @JsonCreator
        public EnumProperty(@JsonProperty("enum_value") TestEnum enumValue) {
            this.enumValue = enumValue;
        }
        
        public TestEnum getEnumValue() { return enumValue; }
    }
    
    enum TestEnum {
        VALUE_ONE, VALUE_TWO
    }
    
    static class CollectionProperty {
        private final List<String> items;
        
        @JsonCreator
        public CollectionProperty(@JsonProperty("item_list") List<String> items) {
            this.items = items;
        }
        
        public List<String> getItems() { return items; }
    }
    
    static class MapProperty {
        private final Map<String, Integer> mapValue;
        
        @JsonCreator
        public MapProperty(@JsonProperty("map_value") Map<String, Integer> mapValue) {
            this.mapValue = mapValue;
        }
        
        public Map<String, Integer> getMapValue() { return mapValue; }
    }
    
    static class ArrayProperty {
        private final String[] arrayValue;
        
        @JsonCreator
        public ArrayProperty(@JsonProperty("array_value") String[] arrayValue) {
            this.arrayValue = arrayValue;
        }
        
        public String[] getArrayValue() { return arrayValue; }
    }
    
    static class NestedProperty {
        private final OneProperty nested;
        
        @JsonCreator
        public NestedProperty(@JsonProperty("nested_property") OneProperty nested) {
            this.nested = nested;
        }
        
        public OneProperty getNested() { return nested; }
    }
    
    static class GenericProperty<T> {
        private final T value;
        
        @JsonCreator
        public GenericProperty(@JsonProperty("generic_value") T value) {
            this.value = value;
        }
        
        public T getValue() { return value; }
    }
    
    static class OptionalProperty {
        private final Optional<String> optionalValue;
        
        @JsonCreator
        public OptionalProperty(@JsonProperty("optional_value") Optional<String> optionalValue) {
            this.optionalValue = optionalValue;
        }
        
        public Optional<String> getOptionalValue() { return optionalValue; }
    }
    
    static class AtomicReferenceProperty {
        private final AtomicReference<String> refValue;
        
        @JsonCreator
        public AtomicReferenceProperty(@JsonProperty("ref_value") AtomicReference<String> refValue) {
            this.refValue = refValue;
        }
        
        public AtomicReference<String> getRefValue() { return refValue; }
    }
    
    private ObjectMapper createSnakeCaseMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        return mapper;
    }
    
    private ObjectMapper createKebabCaseMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.KEBAB_CASE);
        return mapper;
    }
    
    private ObjectMapper createLowerCaseMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CASE);
        return mapper;
    }
    
    private ObjectMapper createLowerCaseDotMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CASE_DOT);
        return mapper;
    }
    
    private ObjectMapper createLowerCaseWithUnderscoresMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        return mapper;
    }
    
    // Test 1: Core defect test - snake_case with single argument
    @Test(timeout = 4000)
    public void testSnakeCaseWithOneArg() throws Exception {
        ObjectMapper mapper = createSnakeCaseMapper();
        
        // This should work with snake_case naming strategy
        String json = "{\"param_name0\":\"value\"}";
        
        try {
            OneProperty result = mapper.readValue(json, OneProperty.class);
            assertNotNull("Result should not be null", result);
            assertEquals("Value should be deserialized correctly", "value", result.getParamName0());
        } catch (InvalidDefinitionException e) {
            fail("Failed to deserialize with snake_case naming strategy: " + e.getMessage());
        }
    }
    
    // Test 2: Snake case with multiple properties
    @Test(timeout = 4000)
    public void testSnakeCaseWithMultipleArgs() throws Exception {
        ObjectMapper mapper = createSnakeCaseMapper();
        
        String json = "{\"param_name0\":\"value1\",\"param_name1\":42}";
        
        try {
            TwoProperties result = mapper.readValue(json, TwoProperties.class);
            assertNotNull("Result should not be null", result);
            assertEquals("First property should match", "value1", result.getParamName0());
            assertEquals("Second property should match", 42, result.getParamName1());
        } catch (InvalidDefinitionException e) {
            fail("Failed to deserialize with multiple snake_case properties: " + e.getMessage());
        }
    }
    
    // Test 3: No explicit name with naming strategy
    @Test(timeout = 4000)
    public void testNoExplicitNameWithSnakeCase() throws Exception {
        ObjectMapper mapper = createSnakeCaseMapper();
        
        String json = "{\"value\":\"test\"}";
        
        try {
            NoExplicitName result = mapper.readValue(json, NoExplicitName.class);
            assertNotNull("Result should not be null", result);
            assertEquals("Value should match", "test", result.getValue());
        } catch (InvalidDefinitionException e) {
            fail("Failed to deserialize with no explicit name: " + e.getMessage());
        }
    }
    
    // Test 4: Mixed explicit and implicit names
    @Test(timeout = 4000)
    public void testMixedPropertiesWithSnakeCase() throws Exception {
        ObjectMapper mapper = createSnakeCaseMapper();
        
        String json = "{\"explicit_name\":\"exp\",\"implicit_name\":\"imp\"}";
        
        try {
            MixedProperties result = mapper.readValue(json, MixedProperties.class);
            assertNotNull("Result should not be null", result);
            assertEquals("Explicit name should match", "exp", result.getExplicitName());
            assertEquals("Implicit name should match", "imp", result.getImplicitName());
        } catch (InvalidDefinitionException e) {
            fail("Failed to deserialize with mixed properties: " + e.getMessage());
        }
    }
    
    // Test 5: Injectable property with naming strategy
    @Test(timeout = 4000)
    public void testInjectablePropertyWithSnakeCase() throws Exception {
        ObjectMapper mapper = createSnakeCaseMapper();
        mapper.registerModule(new SimpleModule() {
            @Override
            public void setupModule(SetupContext context) {
                context.addValueInstantiator(InjectableProperty.class, 
                    new com.fasterxml.jackson.databind.deser.std.StdValueInstantiator(
                        context.getDeserializationConfig(), 
                        context.getTypeFactory().constructType(InjectableProperty.class)) {
                        @Override
                        public Object createUsingDelegate(DeserializationContext ctxt, 
                                                          Object delegate) {
                            return new InjectableProperty("value", "injected");
                        }
                    });
            }
        });
        
        String json = "{\"value\":\"test\"}";
        
        try {
            InjectableProperty result = mapper.readValue(json, InjectableProperty.class);
            assertNotNull("Result should not be null", result);
        } catch (InvalidDefinitionException e) {
            // This might fail due to injection setup, but should not fail due to naming
            fail("Failed to deserialize with injectable property: " + e.getMessage());
        }
    }
    
    // Test 6: Enum property with naming strategy
    @Test(timeout = 4000)
    public void testEnumPropertyWithSnakeCase() throws Exception {
        ObjectMapper mapper = createSnakeCaseMapper();
        
        String json = "{\"enum_value\":\"VALUE_ONE\"}";
        
        try {
            EnumProperty result = mapper.readValue(json, EnumProperty.class);
            assertNotNull("Result should not be null", result);
            assertEquals("Enum value should match", TestEnum.VALUE_ONE, result.getEnumValue());
        } catch (InvalidDefinitionException e) {
            fail("Failed to deserialize with enum property: " + e.getMessage());
        }
    }
    
    // Test 7: Collection property with naming strategy
    @Test(timeout = 4000)
    public void testCollectionPropertyWithSnakeCase() throws Exception {
        ObjectMapper mapper = createSnakeCaseMapper();
        
        String json = "{\"item_list\":[\"a\",\"b\",\"c\"]}";
        
        try {
            CollectionProperty result = mapper.readValue(json, CollectionProperty.class);
            assertNotNull("Result should not be null", result);
            assertNotNull("Items should not be null", result.getItems());
            assertEquals("Items should have 3 elements", 3, result.getItems().size());
            assertEquals("First item should match", "a", result.getItems().get(0));
        } catch (InvalidDefinitionException e) {
            fail("Failed to deserialize with collection property: " + e.getMessage());
        }
    }
    
    // Test 8: Map property with naming strategy
    @Test(timeout = 4000)
    public void testMapPropertyWithSnakeCase() throws Exception {
        ObjectMapper mapper = createSnakeCaseMapper();
        
        String json = "{\"map_value\":{\"key1\":1,\"key2\":2}}";
        
        try {
            MapProperty result = mapper.readValue(json, MapProperty.class);
            assertNotNull("Result should not be null", result);
            assertNotNull("Map should not be null", result.getMapValue());
            assertEquals("Map should have 2 entries", 2, result.getMapValue().size());
            assertEquals("Value for key1 should match", Integer.valueOf(1), result.getMapValue().get("key1"));
        } catch (InvalidDefinitionException e) {
            fail("Failed to deserialize with map property: " + e.getMessage());
        }
    }
    
    // Test 9: Array property with naming strategy
    @Test(timeout = 4000)
    public void testArrayPropertyWithSnakeCase() throws Exception {
        ObjectMapper mapper = createSnakeCaseMapper();
        
        String json = "{\"array_value\":[\"x\",\"y\"]}";
        
        try {
            ArrayProperty result = mapper.readValue(json, ArrayProperty.class);
            assertNotNull("Result should not be null", result);
            assertNotNull("Array should not be null", result.getArrayValue());
            assertEquals("Array should have 2 elements", 2, result.getArrayValue().length);
            assertEquals("First element should match", "x", result.getArrayValue()[0]);
        } catch (InvalidDefinitionException e) {
            fail("Failed to deserialize with array property: " + e.getMessage());
        }
    }
    
    // Test 10: Nested property with naming strategy
    @Test(timeout = 4000)
    public void testNestedPropertyWithSnakeCase() throws Exception {
        ObjectMapper mapper = createSnakeCaseMapper();
        
        String json = "{\"nested_property\":{\"param_name0\":\"nested\"}}";
        
        try {
            NestedProperty result = mapper.readValue(json, NestedProperty.class);
            assertNotNull("Result should not be null", result);
            assertNotNull("Nested should not be null", result.getNested());
            assertEquals("Nested value should match", "nested", result.getNested().getParamName0());
        } catch (InvalidDefinitionException e) {
            fail("Failed to deserialize with nested property: " + e.getMessage());
        }
    }
    
    // Test 11: Generic property with naming strategy
    @Test(timeout = 4000)
    public void testGenericPropertyWithSnakeCase() throws Exception {
        ObjectMapper mapper = createSnakeCaseMapper();
        
        String json = "{\"generic_value\":\"generic\"}";
        
        try {
            GenericProperty<String> result = mapper.readValue(json, 
                new TypeReference<GenericProperty<String>>() {});
            assertNotNull("Result should not be null", result);
            assertEquals("Generic value should match", "generic", result.getValue());
        } catch (InvalidDefinitionException e) {
            fail("Failed to deserialize with generic property: " + e.getMessage());
        }
    }
    
    // Test 12: Optional property with naming strategy
    @Test(timeout = 4000)
    public void testOptionalPropertyWithSnakeCase() throws Exception {
        ObjectMapper mapper = createSnakeCaseMapper();
        
        String json = "{\"optional_value\":\"optional\"}";
        
        try {
            OptionalProperty result = mapper.readValue(json, OptionalProperty.class);
            assertNotNull("Result should not be null", result);
            assertNotNull("Optional should not be null", result.getOptionalValue());
            assertTrue("Optional should be present", result.getOptionalValue().isPresent());
            assertEquals("Optional value should match", "optional", result.getOptionalValue().get());
        } catch (InvalidDefinitionException e) {
            fail("Failed to deserialize with optional property: " + e.getMessage());
        }
    }
    
    // Test 13: AtomicReference property with naming strategy
    @Test(timeout = 4000)
    public void testAtomicReferencePropertyWithSnakeCase() throws Exception {
        ObjectMapper mapper = createSnakeCaseMapper();
        
        String json = "{\"ref_value\":\"atomic\"}";
        
        try {
            AtomicReferenceProperty result = mapper.readValue(json, AtomicReferenceProperty.class);
            assertNotNull("Result should not be null", result);
            assertNotNull("AtomicReference should not be null", result.getRefValue());
            assertEquals("AtomicReference value should match", "atomic", result.getRefValue().get());
        } catch (InvalidDefinitionException e) {
            fail("Failed to deserialize with AtomicReference property: " + e.getMessage());
        }
    }
    
    // Test 14: Kebab-case naming strategy
    @Test(timeout = 4000)
    public void testKebabCaseWithOneArg() throws Exception {
        ObjectMapper mapper = createKebabCaseMapper();
        
        String json = "{\"param-name0\":\"value\"}";
        
        try {
            OneProperty result = mapper.readValue(json, OneProperty.class);
            assertNotNull("Result should not be null", result);
            assertEquals("Value should be deserialized correctly", "value", result.getParamName0());
        } catch (InvalidDefinitionException e) {
            fail("Failed to deserialize with kebab-case naming strategy: " + e.getMessage());
        }
    }
    
    // Test 15: Lower-case naming strategy
    @Test(timeout = 4000)
    public void testLowerCaseWithOneArg() throws Exception {
        ObjectMapper mapper = createLowerCaseMapper();
        
        String json = "{\"paramname0\":\"value\"}";
        
        try {
            OneProperty result = mapper.readValue(json, OneProperty.class);
            assertNotNull("Result should not be null", result);
            assertEquals("Value should be deserialized correctly", "value", result.getParamName0());
        } catch (InvalidDefinitionException e) {
            fail("Failed to deserialize with lower-case naming strategy: " + e.getMessage());
        }
    }
    
    // Test 16: Lower-case dot naming strategy
    @Test(timeout = 4000)
    public void testLowerCaseDotWithOneArg() throws Exception {
        ObjectMapper mapper = createLowerCaseDotMapper();
        
        String json = "{\"paramname0\":\"value\"}";
        
        try {
            OneProperty result = mapper.readValue(json, OneProperty.class);
            assertNotNull("Result should not be null", result);
            assertEquals("Value should be deserialized correctly", "value", result.getParamName0());
        } catch (InvalidDefinitionException e) {
            fail("Failed to deserialize with lower-case dot naming strategy: " + e.getMessage());
        }
    }
    
    // Test 17: Lower-case with underscores naming strategy
    @Test(timeout = 4000)
    public void testLowerCaseWithUnderscoresWithOneArg() throws Exception {
        ObjectMapper mapper = createLowerCaseWithUnderscoresMapper();
        
        String json = "{\"param_name0\":\"value\"}";
        
        try {
            OneProperty result = mapper.readValue(json, OneProperty.class);
            assertNotNull("Result should not be null", result);
            assertEquals("Value should be deserialized correctly", "value", result.getParamName0());
        } catch (InvalidDefinitionException e) {
            fail("Failed to deserialize with lower-case with underscores naming strategy: " + e.getMessage());
        }
    }
    
    // Test 18: Multiple naming strategies with different property names
    @Test(timeout = 4000)
    public void testMultipleNamingStrategies() throws Exception {
        ObjectMapper mapper = createSnakeCaseMapper();
        
        // Test with explicit @JsonProperty that overrides naming strategy
        String json = "{\"explicit_name\":\"exp\"}";
        
        try {
            MixedProperties result = mapper.readValue(json, MixedProperties.class);
            assertNotNull("Result should not be null", result);
            assertEquals("Explicit name should match", "exp", result.getExplicitName());
        } catch (InvalidDefinitionException e) {
            fail("Failed to deserialize with explicit name: " + e.getMessage());
        }
    }
    
    // Test 19: Empty JSON object
    @Test(timeout = 4000)
    public void testEmptyJsonObject() throws Exception {
        ObjectMapper mapper = createSnakeCaseMapper();
        
        String json = "{}";
        
        try {
            OneProperty result = mapper.readValue(json, OneProperty.class);
            assertNotNull("Result should not be null", result);
            assertNull("Value should be null for empty JSON", result.getParamName0());
        } catch (InvalidDefinitionException e) {
            fail("Failed to deserialize empty JSON object: " + e.getMessage());
        }
    }
    
    // Test 20: Null JSON
    @Test(timeout = 4000)
    public void testNullJson() throws Exception {
        ObjectMapper mapper = createSnakeCaseMapper();
        
        try {
            OneProperty result = mapper.readValue("null", OneProperty.class);
            assertNull("Result should be null for null JSON", result);
        } catch (InvalidDefinitionException e) {
            fail("Failed to deserialize null JSON: " + e.getMessage());
        }
    }
}