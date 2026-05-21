package com.ytdlpjava.subtitle.config;

public record SubtitleConfig(int minTimestampGapSeconds, int paragraphLengthLimit) {
    public static SubtitleConfig defaultSettings() {
        return new SubtitleConfig(10, 600);
    }
}
