package com.ytdlpjava;

import java.io.IOException;
import java.nio.file.Path;

public interface Downloader {
    Path download(String url, String outputBasename) throws IOException, InterruptedException;
    String getTitle(String url) throws IOException, InterruptedException;
    String getStreamUrl(String url) throws IOException, InterruptedException;
    long getDuration(String url) throws IOException, InterruptedException;
}
