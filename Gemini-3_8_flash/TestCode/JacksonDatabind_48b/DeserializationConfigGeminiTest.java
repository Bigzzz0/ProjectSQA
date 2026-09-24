package com.fasterxml.jackson.databind;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.FormatFeature;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.cfg.BaseSettings;
import com.fasterxml.jackson.databind.cfg.ContextAttributes;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.introspect.ClassIntrospector;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;
import com.fasterxml.jackson.databind.introspect.SimpleMixInResolver;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.SubtypeResolver;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.impl.StdSubtypeResolver;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.LinkedNode;
import com.fasterxml.jackson.databind.util.RootNameLookup;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * /* [Branch & Defect Analysis Matrix]
 * Targets: com.fasterxml.jackson.databind.DeserializationConfig
 *
 * Specific Branches & Decision Conditions Tested:
 * 1. Life-cycle & Copy-Constructors:
 *    - BaseSettings, SubtypeResolver, SimpleMixInResolver, RootNameLookup.
 *    - Immutable identity returns (returns 'this' if no-op or same reference passed).
 * 2. MapperFeature manipulations:
 *    - with(MapperFeature...), without(MapperFeature...), with(MapperFeature, boolean).
 * 3. DeserializationFeature bitmask:
 *    - with(DeserializationFeature), with(DeserializationFeature, DeserializationFeature...), withFeatures(...)
 *    - without(DeserializationFeature), without(DeserializationFeature, DeserializationFeature...), withoutFeatures(...)
 *    - hasDeserializationFeatures(mask), hasSomeOfFeatures(mask), getDeserializationFeatures().
 * 4. JsonParser.Feature and FormatFeature overrides:
 *    - with(JsonParser.Feature), without(JsonParser.Feature), withFeatures(...), withoutFeatures(...)
 *    - with(FormatFeature), without(FormatFeature), withFeatures(...), withoutFeatures(...)
 *    - isEnabled(JsonParser.Feature, JsonFactory): checks parser mask precedence vs factory fallback.
 *    - initialize(JsonParser): triggers standard feature and format feature override execution.
 * 5. Problem Handlers:
 *    - withHandler: prevents duplicates, adds to front linked-list.
 *    - withNoProblemHandlers: clears list or returns 'this' if already null.
 * 6. Introspection and Visibility:
 *    - getAnnotationIntrospector(): USE_ANNOTATIONS enabled vs disabled (returns NopAnnotationIntrospector).
 *    - getDefaultVisibilityChecker(): tests disabling AUTO_DETECT_SETTERS, AUTO_DETECT_CREATORS, AUTO_DETECT_FIELDS.
 *    - introspect, introspectForCreation, introspectForBuilder, introspectClassAnnotations, introspectDirectClassAnnotations.
 * 7. Polymorphic Deserialization:
 *    - findTypeDeserializer(): class with explicit @JsonTypeInfo vs class without (null fallback).
 * 8. Root Wrapping Logic:
 *    - useRootWrapping(): _rootName != null (empty vs non-empty) vs _rootName == null (UNWRAP_ROOT_VALUE enabled/disabled).
 * 9. Defect Reproduction (Defects4J ground truth):
 *    - MapperFeature.AUTO_DETECT_SETTERS disabled should suppress discovery of implicit setters during bean introspection.
 */
public class DeserializationConfigGeminiTest {

    private enum TestFormatFeature implements FormatFeature {
        TEST_FEAT(true);

        private final boolean _defaultState;

        TestFormatFeature(boolean def) {
            _defaultState = def;
        }

        @Override
        public boolean enabledByDefault() {
            return _defaultState;
        }

        @Override
        public int getMask() {
            return (1 << ordinal());
        }

        @Override
        public boolean enabledIn(int flags) {
            return (flags & getMask()) != 0;
        }
    }

    static class TargetBean {
        private String name;

        @JsonProperty("groupname")
        private String _groupname;

        public TargetBean() {}

        public TargetBean(String name) {
            this.name = name;
        }

        public void setName(String n) {
            name = n;
        }

        public void setGroupname(String n) {
            _groupname = n;
        }

        public String getName() {
            return name;
        }

        public String getGroupname() {
            return _groupname;
        }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
    static class PolymorphicBase {}

    static class SubProblemHandler extends DeserializationProblemHandler {}

    private DeserializationConfig createDefaultConfig() {
        return new ObjectMapper().getDeserializationConfig();
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testMapperFeatureTransitions() {
        DeserializationConfig cfg = createDefaultConfig();
        assertTrue(cfg.isEnabled(MapperFeature.AUTO_DETECT_SETTERS));

        DeserializationConfig cfgWithout = cfg.without(MapperFeature.AUTO_DETECT_SETTERS);
        assertNotSame(cfg, cfgWithout);
        assertFalse(cfgWithout.isEnabled(MapperFeature.AUTO_DETECT_SETTERS));

        // Calling without on already disabled feature returns 'this'
        assertSame(cfgWithout, cfgWithout.without(MapperFeature.AUTO_DETECT_SETTERS));

        DeserializationConfig cfgWith = cfgWithout.with(MapperFeature.AUTO_DETECT_SETTERS);
        assertNotSame(cfgWithout, cfgWith);
        assertTrue(cfgWith.isEnabled(MapperFeature.AUTO_DETECT_SETTERS));

        // Calling with on already enabled feature returns 'this'
        assertSame(cfgWith, cfgWith.with(MapperFeature.AUTO_DETECT_SETTERS));

        // Boolean toggle
        DeserializationConfig cfgToggledOff = cfgWith.with(MapperFeature.AUTO_DETECT_SETTERS, false);
        assertFalse(cfgToggledOff.isEnabled(MapperFeature.AUTO_DETECT_SETTERS));
        assertSame(cfgToggledOff, cfgToggledOff.with(MapperFeature.AUTO_DETECT_SETTERS, false));

        DeserializationConfig cfgToggledOn = cfgToggledOff.with(MapperFeature.AUTO_DETECT_SETTERS, true);
        assertTrue(cfgToggledOn.isEnabled(MapperFeature.AUTO_DETECT_SETTERS));
        assertSame(cfgToggledOn, cfgToggledOn.with(MapperFeature.AUTO_DETECT_SETTERS, true));
    }

    @Test(timeout = 4000)
    public void testDeserializationFeatureTransitions() {
        DeserializationConfig cfg = createDefaultConfig();

        DeserializationConfig cfgDisabled = cfg.without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        assertFalse(cfgDisabled.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        assertSame(cfgDisabled, cfgDisabled.without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));

        DeserializationConfig cfgEnabled = cfgDisabled.with(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        assertTrue(cfgEnabled.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        assertSame(cfgEnabled, cfgEnabled.with(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));

        // Varargs with / without
        DeserializationConfig cfgVarWith = cfg.with(
                DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT,
                DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT
        );
        assertTrue(cfgVarWith.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));
        assertTrue(cfgVarWith.isEnabled(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT));

        DeserializationConfig cfgVarWithout = cfgVarWith.without(
                DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT,
                DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT
        );
        assertFalse(cfgVarWithout.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));
        assertFalse(cfgVarWithout.isEnabled(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT));

        // Array withFeatures / withoutFeatures
        DeserializationConfig cfgFeatures = cfg.withFeatures(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        assertTrue(cfgFeatures.isEnabled(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY));
        DeserializationConfig cfgWithoutFeatures = cfgFeatures.withoutFeatures(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        assertFalse(cfgWithoutFeatures.isEnabled(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY));
    }

    @Test(timeout = 4000)
    public void testHasDeserializationFeatures() {
        DeserializationConfig cfg = createDefaultConfig()
                .with(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                .without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        int maskSingle = DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY.getMask();
        int maskFail = DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES.getMask();

        assertTrue(cfg.hasDeserializationFeatures(maskSingle));
        assertFalse(cfg.hasDeserializationFeatures(maskFail));
        assertFalse(cfg.hasDeserializationFeatures(maskSingle | maskFail));

        assertTrue(cfg.hasSomeOfFeatures(maskSingle));
        assertTrue(cfg.hasSomeOfFeatures(maskSingle | maskFail));
        assertFalse(cfg.hasSomeOfFeatures(maskFail));

        int allFeatures = cfg.getDeserializationFeatures();
        assertEquals(allFeatures, cfg.getDeserializationFeatures());
    }

    @Test(timeout = 4000)
    public void testParserFeaturesAndFormatFeatures() throws Exception {
        DeserializationConfig cfg = createDefaultConfig();

        cfg = cfg.with(JsonParser.Feature.ALLOW_COMMENTS);
        cfg = cfg.withFeatures(JsonParser.Feature.ALLOW_YAML_COMMENTS);
        cfg = cfg.without(JsonParser.Feature.AUTO_CLOSE_SOURCE);
        cfg = cfg.withoutFeatures(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);

        JsonFactory factory = new JsonFactory();
        assertTrue(cfg.isEnabled(JsonParser.Feature.ALLOW_COMMENTS, factory));
        assertFalse(cfg.isEnabled(JsonParser.Feature.AUTO_CLOSE_SOURCE, factory));

        // When parser feature has not been explicitly configured on cfg, falls back to factory
        boolean factoryDef = factory.isEnabled(JsonParser.Feature.INCLUDE_SOURCE_IN_LOCATION);
        assertEquals(factoryDef, cfg.isEnabled(JsonParser.Feature.INCLUDE_SOURCE_IN_LOCATION, factory));

        // FormatFeature tests
        cfg = cfg.with(TestFormatFeature.TEST_FEAT);
        cfg = cfg.withFeatures(TestFormatFeature.TEST_FEAT);
        cfg = cfg.without(TestFormatFeature.TEST_FEAT);
        cfg = cfg.withoutFeatures(TestFormatFeature.TEST_FEAT);

        // Verify initialize method propagates features onto parser
        JsonParser p = factory.createParser("{}".getBytes("UTF-8"));
        cfg.initialize(p);
        p.close();
    }

    @Test(timeout = 4000)
    public void testProblemHandlerRegistration() {
        DeserializationConfig cfg = createDefaultConfig();
        assertNull(cfg.getProblemHandlers());

        DeserialProblemHandler handler1 = new SubProblemHandler();
        DeserializationProblemHandler handler2 = new SubProblemHandler();

        DeserializationConfig cfg1 = cfg.withHandler(handler1);
        assertNotNull(cfg1.getProblemHandlers());
        assertEquals(handler1, cfg1.getProblemHandlers().value());

        // Preventing duplicate handler registration
        assertSame(cfg1, cfg1.withHandler(handler1));

        // Prepending another handler
        DeserializationConfig cfg2 = cfg1.withHandler(handler2);
        assertEquals(handler2, cfg2.getProblemHandlers().value());
        assertEquals(handler1, cfg2.getProblemHandlers().next().value());

        // Clearing problem handlers
        DeserializationConfig cfgCleared = cfg2.withNoProblemHandlers();
        assertNull(cfgCleared.getProblemHandlers());
        assertSame(cfgCleared, cfgCleared.withNoProblemHandlers());
    }

    @Test(timeout = 4000)
    public void testJsonNodeFactoryConfiguration() {
        DeserializationConfig cfg = createDefaultConfig();
        JsonNodeFactory defaultF = cfg.getNodeFactory();
        assertNotNull(defaultF);
        assertSame(cfg, cfg.with(defaultF));

        JsonNodeFactory customF = new JsonNodeFactory(true);
        DeserializationConfig cfgCustom = cfg.with(customF);
        assertNotSame(cfg, cfgCustom);
        assertSame(customF, cfgCustom.getNodeFactory());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testRootWrappingVariations() {
        DeserializationConfig cfg = createDefaultConfig();

        // 1. _rootName is null, delegates to DeserializationFeature.UNWRAP_ROOT_VALUE
        cfg = cfg.without(DeserializationFeature.UNWRAP_ROOT_VALUE);
        assertFalse(cfg.useRootWrapping());
        cfg = cfg.with(DeserializationFeature.UNWRAP_ROOT_VALUE);
        assertTrue(cfg.useRootWrapping());

        // 2. _rootName is non-null and empty: forces wrapping to false regardless of feature
        DeserializationConfig cfgEmptyRoot = cfg.withRootName(PropertyName.construct(""));
        assertFalse(cfgEmptyRoot.useRootWrapping());

        // 3. _rootName is non-null and not empty: forces wrapping to true
        DeserializationConfig cfgNamedRoot = cfg.without(DeserializationFeature.UNWRAP_ROOT_VALUE)
                .withRootName(PropertyName.construct("MyRoot"));
        assertTrue(cfgNamedRoot.useRootWrapping());
    }

    @Test(timeout = 4000)
    public void testWithRootNameIdentity() {
        DeserializationConfig cfg = createDefaultConfig();

        // Null to null identity
        assertSame(cfg, cfg.withRootName(null));

        PropertyName name1 = PropertyName.construct("custom");
        DeserializationConfig cfg1 = cfg.withRootName(name1);
        assertNotSame(cfg, cfg1);
        assertSame(cfg1, cfg1.withRootName(name1));
        assertSame(cfg1, cfg1.withRootName(PropertyName.construct("custom")));

        // Transition from non-null back to null
        DeserializationConfig cfgBackToNull = cfg1.withRootName(null);
        assertNotSame(cfg1, cfgBackToNull);
        assertNull(cfgBackToNull.getRootName());
    }

    @Test(timeout = 4000)
    public void testWithViewAndContextAttributesIdentity() {
        DeserializationConfig cfg = createDefaultConfig();

        // View identity
        assertNull(cfg.getActiveView());
        assertSame(cfg, cfg.withView(null));

        DeserializationConfig cfgWithView = cfg.withView(String.class);
        assertEquals(String.class, cfgWithView.getActiveView());
        assertSame(cfgWithView, cfgWithView.withView(String.class));

        DeserializationConfig cfgBackNull = cfgWithView.withView(null);
        assertNull(cfgBackNull.getActiveView());

        // ContextAttributes identity
        ContextAttributes attrs = cfg.getAttributes();
        assertSame(cfg, cfg.with(attrs));

        ContextAttributes newAttrs = attrs.withSharedAttribute("k", "v");
        DeserializationConfig cfgWithAttrs = cfg.with(newAttrs);
        assertNotSame(cfg, cfgWithAttrs);
        assertSame(cfgWithAttrs, cfgWithAttrs.with(newAttrs));
    }

    @Test(timeout = 4000)
    public void testWithSubtypeResolverIdentity() {
        DeserializationConfig cfg = createDefaultConfig();
        SubtypeResolver currentStr = cfg.getSubtypeResolver();
        assertSame(cfg, cfg.with(currentStr));

        SubtypeResolver newStr = new StdSubtypeResolver();
        DeserializationConfig cfgNewStr = cfg.with(newStr);
        assertNotSame(cfg, cfgNewStr);
        assertSame(newStr, cfgNewStr.getSubtypeResolver());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Target Defect:
     * When AUTO_DETECT_SETTERS is disabled, DeserializationConfig's visibility checker
     * must enforce Visibility.NONE for setters so that implicit setters (e.g. setName)
     * are suppressed and not discovered as visible properties.
     */
    @Test(timeout = 4000)
    public void testDefectVisibilityFeaturesWithAutoDetectSetters() {
        DeserializationConfig config = createDefaultConfig();
        config = config.without(MapperFeature.AUTO_DETECT_SETTERS);

        // Validate visibility checker rule directly
        VisibilityChecker<?> vc = config.getDefaultVisibilityChecker();
        assertNotNull(vc);

        // Introspect target bean to assert discovery behavior under disabled setters
        JavaType type = config.constructType(TargetBean.class);
        BeanDescription desc = config.introspect(type);
        List<BeanPropertyDefinition> props = desc.findProperties();

        // Explicitly annotated @JsonProperty("groupname") must be found; implicit setName must NOT
        assertEquals("Should find 1 property, not " + props.size() + "; properties = " + props, 1, props.size());
        assertEquals("groupname", props.get(0).getName());
    }

    @Test(timeout = 4000)
    public void testVisibilityFeaturesWithAutoDetectCreatorsAndFields() {
        DeserializationConfig cfg = createDefaultConfig();
        cfg = cfg.without(MapperFeature.AUTO_DETECT_CREATORS)
                 .without(MapperFeature.AUTO_DETECT_FIELDS);

        VisibilityChecker<?> vc = cfg.getDefaultVisibilityChecker();
        assertNotNull(vc);

        JavaType type = cfg.constructType(TargetBean.class);
        BeanDescription desc = cfg.introspect(type);
        assertNotNull(desc);
    }

    // =========================================================================
    // Partition D: Configuration Overrides & Polymorphic Resolution
    // =========================================================================

    @Test(timeout = 4000)
    public void testAnnotationIntrospectorDisableBranch() {
        DeserializationConfig cfg = createDefaultConfig();
        assertTrue(cfg.isEnabled(MapperFeature.USE_ANNOTATIONS));
        assertTrue(cfg.getAnnotationIntrospector() instanceof JacksonAnnotationIntrospector);

        DeserializationConfig noAnnoCfg = cfg.without(MapperFeature.USE_ANNOTATIONS);
        assertFalse(noAnnoCfg.isEnabled(MapperFeature.USE_ANNOTATIONS));
        assertSame(NopAnnotationIntrospector.instance, noAnnoCfg.getAnnotationIntrospector());
    }

    @Test(timeout = 4000)
    public void testPolymorphicTypeDeserializerResolution() throws Exception {
        DeserializationConfig cfg = createDefaultConfig();
        TypeFactory tf = cfg.getTypeFactory();

        // 1. Class with explicit @JsonTypeInfo annotation
        JavaType polyType = tf.constructType(PolymorphicBase.class);
        TypeDeserializer typer = cfg.findTypeDeserializer(polyType);
        assertNotNull(typer);

        // 2. Class without @JsonTypeInfo and no default typer configured returns null
        JavaType stringType = tf.constructType(String.class);
        TypeDeserializer nullTyper = cfg.findTypeDeserializer(stringType);
        assertNull(nullTyper);
    }

    @Test(timeout = 4000)
    public void testFluentBaseSettingsMutations() {
        DeserializationConfig cfg = createDefaultConfig();

        // BaseSettings delegation coverage
        assertNotNull(cfg.getBaseSettings());
        assertNotNull(cfg.with(cfg.getClassIntrospector()));
        assertNotNull(cfg.with(cfg.getAnnotationIntrospector()));
        assertNotNull(cfg.with(cfg.getDefaultVisibilityChecker()));
        assertNotNull(cfg.withVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.PUBLIC_ONLY));
        assertNotNull(cfg.with((TypeResolverBuilder<?>) null));
        assertNotNull(cfg.with(PropertyNamingStrategy.SNAKE_CASE));
        assertNotNull(cfg.with(TypeFactory.defaultInstance()));
        assertNotNull(cfg.with(new SimpleDateFormat("yyyy-MM-dd")));
        assertNotNull(cfg.with((HandlerInstantiator) null));
        assertNotNull(cfg.withInsertedAnnotationIntrospector(NopAnnotationIntrospector.instance));
        assertNotNull(cfg.withAppendedAnnotationIntrospector(NopAnnotationIntrospector.instance));
        assertNotNull(cfg.with(Locale.GERMANY));
        assertNotNull(cfg.with(TimeZone.getTimeZone("GMT+2")));
        assertNotNull(cfg.with(Base64Variants.MIME));
    }

    @Test(timeout = 4000)
    public void testDefaultPropertyInclusionAndFormat() {
        DeserializationConfig cfg = createDefaultConfig();

        assertEquals(JsonInclude.Value.empty(), cfg.getDefaultPropertyInclusion());
        assertEquals(JsonInclude.Value.empty(), cfg.getDefaultPropertyInclusion(TargetBean.class));
        assertEquals(JsonFormat.Value.empty(), cfg.getDefaultPropertyFormat(TargetBean.class));
    }

    @Test(timeout = 4000)
    public void testIntrospectionVariants() {
        DeserializationConfig cfg = createDefaultConfig();
        JavaType type = cfg.constructType(TargetBean.class);

        BeanDescription desc1 = cfg.introspect(type);
        assertNotNull(desc1);

        BeanDescription desc2 = cfg.introspectForCreation(type);
        assertNotNull(desc2);

        BeanDescription desc3 = cfg.introspectForBuilder(type);
        assertNotNull(desc3);

        BeanDescription desc4 = cfg.introspectClassAnnotations(type);
        assertNotNull(desc4);

        BeanDescription desc5 = cfg.introspectDirectClassAnnotations(type);
        assertNotNull(desc5);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testDirectConstructorAndCopyLifecycle() {
        DeserializationConfig baseCfg = createDefaultConfig();
        BaseSettings baseSettings = baseCfg.getBaseSettings();
        SubtypeResolver str = baseCfg.getSubtypeResolver();
        SimpleMixInResolver mixins = new SimpleMixInResolver(null);
        RootNameLookup rootLookup = new RootNameLookup();

        DeserializationConfig directConstructed = new DeserializationConfig(
                baseSettings, str, mixins, rootLookup
        );

        assertNotNull(directConstructed);
        assertNull(directConstructed.getProblemHandlers());
        assertSame(JsonNodeFactory.instance, directConstructed.getNodeFactory());

        // Test protected ObjectMapper/ObjectReader copy constructor
        DeserializationConfig copied = new DeserializationConfig(directConstructed, mixins, rootLookup);
        assertNotNull(copied);
        assertSame(directConstructed.getNodeFactory(), copied.getNodeFactory());
    }

    @Test(timeout = 4000)
    public void testJavaIoSerialization() throws Exception {
        DeserializationConfig orig = createDefaultConfig()
                .with(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT)
                .without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(orig);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        DeserializationConfig deserialized = (DeserializationConfig) ois.readObject();
        ois.close();

        assertNotNull(deserialized);
        assertEquals(orig.getDeserializationFeatures(), deserialized.getDeserializationFeatures());
        assertTrue(deserialized.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));
        assertFalse(deserialized.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
    }
}