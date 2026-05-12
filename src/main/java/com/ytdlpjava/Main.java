package com.ytdlpjava;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.ytdlpjava.core.ProcessExecutor;
import com.ytdlpjava.core.YtdlManager;
import com.ytdlpjava.downloader.AudioDownloader;
import com.ytdlpjava.downloader.MetadataDownloader;
import com.ytdlpjava.downloader.SubtitleDownloader;
import com.ytdlpjava.downloader.VideoDownloader;
import com.ytdlpjava.model.ContentProcessor;
import com.ytdlpjava.model.Downloader;
import com.ytdlpjava.model.FilenameProvider;
import com.ytdlpjava.model.VideoTask;
import com.ytdlpjava.processor.SubtitleCleaner;
import com.ytdlpjava.task.AudioTask;
import com.ytdlpjava.task.MetadataTask;
import com.ytdlpjava.task.ScreenshotTask;
import com.ytdlpjava.task.SubtitleTask;
import com.ytdlpjava.task.VideoDownloadTask;
import com.ytdlpjava.ui.InteractivePromptService;
import com.ytdlpjava.util.FilenameGenerator;
import com.ytdlpjava.util.LemmatizerService;
import com.ytdlpjava.util.DictionaryLoader;
import com.ytdlpjava.processor.SubtitleParser;
import com.ytdlpjava.processor.SubtitleCleanerService;
import com.ytdlpjava.processor.KeywordExtractor;
import com.ytdlpjava.processor.KeywordHighlighter;
import com.ytdlpjava.processor.MarkdownFormatter;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
@Setter
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

    private String videoUrl;

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

        if (main.videoUrls == null || main.videoUrls.isEmpty()) {
            new InteractivePromptService().runInteractive(main);
        }

        main.configureOutputDir();

        ProcessExecutor executor = new ProcessExecutor();
        FilenameProvider filenameProvider = new FilenameGenerator();
        
        Downloader downloader = createDownloader(main, executor);
        VideoTask task = createTask(main, downloader, filenameProvider, executor);

        YtdlManager manager = new YtdlManager(task, downloader);
        try {
            if (main.videoUrls != null && !main.videoUrls.isEmpty()) {
                for (String url : main.videoUrls) {
                    manager.process(url, Path.of(main.outputDir));
                }
            } else if (main.videoUrl != null) {
                manager.process(main.videoUrl, Path.of(main.outputDir));
            }
        } catch (Exception e) {
            log.error("Process failed: {}", e.getMessage());
            System.exit(1);
        }
    }

    private void configureOutputDir() {
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

    private static Downloader createDownloader(Main main, ProcessExecutor executor) {
        return switch (main.type.toLowerCase()) {
            case "audio" -> new AudioDownloader(executor, main.audioFormat, main.audioQuality);
            case "sub" -> new SubtitleDownloader(executor, main.lang);
            case "metadata" -> new MetadataDownloader(executor);
            default -> new VideoDownloader(executor);
        };
    }

    private static VideoTask createTask(Main main, Downloader downloader, FilenameProvider filenameProvider, ProcessExecutor executor) {
        return switch (main.type.toLowerCase()) {
            case "audio" -> new AudioTask(downloader, filenameProvider);
            case "video" -> new VideoDownloadTask(downloader, filenameProvider);
            case "screenshot" -> new ScreenshotTask(downloader, filenameProvider, executor, main.interval);
            case "metadata" -> new MetadataTask(downloader, filenameProvider);
            case "sub" -> new SubtitleTask(downloader, createSubtitleProcessor(main.lang), filenameProvider);
            default -> throw new IllegalArgumentException("Unknown type: " + main.type);
        };
    }

    private static ContentProcessor createSubtitleProcessor(String lang) {
        LemmatizerService.Language language = "ru".equals(lang) ? LemmatizerService.Language.RU : LemmatizerService.Language.EN;
        
        List<String> stopWords = new ArrayList<>();
        stopWords.addAll(DictionaryLoader.load("/dictionaries/stop_words_ru.txt"));
        if ("en".equals(lang)) {
            stopWords.addAll(DictionaryLoader.load("/dictionaries/stop_words_en.txt"));
        }
        
        return new SubtitleCleaner(
                180,
                language,
                new SubtitleParser(),
                new SubtitleCleanerService(DictionaryLoader.load("/dictionaries/fillers.txt")),
                new KeywordExtractor(stopWords, new LemmatizerService(), language),
                new KeywordHighlighter(),
                new MarkdownFormatter()
        );
    }
}
