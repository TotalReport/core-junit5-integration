package com.craftens.totalreport.events.aftereach;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.time.OffsetDateTime;

@Getter
@EqualsAndHashCode
@ToString
public class AfterEachFinished {
    private final Thread thread;
    private final ExtensionContext extensionContext;
    private final OffsetDateTime finishedTimestamp;
    private final Throwable throwable;

    public AfterEachFinished(Thread thread, ExtensionContext extensionContext, OffsetDateTime finishedTimestamp, Throwable throwable) {
        this.thread = thread;
        this.extensionContext = extensionContext;
        this.finishedTimestamp = finishedTimestamp;
        this.throwable = throwable;
    }
}
