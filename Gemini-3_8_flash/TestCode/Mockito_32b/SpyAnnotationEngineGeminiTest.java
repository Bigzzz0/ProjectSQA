package org.mockito.internal.configuration;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.internal.util.MockUtil;

/*
 * [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Target Class: org.mockito.internal.configuration.SpyAnnotationEngine
 *
 * Decision / Condition Coverage:
 * 1. createMockFor(annotation, field)
 *    - Always returns null (contract verification).
 * 2. process(context, testClass):
 *    - Branch: for (Field field : fields)
 *      * Context with 0 declared fields -> loop terminates immediately.
 *      * Context with fields without @Spy -> bypassed, fields remain unmodified.
 *    - Branch: field.isAnnotationPresent(Spy.class)
 *      * TRUE: proceed to annotation assertion and instance extraction.
 *      * FALSE: skipped.
 *    - Branch: assertNoAnnotations(Spy.class, field, Mock.class, MockitoAnnotations.Mock.class, Captor.class)
 *      * Conflicting annotation present (@Mock, deprecated @Mock, @Captor) -> triggers Reporter exception.
 *      * Clean @Spy field -> continues normally.
 *    - Branch: instance == null
 *      * TRUE: throws MockitoException ("Cannot create a @Spy for '<field>' field because the *instance* is missing").
 *      * FALSE: proceeds to mock check.
 *    - Branch: new MockUtil().isMock(instance)
 *      * TRUE: calls Mockito.reset(instance), does not re-wrap, preserves instance reference.
 *      * FALSE: wraps in spy via field.set(testClass, Mockito.spy(instance)).
 *    - Branch: field accessibility & finally block
 *      * Field accessibility is saved (wasAccessible), forced to true, and restored in finally.
 * 3. assertNoAnnotations(annotation, field, undesiredAnnotations):
 *    - Undesired annotation matches field -> Reporter throws MockitoException.
 *    - None match -> clean completion.
 *
 * Known Defect (Defects4J - SpyShouldHaveNiceNameTest::shouldPrintNiceName):
 * - When creating a spy for a field, Mockito.spy(instance) creates a spy without propagating
 *   the field name into the mock settings name attribute. The mock name defaults to the class
 *   name (e.g. "arrayList") instead of the field's name (e.g. "mySpecialNamedSpy").
 * - Partition C targets this exact requirement.
 * ====================================================================================================
 */
public class SpyAnnotationEngineGeminiTest {

    private SpyAnnotationEngine engine;
    private MockUtil mockUtil;

    @Before
    public void setUp() {
        engine = new SpyAnnotationEngine();
        mockUtil = new MockUtil();
    }

    // =========================================================================
    // Test Target Fixtures
    // =========================================================================

    static class EmptyFixture {}

    static class NonAnnotatedFixture {
        String normalString = "hello";
        int normalInt = 100;
        List<String> normalList = new LinkedList<String>();
    }

    static class StandardSpyFixture {
        @Spy
        List<String> spiedList = new ArrayList<String>();
    }

    static class NamedSpyDefectFixture {
        @Spy
        List<String> mySpecialNamedSpy = new ArrayList<String>();
    }

    static class NullSpyFixture {
        @Spy
        List<String> uninitializedSpy = null;
    }

    static class PrivateSpyFixture {
        @Spy
        private List<String> privateList = new ArrayList<String>();

        public List<String> getPrivateList() {
            return privateList;
        }
    }

    static class ConflictWithMockFixture {
        @Spy
        @Mock
        List<String> invalidCombination = new ArrayList<String>();
    }

    @SuppressWarnings("deprecation")
    static class ConflictWithDeprecatedMockFixture {
        @Spy
        @org.mockito.MockitoAnnotations.Mock
        List<String> invalidCombination = new ArrayList<String>();
    }

    static class ConflictWithCaptorFixture {
        @Spy
        @Captor
        List<String> invalidCombination = new ArrayList<String>();
    }

    static class SuperClassFixture {
        @Spy
        List<String> superSpyList = new ArrayList<String>();
    }

    static class SubClassFixture extends SuperClassFixture {
        @Spy
        List<String> subSpyList = new ArrayList<String>();
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateMockForReturnsNull() {
        Object result = engine.createMockFor(null, null);
        assertNull("createMockFor must consistently return null for SpyAnnotationEngine", result);
    }

    @Test(timeout = 4000)
    public void testProcessSpiesInstantiatedField() {
        StandardSpyFixture fixture = new StandardSpyFixture();
        fixture.spiedList.add("initial_item");

        engine.process(StandardSpyFixture.class, fixture);

        assertNotNull("Spied field should not be null", fixture.spiedList);
        assertTrue("Field should have been converted into a Mockito mock/spy", mockUtil.isMock(fixture.spiedList));
        assertEquals("Pre-existing elements in original instance must be preserved", 1, fixture.spiedList.size());
        assertEquals("initial_item", fixture.spiedList.get(0));

        fixture.spiedList.add("new_item");
        assertEquals(2, fixture.spiedList.size());
    }

    @Test(timeout = 4000)
    public void testProcessAlreadySpiedFieldResetsMockWithoutReWrapping() {
        StandardSpyFixture fixture = new StandardSpyFixture();
        engine.process(StandardSpyFixture.class, fixture);

        Object firstSpyInstance = fixture.spiedList;
        assertTrue(mockUtil.isMock(firstSpyInstance));

        // Re-processing the same fixture should hit the `new MockUtil().isMock(instance)` branch
        engine.process(StandardSpyFixture.class, fixture);
        Object secondSpyInstance = fixture.spiedList;

        assertSame("Field instance must be preserved via Mockito.reset rather than replaced with a new spy wrapper",
                firstSpyInstance, secondSpyInstance);
    }

    @Test(timeout = 4000)
    public void testProcessRespectsFieldAccessibilityRestoration() throws Exception {
        PrivateSpyFixture fixture = new PrivateSpyFixture();
        Field field = PrivateSpyFixture.class.getDeclaredField("privateList");

        assertFalse("Field should initially be inaccessible", field.isAccessible());

        engine.process(PrivateSpyFixture.class, fixture);

        assertTrue("Private field must be successfully spied", mockUtil.isMock(fixture.getPrivateList()));
        assertFalse("Field accessibility should be restored to false in finally block", field.isAccessible());
    }

    @Test(timeout = 4000)
    public void testProcessInheritanceHierarchySeparately() {
        SubClassFixture fixture = new SubClassFixture();

        // Process only SubClass level
        engine.process(SubClassFixture.class, fixture);
        assertTrue("Subclass declared spy field must be spied", mockUtil.isMock(fixture.subSpyList));
        assertFalse("Superclass field must not be spied when only sub class context is processed",
                mockUtil.isMock(fixture.superSpyList));

        // Process SuperClass level
        engine.process(SuperClassFixture.class, fixture);
        assertTrue("Superclass spy field must now be spied", mockUtil.isMock(fixture.superSpyList));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testProcessClassWithNoDeclaredFields() {
        EmptyFixture fixture = new EmptyFixture();
        engine.process(EmptyFixture.class, fixture);
        // Validates loop over empty array terminates cleanly
    }

    @Test(timeout = 4000)
    public void testProcessClassWithNoSpyAnnotationsLeavesFieldsUntouched() {
        NonAnnotatedFixture fixture = new NonAnnotatedFixture();
        engine.process(NonAnnotatedFixture.class, fixture);

        assertFalse("Non-annotated list must not be converted to a mock", mockUtil.isMock(fixture.normalList));
        assertEquals("hello", fixture.normalString);
        assertEquals(100, fixture.normalInt);
    }

    @Test(timeout = 4000)
    @SuppressWarnings("unchecked")
    public void testAssertNoAnnotationsPassesWhenNoUndesiredAnnotationsPresent() throws Exception {
        Field field = StandardSpyFixture.class.getDeclaredField("spiedList");
        engine.assertNoAnnotations(Spy.class, field, Mock.class, Captor.class);
        // Clean execution confirms no exception is raised
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (SpyShouldHaveNiceName)
    // =========================================================================

    @Test(timeout = 4000)
    public void testSpyFieldRetainsFieldNameAsMockName_DefectTarget() {
        /*
         * Defect Ground Truth:
         * When @Spy is processed, SpyAnnotationEngine calls Mockito.spy(instance) which does NOT
         * pass the field's name to MockSettings. The mock name defaults to the uncapitalized type name ("arrayList")
         * rather than the field name ("mySpecialNamedSpy").
         * This assertion guarantees that the spy mock possesses the exact field name.
         */
        NamedSpyDefectFixture fixture = new NamedSpyDefectFixture();
        engine.process(NamedSpyDefectFixture.class, fixture);

        assertNotNull("Spied instance must not be null", fixture.mySpecialNamedSpy);
        assertTrue("Instance must be recognized as a mock", mockUtil.isMock(fixture.mySpecialNamedSpy));

        String mockName = mockUtil.getMockName(fixture.mySpecialNamedSpy).toString();
        assertEquals("Spied field should be registered with the field's variable name as its mock name",
                "mySpecialNamedSpy", mockName);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testProcessThrowsMockitoExceptionWhenSpyInstanceIsNull() {
        NullSpyFixture fixture = new NullSpyFixture();
        try {
            engine.process(NullSpyFixture.class, fixture);
            fail("Expected MockitoException when @Spy field instance is null");
        } catch (MockitoException e) {
            String message = e.getMessage();
            assertTrue("Exception message should identify the missing instance",
                    message.contains("Cannot create a @Spy for 'uninitializedSpy' field because the *instance* is missing"));
            assertTrue("Exception message should instruct on proper usage",
                    message.contains("The instance must be created *before* initMocks()"));
        }
    }

    @Test(timeout = 4000)
    public void testProcessThrowsMockitoExceptionWhenBothSpyAndMockPresent() {
        ConflictWithMockFixture fixture = new ConflictWithMockFixture();
        try {
            engine.process(ConflictWithMockFixture.class, fixture);
            fail("Expected MockitoException due to conflicting @Spy and @Mock annotations");
        } catch (MockitoException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    @SuppressWarnings("deprecation")
    public void testProcessThrowsMockitoExceptionWhenBothSpyAndDeprecatedMockPresent() {
        ConflictWithDeprecatedMockFixture fixture = new ConflictWithDeprecatedMockFixture();
        try {
            engine.process(ConflictWithDeprecatedMockFixture.class, fixture);
            fail("Expected MockitoException due to conflicting @Spy and deprecated @Mock annotations");
        } catch (MockitoException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testProcessThrowsMockitoExceptionWhenBothSpyAndCaptorPresent() {
        ConflictWithCaptorFixture fixture = new ConflictWithCaptorFixture();
        try {
            engine.process(ConflictWithCaptorFixture.class, fixture);
            fail("Expected MockitoException due to conflicting @Spy and @Captor annotations");
        } catch (MockitoException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    @SuppressWarnings("unchecked")
    public void testAssertNoAnnotationsDirectInvocationTriggersReporterException() throws Exception {
        Field field = ConflictWithMockFixture.class.getDeclaredField("invalidCombination");
        try {
            engine.assertNoAnnotations(Spy.class, field, Mock.class);
            fail("Expected MockitoException from assertNoAnnotations directly");
        } catch (MockitoException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEngineInstantiableAndImplementsAnnotationEngine() {
        assertNotNull(engine);
        assertTrue("Engine must implement AnnotationEngine",
                engine instanceof org.mockito.configuration.AnnotationEngine);
    }
}