package com.ytdlpjava.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WindowsCommandResolverTest {
    @TempDir Path tempDir;

    @Test
    void resolvesCmdShimUsingProvidedPath() throws Exception {
        Path shim = tempDir.resolve("demo-tool.cmd");
        Files.writeString(shim, "@echo demo");
        List<String> resolved = WindowsCommandResolver.resolve(
                List.of("demo-tool", "--version"),
                tempDir.toString(),
                ".COM;.EXE;.BAT;.CMD",
                true);
        assertEquals(2, resolved.size());
        assertTrue(resolved.getFirst().equalsIgnoreCase(shim.toAbsolutePath().normalize().toString()));
        assertEquals("--version", resolved.get(1));
    }

    @Test
    void leavesCommandsUntouchedOffWindows() throws Exception {
        Path shim = tempDir.resolve("demo-tool.cmd");
        Files.writeString(shim, "@echo demo");
        List<String> command = List.of("demo-tool", "--version");
        assertEquals(command, WindowsCommandResolver.resolve(command, tempDir.toString(), ".CMD", false));
    }

    @Test
    void leavesQualifiedCommandsUntouched() {
        List<String> command = List.of("C:\\tools\\yt-dlp.exe", "--version");
        assertEquals(command, WindowsCommandResolver.resolve(command, tempDir.toString(), ".EXE", true));
    }
}
