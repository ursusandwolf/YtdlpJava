package com.ytdlpjava.release;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarFile;

import static org.junit.jupiter.api.Assertions.*;

class PackagedApplicationIT {
    @TempDir Path directory;
    private final Path jar = Path.of(System.getProperty("packaged.jar")).toAbsolutePath();
    private final String javaCommand = Path.of(System.getProperty("java.home"), "bin", "java").toString();

    @Test
    void runsStandaloneJarOutsideProjectDirectory() throws Exception {
        String output = run(List.of(javaCommand, "-jar", jar.toString(), "--help"), 0);
        assertTrue(output.contains("--type"));
        try (var archive = new JarFile(jar.toFile())) {
            assertEquals("com.ytdlpjava.cli.Main", archive.getManifest().getMainAttributes().getValue("Main-Class"));
            assertNotNull(archive.getEntry("META-INF/YtdlpJava-LICENSE"));
            assertNotNull(archive.getEntry("META-INF/YtdlpJava-THIRD-PARTY-NOTICES.md"));
            assertNull(archive.getEntry("org/junit/jupiter/api/Test.class"));
        }
    }

    @Test
    void invalidArgumentsReturnStatusTwoFromJar() throws Exception {
        assertTrue(run(List.of(javaCommand, "-jar", jar.toString(), "video", "-i", "0"), 2).contains("positive"));
    }

    @Test
    void loadsAllLanguageDictionariesAndRuntimeDependenciesFromJar() throws Exception {
        Path testClasses = Path.of(PackagedResourcesProbe.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        String classpath = testClasses + java.io.File.pathSeparator + jar;
        assertTrue(run(List.of(javaCommand, "-cp", classpath, PackagedResourcesProbe.class.getName()), 0)
                .contains("PACKAGED_RESOURCES_OK"));
    }

    private String run(List<String> command, int expectedStatus) throws Exception {
        var utf8Command = new java.util.ArrayList<>(command);
        utf8Command.add(1, "-Dstdout.encoding=UTF-8");
        utf8Command.add(2, "-Dstderr.encoding=UTF-8");
        Path log = Files.createTempFile(directory, "jar-", ".log");
        Process process = new ProcessBuilder(utf8Command).directory(directory.toFile())
                .redirectErrorStream(true).redirectOutput(log.toFile()).start();
        try {
            assertTrue(process.waitFor(30, TimeUnit.SECONDS), "Packaged application timed out");
            String output = Files.readString(log, StandardCharsets.UTF_8);
            assertEquals(expectedStatus, process.exitValue(), output);
            return output;
        } finally {
            process.destroyForcibly();
        }
    }
}
