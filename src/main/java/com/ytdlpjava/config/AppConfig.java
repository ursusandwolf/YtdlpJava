package com.ytdlpjava.config;

import com.beust.jcommander.Parameter;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.nio.file.Path;

@Getter
@Setter
public class AppConfig {
    @Parameter(description = "Ссылка на YouTube-видео")
    private List<String> videoUrls = new ArrayList<>();

    private String videoUrl;

    @Parameter(names = {"-l", "--lang"}, description = "Язык субтитров (по умолчанию: en)")
    private String lang = "en";

    @Parameter(names = {"-o", "--output_dir"}, description = "Папка для сохранения результата")
    private String outputDir = "output";

    @Parameter(names = {"-t", "--type"}, description = "Тип загрузки: sub (субтитры), audio (аудио), screenshot (скриншоты), video (видео), metadata (метаданные)")
    private String type = "sub";

    @Parameter(names = {"--format"}, description = "Формат аудио (opus, mp3, m4a)")
    private String audioFormat = "opus";

    @Parameter(names = {"--quality"}, description = "Качество аудио (0 - лучшее, 9 - худшее)")
    private String audioQuality = "0";

    @Parameter(names = {"-i", "--interval"}, description = "Интервал между скриншотами в секундах (по умолчанию: 60)")
    private int interval = 60;

    @Parameter(names = {"-h", "--help"}, help = true, description = "Показать справку")
    private boolean help;

    public void validate() {
        type = normalize(type, "Task type");
        audioFormat = normalize(audioFormat, "Audio format");
        lang = normalize(lang, "Subtitle language");
        if (!Set.of("sub", "audio", "video", "screenshot", "metadata").contains(type)) {
            throw new IllegalArgumentException("Unknown task type: " + type);
        }
        if (!Set.of("opus", "mp3", "m4a").contains(audioFormat)) {
            throw new IllegalArgumentException("Unsupported audio format: " + audioFormat);
        }
        if (audioQuality == null || !audioQuality.matches("[0-9]")) {
            throw new IllegalArgumentException("Audio quality must be an integer from 0 to 9");
        }
        if (interval <= 0) throw new IllegalArgumentException("Screenshot interval must be positive");
        if (outputDir == null || outputDir.isBlank()) {
            throw new IllegalArgumentException("Output directory must not be blank");
        }
        Path.of(outputDir);
    }

    private static String normalize(String value, String label) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(label + " must not be blank");
        return value.trim().toLowerCase(Locale.ROOT);
    }

    public void configureOutputDir() {
        if ("output".equals(this.outputDir)) {
            this.outputDir = switch (this.type.toLowerCase()) {
                case "sub" -> "txt";
                case "audio" -> "output/audio";
                case "video" -> "output/video";
                case "screenshot" -> "output/img";
                case "metadata" -> "output/metadata";
                default -> this.outputDir;
            };
        }
    }
}
