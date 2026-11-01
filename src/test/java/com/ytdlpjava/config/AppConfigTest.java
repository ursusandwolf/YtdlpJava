package com.ytdlpjava.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AppConfigTest {
    @Test
    void configuresDefaultSubtitleOutputDirectory() {
        AppConfig config = new AppConfig();

        config.configureOutputDir();

        assertEquals("txt", config.getOutputDir());
    }

    @Test
    void configuresDefaultOutputDirectoryForAudio() {
        AppConfig config = new AppConfig();
        config.setType("audio");

        config.configureOutputDir();

        assertEquals("output/audio", config.getOutputDir());
    }

    @Test
    void keepsCustomOutputDirectory() {
        AppConfig config = new AppConfig();
        config.setType("video");
        config.setOutputDir("custom-dir");

        config.configureOutputDir();

        assertEquals("custom-dir", config.getOutputDir());
    }

    @Test
    void normalizesValidLogLevel() {
        AppConfig config = new AppConfig();
        config.setLogLevel("debug");
        config.validate();
        assertEquals("DEBUG", config.getLogLevel());
    }

    @Test
    void rejectsUnknownLogLevel() {
        AppConfig config = new AppConfig();
        config.setLogLevel("loud");
        assertThrows(IllegalArgumentException.class, config::validate);
    }
}
