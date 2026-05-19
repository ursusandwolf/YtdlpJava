package com.ytdlpjava.task;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TelegramResultHandlerTest {

    @Mock
    private TelegramBot mockBot;

    @Test
    void shouldExecuteSendDocumentRequest() throws Exception {
        // GIVEN
        TelegramResultHandler handler = new TelegramResultHandler("token", "chatId", mockBot);
        Path tempFile = Files.createTempFile("test", ".txt");
        
        // WHEN
        handler.handle("Test Title", tempFile);

        // THEN
        // Проверяем, что бот попытался отправить запрос
        verify(mockBot).execute(any(SendDocument.class));
        
        Files.delete(tempFile);
    }
}
