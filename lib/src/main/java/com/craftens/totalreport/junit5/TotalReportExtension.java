package com.craftens.totalreport.junit5;

import com.craftens.totalreport.client.TotalReportClient;
import com.craftens.totalreport.events.TotalReportEvents;
import com.craftens.totalreport.events.afterall.AfterAllCreatedAndStarted;
import com.craftens.totalreport.events.afterall.AfterAllFinished;
import com.craftens.totalreport.events.aftereach.AfterEachCreatedAndStarted;
import com.craftens.totalreport.events.aftereach.AfterEachFinished;
import com.craftens.totalreport.events.beforeall.BeforeAllCreatedAndStarted;
import com.craftens.totalreport.events.beforeall.BeforeAllFinished;
import com.craftens.totalreport.events.beforeeach.BeforeEachCreatedAndStarted;
import com.craftens.totalreport.events.beforeeach.BeforeEachFinished;
import com.craftens.totalreport.events.context.TestContextCreatedAndStarted;
import com.craftens.totalreport.events.context.TestContextFinished;
import com.craftens.totalreport.events.test.TestCreatedAndStarted;
import com.craftens.totalreport.events.test.TestFinished;
import com.craftens.totalreport.events.test.TestSkipped;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.DynamicTestInvocationContext;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.LifecycleMethodExecutionExceptionHandler;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.jupiter.api.extension.TestInstanceFactoryContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.junit.jupiter.api.extension.TestInstancePreConstructCallback;
import org.junit.jupiter.api.extension.TestInstancePreDestroyCallback;
import org.junit.jupiter.api.extension.TestWatcher;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.util.Optional;

@Slf4j
public class TotalReportExtension implements LifecycleMethodExecutionExceptionHandler, InvocationInterceptor, TestInstancePreDestroyCallback, TestWatcher, AfterEachCallback, BeforeEachCallback, AfterTestExecutionCallback, BeforeTestExecutionCallback, BeforeAllCallback, AfterAllCallback, TestInstancePreConstructCallback, TestInstancePostProcessor/* , TestExecutionExceptionHandler */ {
    private final TotalReportClient client;
    private final Integer reportId;
    private final Integer launchId;
    private final TotalReportEvents events;

    public TotalReportExtension() {
        String totalReportUrl = System.getProperty(PropertiesNames.TOTAL_REPORT_URL);
        if (totalReportUrl == null || totalReportUrl.isEmpty()) {
            throw new IllegalArgumentException("Total Report URL is not set. Set the property " + PropertiesNames.TOTAL_REPORT_URL);
        }

        client = new TotalReportClient(totalReportUrl);

        // FIXME {@link TotalReportExtension} can have multiple instances,
        //  but the report and launch IDs should be the same for all instances
        String reportIdFromProperty = System.getProperty(PropertiesNames.REPORT_ID);
        if (reportIdFromProperty == null || reportIdFromProperty.isEmpty()) {
            String reportTitleFromProperty = System.getProperty(PropertiesNames.REPORT_TITLE);
            if (reportTitleFromProperty == null || reportTitleFromProperty.isEmpty()) {
                throw new IllegalArgumentException("Neither Report ID nor Report title are not set. Set the property " +
                        PropertiesNames.REPORT_ID + " or " + PropertiesNames.REPORT_TITLE);
            }
            this.reportId = client.createReport(reportTitleFromProperty);
        } else {
            this.reportId = Integer.parseInt(reportIdFromProperty);
        }

        String launchIdFromProperty = System.getProperty(PropertiesNames.LAUNCH_ID);
        if (launchIdFromProperty == null) {
            String launchTitleFromProperty = System.getProperty(PropertiesNames.LAUNCH_TITLE);

            if (launchTitleFromProperty == null || launchTitleFromProperty.isEmpty()) {
                throw new IllegalArgumentException("Neither Launch ID nor Launch title are not set. Set the property " +
                        PropertiesNames.LAUNCH_ID + " or " + PropertiesNames.LAUNCH_TITLE);
            }

            OffsetDateTime timestamp = OffsetDateTime.now();

            launchId = client.launchCreatedAndStarted(this.reportId, launchTitleFromProperty, timestamp, timestamp);
        } else {
            this.launchId = Integer.parseInt(launchIdFromProperty);
        }

        events = new TotalReportEvents(client);

        log.trace("Total report extension initialized with report ID {} and launch ID {}", reportId, launchId);
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        log.trace("beforeAll, unique ID: {}", context.getUniqueId());

        OffsetDateTime timestamp = OffsetDateTime.now();

        events.getTestContext().onCreatedAndStarted(new TestContextCreatedAndStarted(launchId, context.getTestClass().get(), timestamp, timestamp));
    }

    @Override
    public void interceptBeforeAllMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        log.trace("interceptBeforeAllMethod, unique ID: {}", extensionContext.getUniqueId());

        OffsetDateTime timestamp = OffsetDateTime.now();
        events.getBeforeAll().onCreatedAndStarted(new BeforeAllCreatedAndStarted(launchId, Thread.currentThread(), extensionContext, invocationContext, timestamp, timestamp));

        try {
            InvocationInterceptor.super.interceptBeforeAllMethod(invocation, invocationContext, extensionContext);
        } catch (Throwable throwable) {
            OffsetDateTime finishedTimestamp = OffsetDateTime.now();
            events.getBeforeAll().onFinished(new BeforeAllFinished(Thread.currentThread(), extensionContext, finishedTimestamp, throwable));

            throw throwable;
        }

        OffsetDateTime finishedTimestamp = OffsetDateTime.now();
        events.getBeforeAll().onFinished(new BeforeAllFinished(Thread.currentThread(), extensionContext, finishedTimestamp, null));
    }

    @Override
    public void handleBeforeAllMethodExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        log.trace("handleBeforeAllMethodExecutionException, unique ID: {}", context.getUniqueId());
        // FIXME In theory somebody can override this method in different extension to handle exceptions.
        //  In this case the exception will be sent to Total Report in {@link TotalReportExtension#interceptBeforeAllMethod},
        //  but handled here to change it or remove. As a result can be inconsistency between sent status and status after handle.

        LifecycleMethodExecutionExceptionHandler.super.handleBeforeAllMethodExecutionException(context, throwable);
    }

    @Override
    public void preConstructTestInstance(TestInstanceFactoryContext factoryContext, ExtensionContext context) throws Exception {
        log.trace("preConstructTestInstance, unique ID: {}", context.getUniqueId());
    }

    @Override
    public <T> T interceptTestClassConstructor(Invocation<T> invocation, ReflectiveInvocationContext<Constructor<T>> invocationContext, ExtensionContext extensionContext) throws Throwable {
        log.trace("interceptTestClassConstructor, unique ID: {}", extensionContext.getUniqueId());

        return InvocationInterceptor.super.interceptTestClassConstructor(invocation, invocationContext, extensionContext);
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
        log.trace("postProcessTestInstance, unique ID: {}", context.getUniqueId());
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        log.trace("beforeEach, unique ID: {}", context.getUniqueId());
    }

    @Override
    public void interceptBeforeEachMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        log.trace("interceptBeforeEachMethod, unique ID: {}", extensionContext.getUniqueId());

        OffsetDateTime createdAndStarted = OffsetDateTime.now();
        events.getBeforeEach().onCreatedAndStarted(new BeforeEachCreatedAndStarted(launchId, Thread.currentThread(), extensionContext, invocationContext, createdAndStarted, createdAndStarted));

        try {
            InvocationInterceptor.super.interceptBeforeEachMethod(invocation, invocationContext, extensionContext);
        } catch (Throwable throwable) {
            OffsetDateTime finishedTimestamp = OffsetDateTime.now();
            events.getBeforeEach().onFinished(new BeforeEachFinished(Thread.currentThread(), extensionContext, finishedTimestamp, throwable));

            throw throwable;
        }

        OffsetDateTime finishedTimestamp = OffsetDateTime.now();
        events.getBeforeEach().onFinished(new BeforeEachFinished(Thread.currentThread(), extensionContext, finishedTimestamp, null));
    }

    @Override
    public void handleBeforeEachMethodExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        log.trace("handleBeforeEachMethodExecutionException, unique ID: {}", context.getUniqueId());

        LifecycleMethodExecutionExceptionHandler.super.handleBeforeEachMethodExecutionException(context, throwable);
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) throws Exception {
        log.trace("beforeTestExecution, unique ID: {}", context.getUniqueId());
    }

    @Override
    public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        log.trace("interceptTestMethod, unique ID: {}", extensionContext.getUniqueId());

        OffsetDateTime timestamp = OffsetDateTime.now();

        events.getTest().onCreatedAndStarted(new TestCreatedAndStarted(launchId, extensionContext, invocationContext, timestamp));

        InvocationInterceptor.super.interceptTestMethod(invocation, invocationContext, extensionContext);
    }

    @Override
    public void afterTestExecution(ExtensionContext context) throws Exception {
        OffsetDateTime timestamp = OffsetDateTime.now();

        log.trace("afterTestExecution, unique ID: {}", context.getUniqueId());

        events.getTest().onFinished(new TestFinished(context, timestamp));
    }

    @Override
    public void interceptAfterEachMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        log.trace("interceptAfterEachMethod, unique ID: {}", extensionContext.getUniqueId());

        OffsetDateTime timestamp = OffsetDateTime.now();
        events.getAfterEach().onCreatedAndStarted(new AfterEachCreatedAndStarted(launchId, Thread.currentThread(), extensionContext, invocationContext, timestamp, timestamp));

        try {
            InvocationInterceptor.super.interceptAfterEachMethod(invocation, invocationContext, extensionContext);
        } catch (Throwable throwable) {
            OffsetDateTime finishedTimestamp = OffsetDateTime.now();
            events.getAfterEach().onFinished(new AfterEachFinished(Thread.currentThread(), extensionContext, finishedTimestamp, throwable));

            throw throwable;
        }

        OffsetDateTime finishedTimestamp = OffsetDateTime.now();
        events.getAfterEach().onFinished(new AfterEachFinished(Thread.currentThread(), extensionContext, finishedTimestamp, null));
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        log.trace("afterEach, unique ID: {}", context.getUniqueId());
    }

    @Override
    public void handleAfterEachMethodExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        log.trace("handleAfterEachMethodExecutionException, unique ID: {}", context.getUniqueId());

        LifecycleMethodExecutionExceptionHandler.super.handleAfterEachMethodExecutionException(context, throwable);
    }

    @Override
    public void preDestroyTestInstance(ExtensionContext context) throws Exception {
        log.trace("preDestroyTestInstance, unique ID: {}", context.getUniqueId());
    }

    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        log.trace("testDisabled, unique ID: {}", context.getUniqueId());

        OffsetDateTime timestamp = OffsetDateTime.now();
        events.getTest().onSkipped(new TestSkipped(launchId, context, timestamp));

        TestWatcher.super.testDisabled(context, reason);
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        log.trace("testSuccessful, unique ID: {}", context.getUniqueId());

        TestWatcher.super.testSuccessful(context);
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        log.trace("testAborted, unique ID: {}", context.getUniqueId());

        TestWatcher.super.testAborted(context, cause);
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        log.trace("testFailed, unique ID: {}", context.getUniqueId());

        TestWatcher.super.testFailed(context, cause);
    }

    @Override
    public void interceptAfterAllMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        log.trace("interceptAfterAllMethod, unique ID: {}", extensionContext.getUniqueId());

        OffsetDateTime timestamp = OffsetDateTime.now();
        events.getAfterAll().onCreatedAndStarted(new AfterAllCreatedAndStarted(launchId, Thread.currentThread(), extensionContext, invocationContext, timestamp, timestamp));

        try {
            InvocationInterceptor.super.interceptAfterAllMethod(invocation, invocationContext, extensionContext);
        } catch (Throwable throwable) {
            OffsetDateTime finishedTimestamp = OffsetDateTime.now();
            events.getAfterAll().onFinished(new AfterAllFinished(Thread.currentThread(), extensionContext, finishedTimestamp, throwable));

            throw throwable;
        }

        OffsetDateTime finishedTimestamp = OffsetDateTime.now();

        events.getAfterAll().onFinished(new AfterAllFinished(Thread.currentThread(), extensionContext, finishedTimestamp, null));
    }

    @Override
    public void handleAfterAllMethodExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        log.trace("handleAfterAllMethodExecutionException, unique ID: {}", context.getUniqueId());

        LifecycleMethodExecutionExceptionHandler.super.handleAfterAllMethodExecutionException(context, throwable);
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        log.trace("afterAll, unique ID: {}", context.getUniqueId());

        OffsetDateTime timestamp = OffsetDateTime.now();
        events.getTestContext().onFinished(new TestContextFinished(context.getTestClass().get(), timestamp));

        events.onComplete();
        events.waitCompleteAndShutdown();
    }

    @Override
    public <T> T interceptTestFactoryMethod(Invocation<T> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        log.trace("interceptTestFactoryMethod, unique ID: {}", extensionContext.getUniqueId());

        return InvocationInterceptor.super.interceptTestFactoryMethod(invocation, invocationContext, extensionContext);
    }

    @Override
    public void interceptTestTemplateMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        log.trace("interceptTestTemplateMethod, unique ID: {}", extensionContext.getUniqueId());

        InvocationInterceptor.super.interceptTestTemplateMethod(invocation, invocationContext, extensionContext);
    }

    @Override
    public void interceptDynamicTest(Invocation<Void> invocation, ExtensionContext extensionContext) throws Throwable {
        log.trace("interceptDynamicTest, unique ID: {}", extensionContext.getUniqueId());

        InvocationInterceptor.super.interceptDynamicTest(invocation, extensionContext);
    }

    @Override
    public void interceptDynamicTest(Invocation<Void> invocation, DynamicTestInvocationContext invocationContext, ExtensionContext extensionContext) throws Throwable {
        log.trace("interceptDynamicTest, unique ID: {}", extensionContext.getUniqueId());

        InvocationInterceptor.super.interceptDynamicTest(invocation, invocationContext, extensionContext);
    }

//    /**
//     * Handled in {@link TotalReportExtension#testFailed}.
//     */
//    @Override
//    public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
//        log.trace("Handling test execution exception");
//    }
}
