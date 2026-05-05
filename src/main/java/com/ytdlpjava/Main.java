package com.ytdlpjava;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import lombok.extern.slf4j.Slf4j;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

@Slf4j
public class Main {
    @Parameter(description = "Ссылка на YouTube-видео")
    private List<String> videoUrls;

    @Parameter(names = {"-l", "--lang"}, description = "Язык субтитров (по умолчанию: en)")
    private String lang = "en";

    @Parameter(names = {"-o", "--output_dir"}, description = "Папка для сохранения результата")
    private String outputDir = "output";

    @Parameter(names = {"-t", "--type"}, description = "Тип загрузки: sub (субтитры), audio (аудио), screenshot (скриншоты), video (видео)")
    private String type = "sub";

    @Parameter(names = {"--format"}, description = "Формат аудио (opus, mp3, m4a)")
    private String audioFormat = "opus";

    @Parameter(names = {"--quality"}, description = "Качество аудио (0 - лучшее, 9 - худшее)")
    private String audioQuality = "0";

    @Parameter(names = {"-i", "--interval"}, description = "Интервал между скриншотами в секундах (по умолчанию: 60)")
    private int interval = 60;

    public static void main(String[] args) {
        Main main = new Main();
        JCommander jc = JCommander.newBuilder().addObject(main).build();
        jc.parse(args);

        String videoUrl = (main.videoUrls != null && !main.videoUrls.isEmpty()) ? main.videoUrls.get(0) : null;

        if (videoUrl == null) {
            try (Scanner scanner = new Scanner(System.in)) {
                System.out.print("🔗 Введите ссылку на YouTube-видео: ");
                videoUrl = scanner.nextLine().trim();
                if (videoUrl.isEmpty()) {
                    log.error("Ссылка не указана.");
                    System.exit(1);
                }

                if ("sub".equalsIgnoreCase(main.type)) {
                    System.out.print("🌐 Введите язык субтитров (например, en, ru): ");
                    String inputLang = scanner.nextLine().trim();
                    if (!inputLang.isEmpty()) {
                        main.lang = inputLang;
                    }
                }
            }
        }

        ProcessExecutor executor = new ProcessExecutor();
        FilenameProvider filenameProvider = new FilenameGenerator();
        
        Downloader downloader = createDownloader(main, executor);
        VideoTask task = createTask(main, downloader, filenameProvider);

        YtdlManager manager = new YtdlManager(task, downloader);
        try {
            manager.process(videoUrl, Path.of(main.outputDir));
        } catch (Exception e) {
            log.error("Process failed: {}", e.getMessage());
            System.exit(1);
        }
    }

    private static Downloader createDownloader(Main main, ProcessExecutor executor) {
        return switch (main.type.toLowerCase()) {
            case "audio" -> new AudioDownloader(executor, main.audioFormat, main.audioQuality);
            case "sub" -> new SubtitleDownloader(executor, main.lang);
            default -> new VideoDownloader(executor);
        };
    }

    private static VideoTask createTask(Main main, Downloader downloader, FilenameProvider filenameProvider) {
        return switch (main.type.toLowerCase()) {
            case "audio" -> new AudioTask(downloader, filenameProvider);
            case "video" -> new VideoDownloadTask(downloader, filenameProvider);
            case "screenshot" -> new ScreenshotTask(downloader, filenameProvider, main.interval);
            case "sub" -> {
                ContentProcessor processor = new SubtitleCleaner(300);
                yield new SubtitleTask(downloader, processor, filenameProvider);
            }
            default -> throw new IllegalArgumentException("Unknown type: " + main.type);
        };
    }
}
