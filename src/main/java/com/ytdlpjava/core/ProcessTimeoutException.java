package com.ytdlpjava.core;

import java.util.concurrent.TimeUnit;

public class ProcessTimeoutException extends ProcessException {
    public ProcessTimeoutException(String message, long timeout, TimeUnit unit) {
        super("%s (Timeout after %d %s)".formatted(message, timeout, unit));
    }
}
