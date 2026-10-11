package com.ytdlpjava.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.io.IOException;
import java.io.OutputStream;

@Slf4j
@RequiredArgsConstructor
public class FileResultHandler implements TaskResultHandler {
    private final Path outputDir;

    @Override
    public void handle(String title, Path resultFile) throws Exception {
        handle(title, resultFile, outputDir);
    }

    @Override
    public void handle(String title, Path resultFile, Path destinationDir) throws Exception {
        Files.createDirectories(destinationDir);
        String filename = resultFile.getFileName().toString();
        int dot = filename.lastIndexOf('.');
        String stem = dot > 0 ? filename.substring(0, dot) : filename;
        String extension = dot > 0 ? filename.substring(dot) : "";
        for (int index = 0; ; index++) {
            String candidate = index == 0 ? filename : stem + " (" + index + ")" + extension;
            Path target = destinationDir.resolve(candidate);
            try (var input = Files.newInputStream(resultFile)) {
                // CREATE_NEW reserves the name atomically, including on Windows.
                OutputStream output;
                try {
                    output = Files.newOutputStream(target, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
                } catch (FileAlreadyExistsException e) {
                    continue;
                }
                try (output) {
                    input.transferTo(output);
                } catch (IOException e) {
                    try { Files.deleteIfExists(target); } catch (IOException cleanup) { e.addSuppressed(cleanup); }
                    throw e;
                }
                log.info("Result saved to: {}", target);
                return;
            }
        }
    }
}
