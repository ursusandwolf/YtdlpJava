package com.ytdlpjava.config;

import com.ytdlpjava.core.ProcessExecutor;
import com.ytdlpjava.infrastructure.ffmpeg.FfmpegFrameExtractor;
import com.ytdlpjava.infrastructure.filesystem.FilesystemTemporaryFileManager;
import com.ytdlpjava.infrastructure.ytdlp.AudioDownloader;
import com.ytdlpjava.infrastructure.ytdlp.MetadataDownloader;
import com.ytdlpjava.infrastructure.ytdlp.SubtitleDownloader;
import com.ytdlpjava.infrastructure.ytdlp.VideoDownloader;
import com.ytdlpjava.model.ContentProcessor;
import com.ytdlpjava.model.Downloader;
import com.ytdlpjava.model.FilenameProvider;
import com.ytdlpjava.model.FrameExtractor;
import com.ytdlpjava.model.TemporaryFileManager;
import com.ytdlpjava.model.VideoTask;
import com.ytdlpjava.processor.KeywordExtractor;
import com.ytdlpjava.processor.KeywordHighlighter;
import com.ytdlpjava.processor.MarkdownFormatter;
import com.ytdlpjava.processor.SubtitleCleaner;
import com.ytdlpjava.processor.SubtitleCleanerService;
import com.ytdlpjava.processor.SubtitleParser;
import com.ytdlpjava.task.AudioTask;
import com.ytdlpjava.task.FileResultHandler;
import com.ytdlpjava.task.MetadataTask;
import com.ytdlpjava.task.ScreenshotTask;
import com.ytdlpjava.task.SubtitleTask;
import com.ytdlpjava.task.TaskResultHandler;
import com.ytdlpjava.task.VideoDownloadTask;
import com.ytdlpjava.util.DictionaryLoader;
import com.ytdlpjava.util.FilenameGenerator;
import com.ytdlpjava.util.LemmatizerService;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ApplicationFactory {
    public ProcessExecutor createProcessExecutor() {
        return new ProcessExecutor();
    }

    public FilenameProvider createFilenameProvider() {
        return new FilenameGenerator();
    }

    public TemporaryFileManager createTemporaryFileManager() {
        return new FilesystemTemporaryFileManager();
    }

    public FrameExtractor createFrameExtractor(ProcessExecutor executor) {
        return new FfmpegFrameExtractor(executor);
    }

    public Downloader createDownloader(AppConfig config, ProcessExecutor executor) {
        return switch (config.getType().toLowerCase()) {
            case "audio" -> new AudioDownloader(executor, config.getAudioFormat(), config.getAudioQuality());
            case "sub" -> new SubtitleDownloader(executor, config.getLang());
            case "metadata" -> new MetadataDownloader(executor);
            default -> new VideoDownloader(executor);
        };
    }

    public VideoTask createTask(AppConfig config, Downloader downloader, FilenameProvider filenameProvider, ProcessExecutor executor) {
        List<TaskResultHandler> handlers = List.of(new FileResultHandler(Path.of(config.getOutputDir())));
        TemporaryFileManager temporaryFileManager = createTemporaryFileManager();
        FrameExtractor frameExtractor = createFrameExtractor(executor);

        return switch (config.getType().toLowerCase()) {
            case "audio" -> new AudioTask(downloader, downloader, filenameProvider, temporaryFileManager, handlers);
            case "video" -> new VideoDownloadTask(downloader, downloader, filenameProvider, temporaryFileManager, handlers);
            case "screenshot" -> new ScreenshotTask(downloader, downloader, downloader, filenameProvider, frameExtractor, temporaryFileManager, config.getInterval(), handlers);
            case "metadata" -> new MetadataTask(downloader, downloader, filenameProvider, temporaryFileManager, handlers);
            case "sub" -> new SubtitleTask(downloader, downloader, createSubtitleProcessor(config.getLang()), filenameProvider, temporaryFileManager, handlers);
            default -> throw new IllegalArgumentException("Unknown type: " + config.getType());
        };
    }

    private ContentProcessor createSubtitleProcessor(String lang) {
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
