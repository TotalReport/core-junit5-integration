package com.craftens.totalreport.events.context;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.OffsetDateTime;

/**
 * Event that represents a test context being created and started.
 */
@Getter
@EqualsAndHashCode
public class TestContextCreatedAndStarted {
    final Integer launchId;
    final Class<?> testClass;
    final OffsetDateTime createdTimestamp;
    final OffsetDateTime startedTimestamp;

    public TestContextCreatedAndStarted(Integer launchId, Class<?> testClass, OffsetDateTime createdTimestamp, OffsetDateTime startedTimestamp) {
        this.launchId = launchId;
        this.testClass = testClass;
        this.createdTimestamp = createdTimestamp;
        this.startedTimestamp = startedTimestamp;
    }
}
