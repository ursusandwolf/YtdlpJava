package com.ytdlpjava.cli;

import com.ytdlpjava.config.AppConfig;
import lombok.extern.slf4j.Slf4j;
import java.util.Scanner;

@Slf4j
public class InteractivePromptService {
    public void runInteractive(AppConfig config) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("🎬 === YouTube Downloader & Processor ===");
            System.out.print("🔗 Введите ссылку на YouTube (видео или плейлист): ");
            String videoUrl = scanner.nextLine().trim();
            if (videoUrl.isEmpty()) {
                log.error("Ссылка не указана.");
                System.exit(1);
            }
            config.setVideoUrl(videoUrl);

            System.out.println("\nВыберите тип задачи:");
            System.out.println("1. Субтитры (sub)");
            System.out.println("2. Аудио (audio)");
            System.out.println("3. Видео (video)");
            System.out.println("4. Скриншоты (screenshot)");
            System.out.println("5. Метаданные (metadata)");
            System.out.print("Введите номер (по умолчанию 1): ");
            
            String typeChoice = scanner.nextLine().trim();
            config.setType(switch (typeChoice) {
                case "2" -> "audio";
                case "3" -> "video";
                case "4" -> "screenshot";
                case "5" -> "metadata";
                default -> "sub";
            });

            if ("sub".equalsIgnoreCase(config.getType())) {
                System.out.println("\nВыберите язык субтитров:");
                System.out.println("1. Английский (en)");
                System.out.println("2. Русский (ru)");
                System.out.println("3. Украинский (uk)");
                System.out.print("Введите номер или код языка [en]: ");
                String inputLang = scanner.nextLine().trim();
                if (!inputLang.isEmpty()) {
                    config.setLang(switch (inputLang) {
                        case "1" -> "en";
                        case "2" -> "ru";
                        case "3" -> "uk";
                        default -> inputLang;
                    });
                }
            } else if ("audio".equalsIgnoreCase(config.getType())) {
                System.out.print("🎵 Введите формат (opus, mp3, m4a) [opus]: ");
                String fmt = scanner.nextLine().trim();
                if (!fmt.isEmpty()) config.setAudioFormat(fmt);
            }
        }
    }
}
