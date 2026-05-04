package com.ytdlpjava;

import java.io.IOException;
import java.nio.file.Path;

public interface ContentProcessor {
    String process(Path inputPath) throws IOException;
}
