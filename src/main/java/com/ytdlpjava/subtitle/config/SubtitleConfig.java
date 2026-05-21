package com.ytdlpjava.subtitle.config;

public record SubtitleConfig(int minTimestampGapSeconds, int paragraphSoftLimit, int paragraphHardLimit) {
    public static SubtitleConfig defaultSettings() {
        return new SubtitleConfig(10, 600, 800);
    }
}
