package com.craftens.totalreport.events.aftereach;

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
public class AfterEachCreatedAndStarted {
    final Integer launchId;
    final Thread thread;
    final ExtensionContext extensionContext;
    final ReflectiveInvocationContext<Method> invocationContext;
    final OffsetDateTime createdTimestamp;
    final OffsetDateTime startedTimestamp;

    public AfterEachCreatedAndStarted(Integer launchId, Thread thread, ExtensionContext extensionContext, ReflectiveInvocationContext<Method> invocationContext, OffsetDateTime createdTimestamp, OffsetDateTime startedTimestamp) {
        this.launchId = launchId;
        this.thread = thread;
        this.extensionContext = extensionContext;
        this.invocationContext = invocationContext;
        this.createdTimestamp = createdTimestamp;
        this.startedTimestamp = startedTimestamp;
    }
}
