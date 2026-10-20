package com.ytdlpjava.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

class ProcessExecutorTest {
    @TempDir Path tempDir;

    private List<String> command(String mode, Path pidFile) {
        return List.of(Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "-cp", System.getProperty("java.class.path"), Child.class.getName(), mode, pidFile.toString());
    }

    @Test
    void decodesUtf8RegardlessOfJvmDefaultCharset() throws Exception {
        Path result = tempDir.resolve("unicode.txt");
        new ProcessExecutor().run(List.of(Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "-Dfile.encoding=windows-1251", "-cp", System.getProperty("java.class.path"),
                Child.class.getName(), "decode-utf8", result.toString()), "failed", 10, TimeUnit.SECONDS);
        assertEquals("Все времена в немецком — Україна", Files.readString(result));
    }

    @Test
    void capturesBothOutputStreams() throws Exception {
        String output = new ProcessExecutor().run(command("output", tempDir.resolve("pid")), "failed", 5, TimeUnit.SECONDS);
        assertTrue(output.contains("stdout"));
        assertTrue(output.contains("stderr"));
    }

    @Test
    void reportsExitCodeAndDiagnosticOutput() {
        ProcessExecutionException error = assertThrows(ProcessExecutionException.class,
                () -> new ProcessExecutor().run(command("fail", tempDir.resolve("pid")), "failed", 5, TimeUnit.SECONDS));
        assertEquals(7, error.getExitCode());
        assertTrue(error.getOutput().contains("diagnostic"));
    }

    @Test
    void timesOutSilentProcessAndTerminatesIt() {
        checkTimeout("silent");
    }

    @Test
    void timesOutProcessWithUnterminatedOutputLine() {
        checkTimeout("partial");
    }

    @Test
    void terminatesDescendantsOnTimeout() {
        checkTimeout("tree");
        assertProcessDead(tempDir.resolve("pid.child"));
    }

    private void checkTimeout(String mode) {
        Path pid = tempDir.resolve("pid");
        assertTimeoutPreemptively(Duration.ofSeconds(8), () -> {
            assertThrows(ProcessTimeoutException.class,
                    () -> new ProcessExecutor().run(command(mode, pid), "failed", 1500, TimeUnit.MILLISECONDS));
            assertProcessDead(pid);
        });
    }

    private void assertProcessDead(Path pid) {
        assertTrue(Files.exists(pid), "Child process must have started");
        assertTimeoutPreemptively(Duration.ofSeconds(3), () -> {
            long id = Long.parseLong(Files.readString(pid));
            while (ProcessHandle.of(id).map(ProcessHandle::isAlive).orElse(false)) Thread.sleep(20);
        });
    }

    @Test
    void interruptionTerminatesProcess() throws Exception {
        Path pid = tempDir.resolve("pid");
        AtomicReference<Throwable> result = new AtomicReference<>();
        Thread worker = Thread.ofVirtual().start(() -> {
            try {
                new ProcessExecutor().run(command("silent", pid), "failed");
            } catch (Throwable e) {
                result.set(e);
            }
        });
        try {
            assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
                while (!Files.exists(pid)) Thread.sleep(20);
            });
            worker.interrupt();
            worker.join(3000);
            assertFalse(worker.isAlive());
            assertInstanceOf(InterruptedException.class, result.get());
            assertProcessDead(pid);
        } finally {
            worker.interrupt();
        }
    }

    public static class Child {
        public static void main(String[] args) throws Exception {
            Path pid = Path.of(args[1]);
            Files.writeString(pid, Long.toString(ProcessHandle.current().pid()));
            switch (args[0]) {
                case "utf8" -> System.out.write("Все времена в немецком — Україна".getBytes(StandardCharsets.UTF_8));
                case "decode-utf8" -> {
                    String output = new ProcessExecutor().run(List.of(
                            Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                            "-cp", System.getProperty("java.class.path"), Child.class.getName(),
                            "utf8", pid + ".child"), "failed", 5, TimeUnit.SECONDS);
                    Files.writeString(pid, output);
                }
                case "output" -> { System.out.println("stdout"); System.err.println("stderr"); }
                case "fail" -> { System.err.println("diagnostic"); System.exit(7); }
                default -> {
                    if ("partial".equals(args[0])) { System.out.print("partial"); System.out.flush(); }
                    if ("tree".equals(args[0])) {
                        new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                                "-cp", System.getProperty("java.class.path"), Child.class.getName(),
                                "silent", pid + ".child").start();
                    }
                    Thread.sleep(60000);
                }
            }
        }
    }
}
