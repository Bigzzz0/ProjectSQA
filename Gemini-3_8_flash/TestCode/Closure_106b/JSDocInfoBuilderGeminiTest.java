package com.google.javascript.rhino;

import com.google.javascript.rhino.JSDocInfo.Visibility;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 *
 * Target Class: com.google.javascript.rhino.JSDocInfoBuilder
 *
 * Branch & Condition Coverage Points:
 * 1. Constructor: parseDocumentation (true vs false).
 * 2. isPopulated(): state transition across recordings, build(), and reset.
 * 3. isPopulatedWithFileOverview(): isPopulated() && currentInfo.hasFileOverview().
 * 4. isDescriptionRecorded(): currentInfo.getDescription() != null.
 * 5. build(sourceName): populated=true (defaults applied, sourceName set, reset) vs populated=false (returns null).
 * 6. populateDefaults(built): visibility is null (defaults to INHERITED) vs visibility already set.
 * 7. Marker subsystem:
 *    - markAnnotation: marker non-null (parseDocumentation=true) vs null (parseDocumentation=false).
 *    - markText, markTypeNode, markName: currentMarker non-null vs currentMarker null.
 * 8. recordBlockDescription: parseDocumentation=true (populated=true) vs false; duplicate description handling.
 * 9. recordVisibility: visibility==null (succeeds) vs visibility!=null (collision, fails).
 * 10. recordParameter & recordParameterDescription: hasAnySingletonTypeTags() guard, duplicate param name.
 * 11. Type exclusivity rules (hasAnyTypeRelatedTags vs hasAnySingletonTypeTags):
 *     - recordType, recordTypedef, recordEnumParameterType: blocked if hasAnyTypeRelatedTags().
 *     - recordReturnType, recordThisType, recordBaseType, recordConstructor, recordInterface:
 *       blocked if hasAnySingletonTypeTags() or respective collision.
 * 12. recordDefineType: type!=null && !isConstant() && !isDefine() && recordType(type).
 * 13. Boolean flags: constancy, hiddenness, noTypeCheck, preserveTry, override, noAlias,
 *     deprecated, export, noShadow, implicitCast, noSideEffects (each true on first call, false on duplicate).
 * 14. Defects4J Alignment: Closure-106 (CheckSideEffectsTest::testJSDocComments, CollapsePropertiesTest)
 *     targeting block descriptions under documentation parsing modes, fileOverview status, noSideEffects,
 *     and constant/define interactions preventing compiler crashes.
 */
public class JSDocInfoBuilderGeminiTest {

  private JSTypeExpression createTypeExpression(String typeName) {
    return new JSTypeExpression(new Node(Token.NAME), "testSource");
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testInitialStateAndEmptyBuild() {
    JSDocInfoBuilder builder = new JSDocInfoBuilder(true);
    assertFalse("Initial builder must not be populated", builder.isPopulated());
    assertFalse("Initial builder must not have file overview", builder.isPopulatedWithFileOverview());
    assertFalse("Initial builder must not have recorded description", builder.isDescriptionRecorded());
    assertFalse("Initial builder must not have constructor recorded", builder.isConstructorRecorded());
    assertFalse("Initial builder must not have interface recorded", builder.isInterfaceRecorded());
    assertNull("Building an unpopulated builder must return null", builder.build("source.js"));
  }

  @Test(timeout = 4000)
  public void testBuildLifecycleAndReusability() {
    JSDocInfoBuilder builder = new JSDocInfoBuilder(true);
    assertTrue(builder.recordDeprecated());
    assertTrue(builder.isPopulated());

    JSDocInfo info1 = builder.build("file1.js");
    assertNotNull(info1);
    assertTrue(info1.isDeprecated());
    assertEquals("file1.js", info1.getSourceName());
    assertEquals(Visibility.INHERITED, info1.getVisibility());

    // Post-build state: builder should be reset and unpopulated
    assertFalse(builder.isPopulated());
    assertNull("Subsequent build without new properties should return null", builder.build("file1.js"));

    // Builder reuse
    assertTrue(builder.recordNoSideEffects());
    assertTrue(builder.isPopulated());
    JSDocInfo info2 = builder.build("file2.js");
    assertNotNull(info2);
    assertTrue(info2.isNoSideEffects());
    assertFalse(info2.isDeprecated());
    assertEquals("file2.js", info2.getSourceName());
  }

  @Test(timeout = 4000)
  public void testVisibilityExplicitVsInheritedDefault() {
    JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
    assertTrue(builder.recordVisibility(Visibility.PRIVATE));
    // Duplicate recording must fail
    assertFalse(builder.recordVisibility(Visibility.PUBLIC));

    JSDocInfo info = builder.build("vis.js");
    assertNotNull(info);
    assertEquals(Visibility.PRIVATE, info.getVisibility());

    // Without explicit visibility, default populateDefaults() sets INHERITED
    builder.recordDeprecated();
    JSDocInfo defaultInfo = builder.build("default.js");
    assertNotNull(defaultInfo);
    assertEquals(Visibility.INHERITED, defaultInfo.getVisibility());
  }

  @Test(timeout = 4000)
  public void testMarkersWhenDocumentationParsingEnabled() {
    JSDocInfoBuilder builder = new JSDocInfoBuilder(true);
    builder.recordDeprecated(); // Populate builder

    builder.markAnnotation("param", 10, 5);
    builder.markText("textBlock", 10, 11, 10, 20);
    Node typeNode = new Node(Token.NAME);
    builder.markTypeNode(typeNode, 10, 21, 27, true);
    builder.markName("arg0", 10, 28);

    JSDocInfo info = builder.build("markers.js");
    assertNotNull(info);
    Collection<JSDocInfo.Marker> markers = info.getMarkers();
    assertNotNull(markers);
    assertEquals(1, markers.size());

    JSDocInfo.Marker marker = markers.iterator().next();
    assertNotNull(marker.annotation);
    assertEquals("param", marker.annotation.getItem());
    assertEquals(10, marker.annotation.getStartLine());
    assertEquals(5, marker.annotation.getStartChar());
    assertEquals(10, marker.annotation.getEndLine());
    assertEquals(10, marker.annotation.getEndChar());

    assertNotNull(marker.description);
    assertEquals("textBlock", marker.description.getItem());
    assertEquals(10, marker.description.getStartLine());
    assertEquals(11, marker.description.getStartChar());
    assertEquals(10, marker.description.getEndLine());
    assertEquals(20, marker.description.getEndChar());

    assertNotNull(marker.type);
    assertEquals(typeNode, marker.type.getItem());
    assertTrue(marker.type.hasBrackets);
    assertEquals(10, marker.type.getStartLine());
    assertEquals(21, marker.type.getStartChar());
    assertEquals(10, marker.type.getEndLine());
    assertEquals(27, marker.type.getEndChar());

    assertNotNull(marker.name);
    assertEquals("arg0", marker.name.getItem());
    assertEquals(10, marker.name.getStartLine());
    assertEquals(28, marker.name.getStartChar());
    assertEquals(10, marker.name.getEndLine());
    assertEquals(32, marker.name.getEndChar());
  }

  @Test(timeout = 4000)
  public void testMarkersWhenDocumentationParsingDisabled() {
    JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
    builder.recordDeprecated();

    // With parseDocumentation=false, addMarker() returns null, currentMarker remains null
    builder.markAnnotation("param", 1, 0);
    builder.markText("text", 1, 6, 1, 10);
    builder.markTypeNode(new Node(Token.NAME), 1, 11, 15, false);
    builder.markName("arg", 1, 16);

    JSDocInfo info = builder.build("noMarkers.js");
    assertNotNull(info);
    assertTrue(info.getMarkers() == null || info.getMarkers().isEmpty());
  }

  @Test(timeout = 4000)
  public void testMarkWithoutActiveAnnotationDoesNotThrow() {
    JSDocInfoBuilder builder = new JSDocInfoBuilder(true);
    // Directly calling markText, markTypeNode, markName when currentMarker is null
    builder.markText("orphan text", 0, 0, 0, 5);
    builder.markTypeNode(new Node(Token.NAME), 0, 0, 5, false);
    builder.markName("orphan name", 0, 0);

    builder.recordDeprecated();
    JSDocInfo info = builder.build("orphan.js");
    assertNotNull(info);
    assertTrue(info.getMarkers() == null || info.getMarkers().isEmpty());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testRecordTypeWithNullAndEmptyBoundaries() {
    JSDocInfoBuilder builder = new JSDocInfoBuilder(true);
    assertFalse("recordType(null) should return false", builder.recordType(null));
    assertFalse(builder.isPopulated());

    assertFalse("recordTypedef(null) should return false", builder.recordTypedef(null));
    assertFalse(builder.isPopulated());

    assertFalse("recordReturnType(null) should return false", builder.recordReturnType(null));
    assertFalse(builder.isPopulated());

    assertFalse("recordEnumParameterType(null) should return false", builder.recordEnumParameterType(null));
    assertFalse(builder.isPopulated());

    assertFalse("recordThisType(null) should return false", builder.recordThisType(null));
    assertFalse(builder.isPopulated());

    assertFalse("recordBaseType(null) should return false", builder.recordBaseType(null));
    assertFalse(builder.isPopulated());

    assertFalse("recordDescription(null) should return false", builder.recordDescription(null));
    assertFalse(builder.isPopulated());
  }

  @Test(timeout = 4000)
  public void testEmptyStringsAndNullSourceName() {
    JSDocInfoBuilder builder = new JSDocInfoBuilder(true);
    assertTrue(builder.recordDescription(""));
    assertTrue(builder.isDescriptionRecorded());
    assertTrue(builder.recordVersion(""));
    assertTrue(builder.addAuthor(""));
    assertTrue(builder.addReference(""));
    assertTrue(builder.recordDeprecationReason(""));
    assertTrue(builder.recordReturnDescription(""));

    JSDocInfo info = builder.build(null);
    assertNotNull(info);
    assertNull(info.getSourceName());
    assertEquals("", info.getDescription());
    assertEquals("", info.getVersion());
    assertEquals("", info.getDeprecationReason());
    assertEquals("", info.getReturnDescription());
  }

  @Test(timeout = 4000)
  public void testDuplicateRecordingGuardConditions() {
    JSDocInfoBuilder builder = new JSDocInfoBuilder(true);

    assertTrue(builder.recordConstancy());
    assertFalse("Duplicate constancy must return false", builder.recordConstancy());

    assertTrue(builder.recordHiddenness());
    assertFalse("Duplicate hiddenness must return false", builder.recordHiddenness());

    assertTrue(builder.recordNoTypeCheck());
    assertFalse("Duplicate noTypeCheck must return false", builder.recordNoTypeCheck());

    assertTrue(builder.recordPreserveTry());
    assertFalse("Duplicate preserveTry must return false", builder.recordPreserveTry());

    assertTrue(builder.recordOverride());
    assertFalse("Duplicate override must return false", builder.recordOverride());

    assertTrue(builder.recordNoAlias());
    assertFalse("Duplicate noAlias must return false", builder.recordNoAlias());

    assertTrue(builder.recordDeprecated());
    assertFalse("Duplicate deprecated must return false", builder.recordDeprecated());

    assertTrue(builder.recordExport());
    assertFalse("Duplicate export must return false", builder.recordExport());

    assertTrue(builder.recordNoShadow());
    assertFalse("Duplicate noShadow must return false", builder.recordNoShadow());

    assertTrue(builder.recordImplicitCast());
    assertFalse("Duplicate implicitCast must return false", builder.recordImplicitCast());

    assertTrue(builder.recordNoSideEffects());
    assertFalse("Duplicate noSideEffects must return false", builder.recordNoSideEffects());

    assertTrue(builder.recordDescription("desc"));
    assertFalse("Duplicate description must return false", builder.recordDescription("desc2"));

    assertTrue(builder.recordFileOverview("overview"));
    assertFalse("Duplicate fileOverview must return false", builder.recordFileOverview("overview2"));
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Closure-106 Alignments)
  // =========================================================================

  @Test(timeout = 4000)
  public void testDefectCheckSideEffectsBlockDescriptionPopulation() {
    // Targets Defects4J CheckSideEffectsTest::testJSDocComments failure mode
    // When documentation parsing is enabled, recordBlockDescription must populate the builder
    JSDocInfoBuilder builderWithDocs = new JSDocInfoBuilder(true);
    assertTrue(builderWithDocs.recordBlockDescription("Documentation block"));
    assertTrue("Builder must be populated when doc parsing is enabled", builderWithDocs.isPopulated());
    JSDocInfo infoWithDocs = builderWithDocs.build("doc.js");
    assertNotNull("JSDocInfo must be generated when populated by block description", infoWithDocs);
    assertEquals("Documentation block", infoWithDocs.getBlockDescription());

    // When documentation parsing is disabled, documentBlock returns false
    JSDocInfoBuilder builderNoDocs = new JSDocInfoBuilder(false);
    boolean recorded = builderNoDocs.recordBlockDescription("Ignored doc block");
    assertFalse("recordBlockDescription must return false when parseDocumentation=false", recorded);
    assertFalse("Builder must not be populated when parseDocumentation=false", builderNoDocs.isPopulated());
    assertNull("Building without valuable info must return null", builderNoDocs.build("nodoc.js"));
  }

  @Test(timeout = 4000)
  public void testDefectCheckSideEffectsFileOverviewAndNoSideEffectsIntegrity() {
    // Tests interactions vital to CheckSideEffects pass preventing JSC_USELESS_CODE
    JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
    assertFalse(builder.isPopulatedWithFileOverview());

    assertTrue(builder.recordFileOverview("File header comment"));
    assertTrue("Must be populated with file overview", builder.isPopulatedWithFileOverview());
    assertTrue(builder.recordNoSideEffects());

    JSDocInfo info = builder.build("file.js");
    assertNotNull(info);
    assertTrue(info.hasFileOverview());
    assertEquals("File header comment", info.getFileOverview());
    assertTrue(info.isNoSideEffects());

    // Post-build check
    assertFalse(builder.isPopulatedWithFileOverview());
  }

  @Test(timeout = 4000)
  public void testDefectCollapsePropertiesConstancyAndDefineInteraction() {
    // Targets CollapseProperties crashes when constant and define tags interact
    JSDocInfoBuilder builder1 = new JSDocInfoBuilder(true);
    assertTrue(builder1.recordConstancy());
    JSTypeExpression type = createTypeExpression("number");
    assertFalse("recordDefineType must fail if constancy is already set", builder1.recordDefineType(type));

    JSDocInfoBuilder builder2 = new JSDocInfoBuilder(true);
    assertTrue(builder2.recordDefineType(type));
    assertTrue(builder2.isPopulated());
    // Once define is recorded, constancy recording must be rejected to prevent corrupt state
    assertFalse("recordDefineType cannot be called again once isDefine is set", builder2.recordDefineType(type));
    assertFalse("recordConstancy must fail if define is already set", builder2.recordConstancy());

    JSDocInfo info = builder2.build("define.js");
    assertNotNull(info);
    assertTrue(info.isDefine());
    assertFalse(info.isConstant());
  }

  // =========================================================================
  // Partition D: Mutual Exclusivity & Type Dependency Trees
  // =========================================================================

  @Test(timeout = 4000)
  public void testSingletonTypeTagExclusivity() {
    JSTypeExpression type = createTypeExpression("string");

    // Case 1: @type blocks other singleton tags & constructor/interface
    JSDocInfoBuilder builder1 = new JSDocInfoBuilder(true);
    assertTrue(builder1.recordType(type));
    assertFalse(builder1.recordTypedef(type));
    assertFalse(builder1.recordEnumParameterType(type));
    assertFalse(builder1.recordConstructor());
    assertFalse(builder1.recordInterface());
    assertFalse(builder1.recordParameter("p1", type));
    assertFalse(builder1.recordReturnType(type));
    assertFalse(builder1.recordThisType(type));
    assertFalse(builder1.recordBaseType(type));
    assertFalse(builder1.recordThrowType(type));

    // Case 2: @typedef blocks other type tags
    JSDocInfoBuilder builder2 = new JSDocInfoBuilder(true);
    assertTrue(builder2.recordTypedef(type));
    assertFalse(builder2.recordType(type));
    assertFalse(builder2.recordConstructor());
    assertFalse(builder2.recordReturnType(type));

    // Case 3: @enum blocks other type tags
    JSDocInfoBuilder builder3 = new JSDocInfoBuilder(true);
    assertTrue(builder3.recordEnumParameterType(type));
    assertFalse(builder3.recordType(type));
    assertFalse(builder3.recordConstructor());
    assertFalse(builder3.recordParameter("p", type));
  }

  @Test(timeout = 4000)
  public void testTypeRelatedTagsBlockRecordType() {
    JSTypeExpression type = createTypeExpression("Object");

    // Branch: constructor blocks recordType
    JSDocInfoBuilder b1 = new JSDocInfoBuilder(true);
    assertTrue(b1.recordConstructor());
    assertTrue(b1.isConstructorRecorded());
    assertFalse(b1.recordType(type));
    assertFalse(b1.recordTypedef(type));
    assertFalse(b1.recordEnumParameterType(type));

    // Branch: interface blocks recordType and recordConstructor
    JSDocInfoBuilder b2 = new JSDocInfoBuilder(true);
    assertTrue(b2.recordInterface());
    assertTrue(b2.isInterfaceRecorded());
    assertFalse(b2.recordConstructor());
    assertFalse(b2.recordType(type));

    // Branch: parameters block recordType
    JSDocInfoBuilder b3 = new JSDocInfoBuilder(true);
    assertTrue(b3.recordParameter("x", type));
    assertFalse(b3.recordType(type));

    // Branch: returnType blocks recordType
    JSDocInfoBuilder b4 = new JSDocInfoBuilder(true);
    assertTrue(b4.recordReturnType(type));
    assertFalse(b4.recordType(type));

    // Branch: baseType blocks recordType
    JSDocInfoBuilder b5 = new JSDocInfoBuilder(true);
    assertTrue(b5.recordBaseType(type));
    assertFalse(b5.recordType(type));

    // Branch: thisType blocks recordType
    JSDocInfoBuilder b6 = new JSDocInfoBuilder(true);
    assertTrue(b6.recordThisType(type));
    assertFalse(b6.recordType(type));
  }

  @Test(timeout = 4000)
  public void testDuplicateComplexTypes() {
    JSTypeExpression type1 = createTypeExpression("TypeA");
    JSTypeExpression type2 = createTypeExpression("TypeB");

    JSDocInfoBuilder builder = new JSDocInfoBuilder(true);
    assertTrue(builder.recordReturnType(type1));
    assertFalse("Duplicate return type must fail", builder.recordReturnType(type2));

    assertTrue(builder.recordThisType(type1));
    assertFalse("Duplicate this type must fail", builder.recordThisType(type2));

    assertTrue(builder.recordBaseType(type1));
    assertFalse("Duplicate base type must fail", builder.recordBaseType(type2));
  }

  // =========================================================================
  // Partition E: Parameter, Throws, Collections & Integrated Validation
  // =========================================================================

  @Test(timeout = 4000)
  public void testParameterRecordingAndDescription() {
    JSDocInfoBuilder builder = new JSDocInfoBuilder(true);
    JSTypeExpression type = createTypeExpression("string");

    assertFalse(builder.hasParameter("paramA"));
    assertTrue(builder.recordParameter("paramA", type));
    assertTrue(builder.hasParameter("paramA"));
    assertFalse("Duplicate parameter name must fail", builder.recordParameter("paramA", type));

    assertTrue(builder.recordParameterDescription("paramA", "Description of A"));
    assertFalse("Duplicate parameter description must fail",
        builder.recordParameterDescription("paramA", "Description of A again"));

    JSDocInfo info = builder.build("params.js");
    assertNotNull(info);
    assertEquals(1, info.getParameterCount());
    assertTrue(info.hasParameter("paramA"));
    assertEquals("Description of A", info.getParameterDescription("paramA"));
  }

  @Test(timeout = 4000)
  public void testThrowsRecordingAndDescription() {
    JSDocInfoBuilder builder = new JSDocInfoBuilder(true);
    JSTypeExpression errType = createTypeExpression("Error");

    assertTrue(builder.recordThrowType(errType));
    assertTrue(builder.recordThrowDescription(errType, "Throws on failure"));
    assertFalse("Duplicate throw description for same type must fail",
        builder.recordThrowDescription(errType, "Duplicate description"));

    JSDocInfo info = builder.build("throws.js");
    assertNotNull(info);
    assertEquals(1, info.getThrows().size());
    assertEquals("Throws on failure", info.getThrowsDescriptionByType(errType));
  }

  @Test(timeout = 4000)
  public void testSuppressionsTemplateTypesAndImplementedInterfaces() {
    JSDocInfoBuilder builder = new JSDocInfoBuilder(true);

    Set<String> suppressions = new HashSet<String>();
    suppressions.add("checkTypes");
    suppressions.add("extraRequire");
    assertTrue(builder.recordSuppressions(suppressions));
    assertFalse("Duplicate suppressions must fail", builder.recordSuppressions(suppressions));

    assertTrue(builder.recordTemplateTypeName("T"));
    assertFalse("Duplicate template type must fail", builder.recordTemplateTypeName("T"));

    JSTypeExpression iface = createTypeExpression("Disposable");
    assertTrue(builder.recordImplementedInterface(iface));

    JSDocInfo info = builder.build("misc.js");
    assertNotNull(info);
    assertEquals(suppressions, info.getSuppressions());
    assertEquals(1, info.getTemplateTypeNames().size());
    assertTrue(info.getTemplateTypeNames().contains("T"));
    assertEquals(1, info.getImplementedInterfaces().size());
  }

  @Test(timeout = 4000)
  public void testAuthorsReferencesAndVersion() {
    JSDocInfoBuilder builder = new JSDocInfoBuilder(true);

    assertTrue(builder.addAuthor("Alice"));
    assertTrue(builder.addAuthor("Bob"));

    assertTrue(builder.addReference("http://example.com/ref1"));
    assertTrue(builder.addReference("http://example.com/ref2"));

    assertTrue(builder.recordVersion("1.0.0"));
    assertFalse("Duplicate version must fail", builder.recordVersion("2.0.0"));

    JSDocInfo info = builder.build("meta.js");
    assertNotNull(info);
    assertEquals(2, info.getAuthors().size());
    assertTrue(info.getAuthors().contains("Alice"));
    assertTrue(info.getAuthors().contains("Bob"));

    assertEquals(2, info.getReferences().size());
    assertTrue(info.getReferences().contains("http://example.com/ref1"));

    assertEquals("1.0.0", info.getVersion());
  }
}