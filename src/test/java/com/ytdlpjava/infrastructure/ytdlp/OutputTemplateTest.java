package com.ytdlpjava.infrastructure.ytdlp;

import com.ytdlpjava.core.ProcessExecutor;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OutputTemplateTest {
    @TempDir Path directory;

    @ParameterizedTest
    @ValueSource(strings = {"video", "audio", "sub", "metadata"})
    void preservesLiteralPercentInOutputBasename(String type) throws Exception {
        Path basename = directory.resolve("100% %(id)s");
        String extension = switch (type) {
            case "sub" -> ".en.vtt";
            case "metadata" -> ".info.json";
            case "audio" -> ".opus";
            default -> ".mp4";
        };
        Path expected = Path.of(basename + extension);
        var executor = new ProcessExecutor() {
            @Override public String run(List<String> command, String message) throws IOException {
                int option = command.contains("--output") ? command.indexOf("--output") : command.indexOf("-o");
                String template = command.get(option + 1);
                String expanded = template.replace("%%", "\u0000").replace("%(id)s", "video-id")
                        .replace("%(ext)s", extension.substring(1)).replace('\u0000', '%');
                String suffix = type.equals("sub") || type.equals("metadata") ? extension : "";
                Path file = Files.writeString(Path.of(expanded + suffix), "content");
                return file.toAbsolutePath().toString();
            }
        };
        AbstractYoutubeService downloader = switch (type) {
            case "sub" -> new SubtitleDownloader(executor, "en");
            case "metadata" -> new MetadataDownloader(executor);
            case "audio" -> new AudioDownloader(executor, "opus", "0");
            default -> new VideoDownloader(executor);
        };
        assertEquals(expected, downloader.download("video", basename.toString()));
    }
}
