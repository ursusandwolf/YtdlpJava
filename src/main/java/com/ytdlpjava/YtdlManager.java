package com.ytdlpjava;

import lombok.RequiredArgsConstructor;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RequiredArgsConstructor
public class YtdlManager {
    private final YoutubeDownloader downloader;
    private final SubtitleCleaner cleaner;
    private final FilenameGenerator filenameGenerator;

    public void processVideo(String videoUrl, String lang, Path outputDir) {
        System.out.println("📥 Загружаем субтитры на языке: " + lang);
        Path vttPath = null;
        try {
            String title = downloader.getVideoTitle(videoUrl);
            String outputBasename = filenameGenerator.buildFilename(title, 80);
            
            vttPath = downloader.downloadSubtitles(videoUrl, lang, outputBasename);

            System.out.println("🧼 Обрабатываем файл: " + vttPath.getFileName());
            String cleanedText = cleaner.cleanVttToText(vttPath, 300);

            if (!Files.exists(outputDir)) Files.createDirectories(outputDir);
            String txtFilename = vttPath.getFileName().toString().replace(".vtt", ".txt");
            Path outputFile = outputDir.resolve(txtFilename);
            Files.writeString(outputFile, cleanedText);

            System.out.println("✅ Готово! Текст сохранён в: " + outputFile);

        } catch (Exception e) {
            System.err.println("❌ Ошибка: " + e.getMessage());
            System.exit(1);
        } finally {
            if (vttPath != null && Files.exists(vttPath)) {
                try {
                    Files.delete(vttPath);
                    System.out.println("🗑️ Временный файл «" + vttPath.getFileName() + "» удалён.");
                } catch (IOException e) {
                    System.err.println("⚠️ Не удалось удалить временный файл: " + e.getMessage());
                }
            }
        }
    }
}
