package com.ytdlpjava.util;

import java.time.Duration;

public final class TextFormatUtils {
    private TextFormatUtils() {}

    public static boolean isSentenceEnding(char c) {
        return c == '.' || c == '!' || c == '?' || c == '…';
    }

    public static String formatTimestamp(Duration d) {
        long s = d.getSeconds();
        return String.format("%02d:%02d:%02d", s / 3600, (s % 3600) / 60, s % 60);
    }
}
