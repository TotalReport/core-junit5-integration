package com.craftens.totalreport.events.afterall;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.time.OffsetDateTime;

@Getter
@EqualsAndHashCode
@ToString
public class AfterAllFinished {
    private final Thread thread;
    private final ExtensionContext extensionContext;
    private final OffsetDateTime finishedTimestamp;
    private final Throwable throwable;

    public AfterAllFinished(Thread thread, ExtensionContext extensionContext, OffsetDateTime finishedTimestamp, Throwable throwable) {
        this.thread = thread;
        this.extensionContext = extensionContext;
        this.finishedTimestamp = finishedTimestamp;
        this.throwable = throwable;
    }
}
