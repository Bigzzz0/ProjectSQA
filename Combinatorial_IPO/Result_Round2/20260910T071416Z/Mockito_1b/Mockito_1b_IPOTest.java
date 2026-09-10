package org.mockito.internal.invocation;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.List;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.internal.matchers.AnyVararg;
import org.mockito.internal.matchers.CapturingMatcher;
import org.mockito.internal.matchers.LocalizedMatcher;
import org.mockito.invocation.Invocation;
import org.mockitousage.IMethods;
import org.mockitoutil.TestBase;
import static java.util.Arrays.asList;

/** Generated from approved defect-focused scenarios using native IPO. */
public class Mockito_1b_IPOTest extends TestBase {

@Mock private IMethods mock;

    @Test(timeout = 4000)
    public void test_capture_varargs_001() throws Exception {
        // Native IPO combination: arguments=empty, matcher=any_vararg, capture_count=once
        mock.varargs(new String[0]);
        Invocation invocation = getLastInvocation();
        Matcher matcher = AnyVararg.ANY_VARARG;
        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, (List) asList(matcher));
        for (int i = 0; i < 1; i++) { invocationMatcher.captureArgumentsFrom(invocation); }
        assertNotNull(invocationMatcher);
    }

    @Test(timeout = 4000)
    public void test_capture_varargs_002() throws Exception {
        // Native IPO combination: arguments=empty, matcher=captor, capture_count=twice
        mock.varargs(new String[0]);
        Invocation invocation = getLastInvocation();
        Matcher matcher = new LocalizedMatcher(new CapturingMatcher());
        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, (List) asList(matcher));
        for (int i = 0; i < 2; i++) { invocationMatcher.captureArgumentsFrom(invocation); }
        assertNotNull(invocationMatcher);
    }

    @Test(timeout = 4000)
    public void test_capture_varargs_003() throws Exception {
        // Native IPO combination: arguments=two, matcher=any_vararg, capture_count=twice
        mock.varargs(new String[] { "a", "b" });
        Invocation invocation = getLastInvocation();
        Matcher matcher = AnyVararg.ANY_VARARG;
        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, (List) asList(matcher));
        for (int i = 0; i < 2; i++) { invocationMatcher.captureArgumentsFrom(invocation); }
        assertNotNull(invocationMatcher);
    }

    @Test(timeout = 4000)
    public void test_capture_varargs_004() throws Exception {
        // Native IPO combination: arguments=two, matcher=captor, capture_count=once
        mock.varargs(new String[] { "a", "b" });
        Invocation invocation = getLastInvocation();
        Matcher matcher = new LocalizedMatcher(new CapturingMatcher());
        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, (List) asList(matcher));
        for (int i = 0; i < 1; i++) { invocationMatcher.captureArgumentsFrom(invocation); }
        assertNotNull(invocationMatcher);
    }

}
