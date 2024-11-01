package com.craftens.totalreport.junit5;

import com.craftens.totalreport.agent.DefaultTestStatuses;
import com.craftens.totalreport.agent.TotalReportAgent;
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
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.junit.jupiter.api.extension.TestInstanceFactoryContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.junit.jupiter.api.extension.TestInstancePreConstructCallback;
import org.junit.jupiter.api.extension.TestInstancePreDestroyCallback;
import org.junit.jupiter.api.extension.TestWatcher;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class TotalReportExtension implements LifecycleMethodExecutionExceptionHandler, TestExecutionExceptionHandler, InvocationInterceptor, TestInstancePreDestroyCallback, TestWatcher, AfterEachCallback, BeforeEachCallback, AfterTestExecutionCallback, BeforeTestExecutionCallback, BeforeAllCallback, AfterAllCallback, TestInstancePreConstructCallback, TestInstancePostProcessor {
    private final TotalReportAgent agent;
    private final Integer reportId;
    private final Integer launchId;
    private final ConcurrentHashMap<Class<?>, Integer> testContexts = new ConcurrentHashMap<>();

    public TotalReportExtension() {
        String totalReportUrl = System.getProperty(PropertiesNames.TOTAL_REPORT_URL);
        if (totalReportUrl == null || totalReportUrl.isEmpty()) {
            throw new IllegalArgumentException("Total Report URL is not set. Set the property " + PropertiesNames.TOTAL_REPORT_URL);
        }

        agent = new TotalReportAgent(totalReportUrl);

        // FIXME {@link TotalReportExtension} can have multiple instances,
        //  but the report and launch IDs should be the same for all instances
        String reportIdFromProperty = System.getProperty(PropertiesNames.REPORT_ID);
        if (reportIdFromProperty == null || reportIdFromProperty.isEmpty()) {
            String reportTitleFromProperty = System.getProperty(PropertiesNames.REPORT_TITLE);
            if (reportTitleFromProperty == null || reportTitleFromProperty.isEmpty()) {
                throw new IllegalArgumentException("Neither Report ID nor Report title are not set. Set the property " +
                        PropertiesNames.REPORT_ID + " or " + PropertiesNames.REPORT_TITLE);
            }
            this.reportId = agent.createReport(reportTitleFromProperty);
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

            launchId = agent.launchCreatedAndStarted(this.reportId, launchTitleFromProperty, timestamp, timestamp);
        } else {
            this.launchId = Integer.parseInt(launchIdFromProperty);
        }

        log.trace("Total report extension initialized with report ID {} and launch ID {}", reportId, launchId);
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        log.trace("Context root unique ID: {}", context.getRoot().getUniqueId());
        log.trace("Context unique ID: {}", context.getUniqueId());

        log.trace("Before all tests {}, {}, {}, {}", context.getTestClass(), context.getTestMethod(), context.getElement(),
                context.getParent());
        OffsetDateTime timestamp = OffsetDateTime.now();

        Class<?> testContextClass = context.getTestClass().get();
        String testContextName = testContextClass.getName();


        Integer tesContextId = agent.contextCreatedAndStarted(launchId, testContextName, timestamp, timestamp);

        testContexts.put(testContextClass, tesContextId);

//        TotalReportManager.testContextStarted(testContextClass);
    }

    @Override
    public void interceptBeforeAllMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        log.trace("Intercepting before all method {}, {}, {}", invocation, invocationContext.getExecutable(), extensionContext);
//        beforeAllTestStarted(invocationContext.getExecutable().getDeclaringClass(), invocationContext.getExecutable());

        OffsetDateTime timestamp = OffsetDateTime.now();
        String name = invocationContext.getExecutable().getDeclaringClass().getName() + "#" + invocationContext.getExecutable().getName();
        Integer testContextId = testContexts.get(invocationContext.getExecutable().getDeclaringClass());
        Integer beforeAllId = agent.beforeTestCreatedAndStarted(launchId, testContextId, name, timestamp, timestamp);

        try {
            InvocationInterceptor.super.interceptBeforeAllMethod(invocation, invocationContext, extensionContext);
        } catch (Throwable throwable) {
            OffsetDateTime finishedTimestamp = OffsetDateTime.now();
            if (throwable instanceof AssertionError) {
                agent.beforeTestFinished(beforeAllId, finishedTimestamp, DefaultTestStatuses.PRODUCT_BUG);
            } else {
                agent.beforeTestFinished(beforeAllId, finishedTimestamp, DefaultTestStatuses.AUTOMATION_BUG);
            }
            throw throwable;
        }

        OffsetDateTime finishedTimestamp = OffsetDateTime.now();

        agent.beforeTestFinished(beforeAllId, finishedTimestamp, DefaultTestStatuses.SUCCESSFUL);
    }

    @Override
    public void handleBeforeAllMethodExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        log.trace("Handling before all method execution exception");
        // FIXME In theory somebody can override this method in different extension to handle exceptions.
        //  In this case the exception will be sent to Total Report in {@link TotalReportExtension#interceptBeforeAllMethod},
        //  but handled here to change it or remove. As a result can be inconsistency between sent status and status after handle.

//        TotalReportManager.beforeAllFailed(context.getTestClass().get(), throwable);
        LifecycleMethodExecutionExceptionHandler.super.handleBeforeAllMethodExecutionException(context, throwable);
    }

    @Override
    public void preConstructTestInstance(TestInstanceFactoryContext factoryContext, ExtensionContext context) throws Exception {
//        OffsetDateTime beforeAllFinishTime = OffsetDateTime.now();

        log.trace("Test instance pre-construct: {}", factoryContext.getTestClass());

//        TotalReportManager.allBeforeAllFinished(context.getTestClass().get(), beforeAllFinishTime, DefaultTestStatuses.SUCCESSFUL);
    }

    @Override
    public <T> T interceptTestClassConstructor(Invocation<T> invocation, ReflectiveInvocationContext<Constructor<T>> invocationContext, ExtensionContext extensionContext) throws Throwable {
        log.trace("Intercepting test class constructor");

        return InvocationInterceptor.super.interceptTestClassConstructor(invocation, invocationContext, extensionContext);
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
        log.trace("Test instance post-processor: {}", testInstance);
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        log.trace("Context root unique ID: {}", context.getRoot().getUniqueId());

        log.trace("Context unique ID: {}", context.getUniqueId());

        log.trace("Before each test: {}", context.getTestMethod());
    }

    @Override
    public void interceptBeforeEachMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        log.trace("Intercepting before each method");

//        beforeEachStarted(Thread.currentThread(), invocationContext.getExecutable().getDeclaringClass(), invocationContext.getExecutable());

        OffsetDateTime timestamp = OffsetDateTime.now();
        String name = invocationContext.getExecutable().getDeclaringClass().getName() + "#" + invocationContext.getExecutable().getName();
        Integer testContextId = testContexts.get(invocationContext.getExecutable().getDeclaringClass());

        Integer beforeAllId = agent.beforeTestCreatedAndStarted(launchId, testContextId, name, timestamp, timestamp);

        try {
            InvocationInterceptor.super.interceptBeforeEachMethod(invocation, invocationContext, extensionContext);
        } catch (Throwable throwable) {
            OffsetDateTime finishedTimestamp = OffsetDateTime.now();
            if (throwable instanceof AssertionError) {
                agent.beforeTestFinished(beforeAllId, finishedTimestamp, DefaultTestStatuses.PRODUCT_BUG);
            } else {
                agent.beforeTestFinished(beforeAllId, finishedTimestamp, DefaultTestStatuses.AUTOMATION_BUG);
            }
            throw throwable;
        }

        OffsetDateTime finishedTimestamp = OffsetDateTime.now();

        agent.beforeTestFinished(beforeAllId, finishedTimestamp, DefaultTestStatuses.SUCCESSFUL);
    }

    @Override
    public void handleBeforeEachMethodExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        log.trace("Handling before each method execution exception");

//        TotalReportManager.beforeEachFailed(Thread.currentThread(), context.getTestClass().get(), throwable);

        LifecycleMethodExecutionExceptionHandler.super.handleBeforeEachMethodExecutionException(context, throwable);
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) throws Exception {
        log.trace("Context root unique ID: {}", context.getRoot().getUniqueId());
        log.trace("Context unique ID: {}", context.getUniqueId());
//        OffsetDateTime beforeEachFinishTimestamp = OffsetDateTime.now();

        log.trace("Before test execution: {}", context.getTestMethod());

//        TotalReportManager.beforeEachFinished(Thread.currentThread(), context.getTestClass().get(), beforeEachFinishTimestamp, DefaultTestStatuses.SUCCESSFUL);
    }

    @Override
    public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        log.trace("Intercepting test method");

//        TotalReportManager.testStarted(Thread.currentThread(), invocationContext.getExecutable().getDeclaringClass(), invocationContext.getExecutable());

        OffsetDateTime timestamp = OffsetDateTime.now();
        String name = invocationContext.getExecutable().getDeclaringClass().getName() + "#" + invocationContext.getExecutable().getName();
        Integer testContextId = testContexts.get(invocationContext.getExecutable().getDeclaringClass());
        Integer beforeAllId = agent.testCreatedAndStarted(launchId, testContextId, name, timestamp, timestamp);

        try {
            InvocationInterceptor.super.interceptTestMethod(invocation, invocationContext, extensionContext);
        } catch (Throwable throwable) {
            OffsetDateTime finishedTimestamp = OffsetDateTime.now();
            if (throwable instanceof AssertionError) {
                agent.testFinished(beforeAllId, finishedTimestamp, DefaultTestStatuses.PRODUCT_BUG);
            } else {
                agent.testFinished(beforeAllId, finishedTimestamp, DefaultTestStatuses.AUTOMATION_BUG);
            }
            throw throwable;
        }

        OffsetDateTime finishedTimestamp = OffsetDateTime.now();

        agent.testFinished(beforeAllId, finishedTimestamp, DefaultTestStatuses.SUCCESSFUL);


//        InvocationInterceptor.super.interceptTestMethod(invocation, invocationContext, extensionContext);
    }

    @Override
    public void afterTestExecution(ExtensionContext context) throws Exception {
        log.trace("After test execution: {}", context.getTestMethod());
    }

    @Override
    public void interceptAfterEachMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        log.trace("Intercepting after each method");

        OffsetDateTime timestamp = OffsetDateTime.now();
        String name = invocationContext.getExecutable().getDeclaringClass().getName() + "#" + invocationContext.getExecutable().getName();
        Integer testContextId = testContexts.get(invocationContext.getExecutable().getDeclaringClass());

        Integer afterEachId = agent.afterTestCreatedAndStarted(launchId, testContextId, name, timestamp, timestamp);

        try {
            InvocationInterceptor.super.interceptAfterEachMethod(invocation, invocationContext, extensionContext);
        } catch (Throwable throwable) {
            OffsetDateTime finishedTimestamp = OffsetDateTime.now();
            if (throwable instanceof AssertionError) {
                agent.afterTestFinished(afterEachId, finishedTimestamp, DefaultTestStatuses.PRODUCT_BUG);
            } else {
                agent.afterTestFinished(afterEachId, finishedTimestamp, DefaultTestStatuses.AUTOMATION_BUG);
            }
            throw throwable;
        }

        OffsetDateTime finishedTimestamp = OffsetDateTime.now();

        agent.afterTestFinished(afterEachId, finishedTimestamp, DefaultTestStatuses.SUCCESSFUL);

//        TotalReportManager.afterEachStarted(Thread.currentThread(), invocationContext.getExecutable().getDeclaringClass(), invocationContext.getExecutable());

//        InvocationInterceptor.super.interceptAfterEachMethod(invocation, invocationContext, extensionContext);
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        log.trace("After each test: {}", context.getTestMethod());
    }

    @Override
    public void handleAfterEachMethodExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        log.trace("Handling after each method execution exception");

//        TotalReportManager.afterEachFailed(Thread.currentThread(), context.getTestClass().get(), throwable);

        LifecycleMethodExecutionExceptionHandler.super.handleAfterEachMethodExecutionException(context, throwable);
    }

    @Override
    public void preDestroyTestInstance(ExtensionContext context) throws Exception {
        log.trace("Pre-destroy test instance: {}", context.getTestClass());
    }

    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        log.trace("Test disabled: {}", context.getTestMethod());

//        TotalReportManager.testFinished(Thread.currentThread(), context.getTestClass().get(), context.getTestMethod().get(),
//                DefaultTestStatuses.SKIPPED);

        TestWatcher.super.testDisabled(context, reason);
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        log.trace("Test successful: {}", context.getTestMethod());

//        TotalReportManager.testFinished(Thread.currentThread(), context.getTestClass().get(), context.getTestMethod().get(),
//                DefaultTestStatuses.SUCCESSFUL);

        TestWatcher.super.testSuccessful(context);
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        log.trace("Test aborted: {}", context.getTestMethod());

//        TotalReportManager.testFinished(Thread.currentThread(), context.getTestClass().get(), context.getTestMethod().get(),
//                DefaultTestStatuses.ABORTED);

        TestWatcher.super.testAborted(context, cause);
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
//        if (cause instanceof AssertionError) {
//            TotalReportManager.testFinished(Thread.currentThread(), context.getTestClass().get(), context.getTestMethod().get(),
//                    DefaultTestStatuses.PRODUCT_BUG);
//        } else {
//            TotalReportManager.testFinished(Thread.currentThread(), context.getTestClass().get(), context.getTestMethod().get(),
//                    DefaultTestStatuses.AUTOMATION_BUG);
//        }
        TestWatcher.super.testFailed(context, cause);
        log.trace("Test failed: {}", context.getTestMethod());
    }

    @Override
    public void interceptAfterAllMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        log.trace("Intercepting after all method");

        OffsetDateTime timestamp = OffsetDateTime.now();
        String name = invocationContext.getExecutable().getDeclaringClass().getName() + "#" + invocationContext.getExecutable().getName();
        Integer testContextId = testContexts.get(invocationContext.getExecutable().getDeclaringClass());

        Integer afterAllId = agent.afterTestCreatedAndStarted(launchId, testContextId, name, timestamp, timestamp);

        try {
            InvocationInterceptor.super.interceptAfterAllMethod(invocation, invocationContext, extensionContext);
        } catch (Throwable throwable) {
            OffsetDateTime finishedTimestamp = OffsetDateTime.now();
            if (throwable instanceof AssertionError) {
                agent.afterTestFinished(afterAllId, finishedTimestamp, DefaultTestStatuses.PRODUCT_BUG);
            } else {
                agent.afterTestFinished(afterAllId, finishedTimestamp, DefaultTestStatuses.AUTOMATION_BUG);
            }
            throw throwable;
        }

        OffsetDateTime finishedTimestamp = OffsetDateTime.now();

        agent.afterTestFinished(afterAllId, finishedTimestamp, DefaultTestStatuses.SUCCESSFUL);

//        TotalReportManager.afterAllStarted(invocationContext.getExecutable().getDeclaringClass(), invocationContext.getExecutable());
//        InvocationInterceptor.super.interceptAfterAllMethod(invocation, invocationContext, extensionContext);
    }

    @Override
    public void handleAfterAllMethodExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        log.trace("Handling after all method execution exception {} {}", context.getTestClass().get(), context.getTestMethod());

//        TotalReportManager.afterAllFailed(context.getTestClass().get(), throwable);
        LifecycleMethodExecutionExceptionHandler.super.handleAfterAllMethodExecutionException(context, throwable);
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
//        TotalReportManager.afterAllFinished(context.getTestClass().get(), DefaultTestStatuses.SUCCESSFUL);

        Integer testContextId = testContexts.get(context.getTestClass().get());

        agent.contextFinished(testContextId, OffsetDateTime.now());

//        TotalReportManager.testContextFinished(context.getTestClass().get());
        log.trace("After all tests {}, {}, {}, {}", context.getTestClass(), context.getTestMethod(), context.getElement(),
                context.getParent());
    }


    @Override
    public <T> T interceptTestFactoryMethod(Invocation<T> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        log.trace("Intercepting test factory method");
        return InvocationInterceptor.super.interceptTestFactoryMethod(invocation, invocationContext, extensionContext);
    }

    @Override
    public void interceptTestTemplateMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        log.trace("Intercepting test template method");
        InvocationInterceptor.super.interceptTestTemplateMethod(invocation, invocationContext, extensionContext);
    }

    @Override
    public void interceptDynamicTest(Invocation<Void> invocation, ExtensionContext extensionContext) throws Throwable {
        log.trace("Intercepting dynamic test");
        InvocationInterceptor.super.interceptDynamicTest(invocation, extensionContext);
    }

    @Override
    public void interceptDynamicTest(Invocation<Void> invocation, DynamicTestInvocationContext invocationContext, ExtensionContext extensionContext) throws Throwable {
        log.trace("Intercepting dynamic test");
        InvocationInterceptor.super.interceptDynamicTest(invocation, invocationContext, extensionContext);
    }

    /**
     * Handled in {@link TotalReportExtension#testFailed}.
     */
    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        log.trace("Handling test execution exception");
    }
}
