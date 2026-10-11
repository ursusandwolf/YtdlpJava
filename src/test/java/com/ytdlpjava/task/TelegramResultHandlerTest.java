package com.ytdlpjava.task;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendDocument;
import com.pengrad.telegrambot.response.SendResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TelegramResultHandlerTest {
    @TempDir Path tempDir;

    @Test
    void acceptsSuccessfulApiResponseWithoutRemovingFile() throws Exception {
        TelegramBot bot = mock(TelegramBot.class);
        SendResponse response = mock(SendResponse.class);
        when(response.isOk()).thenReturn(true);
        when(bot.execute(any(SendDocument.class))).thenReturn(response);
        Path file = Files.writeString(tempDir.resolve("test.txt"), "content");
        new TelegramResultHandler("token", "chatId", bot).handle("title", file);
        verify(bot).execute(any(SendDocument.class));
        assertTrue(Files.exists(file));
    }

    @Test
    void reportsRejectedApiResponse() throws Exception {
        TelegramBot bot = mock(TelegramBot.class);
        SendResponse response = mock(SendResponse.class);
        when(response.isOk()).thenReturn(false);
        when(response.errorCode()).thenReturn(403);
        when(response.description()).thenReturn("Forbidden");
        when(bot.execute(any(SendDocument.class))).thenReturn(response);
        Path file = Files.writeString(tempDir.resolve("test.txt"), "content");
        IOException error = assertThrows(IOException.class,
                () -> new TelegramResultHandler("token", "chatId", bot).handle("title", file));
        assertTrue(error.getMessage().contains("403 Forbidden"));
        assertTrue(Files.exists(file));
    }

    @Test
    void reportsMissingApiResponse() throws Exception {
        Path file = Files.writeString(tempDir.resolve("test.txt"), "content");
        assertThrows(IOException.class,
                () -> new TelegramResultHandler("token", "chatId", mock(TelegramBot.class)).handle("title", file));
    }
}
