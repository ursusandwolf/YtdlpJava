package com.ytdlpjava.model;

import java.io.IOException;
import java.nio.file.Path;

public interface MediaDownloader {
    Path download(String url, String outputBasename) throws IOException, InterruptedException;
}
