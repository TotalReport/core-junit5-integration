package com.craftens.totalreport.events.test;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import java.lang.reflect.Method;
import java.time.OffsetDateTime;

@Getter
@EqualsAndHashCode
@ToString
public class TestCreatedAndStarted {
    private final Integer launchId;
    private final ExtensionContext extensionContext;
    private final ReflectiveInvocationContext<Method> invocationContext;
    private final OffsetDateTime createdTimestamp;

    public TestCreatedAndStarted(Integer launchId, ExtensionContext extensionContext, ReflectiveInvocationContext<Method> invocationContext, OffsetDateTime createdTimestamp) {
        this.launchId = launchId;
        this.extensionContext = extensionContext;
        this.invocationContext = invocationContext;
        this.createdTimestamp = createdTimestamp;
    }
}
