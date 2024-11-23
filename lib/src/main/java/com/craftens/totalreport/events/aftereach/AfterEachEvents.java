package com.craftens.totalreport.events.aftereach;

import com.craftens.totalreport.client.DefaultTestStatuses;
import com.craftens.totalreport.client.TotalReportClient;
import com.craftens.totalreport.junit5.TotalReportAdapter;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;


public class AfterEachEvents {
    private final TotalReportClient client;
    private final ExecutorService executorService;

    public AfterEachEvents(TotalReportClient client, ExecutorService executorService) {
        this.client = client;
        this.executorService = executorService;
    }

    public void onCreatedAndStarted(AfterEachCreatedAndStarted event) {
        executorService.submit(() -> {
            ReflectiveInvocationContext<Method> testMethod = event.getInvocationContext();
            String testTitle = TotalReportAdapter.getEntityTitle(testMethod);

            Integer testContextId = TotalReportAdapter.getTestContextId(testMethod.getTargetClass());

            Integer id = client.afterTestCreatedAndStarted(event.launchId, testContextId, testTitle, event.getCreatedTimestamp(), event.getCreatedTimestamp());

            TotalReportAdapter.putLastAfterEachId(event.getThread(), id);
        });
    }

    public void onFinished(AfterEachFinished event) {
        executorService.submit(() -> {
            Integer id = TotalReportAdapter.getLastAfterEachId(event.getThread());
            Throwable throwable = event.getThrowable();

            if (throwable == null) {
                client.afterTestFinished(id, event.getFinishedTimestamp(), DefaultTestStatuses.SUCCESSFUL);
            } else if (throwable instanceof AssertionError) {
                client.afterTestFinished(id, event.getFinishedTimestamp(), DefaultTestStatuses.PRODUCT_BUG);
            } else {
                if (TotalReportAdapter.isExecutionAbortedByTimeout(throwable)) {
                    client.afterTestFinished(id, event.getFinishedTimestamp(), DefaultTestStatuses.ABORTED);
                } else {
                    client.afterTestFinished(id, event.getFinishedTimestamp(), DefaultTestStatuses.AUTOMATION_BUG);
                }
            }

            TotalReportAdapter.removeLastAfterEachId(event.getThread());
        });
    }
}