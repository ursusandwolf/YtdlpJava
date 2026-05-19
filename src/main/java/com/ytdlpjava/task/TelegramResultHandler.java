package com.ytdlpjava.task;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;

@Slf4j
@RequiredArgsConstructor
public class TelegramResultHandler implements TaskResultHandler {
    private final String botToken;
    private final String chatId;
    private final TelegramBot bot;

    public TelegramResultHandler(String botToken, String chatId) {
        this.botToken = botToken;
        this.chatId = chatId;
        this.bot = new TelegramBot(botToken);
    }

    @Override
    public void handle(String title, Path resultFile) throws Exception {
        log.info("Sending file to Telegram: {} (Chat: {})", resultFile.getFileName(), chatId);
        
        SendDocument request = new SendDocument(chatId, resultFile.toFile())
                .caption("Result for: " + title);
        
        bot.execute(request);
    }
}
