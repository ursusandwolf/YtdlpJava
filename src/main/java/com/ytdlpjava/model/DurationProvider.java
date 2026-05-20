package com.ytdlpjava.model;

import java.io.IOException;

public interface DurationProvider {
    long getDuration(String url) throws IOException, InterruptedException;
}
