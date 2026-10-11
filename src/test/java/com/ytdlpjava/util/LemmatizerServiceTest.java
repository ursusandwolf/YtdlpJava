package com.ytdlpjava.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LemmatizerServiceTest {
    @Test
    void supportsAllConfiguredLanguagesAndCannotBeReusedAfterClose() {
        LemmatizerService service = new LemmatizerService();
        try (service) {
            for (LemmatizerService.Language language : LemmatizerService.Language.values()) {
                assertFalse(service.getLemma("testing", language).isBlank());
            }
        }
        service.close();
        assertThrows(IllegalStateException.class, () -> service.getLemma("testing", LemmatizerService.Language.EN));
    }
}
