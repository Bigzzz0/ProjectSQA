package com.google.javascript.rhino;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.JSDocInfo.Visibility;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.JSTypeExpression;
import com.google.javascript.rhino.Node;
import java.util.HashSet;
import java.util.Set;

/*
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: JSDocInfoBuilder - builder for JSDocInfo objects with state management
 * 
 * Key branches targeted:
 * 1. Constructor: parseDocumentation true/false initialization
 * 2. isPopulated() - initial false, becomes true after any record operation
 * 3. isPopulatedWithFileOverview() - requires populated && hasFileOverview
 * 4. isDescriptionRecorded() - checks description != null
 * 5. build() - populated vs non-populated path, resets state
 * 6. populateDefaults() - visibility null -> INHERITED
 * 7. markAnnotation/markText/markTypeNode/markName - currentMarker null vs non-null
 * 8. recordBlockDescription - parseDocumentation flag affects populated
 * 9. recordVisibility - first vs subsequent calls
 * 10. recordParameter - hasAnySingletonTypeTags guard
 * 11. recordParameterDescription - documentParam success/failure
 * 12. recordTemplateTypeName - declareTemplateTypeName success/failure
 * 13. recordThrowType - hasAnySingletonTypeTags guard
 * 14. recordThrowDescription - documentThrows success/failure
 * 15. addAuthor/addReference/recordVersion - document* success/failure
 * 16. recordDeprecationReason - setDeprecationReason success/failure
 * 17. recordSuppressions - setSuppressions success/failure
 * 18. recordType/recordTypedef - null check + hasAnyTypeRelatedTags
 * 19. recordReturnType - null check + getReturnType null + singleton guard
 * 20. recordReturnDescription - documentReturn success/failure
 * 21. recordDefineType - type != null && !isConstant && !isDefine && recordType
 * 22. recordEnumParameterType - null check + hasAnyTypeRelatedTags
 * 23. recordThisType - null check + singleton guard + hasThisType
 * 24. recordBaseType - null check + singleton guard + hasBaseType
 * 25. recordConstancy/recordHiddenness/recordNoTypeCheck - flag already set
 * 26. recordConstructor/recordInterface - singleton guard + mutual exclusion
 * 27. recordPreserveTry/recordOverride/recordNoAlias/recordDeprecated - flag already set
 * 28. recordExport/recordNoShadow/recordImplicitCast/recordNoSideEffects - flag already set
 * 29. hasParameter - delegate to currentInfo
 * 30. recordImplementedInterface - addImplementedInterface success/failure
 * 31. hasAnyTypeRelatedTags - complex OR condition
 * 32. hasAnySingletonTypeTags - hasType || hasTypedefType || hasEnumParameterType
 * 
 * Defect targeting: The known defect relates to JSDoc comments causing
 * JSC_USELESS_CODE errors. The builder must correctly handle the case where
 * recordBlockDescription is called with parseDocumentation=false - the populated
 * flag should NOT be set, preventing the builder from producing a JSDocInfo
 * that would cause spurious warnings.
 */
public class JSDocInfoBuilderDeepseekTest {

    // Helper to create a simple JSTypeExpression for testing
    private JSTypeExpression createTypeExpression() {
        Node node = new Node(Token.STRING, "testType");
        return new JSTypeExpression(node, "testSource");
    }

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testConstructorAndInitialState() {
        JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
        assertFalse("Builder should not be populated initially", builder.isPopulated());
        assertFalse("Builder should not have file overview initially", builder.isPopulatedWithFileOverview());
        assertFalse("Description should not be recorded initially", builder.isDescriptionRecorded());
        assertFalse("Constructor should not be recorded initially", builder.isConstructorRecorded());
        assertFalse("Interface should not be recorded initially", builder.isInterfaceRecorded());
        assertFalse("Should not have parameter initially", builder.hasParameter("param"));
    }

    @Test(timeout = 4000)
    public void testConstructorWithParseDocumentation() {
        JSDocInfoBuilder builder = new JSDocInfoBuilder(true);
        assertFalse("Builder should not be populated initially", builder.isPopulated());
        assertFalse("Description should not be recorded initially", builder.isDescriptionRecorded());
    }

    @Test(timeout = 4000)
    public void testBuildWithNoPopulationReturnsNull() {
        JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
        JSDocInfo result = builder.build("testSource.js");
        assertNull("build() should return null when not populated", result);
        assertFalse("Builder should still not be populated after build", builder.isPopulated());
    }

    @Test(timeout = 4000)
    public void testBuildWithPopulationAndStateReset() {
        JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
        assertTrue("Visibility should be recorded", builder.recordVisibility(Visibility.PUBLIC));
        assertTrue("Builder should be populated after recording", builder.isPopulated());

        JSDocInfo result = builder.build("testSource.js");
        assertNotNull("build() should return JSDocInfo when populated", result);
        assertEquals("Source name should be set", "testSource.js", result.getSourceName());
        assertEquals("Visibility should be PUBLIC", Visibility.PUBLIC, result.getVisibility());

        assertFalse("Builder should be reset after build", builder.isPopulated());
        assertFalse("New builder should not have visibility", builder.isPopulatedWithFileOverview());

        // Build again should return null since populated was reset
        assertNull("Second build should return null", builder.build("other.js"));
    }

    @Test(timeout = 4000)
    public void testPopulateDefaultsSetsInheritedVisibility() {
        JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
        // Record something to make it populated
        assertTrue("Constancy should be recorded", builder.recordConstancy());
        JSDocInfo result = builder.build("test.js");
        assertNotNull("Result should not be null", result);
        assertEquals("Default visibility should be INHERITED", Visibility.INHERITED, result.getVisibility());
    }

    @Test(timeout = 4000)
    public void testIsPopulatedWithFileOverview() {
        JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
        assertFalse("Should not have file overview initially", builder.isPopulatedWithFileOverview());

        assertTrue("File overview should be recorded", builder.recordFileOverview("Overview text"));
        assertTrue("Should be populated with file overview", builder.isPopulatedWithFileOverview());
    }

    @Test(timeout = 4000)
    public void testIsDescriptionRecorded() {
        JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
        assertFalse("Description should not be recorded initially", builder.isDescriptionRecorded());

        assertTrue("Description should be recorded", builder.recordDescription("Test description"));
        assertTrue("Description should now be recorded", builder.isDescriptionRecorded());
    }

    // ==================== Partition B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000)
    public void testRecordVisibilityNull() {
        JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
        assertTrue("Null visibility should be recorded", builder.recordVisibility(null));
        assertTrue("Builder should be populated", builder.isPopulated());

        JSDocInfo result = builder.build("test.js");
        assertNotNull("Result should not be null", result);
        assertNull("Visibility should be null", result.getVisibility());
    }

    @Test(timeout = 4000)
    public void testRecordVisibilityDuplicate() {
        JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
        assertTrue("First visibility should be recorded", builder.recordVisibility(Visibility.PUBLIC));
        assertFalse("Second visibility should not be recorded", builder.recordVisibility(Visibility.PRIVATE));
        assertTrue("Builder should still be populated", builder.isPopulated());
    }

    @Test(timeout = 4000)
    public void testRecordTypeNull() {
        JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
        assertFalse("Null type should not be recorded", builder.recordType(null));
        assertFalse("Builder should not be populated", builder.isPopulated());
    }

    @Test(timeout = 4000)
    public void testRecordReturnTypeNull() {
        JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
        assertFalse("Null return type should not be recorded", builder.recordReturnType(null));
        assertFalse("Builder should not be populated", builder.isPopulated());
    }

    @Test(timeout = 4000)
    public void testRecordDescriptionNull() {
        JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
        assertFalse("Null description should not be recorded", builder.recordDescription(null));
        assertFalse("Builder should not be populated", builder.isPopulated());
    }

    @Test(timeout = 4000)
    public void testRecordSuppressionsEmptySet() {
        JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
        Set<String> emptySet = new HashSet<>();
        assertTrue("Empty suppressions should be recorded", builder.recordSuppressions(emptySet));
        assertTrue("Builder should be populated", builder.isPopulated());

        JSDocInfo result = builder.build("test.js");
        assertNotNull("Result should not be null", result);
        assertNotNull("Suppressions should not be null", result.getSuppressions());
        assertTrue("Suppressions should be empty", result.getSuppressions().isEmpty());
    }

    @Test(timeout = 4000)
    public void testRecordSuppressionsNull() {
        JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
        assertTrue("Null suppressions should be recorded", builder.recordSuppressions(null));
        assertTrue("Builder should be populated", builder.isPopulated());
    }

    @Test(timeout = 4000)
    public void testMarkAnnotationWithNullMarker() {
        JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
        // No marker should exist initially, but markAnnotation should not throw
        builder.markAnnotation("@param", 1, 0);
        // Should not throw and should not affect populated state
        assertFalse("Builder should not be populated", builder.isPopulated());
    }

    @Test(timeout = 4000)
    public void testMarkTextWithNullMarker() {
        JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
        builder.markText("text", 1, 0, 1, 4);
        assertFalse("Builder should not be populated", builder.isPopulated());
    }

    @Test(timeout = 4000)
    public void testMarkTypeNodeWithNullMarker() {
        JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
        Node typeNode = new Node(Token.STRING, "type");
        builder.markTypeNode(typeNode, 1, 0, 4, true);
        assertFalse("Builder should not be populated", builder.isPopulated());
    }

    @Test(timeout = 4000)
    public void testMarkNameWithNullMarker() {
        JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
        builder.markName("name", 1, 0);
        assertFalse("Builder should not be populated", builder.isPopulated());
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    @Test(timeout = 4000)
    public void testRecordBlockDescriptionWithParseDocumentationFalse() {
        // This is the critical defect-targeting test.
        // When parseDocumentation is false, recordBlockDescription should NOT
        // set the populated flag, preventing spurious JSC_USELESS_CODE errors.
        JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
        boolean result = builder.recordBlockDescription("Block description");
        assertFalse("Block description should not be recorded when parseDocumentation is false", result);
        assertFalse("Builder should NOT be populated when parseDocumentation is false", builder.isPopulated());

        // build() should return null because nothing was populated
        JSDocInfo info = builder.build("test.js");
        assertNull("build() should return null when only block description with parseDocumentation=false", info);
    }

    @Test(timeout = 4000)
    public void testRecordBlockDescriptionWithParseDocumentationTrue() {
        JSDocInfoBuilder builder = new JSDocInfoBuilder(true);
        boolean result = builder.recordBlockDescription("Block description");
        assertTrue("Block description should be recorded when parseDocumentation is true", result);
        assertTrue("Builder should be populated when parseDocumentation is true", builder.isPopulated());

        JSDocInfo info = builder.build("test.js");
        assertNotNull("build() should return JSDocInfo when populated", info);
        assertEquals("Block description should be set", "Block description", info.getBlockDescription());
    }

    @Test(timeout = 4000)
    public void testRecordBlockDescriptionWithNull() {
        JSDocInfoBuilder builder = new JSDocInfoBuilder(true);
        boolean result = builder.recordBlockDescription(null);
        assertFalse("Null block description should not be recorded", result);
        assertFalse("Builder should not be populated with null block description", builder.isPopulated());
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000)
    public void testRecordTypeAfterConstructor() {
        JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
        assertTrue("Constructor should be recorded", builder.recordConstructor());
        assertFalse("Type should not be recorded after constructor", builder.recordType(createTypeExpression()));
        assertTrue("Builder should still be populated", builder.isPopulated());
    }

    @Test(timeout = 4000)
    public void testRecordConstructorAfterType() {
        JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
        assertTrue("Type should be recorded", builder.recordType(createTypeExpression()));
        assertFalse("Constructor should not be recorded after type", builder.recordConstructor());
        assertTrue("Builder should still be populated", builder.isPopulated());
    }

    @Test(timeout = 4000)
    public void testRecordInterfaceAfterConstructor() {
        JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
        assertTrue("Constructor should be recorded", builder.recordConstructor());
        assertFalse("Interface should not be recorded after constructor", builder.recordInterface());
        assertTrue("Builder should still be populated", builder.isPopulated());
    }

    @Test(timeout = 4000)
    public void testRecordConstructorAfterInterface() {
        JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
        assertTrue("Interface should be recorded", builder.recordInterface());
        assertFalse("Constructor should not be recorded after interface", builder.recordConstructor());
        assertTrue("Builder should still be populated", builder.isPopulated());
    }

    @Test(timeout = 4000)
    public void testRecordParameterWithSingletonTypeTag() {
        JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
        assertTrue("Type should be recorded", builder.recordType(createTypeExpression()));
        assertFalse("Parameter should not be recorded when singleton type tag exists", 
                builder.recordParameter("param", createTypeExpression()));
        assertTrue("Builder should still be populated", builder.isPopulated());
    }

    @Test(timeout = 4000)
    public void testRecordThrowTypeWithSingletonTypeTag() {
        JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
        assertTrue("Typedef should be recorded", builder.recordTypedef(createTypeExpression()));
        assertFalse("Throw type should not be recorded when singleton type tag exists", 
                builder.recordThrowType(createTypeExpression()));
        assertTrue("Builder should still be populated", builder.isPopulated());
    }

    @Test(timeout = 4000)
    public void testRecordReturnTypeWithSingletonTypeTag() {
        JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
        assertTrue("Enum parameter type should be recorded", builder.recordEnumParameterType(createTypeExpression()));
        assertFalse("Return type should not be recorded when singleton type tag exists", 
                builder.recordReturnType(createTypeExpression()));
        assertTrue("Builder should still be populated", builder.isPopulated());
    }

    @Test(timeout = 4000)
    public void testRecordDefineTypeWithConstant() {
        JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
        assertTrue("Constancy should be recorded", builder.recordConstancy());
        assertFalse("Define type should not be recorded when constant", 
                builder.recordDefineType(createTypeExpression()));
        assertTrue("Builder should still be populated", builder.isPopulated());
    }

    @Test(timeout = 4000)
    public void testRecordThisTypeAfterThisType() {
        JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
        assertTrue("First this type should be recorded", builder.recordThisType(createTypeExpression()));
        assertFalse("Second this type should not be recorded", builder.recordThisType(createTypeExpression()));
        assertTrue("Builder should still be populated", builder.isPopulated());
    }

    @Test(timeout = 4000)
    public void testRecordBaseTypeAfterBaseType() {
        JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
        assertTrue("First base type should be recorded", builder.recordBaseType(createTypeExpression()));
        assertFalse("Second base type should not be recorded", builder.recordBaseType(createTypeExpression()));
        assertTrue("Builder should still be populated", builder.isPopulated());
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testBuilderReuseAfterBuild() {
        JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
        assertTrue("Visibility should be recorded", builder.recordVisibility(Visibility.PUBLIC));
        JSDocInfo first = builder.build("first.js");
        assertNotNull("First build should not be null", first);
        assertEquals("First source name", "first.js", first.getSourceName());

        // Reuse builder for second JSDocInfo
        assertTrue("Constancy should be recorded", builder.recordConstancy());
        JSDocInfo second = builder.build("second.js");
        assertNotNull("Second build should not be null", second);
        assertEquals("Second source name", "second.js", second.getSourceName());
        assertTrue("Second should be constant", second.isConstant());
        assertNull("Second should not have visibility", second.getVisibility());
    }

    @Test(timeout = 4000)
    public void testMarkerLifecycle() {
        JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
        // Mark annotation creates a marker
        builder.markAnnotation("@param", 1, 0);
        // Mark text should work with existing marker
        builder.markText("param description", 1, 7, 1, 25);
        // Mark name should work with existing marker
        builder.markName("paramName", 1, 7);
        // Mark type should work with existing marker
        Node typeNode = new Node(Token.STRING, "string");
        builder.markTypeNode(typeNode, 1, 7, 13, false);

        // Record a parameter to make it populated
        assertTrue("Parameter should be recorded", builder.recordParameter("paramName", createTypeExpression()));
        JSDocInfo result = builder.build("test.js");
        assertNotNull("Result should not be null", result);
        assertEquals("Should have one parameter", 1, result.getParameterCount());
    }

    @Test(timeout = 4000)
    public void testRecordMultipleParameters() {
        JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
        assertTrue("First parameter should be recorded", builder.recordParameter("param1", createTypeExpression()));
        assertTrue("Second parameter should be recorded", builder.recordParameter("param2", createTypeExpression()));
        assertFalse("Duplicate parameter should not be recorded", builder.recordParameter("param1", createTypeExpression()));
        assertTrue("Builder should be populated", builder.isPopulated());
        assertTrue("Should have param1", builder.hasParameter("param1"));
        assertTrue("Should have param2", builder.hasParameter("param2"));
        assertFalse("Should not have param3", builder.hasParameter("param3"));
    }

    @Test(timeout = 4000)
    public void testRecordAllFlags() {
        JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
        assertTrue("PreserveTry", builder.recordPreserveTry());
        assertTrue("Override", builder.recordOverride());
        assertTrue("NoAlias", builder.recordNoAlias());
        assertTrue("Deprecated", builder.recordDeprecated());
        assertTrue("Export", builder.recordExport());
        assertTrue("NoShadow", builder.recordNoShadow());
        assertTrue("ImplicitCast", builder.recordImplicitCast());
        assertTrue("NoSideEffects", builder.recordNoSideEffects());
        assertTrue("Hiddenness", builder.recordHiddenness());
        assertTrue("NoTypeCheck", builder.recordNoTypeCheck());

        JSDocInfo result = builder.build("test.js");
        assertNotNull("Result should not be null", result);
        assertTrue("Should preserve try", result.shouldPreserveTry());
        assertTrue("Should be override", result.isOverride());
        assertTrue("Should be no alias", result.isNoAlias());
        assertTrue("Should be deprecated", result.isDeprecated());
        assertTrue("Should be export", result.isExport());
        assertTrue("Should be no shadow", result.isNoShadow());
        assertTrue("Should be implicit cast", result.isImplicitCast());
        assertTrue("Should be no side effects", result.isNoSideEffects());
        assertTrue("Should be hidden", result.isHidden());
        assertTrue("Should be no type check", result.isNoTypeCheck());
    }

    @Test(timeout = 4000)
    public void testRecordDuplicateFlags() {
        JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
        assertTrue("First recordPreserveTry", builder.recordPreserveTry());
        assertFalse("Second recordPreserveTry", builder.recordPreserveTry());
        assertTrue("First recordOverride", builder.recordOverride());
        assertFalse("Second recordOverride", builder.recordOverride());
        assertTrue("First recordNoAlias", builder.recordNoAlias());
        assertFalse("Second recordNoAlias", builder.recordNoAlias());
        assertTrue("First recordDeprecated", builder.recordDeprecated());
        assertFalse("Second recordDeprecated", builder.recordDeprecated());
        assertTrue("First recordExport", builder.recordExport());
        assertFalse("Second recordExport", builder.recordExport());
        assertTrue("First recordNoShadow", builder.recordNoShadow());
        assertFalse("Second recordNoShadow", builder.recordNoShadow());
        assertTrue("First recordImplicitCast", builder.recordImplicitCast());
        assertFalse("Second recordImplicitCast", builder.recordImplicitCast());
        assertTrue("First recordNoSideEffects", builder.recordNoSideEffects());
        assertFalse("Second recordNoSideEffects", builder.recordNoSideEffects());
        assertTrue("First recordHiddenness", builder.recordHiddenness());
        assertFalse("Second recordHiddenness", builder.recordHiddenness());
        assertTrue("First recordNoTypeCheck", builder.recordNoTypeCheck());
        assertFalse("Second recordNoTypeCheck", builder.recordNoTypeCheck());
    }

    @Test(timeout = 4000)
    public void testRecordImplementedInterface() {
        JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
        JSTypeExpression interfaceType = createTypeExpression();
        assertTrue("First interface should be recorded", builder.recordImplementedInterface(interfaceType));
        assertTrue("Second interface should be recorded", builder.recordImplementedInterface(createTypeExpression()));
        assertTrue("Builder should be populated", builder.isPopulated());

        JSDocInfo result = builder.build("test.js");
        assertNotNull("Result should not be null", result);
        assertEquals("Should have 2 implemented interfaces", 2, result.getImplementedInterfaceCount());
    }

    @Test(timeout = 4000)
    public void testRecordAuthorReferenceVersion() {
        JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
        assertTrue("Author should be recorded", builder.addAuthor("John Doe"));
        assertTrue("Reference should be recorded", builder.addReference("see docs"));
        assertTrue("Version should be recorded", builder.recordVersion("1.0"));

        JSDocInfo result = builder.build("test.js");
        assertNotNull("Result should not be null", result);
        assertEquals("Should have one author", 1, result.getAuthors().size());
        assertEquals("Should have one reference", 1, result.getReferences().size());
        assertEquals("Version should be 1.0", "1.0", result.getVersion());
    }

    @Test(timeout = 4000)
    public void testRecordDeprecationReasonAndSuppressions() {
        JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
        assertTrue("Deprecation reason should be recorded", builder.recordDeprecationReason("Old API"));
        Set<String> suppressions = new HashSet<>();
        suppressions.add("deprecated");
        suppressions.add("unused");
        assertTrue("Suppressions should be recorded", builder.recordSuppressions(suppressions));

        JSDocInfo result = builder.build("test.js");
        assertNotNull("Result should not be null", result);
        assertEquals("Deprecation reason", "Old API", result.getDeprecationReason());
        assertTrue("Should have suppressions", result.getSuppressions().contains("deprecated"));
        assertTrue("Should have suppressions", result.getSuppressions().contains("unused"));
    }

    @Test(timeout = 4000)
    public void testRecordParameterDescription() {
        JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
        assertTrue("Parameter should be recorded", builder.recordParameter("param", createTypeExpression()));
        assertTrue("Parameter description should be recorded", builder.recordParameterDescription("param", "Description"));
        assertFalse("Duplicate parameter description should not be recorded", 
                builder.recordParameterDescription("param", "Another description"));

        JSDocInfo result = builder.build("test.js");
        assertNotNull("Result should not be null", result);
        assertEquals("Parameter description", "Description", result.getDescriptionForParameter("param"));
    }

    @Test(timeout = 4000)
    public void testRecordTemplateTypeName() {
        JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
        assertTrue("First template type should be recorded", builder.recordTemplateTypeName("T"));
        assertTrue("Second template type should be recorded", builder.recordTemplateTypeName("U"));
        assertFalse("Duplicate template type should not be recorded", builder.recordTemplateTypeName("T"));

        JSDocInfo result = builder.build("test.js");
        assertNotNull("Result should not be null", result);
        assertEquals("Should have 2 template types", 2, result.getTemplateTypeNames().size());
    }

    @Test(timeout = 4000)
    public void testRecordThrowDescription() {
        JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
        JSTypeExpression throwType = createTypeExpression();
        assertTrue("Throw type should be recorded", builder.recordThrowType(throwType));
        assertTrue("Throw description should be recorded", builder.recordThrowDescription(throwType, "Throws description"));

        JSDocInfo result = builder.build("test.js");
        assertNotNull("Result should not be null", result);
        assertEquals("Throw description", "Throws description", result.getDescriptionForThrows(throwType));
    }

    @Test(timeout = 4000)
    public void testRecordReturnDescription() {
        JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
        assertTrue("Return type should be recorded", builder.recordReturnType(createTypeExpression()));
        assertTrue("Return description should be recorded", builder.recordReturnDescription("Returns something"));
        assertFalse("Duplicate return description should not be recorded", 
                builder.recordReturnDescription("Returns something else"));

        JSDocInfo result = builder.build("test.js");
        assertNotNull("Result should not be null", result);
        assertEquals("Return description", "Returns something", result.getReturnDescription());
    }

    @Test(timeout = 4000)
    public void testRecordFileOverviewAndDescription() {
        JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
        assertTrue("File overview should be recorded", builder.recordFileOverview("File overview"));
        assertTrue("Description should be recorded", builder.recordDescription("Description"));

        JSDocInfo result = builder.build("test.js");
        assertNotNull("Result should not be null", result);
        assertEquals("File overview", "File overview", result.getFileOverview());
        assertEquals("Description", "Description", result.getDescription());
    }
}