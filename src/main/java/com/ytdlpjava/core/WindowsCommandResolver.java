package com.ytdlpjava.core;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Resolves bare command names on Windows via PATH + PATHEXT (needed for .cmd/.bat). */
final class WindowsCommandResolver {
    private WindowsCommandResolver() {}

    static List<String> resolve(List<String> command) {
        return resolve(command, System.getenv("PATH"), System.getenv("PATHEXT"), isWindows());
    }

    static List<String> resolve(List<String> command, String path, String pathext, boolean windows) {
        if (command.isEmpty() || !windows) return command;
        String program = command.getFirst();
        if (program.isBlank() || hasDirectory(program) || hasExtension(program)) return command;
        Path found = findOnPath(program, path, pathext);
        if (found == null) return command;
        List<String> resolved = new ArrayList<>(command);
        resolved.set(0, found.toAbsolutePath().normalize().toString());
        return List.copyOf(resolved);
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
    }

    private static boolean hasDirectory(String program) {
        return program.contains("/") || program.contains("\\") || program.contains(":");
    }

    private static boolean hasExtension(String program) {
        int slash = Math.max(program.lastIndexOf('/'), program.lastIndexOf('\\'));
        return program.indexOf('.', slash + 1) >= 0;
    }

    private static Path findOnPath(String program, String path, String pathext) {
        if (path == null || path.isBlank()) return null;
        List<String> extensions = pathExtensions(pathext);
        for (String dir : path.split(";")) {
            if (dir.isBlank()) continue;
            Path directory = Path.of(dir.trim());
            for (String extension : extensions) {
                Path candidate = findRegularFile(directory, program, extension);
                if (candidate != null) return candidate;
            }
        }
        return null;
    }

    private static Path findRegularFile(Path directory, String program, String extension) {
        Path exact = directory.resolve(program + extension);
        if (Files.isRegularFile(exact)) return exact;
        // PATHEXT is usually uppercase (.CMD); shims are often lowercase on case-sensitive FS.
        String lowerExtension = extension.toLowerCase(Locale.ROOT);
        if (!lowerExtension.equals(extension)) {
            Path lower = directory.resolve(program + lowerExtension);
            if (Files.isRegularFile(lower)) return lower;
        }
        return null;
    }

    private static List<String> pathExtensions(String pathext) {
        if (pathext == null || pathext.isBlank()) {
            return List.of(".EXE", ".CMD", ".BAT", "");
        }
        List<String> extensions = new ArrayList<>();
        for (String raw : pathext.split(";")) {
            if (raw.isBlank()) continue;
            String extension = raw.startsWith(".") ? raw : "." + raw;
            extensions.add(extension);
        }
        extensions.add("");
        return extensions;
    }
}
