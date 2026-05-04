package com.ytdlpjava;

import java.io.IOException;
import java.nio.file.Path;

public interface VideoTask {
    void execute(String url, Path outputDir) throws Exception;
}
