package com.craftens.totalreport.events.test;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.time.OffsetDateTime;

@Getter
@EqualsAndHashCode
@ToString
public class TestFinished {
    private final ExtensionContext extensionContext;
    private final OffsetDateTime finishedTimestamp;

    public TestFinished(ExtensionContext extensionContext, OffsetDateTime finishedTimestamp) {
        this.extensionContext = extensionContext;
        this.finishedTimestamp = finishedTimestamp;
    }
}
