package com.ytdlpjava.model;

import java.nio.file.Path;

public interface FrameExtractor {
    void extractFrame(Path videoFile, long timestampSeconds, Path outputFile) throws Exception;
}
