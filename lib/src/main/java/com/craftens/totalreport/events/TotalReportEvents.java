package com.craftens.totalreport.events;

import com.craftens.totalreport.client.TotalReportClient;
import com.craftens.totalreport.events.afterall.AfterAllEvents;
import com.craftens.totalreport.events.aftereach.AfterEachEvents;
import com.craftens.totalreport.events.beforeall.BeforeAllEvents;
import com.craftens.totalreport.events.beforeeach.BeforeEachEvents;
import com.craftens.totalreport.events.context.TestContextEvents;
import com.craftens.totalreport.events.test.TestEvents;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@EqualsAndHashCode
public class TotalReportEvents {
    private final ExecutorService executor;
    @Getter
    private final TestContextEvents testContext;
    @Getter
    private final BeforeAllEvents beforeAll;
    @Getter
    private final BeforeEachEvents beforeEach;
    @Getter
    private final TestEvents test;
    @Getter
    private final AfterEachEvents afterEach;
    @Getter
    private final AfterAllEvents afterAll;

    public TotalReportEvents(TotalReportClient client) {
        this.executor = Executors.newSingleThreadExecutor();
        this.testContext = new TestContextEvents(client, this.executor);
        this.beforeAll = new BeforeAllEvents(client, this.executor);
        this.beforeEach = new BeforeEachEvents(client, this.executor);
        this.test = new TestEvents(client, this.executor);
        this.afterEach = new AfterEachEvents(client, this.executor);
        this.afterAll = new AfterAllEvents(client, this.executor);
    }

    public void onComplete() {
        this.executor.shutdown();
    }

    @SneakyThrows
    public void waitCompleteAndShutdown() {
        this.executor.shutdown();
        this.executor.awaitTermination(1, TimeUnit.HOURS);
    }
}
