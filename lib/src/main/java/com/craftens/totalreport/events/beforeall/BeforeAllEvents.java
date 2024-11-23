package com.craftens.totalreport.events.beforeall;

import com.craftens.totalreport.client.DefaultTestStatuses;
import com.craftens.totalreport.client.TotalReportClient;
import com.craftens.totalreport.junit5.TotalReportAdapter;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;


public class BeforeAllEvents {
    private final TotalReportClient client;
    private final ExecutorService executorService;

    public BeforeAllEvents(TotalReportClient client, ExecutorService executorService) {
        this.client = client;
        this.executorService = executorService;
    }

    public void onCreatedAndStarted(BeforeAllCreatedAndStarted event) {
        executorService.submit(() -> {
            ReflectiveInvocationContext<Method> testMethod = event.getInvocationContext();
            String testTitle = TotalReportAdapter.getEntityTitle(testMethod);

            Integer testContextId = TotalReportAdapter.getTestContextId(testMethod.getExecutable().getDeclaringClass());

            Integer id = client.beforeTestCreatedAndStarted(event.launchId, testContextId, testTitle, event.getCreatedTimestamp(), event.getCreatedTimestamp());

            TotalReportAdapter.putLastBeforeAllId(event.getThread(), id);
        });
    }

    public void onFinished(BeforeAllFinished event) {
        executorService.submit(() -> {

            Integer id = TotalReportAdapter.getLastBeforeAllId(event.getThread());
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

            TotalReportAdapter.removeLastBeforeAllId(event.getThread());
        });
    }
}