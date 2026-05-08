package com.ytdlpjava.model;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface Downloader {
    Path download(String url, String outputBasename) throws IOException, InterruptedException;
    String getTitle(String url) throws IOException, InterruptedException;
    String getStreamUrl(String url) throws IOException, InterruptedException;
    long getDuration(String url) throws IOException, InterruptedException;
    List<String> getPlaylistUrls(String url) throws IOException, InterruptedException;
    String getPlaylistTitle(String url) throws IOException, InterruptedException;
}
