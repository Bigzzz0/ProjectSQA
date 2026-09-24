package org.mockito.internal.creation.bytebuddy;

import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.internal.InternalMockHandler;
import org.mockito.internal.creation.instance.ClassInstantiator;
import org.mockito.internal.creation.instance.InstantiationException;
import org.mockito.invocation.MockHandler;
import org.mockito.mock.MockCreationSettings;
import org.mockito.mock.SerializableMode;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: org.mockito.internal.creation.bytebuddy.ByteBuddyMockMaker
 *
 * Decision / Condition Branch Coverage:
 * 1. createMock:
 *    - Branch: settings.getSerializableMode() == SerializableMode.ACROSS_CLASSLOADERS -> throws MockitoException
 *    - Branch: settings.getSerializableMode() != SerializableMode.ACROSS_CLASSLOADERS -> proceeds with bytecode generation
 *    - Exception Path: ClassCastException in ensureMockIsAssignableToMockedType -> catches CCE, describes classes, rethrows MockitoException
 *    - Exception Path: InstantiationException in classInstantiator.instantiate -> catches InstantiationException, rethrows MockitoException
 * 2. ensureMockIsAssignableToMockedType:
 *    - Valid cast: mockInstance is assignable to settings.getTypeToMock() -> returns casted instance
 *    - Invalid cast: throws ClassCastException (caught by createMock)
 * 3. describeClass(Class):
 *    - Branch: type == null -> returns "null"
 *    - Branch: type != null -> returns "'<canonicalName>', loaded by classloader : '<classLoader>'"
 * 4. describeClass(Object):
 *    - Branch: instance == null -> returns "null"
 *    - Branch: instance != null -> calls describeClass(instance.getClass())
 * 5. getHandler(Object):
 *    - Branch: !(mock instanceof MockMethodInterceptor.MockAccess) -> returns null (handles null, plain objects, non-mocks)
 *    - Branch: mock instanceof MockMethodInterceptor.MockAccess -> returns interceptor's MockHandler
 * 6. resetMock(Object, MockHandler, MockCreationSettings):
 *    - Assigns new MockMethodInterceptor with validated InternalMockHandler and settings
 * 7. initializeClassInstantiator():
 *    - Normal instantiation: loads ClassInstantiator$UsingObjenesis via reflection
 *    - Failure instantiation: throws IllegalStateException when class loading fails
 * 8. asInternalMockHandler(MockHandler):
 *    - Branch: !(handler instanceof InternalMockHandler) -> throws MockitoException
 *    - Branch: handler instanceof InternalMockHandler -> returns casted InternalMockHandler
 *
 * Defects4J Known Defect Targeted:
 * - Defects4J issue (e.g. CreatingMocksWithConstructorTest::can_mock_inner_classes / can_create_mock_with_constructor):
 *   On defective versions, ByteBuddyMockMaker instantiated mocks strictly via Objenesis without
 *   invoking constructor or passing outer class instances (settings.getOuterClassInstance() / settings.getConstructorArgs()).
 *   This resulted in null outer references (this$0) or uninitialized constructor fields.
 *   Dedicated defect tests assert proper initialization of inner class mocks with outer instances and constructor mocks.
 */
public class ByteBuddyMockMakerGeminiTest {

    // =========================================================================
    // Test Double Infrastructure (Strictly Java 8 Dynamic Proxies / Handlers)
    // =========================================================================

    public static class SampleClass {
        public String execute() {
            return "original";
        }
    }

    public interface SampleExtraInterface1 {
        void action1();
    }

    public interface SampleExtraInterface2 {
        void action2();
    }

    public static class EnclosingClass {
        public final String label = "outer_enclosing_label";

        public class InnerClass {
            public String getOuterLabel() {
                return label;
            }
        }
    }

    public static class ParameterizedClass {
        private final String message;

        public ParameterizedClass(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    @SuppressWarnings("unchecked")
    private <T> MockCreationSettings<T> createSettingsStub(
            final Class<T> typeToMock,
            final Set<Class<?>> extraInterfaces,
            final SerializableMode serializableMode,
            final Object outerInstance,
            final Object[] constructorArgs) {
        return (MockCreationSettings<T>) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{MockCreationSettings.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        String name = method.getName();
                        if ("getTypeToMock".equals(name)) {
                            return typeToMock;
                        }
                        if ("getExtraInterfaces".equals(name)) {
                            return extraInterfaces != null ? extraInterfaces : Collections.emptySet();
                        }
                        if ("getSerializableMode".equals(name)) {
                            return serializableMode != null ? serializableMode : SerializableMode.NONE;
                        }
                        if ("getOuterClassInstance".equals(name)) {
                            return outerInstance;
                        }
                        if ("getConstructorArgs".equals(name)) {
                            return constructorArgs;
                        }
                        if ("isUsingConstructor".equals(name)) {
                            return constructorArgs != null || outerInstance != null;
                        }
                        if ("isSerializable".equals(name)) {
                            return serializableMode != null && serializableMode != SerializableMode.NONE;
                        }
                        if ("isStripAnnotations".equals(name)) {
                            return false;
                        }
                        if ("equals".equals(name)) {
                            return proxy == args[0];
                        }
                        if ("hashCode".equals(name)) {
                            return System.identityHashCode(proxy);
                        }
                        if ("toString".equals(name)) {
                            return "MockCreationSettingsStub[" + typeToMock.getName() + "]";
                        }
                        Class<?> returnType = method.getReturnType();
                        if (returnType.equals(boolean.class)) return false;
                        if (returnType.equals(int.class)) return 0;
                        if (returnType.equals(long.class)) return 0L;
                        if (returnType.equals(Set.class)) return Collections.emptySet();
                        if (returnType.equals(List.class)) return Collections.emptyList();
                        return null;
                    }
                }
        );
    }

    private InternalMockHandler<?> createInternalMockHandlerStub(final MockCreationSettings<?> settings) {
        return (InternalMockHandler<?>) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{InternalMockHandler.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        String name = method.getName();
                        if ("getMockSettings".equals(name)) {
                            return settings;
                        }
                        if ("equals".equals(name)) {
                            return proxy == args[0];
                        }
                        if ("hashCode".equals(name)) {
                            return System.identityHashCode(proxy);
                        }
                        if ("toString".equals(name)) {
                            return "InternalMockHandlerStub";
                        }
                        Class<?> returnType = method.getReturnType();
                        if (returnType.equals(boolean.class)) return false;
                        if (returnType.equals(int.class)) return 0;
                        return null;
                    }
                }
        );
    }

    private MockHandler createForeignMockHandlerStub() {
        return (MockHandler) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{MockHandler.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) {
                        return null;
                    }
                }
        );
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateMockStandardClass() {
        ByteBuddyMockMaker maker = new ByteBuddyMockMaker();
        MockCreationSettings<SampleClass> settings = createSettingsStub(
                SampleClass.class, Collections.emptySet(), SerializableMode.NONE, null, null);
        InternalMockHandler<?> handler = createInternalMockHandlerStub(settings);

        SampleClass mock = maker.createMock(settings, handler);

        assertNotNull("Created mock must not be null", mock);
        assertTrue("Created mock must be instance of SampleClass", mock instanceof SampleClass);
        assertSame("Mock handler retrieved from maker must match registered handler",
                handler, maker.getHandler(mock));
    }

    @Test(timeout = 4000)
    public void testCreateMockInterface() {
        ByteBuddyMockMaker maker = new ByteBuddyMockMaker();
        MockCreationSettings<Runnable> settings = createSettingsStub(
                Runnable.class, Collections.emptySet(), SerializableMode.NONE, null, null);
        InternalMockHandler<?> handler = createInternalMockHandlerStub(settings);

        Runnable mock = maker.createMock(settings, handler);

        assertNotNull("Interface mock must not be null", mock);
        assertTrue("Mock must implement Runnable", mock instanceof Runnable);
        assertSame("Handler must match", handler, maker.getHandler(mock));
    }

    @Test(timeout = 4000)
    public void testResetMockUpdatesHandler() {
        ByteBuddyMockMaker maker = new ByteBuddyMockMaker();
        MockCreationSettings<SampleClass> settings = createSettingsStub(
                SampleClass.class, Collections.emptySet(), SerializableMode.NONE, null, null);
        InternalMockHandler<?> handler1 = createInternalMockHandlerStub(settings);
        InternalMockHandler<?> handler2 = createInternalMockHandlerStub(settings);

        SampleClass mock = maker.createMock(settings, handler1);
        assertSame("Initial handler must match handler1", handler1, maker.getHandler(mock));

        maker.resetMock(mock, handler2, settings);
        assertSame("Reset handler must match handler2", handler2, maker.getHandler(mock));
    }

    @Test(timeout = 4000)
    public void testGetHandlerOnNonMockReturnsNull() {
        ByteBuddyMockMaker maker = new ByteBuddyMockMaker();

        assertNull("getHandler(null) must return null", maker.getHandler(null));
        assertNull("getHandler(nonMockObject) must return null", maker.getHandler(new Object()));
        assertNull("getHandler(String) must return null", maker.getHandler("just-a-string"));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateMockWithExtraInterfaces() {
        ByteBuddyMockMaker maker = new ByteBuddyMockMaker();
        Set<Class<?>> extraInterfaces = new HashSet<Class<?>>();
        extraInterfaces.add(SampleExtraInterface1.class);
        extraInterfaces.add(SampleExtraInterface2.class);

        MockCreationSettings<SampleClass> settings = createSettingsStub(
                SampleClass.class, extraInterfaces, SerializableMode.NONE, null, null);
        InternalMockHandler<?> handler = createInternalMockHandlerStub(settings);

        SampleClass mock = maker.createMock(settings, handler);

        assertNotNull("Mock with extra interfaces must not be null", mock);
        assertTrue("Mock must be instance of SampleClass", mock instanceof SampleClass);
        assertTrue("Mock must implement SampleExtraInterface1", mock instanceof SampleExtraInterface1);
        assertTrue("Mock must implement SampleExtraInterface2", mock instanceof SampleExtraInterface2);
    }

    @Test(timeout = 4000)
    public void testCreateMockWithBasicSerializationMode() {
        ByteBuddyMockMaker maker = new ByteBuddyMockMaker();
        MockCreationSettings<SampleClass> settings = createSettingsStub(
                SampleClass.class, Collections.emptySet(), SerializableMode.BASIC, null, null);
        InternalMockHandler<?> handler = createInternalMockHandlerStub(settings);

        SampleClass mock = maker.createMock(settings, handler);
        assertNotNull("Mock with BASIC serialization mode must be successfully created", mock);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Known Regressions)
    // =========================================================================

    /**
     * Targets Defects4J regression:
     * - CreatingMocksWithConstructorTest::can_mock_inner_classes
     * - SpyAnnotationTest::should_spy_inner_class
     *
     * In the defective version, ByteBuddyMockMaker instantiates the mock strictly via
     * Objenesis without passing the outer instance or invoking constructor, leaving
     * the synthetic 'this$0' field null. The test verifies that an inner class mock
     * receives and retains its enclosing outer instance.
     */
    @Test(timeout = 4000)
    public void testDefectTargetedInnerClassMockHasOuterInstanceAssigned() throws Exception {
        ByteBuddyMockMaker maker = new ByteBuddyMockMaker();
        EnclosingClass outerInstance = new EnclosingClass();

        MockCreationSettings<EnclosingClass.InnerClass> settings = createSettingsStub(
                EnclosingClass.InnerClass.class,
                Collections.emptySet(),
                SerializableMode.NONE,
                outerInstance,
                null
        );
        InternalMockHandler<?> handler = createInternalMockHandlerStub(settings);

        EnclosingClass.InnerClass mockInner = maker.createMock(settings, handler);
        assertNotNull("Inner class mock must not be null", mockInner);

        Field outerField = null;
        for (Field f : EnclosingClass.InnerClass.class.getDeclaredFields()) {
            if (f.getName().startsWith("this$")) {
                outerField = f;
                break;
            }
        }
        assertNotNull("InnerClass must declare a synthetic outer class reference (this$0)", outerField);
        outerField.setAccessible(true);
        Object actualOuter = outerField.get(mockInner);

        assertEquals("Inner class mock must have its outer instance initialized and assigned",
                outerInstance, actualOuter);
    }

    /**
     * Targets Defects4J regression:
     * - CreatingMocksWithConstructorTest::can_create_mock_with_constructor
     *
     * On defective versions, constructor arguments are ignored and fields initialized
     * inside constructors remain null because Objenesis bypasses constructor invocation.
     */
    @Test(timeout = 4000)
    public void testDefectTargetedMockCreatedWithConstructorArgs() throws Exception {
        ByteBuddyMockMaker maker = new ByteBuddyMockMaker();
        Object[] constructorArgs = new Object[]{"expected_constructor_argument"};

        MockCreationSettings<ParameterizedClass> settings = createSettingsStub(
                ParameterizedClass.class,
                Collections.emptySet(),
                SerializableMode.NONE,
                null,
                constructorArgs
        );
        InternalMockHandler<?> handler = createInternalMockHandlerStub(settings);

        ParameterizedClass mock = maker.createMock(settings, handler);
        assertNotNull("Mock instance must not be null", mock);

        Field messageField = ParameterizedClass.class.getDeclaredField("message");
        messageField.setAccessible(true);
        Object fieldValue = messageField.get(mock);

        assertEquals("Parameterized mock must have constructor executed and field set",
                "expected_constructor_argument", fieldValue);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = MockitoException.class, timeout = 4000)
    public void testCreateMockAcrossClassLoadersSerializationThrowsException() {
        ByteBuddyMockMaker maker = new ByteBuddyMockMaker();
        MockCreationSettings<SampleClass> settings = createSettingsStub(
                SampleClass.class, Collections.emptySet(), SerializableMode.ACROSS_CLASSLOADERS, null, null);
        InternalMockHandler<?> handler = createInternalMockHandlerStub(settings);

        maker.createMock(settings, handler);
    }

    @Test(timeout = 4000)
    public void testCreateMockAcrossClassloadersExceptionMessage() {
        ByteBuddyMockMaker maker = new ByteBuddyMockMaker();
        MockCreationSettings<SampleClass> settings = createSettingsStub(
                SampleClass.class, Collections.emptySet(), SerializableMode.ACROSS_CLASSLOADERS, null, null);
        InternalMockHandler<?> handler = createInternalMockHandlerStub(settings);

        try {
            maker.createMock(settings, handler);
            fail("Expected MockitoException for ACROSS_CLASSLOADERS serialization");
        } catch (MockitoException e) {
            assertTrue("Exception message must detail serialization across classloaders limitation",
                    e.getMessage().contains("Serialization across classloaders not yet supported with ByteBuddyMockMaker"));
        }
    }

    @Test(timeout = 4000)
    public void testNonInternalMockHandlerInCreateMockThrowsException() {
        ByteBuddyMockMaker maker = new ByteBuddyMockMaker();
        MockCreationSettings<SampleClass> settings = createSettingsStub(
                SampleClass.class, Collections.emptySet(), SerializableMode.NONE, null, null);
        MockHandler foreignHandler = createForeignMockHandlerStub();

        try {
            maker.createMock(settings, foreignHandler);
            fail("Expected MockitoException when handler is not an InternalMockHandler");
        } catch (MockitoException e) {
            assertTrue("Exception must inform that custom MockHandler implementations are not supported",
                    e.getMessage().contains("At the moment you cannot provide own implementations of MockHandler"));
        }
    }

    @Test(timeout = 4000)
    public void testNonInternalMockHandlerInResetMockThrowsException() {
        ByteBuddyMockMaker maker = new ByteBuddyMockMaker();
        MockCreationSettings<SampleClass> settings = createSettingsStub(
                SampleClass.class, Collections.emptySet(), SerializableMode.NONE, null, null);
        InternalMockHandler<?> validHandler = createInternalMockHandlerStub(settings);
        SampleClass mock = maker.createMock(settings, validHandler);

        MockHandler foreignHandler = createForeignMockHandlerStub();
        try {
            maker.resetMock(mock, foreignHandler, settings);
            fail("Expected MockitoException when resetMock handler is not an InternalMockHandler");
        } catch (MockitoException e) {
            assertTrue("Exception must inform that custom MockHandler is not supported",
                    e.getMessage().contains("At the moment you cannot provide own implementations of MockHandler"));
        }
    }

    @Test(timeout = 4000)
    public void testClassCastExceptionCaughtAndDescribedInCreateMock() {
        ByteBuddyMockMaker maker = new ByteBuddyMockMaker();
        final AtomicInteger callCount = new AtomicInteger(0);

        @SuppressWarnings("unchecked")
        MockCreationSettings<SampleClass> oscillatingSettings = (MockCreationSettings<SampleClass>) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{MockCreationSettings.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        String name = method.getName();
                        if ("getTypeToMock".equals(name)) {
                            // First call during cachingMockBytecodeGenerator.get() uses SampleClass.class
                            // Subsequent call during ensureMockIsAssignableToMockedType returns String.class, forcing ClassCastException
                            return callCount.getAndIncrement() == 0 ? SampleClass.class : String.class;
                        }
                        if ("getExtraInterfaces".equals(name)) return Collections.emptySet();
                        if ("getSerializableMode".equals(name)) return SerializableMode.NONE;
                        if ("isStripAnnotations".equals(name)) return false;
                        if ("equals".equals(name)) return proxy == args[0];
                        if ("hashCode".equals(name)) return System.identityHashCode(proxy);
                        return null;
                    }
                }
        );

        InternalMockHandler<?> handler = createInternalMockHandlerStub(oscillatingSettings);

        try {
            maker.createMock(oscillatingSettings, handler);
            fail("Expected MockitoException wrapping ClassCastException");
        } catch (MockitoException e) {
            String msg = e.getMessage();
            assertTrue("Message must indicate ClassCastException occurred",
                    msg.contains("ClassCastException occurred while creating the mockito mock"));
            assertTrue("Message must describe class to mock", msg.contains("class to mock :"));
            assertTrue("Message must describe created class", msg.contains("created class :"));
            assertTrue("Message must describe proxy instance class", msg.contains("proxy instance class :"));
            assertTrue("Message must describe instantiator", msg.contains("instance creation by :"));
            assertNotNull("Cause must be ClassCastException", e.getCause());
            assertTrue("Cause must be ClassCastException", e.getCause() instanceof ClassCastException);
        }
    }

    @Test(timeout = 4000)
    public void testInstantiationExceptionCaughtAndDescribedInCreateMock() throws Exception {
        ByteBuddyMockMaker maker = new ByteBuddyMockMaker();

        // Inject a failing ClassInstantiator via reflection
        Field instantiatorField = ByteBuddyMockMaker.class.getDeclaredField("classInstantiator");
        instantiatorField.setAccessible(true);
        ClassInstantiator originalInstantiator = (ClassInstantiator) instantiatorField.get(maker);

        try {
            ClassInstantiator failingInstantiator = new ClassInstantiator() {
                @Override
                public <T> T instantiate(Class<T> cls) {
                    throw new InstantiationException("Injected Instantiation Failure", new RuntimeException("Root cause"));
                }
            };
            instantiatorField.set(maker, failingInstantiator);

            MockCreationSettings<SampleClass> settings = createSettingsStub(
                    SampleClass.class, Collections.emptySet(), SerializableMode.NONE, null, null);
            InternalMockHandler<?> handler = createInternalMockHandlerStub(settings);

            maker.createMock(settings, handler);
            fail("Expected MockitoException when instantiator throws InstantiationException");
        } catch (MockitoException expected) {
            assertTrue("Exception message must detail failure to create mock instance",
                    expected.getMessage().contains("Unable to create mock instance of type 'SampleClass'"));
            assertNotNull("Cause must be InstantiationException", expected.getCause());
            assertTrue("Cause must be InstantiationException", expected.getCause() instanceof InstantiationException);
        } finally {
            instantiatorField.set(maker, originalInstantiator);
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle, Internal Methods & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testDescribeClassPrivateMethodsWithNullAndNonNull() throws Exception {
        Method describeClassMethod = ByteBuddyMockMaker.class.getDeclaredMethod("describeClass", Class.class);
        describeClassMethod.setAccessible(true);

        Method describeObjectMethod = ByteBuddyMockMaker.class.getDeclaredMethod("describeClass", Object.class);
        describeObjectMethod.setAccessible(true);

        // Branch 1: type == null
        assertEquals("null", describeClassMethod.invoke(null, (Class<?>) null));

        // Branch 2: type != null
        String classDesc = (String) describeClassMethod.invoke(null, SampleClass.class);
        assertNotNull(classDesc);
        assertTrue("Description must contain canonical name", classDesc.contains(SampleClass.class.getCanonicalName()));
        assertTrue("Description must contain classloader info", classDesc.contains("loaded by classloader"));

        // Branch 3: instance == null
        assertEquals("null", describeObjectMethod.invoke(null, (Object) null));

        // Branch 4: instance != null
        String instanceDesc = (String) describeObjectMethod.invoke(null, new SampleClass());
        assertNotNull(instanceDesc);
        assertTrue("Instance description must describe its concrete class",
                instanceDesc.contains(SampleClass.class.getCanonicalName()));
    }

    @Test(timeout = 4000)
    public void testInitializeClassInstantiatorReflection() throws Exception {
        Method initMethod = ByteBuddyMockMaker.class.getDeclaredMethod("initializeClassInstantiator");
        initMethod.setAccessible(true);

        Object instantiator = initMethod.invoke(null);
        assertNotNull("initializeClassInstantiator must return non-null ClassInstantiator", instantiator);
        assertTrue("Returned instance must implement ClassInstantiator",
                instantiator instanceof ClassInstantiator);
    }

    @Test(timeout = 4000)
    public void testEnsureMockIsAssignableToMockedTypeDirect() throws Exception {
        ByteBuddyMockMaker maker = new ByteBuddyMockMaker();
        MockCreationSettings<SampleClass> settings = createSettingsStub(
                SampleClass.class, Collections.emptySet(), SerializableMode.NONE, null, null);
        SampleClass instance = new SampleClass();

        Method ensureMethod = ByteBuddyMockMaker.class.getDeclaredMethod(
                "ensureMockIsAssignableToMockedType", MockCreationSettings.class, Object.class);
        ensureMethod.setAccessible(true);

        Object result = ensureMethod.invoke(maker, settings, instance);
        assertSame("Assigned instance must be identical to input mock", instance, result);
    }

    @Test(timeout = 4000)
    public void testAsInternalMockHandlerWithValidHandler() throws Exception {
        Method asInternalMethod = ByteBuddyMockMaker.class.getDeclaredMethod(
                "asInternalMockHandler", MockHandler.class);
        asInternalMethod.setAccessible(true);

        MockCreationSettings<SampleClass> settings = createSettingsStub(
                SampleClass.class, Collections.emptySet(), SerializableMode.NONE, null, null);
        InternalMockHandler<?> internalHandler = createInternalMockHandlerStub(settings);

        Object result = asInternalMethod.invoke(null, internalHandler);
        assertSame("Valid InternalMockHandler must be returned untouched", internalHandler, result);
    }
}