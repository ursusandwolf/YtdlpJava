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

    public static void main(String[] args) {
        Main main = new Main();
        JCommander jc = JCommander.newBuilder().addObject(main).build();
        try {
            jc.parse(args);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            jc.usage();
            System.exit(1);
        }

        if (main.help) {
            jc.usage();
            return;
        }

        String videoUrl = (main.videoUrls != null && !main.videoUrls.isEmpty()) ? main.videoUrls.get(0) : null;

        if (videoUrl == null) {
            try (Scanner scanner = new Scanner(System.in)) {
                System.out.println("🎬 === YouTube Downloader & Processor ===");
                System.out.print("🔗 Введите ссылку на YouTube (видео или плейлист): ");
                videoUrl = scanner.nextLine().trim();
                if (videoUrl.isEmpty()) {
                    log.error("Ссылка не указана.");
                    System.exit(1);
                }

                System.out.println("\nВыберите тип задачи:");
                System.out.println("1. Субтитры (sub)");
                System.out.println("2. Аудио (audio)");
                System.out.println("3. Видео (video)");
                System.out.println("4. Скриншоты (screenshot)");
                System.out.println("5. Метаданные (metadata)");
                System.out.print("Введите номер (по умолчанию 1): ");
                
                String typeChoice = scanner.nextLine().trim();
                main.type = switch (typeChoice) {
                    case "2" -> "audio";
                    case "3" -> "video";
                    case "4" -> "screenshot";
                    case "5" -> "metadata";
                    default -> "sub";
                };

                if ("sub".equalsIgnoreCase(main.type)) {
                    System.out.println("\nВыберите язык субтитров:");
                    System.out.println("1. Английский (en)");
                    System.out.println("2. Русский (ru)");
                    System.out.println("3. Украинский (uk)");
                    System.out.print("Введите номер или код языка [en]: ");
                    String inputLang = scanner.nextLine().trim();
                    if (!inputLang.isEmpty()) {
                        main.lang = switch (inputLang) {
                            case "1" -> "en";
                            case "2" -> "ru";
                            case "3" -> "uk";
                            default -> inputLang;
                        };
                    }
                } else if ("audio".equalsIgnoreCase(main.type)) {
                    System.out.print("🎵 Введите формат (opus, mp3, m4a) [opus]: ");
                    String fmt = scanner.nextLine().trim();
                    if (!fmt.isEmpty()) main.audioFormat = fmt;
                }
            }
        }

        // Set specific output directories based on type if still using default or empty
        if ("output".equals(main.outputDir)) {
            main.outputDir = switch (main.type.toLowerCase()) {
                case "sub" -> "txt";
                case "audio" -> "output/audio";
                case "video" -> "output/video";
                case "screenshot" -> "output/img";
                case "metadata" -> "output/metadata";
                default -> main.outputDir;
            };
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
            case "metadata" -> new MetadataDownloader(executor);
            default -> new VideoDownloader(executor);
        };
    }

    private static VideoTask createTask(Main main, Downloader downloader, FilenameProvider filenameProvider) {
        return switch (main.type.toLowerCase()) {
            case "audio" -> new AudioTask(downloader, filenameProvider);
            case "video" -> new VideoDownloadTask(downloader, filenameProvider);
            case "screenshot" -> new ScreenshotTask(downloader, filenameProvider, main.interval);
            case "metadata" -> new MetadataTask(downloader, filenameProvider);
            case "sub" -> {
                ContentProcessor processor = new SubtitleCleaner(180);
                yield new SubtitleTask(downloader, processor, filenameProvider);
            }
            default -> throw new IllegalArgumentException("Unknown type: " + main.type);
        };
    }
}
