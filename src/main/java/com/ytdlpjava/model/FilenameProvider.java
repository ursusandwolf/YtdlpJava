package com.ytdlpjava.model;

public interface FilenameProvider {
    String buildFilename(String title, int maxLength);
}
