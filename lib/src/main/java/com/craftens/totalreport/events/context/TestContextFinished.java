package com.craftens.totalreport.events.context;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.OffsetDateTime;

/**
 * Event that represents the end of a test context.
 */
@Getter
@EqualsAndHashCode
public class TestContextFinished {
    final Class<?> testClass;
    final OffsetDateTime finishedTimestamp;

    public TestContextFinished(Class<?> testClass, OffsetDateTime finishedTimestamp) {
        this.testClass = testClass;
        this.finishedTimestamp = finishedTimestamp;
    }
}
