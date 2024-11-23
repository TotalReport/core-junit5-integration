package com.craftens.totalreport.events.beforeeach;

import com.craftens.totalreport.client.DefaultTestStatuses;
import com.craftens.totalreport.client.TotalReportClient;
import com.craftens.totalreport.junit5.TotalReportAdapter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;


@Slf4j
public class BeforeEachEvents {
    private final TotalReportClient client;
    private final ExecutorService executorService;

    public BeforeEachEvents(TotalReportClient client, ExecutorService executorService) {
        this.client = client;
        this.executorService = executorService;
    }

    public void onCreatedAndStarted(BeforeEachCreatedAndStarted event) {
        executorService.submit(() -> {
            log.trace("Before each created and started event: {}", event);

            ReflectiveInvocationContext<Method> testMethod = event.getInvocationContext();
            String testTitle = TotalReportAdapter.getEntityTitle(testMethod);

            Integer testContextId = TotalReportAdapter.getTestContextId(testMethod.getTargetClass());

            Integer id = client.beforeTestCreatedAndStarted(event.launchId, testContextId, testTitle, event.getCreatedTimestamp(), event.getStartedTimestamp());

            TotalReportAdapter.putLastBeforeEachId(event.getThread(), id);
        });
    }

    public void onFinished(BeforeEachFinished event) {
        executorService.submit(() -> {
            log.trace("Before each finished event: {}", event);

            Integer id = TotalReportAdapter.getLastBeforeEachId(event.getThread());
            Throwable throwable = event.getThrowable();

            if (throwable == null) {
                client.beforeTestFinished(id, event.getFinishedTimestamp(), DefaultTestStatuses.SUCCESSFUL);
            } else if (throwable instanceof AssertionError) {
                client.beforeTestFinished(id, event.getFinishedTimestamp(), DefaultTestStatuses.PRODUCT_BUG);
            } else {
                if (TotalReportAdapter.isExecutionAbortedByTimeout(throwable)) {
                    client.beforeTestFinished(id, event.getFinishedTimestamp(), DefaultTestStatuses.ABORTED);
                } else {
                    client.beforeTestFinished(id, event.getFinishedTimestamp(), DefaultTestStatuses.AUTOMATION_BUG);
                }
            }

            TotalReportAdapter.removeLastBeforeEachId(event.getThread());
        });
    }
}