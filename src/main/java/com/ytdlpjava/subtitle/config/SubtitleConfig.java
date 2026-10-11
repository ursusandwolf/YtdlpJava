package com.ytdlpjava.subtitle.config;

public record SubtitleConfig(int minTimestampGapSeconds, int paragraphSoftLimit, int paragraphHardLimit) {
    public SubtitleConfig {
        if (minTimestampGapSeconds <= 0 || paragraphSoftLimit <= 0 || paragraphHardLimit < paragraphSoftLimit) {
            throw new IllegalArgumentException("Subtitle thresholds must be positive; hard limit must be >= soft limit");
        }
    }
    public static SubtitleConfig defaultSettings() {
        return new SubtitleConfig(10, 600, 800);
    }
}
