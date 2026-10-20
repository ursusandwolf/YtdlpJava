package com.ytdlpjava.infrastructure.ytdlp;

import com.ytdlpjava.core.ProcessExecutionException;
import com.ytdlpjava.core.ProcessExecutor;
import com.ytdlpjava.core.ProcessTimeoutException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class AbstractYoutubeServiceTest {
    @Test
    void requestsUtf8OutputForUnicodeTitles() throws Exception {
        List<List<String>> calls = new ArrayList<>();
        var service = new VideoDownloader(new ProcessExecutor() {
            @Override public String run(List<String> command, String message) {
                calls.add(List.copyOf(command));
                return "Все времена в немецком — Україна";
            }
        });
        assertEquals("Все времена в немецком — Україна", service.getTitle("video"));
        List<String> command = calls.getFirst();
        assertTrue(command.contains("--encoding"));
        assertEquals("utf-8", command.get(command.indexOf("--encoding") + 1));
    }
    @ParameterizedTest
    @ValueSource(strings = {"NA", "null", "", "0", "-1", "NaN", "Infinity"})
    void rejectsUnavailableOrInvalidDuration(String output) {
        var service = new VideoDownloader(new ProcessExecutor() {
            @Override public String run(List<String> command, String message) { return output; }
        });
        assertThrows(IOException.class, () -> service.getDuration("video"));
    }

    @Test
    void acceptsFractionalDurationFromFallback() throws Exception {
        var service = new VideoDownloader(new ProcessExecutor() {
            @Override public String run(List<String> command, String message) {
                return command.contains("%(duration)j") ? "NA" : "2.5";
            }
        });
        assertEquals(3, service.getDuration("video"));
    }

    @Test
    void metadataQueriesPropagateInterruptionWithoutFallbacks() {
        AtomicInteger calls = new AtomicInteger();
        var service = new VideoDownloader(new ProcessExecutor() {
            @Override public String run(List<String> command, String message) throws InterruptedException {
                calls.incrementAndGet();
                throw new InterruptedException("cancelled");
            }
        });
        List<Executable> queries = List.of(() -> service.getTitle("video"),
                () -> service.getDuration("video"), () -> service.getPlaylistUrls("video"),
                () -> service.getPlaylistTitle("video"));
        for (Executable query : queries) {
            calls.set(0);
            assertThrows(InterruptedException.class, query);
            assertEquals(1, calls.get());
        }
    }

    @Test
    void durationFallbackAlsoPropagatesInterruption() {
        AtomicInteger calls = new AtomicInteger();
        var service = new VideoDownloader(new ProcessExecutor() {
            @Override public String run(List<String> command, String message) throws InterruptedException {
                if (calls.incrementAndGet() == 1) return "NA";
                throw new InterruptedException("cancelled");
            }
        });
        assertThrows(InterruptedException.class, () -> service.getDuration("video"));
        assertEquals(2, calls.get());
    }

    @Test
    void retriesTransientDiagnosticBeforeChangingClient() throws Exception {
        checkRetry(new ProcessExecutionException("failed", 1, "HTTP Error 503: Service Unavailable"));
    }

    @Test
    void retriesProcessTimeoutBeforeChangingClient() throws Exception {
        checkRetry(new ProcessTimeoutException("failed", 1, TimeUnit.SECONDS));
    }

    private void checkRetry(RuntimeException failure) throws Exception {
        List<List<String>> calls = new ArrayList<>();
        var service = new VideoDownloader(new ProcessExecutor() {
            @Override public String run(List<String> command, String message) {
                calls.add(List.copyOf(command));
                if (calls.size() == 1) throw failure;
                return "title";
            }
        });
        assertEquals("title", service.getTitle("video"));
        assertEquals(2, calls.size());
        assertEquals(calls.getFirst(), calls.getLast());
    }
}
