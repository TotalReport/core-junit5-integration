package com.craftens.totalreport.events.test;

import com.craftens.totalreport.client.DefaultTestStatuses;
import com.craftens.totalreport.client.TotalReportClient;
import com.craftens.totalreport.junit5.TotalReportAdapter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;

@Slf4j
public class TestEvents {
    private final TotalReportClient client;
    private final ExecutorService executorService;

    public TestEvents(TotalReportClient client, ExecutorService executorService) {
        this.client = client;
        this.executorService = executorService;
    }

    public void onCreatedAndStarted(TestCreatedAndStarted event) {
        executorService.submit(() -> {
            ReflectiveInvocationContext<Method> testMethod = event.getInvocationContext();
            String testTitle = TotalReportAdapter.getEntityTitle(testMethod);

            Integer testContextId = TotalReportAdapter.getTestContextId(testMethod.getTargetClass());

            Integer id = client.testCreatedAndStarted(event.getLaunchId(), testContextId, testTitle, event.getCreatedTimestamp(), event.getCreatedTimestamp());

            TotalReportAdapter.putTestId(event.getExtensionContext(), id);
        });
    }

    public void onFinished(TestFinished event) {
        executorService.submit(() -> {
            log.trace("Test finished event: {}", event);
            Integer testId = TotalReportAdapter.getTestId(event.getExtensionContext());
            Throwable throwable = event.getExtensionContext().getExecutionException().orElse(null);

            if (throwable == null) {
                client.testFinished(testId, event.getFinishedTimestamp(), DefaultTestStatuses.SUCCESSFUL);
            } else if (throwable instanceof AssertionError) {
                client.testFinished(testId, event.getFinishedTimestamp(), DefaultTestStatuses.PRODUCT_BUG);
            } else {
                if (TotalReportAdapter.isExecutionAbortedByTimeout(throwable)) {
                    client.testFinished(testId, event.getFinishedTimestamp(), DefaultTestStatuses.ABORTED);
                } else {
                    client.testFinished(testId, event.getFinishedTimestamp(), DefaultTestStatuses.AUTOMATION_BUG);
                }
            }
        });
    }

    public void onSkipped(TestSkipped event) {
        executorService.submit(() -> {
            ExtensionContext extensionContext = event.getExtensionContext();
            Integer testContextId = TotalReportAdapter.getTestContextId(extensionContext.getTestClass().get());
            String title = extensionContext.getTestClass().get().getName() + "#" + extensionContext.getTestMethod().get().getName();
            client.testSkipped(event.getLaunchId(), testContextId, title, event.getTimestamp());
        });
    }
}