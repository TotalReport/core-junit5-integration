package com.craftens.totalreport.events.context;

import com.craftens.totalreport.client.TotalReportClient;
import com.craftens.totalreport.junit5.TotalReportAdapter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;

@Slf4j
public class TestContextEvents {
    private final TotalReportClient client;
    private final ExecutorService executorService;

    public TestContextEvents(TotalReportClient client, ExecutorService executorService) {
        this.client = client;
        this.executorService = executorService;
    }

    public void onCreatedAndStarted(TestContextCreatedAndStarted event) {
        log.trace("Add test context created and started event: {}", event);

        executorService.submit(() -> {
            log.trace("Test context created and started event: {}", event);

            String title = event.getTestClass().getName();

            Integer id = client.contextCreatedAndStarted(event.getLaunchId(), title, event.getCreatedTimestamp(), event.getStartedTimestamp());
            TotalReportAdapter.putTestContextId(event.getTestClass(), id);
        });
    }

    public void onFinished(TestContextFinished event) {
        log.trace("Add test context finished event: {}", event);

        executorService.submit(() -> {

            log.trace("Test context finished event: {}", event);

            Integer id = TotalReportAdapter.getTestContextId(event.getTestClass());

            client.contextFinished(id, event.getFinishedTimestamp());
            TotalReportAdapter.removeTestContextId(event.getTestClass());
        });
    }
}
