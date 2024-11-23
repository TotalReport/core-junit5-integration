package com.craftens.totalreport.events.test;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.time.OffsetDateTime;

@Getter
@EqualsAndHashCode
@ToString
public class TestSkipped {
    private final Integer launchId;
    private final ExtensionContext extensionContext;
    private final OffsetDateTime timestamp;

    public TestSkipped(Integer launchId, ExtensionContext extensionContext, OffsetDateTime timestamp) {
        this.launchId = launchId;
        this.extensionContext = extensionContext;
        this.timestamp = timestamp;
    }
}
