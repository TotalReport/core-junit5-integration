package com.craftens.totalreport.events.beforeeach;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.time.OffsetDateTime;

@Getter
@EqualsAndHashCode
@ToString
public class BeforeEachFinished {
    private final Thread thread;
    private final ExtensionContext extensionContext;
    private final OffsetDateTime finishedTimestamp;
    private final Throwable throwable;

    public BeforeEachFinished(Thread thread, ExtensionContext extensionContext, OffsetDateTime finishedTimestamp, Throwable throwable) {
        this.thread = thread;
        this.extensionContext = extensionContext;
        this.finishedTimestamp = finishedTimestamp;
        this.throwable = throwable;
    }
}
