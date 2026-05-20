package com.ytdlpjava.model;

import java.io.IOException;

public interface TitleProvider {
    String getTitle(String url) throws IOException, InterruptedException;
}
