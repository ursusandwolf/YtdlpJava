package com.ytdlpjava.task;

import com.ytdlpjava.model.MediaDownloader;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/** Owns all intermediate files for one download, including partial downloads. */
final class DownloadWorkspace implements AutoCloseable {
    private final Path directory;

    DownloadWorkspace() throws IOException {
        directory = Files.createTempDirectory("ytdlp-").toAbsolutePath().normalize();
    }

    Path download(MediaDownloader downloader, String url, String basename) throws IOException, InterruptedException {
        Path filename = Path.of(basename);
        if (filename.getRoot() != null || filename.getNameCount() != 1 || basename.equals(".") || basename.equals("..")) {
            throw new IOException("Download basename must be a single filename");
        }
        Path result = downloader.download(url, directory.resolve(basename).toString());
        if (result == null || !directory.equals(result.toAbsolutePath().normalize().getParent())
                || Files.isSymbolicLink(result)) {
            throw new IOException("Downloaded file is outside its temporary directory");
        }
        return result;
    }

    @Override
    public void close() throws IOException {
        // walkFileTree does not follow symbolic links; cleanup stays inside the owned directory.
        Files.walkFileTree(directory, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException failure) throws IOException {
                if (failure != null) throw failure;
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
