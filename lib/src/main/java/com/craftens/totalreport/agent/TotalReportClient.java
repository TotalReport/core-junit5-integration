package com.craftens.totalreport.agent;

import com.craftens.totalreport.openapi.api.DefaultApi;
import com.craftens.totalreport.openapi.invoker.ApiClient;
import com.craftens.totalreport.openapi.invoker.Configuration;
import com.craftens.totalreport.openapi.model.*;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;

/**
 * The agent for the Total Report server.
 */
@Slf4j
public class TotalReportClient {
    /**
     * The URL of the Total Report server.
     */
    private final String url;
    /**
     * The API instance.
     */
    private final DefaultApi apiInstance;

    /**
     * Constructor.
     *
     * @param url The URL of the Total Report server.
     */
    public TotalReportClient(String url) {
        this.url = url;
        ApiClient defaultClient = Configuration.getDefaultApiClient().setBasePath(this.url);
        apiInstance = new DefaultApi(defaultClient);
    }

    /**
     * Create a report.
     *
     * @param title The title of the report.
     * @return The ID of the created report.
     */
    public Integer createReport(String title) {
        V1ReportsPost201Response response = apiInstance.v1ReportsPost(new V1ReportsPostRequest().title(title));

        log.debug("Report [ {} | {} ] created.", response.getId(), response.getTitle());

        return response.getId();
    }

    /**
     * Send information to Total Report server about launch creation and start.
     *
     * @param reportId         The ID of the report.
     * @param title            The title of the launch.
     * @param createdTimestamp The timestamp when the launch was created.
     * @param startedTimestamp The timestamp when the launch was started.
     * @return The ID of the created launch.
     */
    public Integer launchCreatedAndStarted(Integer reportId, String title, OffsetDateTime createdTimestamp, OffsetDateTime startedTimestamp) {
        V1LaunchesPost201Response response = apiInstance.v1LaunchesPost(new V1LaunchesPostRequest()
                .reportId(reportId)
                .title(title)
                .createdTimestamp(createdTimestamp)
                .startedTimestamp(startedTimestamp));

        log.debug("Launch [ {} | {} ] started.", response.getId(), response.getTitle());

        return response.getId();
    }

    /**
     * Send information to Total Report server about test context creation and start.
     *
     * @param launchId         The ID of the launch.
     * @param title            The title of the test context.
     * @param createdTimestamp The timestamp when the test context was created.
     * @param startedTimestamp The timestamp when the test context was started.
     * @return The ID of the created test context.
     */
    public Integer contextCreatedAndStarted(Integer launchId, String title, OffsetDateTime createdTimestamp, OffsetDateTime startedTimestamp) {
        V1TestContextsPost201Response response = apiInstance.v1TestContextsPost(new V1TestContextsPostRequest()
                .launchId(launchId)
                .title(title)
                .createdTimestamp(createdTimestamp)
                .startedTimestamp(startedTimestamp));

        log.debug("Test context [ {} | {} ] created and started.", response.getId(), response.getTitle());

        return response.getId();
    }

    /**
     * Send information to Total Report server about before test activity creation and start.
     *
     * @param launchId         The ID of the launch.
     * @param title            The title of the before test activity.
     * @param createdTimestamp The timestamp when the before test activity was created.
     * @param startedTimestamp The timestamp when the before test activity was started.
     * @return The ID of the created before test activity.
     */
    public Integer beforeTestCreatedAndStarted(Integer launchId, Integer testContextId, String title, OffsetDateTime createdTimestamp, OffsetDateTime startedTimestamp) {
        V1BeforeTestsPost201Response response = apiInstance.v1BeforeTestsPost(new V1BeforeTestsPostRequest()
                .launchId(launchId)
                .testContextId(testContextId)
                .title(title)
                .createdTimestamp(createdTimestamp)
                .startedTimestamp(startedTimestamp));

        log.debug("Before test activity [ {} | {} ] created and started.", response.getId(), response.getTitle());

        return response.getId();
    }

    /**
     * Send information to Total Report server about before test activity finish.
     *
     * @param beforeAllId       The ID of the before test activity.
     * @param finishedTimestamp The timestamp when the before test activity was finished.
     * @param statusId          The ID of the status. The default test statuses can be taken from {@link DefaultTestStatuses}.
     */
    public void beforeTestFinished(Integer beforeAllId, OffsetDateTime finishedTimestamp, String statusId) {
        V1BeforeTestsPost201Response response = apiInstance.v1BeforeTestsIdPatch(
                beforeAllId,
                new V1BeforeTestsIdPatchRequest()
                        .finishedTimestamp(finishedTimestamp)
                        .statusId(statusId));

        log.debug("Before test activity [ {} | {} ] finished with status {}.",
                response.getId(), response.getTitle(), response.getStatusId());
    }

    /**
     * Send information to Total Report server about test creation and start.
     *
     * @param launchId         The ID of the launch.
     * @param testContextId    The ID of the test context.
     * @param title            The title of the test.
     * @param createdTimestamp The timestamp when the test was created.
     * @param startedTimestamp The timestamp when the test was started.
     * @return The ID of the test in Total Report.
     */
    public Integer testCreatedAndStarted(Integer launchId, Integer testContextId, String title, OffsetDateTime createdTimestamp, OffsetDateTime startedTimestamp) {
        V1BeforeTestsPost201Response response = apiInstance.v1TestsPost(new V1BeforeTestsPostRequest()
                .launchId(launchId)
                .testContextId(testContextId)
                .title(title)
                .createdTimestamp(createdTimestamp)
                .startedTimestamp(startedTimestamp));

        log.debug("Test [ {} | {} ] created and started.", response.getId(), response.getTitle());

        return response.getId();
    }

    /**
     * Send information to Total Report server about test finish.
     *
     * @param testId            The ID of the test.
     * @param finishedTimestamp The timestamp when the test was finished.
     * @param status            The status of the test. The default test statuses can be taken from {@link DefaultTestStatuses}.
     */
    public void testFinished(Integer testId, OffsetDateTime finishedTimestamp, String status) {
        V1BeforeTestsPost201Response response = apiInstance.v1TestsIdPatch(
                testId,
                new V1BeforeTestsIdPatchRequest()
                        .finishedTimestamp(finishedTimestamp)
                        .statusId(status));

        log.debug("Test [ {} | {} ] finished with status {}.",
                response.getId(), response.getTitle(), response.getStatusId());
    }

    /**
     * Send information to Total Report server about after test activity creation and start.
     *
     * @param launchId         The ID of the launch.
     * @param testContextId    The ID of the test context.
     * @param title            The title of the after test activity.
     * @param createdTimestamp The timestamp when the after test activity was created.
     * @param startedTimestamp The timestamp when the after test activity was started.
     * @return The ID of the created after test activity.
     */
    public Integer afterTestCreatedAndStarted(Integer launchId, Integer testContextId, String title, OffsetDateTime createdTimestamp, OffsetDateTime startedTimestamp) {
        V1BeforeTestsPost201Response response = apiInstance.v1AfterTestsPost(new V1BeforeTestsPostRequest()
                .launchId(launchId)
                .testContextId(testContextId)
                .title(title)
                .createdTimestamp(createdTimestamp)
                .startedTimestamp(startedTimestamp));

        log.debug("After test activity [ {} | {} ] created and started.", response.getId(), response.getTitle());

        return response.getId();
    }

    /**
     * Send information to Total Report server about after test activity finish.
     *
     * @param afterTestId       The ID of the after test activity.
     * @param finishedTimestamp The timestamp when the after test activity was finished.
     * @param statusId          The ID of the status. The default test statuses can be taken from {@link DefaultTestStatuses}.
     */
    public void afterTestFinished(Integer afterTestId, OffsetDateTime finishedTimestamp, String statusId) {
        V1BeforeTestsPost201Response response = apiInstance.v1AfterTestsIdPatch(
                afterTestId,
                new V1BeforeTestsIdPatchRequest()
                        .finishedTimestamp(finishedTimestamp)
                        .statusId(statusId));

        log.debug("After test activity [ {} | {} ] finished with status {}.",
                response.getId(), response.getTitle(), response.getStatusId());
    }

    /**
     * Send information to Total Report server about test context finish.
     *
     * @param testContextId     The ID of the test context.
     * @param finishedTimestamp The timestamp when the test context was finished.
     */
    public void contextFinished(Integer testContextId, OffsetDateTime finishedTimestamp) {
        V1TestContextsPost201Response response = apiInstance.v1TestContextsIdPatch(
                testContextId,
                new V1LaunchesIdPatchRequest()
                        .finishedTimestamp(finishedTimestamp));

        log.debug("Test context [ {} | {} ] finished.", response.getId(), response.getTitle());
    }

    /**
     * Send information to Total Report server about skipped test.
     *
     * @param launchId The ID of the launch.
     * @param testContextId The ID of the test context.
     * @param title The title of the test.
     * @param timestamp The timestamp of the test (created, started, finished).
     * @return The ID of the created test.
     */
    public Integer testSkipped(Integer launchId, Integer testContextId, String title, OffsetDateTime timestamp){
        V1BeforeTestsPost201Response response = apiInstance.v1TestsPost(new V1BeforeTestsPostRequest()
                .launchId(launchId)
                .testContextId(testContextId)
                .title(title)
                .createdTimestamp(timestamp)
                .startedTimestamp(timestamp)
                .finishedTimestamp(timestamp)
                .statusId(DefaultTestStatuses.SKIPPED));

        log.debug("Test [ {} | {} ] skipped.", response.getId(), response.getTitle());

        return response.getId();
    }
}
