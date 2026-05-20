package com.ytdlpjava.infrastructure.ffmpeg;

import com.ytdlpjava.core.ProcessExecutor;
import com.ytdlpjava.model.FrameExtractor;
import lombok.RequiredArgsConstructor;

import java.nio.file.Path;
import java.util.List;

@RequiredArgsConstructor
public class FfmpegFrameExtractor implements FrameExtractor {
    private final ProcessExecutor executor;

    @Override
    public void extractFrame(Path videoFile, long timestampSeconds, Path outputFile) throws Exception {
        List<String> command = List.of(
                "ffmpeg",
                "-ss", String.valueOf(timestampSeconds),
                "-i", videoFile.toString(),
                "-frames:v", "1",
                "-q:v", "2",
                "-y",
                outputFile.toString()
        );

        executor.run(command, "ffmpeg extraction failed");
    }
}
