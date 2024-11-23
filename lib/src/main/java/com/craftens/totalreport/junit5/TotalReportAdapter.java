package com.craftens.totalreport.junit5;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

@Slf4j
public class TotalReportAdapter {
    static ConcurrentHashMap<String, Integer> testClassToTestContextId = new ConcurrentHashMap<>();
    static ConcurrentHashMap<String, Integer> beforeAllMethodToTestId = new ConcurrentHashMap<>();
    static ConcurrentHashMap<String, Integer> threadToLastBeforeAllId = new ConcurrentHashMap<>();
    static ConcurrentHashMap<String, Integer> threadToLastBeforeEachId = new ConcurrentHashMap<>();
    static ConcurrentHashMap<String, Integer> threadToLastAfterAllId = new ConcurrentHashMap<>();
    static ConcurrentHashMap<String, Integer> threadToLastAfterEachId = new ConcurrentHashMap<>();
    static ConcurrentHashMap<String, Integer> testMethodToTestId = new ConcurrentHashMap<>();

    public static void putTestContextId(Class<?> testClass, Integer testContextId) {
        log.trace("Cache test context id for test class: {} -> {}", testClass.getName(), testContextId);

        testClassToTestContextId.put(testClass.getName(), testContextId);
    }

    public static void removeTestContextId(Class<?> testClass) {
        log.trace("Remove test context id for test class: {}", testClass.getName());

        testClassToTestContextId.remove(testClass.getName());
    }

    public static Integer getTestContextId(Class<?> testClass) {
        log.trace("Get test context id for test class: {}", testClass.getName());

        return testClassToTestContextId.get(testClass.getName());
    }

    public static void putTestId(ExtensionContext testMethodExtensionContext, Integer testId) {
        putTestId(testMethodExtensionContext.getUniqueId(), testId);
    }

    public static void putTestId(String testMethodUniqueId, Integer testId) {
        log.trace("Cache test id for test method: {} -> {}", testMethodUniqueId, testId);

        testMethodToTestId.put(testMethodUniqueId, testId);
    }

    public static void putBeforeAllId(ReflectiveInvocationContext<Method> invocationContext, Integer testId) {
        log.trace("Cache before all id for before all method: {} -> {}", invocationContext.getExecutable().getName(), testId);

        beforeAllMethodToTestId.put(invocationContext.getExecutable().getName(), testId);
    }

    public static Integer getBeforeAllId(ReflectiveInvocationContext<Method> invocationContext) {
        log.trace("Get before all id for before all method: {}", invocationContext.getExecutable().getName());

        return beforeAllMethodToTestId.get(invocationContext.getExecutable().getName());
    }

    public static Integer getTestId(ExtensionContext testMethodExtensionContext) {
        log.trace("Get test id for test method: {}", testMethodExtensionContext.getUniqueId());

        return testMethodToTestId.get(testMethodExtensionContext.getUniqueId());
    }

    public static String getEntityTitle(ReflectiveInvocationContext<Method> invocationContext) {
        return invocationContext.getExecutable().getDeclaringClass().getName() + "#" + invocationContext.getExecutable().getName();
    }

    public static boolean isExecutionAbortedByTimeout(Throwable throwable) {
        return throwable instanceof TimeoutException && throwable.getCause() != null && throwable.getCause().getClass().getName().equals("org.junit.jupiter.api.AssertTimeoutPreemptively$ExecutionTimeoutException");
    }

    public static void putLastBeforeAllId(Thread thread, Integer beforeAllId) {
        log.trace("Cache last before all id for thread: {} -> {}", thread.getName(), beforeAllId);

        threadToLastBeforeAllId.put(thread.getName(), beforeAllId);
    }

    public static Integer getLastBeforeAllId(Thread thread) {
        log.trace("Get last before all id for thread: {}", thread.getName());

        return threadToLastBeforeAllId.get(thread.getName());
    }

    public static void removeLastBeforeAllId(Thread thread) {
        log.trace("Remove last before all id for thread: {}", thread.getName());

        threadToLastBeforeAllId.remove(thread.getName());
    }

    public static void putLastBeforeEachId(Thread thread, Integer beforeEachId) {
        log.trace("Cache last before each id for thread: {} -> {}", thread.getName(), beforeEachId);

        threadToLastBeforeEachId.put(thread.getName(), beforeEachId);
    }

    public static Integer getLastBeforeEachId(Thread thread) {
        log.trace("Get last before each id for thread: {}", thread.getName());

        return threadToLastBeforeEachId.get(thread.getName());
    }

    public static void removeLastBeforeEachId(Thread thread) {
        log.trace("Remove last before each id for thread: {}", thread.getName());

        threadToLastBeforeEachId.remove(thread.getName());
    }

    public static void putLastAfterEachId(Thread thread, Integer afterEachId) {
        log.trace("Cache last after each id for thread: {} -> {}", thread.getName(), afterEachId);

        threadToLastAfterEachId.put(thread.getName(), afterEachId);
    }

    public static Integer getLastAfterEachId(Thread thread) {
        log.trace("Get last after each id for thread: {}", thread.getName());

        return threadToLastAfterEachId.get(thread.getName());
    }

    public static void removeLastAfterEachId(Thread thread) {
        log.trace("Remove last after each id for thread: {}", thread.getName());

        threadToLastAfterEachId.remove(thread.getName());
    }

    public static void putLastAfterAllId(Thread thread, Integer afterAllId) {
        log.trace("Cache last after all id for thread: {} -> {}", thread.getName(), afterAllId);

        threadToLastAfterAllId.put(thread.getName(), afterAllId);
    }

    public static Integer getLastAfterAllId(Thread thread) {
        log.trace("Get last after all id for thread: {}", thread.getName());

        return threadToLastAfterAllId.get(thread.getName());
    }

    public static void removeLastAfterAllId(Thread thread) {
        log.trace("Remove last after all id for thread: {}", thread.getName());

        threadToLastAfterAllId.remove(thread.getName());
    }
}
