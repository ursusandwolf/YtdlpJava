package com.ytdlpjava.model;

import java.io.IOException;
import java.util.List;

public interface PlaylistProvider {
    List<String> getPlaylistUrls(String url) throws IOException, InterruptedException;

    String getPlaylistTitle(String url) throws IOException, InterruptedException;
}
