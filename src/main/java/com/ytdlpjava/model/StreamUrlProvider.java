package com.ytdlpjava.model;

import java.io.IOException;

public interface StreamUrlProvider {
    String getStreamUrl(String url) throws IOException, InterruptedException;
}
