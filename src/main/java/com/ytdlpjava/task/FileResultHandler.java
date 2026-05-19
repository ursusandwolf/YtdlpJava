package com.ytdlpjava.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Slf4j
@RequiredArgsConstructor
public class FileResultHandler implements TaskResultHandler {
    private final Path outputDir;

    @Override
    public void handle(String title, Path resultFile) throws Exception {
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }
        
        Path targetPath = outputDir.resolve(resultFile.getFileName());
        
        // If they are on the same partition, move is fast. Otherwise, it copies.
        Files.move(resultFile, targetPath, StandardCopyOption.REPLACE_EXISTING);
        log.info("✅ Result saved to: {}", targetPath);
    }
}
