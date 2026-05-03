package com.ytdlpjava;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

public class Main {
    @Parameter(description = "Ссылка на YouTube-видео")
    private List<String> videoUrls;

    @Parameter(names = {"-l", "--lang"}, description = "Язык субтитров (по умолчанию: en)")
    private String lang = "en";

    @Parameter(names = {"-o", "--output_dir"}, description = "Папка для сохранения результата")
    private String outputDir = "txt";

    public static void main(String[] args) {
        Main main = new Main();
        JCommander jc = JCommander.newBuilder().addObject(main).build();
        jc.parse(args);

        String videoUrl = (main.videoUrls != null && !main.videoUrls.isEmpty()) ? main.videoUrls.get(0) : null;
        String lang = main.lang;

        if (videoUrl == null) {
            try (Scanner scanner = new Scanner(System.in)) {
                System.out.print("🔗 Введите ссылку на YouTube-видео: ");
                videoUrl = scanner.nextLine().trim();
                if (videoUrl.isEmpty()) {
                    System.err.println("❌ Ссылка не указана.");
                    System.exit(1);
                }

                System.out.print("🌍 Укажите язык субтитров (по умолчанию: " + lang + "): ");
                String langInput = scanner.nextLine().trim().toLowerCase();
                if (!langInput.isEmpty()) lang = langInput;
            }
        }

        YtdlManager manager = new YtdlManager(new YoutubeDownloader(), new SubtitleCleaner(), new FilenameGenerator());
        manager.processVideo(videoUrl, lang, Path.of(main.outputDir));
    }
}
