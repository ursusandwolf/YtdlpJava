package com.ytdlpjava.config;

import com.beust.jcommander.Parameter;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

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
