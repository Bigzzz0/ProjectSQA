package com.fasterxml.jackson.databind.deser.impl;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.deser.CreatorProperty;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.type.TypeBindings;
import com.fasterxml.jackson.databind.util.ClassUtil;

import java.lang.reflect.Member;
import java.util.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: CreatorCollector.verifyNonDup() and related creator registration methods.
 * 
 * Key Branches:
 * 1. verifyNonDup: oldOne != null (line ~200)
 *    - 1a: (_explicitCreators & mask) != 0 && !explicit -> return early (skip new)
 *    - 1b: (_explicitCreators & mask) != 0 && explicit -> fall through to conflict check
 *    - 1c: (_explicitCreators & mask) == 0 -> fall through
 * 2. verifyNonDup: oldOne.getClass() == newOne.getClass() -> throw IllegalArgumentException (line ~208)
 *    - This is the defect path: when both are same class (e.g., both constructors), it throws even if they are compatible
 * 3. verifyNonDup: explicit flag sets _explicitCreators mask
 * 4. constructValueInstantiator: maybeVanilla logic with _hasNonDefaultCreator
 * 5. addPropertyCreator: duplicate name detection (HashMap logic)
 * 6. addIncompeteParameter: only sets if null
 * 7. _fixAccess: null check and _canFixAccess flag
 * 
 * Defect: In verifyNonDup, when oldOne.getClass() == newOne.getClass(), it throws IllegalArgumentException
 * without checking if the creators are actually conflicting (e.g., same constructor from different sources).
 * The fix should allow overriding when both are same class (e.g., both constructors) if explicit flags differ.
 */
public class CreatorCollectorDeepseekTest {

    /*
     * Helper to create a minimal AnnotatedWithParams for testing.
     * Uses AnnotatedConstructor as a concrete implementation.
     */
    private static class TestAnnotatedConstructor extends AnnotatedConstructor {
        private static final long serialVersionUID = 1L;
        private final String label;

        public TestAnnotatedConstructor(String label) {
            super(null, null, null);
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }

        @Override
        public Annotated withAnnotations(AnnotationMap annotations) {
            return this;
        }

        @Override
        public Class<?> getRawType() {
            return Object.class;
        }

        @Override
        public int getModifiers() {
            return 0;
        }

        @Override
        public String getName() {
            return label;
        }

        @Override
        public AnnotatedElement getAnnotated() {
            return null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestAnnotatedConstructor that = (TestAnnotatedConstructor) o;
            return Objects.equals(label, that.label);
        }

        @Override
        public int hashCode() {
            return Objects.hash(label);
        }
    }

    private static class TestBeanDescription extends BeanDescription {
        private final JavaType type;

        public TestBeanDescription(JavaType type) {
            super(null, null, null);
            this.type = type;
        }

        @Override
        public JavaType getType() {
            return type;
        }

        @Override
        public TypeBindings bindingsForBeanType() {
            return TypeBindings.emptyBindings();
        }

        @Override
        public Class<?> getBeanClass() {
            return type.getRawClass();
        }

        @Override
        public boolean hasKnownClassAnnotations() {
            return false;
        }

        @Override
        public AnnotatedClass getClassInfo() {
            return null;
        }

        @Override
        public Object instantiateBean(boolean fixAccess) {
            return null;
        }

        @Override
        public AnnotatedMethod findMethod(String name, Class<?>[] paramTypes) {
            return null;
        }

        @Override
        public AnnotatedConstructor findDefaultConstructor() {
            return null;
        }

        @Override
        public AnnotatedConstructor findConstructor(Class<?>[] paramTypes) {
            return null;
        }

        @Override
        public AnnotatedMethod findFactoryMethod(Class<?>... argTypes) {
            return null;
        }

        @Override
        public Map<Object, AnnotatedMember> findInjectables() {
            return null;
        }

        @Override
        public List<BeanPropertyDefinition> findProperties() {
            return null;
        }

        @Override
        public Set<String> getIgnoredPropertyNames() {
            return null;
        }

        @Override
        public ObjectIdInfo getObjectIdInfo() {
            return null;
        }

        @Override
        public Class<?> getDefaultViews() {
            return null;
        }

        @Override
        public boolean hasDefaultConstructor() {
            return false;
        }

        @Override
        public boolean isAbstract() {
            return false;
        }

        @Override
        public boolean isNonStaticInnerClass() {
            return false;
        }

        @Override
        public boolean isFinal() {
            return false;
        }

        @Override
        public boolean isPrimitive() {
            return false;
        }

        @Override
        public boolean isJsonValueType() {
            return false;
        }

        @Override
        public boolean isJsonViewType() {
            return false;
        }

        @Override
        public boolean isJsonManagedReference() {
            return false;
        }

        @Override
        public boolean isJsonBackReference() {
            return false;
        }

        @Override
        public boolean isJsonIgnore() {
            return false;
        }

        @Override
        public boolean isJsonAnyGetter() {
            return false;
        }

        @Override
        public boolean isJsonAnySetter() {
            return false;
        }

        @Override
        public boolean isJsonCreator() {
            return false;
        }

        @Override
        public boolean isJsonProperty() {
            return false;
        }

        @Override
        public boolean isJsonUnwrapped() {
            return false;
        }

        @Override
        public boolean isJsonTypeId() {
            return false;
        }

        @Override
        public boolean isJsonValue() {
            return false;
        }

        @Override
        public boolean isJsonRawValue() {
            return false;
        }

        @Override
        public boolean isJsonInclude() {
            return false;
        }

        @Override
        public boolean isJsonFormat() {
            return false;
        }

        @Override
        public boolean isJsonAnyGetterAnnotation() {
            return false;
        }

        @Override
        public boolean isJsonAnySetterAnnotation() {
            return false;
        }

        @Override
        public boolean isJsonCreatorAnnotation() {
            return false;
        }

        @Override
        public boolean isJsonPropertyAnnotation() {
            return false;
        }

        @Override
        public boolean isJsonUnwrappedAnnotation() {
            return false;
        }

        @Override
        public boolean isJsonTypeIdAnnotation() {
            return false;
        }

        @Override
        public boolean isJsonValueAnnotation() {
            return false;
        }

        @Override
        public boolean isJsonRawValueAnnotation() {
            return false;
        }

        @Override
        public boolean isJsonIncludeAnnotation() {
            return false;
        }

        @Override
        public boolean isJsonFormatAnnotation() {
            return false;
        }

        @Override
        public boolean isJsonIgnoreAnnotation() {
            return false;
        }

        @Override
        public boolean isJsonManagedReferenceAnnotation() {
            return false;
        }

        @Override
        public boolean isJsonBackReferenceAnnotation() {
            return false;
        }

        @Override
        public boolean isJsonViewAnnotation() {
            return false;
        }

        @Override
        public boolean isJsonAnyGetterAnnotation(AnnotatedMember member) {
            return false;
        }

        @Override
        public boolean isJsonAnySetterAnnotation(AnnotatedMember member) {
            return false;
        }

        @Override
        public boolean isJsonCreatorAnnotation(AnnotatedMember member) {
            return false;
        }

        @Override
        public boolean isJsonPropertyAnnotation(AnnotatedMember member) {
            return false;
        }

        @Override
        public boolean isJsonUnwrappedAnnotation(AnnotatedMember member) {
            return false;
        }

        @Override
        public boolean isJsonTypeIdAnnotation(AnnotatedMember member) {
            return false;
        }

        @Override
        public boolean isJsonValueAnnotation(AnnotatedMember member) {
            return false;
        }

        @Override
        public boolean isJsonRawValueAnnotation(AnnotatedMember member) {
            return false;
        }

        @Override
        public boolean isJsonIncludeAnnotation(AnnotatedMember member) {
            return false;
        }

        @Override
        public boolean isJsonFormatAnnotation(AnnotatedMember member) {
            return false;
        }

        @Override
        public boolean isJsonIgnoreAnnotation(AnnotatedMember member) {
            return false;
        }

        @Override
        public boolean isJsonManagedReferenceAnnotation(AnnotatedMember member) {
            return false;
        }

        @Override
        public boolean isJsonBackReferenceAnnotation(AnnotatedMember member) {
            return false;
        }

        @Override
        public boolean isJsonViewAnnotation(AnnotatedMember member) {
            return false;
        }

        @Override
        public boolean isJsonAnyGetterAnnotation(AnnotatedField field) {
            return false;
        }

        @Override
        public boolean isJsonAnySetterAnnotation(AnnotatedField field) {
            return false;
        }

        @Override
        public boolean isJsonCreatorAnnotation(AnnotatedField field) {
            return false;
        }

        @Override
        public boolean isJsonPropertyAnnotation(AnnotatedField field) {
            return false;
        }

        @Override
        public boolean isJsonUnwrappedAnnotation(AnnotatedField field) {
            return false;
        }

        @Override
        public boolean isJsonTypeIdAnnotation(AnnotatedField field) {
            return false;
        }

        @Override
        public boolean isJsonValueAnnotation(AnnotatedField field) {
            return false;
        }

        @Override
        public boolean isJsonRawValueAnnotation(AnnotatedField field) {
            return false;
        }

        @Override
        public boolean isJsonIncludeAnnotation(AnnotatedField field) {
            return false;
        }

        @Override
        public boolean isJsonFormatAnnotation(AnnotatedField field) {
            return false;
        }

        @Override
        public boolean isJsonIgnoreAnnotation(AnnotatedField field) {
            return false;
        }

        @Override
        public boolean isJsonManagedReferenceAnnotation(AnnotatedField field) {
            return false;
        }

        @Override
        public boolean isJsonBackReferenceAnnotation(AnnotatedField field) {
            return false;
        }

        @Override
        public boolean isJsonViewAnnotation(AnnotatedField field) {
            return false;
        }

        @Override
        public boolean isJsonAnyGetterAnnotation(AnnotatedMethod method) {
            return false;
        }

        @Override
        public boolean isJsonAnySetterAnnotation(AnnotatedMethod method) {
            return false;
        }

        @Override
        public boolean isJsonCreatorAnnotation(AnnotatedMethod method) {
            return false;
        }

        @Override
        public boolean isJsonPropertyAnnotation(AnnotatedMethod method) {
            return false;
        }

        @Override
        public boolean isJsonUnwrappedAnnotation(AnnotatedMethod method) {
            return false;
        }

        @Override
        public boolean isJsonTypeIdAnnotation(AnnotatedMethod method) {
            return false;
        }

        @Override
        public boolean isJsonValueAnnotation(AnnotatedMethod method) {
            return false;
        }

        @Override
        public boolean isJsonRawValueAnnotation(AnnotatedMethod method) {
            return false;
        }

        @Override
        public boolean isJsonIncludeAnnotation(AnnotatedMethod method) {
            return false;
        }

        @Override
        public boolean isJsonFormatAnnotation(AnnotatedMethod method) {
            return false;
        }

        @Override
        public boolean isJsonIgnoreAnnotation(AnnotatedMethod method) {
            return false;
        }

        @Override
        public boolean isJsonManagedReferenceAnnotation(AnnotatedMethod method) {
            return false;
        }

        @Override
        public boolean isJsonBackReferenceAnnotation(AnnotatedMethod method) {
            return false;
        }

        @Override
        public boolean isJsonViewAnnotation(AnnotatedMethod method) {
            return false;
        }
    }

    // ========== Partition A: Core Functional Logic & State Transitions ==========

    @Test(timeout = 4000)
    public void testDefaultCreatorRegistration() {
        CreatorCollector collector = new CreatorCollector(null, false);
        assertFalse("Default creator should not exist initially", collector.hasDefaultCreator());

        TestAnnotatedConstructor creator = new TestAnnotatedConstructor("default");
        collector.setDefaultCreator(creator);
        assertTrue("Default creator should exist after registration", collector.hasDefaultCreator());
    }

    @Test(timeout = 4000)
    public void testStringCreatorRegistration() {
        CreatorCollector collector = new CreatorCollector(null, false);
        TestAnnotatedConstructor creator = new TestAnnotatedConstructor("string");
        collector.addStringCreator(creator, true);
        // No exception expected
    }

    @Test(timeout = 4000)
    public void testIntCreatorRegistration() {
        CreatorCollector collector = new CreatorCollector(null, false);
        TestAnnotatedConstructor creator = new TestAnnotatedConstructor("int");
        collector.addIntCreator(creator, true);
    }

    @Test(timeout = 4000)
    public void testLongCreatorRegistration() {
        CreatorCollector collector = new CreatorCollector(null, false);
        TestAnnotatedConstructor creator = new TestAnnotatedConstructor("long");
        collector.addLongCreator(creator, true);
    }

    @Test(timeout = 4000)
    public void testDoubleCreatorRegistration() {
        CreatorCollector collector = new CreatorCollector(null, false);
        TestAnnotatedConstructor creator = new TestAnnotatedConstructor("double");
        collector.addDoubleCreator(creator, true);
    }

    @Test(timeout = 4000)
    public void testBooleanCreatorRegistration() {
        CreatorCollector collector = new CreatorCollector(null, false);
        TestAnnotatedConstructor creator = new TestAnnotatedConstructor("boolean");
        collector.addBooleanCreator(creator, true);
    }

    @Test(timeout = 4000)
    public void testDelegatingCreatorRegistration() {
        CreatorCollector collector = new CreatorCollector(null, false);
        TestAnnotatedConstructor creator = new TestAnnotatedConstructor("delegate");
        collector.addDelegatingCreator(creator, true, null);
    }

    @Test(timeout = 4000)
    public void testPropertyCreatorRegistration() {
        CreatorCollector collector = new CreatorCollector(null, false);
        TestAnnotatedConstructor creator = new TestAnnotatedConstructor("props");
        CreatorProperty[] props = new CreatorProperty[0];
        collector.addPropertyCreator(creator, true, props);
    }

    @Test(timeout = 4000)
    public void testIncompleteParameterRegistration() {
        CreatorCollector collector = new CreatorCollector(null, false);
        AnnotatedParameter param = null; // We can't easily create one, but test null handling
        collector.addIncompeteParameter(param);
        // No exception expected
    }

    // ========== Partition B: Boundary Value Analysis & Extremes ==========

    @Test(timeout = 4000)
    public void testNullCreatorRegistration() {
        CreatorCollector collector = new CreatorCollector(null, false);
        collector.setDefaultCreator(null);
        assertFalse("Default creator should remain null", collector.hasDefaultCreator());
    }

    @Test(timeout = 4000)
    public void testEmptyPropertyArray() {
        CreatorCollector collector = new CreatorCollector(null, false);
        TestAnnotatedConstructor creator = new TestAnnotatedConstructor("props");
        CreatorProperty[] props = new CreatorProperty[0];
        collector.addPropertyCreator(creator, true, props);
    }

    @Test(timeout = 4000)
    public void testSinglePropertyNoDuplicateCheck() {
        CreatorCollector collector = new CreatorCollector(null, false);
        TestAnnotatedConstructor creator = new TestAnnotatedConstructor("props");
        CreatorProperty prop = new CreatorProperty(null, null, null, null, null, null, false, null, null, false);
        CreatorProperty[] props = new CreatorProperty[] { prop };
        collector.addPropertyCreator(creator, true, props);
    }

    @Test(timeout = 4000)
    public void testDuplicatePropertyNames() {
        CreatorCollector collector = new CreatorCollector(null, false);
        TestAnnotatedConstructor creator = new TestAnnotatedConstructor("props");
        CreatorProperty prop1 = new CreatorProperty(null, null, null, "dup", null, null, false, null, null, false);
        CreatorProperty prop2 = new CreatorProperty(null, null, null, "dup", null, null, false, null, null, false);
        CreatorProperty[] props = new CreatorProperty[] { prop1, prop2 };
        try {
            collector.addPropertyCreator(creator, true, props);
            fail("Should have thrown IllegalArgumentException for duplicate property names");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Duplicate creator property"));
        }
    }

    @Test(timeout = 4000)
    public void testPropertyWithEmptyNameAndInjectable() {
        CreatorCollector collector = new CreatorCollector(null, false);
        TestAnnotatedConstructor creator = new TestAnnotatedConstructor("props");
        CreatorProperty prop = new CreatorProperty(null, null, null, "", null, null, false, null, "injectableId", false);
        CreatorProperty[] props = new CreatorProperty[] { prop };
        collector.addPropertyCreator(creator, true, props);
        // Should not throw because empty name with injectable is skipped
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    @Test(timeout = 4000)
    public void testVerifyNonDupSameClassConflict() {
        // This test targets the known defect: when two creators of the same class are registered,
        // it should NOT throw if they are compatible (e.g., same constructor from different sources)
        CreatorCollector collector = new CreatorCollector(null, false);
        TestAnnotatedConstructor creator1 = new TestAnnotatedConstructor("StringBuilder constructor");
        TestAnnotatedConstructor creator2 = new TestAnnotatedConstructor("StringBuilder constructor");

        // First registration should succeed
        collector.addStringCreator(creator1, true);

        // Second registration with same class should NOT throw (defect: it currently throws)
        // The bug is that it throws IllegalArgumentException when it should allow override
        try {
            collector.addStringCreator(creator2, true);
            // If we reach here, the bug is fixed (no exception thrown)
            // This is the expected correct behavior
        } catch (IllegalArgumentException e) {
            // If exception is thrown, the bug is present
            // We re-throw to make the test fail, revealing the defect
            throw new AssertionError("Defect revealed: Conflicting creators of same class should not throw", e);
        }
    }

    @Test(timeout = 4000)
    public void testVerifyNonDupExplicitOverridesNonExplicit() {
        CreatorCollector collector = new CreatorCollector(null, false);
        TestAnnotatedConstructor creator1 = new TestAnnotatedConstructor("implicit");
        TestAnnotatedConstructor creator2 = new TestAnnotatedConstructor("explicit");

        // Register implicit first
        collector.addStringCreator(creator1, false);

        // Register explicit second - should override without exception
        collector.addStringCreator(creator2, true);
    }

    @Test(timeout = 4000)
    public void testVerifyNonDupNonExplicitDoesNotOverrideExplicit() {
        CreatorCollector collector = new CreatorCollector(null, false);
        TestAnnotatedConstructor creator1 = new TestAnnotatedConstructor("explicit");
        TestAnnotatedConstructor creator2 = new TestAnnotatedConstructor("implicit");

        // Register explicit first
        collector.addStringCreator(creator1, true);

        // Register implicit second - should NOT override, no exception
        collector.addStringCreator(creator2, false);
    }

    @Test(timeout = 4000)
    public void testVerifyNonDupDifferentClasses() {
        CreatorCollector collector = new CreatorCollector(null, false);
        TestAnnotatedConstructor creator1 = new TestAnnotatedConstructor("first") {
            private static final long serialVersionUID = 1L;
        };
        TestAnnotatedConstructor creator2 = new TestAnnotatedConstructor("second") {
            private static final long serialVersionUID = 1L;
        };

        // Different anonymous classes should be allowed
        collector.addStringCreator(creator1, true);
        collector.addStringCreator(creator2, true);
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000)
    public void testVerifyNonDupExplicitBothSameClass() {
        CreatorCollector collector = new CreatorCollector(null, false);
        TestAnnotatedConstructor creator1 = new TestAnnotatedConstructor("same");
        TestAnnotatedConstructor creator2 = new TestAnnotatedConstructor("same");

        collector.addStringCreator(creator1, true);
        // This should throw because both are explicit and same class
        try {
            collector.addStringCreator(creator2, true);
            fail("Should have thrown IllegalArgumentException for conflicting explicit creators");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Conflicting"));
        }
    }

    @Test(timeout = 4000)
    public void testPropertyCreatorWithDuplicateNamesAndInjectables() {
        CreatorCollector collector = new CreatorCollector(null, false);
        TestAnnotatedConstructor creator = new TestAnnotatedConstructor("props");
        CreatorProperty prop1 = new CreatorProperty(null, null, null, "name", null, null, false, null, null, false);
        CreatorProperty prop2 = new CreatorProperty(null, null, null, "name", null, null, false, null, "injectable", false);
        CreatorProperty[] props = new CreatorProperty[] { prop1, prop2 };
        try {
            collector.addPropertyCreator(creator, true, props);
            fail("Should have thrown IllegalArgumentException for duplicate property names");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Duplicate creator property"));
        }
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testConstructValueInstantiatorWithNoCreators() {
        // Create a minimal BeanDescription with a simple type
        JavaType type = new JavaType() {
            private static final long serialVersionUID = 1L;

            @Override
            public Class<?> getRawClass() {
                return Object.class;
            }

            @Override
            public boolean isAbstract() {
                return false;
            }

            @Override
            public boolean isFinal() {
                return false;
            }

            @Override
            public boolean isCollectionLike() {
                return false;
            }

            @Override
            public boolean isMapLike() {
                return false;
            }

            @Override
            public JavaType withTypeHandler(Object h) {
                return this;
            }

            @Override
            public JavaType withValueHandler(Object h) {
                return this;
            }

            @Override
            public JavaType withContentTypeHandler(Object h) {
                return this;
            }

            @Override
            public JavaType withContentValueHandler(Object h) {
                return this;
            }

            @Override
            public JavaType withStaticTyping() {
                return this;
            }

            @Override
            public JavaType narrowBy(Class<?> subclass) {
                return this;
            }

            @Override
            public JavaType forceNarrowBy(Class<?> subclass) {
                return this;
            }

            @Override
            public String toCanonical() {
                return "java.lang.Object";
            }

            @Override
            public boolean isTypeOrSubTypeOf(Class<?> clz) {
                return clz == Object.class;
            }

            @Override
            public boolean isContainerType() {
                return false;
            }

            @Override
            public boolean isPrimitive() {
                return false;
            }

            @Override
            public boolean isArrayType() {
                return false;
            }

            @Override
            public boolean isEnumType() {
                return false;
            }

            @Override
            public boolean isInterface() {
                return false;
            }

            @Override
            public boolean isThrowable() {
                return false;
            }

            @Override
            public JavaType containedType(int index) {
                return null;
            }

            @Override
            public int containedTypeCount() {
                return 0;
            }

            @Override
            public String containedTypeName(int index) {
                return null;
            }

            @Override
            public JavaType getKeyType() {
                return null;
            }

            @Override
            public JavaType getContentType() {
                return null;
            }

            @Override
            public JavaType getReferencedType() {
                return null;
            }

            @Override
            public JavaType getSuperClass() {
                return null;
            }

            @Override
            public List<JavaType> getInterfaces() {
                return null;
            }

            @Override
            public JavaType findSuperType(Class<?> rawTarget) {
                return null;
            }

            @Override
            public JavaType[] findTypeParameters(Class<?> expType) {
                return new JavaType[0];
            }

            @Override
            public boolean hasGenericTypes() {
                return false;
            }

            @Override
            public boolean isGenericType() {
                return false;
            }

            @Override
            public String getGenericSignature() {
                return null;
            }

            @Override
            public boolean isConcrete() {
                return true;
            }

            @Override
            public JavaType assertConcrete() {
                return this;
            }

            @Override
            public boolean isJavaLangObject() {
                return true;
            }

            @Override
            public StringBuilder getErasedSignature(StringBuilder sb) {
                return sb.append("Ljava/lang/Object;");
            }

            @Override
            public StringBuilder getGenericSignature(StringBuilder sb) {
                return sb.append("Ljava/lang/Object;");
            }

            @Override
            protected JavaType _narrow(Class<?> subclass) {
                return this;
            }
        };
        BeanDescription beanDesc = new TestBeanDescription(type);
        CreatorCollector collector = new CreatorCollector(beanDesc, false);
        DeserializationConfig config = null; // We can't easily create one, but method handles null
        ValueInstantiator instantiator = collector.constructValueInstantiator(config);
        assertNotNull("Should return an instantiator", instantiator);
    }

    @Test(timeout = 4000)
    public void testConstructValueInstantiatorWithVanillaTypes() {
        // Test for Collection type
        JavaType collectionType = new JavaType() {
            private static final long serialVersionUID = 1L;

            @Override
            public Class<?> getRawClass() {
                return ArrayList.class;
            }

            @Override
            public boolean isAbstract() { return false; }
            @Override
            public boolean isFinal() { return false; }
            @Override
            public boolean isCollectionLike() { return true; }
            @Override
            public boolean isMapLike() { return false; }
            @Override
            public JavaType withTypeHandler(Object h) { return this; }
            @Override
            public JavaType withValueHandler(Object h) { return this; }
            @Override
            public JavaType withContentTypeHandler(Object h) { return this; }
            @Override
            public JavaType withContentValueHandler(Object h) { return this; }
            @Override
            public JavaType withStaticTyping() { return this; }
            @Override
            public JavaType narrowBy(Class<?> subclass) { return this; }
            @Override
            public JavaType forceNarrowBy(Class<?> subclass) { return this; }
            @Override
            public String toCanonical() { return "java.util.ArrayList"; }
            @Override
            public boolean isTypeOrSubTypeOf(Class<?> clz) { return clz == ArrayList.class; }
            @Override
            public boolean isContainerType() { return true; }
            @Override
            public boolean isPrimitive() { return false; }
            @Override
            public boolean isArrayType() { return false; }
            @Override
            public boolean isEnumType() { return false; }
            @Override
            public boolean isInterface() { return false; }
            @Override
            public boolean isThrowable() { return false; }
            @Override
            public JavaType containedType(int index) { return null; }
            @Override
            public int containedTypeCount() { return 0; }
            @Override
            public String containedTypeName(int index) { return null; }
            @Override
            public JavaType getKeyType() { return null; }
            @Override
            public JavaType getContentType() { return null; }
            @Override
            public JavaType getReferencedType() { return null; }
            @Override
            public JavaType getSuperClass() { return null; }
            @Override
            public List<JavaType> getInterfaces() { return null; }
            @Override
            public JavaType findSuperType(Class<?> rawTarget) { return null; }
            @Override
            public JavaType[] findTypeParameters(Class<?> expType) { return new JavaType[0]; }
            @Override
            public boolean hasGenericTypes() { return false; }
            @Override
            public boolean isGenericType() { return false; }
            @Override
            public String getGenericSignature() { return null; }
            @Override
            public boolean isConcrete() { return true; }
            @Override
            public JavaType assertConcrete() { return this; }
            @Override
            public boolean isJavaLangObject() { return false; }
            @Override
            public StringBuilder getErasedSignature(StringBuilder sb) { return sb.append("Ljava/util/ArrayList;"); }
            @Override
            public StringBuilder getGenericSignature(StringBuilder sb) { return sb.append("Ljava/util/ArrayList;"); }
            @Override
            protected JavaType _narrow(Class<?> subclass) { return this; }
        };
        BeanDescription beanDesc = new TestBeanDescription(collectionType);
        CreatorCollector collector = new CreatorCollector(beanDesc, false);
        ValueInstantiator instantiator = collector.constructValueInstantiator(null);
        assertNotNull("Should return a Vanilla instantiator for ArrayList", instantiator);
        assertTrue("Should be Vanilla type", instantiator instanceof CreatorCollector.Vanilla);
    }

    @Test(timeout = 4000)
    public void testFixAccessWithCanFixAccessTrue() {
        CreatorCollector collector = new CreatorCollector(null, true);
        TestAnnotatedConstructor creator = new TestAnnotatedConstructor("test");
        collector.setDefaultCreator(creator);
        assertTrue("Default creator should exist", collector.hasDefaultCreator());
    }

    @Test(timeout = 4000)
    public void testFixAccessWithCanFixAccessFalse() {
        CreatorCollector collector = new CreatorCollector(null, false);
        TestAnnotatedConstructor creator = new TestAnnotatedConstructor("test");
        collector.setDefaultCreator(creator);
        assertTrue("Default creator should exist", collector.hasDefaultCreator());
    }

    @Test(timeout = 4000)
    public void testDeprecatedMethods() {
        CreatorCollector collector = new CreatorCollector(null, false);
        TestAnnotatedConstructor creator = new TestAnnotatedConstructor("test");
        
        // Test deprecated methods (they should not throw)
        collector.addStringCreator(creator);
        collector.addIntCreator(creator);
        collector.addLongCreator(creator);
        collector.addDoubleCreator(creator);
        collector.addBooleanCreator(creator);
        collector.addDelegatingCreator(creator, null);
        collector.addPropertyCreator(creator, new CreatorProperty[0]);
    }
}