package com.ytdlpjava.cli;

import java.util.concurrent.atomic.AtomicBoolean;

/** Lets the CLI unwind its resource scopes when the JVM receives a console signal. */
final class ShutdownCancellation implements AutoCloseable {
    private final AtomicBoolean shuttingDown = new AtomicBoolean();
    private final Thread hook;

    ShutdownCancellation() {
        Thread owner = Thread.currentThread();
        hook = new Thread(() -> {
            shuttingDown.set(true);
            owner.interrupt();
            try {
                owner.join(10_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "cli-shutdown");
        Runtime.getRuntime().addShutdownHook(hook);
    }

    boolean isShuttingDown() {
        return shuttingDown.get();
    }

    @Override
    public void close() {
        try {
            Runtime.getRuntime().removeShutdownHook(hook);
        } catch (IllegalStateException e) {
            // Shutdown has started; the hook owns cancellation and the exit status.
            shuttingDown.set(true);
        }
    }
}
