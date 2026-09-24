package com.fasterxml.jackson.databind;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.FormatFeature;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.cfg.BaseSettings;
import com.fasterxml.jackson.databind.cfg.ContextAttributes;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.introspect.ClassIntrospector;
import com.fasterxml.jackson.databind.introspect.SimpleMixInResolver;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.jsontype.SubtypeResolver;
import com.fasterxml.jackson.databind.jsontype.impl.StdSubtypeResolver;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.RootNameLookup;
import com.fasterxml.jackson.databind.util.StdDateFormat;

/*
 * [Branch & Defect Analysis Matrix]
 * Targets: com.fasterxml.jackson.databind.SerializationConfig
 *
 * 1. Defect-Targeted Branches:
 *    - getDefaultVisibilityChecker():
 *      Targeted defect: com.fasterxml.jackson.databind.ser.TestFeatures::testVisibilityFeatures
 *      Condition: When MapperFeature.AUTO_DETECT_SETTERS or AUTO_DETECT_CREATORS is disabled,
 *      SerializationConfig.getDefaultVisibilityChecker() must properly honor the disabled state
 *      (e.g., setting setter/creator visibility to NONE). Failure to check AUTO_DETECT_SETTERS
 *      causes bean serialization introspection to detect non-getter methods (setters) as visible.
 *
 * 2. MapperFeature Variations:
 *    - with(MapperFeature...), without(MapperFeature...), with(MapperFeature, boolean)
 *    - Branch: newMapperFlags == _mapperFeatures (no-op returns this) vs modified flags
 *
 * 3. SerializationFeature Variations:
 *    - with(SerializationFeature), with(SerializationFeature, SerializationFeature...)
 *    - withFeatures(SerializationFeature...), without(SerializationFeature)
 *    - without(SerializationFeature, SerializationFeature...), withoutFeatures(SerializationFeature...)
 *    - hasSerializationFeatures(int), isEnabled(SerializationFeature)
 *
 * 4. JsonGenerator.Feature & FormatFeature Variations:
 *    - with/without/withFeatures/withoutFeatures for JsonGenerator.Feature
 *    - with/without/withFeatures/withoutFeatures for FormatFeature
 *    - initialize(JsonGenerator): branches for INDENT_OUTPUT, WRITE_BIGDECIMAL_AS_PLAIN,
 *      generatorFeaturesToChange != 0, formatWriteFeaturesToChange != 0
 *
 * 5. Base & Introspection Configuration:
 *    - with(DateFormat): null toggles WRITE_DATES_AS_TIMESTAMPS to true; non-null toggles to false
 *    - withRootName(PropertyName): null vs non-null vs same instance
 *    - useRootWrapping(): _rootName != null (empty vs non-empty) vs WRAP_ROOT_VALUE
 *    - with(SubtypeResolver), with(ContextAttributes), withView(Class), withPropertyInclusion, etc.
 *
 * 6. Object Lifecycle & Serialization Integrity:
 *    - JDK serialization round-trip verification
 *    - toString representation
 */
public class SerializationConfigGeminiTest {

    // Helper dummy class for visibility / defect testing matching Defects4J ground truth
    static class TCls {
        @JsonProperty("groupname")
        private String groupname;

        private String name;

        public void setName(String n) {
            this.name = n;
        }

        public String getGroupname() {
            return groupname;
        }
    }

    // Helper dummy FormatFeature implementation for testing FormatFeature branches
    enum DummyFormatFeature implements FormatFeature {
        FEAT_A(true),
        FEAT_B(false);

        private final boolean _defaultState;
        private final int _mask;

        DummyFormatFeature(boolean defaultState) {
            _defaultState = defaultState;
            _mask = (1 << ordinal());
        }

        @Override
        public boolean enabledByDefault() {
            return _defaultState;
        }

        @Override
        public int getMask() {
            return _mask;
        }

        @Override
        public boolean enabledIn(int flags) {
            return (flags & _mask) != 0;
        }
    }

    private SerializationConfig createBaseConfig() {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.getSerializationConfig();
    }

    /*
    /**********************************************************
    /* Partition C: Defect-Targeted Branch Zone
    /**********************************************************
     */

    /**
     * Targets Defects4J known failure: TestFeatures::testVisibilityFeatures.
     * When AUTO_DETECT_SETTERS is disabled, SerializationConfig's visibility checker
     * must reflect setter visibility NONE, preventing setter-only properties from
     * being collected during serialization introspection.
     */
    @Test(timeout = 4000)
    public void testDefectVisibilityFeaturesAutoDetectSetters() {
        SerializationConfig config = createBaseConfig();
        config = config.without(MapperFeature.AUTO_DETECT_SETTERS);

        VisibilityChecker<?> vc = config.getDefaultVisibilityChecker();
        assertFalse("Setter visibility should be disabled when AUTO_DETECT_SETTERS is false",
                vc.isSetterVisible(null) && !vc.toString().contains("setter=NONE"));

        JavaType type = config.constructType(TCls.class);
        BeanDescription desc = config.introspect(type);
        // Expecting only 1 property ("groupname"), not 2 ("name" via setName should be excluded)
        assertEquals("Should find 1 property, not 2 when AUTO_DETECT_SETTERS is disabled",
                1, desc.findProperties().size());
        assertEquals("groupname", desc.findProperties().get(0).getName());
    }

    /**
     * Targets creator visibility behavior when AUTO_DETECT_CREATORS is disabled.
     */
    @Test(timeout = 4000)
    public void testDefectVisibilityFeaturesAutoDetectCreators() {
        SerializationConfig config = createBaseConfig();
        config = config.without(MapperFeature.AUTO_DETECT_CREATORS);

        VisibilityChecker<?> vc = config.getDefaultVisibilityChecker();
        assertFalse("Creator visibility should be NONE when AUTO_DETECT_CREATORS is disabled",
                vc.isCreatorVisible(null) && !vc.toString().contains("creator=NONE"));
    }

    /*
    /**********************************************************
    /* Partition A: Core Functional Logic & State Transitions
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testMapperFeatureMutations() {
        SerializationConfig config = createBaseConfig();
        assertTrue(config.isEnabled(MapperFeature.USE_ANNOTATIONS));

        // Disable feature
        SerializationConfig cfgDisabled = config.without(MapperFeature.USE_ANNOTATIONS);
        assertNotSame(config, cfgDisabled);
        assertFalse(cfgDisabled.isEnabled(MapperFeature.USE_ANNOTATIONS));

        // Disable already disabled (branch: newMapperFlags == _mapperFeatures)
        assertSame(cfgDisabled, cfgDisabled.without(MapperFeature.USE_ANNOTATIONS));

        // Re-enable feature
        SerializationConfig cfgEnabled = cfgDisabled.with(MapperFeature.USE_ANNOTATIONS);
        assertNotSame(cfgDisabled, cfgEnabled);
        assertTrue(cfgEnabled.isEnabled(MapperFeature.USE_ANNOTATIONS));

        // Enable already enabled
        assertSame(cfgEnabled, cfgEnabled.with(MapperFeature.USE_ANNOTATIONS));

        // with(MapperFeature, boolean)
        assertSame(config, config.with(MapperFeature.USE_ANNOTATIONS, true));
        SerializationConfig toggledOff = config.with(MapperFeature.USE_ANNOTATIONS, false);
        assertNotSame(config, toggledOff);
        assertFalse(toggledOff.isEnabled(MapperFeature.USE_ANNOTATIONS));
    }

    @Test(timeout = 4000)
    public void testSerializationFeatureMutations() {
        SerializationConfig config = createBaseConfig();

        // Single feature with/without
        SerializationConfig cfg1 = config.with(SerializationFeature.INDENT_OUTPUT);
        assertTrue(cfg1.isEnabled(SerializationFeature.INDENT_OUTPUT));
        assertSame(cfg1, cfg1.with(SerializationFeature.INDENT_OUTPUT)); // no-op

        SerializationConfig cfg2 = cfg1.without(SerializationFeature.INDENT_OUTPUT);
        assertFalse(cfg2.isEnabled(SerializationFeature.INDENT_OUTPUT));
        assertSame(cfg2, cfg2.without(SerializationFeature.INDENT_OUTPUT)); // no-op

        // Multiple features varargs with(first, features...)
        SerializationConfig cfg3 = config.with(SerializationFeature.INDENT_OUTPUT,
                SerializationFeature.WRAP_ROOT_VALUE);
        assertTrue(cfg3.isEnabled(SerializationFeature.INDENT_OUTPUT));
        assertTrue(cfg3.isEnabled(SerializationFeature.WRAP_ROOT_VALUE));

        // withFeatures(features...)
        SerializationConfig cfg4 = config.withFeatures(SerializationFeature.INDENT_OUTPUT,
                SerializationFeature.WRAP_ROOT_VALUE);
        assertTrue(cfg4.isEnabled(SerializationFeature.INDENT_OUTPUT));
        assertTrue(cfg4.isEnabled(SerializationFeature.WRAP_ROOT_VALUE));

        // without(first, features...)
        SerializationConfig cfg5 = cfg3.without(SerializationFeature.INDENT_OUTPUT,
                SerializationFeature.WRAP_ROOT_VALUE);
        assertFalse(cfg5.isEnabled(SerializationFeature.INDENT_OUTPUT));
        assertFalse(cfg5.isEnabled(SerializationFeature.WRAP_ROOT_VALUE));

        // withoutFeatures(features...)
        SerializationConfig cfg6 = cfg4.withoutFeatures(SerializationFeature.INDENT_OUTPUT,
                SerializationFeature.WRAP_ROOT_VALUE);
        assertFalse(cfg6.isEnabled(SerializationFeature.INDENT_OUTPUT));
        assertFalse(cfg6.isEnabled(SerializationFeature.WRAP_ROOT_VALUE));

        // Bulk mask check
        int mask = SerializationFeature.INDENT_OUTPUT.getMask() | SerializationFeature.WRAP_ROOT_VALUE.getMask();
        assertTrue(cfg3.hasSerializationFeatures(mask));
        assertFalse(cfg5.hasSerializationFeatures(mask));
    }

    @Test(timeout = 4000)
    public void testJsonGeneratorFeatures() {
        SerializationConfig config = createBaseConfig();
        JsonFactory jf = new JsonFactory();

        // Initially not modified, factory state is reflected
        assertFalse(config.isEnabled(JsonGenerator.Feature.ESCAPE_NON_ASCII, jf));

        SerializationConfig cfgEnabled = config.with(JsonGenerator.Feature.ESCAPE_NON_ASCII);
        assertNotSame(config, cfgEnabled);
        assertTrue(cfgEnabled.isEnabled(JsonGenerator.Feature.ESCAPE_NON_ASCII, jf));
        assertSame(cfgEnabled, cfgEnabled.with(JsonGenerator.Feature.ESCAPE_NON_ASCII)); // no-op branch

        SerializationConfig cfgDisabled = cfgEnabled.without(JsonGenerator.Feature.ESCAPE_NON_ASCII);
        assertNotSame(cfgEnabled, cfgDisabled);
        assertFalse(cfgDisabled.isEnabled(JsonGenerator.Feature.ESCAPE_NON_ASCII, jf));
        assertSame(cfgDisabled, cfgDisabled.without(JsonGenerator.Feature.ESCAPE_NON_ASCII)); // no-op branch

        // Bulk withFeatures & withoutFeatures
        SerializationConfig bulk = config.withFeatures(JsonGenerator.Feature.AUTO_CLOSE_TARGET,
                JsonGenerator.Feature.QUOTE_FIELD_NAMES);
        assertTrue(bulk.isEnabled(JsonGenerator.Feature.AUTO_CLOSE_TARGET, jf));
        assertTrue(bulk.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES, jf));
        assertSame(bulk, bulk.withFeatures(JsonGenerator.Feature.AUTO_CLOSE_TARGET));

        SerializationConfig unbulk = bulk.withoutFeatures(JsonGenerator.Feature.AUTO_CLOSE_TARGET,
                JsonGenerator.Feature.QUOTE_FIELD_NAMES);
        assertFalse(unbulk.isEnabled(JsonGenerator.Feature.AUTO_CLOSE_TARGET, jf));
        assertSame(unbulk, unbulk.withoutFeatures(JsonGenerator.Feature.AUTO_CLOSE_TARGET));
    }

    @Test(timeout = 4000)
    public void testFormatFeatures() {
        SerializationConfig config = createBaseConfig();

        SerializationConfig cfg1 = config.with(DummyFormatFeature.FEAT_A);
        assertNotSame(config, cfg1);
        assertSame(cfg1, cfg1.with(DummyFormatFeature.FEAT_A)); // no-op branch

        SerializationConfig cfg2 = cfg1.without(DummyFormatFeature.FEAT_A);
        assertNotSame(cfg1, cfg2);
        assertSame(cfg2, cfg2.without(DummyFormatFeature.FEAT_A)); // no-op branch

        SerializationConfig bulk = config.withFeatures(DummyFormatFeature.FEAT_A, DummyFormatFeature.FEAT_B);
        assertNotSame(config, bulk);
        assertSame(bulk, bulk.withFeatures(DummyFormatFeature.FEAT_A));

        SerializationConfig unbulk = bulk.withoutFeatures(DummyFormatFeature.FEAT_A, DummyFormatFeature.FEAT_B);
        assertNotSame(bulk, unbulk);
        assertSame(unbulk, unbulk.withoutFeatures(DummyFormatFeature.FEAT_A));
    }

    @Test(timeout = 4000)
    public void testDateFormatBranching() {
        SerializationConfig config = createBaseConfig();
        // Base config by default has WRITE_DATES_AS_TIMESTAMPS enabled
        assertTrue(config.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));

        DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        SerializationConfig withDf = config.with(df);
        // Setting non-null date format must disable WRITE_DATES_AS_TIMESTAMPS
        assertFalse(withDf.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
        assertEquals(df, withDf.getDateFormat());

        // Setting null date format must re-enable WRITE_DATES_AS_TIMESTAMPS
        SerializationConfig nullDf = withDf.with((DateFormat) null);
        assertTrue(nullDf.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
    }

    @Test(timeout = 4000)
    public void testRootNameAndWrapping() {
        SerializationConfig config = createBaseConfig();
        assertFalse(config.useRootWrapping());

        // null with null returns this
        assertSame(config, config.withRootName((PropertyName) null));

        // Same rootName returns this
        PropertyName name1 = PropertyName.construct("root");
        SerializationConfig withRoot = config.withRootName(name1);
        assertNotSame(config, withRoot);
        assertSame(withRoot, withRoot.withRootName(name1));

        // Non-empty root name enables root wrapping
        assertTrue(withRoot.useRootWrapping());

        // Empty root name disables root wrapping
        SerializationConfig emptyRoot = config.withRootName(PropertyName.construct(""));
        assertFalse(emptyRoot.useRootWrapping());

        // Null root name falls back to WRAP_ROOT_VALUE feature
        SerializationConfig wrapFeature = config.with(SerializationFeature.WRAP_ROOT_VALUE);
        assertTrue(wrapFeature.useRootWrapping());
    }

    @Test(timeout = 4000)
    public void testGeneratorInitialization() throws Exception {
        SerializationConfig config = createBaseConfig()
                .with(SerializationFeature.INDENT_OUTPUT)
                .with(SerializationFeature.WRITE_BIGDECIMAL_AS_PLAIN)
                .with(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS)
                .with(DummyFormatFeature.FEAT_A);

        JsonFactory factory = new JsonFactory();
        StringWriter sw = new StringWriter();
        JsonGenerator generator = factory.createGenerator(sw);

        assertNull(generator.getPrettyPrinter());
        config.initialize(generator);

        assertNotNull(generator.getPrettyPrinter());
        assertTrue(generator.isEnabled(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN));
        assertTrue(generator.isEnabled(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS));
        generator.close();
    }

    @Test(timeout = 4000)
    public void testGeneratorInitializationWithExplicitPrettyPrinter() throws Exception {
        SerializationConfig config = createBaseConfig().with(SerializationFeature.INDENT_OUTPUT);
        JsonFactory factory = new JsonFactory();
        StringWriter sw = new StringWriter();
        JsonGenerator generator = factory.createGenerator(sw);

        PrettyPrinter customPp = new MinimalPrettyPrinter();
        generator.setPrettyPrinter(customPp);

        // initialize should not overwrite explicit pretty printer
        config.initialize(generator);
        assertSame(customPp, generator.getPrettyPrinter());
        generator.close();
    }

    @Test(timeout = 4000)
    public void testConstructDefaultPrettyPrinter() {
        SerializationConfig config = createBaseConfig();
        PrettyPrinter pp1 = config.constructDefaultPrettyPrinter();
        assertNotNull(pp1);

        PrettyPrinter customPp = new MinimalPrettyPrinter();
        SerializationConfig customConfig = config.withDefaultPrettyPrinter(customPp);
        assertSame(customPp, customConfig.getDefaultPrettyPrinter());
        assertSame(customConfig, customConfig.withDefaultPrettyPrinter(customPp)); // same instance
        assertSame(customPp, customConfig.constructDefaultPrettyPrinter());
    }

    @Test(timeout = 4000)
    public void testPropertyInclusion() {
        SerializationConfig config = createBaseConfig();
        assertEquals(JsonInclude.Include.ALWAYS, config.getSerializationInclusion());

        JsonInclude.Value inclVal = JsonInclude.Value.construct(JsonInclude.Include.NON_NULL, JsonInclude.Include.NON_EMPTY);
        SerializationConfig withIncl = config.withPropertyInclusion(inclVal);
        assertNotSame(config, withIncl);
        assertSame(withIncl, withIncl.withPropertyInclusion(inclVal)); // equality branch
        assertEquals(JsonInclude.Include.NON_NULL, withIncl.getSerializationInclusion());
        assertEquals(inclVal, withIncl.getDefaultPropertyInclusion());
        assertEquals(inclVal, withIncl.getDefaultPropertyInclusion(String.class));

        // Deprecated helper
        SerializationConfig depIncl = config.withSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
        assertEquals(JsonInclude.Include.NON_DEFAULT, depIncl.getSerializationInclusion());

        // Default property format check
        assertEquals(JsonFormat.Value.empty(), config.getDefaultPropertyFormat(String.class));
    }

    /*
    /**********************************************************
    /* Partition B: Boundary Value Analysis (BVA) & Extremes
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testFluentBaseModifiersIdentity() {
        SerializationConfig config = createBaseConfig();

        // SubtypeResolver
        SubtypeResolver str = config.getSubtypeResolver();
        assertSame(config, config.with(str));
        SubtypeResolver newStr = new StdSubtypeResolver();
        assertNotSame(config, config.with(newStr));

        // ContextAttributes
        ContextAttributes attrs = config.getAttributes();
        assertSame(config, config.with(attrs));
        ContextAttributes newAttrs = ContextAttributes.getEmpty().withSharedAttribute("k", "v");
        assertNotSame(config, config.with(newAttrs));

        // View
        assertSame(config, config.withView((Class<?>) null));
        SerializationConfig withView = config.withView(String.class);
        assertNotSame(config, withView);
        assertSame(withView, withView.withView(String.class));

        // FilterProvider
        assertSame(config, config.withFilters((FilterProvider) null));
        FilterProvider fp = new SimpleFilterProvider();
        SerializationConfig withFp = config.withFilters(fp);
        assertNotSame(config, withFp);
        assertSame(fp, withFp.getFilterProvider());
        assertSame(withFp, withFp.withFilters(fp));

        // BaseSettings delegation methods
        assertSame(config, config.with(config.getAnnotationIntrospector()));
        assertSame(config, config.with(config.getClassIntrospector()));
        assertSame(config, config.with((HandlerInstantiator) null));
        assertSame(config, config.with(config.getPropertyNamingStrategy()));
        assertSame(config, config.with(config.getTypeFactory()));
        assertSame(config, config.with(config.getDefaultVisibilityChecker()));
        assertSame(config, config.with(config.getLocale()));
        assertSame(config, config.with(config.getTimeZone()));
        assertSame(config, config.with(config.getBase64Variant()));

        // Check non-identity changes
        assertNotSame(config, config.with(Locale.FRANCE));
        assertNotSame(config, config.with(TimeZone.getTimeZone("GMT")));
        assertNotSame(config, config.with(Base64Variants.MIME_NO_LINEFEEDS));
        assertNotSame(config, config.with(TypeFactory.defaultInstance().withModifier(null)));
        assertNotSame(config, config.withVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC));
        assertNotSame(config, config.withAppendedAnnotationIntrospector(AnnotationIntrospector.nopInstance()));
        assertNotSame(config, config.withInsertedAnnotationIntrospector(AnnotationIntrospector.nopInstance()));
    }

    @Test(timeout = 4000)
    public void testAnnotationIntrospectorDisabled() {
        SerializationConfig config = createBaseConfig();
        assertNotNull(config.getAnnotationIntrospector());
        assertNotEquals(AnnotationIntrospector.nopInstance(), config.getAnnotationIntrospector());

        SerializationConfig noAnnConfig = config.without(MapperFeature.USE_ANNOTATIONS);
        assertSame(AnnotationIntrospector.nopInstance(), noAnnConfig.getAnnotationIntrospector());
    }

    @Test(timeout = 4000)
    public void testVisibilityCheckerDisablingBranches() {
        SerializationConfig config = createBaseConfig()
                .without(MapperFeature.AUTO_DETECT_GETTERS)
                .without(MapperFeature.AUTO_DETECT_IS_GETTERS)
                .without(MapperFeature.AUTO_DETECT_FIELDS);

        VisibilityChecker<?> vc = config.getDefaultVisibilityChecker();
        assertFalse(vc.isGetterVisible(null));
        assertFalse(vc.isIsGetterVisible(null));
        assertFalse(vc.isFieldVisible(null));
    }

    /*
    /**********************************************************
    /* Partition D: Introspection Methods & Type Descriptions
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testIntrospectMethods() {
        SerializationConfig config = createBaseConfig();
        JavaType type = config.constructType(TCls.class);

        BeanDescription fullDesc = config.introspect(type);
        assertNotNull(fullDesc);
        assertEquals(TCls.class, fullDesc.getBeanClass());

        BeanDescription classDesc = config.introspectClassAnnotations(type);
        assertNotNull(classDesc);
        assertEquals(TCls.class, classDesc.getBeanClass());

        BeanDescription directDesc = config.introspectDirectClassAnnotations(type);
        assertNotNull(directDesc);
        assertEquals(TCls.class, directDesc.getBeanClass());
    }

    /*
    /**********************************************************
    /* Partition E: Object Lifecycle & Contract Integrity
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testToStringAndSerialization() throws Exception {
        SerializationConfig config = createBaseConfig();
        String str = config.toString();
        assertNotNull(str);
        assertTrue(str.startsWith("[SerializationConfig: flags=0x"));

        // JDK Serialization round-trip
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(config);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object deserialized = ois.readObject();
        ois.close();

        assertTrue(deserialized instanceof SerializationConfig);
        SerializationConfig roundTrip = (SerializationConfig) deserialized;
        assertEquals(config.getSerializationFeatures(), roundTrip.getSerializationFeatures());
        assertEquals(config.useRootWrapping(), roundTrip.useRootWrapping());
    }
}