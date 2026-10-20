package com.ytdlpjava.infrastructure.ytdlp;

import com.ytdlpjava.core.ProcessExecutionException;
import com.ytdlpjava.core.ProcessExecutor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MetadataDownloaderTest {
    @TempDir Path directory;

    @Test
    void retriesTransientMetadataFailureWithConfiguredRuntime() throws Exception {
        Path basename = directory.resolve("title");
        Path metadata = Path.of(basename + ".info.json");
        List<List<String>> calls = new ArrayList<>();
        ProcessExecutor executor = new ProcessExecutor() {
            @Override public String run(List<String> command, String message) throws IOException {
                calls.add(List.copyOf(command));
                if (calls.size() == 1) throw new ProcessExecutionException(message, 1, "HTTP Error 503");
                Files.writeString(metadata, "{}");
                return "";
            }
        };
        assertEquals(metadata, new MetadataDownloader(executor).download("video", basename.toString()));
        assertEquals(2, calls.size());
        assertEquals(calls.getFirst(), calls.getLast());
        assertEquals("node", calls.getFirst().get(calls.getFirst().indexOf("--js-runtimes") + 1));
    }
}
